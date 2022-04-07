// Copyright 2000-2022 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.codeInsight.completion.kind.state;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Supplier;

public class LazyValue<T> implements Supplier<T> {
  private final @NotNull Supplier<? extends T> myInitializer;
  @Nullable private T myValue = null;

  public LazyValue(@NotNull Supplier<? extends T> initializer) { myInitializer = initializer; }

  @Override
  public @NotNull T get() {
    if (myValue == null) {
      myValue = myInitializer.get();
    }
    return myValue;
  }
}
