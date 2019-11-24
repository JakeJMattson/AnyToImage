package io.github.jakejmattson.anytoimage.utils

import javafx.scene.control.*
import javafx.scene.control.Alert.AlertType.*
import javafx.scene.layout.*

import java.io.*

var isGraphical = false
var shouldPrint = false

fun displayInfo(title: String, message: String) {
    if (shouldPrint) println(message)
    if (!isGraphical) return

    createDialog(INFORMATION, title, message).showAndWait()
}

fun displayError(title: String, message: String) {
    if (shouldPrint) println(message)
    if (!isGraphical) return

    createDialog(ERROR, title, message).showAndWait()
}

fun displayException(e: Exception, message: String) {
    if (shouldPrint) {
        println(message)
        e.printStackTrace()
    }

    if (!isGraphical)
        return

    val sw = StringWriter()
    e.printStackTrace(PrintWriter(sw))
    val exceptionText = sw.toString()

    val textArea = TextArea(exceptionText).apply {
        style = "-fx-text-inner-color: red;"
        isEditable = false
    }

    GridPane.setVgrow(textArea, Priority.ALWAYS)
    GridPane.setHgrow(textArea, Priority.ALWAYS)

    val expContent = GridPane().apply {
        add(Label("Exception stacktrace:"), 0, 0)
        add(textArea, 0, 1)
    }

    createDialog(ERROR, "Something went wrong!", message).apply {
        dialogPane.expandableContent = expContent
    }.showAndWait()
}

fun displayHelp() = println(
    """
        Supported command line arguments:

        0 Arguments - GUI mode
        -------------------------
            Running with no arguments will start the application in graphical mode.
            Messages to the user will appear as dialog boxes rather than printing to console.

        3+ Arguments - CLI mode
        -------------------------
            Arguments are accepted in this format: <Conversion Type> <Input Files...> <Output File>
                Conversion Type
                    0 - Convert input file(s) to an image
                    1 - Convert input image(s) to files

                Input Files
                    This can be any number of file or directory paths to convert.

                Output File
                    This is the path where the output will be stored when the conversion is complete.
    """.trimIndent()
)

private fun createDialog(type: Alert.AlertType, title: String, message: String)=
    Alert(type, message).apply {
        this.title = title
        this.headerText = null
    }