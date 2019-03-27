package io.github.jakejmattson.anytoimage.utils

import javafx.scene.control.*
import javafx.scene.control.Alert.AlertType.*
import javafx.scene.layout.*

import java.io.*

var isGraphical = true
var shouldPrint = true

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

private fun createDialog(type: Alert.AlertType, title: String, message: String)=
    Alert(type, message).apply {
        this.title = title
        this.headerText = null
    }