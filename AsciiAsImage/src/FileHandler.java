import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.dnd.*;
import java.awt.event.*;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.swing.*;
import javax.swing.border.TitledBorder;

//TODO fix known resizing issue upon filling JList in GUI

public class FileHandler extends JFrame implements ActionListener
{
	//File inputs
	private List<List<File>> inputFiles;
	private File[] outputFile;

	//Buttons
	private JButton[] btnJfcInput;
	private JButton[] btnJfcOutput;
	private JButton[] btnSubmit;
	private JButton[] btnExit;
	private JButton[] btnClear;
	private JButton[] btnRemoveSelected;

	//Input displays
	private JList<String>[] lstInput;
	private List<DefaultListModel<String>> listContent;
	private JTextField[] txtOutput;

	//Constants
	private final int TEXT_TO_IMAGE = 0;
	private final int IMAGE_TO_TEXT = 1;
	private final int NUM_OF_TABS = 2;

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
		inputFiles = new ArrayList<List<File>>(NUM_OF_TABS);
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
		listContent = new ArrayList<DefaultListModel<String>>(NUM_OF_TABS);
		txtOutput = new JTextField[NUM_OF_TABS];
	}

	private void buildGUI()
	{
		//Create pane
		JTabbedPane tabbedPane = new JTabbedPane();

		//Add tabs to pane
		tabbedPane.addTab("Text to Image", null, createDisplayPanel(TEXT_TO_IMAGE));
		tabbedPane.addTab("Image to Text", null, createDisplayPanel(IMAGE_TO_TEXT));

		//Add pane to frame
		this.add(tabbedPane);

		//Set frame preferences
		this.setDefaultCloseOperation(EXIT_ON_CLOSE);
		this.pack();
		this.setTitle("AsciiAsImage");
		this.setVisible(true);
	}

	private JPanel createDisplayPanel(int tabNum)
	{
		//Create panels
		JPanel displayPanel = new JPanel();
		JPanel fileInputPanel = createFileInputPanel(tabNum);
		JPanel fileOutputPanel = createFileOutputPanel(tabNum);
		JPanel buttonPanel = createButtonPanel(tabNum);
		JPanel inputDisplay = createListPanel(tabNum);
		JPanel outputDisplay = createTextboxPanel(tabNum);

		//Format panels
		displayPanel.setLayout(new GridBagLayout());
		GridBagConstraints constraints = new GridBagConstraints();
		constraints.fill = GridBagConstraints.BOTH;

		//Create button
		btnClear[tabNum] = new JButton("Clear all");
		btnClear[tabNum].addActionListener(this);

		//Create input list for each tab
		inputFiles.add(new ArrayList<File>());

		//Add components to panels
		constraints.gridx = 0;
		constraints.gridy = 0;
		displayPanel.add(fileInputPanel, constraints);

		constraints.gridx = 0;
		constraints.gridy = 1;
		displayPanel.add(fileOutputPanel, constraints);

		constraints.gridx = 0;
		constraints.gridy = 2;
		displayPanel.add(buttonPanel, constraints);

		constraints.gridx = 1;
		constraints.gridy = 0;
		displayPanel.add(inputDisplay, constraints);

		constraints.gridx = 1;
		constraints.gridy = 1;
		displayPanel.add(outputDisplay, constraints);

		constraints.gridx = 1;
		constraints.gridy = 2;
		displayPanel.add(btnClear[tabNum], constraints);

		return displayPanel;
	}

	private JPanel createFileInputPanel(int tabNum)
	{
		//Create panels
		JPanel inputPanel = new JPanel();
		JPanel dropPanel = new JPanel();
		JPanel chooserPanel = new JPanel();

		//Format panels
		inputPanel.setLayout(new GridLayout(2, 0));
		dropPanel.setLayout(new GridBagLayout());
		chooserPanel.setLayout(new GridLayout(0, 2));
		inputPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.BLACK), "Input",
				TitledBorder.CENTER, TitledBorder.TOP));
		chooserPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEmptyBorder(), "OR",
				TitledBorder.CENTER, TitledBorder.ABOVE_TOP));

		//Add Drag and Drop to panel
		DropTarget dndZone = createDropTarget(tabNum);
		inputPanel.setDropTarget(dndZone);

		//Create labels
		String[] fileTypeOptions = {"a text file", "an image file"};
		JLabel lblDrop = new JLabel("Drag and drop " + fileTypeOptions[tabNum] + " into this area");
		JLabel lblJFC = new JLabel("Select from directories:");

		//Add components to intermediate panels
		dropPanel.add(lblDrop);
		chooserPanel.add(lblJFC);
		chooserPanel.add(createChooserPanel(tabNum));

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
					{
						//Limit input to files
						if (file.isFile())
						{
							inputFiles.get(tabNum).add(file);
							System.out.println("Dropped file added: " + file.getAbsolutePath());
						}
					}

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

	private JPanel createChooserPanel(int tabNum)
	{
		//Create panel
		JPanel chooserPanel = new JPanel();

		//Create button
		btnJfcInput[tabNum] = new JButton("Select File");
		btnJfcInput[tabNum].addActionListener(this);

		//Add button to panel
		chooserPanel.add(btnJfcInput[tabNum]);

		return chooserPanel;
	}

	private JPanel createFileOutputPanel(int tabNum)
	{
		//Create panel
		JPanel outputPanel = new JPanel();

		//Format panel
		outputPanel.setLayout(new GridLayout(0, 2));
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
		JPanel buttonPanel = new JPanel();

		//Format panel
		buttonPanel.setLayout(new GridLayout(0, 2));

		//Create buttons
		btnSubmit[tabNum] = new JButton("Submit");
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
		JPanel listPanel = new JPanel();
		JPanel listboxPanel = new JPanel();

		//Format panels
		listboxPanel.setLayout(new BorderLayout());
		listPanel.setLayout(new GridBagLayout());
		listPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.BLACK), "Files",
				TitledBorder.CENTER, TitledBorder.TOP));

		//Create JList
		listContent.add(new DefaultListModel<String>());
		lstInput[tabNum] = new JList<String>(listContent.get(tabNum));
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

				if (index > -1)
				{
					//Set tooltip text to the complete file path of the item
					list.setToolTipText(inputFiles.get(tabNum).get(index).toString());
				}
			}
		};

		return listener;
	}

	private JPanel createTextboxPanel(int tabNum)
	{
		//Create panel
		JPanel textboxPanel = new JPanel();

		//Format panel
		textboxPanel.setLayout(new BorderLayout());
		textboxPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.BLACK),
				"File output", TitledBorder.CENTER, TitledBorder.TOP));

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
		if (e.getSource() == btnJfcInput[TEXT_TO_IMAGE])
		{
			//Allow user to select an input file
			File selectedFile = createFileChooser(JFileChooser.FILES_ONLY);

			//Save file
			if (selectedFile != null)
				inputFiles.get(TEXT_TO_IMAGE).add(selectedFile);

			//Refresh display
			refreshInputDisplay(TEXT_TO_IMAGE);
		}

		else if (e.getSource() == btnJfcInput[IMAGE_TO_TEXT])
		{
			//Allow user to select an input file
			File selectedFile = createFileChooser(JFileChooser.FILES_ONLY);

			//Save file
			if (selectedFile != null)
				inputFiles.get(IMAGE_TO_TEXT).add(selectedFile);

			//Refresh display
			refreshInputDisplay(IMAGE_TO_TEXT);
		}

		else if (e.getSource() == btnJfcOutput[TEXT_TO_IMAGE])
		{
			//Allow user to select an output file
			File selectedFile = createFileChooser(JFileChooser.FILES_ONLY);

			//Save file
			if (selectedFile != null)
				outputFile[TEXT_TO_IMAGE] = selectedFile;

			//Refresh display
			refreshOutputDisplay(TEXT_TO_IMAGE);
		}

		else if (e.getSource() == btnJfcOutput[IMAGE_TO_TEXT])
		{
			//Allow user to select an output directory
			File selectedFile = createFileChooser(JFileChooser.DIRECTORIES_ONLY);

			//Save file
			if (selectedFile != null)
				outputFile[IMAGE_TO_TEXT] = selectedFile;

			//Refresh display
			refreshOutputDisplay(IMAGE_TO_TEXT);
		}

		else if (e.getSource() == btnClear[TEXT_TO_IMAGE])
		{
			//Clear saved files
			inputFiles.get(TEXT_TO_IMAGE).clear();
			outputFile[TEXT_TO_IMAGE] = null;

			//Refresh display
			refreshInputDisplay(TEXT_TO_IMAGE);
			refreshOutputDisplay(TEXT_TO_IMAGE);
		}

		else if (e.getSource() == btnClear[IMAGE_TO_TEXT])
		{
			//Clear saved files
			inputFiles.get(IMAGE_TO_TEXT).clear();
			outputFile[IMAGE_TO_TEXT] = null;

			//Refresh display
			refreshInputDisplay(IMAGE_TO_TEXT);
			refreshOutputDisplay(IMAGE_TO_TEXT);
		}

		else if (e.getSource() == btnSubmit[TEXT_TO_IMAGE])
		{
			//Validate input
			if (!inputFiles.get(TEXT_TO_IMAGE).isEmpty())
			{
				if (outputFile[TEXT_TO_IMAGE] != null)
				{
					//Convert list to array
					List<File> fileList = inputFiles.get(TEXT_TO_IMAGE);
					File[] fileArray = fileList.toArray(new File[fileList.size()]);

					//Process input
					TextToImage driver = new TextToImage(fileArray, outputFile[TEXT_TO_IMAGE]);
					driver.start();
				}
				else
					displayError("Please select an output file!");
			}
			else
				displayError("Please select an input file!");
		}

		else if (e.getSource() == btnSubmit[IMAGE_TO_TEXT])
		{
			//Validate input
			if (!inputFiles.get(IMAGE_TO_TEXT).isEmpty())
			{
				if (outputFile[IMAGE_TO_TEXT] != null)
				{
					//Convert list to array
					List<File> fileList = inputFiles.get(IMAGE_TO_TEXT);
					File[] fileArray = fileList.toArray(new File[fileList.size()]);

					//Process input
					ImageToText driver = new ImageToText(fileArray, outputFile[IMAGE_TO_TEXT]);
					driver.start();
				}
				else
					displayError("Please select an output directory!");
			}
			else
				displayError("Please select an input file!");
		}

		else if (e.getSource() == btnExit[TEXT_TO_IMAGE] || e.getSource() == btnExit[IMAGE_TO_TEXT])
		{
			//Terminate program
			System.exit(0);
		}

		else if (e.getSource() == btnRemoveSelected[TEXT_TO_IMAGE])
		{
			//Get user selection
			int index = lstInput[TEXT_TO_IMAGE].getSelectedIndex();

			if (index != -1)
			{
				//Remove selection from saved files
				inputFiles.get(TEXT_TO_IMAGE).remove(index);

				//Refresh display
				refreshInputDisplay(TEXT_TO_IMAGE);
			}
		}

		else if (e.getSource() == btnRemoveSelected[IMAGE_TO_TEXT])
		{
			//Get user selection
			int index = lstInput[IMAGE_TO_TEXT].getSelectedIndex();

			if (index != -1)
			{
				//Remove selection from saved files
				inputFiles.get(IMAGE_TO_TEXT).remove(index);

				//Refresh display
				refreshInputDisplay(IMAGE_TO_TEXT);
			}
		}
	}

	private File createFileChooser(int selectionMode)
	{
		//Create file dialog
		JFileChooser chooser = new JFileChooser();
		chooser.setFileSelectionMode(selectionMode);

		//Display dialog
		int returnValue = chooser.showOpenDialog(null);

		//Get user selection
		File selectedFile = null;
		if (returnValue == JFileChooser.APPROVE_OPTION)
		{
			selectedFile = chooser.getSelectedFile();
			System.out.println("File selected from chooser: " + selectedFile.getAbsolutePath());
		}

		return selectedFile;
	}

	private void displayError(String message)
	{
		//Display error dialog to user
		JOptionPane.showMessageDialog(null, message, "Error!", JOptionPane.ERROR_MESSAGE);
	}

	private void refreshInputDisplay(int tabNum)
	{
		//Clear old list data
		listContent.get(tabNum).clear();

		//Fill list with current data
		for (int i = 0; i < inputFiles.get(tabNum).size(); i++)
			listContent.get(tabNum).addElement(inputFiles.get(tabNum).get(i).getName());
	}

	private void refreshOutputDisplay(int tabNum)
	{
		if (outputFile[tabNum] != null)
		{
			//Set display to file name
			txtOutput[tabNum].setText(outputFile[tabNum].getName());

			//Set tooltip text to file path
			txtOutput[tabNum].setToolTipText(outputFile[tabNum].getAbsolutePath());
		}
		else
			//Clear text
			txtOutput[tabNum].setText("");
	}
}