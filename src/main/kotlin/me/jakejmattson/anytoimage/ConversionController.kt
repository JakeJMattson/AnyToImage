package me.jakejmattson.anytoimage

import me.jakejmattson.anytoimage.converters.convertFileToImage
import me.jakejmattson.anytoimage.converters.convertImageToFile
import me.jakejmattson.anytoimage.gui.AnyToImage
import me.jakejmattson.anytoimage.utils.DisplayMode
import me.jakejmattson.anytoimage.utils.Logger
import tornadofx.launch
import java.io.File

fun main(args: Array<String>) {
    when (args.size) {
        0 -> {
            Logger.displayMode = DisplayMode.GRAPHICAL
            launch<AnyToImage>()
        }
        1 -> {
            if (args.first().lowercase() != "help")
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