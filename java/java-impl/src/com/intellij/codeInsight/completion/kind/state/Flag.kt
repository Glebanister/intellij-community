// Copyright 2000-2022 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.codeInsight.completion.kind.state

abstract class Flag {
  abstract fun value(): Boolean?

  open fun assignOr(actor: Any, other: Boolean): Unit = throw UnsupportedOperationException()
  open fun assignAnd(actor: Any, other: Boolean): Unit = throw UnsupportedOperationException()
  open fun assign(actor: Any, other: Boolean): Unit = throw UnsupportedOperationException()

  abstract fun registerActor(actor: Any);

  fun isTrue() = value() == true
  fun isFalse() = value() == false
  fun isUndefined() = value() == null
}
