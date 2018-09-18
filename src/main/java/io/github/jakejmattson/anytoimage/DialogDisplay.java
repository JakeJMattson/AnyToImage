/*
 * The MIT License
 * Copyright © 2018 Jake Mattson
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package io.github.jakejmattson.anytoimage;

import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.io.*;

final class DialogDisplay
{
	static boolean isGraphical = true;
	static boolean shouldPrint = true;

	private DialogDisplay(){}

	static void displayInfo(String title, String message)
	{
		if (shouldPrint)
			System.out.println(message);

		if (!isGraphical)
			return;

		Alert dialog = createDialog(Alert.AlertType.INFORMATION, title, message);
		dialog.showAndWait();
	}

	static void displayError(String title, String message)
	{
		if (shouldPrint)
			System.out.println(message);

		if (!isGraphical)
			return;

		Alert dialog = createDialog(Alert.AlertType.ERROR, title, message);
		dialog.showAndWait();
	}

	static void displayException(Exception e, String message)
	{
		if (shouldPrint)
		{
			System.out.println(message);
			e.printStackTrace();
		}

		if (!isGraphical)
			return;

		//Create dialog
		Alert alert = createDialog(Alert.AlertType.ERROR, "Something went wrong!", message);

		//Get exception as string
		StringWriter sw = new StringWriter();
		e.printStackTrace(new PrintWriter(sw));
		String exceptionText = sw.toString();

		//Create a text area to display the exception
		TextArea textArea = new TextArea(exceptionText);
		textArea.setStyle("-fx-text-inner-color: red;");
		textArea.setEditable(false);

		//Format content
		GridPane.setVgrow(textArea, Priority.ALWAYS);
		GridPane.setHgrow(textArea, Priority.ALWAYS);
		GridPane expContent = new GridPane();
		expContent.add(new Label("Exception stacktrace:"), 0, 0);
		expContent.add(textArea, 0, 1);

		//Display exception
		alert.getDialogPane().setExpandableContent(expContent);
		alert.showAndWait();
	}

	private static Alert createDialog(Alert.AlertType type, String title, String message)
	{
		Alert dialog = new Alert(type, message);
		dialog.setTitle(title);
		dialog.setHeaderText(null);

		return dialog;
	}
}