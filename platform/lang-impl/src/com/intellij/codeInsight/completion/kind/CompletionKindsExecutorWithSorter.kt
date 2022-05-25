// Copyright 2000-2022 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.codeInsight.completion.kind

import com.intellij.codeInsight.completion.CompletionParameters
import com.intellij.codeInsight.completion.CompletionSession
import com.intellij.codeInsight.completion.kind.state.*
import com.intellij.ui.JBColor
import java.lang.IllegalStateException
import java.util.function.Supplier

abstract class CompletionKindsExecutorWithSorter(
  private var doShowLookup: Runnable? = null,
  private val myCompletionKinds: MutableList<Pair<CompletionKind, CompletionSession>> = ArrayList()
) : CompletionKindsExecutor {

  override fun addKind(kind: CompletionKind, session: CompletionSession) {
    myCompletionKinds.add(kind to session)
  }

  override fun whenLookupReady(doShowLookup: Runnable) {
    this.doShowLookup = doShowLookup
  }

  abstract val sorter: CompletionKindsRelevanceSorter

  override fun executeAll(parameters: CompletionParameters) {
    val executionOrder = sorter.sort(myCompletionKinds, parameters)

    val sessions = mutableSetOf<CompletionSession>()
    executionOrder.primaryBatch.filter { it.first.isApplicable }.forEach { (kind, session) ->
      kind.fillKindVariantsOnce(session, JBColor.GREEN)
      sessions.add(session)
    }

    sessions.forEach { it.flushBatchItems() }
    doShowLookup?.run() ?: throw IllegalStateException("Show lookup activity is not set")
    executionOrder.secondaryBatch.filter { it.first.isApplicable }.forEach {
      it.first.fillKindVariantsOnce(it.second, JBColor.YELLOW)
      it.second.flushBatchItems()
    }
  }

  override fun <T> wrapNotNullSupplier(supplier: Supplier<T>) = LazyValue(supplier)

  override fun makeFlagAnd(init: Boolean): Flag = ActorsAwaitingAnd(init)

  override fun <T> wrapNullableSupplier(supplier: Supplier<T?>) = LazyNullableValue(supplier)

  override fun makeFlagOr(init: Boolean): Flag = ActorsAwaitingOr(init)

  override fun makeConstFlag(init: Boolean) = ConstFlag(init)

  override fun makeFlagOnceReassignable(init: Boolean) = LatestValueTakingFlag(init)
}
