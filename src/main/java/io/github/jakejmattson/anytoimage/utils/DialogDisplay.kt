package io.github.jakejmattson.anytoimage.utils

import javafx.scene.control.*
import javafx.scene.layout.*

import java.io.*

var isGraphical = true
var shouldPrint = true

fun displayInfo(title: String, message: String) {
    if (shouldPrint)
        println(message)

    if (!isGraphical)
        return

    val dialog = createDialog(Alert.AlertType.INFORMATION, title, message)
    dialog.showAndWait()
}

fun displayError(title: String, message: String) {
    if (shouldPrint)
        println(message)

    if (!isGraphical)
        return

    val dialog = createDialog(Alert.AlertType.ERROR, title, message)
    dialog.showAndWait()
}

fun displayException(e: Exception, message: String) {
    if (shouldPrint) {
        println(message)
        e.printStackTrace()
    }

    if (!isGraphical)
        return

    val alert = createDialog(Alert.AlertType.ERROR, "Something went wrong!", message)

    val sw = StringWriter()
    e.printStackTrace(PrintWriter(sw))
    val exceptionText = sw.toString()

    val textArea = TextArea(exceptionText)
    textArea.style = "-fx-text-inner-color: red;"
    textArea.isEditable = false

    GridPane.setVgrow(textArea, Priority.ALWAYS)
    GridPane.setHgrow(textArea, Priority.ALWAYS)
    val expContent = GridPane()
    expContent.add(Label("Exception stacktrace:"), 0, 0)
    expContent.add(textArea, 0, 1)

    alert.dialogPane.expandableContent = expContent
    alert.showAndWait()
}

private fun createDialog(type: Alert.AlertType, title: String, message: String): Alert {
    val dialog = Alert(type, message)
    dialog.title = title
    dialog.headerText = null

    return dialog
}