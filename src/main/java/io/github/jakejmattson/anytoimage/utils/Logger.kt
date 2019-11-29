package io.github.jakejmattson.anytoimage.utils

import javafx.application.Platform
import javafx.scene.control.*
import javafx.scene.control.Alert.AlertType.*
import javafx.scene.layout.*

import java.io.*

enum class DisplayMode {
    CONSOLE, GRAPHICAL
}

class Logger {
    companion object {
        private lateinit var infoStream: TextArea

        var displayMode: DisplayMode = DisplayMode.CONSOLE

        fun initializeInfoStream(windowTitle: String) {
            val textArea = TextArea().apply {
                isEditable = false
            }

            GridPane.setVgrow(textArea, Priority.ALWAYS)
            GridPane.setHgrow(textArea, Priority.ALWAYS)

            val display = GridPane().apply {
                add(textArea, 0, 1)
            }

            infoStream = textArea

            createDialog(INFORMATION, windowTitle, "").apply {
                dialogPane.content = display
            }.show()
        }

        fun streamInfo(text: String) {
            when (displayMode) {
                DisplayMode.CONSOLE -> println(text)
                DisplayMode.GRAPHICAL -> Platform.runLater {
                    infoStream.appendText("$text\n")
                }
            }
        }

        fun displayError(title: String, message: String) {
            when (displayMode) {
                DisplayMode.CONSOLE -> println(message)
                DisplayMode.GRAPHICAL -> createDialog(ERROR, title, message).showAndWait()
            }
        }

        fun displayException(e: Exception, message: String) {
            when (displayMode) {
                DisplayMode.CONSOLE -> {
                    println(message)
                    e.printStackTrace()
                }
                DisplayMode.GRAPHICAL -> {
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
            }
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

        private fun createDialog(type: Alert.AlertType, title: String, message: String) =
            Alert(type, message).apply {
                this.title = title
                this.headerText = null
            }
    }
}

