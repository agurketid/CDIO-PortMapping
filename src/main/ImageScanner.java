package main;

import static com.googlecode.javacv.cpp.opencv_core.CV_AA;
import static com.googlecode.javacv.cpp.opencv_core.CV_RGB;
import static com.googlecode.javacv.cpp.opencv_core.cvCreateImage;
import static com.googlecode.javacv.cpp.opencv_core.cvGetSize;
import static com.googlecode.javacv.cpp.opencv_core.cvInRangeS;
import static com.googlecode.javacv.cpp.opencv_core.cvPoint;
import static com.googlecode.javacv.cpp.opencv_core.cvRectangle;
import static com.googlecode.javacv.cpp.opencv_highgui.cvLoadImage;
import static com.googlecode.javacv.cpp.opencv_imgproc.CV_CHAIN_APPROX_SIMPLE;
import static com.googlecode.javacv.cpp.opencv_imgproc.CV_MEDIAN;
import static com.googlecode.javacv.cpp.opencv_imgproc.CV_RETR_LIST;
import static com.googlecode.javacv.cpp.opencv_imgproc.CV_THRESH_BINARY_INV;
import static com.googlecode.javacv.cpp.opencv_imgproc.cvBoundingRect;
import static com.googlecode.javacv.cpp.opencv_imgproc.cvFindContours;
import static com.googlecode.javacv.cpp.opencv_imgproc.cvSmooth;
import static com.googlecode.javacv.cpp.opencv_imgproc.cvThreshold;
import java.util.ArrayList;
import main.Block.Color;
import com.googlecode.javacpp.Loader;
import com.googlecode.javacv.CanvasFrame;
import com.googlecode.javacv.cpp.opencv_core.CvContour;
import com.googlecode.javacv.cpp.opencv_core.CvMemStorage;
import com.googlecode.javacv.cpp.opencv_core.CvRect;
import com.googlecode.javacv.cpp.opencv_core.CvScalar;
import com.googlecode.javacv.cpp.opencv_core.CvSeq;
import com.googlecode.javacv.cpp.opencv_core.IplImage;

public class ImageScanner {
	static CvScalar minRed = CV_RGB(100,0,0);
	static CvScalar maxRed = CV_RGB(255,70,130); 
	static CvScalar minGreen = CV_RGB(0,100,0);
	static CvScalar maxGreen = CV_RGB(100,255,100);
	
	public static void main(String[] args) {
		IplImage orgImg = cvLoadImage("capture.jpg");
		ArrayList<Block> redBlocks = findBlocks(orgImg, Color.RED);
		ArrayList<Block> greenBlocks = findBlocks(orgImg, Color.GREEN);
		ArrayList<Port> ports = findPorts(redBlocks, greenBlocks);
		
		
		// Show red ports
		System.out.println("Red: " + redBlocks.get(0).getCenter() + "  ;  " + redBlocks.get(1).getCenter());
		cvRectangle(orgImg, cvPoint(redBlocks.get(0).getCenter().getX(), redBlocks.get(0).getCenter().getY()), 
				cvPoint(redBlocks.get(0).getCenter().getX()+5, redBlocks.get(0).getCenter().getY()+5), 
				CvScalar.RED, 1, CV_AA, 0);
		cvRectangle(orgImg, cvPoint(redBlocks.get(1).getCenter().getX(), redBlocks.get(1).getCenter().getY()), 
				cvPoint(redBlocks.get(1).getCenter().getX()+5, redBlocks.get(1).getCenter().getY()+5), 
				CvScalar.RED, 1, CV_AA, 0);
		
		// Show green ports
		System.out.println("Green: " + greenBlocks.get(0).getCenter() + "  ;  " + greenBlocks.get(1).getCenter());
		cvRectangle(orgImg, cvPoint(greenBlocks.get(0).getCenter().getX(), greenBlocks.get(0).getCenter().getY()), 
				cvPoint(greenBlocks.get(0).getCenter().getX()+5, greenBlocks.get(0).getCenter().getY()+5), 
				CvScalar.GREEN, 1, CV_AA, 0);
		cvRectangle(orgImg, cvPoint(greenBlocks.get(1).getCenter().getX(), greenBlocks.get(1).getCenter().getY()), 
				cvPoint(greenBlocks.get(1).getCenter().getX()+5, greenBlocks.get(1).getCenter().getY()+5),
				CvScalar.GREEN, 1, CV_AA, 0);
		
		System.out.println("Port 1, in: " + ports.get(0).getIn().getX() + ", " + ports.get(0).getIn().getY());
		System.out.println("Port 2, in: " + ports.get(1).getIn().getX() + ", " + ports.get(1).getIn().getY());
		System.out.println("Port 1: " + ports.get(0).getRed().getCenter().getX() + ", " + 
				ports.get(0).getGreen().getCenter().getX() );
		System.out.println("Port 2: " + ports.get(1).getRed().getCenter().getX() + " , " + 
				ports.get(1).getGreen().getCenter().getX());
		
		// Show ins and outs
		cvRectangle(orgImg, cvPoint(ports.get(0).getIn().getX(), ports.get(0).getIn().getY()), 
				cvPoint(ports.get(0).getIn().getX(), ports.get(0).getIn().getY()), CvScalar.BLUE, 3, CV_AA, 0);
		cvRectangle(orgImg, cvPoint(ports.get(1).getIn().getX(), ports.get(1).getIn().getY()), 
				cvPoint(ports.get(1).getIn().getX(), ports.get(1).getIn().getY()), CvScalar.BLUE, 3, CV_AA, 0);
		cvRectangle(orgImg, cvPoint(ports.get(0).getOut().getX(), ports.get(0).getOut().getY()), 
				cvPoint(ports.get(0).getOut().getX(), ports.get(0).getOut().getY()), CvScalar.MAGENTA, 3, CV_AA, 0);
		cvRectangle(orgImg, cvPoint(ports.get(1).getOut().getX(), ports.get(1).getOut().getY()), 
				cvPoint(ports.get(1).getOut().getX(), ports.get(1).getOut().getY()), CvScalar.MAGENTA, 3, CV_AA, 0);
		
		// Show picture
		CanvasFrame cnvs=new CanvasFrame("Beam");
		cnvs.setDefaultCloseOperation(javax.swing.JFrame.EXIT_ON_CLOSE);
		cnvs.showImage(orgImg);
	}
	
	public static ArrayList<Block> findBlocks(IplImage orgImage, Color color) {
		IplImage imgThreshold = cvCreateImage(cvGetSize(orgImage), 8, 1);
		if (color == Color.RED) {
			cvInRangeS(orgImage, minRed, maxRed, imgThreshold);
		} else {
			cvInRangeS(orgImage, minGreen, maxGreen, imgThreshold);
		}
		cvSmooth(imgThreshold, imgThreshold, CV_MEDIAN, 13);
		cvThreshold(imgThreshold, imgThreshold, 100, 255, CV_THRESH_BINARY_INV);
		CvSeq cvSeq = new CvSeq();
		CvMemStorage memory = CvMemStorage.create();
		cvFindContours(imgThreshold, memory, cvSeq, Loader.sizeof(CvContour.class), CV_RETR_LIST, 
				CV_CHAIN_APPROX_SIMPLE);
		
		ArrayList<Block> blocks = new ArrayList<Block>();
		while (cvSeq != null && !cvSeq.isNull()) {
			CvRect rect=cvBoundingRect(cvSeq, 0);
			int x=rect.x();
			int y=rect.y();
			int h=rect.height();
			int w=rect.width();
			if (w < 1000 || h < 1000) {
				blocks.add(new Block(new Position(x,y), new Position(x+w,y), new Position(x,y+h), new Position(x+w,y+h)));
			}
			cvSeq=cvSeq.h_next();
		}
		return blocks;
	}
	
	public static ArrayList<Port> findPorts(ArrayList<Block> redBlocks, ArrayList<Block> greenBlocks) {
		ArrayList<Port> ports = new ArrayList<Port>();
		for(Block red : redBlocks) {
			for (Block green : greenBlocks) {
				if (Math.abs(red.getCenter().getX()-green.getCenter().getX()) < 500 && 
						Math.abs(red.getCenter().getY()-green.getCenter().getY()) < 500) {
					ports.add(new Port(red, green));
				}
			}
		}
		return ports;
	}	
}
