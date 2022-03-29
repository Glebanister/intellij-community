// Copyright 2000-2022 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.codeInsight.completion.kind


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
    @JvmStatic
    fun withDynamicCompletionDecision(
      name: String,
      isKindApplicable: () -> Boolean,
      doFillVariants: Runnable = Runnable {},
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
    fun withStaticCompletionDecision(
      name: String,
      isKindApplicable: Boolean,
      doFillVariants: Runnable = Runnable {},
    ): CompletionKindWithMutableFiller = withDynamicCompletionDecision(name, { isKindApplicable }, doFillVariants)

    @JvmStatic
    fun withFillFunction(
      name: String,
      doFillVariants: Runnable = Runnable {},
    ): CompletionKindWithMutableFiller = withStaticCompletionDecision(name, true, doFillVariants)
  }
}

open class CompletionKindWithMutableFiller(
  name: String,
  override val isApplicable: Boolean,
  var variantFiller: Runnable
) : CompletionKind(name) {
  override fun fillKindVariants() = variantFiller.run()
}
