package io.github.jakejmattson.anytoimage

import io.github.jakejmattson.anytoimage.utils.*
import javafx.collections.FXCollections
import javafx.scene.control.*
import javafx.scene.input.*
import javafx.scene.layout.Pane
import javafx.stage.*

import java.io.*
import java.util.*

import io.github.jakejmattson.anytoimage.converters.*
import tornadofx.*

fun main(args: Array<String>) {
    if (args.size >= 3) {
        shouldPrint = true

        val conversionType = Integer.parseInt(args[0])
        val input = ArrayList<File>()

        for (i in 1 until args.size - 1)
            input.add(File(args[i]))

        val output = File(args[args.size - 1])

        if (conversionType == 0)
            convertFileToImage(input, output)
        else if (conversionType == 1)
            convertImageToFile(input, output)
        else
            displayException(Exception(), "Unrecognized conversion type!")
    } else if (args.size == 0) {
        isGraphical = true
        launch<AnyToImage>()
    } else
        displayException(Exception(), "Insufficient arguments!")
}

class AnyToImage : App() {
    override val primaryView = ConversionController::class
    override fun start(stage: Stage) {
        super.start(stage)
        stage.isResizable = false
    }
}

class ConversionController : View("AnyToImage") {
    override val root: Pane by fxml("/ConversionView.fxml")

    private val btnAddFile: Button? by fxid()
    private val btnAddDirectory: Button? by fxid()
    private val btnOutput: Button? by fxid()
    private val btnRemove: Button? by fxid()
    private val btnSubmit: Button? by fxid()
    private val btnClear: Button? by fxid()
    private val radFiles: RadioButton? by fxid()
    private val radImage: RadioButton? by fxid()
    private val dndPane: Pane? by fxid()
    private val lstInputs: ListView<String>? by fxid()
    private val txtOutput: TextField? by fxid()
    private val lblDirectionArrow: Label? by fxid()

    private val inputFiles = ArrayList<File>()
    private var outputFile: File? = null

    init {
        lstInputs!!.setItems(FXCollections.observableArrayList<String>())

        addEvents()
        updateState(true)
    }

    private fun addEvents() {
        //IO buttons
        btnAddFile!!.setOnAction { event -> addFile() }
        btnAddDirectory!!.setOnAction { event -> addDirectory() }
        btnOutput!!.setOnAction { event -> setOutput() }

        //Action buttons
        btnRemove!!.setOnAction { event -> removeSelection() }
        btnSubmit!!.setOnAction { event -> convertInput() }
        btnClear!!.setOnAction { event -> clearAll() }

        //Conversion direction
        radFiles!!.setOnAction { event -> updateState(true) }
        radImage!!.setOnAction { event -> updateState(false) }

        //Prepare drag and drop pane to receive files
        createDragHandler()
    }

    private fun addFile() {
        val selection = createFileChooser("Add input file", radImage!!.isSelected).showOpenDialog(null)

        if (selection != null) {
            inputFiles.add(selection)
            lstInputs!!.items.add(selection.name)
        }
    }

    private fun addDirectory() {
        val selection = createDirectoryChooser("Add input directory").showDialog(null)

        if (selection != null) {
            inputFiles.add(selection)
            lstInputs!!.items.add(selection.name)
        }
    }

    private fun setOutput() {
        val selection: File?

        if (radFiles!!.isSelected)
            selection = createFileChooser("Create an output file", true).showSaveDialog(null)
        else
            selection = createDirectoryChooser("Select an output directory").showDialog(null)

        if (selection != null) {
            outputFile = selection
            txtOutput!!.text = outputFile!!.absolutePath
        }
    }

    private fun removeSelection() {
        val index = lstInputs!!.selectionModel.selectedIndex

        if (index == -1)
            return

        inputFiles.removeAt(index)
        lstInputs!!.items.removeAt(index)
    }

    private fun convertInput() {
        if (!validateConversion())
            return

        val infoTitle = "Operation successful!"
        val errorTitle = "Operation failed!"

        if (radFiles!!.isSelected) {
            val wasSuccessful = convertFileToImage(inputFiles, outputFile!!)

            if (wasSuccessful)
                displayInfo(infoTitle, "Image created from files.")
            else
                displayError(errorTitle, "Image not created due to errors.")
        } else {
            val wasSuccessful = convertImageToFile(inputFiles, outputFile!!)

            if (wasSuccessful)
                displayInfo(infoTitle, "Files extracted from image.")
            else
                displayError(errorTitle, "Unable to extract any files.")
        }
    }

    private fun validateConversion(): Boolean {
        val title = "Incomplete field"

        if (inputFiles.isEmpty()) {
            displayError(title, "Please add input files to continue.")
            return false
        }

        if (outputFile == null) {
            displayError(title, "Please specify the output to continue.")
            return false
        }

        return true
    }

    private fun clearAll() {
        inputFiles.clear()
        outputFile = null
        lstInputs!!.items.clear()
        txtOutput!!.clear()
    }

    private fun updateState(isFileConversion: Boolean) {
        clearAll()

        btnSubmit!!.text = if (isFileConversion) "Create Image" else "Extract Files"
        lblDirectionArrow!!.text = if (isFileConversion) "  ->   " else "  <-   "
    }

    private fun createDragHandler() {
        dndPane!!.setOnDragOver { event ->
            if (event.dragboard.hasFiles())
                event.acceptTransferModes(TransferMode.COPY)
        }

        dndPane!!.setOnDragDropped { event ->
            val dragboard = event.dragboard
            var success = false

            if (dragboard.hasFiles()) {
                var droppedFiles = dragboard.files

                if (radImage!!.isSelected)
                    droppedFiles = droppedFiles.filter { file -> file.isDirectory || file.hasValidExtension() }

                for (file in droppedFiles) {
                    inputFiles.add(file)
                    lstInputs!!.items.add(file.name)
                }

                success = true
            }

            event.isDropCompleted = success
        }

        dndPane!!.setOnDragEntered { event ->
            val style = "-fx-border-style: dashed; -fx-border-color: " + if (event.dragboard.hasFiles()) "lime" else "red"
            dndPane!!.style = style
        }

        dndPane!!.setOnDragExited { event -> dndPane!!.style = "-fx-border-style: dashed; -fx-border-color: black" }
    }
}