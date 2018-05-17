# bmp_negative #

## Overview ##
* This tool creates photo negative of a BMP file (uncompressed, 24 bits per pixel)
* Works by modifying the pixel data of the original BMP file
  * For each color in a pixel: 255 - (original byte)
  * Colors of a pixel are: red, green, blue 
* Limitations: Only works if uncompressed AND 24 bits per pixel
  * Handled by throwing Exceptions for each of these limitations

## How to Run ##
1. Compile with javac BMPEditor.java
2. Run by java BMPEditor 'filename'.bmp OR java BMPEditor --help (for help message)

## Test Images ##
* Positive Tests
  * windows.bmp, tiger.bmp 

* Negative Test
  * Case "Not 24 bits per pixel" : land.bmp, land2.bmp, space.bmp
  * Case "Wrong file type" : butterfly.png
  

