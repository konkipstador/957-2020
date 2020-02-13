package frc.robot;

import com.revrobotics.CANEncoder;
import com.revrobotics.CANPIDController;
import com.revrobotics.CANSparkMax;
import com.revrobotics.CANSparkMax.IdleMode;
import com.revrobotics.CANSparkMaxLowLevel;
import com.revrobotics.ControlType;

import edu.wpi.first.wpilibj.DigitalInput;
import edu.wpi.first.wpilibj.DoubleSolenoid;
import edu.wpi.first.wpilibj.DoubleSolenoid.Value;
import frc.robot.RobotState.State;

public class PowerCell{

    double m_shooterSpeed = 0;

    DoubleSolenoid m_arm = new DoubleSolenoid(0, 1);
    DoubleSolenoid m_arm2 = new DoubleSolenoid(2, 3);

    RobotState m_state = RobotState.getInstance();

    CANSparkMax m_neoIntake = new CANSparkMax(5, CANSparkMaxLowLevel.MotorType.kBrushless);
    CANSparkMax m_neoPassthrough = new CANSparkMax(6, CANSparkMaxLowLevel.MotorType.kBrushless);
    CANSparkMax m_neoPassthrough2 = new CANSparkMax(7, CANSparkMaxLowLevel.MotorType.kBrushless);
    CANEncoder m_neoPassEncoder = m_neoIntake.getEncoder();
    CANPIDController m_pidController = m_neoPassthrough.getPIDController();

    DigitalInput m_intakeSensor = new DigitalInput(0);
    DigitalInput m_intakeSensor2 = new DigitalInput(0);
    DigitalInput m_shooterSensor = new DigitalInput(0);

    CANSparkMax m_shooterMaster = new CANSparkMax(7, CANSparkMaxLowLevel.MotorType.kBrushless);
    CANSparkMax m_shooterslave = new CANSparkMax(8, CANSparkMaxLowLevel.MotorType.kBrushless);

    int m_ballCount = 0;
    double m_setPoint = 1;
    double m_timer = 0;

    double kP = 0.00008;
    double kI = 5e-6;
    double kD = 0.00001;
    double kIz = 2;
    double kFF = 0.0002;
    int maxRPM = 5700;
    int maxVel = 5700;
	double maxAcc = 3750;

    private static PowerCell m_powercell = null;

    double m_index_distance = 581/(5*Math.PI);

    private PowerCell(){
        m_neoIntake.restoreFactoryDefaults();
        m_neoPassthrough.restoreFactoryDefaults();
        m_neoPassthrough2.restoreFactoryDefaults();

        m_neoPassthrough2.follow(m_neoPassthrough);
        m_neoPassthrough2.setInverted(true);

        m_pidController.setP(kP);
		m_pidController.setI(kI);
		m_pidController.setD(kD);
		m_pidController.setIZone(kIz);
		m_pidController.setFF(kFF);
		m_pidController.setSmartMotionMaxVelocity(maxVel, 0);
		m_pidController.setSmartMotionMinOutputVelocity(0, 0);
		m_pidController.setSmartMotionMaxAccel(maxAcc,0);
		m_pidController.setOutputRange(-1, 1);	
    } 
     public void setIdleMode(IdleMode mode){
        m_neoPassthrough.setIdleMode(mode);
        m_neoPassthrough2.setIdleMode(mode);
        
     }
    public static PowerCell getInstance(){
        if(m_powercell == null)
        m_powercell = new PowerCell();

        return m_powercell;
    }

    public void run(boolean buttonIntake, boolean buttonEject, boolean buttonReverse, boolean buttonShoot, boolean buttonGrab){

        boolean armState = false;

         
        switch(m_state.state()){
            case GRAB_CELL:

                m_neoIntake.set(1);
                if(m_intakeSensor.get() == false && m_ballCount < 5){
                    m_state.setState(State.WAITING);
                    m_setPoint = m_setPoint + m_index_distance;
                    m_ballCount = m_ballCount + 1; 
                    
                }

                if(m_ballCount == 4 && m_intakeSensor2.get()){
                    m_setPoint = m_setPoint + 10;
                    m_ballCount = m_ballCount + 1;
                }
                break;
          
            case WAITING:

                m_neoIntake.set(0);

                if(m_intakeSensor.get() == false){
                    m_setPoint = m_setPoint + m_index_distance;
                }
                
                break;
            
            case EJECT:
                
                m_shooterMaster.set(.5);
                m_state.setState(State.WAITING);    
                            
                break;
            
            case SHOOT:
                
                m_shooterMaster.set(1);
                m_state.setState(State.SCORE);

                break;
            
            case SCORE:

                m_setPoint = m_setPoint + 184.938043873;
                m_ballCount = 0;
                m_state.setState(State.WAITING);

                break;
            
            default:

                armState = false;
                m_neoIntake.set(0);

                break;
        }

         if(armState){
            m_arm.set(Value.kReverse);
            m_arm2.set(Value.kReverse);
        }else{
            m_arm.set(Value.kForward);
            m_arm2.set(Value.kForward);
    
        }

        if(buttonIntake == true && armState == true){
            armState = false;
        }

        if(buttonIntake == true && armState == false){
            armState = true;
        }

        if(buttonEject == true){
            m_state.setState(State.EJECT);
        }

        if(buttonReverse == true){
            m_setPoint = m_setPoint - m_index_distance;
        }

        if(buttonShoot == true){
            m_state.setState(State.SHOOT);
        }

        if(buttonGrab = true){
            m_state.setState(State.GRAB_CELL);
        }

        m_pidController.setReference(m_setPoint, ControlType.kSmartMotion);
        
    }

      public void reset(){
        m_neoPassEncoder.setPosition(0);
    }
      public void passthroughReset(){
        m_state.setState(State.WAITING);
        reset();
        m_setPoint = 221.925652648;
        reset(); 
        }
}