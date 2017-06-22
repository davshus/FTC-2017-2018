package virtualRobot.commands;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.util.Log;

import java.util.concurrent.atomic.AtomicBoolean;

import virtualRobot.AutonomousRobot;
import virtualRobot.Condition;
import virtualRobot.PIDController;
import virtualRobot.SallyJoeBot;
import virtualRobot.VuforiaLocalizerImplSubclass;
import virtualRobot.utils.Vector2i;

/**
 * Created by ethachu19 on 12/7/2016.
 *
 * Moves to Beacon with Heading Correction and Alligns with Beacon using PID while getting redIsLeft
 * AKA the Great Deprecation of 2017
 *
 * NOTE FOR TEAM OF 2016 - 2017: CAMERA IS FLIPPED AND 180 DEGREE ROTATE IS TOO COSTLY
 */

public class AllignWithBeacon implements Command {

    public enum Mode {
        MIDSPLIT, TWORECTANGLES
    }
    private final static boolean flipUpsideDown = true;
    public final static Mode currentMode = Mode.MIDSPLIT;
    public volatile static double startXPercent = 0;
    public volatile static double endXPercent = 1;
    public volatile static double startYPercent = 0.135;
    public volatile static double endYPercent = 1;

    public volatile static double start1XPercent = 0;
    public volatile static double end1XPercent = .2;

    public volatile static double start1YPercent = 0;
    public volatile static double end1YPercent = 1;

    public volatile static double start2XPercent = 0.4;
    public volatile static double end2XPercent = 1;

    public volatile static double start2YPercent = 0;
    public volatile static double end2YPercent = 1;
    
    AtomicBoolean redIsLeft;
    SallyJoeBot robot = Command.ROBOT;
    VuforiaLocalizerImplSubclass vuforia;
    Condition condition = new Condition() {
        @Override
        public boolean isConditionMet() {
            return false;
        }
    };
    private int whiteTape = 13;
    public final static double BLUETHRESHOLD = 0.7; //.65
    public final static double REDTHRESHOLD = 1.43;
    public final static double LINETHRESHOLD = 0.62;
    public double timeLimit = -1;
    private static double tp = -0.15;
    private PIDController heading = new PIDController(0,0,0,0,0);
    private PIDController compensate = new PIDController(1.5,.05357,10.5,0,(BLUETHRESHOLD + REDTHRESHOLD)/2);
    private Direction direction;
    private double maxDistance = Double.MAX_VALUE;
    AtomicBoolean maxDistanceReached;
    public AllignWithBeacon(VuforiaLocalizerImplSubclass vuforia, AtomicBoolean redIsLeft, Direction dir) {
        this.vuforia = vuforia;
        this.redIsLeft = redIsLeft;
        this.direction = dir;
    }


    public AllignWithBeacon(VuforiaLocalizerImplSubclass vuforia, AtomicBoolean redIsLeft, Direction dir, double timeLimit) {
        this(vuforia, redIsLeft, dir);
        this.timeLimit = timeLimit;
    }
    public AllignWithBeacon(VuforiaLocalizerImplSubclass vuforia, AtomicBoolean redIsLeft, Direction dir, double timeLimit, double maxDistance, AtomicBoolean maxDistanceReached) {
        this(vuforia, redIsLeft, dir, timeLimit);
        this.maxDistance = maxDistance;
        this.maxDistanceReached = maxDistanceReached;
    }
    public AllignWithBeacon(VuforiaLocalizerImplSubclass vuforia, AtomicBoolean redIsLeft, Direction dir, double timeLimit, double maxDistance, AtomicBoolean maxDistanceReached, double referenceAnlge) {
        this(vuforia, redIsLeft, dir, timeLimit);
        this.maxDistance = maxDistance;
        this.maxDistanceReached = maxDistanceReached;
        heading.setTarget(referenceAnlge);
    }

    public void setCondition(Condition condition) {
        this.condition = condition;
    }

    private int closestToFrac(double num, double frac) {
        int res = -1;
        double leastDist = Double.MAX_VALUE;
        for (int i = 0; true; i++) {
            if (Math.abs(i/frac - num) < leastDist) {
                res = i;
                leastDist = Math.abs(i/frac - num);
            } else {
                break;
            }
        }
        return res;
    }

    private boolean isOnLine() {
        return robot.getLightSensor1().getValue() > LINETHRESHOLD ||
                robot.getLightSensor2().getValue() > LINETHRESHOLD ||
                robot.getLightSensor3().getValue() > LINETHRESHOLD ||
                robot.getLightSensor4().getValue() > LINETHRESHOLD ||
                ((robot.getColorSensor().getRed() >= whiteTape && robot.getColorSensor().getBlue() >= whiteTape && robot.getColorSensor().getGreen() >= whiteTape && robot.getColorSensor().getBlue() < 255));
    }

    @Override
    public boolean changeRobotState() throws InterruptedException {
        robot.getLFEncoder().clearValue();
        robot.getRFEncoder().clearValue();
        robot.getLBEncoder().clearValue();
        robot.getRBEncoder().clearValue();
        int width = vuforia.rgb.getWidth(), height = vuforia.rgb.getHeight();
        Vector2i start1;
        Vector2i end1;
        Vector2i start2;
        Vector2i end2;
        Bitmap bm = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
        int coF = 12;
        Log.d("width/height", width + " " + height);

        if (currentMode == Mode.MIDSPLIT) {
            if (flipUpsideDown) {
                start2 = new Vector2i((int) (startXPercent * width), (int) (startYPercent * height));
                end1 = new Vector2i((int) (endXPercent * width), (int) (endYPercent * height));
                end2 = new Vector2i((start2.x + end1.x) / 2, end1.y);
                start1 = new Vector2i(end2.x, start2.y);

            } else {
                start1 = new Vector2i((int) (startXPercent * width), (int) (startYPercent * height));
                end2 = new Vector2i((int) (endXPercent * width), (int) (endYPercent * height));
                end1 = new Vector2i((start1.x + end2.x) / 2, end2.y);
                start2 = new Vector2i(end1.x, start1.y);
            }
        } else {
            if (flipUpsideDown) {
                start2 = new Vector2i((int) (start1XPercent * width), (int) (start1YPercent * height));
                end1 = new Vector2i((int) (end2XPercent * width), (int) (end2YPercent * height));
                start1 = new Vector2i((int) (start2XPercent * width), (int) (start2YPercent * height));
                end2 = new Vector2i((int) (end1XPercent * width), (int) (end1YPercent * height));
            } else {
                start1 = new Vector2i((int) (start1XPercent * width), (int) (start1YPercent * height));
                end2 = new Vector2i((int) (end2XPercent * width), (int) (end2YPercent * height));
                start2 = new Vector2i((int) (start2XPercent * width), (int) (start2YPercent * height));
                end1 = new Vector2i((int) (end1XPercent * width), (int) (end1YPercent * height));
            }
        }
        Vector2i slope1, slope2;
        if (vuforia.rgb.getHeight() > vuforia.rgb.getWidth()) {
            slope1 = new Vector2i(coF, closestToFrac(((double) (end1.y - start1.y)) / (end1.x - start1.x), coF));
            slope2 = new Vector2i(coF, closestToFrac(((double) (end2.y - start2.y)) / (end2.x - start2.x), coF));
        } else {
            slope1 = new Vector2i(coF, closestToFrac(((double) (end1.y - start1.y)) / (end1.x - start1.x), coF));
            slope2 = new Vector2i(coF, closestToFrac(((double) (end2.y - start2.y)) / (end2.x - start2.x), coF));
        }
        double currLeft = 0, currRight = 0, adjustedPower = 0, red = 0, blue = 0;
        int leftCovered = 0, rightCovered = 0;
        Vector2i currentPos;
        boolean isInterrupted = false, satisfied = false;

        while (!condition.isConditionMet() && !isInterrupted && (!satisfied || !isOnLine())) {
            currLeft = 0;
            currRight = 0;
            bm.copyPixelsFromBuffer(vuforia.rgb.getPixels());
            currentPos = new Vector2i(start1);
            for (leftCovered = 0; currentPos.x < end1.x && currentPos.y < end1.y; ) {
                red = Color.red(bm.getPixel(currentPos.x, currentPos.y));
                blue = Color.blue(bm.getPixel(currentPos.x, currentPos.y));
                if (blue != 0) {
                    Log.d("Debug", red + " " + blue + " " + currentPos.toString());

                    currLeft += red / blue;
                    leftCovered++;
                }
                currentPos.x += slope1.x;
                currentPos.y += slope1.y;
            }
            if (leftCovered == 0)
                continue;
            currLeft /= leftCovered;
            robot.addToTelemetry("currLEFT: ", currLeft);
            currentPos = new Vector2i(start2);
            for (rightCovered = 0; currentPos.x < end2.x && currentPos.y < end2.y; ) {
                red = Color.red(bm.getPixel(currentPos.x, currentPos.y));
                blue = Color.blue(bm.getPixel(currentPos.x, currentPos.y));
                if (blue != 0) {
                    Log.d("Debug", red + " " + blue + " " + currentPos.toString());
                    currRight += red / blue;
                    rightCovered++;
                }
                currentPos.x += slope2.x;
                currentPos.y += slope2.y;
            }
            if (rightCovered == 0)
                continue;
            currRight /= rightCovered;
            robot.addToTelemetry("currRIGHT: ", currRight);
                //  Log.d("currRIGHT", String.valueOf(currRight));
            if (currRight > REDTHRESHOLD || currRight < BLUETHRESHOLD) {
                satisfied = true;
            } else if (currLeft < BLUETHRESHOLD || currLeft > REDTHRESHOLD) {
                satisfied = true;
            }
            if (satisfied && isOnLine()) {
                robot.stopMotors();
                break;
            }
            adjustedPower = heading.getPIDOutput(robot.getHeadingSensor().getValue());
            robot.getLFMotor().setPower((tp + adjustedPower) * direction.getMultiplier());
            robot.getLBMotor().setPower((tp + adjustedPower) * direction.getMultiplier());
            robot.getRFMotor().setPower((tp - adjustedPower) * direction.getMultiplier());
            robot.getRBMotor().setPower((tp - adjustedPower) * direction.getMultiplier());
            robot.addToTelemetry("CurrentDistance", getAvgDistance());
            if (getAvgDistance() > maxDistance) {
                maxDistanceReached.set(true);
                condition = new Condition() {
                    @Override
                    public boolean isConditionMet() { //will then automatically break out of rest of parts of allign to beacon
                        return true;
                    }
                };
            }
            if (Thread.currentThread().isInterrupted()) {
                isInterrupted = true;
                break;
            }
            try {
                Thread.sleep(10);
            } catch (InterruptedException ex) {
                isInterrupted = true;
                break;
            }
        }
        robot.stopMotors();
        Thread.sleep(500);
        if (!condition.isConditionMet()) {
            robot.addToProgress("Switched To Correction"); //acounts for times where it goes to far and alligns with one side of beacon
            if (currLeft > currRight) {
                redIsLeft.set(true);
            } else  {
                redIsLeft.set(false);
            }

//            } else if (currLeft < BLUETHRESHOLD) {
//                redIsLeft.set(true);
//            } else if (currLeft > REDTHRESHOLD) {
//                redIsLeft.set(false);
//            } else if (currRight < BLUETHRESHOLD) {
//                redIsLeft.set(false);
//            } else if (currRight > REDTHRESHOLD) {
//                redIsLeft.set(true);
//            }
        }
        robot.addToProgress("Switched To Precision");
        robot.stopMotors();
        Thread.sleep(1000);
        double power, curr = 0;
        int covered;
        long start = System.currentTimeMillis();
        while (!condition.isConditionMet() && !isInterrupted && (System.currentTimeMillis() - start < timeLimit)) {
            curr = 0;
            bm.copyPixelsFromBuffer(vuforia.rgb.getPixels());
            currentPos = new Vector2i(start1.x, vuforia.rgb.getHeight()/2);
            for (covered = 0; currentPos.x < end2.x;) {
                red = Color.red(bm.getPixel(currentPos.x, currentPos.y));
                blue = Color.blue(bm.getPixel(currentPos.x, currentPos.y));
                if (blue != 0 && (blue > 200 || red > 200) && (red/blue < AllignWithBeacon.BLUETHRESHOLD || red/blue > AllignWithBeacon.REDTHRESHOLD)) {
                    curr += red / blue;
                    covered++;
                }
                currentPos.x += 8;
            }
            if (covered == 0)
                continue;
            curr /= covered;
            power = (redIsLeft.get() ? 1 : -1) * compensate.getPIDOutput(curr);
            //adjustedPower = heading.getPIDOutput(robot.getHeadingSensor().getValue());
            //adjustedPower *= tp;
            Log.d("AllignWithBeacon","" + power + " " + adjustedPower + " " + curr + " " + covered);
            robot.addToTelemetry("AllignWithBeacon ", curr + " " + covered + " " + power);
            robot.getLFMotor().setPower(power + adjustedPower);
            robot.getLBMotor().setPower(power + adjustedPower);
            robot.getRFMotor().setPower(power - adjustedPower);
            robot.getRBMotor().setPower(power - adjustedPower);
            if (Thread.currentThread().isInterrupted()) {
                isInterrupted = true;
                break;
            }
            try {
                Thread.sleep(10);
            } catch (InterruptedException ex) {
                isInterrupted = true;
                break;
            }
        }
        robot.stopMotors();
        return isInterrupted;
    }
    private double getAvgDistance() {
        double LFvalue = robot.getLFEncoder().getValue();
        double RFvalue = robot.getRFEncoder().getValue();
        double LBvalue = robot.getLBEncoder().getValue();
        double RBvalue = robot.getRBEncoder().getValue();
        Log.d("AVGDIST", " " + Math.abs((Math.abs(LFvalue) + Math.abs(RFvalue) + Math.abs(LBvalue) + Math.abs(RBvalue))/4));
        return (Math.abs(LFvalue) + Math.abs(RFvalue) + Math.abs(LBvalue) + Math.abs(RBvalue))/4;
    }
    public enum Direction {
        FORWARD(-1),
        BACKWARD(1);

        private int dir;
        private Direction(int x) {
            this.dir = x;
        }

        private int getMultiplier() {
            return dir;
        }
    }
}