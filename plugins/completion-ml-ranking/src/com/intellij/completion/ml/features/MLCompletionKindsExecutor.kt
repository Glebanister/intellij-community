// Copyright 2000-2022 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.completion.ml.features

import com.intellij.codeInsight.completion.CompletionContributor
import com.intellij.codeInsight.completion.CompletionParameters
import com.intellij.codeInsight.completion.CompletionThreadingBase
import com.intellij.codeInsight.completion.kind.CompletionKindsExecutorWithSorter
import com.intellij.codeInsight.completion.kind.reorderContirbutorsWithKindsFirst

class MLCompletionKindsExecutor : CompletionKindsExecutorWithSorter() {
  override val sorter = MLKindsRelevanceSorter()

  override fun reorderContirbutors(contributorsUnordered: MutableList<CompletionContributor>) =
    reorderContirbutorsWithKindsFirst(contributorsUnordered)

  override fun executeAll(parameters: CompletionParameters) {
    CompletionThreadingBase.setAwaitForBatchFlushFinishOnce()
    super.executeAll(parameters)
  }
}
