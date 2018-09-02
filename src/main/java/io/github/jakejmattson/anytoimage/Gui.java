package io.github.jakejmattson.anytoimage;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.fxml.*;
import javafx.scene.*;
import javafx.scene.control.*;
import javafx.scene.input.*;
import javafx.scene.layout.Pane;
import javafx.stage.*;

import java.io.*;
import java.util.*;

public class Gui extends Application
{
	@FXML Button btnAddFile, btnAddDirectory, btnOutput, btnRemove, btnSubmit, btnClear;
	@FXML RadioButton radFiles, radImage;
	@FXML Pane dndPane;
	@FXML ListView<String> lstInputs;
	@FXML TextField txtOutput;
	@FXML Label lblDirectionArrow;

	//Backing fields
	private List<File> inputFiles = new ArrayList<>();
	private File outputFile;

	private static final FileChooser.ExtensionFilter pngFilter =
			new FileChooser.ExtensionFilter("*.png", "*.png", "*.PNG");

	public static void main(String[] args)
	{
		launch(args);
	}

	@Override
	public void start(Stage primaryStage) throws IOException
	{
		Parent root = FXMLLoader.load(getClass().getResource("/Gui.fxml"));
		primaryStage.setTitle("AnyToImage");
		primaryStage.setScene(new Scene(root));
		primaryStage.setResizable(false);
		primaryStage.show();
	}

	@FXML
	public void initialize()
	{
		lstInputs.setItems(FXCollections.observableArrayList());

		addEvents();
		updateState(true);
	}

	private void addEvents()
	{
		//IO buttons
		btnAddFile.setOnAction(event -> addFile());
		btnAddDirectory.setOnAction(event -> addDirectory());
		btnOutput.setOnAction(event -> getOutput());

		//Action buttons
		btnRemove.setOnAction(event -> removeSelection());
		btnSubmit.setOnAction(event -> convertInput());
		btnClear.setOnAction(event -> clearAll());

		//Conversion direction
		radFiles.setOnAction(event -> updateState(true));
		radImage.setOnAction(event -> updateState(false));

		//Prepare drag and drop pane to receive files
		createDragHandler();
	}

	private void addFile()
	{
		FileChooser chooser = new FileChooser();
		chooser.setTitle("Add input file");

		if (radFiles.isSelected())
			chooser.getExtensionFilters().add(pngFilter);

		File selection = chooser.showOpenDialog(null);

		if (selection != null)
		{
			inputFiles.add(selection);
			lstInputs.getItems().add(selection.getName());
		}
	}

	private void addDirectory()
	{
		DirectoryChooser chooser = new DirectoryChooser();
		chooser.setTitle("Add input directory");
		File selection = chooser.showDialog(null);

		if (selection != null)
		{
			inputFiles.add(selection);
			lstInputs.getItems().add(selection.getName());
		}
	}

	private void getOutput()
	{
		File selection;

		if (radFiles.isSelected())
		{
			FileChooser chooser = new FileChooser();
			chooser.setTitle("Create an output file");
			chooser.getExtensionFilters().add(pngFilter);
			selection = chooser.showSaveDialog(null);
		}
		else
		{
			DirectoryChooser chooser = new DirectoryChooser();
			chooser.setTitle("Select an output directory");
			selection = chooser.showDialog(null);
		}

		if (selection != null)
		{
			outputFile = selection;
			txtOutput.setText(outputFile.getAbsolutePath());
		}
	}

	private void removeSelection()
	{
		int index = lstInputs.getSelectionModel().getSelectedIndex();

		if (index == -1)
			return;

		inputFiles.remove(index);
		lstInputs.getItems().remove(index);
	}

	private void convertInput()
	{
		if (!validateConversion())
			return;

		if (radFiles.isSelected())
			FileToImage.convert(inputFiles, outputFile, true);
		else
			ImageToFile.convert(inputFiles, outputFile, true);
	}

	private boolean validateConversion()
	{
		Alert alert = new Alert(Alert.AlertType.ERROR);
		alert.setTitle("Incomplete field");
		alert.setHeaderText(null);

		if (inputFiles.size() == 0)
		{
			alert.setContentText("Please add input files to continue.");
			alert.showAndWait();
			return false;
		}

		if (outputFile == null)
		{
			alert.setContentText("Please specify the output to continue.");
			alert.showAndWait();
			return false;
		}

		return true;
	}

	private void clearAll()
	{
		//Clear data
		inputFiles.clear();
		outputFile = null;

		//Clear GUI
		lstInputs.getItems().clear();
		txtOutput.clear();
	}

	private void updateState(boolean isFileConversion)
	{
		clearAll();

		btnSubmit.setText(isFileConversion ? "Create Image" : "Extract Files");
		lblDirectionArrow.setText(isFileConversion ? "  ->   " : "  <-   ");
	}

	private void createDragHandler()
	{
		dndPane.setOnDragOver(event ->
		{
			if (event.getDragboard().hasFiles())
				event.acceptTransferModes(TransferMode.COPY);

			event.consume();
		});

		dndPane.setOnDragDropped(event ->
		{
			Dragboard dragboard = event.getDragboard();
			boolean success = false;

			if (dragboard.hasFiles())
			{
				List<File> droppedFiles = dragboard.getFiles();

				for (File file : droppedFiles)
				{
					inputFiles.add(file);
					lstInputs.getItems().add(file.getName());
				}

				success = true;
			}

			event.setDropCompleted(success);
			event.consume();
		});

		dndPane.setOnDragEntered(event ->
		{
			String style = "-fx-border-style: dashed; -fx-border-color: ";
			style += event.getDragboard().hasFiles() ? "lime" : "red";
			dndPane.setStyle(style);

			event.consume();
		});

		dndPane.setOnDragExited(event ->
		{
			dndPane.setStyle("-fx-border-style: dashed; -fx-border-color: black");

			event.consume();
		});
	}
}