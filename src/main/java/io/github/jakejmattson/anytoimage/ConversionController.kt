package io.github.jakejmattson.anytoimage

import io.github.jakejmattson.anytoimage.converters.*
import io.github.jakejmattson.anytoimage.gui.AnyToImage
import io.github.jakejmattson.anytoimage.utils.*
import tornadofx.*
import java.io.File

fun main(args: Array<String>) {
    when (args.size) {
        0 -> {
            Logger.displayMode = DisplayMode.GRAPHICAL
            launch<AnyToImage>()
        }
        1 -> {
            if (args.first().toLowerCase() != "help")
                println("Invalid Argument Size.")

            Logger.displayHelp()
        }
        2 -> {
            println("Invalid Argument Size.")
            Logger.displayHelp()
        }
        else -> {
            val conversionType = args.first().toIntOrNull()
            val input = args.toList().subList(1, args.lastIndex).map { File(it) }
            val output = File(args.last())

            when (conversionType) {
                0 -> {
                    Logger.initializeInfoStream("Encoding Files")
                    convertFileToImage(input, output)
                }
                1 -> {
                    Logger.initializeInfoStream("Decoding Files")
                    convertImageToFile(input, output)
                }
                else -> {
                    println("Unrecognized conversion type!")
                    Logger.displayHelp()
                }
            }
        }
    }
}