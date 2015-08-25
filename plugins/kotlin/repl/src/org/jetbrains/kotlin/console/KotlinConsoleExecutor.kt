/*
 * Copyright 2010-2015 JetBrains s.r.o.
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

package org.jetbrains.kotlin.console

import com.intellij.execution.process.BaseOSProcessHandler
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.util.text.StringUtil
import org.jetbrains.kotlin.console.actions.logError
import org.jetbrains.kotlin.console.highlight.KotlinHistoryHighlighter

private val XML_PREAMBLE = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"

public class KotlinConsoleExecutor(
        private val runner: KotlinConsoleRunner,
        private val historyManager: KotlinConsoleHistoryManager
) {
    private val historyHighlighter = KotlinHistoryHighlighter(runner)

    fun executeCommand() = WriteCommandAction.runWriteCommandAction(runner.project) {
        val consoleView = runner.consoleView
        val document = consoleView.editorDocument
        val inputText = document.text.trim()

        if (inputText.isNotEmpty()) {
            val command = "$inputText\n"
            document.setText("")

            historyHighlighter.addAndHighlightNewCommand(command)
            submitCommand(command)
        }
    }

    private fun submitCommand(command: String) {
        historyManager.updateHistory(command)

        val processHandler = runner.processHandler
        val processInputOS = processHandler.processInput ?: return logError(javaClass, "<p>Broken process stream</p>")
        val charset = (processHandler as? BaseOSProcessHandler)?.charset ?: Charsets.UTF_8

        val xmlRes = "$XML_PREAMBLE" +
                     "<input>" +
                     "${StringUtil.escapeXml(
                             StringUtil.replace(command.trim(), SOURCE_CHARS, XML_REPLACEMENTS)
                     )}" +
                     "</input>"
        val bytes = ("$xmlRes\n").toByteArray(charset)
        processInputOS.write(bytes)
        processInputOS.flush()
    }
}