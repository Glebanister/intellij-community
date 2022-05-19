// Copyright 2000-2022 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.codeInsight.completion.kind.ml

import com.intellij.codeInsight.completion.CompletionParameters
import com.intellij.codeInsight.completion.CompletionSession
import com.intellij.codeInsight.completion.kind.CompletionKind
import com.intellij.codeInsight.completion.kind.CompletionKindsExecutionDecision
import com.intellij.codeInsight.completion.kind.CompletionKindsRelevanceSorter
import com.intellij.internal.ml.catboost.CatBoostResourcesModelMetadataReader

//class MlKindsRelevanceSorter : CompletionKindsRelevanceSorter, CatBoostResourcesModelMetadataReader {
//  override fun sort(kinds: List<Pair<CompletionKind, CompletionSession>>, parameters: CompletionParameters): CompletionKindsExecutionDecision {
//    return CompletionKindsExecutionDecision.fromWeights()
//  }
//}
