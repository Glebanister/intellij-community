// Copyright 2000-2022 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.codeInsight.completion.kind;

import com.intellij.codeInsight.completion.CompletionSession;
import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.openapi.util.Key;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.Duration;
import java.time.Instant;

public abstract class CompletionKind {
  public static final Key<CompletionKind> LOOKUP_ELEMENT_COMPLETION_KIND = Key.create("lookup element completion kind");

  private final String name;
  private @Nullable ExecutionInfo myExecutionInfo = null;

  public CompletionKind(String name) { this.name = name; }

  protected abstract void fillKindVariants();

  public abstract boolean isApplicable();

  public String getName() { return name; }

  public boolean wasExecuted() {
    return myExecutionInfo != null;
  }

  public @NotNull ExecutionInfo getExecutionInfo() {
    if (myExecutionInfo == null) {
      throw new IllegalStateException(String.format("%s was not yet executed", name));
    } else {
      return myExecutionInfo;
    }
  }

  public void fillKindVariantsOnce(CompletionSession session) {
    if (wasExecuted()) {
      return;
    }
    session.getResult().setCurrentCompletionKind(this);
    Instant timeStart = Instant.now();
    boolean finishedWithException = false;
    try {
      fillKindVariants();
    }
    catch (Throwable e) {
      finishedWithException = true;
      throw e;
    }
    finally {
      myExecutionInfo = new ExecutionInfo(finishedWithException,
                                          Duration.between(timeStart, Instant.now()));
      session.getResult().resetCurrentCompletionKind();
    }
  }

  public void putKindInfoRequireEmpty(@NotNull LookupElement element) {
    if (element.getUserData(LOOKUP_ELEMENT_COMPLETION_KIND) != null) {
      throw new IllegalStateException(
        String.format("For element: '%s' LOOKUP_ELEMENT_COMPLETION_KIND must be empty", element.getLookupString()));
    }
    element.putUserData(LOOKUP_ELEMENT_COMPLETION_KIND, this);
  }

  public static class ExecutionInfo {
    public final boolean finishedWithException;
    public final @NotNull Duration executionTime;

    public ExecutionInfo(boolean exception, Duration time) {
      finishedWithException = exception;
      executionTime = time;
    }
  }
}
