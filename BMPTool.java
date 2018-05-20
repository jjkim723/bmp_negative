import java.nio.file.Files;
import java.nio.file.Paths;
import java.io.FileInputStream;
import java.io.DataInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

public class BMPTool {
    /** Creates BMP image from given pixel array and relevant headerInfo
     *  @param b, byte[] containing the pixel data
     *  @param offset, int of the starting index of pixel data in original file OR end of headers
     */

    /** One and only instance variable */
    private static final BMPTool bmpTool = new BMPTool();

    /** Private Constructor (singleton implementation) */
    private BMPTool() {} //

    /** Get method for single instance of tool */
    public static BMPTool getInstance( ) {
      return bmpTool;
   }

    protected void createModifiedBMP(BMPImage img, byte[] b, int offset) throws IOException {
        final DataInputStream ds = new DataInputStream(new FileInputStream(img.filename)); // for reading BMP file
        final String outfile = img.filename.substring(0, img.filename.lastIndexOf('.')) + "_EDITED.bmp";
        FileOutputStream fs = new FileOutputStream(outfile); // output file
        // copying header into the outfile
        for (int i = 0; i < img.pOffset; i++) {
            fs.write(ds.readByte());
        }

        fs.write(b); // writing the pixel array
        fs.close(); // closing resources
        ds.close(); // closing resources
    }

    /** Creates copy of BMP image */
    protected void copyBMP(BMPImage img) throws IOException {
        String outfile = img.filename.substring(0, img.filename.lastIndexOf('.')) + "_COPY.bmp";
        FileOutputStream fs = new FileOutputStream(outfile);
        Files.copy(Paths.get(img.filename), fs);
        fs.close();
    }
}