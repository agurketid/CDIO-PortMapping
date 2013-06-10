package main;
import main.Point;

public class Main {
	static Point p0 = new Point(1,1);
	static Point p1 = new Point(3,1);
	static Point p2 = new Point(1,3);
	
	public static void main(String[] args) {
		Point p0 = new Point(1,1);
		System.out.println(angleBetween(p0, p1, p2));
	}
	
	static double angleBetween(Point center, Point current, Point previous) {

		  return Math.toDegrees(Math.atan2(current.x - center.x,current.y - center.y)-
		                        Math.atan2(previous.x- center.x,previous.y- center.y));
	}
	

}


