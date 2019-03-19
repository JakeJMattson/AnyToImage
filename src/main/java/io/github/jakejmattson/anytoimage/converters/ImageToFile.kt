package io.github.jakejmattson.anytoimage.converters

import io.github.jakejmattson.anytoimage.utils.*

import javax.imageio.ImageIO
import java.awt.image.*
import java.io.*
import java.nio.file.*
import java.util.*
import java.util.function.Consumer
import java.util.stream.Collectors

/**
 * Extract files from images created by the 'FileToImage' class.
 *
 * @author JakeJMattson
 */
object ImageToFile {

    /**
     * Static method to initiate the conversion.
     *
     * @param inputFiles
     * List of image files to be converted
     * @param outputDir
     * Directory to store all output files in
     */
    fun convert(inputFiles: List<File>, outputDir: File): Boolean {
        val validInput = inputFiles.filter { it.exists() }
        val validFiles = validInput.filter { FileUtils.validateFile(it) }.toMutableList()
        validInput.filter { it.isDirectory }.forEach { validFiles.addAll(FileUtils.walkDirectory(it).filter { FileUtils.validateFile(it) }) }

        var wasSuccessful = false

        for (file in inputFiles) {
            val pixels = extractPixels(file) ?: continue

            val allBytes = extractBytes(pixels)

            if (createFiles(allBytes, outputDir))
                wasSuccessful = true
        }

        return wasSuccessful
    }

    /**
     * Read an image from a file and extract pixels.
     *
     * @param file
     * File containing the image to be read
     *
     * @return Pixels from image
     */
    private fun extractPixels(file: File): IntArray? {
        var pixels: IntArray? = null

        try {
            val fileImage = ImageIO.read(file)
            val image = BufferedImage(fileImage.width, fileImage.height, BufferedImage.TYPE_INT_RGB)
            image.graphics.drawImage(fileImage, 0, 0, null)
            pixels = (image.raster.dataBuffer as DataBufferInt).data
        } catch (e: IOException) {
            DialogDisplay.displayException(e, "Failed to read image: $file")
        }

        return pixels
    }

    /**
     * Extract bytes from each pixel.
     *
     * @param pixels
     * Int array containing all pixels from the image
     *
     * @return Bytes
     */
    private fun extractBytes(pixels: IntArray): ByteArray {
        val stream = ByteArrayOutputStream()

        for (pixel in pixels)
            try {
                stream.write(ByteUtils.intToBytes(pixel, 3))
            } catch (e: IOException) {
                DialogDisplay.displayException(e, "Error writing array to stream!")
            }

        return stream.toByteArray()
    }

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
            allNewFiles.forEach(Consumer<File> { it.delete() })
            DialogDisplay.displayException(e, "Incorrectly encoded input image!")
        } catch (e: ArrayIndexOutOfBoundsException) {
            filesExtracted = false
            allNewFiles.forEach(Consumer<File> { it.delete() })
            DialogDisplay.displayException(e, "Incorrectly encoded input image!")
        } catch (e: IOException) {
            DialogDisplay.displayException(e, "Failed to create file: " + newFile!!.toString())
        }

        return filesExtracted
    }
}