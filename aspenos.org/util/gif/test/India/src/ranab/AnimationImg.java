package ranab;

import java.io.*;
import java.awt.*;
import ranab.img.*;


public class AnimationImg {
  
  public static void main(String args[]) {
  
    if(args.length != 1) {
      System.err.println("Usage : java AnimationImg <filename>");
      return;
    }
    
    MyGifImage img = getImage(0);
    img.setIterationCount(0); // infinite loop
    
    for(int i=1; i<20; i++) {
      img.addImage(getImage(i));
    }
    
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
  
  private static MyGifImage getImage(int count) {
    MyGifImage img = new MyGifImage(200, 50);
    img.setDelay(25);
    
    String str = "Animation Image";;
    int x = 200-20*count;
    
    Graphics2D graphics = img.getGraphics();
    graphics.setColor(Color.white);
    graphics.fillRect(0, 0, 200, 50);
    graphics.setFont(new Font("Fixed", Font.BOLD|Font.ITALIC, 20));
    
    graphics.setColor(Color.red);
    graphics.drawString(str, x, 26);
    
    graphics.setColor(Color.green);
    graphics.drawString(str, x+1, 27);
    
    graphics.setColor(Color.blue);
    graphics.drawString(str, x+2, 28);
    
    return img;
  }
  
}