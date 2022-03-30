// Copyright 2000-2022 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.codeInsight.completion.kind

import com.intellij.codeInsight.completion.kind.state.LazyValue


abstract class CompletionKind(val name: String) {
  private var alreadyFilled = false

  protected abstract fun fillKindVariants()
  abstract val isApplicable: LazyValue<Boolean>

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
    fun withCompletionDecision(
      name: String,
      isApplicable: LazyValue<Boolean>,
      doFillVariants: Runnable,
    ): CompletionKindWithMutableFiller {

      class CompletionKindWithDynamicCompletionDecision : CompletionKindWithMutableFiller(name, isApplicable, doFillVariants) {
        override fun fillKindVariants() = variantFiller.run()
        fun setFiller(newVariantFiller: Runnable) {
          variantFiller = newVariantFiller
        }
      }

      return CompletionKindWithDynamicCompletionDecision()
    }

    @JvmStatic
    fun withCompletionDecision(
      name: String,
      isKindApplicable: LazyValue<Boolean>
    ) = withCompletionDecision(name, isKindApplicable, EmptyVariantFiller());

    @JvmStatic
    fun withCompletionDecision(
      name: String,
      isKindApplicable: () -> Boolean,
      doFillVariants: Runnable,
    ) = withCompletionDecision(name, LazyValue(isKindApplicable), doFillVariants);

    @JvmStatic
    fun withFillFunction(
      name: String,
      doFillVariants: Runnable
    ): CompletionKindWithMutableFiller = withCompletionDecision(name,
                                                                { true },
                                                                doFillVariants)

    @JvmStatic
    fun withEmptyFillFunction(
      name: String
    ) = withFillFunction(name, EmptyVariantFiller())
  }
}

open class CompletionKindWithMutableFiller(
  name: String,
  override val isApplicable: LazyValue<Boolean>,
  var variantFiller: Runnable
) : CompletionKind(name) {
  override fun fillKindVariants() = variantFiller.run()
}
