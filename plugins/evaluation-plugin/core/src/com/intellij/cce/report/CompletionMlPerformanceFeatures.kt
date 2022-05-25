// Copyright 2000-2022 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.cce.report

import com.intellij.cce.core.Session
import com.intellij.cce.workspace.EvaluationWorkspace
import java.io.File
import java.nio.file.Path
import java.util.*
import kotlin.io.path.bufferedWriter
import kotlin.io.path.div
import com.intellij.completion.ml.features.*

data class CompletionMlPerformanceDataset(
  val featuresHeader: MutableList<String> = mutableListOf(),
  val features: MutableList<List<Double>> = mutableListOf(),
  val labels: MutableList<List<Double>> = mutableListOf()
) {
  fun addFeaturesFromSession(session: Session) {
    if (session.lookups.size != 1) throw IllegalArgumentException("Session may have only one lookup")

    val lookup = session.lookups[0]
    val kind = lookup.suggestions.find { it.text == session.expectedText }?.completionContributorKind ?: return
    val processedFeatures = computeAdditionalKindOrderingFeatures(session.getFeatures()[0].common.context)
    labels.add(computeKind(kind))

    if (featuresHeader.isEmpty()) {
      featuresHeader.addAll(processedFeatures.keys)
    }
    features.add(processedFeatures.values.toList())
  }

  fun writeToCsv(workspace: EvaluationWorkspace) {
    //File("/Users/glebmarin/projects/intellij-evaluation/all_psi.txt").bufferedWriter().use { writer ->
    //  writer.write(ACTUALLY_ALL_PSI_PER_LEVEL.withIndex().flatMap { (levelIndex, psiParents) ->
    //    psiParents.entries.map { (psiParent, count) ->
    //      "$levelIndex $psiParent $count"
    //    }
    //  }.joinToString("\n"))
    //}
    //File("/Users/glebmarin/projects/intellij-evaluation/all_kinds.txt").bufferedWriter().use { writer ->
    //  writer.write(ACTUALLY_ALL_KINDS.map { "${it.key} ${it.value}" }.joinToString("\n"))
    //}

    fun dumpCsv(file: Path, header: List<String>, content: List<List<Double>>) {
      file.bufferedWriter().use {
        it.appendLine(header.joinToString(",") { "\"$it\"" })
        content.forEach { row -> it.appendLine(row.joinToString(",")) }
      }
    }

    dumpCsv(
      workspace.completionMlPerformanceFeaturesDir / "features.csv",
      featuresHeader,
      features
    )

    dumpCsv(
      workspace.completionMlPerformanceFeaturesDir / "labels.csv",
      ALL_KINDS,
      labels
    )
  }
}
