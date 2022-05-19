// Copyright 2000-2022 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.codeInsight.completion.kind;

import com.intellij.codeInsight.completion.CompletionContributor;
import com.intellij.codeInsight.completion.CompletionContributorEP;
import com.intellij.codeInsight.completion.CompletionParameters;
import com.intellij.codeInsight.completion.CompletionSession;
import com.intellij.codeInsight.completion.kind.state.Flag;
import com.intellij.codeInsight.completion.kind.state.LazyNullableValue;
import com.intellij.codeInsight.completion.kind.state.LazyValue;
import com.intellij.openapi.extensions.ExtensionPointName;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public interface CompletionKindsExecutor {
  ExtensionPointName<CompletionKindsExecutorEP> EP = new ExtensionPointName<>("com.intellij.completion.kind.executor");

  static CompletionKindsExecutor getInstance() {
    int size = EP.getExtensionList().size();
    if (size == 0) {
      throw new IllegalStateException("No CompletionKindsExecutor was defined");
    }
    if (size > 1) {
      throw new IllegalStateException("Found more than one CompletionKindsExecutor:" + EP.getExtensionList().stream().map((e) -> {
          return e.getInstance().getClass().getName();
        })
        .collect(Collectors.joining(", "))
      );
    }
    return EP.getExtensionList().get(0).getInstance();
  }

  void addKind(@NotNull CompletionKind kind, @NotNull CompletionSession session);

  void executeAll(CompletionParameters parameters);

  <T> @NotNull LazyValue<T> wrapNotNullSupplier(@NotNull Supplier<T> supplier);

  <T> @NotNull LazyNullableValue<T> wrapNullableSupplier(@NotNull Supplier<@Nullable T> supplier);

  Flag makeConstFlag(boolean init);

  Flag makeFlagOr(boolean init);

  Flag makeFlagOnceReassignable(boolean init);

  Flag makeFlagAnd(boolean init);

  default boolean sureFoundCorrect() {
    return false;
  }

  default @NotNull List<CompletionContributor> reorderContirbutors(@NotNull List<CompletionContributor> contributors) {
    return contributors;
  }
}
