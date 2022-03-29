// Copyright 2000-2022 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.codeInsight.completion.kind.state

class LatestValueTakingFlag(private var actual: Boolean) : Flag() {
  override fun value() = actual

  override fun assignOr(actor: Any, other: Flag) {
    actual = actual || other.value()!!
  }

  override fun assignAnd(actor: Any, other: Flag) {
    actual = actual && other.value()!!
  }

  override fun assign(actor: Any, other: Flag) {
    actual = other.value()!!
  }

  override fun registerActor(actor: Any) {}
}
