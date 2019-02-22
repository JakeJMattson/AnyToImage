package io.github.jakejmattson.anytoimage;

import io.github.jakejmattson.anytoimage.converters.*;
import io.github.jakejmattson.anytoimage.utils.*;
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
import java.util.stream.Collectors;

public class ConversionController extends Application
{
	@FXML private Button btnAddFile;
	@FXML private Button btnAddDirectory;
	@FXML private Button btnOutput;
	@FXML private Button btnRemove;
	@FXML private Button btnSubmit;
	@FXML private Button btnClear;
	@FXML private RadioButton radFiles;
	@FXML private RadioButton radImage;
	@FXML private Pane dndPane;
	@FXML private ListView<String> lstInputs;
	@FXML private TextField txtOutput;
	@FXML private Label lblDirectionArrow;

	private List<File> inputFiles = new ArrayList<>();
	private File outputFile;

	public static void main(String[] args)
	{
		if (args.length >= 3)
		{
			DialogDisplay.isGraphical = false;

			int conversionType = Integer.parseInt(args[0]);
			List<File> input = new ArrayList<>();

			for (int i = 1; i < args.length - 1; i++)
				input.add(new File(args[i]));

			File output = new File(args[args.length - 1]);

			if (conversionType == 0)
				FileToImage.convert(input, output);
			else if (conversionType == 1)
				ImageToFile.convert(input, output);
			else
				DialogDisplay.displayException(new Exception(), "Unrecognized conversion type!");
		}
		else if (args.length == 0)
			launch(args);
		else
			DialogDisplay.displayException(new Exception(), "Insufficient arguments!");
	}

	@Override
	public void start(Stage primaryStage) throws IOException
	{
		Parent root = FXMLLoader.load(getClass().getResource("/ConversionView.fxml"));
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
		btnOutput.setOnAction(event -> setOutput());

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
		File selection = FileUtils.selectFile("Add input file", radImage.isSelected(), false);

		if (selection != null)
		{
			inputFiles.add(selection);
			lstInputs.getItems().add(selection.getName());
		}
	}

	private void addDirectory()
	{
		File selection = FileUtils.selectDirectory("Add input directory");

		if (selection != null)
		{
			inputFiles.add(selection);
			lstInputs.getItems().add(selection.getName());
		}
	}

	private void setOutput()
	{
		File selection;

		if (radFiles.isSelected())
			selection = FileUtils.selectFile("Create an output file", true, true);
		else
			selection = FileUtils.selectDirectory("Select an output directory");

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

		String infoTitle = "Operation successful!";
		String errorTitle = "Operation failed!";

		if (radFiles.isSelected())
		{
			boolean wasSuccessful = FileToImage.convert(inputFiles, outputFile);

			if (wasSuccessful)
				DialogDisplay.displayInfo(infoTitle, "Image created from files.");
			else
				DialogDisplay.displayError(errorTitle, "Image not created due to errors.");
		}
		else
		{
			boolean wasSuccessful = ImageToFile.convert(inputFiles, outputFile);

			if (wasSuccessful)
				DialogDisplay.displayInfo(infoTitle, "Files extracted from image.");
			else
				DialogDisplay.displayError(errorTitle, "Unable to extract any files.");
		}
	}

	private boolean validateConversion()
	{
		String title = "Incomplete field";

		if (inputFiles.isEmpty())
		{
			DialogDisplay.displayError(title, "Please add input files to continue.");
			return false;
		}

		if (outputFile == null)
		{
			DialogDisplay.displayError(title, "Please specify the output to continue.");
			return false;
		}

		return true;
	}

	private void clearAll()
	{
		inputFiles.clear();
		outputFile = null;
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
		});

		dndPane.setOnDragDropped(event ->
		{
			Dragboard dragboard = event.getDragboard();
			boolean success = false;

			if (dragboard.hasFiles())
			{
				List<File> droppedFiles = dragboard.getFiles();

				if (radImage.isSelected())
					droppedFiles = droppedFiles.stream().filter(file -> file.isDirectory() || FileUtils.validateFile(file))
							.collect(Collectors.toList());

				for (File file : droppedFiles)
				{
					inputFiles.add(file);
					lstInputs.getItems().add(file.getName());
				}

				success = true;
			}

			event.setDropCompleted(success);
		});

		dndPane.setOnDragEntered(event ->
		{
			String style = "-fx-border-style: dashed; -fx-border-color: " + (event.getDragboard().hasFiles() ? "lime" : "red");
			dndPane.setStyle(style);
		});

		dndPane.setOnDragExited(event -> dndPane.setStyle("-fx-border-style: dashed; -fx-border-color: black"));
	}
}