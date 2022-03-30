// Copyright 2000-2022 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.codeInsight.completion.kind

import com.intellij.codeInsight.completion.kind.state.Flag
import com.intellij.codeInsight.completion.kind.state.LazyNullableValue
import com.intellij.codeInsight.completion.kind.state.LazyValue

interface CompletionKindsExecutor {
  fun addKind(kind: CompletionKind)

  fun executeAll()

  fun <T> wrapNotNullSupplier(supplier: () -> T): LazyValue<T>;

  fun <T> wrapNullableSupplier(supplier: () -> T?): LazyNullableValue<T>;

  fun makeFlagOr(init: Boolean): Flag

  fun makeFlagOnceReassignable(init: Boolean): Flag
}
