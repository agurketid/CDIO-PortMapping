package main;

public class Port {
	private Block red;
	private Block green;
	private Position in = new Position();
	private Position out = new Position();
	
	public Port(Block red, Block green) {
		this.red = red;
		this.green = green;
		calcInAndOut();
	}
	
	/**
	 * Calculates the entry (in) and exit (out) points of a port
	 */
	private void calcInAndOut() {
		int vX = (-1*(red.getCenter().getY() - green.getCenter().getY())) / 3;
		int vY = (red.getCenter().getX() - green.getCenter().getX()) / 3;
		int centerX = (red.getCenter().getX() + green.getCenter().getX()) / 2;
		int centerY = (red.getCenter().getY() + green.getCenter().getY()) / 2;
		
		// the in and out should match the intended direction - red is left and green is right
		if (red.getCenter().getY() > green.getCenter().getY()) {
			in.setX(centerX + vX);
			in.setY(centerY + vY);
			out.setX(centerX - vX);
			out.setY(centerY - vY);
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
