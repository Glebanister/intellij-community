// Copyright 2000-2022 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.codeInsight.completion.kind

import com.intellij.codeInsight.completion.CompletionParameters
import com.intellij.codeInsight.completion.CompletionSession
import com.intellij.codeInsight.completion.kind.state.*
import java.util.function.Supplier

class CompletionKindsImmediateExecutor : CompletionKindsExecutor {
  override fun whenLookupReady(doShowLookup: Runnable) {
  }

  override fun addKind(kind: CompletionKind, session: CompletionSession) {
    if (kind.isApplicable) kind.fillKindVariantsOnce(session, null)
  }

  override fun executeAll(parameters: CompletionParameters) {}

  override fun <T> wrapNotNullSupplier(supplier: Supplier<T>) = ImmediatelyGettingSupplier { supplier.get() }

  override fun <T> wrapNullableSupplier(supplier: Supplier<T?>) = ImmediatelyGettingNullableSupplier { supplier.get() }

  override fun makeConstFlag(init: Boolean) = ConstFlag(init)

  override fun makeFlagOr(init: Boolean): Flag = LatestValueTakingFlag(init)

  override fun makeFlagOnceReassignable(init: Boolean): Flag = LatestValueTakingFlag(init)

  override fun makeFlagAnd(init: Boolean): Flag = LatestValueTakingFlag(init)
}
