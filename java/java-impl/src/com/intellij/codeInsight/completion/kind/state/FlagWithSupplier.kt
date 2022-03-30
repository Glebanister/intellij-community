// Copyright 2000-2022 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.codeInsight.completion.kind.state

class FlagWithSupplier(private var supplier: () -> Boolean) : Flag() {
  override fun value(): Boolean = supplier()

  override fun registerActor(actor: Any) {}

  fun withLazySupplier(): FlagWithSupplier {
    if (supplier is LazyValue<*>) {
      return this
    }
    supplier = LazyValue(supplier)
    return this
  }
}
