// Copyright 2000-2022 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.codeInsight.completion.kind

import com.google.gson.JsonParser
import java.nio.file.Path
import kotlin.io.path.bufferedReader
import kotlin.io.path.div
import com.intellij.codeInsight.completion.kind.IdealJavaFileCompletionSuggestions.*

class IdealCompletionSuggestionsManager(
  private val idealSuggestionsPerFile: MutableMap<Path, IdealJavaFileCompletionSuggestions> = mutableMapOf()
) {
  constructor(resultsPath: Path) : this() {
    val evaluatedFiles = (resultsPath / "data" / "files.json").bufferedReader().use {
      JsonParser.parseReader(it).asJsonObject.asJsonObject
        .entrySet()
        .forEach {
          idealSuggestionsPerFile[Path.of(it.key)] = IdealJavaFileCompletionSuggestions(
            resultsPath / "data" / "files" / it.value.asString
          )
        }
    }
  }

  fun getIdealSuggestion(filePath: Path,
                         position: FilePosition
  ): Suggestion? {
    val fileSuggestions = idealSuggestionsPerFile[filePath] ?: throw IllegalArgumentException("Completion was not loaded for $filePath")
    return fileSuggestions.getIdealSuggestion(position)
  }
}
