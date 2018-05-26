import java.io.File;
import java.io.IOException;
import java.util.concurrent.LinkedBlockingQueue;

public abstract class ThreadTemplate implements Runnable {
    private final LinkedBlockingQueue<Couple<File, File>> inputQueue;
    private final LinkedBlockingQueue<Couple<File, File>> outputQueue;
    private ThreadState threadState;
    private String threadName;
    private boolean interactiveMode;

    ThreadTemplate(LinkedBlockingQueue<Couple<File, File>> inputQueue, LinkedBlockingQueue<Couple<File, File>> outputQueue, String threadName, boolean interactiveMode) {
        this.inputQueue = inputQueue;
        this.outputQueue = outputQueue;
        this.threadState = ThreadState.INITIALIZED;
        this.threadName = threadName;
        this.interactiveMode = interactiveMode;
    }

    abstract void beforeTask() throws IOException;

    abstract boolean task() throws IOException, InterruptedException;

    abstract void afterTask() throws IOException;

    @Override
    public void run() {
        threadState = ThreadState.RUNNING;

        try {
            beforeTask();

            while (true) {

                boolean isThereSomeJob = task();

                synchronized (this) {
                    while (threadState == ThreadState.SUSPENDED) {
                        wait();
                    }
                    if (!isThereSomeJob) {
                        threadState = ThreadState.STOPPED;
                    }
                    if (threadState == ThreadState.STOPPED) {
                        break;
                    }
                }
            }

            afterTask();
        }
        catch (IOException | InterruptedException e) {
            e.printStackTrace();
            threadState = ThreadState.STOPPED;
        }
    }

    synchronized void stop() {
        if (threadState == ThreadState.RUNNING) {
            threadState = ThreadState.STOPPED;
            notify();
        }
    }

    synchronized void suspend() {
        if (threadState == ThreadState.RUNNING) {
            threadState = ThreadState.SUSPENDED;
        }
    }

    synchronized void resume() {
        if (threadState == ThreadState.SUSPENDED) {
            threadState = ThreadState.RUNNING;
            notify();
        }
    }

    Couple<File, File> pollInputQueue() {
        return inputQueue.poll();
    }

    Couple<File, File> pollOutputQueue() {
        return outputQueue.poll();
    }

    void putOutputQueue(Couple<File, File> couple) throws InterruptedException {
        outputQueue.put(couple);
    }

    ThreadState getThreadState() {
        return threadState;
    }

    String getThreadName() {
        return threadName;
    }

    boolean isNotInteractiveMode() {
        return !interactiveMode;
    }
}
