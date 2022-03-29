// Copyright 2000-2022 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.codeInsight.completion.kind.state


abstract class ActorsAwaitingFlag(
  private val contributionPerActor: MutableMap<Any, Flag?> = HashMap(),
  private var lastContributedActor: Pair<Any, Flag>? = null
) : Flag() {
  constructor(initialValue: Boolean?) : this() {
    initialValue?.let {
      registerActor(this)
      act(this, LatestValueTakingFlag(it));
    }
  }

  override fun registerActor(actor: Any) {
    contributionPerActor[actor] = LatestValueTakingFlag(false)
  }

  protected fun act(actor: Any, value: Flag) {
    if (actor !in contributionPerActor) {
      throw IllegalArgumentException("Not registered actor tried to make contribution")
    }
    lastContributedActor = actor to value
    contributionPerActor[actor] = value
  }

  protected fun everyoneContributedWith(value: Flag): Boolean =
    contributionPerActor.values.stream().allMatch { it == value }

  protected fun someoneContributedWith(value: Flag): Boolean =
    contributionPerActor.values.stream().anyMatch { it == value }

  protected fun lastContribution(): Flag? =
    lastContributedActor?.second
}

class ActorsAwaitingOr(initialValue: Boolean?) : ActorsAwaitingFlag(initialValue) {
  override fun value(): Boolean? {
    if (someoneContributedWith(LatestValueTakingFlag(true))) {
      return true;
    }
    return if (everyoneContributedWith(LatestValueTakingFlag(false))) false
    else null
  }

  override fun assignOr(actor: Any, other: Flag) = act(actor, other)
}

class ActorsAwaitingAnd(initialValue: Boolean?) : ActorsAwaitingFlag(initialValue) {
  override fun value(): Boolean? {
    if (someoneContributedWith(LatestValueTakingFlag(false))) {
      return false;
    }
    return if (everyoneContributedWith(LatestValueTakingFlag(true))) true
    else null
  }

  override fun assignAnd(actor: Any, other: Flag) = act(actor, other)
}
