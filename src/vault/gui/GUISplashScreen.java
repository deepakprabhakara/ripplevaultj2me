package vault.gui;
import java.io.InputStream;
import java.util.Vector;

import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Font;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;

import vault.app.ApplicationAssistant;
import vault.app.ImageAssistant;
import vault.util.Constants;

class GUISplashScreen extends GUIScreen
{
	private Image iconlogo = null;
//	private byte [] originalbytes = null;
	private int originalheight;
	private int originalwidth;
	public GUISplashScreen(VaultMIDLET midlet)
	{
		try
		{	
			if(Constants.debug) System.out.println("GUISplashScreen.GUISplashScreen()");
		 	statusprogress = 0;
			statusprogressbar = false;
			firsttime = false;
			secondtime = false;
			this.midlet = midlet;
			this.screenname = Constants.SCREENSPLASH; 
			pos = 0;
			/*Image tmpimg = Image.createImage(Constants.iconlogo);
			originalheight = tmpimg.getHeight();
			originalwidth = tmpimg.getWidth();
			tmpimg = null;*/
			iconlogo = Image.createImage(Constants.iconlogo);
			ApplicationAssistant.gc();
			
			/*InputStream in = null;
			try
			{
				in = getClass().getResourceAsStream(Constants.iconlogo);
				this.originalbytes = new byte [in.available()];
				in.read(originalbytes, 0, in.available());
				in.close();
				in = null;
			}
			catch (OutOfMemoryError e)
			{
				if(Constants.debug)  System.out.println("GUISplashScreen.GUISplashScreen() " + e.toString());
			}
			catch (Exception e)
			{
				if(Constants.debug)  System.out.println("GUISplashScreen.GUISplashScreen() " + e.toString());
			}	
			finally
			{
				if(in!=null)
				{
					try
					{
						in.close();
					}
					catch(Exception ex)
					{
						if(Constants.debug)  System.out.println("GUISplashScreen.GUISplashScreen() " + ex.toString());
					}
					finally
					{
						if(in!=null)
							in = null;
					}
				}
			}*/
			ApplicationAssistant.gc();
		 }
		 catch (Exception e)
		 {
			if(Constants.debug)  System.out.println("GUISplashScreen.GUISplashScreen() " + e.toString());
		 }			
		 setCommandListener(this);
		 
		 showScreenCommands();
		 ApplicationAssistant.gc();
	}
	
	public void showScreenCommands()
	{
		if(Constants.debug) System.out.println("GUISplashScreen.showScreenCommands()");
		
		if(this.commands!=null)
			return;
		
		this.removeCommands();
		this.commands = new Vector();
		commands.addElement(new Command(Constants.COMMANDLABELNEXT[ApplicationAssistant.language], Command.OK, 2));
		commands.addElement(new Command(Constants.COMMANDLABELEXIT[ApplicationAssistant.language], Command.EXIT, 1));
		this.addCommands();
	}
	
	public void cleanUp()
	{
		if(Constants.debug) System.out.println("GUISplashScreen.cleanUp()");
		this.screenname = null;
//		this.originalbytes = null;
		this.iconlogo = null;
		ApplicationAssistant.gc();
	}
	
	public void paint(Graphics g)
	{
		if(Constants.debug) System.out.println("GUISplashScreen.paint()");
		boolean couldntpaintimage = true;
		Font f = Font.getFont(Font.FACE_SYSTEM, Font.STYLE_BOLD, Font.SIZE_SMALL);
		try
		{
			//theres generally a backlag, issue new display.setcurrent(), but old one just goes for another paint with null
		 	if(screenname == null || !screenname.equals(Constants.SCREENSPLASH))
		 		return;	
		 	
		 	setColorTheme();
			showScreenCommands();
			
		 	//clear canvas
		 	g.setColor(GUIScreen.COLOR_BGSPLASH); // background color			
			g.fillRect(0, 0, getWidth(), getHeight());
			g.setFont(f);
			g.setColor(255,255,255);
			g.drawString("v" + ApplicationAssistant.version, screenwidth-3, 3, Graphics.TOP|Graphics.RIGHT);
			/*if(this.originalbytes!=null)
			{
				try
				{
					int [] resizedrgbdata = ImageAssistant.getResizedBytes(this.originalbytes, this.originalwidth, this.originalheight, screenwidth, screenheight);
					Image imglogo = Image.createRGBImage(resizedrgbdata, screenwidth, screenheight, false);
					g.drawImage(imglogo, screenwidth/2, screenheight/2, Graphics.VCENTER | Graphics.HCENTER);
					resizedrgbdata = null;
					imglogo = null;
					ApplicationAssistant.gc(true);
					couldntpaintimage = false;
				}
				catch (OutOfMemoryError e)
				{
					if(Constants.debug)  System.out.println("GUISplashScreen.paint() " + e.toString());
				}
				catch (Exception e)
				{
					if(Constants.debug)  System.out.println("GUISplashScreen.paint() " + e.toString());
				}				
			}*/
		}
		catch (Exception e)
		{
			if(Constants.debug)  System.out.println("GUISplashScreen.paint() " + e.toString());
		}
			
		if(couldntpaintimage)
		{
			boolean couldntpaintlogo = true;
			try
			{
				g.drawImage(iconlogo, screenwidth/2, screenheight/2, Graphics.VCENTER | Graphics.HCENTER);
				iconlogo = null;
				ApplicationAssistant.gc();
				couldntpaintlogo = false;
			}
			catch (OutOfMemoryError e)
			{
				if(Constants.debug)  System.out.println("GUISplashScreen.paint() " + e.toString());
			}
			catch (Exception e)
			{
				if(Constants.debug)  System.out.println("GUISplashScreen.paint() " + e.toString());
			}
			
			if(couldntpaintlogo)
			{
				g.drawString(Constants.apptitle[ApplicationAssistant.language], screenwidth/2, screenheight/2 - f.getHeight()/2, Graphics.TOP|Graphics.HCENTER);
			}
		}
		ApplicationAssistant.gc();
	}

	public void commandAction(Command c, Displayable d)
	{
		if(Constants.debug) System.out.println("GUISplashScreen.commandAction()");
		try
		{
			midlet.commandAction(c, d);
		}	
		catch(Exception e)
		{
			if(Constants.debug)  System.out.println("GUISplashScreen.commandAction() " + e.toString());
		}	
	}

	public void keyPressed(int key)
	{
		if(Constants.debug) System.out.println("GUISplashScreen.keyPressed()");
		try
		{
			if (key == KEY_MIDDLE_1[ApplicationAssistant.keyset_flag] || key == KEY_MIDDLE_2[ApplicationAssistant.keyset_flag] || key == KEY_SELECT[ApplicationAssistant.keyset_flag] || 
				key == KEY_UP[ApplicationAssistant.keyset_flag] || key == KEY_DOWN[ApplicationAssistant.keyset_flag] || key == KEY_LEFT[ApplicationAssistant.keyset_flag] || key == KEY_RIGHT[ApplicationAssistant.keyset_flag])
			{
				if(commands!=null && commands.size()>0)
					commandAction((Command) commands.elementAt(0), this);
			}
		}
		catch(Exception e)
		{
			if(Constants.debug)  System.out.println("GUISplashScreen.keyPressed() " + e.toString());
		}
	}
}