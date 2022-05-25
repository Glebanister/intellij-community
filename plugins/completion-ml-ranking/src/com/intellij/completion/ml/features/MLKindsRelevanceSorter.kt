// Copyright 2000-2022 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.completion.ml.features

import com.intellij.codeInsight.completion.CompletionParameters
import com.intellij.codeInsight.completion.CompletionSession
import com.intellij.codeInsight.completion.kind.CompletionKind
import com.intellij.codeInsight.completion.kind.CompletionKindsExecutionDecision
import com.intellij.codeInsight.completion.kind.CompletionKindsRelevanceSorter
import com.intellij.codeInsight.lookup.LookupManager
import com.intellij.codeInsight.lookup.impl.LookupImpl
import com.intellij.completion.ml.ranker.local.catboost.LocalCatBoostModelProvider
import com.intellij.completion.ml.sorting.ContextFeaturesContributor
import com.intellij.completion.ml.storage.MutableLookupStorage
import java.lang.IllegalStateException
import java.util.zip.ZipFile


class MLKindsRelevanceSorter : CompletionKindsRelevanceSorter {
  override fun sort(kinds: List<Pair<CompletionKind, CompletionSession>>,
                    parameters: CompletionParameters): CompletionKindsExecutionDecision {

    val lookup = LookupManager.getActiveLookup(parameters.editor) as? LookupImpl ?: throw IllegalStateException(
      "Lookup was not obtained yet")
    val storage = MutableLookupStorage.get(lookup) ?: throw IllegalArgumentException("MutableLookupStorage does not exist")

    if (!storage.isContextFactorsInitialized()) {
      ContextFeaturesContributor().calculateContextFactors(
        lookup,
        parameters,
        storage
      )
    }

    val contextFeatures = computeAdditionalKindOrderingFeatures(storage.contextFactors)
    val kindCheckFeatures = model.featuresOrder.drop(contextFeatures.size).map { it.featureName }
    val commonFeaturesValues = model.featuresOrder.dropLast(kindCheckFeatures.size).map {
      contextFeatures[it.featureName] ?: throw IllegalArgumentException("Feature not found: ${it.featureName}")
    }

    fun CompletionKind.featureName() = "is_${name}"

    fun CompletionKind.computeProba(): Double {
      return model.predict((commonFeaturesValues + kindCheckFeatures.map {
        if (it == this.featureName()) 1.0
        else 0.0
      }).toDoubleArray())
    }

    val kindsCandidates = kinds.filter { (kind, _) -> kind.featureName() in kindCheckFeatures }

    if (kindsCandidates.isEmpty()) return CompletionKindsExecutionDecision.keepGivenOrder(kinds)

    return CompletionKindsExecutionDecision.fromWeights(
      kindsCandidates
        .map { (kind, session) -> (kind to session) to kind.computeProba() },
      1
    )
  }

  companion object {
    val model = LocalCatBoostModelProvider()
      .loadModel(ZipFile("/Users/glebmarin/projects/ml-completion-performance/model-java/local_model.zip"))
      .decisionFunction
  }
}
