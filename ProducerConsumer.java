import java.util.ArrayDeque;
import java.util.Queue;

public class ProducerConsumer {
    static class BoundedBuffer<T> {
        private final Queue<T> q = new ArrayDeque<>();
        private final int capacity;
        BoundedBuffer(int capacity){ this.capacity = capacity; }

        public synchronized void put(T x) throws InterruptedException {
            // for producer
            while (q.size() == capacity) wait(); // wait for space
            q.add(x); // otherwise there may be overflow
            notifyAll(); // wake others who are waiting
            /*
            if the buffer was empty, there will be some consumers waiting, if we don't
            call the notifyAll() here, they will stuck in the while loop forever
            */
        }

        public synchronized T take() throws InterruptedException {
            // for consumer
            while (q.isEmpty()) wait(); // wait for item
            T x = q.remove();
            notifyAll(); // wake others who are waiting to operate on the buffer
            return x;
        }
    }

    // ---- Producer ----
    static class Producer implements Runnable {
        private final BoundedBuffer<Integer> buf;
        Producer(BoundedBuffer<Integer> buf) { this.buf = buf; }
        @Override public void run() {
            try {
                for (int i = 1; i <= 20; i++) {
                    buf.put(i);
                    System.out.println("P -> " + i);
                    Thread.sleep(10);
                }
            } catch (InterruptedException ignored) {}
        }
    }

    // ---- Consumer ----
    static class Consumer implements Runnable {
        private final BoundedBuffer<Integer> buf;
        Consumer(BoundedBuffer<Integer> buf) { this.buf = buf; }
        @Override public void run() {
            try {
                for (int i = 1; i <= 20; i++) {
                    int v = buf.take();
                    System.out.println("  C <- " + v);
                    Thread.sleep(15);
                }
            } catch (InterruptedException ignored) {}
        }
    }



    public static void main(String[] args) throws Exception {
        BoundedBuffer<Integer> buf = new BoundedBuffer<>(5);

        Thread p = new Thread(new Producer(buf));
        Thread c = new Thread(new Consumer(buf));

        p.start(); c.start();
        p.join(); c.join();

        System.out.println("Done.");
    }
}