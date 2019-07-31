package grondag.fluidity.test;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

import org.junit.jupiter.api.Test;

class LockTest {

    final Locker locker = new Locker();
    final ExecutorService exec = Executors.newFixedThreadPool(8);

    @Test
    void test() {
        exec.execute(this::doRun);
        exec.execute(this::doRun);
        exec.execute(this::doRun);
        exec.execute(this::doRun);
        exec.execute(this::doRun);
        exec.execute(this::doRun);
        exec.execute(this::doRun);
        exec.execute(this::doRun);
        doRun();
        doRun();
        doRun();
        doRun();
        try {
            exec.awaitTermination(10, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    void doRun() {
        final String myThread = Thread.currentThread().toString() + " - ";
        for(int i = 0; i < 100; i++) {
            locker.open();
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
               // e.printStackTrace();
            }
            System.out.println(myThread + i);
            locker.close();
        }
    }
    
    private static class Locker {
        final Thread mainThread = Thread.currentThread();
        
        static final ReentrantLock innerLock = new ReentrantLock();
        static final ReentrantLock outerLock = new ReentrantLock();
        
        void open() {
            
        }
        
        void close() {
            final Thread myThread = Thread.currentThread();
            innerLock.unlock();
            if(myThread != mainThread) {
                outerLock.unlock();
                Thread.yield();
            }
        }
    }
}
