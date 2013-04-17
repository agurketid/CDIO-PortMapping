package gui;

import java.util.ArrayList;
import java.util.LinkedList;

import com.googlecode.javacv.cpp.opencv_core.CvPoint;
import com.googlecode.javacv.cpp.opencv_core.IplImage;

import main.ImageScanner;
import main.Port;
import main.Position;

public class DrawData {
	private IplImage image;
	private int robotFront_x;
	private int robotFront_y;
	private int robotBack_x;
	private int robotBack_y;
	private ArrayList<Port> ports;
	private LinkedList<Position> points;
	private Position start;
	private double direction;

	public DrawData() {
		this.image = ImageScanner.getImage();
		this.ports = ImageScanner.getPorts();
		this.points = ImageScanner.getPoints();
		this.robotFront_x = (ImageScanner.getRobotFront()).getX();
		this.robotFront_y = (ImageScanner.getRobotFront()).getY();
		this.robotBack_x = (ImageScanner.getRobotBack()).getX();
		this.robotBack_y = (ImageScanner.getRobotBack()).getY();
		this.start = ImageScanner.getStart();
		this.direction = ImageScanner.getDirection();
		
		//Run drawing methods.
		drawRobot();
		drawBlocks();
		drawLine();
		cvSaveImage("FrameImage.jpg", image);
	}

	public void drawRobot() {
		cvRectangle(image, cvPoint(robotFront_x, robotFront_y),
				cvPoint(robotFront_x + 5, robotFront_y + 5), CvScalar.MAGENTA,
				1, CV_AA, 0);
		cvRectangle(image, cvPoint(robotBack_x, robotBack_y),
				cvPoint(robotBack_x + 5, robotBack_y + 5), CvScalar.CYAN, 1,
				CV_AA, 0);
	}

	public void drawBlocks() {
		for (int i = 0; i < ports.size(); i++) {
			// Show green ports
			cvRectangle(
					image,
					cvPoint(ports.get(i).getGreen().getCenter().getX(), ports
							.get(i).getGreen().getCenter().getY()),
					cvPoint(ports.get(i).getGreen().getCenter().getX() + 5,
							ports.get(i).getGreen().getCenter().getY() + 5),
					CvScalar.BLACK, 1, CV_AA, 0);
			// Show red ports
			cvRectangle(
					image,
					cvPoint(ports.get(i).getRed().getCenter().getX(), ports
							.get(i).getRed().getCenter().getY()),
					cvPoint(ports.get(i).getRed().getCenter().getX() + 5, ports
							.get(i).getRed().getCenter().getY() + 5),
					CvScalar.BLACK, 1, CV_AA, 0);
			System.out
					.println("Port: (r,g) " + ports.get(i).getRed().getCenter()
							+ " ; " + ports.get(i).getGreen().getCenter()
							+ " - in: " + ports.get(i).getIn() + " - out: "
							+ ports.get(i).getOut());
		}
		for (int i = 0; i < ports.size(); i++) {

			cvRectangle(
					image,
					cvPoint(ports.get(i).getIn().getX(), ports.get(i).getIn()
							.getY()),
					cvPoint(ports.get(i).getIn().getX(), ports.get(i).getIn()
							.getY()), CvScalar.BLUE, 3, CV_AA, 0);
			cvRectangle(
					image,
					cvPoint(ports.get(i).getOut().getX(), ports.get(i).getOut()
							.getY()),
					cvPoint(ports.get(i).getOut().getX(), ports.get(i).getOut()
							.getY()), CvScalar.MAGENTA, 3, CV_AA, 0);
		}
	}

	public void drawLine() {
		for (int i = 0; i < points.size(); i++) {
			if (i != points.size() - 1) {
				CvPoint p1 = new CvPoint(points.get(i).getX(), points.get(i)
						.getY());
				CvPoint p2 = new CvPoint(points.get(i + 1).getX(), points.get(
						i + 1).getY());
				cvLine(image, p1, p2, CV_RGB(255, 0, 210), 1, CV_AA, 0);
				System.out.println(points.get(i) + " ; " + points.get(i + 1));
			} else {
				CvPoint p1 = new CvPoint(points.get(i).getX(), points.get(i)
						.getY());
				CvPoint p2 = new CvPoint(points.get(1).getX(), points.get(1)
						.getY());
				cvLine(image, p1, p2, CV_RGB(255, 0, 210), 1, CV_AA, 0);
				System.out.println(points.get(i) + " ; " + points.get(0));
			}
		}
		
		CvPoint r1 = new CvPoint(start.getX(), start.getY());
		CvPoint r2;
		if (robotFront_x < robotBack_x) {
		r2 = new CvPoint(start.getX()-100, (int) (start.getY()-(100*direction)));
		} else {
		r2 = new CvPoint(start.getX()+100, (int) (start.getY()+(100*direction)));
		}
		cvLine(image, r1, r2, CV_RGB(255,0,0), 1, CV_AA, 0);
	}

}
