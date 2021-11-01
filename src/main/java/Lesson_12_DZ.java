import java.util.Arrays;

public class Lesson_12_DZ {
    static final int SIZE = 10000000;
    static final int HALF = SIZE / 2;
    static Object lock = new Object();

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

        float[] halfArray2 = new float[HALF];

        MyThread threadForHelp = new MyThread(arrayFromMethod2);
        threadForHelp.start();

        System.arraycopy(arrayFromMethod2, HALF, halfArray2, 0, HALF);
        for (int i = 0; i < halfArray2.length; i++) {
            halfArray2[i] = (float) (halfArray2[i] * Math.sin(0.2f + (i + HALF) / 5) * Math.cos(0.2f + (i + HALF) / 5) * Math.cos(0.4f + (i + HALF) / 2));
        }
        synchronized (lock) {
            while (!threadForHelp.isItsDone()) {
                try {
                    lock.wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
        System.arraycopy(threadForHelp.getHalfArray1(), 0, arrayFromMethod2, 0, HALF);
        System.arraycopy(halfArray2, 0, arrayFromMethod2, HALF, HALF);
        System.out.println("method2 выполнялся " + (System.currentTimeMillis() - a) + " миллисекунд");
    }


    static class MyThread extends Thread {
        private float[] halfArray1;
        private boolean itsDone;

        public MyThread(float[] arrayFromMethod2) {
            this.halfArray1 = new float[Lesson_12_DZ.HALF];
            System.arraycopy(arrayFromMethod2, 0, halfArray1, 0, Lesson_12_DZ.HALF);
            this.itsDone = false;
        }

        public boolean isItsDone() {
            return itsDone;
        }

        public float[] getHalfArray1() {
            return halfArray1;
        }

        @Override
        public void run() {
            synchronized (lock) {
                for (int i = 0; i < this.halfArray1.length; i++) {
                    halfArray1[i] = (float) (halfArray1[i] * Math.sin(0.2f + i / 5) * Math.cos(0.2f + i / 5) * Math.cos(0.4f + i / 2));
                }
                this.itsDone = true;
                lock.notify();
            }
        }
    }
}