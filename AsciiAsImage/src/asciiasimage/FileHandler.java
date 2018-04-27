/*
 * Project Description:
 * Convert between text and images.
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

// TODO fix resizing issue upon filling JList in GUI

/**
 * Display/Main - GUI to accept user input and convert files
 *
 * @author mattson543
 */
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
			new FileNameExtensionFilter("Text file", "txt", "bat", "c", "cpp", "cs", "h", "java", "py", "sh",
					"sln", "swift", "vb", "xml"),
			new FileNameExtensionFilter("*.png", "png")
	};

	public static void main(String[] args)
	{
		FileHandler driver = new FileHandler();
		driver.buildGUI();
	}

	/**
	 * Constructor - initializes all fields
	 */
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

	/**
	 * Construct the frame and all of its components
	 */
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

	/**
	 * Create a new tab for the pane based on the index
	 *
	 * @param tabIndex
	 *            Index of the current tab
	 * @return Panel
	 */
	private JPanel createTab(int tabIndex)
	{
		//Create panels
		JPanel tab = new JPanel(new GridBagLayout());
		JPanel fileInputPanel = createFileInputPanel(tabIndex);
		JPanel fileOutputPanel = createFileOutputPanel(tabIndex);
		JPanel buttonPanel = createButtonPanel(tabIndex);
		JPanel inputDisplay = createListPanel(tabIndex);
		JPanel outputDisplay = createOutputPanel(tabIndex);

		//Create button
		btnClear[tabIndex] = new JButton("Clear all");
		btnClear[tabIndex].addActionListener(this);

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
		tab.add(btnClear[tabIndex], constraints);

		return tab;
	}

	/**
	 * Create the panel that allows a user to enter the input
	 *
	 * @param tabIndex
	 *            Index of the current tab
	 * @return Panel
	 */
	private JPanel createFileInputPanel(int tabIndex)
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
		DropTarget dndZone = createDropTarget(tabIndex);
		inputPanel.setDropTarget(dndZone);

		//Create labels
		String[] fileTypeOptions = {"a text", "an image"};
		JLabel lblDrop = new JLabel("Drag and drop " + fileTypeOptions[tabIndex] + " file into this area");
		JLabel lblJFC = new JLabel("Select one from directories:");

		//Create button
		btnJfcInput[tabIndex] = new JButton("Add File");
		btnJfcInput[tabIndex].addActionListener(this);

		//Add components to intermediate panels
		dropPanel.add(lblDrop);
		chooserPanel.add(lblJFC);
		chooserPanel.add(btnJfcInput[tabIndex]);

		//Add intermediate panels
		inputPanel.add(dropPanel);
		inputPanel.add(chooserPanel);

		return inputPanel;
	}

	/**
	 * Add drag and drop functionality
	 *
	 * @param tabIndex
	 *            Index of the current tab
	 * @return DropTarget
	 */
	private DropTarget createDropTarget(int tabIndex)
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

							if (Arrays.asList(inputRestrictions[tabIndex].getExtensions()).contains(extension))
							{
								inputFiles.get(tabIndex).add(file);
								System.out.println("Dropped file added: " + file.getAbsolutePath());
							}
						}

					//Refresh display
					refreshInputDisplay(tabIndex);
				}
				catch (Exception ex)
				{
					ex.printStackTrace();
				}
			}
		};

		return target;
	}

	/**
	 * Extract extension from file
	 *
	 * @param file
	 *            File to extract extension from
	 * @return Extension
	 */
	private String getFileExtension(File file)
	{
		//Get path
		String fileName = file.getName();

		//Parse path
		String extension = "";
		int index = fileName.lastIndexOf('.');

		if (index > 0)
			extension = fileName.substring(index + 1);

		return extension;
	}

	/**
	 * Create the panel that allows a user to enter the output
	 *
	 * @param tabIndex
	 *            Index of the current tab
	 * @return Panel
	 */
	private JPanel createFileOutputPanel(int tabIndex)
	{
		//Create panel
		JPanel outputPanel = new JPanel(new GridLayout(0, 2));

		//Create border
		outputPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.BLACK), "Output",
				TitledBorder.CENTER, TitledBorder.TOP));

		//Create label
		JLabel lblOutput = new JLabel("Select destination:");

		//Create button
		btnJfcOutput[tabIndex] = new JButton("Select Output");
		btnJfcOutput[tabIndex].addActionListener(this);

		//Add components to panel
		outputPanel.add(lblOutput);
		outputPanel.add(btnJfcOutput[tabIndex]);

		return outputPanel;
	}

	/**
	 * Create a panel to hold all basic buttons
	 *
	 * @param tabIndex
	 *            Index of the current tab
	 * @return Panel
	 */
	private JPanel createButtonPanel(int tabIndex)
	{
		//Create panel
		JPanel buttonPanel = new JPanel(new GridLayout(0, 2));

		//Create buttons
		String[] submitOptions = {"Create Image", "Extract Files"};
		btnSubmit[tabIndex] = new JButton(submitOptions[tabIndex]);
		btnSubmit[tabIndex].addActionListener(this);

		btnExit[tabIndex] = new JButton("Exit");
		btnExit[tabIndex].addActionListener(this);

		//Add buttons to panel
		buttonPanel.add(btnSubmit[tabIndex]);
		buttonPanel.add(btnExit[tabIndex]);

		return buttonPanel;
	}

	/**
	 * Create a panel to display the list of file inputs
	 *
	 * @param tabIndex
	 *            Index of the current tab
	 * @return Panel
	 */
	private JPanel createListPanel(int tabIndex)
	{
		//Create panels
		JPanel listPanel = new JPanel(new GridBagLayout());
		JPanel listboxPanel = new JPanel(new BorderLayout());

		//Create border
		listPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.BLACK), "Files",
				TitledBorder.CENTER, TitledBorder.TOP));

		//Create JList
		listContent.add(new DefaultListModel<String>());
		lstInput[tabIndex] = new JList<>(listContent.get(tabIndex));
		lstInput[tabIndex].setPrototypeCellValue("LongTestFileName.txt plus some");

		//Configure tool tips
		MouseMotionAdapter listener = createMouseMotionAdapter(tabIndex);
		lstInput[tabIndex].addMouseMotionListener(listener);

		//Create JScrolPane for JList
		JScrollPane scrollPane = new JScrollPane(lstInput[tabIndex]);
		scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

		//Create button
		btnRemoveSelected[tabIndex] = new JButton("Remove Selected");
		btnRemoveSelected[tabIndex].addActionListener(this);

		//Add components to panels
		listboxPanel.add(scrollPane, BorderLayout.CENTER);

		GridBagConstraints constraints = new GridBagConstraints();
		constraints.fill = GridBagConstraints.BOTH;

		constraints.gridy = 0;
		listPanel.add(listboxPanel, constraints);

		constraints.gridy = 1;
		listPanel.add(btnRemoveSelected[tabIndex], constraints);

		return listPanel;
	}

	/**
	 * Create a listener for the list boxes to add dynamic tool tips
	 *
	 * @param tabIndex
	 *            Index of the current tab
	 * @return Listener
	 */
	private MouseMotionAdapter createMouseMotionAdapter(int tabIndex)
	{
		MouseMotionAdapter listener = new MouseMotionAdapter()
		{
			@Override
			public void mouseMoved(MouseEvent e)
			{
				//Get list
				@SuppressWarnings("unchecked")
				JList<String> list = (JList<String>) e.getSource();

				if (inputFiles.get(tabIndex).size() != 0)
				{
					//Get index of item
					int index = list.locationToIndex(e.getPoint());

					//Set tooltip text to complete file path
					if (index > -1)
						list.setToolTipText(inputFiles.get(tabIndex).get(index).toString());
				}
				else
					list.setToolTipText(null);
			}
		};

		return listener;
	}

	/**
	 * Create a panel to hold the output image/directory name
	 *
	 * @param tabIndex
	 *            Index of the current tab
	 * @return Panel
	 */
	private JPanel createOutputPanel(int tabIndex)
	{
		//Create panel
		JPanel outputPanel = new JPanel(new BorderLayout());

		//Create border
		String[] displayOptions = {"Output Image", "Output Directory"};
		outputPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.BLACK),
				displayOptions[tabIndex], TitledBorder.CENTER, TitledBorder.TOP));

		//Create text boxes
		txtOutput[tabIndex] = new JTextField();
		txtOutput[tabIndex].setEditable(false);

		//Add components to panel
		outputPanel.add(txtOutput[tabIndex]);

		return outputPanel;
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	@Override
	public void actionPerformed(ActionEvent e)
	{
		Object buttonClicked = e.getSource();
		int tabIndex;

		if ((tabIndex = Arrays.asList(btnJfcInput).indexOf(buttonClicked)) != -1)
		{
			//Allow user to select an input file
			File selectedFile = createFileChooser(JFileChooser.FILES_ONLY, inputRestrictions[tabIndex], true);

			//Save file
			if (selectedFile != null)
				inputFiles.get(tabIndex).add(selectedFile);

			//Refresh display
			refreshInputDisplay(tabIndex);
		}

		else if ((tabIndex = Arrays.asList(btnJfcOutput).indexOf(buttonClicked)) != -1)
		{
			//Restrict input
			int[] fileTypes = {JFileChooser.FILES_ONLY, JFileChooser.DIRECTORIES_ONLY};
			FileNameExtensionFilter[] fileFilters = {new FileNameExtensionFilter("*.png", "png"),
					new FileNameExtensionFilter("Directory", " ")};

			//Allow user to select an output file
			File selectedFile = createFileChooser(fileTypes[tabIndex], fileFilters[tabIndex], false);

			//Save file
			if (selectedFile != null)
				outputFile[tabIndex] = selectedFile;

			//Refresh display
			refreshOutputDisplay(tabIndex);
		}

		else if ((tabIndex = Arrays.asList(btnRemoveSelected).indexOf(buttonClicked)) != -1)
		{
			//Get user selection
			int index = lstInput[tabIndex].getSelectedIndex();

			if (index != -1)
			{
				//Remove selection from saved files
				inputFiles.get(tabIndex).remove(index);

				//Refresh display
				refreshInputDisplay(tabIndex);
			}
		}

		else if ((tabIndex = Arrays.asList(btnSubmit).indexOf(buttonClicked)) != -1)
		{
			if (!inputFiles.get(tabIndex).isEmpty())
			{
				if (outputFile[tabIndex] != null)
				{
					//Text group separation character
					byte unitSeparator = (byte) 31;

					//Convert list to array
					List<File> fileList = inputFiles.get(tabIndex);
					File[] fileArray = fileList.toArray(new File[fileList.size()]);

					//Process input
					if (tabIndex == TEXT_TO_IMAGE)
						TextToImage.convert(fileArray, outputFile[tabIndex], unitSeparator);
					else if (tabIndex == IMAGE_TO_TEXT)
						ImageToText.convert(fileArray, outputFile[tabIndex], unitSeparator);
				}
				else
				{
					String[] outputOptions = {"Please name the output image!", "Please select an output directory!"};
					displayError(outputOptions[tabIndex]);
				}
			}
			else
				displayError("Please add input file(s)!");
		}

		else if ((tabIndex = Arrays.asList(btnClear).indexOf(buttonClicked)) != -1)
		{
			//Clear saved files
			inputFiles.get(tabIndex).clear();
			outputFile[tabIndex] = null;

			//Refresh display
			refreshInputDisplay(tabIndex);
			refreshOutputDisplay(tabIndex);
		}

		else if (Arrays.asList(btnExit).contains(buttonClicked))
			System.exit(0);
	}

	/**
	 * Display a dialog to allow the user to select a file.
	 *
	 * @param selectionType
	 *            Type of files the user is allowed to select: Files/Directories
	 * @param fileFilter
	 *            Extension filter to prevent the selection of invalid files
	 * @param isOpening
	 *            Boolean to determine operation: Opening/Saving
	 * @return File selected by the user
	 */
	private File createFileChooser(int selectionType, FileNameExtensionFilter fileFilter, boolean isOpening)
	{
		//Create file dialog
		JFileChooser chooser = new JFileChooser();

		//Set dialog options
		chooser.setFileSelectionMode(selectionType);
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

	/**
	 * Empty the list and refill it with the correct data
	 *
	 * @param tabIndex
	 *            Index of the current tab
	 */
	private void refreshInputDisplay(int tabIndex)
	{
		//Clear old list data
		listContent.get(tabIndex).clear();

		//Fill list with current data
		for (File file : inputFiles.get(tabIndex))
			listContent.get(tabIndex).addElement(file.getName());
	}

	/**
	 * Determine the text to display in the output box
	 *
	 * @param tabIndex
	 *            Index of the current tab
	 */
	private void refreshOutputDisplay(int tabIndex)
	{
		String fileName = "";
		String filePath = null;

		if (outputFile[tabIndex] != null)
		{
			//Get file attributes
			fileName = outputFile[tabIndex].getName();
			filePath = outputFile[tabIndex].getAbsolutePath();
		}

		txtOutput[tabIndex].setText(fileName);
		txtOutput[tabIndex].setToolTipText(filePath);
	}

	/**
	 * Display a dialog to report an error to the user.
	 *
	 * @param message
	 *            Error message to be displayed
	 */
	private void displayError(String message)
	{
		JOptionPane.showMessageDialog(null, message, "Error!", JOptionPane.ERROR_MESSAGE);
	}
}