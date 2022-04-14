/*
 * Copyright 2000-2010 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.intellij.codeInsight.completion;

import com.intellij.codeInsight.completion.kind.CompletionKind;
import com.intellij.codeInsight.lookup.Lookup;
import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.openapi.util.Pair;
import com.intellij.patterns.ElementPattern;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class CompletionSession {
  private final List<Pair<? extends Collection<? extends LookupElement>, @NotNull CompletionKind>> myBatchItems = new ArrayList<>();
  private final List<LookupElement> myBatchWithoutKind = new ArrayList<>();
  protected final CompletionResultSet myResult;

  public CompletionSession(CompletionResultSet result) {
    myResult = result;
  }

  public void registerBatchItems(Collection<? extends LookupElement> elements) {
    var currentCompletionKind = myResult.getCurrentCompletionKind();
    if (currentCompletionKind == null) {
      myBatchWithoutKind.addAll(elements);
      System.out.println("Registered an element without kind from context");
      System.out.println(Arrays.toString(Thread.currentThread().getStackTrace()));
    }
    else {
      myBatchItems.add(
        new Pair<>(elements, currentCompletionKind)
      );
    }
  }

  public void flushBatchItems() {
    myResult.addAllElementsWithKinds(myBatchItems, myBatchWithoutKind);
    myBatchItems.clear();
  }

  @NotNull PrefixMatcher getMatcher() {
    return myResult.getPrefixMatcher();
  }

  public CompletionResultSet getResult() {
    return myResult;
  }

  public CompletionResultSet createDelegatingResultSet(CompletionResultSet originalSet) {
    return new DelegatingResultSet(originalSet);
  }

  class DelegatingResultSet extends CompletionResultSet {
    private final CompletionResultSet originalResultSet;

    DelegatingResultSet(CompletionResultSet set) {
      super(set.getPrefixMatcher(), set.getConsumer(), set.myContributor);
      originalResultSet = set;
    }

    @Override
    public void addElement(@NotNull LookupElement element) {
      registerBatchItems(List.of(element));
    }

    @Override
    public void addAllElements(@NotNull Iterable<? extends LookupElement> elements) {
      ArrayList<LookupElement> elementsArray = new ArrayList<>();
      elements.forEach(elementsArray::add);
      registerBatchItems(elementsArray);
    }

    @Override
    protected void setNullableCurrentCompletionKind(@Nullable CompletionKind completionKind) {
      originalResultSet.setNullableCurrentCompletionKind(completionKind);
    }

    @Override
    protected @Nullable CompletionKind getCurrentCompletionKind() {
      return originalResultSet.getCurrentCompletionKind();
    }

    @Override
    public @NotNull CompletionResultSet withPrefixMatcher(@NotNull PrefixMatcher matcher) {
      return new DelegatingResultSet(originalResultSet.withPrefixMatcher(matcher));
    }

    @Override
    public @NotNull CompletionResultSet withPrefixMatcher(@NotNull String prefix) {
      return new DelegatingResultSet(originalResultSet.withPrefixMatcher(prefix));
    }

    @Override
    public @NotNull CompletionResultSet withRelevanceSorter(@NotNull CompletionSorter sorter) {
      return new DelegatingResultSet(originalResultSet.withRelevanceSorter(sorter));
    }

    @Override
    public void addLookupAdvertisement(@NotNull String text) {
      originalResultSet.addLookupAdvertisement(text);
    }

    @Override
    public @NotNull CompletionResultSet caseInsensitive() {
      return new DelegatingResultSet(originalResultSet.caseInsensitive());
    }

    @Override
    public void restartCompletionOnPrefixChange(ElementPattern<String> prefixCondition) {
      originalResultSet.restartCompletionOnPrefixChange(prefixCondition);
    }

    @Override
    public void restartCompletionWhenNothingMatches() {
      originalResultSet.restartCompletionWhenNothingMatches();
    }
  }
}
