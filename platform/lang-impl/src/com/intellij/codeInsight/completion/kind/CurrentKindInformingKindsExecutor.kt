// Copyright 2000-2022 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.codeInsight.completion.kind

import com.intellij.codeInsight.completion.CompletionSession
import java.lang.IllegalStateException

interface CurrentKindInformer {
  val session: CompletionSession?

  fun fillKindOnceInformingSession(completionKind: CompletionKind) {
    session?.let {
      try {
        it.result.setCurrentCompletionKind(completionKind)
      }
      finally {
        it.result.resetCurrentCompletionKind()
      }
    } ?: throw throw IllegalStateException("Unable to inform session: it is null")
  }
}
