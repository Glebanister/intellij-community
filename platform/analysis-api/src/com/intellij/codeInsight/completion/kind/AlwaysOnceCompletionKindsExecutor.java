// Copyright 2000-2022 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.codeInsight.completion.kind;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public abstract class AlwaysOnceCompletionKindsExecutor implements CompletionKindsExecutor {
  private volatile boolean myStartedExecution = false;
  private final Lock myExecutionLock = new ReentrantLock();

  @Override
  public final void executeAll() {
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
}
