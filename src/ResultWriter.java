import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.concurrent.LinkedBlockingQueue;

public class ResultWriter extends ThreadTemplate {
    private BufferedWriter bufferedWriter;

    ResultWriter(LinkedBlockingQueue<Couple<File, File>> inputQueue, LinkedBlockingQueue<Couple<File, File>> outputQueue, String threadName, boolean interactiveMode) {
        super(inputQueue, outputQueue, threadName, interactiveMode);
    }

    @Override
    public void beforeTask() throws IOException {
        final FileWriter fileWriter = new FileWriter("DuplicateFiles.txt");
        bufferedWriter = new BufferedWriter(fileWriter);
    }

    @Override
    public boolean task() throws IOException {

        Couple<File, File> sameFiles = pollOutputQueue();
        if (sameFiles != null) {

            if ((sameFiles.getFirst() != null) && (sameFiles.getSecond() != null)) {

                // Write message to console in non-interactive mode
                if (isNotInteractiveMode()) {
                    System.out.println(getThreadName() + ": " + sameFiles.getFirst().getPath() + " equals " + sameFiles.getSecond().getPath());
                }

                // Write identical pair to output file
                bufferedWriter.write(sameFiles.getFirst().getPath() + ", " + sameFiles.getSecond().getPath() + System.lineSeparator());

            }
            else {
                // Null pair detected - stop this thread
                return false;
            }

        }

        // Let this thread running
        return true;
    }

    @Override
    public void afterTask() throws IOException {
        bufferedWriter.close();
    }

}
