// Copyright 2000-2022 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.completion.ml.features

import com.intellij.psi.codeStyle.NameUtil
import java.util.*


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

//val ACTUALLY_ALL_PSI_PER_LEVEL: List<MutableMap<String, Int>> = MutableList(PSI_LEVELS_TO_REMEMBER) { mutableMapOf() }

private fun computePsiParent(psiParentOrNull: String?, parentNum: Int): List<Pair<String, Double>> {
  val parentPrefix = "psi_parent"
  val psiParent = psiParentOrNull ?: return ALL_PSI_PARENTS_PER_LEVEL
    .withIndex()
    .flatMap { (level, items) -> items.map { psiName -> "${parentPrefix}_${level}_is_${psiName}" to 0.0 } }

  //val psiParentsOfThisLevel: MutableMap<String, Int> = ACTUALLY_ALL_PSI_PER_LEVEL[parentNum - 1]
  //psiParentsOfThisLevel[psiParent] = psiParentsOfThisLevel[psiParent]?.plus(1) ?: 1
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

private enum class JavaKeyword {
  ABSTRACT,
  BOOLEAN,
  BREAK,
  CASE,
  CATCH,
  CHAR,
  CLASS,
  CONST,
  CONTINUE,
  DOUBLE,
  ELSE,
  EXTENDS,
  FINAL,
  FINALLY,
  FLOAT,
  FOR,
  IF,
  IMPLEMENTS,
  IMPORT,
  INSTANCEOF,
  INT,
  INTERFACE,
  LONG,
  NEW,
  PRIVATE,
  PROTECTED,
  PUBLIC,
  RETURN,
  STATIC,
  SUPER,
  SWITCH,
  THIS,
  THROW,
  THROWS,
  TRY,
  VOID,
  WHILE,
  TRUE,
  FALSE,
  NULL,
  ANOTHER;
}

public fun computeKind(kind: String): List<Double> {
  return ALL_KINDS.map { (if (it == kind) 1.0 else 0.0) }
}

private fun computePositionMatcher(matcher: String?): List<Pair<String, Double>> {
  return ALL_POSITION_MATCHERS.map { it to (if (it == matcher) 1.0 else 0.0) }
}

private fun computeKeyword(keyword: String?): List<Pair<String, Double>> {
  return JavaKeyword.values().map { "is_keyword_${it.name}" to if (it.toString() == keyword) 1.0 else 0.0 }
}

fun computeAdditionalKindOrderingFeatures(contextFeatures: Map<String, String>): SortedMap<String, Double> {
  val processedFeatures = mutableMapOf<String, Double>().toSortedMap()
  contextFeatures.copyTo(listOf(
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
    contextFeatures["ml_ctx_common_line_num"]!!.toDouble(),
    contextFeatures["ml_ctx_common_lines_in_document"]!!.toDouble()
  )
  processedFeatures["indent_level"] = contextFeatures["ml_ctx_common_indent_level"]!!.toDouble()
  processedFeatures.putAll(computeCaseSensitivity(contextFeatures["ml_ctx_common_case_sensitivity"]!!))

  for (parentN in 1..PSI_LEVELS_TO_REMEMBER) {
    processedFeatures.putAll(computePsiParent(contextFeatures["ml_ctx_common_parent_$parentN"], parentN))
  }
  processedFeatures.putAll(computePositionMatcher(contextFeatures["ml_ctx_java_position_matcher"]))
  processedFeatures.putAll(computeKeyword(contextFeatures["ml_ctx_java_prev_neighbour_keyword"]))
  JavaKeyword.values().map { it.name }

  return processedFeatures
}
