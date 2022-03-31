// Copyright 2000-2022 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.codeInsight.completion.kind

import com.intellij.codeInsight.completion.CompletionKind
import com.intellij.codeInsight.completion.kind.state.Flag
import com.intellij.codeInsight.completion.kind.state.ActorsAwaitingAnd
import com.intellij.codeInsight.completion.kind.state.ActorsAwaitingOr
import com.intellij.codeInsight.completion.kind.state.LazyNullableValue
import com.intellij.codeInsight.completion.kind.state.LazyValue

abstract class CompletionKindsExecutorWithSorter(
  private val myCompletionKinds: MutableList<CompletionKind> = ArrayList()
) : CompletionKindsExecutor {

  override fun addKind(kind: CompletionKind) {
    myCompletionKinds.add(kind)
  }

  abstract val sorter: CompletionKindsRelevanceSorter

  override fun executeAll() = executeAll {}

  override fun <T> wrapNotNullSupplier(supplier: () -> T) = LazyValue(supplier)

  override fun makeFlagAnd(init: Boolean): Flag = ActorsAwaitingAnd(init)

  override fun <T> wrapNullableSupplier(supplier: () -> T?) = LazyNullableValue(supplier)

  override fun makeFlagOr(init: Boolean): Flag = ActorsAwaitingOr(init)

  fun executeAll(taskAfterPrimary: Runnable) {
    val executionOrder = sorter.sort(myCompletionKinds)

    executionOrder.primaryBatch.forEach { it.fillKindVariantsOnce() }
    taskAfterPrimary.run()
    executionOrder.secondaryBatch.filter { it.isApplicable() }.forEach { it.fillKindVariantsOnce() }
  }
}
