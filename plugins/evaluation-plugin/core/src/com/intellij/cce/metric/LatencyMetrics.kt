package com.intellij.cce.metric

import com.intellij.cce.core.Lookup
import com.intellij.cce.core.Session
import com.intellij.cce.metric.util.Sample
import java.util.stream.Stream

abstract class LatencyMetric(private val extractLong: (Lookup) -> Long?, override val name: String) : Metric {
  private val sample = Sample()

  override val value: Double
    get() = compute(sample)

  override fun evaluate(sessions: List<Session>, comparator: SuggestionsComparator): Double {
    val fileSample = Sample()
    sessions.stream()
      .flatMap { session -> session.lookups.stream() }
      .flatMap { lookup -> extractLong(lookup)?.let { Stream.of(it) } ?: Stream.of() }
      .forEach {
        this.sample.add(it.toDouble())
        fileSample.add(it.toDouble())
      }
    return compute(fileSample)
  }

  abstract fun compute(sample: Sample): Double
}


class MaxLatencyMetric : LatencyMetric(Lookup::latency, "InvkMx") {
  override fun compute(sample: Sample): Double = sample.max()

  override val valueType = MetricValueType.INT
}

class MeanLatencyMetric : LatencyMetric(Lookup::latency, "InvkMn") {
  override fun compute(sample: Sample): Double = sample.mean()

  override val valueType = MetricValueType.DOUBLE
}


class MaxLookupShownLatencyMetric : LatencyMetric(Lookup::shownLatency, "ShowMx") {
  override fun compute(sample: Sample): Double = sample.max()

  override val valueType = MetricValueType.INT
}

class MeanLookupShownLatencyMetric : LatencyMetric(Lookup::shownLatency, "ShowMn") {
  override fun compute(sample: Sample): Double = sample.mean()

  override val valueType = MetricValueType.DOUBLE
}

class MeanCorrectElementAddTimeLatencyMetric : LatencyMetric(
  { it.correctElementInfo?.addTime },
  "GoodAddMn"
) {
  override fun compute(sample: Sample): Double = sample.mean()

  override val valueType = MetricValueType.DOUBLE
}

class MeanCorrectElementFirstAppearanceLatencyMetric : LatencyMetric(
  { it.correctElementInfo?.firstAppearanceTime },
  "GoodShowMn"
) {
  override fun compute(sample: Sample): Double = sample.mean()

  override val valueType = MetricValueType.DOUBLE
}

class MeanCorrectElementKindStartMetric : LatencyMetric(
  { it.correctElementInfo?.kindStartTime },
  "KindStartMn"
) {
  override fun compute(sample: Sample): Double = sample.mean()

  override val valueType = MetricValueType.DOUBLE
}

class MeanFirstElementAppearanceLatencyMetric : LatencyMetric(
  { it.firstElementAddTime },
  "FirstShowMn"
) {
  override fun compute(sample: Sample): Double = sample.mean()

  override val valueType = MetricValueType.DOUBLE
}

class MeanIsCorrectElementAddedBeforeLookupShown : LatencyMetric(
  { it.correctElementInfo?.addedToResultBeforeLookupShown?.let { flag -> if (flag) 1 else 0 } },
  "GoodAddBeforeShow"
) {
  override fun compute(sample: Sample): Double = sample.mean()

  override val valueType = MetricValueType.DOUBLE
}

class MeanIsCorrectElementAddedToLookupBeforeLookupShown : LatencyMetric(
  { it.correctElementInfo?.addedToLookupBeforeLookupShown?.let { flag -> if (flag) 1 else 0 } },
  "GoodShownInFirst"
) {
  override fun compute(sample: Sample): Double = sample.mean()

  override val valueType = MetricValueType.DOUBLE
}

class MeanRestartLatency : LatencyMetric(
  { it.restartLatency },
  "Restart"
) {
  override fun compute(sample: Sample): Double = sample.mean()

  override val valueType = MetricValueType.DOUBLE
}

