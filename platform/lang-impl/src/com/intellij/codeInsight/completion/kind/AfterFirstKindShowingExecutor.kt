// Copyright 2000-2022 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.codeInsight.completion.kind

import com.intellij.codeInsight.completion.CompletionContributor
import com.intellij.codeInsight.completion.CompletionContributorWithKinds
import com.intellij.codeInsight.completion.CompletionSession
import com.intellij.codeInsight.completion.kind.state.*
import com.intellij.ui.JBColor
import java.util.function.Supplier
import kotlin.system.measureTimeMillis

data class CompletionKindContext(
  val ck: CompletionKind,
  val session: CompletionSession,

  )

class AfterFirstKindShowingExecutor(var myDoShowLookup: Runnable) : LazyKindsExecutor() {
  val myOtherKinds = ArrayList<Pair<CompletionKind, CompletionSession>>()
  private fun executeKind(completionKind: CompletionKind, session: CompletionSession, highlight: Boolean) {
    if (completionKind.isApplicable) {
      val execTime = measureTimeMillis {
        completionKind.fillKindVariantsOnce(session, if (highlight) JBColor.GREEN else null)
      }
    }
  }

  override fun whenLookupReady(doShowLookup: Runnable) {
    myDoShowLookup = doShowLookup
  }

  override fun addKind(kind: CompletionKind,
                       session: CompletionSession) {
    if (myOtherKinds.any { (exKind, _) -> exKind.name == kind.name }) {
      throw IllegalArgumentException("${kind.name} duplicated")
    }
    myOtherKinds.add(kind to session)
  }

  override fun executeAllOnce() {
    println("Executing from context: ${Thread.currentThread().stackTrace.joinToString("\n")}")
    println("All kinds:")
    for ((kind, _) in myOtherKinds) {
      println("- ${kind.name}")
    }

    val firstPortionLength = 3.coerceAtMost(myOtherKinds.size)

    var toFlush: CompletionSession? = null
    for ((kind, session) in myOtherKinds.subList(0, firstPortionLength)) {
      executeKind(kind, session, true)
      toFlush = session
    }

    toFlush?.flushBatchItems()

    for ((kind, session) in myOtherKinds.subList(firstPortionLength, myOtherKinds.size)) {
      executeKind(kind, session, false)
      session.flushBatchItems()
    }
  }

  override fun <T> wrapNotNullSupplier(supplier: Supplier<T>): LazyValue<T> {
    return LazyValue(supplier)
  }

  override fun <T> wrapNullableSupplier(supplier: Supplier<T?>): LazyNullableValue<T> {
    return LazyNullableValue(supplier)
  }

  override fun makeConstFlag(init: Boolean): Flag {
    return ConstFlag(init)
  }

  override fun makeFlagOr(init: Boolean): Flag {
    return LatestValueTakingFlag(init);
  }

  override fun makeFlagOnceReassignable(init: Boolean): Flag {
    return LatestValueTakingFlag(init);
  }

  override fun makeFlagAnd(init: Boolean): Flag {
    return LatestValueTakingFlag(init);
  }

  override fun reorderContirbutors(contributorsUnordered: MutableList<CompletionContributor>) =
    reorderContirbutorsWithKindsFirst(contributorsUnordered)
}
