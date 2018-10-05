package io.github.jakejmattson.anytoimage;

import javafx.stage.*;

import javax.swing.filechooser.FileSystemView;
import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.stream.*;

final class FileManager
{
	static final File defaultDirectory = FileSystemView.getFileSystemView().getDefaultDirectory();
	static final FileChooser.ExtensionFilter fileFilter =
			new FileChooser.ExtensionFilter("*.png", "*.png", "*.PNG");

	private FileManager(){}

	/**
	 * Collect a list of files from a directory and all sub-directories.
	 *
	 * @param dir
	 * 		Directory to be walked
	 *
	 * @return List of files obtained from walk
	 */
	static List<File> walkDirectory(File dir)
	{
		try (Stream<Path> paths = Files.walk(dir.toPath()))
		{
			return paths.filter(Files::isRegularFile).map(Path::toFile).collect(Collectors.toList());
		}
		catch (IOException e)
		{
			DialogDisplay.displayException(e, "Unable to walk directory: " + dir.toString());
			return Collections.emptyList();
		}
	}

	static boolean validateFile(File file)
	{
		String extension = fileFilter.getExtensions().get(0).toLowerCase().substring(1);
		return file.getPath().endsWith(extension);
	}

	static File selectFile(String title, boolean shouldFilter, boolean isSave)
	{
		FileChooser chooser = new FileChooser();
		chooser.setTitle(title);
		chooser.setInitialDirectory(FileManager.defaultDirectory);

		if (shouldFilter)
			chooser.getExtensionFilters().add(FileManager.fileFilter);

		return isSave ? chooser.showSaveDialog(null) : chooser.showOpenDialog(null);
	}

	static File selectDirectory(String title)
	{
		DirectoryChooser chooser = new DirectoryChooser();
		chooser.setTitle(title);
		chooser.setInitialDirectory(FileManager.defaultDirectory);
		return chooser.showDialog(null);
	}
}