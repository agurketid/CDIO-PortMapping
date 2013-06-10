package hsvtests;

import static com.googlecode.javacv.cpp.opencv_core.CV_AA;

import static com.googlecode.javacv.cpp.opencv_core.CV_RGB;
import static com.googlecode.javacv.cpp.opencv_core.cvCreateImage;
import static com.googlecode.javacv.cpp.opencv_core.*;


import static com.googlecode.javacv.cpp.opencv_highgui.cvLoadImage;
import static com.googlecode.javacv.cpp.opencv_highgui.cvSaveImage;
import java.util.ArrayList;
import java.util.LinkedList;

import javax.bluetooth.RemoteDevice;


import lejos.nxt.Motor;
import lejos.nxt.remote.*;
import lejos.pc.comm.NXTComm;
import lejos.pc.comm.NXTCommException;
import lejos.pc.comm.NXTCommFactory;
import lejos.pc.comm.NXTInfo;
import main.Block.Color;
import com.googlecode.javacpp.Loader;
import com.googlecode.javacv.CanvasFrame;
import com.googlecode.javacv.FrameGrabber.Exception;
import com.googlecode.javacv.OpenCVFrameGrabber;
import com.googlecode.javacv.cpp.opencv_core.CvContour;
import com.googlecode.javacv.cpp.opencv_core.CvMemStorage;
import com.googlecode.javacv.cpp.opencv_core.CvRect;
import com.googlecode.javacv.cpp.opencv_core.*;
import com.googlecode.javacv.cpp.opencv_core.CvScalar;
import com.googlecode.javacv.cpp.opencv_core.CvSeq;
import com.googlecode.javacv.cpp.opencv_core.IplImage;
import static com.googlecode.javacv.cpp.opencv_core.*;
import static com.googlecode.javacv.cpp.opencv_imgproc.*;

public class ImageScanner {

	// color ranges
	static CvScalar minRed = cvScalar(100,0,0,0);
	static CvScalar maxRed = cvScalar(160,50,50,0); 
	static CvScalar minGreen = cvScalar(0,55,0,0);
	static CvScalar maxGreen = cvScalar(60,255,60,0);
	static CvScalar minLightBlue = CV_RGB(0,60,90);
	static CvScalar maxLightBlue = CV_RGB(70,170,200);
	static CvScalar minDarkBlue = CV_RGB(170,170,170);
	static CvScalar maxDarkBlue = CV_RGB(255,255,255);
//	static CvScalar minDarkBlue = CV_RGB(0,20,60);
//	static CvScalar maxDarkBlue = CV_RGB(70,90,150);
//	static CvScalar minRed = CV_RGB(100,0,0);
//	static CvScalar maxRed = CV_RGB(200,70,70); 
//	static CvScalar minGreen = CV_RGB(0,60,0);
//	static CvScalar maxGreen = CV_RGB(60,100,60);
//	static CvScalar minLightBlue = CV_RGB(0,100,150);
//	static CvScalar maxLightBlue = CV_RGB(100,255,255);
//	static CvScalar minDarkBlue = CV_RGB(30,30,30);
//	static CvScalar maxDarkBlue = CV_RGB(50,60,80);
//	static CvScalar minRed = CV_RGB(240,0,0);
//	static CvScalar maxRed = CV_RGB(255,20,20); 
//	static CvScalar minGreen = CV_RGB(0,240,0);
//	static CvScalar maxGreen = CV_RGB(20,255,20);
//	static CvScalar minLightBlue = CV_RGB(0,230,240);
//	static CvScalar maxLightBlue = CV_RGB(20,255,255);
//	static CvScalar minDarkBlue = CV_RGB(0,0,240);
//	static CvScalar maxDarkBlue = CV_RGB(20,20,255);

	static CaptureImage ci = new CaptureImage();
	static IplImage orgImg;
	static ArrayList<Position> points = new ArrayList<Position>();
	static ArrayList<Block> greenBlocks =  new ArrayList<Block>();
	static ArrayList<Block> redBlocks = new ArrayList<Block>();
	static ArrayList<Port> ports =  new ArrayList<Port>();
	static Robot robot = new Robot();
	
	public static void main(String[] args) {
		
		long startTime = System.currentTimeMillis();
		Motor.A.setSpeed(100); 
		Position robotFront;
		Position robotBack;
		Position start;
		int speedDifference;
		final int robotSpeed = 250;
		
		ci.run();
		try {
			Thread.sleep(10);
		} catch (InterruptedException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		// load taken image
		orgImg = cvLoadImage("image.jpg");
		 
		// map image to objects
		greenBlocks = findBlocks(orgImg, Color.GREEN);
		greenBlocks.remove(greenBlocks.size()-1);
		System.out.println("Green blocks: " + greenBlocks.size());
		
		redBlocks = findBlocks(orgImg, Color.RED);
		redBlocks.remove(redBlocks.size()-1);
		System.out.println("Red blocks: " + redBlocks.size());
		
		ports = mapPorts(redBlocks, greenBlocks);
		
		
		CanvasFrame cnvs=new CanvasFrame("CUDACruiser");

 		while(true)	{
 			System.out.println("================================");
			// take picture and save picture as image.jpg (in CaptureImage class)
 			ci.run();
			try {
				Thread.sleep(10);
			} catch (InterruptedException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			// load taken image
			orgImg = cvLoadImage("image.jpg");
			 
			System.out.println("Ports: " + ports.size());
			
			// find robot on image an map to Robot object
			robotFront = findRobot(orgImg, "front");
			System.out.println("Front: " + robotFront.toString());
			robotBack = findRobot(orgImg, "back");
			System.out.println("Back: " + robotBack.toString());
			robot.robotInit(robotFront, robotBack);
			start = robot.getMiddle();
				//System.out.println(robot.getMiddle().toString());
			
			// find route for robot
			points = mapRoute(ports, start, robot);

			// calculate robot movement
			String move = calculateRobotMovement(robot, points);
			System.out.println(move);
			speedDifference = calculateRobotSpeed(robot, points);
			
			// send move signals to robot
			if (move.equals("RIGHT")) {
				Motor.A.setSpeed(robotSpeed);
				Motor.A.backward();
				Motor.B.setSpeed(robotSpeed-speedDifference);
				//Motor.B.setSpeed(60);
				Motor.B.backward();
			} else { // move.equals("LEFT")
				Motor.B.setSpeed(robotSpeed);
				Motor.B.backward();
				Motor.A.setSpeed(robotSpeed-speedDifference);
				//Motor.A.setSpeed(60);
				Motor.A.backward();
			}
			
			// DRAW ROBOT
			cvRectangle(orgImg, cvPoint(robotFront.getX()-5, robotFront.getY()-5), cvPoint(robotFront.getX()+5, robotFront.getY()+5),
					CvScalar.CYAN, CV_FILLED, CV_AA, 0);
			cvRectangle(orgImg, cvPoint(robotBack.getX()-5, robotBack.getY()-5), 
					cvPoint(robotBack.getX()+5, robotBack.getY()+5), CvScalar.WHITE, CV_FILLED, CV_AA, 0);

			for (int i = 0; i < ports.size(); i++) {
				// Show green ports
				cvRectangle(orgImg, cvPoint(ports.get(i).getGreen().getCenter().getX()-10, ports.get(i).getGreen().getCenter().getY()-10), 
						cvPoint(ports.get(i).getGreen().getCenter().getX()+10, ports.get(i).getGreen().getCenter().getY()+10), 
						CvScalar.GREEN, CV_FILLED, CV_AA, 0);
				// Show red ports
				cvRectangle(orgImg, cvPoint(ports.get(i).getRed().getCenter().getX()-10, ports.get(i).getRed().getCenter().getY()-10), 
						cvPoint(ports.get(i).getRed().getCenter().getX()+10, ports.get(i).getRed().getCenter().getY()+10), 
						CvScalar.RED, CV_FILLED, CV_AA, 0);
			}


			// DRAW IN AND OUT PORTS
			for (int i = 0; i < ports.size(); i++) {

				cvRectangle(orgImg, cvPoint(ports.get(i).getIn().getX(), ports.get(i).getIn().getY()), 
						cvPoint(ports.get(i).getIn().getX(), ports.get(i).getIn().getY()), CvScalar.BLUE, 3, CV_AA, 0);
				cvRectangle(orgImg, cvPoint(ports.get(i).getOut().getX(), ports.get(i).getOut().getY()), 
						cvPoint(ports.get(i).getOut().getX(), ports.get(i).getOut().getY()), CvScalar.MAGENTA, 3, CV_AA, 0);
			}

			// DRAW LINE
			for(int i = 0; i < points.size(); i++) {
				if (i != points.size()-1) {
					CvPoint p1 = new CvPoint(points.get(i).getX(),points.get(i).getY());
					CvPoint p2 = new CvPoint(points.get(i+1).getX(),points.get(i+1).getY());
					cvLine(orgImg, p1, p2, CV_RGB(255,0,210), 1, CV_AA, 0);
				} else {
					CvPoint p1 = new CvPoint(points.get(i).getX(),points.get(i).getY());
					CvPoint p2 = new CvPoint(points.get(1).getX(),points.get(1).getY());
					cvLine(orgImg, p1, p2, CV_RGB(255,0,210), 1, CV_AA, 0);
				}
			}

			CvPoint r1 = new CvPoint(robot.getMiddle().getX(), robot.getMiddle().getY());
			CvPoint r2;
			if (robot.getFront().getX() < robot.getBack().getX()) {
				r2 = new CvPoint(robot.getMiddle().getX()-100, (int) (robot.getMiddle().getY()-(100*robot.getDirection())));
			} else {
				r2 = new CvPoint(robot.getMiddle().getX()+100, (int) (robot.getMiddle().getY()+(100*robot.getDirection())));
			}
				cvLine(orgImg, r1, r2, CV_RGB(255,0,0), 1, CV_AA, 0);

//			if(robot.getDirection() < robot.getMiddle().calculateSlope(points.get(1))) {
//				System.out.println("TURN RIGHT");
//			} 
//			if(robot.getDirection() > robot.getMiddle().calculateSlope(points.get(1))) {
//				System.out.println("TURN LEFT");
//			}		
//			if(robot.getDirection() == robot.getMiddle().calculateSlope(points.get(1))) {
//				System.out.println("STRAIGHT AHEAD");
//			}

			// SHOW IMAGE
			cnvs.setDefaultCloseOperation(javax.swing.JFrame.EXIT_ON_CLOSE);
			cnvs.showImage(orgImg);
			cvSaveImage("drawImg.jpg", orgImg);
	
			long middle = System.currentTimeMillis();
			long middleTime = middle - startTime;
			System.out.println("TIME: " + middleTime);
			//orgImg.release();
		}

	}

	/*
	 * FIND BLOCKS
	 */
	public static ArrayList<Block> findBlocks(IplImage orgImage, Color color) {
		// create threshold image
		IplImage imgThreshold = cvCreateImage(cvGetSize(orgImage), 8, 1);
		//Convert to HSV
		cvCvtColor(orgImage,imgThreshold,CV_BGR2HSV);
		// set color range - red/green
		if (color == Color.RED) {
			cvInRangeS(imgThreshold, minRed, maxRed, imgThreshold);
		} else {
			cvInRangeS(imgThreshold, minGreen, maxGreen, imgThreshold);
		}

		// smooth image - median
		cvSmooth(imgThreshold, imgThreshold, CV_MEDIAN, 13);
		
		// make all colors find in range black and everything else white
//		cvThreshold(imgThreshold, imgThreshold, 100, 255, CV_THRESH_BINARY_INV);

		// setup memory storage for saving "blocks"
		CvMemStorage memory=CvMemStorage.create();

		// new growable sequence of elements
		CvSeq cvSeq=new CvSeq();

		// find contours of blocks, and save them as a sequence in the memory storage
		cvFindContours(imgThreshold, memory, cvSeq, Loader.sizeof(CvContour.class), 
				CV_RETR_LIST, CV_CHAIN_APPROX_SIMPLE);

		// new arraylist to store "blocks" as Block objects
		ArrayList<Block> blocks = new ArrayList<Block>();

		// map found elements to Block objects
		while (cvSeq != null && !cvSeq.isNull()) {
			CvRect rect = cvBoundingRect(cvSeq, 0);
			int x = rect.x();
			int y = rect.y();
			int h = rect.height();
			int w = rect.width();
			
			// determine what size of elements you want as "blocks"
			if (w < 3000 || h < 3000) {
				blocks.add(new Block(new Position(x,y), new Position(x+w,y), new Position(x,y+h), 
						new Position(x+w,y+h), color));
			}
			cvSeq = cvSeq.h_next();
		}
		cvClearMemStorage(memory);
		return blocks;
	}

	/*
	 * FIND PORTS - MAPS RED AND GREEN BLOCK TOGETHER TO PORTS
	 */
	public static ArrayList<Port> mapPorts(ArrayList<Block> redBlocks, ArrayList<Block> greenBlocks) {
		ArrayList<Port> portsTemp = new ArrayList<Port>();
		int index = 0; // the index of the closest green Block - from the red Block
		int d0, d1; // variables to store distance measurements 

		// find the closest green Block for all red Blocks and create a Port
		for(int i = 0; i < redBlocks.size(); i++) {

			// distance between the red Block and the first green Block in the list - base case
			d0 = redBlocks.get(i).getCenter().calculateDistance(greenBlocks.get(0).getCenter());

			// index of the first element
			index = 0;

			// finds the index of the green Block closest to the red Block
			for (int j = 1; j < greenBlocks.size(); j++) {
				d1 = redBlocks.get(i).getCenter().calculateDistance(greenBlocks.get(j).getCenter());
				if (d1 < d0) {
					d0 = d1;
					index = j;
				}
			}
			portsTemp.add(new Port(redBlocks.get(i),greenBlocks.get(index)));
		}
		return portsTemp;
	}	

	/*
	 * CALCULATE ROUTE
	 */
	public static ArrayList<Position> mapRoute(ArrayList<Port> ports, Position start, Robot robot) {
		ArrayList<Position> points = new ArrayList<Position>();
		
		// Create copy of port array
		ArrayList<Port> portsTemp = new ArrayList<Port>();
		portsTemp.addAll(ports);

		// Add start position to the "route list"
		points.add(start);

		int d0, d1; // variables to store distance measurements 
		int index = 0; // index on the array of the closest point

		int size = ports.size(); // for some reason, it won't run as intended, if ports.size() is set in loop
		for (int i = 0; i < size; i++) {

			// distance between latest added position, and first in position in the portTemp array - base case
			d0 = points.get(points.size()-1).calculateDistance(portsTemp.get(0).getIn());

			// index of the first element
			index = 0;

			// find the index of the closest in position
			for(int j = 1; j < portsTemp.size(); j++) {
				d1 = points.get(points.size()-1).calculateDistance(portsTemp.get(j).getIn());
				if (d1 < d0) {
					d0 = d1;
					index = j;
				}
			}

			// add the in position and out position to the "route list"
			points.add(portsTemp.get(index).getIn());
			points.add(portsTemp.get(index).getOut());

			// remove the port (in and out) from the list - it should only appear in the "route list" once
			portsTemp.remove(index);
		}
		if (robot.getBack().calculateDistance(points.get(1)) < robot.getFront().calculateDistance(points.get(1))) {
			points.remove(1);
			points.remove(2);
		}
		if (start.calculateDistance(points.get(1)) < 0) {
			points.remove(1);
		}
		return points;
	}


	/*
	 * FIND ROBOT - FRONT AND BACK POSITIONS
	 */
	public static Position findRobot(IplImage orgImage, String part) {
		// create threshold image
		IplImage imgThreshold = cvCreateImage(cvGetSize(orgImage), 8, 1);
		//Convert to HSV
		cvCvtColor(orgImage,imgThreshold,CV_BGR2HSV);
		// set color range - front = blue
		if (part.equals("front")) {
			cvInRangeS(imgThreshold, minLightBlue, maxLightBlue, imgThreshold);
		} 
		if (part.equals("back")) {
			cvInRangeS(imgThreshold, minDarkBlue, maxDarkBlue, imgThreshold);
		}	

		// smooth image - median
		cvSmooth(imgThreshold, imgThreshold, CV_MEDIAN, 13);

		// make all colors find in range black and everything else white
//		cvThreshold(imgThreshold, imgThreshold, 100, 255, CV_THRESH_BINARY_INV);

		// setup memory storage for saving "blocks"
		CvMemStorage memory=CvMemStorage.create();

		// new growable sequence of elements
		CvSeq cvSeq=new CvSeq();

		// find contours of blocks, and save them as a sequence in the memory storage
		cvFindContours(imgThreshold, memory, cvSeq, Loader.sizeof(CvContour.class), 
				CV_RETR_LIST, CV_CHAIN_APPROX_SIMPLE);

		Position back = new Position();

		CvRect rect = cvBoundingRect(cvSeq, 0);
		int x = rect.x();
		int y = rect.y();
		int h = rect.height();
		int w = rect.width();

		// determine what size of elements you want 
		if (w < 500 || h < 500) {
			back.setX(x+(w/2));
			back.setY(y+(h/2));
		}
		cvClearMemStorage(memory);
		return back;
	}
	
	/*
	 * CALCULATE ROBOTS MOVEMENT FROM CURRENT POSITION AND DESIRED POSITION (PORT)
	 */
	public static String calculateRobotMovement(Robot robot, ArrayList<Position> points) {
		
		boolean frontRight = robot.getFront().getX() > robot.getBack().getX();
		boolean dir = robot.getDirection() > robot.getMiddle().calculateSlope(points.get(1));
		boolean left = robot.getMiddle().getX() < points.get(1).getX();
		boolean up = robot.getMiddle().getY() < points.get(1).getY();
		
		// up
		if ((frontRight && !dir && !left && !up) || (!frontRight && dir && !left && !up) 
				|| (frontRight && dir && left && !up)) {
			return "LEFT";
		}
		
		// down
		if ((frontRight && dir && left && up) || (!frontRight && !dir && left && up) 
				|| (!frontRight && dir && !left && up)) {
			return "LEFT";
		}
		
		// right - this could be deleted, because the cases are already covered
		if ((frontRight && dir && left && !up) || (frontRight && dir && left && up)) {
			return "LEFT";
		}
		
		// left
		if ((!frontRight && dir && !left && up) || (!frontRight && dir && !left && !up)) {
			return "LEFT";
		}
		
		return "RIGHT";
	}
	
    /*
     * CALCULATE THE DIFFERENCE BETWEEN SPEED ON ROBOT WHEELS TO SIMULATE TURNS
     */
    static int calculateRobotSpeed(Robot robot, ArrayList<Position> points) {
    	
    	// center
    	Position p0 = new Position();
    	
    	// current
    	Position p1 = new Position();
  
    	p0.setX(robot.getMiddle().getX());
    	p0.setY(robot.getMiddle().getY());
    	
    	p1.setX(points.get(1).getX());
    	p1.setY(points.get(1).getY());
    	
    	double b = robot.getMiddle().getY()-(robot.getDirection()*robot.getMiddle().getX());
    	
    	// previous
    	int y = (int) ((robot.getDirection()*(robot.getMiddle().getX()+30))+b);
    	int x = robot.getMiddle().getX()+30;
    	
    	double angle = 180-Math.abs(Math.toDegrees(Math.atan2(p1.getX() - p0.getX(),p1.getY() - p0.getY())-
                Math.atan2(x- p0.getX(),y- p0.getY())));

    	System.out.println("ANGLE: " + Math.abs(angle/2));

    	return (int) Math.abs(angle/2);
    }
}
