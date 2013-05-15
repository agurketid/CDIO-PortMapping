package test;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import org.monte.media.image.*;

import javax.imageio.ImageIO;

import org.monte.media.image.WhiteBalance;

public class ConvertIMG {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		BufferedImage img = null;
		BufferedImage manipulatedImg = null;
		try {
		    img = ImageIO.read(new File("capture2.jpg"));
		    manipulatedImg = whiteBalanceGreyworld(img);
		} catch (IOException e) {
		}
		
	}

}
