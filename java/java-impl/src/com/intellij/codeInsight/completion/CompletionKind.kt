// Copyright 2000-2022 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.codeInsight.completion

import java.util.Comparator
import kotlin.streams.toList


abstract class CompletionKind<T>(val name: String) {
  var myContext: T? = null
  var myAlreadyFilled = false
  protected abstract fun fillKindVariants()
  abstract val isApplicable: Boolean

  fun fillKindVariantsOnce() {
    checkNotNull(myContext) { "CompletionKindContext is not set yet" }
    if (myAlreadyFilled) {
      return
    }
    fillKindVariants()
    myAlreadyFilled = true
  }

  fun withContext(context: T) {
    myContext = context
  }
}

class CompletionKindsExecutionDecision<T>(val primaryBatch: List<CompletionKind<T>>,
                                          val secondaryBatch: List<CompletionKind<T>>) {

  companion object {
    private fun <T> fromWeights(
      kindWeights: Collection<Map.Entry<CompletionKind<T>, Double>>,
      primaryBatchSize: Int
    ): CompletionKindsExecutionDecision<T> {

      val order: List<CompletionKind<T>> = kindWeights.stream()
        .filter { (kind, _) -> kind.isApplicable }
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

interface CompletionKindsRelevanceSorter<T> {
  fun sort(kinds: List<CompletionKind<T>>): CompletionKindsExecutionDecision<T>
}

abstract class CompletionKindsExecutor<T>(
  private val myContext: T,
  private val myCompletionKinds: MutableList<CompletionKind<T>> = ArrayList()
) {

  fun addKind(kind: CompletionKind<T>): CompletionKindsExecutor<T> {
    kind.withContext(myContext)
    myCompletionKinds.add(kind)
    return this
  }

  abstract val sorter: CompletionKindsRelevanceSorter<T>

  fun executeAll(taskAfterPrimaryBatch: Runnable? = null) {
    val executionOrder = sorter.sort(
      myCompletionKinds.stream()
        .filter(CompletionKind<T>::isApplicable)
        .toList()
    )
    executionOrder.primaryBatch.forEach { it.fillKindVariantsOnce() }
    taskAfterPrimaryBatch?.run()
    executionOrder.secondaryBatch.forEach { it.fillKindVariantsOnce() }
  }
}

class GivenOrderSorter<T> : CompletionKindsRelevanceSorter<T> {
  override fun sort(kinds: List<CompletionKind<T>>) = CompletionKindsExecutionDecision(
    kinds,
    emptyList(),
  )
}

class CompletionKindsGivenOrderExecutor<T>(
  myContext: T,
  override val sorter: CompletionKindsRelevanceSorter<T> = GivenOrderSorter()
) : CompletionKindsExecutor<T>(myContext)
