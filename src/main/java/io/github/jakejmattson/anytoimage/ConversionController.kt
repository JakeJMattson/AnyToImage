package io.github.jakejmattson.anytoimage

import io.github.jakejmattson.anytoimage.utils.*
import io.github.jakejmattson.anytoimage.converters.*
import io.github.jakejmattson.anytoimage.gui.AnyToImage
import tornadofx.*
import java.io.File

fun main(args: Array<String>) {
    when {
        args.isEmpty() -> {
            isGraphical = true
            launch<AnyToImage>()
        }
        args.size >= 3 -> {
            shouldPrint = true

            val conversionType = Integer.parseInt(args.first())
            val input = args.toList().subList(1, args.lastIndex).map { File(it) }
            val output = File(args.last())

            when (conversionType) {
                0 -> convertFileToImage(input, output)
                1 -> convertImageToFile(input, output)
                else -> displayException(Exception(), "Unrecognized conversion type!")
            }
        }
        else -> displayException(Exception(), "Insufficient arguments!")
    }
}