package io.github.jakejmattson.anytoimage.converters

import io.github.jakejmattson.anytoimage.utils.*
import kotlinx.coroutines.*
import java.awt.image.*
import java.io.*
import java.nio.file.Files
import java.util.ArrayList
import javax.imageio.ImageIO

fun convertImageToFile(inputFiles: List<File>, outputDir: File) {
    val validInput = inputFiles.filter { it.exists() }
    val validFiles = validInput.filter { it.hasValidImageExtension() }.toMutableList()
    validInput.filter { it.isDirectory }.forEach { validFiles.addAll(it.collectFiles().filter { it.hasValidImageExtension() }) }

    GlobalScope.launch {
        validFiles.forEach { file ->
            val pixels = extractPixels(file) ?: return@forEach
            val allBytes = extractBytes(pixels)

            createFiles(allBytes, outputDir)
        }

        Logger.streamInfo("Process complete.", 1.0)
    }
}

/**
 * Read an image from a file and extract pixels.
 */
private fun extractPixels(file: File) =
    try {
        val fileImage = ImageIO.read(file)
        val image = BufferedImage(fileImage.width, fileImage.height, BufferedImage.TYPE_INT_RGB)
        image.graphics.drawImage(fileImage, 0, 0, null)
        (image.raster.dataBuffer as DataBufferInt).data
    } catch (e: IOException) {
        Logger.displayException(e, "Failed to read image: $file")
        null
    }

/**
 * Extract bytes from each pixel.
 */
private fun extractBytes(pixels: IntArray) =
    ByteArrayOutputStream().apply {
        pixels.forEach { pixel ->
            write(pixel.extractBytes(3))
        }
    }.toByteArray()

/**
 * Create all files contained within the image.
 */
private fun createFiles(bytes: ByteArray, outputDir: File) {
    var index = 0
    val fileData = ArrayList<Pair<ByteArray, ByteArray>>()

    try {
        while (index != bytes.size) {
            val nameLength = byteArrayOf(0, 0, bytes[index++]).bytesToInt().takeUnless { it == 0 } ?: break
            val name = bytes.sliceArray(index until index + nameLength).also { index += nameLength }
            val dataLength = byteArrayOf(bytes[index++], bytes[index++], bytes[index++], bytes[index++]).bytesToInt()
            val data = bytes.sliceArray(index until index + dataLength).also { index += dataLength }

            fileData.add(name to data)
        }
    } catch (e: RuntimeException) {
        Logger.displayException(e, "Incorrectly encoded input image!")
        return
    }

    if (fileData.isEmpty()) return

    fileData.forEach {
        val newFile = File(outputDir.toString() + File.separator + String(it.first))
        newFile.parentFile.mkdirs()

        Files.write(newFile.toPath(), it.second)
        Logger.streamInfo(newFile.toPath().toString(), 0.0)
    }
}