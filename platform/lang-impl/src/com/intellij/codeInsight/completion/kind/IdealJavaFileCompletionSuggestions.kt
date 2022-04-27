// Copyright 2000-2022 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.codeInsight.completion.kind

import com.google.gson.*
import com.intellij.util.io.inputStream
import java.nio.file.Path
import java.util.zip.GZIPInputStream

class IdealJavaFileCompletionSuggestions(
  private val knownSuggestions: MutableMap<FilePosition, Suggestion> = mutableMapOf()
) {

  constructor(archivePath: Path) : this() {
    val evaluated = archivePath.inputStream().use { compressed ->
      GZIPInputStream(compressed).bufferedReader().use { decompressed ->
        JsonParser.parseReader(decompressed).asJsonObject!!
      }
    }

    val sessions = evaluated["sessions"].asJsonArray

    sessions.forEach { sessionJson ->
      val session = sessionJson.asJsonObject
      val known = session["success"].asBoolean
      if (!known) return
      val offset = session["offset"].asInt
      val expectedText = session["expectedText"].asString
      val lookups = session["_lookups"].asJsonArray
      if (lookups.size() != 1)
        throw IllegalArgumentException("Each session must contain exactly one lookup")
      val lookup = lookups[0].asJsonObject
      val correctSuggestion = lookup["suggestions"].asJsonArray.find {
        it.asJsonObject["text"].asString == expectedText
      }?.asJsonObject ?: throw IllegalArgumentException("Session marked as successful, but it does not contain correct suggestion")
      val correctKind = correctSuggestion["completionContributorKind"].let {
        if (it.isJsonNull) null
        else it.asString
      } ?: return
      knownSuggestions[FilePosition(offset)] = Suggestion(correctKind, expectedText)
    }
  }

  fun getIdealSuggestion(position: FilePosition): Suggestion? {
    return knownSuggestions[position]
  }

  data class FilePosition(
    val offset: Int
  )

  data class Suggestion(
    val completionKind: String,
    val expectedText: String
  )
}
