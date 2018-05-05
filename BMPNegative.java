import java.io.*;

public class BMPNegative {

    /** Prints help message
     */
    public static void helpMessage() {
        final String NEWLINE = System.getProperty("line.separator");
        String out = "Usage: java BMPNegative [--options] \n"
            + "where options include: \n"
            + "\t --help  print help message";
        System.out.println(out);
    }

    /** Check whether the file extension is 'bmp'
     *  @param filename, String
     *  @return boolean, true if .bmp, otherwise, false
     */
    public static boolean equalsBMPExtension(String filename) {
        try {
            return ".bmp"
                .equals(filename.substring(filename.lastIndexOf(".") + 1));
        } catch (Exception e) {
            return false;
        }
    }

    public static void main(String[] args) {
        if (args.length < 1) { // must have bmp file at least as an argument
            helpMessage();
            System.exit(0);
        }

        int count = 0; // number of .bmp files
        for (String i : args) {
            if (i.equals("--help")) { // prints help message, when called
                helpMessage();
                System.exit(0);
            }

            if (equalsBMPExtension(i)) { // counts .bmp file(s) given
                count++;
            }
        }

        if (count != 1) { // only one .bmp is allowed
            helpMessage();
            System.out.println();
        }
        //run here
    }
}
