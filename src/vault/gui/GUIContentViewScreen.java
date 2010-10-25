package vault.gui;
import java.util.Vector;

import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Font;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;

import vault.app.ApplicationAPI;
import vault.app.ApplicationAssistant;
import vault.app.ImageAssistant;
import vault.conn.ConnectionHome;
import vault.dto.MediaDTO;
import vault.util.Constants;

class GUIContentViewScreen extends GUIScreen
{
	private Image imgicon;
	private Image [] thumbnails;
	private Image [] photos;
	private int numtotalphotos;
	private int startindex;
	private final int numthumbnailsperscreen = 4;
	private final int numphotostocache = numthumbnailsperscreen*2;
	private Image imgtick;
	
	private int titlebarheight;
	private int bottombarheight;
	private int maxphotoheight;
	private int maxphotowidth;
	private int maxthumbnailheight;
	private int maxthumbnailwidth;
	private int contenttype;
	
	private boolean loadingphotos = false; //freeze controls when loading
	
	public GUIContentViewScreen(VaultMIDLET midlet, int contenttype)
	{
		try
		{	
			if(Constants.debug) System.out.println("GUIPhotoViewScreen.GUIPhotoViewScreen()");
		 	statusprogress = 0;
			statusprogressbar = false;
			firsttime = false;
			secondtime = false;
			this.midlet = midlet;
			this.screenname = Constants.SCREENPHOTOVIEW; 
			this.contenttype = contenttype;
			pos = 0;
			startindex = 0;
			numtotalphotos = (ApplicationAPI.mediadtos==null)?0:ApplicationAPI.mediadtos.size();
			if(contenttype == Constants.CONTENTTYPE_PHOTO)
				imgicon = Image.createImage(Constants.iconphoto);
			else if(contenttype == Constants.CONTENTTYPE_WALLPAPER)
				imgicon = Image.createImage(Constants.iconwallpaper);
			loadPhotos();
			imgtick = Image.createImage(Constants.icontick);
			ApplicationAPI.numpacketstoupload = 0;
			
			setCommandListener(this);
			
			showScreenCommands();
		 }
		 catch (Exception e)
		 {
			if(Constants.debug)  System.out.println("GUIPhotoViewScreen.GUIPhotoViewScreen() " + e.toString());
		 }
		 ApplicationAssistant.gc();
	}
	
	public void showScreenCommands()
	{
		if(Constants.debug) System.out.println("GUIPhotoViewScreen.showScreenCommands()");
		//Select
		//Publish
		//Back	

		//if commands are there and its not just 1 (error/confirmation/progressbar) then no need to remove and readd
		if(this.commands!=null && this.commands.size()!=1)
			return;
		
		this.removeCommands();
		this.commands = new Vector();
		if(numtotalphotos > 0)
		{
			commands.addElement(new Command(Constants.COMMANDLABELSELECT[ApplicationAssistant.language], Command.OK, 2));
			commands.addElement(new Command(Constants.COMMANDLABELPUBLISH[ApplicationAssistant.language], Command.OK, 3));
		}
		commands.addElement(new Command(Constants.COMMANDLABELBACK[ApplicationAssistant.language], Command.BACK, 1));
		this.addCommands();
	}
	
	public void cleanUp()
	{
		if(Constants.debug) System.out.println("GUIPhotoViewScreen.cleanUp()");
		this.screenname = null;
		this.photos = null;
		this.thumbnails = null;
		this.imgtick = null;
		this.imgicon = null;
		ApplicationAssistant.gc();
	}
	 
	private void loadPhotos()
	{
		if(Constants.debug) System.out.println("GUIPhotoViewScreen.loadPhotos()");
		loadingphotos = true;
		/**
		 * THIS IS NOT IN LINE WITH THE CODE FLOW & IS NOT IN KEEPING WITH J2ME STANDARDS EITHER.
		 * WHY AM I STILL DOING IT THIS WAY?
		 * 
		 * IDEAL APPROACH: IDEALLY MIDLET WOULD FORK OUT LOGICTHREAD WHICH IN TURN WOULD CALL 
		 * APPLICATIONAPI.READPHOTOS THIS WOULD READ ALL THE PHOTOS INTO MEDIADTOS AND THEN RETURN TO LOGICTHREAD.
		 * LOGICTHREAD WOULD RETURN CONTROL BACK TO MIDLET WHICH IN TURN WOULD CALL THIS SCREEN WITH THE MEDIADTOS
		 * AS PARAMETER
		 * 
		 * PROBLEM WITH THIS APPROACH: IF THERE ARE A MODERATE 30 ODD PHOTOS ON THE PHONE, EACH AROUND 100KB (MODERN
		 * DAY CAMERAS ARE THAT GOOD WITH RESLUTION), WE LOAD 3MB OF DATA INTO DTOS, WHICH IS UNACCEPTABLE
		 * 
		 * ALTERNATIVE APPROACH: MIDLET WOULD FORK OUT LOGICTHREAD WHICH IN TURN WOULD CALL 
		 * APPLICATIONAPI.READPHOTOS THIS WOULD READ ALL THE PHOTOS INTO MEDIADTOS (WITHOUT THE MEDIA BYTES) 
		 * AND THEN RETURN TO LOGICTHREAD. LOGICTHREAD WOULD RETURN CONTROL BACK TO MIDLET WHICH IN TURN 
		 * WOULD CALL THIS SCREEN. THIS SCREEN LOADS PHOTOS BASED ON THE THE FILENAMES IN THE MEDIA DTOS. ALSO, 
		 * IT READS 5 PHOTOS AT A TIME. 
		 **/
		
		try
		{
			int numphotos = 0;
			if(startindex+numphotostocache > numtotalphotos)
				numphotos = numtotalphotos - startindex;
			else
				numphotos = numphotostocache;
			
			photos = new Image[numphotos];
			thumbnails = new Image[numphotos];
			
			Font f = Font.getFont(Font.FACE_SYSTEM, Font.STYLE_BOLD, Font.SIZE_SMALL);
			titlebarheight = f.getHeight()+4;
			bottombarheight = 45;
			maxphotoheight = screenheight - (titlebarheight+2) - (bottombarheight+2);
			maxphotowidth = screenwidth -2 -2;
			maxthumbnailheight = bottombarheight - 6;
			maxthumbnailwidth = (screenwidth -4)/numthumbnailsperscreen - 2; 
			
			for(int i=0; i<numphotos; i++)
			{
				try
				{
					MediaDTO photo = (MediaDTO) ApplicationAPI.mediadtos.elementAt(i+startindex);
					
					if(Constants.partialdebug)  System.out.println("\nGUIPhotoViewScreen.loadPhotos() ALREADY UPLOADED FLAG = " + photo.getQueuestatus());
					
					
					//read photo bytes from filename
					byte [] imgbytes = ApplicationAPI.getPhotoBytes(photo.getFilename());
					if(Constants.partialdebug)  System.out.println("\n1");
					int [] dimensions = ImageAssistant.getDimensions(imgbytes);
					if(Constants.partialdebug)  System.out.println("\n2");
					int actualimagewidth = dimensions[0];
					if(Constants.partialdebug)  System.out.println("\n3");
					int actualimageheight = dimensions[1];
					if(Constants.partialdebug)  System.out.println("\n4");
					dimensions = null;
					ApplicationAssistant.gc();
					if(Constants.partialdebug)  System.out.println("\n5");
					int photodimensions [] = ImageAssistant.getResizedDimensions(actualimagewidth, actualimageheight, maxphotowidth, maxphotoheight);
					if(Constants.partialdebug)  System.out.println("\n6");
					int thumbnaildimensions [] = ImageAssistant.getResizedDimensions(actualimagewidth, actualimageheight, maxthumbnailwidth, maxthumbnailheight);
					if(Constants.partialdebug)  System.out.println("\n7");
					
					if(thumbnaildimensions[0] == actualimagewidth && thumbnaildimensions[1] == actualimageheight)
	                //resize not required for thumbnail => resize not required for photo
					{	
						if(Constants.debug) System.out.println("#### CASE 1");
						Image imgread = Image.createImage(imgbytes, 0, imgbytes.length);
						imgbytes = null;
						ApplicationAssistant.gc(true);
						thumbnails[i] = imgread;
						photos[i] = imgread;
						imgread = null;
						ApplicationAssistant.gc(true);
					}
					else
					{
						if(photodimensions[0] == actualimagewidth && photodimensions[1] == actualimageheight)
						//resize not required for photo
						{
							if(Constants.debug) System.out.println("#### CASE 2");
							Image imgread = Image.createImage(imgbytes, 0, imgbytes.length);
							photos[i] = imgread;
							imgread = null;
							ApplicationAssistant.gc(true);
							int resizedrgbdata[] = ImageAssistant.getResizedBytes(imgbytes, actualimagewidth, actualimageheight, thumbnaildimensions[0], thumbnaildimensions[1]);
							imgbytes = null;
							ApplicationAssistant.gc(true);
							thumbnails[i] = Image.createRGBImage(resizedrgbdata, thumbnaildimensions[0], thumbnaildimensions[1], false);
							resizedrgbdata = null;
							ApplicationAssistant.gc(true);
						}
						else
						{
							if(Constants.debug) System.out.println("#### CASE 3");
							int resizedrgbdata[] = ImageAssistant.getResizedBytes(imgbytes, actualimagewidth, actualimageheight, photodimensions[0], photodimensions[1]);
							photos[i] = Image.createRGBImage(resizedrgbdata, photodimensions[0], photodimensions[1], false);
							resizedrgbdata = null;
							ApplicationAssistant.gc(true);
							resizedrgbdata = ImageAssistant.getResizedBytes(imgbytes, actualimagewidth, actualimageheight, thumbnaildimensions[0], thumbnaildimensions[1]);
							thumbnails[i] = Image.createRGBImage(resizedrgbdata, thumbnaildimensions[0], thumbnaildimensions[1], false);
							resizedrgbdata = null;
							ApplicationAssistant.gc(true);
							imgbytes = null;
							ApplicationAssistant.gc(true);
						}
					}
				}
				catch (OutOfMemoryError e)
				{
					if(Constants.debug)  System.out.println("GUIPhotoViewScreen.loadPhotos() " + e.toString());
					if(photos[i] == null)
						photos [i] = imgicon;
					if(thumbnails[i] == null)
						thumbnails [i] = imgicon;
				}
				catch (Exception e)
				{				
					if(Constants.debug)  System.out.println("GUIPhotoViewScreen.loadPhotos() " + e.toString());
					if(photos[i] == null)
						photos [i] = imgicon;
					if(thumbnails[i] == null)
						thumbnails [i] = imgicon;
				}	
			}
		}
		catch (Exception e)
		{
			if(Constants.debug)  System.out.println("GUIPhotoViewScreen.loadPhotos() " + e.toString());
		}		
		loadingphotos = false;
	}
		
	public void paint(Graphics g)
	{
		if(Constants.debug) System.out.println("GUIPhotoViewScreen.paint()");
		try
		{
			//theres generally a backlag, issue new display.setcurrent(), but old one just goes for another paint with null
		 	if(screenname == null || !screenname.equals(Constants.SCREENPHOTOVIEW))
		 		return;
		 	
		 	setColorTheme();
			
		 	if(statusprogressbar&&(!firsttime)&&(!secondtime))
			{
				paintStatusDialog(g);
				return;
			}
		 	if(secondtime)
				secondtime = false;
			if(firsttime)
			{
				firsttime = false;
				secondtime = true;
			}
			if(!statusprogressbar)
				showScreenCommands();
			
			//clear canvas
			g.setColor(COLOR_RGBVALUE[ApplicationAssistant.colortheme][COLORCOMPONENT_BG]); // background color			
			g.fillRect(0, 0, getWidth(), getHeight());
			
			//title
			Font f = Font.getFont(Font.FACE_SYSTEM, Font.STYLE_BOLD, Font.SIZE_SMALL);
			g.setFont(f);
			g.setColor(COLOR_RGBVALUE[ApplicationAssistant.colortheme][COLORCOMPONENT_BAR]);			
			g.fillRect(0, 0, getWidth(), titlebarheight);
			g.setColor(COLOR_RGBVALUE[ApplicationAssistant.colortheme][COLORCOMPONENT_BAR_TEXT]);
			String title = "";
			if(contenttype == Constants.CONTENTTYPE_PHOTO)
				title = Constants.MEDIAPHOTOSTITLE[ApplicationAssistant.language];
			if(contenttype == Constants.CONTENTTYPE_WALLPAPER)
				title = Constants.MEDIAWALLPAPERSTITLE[ApplicationAssistant.language];
			if(numtotalphotos != 0)
				title = ((MediaDTO) ApplicationAPI.mediadtos.elementAt(pos)).getTitle();
			g.drawString(title, 3, 4, Graphics.TOP|Graphics.LEFT);
			title = null;
			ApplicationAssistant.gc();
			
			if(numtotalphotos==0)
			{
				f = Font.getFont(Font.FACE_SYSTEM, Font.STYLE_PLAIN, Font.SIZE_SMALL);
				g.setFont(f);
				g.setColor(COLOR_RGBVALUE[ApplicationAssistant.colortheme][COLORCOMPONENT_BG_TEXT]);
				g.drawString(Constants.MEDIAYOUDONTHAVE[ApplicationAssistant.language], 
						screenwidth/2, titlebarheight+2 + maxphotoheight/2 - 2 - f.getHeight(), 
						Graphics.TOP|Graphics.HCENTER);
				String text = "";
				if(contenttype == Constants.CONTENTTYPE_PHOTO)
					title = Constants.MEDIAANYPHOTOS[ApplicationAssistant.language];
				if(contenttype == Constants.CONTENTTYPE_WALLPAPER)
					title = Constants.MEDIAANYWALLPAPERS[ApplicationAssistant.language];
				g.drawString(title, 
						screenwidth/2, titlebarheight+2 + maxphotoheight/2 + 2, 
						Graphics.TOP|Graphics.HCENTER);
			}
			else
			{
				g.drawImage(photos[pos-startindex], 
							 screenwidth/2, 
							 titlebarheight+2 + maxphotoheight/2 , 
							 Graphics.VCENTER | Graphics.HCENTER);
				//timestamp
				f = Font.getFont(Font.FACE_SYSTEM, Font.STYLE_PLAIN, Font.SIZE_SMALL);
				g.setFont(f);
				g.setColor(COLOR_RGBVALUE[ApplicationAssistant.colortheme][COLORCOMPONENT_BG_TEXT]);
				g.drawString(((MediaDTO) ApplicationAPI.mediadtos.elementAt(pos)).getTimestampAsString(), 
						screenwidth-3, titlebarheight + 3, Graphics.TOP|Graphics.RIGHT);
			}
			
			//bottom bar
			g.setColor(COLOR_RGBVALUE[ApplicationAssistant.colortheme][COLORCOMPONENT_BAR]);
			g.fillRect(0, getHeight()-bottombarheight, getWidth(), bottombarheight);
			
			int framestart = startindex;
			while(pos > framestart+numthumbnailsperscreen-1)
				framestart += numthumbnailsperscreen;
			for(int i=framestart, j=0; i<framestart+numthumbnailsperscreen && i<numtotalphotos; i++, j++)
			{
				//draw highlight
		 		if(i==pos)
		 		{
		 			g.setColor(COLOR_RGBVALUE[ApplicationAssistant.colortheme][COLORCOMPONENT_BG]);
					g.fillRect(1+j*(maxthumbnailwidth+1), getHeight()-bottombarheight, maxthumbnailwidth+2, bottombarheight-1);
				}
		 		g.drawImage(thumbnails[i-startindex], 
						 2+j*(maxthumbnailwidth+1)+maxthumbnailwidth/2, 
						 getHeight()-bottombarheight+3+maxthumbnailheight/2, 
						 Graphics.VCENTER | Graphics.HCENTER);
		 		if(((MediaDTO) ApplicationAPI.mediadtos.elementAt(i)).isSelected())
		 		{
		 			//selected
		 			g.drawImage(imgtick, 
							 2+j*(maxthumbnailwidth+2)+2, 
							 getHeight()-bottombarheight+5, 
							 Graphics.TOP | Graphics.LEFT);
		 		}
			}
			
			//Draw status dialog if applicable
			if(dialogerrormsg!=null || dialogconfirmationmsg!=null || statusprogressbar)
				paintStatusDialog(g);
		}
		catch (Exception e)
		{
			if(Constants.debug)  System.out.println("GUIPhotoViewScreen.paint() " + e.toString());
		}
		ApplicationAssistant.gc();
	}

	public void commandAction(Command c, Displayable d)
	{
		if(Constants.debug) System.out.println("GUIPhotoViewScreen.commandAction()");
		dialogconfirmationmsg = null;
		dialogerrormsg = null;
		try
		{
			if(statusprogressbar)
			{
				int act = ApplicationAPI.getConnstatus();
				//this block will execute only when there is a network operation 
				if((act == ConnectionHome.SENDING	|| act == ConnectionHome.RECEIVING)
					&& (c.getLabel().equals(Constants.COMMANDLABELCANCEL[ApplicationAssistant.language])))	
				{
					//Changing the paintflag for the connection to start from the first operation
					statusprogressbar = false;
					dialogprogressbarlabel = null;
					midlet.stopLogicThread();
					repaint();
				}
				return;
			}
			else
			{
				firsttime = true;
				if(c.getLabel().equals(Constants.COMMANDLABELOK[ApplicationAssistant.language]))
				{
					repaint();
					return;
				}
				else if(c.getLabel().equals(Constants.COMMANDLABELSELECT[ApplicationAssistant.language]))
				{
					if(((MediaDTO) ApplicationAPI.mediadtos.elementAt(pos)).isSelected())
						ApplicationAPI.numpacketstoupload -= ((MediaDTO) ApplicationAPI.mediadtos.elementAt(pos)).getStatus().length;
					else
						ApplicationAPI.numpacketstoupload += ((MediaDTO) ApplicationAPI.mediadtos.elementAt(pos)).getStatus().length;
					
					((MediaDTO) ApplicationAPI.mediadtos.elementAt(pos)).toggleSelected();
					repaint();
					return;
				}
				else if(c.getLabel().equals(Constants.COMMANDLABELPUBLISH[ApplicationAssistant.language]))
				{
					if(ApplicationAPI.numpacketstoupload <= 0)
					{
							if(contenttype == Constants.CONTENTTYPE_PHOTO)
								dialogerrormsg = Constants.MESSAGE_PUBLISH_NOPHOTOSELECTED[ApplicationAssistant.language];
							else if(contenttype == Constants.CONTENTTYPE_WALLPAPER)
								dialogerrormsg = Constants.MESSAGE_PUBLISH_NOWALLPAPERSELECTED[ApplicationAssistant.language];
							repaint();
							return;
					}
				}
				midlet.commandAction(c, d);
			}
		}	
		catch(Exception e)
		{
			if(Constants.debug)  System.out.println("GUIPhotoViewScreen.commandAction() " + e.toString());
		}	
	}

	public void keyPressed(int key)
	{
		if(Constants.debug) System.out.println("GUIPhotoViewScreen.keyPressed()");
		dialogconfirmationmsg = null;
		dialogerrormsg = null;
		if(statusprogressbar)
			return;
		try
		{
			if (key == KEY_MIDDLE_1[ApplicationAssistant.keyset_flag] || key == KEY_MIDDLE_2[ApplicationAssistant.keyset_flag] || key == KEY_SELECT[ApplicationAssistant.keyset_flag])
			{
				statusprogress = 0;
				if(Constants.debug)	System.out.println("*********************GUIPhotoViewScreen keypressed ******************** "+Thread.currentThread().toString());
				if(commands!=null && commands.size()>0)
					commandAction((Command) commands.elementAt(0), this);
			}
			else
			{
				if(!loadingphotos)
				{
					if (key == KEY_LEFT[ApplicationAssistant.keyset_flag])
					{
						if(pos > 0)
						{
							--pos;
							if(pos < startindex)
							{
								startindex -= numphotostocache;
								loadPhotos();
							}
							repaint();
						}
						return;
					}
					else if (key == KEY_RIGHT[ApplicationAssistant.keyset_flag])
					{
						if(pos < numtotalphotos-1)
						{
							++pos;
							if(pos >= startindex+numphotostocache)
							{
								startindex += numphotostocache;
								loadPhotos();
							}
							repaint();
						}
						return;
					}
				}
			}
	  }
	  catch(Exception e)
	  {
		if(Constants.debug)  System.out.println("GUIPhotoViewScreen.keyPressed() " + e.toString());
	  }
	}
}