<p align="center">
  <a href="https://kotlinlang.org/">
    <img src="https://img.shields.io/badge/Kotlin-1.4.10-blue.svg" alt="Kotlin 1.4.10">
  </a>
  <a href="https://github.com/edvin/tornadofx">
    <img src="https://img.shields.io/badge/TornadoFX-1.7.20-blue.svg" alt="TornadoFX 1.7.20">
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
* File name is 1 byte. Equivalent to 256 characters/bytes (including extensions).
* File size is 4 bytes. Equivalent to 4,294,967,295 bytes or 4.29 gigabytes of data.
* If the input is too large, you may exceed the Java heap space, resulting in a crash.

## License
This project is licensed under the MIT License - see the [LICENSE.md](LICENSE.md) file for details
