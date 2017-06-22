package virtualRobot.godThreads;

import java.util.concurrent.atomic.AtomicBoolean;

import virtualRobot.GodThread;
import virtualRobot.LogicThread;
import virtualRobot.MonitorThread;
import virtualRobot.logicThreads.NoSensorAutonomouses.moveAndFireBalls;
import virtualRobot.monitorThreads.TimeMonitor;

/**
 * Created by 17osullivand on 11/27/16.
 * Fires the Balls and that's about it
 */

public class FireBallsGodThread extends GodThread {

    @Override
    public void realRun() throws InterruptedException {

        MonitorThread watchingForTime = new TimeMonitor(10000);
        Thread tm = new Thread(watchingForTime);
        tm.start();
        children.add(tm);
        LogicThread fireBalls = new moveAndFireBalls(new AtomicBoolean(true));
        Thread fb = new Thread(fireBalls);
        fb.start();
        children.add(fb);
        delegateMonitor(fb, new MonitorThread[]{watchingForTime});
    }
}