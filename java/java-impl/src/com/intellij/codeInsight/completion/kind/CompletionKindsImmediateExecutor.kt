// Copyright 2000-2022 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.codeInsight.completion.kind

import com.intellij.codeInsight.completion.CompletionKind
import com.intellij.codeInsight.completion.kind.state.Flag
import com.intellij.codeInsight.completion.kind.state.ImmediatelyGettingNullableSupplier
import com.intellij.codeInsight.completion.kind.state.LatestValueTakingFlag
import com.intellij.codeInsight.completion.kind.state.ImmediatelyGettingSupplier

class CompletionKindsImmediateExecutor(
  override val session: CompletionKindInsertingSession
) : CompletionKindsExecutor, CurrentKindInformer {

  override fun addKind(kind: CompletionKind) {
    if (kind.isApplicable()) fillKindOnceInformingSession(kind)
  }

  override fun executeAll() {}

  override fun <T> wrapNotNullSupplier(supplier: () -> T) = ImmediatelyGettingSupplier(supplier)

  override fun <T> wrapNullableSupplier(supplier: () -> T?) = ImmediatelyGettingNullableSupplier(supplier)

  override fun makeFlagOr(init: Boolean): Flag = LatestValueTakingFlag(init)

  override fun makeFlagOnceReassignable(init: Boolean): Flag = LatestValueTakingFlag(init)

  override fun makeFlagAnd(init: Boolean): Flag = LatestValueTakingFlag(init)
}
