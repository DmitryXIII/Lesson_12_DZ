import java.util.Arrays;
import java.util.concurrent.CountDownLatch;

public class Lesson_12_DZ {
    static final int SIZE = 10000000;
    static final int HALF = SIZE / 2;
    static Object lock = new Object();

    public static void main(String[] args) {
        method1(); // вычисления в один поток
        method2(); // вычисления в 2 потока (через lock и wait() / notify ())
        method3(); // вычисления в 2 потока (через CountDownLatch)
    }

    public static void method1() {
        System.out.println("method1 запустился");
        float[] arrFromMethod1 = new float[SIZE];
        Arrays.fill(arrFromMethod1, 1);

        long a = System.currentTimeMillis();
        arrayCalculation1(arrFromMethod1);
        System.out.println("Контроль [0]: " + arrFromMethod1[0]);
        System.out.println("Контроль [4999999]: " + arrFromMethod1[4999999]);
        System.out.println("Контроль [5000000]: " + arrFromMethod1[5000000]);
        System.out.println("Контроль [9999999]: " + arrFromMethod1[9999999]);
        System.out.println("method1 выполнялся " + (System.currentTimeMillis() - a) + " миллисекунд");
        System.out.println("===============================================");
        System.out.println();
    }

    public static void method2() {
        System.out.println("method2 запустился");
        float[] arrayFromMethod2 = new float[SIZE];
        Arrays.fill(arrayFromMethod2, 1);

        long a = System.currentTimeMillis();

        float[] halfArray2 = new float[HALF];

        MyThread threadForHelp = new MyThread(arrayFromMethod2);
        threadForHelp.start();

        System.arraycopy(arrayFromMethod2, HALF, halfArray2, 0, HALF);
        arrayCalculation2(halfArray2);
        synchronized (lock) {
            while (!threadForHelp.getIsDone()) {
                try {
                    lock.wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }

        System.arraycopy(threadForHelp.getHalfArray1(), 0, arrayFromMethod2, 0, HALF);
        System.arraycopy(halfArray2, 0, arrayFromMethod2, HALF, HALF);
        System.out.println("Контроль [0]: " + arrayFromMethod2[0]);
        System.out.println("Контроль [4999999]: " + arrayFromMethod2[4999999]);
        System.out.println("Контроль [5000000]: " + arrayFromMethod2[5000000]);
        System.out.println("Контроль [9999999]: " + arrayFromMethod2[9999999]);
        System.out.println("method2 выполнялся " + (System.currentTimeMillis() - a) + " миллисекунд");
        System.out.println("===============================================");
        System.out.println();
    }

    public static void method3() {
        System.out.println("method3 запустился");
        float[] arrayFromMethod3 = new float[SIZE];
        Arrays.fill(arrayFromMethod3, 1);
        long a = System.currentTimeMillis();
        CountDownLatch cdl = new CountDownLatch(2);

        new Thread(() -> {
            System.out.println("Поток 0 запущен");
            float[] halfArray1 = new float[HALF];
            System.arraycopy(arrayFromMethod3, 0, halfArray1, 0, HALF);
            arrayCalculation1(halfArray1);
            System.arraycopy(halfArray1, 0, arrayFromMethod3, 0, HALF);
            System.out.println("Поток 0 закончил работу");
            cdl.countDown();
        }).start();

        new Thread(() -> {
            System.out.println("Поток 1 запущен");
            float[] halfArray2 = new float[HALF];
            System.arraycopy(arrayFromMethod3, HALF, halfArray2, 0, HALF);
            arrayCalculation2(halfArray2);
            System.arraycopy(halfArray2, 0, arrayFromMethod3, HALF, HALF);
            System.out.println("Поток 1 закончил работу");
            cdl.countDown();
        }).start();

        try {
            cdl.await();
        } catch (
                InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println("Контроль [0]: " + arrayFromMethod3[0]);
        System.out.println("Контроль [4999999]: " + arrayFromMethod3[4999999]);
        System.out.println("Контроль [5000000]: " + arrayFromMethod3[5000000]);
        System.out.println("Контроль [9999999]: " + arrayFromMethod3[9999999]);
        System.out.println("method3 выполнялся " + (System.currentTimeMillis() - a) + " миллисекунд");
    }

    public static void arrayCalculation1(float[] array) {
        for (int i = 0; i < array.length; i++) {
            array[i] = (float) (array[i] * Math.sin(0.2f + i / 5) * Math.cos(0.2f + i / 5) * Math.cos(0.4f + i / 2));
        }
    }

    public static void arrayCalculation2(float[] array) {
        for (int j = 0; j < array.length; j++) {
            array[j] = (float) (array[j] * Math.sin(0.2f + (j + HALF) / 5) * Math.cos(0.2f + (j + HALF) / 5) * Math.cos(0.4f + (j + HALF) / 2));
        }
    }

    static class MyThread extends Thread {
        private float[] halfArray1;
        private boolean isDone;

        public MyThread(float[] arrayFromMethod2) {
            this.halfArray1 = new float[HALF];
            System.arraycopy(arrayFromMethod2, 0, halfArray1, 0, HALF);
            this.isDone = false;
        }

        public boolean getIsDone() {
            return isDone;
        }

        public float[] getHalfArray1() {
            return halfArray1;
        }

        @Override
        public void run() {
            System.out.println("Поток 0 запущен");
            synchronized (lock) {
                arrayCalculation1(this.halfArray1);
                this.isDone = true;
                System.out.println("Поток 0 закончил работу");
                lock.notify();
            }
        }
    }
}