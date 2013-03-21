package main;

import static com.googlecode.javacv.cpp.opencv_core.*;
import static com.googlecode.javacv.cpp.opencv_highgui.*;
import static com.googlecode.javacv.cpp.opencv_imgproc.*;
import com.googlecode.javacv.cpp.opencv_core.CvScalar;
import com.googlecode.javacv.cpp.opencv_core.IplImage;

public class ColorDetection {
	//	// color range of red like color
	//	static int hueLowerR = 160;
	//	static int hueUpperR = 180;
	//	public static void main(String[] args) {
	//		IplImage orgImg = cvLoadImage("Shapes2.jpg");
	//		cvSaveImage("hsvthreshold.jpg", hsvThreshold(orgImg));
	//	}
	//	
	//	static IplImage hsvThreshold(IplImage orgImg) {
	//		// 8-bit, 3- color =(RGB)
	//		IplImage imgHSV = cvCreateImage(cvGetSize(orgImg), 8, 3);
	//		System.out.println(cvGetSize(orgImg));
	//		cvCvtColor(orgImg, imgHSV, CV_BGR2HSV);
	//		// 8-bit 1- color = monochrome
	//		IplImage imgThreshold = cvCreateImage(cvGetSize(orgImg), 8, 1);
	//		// cvScalar : ( H , S , V, A)
	//		cvInRangeS(imgHSV, cvScalar(hueLowerR, 100, 100, 0), cvScalar(hueUpperR, 255, 255, 0), imgThreshold);
	//		cvReleaseImage(imgHSV);
	//		cvSmooth(imgThreshold, imgThreshold, CV_MEDIAN, 13);
	//		// save
	//		return imgThreshold;
	//	}

	//color range of red like color
	static CvScalar minRed = CV_RGB(100,0,0);
	static CvScalar maxRed = CV_RGB(255,70,130); 
	static CvScalar minGreen = CV_RGB(0,100,0);
	static CvScalar maxGreen = CV_RGB(100,255,100);
	
	public static void main(String[] args) {
		//read image
		IplImage orgImg = cvLoadImage("capture.jpg");
		//create binary image of original size
		IplImage imgThresholdRed = cvCreateImage(cvGetSize(orgImg), 8, 1);
		IplImage imgThresholdGreen = cvCreateImage(cvGetSize(orgImg), 8, 1);
		


		//apply thresholding
		cvInRangeS(orgImg, minRed, maxRed, imgThresholdRed);
		//smooth filter- median
		cvSmooth(imgThresholdRed, imgThresholdRed, CV_MEDIAN, 13);
		cvThreshold(imgThresholdRed, imgThresholdRed, 100, 255, CV_THRESH_BINARY_INV);
		


		//apply thresholding
		cvInRangeS(orgImg, minGreen, maxGreen, imgThresholdGreen);
		//smooth filter- median
		cvSmooth(imgThresholdGreen, imgThresholdGreen, CV_MEDIAN, 13);
		cvThreshold(imgThresholdGreen, imgThresholdGreen, 100, 255, CV_THRESH_BINARY_INV);

		cvCopy(imgThresholdRed, imgThresholdGreen, imgThresholdGreen);
		//save
		cvSaveImage("threshold.jpg", imgThresholdGreen);
	}
}