import java.io.File;
import java.io.IOException;
import java.nio.MappedByteBuffer;
import java.nio.file.Files;
import java.nio.channels.FileChannel;
import java.util.concurrent.LinkedBlockingQueue;

public class FileComparator extends ThreadTemplate {

    FileComparator(LinkedBlockingQueue<Couple<File, File>> inputQueue, LinkedBlockingQueue<Couple<File, File>> outputQueue, String threadName, boolean interactiveMode) {
        super(inputQueue, outputQueue, threadName, interactiveMode);
    }

    private static MappedByteBuffer mapChannel(FileChannel fileChannel, long position, long fileSize, int mapSize) throws IOException {
        final long end = Math.min(fileSize, position + mapSize);
        final long mapLength = (int)(end - position);
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, position, mapLength);
    }

    @Override
    public void beforeTask() { }

    @Override
    public boolean task() throws IOException, InterruptedException {

        Couple<File, File> filesToCompare = pollInputQueue();
        if (filesToCompare != null) {

            // Write message to console in non-interactive mode
            if (isNotInteractiveMode()) {
                System.out.println(
                        getThreadName() + ": comparing " +
                                filesToCompare.getFirst().getPath() + ", " +
                                filesToCompare.getSecond().getPath()
                );
            }

            final long fileSize1 = Files.size(filesToCompare.getFirst().toPath());
            final long fileSize2 = Files.size(filesToCompare.getSecond().toPath());

            // Both files are same size
            if (fileSize1 == fileSize2) {

                FileChannel fileChannel1 = (FileChannel)Files.newByteChannel(filesToCompare.getFirst().toPath());
                FileChannel fileChannel2 = (FileChannel)Files.newByteChannel(filesToCompare.getSecond().toPath());

                // 8MB of memory-mapped buffer
                final int mapSize = 8388608;

                // Pass through the files
                for (long position = 0; position < fileSize1; position += mapSize) {

                    // Some difference has been found - end of comparing
                    if (! mapChannel(fileChannel1, position, fileSize1, mapSize)
                            .equals(mapChannel(fileChannel2, position, fileSize2, mapSize))) {
                        return true;
                    }
                }

                // Both files are equal - write them to the output queue
                putOutputQueue(filesToCompare);
            }

            // Let this thread running
            return true;
        }

        // There are no more pairs of files to compare - stop this thread
        return false;
    }

    @Override
    public void afterTask() { }

}
