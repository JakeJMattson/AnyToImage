package io.github.jakejmattson.anytoimage.converters

import io.github.jakejmattson.anytoimage.utils.*
import kotlinx.coroutines.*
import java.io.*
import java.nio.file.Files
import kotlin.math.*

private val stream = ByteArrayOutputStream()
private lateinit var writer: ImageWriter
private var totalBytes: Int = 0

private const val CHANNEL_COUNT = 3

fun convertFileToImage(inputFiles: List<File>, outputFile: File) {
    val validInput = inputFiles.filter { it.exists() }
    totalBytes = calculateBytesRequired(validInput)
    val dims = ceil(sqrt((totalBytes / CHANNEL_COUNT).toDouble())).toInt()

    if (dims == 0) {
        Logger.displayError("Fatal Error", "No valid input files.")
        return
    }

    writer = ImageWriter(dims)

    GlobalScope.launch {
        validInput.forEach { file ->
            if (file.isDirectory)
                directoryToBytes(file)
            else
                fileToBytes(file, file.name)
        }

        finalizeStream()
        writer.saveImage(outputFile)
        Logger.streamInfo("Process complete.", 100.0)
    }
}

/**
 * Calculate the number of bytes needed to store all files in the list.
 */
private fun calculateBytesRequired(files: List<File>) =
    files.sumBy { inputFile ->
        if (inputFile.isDirectory)
            inputFile.collectFiles().sumBy { file ->
                val fullPath = file.path
                val fileName = fullPath.substring(fullPath.indexOf(inputFile.name))

                calculateFileSize(file, fileName)
            }
        else
            calculateFileSize(inputFile, inputFile.name)
    }

/**
 * Calculate the number of bytes needed to store and recreate a file.
 */
private fun calculateFileSize(file: File, fileName: String) = fileName.toByteArray().size + file.length().toInt() + 5

/**
 * Collect all files from a directory (and sub-directories) and convert each to bytes.
 */
private fun directoryToBytes(dir: File) =
    dir.collectFiles().forEach {
        val fullPath = it.path
        val fileName = fullPath.substring(fullPath.indexOf(dir.name))

        fileToBytes(it, fileName)
    }

/**
 * Collect all necessary file information as bytes and write it into the stream.
 */
private fun fileToBytes(file: File, fileName: String) {
    try {
        with(stream) {
            write(fileName.length)
            write(fileName.toByteArray())
            write(file.length().toInt().extractBytes(4))
            write(Files.readAllBytes(file.toPath()))
        }

        writeDataToImage()
        Logger.streamInfo(fileName, stream.size() / totalBytes.toDouble())
    } catch (e: IOException) {
        Logger.displayException(e, "Unable to read file: $file")
    }
}

/**
 * Create pixels from stream data and write them onto the image; remove data from stream.
 */
private fun writeDataToImage() {
    val fileBytes = stream.toByteArray()
    var index = 0

    while (index < fileBytes.size - CHANNEL_COUNT) {
        val pixelData = byteArrayOf(fileBytes[index], fileBytes[index + 1], fileBytes[index + 2])
        val pixel = pixelData.bytesToInt()

        writer.writePixel(pixel)
        index += CHANNEL_COUNT
    }

    stream.reset()

    while (index < fileBytes.size) {
        stream.write(fileBytes[index].toInt())
        index++
    }
}

/**
 * Write remaining stream data onto the image and reset the stream.
 */
private fun finalizeStream() {
    val finalPixelData = ByteArray(CHANNEL_COUNT)
    val partialPixel = stream.toByteArray()
    System.arraycopy(partialPixel, 0, finalPixelData, 0, partialPixel.size)
    val pixel = finalPixelData.bytesToInt()

    writer.writePixel(pixel)
    stream.reset()
}