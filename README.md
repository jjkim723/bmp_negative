# bmp_negative #

## Overview ##
* This tool creates photo negative of a BMP file (uncompressed, 24 bits per pixel)
* Works by modifying the pixel data of the original BMP file
  * For each color in a pixel: 255 - (original byte)
  * Colors of a pixel are: red, green, blue 
* Limitations: Only works if uncompressed AND 24 bits per pixel
  * Handled by throwing Exceptions for each of these limitations

## Files ##
* BMPImage.java - structure that contains read BMP file information
* BMPTool.java  - methods to create a modified/copy BMP file
* Run.java      - menu and running

## How to Run ##
1. Compile with javac BMPImage.java BMPTool.java Run.java
2. Run with "java Run" OR "java [args] --help", for help message
3. Follow menu instructions

## Test Images ##
* Positive Tests
  * windows.bmp, tiger.bmp 

* Negative Test - exits with Exception
  * Case "Not 24 bits per pixel" : land.bmp, land2.bmp, space.bmp
  * Case "Wrong file type" : butterfly.png
  
## Challenges and Lessons ##
* Biggest Challege: BMP File Format
	* Bytes are ordered in little-endian format
		* Requires helper functions i.e. convertInt() and convertShort() 
		to convert 32- and 16- bit numbers to int
		* Lesson: Endianness and storage in memory
* Most Important Lesson: Documentation
	* Half the battle was processing the BMP information
		* Lesson: Write out examples and test understanding of 
		documentation by whiteboarding

