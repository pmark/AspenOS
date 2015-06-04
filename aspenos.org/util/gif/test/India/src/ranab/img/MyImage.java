package ranab.img;

import java.io.OutputStream;
import java.awt.Graphics2D;

/**
 * This is the abstract base class of all the image
 * classes. All the image classes have to extend this class.
 */
public 
abstract class MyImage {
	
	protected int miHeight;
	protected int miWidth;
	
	/**
	 * Constructor
	 */
	public MyImage(int width, int height) {
		miWidth = width;
		miHeight = height;	
	}
	
	
	/**
	 * get image height
	 */
	public int getHeight() {
		return miHeight;	
	}
	
	
	/**
	 * get image height
	 */
	public int getWidth() {
		return miWidth;	
	}

		
	/**
	 * Get graphics to draw
	 */
	public abstract Graphics2D getGraphics();	
	
	/**
	 * Encode function
	 */
	public abstract void encode(OutputStream os) throws Exception; 

	
	/**
	 * Get mime type
	 */
	public abstract String getMimeType();
}