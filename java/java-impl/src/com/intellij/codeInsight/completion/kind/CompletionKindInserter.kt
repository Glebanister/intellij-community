// Copyright 2000-2022 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.codeInsight.completion.kind

import com.intellij.codeInsight.completion.BaseCompletionService.*
import com.intellij.codeInsight.completion.CompletionKind
import com.intellij.codeInsight.lookup.LookupElement
import com.intellij.openapi.util.Key

interface CompletionKindInserter {
  var currentCompletionKind: CompletionKind?

  fun insertCompletionKindTo(item: LookupElement) {
    currentCompletionKind
      ?.let { notNullCurrentCompletionKind ->
        item.getUserData(LOOKUP_ELEMENT_COMPLETION_KIND)?.let { userDataCompletionKind ->
          if (userDataCompletionKind != notNullCurrentCompletionKind.name) {

            val prevSet = item.getUserData(LOOKUP_ELEMENT_COMPLETION_RESULT_SET) ?: "<null set>"
            val prevKind = userDataCompletionKind
            val prevContributor = item.getUserData(LOOKUP_ELEMENT_CONTRIBUTOR) ?: "<null contributor>"
            val nextSet = this::class.java.simpleName
            val nextKind = notNullCurrentCompletionKind.name

            throw IllegalStateException(
              "Lookup element completion kind can not be changed:\n\t[set] $prevSet -> $nextSet\n\t[kind] $prevKind -> $nextKind\n\t[contributor] $prevContributor")
          }
        }
        item.putUserData(LOOKUP_ELEMENT_COMPLETION_RESULT_SET, this::class.java.simpleName)
        item.putUserData(LOOKUP_ELEMENT_COMPLETION_KIND, notNullCurrentCompletionKind.name)
      }
    ?: throw IllegalStateException("Current completion kind is null")
  }

  companion object {
    val LOOKUP_ELEMENT_COMPLETION_KIND = Key.create<String>("lookup element completion kind")
    val LOOKUP_ELEMENT_COMPLETION_RESULT_SET = Key.create<String>("lookup element completion kind")
  }
}
