/*----------------------------------------------------------------------------*/
/* Copyright (c) FIRST 2008. All Rights Reserved.                             */
/* Open Source Software - may be modified and shared by FRC teams. The code   */
/* must be accompanied by the FIRST BSD license file in the root directory of */
/* the project.                                                               */
/*----------------------------------------------------------------------------*/
package edu.wpi.first.wpilibj.templates;

import edu.wpi.first.wpilibj.Compressor;
import edu.wpi.first.wpilibj.DigitalInput;
import edu.wpi.first.wpilibj.DoubleSolenoid;
import edu.wpi.first.wpilibj.DriverStationLCD;
import edu.wpi.first.wpilibj.IterativeRobot;
import edu.wpi.first.wpilibj.Jaguar;
import edu.wpi.first.wpilibj.Joystick;
import edu.wpi.first.wpilibj.Timer;
import edu.wpi.first.wpilibj.Victor;
import edu.wpi.first.wpilibj.Watchdog;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;

/**
 * The VM is configured to automatically run this class, and to call the
 * functions corresponding to each mode, as described in the IterativeRobot
 * documentation. If you change the name of this class or the package after
 * creating this project, you must also update the manifest file in the resource
 * directory.
 */
/**
 * Thanks to everyone on 4464 who's contributed to the code in 2014: Reed;
 * Dorian; Elias; Eli; Nitay; And, yes, even Allen. We wouldn't have this
 * working without all of you!
 */
public class RobotTemplate extends IterativeRobot {

    /**
     * This function is run when the robot is first started up and should be
     * used for any initialization code.
     */
    //These booleans are used as flags.
    boolean down = true; //Is the arm down?
    boolean lowering = false; //Is the arm lowering?
    boolean closeFiring = false; //Is the shooter performing a close shot?
    boolean longFiring = false; //Is the shooter performing a long shot?
    boolean isTimed = false; //Has the shooter delay timer running out?
    double shotTime; //Pegged to the FGPA timestamp, i.e. the global CRIO system time

    //Flags for changing drive directions
    boolean driveDirection = true;
    boolean driveButtonCheck = true;

    //Flags for roller
    boolean running = false;
    boolean rollerBarButtonCheck = true;
    boolean rollerArmButtonCheck=true;

    // Declare motor controller objects for the robot drive system
    Jaguar m_victor_l1;
    Jaguar m_victor_l2;
    Jaguar m_victor_l3;
    Jaguar m_victor_r1;
    Jaguar m_victor_r2;
    Jaguar m_victor_r3;
    Victor m_roller_arm1;
    Victor m_roller_arm2;
    Jaguar m_roller;

    // Used for accessing the driver station message box, used by caling "getInstance()" followed by "println(int line, int startingColumn, String text)" and finally, "updateLCD()" to send the text to the driver station
    DriverStationLCD m_driverLCD;

    // Declare limit switches
    DigitalInput m_roller_limit_1;
    DigitalInput m_roller_limit_2;
    DigitalInput m_roller_limit_3;
    DigitalInput m_roller_limit_4;

    DigitalInput m_shoot_toggle;

    // Declare compressor
    Compressor m_compressor;

    // Declare jostick
    Joystick m_gamepad;

    //Declare solenoids
    DoubleSolenoid[] DS;
    DoubleSolenoid hardStop;

    public RobotTemplate() {
        // Initialize motor controllers
        m_victor_l1 = new Jaguar(1);
        m_victor_l2 = new Jaguar(2);
        m_victor_l3 = new Jaguar(3);
        m_victor_r1 = new Jaguar(4);
        m_victor_r2 = new Jaguar(5);
        m_victor_r3 = new Jaguar(6);
        m_roller_arm1 = new Victor(7);
        m_roller_arm2 = new Victor(8);
        m_roller = new Jaguar(9);

        // Initialize limit switches
        m_roller_limit_1 = new DigitalInput(1);
        m_roller_limit_2 = new DigitalInput(2);
        m_roller_limit_3 = new DigitalInput(3);
        m_roller_limit_4 = new DigitalInput(4);

        //Initialize shot toggle switch
        m_shoot_toggle = new DigitalInput(5);

        // Initialize compressor
        m_compressor = new Compressor(6, 1);
    }

    public void robotInit() {
        // Initialize joystick
        m_gamepad = new Joystick(1);

        // Initialize solenoids
        DS = new DoubleSolenoid[4];
        for (int i = 0; i < 4; i++) {
            DS[i] = new DoubleSolenoid(i + 1, i + 5);
        }

        SmartDashboard.putNumber("Drive Speed Multiplier", 0.25);

    }

    /**
     * This function is called periodically during autonomous
     */
    public void autonomousPeriodic() {
        goForward();
        moveRollerArmDown();
        Timer.delay(1.5);
        hardStopUp();
        Timer.delay(0.5);
        shoot();
        if (m_shoot_toggle.get()) {
            hardStopDown();
            Timer.delay(0.5);
            goBack();
            pickUp();
            moveRollerArmUp();
            Timer.delay(1);
            goForward();
            moveRollerArmDown();
            Timer.delay(1.5);
            hardStopUp();
            Timer.delay(1);
            shoot();
            Timer.delay(0.5);
            hardStopDown();
            Timer.delay(0.5);
            moveRollerArmUp();
        } else {
            Timer.delay(0.5);
            hardStopDown();
            Timer.delay(1);
            moveRollerArmUp();
        }

        //Robobees autonomous
        //start same way
        moveRollerArmDown();
        Timer.delay(1.5);
        startRollerBar(); //might need to make this constant rather than timed
        startDriveForward();
        Timer.delay(1);
        stopRollerBar();
        stopDrive();
        Timer.delay(0.5);
        hardStopUp();
        Timer.delay(1);
        shoot();
        Timer.delay(1.5);
        hardStopDown();
        Timer.delay(1);
        startRollerBar();
        Timer.delay(1);
        hardStopUp();
        Timer.delay(1);
        shoot();
        Timer.delay(0.5);
        hardStopDown();
        Timer.delay(0.5);
        moveRollerArmUp();

        //lower roller arm
        //bar spin inward
        //drive forward
        //stop driving and spinning
        //raise hard top
        //shoot
        //lower hardstop
        //roller inward
        //raise hard stop
        //shoot
        //lower hard stop
        //raise roller arm
    }

    /**
     * This function is called periodically during operator control
     */
    public void teleopInit() {
        //Initialize solenoids
        for (int i = 0; i < 4; i++) {
            DS[i].set(DoubleSolenoid.Value.kReverse);;
        }
    }

    public void teleopPeriodic() {

        // Initialize global system time
        Timer.getFPGATimestamp();

        // Feed the user watchdog at every period when in autonomous
        Watchdog.getInstance().feed();

        //********************************************************************
        //START OF NITAY'S ARCADE CODE 
        //********************************************************************
        double forward_input = m_gamepad.getY();
        double turn_input = m_gamepad.getRawAxis(4);
        double turn_output;
        double turn_scaling;
        double forward_output;
        double left_output;
        double right_output;

        //Deadband Forward motion
        if (Math.abs(forward_input) < .05) {
            forward_output = 0;
        } else {
            forward_output = forward_input * Math.abs(forward_input) * Math.abs(forward_input);
        }

        //If no forward output, then in turning mode, full turning available. Else turning limited to 75%
        if (forward_output == 0) {
            turn_scaling = 1;
        } else {
            turn_scaling = .75;
        }

        //Deadband turn output and apply turn scaling.
        if (Math.abs(turn_input) < .05) {
            turn_output = 0;
        } else {
            turn_output = turn_input * Math.abs(turn_input) * Math.abs(turn_input) * turn_scaling;
        }

        //Add forward and turning values such that -turning value turns robot right.
        if (!driveDirection) {
            forward_output *= -1;
        }
        left_output = forward_output - turn_output;
        right_output = forward_output + turn_output;

        //Cap outputs between -1 to +1
        if (Math.abs(left_output) > 1) {
            left_output /= Math.abs(left_output);
        }
        if (Math.abs(right_output) > 1) {
            right_output /= Math.abs(right_output);
        }
        left_output *= SmartDashboard.getNumber("Drive Speed Multiplier");
        right_output *= SmartDashboard.getNumber("Drive Speed Multiplier");
        m_victor_l1.set(-left_output);
        m_victor_l2.set(-left_output);
        m_victor_l3.set(-left_output);
        m_victor_r1.set(right_output);
        m_victor_r2.set(right_output);
        m_victor_r3.set(right_output);
//        if (flag1) {
//            m_victor_l1.set(-left_output);
//            m_victor_l2.set(-left_output);
//            m_victor_l3.set(-left_output);
//            m_victor_r1.set(right_output);
//            m_victor_r2.set(right_output);
//            m_victor_r3.set(right_output);
//        } else {
//            m_victor_l1.set(-left_output);
//            m_victor_l2.set(-left_output);
//            m_victor_l3.set(-left_output);
//            m_victor_r1.set(right_output);
//            m_victor_r2.set(right_output);
//            m_victor_r3.set(right_output);
//        }

        //********************************************************************
        //END OF NITAY'S ARCADE CODE 
        //********************************************************************
        //start compressor
        m_compressor.start();
        
        if(m_gamepad.getRawButton(1)&&rollerArmButtonCheck){
            down=!down;
            rollerArmButtonCheck=false;
            if(down){
                lowering=true;
            }
        }
        
        if(!m_gamepad.getRawButton(1))
        {
            rollerArmButtonCheck=true;
        }

//        if (m_gamepad.getRawButton(2) && !down) { //Sets desired arm state from user input, sets lowering to be true if changing to down from up
//            down = true;
//            lowering = true;
//        } else if (m_gamepad.getRawButton(1) && down) {
//            down = false;
//        }

        if (down) { //Moving the arm to desired state
            if (!m_roller_limit_4.get()) {
                m_roller_arm1.set(0.1);
            } else {
                m_roller_arm1.set(0);
            }

            if (!m_roller_limit_3.get()) {
                m_roller_arm2.set(-0.1);
            } else {
                m_roller_arm2.set(0);
            }

        } else {
            if (!m_roller_limit_2.get()) {
                m_roller_arm1.set(-0.3);
            } else {
                m_roller_arm1.set(0.05);
            }

            if (!m_roller_limit_1.get()) {
                m_roller_arm2.set(0.3);
            } else {
                m_roller_arm2.set(0.05);
            }

        }
        if (lowering) { //Prevents ball from getting yanked off catapult if lowering arm, but does not run roller in at 25% if arm bumps off of limit switches
            m_roller.set(-0.25);
            if (m_roller_limit_3.get() && m_roller_limit_4.get()) {
                m_roller.set(0);
                lowering = false;
            }
        }
        if (m_gamepad.getRawButton(7)) { //Sets initial firing state
            down = true;
            closeFiring = true;
            if (!m_roller_limit_3.get() && !m_roller_limit_4.get()) {
                lowering = true;
            }
        }
        if (m_gamepad.getRawButton(8)) {
            down = true;
            longFiring = true;
            if (!m_roller_limit_3.get() && !m_roller_limit_4.get()) {
                lowering = true;
            }
        }

        if (closeFiring && !lowering) { //Shoots the ball if arm is out of the way.
            closeShoot();
        }
        if (longFiring && !lowering) {
            longShoot();
        }

        if (down) {
            if (running) {
                m_roller.set(-1);
            } else if (!lowering) {
                m_roller.set(0);
            }
        } else {
            running = false;
            m_roller.set(0);
        }

        if (m_gamepad.getRawButton(4) && rollerBarButtonCheck) {
            running = !running;
            rollerBarButtonCheck = false;
        }

        if (m_gamepad.getRawButton(4) && !down) {
            m_roller.set(1);
        }

        if (!m_gamepad.getRawButton(4)) {
            rollerBarButtonCheck = true;
        }

        if (m_gamepad.getRawButton(10) && driveDirection == true) {
            driveButtonCheck = false;
        }
        if (!m_gamepad.getRawButton(10) && driveButtonCheck == false) {
            driveDirection = false;
        }

        if (m_gamepad.getRawButton(10) && driveDirection == false) {
            driveButtonCheck = true;
        }
        if (!m_gamepad.getRawButton(10) && driveButtonCheck == true) {
            driveDirection = true;
        }

        System.out.println("Limit Switch 1: " + m_roller_limit_1.get());
        System.out.println("Limit Switch 2: " + m_roller_limit_2.get());
        System.out.println("Limit Switch 3: " + m_roller_limit_3.get());
        System.out.println("Limit Switch 4: " + m_roller_limit_4.get());
        System.out.println("Down: " + down);
        System.out.println("Lowering: " + lowering);
        System.out.println("Long shot: " + longFiring);
        System.out.println("Close shot: " + closeFiring);

    }

    public void closeShoot() { //Method for shooting the ball based on a timer
        if (!isTimed) { //Starts timer if timer has not been started
            shotTime = Timer.getFPGATimestamp();
            isTimed = true;
        } else if ((Timer.getFPGATimestamp() - shotTime) <= 1.5) { //Fires cylinders if timer has not expired
            DS[0].set(DoubleSolenoid.Value.kForward);
            DS[1].set(DoubleSolenoid.Value.kForward);
            DS[2].set(DoubleSolenoid.Value.kForward);
            DS[3].set(DoubleSolenoid.Value.kForward);
        } else { //Retracts cylinders, ends firing cycle once timer expires
            DS[0].set(DoubleSolenoid.Value.kReverse);
            DS[1].set(DoubleSolenoid.Value.kReverse);
            DS[2].set(DoubleSolenoid.Value.kReverse);
            DS[3].set(DoubleSolenoid.Value.kReverse);
            isTimed = false;
            closeFiring = false;
        }
    }

    public void longShoot() {
        if (!isTimed) {
            shotTime = Timer.getFPGATimestamp();
            isTimed = true;
        } else if ((Timer.getFPGATimestamp() - shotTime) <= 1) {
            //hardStop.set(DoubleSolenoid.Value.kForward);
            System.out.println("There's no hardstop, silly!");
        } else if ((Timer.getFPGATimestamp() - shotTime) <= 1.5) {
            DS[0].set(DoubleSolenoid.Value.kForward);
            DS[1].set(DoubleSolenoid.Value.kForward);
            DS[2].set(DoubleSolenoid.Value.kForward);
            DS[3].set(DoubleSolenoid.Value.kForward);
        } else {
            //hardStop.set(DoubleSolenoid.Value.kReverse);
            System.out.println("There's no hardstop, silly!");
            DS[0].set(DoubleSolenoid.Value.kReverse);
            DS[1].set(DoubleSolenoid.Value.kReverse);
            DS[2].set(DoubleSolenoid.Value.kReverse);
            DS[3].set(DoubleSolenoid.Value.kReverse);
            isTimed = false;
            longFiring = false;
        }
    }

    /**
     * This function is called periodically during test mode
     */
    public void testPeriodic() {

    }

    public void hardStopUp() {
        hardStop.set(DoubleSolenoid.Value.kForward);
    }

    public void goForward() {
        m_victor_l1.set(1);
        m_victor_l2.set(1);
        m_victor_l3.set(1);
        m_victor_r1.set(-1);
        m_victor_r2.set(-1);
        m_victor_r3.set(-1);
        Timer.delay(2);
        m_victor_l1.set(0);
        m_victor_l2.set(0);
        m_victor_l3.set(0);
        m_victor_r1.set(0);
        m_victor_r2.set(0);
        m_victor_r3.set(0);
    }

    public void shoot() {
        DS[0].set(DoubleSolenoid.Value.kForward);
        DS[1].set(DoubleSolenoid.Value.kForward);
        DS[2].set(DoubleSolenoid.Value.kForward);
        DS[3].set(DoubleSolenoid.Value.kForward);
        Timer.delay(1.5);
        DS[0].set(DoubleSolenoid.Value.kReverse);
        DS[1].set(DoubleSolenoid.Value.kReverse);
        DS[2].set(DoubleSolenoid.Value.kReverse);
        DS[3].set(DoubleSolenoid.Value.kReverse);
    }

    public void hardStopDown() {
        hardStop.set(DoubleSolenoid.Value.kReverse);
    }

    public void goBack() {
        m_victor_l1.set(-1);
        m_victor_l2.set(-1);
        m_victor_l3.set(-1);
        m_victor_r1.set(1);
        m_victor_r2.set(1);
        m_victor_r3.set(1);
        Timer.delay(2);
        m_victor_l1.set(0);
        m_victor_l2.set(0);
        m_victor_l3.set(0);
        m_victor_r1.set(0);
        m_victor_r2.set(0);
        m_victor_r3.set(0);
    }

    public void pickUp() {
        m_roller.set(-0.75);
        Timer.delay(1.5);
        m_roller.set(0);
    }

    public void moveRollerArmDown() {
        double startTime = Timer.getFPGATimestamp();
        double loopTimer;
        while (!m_roller_limit_3.get() && !m_roller_limit_4.get()) {
            m_roller.set(-0.25);
            loopTimer = Timer.getFPGATimestamp();
            if ((loopTimer - startTime) > 3) {
                break;
            }
            if (!m_roller_limit_3.get()) {
                m_roller_arm1.set(0.80);
            } else {
                m_roller_arm1.set(0.05);
            }
            if (!m_roller_limit_4.get()) {
                m_roller_arm2.set(-0.80);
            } else {
                m_roller_arm2.set(-0.05);
            }
        }
        m_roller.set(0);
    }

    public void moveRollerArmUp() {
        double startTime = Timer.getFPGATimestamp();
        double loopTimer;
        while (!m_roller_limit_1.get() && !m_roller_limit_2.get()) {
            loopTimer = Timer.getFPGATimestamp();
            if ((loopTimer - startTime) > 3) {
                break;
            }
            if (!m_roller_limit_1.get()) {
                m_roller_arm1.set(0.80);
            } else {
                m_roller_arm1.set(0.05);
            }
            if (!m_roller_limit_2.get()) {
                m_roller_arm2.set(-0.80);
            } else {
                m_roller_arm2.set(-0.05);
            }
        }
    }

    public void startRollerBar() {
        m_roller.set(-1);
    }

    public void stopRollerBar() {
        m_roller.set(0);
    }

    public void startDriveForward() {
        m_victor_l1.set(1);
        m_victor_l2.set(1);
        m_victor_l3.set(1);
        m_victor_r1.set(-1);
        m_victor_r2.set(-1);
        m_victor_r3.set(-1);
    }

    public void startDriveBack() {
        m_victor_l1.set(-1);
        m_victor_l2.set(-1);
        m_victor_l3.set(-1);
        m_victor_r1.set(1);
        m_victor_r2.set(1);
        m_victor_r3.set(1);
    }

    public void stopDrive() {
        m_victor_l1.set(0);
        m_victor_l2.set(0);
        m_victor_l3.set(0);
        m_victor_r1.set(0);
        m_victor_r2.set(0);
        m_victor_r3.set(0);
    }

}
