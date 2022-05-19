// Copyright 2000-2022 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.completion.ml.features

import com.intellij.codeInsight.completion.CompletionParameters
import com.intellij.codeInsight.completion.CompletionSession
import com.intellij.codeInsight.completion.kind.CompletionKind
import com.intellij.codeInsight.completion.kind.CompletionKindsExecutionDecision
import com.intellij.codeInsight.completion.kind.CompletionKindsRelevanceSorter
import com.intellij.codeInsight.completion.ml.ElementFeatureProvider
import com.intellij.codeInsight.completion.ml.MLFeatureValue
import com.intellij.completion.ml.storage.LookupStorage

//class MLKindsRelevanceSorter : CompletionKindsRelevanceSorter {
//  override fun sort(kinds: List<Pair<CompletionKind, CompletionSession>>, parameters: CompletionParameters): CompletionKindsExecutionDecision {
//    val storage: LookupStorage = LookupStorage.get(parameters) ?: return CompletionKindsExecutionDecision.keepGivenOrder(kinds)
//    val result = mutableMapOf<String, MLFeatureValue>()
//
//
//
//    //return if (result.isEmpty()) MLCompletionWeigher.DummyComparable.EMPTY else MLCompletionWeigher.DummyComparable(result)
//  }
//}
