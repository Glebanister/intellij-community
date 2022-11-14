// Copyright 2000-2022 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.cce.metric

import com.intellij.cce.core.Session
import com.intellij.cce.metric.util.Sample
import com.intellij.util.containers.notNullize

open class ShowLatencyCategoryMetric(override val name: String, private val acceptRange: LongRange) : Metric {
  private val sample = Sample()

  override val valueType: MetricValueType
    get() = MetricValueType.DOUBLE

  override val value: Double
    get() = sample.mean()

  override fun evaluate(sessions: List<Session>, comparator: SuggestionsComparator): Double {
    val fileSample = Sample()
    sessions
      .flatMap { session -> session.lookups }
      .map { if (it.shownLatency in acceptRange) 1.0 else 0.0 }
      .forEach {
        this.sample.add(it)
        fileSample.add(it)
      }
    return fileSample.mean()
  }
}

enum class UserExperienceThreshold(public val range: LongRange) {
  Immediate(LongRange(0, 100)),
  Fast(LongRange(101, 300)),
  Slow(LongRange(301, Long.MAX_VALUE))
}

class UXShowLatencyMetric(threshold: UserExperienceThreshold) : ShowLatencyCategoryMetric(
  "Session${threshold.name}",
  threshold.range
)
