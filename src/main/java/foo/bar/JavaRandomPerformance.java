package foo.bar;

import java.util.Random;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ThreadLocalRandom;

public class JavaRandomPerformance {

    public static void main(String[] args) throws Exception {
        JavaRandomPerformance rt = new JavaRandomPerformance();

        int size = 10_000_000;

        System.out
                .println("Threads, Math.random(), Random.nextDouble(), ThreadLocalRandom.nextDouble(), new Random().nextDouble()");

        for (int threads = 1; threads <= 15; threads++) {

            long rnd1 = rt.run(threads, size / threads, new MathRandom());
            long rnd2 = rt.run(threads, size / threads, new SingleUtilRandom());
            long rnd3 = rt.run(threads, size / threads, new ThreadRandom());
            long rnd4 = rt.run(threads, size / threads, new InstanceUtilRandom());

            System.out.println(String.format("%d, %d, %d, %d, %d", threads,
                    rnd1, rnd2, rnd3, rnd4));
        }
    }

    /**
     * Call random generator in parallel threads.
     *
     * @param threads   Number of threads
     * @param size      Size of the loop in each thread.
     * @param generator Random generator instance.
     * @return Execution time in milliseconds.
     * @throws InterruptedException
     */
    public long run(final int threads, final int size,
                    final RandomGenerator generator) throws InterruptedException {
        final CountDownLatch startGate = new CountDownLatch(1);
        final CountDownLatch endGate = new CountDownLatch(threads);

        // create same task for all threads
        final Runnable task = new Runnable() {
            public void run() {
                for (int i = 0; i < size; i++) {
                    generator.random();
                }
            }
        };

        // create all threads and wait
        for (int i = 0; i < threads; i++) {
            new Thread() {
                public void run() {
                    try {
                        startGate.await();
                        try {
                            task.run();
                        } finally {
                            endGate.countDown();
                        }
                    } catch (InterruptedException ignored) {
                    }
                }
            }.start();
        }

        // start execution
        long start = System.currentTimeMillis();
        startGate.countDown();

        // wait and calculate time in millis
        endGate.await();
        long end = System.currentTimeMillis();

        return end - start;
    }

    /**
     * Simple interface to enforce strategy pattern.
     */
    static interface RandomGenerator {
        public double random();
    }

    static class MathRandom implements RandomGenerator {
        public double random() {
            return Math.random();
        }
    }

    static class SingleUtilRandom implements RandomGenerator {

        private Random random = new Random();

        public double random() {
            return random.nextDouble();
        }
    }

    static class ThreadRandom implements RandomGenerator {
        public double random() {
            return ThreadLocalRandom.current().nextDouble();
        }
    }

    static class InstanceUtilRandom implements RandomGenerator {
        public double random() {
            return new Random().nextDouble();
        }
    }
}
