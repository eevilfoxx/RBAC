import java.util.ArrayList;
import java.util.List;

public class MultiThreadProgressApp {

    private static final int THREAD_COUNT = 5;
    private static final int WORK_STEPS = 30;
    private static final int STEP_DELAY_MS = 100;
    
    private static volatile int[] progress = new int[THREAD_COUNT];
    private static long[] threadIds = new long[THREAD_COUNT];
    private static long[] startTimes = new long[THREAD_COUNT];
    private static long[] endTimes = new long[THREAD_COUNT];
    private static volatile boolean[] finished = new boolean[THREAD_COUNT];

    static class Worker implements Runnable {

        private final int index;

        public Worker(int index) {
            this.index = index;
        }

        @Override
        public void run() {
            threadIds[index] = Thread.currentThread().getId();
            startTimes[index] = System.currentTimeMillis();

            try {
                for (int i = 0; i <= WORK_STEPS; i++) {
                    progress[index] = i;
                    Thread.sleep(STEP_DELAY_MS);
                }
            } catch (InterruptedException ignored) {}

            endTimes[index] = System.currentTimeMillis();
            finished[index] = true;
        }
    }

    public static void main(String[] args) throws InterruptedException {

        List<Thread> threads = new ArrayList<>();

        for (int i = 0; i < THREAD_COUNT; i++) {
            Thread t = new Thread(new Worker(i));
            threads.add(t);
            t.start();
        }

        boolean running = true;
        while (running) {
            running = false;

            for (int i = 0; i < THREAD_COUNT; i++) {
                if (!finished[i]) {
                    running = true;
                    break;
                }
            }

            print();
            Thread.sleep(50);
        }

        print();

        for (Thread t : threads) {
            t.join();
        }
    }

    private static void print() {
        System.out.print("\033[H\033[2J");
        System.out.flush();

        for (int i = 0; i < THREAD_COUNT; i++) {

            StringBuilder bar = new StringBuilder();
            bar.append("[");

            for (int j = 0; j < WORK_STEPS; j++) {
                if (j < progress[i]) {
                    bar.append("#");
                } else {
                    bar.append(" ");
                }
            }
            bar.append("]");

            String timeInfo = "";
            if (finished[i]) {
                long time = endTimes[i] - startTimes[i];
                timeInfo = " | time: " + time + " ms";
            }

            System.out.printf(
                    "Thread #%d | id=%d | %s%s%n",
                    i + 1,
                    threadIds[i],
                    bar,
                    timeInfo
            );
        }
    }
}