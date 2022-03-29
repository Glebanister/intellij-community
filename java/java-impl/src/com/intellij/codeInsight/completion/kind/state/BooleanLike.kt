// Copyright 2000-2022 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.codeInsight.completion.kind.state

abstract class BooleanLike {
  abstract fun value(): Boolean?
  open fun assignOr(actor: Any, other: BooleanLike): Unit = throw UnsupportedOperationException()
  open fun assignAnd(actor: Any, other: BooleanLike): Unit = throw UnsupportedOperationException()
  open fun assign(actor: Any, other: BooleanLike): Unit = throw UnsupportedOperationException()

  fun assignOr(actor: Any, other: Boolean) = assignOr(actor, BooleanWrap(other))
  fun assignAnd(actor: Any, other: Boolean) = assignAnd(actor, BooleanWrap(other))
  fun assign(actor: Any, other: Boolean) = assign(actor, BooleanWrap(other))

  abstract fun registerActor(actor: Any);

  fun isTrue() = value() == true
  fun isFalse() = value() == false
  fun isUndefined() = value() == null

  override fun equals(other: Any?): Boolean {
    if (other !is BooleanLike) return false

    val asBl = other as BooleanLike
    return value() == asBl.value()
  }

  override fun hashCode(): Int = when (value()) {
    null -> 0
    true -> 1
    false -> 2
  }
}
