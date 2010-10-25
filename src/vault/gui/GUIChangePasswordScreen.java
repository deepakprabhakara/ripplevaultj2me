package vault.gui;

import java.util.Vector;

import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Font;
import javax.microedition.lcdui.Graphics;

import vault.app.ApplicationAPI;
import vault.app.ApplicationAssistant;
import vault.conn.ConnectionHome;
import vault.util.Constants;


class GUIChangePasswordScreen extends GUIScreen 
{
	private String [] fields = new String [] {"", "", ""};

	private int cursor [] = {0, 0, 0};
	private long keypresstime [] = {0, 0, 0};
	private int keypadindex [] = {0, 0, 0};
	private int prevkey [] = {10, 10, 10};
	
	int fieldboxwidth = 0;
	int fieldboxheight = 0;
	
	public GUIChangePasswordScreen(VaultMIDLET midlet, String currentpassword, String newpassword, String confirmpassword) 
	{
		try
		{
			statusprogress = 0;
			statusprogressbar = false;
			firsttime = false;
			secondtime = false;
			this.midlet = midlet;
			this.screenname = Constants.SCREENCHANGEPASSWORD;
			
			String longesttitle = Constants.GENERALCURRENTPASSWORD[ApplicationAssistant.language];
			if(longesttitle.length()<Constants.GENERALNEWPASSWORD[ApplicationAssistant.language].length())
				longesttitle = Constants.GENERALNEWPASSWORD[ApplicationAssistant.language];
			if(longesttitle.length()<Constants.GENERALCONFIRMPASSWORD[ApplicationAssistant.language].length())
				longesttitle = Constants.GENERALCONFIRMPASSWORD[ApplicationAssistant.language];
			Font f = Font.getFont(Font.FACE_SYSTEM, Font.STYLE_PLAIN, Font.SIZE_SMALL);
			fieldboxheight = f.getHeight()+4;
			fieldboxwidth = getWidth() - 6 - f.stringWidth(longesttitle) - 3 - 3;
			fields[0] = currentpassword;
			fields[1] = newpassword;
			fields[2] = confirmpassword;
			
			setCommandListener(this);
			
			showScreenCommands();
		}
		catch (Exception e)
		{
			if(Constants.debug)  System.out.println("GUIChangePasswordScreen.constructor() " + e.toString());
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
		commands.addElement(new Command(Constants.COMMANDLABELCONFIRM[ApplicationAssistant.language], Command.OK, 3));
		commands.addElement(new Command(Constants.COMMANDLABELBACK[ApplicationAssistant.language], Command.BACK, 1));
		commands.addElement(new Command(Constants.COMMANDLABELCLEAR[ApplicationAssistant.language], Command.BACK, 2));
		this.addCommands();
	}
	
	protected void cleanUp() 
	{
		this.commands = null;	
		this.fields = null;
		this.cursor = null;
		this.keypresstime = null;
		this.keypadindex = null;
		this.prevkey = null;
		ApplicationAssistant.gc();
	}

	protected void paint(Graphics g) 
	{
		try 
		{
			//theres generally a backlag, issue new display.setcurrent(), but old one just goes for another paint with null
		 	if(screenname == null || !screenname.equals(Constants.SCREENCHANGEPASSWORD))
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
			g.drawString(Constants.GENERALCHANGEPASSWORD[ApplicationAssistant.language], 3, 4, Graphics.TOP|Graphics.LEFT);
		
			f = Font.getFont(Font.FACE_SYSTEM, Font.STYLE_PLAIN, Font.SIZE_SMALL);
			g.setFont(f);
			//selected item
			int boxheight = (getHeight() - titleheight - 4 - 2*(3))/3; 
			g.setColor(COLOR_RGBVALUE[ApplicationAssistant.colortheme][COLORCOMPONENT_HIGHLIGHT]);			
			g.fillRect(0, titleheight+2 + pos*(boxheight+3), getWidth(), boxheight);
			
			//current password
			String optionboxtitle = Constants.GENERALCURRENTPASSWORD[ApplicationAssistant.language];
			if(pos==0)
				g.setColor(COLOR_RGBVALUE[ApplicationAssistant.colortheme][COLORCOMPONENT_HIGHLIGHT_TEXT]);
			else
				g.setColor(COLOR_RGBVALUE[ApplicationAssistant.colortheme][COLORCOMPONENT_BG_TEXT]);			
			g.drawString(optionboxtitle, 3, titleheight+2 + boxheight/2 - f.getHeight()/2, Graphics.TOP|Graphics.LEFT);
			g.setColor(0,0,0); //shadow
			g.fillRect(getWidth()-3 - fieldboxwidth, titleheight+2 + boxheight/2 - fieldboxheight/2 + 2, fieldboxwidth, fieldboxheight);
			g.setColor(COLOR_RGBVALUE[ApplicationAssistant.colortheme][COLORCOMPONENT_CALLOUT]);
			g.fillRect(getWidth()-3 - fieldboxwidth - 2, titleheight+2 + boxheight/2 - fieldboxheight/2, fieldboxwidth, fieldboxheight);
			g.setColor(COLOR_RGBVALUE[ApplicationAssistant.colortheme][COLORCOMPONENT_CALLOUT_TEXT]);
			g.drawString(fields[0].substring(cursor[0]), getWidth()-3 - fieldboxwidth + 2, titleheight+2 + boxheight/2 - f.getHeight()/2 + 2, Graphics.TOP|Graphics.LEFT);
			
			//new password
			optionboxtitle = Constants.GENERALNEWPASSWORD[ApplicationAssistant.language];
			if(pos==1)
				g.setColor(COLOR_RGBVALUE[ApplicationAssistant.colortheme][COLORCOMPONENT_HIGHLIGHT_TEXT]);
			else
				g.setColor(COLOR_RGBVALUE[ApplicationAssistant.colortheme][COLORCOMPONENT_BG_TEXT]);			
			g.drawString(optionboxtitle, 3, titleheight+2 + boxheight + boxheight/2 - f.getHeight()/2, Graphics.TOP|Graphics.LEFT);
			g.setColor(0,0,0); //shadow
			g.fillRect(getWidth()-3 - fieldboxwidth, titleheight+2 + boxheight + boxheight/2 - fieldboxheight/2 + 2, fieldboxwidth, fieldboxheight);
			g.setColor(COLOR_RGBVALUE[ApplicationAssistant.colortheme][COLORCOMPONENT_CALLOUT]);
			g.fillRect(getWidth()-3 - fieldboxwidth - 2, titleheight+2 + boxheight + boxheight/2 - fieldboxheight/2, fieldboxwidth, fieldboxheight);
			g.setColor(COLOR_RGBVALUE[ApplicationAssistant.colortheme][COLORCOMPONENT_CALLOUT_TEXT]);
			g.drawString(fields[1].substring(cursor[1]), getWidth()-3 - fieldboxwidth + 2, titleheight+2 + boxheight + boxheight/2 - f.getHeight()/2 + 2, Graphics.TOP|Graphics.LEFT);
			
			//confirm password
			optionboxtitle = Constants.GENERALCONFIRMPASSWORD[ApplicationAssistant.language];
			if(pos==2)
				g.setColor(COLOR_RGBVALUE[ApplicationAssistant.colortheme][COLORCOMPONENT_HIGHLIGHT_TEXT]);
			else
				g.setColor(COLOR_RGBVALUE[ApplicationAssistant.colortheme][COLORCOMPONENT_BG_TEXT]);			
			g.drawString(optionboxtitle, 3, titleheight+2 + boxheight*2 + boxheight/2 - f.getHeight()/2, Graphics.TOP|Graphics.LEFT);
			g.setColor(0,0,0); //shadow
			g.fillRect(getWidth()-3 - fieldboxwidth, titleheight+2 + boxheight*2 + boxheight/2 - fieldboxheight/2 + 2, fieldboxwidth, fieldboxheight);
			g.setColor(COLOR_RGBVALUE[ApplicationAssistant.colortheme][COLORCOMPONENT_CALLOUT]);
			g.fillRect(getWidth()-3 - fieldboxwidth - 2, titleheight+2 + boxheight*2 + boxheight/2 - fieldboxheight/2, fieldboxwidth, fieldboxheight);
			g.setColor(COLOR_RGBVALUE[ApplicationAssistant.colortheme][COLORCOMPONENT_CALLOUT_TEXT]);
			g.drawString(fields[2].substring(cursor[2]), getWidth()-3 - fieldboxwidth + 2, titleheight+2 + boxheight*2 + boxheight/2 - f.getHeight()/2 + 2, Graphics.TOP|Graphics.LEFT);
			
			//bottom bar
			g.setColor(COLOR_RGBVALUE[ApplicationAssistant.colortheme][COLORCOMPONENT_BAR]);
			g.fillRect(0, getHeight()-4, getWidth(), 4);
			
			//Draw status dialog if applicable
			if(dialogerrormsg!=null || dialogconfirmationmsg!=null || statusprogressbar)
				paintStatusDialog(g);
		}
		catch (Exception e)
		{
			if(Constants.debug)  System.out.println("GUIChangePasswordScreen.paint() " + e.toString()); 
		} 
		ApplicationAssistant.gc();
	}

	public void commandAction(Command c, Displayable d) 
	{
		if(Constants.debug)	System.out.println("******progress in GUIChangePasswordScreen commandaction is "+statusprogressbar+" "+Thread.currentThread().toString());
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
				if(c.getLabel().equals(Constants.COMMANDLABELCLEAR[ApplicationAssistant.language])) 
				{
					keyPressed(KEY_LEFT[ApplicationAssistant.keyset_flag]);
					return;
				}
				else if(c.getLabel().equals(Constants.COMMANDLABELCONFIRM[ApplicationAssistant.language])) 
				{
					if(fields[1].length()<6)
					{
						dialogerrormsg = Constants.MESSAGE_PASSWORD_TOOSHORT[ApplicationAssistant.language];
						repaint();
						return;
					}
					else if(!fields[1].equals(fields[2]))
					{
						dialogerrormsg = Constants.MESSAGE_PASSWORD_DONTTALLY[ApplicationAssistant.language];
						repaint();
						return;
					}
				}
				else if(c.getLabel().equals(Constants.COMMANDLABELOK[ApplicationAssistant.language]))
				{
					repaint();
					return;
				}
				midlet.commandAction(c, d);
			}
		}
		catch(Exception e)
		{
			if(Constants.debug)  System.out.println("GUIChangePasswordScreen.commandAction() " + e.toString()); 
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
			if(Constants.debug)	System.out.println("*********************GUIChangePasswordScreen keypressed ******************** "+Thread.currentThread().toString());
			if(commands!=null && commands.size()>0)
				commandAction((Command) commands.elementAt(0), this);
		}
		else
		{
			if (key == KEY_UP[ApplicationAssistant.keyset_flag]) 
			{
				if(pos>0)
					--pos;
			}
			else if (key == KEY_DOWN[ApplicationAssistant.keyset_flag]) 
			{
				if(pos<2)
					++pos;
			}
			else if (key == KEY_LEFT[ApplicationAssistant.keyset_flag]) 
			{
				if (fields[pos].length() > 0)
					fields[pos] = fields[pos].substring(0, fields[pos].length() - 1);
				if (cursor[0] > 0)
					--cursor[0];
				prevkey[0] = 10;
			}
			else if (key >= KEY_NUM0 && key <= KEY_NUM9) 
			{
			    Font f = Font.getFont(Font.FACE_SYSTEM, Font.STYLE_PLAIN, Font.SIZE_SMALL);
				if (prevkey[pos] == key - KEY_NUM0 && System.currentTimeMillis() - keypresstime[pos] < 1000) 
			    {
			    	keypadindex[pos] = (keypadindex[pos] + 1) % keypad[key - KEY_NUM0].length();
					if (fields[pos].length() > 0)
						fields[pos] = fields[pos].substring(0, fields[pos].length() - 1);
				} 
			    else 
			    {
					keypadindex[pos] = 0;
				}

			    fields[pos] += keypad[key - KEY_NUM0].charAt(keypadindex[pos]);

				if (f.stringWidth(fields[pos].substring(cursor[pos])) > fieldboxwidth - f.stringWidth("R")) 
				{
					cursor[pos]++;
				}

				keypresstime[pos] = System.currentTimeMillis();
				prevkey[pos] = key - KEY_NUM0;		
			}
			repaint();  
		}
	  }
	  catch(Exception e)
	  {
		if(Constants.debug)  System.out.println("GUIChangePasswordScreen.keyPressed() " + e.toString());
	  }
	}
	/**
	 * @return Returns the selected_synchmode.
	 */
	public String getNewpassword() {
		return fields[1];
	}
	public String getConfirmpassword() {
		return fields[2];
	}
	public String getCurrentpassword() {
		return fields[0];
	}
}