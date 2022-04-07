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
import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.openapi.util.Pair;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class CompletionSession {
  private final List<Pair<? extends Collection<? extends LookupElement>, CompletionKind>> myBatchItems = new ArrayList<>();
  protected final CompletionResultSet myResult;

  public CompletionSession(CompletionResultSet result) {
    myResult = result;
  }

  public void registerBatchItems(Collection<? extends LookupElement> elements) {
    myBatchItems.add(
      new Pair<>(elements, myResult.getCurrentCompletionKind())
    );
  }

  protected void flushBatchItems() {
    myResult.addAllElementsWithKinds(myBatchItems);
    myBatchItems.clear();
  }

  @NotNull PrefixMatcher getMatcher() {
    return myResult.getPrefixMatcher();
  }

  public CompletionResultSet getResult() {
    return myResult;
  }
}
