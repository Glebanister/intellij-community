// Copyright 2000-2022 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.codeInsight.completion.kind.state


abstract class DeferredBoolean(
  private val contributionPerActor: MutableMap<Any, BooleanLike?> = HashMap(),
  private var lastContributedActor: Pair<Any, BooleanLike>? = null
) : BooleanLike() {
  constructor(initialValue: Boolean?) : this() {
    initialValue?.let {
      registerActor(this)
      act(this, BooleanWrap(it));
    }
  }

  override fun registerActor(actor: Any) {
    contributionPerActor[actor] = BooleanWrap(false)
  }

  protected fun act(actor: Any, value: BooleanLike) {
    if (actor !in contributionPerActor) {
      throw IllegalArgumentException("Not registered actor tried to make contribution")
    }
    lastContributedActor = actor to value
    contributionPerActor[actor] = value
  }

  protected fun everyoneContributedWith(value: BooleanLike): Boolean =
    contributionPerActor.values.stream().allMatch { it == value }

  protected fun someoneContributedWith(value: BooleanLike): Boolean =
    contributionPerActor.values.stream().anyMatch { it == value }

  protected fun lastContribution(): BooleanLike? =
    lastContributedActor?.second
}

class DeferredOr(initialValue: Boolean?) : DeferredBoolean(initialValue) {
  override fun value(): Boolean? {
    if (someoneContributedWith(BooleanWrap(true))) {
      return true;
    }
    return if (everyoneContributedWith(BooleanWrap(false))) false
    else null
  }

  override fun assignOr(actor: Any, other: BooleanLike) = act(actor, other)
}

class DeferredAnd(initialValue: Boolean?) : DeferredBoolean(initialValue) {
  override fun value(): Boolean? {
    if (someoneContributedWith(BooleanWrap(false))) {
      return false;
    }
    return if (everyoneContributedWith(BooleanWrap(true))) true
    else null
  }

  override fun assignAnd(actor: Any, other: BooleanLike) = act(actor, other)
}

class DeferredAssign(initialValue: Boolean?) : DeferredBoolean(initialValue) {
  override fun value() = lastContribution()?.value()

  override fun assign(actor: Any, other: BooleanLike) = act(actor, other)
}

