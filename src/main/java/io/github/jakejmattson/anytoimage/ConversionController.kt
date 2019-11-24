package io.github.jakejmattson.anytoimage

import io.github.jakejmattson.anytoimage.utils.*
import io.github.jakejmattson.anytoimage.converters.*
import io.github.jakejmattson.anytoimage.gui.AnyToImage
import tornadofx.*
import java.io.File

fun main(args: Array<String>) {
    when (args.size) {
        0 -> {
            isGraphical = true
            launch<AnyToImage>()
        }
        1 -> {
            if (args.first().toLowerCase() != "help")
                println("Unknown Argument. Expected: help")

            displayHelp()
        }
        2 -> {
            println("Invalid Argument Size. Expected 3 or more.")
            displayHelp()
        }
        else -> {
            shouldPrint = true

            val conversionType = args.first().toIntOrNull()
            val input = args.toList().subList(1, args.lastIndex).map { File(it) }
            val output = File(args.last())

            when (conversionType) {
                0 -> convertFileToImage(input, output)
                1 -> convertImageToFile(input, output)
                else -> displayException(Exception(), "Unrecognized conversion type!")
            }
        }
    }
}