// Copyright 2000-2022 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.codeInsight.completion.kind

import com.google.gson.JsonParser
import com.intellij.codeInsight.completion.kind.IdealJavaFileCompletionSuggestions.FilePosition
import com.intellij.codeInsight.completion.kind.IdealJavaFileCompletionSuggestions.Suggestion
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.openapi.startup.StartupActivity
import java.nio.file.Path
import kotlin.io.path.bufferedReader
import kotlin.io.path.div


@Service
class IdealCompletionSuggestionsService(
  private val idealSuggestionsPerFile: MutableMap<Path, IdealJavaFileCompletionSuggestions> = mutableMapOf()
) {

  init {
    // CHANGE IT
    val resultsPath = Path.of("/Users/glebmarin/projects/intellij-evaluation/ideal_suggestions")
    (resultsPath / "data" / "files.json").bufferedReader().use { filesJson ->
      JsonParser.parseReader(filesJson).asJsonObject
        .entrySet()
        .forEach {
          println("Loading ideal suggestions for ${it.key}")
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

internal class LoadIdealSuggestions : StartupActivity.DumbAware {
  override fun runActivity(project: Project) {
    service<IdealCompletionSuggestionsService>()
  }
}
