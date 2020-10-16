package me.jakejmattson.anytoimage.gui

import javafx.collections.FXCollections.observableArrayList
import javafx.scene.control.*
import javafx.scene.input.TransferMode
import javafx.scene.layout.Pane
import me.jakejmattson.anytoimage.converters.*
import me.jakejmattson.anytoimage.utils.*
import tornadofx.*
import java.io.File

class PrimaryView : View("AnyToImage") {
    private lateinit var lstInputs: ListView<String>
    private lateinit var txtOutput: TextField
    private lateinit var radFiles: RadioButton
    private lateinit var radImage: RadioButton
    private val inputFiles = mutableListOf<File>()
    private var outputFile: File? = null

    override val root = pane {
        prefHeight = 288.0
        prefWidth = 560.0

        splitpane {
            lookupAll(".split-pane-divider").forEach { it.isMouseTransparent = true }
            prefHeight = 218.0
            prefWidth = 570.0

            anchorpane {
                prefHeight = 210.0
                prefWidth = 285.0

                pane {
                    layoutX = 11.0
                    layoutY = 8.0
                    prefHeight = 160.0
                    prefWidth = 259.0
                    style = "-fx-border-style: dashed;"
                    setDragEvents()

                    label {
                        layoutX = 84.0
                        layoutY = 70.0
                        text = "Drag and Drop"
                    }
                }

                button {
                    layoutX = 11.0
                    layoutY = 174.0
                    prefHeight = 33.0
                    prefWidth = 128.0
                    text = "Add File"

                    setOnAction {
                        val selection = createFileChooser("Add input file", radImage.isSelected).showOpenDialog(null)
                            ?: return@setOnAction
                        addInput(selection)
                    }
                }

                button {
                    layoutX = 141.0
                    layoutY = 174.0
                    prefHeight = 33.0
                    prefWidth = 128.0
                    text = "Add Directory"

                    setOnAction {
                        val selection = createDirectoryChooser("Add input directory").showDialog(null)
                            ?: return@setOnAction
                        addInput(selection)
                    }
                }
            }

            anchorpane {
                prefHeight = 282.0
                prefWidth = 285.0

                button {
                    layoutX = 11.0
                    layoutY = 174.0
                    prefHeight = 33.0
                    prefWidth = 259.0
                    text = "Remove Selected"

                    setOnAction {
                        val index = lstInputs.selectionModel.selectedIndex.takeUnless { it == -1 } ?: return@setOnAction

                        inputFiles.removeAt(index)
                        lstInputs.items.removeAt(index)
                    }
                }

                lstInputs = listview {
                    layoutX = 11.0
                    layoutY = 7.0
                    prefHeight = 160.0
                    prefWidth = 260.0
                    items = observableArrayList()

                    isEditable = false
                }
            }
        }

        toolbar {
            layoutX = 300.0
            layoutY = 255.0
            prefHeight = 44.0
            prefWidth = 270.0

            button {
                prefHeight = 25.0
                prefWidth = 125.0
                text = "Convert"

                setOnAction {
                    convertInput()
                }
            }

            button {
                layoutX = 10.0
                layoutY = 16.0
                prefHeight = 25.0
                prefWidth = 125.0
                text = "Clear"

                setOnAction {
                    clearAll()
                }
            }
        }

        button {
            layoutX = 436.0
            layoutY = 230.0
            prefHeight = 25.0
            prefWidth = 125.0
            text = "Select Output"

            setOnAction {
                setOutput()
            }
        }

        txtOutput = textfield {
            isEditable = false
            layoutX = 5.0
            layoutY = 230.0
            prefHeight = 25.0
            prefWidth = 427.0
            promptText = "Output Path"
        }

        toolbar {
            layoutY = 257.0
            prefHeight = 42.0
            prefWidth = 303.0

            label {
                text = "Conversion:"
            }

            togglegroup {
                radFiles = radiobutton {
                    isSelected = true
                    text = "Files to Image"

                    setOnAction {
                        clearAll()
                    }
                }

                radImage = radiobutton {
                    text = "Image to Files"

                    setOnAction {
                        clearAll()
                    }
                }
            }
        }
    }

    private fun addInput(file: File) {
        cleanInput()

        if (!file.exists())
            return

        if (file in inputFiles)
            return

        inputFiles.add(file)
        lstInputs.items.add(file.name)
    }

    private fun setOutput() {
        val selection =
            if (radFiles.isSelected)
                createFileChooser("Create an output file", true).showSaveDialog(null)
            else
                createDirectoryChooser("Select an output directory").showDialog(null)

        if (selection != null) {
            outputFile = selection
            txtOutput.text = outputFile!!.absolutePath
        }
    }

    private fun convertInput() {
        if (!validateConversion())
            return

        if (radFiles.isSelected) {
            Logger.initializeInfoStream("Encoding Files")
            convertFileToImage(inputFiles, outputFile!!)
        } else {
            Logger.initializeInfoStream("Decoding Files")
            convertImageToFile(inputFiles, outputFile!!)
        }
    }

    private fun validateConversion(): Boolean {
        val title = "Incomplete field"

        if (inputFiles.isEmpty()) {
            Logger.displayError(title, "Please input files to continue.")
            return false
        }

        if (outputFile == null) {
            Logger.displayError(title, "Please specify the output to continue.")
            return false
        }

        return true
    }

    private fun clearAll() {
        inputFiles.clear()
        outputFile = null
        lstInputs.items.clear()
        txtOutput.clear()
    }

    private fun Pane.setDragEvents() {
        setOnDragOver {
            if (it.dragboard.hasFiles())
                it.acceptTransferModes(TransferMode.COPY)
        }

        setOnDragDropped { event ->
            val droppedFiles = with(event.dragboard) {
                if (radImage.isSelected)
                    files.filter { it.isDirectory || it.hasValidImageExtension() }
                else
                    files
            }

            droppedFiles.forEach { file ->
                addInput(file)
            }
        }

        setOnDragEntered {
            val color = if (it.dragboard.hasFiles()) "lime" else "red"
            style = "-fx-border-style: dashed; -fx-border-color: $color"
        }

        setOnDragExited {
            style = "-fx-border-style: dashed; -fx-border-color: black"
        }
    }

    private fun cleanInput() {
        val zipped = inputFiles.zip(lstInputs.items)

        zipped.forEach { (file, name) ->
            if (!file.exists()) {
                inputFiles.remove(file)
                lstInputs.items.remove(name)
            }
        }
    }
}