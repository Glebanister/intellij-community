// Copyright 2000-2022 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.codeInsight.completion.kind

import com.intellij.codeInsight.completion.CompletionContributor
import com.intellij.codeInsight.completion.CompletionContributorWithKinds

fun reorderContirbutorsWithKindsFirst(contributorsUnordered: MutableList<CompletionContributor>): List<CompletionContributor> {
  val withKindsContributors: List<CompletionContributor> = contributorsUnordered
    .filterIsInstance<CompletionContributorWithKinds>()
    .map { c: CompletionContributor? -> c as CompletionContributorWithKinds }

  val otherContributors: List<CompletionContributor> = contributorsUnordered
    .filter { c: CompletionContributor? -> c !is CompletionContributorWithKinds }

  return withKindsContributors + otherContributors
}
