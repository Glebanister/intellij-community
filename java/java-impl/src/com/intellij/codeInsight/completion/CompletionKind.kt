// Copyright 2000-2022 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.codeInsight.completion

import java.lang.IllegalArgumentException
import java.util.Comparator
import java.util.function.Consumer
import kotlin.streams.toList


abstract class CompletionKind(val name: String) {
  private var alreadyFilled = false
  protected abstract fun fillKindVariants()
  abstract val isApplicable: Boolean

  fun fillKindVariantsOnce() {
    //checkNotNull(context) { "CompletionKindContext is not set yet" }
    if (alreadyFilled) {
      return
    }
    fillKindVariants()
    alreadyFilled = true
  }

  companion object {
    @JvmStatic
    fun withDynamicCompletionDecision(
      name: String,
      isKindApplicable: () -> Boolean,
      doFillVariants: Runnable,
    ): CompletionKind {

      class CompletionKindWithDynamicCompletionDecision : CompletionKind(name) {
        override fun fillKindVariants() = doFillVariants.run()
        override val isApplicable = isKindApplicable()
      }

      return CompletionKindWithDynamicCompletionDecision()
    }

    @JvmStatic
    fun withStaticCompletionDecision(
      name: String,
      isKindApplicable: Boolean,
      doFillVariants: Runnable,
    ): CompletionKind = withDynamicCompletionDecision(name, { isKindApplicable }, doFillVariants)

    @JvmStatic
    fun withFillFunction(
      name: String,
      doFillVariants: Runnable,
    ): CompletionKind = withStaticCompletionDecision(name, true, doFillVariants)
  }
}

class CompletionKindsExecutionDecision(val primaryBatch: List<CompletionKind>,
                                       val secondaryBatch: List<CompletionKind>) {

  companion object {
    private fun <T> fromWeights(
      kindWeights: Collection<Map.Entry<CompletionKind, Double>>,
      primaryBatchSize: Int
    ): CompletionKindsExecutionDecision {

      val order: List<CompletionKind> = kindWeights.stream()
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

interface CompletionKindsRelevanceSorter {
  fun sort(kinds: List<CompletionKind>): CompletionKindsExecutionDecision
}

class GivenOrderSorter : CompletionKindsRelevanceSorter {
  override fun sort(kinds: List<CompletionKind>) = CompletionKindsExecutionDecision(
    kinds,
    emptyList(),
  )
}

interface CompletionKindsExecutor {
  fun addKind(kind: CompletionKind);
  fun executeAll(taskAfterPrimaryBatch: Runnable?);
}

abstract class CompletionKindsExecutorWithSorter(
  private val myCompletionKinds: MutableList<CompletionKind> = ArrayList()
) : CompletionKindsExecutor {

  override fun addKind(kind: CompletionKind) {
    myCompletionKinds.add(kind)
  }

  abstract val sorter: CompletionKindsRelevanceSorter

  override fun executeAll(taskAfterPrimaryBatch: Runnable?) {
    val executionOrder = sorter.sort(
      myCompletionKinds.stream()
        .filter(CompletionKind::isApplicable)
        .toList()
    )
    executionOrder.primaryBatch.forEach { it.fillKindVariantsOnce() }
    taskAfterPrimaryBatch?.run()
    executionOrder.secondaryBatch.forEach { it.fillKindVariantsOnce() }
  }
}

class CompletionKindsGivenOrderExecutor(
  override val sorter: CompletionKindsRelevanceSorter = GivenOrderSorter()
) : CompletionKindsExecutorWithSorter()

class CompletionKindsImmediateExecutor : CompletionKindsExecutor {
  override fun addKind(kind: CompletionKind) {
    if (kind.isApplicable) kind.fillKindVariantsOnce()
  }

  override fun executeAll(taskAfterPrimaryBatch: Runnable?) {
    if (taskAfterPrimaryBatch != null)
      throw IllegalArgumentException("All tasks already were executed")
  }
}
