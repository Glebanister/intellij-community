// Copyright 2000-2022 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.codeInsight.completion.kind.state

class ConstFlag(private val value: Boolean) : Flag() {
  override fun value(): Boolean = value

  override fun registerActor(actor: Any) {}
}
