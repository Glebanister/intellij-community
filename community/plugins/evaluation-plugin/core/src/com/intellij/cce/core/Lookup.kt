package com.intellij.cce.core

data class CorrectElementInfo(
  val addTime: Long?,
  val addedToResultBeforeLookupShown: Boolean?,
  val addedToLookupBeforeLookupShown: Boolean?,
  val firstAppearanceTime: Long?,
  val kindStartTime: Long?,
  val hasKind: Boolean,
)

// TODO: @Gleb.Marin Split into several data classes
data class Lookup(
  val prefix: String,
  val suggestions: List<Suggestion>,
  val latency: Long,
  var features: Features? = null,
  val selectedPosition: Int,
  val isNew: Boolean,
  val shownLatency: Long?,
  val restartLatency: Long?,
  val kindsExecutionInfo: List<CompletionKindExecutionInfo>,
  val correctElementInfo: CorrectElementInfo?,
  val firstElementAddTime: Long?,
  val lookupIsShown: Boolean
) {
  fun clearFeatures() {
    features = null
  }

  companion object {
    fun fromExpectedText(
      expectedText: String,
      text: String,
      suggestions: List<Suggestion>,
      latency: Long,
      isNew: Boolean,
      shownLatency: Long?,
      restartLatency: Long?,
      features: Features?,
      kindsExecutionInfo: List<CompletionKindExecutionInfo>,
      correctElementInfo: CorrectElementInfo?,
      firstElementAddTime: Long?,
      lookupIsShown: Boolean
    ): Lookup {
      val selectedPosition = suggestions.indexOfFirst { it.text == expectedText }
        .let { if (it < 0) -1 else it }

      return Lookup(
        text, suggestions, latency, features, selectedPosition, isNew, shownLatency, restartLatency,
        kindsExecutionInfo, correctElementInfo, firstElementAddTime, lookupIsShown
      )
    }
  }
}
