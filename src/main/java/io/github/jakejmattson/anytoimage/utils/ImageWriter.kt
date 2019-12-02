package io.github.jakejmattson.anytoimage.utils

import java.awt.image.BufferedImage
import java.io.*
import javax.imageio.ImageIO

class ImageWriter(dimensions: Int) {
    private var image: BufferedImage = BufferedImage(dimensions, dimensions, BufferedImage.TYPE_INT_RGB)
    private var row: Int = 0
    private var col: Int = 0

    fun writePixel(pixel: Int) {
        image.setRGB(row, col, pixel)
        row++

        if (row == image.width) {
            row = 0
            col++
        }
    }

    fun saveImage(outputFile: File) {
        try {
            ImageIO.write(image, "png", outputFile)
        } catch (e: IOException) {
            Logger.displayException(e, "Error creating image!")
        }
    }
}