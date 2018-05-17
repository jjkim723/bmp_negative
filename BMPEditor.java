import java.nio.file.Files;
import java.nio.file.Paths;
import java.io.FileInputStream;
import java.io.DataInputStream;
import java.io.FileOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

public class BMPEditor {
    public static final short BITS_PER_PIXEL = 24; // assumption
    public static final int COMPRESSION = 0; //assumption

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
    public BMPEditor(String filename) throws FileNotFoundException, IOException, Exception {
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
        if ((char) ds.read() != 'B' || (char) ds.read() != 'M') {
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
    	ds.skip(14); // size of fileHeader in BMP is 14 bytes
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
        this.pRowSize = ((this.pWidth * this.bpp + 31) / 32) * 4;
        this.paddingSize = this.pRowSize - (this.pWidth * 3); // 3 bytes per pixel (using 24 bpp image)
        this.pArraySize = pRowSize * pHeight;
        ds.close(); // done reading infoHeader
    }
    
    /** Sets the instance variable rawPixelArr from with read info */
    private void setPixelArray() throws IOException, Exception {
    	// pOffset was not set
    	if (this.pOffset == 0) {
    		throw new Exception("Error: Unknown start of pixel array data");
    	}
        // skipping to pixel data
    	DataInputStream ds = new DataInputStream(new FileInputStream(filename)); // reading BMP file
        ds.skip(this.pOffset);

        this.rawPixelArr = new byte[this.pArraySize]; //initializing the byte array for pixels
        ds.readFully(this.rawPixelArr, 0, this.pArraySize);
        ds.close(); //close the resource after use
    }
    
    /** Sets the instance variable negativePixelArr from with read info */
    private void setNegativePixelArray() throws IOException, Exception {
    	if (this.rawPixelArr == null) {
    		throw new Exception("Error: Pixel array not initialized");
    	}
        this.negativePixelArr = new byte[this.pArraySize]; //initializing the byte array for pixels
        for (int i = 0; i < this.rawPixelArr.length; i++) { 
        	// the end of the pixel and start of padding reached
        	if ((i + this.paddingSize + 1) % this.pRowSize == 0) {
        		//skipping the padding
        		for (int j = 1; j <= this.paddingSize; j++) {
        			this.negativePixelArr[i + j] = 0;
        		}
        		i += this.paddingSize; //move over the padded numbers
        	}
        	else {
        		// subtracts original pixel value from 255 to negate, then convert back to byte
        		this.negativePixelArr[i] = (byte) (255 - (this.rawPixelArr[i] & 0xff) & 0xff);
        	}
        }
    }
    
    /** Creates BMP image from given pixel array and relevant headerInfo
     *  @param b, byte[] containing the pixel data
     *  @param offset, int of the starting index of pixel data in original file OR end of headers
     */
    public void createModifiedBMP(byte[] b, int offset) throws IOException {
    	DataInputStream ds = new DataInputStream(new FileInputStream(filename)); // for reading BMP file
        String outfile = this.filename.substring(0, this.filename.lastIndexOf('.')) + "_EDITED.bmp";
        FileOutputStream fs = new FileOutputStream(outfile); // output file
        // copying header into the outfile
        for (int i = 0; i < this.pOffset; i++) { 
        	fs.write(ds.readByte());
        }
        fs.write(b); // writing the pixel array
        fs.close(); // closing resources
        ds.close(); // closing resources
    }
    
    /** Creates copy of BMP image */
    public void copyBMP() throws IOException {
        String outfile = this.filename.substring(0, this.filename.lastIndexOf('.')) + "_COPY.bmp";
        FileOutputStream fs = new FileOutputStream(outfile);
        Files.copy(Paths.get(filename), fs);
        fs.close();
    }
    
    /** Converts 16-bit number into int 
     * 	Used since when readShort() returns two bytes together -> interpret as a single int
     * 	@param	i, 16-bit number
     *  @return 16-bit number as an int
     */
    private int convertShort(int i) {
        return ((i >> 8) & 0xff) + ((i << 8) & 0xff00);
    }

    /** Converts 32-bit number into int
     * similar to convertShort(), but for 4 bytes mashed together) 
     *	@param	i, 32-bit number
     *  @return 32-bit number as an int
     */
    private int convertInt(int i) {
        return ((i & 0xff) << 24) + ((i & 0xff00) << 8) +
            ((i & 0xff0000) >> 8) + ((i >> 24) & 0xff);
    }
    
    /** Prints values of the BMP image read*/
    public void printValues() {
    	System.out.println("pOffset: " + this.pOffset);
    	System.out.println("bpp: " + this.bpp);
    	System.out.println("compression: " + this.compression);
    	System.out.println("pWidth: " + this.pWidth);
    	System.out.println("pHeight: " + this.pHeight);
    	System.out.println("pRowSize: " + this.pRowSize);
    	System.out.println("pArraySize: " + this.pArraySize);
    }
    
    /** Prints help message */
    public static void helpMessage() {
        String out = "Usage: java BMPNegative [--options] \n"
            + "where options include: \n"
            + "\t --help  print help message";
        System.out.println(out);
    }

    public static void main(String[] args) throws IOException, Exception {
    	if (args.length < 1) { // must have bmp file at least as an argument
            helpMessage();
            System.exit(0);
        }

        int count = 0; // number files given
        for (String i : args) {
            if (i.equals("--help")) { // prints help message, when called
                helpMessage();
                System.exit(0);
            }
            count++; // argument count 
        }

        if (count != 1) { // only one .bmp is allowed
            helpMessage();
            System.exit(0);
        }	
        // making negative of the BMP image
        BMPEditor img = new BMPEditor(args[0]);
        img.createModifiedBMP(img.negativePixelArr, img.pOffset);
        System.out.println("Negated Image Made as " + "\'filename\'_EDITED.bmp");
    }		
}
