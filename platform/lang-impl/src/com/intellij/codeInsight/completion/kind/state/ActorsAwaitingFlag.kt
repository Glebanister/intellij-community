// Copyright 2000-2022 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.codeInsight.completion.kind.state


abstract class ActorsAwaitingFlag(
  private val contributionPerActor: MutableMap<Any, Boolean?> = HashMap(),
  private var lastContributedActor: Pair<Any, Boolean>? = null
) : Flag() {
  constructor(initialValue: Boolean?) : this() {
    initialValue?.let {
      registerActor(this)
      act(this, it);
    }
  }

  override fun registerActor(actor: Any) {
    contributionPerActor[actor] = null
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

class ActorsAwaitingOr(initialValue: Boolean?) : ActorsAwaitingFlag(initialValue) {
  override fun value(): Boolean? {
    if (someoneContributedWith(true)) {
      return true;
    }
    return if (everyoneContributedWith(false)) false
    else null
  }

  override fun assignOr(actor: Any, other: Boolean) = act(actor, other)
}

class ActorsAwaitingAnd(initialValue: Boolean?) : ActorsAwaitingFlag(initialValue) {
  override fun value(): Boolean? {
    if (someoneContributedWith(false)) {
      return false;
    }
    return if (everyoneContributedWith(true)) true
    else null
  }

  override fun assignAnd(actor: Any, other: Boolean) = act(actor, other)
}
