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

	static CvScalar minRed = CV_RGB(100,0,0);
	static CvScalar maxRed = CV_RGB(255,70,130); 
	static CvScalar minGreen = CV_RGB(0,80,0);
	static CvScalar maxGreen = CV_RGB(100,255,100);
	static CvScalar minLightBlue = CV_RGB(0,80,120);
	static CvScalar maxLightBlue = CV_RGB(80,170,190);
	static CvScalar minDarkBlue = CV_RGB(0,0,40);
	static CvScalar maxDarkBlue = CV_RGB(50,60,100);

	public static void main(String[] args) {
		long startTime = System.currentTimeMillis();

		IplImage orgImg = cvLoadImage("capture2.jpg");
		ArrayList<Block> greenBlocks = findBlocks(orgImg, Color.GREEN);
		ArrayList<Block> redBlocks = findBlocks(orgImg, Color.RED);
		redBlocks.remove(redBlocks.size()-1);
		greenBlocks.remove(greenBlocks.size()-1);
		ArrayList<Port> ports = mapPorts(redBlocks, greenBlocks);

		Position robotFront = findRobot(orgImg, "front");
		Position robotBack = findRobot(orgImg, "back");
		Robot robot = new Robot(robotFront, robotBack);

		long middle = System.currentTimeMillis();
		long middleTime = middle - startTime;
		System.out.println("TIME: " + middleTime);


		/*
		 * BEGIN TEST
		 */

		// DRAW ROBOT
		cvRectangle(orgImg, cvPoint(robotFront.getX(), robotFront.getY()), cvPoint(robotFront.getX()+5, robotFront.getY()+5),
				CvScalar.MAGENTA, 1, CV_AA, 0);
		cvRectangle(orgImg, cvPoint(robotBack.getX(), robotBack.getY()), 
				cvPoint(robotBack.getX()+5, robotBack.getY()+5), CvScalar.CYAN, 1, CV_AA, 0);
		System.out.println("BLUE: " + robotFront.toString());
		System.out.println("YELLOW: " + robotBack.toString());
		
		

		System.out.println("Green blocks: " + greenBlocks.size());
		System.out.println("Red blocks : " + redBlocks.size());
		System.out.println("Ports: " + ports.size());

		for (int i = 0; i < ports.size(); i++) {
			// Show green ports
			cvRectangle(orgImg, cvPoint(ports.get(i).getGreen().getCenter().getX(), ports.get(i).getGreen().getCenter().getY()), 
					cvPoint(ports.get(i).getGreen().getCenter().getX()+5, ports.get(i).getGreen().getCenter().getY()+5), 
					CvScalar.BLACK, 1, CV_AA, 0);
			// Show red ports
			cvRectangle(orgImg, cvPoint(ports.get(i).getRed().getCenter().getX(), ports.get(i).getRed().getCenter().getY()), 
					cvPoint(ports.get(i).getRed().getCenter().getX()+5, ports.get(i).getRed().getCenter().getY()+5), 
					CvScalar.BLACK, 1, CV_AA, 0);
			System.out.println("Port: (r,g) " + ports.get(i).getRed().getCenter() + "  ;  " + 
					ports.get(i).getGreen().getCenter() + " - in: " + ports.get(i).getIn() + " - out: " + ports.get(i).getOut());
		}


		// DRAW IN AND OUT PORTS
		for (int i = 0; i < ports.size(); i++) {

			cvRectangle(orgImg, cvPoint(ports.get(i).getIn().getX(), ports.get(i).getIn().getY()), 
					cvPoint(ports.get(i).getIn().getX(), ports.get(i).getIn().getY()), CvScalar.BLUE, 3, CV_AA, 0);
			cvRectangle(orgImg, cvPoint(ports.get(i).getOut().getX(), ports.get(i).getOut().getY()), 
					cvPoint(ports.get(i).getOut().getX(), ports.get(i).getOut().getY()), CvScalar.MAGENTA, 3, CV_AA, 0);
		}

		// DRAW LINE
		Position start = robot.getMiddle();
		//Position start = new Position(200, 200);
		LinkedList<Position> points = mapRoute(ports, start);
		System.out.println(points.size());
		for(int i = 0; i < points.size(); i++) {
			if (i != points.size()-1) {
				CvPoint p1 = new CvPoint(points.get(i).getX(),points.get(i).getY());
				CvPoint p2 = new CvPoint(points.get(i+1).getX(),points.get(i+1).getY());
				cvLine(orgImg, p1, p2, CV_RGB(255,0,210), 1, CV_AA, 0);
				System.out.println(points.get(i) + " ; " + points.get(i+1));
			} else {
				CvPoint p1 = new CvPoint(points.get(i).getX(),points.get(i).getY());
				CvPoint p2 = new CvPoint(points.get(1).getX(),points.get(1).getY());
				cvLine(orgImg, p1, p2, CV_RGB(255,0,210), 1, CV_AA, 0);
				System.out.println(points.get(i) + " ; " + points.get(0));
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
		
		
		System.out.println("Robot direction: " + robot.getDirection());
		System.out.println("Should be: " + robot.getMiddle().calculateSlope(points.get(1)));
//		if(robot.getDirection() < robot.getMiddle().calculateSlope(points.get(1))) {
//			System.out.println("TURN RIGHT");
//		} 
//		if(robot.getDirection() > robot.getMiddle().calculateSlope(points.get(1))) {
//			System.out.println("TURN LEFT");
//		}		
//		if(robot.getDirection() == robot.getMiddle().calculateSlope(points.get(1))) {
//			System.out.println("STRAIGHT AHEAD");
//		}
		System.out.println(calculateRobotMovement(robot, points));

		cvSaveImage("test2.jpg", orgImg);

		// SHOW IMAGE
		CanvasFrame cnvs=new CanvasFrame("CUDACruiser");
		cnvs.setDefaultCloseOperation(javax.swing.JFrame.EXIT_ON_CLOSE);
		cnvs.showImage(orgImg);
		/*
		 * END TEST>
		 */
		long end = System.currentTimeMillis();
		long endTime = end - startTime;
		System.out.println("TIME: " + endTime);
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

			// remove the port (in and out) from the list - it should only appear in the "route list" once
			portsTemp.remove(index);
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
		
//		if ((frontRight && !dir && !left && !up) || (!frontRight && dir && !left && !up) || 
//				(frontRight && dir && left && !up) || (frontRight && dir && left && up) || 
//				(!frontRight && !dir && left && up) || (!frontRight && dir && !left && up) || 
//				(frontRight && !dir && left && !up) || (!frontRight && dir && !left && !up)) {
//			return "LEFT";
//		} else {
//			return "RIGHT";
//		}
	}
	
    static void captureImage() {
        // 0-default camera, 1 - next...so on
        final OpenCVFrameGrabber grabber = new OpenCVFrameGrabber(0);

            try {
            	IplImage img = grabber.grab();
				grabber.start();
				if (img != null) {
					//cvSaveImage(name, img);
					cvSaveImage("capture2.jpg",img);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
    }
}
