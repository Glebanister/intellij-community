// Copyright 2000-2022 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.codeInsight.completion.kind.state

import com.intellij.codeInsight.completion.LazyNotNullValue
import java.util.function.Supplier

class ImmediatelyGettingSupplier<T>(supplier: Supplier<T>) : LazyNotNullValue<T>(supplier) {
  init {
    get()
  }
}
