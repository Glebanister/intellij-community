// Copyright 2000-2022 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.codeInsight.completion

interface DeferredBoolean {
  fun value(): Boolean?

  fun eq(boolean: Boolean): Boolean = value()
                                        ?.let { it == boolean }
                                      ?: false

  fun eqTrue(): Boolean = eq(true)
  fun eqFalse(): Boolean = eq(false)
}

abstract class DeferredBooleanWithRegisteredActors(
  private val contributionPerActor: MutableMap<Any, Boolean?> = HashMap(),
  private var lastContributedActor: Pair<Any, Boolean>? = null
) : DeferredBoolean {
  constructor(initialValue: Boolean?) : this() {
    initialValue?.let {
      registerActor(this)
      act(this, it);
    }
  }

  fun registerActor(actor: Any) {
    contributionPerActor[actor] = false
  }

  protected fun act(actor: Any, value: Boolean) {
    if (actor !in contributionPerActor) {
      throw IllegalArgumentException("Not registered actor tried to make contribution")
    }
    lastContributedActor = actor to value
    contributionPerActor[actor] = value
  }

  protected fun everyoneContributedWith(value: Boolean): Boolean =
    contributionPerActor.values.stream().allMatch { it == value }

  protected fun someoneContributedWith(value: Boolean): Boolean =
    contributionPerActor.values.stream().anyMatch { it == value }

  protected fun lastContribution(): Boolean? =
    lastContributedActor?.second
}

class DeferredOr(initialValue: Boolean?) : DeferredBooleanWithRegisteredActors(initialValue) {
  override fun value(): Boolean? {
    if (someoneContributedWith(true)) {
      return true;
    }
    return if (everyoneContributedWith(false)) false
    else null
  }

  fun or(actor: Any, boolean: Boolean) = act(actor, boolean)
}

class DeferredAnd(initialValue: Boolean?) : DeferredBooleanWithRegisteredActors(initialValue) {
  override fun value(): Boolean? {
    if (someoneContributedWith(false)) {
      return false;
    }
    return if (everyoneContributedWith(true)) true
    else null
  }

  fun and(actor: Any, boolean: Boolean) = act(actor, boolean)
}

class DeferredAssign(initialValue: Boolean?) : DeferredBooleanWithRegisteredActors(initialValue) {
  override fun value() = lastContribution()

  fun assign(actor: Any, boolean: Boolean) = act(actor, boolean)
}
