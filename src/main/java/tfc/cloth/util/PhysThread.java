package tfc.cloth.util;

import java.util.ArrayList;

public class PhysThread implements Runnable {
    ArrayList<Runnable> actions = new ArrayList<>();
    boolean active = false;
    Thread td = new Thread(this);

    boolean canRun = false;

    public void start() {
        td.start();
    }

    public void run() {
        while (true) {
            if (canRun) {
                Runnable[] toRun = null;
                synchronized (actions) {
                    if (actions.size() != 0) {
                        toRun = actions.toArray(new Runnable[0]);
                        actions.clear();
                        active = true;
                    }
                }
                if (toRun != null) {
                    for (Runnable runnable : toRun) {
                        runnable.run();
                    }
                }
                active = false;

                try {
                    Thread.sleep(1);
                }catch ( Throwable ignored) {
                }
            }
        }
    }

    public void schedule(Runnable r) {
        synchronized (actions) {
            actions.add(r);
        }
    }

    public void await() {
//        canRun = true;
        try {
            while (!actions.isEmpty()) {
                Thread.sleep(1);
            }
        } catch (Throwable ignored) {
        }
        try {
            while (active) {
                Thread.sleep(1);
            }
        } catch (Throwable ignored) {
        }
//        canRun = false;
    }

    public void pause() {
        canRun = false;
    }

    public void startWork() {
        canRun = true;
    }
}
