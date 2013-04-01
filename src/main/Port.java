package main;

public class Port {
	
	private Block red;
	private Block green;
	private Position in = new Position();
	private Position out = new Position();
	
	public Port(Block red, Block green) {
		this.red = red;
		this.green = green;
		calculateInAndOut();
	}
	
	// calculates the entry (in) and exit (out) points of a port
	private void calculateInAndOut() {
		int vX = (-1*(green.getCenter().getY() - red.getCenter().getY())) / 2;
		int vY = (green.getCenter().getX() - red.getCenter().getX()) / 2;
		int centerX = (red.getCenter().getX() + green.getCenter().getX()) / 2;
		int centerY = (red.getCenter().getY() + green.getCenter().getY()) / 2;
		
		// the in and out should match the intended direction
		if (green.getCenter().getX() < red.getCenter().getX()) {
			in.setX(centerX - vX);
			in.setY(centerY - vY);
			out.setX(centerX + vX);
			out.setY(centerY + vY);
		} else {
			in.setX(centerX - vX);
			in.setY(centerY - vY);
			out.setX(centerX + vX);
			out.setY(centerY + vY);
		}
	}
	
	public Block getRed() {
		return red;
	}
	
	public Block getGreen() {
		return green;
	}
	
	public Position getIn() {
		return in;
	}
	
	public Position getOut() {
		return out;
	}
}
