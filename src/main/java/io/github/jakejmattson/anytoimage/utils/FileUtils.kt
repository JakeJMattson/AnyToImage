package io.github.jakejmattson.anytoimage.utils

import javafx.stage.*

import javax.swing.filechooser.FileSystemView
import java.io.*
import java.nio.file.*
import java.util.stream.Collectors

private val defaultDirectory = FileSystemView.getFileSystemView().defaultDirectory
internal val fileFilter = FileChooser.ExtensionFilter("*.png", "*.png", "*.PNG")

/**
 * Collect a list of files from a directory and all sub-directories.
 */
fun File.collectFiles() = this.walkTopDown().filterIsInstance(File::class.java).filter { it.isFile }

fun File.hasValidExtension(): Boolean {
    val validExtension = fileFilter.extensions[0].takeLast(3).toLowerCase()
    return this.extension.toLowerCase() == validExtension
}

fun createFileChooser(title: String, shouldFilter: Boolean) =
    FileChooser().apply {
        this.title = title
        this.initialDirectory = defaultDirectory

        if (shouldFilter)
            this.extensionFilters.add(fileFilter)
    }

fun createDirectoryChooser(title: String) =
    DirectoryChooser().apply {
        this.title = title
        this.initialDirectory = defaultDirectory
    }