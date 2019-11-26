package io.github.jakejmattson.anytoimage.utils

import javafx.stage.*

import javax.swing.filechooser.FileSystemView
import java.io.*

private val defaultDirectory = FileSystemView.getFileSystemView().defaultDirectory
private const val imageExtension = "png"

/**
 * Collect a list of files from a directory and all sub-directories.
 */
fun File.collectFiles() = walkTopDown().filterIsInstance(File::class.java).filter { it.isFile }

fun File.hasValidImageExtension() = extension.toLowerCase() == imageExtension

fun createFileChooser(title: String, shouldFilter: Boolean) =
    FileChooser().apply {
        this.title = title
        this.initialDirectory = defaultDirectory

        if (shouldFilter) {
            val fileFilter = FileChooser.ExtensionFilter("*.png", "*.png", "*.PNG")
            extensionFilters.add(fileFilter)
        }
    }

fun createDirectoryChooser(title: String) =
    DirectoryChooser().apply {
        this.title = title
        this.initialDirectory = defaultDirectory
    }