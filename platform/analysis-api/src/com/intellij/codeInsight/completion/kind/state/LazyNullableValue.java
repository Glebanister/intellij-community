// Copyright 2000-2022 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.codeInsight.completion.kind.state;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Supplier;

public class LazyNullableValue<T> implements Supplier<T> {
  private final @NotNull Supplier<? extends T> myInitializer;
  private boolean myIsCalculated = false;
  @Nullable private T myValue = null;

  public LazyNullableValue(@NotNull Supplier<? extends T> initializer) { myInitializer = initializer; }

  @Override
  public @Nullable T get() {
    if (!myIsCalculated) {
      myValue = myInitializer.get();
      myIsCalculated = true;
    }
    return myValue;
  }
}
