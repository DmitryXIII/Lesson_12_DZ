import java.util.Arrays;

public class Lesson_12_DZ {
    static final int SIZE = 10000000;
    static final int HALF = SIZE / 2;
    static float[] halfArray1;
    static float[] halfArray2;
    static Object lock = new Object();
    static int COUNTER;

    public static void main(String[] args) {
        method1();
        method2();
    }

    public static void method1() {
        float[] arrFromMethod1 = new float[SIZE];
        Arrays.fill(arrFromMethod1, 1);

        long a = System.currentTimeMillis();
        for (int i = 0; i < arrFromMethod1.length; i++) {
            arrFromMethod1[i] = (float) (arrFromMethod1[i] * Math.sin(0.2f + i / 5) * Math.cos(0.2f + i / 5) * Math.cos(0.4f + i / 2));
        }
        System.out.println("method1 выполнялся " + (System.currentTimeMillis() - a) + " миллисекунд");
    }

    public static void method2() {
        float[] arrayFromMethod2 = new float[SIZE];

        Arrays.fill(arrayFromMethod2, 1);

        long a = System.currentTimeMillis();

        synchronized (lock) {
            halfArray1 = new float[HALF];
            halfArray2 = new float[HALF];
            System.arraycopy(arrayFromMethod2, 0, halfArray1, 0, HALF);
            System.arraycopy(arrayFromMethod2, HALF, halfArray2, 0, HALF);

            new Thread(new MyThread()).start();

            for (int i = 0; i < halfArray2.length; i++) {
                halfArray2[i] = (float) (halfArray2[i] * Math.sin(0.2f + i / 5) * Math.cos(0.2f + i / 5) * Math.cos(0.4f + i / 2));
            }

            while (COUNTER < halfArray1.length) {
                try {
                    lock.wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
        System.arraycopy(halfArray1, 0, arrayFromMethod2, 0, HALF);
        System.arraycopy(halfArray2, 0, arrayFromMethod2, HALF, HALF);
        System.out.println("method2 выполнялся " + (System.currentTimeMillis() - a) + " миллисекунд");
    }

    static class MyThread extends Thread {
        @Override
        public void run() {
            synchronized (lock) {
                for (COUNTER = 0; COUNTER < halfArray1.length; COUNTER++) {
                    halfArray1[COUNTER] = (float) (halfArray1[COUNTER] * Math.sin(0.2f + COUNTER / 5) * Math.cos(0.2f + COUNTER / 5) * Math.cos(0.4f + COUNTER / 2));
                }
                lock.notify();
            }
        }
    }
}
