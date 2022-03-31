// Copyright 2000-2022 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.codeInsight.completion.kind

import com.intellij.codeInsight.completion.CompletionResultSet
import com.intellij.codeInsight.completion.CompletionSorter
import com.intellij.codeInsight.completion.PrefixMatcher
import com.intellij.patterns.ElementPattern

abstract class DelegatingCompletionResultSet(
  protected val originalCompletionResultSet: CompletionResultSet,
) : CompletionResultSet(originalCompletionResultSet.prefixMatcher,
                        originalCompletionResultSet.consumer,
                        originalCompletionResultSet.contributor) {
  abstract fun produceSimilar(result: CompletionResultSet): CompletionResultSet

  override fun withPrefixMatcher(matcher: PrefixMatcher): CompletionResultSet =
    produceSimilar(originalCompletionResultSet.withPrefixMatcher(matcher))

  override fun withPrefixMatcher(prefix: String): CompletionResultSet =
    produceSimilar(originalCompletionResultSet.withPrefixMatcher(prefix))

  override fun withRelevanceSorter(sorter: CompletionSorter): CompletionResultSet =
    produceSimilar(originalCompletionResultSet.withRelevanceSorter(sorter))

  override fun addLookupAdvertisement(text: String) =
    originalCompletionResultSet.addLookupAdvertisement(text)

  override fun caseInsensitive(): CompletionResultSet =
    produceSimilar(originalCompletionResultSet.caseInsensitive())

  override fun restartCompletionOnPrefixChange(prefixCondition: ElementPattern<String>?) =
    originalCompletionResultSet.restartCompletionOnPrefixChange(prefixCondition)

  override fun restartCompletionWhenNothingMatches() =
    originalCompletionResultSet.restartCompletionWhenNothingMatches()
}
