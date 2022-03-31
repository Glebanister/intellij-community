// Copyright 2000-2022 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.codeInsight.completion.kind

import com.intellij.codeInsight.completion.CompletionKind
import com.intellij.codeInsight.completion.CompletionResultSet
import com.intellij.codeInsight.completion.JavaCompletionSession
import com.intellij.codeInsight.lookup.LookupElement

class CompletionKindInsertingSession(
  private val resultWithKind: CompletionKindInsertingCompletionResultSet,
) : JavaCompletionSession(resultWithKind), CompletionKindInserter {
  constructor(
    result: CompletionResultSet
  ) : this(DirectCompletionKindInsertingCompletionResultSet(result, null))

  override fun registerBatchItems(elements: MutableCollection<out LookupElement>?) {
    elements ?. let { resultWithKind.addAllElements(it) }
  }

  override fun addClassItem(lookupElement: LookupElement?) {
    lookupElement?.let { insertCompletionKindTo(it) }
    super.addClassItem(lookupElement)
  }

  override var currentCompletionKind: CompletionKind?
    get() = resultWithKind.currentCompletionKind
    set(value) {
      resultWithKind.currentCompletionKind = value
    }

  abstract class CompletionKindInsertingCompletionResultSet(originalCompletionResultSet: CompletionResultSet)
    : DelegatingCompletionResultSet(originalCompletionResultSet), CompletionKindInserter {
    override fun consume(element: LookupElement?) {
      element ?.let { insertCompletionKindTo(it) }
      super.consume(element)
    }

    override fun addElement(element: LookupElement) {
      insertCompletionKindTo(element)
      originalCompletionResultSet.addElement(element)
    }
  }


  class DirectCompletionKindInsertingCompletionResultSet(
    originalCompletionResultSet: CompletionResultSet,
    override var currentCompletionKind: CompletionKind?
  ) : CompletionKindInsertingCompletionResultSet(originalCompletionResultSet), CompletionKindInserter {

    override fun produceSimilar(result: CompletionResultSet): CompletionResultSet {
      return DelegatingCompletionKindInsertingCompletionResultSet(result, this)
    }
  }

  class DelegatingCompletionKindInsertingCompletionResultSet(
    wrappedSet: CompletionResultSet,
    private val kindInitialSource: CompletionKindInserter
  ) : CompletionKindInsertingCompletionResultSet(wrappedSet), CompletionKindInserter {

    override fun produceSimilar(result: CompletionResultSet): CompletionResultSet {
      return DelegatingCompletionKindInsertingCompletionResultSet(result, kindInitialSource)
    }

    override var currentCompletionKind: CompletionKind?
      get() = kindInitialSource.currentCompletionKind
      set(value) {
        kindInitialSource.currentCompletionKind = value
      }
  }
}
