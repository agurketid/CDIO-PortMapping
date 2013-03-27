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
	static CvScalar minGreen = CV_RGB(0,100,0);
	static CvScalar maxGreen = CV_RGB(100,255,100);
	
	public static void main(String[] args) {
		IplImage orgImg = cvLoadImage("test.jpg");
		ArrayList<Block> greenBlocks = findBlocks(orgImg, Color.GREEN);
		ArrayList<Block> redBlocks = findBlocks(orgImg, Color.RED);
		redBlocks.remove(redBlocks.size()-1);
		greenBlocks.remove(greenBlocks.size()-1);
		ArrayList<Port> ports = mapPorts(redBlocks, greenBlocks);
		

		/*
		 * BEGIN TEST
		 */
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
		Position start = new Position(150,50);
		LinkedList<Position> points = mapRoute(ports, start);
		System.out.println(points.size());
		for(int i = 0; i < points.size(); i++) {
			if (i != points.size()-1) {
				CvPoint p1 = new CvPoint(points.get(i).getX(),points.get(i).getY());
				CvPoint p2 = new CvPoint(points.get(i+1).getX(),points.get(i+1).getY());
				cvLine(orgImg, p1, p2, CV_RGB(255,255,0), 2, CV_AA, 0);
				System.out.println(points.get(i) + " ; " + points.get(i+1));
			} else {
				CvPoint p1 = new CvPoint(points.get(i).getX(),points.get(i).getY());
				CvPoint p2 = new CvPoint(points.get(1).getX(),points.get(1).getY());
				cvLine(orgImg, p1, p2, CV_RGB(255,255,0), 2, CV_AA, 0);
				System.out.println(points.get(i) + " ; " + points.get(0));
			}
		}
		cvSaveImage("test2.jpg", orgImg);
		


		// SHOW IMAGE
		CanvasFrame cnvs=new CanvasFrame("CUDACruiser");
		cnvs.setDefaultCloseOperation(javax.swing.JFrame.EXIT_ON_CLOSE);
		cnvs.showImage(orgImg);
		/*
		 * END TEST>
		 */
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
}
