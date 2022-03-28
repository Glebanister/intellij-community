// Copyright 2000-2022 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.codeInsight.completion;

import org.jetbrains.annotations.NotNull;

public abstract class CompletionEarlyLookupContributor extends CompletionContributor {
  @Override
  public void fillCompletionVariants(@NotNull final CompletionParameters parameters, @NotNull CompletionResultSet result) {
    fillCompletionVariantsWithEarlyLookup(parameters, result, () -> {
    });
  }

  public abstract void fillCompletionVariantsWithEarlyLookup(@NotNull final CompletionParameters parameters,
                                                             @NotNull CompletionResultSet result,
                                                             Runnable showLookupEarly);
}
