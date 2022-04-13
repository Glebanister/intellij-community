// Copyright 2000-2022 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.codeInsight.completion.kind

import com.intellij.codeInsight.completion.CompletionSession
import com.intellij.codeInsight.completion.kind.state.*
import com.intellij.codeInsight.lookup.LookupElementBuilder
import java.util.function.Supplier
import javax.annotation.processing.Completion
import kotlin.system.measureTimeMillis

data class CompletionKindContext(
  val ck: CompletionKind,
  val session: CompletionSession,

)

class AfterFirstKindShowingExecutor(val myDoShowLookup: Runnable) : CompletionKindsExecutor {
  val myOtherKinds = ArrayList<Pair<CompletionKind, CompletionSession>>()
  private fun executeKind(completionKind: CompletionKind, session: CompletionSession) {
    println("Check condition of ${completionKind.name}")
    if (completionKind.isApplicable) {
      val execTime = measureTimeMillis {
        completionKind.fillKindVariantsOnce(session)
      }
      println("Executed ${completionKind.name}: $execTime ms")
    }
    else {
      println("Not applicable: ${completionKind.name}")
    }
  }

  override fun addKind(kind: CompletionKind,
                       session: CompletionSession) {
    if (myOtherKinds.any { (exKind, _) -> exKind.name == kind.name }) {
      throw IllegalArgumentException("${kind.name} duplicated")
    }
    myOtherKinds.add(Pair(kind, session))
  }

  override fun executeAll() {
    println("All kinds:")
    for ((kind, _) in myOtherKinds) {
      println("- ${kind.name}")
    }

    val firstPortionLength = 3.coerceAtMost(myOtherKinds.size)

    for ((kind, session) in myOtherKinds.subList(0, firstPortionLength)) {
      executeKind(kind, session)
    }
    //myDoShowLookup.run()
    for ((kind, session) in myOtherKinds.subList(firstPortionLength, myOtherKinds.size)) {
      executeKind(kind, session)
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
}
