import java.nio.file.Files;
import java.nio.file.Paths;
import java.io.FileInputStream;
import java.io.DataInputStream;
import java.io.FileOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

public class BMPImage {
    public static final short BITS_PER_PIXEL = 24; // assumption
    public static final int COMPRESSION = 0; //assumption
    public static final int FILE_HEADER_SIZE = 14; // 14 bytes from BMP specification

    String filename; // filename of image
    int pOffset; // start index of pixel array
    int pWidth; // width in pixels of pixel array
    int pHeight; // height in pixels of pixel array
    int bpp; // bits per pixel
    int compression; // expected (=0)

    // variables for reading
    int pArraySize; // size of pixel array with padding (multiple of 4 bytes)
    int pRowSize; // length of row of pixels with padding
    int paddingSize; // bytes added at end of pixel array
    byte[] rawPixelArr; // raw pixel data array
    byte[] negativePixelArr; // processed pixel data

    /** Constructor, requires filename to be given */
    public BMPImage(String filename) throws FileNotFoundException, IOException, Exception {
        try {
        	this.filename = filename;
            setAll();
        } catch (FileNotFoundException fe) {
            System.err.println("Error: " + filename + " could not be properly opened");
            fe.printStackTrace();
        } catch (IOException ie) {
        	System.err.println("Error: Reading " + filename);
            ie.printStackTrace();
        }
    }

    /** Opens image and sets all instance variables */
    private void setAll() throws IOException, Exception {
    	this.readHeader();
    	this.readInfoHeader();
    	this.setPixelArray();
    	this.setNegativePixelArray();
    }

    /** Reads and records important information to manipulate pixel array
     *  contains 14 bytes (total): ordering based on BMP structure
     */
    private void readHeader() throws IOException, Exception {
    	DataInputStream ds = new DataInputStream(new FileInputStream(filename)); // reading BMP file
        // exits when file type is not BMP
        if ((char) ds.read() != 'B' || (char) ds.read() != 'M') { // Howon: readChar()?
        	ds.close();
            throw new Exception("Error: Wrong image file type");
        }
        ds.readInt(); // skip 'FileSize
        ds.readInt(); // skip 'reserved'
        this.pOffset = convertInt(ds.readInt());
        ds.close();
    }

    /** Reads and records important information to manipulate pixel array
     * ordering of data based on BMP structure
     */
    private void readInfoHeader() throws IOException, Exception {
    	DataInputStream ds = new DataInputStream(new FileInputStream(filename)); // reading BMP file
    	ds.skip(FILE_HEADER_SIZE); // size of fileHeader in BMP is 14 bytes
        ds.readInt(); // skip size of Header
        this.pWidth = convertInt(ds.readInt());
        this.pHeight =  Math.abs(convertInt(ds.readInt())); // height may be negative
        ds.readShort(); // skip planes
        this.bpp = convertShort(ds.readShort());

        if (this.bpp != BITS_PER_PIXEL) { // must be 24 bpp (assumption)
        	ds.close();
            throw new Exception("Usage: Only 24 bpp supported"); 
        }

        this.compression = ds.readInt();

        if (this.compression != COMPRESSION) { // must be uncompressed (assumption)
        	ds.close();
            throw new Exception("Usage: Only uncompressed files supported");
        }

        // calculating length of row with padding (equation from wikipedia)
        this.pRowSize = ((this.pWidth * this.bpp + 31) / 32) * 4; // don't hardcode this.
        this.paddingSize = this.pRowSize - (this.pWidth * 3); // 3 bytes per pixel (using 24 bpp image)
        this.pArraySize = pRowSize * pHeight;
        ds.close(); // done reading infoHeader
    }

    /** Sets the instance variable rawPixelArr from read info */
    // Howon: Use Javadoc style comments.
    private void setPixelArray() throws IOException, Exception {
    	// pOffset was not set
    	if (this.pOffset == 0) {
    		throw new Exception("Error: Unknown start of pixel array data");
    	}
        // skipping to pixel data
    	final DataInputStream ds = new DataInputStream(new FileInputStream(filename)); // reading BMP file.
        ds.skip(this.pOffset);

        this.rawPixelArr = new byte[this.pArraySize]; //initializing the byte array for pixels
        ds.readFully(this.rawPixelArr, 0, this.pArraySize);
        ds.close(); // close the resource after use
    }

    /** Sets the instance variable negativePixelArr from with read info */
    private void setNegativePixelArray() throws IOException, Exception {
    	if (this.rawPixelArr == null) {
    		throw new Exception("Error: Pixel array not initialized");
    	}

        this.negativePixelArr = new byte[this.pArraySize]; //initializing the byte array for pixels

        for (int i = 0; i < this.rawPixelArr.length; i++) {
        	// the end of the pixel and start of padding reached
        	if ((i + 1 + this.paddingSize) % this.pRowSize == 0) {
        		//skipping the padding
        		for (int j = 1; j <= this.paddingSize; j++) {
        			this.negativePixelArr[i + j] = 0;
        		}

        		i += this.paddingSize; //move over the padded numbers
        	} else {
        		// subtracts original pixel value from 255 to negate, then convert back to byte
        		this.negativePixelArr[i] = (byte) (255 - (this.rawPixelArr[i] & 0xff) & 0xff);
        	}
        }
    }

    // Following two functions are structured this way, BMP is in litte-endian format
    /**
     * Converts 16-bit little-endian order number into int
     * 	Used since when readShort() returns two bytes together -> interpret as a single int
     * 	@param	i, 16-bit number from BMP file
     *  @return 16-bit number as an int
     */
    private int convertShort(int i) {
        // ordered in BMP: b1 b2, where b1 is the least significant
        // masks ensure 8-bit partitions and ordering is handled properly
        // b2 shifted 8 bits left
        // b1 shifted 8 bits right
        // , then added
        return ((i >> 8) & 0xff) + ((i << 8) & 0xff00);
    }

    /**
     * Converts 32-bit little-endian number into int
     *	@param	i, 32-bit little-endian number order from BMP file
     *  @return 32-bit number as an int
     */
    private int convertInt(int i) {
        // similar to convertShort
        // ordered in BMP: b1 b2 b3 b4, where b1 is the least significant
        // b4 shifted 24 bits left
        // b3 shifted 8 bits left
        // b2 shifted 8 bits right
        // b1 shifted 24 bits right
        // , then added
        return ((i & 0xff) << 24) + ((i & 0xff00) << 8) +
            ((i & 0xff0000) >> 8) + ((i >> 24) & 0xff);
    }

    @Override
    public String toString() {
        return "pOffset: " + this.pOffset + "\n"
                + "bpp: " + this.bpp + "\n"
                + "compression: " + this.compression + "\n"
                + "pWidth: " + this.pWidth + "\n"
                + "pHeight: " + this.pHeight + "\n"
                + "pRowSize: " + this.pRowSize + "\n"
                + "pArraySize: " + this.pArraySize;
    }
}
