package main;

import java.awt.image.BufferedImage;

import com.googlecode.javacv.CanvasFrame;
import com.googlecode.javacv.OpenCVFrameGrabber;
import com.googlecode.javacv.cpp.opencv_core.IplImage;
import static com.googlecode.javacv.cpp.opencv_highgui.*;

public class CaptureImage {
    
    IplImage image;
 //   static CanvasFrame canvas = new CanvasFrame("Web Cam");
    static BufferedImage bufferedImage;
    final OpenCVFrameGrabber grabber = new OpenCVFrameGrabber(0);
    
//    public CaptureImage() {
//        canvas.setDefaultCloseOperation(javax.swing.JFrame.EXIT_ON_CLOSE);
//    }
    
    public void captureFrame() {
        // 0-default camera, 1 - next...so on
        try {
            IplImage img = this.grabber.grab();
            if (img != null) {
                //cvSaveImage(name, img);
                cvSaveImage("capture2.jpg",img);
             //   canvas.showImage(img);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public void run() {
    		startGrabber();
            captureFrame(); 
        }
      
    public void startGrabber(){
    	try {
			this.grabber.start();
		} catch (com.googlecode.javacv.FrameGrabber.Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	
    }
}
