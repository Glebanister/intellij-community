// Copyright 2000-2022 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.cce.report

import com.intellij.cce.core.Session
import com.intellij.cce.workspace.EvaluationWorkspace
import com.intellij.codeInsight.completion.ml.JavaCompletionFeatures
import com.intellij.psi.codeStyle.NameUtil
import java.io.File
import java.nio.file.Path
import java.util.*
import java.util.stream.Stream
import kotlin.io.path.bufferedWriter
import kotlin.io.path.div
import kotlin.math.exp

private fun Map<String, String>.copyTo(feature: String, to: MutableMap<String, Double>) {
  to[feature] = this[feature]?.toDouble() ?: 0.0
}

private fun Map<String, String>.copyTo(features: List<String>, to: MutableMap<String, Double>) {
  features.forEach { copyTo(it, to) }
}

private fun computeRelativeLineNum(lineNum: Double, totalLines: Double): Double {
  return if (totalLines == 0.0) 0.0 else lineNum / totalLines
}

private fun computeCaseSensitivity(caseSensitivity: String): List<Pair<String, Double>> {
  return NameUtil.MatchingCaseSensitivity.values().map { "case_sensitivity_${it.name}" to if (it.name == caseSensitivity) 1.0 else 0.0 }
}

const val PSI_LEVELS_TO_REMEMBER = 5;

val ALL_PSI_PARENTS_PER_LEVEL: List<List<String>> = listOf(
  listOf(
    "PsiTypeElementImpl",
    "PsiExpressionListImpl",
    "PsiReferenceExpressionImpl",
    "PsiExpressionStatementImpl",
    "PsiAnnotationImpl",
    "PsiLocalVariableImpl",
    "PsiBinaryExpressionImpl",
  ),
  listOf(
    "PsiMethodCallExpressionImpl",
    "PsiCodeBlockImpl",
    "PsiModifierListImpl",
    "PsiParameterImpl",
    "PsiClassImpl",
    "PsiMethodImpl",
    "PsiDeclarationStatementImpl",
    "PsiReferenceExpressionImpl",
    "PsiLocalVariableImpl",
  ),
  listOf(
    "PsiMethodImpl",
    "PsiExpressionStatementImpl",
    "PsiCodeBlockImpl",
    "PsiLocalVariableImpl",
    "PsiExpressionListImpl",
    "PsiParameterListImpl",
    "PsiParameterImpl",
    "PsiMethodCallExpressionImpl",
    "PsiClassImpl",
    "PsiJavaFileImpl",
    "PsiReferenceExpressionImpl",
    "PsiBlockStatementImpl",
  ),
  listOf(
    "PsiClassImpl",
    "PsiCodeBlockImpl",
    "PsiMethodImpl",
    "PsiMethodCallExpressionImpl",
    "PsiDeclarationStatementImpl",
    "PsiParameterListImpl",
    "PsiJavaFileImpl",
    "PsiIfStatementImpl",
    "PsiExpressionListImpl",
    "PsiAnonymousClassImpl",
  ),
  listOf(
    "PsiMethodImpl",
    "PsiJavaFileImpl",
    "PsiCodeBlockImpl",
    "PsiClassImpl",
    "PsiBlockStatementImpl",
    "PsiExpressionStatementImpl",
    "PsiLocalVariableImpl",
    "PsiMethodCallExpressionImpl",
    "PsiNewExpressionImpl",
  )
)

val ACTUALLY_ALL_PSI_PER_LEVEL: List<MutableMap<String, Int>> = MutableList(PSI_LEVELS_TO_REMEMBER) { mutableMapOf() }

private fun computePsiParent(psiParentOrNull: String?, parentNum: Int): List<Pair<String, Double>> {
  val parentPrefix = "psi_parent"
  val psiParent = psiParentOrNull ?: return ALL_PSI_PARENTS_PER_LEVEL
    .withIndex()
    .flatMap { (level, items) -> items.map { psiName -> "${parentPrefix}_${level}_is_${psiName}" to 0.0 } }

  val psiParentsOfThisLevel: MutableMap<String, Int> = ACTUALLY_ALL_PSI_PER_LEVEL[parentNum - 1]
  psiParentsOfThisLevel[psiParent] = psiParentsOfThisLevel[psiParent]?.plus(1) ?: 1
  return ALL_PSI_PARENTS_PER_LEVEL
    .withIndex()
    .flatMap { (level, items) ->
      items.map { psiName ->
        "${parentPrefix}_${level}_is_${psiName}" to (if (psiParent == psiName) 1.0 else 0.0)
      }
    }.toList()
}

val ALL_KINDS = mutableListOf(
  "identifier_fast",
  "reference_non_method_call",
  "reference_method_call",
)

val ALL_POSITION_MATCHERS = listOf(
  "AnnotationPositionMatcher",
  "ExceptionPositionMatcher",
  "ExtendsDeclarationPositionMatcher",
  "ImplementsDeclarationPositionMatcher",
  "TryWithResourcesPositionMatcher",
  "TypeParameterPositionMatcher"
)

val ACTUALLY_ALL_KINDS = mutableMapOf<String, Int>()

private fun computeKind(kind: String): List<Double> {
  ACTUALLY_ALL_KINDS[kind] = ACTUALLY_ALL_KINDS[kind]?.plus(1) ?: 1
  return ALL_KINDS.map { (if (it == kind) 1.0 else 0.0) }
}

private fun computePositionMatcher(matcher: String?): List<Pair<String, Double>> {
  return ALL_POSITION_MATCHERS.map { it to (if (it == matcher) 1.0 else 0.0) }
}

private fun computeKeyword(keyword: String?): List<Pair<String, Double>> {
  return JavaCompletionFeatures.JavaKeyword.values().map { "is_keyword_${it.name}" to if (it.toString() == keyword) 1.0 else 0.0 }
}

data class CompletionMlPerformanceDataset(
  val featuresHeader: MutableList<String> = mutableListOf(),
  val features: MutableList<List<Double>> = mutableListOf(),
  val labels: MutableList<List<Double>> = mutableListOf()
) {
  fun addFeaturesFromSession(session: Session) {
    if (session.lookups.size != 1) throw IllegalArgumentException("Session may have only one lookup")

    val lookup = session.lookups[0]
    val kind = lookup.suggestions.find { it.text == session.expectedText }?.completionContributorKind ?: return

    labels.add(computeKind(kind))

    if (session.getFeatures().size != 1) throw IllegalArgumentException(
      "Session must have only one set of features, it has ${session.getFeatures().size}")
    val rawFeatures = session.getFeatures()[0].common.context
    val processedFeatures = mutableMapOf<String, Double>().toSortedMap()
    rawFeatures.copyTo(listOf(
      "ml_ctx_common_is_in_line_beginning",
      "ml_ctx_common_is_after_dot",
      "ml_ctx_common_line_num",
      "ml_ctx_common_lines_in_document",
      "ml_java_is_in_qualifier_expression",
      "ml_ctx_java_parent_is_code_ref",
      "ml_ctx_java_is_in_qualifier_expression",
      "ml_ctx_java_is_in_qualifier_expression",
      "ml_ctx_java_is_after_method_call",
      "ml_ctx_java_is_identifier",
      "ml_ctx_java_parent_is_code_ref",
      "ml_ctx_java_parent_is_module_ref",
    ), processedFeatures)
    processedFeatures["relative_line_n"] = computeRelativeLineNum(
      rawFeatures["ml_ctx_common_line_num"]!!.toDouble(),
      rawFeatures["ml_ctx_common_lines_in_document"]!!.toDouble()
    )
    processedFeatures["indent_level"] = rawFeatures["ml_ctx_common_indent_level"]!!.toDouble()
    val caseSensitivityKey = "ml_ctx_common_case_sensitivity"
    processedFeatures.putAll(computeCaseSensitivity(rawFeatures[caseSensitivityKey]!!))

    for (parentN in 1..PSI_LEVELS_TO_REMEMBER) {
      processedFeatures.putAll(computePsiParent(rawFeatures["ml_ctx_common_parent_$parentN"], parentN))
    }
    processedFeatures.putAll(computePositionMatcher(rawFeatures["ml_ctx_java_position_matcher"]))
    processedFeatures.putAll(computeKeyword(rawFeatures["ml_ctx_java_prev_neighbour_keyword"]))
    JavaCompletionFeatures.JavaKeyword.values().map { it.name }

    if (featuresHeader.isEmpty()) {
      featuresHeader.addAll(processedFeatures.keys)
    }
    features.add(processedFeatures.values.toList())
  }

  fun writeToCsv(workspace: EvaluationWorkspace) {
    File("/Users/glebmarin/projects/intellij-evaluation/all_psi.txt").bufferedWriter().use { writer ->
      writer.write(ACTUALLY_ALL_PSI_PER_LEVEL.withIndex().flatMap { (levelIndex, psiParents) ->
        psiParents.entries.map { (psiParent, count) ->
          "$levelIndex $psiParent $count"
        }
      }.joinToString("\n"))
    }
    File("/Users/glebmarin/projects/intellij-evaluation/all_kinds.txt").bufferedWriter().use { writer ->
      writer.write(ACTUALLY_ALL_KINDS.map { "${it.key} ${it.value}" }.joinToString("\n"))
    }

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
