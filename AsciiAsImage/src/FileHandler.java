import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.dnd.*;
import java.awt.event.*;
import java.io.File;
import java.util.List;

import javax.swing.*;
import javax.swing.border.TitledBorder;

public class FileHandler extends JFrame implements ActionListener
{
	private File[] inputFile;
	private File[] outputFile;
	private JButton[] btnJfcInput;
	private JButton[] btnJfcOutput;
	private JButton[] btnClear;
	private JButton[] btnSubmit;
	private JButton[] btnExit;
	
	private final int TEXT_TO_IMAGE = 0;
	private final int IMAGE_TO_TEXT = 1;
	private final int NUM_OF_TABS = 2;

	public static void main(String[] args)
	{
		FileHandler driver = new FileHandler();
		driver.buildGUI();
	}

	private FileHandler()
	{
		//Create frame
		super();
		
		//Initialize arrays with size
		inputFile = new File[NUM_OF_TABS];
		outputFile = new File[NUM_OF_TABS];
		btnJfcInput = new JButton[NUM_OF_TABS];
		btnJfcOutput = new JButton[NUM_OF_TABS];
		btnClear = new JButton[NUM_OF_TABS];
		btnSubmit = new JButton[NUM_OF_TABS];
		btnExit = new JButton[NUM_OF_TABS];
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
		this.pack();
		this.setTitle("AsciiAsImage");
		this.setVisible(true);
	}

	private JPanel createDisplayPanel(int tabNum)
	{
		//Create panels
		JPanel displayPanel = new JPanel();	
		JPanel inputPanel = createInputPanel(tabNum);
		JPanel outputPanel = createOutputPanel(tabNum);
		JPanel buttonPanel = createButtonPanel(tabNum);

		//Format panels
		displayPanel.setLayout(new GridBagLayout());
		GridBagConstraints constraint = new GridBagConstraints();

		//Add panels
		constraint.fill = GridBagConstraints.HORIZONTAL;
		constraint.gridx = 0;
		constraint.gridy = 0;
		displayPanel.add(inputPanel, constraint);
		
		constraint.fill = GridBagConstraints.HORIZONTAL;
		constraint.gridx = 0;
		constraint.gridy = 1;
		displayPanel.add(outputPanel, constraint);
		
		constraint.fill = GridBagConstraints.HORIZONTAL;
		constraint.gridx = 0;
		constraint.gridy = 2;
		displayPanel.add(buttonPanel, constraint);
		
		return displayPanel;
	}
	
	private JPanel createInputPanel(int tabNum)
	{
		//Create panels
		JPanel inputPanel = new JPanel();
		JPanel dropPanel = new JPanel();
		JPanel chooserPanel = new JPanel();
		
		//Format panels
		inputPanel.setLayout(new GridLayout(2, 0));
		dropPanel.setLayout(new GridBagLayout());
		chooserPanel.setLayout(new GridLayout(0, 2));
		inputPanel.setBorder(BorderFactory.createTitledBorder(
				BorderFactory.createLineBorder(Color.BLACK), "Input", TitledBorder.CENTER, TitledBorder.TOP));
		chooserPanel.setBorder(BorderFactory.createTitledBorder(
				BorderFactory.createEmptyBorder(), "OR", TitledBorder.CENTER, TitledBorder.ABOVE_TOP));
		
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
						//Save file
						inputFile[tabNum] = file;
					}
					
					System.out.println("Dropped file added: " + inputFile[tabNum].getAbsolutePath());
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
	
	private JPanel createOutputPanel(int tabNum)
	{
		//Create panel
		JPanel outputPanel = new JPanel();
		
		//Format panel
		outputPanel.setLayout(new GridLayout(0, 2));
		outputPanel.setBorder(BorderFactory.createTitledBorder(
				BorderFactory.createLineBorder(Color.BLACK), "Output", TitledBorder.CENTER, TitledBorder.TOP));
		
		//Create description label
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
		buttonPanel.setLayout(new GridLayout(0, 3));

		//Create buttons
		btnClear[tabNum] = new JButton("Clear");
		btnClear[tabNum].addActionListener(this);

		btnSubmit[tabNum] = new JButton("Submit");
		btnSubmit[tabNum].addActionListener(this);

		btnExit[tabNum] = new JButton("Exit");
		btnExit[tabNum].addActionListener(this);

		//Add buttons to panel
		buttonPanel.add(btnClear[tabNum]);
		buttonPanel.add(btnSubmit[tabNum]);
		buttonPanel.add(btnExit[tabNum]);
		
		return buttonPanel;
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
	
	@Override
	public void actionPerformed(ActionEvent e)
	{
		if (e.getSource() == btnJfcInput[TEXT_TO_IMAGE])
		{
			//Allow user to select an input file
			File selectedFile = createFileChooser(JFileChooser.FILES_ONLY);
			
			//Save file
			if (selectedFile != null)
				inputFile[TEXT_TO_IMAGE] = selectedFile;
		}
		
		else if (e.getSource() == btnJfcInput[IMAGE_TO_TEXT])
		{
			//Allow user to select an input file
			File selectedFile = createFileChooser(JFileChooser.FILES_ONLY);
			
			//Save file
			if (selectedFile != null)
				inputFile[IMAGE_TO_TEXT] = selectedFile;
		}
		
		else if (e.getSource() == btnJfcOutput[TEXT_TO_IMAGE])
		{
			//Allow user to select an output file
			File selectedFile = createFileChooser(JFileChooser.FILES_ONLY);
			
			//Save file
			if (selectedFile != null)
				outputFile[TEXT_TO_IMAGE] = selectedFile;
		}
		
		else if (e.getSource() == btnJfcOutput[IMAGE_TO_TEXT])
		{
			//Allow user to select an output directory
			File selectedFile = createFileChooser(JFileChooser.DIRECTORIES_ONLY);
			
			//Save file
			if (selectedFile != null)
				outputFile[IMAGE_TO_TEXT] = selectedFile;
		}
		
		else if (e.getSource() == btnClear[TEXT_TO_IMAGE])
		{
			//Clear saved files
			inputFile[TEXT_TO_IMAGE] = null;
			outputFile[TEXT_TO_IMAGE] = null;
		}

		else if (e.getSource() == btnClear[IMAGE_TO_TEXT])
		{
			//Clear saved files
			inputFile[IMAGE_TO_TEXT] = null;
			outputFile[IMAGE_TO_TEXT] = null;
		}
		
		else if (e.getSource() == btnSubmit[TEXT_TO_IMAGE])
		{
			//Validate input
			if (inputFile[TEXT_TO_IMAGE] != null)
			{
				if (outputFile[TEXT_TO_IMAGE] != null)
				{
					//Process input
					TextToImage driver = new TextToImage(inputFile[TEXT_TO_IMAGE], outputFile[TEXT_TO_IMAGE]);
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
			if (inputFile[IMAGE_TO_TEXT] != null)
			{
				if (outputFile[IMAGE_TO_TEXT] != null)
				{
					//Process input
					ImageToText driver = new ImageToText(inputFile[IMAGE_TO_TEXT], outputFile[IMAGE_TO_TEXT]);
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
	}
	
	private void displayError(String message)
	{
		//Display error dialog to user
		JOptionPane.showMessageDialog(null, message, "Error!", JOptionPane.ERROR_MESSAGE);
	}
}