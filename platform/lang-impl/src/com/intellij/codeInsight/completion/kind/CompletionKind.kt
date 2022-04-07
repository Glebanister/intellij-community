// Copyright 2000-2022 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.codeInsight.completion.kind

import com.intellij.codeInsight.completion.kind.CompletionKind
import com.intellij.codeInsight.completion.kind.state.LazyValue

private class EmptyVariantFiller : Runnable {
  override fun run() = throw UnsupportedOperationException()
}

fun withCompletionDecision(
  name: String,
  checkIsApplicable: LazyValue<Boolean>,
  doFillVariants: Runnable,
): CompletionKindWithMutableFiller {

  class CompletionKindWithDynamicCompletionDecision : CompletionKindWithMutableFiller(name, checkIsApplicable, doFillVariants) {
    override fun fillKindVariants() = variantFiller.run()
    override fun isApplicable(): Boolean = checkIsApplicable.get()

    fun setFiller(newVariantFiller: Runnable) {
      variantFiller = newVariantFiller
    }
  }

  return CompletionKindWithDynamicCompletionDecision()
}

fun withCompletionDecision(
  name: String,
  isKindApplicable: LazyValue<Boolean>
) = withCompletionDecision(name, isKindApplicable, EmptyVariantFiller());

fun withCompletionDecision(
  name: String,
  isKindApplicable: () -> Boolean,
  doFillVariants: Runnable,
) = withCompletionDecision(name, LazyValue(isKindApplicable), doFillVariants);

fun withFillFunction(
  name: String,
  doFillVariants: Runnable
): CompletionKindWithMutableFiller = withCompletionDecision(name, { true }, doFillVariants)

fun withEmptyFillFunction(
  name: String
) = withFillFunction(name, EmptyVariantFiller())

open class CompletionKindWithMutableFiller(
  name: String,
  val isApplicable: LazyValue<Boolean>,
  var variantFiller: Runnable
) : CompletionKind(name) {
  override fun fillKindVariants() = variantFiller.run()

  override fun isApplicable() = isApplicable.get()
}
