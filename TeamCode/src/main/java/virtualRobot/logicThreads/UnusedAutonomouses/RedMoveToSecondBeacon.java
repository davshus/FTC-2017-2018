package virtualRobot.logicThreads.UnusedAutonomouses;

import java.util.concurrent.atomic.AtomicBoolean;

import virtualRobot.AutonomousRobot;
import virtualRobot.Condition;
import virtualRobot.LogicThread;
import virtualRobot.VuforiaLocalizerImplSubclass;
import virtualRobot.commands.Pause;
import virtualRobot.commands.WallTrace;

/**
 * Created by Warren on 10/6/2016.
 * Takes pic of second beacon
 */
@Deprecated
public class RedMoveToSecondBeacon extends LogicThread<AutonomousRobot> {

    AtomicBoolean redIsLeft;
    VuforiaLocalizerImplSubclass vuforia;

    final double currentLine = RedAutonomousLogic.Line; //get the value of what the color sensor was at the start of autonomous (what the value of grey is)
    final Condition atwhiteline = new Condition() {
        @Override
        public boolean isConditionMet() {
            if (Math.abs(robot.getLightSensor1().getRawValue() - currentLine) > .7) {
                return true;
            }
            return false;
        }
    };

    public RedMoveToSecondBeacon(AtomicBoolean redIsLeft, VuforiaLocalizerImplSubclass vuforia){
        super();
        this.redIsLeft = redIsLeft;
        this.vuforia = vuforia;
    }
    @Override
    public void loadCommands() {
        WallTrace toWhiteLine =  new WallTrace(WallTrace.Direction.FORWARD, 8); //Move to the other beacon
        toWhiteLine.setCondition(atwhiteline);
        commands.add(toWhiteLine);
        commands.add(new Pause(500));
        WallTrace toWhiteLine2 =  new WallTrace(WallTrace.Direction.FORWARD, 8); //Accounts for the slight overshoot
        toWhiteLine2.setCondition(atwhiteline);
        commands.add(toWhiteLine2);
        robot.addToProgress("Went to Line");
        commands.add(new Pause(500));
        //FTCTakePicture pic = new FTCTakePicture(this.redIsLeft,this.vuforia); //Take another picture
        //commands.add(pic);
    }
}