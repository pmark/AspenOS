/*
 * sample call:
	java com.hldesign.util.gif.GifTestRGB 255 255 255 dynimg.gif 
 */
package com.hldesign.util.gif;

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
		if (args.length != 4) 
		{
			System.out.println("GifTest [red] [green] [blue] [filename]");
			return;
		}

		final int WIDTH = 50;
		final int HEIGHT = 50;

		// Construct arrays
		byte[] col = new byte[WIDTH];
		byte[][] r = new byte[HEIGHT][WIDTH];
		byte[][] g = new byte[HEIGHT][WIDTH];
		byte[][] b = new byte[HEIGHT][WIDTH];

		int red = Integer.parseInt(args[0]);
		int green = Integer.parseInt(args[1]);
		int blue = Integer.parseInt(args[2]);

		for (int i=0; i<WIDTH; i++)
			col[i] = (byte)red;
		for (int i=0; i<HEIGHT; i++)
			r[i] = col;

		for (int i=0; i<WIDTH; i++)
			col[i] = (byte)green;
		for (int i=0; i<HEIGHT; i++)
			g[i] = col;

		for (int i=0; i<WIDTH; i++)
			col[i] = (byte)blue;
		for (int i=0; i<HEIGHT; i++)
			b[i] = col;

	
		// encode the image as a GIF
		GifEncoder encode = new GifEncoder(r, g, b);
		OutputStream output = new BufferedOutputStream(
			new FileOutputStream(args[3]));
		encode.Write(output);
		
		System.exit(0);
    }
}
