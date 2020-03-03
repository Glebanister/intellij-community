/*
 * Copyright 2010-2020 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.idea.klib

import com.intellij.openapi.vfs.VirtualFile
import org.jetbrains.kotlin.library.*
import org.jetbrains.kotlin.library.impl.BuiltInsPlatform
import org.jetbrains.kotlin.platform.TargetPlatform
import org.jetbrains.kotlin.platform.isCommon
import org.jetbrains.kotlin.platform.js.isJs
import org.jetbrains.kotlin.platform.jvm.isJvm
import org.jetbrains.kotlin.platform.konan.isNative
import java.io.IOException
import java.util.*

fun VirtualFile.isKlibLibraryRootForPlatform(targetPlatform: TargetPlatform): Boolean {
    // The virtual file for a library packed in a ZIP file will have path like "/some/path/to/the/file.klib!/",
    // and therefore will be recognized by VFS as a directory (isDirectory == true).
    // So, first, let's check the extension.
    val extension = extension
    if (!extension.isNullOrEmpty() && extension != KLIB_FILE_EXTENSION) return false

    // run check for library root too
    // this is necessary to recognize old style KLIBs that do not have components, and report tem to user appropriately
    val requestedBuiltInsPlatform = targetPlatform.toBuiltInsPlatform()
    return checkKlibComponent(this, requestedBuiltInsPlatform) ||
            children?.any { checkKlibComponent(it, requestedBuiltInsPlatform) } == true
}

private fun checkKlibComponent(componentFile: VirtualFile, requestedBuiltInsPlatform: BuiltInsPlatform): Boolean {
    val manifestFile = componentFile.findChild(KLIB_MANIFEST_FILE_NAME)?.takeIf { !it.isDirectory } ?: return false

    val manifestProperties = try {
        manifestFile.inputStream.use { Properties().apply { load(it) } }
    } catch (_: IOException) {
        return false
    }

    if (!manifestProperties.containsKey(KLIB_PROPERTY_UNIQUE_NAME)) return false

    // No builtins_platform property => either a new common klib (we don't write builtins_platform for common) or old Native klib
    val builtInsPlatformProperty = manifestProperties.getProperty(KLIB_PROPERTY_BUILTINS_PLATFORM)
    // TODO(dsavvinov): drop additional legacy check after 1.4
        ?: return requestedBuiltInsPlatform == BuiltInsPlatform.NATIVE && componentFile.isLegacyNativeKlibComponent

    val builtInsPlatform = BuiltInsPlatform.parseFromString(builtInsPlatformProperty) ?: return false

    return builtInsPlatform == requestedBuiltInsPlatform
}

private fun TargetPlatform.toBuiltInsPlatform() = when {
    isCommon() -> BuiltInsPlatform.COMMON
    isNative() -> BuiltInsPlatform.NATIVE
    isJvm() -> BuiltInsPlatform.JVM
    isJs() -> BuiltInsPlatform.JS
    else -> throw IllegalArgumentException("Unknown platform $this")
}

private val VirtualFile.isLegacyNativeKlibComponent: Boolean
    get() {
        val irFolder = findChild(KLIB_IR_FOLDER_NAME)
        return irFolder != null && irFolder.children.isNotEmpty()
    }


fun <T> KotlinLibrary.readSafe(defaultValue: T, action: KotlinLibrary.() -> T) = try {
    action()
} catch (_: IOException) {
    defaultValue
}
