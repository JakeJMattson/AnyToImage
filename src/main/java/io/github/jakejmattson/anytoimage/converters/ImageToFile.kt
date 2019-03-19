package io.github.jakejmattson.anytoimage.converters

import io.github.jakejmattson.anytoimage.utils.*
import java.awt.image.*
import java.io.*
import java.nio.file.*
import java.util.ArrayList
import javax.imageio.ImageIO

fun convertImageToFile(inputFiles: List<File>, outputDir: File): Boolean {
    val validInput = inputFiles.filter { it.exists() }
    val validFiles = validInput.filter { FileUtils.validateFile(it) }.toMutableList()
    validInput.filter { it.isDirectory }.forEach { validFiles.addAll(FileUtils.walkDirectory(it).filter { FileUtils.validateFile(it) }) }

    var wasSuccessful = false

    validFiles.forEach { file ->
        val pixels = extractPixels(file) ?: return@forEach
        val allBytes = extractBytes(pixels)

        if (createFiles(allBytes, outputDir))
            wasSuccessful = true
    }

    return wasSuccessful
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
        DialogDisplay.displayException(e, "Failed to read image: $file")
        null
    }

/**
 * Extract bytes from each pixel.
 */
private fun extractBytes(pixels: IntArray) =
    ByteArrayOutputStream().apply {
        pixels.forEach { pixel ->
            write(ByteUtils.intToBytes(pixel, 3))
        }
    }.toByteArray()

/**
 * Create all files contained within the image.
 *
 * @param bytes
 * File names and data as a byte array
 * @param outputDir
 * Directory to store all output files in
 */
private fun createFiles(bytes: ByteArray, outputDir: File): Boolean {
    var filesExtracted = false
    var index = 0
    var newFile: File? = null
    val allNewFiles = ArrayList<File>()

    try {
        while (index != bytes.size) {
            val name = ByteArrayOutputStream()
            val data = ByteArrayOutputStream()

            //Calculate the number of bytes in each cluster (name/data)
            var sizeBytes = byteArrayOf(0, 0, bytes[index++])
            var clusterLength = ByteUtils.bytesToInt(sizeBytes)

            //EOF
            if (clusterLength == 0)
                break

            for (i in 0 until clusterLength)
                name.write(bytes[index++].toInt())

            sizeBytes = byteArrayOf(bytes[index++], bytes[index++], bytes[index++], bytes[index++])
            clusterLength = ByteUtils.bytesToInt(sizeBytes)

            for (i in 0 until clusterLength)
                data.write(bytes[index++].toInt())

            //Create file
            newFile = File(outputDir.toString() + File.separator + String(name.toByteArray()))
            val parentDir = newFile.parentFile
            allNewFiles.add(newFile)

            if (!parentDir.exists())
                parentDir.mkdirs()

            Files.write(newFile.toPath(), data.toByteArray())
            filesExtracted = true

            name.reset()
            data.reset()
        }
    } catch (e: InvalidPathException) {
        filesExtracted = false
        allNewFiles.forEach { it.delete() }
        DialogDisplay.displayException(e, "Incorrectly encoded input image!")
    } catch (e: ArrayIndexOutOfBoundsException) {
        filesExtracted = false
        allNewFiles.forEach { it.delete() }
        DialogDisplay.displayException(e, "Incorrectly encoded input image!")
    } catch (e: IOException) {
        DialogDisplay.displayException(e, "Failed to create file: " + newFile!!.toString())
    }

    return filesExtracted
}