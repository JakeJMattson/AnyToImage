package io.github.jakejmattson.anytoimage

import io.github.jakejmattson.anytoimage.utils.*
import io.github.jakejmattson.anytoimage.converters.*
import io.github.jakejmattson.anytoimage.gui.AnyToImage
import tornadofx.*
import java.io.File

fun main(args: Array<String>) {
    when (args.size) {
        0 -> {
            displayMode = DisplayMode.GRAPHICAL
            launch<AnyToImage>()
        }
        1 -> {
            if (args.first().toLowerCase() != "help")
                println("Invalid Argument Size.")

            displayHelp()
        }
        2 -> {
            println("Invalid Argument Size.")
            displayHelp()
        }
        else -> {
            val conversionType = args.first().toIntOrNull()
            val input = args.toList().subList(1, args.lastIndex).map { File(it) }
            val output = File(args.last())

            when (conversionType) {
                0 -> convertFileToImage(input, output)
                1 -> convertImageToFile(input, output)
                else -> {
                    println("Unrecognized conversion type!")
                    displayHelp()
                }
            }
        }
    }
}