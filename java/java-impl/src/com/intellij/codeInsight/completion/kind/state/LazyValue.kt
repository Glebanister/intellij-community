// Copyright 2000-2022 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.codeInsight.completion.kind.state

open class LazyValue<T>(private val supplier: () -> T) : () -> T {
  private var value: T? = null

  override fun invoke() = value ?: let {
    value = supplier()
    value!!
  }
}

open class LazyNullableValue<T>(private val supplier: () -> T?) : () -> T? {
  private var value: T? = null
  private var calculated: Boolean = false

  override fun invoke(): T? = if (calculated) value else {
    value = supplier()
    calculated = true
    value
  }

  fun invokeNotNull(): T = invoke()!!
}
