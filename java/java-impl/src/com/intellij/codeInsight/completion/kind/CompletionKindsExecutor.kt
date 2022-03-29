// Copyright 2000-2022 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.codeInsight.completion.kind

import com.intellij.codeInsight.completion.kind.state.Flag
import java.util.function.Supplier

interface CompletionKindsExecutor {
  fun addKind(kind: CompletionKind)

  fun executeAll()

  fun <T> wrapSupplier(supplier: Supplier<T>): Supplier<T>

  fun makeFlagOr(init: Boolean): Flag
}
