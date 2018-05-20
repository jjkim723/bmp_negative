import java.util.Scanner;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

public class Run {
    /** Prints help message */
    public static void helpMessage() {
        final String out = "Usage: java Run [--options] \n" 
            + "where options include: \n"
            + "\t --help  print help message";
        System.out.println(out);
    }

    /** Searches for "--help" in user input*/
    public static boolean needsHelp(String[] args) {
        for (final String s : args) {
            if (s.equals("--help")) {
                return true;
            }
        }

        return false;
    }

    /** Checks if file exists
     *  @param filename, file to be checked
     *  @return true if exists, otherwise false
     */
    public static boolean fileExists(String filename) {
        final File f = new File(filename);
        return (f.exists() && !f.isDirectory());
    }

    /** Prints menu options */
    public static void printMenu() {
        final String out = "\nWelcome to the BMP Image Editor: \n" 
            + "Press \"1\" : Create copy of BMP file \n"
            + "Press \"2\" : Create photo negative of BMP file \n"
            + "Press \"3\" : Change file \n"
            + "Press \"0\" : Quit \n";
        System.out.println(out);
    }

    /** Menu: Will loop until done */
    public static void menu() throws FileNotFoundException, IOException, Exception {
        Scanner reader = new Scanner(System.in); // reads user input
        BMPTool bTool = BMPTool.getInstance(); // tool to read
        BMPImage bImage = null;
        String file;
        // getting initial file
        do {
            System.out.println("Please enter filename: ");
            file = reader.next(); // file to be used
            if (fileExists(file)) {
                bImage = new BMPImage(file);
            }
        } while (bImage == null);

        String choice; // holds user input
        do {
            printMenu();
            choice = reader.next();
            if (choice.equals("1")) {
                option1(bImage, bTool);
            } else if (choice.equals("2")) {
                option2(bImage, bTool);
            } else if (choice.equals("3")) {
                bImage = null; // change file
                do {
                    System.out.println("Please enter filename: ");
                    file = reader.next(); // file to be used
                    if (fileExists(file)) {
                        bImage = new BMPImage(file);
                    }
                } while (bImage == null);
                System.out.println("Changed File Successfully!");
            }
        } while (choice.equals("0") == false);
        System.out.println("Thanks for using this tool!");
    }

    /** Option 1: Creates copy of BMP file */
    //just delcare it outside and then pass them in.
    public static void option1(BMPImage img, BMPTool tool) throws FileNotFoundException, IOException, Exception {
        tool.copyBMP(img);
        System.out.println("Copied Successfully!");
    }

    /** Option 2: Creates photo negative of BMP file */
    public static void option2(BMPImage img, BMPTool tool) throws FileNotFoundException, IOException, Exception {
        tool.createModifiedBMP(img, img.negativePixelArr, img.pOffset);
        System.out.println("Photo Negative Created Successfully!");
    }


    public static void main(String[] args) throws FileNotFoundException, IOException, Exception {
        // user asks for help or there are extra arguments
    	if (needsHelp(args) || args.length != 0) {
            helpMessage();
            System.exit(0);
        }
	    menu();
    }
}