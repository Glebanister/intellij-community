// Copyright 2000-2022 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.codeInsight.completion.kind.state

class ImmediatelyGettingSupplier<T>(supplier: () -> T) : LazyValue<T>(supplier) {
  init {
    invoke()
  }
}

class ImmediatelyGettingNullableSupplier<T>(supplier: () -> T?) : LazyNullableValue<T>(supplier) {
  init {
    invoke()
  }
}
