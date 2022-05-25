// Copyright 2000-2022 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.codeInsight.completion.kind

import com.intellij.codeInsight.completion.CompletionContributor
import com.intellij.codeInsight.completion.CompletionParameters
import com.intellij.codeInsight.completion.CompletionSession
import com.intellij.codeInsight.completion.CompletionThreadingBase
import com.intellij.codeInsight.completion.kind.IdealJavaFileCompletionSuggestions.FilePosition
import com.intellij.codeInsight.completion.kind.state.ConstFlag
import com.intellij.codeInsight.completion.kind.state.LatestValueTakingFlag
import com.intellij.codeInsight.completion.kind.state.LazyNullableValue
import com.intellij.codeInsight.completion.kind.state.LazyValue
import com.intellij.openapi.components.service
import com.intellij.ui.JBColor
import java.nio.file.Path
import java.util.function.Supplier

class IdealKindsExecutor(
  private val filePath: Path,
  private val filePosition: FilePosition,
  private var showLookup: Runnable
) : CompletionKindsExecutor {
  private val idealSuggestions = service<IdealCompletionSuggestionsService>()
  private var executed: Boolean = false

  override fun whenLookupReady(doShowLookup: Runnable) {
    showLookup = doShowLookup
  }

  override fun addKind(kind: CompletionKind, session: CompletionSession) {
    if (executed) return
    idealSuggestions.getIdealSuggestion(filePath, filePosition)?.completionKind?.let {
      if (it == kind.name) {
        CompletionThreadingBase.setAwaitForBatchFlushFinishOnce()
        kind.fillKindVariantsOnce(session, JBColor.PINK)
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

  override fun reorderContirbutors(contributorsUnordered: MutableList<CompletionContributor>) =
    reorderContirbutorsWithKindsFirst(contributorsUnordered)
}
