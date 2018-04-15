/*
 * Class Description:
 * Display/Main - GUI to accept user input and convert files
 */

package asciiasimage;

import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.dnd.*;
import java.awt.event.*;
import java.io.File;
import java.util.*;
import java.util.List;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.filechooser.FileNameExtensionFilter;

//TODO fix resizing issue upon filling JList in GUI
//TODO fix lingering tooltip text

@SuppressWarnings("serial")
public class FileHandler extends JFrame implements ActionListener
{
	//File inputs
	private final List<List<File>> inputFiles;
	private final File[] outputFile;

	//Buttons
	private final JButton[] btnJfcInput;
	private final JButton[] btnJfcOutput;
	private final JButton[] btnSubmit;
	private final JButton[] btnExit;
	private final JButton[] btnClear;
	private final JButton[] btnRemoveSelected;

	//Input displays
	private final JList<String>[] lstInput;
	private final List<DefaultListModel<String>> listContent;
	private final JTextField[] txtOutput;

	//Constants
	private final int TEXT_TO_IMAGE = 0;
	private final int IMAGE_TO_TEXT = 1;
	private final int NUM_OF_TABS = 2;
	private final FileNameExtensionFilter[] inputRestrictions = {
			new FileNameExtensionFilter("Text file", "txt", "bat", "c", "cpp", "cs", "h", "java", "py", "sh", "sln",
					"swift", "vb", "xml"),
			new FileNameExtensionFilter("*.png", "png")
	};

	public static void main(String[] args)
	{
		FileHandler driver = new FileHandler();
		driver.buildGUI();
	}

	@SuppressWarnings("unchecked")
	private FileHandler()
	{
		//Create frame
		super();

		//File inputs
		inputFiles = new ArrayList<>(NUM_OF_TABS);
		outputFile = new File[NUM_OF_TABS];

		//Buttons
		btnJfcInput = new JButton[NUM_OF_TABS];
		btnJfcOutput = new JButton[NUM_OF_TABS];
		btnClear = new JButton[NUM_OF_TABS];
		btnSubmit = new JButton[NUM_OF_TABS];
		btnExit = new JButton[NUM_OF_TABS];
		btnRemoveSelected = new JButton[NUM_OF_TABS];

		//Input displays
		lstInput = new JList[NUM_OF_TABS];
		listContent = new ArrayList<>(NUM_OF_TABS);
		txtOutput = new JTextField[NUM_OF_TABS];
	}

	private void buildGUI()
	{
		//Create pane
		JTabbedPane tabbedPane = new JTabbedPane();

		//Add tabs to pane
		tabbedPane.addTab("Text to Image", null, createTab(TEXT_TO_IMAGE));
		tabbedPane.addTab("Image to Text", null, createTab(IMAGE_TO_TEXT));

		//Add pane to frame
		this.add(tabbedPane);

		//Set frame preferences
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		setTitle("AsciiAsImage");
		pack();
		setVisible(true);
	}

	private JPanel createTab(int tabNum)
	{
		//Create panels
		JPanel tab = new JPanel(new GridBagLayout());
		JPanel fileInputPanel = createFileInputPanel(tabNum);
		JPanel fileOutputPanel = createFileOutputPanel(tabNum);
		JPanel buttonPanel = createButtonPanel(tabNum);
		JPanel inputDisplay = createListPanel(tabNum);
		JPanel outputDisplay = createTextboxPanel(tabNum);

		//Create button
		btnClear[tabNum] = new JButton("Clear all");
		btnClear[tabNum].addActionListener(this);

		//Create input list for each tab
		inputFiles.add(new ArrayList<File>());

		//Add components to panels
		GridBagConstraints constraints = new GridBagConstraints();
		constraints.fill = GridBagConstraints.BOTH;

		constraints.gridx = 0;
		constraints.gridy = 0;
		tab.add(fileInputPanel, constraints);

		constraints.gridx = 0;
		constraints.gridy = 1;
		tab.add(fileOutputPanel, constraints);

		constraints.gridx = 0;
		constraints.gridy = 2;
		tab.add(buttonPanel, constraints);

		constraints.gridx = 1;
		constraints.gridy = 0;
		tab.add(inputDisplay, constraints);

		constraints.gridx = 1;
		constraints.gridy = 1;
		tab.add(outputDisplay, constraints);

		constraints.gridx = 1;
		constraints.gridy = 2;
		tab.add(btnClear[tabNum], constraints);

		return tab;
	}

	private JPanel createFileInputPanel(int tabNum)
	{
		//Create panels
		JPanel inputPanel = new JPanel(new GridLayout(2, 0));
		JPanel dropPanel = new JPanel(new GridBagLayout());
		JPanel chooserPanel = new JPanel();

		//Create border
		inputPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.BLACK), "Input",
				TitledBorder.CENTER, TitledBorder.TOP));
		chooserPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEmptyBorder(), "OR",
				TitledBorder.CENTER, TitledBorder.ABOVE_TOP));

		//Add Drag and Drop to panel
		DropTarget dndZone = createDropTarget(tabNum);
		inputPanel.setDropTarget(dndZone);

		//Create labels
		String[] fileTypeOptions = {"a text", "an image"};
		JLabel lblDrop = new JLabel("Drag and drop " + fileTypeOptions[tabNum] + " file into this area");
		JLabel lblJFC = new JLabel("Select one from directories:");

		//Create button
		btnJfcInput[tabNum] = new JButton("Add File");
		btnJfcInput[tabNum].addActionListener(this);

		//Add components to intermediate panels
		dropPanel.add(lblDrop);
		chooserPanel.add(lblJFC);
		chooserPanel.add(btnJfcInput[tabNum]);

		//Add intermediate panels
		inputPanel.add(dropPanel);
		inputPanel.add(chooserPanel);

		return inputPanel;
	}

	private DropTarget createDropTarget(int tabNum)
	{
		//Create new Drag and Drop zone
		DropTarget target = new DropTarget()
		{
			@Override
			public synchronized void drop(DropTargetDropEvent drop)
			{
				try
				{
					//Set drop action
					drop.acceptDrop(DnDConstants.ACTION_COPY);

					//Get dropped files as list
					@SuppressWarnings("unchecked")
					List<File> droppedFiles = (List<File>) drop.getTransferable()
							.getTransferData(DataFlavor.javaFileListFlavor);

					for (File file : droppedFiles)
						if (file.isFile())
						{
							//Verify valid extension
							String extension = getFileExtension(file);

							if (Arrays.asList(inputRestrictions[tabNum].getExtensions()).contains(extension))
							{
								inputFiles.get(tabNum).add(file);
								System.out.println("Dropped file added: " + file.getAbsolutePath());
							}
						}

					//Refresh display
					refreshInputDisplay(tabNum);
				}
				catch (Exception ex)
				{
					ex.printStackTrace();
				}
			}
		};

		return target;
	}

	private String getFileExtension(File file)
	{
		//Get path
		String fileName = file.getAbsolutePath();

		//Parse path
		String extension = "";
		int index = fileName.lastIndexOf('.');
		if (index > 0)
			extension = fileName.substring(index + 1);

		return extension;
	}

	private JPanel createFileOutputPanel(int tabNum)
	{
		//Create panel
		JPanel outputPanel = new JPanel(new GridLayout(0, 2));

		//Create border
		outputPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.BLACK), "Output",
				TitledBorder.CENTER, TitledBorder.TOP));

		//Create label
		JLabel lblOutput = new JLabel("Select destination:");

		//Create button
		btnJfcOutput[tabNum] = new JButton("Select Output");
		btnJfcOutput[tabNum].addActionListener(this);

		//Add components to panel
		outputPanel.add(lblOutput);
		outputPanel.add(btnJfcOutput[tabNum]);

		return outputPanel;
	}

	private JPanel createButtonPanel(int tabNum)
	{
		//Create panel
		JPanel buttonPanel = new JPanel(new GridLayout(0, 2));

		//Create buttons
		String[] submitOptions = {"Create Image", "Extract Files"};
		btnSubmit[tabNum] = new JButton(submitOptions[tabNum]);
		btnSubmit[tabNum].addActionListener(this);

		btnExit[tabNum] = new JButton("Exit");
		btnExit[tabNum].addActionListener(this);

		//Add buttons to panel
		buttonPanel.add(btnSubmit[tabNum]);
		buttonPanel.add(btnExit[tabNum]);

		return buttonPanel;
	}

	private JPanel createListPanel(int tabNum)
	{
		//Create panels
		JPanel listPanel = new JPanel(new GridBagLayout());
		JPanel listboxPanel = new JPanel(new BorderLayout());

		//Create border
		listPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.BLACK), "Files",
				TitledBorder.CENTER, TitledBorder.TOP));

		//Create JList
		listContent.add(new DefaultListModel<String>());
		lstInput[tabNum] = new JList<>(listContent.get(tabNum));
		lstInput[tabNum].setPrototypeCellValue("LongTestFileName.txt plus some");

		//Configure tool tips
		MouseMotionAdapter listener = createMouseMotionAdapter(tabNum);
		lstInput[tabNum].addMouseMotionListener(listener);

		//Create JScrolPane for JList
		JScrollPane scrollPane = new JScrollPane(lstInput[tabNum]);
		scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

		//Create button
		btnRemoveSelected[tabNum] = new JButton("Remove Selected");
		btnRemoveSelected[tabNum].addActionListener(this);

		//Add components to panels
		listboxPanel.add(scrollPane, BorderLayout.CENTER);

		GridBagConstraints constraints = new GridBagConstraints();
		constraints.fill = GridBagConstraints.BOTH;

		constraints.gridy = 0;
		listPanel.add(listboxPanel, constraints);

		constraints.gridy = 1;
		listPanel.add(btnRemoveSelected[tabNum], constraints);

		return listPanel;
	}

	private MouseMotionAdapter createMouseMotionAdapter(int tabNum)
	{
		MouseMotionAdapter listener = new MouseMotionAdapter()
		{
			@Override
			public void mouseMoved(MouseEvent e)
			{
				//Get list
				@SuppressWarnings("unchecked")
				JList<String> list = (JList<String>) e.getSource();

				//Get index of item
				int index = list.locationToIndex(e.getPoint());

				//Set tooltip text to complete file path
				if (index > -1)
					list.setToolTipText(inputFiles.get(tabNum).get(index).toString());
			}
		};

		return listener;
	}

	private JPanel createTextboxPanel(int tabNum)
	{
		//Create panel
		JPanel textboxPanel = new JPanel(new BorderLayout());

		//Create border
		String[] displayOptions = {"Output Image", "Output Directory"};
		textboxPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.BLACK),
				displayOptions[tabNum], TitledBorder.CENTER, TitledBorder.TOP));

		//Create text boxes
		txtOutput[tabNum] = new JTextField();
		txtOutput[tabNum].setEditable(false);

		//Add components to panel
		textboxPanel.add(txtOutput[tabNum]);

		return textboxPanel;
	}

	@Override
	public void actionPerformed(ActionEvent e)
	{
		Object buttonClicked = e.getSource();
		int tabNum;

		if ((tabNum = Arrays.asList(btnJfcInput).indexOf(buttonClicked)) != -1)
		{
			//Allow user to select an input file
			File selectedFile = createFileChooser(JFileChooser.FILES_ONLY, inputRestrictions[tabNum], true);

			//Save file
			if (selectedFile != null)
				inputFiles.get(tabNum).add(selectedFile);

			//Refresh display
			refreshInputDisplay(tabNum);
		}

		else if ((tabNum = Arrays.asList(btnJfcOutput).indexOf(buttonClicked)) != -1)
		{
			//Restrict input
			int[] fileTypes = {JFileChooser.FILES_ONLY, JFileChooser.DIRECTORIES_ONLY};
			FileNameExtensionFilter[] fileFilters = {new FileNameExtensionFilter("*.png", "png"),
					new FileNameExtensionFilter("Directory", " ")};

			//Allow user to select an output file
			File selectedFile = createFileChooser(fileTypes[tabNum], fileFilters[tabNum], false);

			//Save file
			if (selectedFile != null)
				outputFile[tabNum] = selectedFile;

			//Refresh display
			refreshOutputDisplay(tabNum);
		}

		else if ((tabNum = Arrays.asList(btnRemoveSelected).indexOf(buttonClicked)) != -1)
		{
			//Get user selection
			int index = lstInput[tabNum].getSelectedIndex();

			if (index != -1)
			{
				//Remove selection from saved files
				inputFiles.get(tabNum).remove(index);

				//Refresh display
				refreshInputDisplay(tabNum);
			}
		}

		else if ((tabNum = Arrays.asList(btnSubmit).indexOf(buttonClicked)) != -1)
		{
			if (isInputValid(tabNum))
			{
				//Text group separation character
				char unitSeparator = (char) 31;

				//Convert list to array
				List<File> fileList = inputFiles.get(tabNum);
				File[] fileArray = fileList.toArray(new File[fileList.size()]);

				//Process input
				if (tabNum == TEXT_TO_IMAGE)
					TextToImage.convert(fileArray, outputFile[tabNum], unitSeparator);
				else if (tabNum == IMAGE_TO_TEXT)
					ImageToText.convert(fileArray, outputFile[tabNum], unitSeparator);
			}
		}

		else if ((tabNum = Arrays.asList(btnClear).indexOf(buttonClicked)) != -1)
		{
			//Clear saved files
			inputFiles.get(tabNum).clear();
			outputFile[tabNum] = null;

			//Refresh display
			refreshInputDisplay(tabNum);
			refreshOutputDisplay(tabNum);
		}

		else if (Arrays.asList(btnExit).contains(buttonClicked))
			System.exit(0);
	}

	private File createFileChooser(int selectionMode, FileNameExtensionFilter fileFilter, boolean isOpening)
	{
		//Create file dialog
		JFileChooser chooser = new JFileChooser();

		//Set dialog options
		chooser.setFileSelectionMode(selectionMode);
		chooser.setFileFilter(fileFilter);
		chooser.setAcceptAllFileFilterUsed(false);

		//Display dialog
		int returnValue = isOpening ? chooser.showOpenDialog(null) : chooser.showSaveDialog(null);

		//Get user selection
		File selectedFile = null;
		if (returnValue == JFileChooser.APPROVE_OPTION)
		{
			selectedFile = chooser.getSelectedFile();

			if (!isOpening)
			{
				//Verify extension
				String extension = fileFilter.getExtensions()[0].toLowerCase().trim();
				String defaultExtension = (!extension.equals("") ? "." : "") + extension;

				//Assert extension
				String filePath = selectedFile.getPath();
				if (!filePath.toLowerCase().endsWith(defaultExtension))
					selectedFile = new File(filePath + defaultExtension);
			}

			System.out.println("File selected from chooser: " + selectedFile.getAbsolutePath());
		}

		return selectedFile;
	}

	private void refreshInputDisplay(int tabNum)
	{
		//Clear old list data
		listContent.get(tabNum).clear();

		//Fill list with current data
		for (File file : inputFiles.get(tabNum))
			listContent.get(tabNum).addElement(file.getName());
	}

	private void refreshOutputDisplay(int tabNum)
	{
		String fileName = "";
		String filePath = null;

		if (outputFile[tabNum] != null)
		{
			//Get file attributes
			fileName = outputFile[tabNum].getName();
			filePath = outputFile[tabNum].getAbsolutePath();
		}

		txtOutput[tabNum].setText(fileName);
		txtOutput[tabNum].setToolTipText(filePath);
	}

	private boolean isInputValid(int tabNum)
	{
		String[] outputOptions = {"Please name the output image!", "Please select an output directory!"};

		boolean isValid = false;
		if (!inputFiles.get(tabNum).isEmpty())
		{
			if (outputFile[tabNum] != null)
				isValid = true;
			else
				displayError(outputOptions[tabNum]);
		}
		else
			displayError("Please add input file(s)!");

		return isValid;
	}

	private void displayError(String message)
	{
		JOptionPane.showMessageDialog(null, message, "Error!", JOptionPane.ERROR_MESSAGE);
	}
}