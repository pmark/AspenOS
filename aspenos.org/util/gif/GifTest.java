/*
 *
 */
package org.aspenos.util.gif;

import java.awt.*;
import java.io.*;
import java.net.*;

// This app will load the image URL given as the first argument, and
// save it as a GIF to the file given as the second argument. Beware
// of not having enough memory!
public class GifTest  {
    public static void main(String args[]) throws Exception {
	if (args.length != 2) {
	    System.out.println("GifTest [url to load] [output file]");
	    return;
	}

	// need a component in order to use MediaTracker
	Frame f = new Frame("GifTest");

	// load an image
	Image image = f.getToolkit().getImage(new URL(args[0]));
	
	// wait for the image to entirely load
	MediaTracker tracker = new MediaTracker(f);
	tracker.addImage(image, 0);
	try
	{
	    tracker.waitForID(0);
	}
	catch (InterruptedException e)
	{
	}

	if (tracker.statusID(0, true) != MediaTracker.COMPLETE)
	    throw new AWTException("Could not load: " + args[0] + " " +
				   tracker.statusID(0, true));
	
	// encode the image as a GIF
	GifEncoder encode = new GifEncoder(image);
	OutputStream output = new BufferedOutputStream(
	    new FileOutputStream(args[1]));
	encode.Write(output);
	
	System.exit(0);
    }
}
