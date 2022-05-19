// Copyright 2000-2022 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.codeInsight.completion.kind

import com.intellij.codeInsight.completion.CompletionContributor
import com.intellij.codeInsight.completion.CompletionContributorWithKinds
import com.intellij.codeInsight.completion.CompletionParameters
import com.intellij.codeInsight.completion.CompletionSession
import com.intellij.openapi.components.service
import com.intellij.codeInsight.completion.kind.IdealJavaFileCompletionSuggestions.*
import com.intellij.codeInsight.completion.kind.state.*
import java.nio.file.Path
import java.util.function.Supplier

class IdealKindsExecutor(
  private val filePath: Path,
  private val filePosition: FilePosition,
  private val showLookup: Runnable
) : CompletionKindsExecutor {
  private val idealSuggestions = service<IdealCompletionSuggestionsService>()
  private var executed: Boolean = false

  override fun addKind(kind: CompletionKind, session: CompletionSession) {
    if (executed) return
    idealSuggestions.getIdealSuggestion(filePath, filePosition)?.completionKind?.let {
      if (it == kind.name) {
        kind.fillKindVariantsOnce(session, true)
        session.flushBatchItems()
        showLookup.run()
        executed = true
      }
    }
  }

  fun hasIdealSuggestion(): Boolean {
    return idealSuggestions.getIdealSuggestion(filePath, filePosition) != null
  }

  override fun sureFoundCorrect(): Boolean = executed

  override fun executeAll(parameters: CompletionParameters) {}

  override fun <T : Any?> wrapNotNullSupplier(supplier: Supplier<T>) = LazyValue(supplier)

  override fun <T : Any?> wrapNullableSupplier(supplier: Supplier<T?>) = LazyNullableValue(supplier)

  override fun makeConstFlag(init: Boolean) = ConstFlag(init)

  override fun makeFlagOr(init: Boolean) = LatestValueTakingFlag(init)

  override fun makeFlagOnceReassignable(init: Boolean) = LatestValueTakingFlag(init)

  override fun makeFlagAnd(init: Boolean) = LatestValueTakingFlag(init)

  override fun reorderContirbutors(contributorsUnordered: MutableList<CompletionContributor>): List<CompletionContributor> {
    val withKindsContributors: List<CompletionContributor> = contributorsUnordered
      .filterIsInstance<CompletionContributorWithKinds>()
      .map { c: CompletionContributor? -> c as CompletionContributorWithKinds }

    val otherContributors: List<CompletionContributor> = contributorsUnordered
      .filter { c: CompletionContributor? -> c !is CompletionContributorWithKinds }

    //println("with kinds: ${withKindsContributors.size}, without: ${otherContributors.size}")

    return withKindsContributors + otherContributors
  }
}
