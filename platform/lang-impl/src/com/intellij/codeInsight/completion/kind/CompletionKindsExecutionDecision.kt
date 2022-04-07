// Copyright 2000-2022 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.codeInsight.completion.kind

import com.intellij.codeInsight.completion.CompletionSession
import com.intellij.codeInsight.completion.kind.CompletionKind
import java.util.Comparator
import kotlin.streams.toList

class CompletionKindsExecutionDecision(val primaryBatch: List<Pair<CompletionKind, CompletionSession>>,
                                       val secondaryBatch: List<Pair<CompletionKind, CompletionSession>>) {
  companion object {
    private fun <T> fromWeights(
      kindWeights: Collection<Map.Entry<Pair<CompletionKind, CompletionSession>, Double>>,
      primaryBatchSize: Int
    ): CompletionKindsExecutionDecision {

      val order: List<Pair<CompletionKind, CompletionSession>> = kindWeights.stream()
        .sorted(Comparator.comparing { (_, weight) -> weight })
        .map { it.component1() }
        .toList();
      return CompletionKindsExecutionDecision(
        order.subList(0, primaryBatchSize),
        order.subList(primaryBatchSize, order.size)
      )
    }
  }
}
