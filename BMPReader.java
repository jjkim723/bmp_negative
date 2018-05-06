import java.io.OutputStream;
import java.io.FileInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.EOFException;

public class BMPReader {
    public static final int READ_LIMIT = 100; // default
    public static final int BYTES_IN_HEADER = 54;
    public static final short BITS_PER_PIXEL = 24; // assumption
    public static final int COMPRESSION = 0; //assumption
    public static final short FILE_EXT = 0x4d42; // "BM"

    DataInputStream ds; // read file
    OutputStream os; // write file

    int offset; // location of start of image data
    int width; // width in pixels
    int height; // height in pixels
    short bpp; // bits per pixel
    int compression; // expect 0, no compression
    int actualBMPSize; // size of bit map without padding
    int lineSize; // size of line with padding in bitmap data
    int[] intData; // pixel data unpacked
    int colorsUsed; // colors in color table

    public BMPReader(String filename) throws Exception, IOException { // constructor
        try {
            ds = new DataInputStream(new FileInputStream(filename));
        } catch (Exception e) {
            throw new Exception("File could not be opened");
        }
        ds.mark(READ_LIMIT); // marks the beginning of the file
    }

    public void readBMPFileHeader() throws Exception, IOException { // 14 bytes in file header
        short signature = ds.readShort(); // must be 'BM'
        if (signature != FILE_EXT) {
            throw new Exception("Not BMP filetype"); // mismatch in signature
        }
        int fileSize = ds.readInt(); // file size in bytes
        int reserved = ds.readInt(); // unused
        offset = ds.readInt(); // offset of bmp data
    }

    public void readBMPInfoHeader() throws Exception, IOException { // 40 bytes in info header
        int size = ds.readInt(); //size of info header
        width = ds.readInt(); height = Math.abs(ds.readInt()); // width/height in pixels
        short plane = ds.readShort(); // planes
        bpp = ds.readShort(); compression = ds.readInt();
        if (bpp != BITS_PER_PIXEL) { // assumption
            throw new Exception("Image is not 24 bits per pixel");
        }
        if (compression != COMPRESSION) { // assumption
            throw new Exception("Compression is not allowed");
        }

        int bmpSize = ds.readInt(); // compressed value, so ignored
        int hResolution = ds.readInt(); // horizontal pixels/meter
        int vResolution = ds.readInt(); //vertical pixels/meter
        colorsUsed = ds.readInt(); // # of actually used colors
        int iColors = ds.readInt(); // number of important colors

        // lines are padded to be multiples of 4 bytes
        lineSize = ((bpp * width + 31) / 32) * 4; // found on wikipedia
        actualBMPSize = lineSize * height; // BMP size with padding
    }

    /** Skips over color table of file
     */
    public void skipColorTable() throws IOException {
        for (int i = 0; i < colorsUsed; i++) {
            ds.readInt();
        }
    }

    /** Stores pixel data in 'intData'
     */
    public void readPixelData() throws EOFException, IOException {
        byte[] data; // packed data
        ds.reset(); // move back to beginning of file
        ds.skipBytes(BYTES_IN_HEADER); // skip to pixel array

        int lineLength = lineSize; // the length of each pixel data line
        intData = new int[width * height]; // processed data
        data = new byte[actualBMPSize]; // unprocessed data

        int dataOffset = 0; // position in raw pixel data
        int intDataOffset = (height - 1) * width; // offset starts here, pixel data
                                                  // starts at bottom

        for (int i = height - 1; i >= 0; i--) {
            try {
                ds.readFully(data, dataOffset, lineLength);
            } catch (EOFException eof) {
                throw new Exception("Line was shorter than expected");
            } catch (IOException ioe) {
                throw new Exception("File unexpectedly closed");
            }
            unpack(data, intData, dataOffset, intDataOffset, width);
            dataOffset += lineLength; // moves to next line in raw pixel data
            intDataOffset -= width; // move upwards in pixel array
        }
    }

    public void unpack(byte[] raw, int[] res
                              , int rawOffset, int resOffset, int r) {
        int j = resOffset; // position in resOffset
        int k = rawOffset; //position in data
        int mask = 0xff; // used to ignore all but last 8 bits
        int colorMask = 0xff000000; // used to turn on bits in mask or input
        for (int i = 0; i < r; i++) {
            intData[j++] = colorMask
                | (((int)(raw[k++])) & mask)
                | ((((int)(raw[k++])) & mask) << 8)
                | ((((int)(raw[k++])) & mask) << 16);
        }
    }

    public void read() throws IOException {
        readBMPFileHeader();
        readBMPInfoHeader();
        skipColorTable();
        readPixelData();
    }
}
