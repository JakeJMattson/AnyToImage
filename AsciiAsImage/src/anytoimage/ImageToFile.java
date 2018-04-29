package anytoimage;

import java.awt.image.*;
import java.io.*;
import java.nio.file.*;

import javax.imageio.ImageIO;
import javax.swing.JOptionPane;

/**
 * Extract files from images created by the 'FileToImage' class.
 *
 * @author mattson543
 */
public class ImageToFile
{
	/**
	 * Static method to initiate the conversion.
	 *
	 * @param inputFiles
	 *            Array of image files to be converted
	 * @param outputDir
	 *            Directory to store all output files in
	 */
	public static void convert(File[] inputFiles, File outputDir)
	{
		for (File file : inputFiles)
			try
			{
				//Extract individual pixels from an image
				int[] pixels = extractPixels(file);

				//Separate pixels into bytes
				byte[] allBytes = extractBytes(pixels);

				//Create files from bytes
				createFiles(allBytes, outputDir);
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}

		JOptionPane.showMessageDialog(null, "All valid files have been extracted from the input.",
				"Extraction Complete!", JOptionPane.INFORMATION_MESSAGE);
	}

	/**
	 * Read an image from a file and extract pixels.
	 *
	 * @param file
	 *            File containing the image to be read
	 * @return Pixels from image
	 * @throws IOException
	 *             Failed to read image from file
	 */
	private static int[] extractPixels(File file) throws IOException
	{
		//Read image from file
		BufferedImage fileImage = ImageIO.read(file);

		//Create a buffered image with the desired type
		BufferedImage image = new BufferedImage(fileImage.getWidth(), fileImage.getHeight(),
				BufferedImage.TYPE_INT_RGB);

		//Draw the image from the file into the buffer
		image.getGraphics().drawImage(fileImage, 0, 0, null);

		//Read all pixels from image
		int[] pixels = ((DataBufferInt) image.getRaster().getDataBuffer()).getData();

		return pixels;
	}

	/**
	 * Extract bytes from each pixel.
	 *
	 * @param pixels
	 *            Int array containing all pixels from the image
	 * @return Bytes
	 * @throws IOException
	 *             Failed to write byte array to stream
	 */
	private static byte[] extractBytes(int[] pixels) throws IOException
	{
		ByteArrayOutputStream bytes = new ByteArrayOutputStream();

		//Read channels from pixel
		for (int pixel : pixels)
			bytes.write(ByteUtils.intToBytes(pixel, 3));

		return bytes.toByteArray();
	}

	/**
	 * Create all files contained within the image.
	 *
	 * @param bytes
	 *            File names and data as a byte array
	 * @param outputDir
	 *            Directory to store all output files in
	 * @throws IOException
	 *             Failed to create file (write bytes)
	 */
	private static void createFiles(byte[] bytes, File outputDir) throws IOException
	{
		//Create stream to store file info
		ByteArrayOutputStream name = new ByteArrayOutputStream();
		ByteArrayOutputStream data = new ByteArrayOutputStream();

		boolean isName = true;
		int index = 0;

		while (index != bytes.length)
		{
			//Calculate the number of bytes in each cluster (name/data)
			byte[] sizeBytes;

			if (isName)
				sizeBytes = new byte[] {0, 0, bytes[index++]};
			else
				sizeBytes = new byte[] {bytes[index++], bytes[index++], bytes[index++], bytes[index++]};

			int clusterLength = ByteUtils.bytesToInt(sizeBytes);

			//EOF
			if (clusterLength == 0)
				break;

			for (int i = 0; i < clusterLength; i++)
				if (isName)
					name.write(bytes[index++]);
				else
					data.write(bytes[index++]);

			if (!isName)
			{
				//Create file
				Path path = Paths.get(outputDir + "/" + new String(name.toByteArray()));
				Files.write(path, data.toByteArray());

				//Clear streams
				name.reset();
				data.reset();
			}

			isName = !isName;
		}
	}
}