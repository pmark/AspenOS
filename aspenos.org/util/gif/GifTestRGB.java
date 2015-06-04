/*
 * sample call:
	java com.hldesign.util.gif.GifTestRGB 255 255 255 dynimg.gif 
 */
package org.aspenos.util.gif;

import java.awt.*;
import java.io.*;
import java.net.*;

// This app will load the image URL given as the first argument, and
// save it as a GIF to the file given as the second argument. Beware
// of not having enough memory!
public class GifTestRGB  
{

    public static void main(String args[]) throws Exception 
	{
		if (args.length < 4) 
		{
			System.out.println("GifTest [red] [green] [blue] " +
					"[filename] [width] [height]");
			return;
		}

		int WIDTH = 50;
		int HEIGHT = 50;
		if (args[4] != null && args[5] != null) {
			WIDTH = Integer.parseInt(args[4]);
			HEIGHT = Integer.parseInt(args[5]);
			
		}

		// Construct arrays
		byte[][] r = new byte[WIDTH][HEIGHT];
		byte[][] g = new byte[WIDTH][HEIGHT];
		byte[][] b = new byte[WIDTH][HEIGHT];

		int red = Integer.parseInt(args[0]);
		int green = Integer.parseInt(args[1]);
		int blue = Integer.parseInt(args[2]);

		int i,j;
		for (i=0; i<WIDTH; i++) {
			for (j=0; j<HEIGHT; j++)
				r[i][j] = (byte)red;
		}

		for (i=0; i<WIDTH; i++) {
			for (j=0; j<HEIGHT; j++)
				g[i][j] = (byte)green;
		}

		for (i=0; i<WIDTH; i++) {
			for (j=0; j<HEIGHT; j++)
				b[i][j] = (byte)blue;
		}

	
		// encode the image as a GIF
		GifEncoder encode = new GifEncoder(r, g, b);
		OutputStream output = new BufferedOutputStream(
			new FileOutputStream(args[3]));
		encode.Write(output);
		
		System.exit(0);
    }
}
