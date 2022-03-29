// Copyright 2000-2022 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.codeInsight.completion.kind

import com.intellij.codeInsight.completion.kind.state.BooleanLike
import com.intellij.codeInsight.completion.kind.state.BooleanWrap
import com.intellij.codeInsight.completion.kind.state.ImmediatelyGettingSupplier
import java.util.function.Supplier

class CompletionKindsImmediateExecutor : CompletionKindsExecutor {
  override fun addKind(kind: CompletionKind) {
    if (kind.isApplicable) kind.fillKindVariantsOnce()
  }

  override fun executeAll() {}

  override fun <T> wrapSupplier(supplier: Supplier<T>): Supplier<T> = ImmediatelyGettingSupplier(supplier)

  override fun makeFlagOr(init: Boolean): BooleanLike = BooleanWrap(init)
}
