package vault.gui;
import java.util.Vector;

import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Font;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;

import vault.app.ApplicationAPI;
import vault.app.ApplicationAssistant;
import vault.conn.ConnectionHome;
import vault.dto.MediaDTO;
import vault.util.Constants;

class GUIContentListScreen extends GUIScreen
{
	private Image imgicon;
	private Image imgvaulticon;
	private Image imgtick;
	private int numtotalmedia;
	private int firstindex = 0; //index of first item in screen
	private int titlebarheight;
	private int bottombarheight;
	private int nummediaperscreen;
	private int mediaheight;
	private int contenttype;
	
	public GUIContentListScreen(VaultMIDLET midlet, int contenttype)
	{
		try
		{	
		 	statusprogress = 0;
			statusprogressbar = false;
			firsttime = false;
			secondtime = false;
			this.midlet = midlet;
			this.screenname = Constants.SCREENMEDIALIST; 
			this.contenttype = contenttype;
			pos = 0;
			numtotalmedia = (ApplicationAPI.mediadtos==null)?0:ApplicationAPI.mediadtos.size();
			if(contenttype == Constants.CONTENTTYPE_VIDEO)
			{
				imgvaulticon = Image.createImage(Constants.iconvaultvideo);
				imgicon = Image.createImage(Constants.iconvideo);
			}
			else if(contenttype == Constants.CONTENTTYPE_RINGTONE)
			{
				imgvaulticon = Image.createImage(Constants.iconvaultringtone);
				imgicon = Image.createImage(Constants.iconringtone);
			}
			imgtick = Image.createImage(Constants.icontick);
			Font f = Font.getFont(Font.FACE_SYSTEM, Font.STYLE_BOLD, Font.SIZE_SMALL);
			titlebarheight = f.getHeight()+4;
			bottombarheight = 8;
			f = Font.getFont(Font.FACE_SYSTEM  , Font.STYLE_PLAIN, Font.SIZE_SMALL);
			mediaheight = ((imgicon.getHeight()>(f.getHeight()*2+2))?imgicon.getHeight():(f.getHeight()*2+2))+2;
			nummediaperscreen = (screenheight - titlebarheight - bottombarheight - 2 - 2)/mediaheight;
			ApplicationAPI.numpacketstoupload = 0;
			
			setCommandListener(this);
			
			showScreenCommands();
		}
		catch (Exception e)
		{
			if(Constants.debug)  System.out.println("GUIMediaListScreen.GUIMediaListScreen() " + e.toString());
		}
		ApplicationAssistant.gc();
	}
	
	public void showScreenCommands()
	{
		//Select
		//Publish
		//Back	

		//if commands are there and its not just 1 (error/confirmation/progressbar) then no need to remove and readd
		if(this.commands!=null && this.commands.size()!=1)
			return;
		
		this.removeCommands();
		this.commands = new Vector();
		if(numtotalmedia > 0)
		{
			commands.addElement(new Command(Constants.COMMANDLABELSELECT[ApplicationAssistant.language], Command.OK, 2));
			commands.addElement(new Command(Constants.COMMANDLABELPUBLISH[ApplicationAssistant.language], Command.OK, 3));
		}
		commands.addElement(new Command(Constants.COMMANDLABELBACK[ApplicationAssistant.language], Command.BACK, 1));
		this.addCommands();
	}
	
	public void cleanUp()
	{
		this.screenname = null;
		this.imgvaulticon = null;
		this.imgicon = null;
		this.imgtick = null;
		ApplicationAssistant.gc();
	}
	 
	public void paint(Graphics g)
	{
		try
		{
			//theres generally a backlag, issue new display.setcurrent(), but old one just goes for another paint with null
		 	if(screenname == null || !screenname.equals(Constants.SCREENMEDIALIST))
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
			if(contenttype == Constants.CONTENTTYPE_VIDEO)
				title = Constants.MEDIAVIDEOSTITLE[ApplicationAssistant.language];
			else if(contenttype == Constants.CONTENTTYPE_RINGTONE)
				title = Constants.MEDIARINGTONESTITLE[ApplicationAssistant.language];
			if(numtotalmedia != 0)
				title = ((MediaDTO) ApplicationAPI.mediadtos.elementAt(pos)).getTitle();
			g.drawString(title, 3, 4, Graphics.TOP|Graphics.LEFT);
			title = null;
			ApplicationAssistant.gc();

			if(numtotalmedia==0)
			{
				f = Font.getFont(Font.FACE_SYSTEM, Font.STYLE_PLAIN, Font.SIZE_SMALL);
				g.setFont(f);
				g.setColor(COLOR_RGBVALUE[ApplicationAssistant.colortheme][COLORCOMPONENT_BG_TEXT]);
				g.drawString(Constants.MEDIAYOUDONTHAVE[ApplicationAssistant.language], 
						screenwidth/2, 
						titlebarheight + (screenheight - titlebarheight - bottombarheight)/2 - 2 - f.getHeight(), 
						Graphics.TOP|Graphics.HCENTER);
				String text = "";
				if(contenttype == Constants.CONTENTTYPE_VIDEO)
					text = Constants.MEDIAANYVIDEOS[ApplicationAssistant.language];
				else if(contenttype == Constants.CONTENTTYPE_RINGTONE)
					text = Constants.MEDIAANYRINGTONES[ApplicationAssistant.language];
				g.drawString(text, 
						screenwidth/2, 
						titlebarheight + (screenheight - titlebarheight - bottombarheight)/2 + 2,
						Graphics.TOP|Graphics.HCENTER);
			}
			else
			{	
				g.setColor(0,0,0);
				g.drawLine(4 + imgicon.getWidth() + 10, titlebarheight, 4 + imgicon.getWidth() + 10, getHeight());
				g.setColor(COLOR_RGBVALUE[ApplicationAssistant.colortheme][COLORCOMPONENT_HIGHLIGHT]);
				g.fillRect(0, (pos-firstindex)*mediaheight + titlebarheight + 2, screenwidth, mediaheight);				
				f = Font.getFont(Font.FACE_SYSTEM  , Font.STYLE_PLAIN, Font.SIZE_SMALL);
				g.setFont(f);
				
				for(int i=0; firstindex+i<numtotalmedia && i<nummediaperscreen; i++)
				{
					if(firstindex+i==pos)
						g.setColor(COLOR_RGBVALUE[ApplicationAssistant.colortheme][COLORCOMPONENT_HIGHLIGHT_TEXT]);
					else
						g.setColor(COLOR_RGBVALUE[ApplicationAssistant.colortheme][COLORCOMPONENT_BG_TEXT]);
					MediaDTO mdto = (MediaDTO) ApplicationAPI.mediadtos.elementAt(firstindex+i);
					
					int tickoffset = 1;
					if(((MediaDTO) ApplicationAPI.mediadtos.elementAt(firstindex+i)).isSelected())
			 		{
			 			//selected
			 			g.drawImage(imgtick, 
								 screenwidth - 3, 
								 i*mediaheight + titlebarheight + 2 + mediaheight/2 - 2 - f.getHeight()/2, 
								 Graphics.VCENTER | Graphics.RIGHT);
			 			tickoffset += (imgtick.getWidth() + 3);
			 		}
					int maxchars = 1;
					while(maxchars < mdto.getTitle().length() && 
						  f.stringWidth(mdto.getTitle().substring(0,maxchars))<screenwidth - 4 - imgicon.getWidth() - 20 - tickoffset)
						++maxchars;
					String label1 = mdto.getTitle().length()<maxchars?mdto.getTitle():mdto.getTitle().substring(0,maxchars);
					maxchars = 1;
					while(maxchars < mdto.getTimestampAsString().length() && 
						  f.stringWidth(mdto.getTimestampAsString().substring(0,maxchars))<screenwidth - 4 - imgicon.getWidth() - 35 - 1)
						++maxchars;
					String label2 = mdto.getTimestampAsString().length()<maxchars?mdto.getTimestampAsString():mdto.getTimestampAsString().substring(0,maxchars);

					if(((MediaDTO) ApplicationAPI.mediadtos.elementAt(firstindex+i)).getQueuestatus() == Constants.MEDIAQUEUESTATUS_UPLOADED)
			 		{
						g.drawImage(imgvaulticon, 
								 4, 
								 i*mediaheight + titlebarheight + 2 + mediaheight/2, 
								 Graphics.VCENTER | Graphics.LEFT);
			 		}
					else
					{
						g.drawImage(imgicon, 
								 4, 
								 i*mediaheight + titlebarheight + 2 + mediaheight/2, 
								 Graphics.VCENTER | Graphics.LEFT);
					}
					
					g.drawString(label1, 
								 4 + imgicon.getWidth() + 20, 
								 i*mediaheight + titlebarheight + 2 + mediaheight/2 - 2, 
								 Graphics.BOTTOM | Graphics.LEFT);

					g.drawString(label2, 
								 4 + imgicon.getWidth() + 35, 
								 i*mediaheight + titlebarheight + 2 + mediaheight/2 + 2, 
								 Graphics.TOP | Graphics.LEFT);
					
					label1 = null;
					label2 = null;
					mdto = null;
					ApplicationAssistant.gc();
				}
			}
			
			//bottom bar
			g.setColor(COLOR_RGBVALUE[ApplicationAssistant.colortheme][COLORCOMPONENT_BAR]);
			g.fillRect(0, screenheight-bottombarheight, getWidth(), bottombarheight);
			
			//Draw status dialog if applicable
			if(dialogerrormsg!=null || dialogconfirmationmsg!=null || statusprogressbar)
				paintStatusDialog(g);
		}
		catch (Exception e)
		{
			if(Constants.debug)  System.out.println("GUIMediaListScreen.paint() " + e.toString());
		}
		ApplicationAssistant.gc();
	}

	public void commandAction(Command c, Displayable d)
	{
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
							if(contenttype == Constants.CONTENTTYPE_VIDEO)
								dialogerrormsg = Constants.MESSAGE_PUBLISH_NOVIDEOSELECTED[ApplicationAssistant.language];
							else if(contenttype == Constants.CONTENTTYPE_RINGTONE)
								dialogerrormsg = Constants.MESSAGE_PUBLISH_NORINGTONESELECTED[ApplicationAssistant.language];
							
							repaint();
							return;
					}
				}
				midlet.commandAction(c, d);				
			}
		}	
		catch(Exception e)
		{
			if(Constants.debug)  System.out.println("GUIMediaListScreen.commandAction() " + e.toString());
		}	
	}

	public void keyPressed(int key)
	{
		dialogconfirmationmsg = null;
		dialogerrormsg = null;
		if(statusprogressbar)
			return;
		try
		{
			if (key == KEY_MIDDLE_1[ApplicationAssistant.keyset_flag] || key == KEY_MIDDLE_2[ApplicationAssistant.keyset_flag] || key == KEY_SELECT[ApplicationAssistant.keyset_flag])
			{
				statusprogress = 0;
				if(Constants.debug)	System.out.println("*********************GUIMediaListScreen keypressed ******************** "+Thread.currentThread().toString());
				if(commands!=null && commands.size()>0)
					commandAction((Command) commands.elementAt(0), this);
			}
			else
			{
				if (key == KEY_UP[ApplicationAssistant.keyset_flag])
				{
					if(pos > 0)
					{
						--pos;
						if(pos < firstindex)
							--firstindex;
						repaint();
					}
					return;
				}
				else if (key == KEY_DOWN[ApplicationAssistant.keyset_flag])
				{

					if(pos < numtotalmedia-1)
					{
						++pos;
						if(pos >= firstindex+nummediaperscreen)
							++firstindex;
						repaint();
					}
					return;
				}
			}
	  	}
		catch(Exception e)
		{
			if(Constants.debug)  System.out.println("GUIMediaListScreen.keyPressed() " + e.toString());
		}
	}
}