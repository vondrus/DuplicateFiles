import java.io.File;
import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;

class ThreadEngine {
    private final LinkedBlockingQueue<Couple<File, File>> inputQueue;
    private final LinkedBlockingQueue<Couple<File, File>> outputQueue;
    private boolean interactiveMode;
    private int threadCount;
    private ThreadTemplate[] threads;

    ThreadEngine(LinkedBlockingQueue<Couple<File, File>> inputQueue, LinkedBlockingQueue<Couple<File, File>> outputQueue, int threadCount, boolean interactiveMode) {
        this.inputQueue = inputQueue;
        this.outputQueue = outputQueue;
        this.interactiveMode = interactiveMode;
        this.threadCount = threadCount;
        this.threads = new ThreadTemplate[threadCount + 1];
    }

    private void showThreadStates() {
        System.out.println(
                "---------------------------------------------\n" +
                "Current state of all threads:"
        );

        for (int i = 0; i < threadCount + 1; i++) {
            System.out.println("   " + i + " : " + threads[i].getThreadName() + " : " + threads[i].getThreadState());
        }

        System.out.print(
                "---------------------------------------------\n" +
                "Write command or press Enter to refresh list.\n>"
        );
    }

    private boolean allFileComparatorThreadsCompleted() {
        for (int i = 1; i < threadCount + 1; i++) {
            if (threads[i].getThreadState() != ThreadState.STOPPED) {
                return false;
            }
        }
        return true;
    }

    void start() {
        ExecutorService executorService = Executors.newFixedThreadPool(threadCount + 1);
        UserControl userControl = new UserControl();
        Couple<UserControlCommand, Integer> userInterfaceCommand;

        // Create and start ResultWriter thread
        threads[0] = new ResultWriter(inputQueue, outputQueue, "ResultWriter", interactiveMode);
        executorService.execute(threads[0]);

        // Create and start FileComparator threads
        for (int i = 1; i < threadCount + 1; i++) {
            threads[i] = new FileComparator(inputQueue, outputQueue, "FileComparator" + i, interactiveMode);
            executorService.execute(threads[i]);
        }

        // Application main loop
        while (true) {

            if (interactiveMode) {

                showThreadStates();
                userInterfaceCommand = userControl.getCommand();

                if (userInterfaceCommand != null) {
                    int threadNumber = userInterfaceCommand.getSecond();

                    // Execute selected action with selected thread (or with all threads)
                    switch (userInterfaceCommand.getFirst()) {
                        // STOP thread/threads
                        case STOP:
                            if (threadNumber < 0) {
                                for (int i = 0; i < threadCount + 1; i++) {
                                    threads[i].stop();
                                }
                            } else {
                                threads[threadNumber].stop();
                            }
                            break;

                        // SUSPEND thread/threads
                        case SUSPEND:
                            if (threadNumber < 0) {
                                for (int i = 0; i < threadCount + 1; i++) {
                                    threads[i].suspend();
                                }
                            } else {
                                threads[threadNumber].suspend();
                            }
                            break;

                        // CONTINUE thread/threads
                        case CONTINUE:
                            if (threadNumber < 0) {
                                for (int i = 0; i < threadCount + 1; i++) {
                                    threads[i].resume();
                                }
                            } else {
                                threads[threadNumber].resume();
                            }
                            break;

                        // RESTART thread/threads
                        case RESTART:
                            if (threadNumber < 0) {
                                for (int i = 0; i < threadCount + 1; i++) {
                                    if (threads[i].getThreadState() == ThreadState.STOPPED) {
                                        executorService.execute(threads[i]);
                                    }
                                }
                            } else if (threads[threadNumber].getThreadState() == ThreadState.STOPPED) {
                                executorService.execute(threads[threadNumber]);
                            }
                            break;

                        // Incorrect syntax of user input
                        case INCORRECT:
                            System.out.println("Incorrect command.");
                            break;
                    }
                }

                System.out.println();

            }   // if (interactiveMode)

            try {
                // All comparator threads are completed - finish the application
                if (allFileComparatorThreadsCompleted()) {
                    // Enforce to stop Result Writer thread
                    threads[0].putOutputQueue(new Couple<>(null, null));

                    // Break the main loop
                    break;
                }

                // Wait a moment, give a time to other threads
                if (! interactiveMode) {
                    Thread.sleep(100);
                }
            }
            catch (InterruptedException e) {
                e.printStackTrace();
            }

        }   // while

        executorService.shutdown();
        // noinspection StatementWithEmptyBody
        while (! executorService.isTerminated());

    }   // start()

}
