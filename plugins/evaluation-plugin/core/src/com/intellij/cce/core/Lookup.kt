package com.intellij.cce.core

data class CorrectElementInfo(
  val addTime: Long?,
  val addedToResultBeforeLookupShown: Boolean?,
  val addedToLookupBeforeLookupShown: Boolean?,
  val firstAppearanceTime: Long?,
  val kindStartTime: Long?,
  val hasKind: Boolean,
)

data class Lookup(
  val prefix: String,
  val suggestions: List<Suggestion>,
  val latency: Long,
  val shownLatency: Long?,
  val restartLatency: Long?,
  var features: Features? = null,
  val selectedPosition: Int,
  val isNew: Boolean,
  val kindsExecutionInfo: List<CompletionKindExecutionInfo>,
  val correctElementInfo: CorrectElementInfo?,
  val firstElementAddTime: Long?,
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
      shownLatency: Long?,
      restartLatency: Long?,
      features: Features?,
      isNew: Boolean,
      kindsExecutionInfo: List<CompletionKindExecutionInfo>,
      correctElementInfo: CorrectElementInfo?,
      firstElementAddTime: Long?,
    ): Lookup {
      val selectedPosition = suggestions.indexOfFirst { it.text == expectedText }
        .let { if (it < 0) -1 else it }

      return Lookup(
        text, suggestions, latency, shownLatency, restartLatency, features, selectedPosition, isNew,
        kindsExecutionInfo,
        correctElementInfo,
        firstElementAddTime
      )
    }
  }
}
