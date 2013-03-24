package main;

public class Position {
	
	private int x;
	private int y;
	
	public Position() {
		x = 0;
		y = 0;
	}
	
	public Position(int x, int y) {
		this.x = x;
		this.y = y;
	}
	
	public int calculateDistance(Position p) {
		int dx = this.x - p.getX();
		int dy = this.y - p.getY();
		return (int) Math.sqrt(Math.pow(dx, 2) + Math.pow(dy, 2));
	}
	
	public int getX() {
		return x;
	}

	public int getY() {
		return y;
	}
	
	public void setX(int x) {
		this.x = x;
	}
	
	public void setY(int y) {
		this.y = y;
	}
	
	public String toString() {
		return "(" + x + "," + y + ")";
	}
	
}
