// Copyright 2000-2022 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.codeInsight.completion.kind

import com.intellij.codeInsight.completion.CompletionSession
import com.intellij.codeInsight.completion.kind.CompletionKind
import com.intellij.codeInsight.completion.kind.state.Flag
import com.intellij.codeInsight.completion.kind.state.ActorsAwaitingAnd
import com.intellij.codeInsight.completion.kind.state.ActorsAwaitingOr
import com.intellij.codeInsight.completion.kind.state.LazyNullableValue
import com.intellij.codeInsight.completion.kind.state.LazyValue
import java.util.function.Supplier

abstract class CompletionKindsExecutorWithSorter(
  private val myCompletionKinds: MutableList<Pair<CompletionKind, CompletionSession>> = ArrayList()
) : CompletionKindsExecutor {

  override fun addKind(kind: CompletionKind, session: CompletionSession) {
    myCompletionKinds.add(kind to session)
  }

  abstract val sorter: CompletionKindsRelevanceSorter

  override fun executeAll() = executeAll {}

  override fun <T> wrapNotNullSupplier(supplier: Supplier<T>) = LazyValue(supplier)

  override fun makeFlagAnd(init: Boolean): Flag = ActorsAwaitingAnd(init)

  override fun <T> wrapNullableSupplier(supplier: Supplier<T?>) = LazyNullableValue(supplier)

  override fun makeFlagOr(init: Boolean): Flag = ActorsAwaitingOr(init)

  fun executeAll(taskAfterPrimary: Runnable) {
    val executionOrder = sorter.sort(myCompletionKinds)

    executionOrder.primaryBatch.forEach { it.first.fillKindVariantsOnce(it.second) }
    taskAfterPrimary.run()
    executionOrder.secondaryBatch.filter { it.first.isApplicable() }.forEach { it.first.fillKindVariantsOnce(it.second) }
  }
}
