import java.io.FileInputStream;
import java.io.DataInputStream;
import java.io.FileNotFoundException;
import java.io.EOFException;
import java.io.IOException;

public class BMPEditor {
    public static final int READ_LIMIT = 100; // default for marking file
    public static final int BYTES_IN_HEADER = 54;
    public static final short BITS_PER_PIXEL = 24; // assumption
    public static final int COMPRESSION = 0; //assumption
    //public static final short FILE_EXT = 0x4d42; // 'BM' file signature

    String filename; // filename of image
    int pOffset; // byte location of start of pixel array
    int pWidth; // width in pixels of pixel array
    int pHeight; // height in pixels of pixel array
    int bpp; // bits per pixel
    int compression; // expected (=0)

    // variables for reading
    int pArraySize; // size of pixel array with padding (multiple of 4 bytes)
    int pRowSize; // length of row of pixels with padding
    byte[] pixelNegativeArr; // processed pixel array
    DataInputStream ds; // handles reading differing byte lengths of information

    // constructor, requires file to be read
    public BMPEditor(String filename) throws FileNotFoundException{
        try {
            this.ds = new DataInputStream(new FileInputStream(filename));
        } catch (FileNotFoundException fe) {
            System.err.println(filename + " could not be properly opened");
            fe.printStackTrace();
        }
    }

    /** Reads and records important information to manipulate pixel array
     *  contains 14 bytes (total): ordering based on BMP structure
     */
    public void readHeader() throws IOException, Exception {
        // exits when file type is not BMP
        if ((char)this.ds.read() != 'B' || (char)this.ds.read() != 'M') {
            throw new Exception("Not BMP file");
        }
        this.ds.readInt(); // skip 'FileSize
        this.ds.readInt(); // skip 'reserved'
        this.pOffset = convertInt(this.ds.readInt());
    }

    /** Reads and records important information to manipulate pixel array
     *  contains 40 bytes (total): ordering based on BMP structure
     */
    public void readInfoHeader() throws IOException, Exception {
        this.ds.readInt(); // skip size of Header
        this.pWidth = (int) this.ds.readInt();
        this.pHeight =  Math.abs((int) this.ds.readInt()); // height may be negative
        this.ds.readShort(); // skip planes
        //this.bpp = (int) this.ds.readShort(); //returns a 16-bit number, which javac interprets as char
        char i = 6144;
        char j = 24;
        System.out.println(i + " " + j);
        System.out.print("this is masking:");
        System.out.println(i & j);
        System.out.println(this.ds.readShort());
        System.out.println(this.bpp + "\n" + BITS_PER_PIXEL);
        if (this.bpp != BITS_PER_PIXEL) { // must be 24 bpp (assumption)
            throw new Exception("Usage: Only 24 bpp supported");
        }
        this.compression = (int) this.ds.readInt();
        System.out.println(this.compression + "\n" + COMPRESSION);
        if (this.compression != COMPRESSION) { // must be uncompressed (assumption)
            throw new Exception("Usage: Only uncompressed files supported");
        }

        // calculating length of row with padding (equation from wikipedia)
        this.pRowSize = ((this.pWidth * this.bpp + 31) / 32) * 4;
        this.pArraySize = pRowSize * pHeight;
    }

    /** Records the negative of pixel data into 'pixelArr'*/
    public void setPixelNegativeArray() throws IOException, Exception {
        byte[] raw = new byte[pArraySize]; // unprocessed data
        this.pixelNegativeArr = new byte[this.pWidth * this.pHeight]; // only pixel data

        // reopening file -> skipping to pixel data
        this.ds.close();
        this.ds = new DataInputStream(new FileInputStream(filename));
        this.ds.skip(pOffset);

        int rawOffset = 0;
        int offset = (this.pHeight - 1) * this.pWidth;
        for (int i = this.pHeight - 1; i >= 0; i--) {
            int bytesRead = ds.read(raw, rawOffset, this.pRowSize);
            if (bytesRead < this.pRowSize) {
                throw new Exception("Error: Pixel data read incorrectly");
            }
            convertToNegative(raw, rawOffset, this.pixelNegativeArr, this.pRowSize);
            rawOffset += this.pRowSize;
            offset -= this.pWidth;
        }
    }

    public void convertToNegative(byte[] rawData, int offset, byte[] res, int w) {
        int j = offset;
        int mask = 0xff; // to retrieve lat 8 bits
        for (int i = 0; i < w; i++) {
            res[j] = (byte) ~(255 - (((int)(rawData[j++])) & mask) ^ mask); // blue
            res[j] = (byte) ~((255 - ((((int)(rawData[j++])) & mask) << 8) >> 8) ^ mask); // green
            res[j] = (byte) ~((255 - ((((int)(rawData[j++])) & mask) << 16) >> 16) ^ mask); // red
        }
    }
    /** Reads raw data and outputs the negative of the array
     *  @return res, byte[] of inverted bits
     */
    // public byte[] negatePixelArray() throws EOFException, IOException, Exception {
    //     byte[] rawData = new byte[pArraySize]; // unprocessed data
    //     byte[] res = new byte[pArraySize]; // altered data

    //     //reopening file
    //     this.ds.close();
    //     this.ds = new DataInputStream(new FileInputStream(this.filename));
    //     this.ds.skipBytes(BYTES_IN_HEADER); // skip to pixel array

    //     // reading rows of pixels
    //     int rawOffset = 0; // position in raw pixel data
    //     for (int i = this.pHeight - 1; i >= 0; i--) {
    //         try {
    //             ds.readFully(rawData, rawOffset, this.pArraySize);
    //         } catch (EOFException eof) {
    //             throw new Exception("Line was shorter than expected");
    //         } catch (IOException ioe) {
    //             throw new Exception("File unexpectedly closed");
    //         }
    //         negate(rawData, res, rawOffset, this.pArraySize);
    //         rawOffset += this.pArraySize; // moves to next line in raw pixel data
    //     }
    //     return res;
    // }

    /** Flips the bits of the byte array of pixel
     */
    // public void negate(byte[] raw, byte[] res, int rawOffset, int width) {
    //     int k = rawOffset; //position in data
    //     for (int i = 0; i < width; i++) {
    //         res[k] = (byte) ~raw[k++];
    //         res[k] = (byte) ~raw[k++];
    //         res[k] = (byte) ~raw[k++];
    //     }
    // }

    // public byte[] calculateNegative() throws IOException, Exception {
    //     readHeader();
    //     readInfoHeader();
    //     return negatePixelArray();
    // }

    /** Converts 16-bit number into local format */
    private static int convertShort(int i) {
        return ((i >> 8) & 0xff) + ((i << 8) & 0xff00);
    }

    /** Converts 32-bit number into local format */
    private static int convertInt(int i) {
        return ((i & 0xff) << 24) + ((i & 0xff00) << 8) +
            ((i & 0xff0000) >> 8) + ((i >> 24) & 0xff);
    }

    public static void main(String[] args) throws IOException, Exception {
        BMPEditor b = new BMPEditor("sample.bmp");
        b.readHeader();
        b.readInfoHeader();
        // for (byte i : b.calculateNegative()) {
        //     System.out.println(i);
        // }
    }
}
