package main;

import static com.googlecode.javacv.cpp.opencv_core.CV_AA;
import static com.googlecode.javacv.cpp.opencv_core.CV_RGB;
import static com.googlecode.javacv.cpp.opencv_core.cvCreateImage;
import static com.googlecode.javacv.cpp.opencv_core.cvGetSize;
import static com.googlecode.javacv.cpp.opencv_core.cvInRangeS;
import static com.googlecode.javacv.cpp.opencv_core.cvPoint;
import static com.googlecode.javacv.cpp.opencv_core.cvRectangle;
import static com.googlecode.javacv.cpp.opencv_highgui.cvLoadImage;
import static com.googlecode.javacv.cpp.opencv_highgui.cvSaveImage;
import java.util.ArrayList;
import java.util.LinkedList;
import lejos.nxt.Motor;
import main.Block.Color;
import com.googlecode.javacpp.Loader;
import com.googlecode.javacv.CanvasFrame;
import com.googlecode.javacv.FrameGrabber.Exception;
import com.googlecode.javacv.OpenCVFrameGrabber;
import com.googlecode.javacv.cpp.opencv_core.CvContour;
import com.googlecode.javacv.cpp.opencv_core.CvMemStorage;
import com.googlecode.javacv.cpp.opencv_core.CvRect;
import com.googlecode.javacv.cpp.opencv_core.CvScalar;
import com.googlecode.javacv.cpp.opencv_core.CvSeq;
import com.googlecode.javacv.cpp.opencv_core.IplImage;
import static com.googlecode.javacv.cpp.opencv_core.*;
import static com.googlecode.javacv.cpp.opencv_imgproc.*;

public class ImageScanner {

	// color ranges
	static CvScalar minRed = CV_RGB(240,0,10);
	static CvScalar maxRed = CV_RGB(255,10,70); 
	static CvScalar minGreen = CV_RGB(20,160,10);
	static CvScalar maxGreen = CV_RGB(100,255,50);
	static CvScalar minLightBlue = CV_RGB(45,240,240);
	static CvScalar maxLightBlue = CV_RGB(120,255,255);
	static CvScalar minDarkBlue = CV_RGB(20,70,180);
	static CvScalar maxDarkBlue = CV_RGB(70,130,235);

	static CaptureImage ci = new CaptureImage();
	static IplImage orgImg;
	static LinkedList<Position> points = new LinkedList<Position>();
	static ArrayList<Block> greenBlocks = new ArrayList<Block>();
	static ArrayList<Block> redBlocks = new ArrayList<Block>();
	static ArrayList<Port> ports = new ArrayList<Port>();
	
	public static void main(String[] args) {
		long startTime = System.currentTimeMillis();
		Motor.A.setSpeed(100);
		Position robotFront;
		Position robotBack;
		Position start;
		int speedDifference;
		final int robotSpeed = 400;
		
		// map image to objects
		greenBlocks = findBlocks(orgImg, Color.GREEN);
		redBlocks = findBlocks(orgImg, Color.RED);
		redBlocks.remove(redBlocks.size()-1);
		greenBlocks.remove(greenBlocks.size()-1);
		ports = mapPorts(redBlocks, greenBlocks);
		
		while(true){
			
			// take picture and save picture as capture.jpg (in CaptureImage class)
			ci.run();
			try {
				Thread.sleep(10);
			} catch (InterruptedException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			// load taken image
			orgImg = cvLoadImage("image.jpg");
			
			// find robot on image an map to Robot object
			robotFront = findRobot(orgImg, "front");
			robotBack = findRobot(orgImg, "back");
			Robot robot = new Robot(robotFront, robotBack);
			start = robot.getMiddle();
				//System.out.println(robot.getMiddle().toString());
			
			// find route for robot
			points = mapRoute(ports, start);
				//System.out.println("points: " + points.size());
			
			// calculate robot movement
			String move = calculateRobotMovement(robot, points);
			System.out.println(move);
			speedDifference = calculateRobotSpeed(robot, points);
			
			// send move signals to robot
			if (move.equals("RIGHT")) {
				Motor.A.setSpeed(robotSpeed);
				Motor.A.backward();
				Motor.B.setSpeed(robotSpeed-speedDifference);
				Motor.B.backward();
			} else { // move.equals("LEFT")
				Motor.B.setSpeed(robotSpeed);
				Motor.B.backward();
				Motor.A.setSpeed(robotSpeed-speedDifference);
				Motor.A.backward();
			}
	
			long middle = System.currentTimeMillis();
			long middleTime = middle - startTime;
			System.out.println("TIME: " + middleTime);
		}
	}

	/*
	 * FIND BLOCKS
	 */
	public static ArrayList<Block> findBlocks(IplImage orgImage, Color color) {
		// create threshold image
		IplImage imgThreshold = cvCreateImage(cvGetSize(orgImage), 8, 1);

		// set color range - red/green
		if (color == Color.RED) {
			cvInRangeS(orgImage, minRed, maxRed, imgThreshold);
		} else {
			cvInRangeS(orgImage, minGreen, maxGreen, imgThreshold);
		}

		// smooth image - median
		cvSmooth(imgThreshold, imgThreshold, CV_MEDIAN, 13);

		// make all colors find in range black and everything else white
		cvThreshold(imgThreshold, imgThreshold, 100, 255, CV_THRESH_BINARY_INV);

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
		return blocks;
	}

	/*
	 * FIND PORTS - MAPS RED AND GREEN BLOCK TOGETHER TO PORTS
	 */
	public static ArrayList<Port> mapPorts(ArrayList<Block> redBlocks, ArrayList<Block> greenBlocks) {
		ArrayList<Port> ports = new ArrayList<Port>();
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
			ports.add(new Port(redBlocks.get(i),greenBlocks.get(index)));
		}
		return ports;
	}	

	/*
	 * CALCULATE ROUTE
	 */
	public static LinkedList<Position> mapRoute(ArrayList<Port> ports, Position start) {
		LinkedList<Position> points = new LinkedList<Position>();

		// Create copy of port array
		ArrayList<Port> portsTemp = ports;

		// Add start position to the "route list"
		points.add(start);
		System.out.println("size: " + points.size());

		int d0, d1; // variables to store distance measurements 
		int index = 0; // index on the array of the closest point

		int size = ports.size(); // for some reason, it won't run as intended, if ports.size() is set in loop
		for (int i = 0; i < size; i++) {

			// distance between latest added position, and first in position in the portTemp array - base case
			d0 = points.getLast().calculateDistance(portsTemp.get(0).getIn());

			// index of the first element
			index = 0;

			// find the index of the closest in position
			for(int j = 1; j < portsTemp.size(); j++) {
				d1 = points.getLast().calculateDistance(portsTemp.get(j).getIn());
				if (d1 < d0) {
					d0 = d1;
					index = j;
				}
			}

			// add the in position and out position to the "route list"
			points.add(portsTemp.get(index).getIn());
			points.add(portsTemp.get(index).getOut());
			System.out.println("size: " + points.size());

			// remove the port (in and out) from the list - it should only appear in the "route list" once
			portsTemp.remove(index);
		}
		if (start.calculateDistance(points.get(1)) < 50) {
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
		
		// set color range - front = blue
		if (part.equals("front")) {
			cvInRangeS(orgImage, minLightBlue, maxLightBlue, imgThreshold);
		} 
		if (part.equals("back")) {
			cvInRangeS(orgImage, minDarkBlue, maxDarkBlue, imgThreshold);
		}	

		// smooth image - median
		cvSmooth(imgThreshold, imgThreshold, CV_MEDIAN, 13);

		// make all colors find in range black and everything else white
		cvThreshold(imgThreshold, imgThreshold, 100, 255, CV_THRESH_BINARY_INV);

		// setup memory storage for saving "blocks"
		CvMemStorage memory=CvMemStorage.create();

		// new growable sequence of elements
		CvSeq cvSeq=new CvSeq();

		// find contours of blocks, and save them as a sequence in the memory storage
		cvFindContours(imgThreshold, memory, cvSeq, Loader.sizeof(CvContour.class), 
				CV_RETR_LIST, CV_CHAIN_APPROX_SIMPLE);

		// new arraylist to store "blocks" as Block objects
		Position back = new Position();

		CvRect rect = cvBoundingRect(cvSeq, 0);
		int x = rect.x();
		int y = rect.y();
		int h = rect.height();
		int w = rect.width();

		// determine what size of elements you want as "blocks"
		if (w < 1000 || h < 1000) {
			back.setX(x+(w/2));
			back.setY(y+(h/2));
		}
 
		return back;
	}
	
	/*
	 * CALCULATE ROBOTS MOVEMENT FROM CURRENT POSITION AND DESIRED POSITION (PORT)
	 */
	public static String calculateRobotMovement(Robot robot, LinkedList<Position> points) {
		
		boolean frontRight = robot.getFront().getX() > robot.getBack().getX();
		boolean dir = robot.getDirection() > robot.getMiddle().calculateSlope(points.get(1));
		boolean left = robot.getMiddle().getX() < points.get(1).getX();
		boolean up = robot.getMiddle().getY() < points.get(1).getY();
		
		// up
		if ((frontRight && !dir && !left && !up) || (!frontRight && dir && !left && !up) 
				|| (frontRight && dir && left && !up)) {
			System.out.println(1);
			return "LEFT";
		}
		
		// down
		if ((frontRight && dir && left && up) || (!frontRight && !dir && left && up) 
				|| (!frontRight && dir && !left && up)) {
			System.out.println(2);
			return "LEFT";
		}
		
		// right - this could be deleted, because the cases are already covered
		if ((frontRight && dir && left && !up) || (frontRight && dir && left && up)) {
			System.out.println(3);
			return "LEFT";
		}
		
		// left
		if ((!frontRight && dir && !left && up) || (!frontRight && dir && !left && !up)) {
			System.out.println(4);
			return "LEFT";
		}
		
		return "RIGHT";
	}
	
    /*
     * CALCULATE THE DIFFERENCE BETWEEN SPEED ON ROBOT WHEELS TO SIMULATE TURNS
     */
    static int calculateRobotSpeed(Robot robot, LinkedList<Position> points) {
    	
    	// slope (m1) for the line of the robot's direction
    	double m1 = robot.getDirection();
    	
    	// slope (m2) for the line of the desired direction
    	double m2 = robot.getMiddle().calculateSlope(points.get(1));
    	double m;
    	
    	if (m2 > m1) {
    		m = m2-m1;
    	} else {
    		m = m1-m2;
    	}
    	
    	// calculate angle between the two lines
    	double angle = Math.atan(m/(1+m1*m2));
    	int speed = 0;
    	
    	// set speed depending on the size of the angle
    	if (angle <= 10) {
    		speed = 25;
    	}
    	if (angle <= 20 && angle > 10) {
    		speed = 50;
    	}
    	if (angle <= 30 && angle > 20) {
    		speed = 75;
    	}
    	if (angle <= 40 && angle > 30) {
    		speed = 100;
    	}
    	if (angle > 50) {
    		speed = 125;
    	}
    	
    	return speed;
    }
}
