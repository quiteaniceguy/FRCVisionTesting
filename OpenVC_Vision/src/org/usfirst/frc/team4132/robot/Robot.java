package org.usfirst.frc.team4132.robot;

import edu.wpi.first.wpilibj.IterativeRobot;
import edu.wpi.first.wpilibj.Joystick;
import edu.wpi.first.wpilibj.smartdashboard.SendableChooser;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;

import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;

import edu.wpi.cscore.CvSink;
import edu.wpi.cscore.CvSource;
import edu.wpi.cscore.UsbCamera;
import edu.wpi.first.wpilibj.CameraServer;
import edu.wpi.first.wpilibj.IterativeRobot;

/**
 * The VM is configured to automatically run this class, and to call the
 * functions corresponding to each mode, as described in the IterativeRobot
 * documentation. If you change the name of this class or the package after
 * creating this project, you must also update the manifest file in the resource
 * directory.
 */
public class Robot extends IterativeRobot {
	final String defaultAuto = "Default";
	final String customAuto = "My Auto";
	String autoSelected;
	SendableChooser<String> chooser = new SendableChooser<>();
	
	Joystick controller;
	boolean buttonPressed=false;
	
	final double RED[]={0,0,255};
	final double GREEN[]={0,255,0};
	final double BLUE[]={255,0,0};
	
	enum Color{
		RED, BLUE, GREEN;
	}
	///only for testing
	int timerCounter=0;
	double lastTime=System.currentTimeMillis();
	
	/**
	 * This function is run when the robot is first started up and should be
	 * used for any initialization code.
	 */
	@Override
	public void robotInit() {
		chooser.addDefault("Default Auto", defaultAuto);
		chooser.addObject("My Auto", customAuto);
		SmartDashboard.putData("Auto choices", chooser);
		
		controller=new Joystick(0);
		
		new Thread(() -> {
			
			
			
            //UsbCamera camera = CameraServer.getInstance().startAutomaticCapture();
			UsbCamera camera = CameraServer.getInstance().startAutomaticCapture("cam0", 0);
			//UsbCamera camera_two = CameraServer.getInstance().startAutomaticCapture("cam1", 1);
			
            camera.setResolution(640, 480);
            //camera_two.setResolution(640, 480);
            
           // camera.setWhiteBalanceAuto();
            camera.setBrightness(8);
            camera.setExposureManual(5);
            
            
            
            CvSink cvSink = CameraServer.getInstance().getVideo("cam0");
            CvSource outputStream = CameraServer.getInstance().putVideo("Blur", 640, 480);
            
            
            
            Mat source = new Mat();
            Mat output = new Mat();
            
            
            while(!Thread.interrupted()) {
            	
            	
            	
                cvSink.grabFrame(source);
                Imgproc.cvtColor(source, output, Imgproc.COLOR_BGR2GRAY);
                
                
                double color[];
                
                ///length must be a multiple of 3 for a rgb mat
                
                int size=(int)(source.total()*source.channels());

                byte[] pixels=new byte[size];
                output.get(0, 0,pixels);
                
                int lineOneL=0, lineTwoL=0, lineThreeL=0;
                boolean lineOneV=false, lineTwoV=false, lineThreeV=false;
                int temp=0;
                int start=0; //the start of the cylinder
                int end=-1; // the end of the cylinder
                int pointx=0;
                int pointy=0;
                int lastColumnValue=0; //whether the last column tested positive
                //each loop, if it tests positive it will set 
                
                
                for(int x=0; x<output.cols(); x+=3){
                	lineOneV=false;
                	lineTwoV=false;
                	lineThreeV=false;
                	lineOneL=0;
                	lineTwoL=0;
                	lineThreeL=0;
                	temp=0;
                	
                	//if true, white
                	boolean lastLineValue=convertColor(pixels[x])>200;
                	for(int y=0; y+x<pixels.length; y+=output.cols()){
	                	///test lines aren't equal spacing
                		///loop through, if switch color, record line value, test if propotions match
                		
                		if(lastLineValue!=convertColor(pixels[x+y])>200){
                			
                			
                			lineOneL=lineTwoL;
                			lineTwoL=lineThreeL;
                			lineThreeL=temp;
                			
                			lineOneV=lineTwoV;
                			lineTwoV=lineThreeV;
                			lineThreeV=lastLineValue;
                			
                			lastLineValue=convertColor(pixels[x+y])>200;
                			temp=1;
                			
                			if(lineOneV==true && lineTwoV==false && lineThreeV==true && lineOneL>10 && lineTwoL>10 && lineThreeL>10){
                				double totalLength=lineOneL+lineTwoL+lineThreeL;
                				double lineOneRatio=(lineOneL)/totalLength;
                				double lineTwoRatio=(lineTwoL)/totalLength;
                				
                				if( lineOneRatio>.3 && lineOneRatio<.5 && lineTwoRatio>.3 && lineTwoRatio<.5){
                					if(lastColumnValue==0){//starts are not starting
                						start=x;
                					}
                					lastColumnValue=1;
                					
                					break;
                				}
                				else{
                					lastColumnValue=0;
                				}
                				//System.out.println("line1:" + lineOneL +"line2: " +lineTwoL+ "line3: " +lineThreeL);
                				
                			}
                		}else{
                			temp++;
                		} 
                		if(y+x+output.cols()>pixels.length){
                			lastColumnValue=0;
                		}
                	
                	}
                	if(lastColumnValue==0 && start !=0 && end==-1){
                		end=x;
                		
                	}
                }
                
                /*
                System.out.println("start"+start+"end" + end);
                System.out.println("center :" + (start+end)/2);
                */
                ///THIS DOES WORK
                pointx=(int)( 640*Math.tan((Math.atan(((double)(start)-320)/640.0)+Math.atan(((double)(end)-320)/640.0))/2) );
                boolean lastLineValue=convertColor(pixels[pointx])>200;
                for(int y=0; y+pointx<pixels.length; y+=output.cols()){ //take the center line and find the y-value, find it for the top point later, currently finds the bottom point
                	
            		if(lastLineValue!=convertColor(pixels[pointx+y])>200){
            			
            			
            			lineOneL=lineTwoL;
            			lineTwoL=lineThreeL;
            			lineThreeL=temp;
            			
            			lineOneV=lineTwoV;
            			lineTwoV=lineThreeV;
            			lineThreeV=lastLineValue;
            			
            			lastLineValue=convertColor(pixels[pointx+y])>200;
            			temp=1;
            			
            			if(lineOneV==true && lineTwoV==false && lineThreeV==true && lineOneL>10 && lineTwoL>10 && lineThreeL>10){
            				double totalLength=lineOneL+lineTwoL+lineThreeL;
            				double lineOneRatio=(lineOneL)/totalLength;
            				double lineTwoRatio=(lineTwoL)/totalLength;
            				
            				if( lineOneRatio>.3 && lineOneRatio<.5 && lineTwoRatio>.3 && lineTwoRatio<.5){

            					pointy=y-240;
            					
            					break;
            				}
            				else{
            					lastColumnValue=0;
            				}
            				//System.out.println("line1:" + lineOneL +"line2: " +lineTwoL+ "line3: " +lineThreeL);
            				
            			}
            		}else{
            			temp++;
            		} 
                }
                //at this point, we should have a pointx and pointy
                double distance;
                double pointh=78.0;
                distance=(Math.sqrt((pointx)^2+640^2)*pointh/(double)(pointy));
                
                
                //System.out.println(get3DDistance(1,1,1,4,5,6));
                output.put(0, 0, pixels);
                
                
                
                
        		//output.put(1,1,0);
            
                
                
                outputStream.putFrame(output);
            
                //System.out.println("brightness :"+ camera.getBrightness());++
                //gray returns array length of one;
                /*
                double pixel[]=source.get(0, 0);
                //prints blue, green , then red
                System.out.print("values: ");
                for(double i: pixel){
                	System.out.print(String.valueOf(i)+" ");
                }
                System.out.print("\n");
                System.out.print(pixels[0]+" ");
                System.out.print(pixels[1]+" ");
                System.out.print(pixels[2]+" ");
                System.out.print("\n");
                */
                /*
                if(timerCounter%60==0){
                	double currentTime=System.currentTimeMillis();
                	System.out.println( "milis Time per sixty frames: "+String.valueOf( currentTime-lastTime) ); 
                	lastTime=currentTime;
                }
                */
            }
        }).start();
	}

	/**
	 * This autonomous (along with the chooser code above) shows how to select
	 * between different autonomous modes using the dashboard. The sendable
	 * chooser code works with the Java SmartDashboard. If you prefer the
	 * LabVIEW Dashboard, remove all of the chooser code and uncomment the
	 * getString line to get the auto name from the text box below the Gyro
	 *
	 * You can add additional auto modes by adding additional comparisons to the
	 * switch structure below with additional strings. If using the
	 * SendableChooser make sure to add them to the chooser code above as well.
	 */
	@Override
	public void autonomousInit() {
		autoSelected = chooser.getSelected();
		// autoSelected = SmartDashboard.getString("Auto Selector",
		// defaultAuto);
		System.out.println("Auto selected: " + autoSelected);
	}

	/**
	 * This function is called periodically during autonomous
	 */
	@Override
	public void autonomousPeriodic() {
		switch (autoSelected) {
		case customAuto:
			// Put custom auto code here
			break;
		case defaultAuto:
		default:
			// Put default auto code here
			break;
		}
	}

	/**
	 * This function is called periodically during operator control
	 */
	@Override
	public void teleopPeriodic() {
		buttonPressed=controller.getRawButton(1);

	}

	/**
	 * This function is called periodically during test mode
	 */
	@Override
	public void testPeriodic() {
		
	}
	
	private double convertColor(int x){
		if(x>0){
			return x;
		}
		return 128+x*-1;
		
	}
	
	//really slow
	private Color getColor(double blue, double green, double red){
		
		double dFromBlue=get3DDistanceSqr(blue, green, red, BLUE[0], BLUE[1], BLUE[2]);
		double dFromGreen=get3DDistanceSqr(blue, green, red, GREEN[0], GREEN[1], GREEN[2]);
		double dFromRed=get3DDistanceSqr(blue, green, red, RED[0], RED[1], RED[2]);
		if(dFromBlue>dFromGreen && dFromBlue>dFromRed){
			return Color.BLUE;
		}
		if(dFromGreen>dFromBlue && dFromGreen>dFromRed){
			return Color.GREEN;
		}
		else{
			return Color.BLUE;
		}
		
	}
	
	private double get3DDistanceSqr(double a, double b, double c, double x, double y, double z){
		return ( x-a)*(x-a) + (y-b)*(y-b)+ ( z-c)*(z-c);
	}
	
	private int getIImageVolume(int bottomR, int topL, int bottomL, int topR){
		return bottomR+topL-bottomL-topR;
	}
	
}

