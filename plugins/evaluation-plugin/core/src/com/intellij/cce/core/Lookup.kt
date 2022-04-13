package com.intellij.cce.core

data class Lookup(
  val prefix: String,
  val suggestions: List<Suggestion>,
  val latency: Long,
  val shownLatency: Long,
  var features: Features? = null,
  val selectedPosition: Int,
  val isNew: Boolean,
  val kindsExecutionInfo: List<CompletionKindExecutionInfo>
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
      features: Features? = null,
      isNew: Boolean = false,
      kindsExecutionInfo: List<CompletionKindExecutionInfo> = emptyList()
    ): Lookup {
      val selectedPosition = suggestions.indexOfFirst { it.text == expectedText }
        .let { if (it < 0) -1 else it }

      return Lookup(text, suggestions, latency, shownLatency, features, selectedPosition, isNew, kindsExecutionInfo)
    }
  }
}
