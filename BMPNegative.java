public class BMPNegative {
    public static final int BITMAPFILEHEADER_SIZE = 14; // 14 bytes
    public static final int BITMAPINFOHEADER_SIZE = 40; // 40 bytes

    /** Prints help message
     */
    public static void helpMessage() {
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
            return "bmp".equals(filename.substring(filename.lastIndexOf(".") + 1));
        } catch (Exception e) {
            return false;
        }
    }

    //ignore BITMAPINFOHEADER (14 bytes) and read BITMAPINFOHEADER (40 bytes) - need

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

        if (count != 1) { // only one .bmp is allowed
            helpMessage();
            System.out.println();
        }
        //run here
    }
}
