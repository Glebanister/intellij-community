// Copyright 2000-2022 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.codeInsight.completion.kind;

import com.intellij.codeInsight.completion.CompletionSession;
import com.intellij.codeInsight.completion.kind.state.Flag;
import com.intellij.codeInsight.completion.kind.state.LazyNullableValue;
import com.intellij.codeInsight.completion.kind.state.LazyValue;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Supplier;

public interface CompletionKindsExecutor {
  void addKind(@NotNull CompletionKind kind, @NotNull CompletionSession session);

  void executeAll();

  <T> @NotNull LazyValue<T> wrapNotNullSupplier(@NotNull Supplier<T> supplier);

  <T> @NotNull LazyNullableValue<T> wrapNullableSupplier(@NotNull Supplier<@Nullable T> supplier);

  Flag makeConstFlag(boolean init);

  Flag makeFlagOr(boolean init);

  Flag makeFlagOnceReassignable(boolean init);

  Flag makeFlagAnd(boolean init);
}
