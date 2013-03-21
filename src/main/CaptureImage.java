package main;

import java.awt.image.BufferedImage;

import com.googlecode.javacv.CanvasFrame;
import com.googlecode.javacv.OpenCVFrameGrabber;
import com.googlecode.javacv.cpp.opencv_core.IplImage;
import static com.googlecode.javacv.cpp.opencv_highgui.*;

public class CaptureImage {
    
    IplImage image;
    static CanvasFrame canvas = new CanvasFrame("Web Cam");
    static BufferedImage bufferedImage;
    
    public CaptureImage() {
        canvas.setDefaultCloseOperation(javax.swing.JFrame.EXIT_ON_CLOSE);
    }
    
    static void captureFrame() {
        // 0-default camera, 1 - next...so on
        final OpenCVFrameGrabber grabber = new OpenCVFrameGrabber(0);
        try {
            grabber.start();
            IplImage img = grabber.grab();
            if (img != null) {
                //cvSaveImage(name, img);
                cvSaveImage("capture2.jpg",img);
                canvas.showImage(img);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public static void main(String[] args) throws InterruptedException {
        while(true){
            captureFrame(); 
           //Thread.sleep(10);
        }
    }     
}
