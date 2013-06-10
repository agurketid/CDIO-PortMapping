package main;

public class Robot {

	private Position front;
	private Position back;
	private Position middle = new Position();
	private double direction;

	public Robot() {
		
	}
	
	public void robotInit(Position front, Position back) {
		this.front = front;
		this.back = back;
		calculateMiddle();
		calculateDirection();
	}
	

	private void calculateDirection() {
		double	dx = front.getX() - back.getX();
		double	dy = front.getY() - back.getY();
		direction = dy/dx;
	}

	private void calculateMiddle() {
		middle.setX((front.getX()+back.getX())/2);
		middle.setY((front.getY()+back.getY())/2);
	}

	public Position getFront() {
		return front;
	}

	public Position getBack() {
		return back;
	}

	public Position getMiddle() {
		return middle;
	}

	public double getDirection() {
		return direction;
	}

}
