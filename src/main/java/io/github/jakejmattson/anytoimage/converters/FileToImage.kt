package io.github.jakejmattson.anytoimage.converters

import io.github.jakejmattson.anytoimage.utils.*
import java.awt.image.BufferedImage
import java.io.*
import java.nio.file.Files
import javax.imageio.ImageIO

private val stream = ByteArrayOutputStream()
private var image: BufferedImage? = null
private var row: Int = 0
private var col: Int = 0

private const val CHANNEL_COUNT = 3

fun convertFileToImage(inputFiles: List<File>, outputFile: File): Boolean {
    val validInput = inputFiles.filter { it.exists() }

    val bytes = calculateBytesRequired(validInput)
    val dims = Math.ceil(Math.sqrt((bytes / CHANNEL_COUNT).toDouble())).toInt()

    if (dims == 0)
        return false

    //(Re)initialize fields
    image = BufferedImage(dims, dims, BufferedImage.TYPE_INT_RGB)
    row = 0
    col = 0

    validInput.forEach { file ->
        if (file.isDirectory)
            directoryToBytes(file)
        else
            fileToBytes(file, file.name)
    }

    finalizeStream()
    return saveImage(outputFile)
}

/**
 * Calculate the number of bytes needed to store all files in the list.
 *
 * @param files
 * List of files to be converted
 *
 * @return Number of bytes required
 */
private fun calculateBytesRequired(files: List<File>): Int {
    var byteCount = 0

    files.forEach { inputFile ->
        if (inputFile.isDirectory) {
            val parentDir = inputFile.name

            inputFile.collectFiles().forEach { file ->
                val fullPath = file.path
                val fileName = fullPath.substring(fullPath.indexOf(parentDir))

                byteCount += calculateFileSize(file, fileName)
            }
        } else
            byteCount += calculateFileSize(inputFile, inputFile.name)
    }

    return byteCount
}

/**
 * Calculate the number of bytes needed to store and recreate a file.
 *
 * @param file
 * File to be sized
 * @param fileName
 * Name of file to be sized
 *
 * @return Number of bytes
 */
private fun calculateFileSize(file: File, fileName: String): Int {
    val SIZE_BYTES = 5
    return (fileName.toByteArray().size.toLong() + file.length() + SIZE_BYTES.toLong()).toInt()
}

/**
 * Collect all files from a directory (and sub-directories) and convertImageToFile each to bytes.
 *
 * @param dir
 * Directory to extract bytes from
 */
private fun directoryToBytes(dir: File) =
    dir.collectFiles().forEach {
        val fullPath = it.path
        val fileName = fullPath.substring(fullPath.indexOf(dir.name))

        fileToBytes(it, fileName)
    }

/**
 * Collect all necessary file information as bytes and write it into the stream.
 *
 * @param file
 * File to extract bytes from
 * @param fileName
 * Name of file (with folder structure if file was in directory)
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
    } catch (e: IOException) {
        displayException(e, "Unable to read file: $file")
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

        image!!.setRGB(row, col, pixel)
        row++

        if (row == image!!.width) {
            row = 0
            col++
        }
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
    image!!.setRGB(row, col, pixel)

    stream.reset()
}

private fun saveImage(output: File) =
    try {
        ImageIO.write(image!!, "png", output)
        true
    } catch (e: IOException) {
        displayException(e, "Error creating image!")
        false
    } finally {
        image = null
    }