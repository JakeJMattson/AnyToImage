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

//TODO validate dropped file extensions

public class Gui extends Application
{
	@FXML Button btnAddFile, btnAddDirectory, btnOutput, btnRemove, btnSubmit, btnClear;
	@FXML RadioButton radFiles, radImage;
	@FXML Pane dndPane;
	@FXML ListView<String> lstInputs;
	@FXML TextField txtOutput;
	@FXML Label lblDirectionArrow;

	private List<File> inputFiles = new ArrayList<>();
	private File outputFile;

	private static final FileChooser.ExtensionFilter pngFilter =
			new FileChooser.ExtensionFilter("*.png", "*.png", "*.PNG");

	public static void main(String[] args)
	{
		if (args.length == 0) //GUI mode
		{
			launch(args);
		}
		else if (args.length >= 3) //CLI mode
		{
			int conversionType = Integer.parseInt(args[0]);
			List<File> input = new ArrayList<>();

			for (int i = 1; i < args.length - 1; i++)
				input.add(new File(args[i]));

			File output = new File(args[args.length - 1]);

			//Process input
			if (conversionType == 0)
				FileToImage.convert(input, output);
			else if (conversionType == 1)
				ImageToFile.convert(input, output);
		}
		else
			System.out.println("Insufficient arguments!");
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

		Alert.AlertType infoType = Alert.AlertType.INFORMATION, errorType = Alert.AlertType.ERROR;
		String infoTitle = "Operation successful!", errorTitle = "Operation failed!";

		if (radFiles.isSelected())
		{
			boolean wasSuccessful = FileToImage.convert(inputFiles, outputFile);

			if (wasSuccessful)
				displayDialog(infoType, infoTitle, "Image created from files.");
			else
				displayDialog(errorType, errorTitle, "Image not created due to errors.");
		}
		else
		{
			boolean wasSuccessful = ImageToFile.convert(inputFiles, outputFile);

			if (wasSuccessful)
				displayDialog(infoType, infoTitle, "Files extracted from image.");
			else
				displayDialog(errorType, errorTitle, "Unable to extract any files.");
		}
	}

	private boolean validateConversion()
	{
		Alert.AlertType type = Alert.AlertType.ERROR;
		String title = "Incomplete field";

		if (inputFiles.size() == 0)
		{
			displayDialog(type, title, "Please add input files to continue.");
			return false;
		}

		if (outputFile == null)
		{
			displayDialog(type, title, "Please specify the output to continue.");
			return false;
		}

		return true;
	}

	private void displayDialog(Alert.AlertType type, String title, String content)
	{
		Alert dialog = new Alert(type);
		dialog.setTitle(title);
		dialog.setHeaderText(null);
		dialog.setContentText(content);
		dialog.showAndWait();
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