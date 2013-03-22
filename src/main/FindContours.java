package main;

import static com.googlecode.javacv.cpp.opencv_core.cvCreateImage;
import static com.googlecode.javacv.cpp.opencv_core.cvGetSize;
import static com.googlecode.javacv.cpp.opencv_core.cvInRangeS;
import static com.googlecode.javacv.cpp.opencv_core.cvReleaseImage;
import static com.googlecode.javacv.cpp.opencv_core.cvScalar;
import static com.googlecode.javacv.cpp.opencv_highgui.cvLoadImage;
import static com.googlecode.javacv.cpp.opencv_highgui.cvSaveImage;
import static com.googlecode.javacv.cpp.opencv_imgproc.CV_BGR2HSV;
import static com.googlecode.javacv.cpp.opencv_imgproc.CV_MEDIAN;
import static com.googlecode.javacv.cpp.opencv_imgproc.cvCvtColor;
import static com.googlecode.javacv.cpp.opencv_imgproc.cvSmooth;
import static com.googlecode.javacv.cpp.opencv_imgproc.cvGetSpatialMoment;
import static com.googlecode.javacv.cpp.opencv_imgproc.cvGetCentralMoment;
import static com.googlecode.javacv.cpp.opencv_imgproc.cvMoments;
import static com.googlecode.javacv.cpp.opencv_core.*;
import static com.googlecode.javacv.cpp.opencv_highgui.*;
import static com.googlecode.javacv.cpp.opencv_imgproc.*;

import com.googlecode.javacpp.Loader;
import com.googlecode.javacv.CanvasFrame;
import com.googlecode.javacv.cpp.opencv_core.CvMemStorage;
import com.googlecode.javacv.cpp.opencv_core.CvPoint;
import com.googlecode.javacv.cpp.opencv_core.CvScalar;
import com.googlecode.javacv.cpp.opencv_core.IplImage;
import java.awt.Dimension;
import java.util.ArrayList;

import main.Block.Color;

import com.googlecode.javacv.cpp.opencv_core.IplImage;

public class FindContours {
	public static void main(String[] args) {
		//		IplImage img_8uc1 = cvLoadImage("threshold.jpg", CV_LOAD_IMAGE_GRAYSCALE);
		//		IplImage img_edge = cvCreateImage( cvGetSize(img_8uc1), 8, 1 );
		//		IplImage img_8uc3 = cvCreateImage( cvGetSize(img_8uc1), 8, 3 );
		//
		//		cvThreshold(img_8uc1, img_edge, 128, 255, CV_THRESH_BINARY);
		//		
		//		CvMemStorage storage = new CvMemStorage();
		//		CvSeq first_contour = new CvSeq();
		//		int nc = cvFindContours(img_edge, storage, first_contour, Loader.sizeof(CvContour.class), CV_RETR_CCOMP, CV_CHAIN_APPROX_SIMPLE);
		//		System.out.println("Total contours detected: " + nc);
		//		for (CvSeq c = first_contour; c != null; c.h_next()) {
		//			cvCvtColor(img_8uc1, img_8uc3, CV_GRAY2BGR);
		//			cvDrawContours(img_8uc3, c, new CvScalar(255,0,0,0), new CvScalar(0,0,255,0), 0, 2, 8);
		//			for (int i=0; i < c.total(); ++i) {
		//				CvPoint p = (CvPoint) cvGetSeqElem(c, i);
		//			}
		//			
		//		}
		//		cvCvtColor( img_8uc1, img_8uc3, CV_GRAY2BGR );
		IplImage src = cvLoadImage("threshold.jpg");//hear path is actual path to image
		IplImage grayImage = IplImage.create(src.width(), src.height(), IPL_DEPTH_8U, 1);
		cvCvtColor(src, grayImage, CV_RGB2GRAY);
		cvThreshold(grayImage, grayImage, 127, 255, CV_THRESH_BINARY);
		CvSeq cvSeq=new CvSeq();
		CvMemStorage memory=CvMemStorage.create();
		cvFindContours(grayImage, memory, cvSeq, Loader.sizeof(CvContour.class), CV_RETR_LIST, CV_CHAIN_APPROX_SIMPLE);
		
		ArrayList<Block> blocks = new ArrayList<Block>();
		while (cvSeq != null && !cvSeq.isNull()) {
			CvRect rect=cvBoundingRect(cvSeq, 0);
			int x=rect.x();
			int y=rect.y();
			int h=rect.height();
			int w=rect.width();
			if (1000 > w || h < 1000) {
				//cvRectangle(src, cvPoint(x, y), cvPoint(x+w, y+h), CvScalar.RED, 1, CV_AA, 0);
				cvRectangle(src, cvPoint(x+(w/2), y+(h/2)), cvPoint(x+(w/2)+5, y+(h/2)+5), CvScalar.BLUE, 1, CV_AA, 0);
				System.out.println("x: " + x + ", y: " + y + ", h: " + h + ", w: " + w);
				blocks.add(new Block(new Position(x,y), new Position(x+w,y), new Position(x,y+h), new Position(x+w,y+h)));
				System.out.println(blocks.get(blocks.size()-1).getCenter());
			}
			
			cvSeq=cvSeq.h_next();
		}
		blocks.get(0).setColor(Color.RED);
		blocks.get(1).setColor(Color.GREEN);
		cvRectangle(src, cvPoint(blocks.get(0).getX().getX(), blocks.get(0).getY().getY()), cvPoint(blocks.get(0).getW().getX(), blocks.get(0).getW().getY()),CvScalar.RED, 1, CV_AA, 0);
		cvRectangle(src, cvPoint(blocks.get(1).getX().getX(), blocks.get(1).getY().getY()), cvPoint(blocks.get(1).getW().getX(), blocks.get(1).getW().getY()),CvScalar.GREEN, 1, CV_AA, 0);
		int x1 = 144;
		int y1 = 978;
		int x2 = 792;
		int y2 = 476;
		cvRectangle(src, cvPoint(x1, y1), cvPoint(x1+5, y1+5), CvScalar.GREEN, 1, CV_AA, 0);
		cvRectangle(src, cvPoint(x2, y2), cvPoint(x2+5, y2+5), CvScalar.GREEN, 1, CV_AA, 0);
		CanvasFrame cnvs=new CanvasFrame("Beam");
		cnvs.setDefaultCloseOperation(javax.swing.JFrame.EXIT_ON_CLOSE);
		cnvs.showImage(src);
	}

}
