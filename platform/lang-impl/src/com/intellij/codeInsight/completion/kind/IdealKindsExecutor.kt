// Copyright 2000-2022 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.codeInsight.completion.kind

import com.intellij.codeInsight.completion.CompletionSession
import com.intellij.codeInsight.completion.kind.state.Flag

class IdealKindsExecutor : LazyKindsExecutor() {
  override fun addKind(kind: CompletionKind, session: CompletionSession) {
    TODO("Not yet implemented")
  }

  override fun makeConstFlag(init: Boolean): Flag {
    TODO("Not yet implemented")
  }

  override fun makeFlagOr(init: Boolean): Flag {
    TODO("Not yet implemented")
  }

  override fun makeFlagOnceReassignable(init: Boolean): Flag {
    TODO("Not yet implemented")
  }

  override fun makeFlagAnd(init: Boolean): Flag {
    TODO("Not yet implemented")
  }

  override fun executeAllOnce() {
    TODO("Not yet implemented")
  }
}
