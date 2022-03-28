// Copyright 2000-2022 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.codeInsight.completion;

import com.intellij.codeInsight.completion.kind.CompletionKindsExecutor;
import org.jetbrains.annotations.NotNull;

public abstract class CompletionContributorWithKinds extends CompletionContributor {
  @Override
  public void fillCompletionVariants(@NotNull final CompletionParameters parameters, @NotNull CompletionResultSet result) {
    throw new IllegalStateException(
      String.format("%s supports kinds, fillCompletionVariantsWithKinds must be called", this.getClass().getSimpleName()));
  }

  public abstract void fillCompletionKinds(@NotNull final CompletionParameters parameters,
                                           @NotNull CompletionResultSet result,
                                           CompletionKindsExecutor ckExecutor);
}
