import java.util.concurrent.locks.*;

public class ReadersWriters {

    static class Database {
        // ReentrantReadWriteLock allows multiple read threads to read at the same time,
        // but write threads must be exclusive (during which other reads and writes are not allowed).
        private final ReadWriteLock rw = new ReentrantReadWriteLock(true); // fair
        private int value = 0;

        int read(int id) {
            rw.readLock().lock();

            try {
                System.out.println("Reader " + id + " ENTER, sees " + value);
                Thread.sleep(100);
                System.out.println("Reader " + id + " EXIT");

                return value;
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            } finally {
                rw.readLock().unlock();
            }
        }

        void write(int v) {
            rw.writeLock().lock();

            try {
                System.out.println("Writer ENTER, set " + v);
                Thread.sleep(200);
                value = v;
                System.out.println("Writer EXIT, set " + v);
            } catch (InterruptedException ignored) {
            } finally {
                rw.writeLock().unlock();
            }
        }
    }

    static class Reader implements Runnable {
        private final Database db;
        private final int id;

        Reader(Database db, int id) {
            this.db = db;
            this.id = id;
        }

        @Override
        public void run() {
            for (int i = 0; i < 3; i++) {
                int v = db.read(id);
                try { Thread.sleep(10); } catch (InterruptedException ignored) {}
            }
        }
    }

    static class Writer implements Runnable {
        private final Database db;

        Writer(Database db) {
            this.db = db;
        }

        @Override
        public void run() {
            for (int i = 1; i <= 3; i++) {
                db.write(i);
                try { Thread.sleep(20); } catch (InterruptedException ignored) {}
            }
        }
    }

    public static void main(String[] args) throws Exception {
        Database db = new Database();

        Thread r1 = new Thread(new Reader(db, 1));
        Thread r2 = new Thread(new Reader(db, 2));
        Thread w  = new Thread(new Writer(db));

        r1.start(); r2.start(); w.start();
        r1.join(); r2.join(); w.join();

        System.out.println("Done.");
    }
}
