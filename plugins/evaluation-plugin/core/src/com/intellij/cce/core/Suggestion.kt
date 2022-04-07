package com.intellij.cce.core

data class Suggestion(
  val text: String,
  val presentationText: String,
  val source: SuggestionSource,
  val kind: SuggestionKind = SuggestionKind.ANY,
  val completionContributorKind: String? = null,
  val completionContributor: String? = null
) {
  fun withSuggestionKind(kind: SuggestionKind): Suggestion {
    return Suggestion(text, presentationText, source, kind, completionContributorKind)
  }

  fun withCompletionContributorKind(completionContributorKind: String?): Suggestion {
    return Suggestion(text, presentationText, source, kind, completionContributorKind)
  }

  fun withCompletionContributor(completionContributor: String?): Suggestion {
    return Suggestion(text, presentationText, source, kind, completionContributorKind, completionContributor)
  }
}
