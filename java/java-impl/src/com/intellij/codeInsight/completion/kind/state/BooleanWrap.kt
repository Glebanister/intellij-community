// Copyright 2000-2022 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.codeInsight.completion.kind.state

class BooleanWrap(private var actual: Boolean) : BooleanLike() {
  override fun value() = actual

  override fun assignOr(actor: Any, other: BooleanLike) {
    actual = actual || other.value()!!
  }

  override fun assignAnd(actor: Any, other: BooleanLike) {
    actual = actual && other.value()!!
  }

  override fun assign(actor: Any, other: BooleanLike) {
    actual = other.value()!!
  }

  override fun registerActor(actor: Any) {}
}
