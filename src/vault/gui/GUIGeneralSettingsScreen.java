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
import vault.util.Constants;


class GUIGeneralSettingsScreen extends GUIScreen 
{
	private int selected_language = ApplicationAssistant.language;
	private Image imgrightarrow;

	public GUIGeneralSettingsScreen(VaultMIDLET midlet) 
	{
		try
		{
			statusprogress = 0;
			statusprogressbar = false;
			firsttime = false;
			secondtime = false;
			this.midlet = midlet;
			this.screenname = Constants.SCREENGENERALSETTINGS;
			imgrightarrow = Image.createImage(Constants.iconrightarrow);
			setCommandListener(this);
			
			showScreenCommands();
		}
		catch (Exception e)
		{
			if(Constants.debug)  System.out.println("GUIGeneralSettingsScreen.constructor() " + e.toString());
		}
		ApplicationAssistant.gc();
	}

	public void showScreenCommands()
	{
		//Select
		//Back	

		//if commands are there and its not just 1 (error/confirmation/progressbar) then no need to remove and readd
		if(this.commands!=null && this.commands.size()!=1)
			return;
		
		this.removeCommands();
		this.commands = new Vector();
		commands.addElement(new Command(Constants.COMMANDLABELSELECT[ApplicationAssistant.language], Command.OK, 2));
		commands.addElement(new Command(Constants.COMMANDLABELCONFIRM[ApplicationAssistant.language], Command.BACK, 1));
		this.addCommands();
	}
	
	protected void cleanUp() 
	{
		this.commands = null;
		this.imgrightarrow = null;
		
		ApplicationAssistant.gc();
	}

	protected void paint(Graphics g) 
	{
		try 
		{
			//theres generally a backlag, issue new display.setcurrent(), but old one just goes for another paint with null
		 	if(screenname == null || !screenname.equals(Constants.SCREENGENERALSETTINGS))
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
			int titleheight = f.getHeight()+4;
			g.setColor(COLOR_RGBVALUE[ApplicationAssistant.colortheme][COLORCOMPONENT_BAR]);			
			g.fillRect(0, 0, getWidth(), titleheight);
			g.setColor(COLOR_RGBVALUE[ApplicationAssistant.colortheme][COLORCOMPONENT_BAR_TEXT]);
			g.drawString(Constants.GENERALSETTINGS[ApplicationAssistant.language], 3, 4, Graphics.TOP|Graphics.LEFT);
		
			f = Font.getFont(Font.FACE_SYSTEM, Font.STYLE_PLAIN, Font.SIZE_SMALL);
			g.setFont(f);
			//selected item
			int boxheight = (getHeight() - titleheight - 4 - 2*(3))/3; 
			g.setColor(COLOR_RGBVALUE[ApplicationAssistant.colortheme][COLORCOMPONENT_HIGHLIGHT]);			
			g.fillRect(0, titleheight+2 + pos*(boxheight+3), getWidth(), boxheight);
			
			//language
			String optionboxtitle = Constants.GENERALLANGUAGE[ApplicationAssistant.language];
			int optionboxheight = f.getHeight()+4;
			int optionboxwidth = getWidth() - 6 - f.stringWidth(optionboxtitle) - 3 - 2 - imgrightarrow.getWidth() - 3;
			if(pos==0)
				g.setColor(COLOR_RGBVALUE[ApplicationAssistant.colortheme][COLORCOMPONENT_HIGHLIGHT_TEXT]);
			else
				g.setColor(COLOR_RGBVALUE[ApplicationAssistant.colortheme][COLORCOMPONENT_BG_TEXT]);			
			g.drawString(optionboxtitle, 3, titleheight+2 + boxheight/2 - f.getHeight()/2 + 2, Graphics.TOP|Graphics.LEFT);
			g.setColor(0,0,0); //shadow
			g.fillRect(3 + f.stringWidth(optionboxtitle) + 3 + 2, titleheight+2 + boxheight/2 - optionboxheight/2 + 2, optionboxwidth, optionboxheight);
			g.setColor(COLOR_RGBVALUE[ApplicationAssistant.colortheme][COLORCOMPONENT_CALLOUT]);
			g.fillRect(3 + f.stringWidth(optionboxtitle) + 3, titleheight+2 + boxheight/2 - optionboxheight/2, optionboxwidth, optionboxheight);
			g.setColor(COLOR_RGBVALUE[ApplicationAssistant.colortheme][COLORCOMPONENT_CALLOUT_TEXT]);
			g.drawString(Constants.LANGUAGES[selected_language], 3 + f.stringWidth(optionboxtitle) + 3 + 4, titleheight+2 + boxheight/2 - f.getHeight()/2 + 2, Graphics.TOP|Graphics.LEFT);
			g.drawImage(imgrightarrow, getWidth() - 5, 
					 titleheight+2 + boxheight/2, 
					 Graphics.VCENTER | Graphics.RIGHT);
			
			//bottom bar
			g.setColor(COLOR_RGBVALUE[ApplicationAssistant.colortheme][COLORCOMPONENT_BAR]);
			g.fillRect(0, getHeight()-4, getWidth(), 4);
			
			//Draw status dialog if applicable
			if(dialogerrormsg!=null || dialogconfirmationmsg!=null || statusprogressbar)
				paintStatusDialog(g);
		}
		catch (Exception e)
		{
			if(Constants.debug)  System.out.println("GUIContactSettingsScreen.paint() " + e.toString()); 
		} 
		ApplicationAssistant.gc();
	}

	public void commandAction(Command c, Displayable d) 
	{
		if(Constants.debug)	System.out.println("******progress in GUIGeneralSettingsScreen commandaction is "+statusprogressbar+" "+Thread.currentThread().toString());
		dialogconfirmationmsg = null;
		dialogerrormsg = null;
		try
		{
			if(statusprogressbar)
			{
				int act = ApplicationAPI.getConnstatus();
				//this block will execute only when there is a network operation 
				if((act == ConnectionHome.SENDING || act == ConnectionHome.RECEIVING)
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
				if(c.getLabel().equals(Constants.COMMANDLABELSELECT[ApplicationAssistant.language])) 
				{
					if(pos == 0)
					{
						//Select pressed when language is selected.
						selected_language = ++selected_language%2;
						repaint();
						return;
					}
				}
				midlet.commandAction(c, d);
			}
		}
		catch(Exception e)
		{
			if(Constants.debug)  System.out.println("GUIGeneralSettingsScreen.commandAction() " + e.toString()); 
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
			if(Constants.debug)	System.out.println("*********************GUIGeneralSettingsScreen keypressed ******************** "+Thread.currentThread().toString());
			if(commands!=null && commands.size()>0)
				commandAction((Command) commands.elementAt(0), this);
		}
		else
		{
			if (key == KEY_RIGHT[ApplicationAssistant.keyset_flag]) 
			{
				if(pos == 0) //language selected
				{
					selected_language = ++selected_language%2;
				}
			} 
			else if (key == KEY_LEFT[ApplicationAssistant.keyset_flag]) 
			{
				if(pos == 0) //language selected
				{
					selected_language = ++selected_language%2;
				}
			} 
			repaint();  
		}
	  }
	  catch(Exception e)
	  {
		if(Constants.debug)  System.out.println("GUIGeneralSettingsScreen.keyPressed() " + e.toString());
	  }
	}
	/**
	 * @return Returns the selected_synchmode.
	 */
	public int getSelected_language() {
		return selected_language;
	}
}