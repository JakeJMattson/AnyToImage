<p align="center">
  <a href="https://jdk.java.net/11/">
    <img src="https://img.shields.io/badge/Java-11-blue.svg" alt="Java 11">
  </a>
  <a href="https://kotlinlang.org/">
    <img src="https://img.shields.io/badge/Kotlin-1.3.30-blue.svg" alt="Kotlin 1.3.30">
  </a>
  <a href="https://openjfx.io/">
    <img src="https://img.shields.io/badge/JavaFX-12-blue.svg" alt="JavaFX 12">
  </a>
  <a href="https://GitHub.com/JakeJMattson/AnyToImage/releases/">
    <img src="https://img.shields.io/github/release/JakeJMattson/AnyToImage.svg" alt="release">
  </a>
  <a href="LICENSE.md">
    <img src="https://img.shields.io/github/license/JakeJMattson/AnyToImage.svg" alt="license">
  </a>
</p>

<p align="justify">
This project allows you to convert almost any file, group of files, or directory into an image.
By reading bytes from a file and using those bytes as color channel values, pixels can be constructed and used to form an image.
The current build allows you to convert files to images, and extract files from converted images.
</p>

<img src="https://user-images.githubusercontent.com/22604455/43825094-6f3ad9c0-9ab9-11e8-869a-55f3602caf64.png" width="100%" />

<p align="justify">
The image above is a demonstration of this code. 
It is composed of 27 folders containing a total of 520 files.
Each source file is an implementation of "Hello World" in a different programming language.
The files can be obtained from the <a href="https://github.com/leachim6/hello-world">hello-world repository</a>
which was inspired by <a href="https://helloworldcollection.github.io/">The Hello World Collection</a>.
</p>

## Limitations

<p align="justify">
Due to the way certain aspects of this code is implemented, there are some known limitations.
</p>

### Input
* File name length is represented by 1 byte. Equivalent to 256 characters - includes extensions).
* File size is represented by to 4 bytes. Equivalent to 4,294,967,295 bytes or 4.29 gigabytes.
* If the amount of input is too large, you may exceed the Java heap space, resulting in a crash.

## Prerequisites

### Languages
* [Java](https://jdk.java.net/11/)
* [Kotlin](https://kotlinlang.org/)

### Libraries
* [JavaFX](https://openjfx.io/)

## Getting Started
### Installing Java
<p align="justify">
Visit the <a href="http://www.oracle.com/technetwork/java/javase/downloads/index.html">Java Downloads</a> 
page and select the version of your choice.
Run the installer and follow the instructions provided.
</p>

### Building
This project is built with Maven. To build the `pom.xml`, please follow the import instructions for your IDE.
* [IntelliJ](https://www.tutorialspoint.com/maven/maven_intellij_idea.htm)
* [Eclipse](https://www.tutorialspoint.com/maven/maven_eclispe_ide.htm)

## License
This project is licensed under the MIT License - see the [LICENSE.md](LICENSE.md) file for details
