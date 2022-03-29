// Copyright 2000-2022 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.codeInsight.completion.kind.state

import java.util.function.Supplier

abstract class BooleanFromSupplier(private val supplier: Supplier<Boolean?>) : BooleanLike() {
  override fun value() = supplier.get()
}
