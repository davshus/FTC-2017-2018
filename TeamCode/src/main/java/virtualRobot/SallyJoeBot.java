package virtualRobot;

import com.qualcomm.robotcore.hardware.CRServo;

import org.opencv.core.Mat;

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Retrofit;
import virtualRobot.hardware.ColorSensor;
import virtualRobot.hardware.ContinuousRotationServo;
import virtualRobot.hardware.DumbColorSensor;
import virtualRobot.hardware.IMU;
import virtualRobot.hardware.Motor;
import virtualRobot.hardware.Sensor;
import virtualRobot.hardware.Servo;
import virtualRobot.hardware.StateSensor;
import virtualRobot.utils.CVTelemetry;
import virtualRobot.utils.MatConverterFactory;

/**
 * Created by DOSullivan on 9/14/16.
 * All of our our virtual hardware and there getters are housed in SallyJoeBot
 */
public class SallyJoeBot {
    //Robot Constants
    public volatile double initialBattery;
    public final double wheelDiameter = 5;
    public final double botWidth = 5;
    public final double botLength = 5;

    //Data to pass to UpdateThread
    private ArrayList<String> robotProgress;
    private ConcurrentHashMap<String, Object> telemetry;

    //Motors and Servos
    private Motor LFMotor, LBMotor, RFMotor, RBMotor;
    private ContinuousRotationServo rollerRight;
    private Motor rollerLeft;
    private ContinuousRotationServo boxLeft, boxRight;
    private Motor relicArm;
    private Motor liftLeft, liftRight;
    private ContinuousRotationServo relicArmWinch;
    private Servo relicArmWrist;
    private Servo relicArmClaw;
    private Servo jewelServo;
    private DumbColorSensor colorSensor;

    //Sensors
    private IMU imu;
    private Sensor voltageSensor;
    private JoystickController joystickController1, joystickController2;
    private StateSensor stateSensor;

    //CVTelemetry
    private CVTelemetry cvtel;
    private final String ipaddr = "http://172.20.95.207:8080/";

    //Motors, sensors, servos instantiated (e.g Motor = new Motor(), some positions can also be set if desired
    public SallyJoeBot() {
        joystickController1 = new JoystickController();
        joystickController2 = new JoystickController();
        voltageSensor = new Sensor();
        robotProgress = new ArrayList<String>();
        telemetry = new ConcurrentHashMap<>();
        stateSensor = new StateSensor();
        imu = new IMU();

        LFMotor = new Motor();
        LBMotor = new Motor();
        RFMotor = new Motor();
        RBMotor = new Motor();
        rollerLeft = new Motor();
        rollerRight = new ContinuousRotationServo();
        boxLeft = new ContinuousRotationServo();
        boxRight = new ContinuousRotationServo();
        relicArm = new Motor();
        liftLeft = new Motor();
        liftRight = new Motor();
        relicArmWinch = new ContinuousRotationServo();
        relicArmWrist = new Servo();
        relicArmClaw = new Servo();
        jewelServo = new Servo();
        colorSensor = new DumbColorSensor();
        //capLift = new SyncedMotors(LiftLeftMotor, LiftRightMotor, LiftLeftEncoder, LiftRightEncoder, KP, KI, KD, SyncedMotors.SyncAlgo.POSITION);
        //capLift.setRatio(1);

    }
    //All of Autonomous and TeleopRobot's functions are created e.g. (public synchronized Motor getMotor() {return Motor;}

    public synchronized DumbColorSensor getColorSensor() {
        return colorSensor;
    }

    public synchronized Sensor getVoltageSensor() {
        return voltageSensor;
    }

    public synchronized IMU getImu() {
        return imu;
    }

    public synchronized Motor getLFMotor() {
        return LFMotor;
    }

    public synchronized Motor getLBMotor() {
        return LBMotor;
    }

    public synchronized Motor getRFMotor() {
        return RFMotor;
    }

    public synchronized Motor getRBMotor() {
        return RBMotor;
    }

    public synchronized Motor getLiftLeft() {
        return liftLeft;
    }

    public synchronized Motor getLiftRight() {
        return liftRight;
    }

    public synchronized ContinuousRotationServo getBoxLeft() {
        return boxLeft;
    }

    public synchronized ContinuousRotationServo getBoxRight() {
        return boxRight;
    }

    public synchronized Motor getRelicArm() {
        return relicArm;
    }

    public synchronized ContinuousRotationServo getRelicArmWinch() {
        return relicArmWinch;
    }

    public synchronized Servo getRelicArmWrist() {
        return relicArmWrist;
    }

    public synchronized Servo getRelicArmClaw() {
        return relicArmClaw;
    }

    public synchronized Motor getRollerLeft() {
        return rollerLeft;
    }

    public synchronized ContinuousRotationServo getRollerRight() {
        return rollerRight;
    }

    public synchronized Servo getJewelServo() {
        return jewelServo;
    }

    public synchronized StateSensor getStateSensor() {
        return stateSensor;
    }

    public synchronized void stopMotors() {
        LFMotor.setPower(0);
        RFMotor.setPower(0);
        LBMotor.setPower(0);
        RBMotor.setPower(0);
    }

    public synchronized JoystickController getJoystickController1() {
        return joystickController1;
    }

    public synchronized JoystickController getJoystickController2() {
        return joystickController2;
    }

    public synchronized void addToProgress(String s) {
        robotProgress.add(s);
    }

    public synchronized ArrayList<String> getProgress() {
        return robotProgress;
    }

    public synchronized void addToTelemetry(String s, Object arg) {
        telemetry.put(s, arg);
    }

    public synchronized ConcurrentHashMap<String, Object> getTelemetry() {
        return telemetry;
    }

    public synchronized void initCVTelemetry() {
        cvtel = new Retrofit.Builder()
                .baseUrl(ipaddr)
                .addConverterFactory(MatConverterFactory.create())
                .build()
                .create(CVTelemetry.class);
    }

    public synchronized Call<Void> sendCVTelemetry(String windowName, Mat img) throws IOException {
        return cvtel.sendImage(windowName, img);
    }
}
