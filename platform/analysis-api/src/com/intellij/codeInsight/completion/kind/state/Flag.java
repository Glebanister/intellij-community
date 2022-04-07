// Copyright 2000-2022 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.codeInsight.completion.kind.state;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class Flag {

  @Nullable
  protected abstract Boolean value();

  public void assignOr(@NotNull Object actor, boolean value) {
    throw new UnsupportedOperationException();
  }

  public void assignAnd(@NotNull Object actor, boolean vlaue) {
    throw new UnsupportedOperationException();
  }

  public void assign(@NotNull Object actor, boolean value) {
    throw new UnsupportedOperationException();
  }

  public void registerActor(@NotNull Object actor) {
    throw new UnsupportedOperationException();
  }

  public boolean isTrue() {
    return Boolean.TRUE.equals(value());
  }

  public boolean isFalse() {
    return Boolean.FALSE.equals(value());
  }

  public boolean isUndefined() {
    return value() == null;
  }
}
