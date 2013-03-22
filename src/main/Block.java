package main;

public class Block {
	
	public enum Color {RED, GREEN};
	private Position x;
	private Position y;
	private Position z;
	private Position w;
	private Position center = new Position();
	private Color color;
	
	public Block(Position x, Position y, Position z, Position w, Color color) {
		this.x = x;
		this.y = y;
		this.z = z;
		this.w = w;
		this.color = color;
		center.setX(x.getX()+((y.getX()-x.getX())/2));
		center.setY(x.getY()+((z.getY()-x.getY())/2)); 
//	System.out.println("x: " + x.toString() + ", y: " + y.toString()
//			+ ", z: " + z.toString() + ", w: " + w.toString() + ", color: " + color);
	}
	
	public Block() {
		center = new Position(0,0);
	}
	
	public Position getX() {
		return x;
	}
	
	public Position getY() {
		return y;
	}
	
	public Position getZ() {
		return z;
	}
	
	public Position getW() {
		return w;
	}
	
	public Color getColor() {
		return color;
	}
	
	public void setColor(Color color) {
		this.color = color;
	}
	
	public Position getCenter() {
		return center;
	}
}
