// Copyright 2000-2022 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.codeInsight.completion.kind

import com.google.gson.JsonParser
import java.nio.file.Path
import kotlin.io.path.bufferedReader
import kotlin.io.path.div
import com.intellij.codeInsight.completion.kind.IdealJavaFileCompletionSuggestions.*
import com.intellij.openapi.components.Service


@Service
class IdealCompletionSuggestionsService(
  private val idealSuggestionsPerFile: MutableMap<Path, IdealJavaFileCompletionSuggestions> = mutableMapOf()
) {

  init {
    val resultsPath = Path.of("/Users/glebmarin/projects/intellij-evaluation/2022-04-19_15-21-58")
    (resultsPath / "data" / "files.json").bufferedReader().use {
      JsonParser.parseReader(it).asJsonObject
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
    val fileSuggestions = idealSuggestionsPerFile[filePath] ?: throw IllegalArgumentException(
      "Completion was not loaded for $filePath, ${idealSuggestionsPerFile.map { it.key }.joinToString { ", " }}")
    return fileSuggestions.getIdealSuggestion(position)
  }
}
