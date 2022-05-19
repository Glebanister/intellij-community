// Copyright 2000-2022 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.codeInsight.completion.kind

import com.intellij.codeInsight.completion.CompletionParameters
import com.intellij.codeInsight.completion.CompletionSession

interface CompletionKindsRelevanceSorter {
  fun sort(kinds: List<Pair<CompletionKind, CompletionSession>>, parameters: CompletionParameters): CompletionKindsExecutionDecision
}

class GivenOrderSorter : CompletionKindsRelevanceSorter {
  override fun sort(kinds: List<Pair<CompletionKind, CompletionSession>>, parameters: CompletionParameters) = CompletionKindsExecutionDecision(
    kinds,
    emptyList(),
  )
}
