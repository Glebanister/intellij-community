// Copyright 2000-2022 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.codeInsight.completion.kind.state

abstract class Flag {
  abstract fun value(): Boolean?
  open fun assignOr(actor: Any, other: Flag): Unit = throw UnsupportedOperationException()
  open fun assignAnd(actor: Any, other: Flag): Unit = throw UnsupportedOperationException()
  open fun assign(actor: Any, other: Flag): Unit = throw UnsupportedOperationException()

  fun assignOr(actor: Any, other: Boolean) = assignOr(actor, LatestValueTakingFlag(other))
  fun assignAnd(actor: Any, other: Boolean) = assignAnd(actor, LatestValueTakingFlag(other))
  fun assign(actor: Any, other: Boolean) = assign(actor, LatestValueTakingFlag(other))

  abstract fun registerActor(actor: Any);

  fun isTrue() = value() == true
  fun isFalse() = value() == false
  fun isUndefined() = value() == null

  override fun equals(other: Any?): Boolean {
    if (other !is Flag) return false

    val asFlag = other as Flag
    return value() == asFlag.value()
  }

  override fun hashCode(): Int = when (value()) {
    null -> 0
    true -> 1
    false -> 2
  }
}
