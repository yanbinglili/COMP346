import java.util.concurrent.Semaphore;

public class DiningPhilosophers {
    static class Fork { }

    static class Philosopher implements Runnable {
        final int id;
        final Fork left, right;
        final Semaphore waiter;

        Philosopher(int id, Fork left, Fork right, Semaphore waiter) {
            this.id = id; this.left = left; this.right = right; this.waiter = waiter;
        }

        public void run() {
            try {
                for (int i=0; i<3; i++) {
                    // let the philosopher pause (think) for a while before trying to eat again
                    think();

                    // acquire the waiter before they eat, and if the waiter says the table is full (N-1 people),
                    // then they have to wait in line.
                    waiter.acquire();

                    // synchronize keyword will make sure only 1 person can access 1 fork at a time
                    synchronized (left) {
                        synchronized (right) {
                            eat();
                        }
                    }
                    // after eating, return the "seat" to the waiter so that other philosophers can come in.
                    waiter.release();
                }
            } catch (InterruptedException ignored) {}
        }

        void think() { sleep(10); }
        void eat()   { System.out.println("Philosopher " + id + " eats"); sleep(10); }
        static void sleep(long ms){ try { Thread.sleep(ms);} catch (InterruptedException ignored) {} }
    }

    public static void main(String[] args) throws Exception {
        final int N = 5;
        Fork[] forks = new Fork[N];
        for(int i=0;i<N;i++)
            forks[i] = new Fork();

        // At most N-1 philosophers can try to get a fork at the same time.

        // This way at least one philosopher will take nothing
        // → someone always manages to get two forks and eat without any problems
        // → release the forks after eating and the system continues to run.
        Semaphore waiter = new Semaphore(N-1);

        Thread[] ts = new Thread[N];

        for (int i=0;i<N;i++) {
            ts[i] = new Thread(new Philosopher(i, forks[i], forks[(i+1)%N], waiter));
            ts[i].start();
        }

        for (Thread t: ts) t.join();
        System.out.println("Done.");
    }
}
