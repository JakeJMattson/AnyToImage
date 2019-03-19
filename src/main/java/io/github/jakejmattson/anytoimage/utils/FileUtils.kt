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
fun walkDirectory(dir: File)=
    try {
        Files.walk(dir.toPath()).collect(Collectors.toList()).filter { Files.isRegularFile(it) }.map { it.toFile() }
    } catch (e: IOException) {
        DialogDisplay.displayException(e, "Unable to walk directory: $dir")
        emptyList<File>()
    }

fun validateFile(file: File): Boolean {
    val extension = fileFilter.extensions[0].toLowerCase().substring(1)
    return file.extension == extension
}

fun selectFile(title: String, shouldFilter: Boolean, isSave: Boolean): File {
    val chooser = FileChooser()
    chooser.title = title
    chooser.initialDirectory = defaultDirectory

    if (shouldFilter)
        chooser.extensionFilters.add(fileFilter)

    return if (isSave) chooser.showSaveDialog(null) else chooser.showOpenDialog(null)
}

fun selectDirectory(title: String): File {
    val chooser = DirectoryChooser()
    chooser.title = title
    chooser.initialDirectory = defaultDirectory
    return chooser.showDialog(null)
}