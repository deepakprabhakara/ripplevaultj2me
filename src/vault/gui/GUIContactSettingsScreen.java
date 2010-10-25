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


class GUIContactSettingsScreen extends GUIScreen 
{
	private String[] synchronisationmodes = { Constants.CONTACTSOPTIONSCHEDULED[ApplicationAssistant.language], Constants.CONTACTSOPTIONMANUAL[ApplicationAssistant.language] };
	private int selected_synchmode = 0;
	private Image imgrightarrow;
	private int scheduleperiod = 7;

	public GUIContactSettingsScreen(VaultMIDLET midlet) 
	{
		try
		{
			statusprogress = 0;
			statusprogressbar = false;
			firsttime = false;
			secondtime = false;
			this.midlet = midlet;
			this.screenname = Constants.SCREENCONTACTSETTINGS;
			if(ApplicationAssistant.nextscheduledsynchtime == 0)
				selected_synchmode = 1; //manual
			else //scheduled
			{
				this.scheduleperiod = (int) ApplicationAssistant.scheduleperiod/(24*3600*1000);
				if(this.scheduleperiod == 0)
					this.scheduleperiod = 7;
			}
			imgrightarrow = Image.createImage(Constants.iconrightarrow);
			setCommandListener(this);
			
			showScreenCommands();
		}
		catch (Exception e)
		{
			if(Constants.debug)  System.out.println("GUIContactSettingsScreen.constructor() " + e.toString());
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
		this.synchronisationmodes = null;
		
		ApplicationAssistant.gc();
	}

	protected void paint(Graphics g) 
	{
		try 
		{
			//theres generally a backlag, issue new display.setcurrent(), but old one just goes for another paint with null
		 	if(screenname == null || !screenname.equals(Constants.SCREENCONTACTSETTINGS))
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
			g.drawString(Constants.CONTACTSOPTIONS[ApplicationAssistant.language], 3, 4, Graphics.TOP|Graphics.LEFT);
		
			f = Font.getFont(Font.FACE_SYSTEM, Font.STYLE_PLAIN, Font.SIZE_SMALL);
			g.setFont(f);
			//selected item
			int boxheight = (getHeight() - titleheight - 4 - 2*(3))/3; 
			g.setColor(COLOR_RGBVALUE[ApplicationAssistant.colortheme][COLORCOMPONENT_HIGHLIGHT]);			
			g.fillRect(0, titleheight+2 + pos*(boxheight+3), getWidth(), boxheight);
			
			//synchronisation mode
			String optionboxtitle = Constants.CONTACTSMODE[ApplicationAssistant.language];
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
			g.drawString(synchronisationmodes[selected_synchmode], 3 + f.stringWidth(optionboxtitle) + 3 + 4, titleheight+2 + boxheight/2 - f.getHeight()/2 + 2, Graphics.TOP|Graphics.LEFT);
			g.drawImage(imgrightarrow, getWidth() - 5, 
					 titleheight+2 + boxheight/2, 
					 Graphics.VCENTER | Graphics.RIGHT);
			
			
			//upload time
			if(selected_synchmode == 0)
			{
				optionboxtitle = Constants.CONTACTSSYNCHRONIZEEVERY[ApplicationAssistant.language];
				optionboxheight = f.getHeight()+4;
				optionboxwidth = f.stringWidth("88" + Constants.CONTACTSDAYS[ApplicationAssistant.language]) + 8;
				if(pos==1)
					g.setColor(COLOR_RGBVALUE[ApplicationAssistant.colortheme][COLORCOMPONENT_HIGHLIGHT_TEXT]);
				else
					g.setColor(COLOR_RGBVALUE[ApplicationAssistant.colortheme][COLORCOMPONENT_BG_TEXT]);			
				g.drawString(optionboxtitle, 3, titleheight+2 + 1*(boxheight+3) + boxheight/2 - f.getHeight()/2 + 2, Graphics.TOP|Graphics.LEFT);
				g.setColor(0,0,0); //shadow
				g.fillRect(3 + f.stringWidth(optionboxtitle) + 3 + 2, titleheight+2 + 1*(boxheight+3) + boxheight/2 - optionboxheight/2 + 2, optionboxwidth, optionboxheight);
				g.setColor(COLOR_RGBVALUE[ApplicationAssistant.colortheme][COLORCOMPONENT_CALLOUT]);
				g.fillRect(3 + f.stringWidth(optionboxtitle) + 3, titleheight+2 + 1*(boxheight+3) + boxheight/2 - optionboxheight/2, optionboxwidth, optionboxheight);
				g.setColor(COLOR_RGBVALUE[ApplicationAssistant.colortheme][COLORCOMPONENT_CALLOUT_TEXT]);
				g.drawString(this.scheduleperiod + Constants.CONTACTSDAYS[ApplicationAssistant.language], 3 + f.stringWidth(optionboxtitle) + 3 + 4, titleheight+2 + 1*(boxheight+3) + boxheight/2 - f.getHeight()/2 + 2, Graphics.TOP|Graphics.LEFT);
				g.drawImage(imgrightarrow, 3 + f.stringWidth(optionboxtitle) + 3 + optionboxwidth + 6, 
						 titleheight+2 + 1*(boxheight+3) + boxheight/2, 
						 Graphics.VCENTER | Graphics.LEFT);
			}
			
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
		if(Constants.debug)	System.out.println("******progress in GUIContactSettingsScreen commandaction is "+statusprogressbar+" "+Thread.currentThread().toString());
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
					//Select pressed when synch mode is selected.
					if(pos == 0)
					{
						selected_synchmode = ++selected_synchmode%2;
						repaint();
						return;
					}
					//Select pressed when upload time is selected
					else if(pos==1)
					{
						if(this.scheduleperiod== 31)
							this.scheduleperiod = 1;
						else
							++this.scheduleperiod;
						repaint();
						return;
					}
					//if Select pressed when synch now is selected, it will move on below
				}
				midlet.commandAction(c, d);
			}
		}
		catch(Exception e)
		{
			if(Constants.debug)  System.out.println("GUIContactSettingsScreen.commandAction() " + e.toString()); 
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
			if(Constants.debug)	System.out.println("*********************GUIContactSettingsScreen keypressed ******************** "+Thread.currentThread().toString());
			if(commands!=null && commands.size()>0)
				commandAction((Command) commands.elementAt(0), this);
		}
		else
		{
			if (key == KEY_DOWN[ApplicationAssistant.keyset_flag]) 
			{
				if(selected_synchmode == 0) //scheduled
					pos = ++pos%2;
				else if(selected_synchmode == 1) //manual
					pos = pos;
			} 
			else if (key == KEY_UP[ApplicationAssistant.keyset_flag]) 
			{
				if(selected_synchmode == 0) //scheduled
				{
					if(pos==0)
						pos = 1;
					else
						--pos;
				}
				else if(selected_synchmode == 1) //manual
				{
					if(pos==0)
						pos = 0;
					else
						--pos;
				}
			} 
			else if (key == KEY_RIGHT[ApplicationAssistant.keyset_flag]) 
			{
				if(pos == 0) //synchmode selected
				{
					selected_synchmode = ++selected_synchmode%2;
				}
				else if(pos == 1) //upload time is selected
				{
					if(this.scheduleperiod== 31)
						this.scheduleperiod = 1;
					else
						++this.scheduleperiod;
				}
			} 
			else if (key == KEY_LEFT[ApplicationAssistant.keyset_flag]) 
			{
				if(pos == 0) //synchmode selected
				{
					selected_synchmode = ++selected_synchmode%2;
				}
				else if(pos == 1) //upload time is selected
				{
					if(this.scheduleperiod == 1)
						this.scheduleperiod = 31;
					else
						--this.scheduleperiod;
				}
			} 
			repaint();  
		}
	  }
	  catch(Exception e)
	  {
		if(Constants.debug)  System.out.println("GUIContactSettingsScreen.keyPressed() " + e.toString());
	  }
	}
	/**
	 * @return Returns the scheduleperiod.
	 */
	public int getScheduleperiod() {
		return scheduleperiod;
	}
	/**
	 * @return Returns the selected_synchmode.
	 */
	public int getSelected_synchmode() {
		return selected_synchmode;
	}
}