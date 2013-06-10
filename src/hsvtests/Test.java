package hsvtests;
import static com.googlecode.javacv.cpp.opencv_highgui.*;
import static com.googlecode.javacv.cpp.opencv_imgproc.*;

import java.awt.image.BufferedImage;
import java.util.Currency;

import com.googlecode.javacv.CanvasFrame;
import com.googlecode.javacv.OpenCVFrameGrabber;
import com.googlecode.javacv.cpp.opencv_core.IplImage;
import static com.googlecode.javacv.cpp.opencv_core.*;


public class Test {
	// color range of red like color
	static int hueLowerR = 160;
	static int hueUpperR = 180;
	static int hueLowerG = 45;
	static int hueUpperG = 85;
	static CanvasFrame canvas = new CanvasFrame("Web Cam");
	static boolean firstrun = true;
	static OpenCVFrameGrabber grabber = new OpenCVFrameGrabber(0);
	//    static IplImage editedImg;
	static IplImage image = null;
	static BufferedImage bufferedImage;
	static CvMemStorage storage;

	public static void main(String[] args) {
		MainFrame frame = new MainFrame();
		canvas.setDefaultCloseOperation(javax.swing.JFrame.EXIT_ON_CLOSE);
		try {
			grabber.start();
		} catch (com.googlecode.javacv.FrameGrabber.Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		while(true){
			storage = CvMemStorage.create();
			convert();
			cvClearMemStorage(storage);
			image = null;
		}
	}

	static IplImage captureFrame() {
		// 0-default camera, 1 - next...so on
		IplImage img = null;
		try {
			img = grabber.grab();
			if (img != null) {
				//cvSaveImage(name, img);
				//                cvSaveImage("capture.jpg",img);
				image = img;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return img;
	}  

	public static void convert() {
		//        IplImage orgImg = cvLoadImage("capture.JPG");
		//    	editedImg = hsvThreshold(image);
		//        cvSaveImage("hsvthreshold.jpg", editedImg);
		try {
//						captureFrame();
			    		image = cvLoadImage("spectrum.bmp");
			Thread.sleep(50);
			displayImg(hsvThreshold(image));
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	static IplImage hsvThreshold(IplImage orgImg) {
		// 8-bit, 3- color =(RGB)
		IplImage imgHSV = cvCreateImage(cvGetSize(orgImg), 8, 3);
		System.out.println(cvGetSize(orgImg));
		cvCvtColor(orgImg, imgHSV, CV_BGR2HSV);
		// 8-bit 1- color = monochrome
		IplImage imgThreshold = cvCreateImage(cvGetSize(orgImg), 8, 1);
		// cvScalar : ( H , S , V, A)
		//        cvInRangeS(imgHSV, cvScalar(hueLowerR, 100, 100, 0), cvScalar(hueUpperR, 255, 255, 0), imgThreshold);
		cvInRangeS(imgHSV, cvScalar(hueLowerG, 12, 67, 0), cvScalar(hueUpperG, 255, 255, 0), imgThreshold);
//		int[] values = MainFrame.getValues();
//		MainFrame.updateValues();
//		cvInRangeS(imgHSV, cvScalar(values[0], values[1], values[2], 0), cvScalar(values[3],values[4], values[5], 0), imgThreshold);
		cvReleaseImage(imgHSV);
		cvSmooth(imgThreshold, imgThreshold, CV_MEDIAN, 13);
		// save
		return imgThreshold;
	}

	static void displayImg(IplImage img){
		canvas.showImage(img);
	}
}