package com.hldesign.util;

import java.io.*;
import java.awt.*;

import Acme.JPM.Encoders.*;

public class GifWriter
{

	public GifWriter()
	{
	}

	public static void save(Image image, String path)
	{
		try
		{
			FileOutputStream out;
			out = new FileOutputStream(path);

			GifEncoder encoder = new GifEncoder(image, out);
			encoder.encode();
		}
		catch (Exception e)
		{
			System.err.println("FaxForm: Image encoding error!");
		}
	}


	public static void main(String args[])
	{
		Graphics g = null;
		Frame frame = null;
		try
		{
			// Create an unshown frame
			frame = new Frame();
			frame.addNotify();

			// Get a graphics region, using the Frame
			Image image = frame.createImage(400,60);
			if (image == null)
				throw new Exception("createImage returned a null image");

			g = image.getGraphics();

			// Draw "Hello World!" to the off-screen graphics context
			g.setFont(new Font("Serif", Font.BOLD, 12));
			g.drawString("hello world!", 10,50);

			GifWriter.save(image, "hello.gif");
		}
		catch (Exception ex)
		{
			System.err.println("Exception: ");
			ex.printStackTrace();
		}
		finally
		{
			// Clean up
			if (g != null) 
				g.dispose();
			if (frame != null)
				frame.removeNotify();
		}
	}

}


