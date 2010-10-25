package vault.app;

import javax.microedition.lcdui.Image;

import vault.util.Constants;

public abstract class ImageAssistant
{
	//returns W x H or throws exception
	public static int [] getDimensions(byte [] jpegbytes) throws Exception
	{
		int bytecursor = 0;
		byte [] tmp = new byte [2];
		tmp[1] = jpegbytes[bytecursor++];
		tmp[0] = jpegbytes[bytecursor++];
		int marker = ApplicationAssistant.toInt(tmp);
		if(marker!=65496) //Start of Image marker = FFD8
			throw new Exception ("Incorrect Jpeg file " + marker);
		
		if(Constants.partialdebug)  System.out.println("\na");
		while(true)
		{
			if(Constants.partialdebug)  System.out.println("\nb");
			tmp = new byte [2];
			tmp[1] = jpegbytes[bytecursor++];
			tmp[0] = jpegbytes[bytecursor++];
			marker = ApplicationAssistant.toInt(tmp);

			tmp = new byte [2];
			tmp[1] = jpegbytes[bytecursor++];
			tmp[0] = jpegbytes[bytecursor++];
			int length = ApplicationAssistant.toInt(tmp) - 2;
			if(length <= 0)
				throw new Exception ("Incorrect Jpeg Header");
			if(marker==65472 || marker==65473 || marker==65474 || marker==65475) //Start of Frame marker = FFC0 or FFC1 or FFC2 or FFC3
			{
				int [] dimensions = new int [2];
				//at bytecursor there is precision, followed by 2 bytes of Y and 2 bytes of X
				tmp = new byte [2];
				tmp[1] = jpegbytes[bytecursor+1];
				tmp[0] = jpegbytes[bytecursor+2];
				dimensions[1] = ApplicationAssistant.toInt(tmp);
				tmp = new byte [2];
				tmp[1] = jpegbytes[bytecursor+3];
				tmp[0] = jpegbytes[bytecursor+4];
				dimensions[0] = ApplicationAssistant.toInt(tmp);
				jpegbytes = null;
				ApplicationAssistant.gc();
				return dimensions;
			}
			bytecursor += length;
		}
	}
	
	public static int [] getResizedDimensions(int actualimagewidth, int actualimageheight, int maxphotowidth, int maxphotoheight) throws Exception
	{
		int photowidth = maxphotowidth;
		int photoheight = maxphotoheight;
		
		if(Constants.debug) 
		{
			System.out.println("actual width : " + actualimagewidth);
			System.out.println("actual height : " + actualimageheight);
			System.out.println("max width : " + maxphotowidth);
			System.out.println("max height : " + maxphotoheight);
		}
		if((actualimageheight*1.0)/photoheight > (actualimagewidth*1.0)/photowidth)
		{
			if(Constants.debug) System.out.println("photo pivot is height");
			if(actualimageheight < photoheight)
				photoheight = actualimageheight;
			photowidth = (photoheight*actualimagewidth)/actualimageheight;
		}
		else
		{
			if(Constants.debug) System.out.println("photo pivot is width");
			if(actualimagewidth < photowidth)
				photowidth = actualimagewidth;
			photoheight = (photowidth*actualimageheight)/actualimagewidth;
		}
		if(Constants.debug)
		{
			System.out.println("resized photo width : " + photowidth);
			System.out.println("resized photo height : " + photoheight);
		}
		return new int [] {photowidth, photoheight};
	}
	
	public static int [] getResizedBytes(byte [] originalimagebytes, int actualwidth, int actualheight, int resizedwidth, int resizedheight) throws Exception
	{
		//--actualwidth;
		if(Constants.debug) System.out.println("getResizedBytes() start");
		Image originalimage = Image.createImage(originalimagebytes, 0, originalimagebytes.length);
		if(Constants.debug) System.out.println("1");
		int out[] = new int[resizedwidth*resizedheight];
		if(Constants.debug) System.out.println("2");
		int num_scanlines_that_fit_in_heap = 25000/actualwidth; //restrict this temp buffer to 100K (25K x 4 bytes)
		if(Constants.debug) System.out.println("num_scanlines_that_fit_in_heap: " + num_scanlines_that_fit_in_heap);
		int max_resized_line_num_already_covered = -1;
		for(int linenum = 0; linenum<actualheight; linenum+=num_scanlines_that_fit_in_heap)
		{
			if(Constants.debug) System.out.println("linenum: " + linenum);
			int height_of_window = num_scanlines_that_fit_in_heap;
			if(linenum + height_of_window > actualheight)
				height_of_window = actualheight - linenum;
			if(Constants.debug) System.out.println("height_of_window: " + height_of_window);
			int [] originalrgbdata = new int [actualwidth*height_of_window];
			originalimage.getRGB(originalrgbdata, 0, actualwidth, 0, linenum, actualwidth, height_of_window);
			if(Constants.debug) System.out.println("filling resized image from line " + (linenum*resizedheight)/actualheight + " to line " + ((linenum+height_of_window)*resizedheight)/actualheight);
			for (int yy = (linenum*resizedheight)/actualheight; yy <= ((linenum+height_of_window)*resizedheight)/actualheight && yy < resizedheight; yy++) 
			{
				if(yy<=max_resized_line_num_already_covered)
					continue;
				max_resized_line_num_already_covered = yy;
				int dy = (yy * actualheight)/resizedheight;
				try
				{
					for (int xx = 0; xx < resizedwidth; xx++) 
					{
						int dx = xx * actualwidth / resizedwidth;
						if((resizedwidth*yy)+xx<out.length && (actualwidth*(dy-linenum))+dx<originalrgbdata.length)
							out[(resizedwidth*yy)+xx]=originalrgbdata[(actualwidth*(dy-linenum))+dx];
					}
				}
				catch(Exception e)
				{
					if(Constants.debug) System.out.println("ImageAssistant.getResizedBytes() " + e.toString());
				}
			}
			originalrgbdata = null;
			ApplicationAssistant.gc(true);
		}
		if(Constants.debug) System.out.println("getResizedBytes() end");
		return out;
	}
	
	/*private int [] resizeBytes(byte [] originalimagebytes, int actualwidth, int actualheight, int resizedwidth, int resizedheight)
	{
		Image originalimage = Image.createImage(originalimagebytes, 0, originalimagebytes.length);
		int out[] = new int[resizedwidth*resizedheight];
		
		int num_scanlines_that_fit_in_heap = 150000/actualwidth; //restrict this temp buffer to 150K
		for(int linenum = 0; linenum<actualheight; linenum+=num_scanlines_that_fit_in_heap)
		{
			if(Constants.debug) System.out.println("linenum: " + linenum);
			int height_of_window = num_scanlines_that_fit_in_heap;
			if(linenum + height_of_window > actualheight)
				height_of_window = actualheight - linenum;
			if(Constants.debug) System.out.println("height_of_window: " + height_of_window);
			int [] originalrgbdata = new int [actualwidth*height_of_window];
			originalimage.getRGB(originalrgbdata, 0, actualwidth, 0, linenum, actualwidth, height_of_window);
			
			double heightratio = actualheight/resizedheight;
			if(Constants.debug) System.out.println("heightratio: " + heightratio);
			if(Constants.debug) System.out.println("filling resized image from line " + linenum/heightratio + " to line " + (linenum+height_of_window)/heightratio);
			for (int yy = (int) (linenum/heightratio); yy < (linenum+height_of_window)/heightratio && yy < resizedheight; yy++) 
			{
				double dy = yy * heightratio;
				for (int xx = 0; xx < resizedwidth; xx++) 
				{
					int dx = xx * actualwidth / resizedwidth;
					out[(resizedwidth*yy)+xx]=originalrgbdata[(int)(actualwidth*(dy-linenum))+dx];
				}
			}
			
			for (int yy = 0; yy < resizedheight; yy++) 
			{
				int dy = yy * actualheight / resizedheight;
				for (int xx = 0; xx < resizedwidth; xx++) 
				{
					int dx = xx * actualwidth / resizedwidth;
					out[(resizedwidth*yy)+xx]=originalrgbdata[(actualwidth*dy)+dx];
				}
			}
		}
		return out;
	}*/
}

	

