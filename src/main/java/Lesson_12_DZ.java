import java.util.Arrays;

public class Lesson_12_DZ {
    static final int SIZE = 10000000;
    static final int HALF = SIZE / 2;
    static float[] arr3;
    static float[] arr4;
    static boolean isMyThreadEnd = false;
    static Object lock = new Object();

    public static void main(String[] args) {
        method1();
        method2();
    }

    public static void method1() {
        float[] arr = new float[SIZE];
        Arrays.fill(arr, 1);

        long a = System.currentTimeMillis();
        for (int i = 0; i < arr.length; i++) {
            arr[i] = (float) (arr[i] * Math.sin(0.2f + i / 5) * Math.cos(0.2f + i / 5) * Math.cos(0.4f + i / 2));
        }
        System.out.println("method1 выполнялся " + (System.currentTimeMillis() - a) + " миллисекунд");
    }

    public static void method2() {
        float[] arr = new float[SIZE];
        Arrays.fill(arr, 1);

        long a = System.currentTimeMillis();

        synchronized (lock) {
            arr3 = new float[HALF];
            arr4 = new float[HALF];
            System.arraycopy(arr, 0, arr3, 0, HALF);
            System.arraycopy(arr, HALF, arr4, 0, HALF);

            new Thread(new MyThread()).start();

            for (int i = 0; i < arr4.length; i++) {
                arr4[i] = (float) (arr4[i] * Math.sin(0.2f + i / 5) * Math.cos(0.2f + i / 5) * Math.cos(0.4f + i / 2));
            }
            long c = System.currentTimeMillis();
            while (!isMyThreadEnd) {
                try {
                    lock.wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
        System.arraycopy(arr3, 0, arr, 0, HALF);
        System.arraycopy(arr4, 0, arr, HALF, HALF);
        System.out.println("method2 выполнялся " + (System.currentTimeMillis() - a) + " миллисекунд");
    }

    static class MyThread extends Thread {
        @Override
        public void run() {
            synchronized (lock) {
                for (int i = 0; i < arr3.length; i++) {
                    arr3[i] = (float) (arr3[i] * Math.sin(0.2f + i / 5) * Math.cos(0.2f + i / 5) * Math.cos(0.4f + i / 2));
                }
                isMyThreadEnd = true;
                lock.notify();
            }
        }
    }
}
