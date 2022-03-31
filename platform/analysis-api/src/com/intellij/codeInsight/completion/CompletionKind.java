// Copyright 2000-2022 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.codeInsight.completion;

public abstract class CompletionKind {
  private final String name;
  private boolean alreadyFilled = false;

  protected CompletionKind(String name) { this.name = name; }

  protected abstract void fillKindVariants();

  public abstract boolean isApplicable();

  public String getName() { return name; }

  public void fillKindVariantsOnce() {
    if (alreadyFilled) {
      return;
    }
    fillKindVariants();
    alreadyFilled = true;
  }
}
