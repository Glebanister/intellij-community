package com.intellij.cce.core

import java.time.Instant

data class Suggestion(
  val text: String,
  val presentationText: String,
  val source: SuggestionSource,
  val kind: SuggestionKind = SuggestionKind.ANY,
  val completionContributorKind: String? = null,
  val completionContributor: String? = null,
  val toResultAddTime: Instant,
  val toLookupAddTime: Int,
  val isHighlighted: Boolean
) {
  fun withSuggestionKind(kind: SuggestionKind): Suggestion {
    return Suggestion(text, presentationText, source, kind, completionContributorKind, completionContributor, toResultAddTime,
                      toLookupAddTime, isHighlighted)
  }
}
