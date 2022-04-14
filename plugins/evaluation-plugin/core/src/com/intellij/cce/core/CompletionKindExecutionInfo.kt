// Copyright 2000-2022 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.cce.core

data class CompletionKindExecutionInfo(
  val kindName: String,
  val finishedWithException: Boolean,
  val duration: Long,
  val startTime: Long
)
