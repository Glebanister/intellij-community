// Copyright 2000-2022 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.codeInsight.completion.kind;

import com.intellij.codeInsight.completion.CompletionSession;
import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.openapi.util.Key;

public abstract class CompletionKind {
  public static final Key<CompletionKind> LOOKUP_ELEMENT_COMPLETION_KIND = Key.create("lookup element completion kind");

  private final String name;
  private boolean alreadyFilled = false;

  public CompletionKind(String name) { this.name = name; }

  protected abstract void fillKindVariants();

  public abstract boolean isApplicable();

  public String getName() { return name; }

  public void fillKindVariantsOnce(CompletionSession session) {
    if (alreadyFilled) {
      return;
    }
    session.getResult().setCurrentCompletionKind(this);
    try {
      fillKindVariants();
    }
    finally {
      alreadyFilled = true;
      session.getResult().resetCurrentCompletionKind();
    }
  }

  public void putKindInfoRequireEmpty(LookupElement element) {
    if (element.getUserData(LOOKUP_ELEMENT_COMPLETION_KIND) != null) {
      throw new IllegalStateException("For element: '%s' LOOKUP_ELEMENT_COMPLETION_KIND must be empty");
    }
    element.putUserData(LOOKUP_ELEMENT_COMPLETION_KIND, this);
  }
}
