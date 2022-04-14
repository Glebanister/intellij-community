package com.intellij.cce.core

import java.time.Duration

data class CorrectElementInfo(
  val addTime: Long,
  val addedBeforeLookupShown: Boolean,
  val firstAppearanceTime: Long,
  val kindStartTime: Long?
)

data class Lookup(
  val prefix: String,
  val suggestions: List<Suggestion>,
  val latency: Long,
  val shownLatency: Long,
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
      shownLatency: Long,
      features: Features?,
      isNew: Boolean,
      kindsExecutionInfo: List<CompletionKindExecutionInfo>,
      correctElementInfo: CorrectElementInfo?,
      firstElementAddTime: Long?,
    ): Lookup {
      val selectedPosition = suggestions.indexOfFirst { it.text == expectedText }
        .let { if (it < 0) -1 else it }

      return Lookup(
        text, suggestions, latency, shownLatency, features, selectedPosition, isNew,
        kindsExecutionInfo,
        correctElementInfo,
        firstElementAddTime
      )
    }
  }
}
