// Copyright 2000-2022 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.codeInsight.completion.kind

import com.intellij.codeInsight.completion.LazyNotNullValue
import com.intellij.codeInsight.completion.kind.state.BooleanLike
import com.intellij.codeInsight.completion.kind.state.DeferredOr
import java.util.function.Supplier
import kotlin.streams.toList

abstract class CompletionKindsExecutorWithSorter(
  private val myCompletionKinds: MutableList<CompletionKind> = ArrayList()
) : CompletionKindsExecutor {

  override fun addKind(kind: CompletionKind) {
    myCompletionKinds.add(kind)
  }

  abstract val sorter: CompletionKindsRelevanceSorter

  override fun executeAll() = executeAll {}

  override fun <T> wrapSupplier(supplier: Supplier<T>): Supplier<T> = LazyNotNullValue<T>(supplier)

  override fun makeFlagOr(init: Boolean): BooleanLike = DeferredOr(init)

  fun executeAll(taskAfterPrimary: Runnable) {
    val executionOrder = sorter.sort(
      myCompletionKinds.stream()
        .filter(CompletionKind::isApplicable)
        .toList()
    )
    executionOrder.primaryBatch.forEach { it.fillKindVariantsOnce() }
    taskAfterPrimary.run()
    executionOrder.secondaryBatch.forEach { it.fillKindVariantsOnce() }
  }
}
