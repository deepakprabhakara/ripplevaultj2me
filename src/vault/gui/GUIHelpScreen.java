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


class GUIHelpScreen extends GUIScreen
{
	private Image imgdownarrow;
	private Image imguparrow;
	private Vector pages; //each element in this vector represents a page, which in turn is a vector of lines
	private int curpage;
	
	private int pageheight = 0;
	private int titleheight = 0;
	private int bottombarheight = 0;
	private String [] txtarray;
	public GUIHelpScreen(VaultMIDLET midlet, String [] txtarray)
	{
		try
		{
			statusprogress = 0;
			statusprogressbar = false;
			firsttime = false;
			secondtime = false;
			this.midlet = midlet;
			this.screenname = Constants.SCREENHELP;
			pos = 0;
			curpage = 0;
			pages = new Vector();
			imgdownarrow = Image.createImage(Constants.icondownarrow);
			imguparrow = Image.createImage(Constants.iconuparrow);
		
			Font f = Font.getFont(Font.FACE_SYSTEM, Font.STYLE_BOLD, Font.SIZE_SMALL);
			titleheight = f.getHeight()+4;
			bottombarheight = 4;
			pageheight = screenheight - titleheight - bottombarheight - 4;
				
			f = Font.getFont(Font.FACE_SYSTEM  , Font.STYLE_PLAIN, Font.SIZE_SMALL);
			int linenum = 0;
			int linewidth = 0;
			Vector page = new Vector();
			this.txtarray = txtarray; 
			for(int i=0; i<txtarray.length; i++)
			{
				String text = txtarray[i];
				while (text.length() > 0) 
				{			
					linewidth = screenwidth -3 -3;
					String [] tmp = getWordWrappedLine(text, f, linewidth);
				    page.addElement(tmp[0]);
				    if(Constants.debug) System.out.println(" text " + tmp[0]);
				    text = tmp[1];
					++linenum;
	
					if((f.getHeight()+1) * (linenum+1) > pageheight || (text.length()==0&&i==txtarray.length-1))
					{
						if(Constants.debug) System.out.println("** next pag");
						pages.addElement(page);
						page = null;
						ApplicationAssistant.gc();
						page = new Vector();
						linenum = 0;
					}
				}
				if(page.size()!=0)
				{
					page.addElement("");
					++linenum;
				}
			}
			f = null;
			ApplicationAssistant.gc();
			setCommandListener(this);
			
			showScreenCommands();
		 }
		 catch (Exception e)
		 {
			if(Constants.debug)  System.out.println("GUIHelpScreen.GUIHelpScreen() " + e.toString());
		 }
		 ApplicationAssistant.gc();
	}
	
	public void showScreenCommands()
	{
		//Back	
		if(this.commands!=null)
			return;
		
		this.removeCommands();
		this.commands = new Vector();
		commands.addElement(new Command(Constants.COMMANDLABELBACK[ApplicationAssistant.language], Command.BACK, 1));
		this.addCommands();
	}
	
	public void cleanUp()
	{
		this.commands = null;
		this.imgdownarrow = null;
		this.imguparrow = null;
		this.pages = null;
		this.txtarray = null;
		
		ApplicationAssistant.gc();
	}
	 
	public void paint(Graphics g)
	{
		try
		{
			//theres generally a backlag, issue new display.setcurrent(), but old one just goes for another paint with null
		 	if(screenname == null || !screenname.equals(Constants.SCREENHELP))
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
			g.fillRect(0, 0, getWidth(), titleheight);
			g.setColor(COLOR_RGBVALUE[ApplicationAssistant.colortheme][COLORCOMPONENT_BAR_TEXT]);
			g.drawString(Constants.HELPTITLE[ApplicationAssistant.language], 3, 4, Graphics.TOP|Graphics.LEFT);
		
			f = Font.getFont(Font.FACE_SYSTEM, Font.STYLE_PLAIN, Font.SIZE_SMALL);
			g.setFont(f);
			g.setColor(COLOR_RGBVALUE[ApplicationAssistant.colortheme][COLORCOMPONENT_BG_TEXT]);
			
			Vector page = (Vector) pages.elementAt(curpage);
			for(int i=0; i<page.size(); i++)
			{
				String line = (String) page.elementAt(i); 
				g.drawString(line, 
						 3, 
						 i*(f.getHeight()+1) + titleheight + 2, 
						 Graphics.TOP | Graphics.LEFT);
			}
			page = null;
			ApplicationAssistant.gc();
			
			if(pages.size()>1) //more than one page
			{
				if(curpage != pages.size()-1)  //... and not last page
					g.drawImage(imgdownarrow, screenwidth -2, titleheight+pageheight + 2, Graphics.BOTTOM | Graphics.RIGHT);
				if(curpage != 0) //... and not first page
					g.drawImage(imguparrow, screenwidth -2, titleheight + 2, Graphics.TOP | Graphics.RIGHT);
			}
			
			g.setColor(COLOR_RGBVALUE[ApplicationAssistant.colortheme][COLORCOMPONENT_BAR]);
			g.fillRect(0, getHeight()-bottombarheight, getWidth(), bottombarheight);
			
			//Draw status dialog if applicable
			if(dialogerrormsg!=null || dialogconfirmationmsg!=null || statusprogressbar)
				paintStatusDialog(g);
		}
		catch (Exception e)
		{
			if(Constants.debug)  System.out.println("GUIHelpScreen.paint() " + e.toString());
		}
		ApplicationAssistant.gc();
	}

	public void commandAction(Command c, Displayable d)
	{
		if(Constants.debug)	System.out.println("******progress in GUIHelpScreen commandaction is "+statusprogressbar+" "+Thread.currentThread().toString());
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
				midlet.commandAction(c, d);
			}
		}
		catch(Exception e)
		{
			if(Constants.debug)  System.out.println("GUIHelp.commandAction() " + e.toString()); 
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
		else if(pages!=null && pages.size()>0)
		{
			if (key == KEY_DOWN[ApplicationAssistant.keyset_flag]) 
			{
				if(curpage < pages.size()-1)
				{
					++curpage;
					repaint(); 
				}
			} 
			else if (key == KEY_UP[ApplicationAssistant.keyset_flag]) 
			{
				if(curpage!=0)
				{
					--curpage;
					repaint(); 
				}
			} 
			repaint();  
		}
	  }
	  catch(Exception e)
	  {
		if(Constants.debug)  System.out.println("GUIHelp.keyPressed() " + e.toString());
	  }
	}
}