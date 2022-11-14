// Copyright 2000-2022 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.codeInsight.completion.kind;

import com.intellij.codeInsight.completion.CompletionParameters;
import com.intellij.codeInsight.completion.kind.state.LazyNullableValue;
import com.intellij.codeInsight.completion.kind.state.LazyValue;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Supplier;

public abstract class LazyKindsExecutor implements CompletionKindsExecutor {
  private volatile boolean myStartedExecution = false;
  private final Lock myExecutionLock = new ReentrantLock();

  @Override
  public final void executeAll(CompletionParameters parameters) {
    myExecutionLock.lock();
    try {
      if (myStartedExecution) {
        return;
      }
      myStartedExecution = true;
    }
    finally {
      myExecutionLock.unlock();
    }
    executeAllOnce();
  }

  protected abstract void executeAllOnce();

  @Override
  public <T> @NotNull LazyValue<T> wrapNotNullSupplier(@NotNull Supplier<T> supplier) {
    return new LazyValue<>(supplier);
  }

  @Override
  public <T> @NotNull LazyNullableValue<T> wrapNullableSupplier(@NotNull Supplier<T> supplier) {
    return new LazyNullableValue<>(supplier);
  }
}
