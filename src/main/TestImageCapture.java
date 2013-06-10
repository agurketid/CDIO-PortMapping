package main;

import static com.googlecode.javacv.cpp.opencv_highgui.cvSaveImage;

import java.awt.image.BufferedImage;

import com.googlecode.javacv.CanvasFrame;
import com.googlecode.javacv.FrameGrabber.Exception;
import com.googlecode.javacv.OpenCVFrameGrabber;
import com.googlecode.javacv.cpp.opencv_core.IplImage;

public class TestImageCapture {


	static OpenCVFrameGrabber grabber;
	static CanvasFrame canvas;
	static IplImage image;
	
	public static void main(String[] args) throws Exception {
		grabber = new OpenCVFrameGrabber(0);
		canvas  = new CanvasFrame("Web Cam");
		new TestImageCapture();
	}

	public TestImageCapture() throws Exception {
		canvas.setDefaultCloseOperation(javax.swing.JFrame.EXIT_ON_CLOSE);
		grabber.start();
		while(true) {
			try {
				image = grabber.grab();

				if (image != null) {
					canvas.showImage(image);
					cvSaveImage("image.jpg", image);
				}
			} catch (com.googlecode.javacv.FrameGrabber.Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			}

	}
}
