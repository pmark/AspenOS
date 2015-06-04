package ranab.img;

import java.io.*;
import java.util.*;
import java.awt.*;
import java.awt.image.*;

/**
 * GIF encoder class. It supports transparency, multiple images
 * and animation.
 */
public
class MyGifImage extends MyImage {
	
	private static final String MIME_TYPE = "image/gif";
	
	private BufferedImage mBi = null; 
	private Graphics2D    mGr = null;
	private GifUtil       mGf = null;
	
	
	/**
	 * Constructor
	 */
	public MyGifImage(int width, int height) {
		super(width, height);
		mBi = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_INDEXED);
		mGr = mBi.createGraphics();
		mGf = new GifUtil(mBi);
	}
	
	/**
	 * Get graphics
	 */
	public Graphics2D getGraphics() {
		return mGr;
	}
	
	/**
	 * print the global color table (debug only)
	 */
	public void printColor() {
		int sz = mGf.mGlobalColTable.length;
		for(int i=0; i<sz; i++) {
			System.out.println(i + "> " + mGf.mGlobalColTable[i].getColor());
		}
	}
	
	/**
	 * Set transparency
	 */
	public void setTransparency(Color col) {		
		int rgb[] = new int[3];
		rgb[0] = col.getRed();
		rgb[1] = col.getGreen();
		rgb[2] = col.getBlue();
		
		IndexColorModel cm = (IndexColorModel)mBi.getColorModel();
		int idx = cm.getDataElement(rgb, 0);
		mGf.mGraphicExt.setTransparency((byte)idx);
	}
	
	/**
	 * Reset transparency
	 */
	public void resetTransparency() {
		mGf.mGraphicExt.resetTransparency();
	}
	
	/**
	 * Set delay
	 */
	public void setDelay(int delay) {
		mGf.mGraphicExt.setDelay((short)delay);
	}
	
	/**
	 * Reset delay
	 */
	public void resetDelay() {
		mGf.mGraphicExt.resetDelay();
	}
	
  
  /**
   * set iteration count
   */
	public void setIterationCount(int count) {
    mGf.mApplBlk.setItrCount(count);
  }
   
   
	/**
	 * Update color table
	 */
	public void updateColorTable(Color col) {
		int rgb[] = new int[3];
		rgb[0] = col.getRed();
		rgb[1] = col.getGreen();
		rgb[2] = col.getBlue();

		IndexColorModel cm = (IndexColorModel)mBi.getColorModel();
		int idx = cm.getDataElement(rgb, 0);
		
		mGf.mGlobalColTable[idx].updateColor(col);		
	}
	
	
	/**
	 * Add image
	 */
	public void addImage(MyGifImage gi) {
		mGf.mOtherImages.add(gi.mGf);
	} 
	
		
	/**
	 * Encode GIF image
	 */
	public void encode(OutputStream os) throws IOException {
		mGf.write(os);
	}
	
	
	/**
	 * get mime type
	 */
	public String getMimeType() {
		return MIME_TYPE;
	}
  
}


///////////////////////////////////////////////////////////////////
///////////////////////////////////////////////////////////////////
/**
 * This gif utility class handles 256 colors. So
 * The color table size is 256 and the numbet of
 * bits to reprecest the pixel value is 8.
 */
final class GifUtil {  
   	
   	// GIF header version
   	static final String mstHeader = "GIF89a";
   	
   	// GIF screen descriptor
   	GifScreenDescriptor mScreenDesc;
   	
   	// GIF global color table
   	GifColor mGlobalColTable[];
   	
   	// Application block (optional)
   	GifNsAppEx mApplBlk;
   	
   	// Comment block (optional)
   	GifCommentEx mComment;
   	
   	// Graphic control block (optional)
   	GifGraphicEx mGraphicExt;
   	
   	// GIF image descriptor
   	GifImageDescriptor  mImageDesc;
    
  
    // Other image data for animation or multiple image
    Vector mOtherImages;
    
    // GIF LZW minimum code size
    private static final byte mbyMinCodeSize = 8; // root size
   	
   	// Actual data which has to be compressed 
   	private BufferedImage mImageData;
   	
   	// GIF terminator
   	private static final byte mbyTerminator = (byte)0x3B; //';'
   	
   
   	// temporary variables
   	private GifHash mGifHash; 
   	private byte[] mbyCodeBuffer = new byte[256+3];
   	private short msBitOffset;
   	private short msClearCode;
   	private short msEofCode;
   	private short msCodeSize;
   	private short msMaxCode;
   	private short msFreeCode;
   	
   	
   	/**
   	 * Constructor to set the image size
   	 */
	public GifUtil(BufferedImage data) {
		
		mImageData = data;
		short height = (short)data.getHeight();
		short width = (short)data.getWidth();
		mScreenDesc = new GifScreenDescriptor(height, width);
		mApplBlk  = new GifNsAppEx();
		mComment  = new GifCommentEx();
		mGraphicExt = new GifGraphicEx();
		mImageDesc  = new GifImageDescriptor(height, width);
		mGifHash = new GifHash();
		mOtherImages = new Vector();
		
		// set global color table
		mGlobalColTable = new GifColor[256];
		IndexColorModel cm = (IndexColorModel)mImageData.getColorModel();
		
		for(short s=0; s<mGlobalColTable.length; s++) {
			mGlobalColTable[s] = new GifColor(cm, s);
		}
	}
	
	
	/**
	 * Send data to the <code>OutputStream</code>. It closes 
	 * the <code>OutputStream</code> after sending.
	 */
	public synchronized void write(OutputStream os) 
		throws IOException {
		
		DataOutputStream ds = new DataOutputStream(os);
		
		// send header
		for(int i=0; i<mstHeader.length(); i++)
			ds.writeByte(mstHeader.charAt(i));
		
		// send screen descriptor
		mScreenDesc.write(ds);
		
		// write global color table
		for(int i=0; i<mGlobalColTable.length; i++) {
			mGlobalColTable[i].write(ds);
    }
    
		// netscape block
		mApplBlk.write(ds);
			
		// write comment
		mComment.write(ds);
		
		// write compressed image data
		writeImage(ds);
		
		
		// write other images
		short height = (short)mImageData.getHeight();
		short width = (short)mImageData.getWidth();

		for(int i=0; i<mOtherImages.size(); i++) {
			GifUtil gu = (GifUtil)mOtherImages.elementAt(i);
			short iheight = (short)gu.mImageData.getHeight();
			short iwidth = (short)gu.mImageData.getWidth();
			
			if(iheight > height || iwidth > width) {
				continue;
      }
			gu.writeImage(ds);
		}
		
		// write gif terminator
		ds.writeByte(mbyTerminator);
		
		ds.close();
		ds = null;
	}
	
	
	/**
	 * Compress the image data (2D array) and send it. 
	 * It may throw Exception.
	 */
	private void writeImage(DataOutputStream os) 
		throws IOException {
		
		// write graphic extension
		mGraphicExt.write(os);

		// write image descriptor
		mImageDesc.write(os);
		
		msBitOffset = 0;
		
		// reset temporary variable
		reset();
		
		// write minimum code size
		os.writeByte(mbyMinCodeSize);
		
		// send clear code
		writeCode(os, msClearCode);
		
		// get height and width
		short height = (short)mImageData.getHeight();
		short width = (short)mImageData.getWidth();
		
		// read each byte and compress
		Raster ras = mImageData.getData();
		int data[] = new int[1];
		
		ras.getPixel(0, 0, data);
		short prefixCode = (short)data[0];
		byte suffixChar = (byte)prefixCode;
		
		CodeEntry hashCode = null;
		for(int yidx=0; yidx<height; yidx++) {
			for(int xidx = (yidx==0)? 1 : 0; xidx<width; xidx++) {
				
				ras.getPixel(xidx, yidx, data);			
				suffixChar = (byte)data[0];
				
				// string table search
				int hx = mGifHash.findMatch(prefixCode, suffixChar);
				if(hx < -1)
					throw new IOException("Hashing error");
				
				// match found in the string table
				hashCode = mGifHash.getCodeEntry(hx);
				if(!hashCode.isFree()) {
					prefixCode = hashCode.getCode();
				}
				else {
					writeCode(os, prefixCode);
					int d = msFreeCode;
					
					// table size is within limit
					if(msFreeCode <= GifHash.MAX_CODE) {
						hashCode.set(prefixCode, msFreeCode, suffixChar);		
						msFreeCode++;
					}
					
					if(d == msMaxCode) {
						
						// increase code size
						if(msCodeSize < 12) {
							msCodeSize++;
							msMaxCode = (short)(msMaxCode*2);
						}
						else {
							writeCode(os, msClearCode);
							reset();
						}	
					}
					prefixCode = (short)(suffixChar & 0xff);
				}
			}
		}
		
		writeCode(os, prefixCode);
		writeCode(os, msEofCode);
		
		// flush the code buffer
		if(msBitOffset > 0) {
			flush(os, (msBitOffset+7)/8);
		}
    
		flush(os, 0);
	}
	
	
	/**
	 * write code
	 */
	private void writeCode(DataOutputStream ds, short code) 
		throws IOException {
		
		int temp;
		short byteOffset = (short)(msBitOffset >>> 3); // msBitOffset / 8
		short bitsLeft = (short)(msBitOffset & 0x07);  // msBitOffset % 8
		
		// send the block
		if(byteOffset >= 254) {
			flush(ds, byteOffset);
			mbyCodeBuffer[0] = mbyCodeBuffer[byteOffset];
			msBitOffset = bitsLeft;
			byteOffset = 0;
		}
		
		if(bitsLeft > 0) {
			temp = (code << bitsLeft) | mbyCodeBuffer[byteOffset];
			mbyCodeBuffer[byteOffset] = (byte)temp;
			mbyCodeBuffer[byteOffset+1] = (byte)(temp >> 8);
			mbyCodeBuffer[byteOffset+2] = (byte)(temp >> 16);
		}
		else {
			mbyCodeBuffer[byteOffset] = (byte)code;
			mbyCodeBuffer[byteOffset+1] = (byte)(code >> 8);
		}
		
		msBitOffset += msCodeSize;
	}
	
	
	/**
	 * Flush the code buffer
	 */
	private void flush(DataOutputStream ds, int n)
		throws IOException {
		
		ds.writeByte(n);
		
		for(int i=0; i<n; i++) {
			ds.writeByte(mbyCodeBuffer[i]);
		}
	}
	
	
	/**
	 * Reset temporary variables
	 */
	private void reset() {
		msClearCode = (short)(1 << mbyMinCodeSize); // 256
		msEofCode = (short)(msClearCode + 1);       // 257
		msFreeCode = (short)(msClearCode + 2);      // 258
		msCodeSize = (short)(mbyMinCodeSize + 1);   // 9
		msMaxCode = (short)(1 << msCodeSize);       // 512
		mGifHash.reset();
	}
	
	
	/**
	 * Change LSB and MSB sequence
	 */
	public static short changeSequence(short b) {
		short msb = (short)(b >>> 8);
		short lsb = (short)(b & 0xFF);
		return (short)((lsb << 8) | msb);
	}
	
	
	///////////////////////////////////////////
	// 			   inner classes             //
	///////////////////////////////////////////
	/**
	 * code entry table
	 */
	final class GifHash {
		
	    private static final int BITS = 12;                  //maximum bits/code
	    private static final int MAX_CODE = (1 << BITS) - 1; //maximum code value (4095)
	    private static final int TABLE_SIZE = 5021;          //table size - prime number
	    
	    private CodeEntry mHashTable[];
	    
	    /**
	     * Initialize the array
	     */
	    public GifHash() {
	    	mHashTable = new CodeEntry[TABLE_SIZE];
	    	for(int i=0; i<TABLE_SIZE; i++) {
	    		mHashTable[i] = new CodeEntry();
	    	}
	    }
	    
	    /**
	     * Reset the array
	     */
	    public void reset() {
	    	for(int i=0; i<TABLE_SIZE; i++) {
	    		mHashTable[i].reset();
	    	}
	    }
	    
	    /**
	     * Hashing routine. It tries to find a match for the
	     * prefix + char string in the string table. If found, 
	     * returns the index, else returns the first available index.
	     */
	    public int findMatch(short codePrefix, byte charValue) {
	    	
            // hash function
            int hx = ((charValue << 5) ^ codePrefix) % TABLE_SIZE;
            if(hx < 0) {
                hx += TABLE_SIZE;
            }
            
            int loopCount = 0;
            while(true) {
                
                // check entry
                if(mHashTable[hx].isMatch(codePrefix, charValue)) {
                    return hx;
                }
                
                // increment count and check for excessive looping
                loopCount++;
                if((++loopCount) >= TABLE_SIZE) {
                    return -1;
                }
                
                // rehash
                hx = (hx + loopCount) % TABLE_SIZE;
            }
            
	    }
		
		
		/**
		 * Get code entry object
		 */
		public CodeEntry getCodeEntry(int index) {
			return mHashTable[index];
		}
	
	} 
	
	
	/**
	 * Hash entry class
	 */
	final class CodeEntry {
		
		private short msPriorCode;
		private short msCode;
		private byte  mbyAddedChar;	
		
		/**
		 * Constructor
		 */
		public CodeEntry() {
			msPriorCode = msCode = -1;
			mbyAddedChar = 0;
		}
		
		/**
		 * Is it a free entry?
		 */
		public boolean isFree() {
			return msCode == -1;
		}
		
		/**
		 * Is it free or a match
		 */
		public boolean isMatch(short pcode, byte achar) {
			return (msCode == -1) || 
				   ((msPriorCode == pcode) && (mbyAddedChar == achar));
		}
		
		/**
		 * Reset entry
		 */
		public void reset() {
			msPriorCode = msCode = -1;
			mbyAddedChar = 0;
		}
		
		/**
		 * Set the values
		 */
		public void set(short pcode, short code, byte achar) {
			msPriorCode = pcode;
			msCode = code;
			mbyAddedChar = achar;
		}
		
		/**
		 * Get entry code
		 */
		public short getCode() {
			return msCode;
		}
	} 
	
}


////////////////////////////////////////////
///         GIF extension blocks         ///
////////////////////////////////////////////
/**
 * Abstract base class of all the GIF extension blocks
 */
abstract class GifEx {
	protected final static byte mbyExIntro = (byte)0x21;
	protected byte mbyCtrlLab = (byte)0x00; // unassigned
	protected final static byte  mbyBlkTerm   = 0x00;
	
	public byte getBlockId() {
		return mbyCtrlLab;
	}
	
	abstract void write(DataOutputStream ds) throws IOException;
}


/**
 * Graphic extension
 */
final class GifGraphicEx extends GifEx {
	
	private final static byte mbyBlockSz = (byte)0x04;
	private byte mbyPackFld = (byte)0x00;
	private short msDelayTm = (short)0x00;
	private byte mbyTrIndex = (byte)0x00;
	
	private boolean mbIsTransparent = false;
	private boolean mbIsDelayed = false;
	
	
	/**
	 * Constructor
	 */
	GifGraphicEx() {
		mbyCtrlLab = (byte)0xF9;
	}
	
	/**
	 * Set transparency
	 */
	public void setTransparency(byte idx) {
		mbyPackFld = (byte)(mbyPackFld | 0x01);
		mbyTrIndex = idx;
		mbIsTransparent = true;	
	}
	
	/**
	 * Reset transparency
	 */
	public void resetTransparency() {
		mbyPackFld = (byte)(mbyPackFld & 0xFE);
		mbyTrIndex = 0;
		mbIsTransparent = false;
	}
	
	
	/**
	 * Get transparent index
	 */
	public int getTransparentIndex() {
		return mbyTrIndex & 0xff;
	}
	
	
	/**
	 * Set delay timing
	 */
	public void setDelay(short delay) {
		mbyPackFld = (byte)(mbyPackFld | 0x02);
		msDelayTm  = delay;
		mbIsDelayed = true;
	}
	
	
	/**
	 * Reset delay
	 */
	public void resetDelay() {
		mbyPackFld = (byte)(mbyPackFld & 0xFD);
		msDelayTm = (short)0x00;
		mbIsDelayed = false;
	}
	
	/**
	 * Is delayed
	 */
	public boolean isDelayed() {
		return mbIsDelayed;
	}
	
	/**
	 * Get delay time
	 */
	public int getDelayTime() {
		return msDelayTm & 0xffff;
	}
	
	/**
	 * Write this block
	 */
	public void write(DataOutputStream ds) throws IOException {
		ds.writeByte(mbyExIntro);
		ds.writeByte(mbyCtrlLab);
		ds.writeByte(mbyBlockSz);
		ds.writeByte(mbyPackFld);
		ds.writeShort(GifUtil.changeSequence(msDelayTm));
		ds.writeByte(mbyTrIndex);
		ds.writeByte(mbyBlkTerm);
	}

}


/**
 * Comment extension
 */
final class GifCommentEx extends GifEx {
	
	private final static String mstComment = "Gif encoder by Rana Bhattacharyya (rana_b@yahoo.com).";
	
	/**
	 * Constructor
	 */
	public GifCommentEx() {
		mbyCtrlLab = (byte)0xFE;
	}
	
	/**
	 * Write this block
	 */
	public void write(DataOutputStream ds) throws IOException {
		ds.writeByte(mbyExIntro);
		ds.writeByte(mbyCtrlLab);
		int sz = mstComment.length();
		ds.writeByte(sz);
		
		for(int i=0; i<sz; i++) {
			ds.writeByte(mstComment.charAt(i));	
		}
		ds.writeByte(mbyBlkTerm);
	}
}

/**
 * Gif application extension
 */
abstract class GifAppEx extends GifEx {
	
	protected final static byte mbyBlkSize = (byte)0x0B;
	protected byte mbyAppId[] = new byte[8];
	protected byte mbyAppAuth[] = new byte[3];
	
	
	/**
	 * Constructor
	 */
	public GifAppEx() {
		mbyCtrlLab = (byte)0xFF;
	}
}

/**
 * Gif Netscape application extension
 */
final class GifNsAppEx extends GifAppEx {
	protected byte mbySubBlkSz = 0x03;
	protected byte mbyByte    = 0x01;	
	protected short msItrCnt  = 0x00;
	
	
	/**
	 * Constructor
	 */
	 public GifNsAppEx() {
	 	
	 	String ns = "NETSCAPE";
	 	for(int i=0; i<mbyAppId.length; i++) {
	 		mbyAppId[i] = (byte)ns.charAt(i);
	 	}
	 	
	 	String var = "2.0";
	 	for(int i=0; i<mbyAppAuth.length; i++) {
	 		mbyAppAuth[i] = (byte)var.charAt(i);
	 	}
	 }
	
	
	/**
	 * Set iteration count. Iteration count 0 means infinite
	 */
	public void setItrCount(int itr) {
		msItrCnt = (short)itr;
	}
	
	/**
	 * Get iteration count
	 */
	public int getItrCount() {
		return msItrCnt & 0xffff;
	}
	

	/**
	 * Write the block
	 */
	public void write(DataOutputStream ds) throws IOException {
		ds.writeByte(mbyExIntro);
		ds.writeByte(mbyCtrlLab);
		ds.writeByte(mbyBlkSize);
		
		for(int i=0; i<mbyAppId.length; i++) {
			ds.writeByte(mbyAppId[i]);
		}
		
		for(int i=0; i<mbyAppAuth.length; i++) {
			ds.writeByte(mbyAppAuth[i]);
		}
		
		ds.writeByte(mbySubBlkSz);
		ds.writeByte(mbyByte);
		ds.writeShort(GifUtil.changeSequence(msItrCnt));
		ds.writeByte(mbyBlkTerm);
	}

}


////////////////////////////////////////////////////////////////////////
/**
 * GIF screen descriptor data structure
 */
final class GifScreenDescriptor {
	
	private short msWidth;
	private short msHeight;
	private final static byte mbyPackedField = (byte)0xF7; //1111 0111
	private byte  mbyBackgroundIndex;
	private final static byte mbyAspectRatio = 0;
	
	
	/**
	 * Constructor
	 */
	GifScreenDescriptor(short height, short width) {
		msWidth = width;
		msHeight = height;
		mbyBackgroundIndex = (byte)20;
	}

	/**
	 * write screen descriptor
	 */
	public void write(DataOutputStream ds) throws IOException {
		ds.writeShort(GifUtil.changeSequence(msWidth));
		ds.writeShort(GifUtil.changeSequence(msHeight));
		ds.writeByte(mbyPackedField);
		ds.writeByte(mbyBackgroundIndex);
		ds.writeByte(mbyAspectRatio);
	}
	
	/**
	 * Set background
	 */
	public void setBackground(byte b) {
		mbyBackgroundIndex = b;
	}

		
	/**
	 * Get image height
	 */
	short getHeight() {
		return msHeight;
	}
	
	/**
	 * get image width
	 */
	short getWidth() {
		return msWidth;
	}
}



/**
 * GIF image descriptor data structure
 */
final class GifImageDescriptor {
 	
 	private final static byte mbySeparator = (byte)0x2C;  //','
 	private final static short msLeft = 0;
 	private final static short msTop = 0;
 	private short msWidth;
 	private short msHeight;
 	final static byte mbyPackedField = 0;
 	
 	/**
 	 * Constructor
 	 */
 	GifImageDescriptor(short height, short width) {
 		msWidth = width;
 		msHeight = height;
 	}
 	
 	/**
 	 * Write image descriptor
 	 */
 	public void write(DataOutputStream ds) throws IOException {
 		ds.writeByte(mbySeparator);
 		ds.writeShort(GifUtil.changeSequence(msLeft));
 		ds.writeShort(GifUtil.changeSequence(msTop));
 		ds.writeShort(GifUtil.changeSequence(msWidth));
 		ds.writeShort(GifUtil.changeSequence(msHeight));
 		ds.writeByte(mbyPackedField);
 	}	
}


/////////////////////////////////////////////////////
/**
 * Color data structure.
 */
final class GifColor {
	
	private byte mbyRed;
	private byte mbyGreen;
	private byte mbyBlue;
	
	/**
	 * Constructor
	 */
	GifColor(IndexColorModel cm, int idx) {
		mbyRed   = (byte)cm.getRed(idx);
		mbyGreen = (byte)cm.getGreen(idx);
		mbyBlue  = (byte)cm.getBlue(idx);
	}
	
	/**
	 * Write gif color
	 */
	 public void write(DataOutputStream ds) throws IOException {
	 	ds.writeByte(mbyRed);
	 	ds.writeByte(mbyGreen);
	 	ds.writeByte(mbyBlue);
	 }
	 
	 /**
	  * Update color entry
	  */
	 public void updateColor(Color col) {
	 	mbyRed = (byte)col.getRed();
	 	mbyGreen = (byte)col.getGreen();
	 	mbyBlue = (byte)col.getBlue();
	 }
	 
	 
	 /**
	  * Get color
	  */
	 public Color getColor() {
	 	return new Color(mbyRed & 0x00ff, mbyGreen & 0xff, mbyBlue & 0xff);
	 }
}