// Copyright 2000-2022 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.codeInsight.completion.kind

import java.util.function.Supplier


abstract class CompletionKind(val name: String) {
  private var alreadyFilled = false

  protected abstract fun fillKindVariants()
  abstract val isApplicable: Boolean

  fun fillKindVariantsOnce() {
    if (alreadyFilled) {
      return
    }
    fillKindVariants()
    alreadyFilled = true
  }

  companion object {
    private class EmptyVariantFiller : Runnable {
      override fun run() = throw UnsupportedOperationException()
    }

    @JvmStatic
    fun withDynamicCompletionDecision(
      name: String,
      isKindApplicable: () -> Boolean,
      doFillVariants: Runnable,
    ): CompletionKindWithMutableFiller {

      class CompletionKindWithDynamicCompletionDecision : CompletionKindWithMutableFiller(name, isKindApplicable(), doFillVariants) {
        override fun fillKindVariants() = variantFiller.run()
        override val isApplicable = isKindApplicable()
        public fun setFiller(newVariantFiller: Runnable) {
          variantFiller = newVariantFiller
        }
      }

      return CompletionKindWithDynamicCompletionDecision()
    }

    @JvmStatic
    fun withDynamicCompletionDecision(
      name: String,
      isKindApplicable: () -> Boolean
    ) = withDynamicCompletionDecision(name, isKindApplicable, EmptyVariantFiller());

    @JvmStatic
    fun withStaticCompletionDecision(
      name: String,
      isKindApplicable: Boolean,
      doFillVariants: Runnable
    ): CompletionKindWithMutableFiller = withDynamicCompletionDecision(name, { isKindApplicable }, doFillVariants)

    @JvmStatic
    fun withStaticCompletionDecision(
      name: String,
      isKindApplicable: Boolean
    ) = withStaticCompletionDecision(name, isKindApplicable, EmptyVariantFiller());

    @JvmStatic
    fun withFillFunction(
      name: String,
      doFillVariants: Runnable
    ): CompletionKindWithMutableFiller = withStaticCompletionDecision(name, true, doFillVariants)

    @JvmStatic
    fun withoutEmptyFillFunction(
      name: String
    ) = withFillFunction(name, EmptyVariantFiller())
  }
}

open class CompletionKindWithMutableFiller(
  name: String,
  override val isApplicable: Boolean,
  var variantFiller: Runnable
) : CompletionKind(name) {
  override fun fillKindVariants() = variantFiller.run()
}
