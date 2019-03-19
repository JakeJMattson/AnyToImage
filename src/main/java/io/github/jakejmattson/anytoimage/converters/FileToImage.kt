package io.github.jakejmattson.anytoimage.converters

import io.github.jakejmattson.anytoimage.utils.*

import javax.imageio.ImageIO
import java.awt.image.BufferedImage
import java.io.*
import java.nio.file.*
import java.util.*
import java.util.stream.*

/**
 * Create images from files by converting bytes to RGB values.
 *
 * @author JakeJMattson
 */
object FileToImage {
    private val stream = ByteArrayOutputStream()
    private var image: BufferedImage? = null
    private var row: Int = 0
    private var col: Int = 0

    private const val CHANNEL_COUNT = 3

    /**
     * Static method to initiate the conversion.
     *
     * @param inputFiles
     * List of files to be converted
     * @param outputFile
     * Image file to be output when conversion is complete
     */
    fun convert(inputFiles: List<File>, outputFile: File): Boolean {
        val validInput = inputFiles.filter { it.exists() }

        val bytes = calculateBytesRequired(validInput)
        val dims = Math.ceil(Math.sqrt((bytes / CHANNEL_COUNT).toDouble())).toInt()

        if (dims == 0)
            return false

        //(Re)initialize fields
        image = BufferedImage(dims, dims, BufferedImage.TYPE_INT_RGB)
        row = 0
        col = 0

        for (file in validInput)
            if (file.isDirectory)
                directoryToBytes(file)
            else
                fileToBytes(file, file.name)

        finalizeStream()
        return saveImage(outputFile)
    }

    /**
     * Calculate the number of bytes needed to store all files in the list.
     *
     * @param inputFiles
     * List of files to be converted
     *
     * @return Number of bytes required
     */
    private fun calculateBytesRequired(inputFiles: List<File>): Int {
        var byteCount = 0

        for (file in inputFiles)
            if (file.isDirectory) {
                val parentDir = file.name
                val directoryFiles = FileUtils.walkDirectory(file)

                for (directoryFile in directoryFiles) {
                    val fullPath = directoryFile.toPath().toString()
                    val fileName = fullPath.substring(fullPath.indexOf(parentDir))

                    byteCount += calculateFileSize(directoryFile, fileName)
                }
            } else
                byteCount += calculateFileSize(file, file.name)

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
     * Collect all files from a directory (and sub-directories) and convert each to bytes.
     *
     * @param dir
     * Directory to extract bytes from
     */
    private fun directoryToBytes(dir: File) {
        val parentDir = dir.name
        val files = FileUtils.walkDirectory(dir)

        for (file in files) {
            val fullPath = file.toPath().toString()
            val fileName = fullPath.substring(fullPath.indexOf(parentDir))

            fileToBytes(file, fileName)
        }
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
            stream.write(fileName.length.toByte().toInt())
            stream.write(fileName.toByteArray())
            stream.write(ByteUtils.intToBytes(file.length().toInt(), 4))
            stream.write(Files.readAllBytes(file.toPath()))

            writeDataToImage()
        } catch (e: IOException) {
            DialogDisplay.displayException(e, "Unable to read file: $file")
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
            val pixel = ByteUtils.bytesToInt(pixelData)

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
        val pixel = ByteUtils.bytesToInt(finalPixelData)
        image!!.setRGB(row, col, pixel)

        stream.reset()
    }

    /**
     * Write image to disk.
     *
     * @param output
     * The desired location of the output image
     *
     * @return Whether or not the image was written to disk
     */
    private fun saveImage(output: File): Boolean {
        var status = false

        try {
            ImageIO.write(image!!, "png", output)
            status = true
        } catch (e: IOException) {
            DialogDisplay.displayException(e, "Error creating image!")
        } finally {
            image = null
        }

        return status
    }
}