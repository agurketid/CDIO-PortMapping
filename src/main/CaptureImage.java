package main;

import java.awt.image.BufferedImage;
import static com.googlecode.javacv.cpp.opencv_imgproc.*;

import com.googlecode.javacv.cpp.opencv_imgproc.*;

import com.googlecode.javacv.CanvasFrame;
import com.googlecode.javacv.OpenCVFrameGrabber;
import com.googlecode.javacv.cpp.opencv_core.CvSize;
import com.googlecode.javacv.cpp.opencv_core.IplImage;

import static com.googlecode.javacv.cpp.opencv_imgproc.cvCvtColor; 
import static com.googlecode.javacv.cpp.opencv_imgproc.CV_BGR2HSV;
import static com.googlecode.javacv.cpp.opencv_imgproc.CV_RGB2HSV;

import static com.googlecode.javacv.cpp.opencv_core.*;
import static com.googlecode.javacv.cpp.opencv_highgui.*;

public class CaptureImage {
    
	
   //static IplImage image; //, rimg, imgHSV, imgThreshed, threshed;
 

   // static CanvasFrame canvas = new CanvasFrame("Web Cam");
    static BufferedImage bufferedImage;
    final OpenCVFrameGrabber grabber = new OpenCVFrameGrabber(0);
    static IplImage image; 
    
    public CaptureImage() {
    	
       // canvas.setDefaultCloseOperation(javax.swing.JFrame.EXIT_ON_CLOSE);
    }
    
    public void captureFrame() {
        // 0-default camera, 1 - next...so on

        try {
            image = this.grabber.grab();
        	//image = cvLoadImage("testmap.jpg");
            if (image != null) {
            	//imgHSV = cvCreateImage(cvGetSize(image), 8, 3);
            	
            	
//            	rimg = cvCreateImage(cvSize(800,600), 8, 3);
//            	imgHSV = cvCreateImage(cvGetSize(rimg), 8, 3);
//            	imgThreshed = cvCreateImage(cvGetSize(rimg), 8, 1);
//            	cvResize(img, rimg, CV_INTER_LINEAR);
//            	threshed = getThresholdedImage(rimg, imgHSV, imgThreshed);

            	cvSaveImage("image.jpg",image);
             //  canvas.showImage(imgHSV);
            }
        } catch (Exception e) {
            e.printStackTrace(); 
        }
    	
    }
    
    public void run() {
    		startGrabber();
            captureFrame(); 
        }
//    public static void main(String[] args) {
//    	startGrabber();
//    	captureFrame();
//    }
      
    public void startGrabber(){
    	try {
			this.grabber.start();
		} catch (com.googlecode.javacv.FrameGrabber.Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	
    }
    
//	public IplImage getThresholdedImasge(IplImage img, IplImage imgHSV,
//			IplImage imgThreshed) {
//		cvCvtColor(img, imgHSV, CV_BGR2HSV);
////		cvInRangeS(imgHSV, cvScalar(b_min, g_min, r_min, 0),
////				cvScalar(b_max, g_max, r_max, 0), imgThreshed);
//		cvInRangeS(imgHSV, cvScalar(b_min, g_min, r_min, 0),
//				cvScalar(b_max, g_max, r_max, 0), imgThreshed);
//		cvSmooth(imgThreshed, imgThreshed, CV_MEDIAN, 1);
//		imgHSV = null;
//		img = null;
//		return imgThreshed;
//	}
}
