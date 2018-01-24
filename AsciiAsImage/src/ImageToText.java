import java.awt.Color;
import java.awt.image.*;
import java.io.*;

import javax.imageio.ImageIO;
import javax.swing.JOptionPane;

public class ImageToText
{
	private File[] inputFiles;
	private File outputDir;

	public ImageToText(File[] inputFiles, File outputDir)
	{
		this.inputFiles = inputFiles;
		this.outputDir = outputDir;
	}

	public void start()
	{
		//Text group separation character
		String unitSeparator = "" + (char) 31;

		for (int i = 0; i < inputFiles.length; i++)
		{
			//Get pixels from file
			BufferedImage image = readFile(inputFiles[i]);
			int[] pixels = ((DataBufferInt) image.getRaster().getDataBuffer()).getData();

			//Build string from pixel info
			String rawText = createText(pixels);

			//Split string by unit separator
			String[] splitString = rawText.split(unitSeparator);

			for (int j = 0; j < splitString.length - 1; j += 2)
			{
				//Get text from split array
				String fileName = splitString[j];
				String fileText = splitString[j + 1];

				//Create output file path
				fileName = outputDir + "/" + fileName;

				//Save text to output file
				saveText(fileName, fileText);
			}
		}
	}

	private BufferedImage readFile(File file)
	{
		//Read image from file
		BufferedImage fileImage = null;
		try
		{
			fileImage = ImageIO.read(file);
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}

		//Set image type
		BufferedImage image = new BufferedImage(fileImage.getWidth(), fileImage.getHeight(),
				BufferedImage.TYPE_INT_RGB);
		image.getGraphics().drawImage(fileImage, 0, 0, null);

		return image;
	}

	private String createText(int[] pixels)
	{
		StringBuffer buffer = new StringBuffer();

		for (int i = 0; i < pixels.length; i++)
		{
			//Create color object from pixel info
			Color pixel = new Color(pixels[i]);

			//Read channels from color object
			buffer.append((char) pixel.getRed());
			buffer.append((char) pixel.getGreen());
			buffer.append((char) pixel.getBlue());
		}

		return buffer.toString();
	}

	private void saveText(String fileName, String fileText)
	{
		try
		{
			//Write text to file
			Writer writer = new BufferedWriter(new FileWriter(fileName));
			writer.write(fileText);
			writer.close();

			//Display status
			String successMessage = "File created successfully!";
			System.out.println(successMessage);
			JOptionPane.showMessageDialog(null, successMessage, "Success!", JOptionPane.INFORMATION_MESSAGE);
		}
		catch (IOException e)
		{
			//Display status
			String errorMessage = "Unable to write to file!";
			e.printStackTrace();
			JOptionPane.showMessageDialog(null, errorMessage, "Error!", JOptionPane.ERROR_MESSAGE);
		}
	}
}