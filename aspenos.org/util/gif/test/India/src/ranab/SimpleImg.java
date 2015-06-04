package ranab;

import java.io.*;
import java.awt.*;
import ranab.img.*;


public class SimpleImg {
  
  public static void main(String args[]) {
  
    if(args.length != 1) {
      System.err.println("Usage : java SimpleImg <filename>");
      return;
    }
    
    MyGifImage img = new MyGifImage(140, 100);
    Graphics2D graphics = img.getGraphics();
    
    // draw background
    graphics.setColor(Color.white);
    graphics.fillRect(0, 0, 140, 100);
    
    graphics.setColor(new Color(70, 140, 240));
    graphics.fillOval(10, 30, 100, 60);
    
    graphics.setColor(Color.black);
    graphics.setFont(new Font("Helvetica", Font.BOLD, 20));
    graphics.drawString("Simple", 50, 50);
    
    // write image
    try {
      FileOutputStream fos = new FileOutputStream(args[0]);
      img.encode(fos);
      fos.close();
    }
    catch(IOException ex) {
      ex.printStackTrace();
    }
  }

}