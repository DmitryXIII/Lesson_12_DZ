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
        for (int i = 0; i < arrFromMethod1.length; i++) {
            arrFromMethod1[i] = (float) (arrFromMethod1[i] * Math.sin(0.2f + i / 5) * Math.cos(0.2f + i / 5) * Math.cos(0.4f + i / 2));
        }
        controlElementsPrint(arrFromMethod1);
        System.out.println("method1 выполнялся " + (System.currentTimeMillis() - a) + " миллисекунд");
        System.out.println("===============================================");
        System.out.println();
    }

    public static void method2() {
        System.out.println("method2 запустился");
        float[] arrayFromMethod2 = new float[SIZE];
        Arrays.fill(arrayFromMethod2, 1);
        long a = System.currentTimeMillis();
        MyThread threadForHelp = new MyThread(arrayFromMethod2);
        threadForHelp.start();
        arrayCalculation2(arrayFromMethod2);
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
        controlElementsPrint(arrayFromMethod2);
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
            arrayCalculation1(arrayFromMethod3);
            System.out.println("Поток 0 закончил работу");
            cdl.countDown();
        }).start();

        new Thread(() -> {
            System.out.println("Поток 1 запущен");
            arrayCalculation2(arrayFromMethod3);
            System.out.println("Поток 1 закончил работу");
            cdl.countDown();
        }).start();

        try {
            cdl.await();
        } catch (
                InterruptedException e) {
            e.printStackTrace();
        }
        controlElementsPrint(arrayFromMethod3);
        System.out.println("method3 выполнялся " + (System.currentTimeMillis() - a) + " миллисекунд");
    }

    // работа с первой половиной массива
    public static void arrayCalculation1(float[] mainArray) {
        float[] halfArray = new float[HALF];
        System.arraycopy(mainArray, 0, halfArray, 0, HALF);
        for (int i = 0; i < halfArray.length; i++) {
            halfArray[i] = (float) (halfArray[i] * Math.sin(0.2f + i / 5) * Math.cos(0.2f + i / 5) * Math.cos(0.4f + i / 2));
        }
        System.arraycopy(halfArray, 0, mainArray, 0, HALF);
    }

    // работа со второй половиной массива
    public static void arrayCalculation2(float[] mainArray) {
        float[] halfArray2 = new float[HALF];
        System.arraycopy(mainArray, HALF, halfArray2, 0, HALF);
        for (int j = 0; j < halfArray2.length; j++) {
            halfArray2[j] = (float) (halfArray2[j] * Math.sin(0.2f + (j + HALF) / 5) * Math.cos(0.2f + (j + HALF) / 5) * Math.cos(0.4f + (j + HALF) / 2));
        }
        System.arraycopy(halfArray2, 0, mainArray, HALF, HALF);
    }
    //вывод в консоль контрольных элементов массива для сравнения правильности рассчетов во всех трех методах
    public static void controlElementsPrint(float[] array) {
        System.out.println("Контроль [0]: " + array[0]);
        System.out.println("Контроль [4999999]: " + array[4999999]);
        System.out.println("Контроль [5000000]: " + array[5000000]);
        System.out.println("Контроль [9999999]: " + array[9999999]);
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
                for (int i = 0; i < this.halfArray1.length; i++) {
                    this.halfArray1[i] = (float) (this.halfArray1[i] * Math.sin(0.2f + i / 5) * Math.cos(0.2f + i / 5) * Math.cos(0.4f + i / 2));
                }
                this.isDone = true;
                System.out.println("Поток 0 закончил работу");
                lock.notify();
            }
        }
    }
}