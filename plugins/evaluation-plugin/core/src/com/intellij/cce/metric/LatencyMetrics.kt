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


class MaxLatencyMetric : LatencyMetric(Lookup::latency, "Max Latency") {
  override fun compute(sample: Sample): Double = sample.max()

  override val valueType = MetricValueType.INT
}

class MeanLatencyMetric : LatencyMetric(Lookup::latency, "Mean Latency") {
  override fun compute(sample: Sample): Double = sample.mean()

  override val valueType = MetricValueType.DOUBLE
}


class MaxLookupShownLatencyMetric : LatencyMetric(Lookup::shownLatency, "Max Lookup Shown Latency") {
  override fun compute(sample: Sample): Double = sample.max()

  override val valueType = MetricValueType.INT
}

class MeanLookupShownLatencyMetric : LatencyMetric(Lookup::shownLatency, "Mean Lookup Shown Latency") {
  override fun compute(sample: Sample): Double = sample.mean()

  override val valueType = MetricValueType.DOUBLE
}

class MeanCorrectElementAddTimeLatencyMetric : LatencyMetric(
  { it.correctElementInfo?.addTime },
  "Mean Correct Element Add Time Latency"
) {
  override fun compute(sample: Sample): Double = sample.mean()

  override val valueType = MetricValueType.DOUBLE
}

class MeanCorrectElementFirstAppearanceLatencyMetric : LatencyMetric(
  { it.correctElementInfo?.firstAppearanceTime },
  "Mean Correct Element First Appearance Latency"
) {
  override fun compute(sample: Sample): Double = sample.mean()

  override val valueType = MetricValueType.DOUBLE
}

class MeanCorrectElementKindStartMetric : LatencyMetric(
  { it.correctElementInfo?.kindStartTime },
  "Mean Correct Kind Start Latency"
) {
  override fun compute(sample: Sample): Double = sample.mean()

  override val valueType = MetricValueType.DOUBLE
}

class MeanFirstElementAppearanceLatencyMetric : LatencyMetric(
  { it.firstElementAddTime },
  "Mean First Element Appearance Latency"
) {
  override fun compute(sample: Sample): Double = sample.mean()

  override val valueType = MetricValueType.DOUBLE
}

class MeanIsCorrectElementAddedBeforeLookupShown : LatencyMetric(
  { it.correctElementInfo?.addedBeforeLookupShown?.let { flag -> if (flag) 1 else 0 } },
  "Mean Is Correct Element Added Before Lookup Shown"
) {
  override fun compute(sample: Sample): Double = sample.mean()

  override val valueType = MetricValueType.DOUBLE
}
