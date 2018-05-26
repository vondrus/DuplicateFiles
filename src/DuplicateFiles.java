import java.io.File;
import java.io.FileFilter;
import java.util.concurrent.LinkedBlockingQueue;

public class DuplicateFiles {
    private static final int MAX_THREAD_COUNT = 20;
    private static File directory;
    private static int threadCount;
    private static boolean interactiveMode;
    private static final LinkedBlockingQueue<Couple<File, File>> coupleQueue = new LinkedBlockingQueue<>();
    private static final LinkedBlockingQueue<Couple<File, File>> resultQueue = new LinkedBlockingQueue<>();

    /**
     *
     * Evaluates command line parameters.
     * Verifies number of parameters theirs syntax and existence of given directory.
     *
     * @param args the array of command line parameters
     * @return the value {@code true} if everything is alright;
     *         the value {@code false} if an error occurred;
     */
    private static boolean evaluateCommandLine(String[] args) {
        boolean returnValue = false;

        // Correct number of command line parameters
        if ((args.length == 2) || (args.length == 3)) {
            directory = new File(args[0]);
            if (directory.exists()) {
                // Parsing of number of threads
                try {
                    threadCount = Integer.parseUnsignedInt(args[1]);
                    if ((threadCount > 0) && (threadCount <= MAX_THREAD_COUNT)) {
                        returnValue = true;
                    }
                    else {
                        System.out.println("Error: Second parameter has to be from interval [1, " + MAX_THREAD_COUNT + "].");
                    }
                }
                catch (NumberFormatException e) {
                    System.out.println("Error: Second parameter is not a parsable positive integer.");
                }
                // Detect interactive mode parameter
                if (args.length == 3) {
                    interactiveMode = args[2].equalsIgnoreCase("I");
                }
            }
            else {
                System.out.println("Error: Directory " + directory.getAbsolutePath() + " does not exist.");
            }
        }

        // Incorrect number of command line parameters
        else {
            System.out.println(
                    "Error: Incorrect number of parameters.\n" +
                    "Usage: directoryName threadCount"
            );
        }

        return returnValue;
    }

    /**
     * Reads content of given directory.
     * Creates queue of couples of filenames to compare.
     *
     * @return the value {@code true} if everything is alright;
     *         the value {@code false} if an error occurred;
     */
    private static boolean evaluateDirectoryContent() {
        boolean returnValue = false;

        final FileFilter fileFilter = File::isFile;
        final File[] fileNames = directory.listFiles(fileFilter);

        if (fileNames != null) {
            if (fileNames.length > 1) {
                try {
                    for (int i = 0; i < fileNames.length - 1; i++) {
                        for (int j = i + 1; j < fileNames.length; j++) {
                            coupleQueue.put(new Couple<>(fileNames[i], fileNames[j]));
                        }
                    }
                    returnValue = true;
                }
                catch (InterruptedException e) {
                    System.out.println("Error: InterruptedException occurred.");
                }
            }
            else {
                System.out.println("Error: Directory " + directory.getAbsolutePath() + " is either empty or contains only one file.");
            }
        }
        else {
            System.out.println("Error: Variable fileNames is null.");
        }

        return returnValue;
    }

    public static void main(String[] args) {
        long startTime = System.currentTimeMillis();

        if (evaluateCommandLine(args)) {
            if (evaluateDirectoryContent()) {

                ThreadEngine threadEngine = new ThreadEngine(coupleQueue, resultQueue, threadCount, interactiveMode);
                threadEngine.start();

                if (! interactiveMode) {
                    long executionTime = System.currentTimeMillis() - startTime;
                    long minutes = executionTime / 60000;
                    long seconds = (executionTime % 60000) / 1000;
                    long millis = executionTime % 1000;
                    System.out.println("Execution time: " + String.format("%02d:%02d.%03d", minutes, seconds, millis));
                }
            }
        }
    }
}
