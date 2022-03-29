// Copyright 2000-2022 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.codeInsight.completion

import java.util.function.Supplier

class LazyNullableValue<T>(private val supplier: Supplier<T?>) : Supplier<T?> {
  private var value: T? = null
  private var alreadyGot = false

  override fun get(): T? {
    if (!alreadyGot) {
      value = supplier.get()
      alreadyGot = true
    }
    return value
  }
}

open class LazyNotNullValue<T>(private val supplier: Supplier<T>) : Supplier<T> {
  private var value: T? = null

  override fun get(): T = value ?: let {
    value = supplier.get()
    value!!
  }
}
