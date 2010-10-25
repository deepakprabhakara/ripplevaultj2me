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

class GUIStatusScreen extends GUIScreen
{
	private int bottombarheight;
	boolean menuopened = false;
	boolean commandschangedonmenutoggle = true;
	private Image imgrightarrow;
	
	boolean registrationcalled = false;
	boolean initialuploadcalled = false;
	
	public GUIStatusScreen(VaultMIDLET midlet)
	{
		try
		{
			//initialise these values, as this is the first screen
			if(ApplicationAssistant.screenwidth == 0)
			{
				ApplicationAssistant.screenheight = this.screenheight;
				ApplicationAssistant.screenwidth = this.screenwidth;
			}
			statusprogress = 0;
			statusprogressbar = false;
			firsttime = false;
			secondtime = false;
			this.midlet = midlet;
			this.screenname = Constants.SCREENSTATUS;
			pos = 0;
			bottombarheight = 8;
			imgrightarrow = Image.createImage(Constants.iconrightarrow);
			
			setCommandListener(this);
			
			showScreenCommands();
		}
		catch (Exception e)
		{
			if(Constants.debug)  System.out.println("GUIStatusScreen.constructor() " + e.toString());
		}
	}
	
	public void showScreenCommands()
	{
		//Select
		//About
		//Exit	

		//if commands are there and its not just 1 (error/confirmation/progressbar) then no need to remove and readd, unless it toggles from menu open to menu not open mode
		if(this.commands!=null && this.commands.size()!=1)
		{
			if(menuopened && !commandschangedonmenutoggle)
			{
				this.removeCommands();
				this.commands = new Vector();
				commands.addElement(new Command(Constants.COMMANDLABELSELECT[ApplicationAssistant.language], Command.OK, 2));
				commands.addElement(new Command(Constants.COMMANDLABELCANCEL[ApplicationAssistant.language], Command.BACK, 1));
				this.addCommands();
				commandschangedonmenutoggle = true;
			}
			else if(!menuopened && !commandschangedonmenutoggle)
			{
				this.removeCommands();
				this.commands = new Vector();
				commands.addElement(new Command(Constants.COMMANDLABELOPTIONS[ApplicationAssistant.language], Command.OK, 2));
				commands.addElement(new Command(Constants.COMMANDLABELEXIT[ApplicationAssistant.language], Command.EXIT, 1));
				this.addCommands();
				commandschangedonmenutoggle = true;
			}
			return;
		}
		
		this.removeCommands();
		this.commands = new Vector();
		commands.addElement(new Command(Constants.COMMANDLABELOPTIONS[ApplicationAssistant.language], Command.OK, 2));
		commands.addElement(new Command(Constants.COMMANDLABELEXIT[ApplicationAssistant.language], Command.EXIT, 1));
		this.addCommands();
	}
	
	public void cleanUp()
	{
		this.screenname = null;
		this.commands = null;
		this.imgrightarrow = null;
		ApplicationAssistant.gc(); 
	}
	
	public void paint(Graphics g)
	{
		 try
		 {	
		 	//theres generally a backlag, issue new display.setcurrent(), but old one just goes for another paint with null
		 	if(screenname == null || !screenname.equals(Constants.SCREENSTATUS))
		 		return;
		 	
		 	setColorTheme();
			
		 	//in case of status screen, we want the rest painted as well, as it has refreshing content. 
			//so not returning after just progress bar print
			/*if(statusprogressbar&&(!firsttime)&&(!secondtime))
			{
				paintStatusDialog(g);
				return;
			}*/
		 	if(secondtime)
				secondtime = false;
			if(firsttime)
			{
				firsttime = false;
				secondtime = true;
			}
			if(!statusprogressbar)
				showScreenCommands();
			else
			{
				menuopened = false;
				commandschangedonmenutoggle = false;
			}
			
			int borderwidth = 0;
			int borderheight = 0;
			
		 	//clear canvas
			g.setColor(COLOR_RGBVALUE[ApplicationAssistant.colortheme][COLORCOMPONENT_BG]); // background color			
			g.fillRect(0, 0, getWidth(), getHeight());
			
			//status
			Font f = Font.getFont(Font.FACE_SYSTEM, Font.STYLE_BOLD, Font.SIZE_SMALL);
			g.setFont(f);
			g.setColor(COLOR_RGBVALUE[ApplicationAssistant.colortheme][COLORCOMPONENT_BG_TEXT]);
			g.drawString(ApplicationAPI.status_activity, 3, 5, Graphics.TOP|Graphics.LEFT);
		
			//counts
			String countstring = "";
			boolean queued = (ApplicationAPI.status_uploading[Constants.LISTINDEX_CONTACTS] != 0) || 
							(ApplicationAPI.status_uploading[Constants.LISTINDEX_PHOTOS] != 0) || 
							(ApplicationAPI.status_uploading[Constants.LISTINDEX_VIDEOS] != 0) || 
							(ApplicationAPI.status_uploading[Constants.LISTINDEX_RINGTONES] != 0) || 
							(ApplicationAPI.status_uploading[Constants.LISTINDEX_WALLPAPERS] != 0);
			if(queued)
			{
				countstring = "<b>" + Constants.STATUSQUEUED[ApplicationAssistant.language] + "\n";
				String tempstring = "";
				if(ApplicationAssistant.contactsynch && ApplicationAPI.status_uploading[Constants.LISTINDEX_CONTACTS]>0)
					tempstring += ApplicationAPI.status_uploading[Constants.LISTINDEX_CONTACTS] + " " + Constants.STATUSCONTACTS[ApplicationAssistant.language] + ", "; 
				if(ApplicationAssistant.mediasynch && ApplicationAPI.status_uploading[Constants.LISTINDEX_PHOTOS]>0)
					tempstring += ApplicationAPI.status_uploading[Constants.LISTINDEX_PHOTOS] + " " + Constants.STATUSPHOTOS[ApplicationAssistant.language] + ", ";
				if(ApplicationAssistant.mediasynch && ApplicationAPI.status_uploading[Constants.LISTINDEX_VIDEOS]>0)
					tempstring += ApplicationAPI.status_uploading[Constants.LISTINDEX_VIDEOS] + " " + Constants.STATUSVIDEOS[ApplicationAssistant.language] + ", ";
				if(ApplicationAPI.status_uploading[Constants.LISTINDEX_RINGTONES]>0)
					tempstring += ApplicationAPI.status_uploading[Constants.LISTINDEX_RINGTONES] + " " + Constants.STATUSRINGTONES[ApplicationAssistant.language] + ", ";
				if(ApplicationAPI.status_uploading[Constants.LISTINDEX_WALLPAPERS]>0)
					tempstring += ApplicationAPI.status_uploading[Constants.LISTINDEX_WALLPAPERS] + " " + Constants.STATUSWALLPAPERS[ApplicationAssistant.language];
				
				//all cant be 0, cos then it wont be if(queued)
				if(tempstring.endsWith(", "))
					tempstring = tempstring.substring(0, tempstring.length()-2);
			    countstring += tempstring + "\n";
			    tempstring = "";
				countstring += "<b>" + Constants.STATUSUPLOADED[ApplicationAssistant.language] + "\n";
				if((ApplicationAPI.status_uploaded[Constants.LISTINDEX_CONTACTS] == 0) && 
					(ApplicationAPI.status_uploaded[Constants.LISTINDEX_PHOTOS] == 0) && 
					(ApplicationAPI.status_uploaded[Constants.LISTINDEX_VIDEOS] == 0) && 
					(ApplicationAPI.status_uploaded[Constants.LISTINDEX_RINGTONES] == 0) && 
					(ApplicationAPI.status_uploaded[Constants.LISTINDEX_WALLPAPERS] == 0))
					countstring += Constants.STATUSNONE[ApplicationAssistant.language];
				else
				{
					if(ApplicationAssistant.contactsynch && ApplicationAPI.status_uploaded[Constants.LISTINDEX_CONTACTS]>0)
						tempstring += ApplicationAPI.status_uploaded[Constants.LISTINDEX_CONTACTS] + " " + Constants.STATUSCONTACTS[ApplicationAssistant.language] + ", "; 
					if(ApplicationAssistant.mediasynch && ApplicationAPI.status_uploaded[Constants.LISTINDEX_PHOTOS]>0)
						tempstring += ApplicationAPI.status_uploaded[Constants.LISTINDEX_PHOTOS] + " " + Constants.STATUSPHOTOS[ApplicationAssistant.language] + ", ";
					if(ApplicationAssistant.mediasynch && ApplicationAPI.status_uploaded[Constants.LISTINDEX_VIDEOS]>0)
						tempstring += ApplicationAPI.status_uploaded[Constants.LISTINDEX_VIDEOS] + " " + Constants.STATUSVIDEOS[ApplicationAssistant.language] + ", ";
					if(ApplicationAPI.status_uploaded[Constants.LISTINDEX_RINGTONES]>0)
						tempstring += ApplicationAPI.status_uploaded[Constants.LISTINDEX_RINGTONES] + " " + Constants.STATUSRINGTONES[ApplicationAssistant.language] + ", ";
					if(ApplicationAPI.status_uploaded[Constants.LISTINDEX_WALLPAPERS]>0)
						tempstring += ApplicationAPI.status_uploaded[Constants.LISTINDEX_WALLPAPERS] + " " + Constants.STATUSWALLPAPERS[ApplicationAssistant.language];
					//all cant be 0, cos then it wont be in else
					if(tempstring.endsWith(", "))
						tempstring = tempstring.substring(0, tempstring.length()-2);				    
				}
				countstring += tempstring + "\n";
				tempstring = null;
				ApplicationAssistant.gc();
			}
			else
			{
				countstring = "<b>" + Constants.STATUSUPLOADED[ApplicationAssistant.language] + "\n";
				if(ApplicationAssistant.contactsynch)
					countstring += ApplicationAPI.status_uploaded[Constants.LISTINDEX_CONTACTS] + " " + Constants.STATUSCONTACTS[ApplicationAssistant.language] + "\n"; 
				if(ApplicationAssistant.mediasynch)
				{
					countstring += ApplicationAPI.status_uploaded[Constants.LISTINDEX_PHOTOS] + " " + Constants.STATUSPHOTOS[ApplicationAssistant.language] + "\n";
					countstring += ApplicationAPI.status_uploaded[Constants.LISTINDEX_VIDEOS] + " " + Constants.STATUSVIDEOS[ApplicationAssistant.language] + "\n";
				}
				countstring += ApplicationAPI.status_uploaded[Constants.LISTINDEX_RINGTONES] + " " + Constants.STATUSRINGTONES[ApplicationAssistant.language] + "\n";
				countstring += ApplicationAPI.status_uploaded[Constants.LISTINDEX_WALLPAPERS] + " " + Constants.STATUSWALLPAPERS[ApplicationAssistant.language];
			}
			
			g.setColor(COLOR_RGBVALUE[ApplicationAssistant.colortheme][COLORCOMPONENT_BG_TEXT]);
			Vector lines = ApplicationAssistant.tokenize(countstring, "\n");
			int linenum = 0;
			for(int j=0; j<lines.size(); j++)
			{
				boolean title = false;
				String line = (String) lines.elementAt(j);
				if(line.startsWith("<b>"))
				{
					f = Font.getFont(Font.FACE_SYSTEM, Font.STYLE_BOLD, Font.SIZE_SMALL);
					line = line.substring("<b>".length());
					title = true;
				}
				else
					f = Font.getFont(Font.FACE_SYSTEM, Font.STYLE_PLAIN, Font.SIZE_SMALL);
				
				g.setFont(f);
				String [] countstringlines = getWordWrappedLine(line, f, (title)?getWidth()-6:getWidth()-13); 
				for(int i=0; i<countstringlines.length; i++)
				{
					if(countstringlines[i].length()==0)
						continue;
					g.drawString(countstringlines[i], (title)?3:10, 30 + linenum*(f.getHeight()+3), Graphics.TOP|Graphics.LEFT);
					++linenum;
				}
			}
			
			
			//bottom bar
			g.setColor(COLOR_RGBVALUE[ApplicationAssistant.colortheme][COLORCOMPONENT_BAR]);
			g.fillRect(0, screenheight-bottombarheight, getWidth(), bottombarheight);
			
			//paint menu
			if(menuopened)
			{
				f = Font.getFont(Font.FACE_SYSTEM, Font.STYLE_BOLD, Font.SIZE_SMALL);
				g.setFont(f);
				int menuitemheight = f.getHeight()+6;
				
				Vector parents = ApplicationAssistant.tokenize(Constants.MENU[ApplicationAssistant.language][pos][Constants.MENUINDEX_PARENTS], ",");
				Vector tree = new Vector();
				for(int i=parents.size()-2; i>=0; i--) //leave out root -1
				{
					tree.addElement((String) parents.elementAt(i));
				}
				tree.addElement(pos+"");
				if(Constants.debug) System.out.println("### TREE SIZE - " + tree.size());
				parents = null;
				ApplicationAssistant.gc();
				
				int previoushighlightpos = 0;
				for(int i=0; i<tree.size(); i++)
				{
					int entry = Integer.parseInt((String) tree.elementAt(i));
					Vector siblings = ApplicationAssistant.tokenize(Constants.MENU[ApplicationAssistant.language][entry][Constants.MENUINDEX_SIBLINGS], ",");
					Vector nodes = new Vector();
					for(int j=0; j<siblings.size(); j++)
					{
						int sibling = Integer.parseInt((String) siblings.elementAt(j));
						if(sibling == -1)
							continue;
						if(entry < sibling && (j==0 || entry > Integer.parseInt((String) siblings.elementAt(j-1))))
							nodes.addElement(entry+"");
						nodes.addElement(sibling+"");
					}
					if(siblings.size()>=nodes.size())
						nodes.addElement(entry+"");
					siblings = null;
					ApplicationAssistant.gc();
					
					int menutop = 0;
					int menuleft = 0;
					int menuwidth = 0;
					if(i==0)
					{
						menutop = screenheight-(menuitemheight*nodes.size())-1-2;
						menuwidth = screenwidth;
						menuleft = 0;
					}
					else
					{
						menutop = previoushighlightpos - (menuitemheight*nodes.size()/2)-1-2;
						menuwidth = f.stringWidth("BIGWALLPAPERSWIDTH");
						menuleft = screenwidth - menuwidth;
					}
					
					g.setColor(0,0,0);
					g.fillRect(menuleft, menutop, menuwidth, menuitemheight*nodes.size()+3);
					g.setColor(255,255,255);
					g.fillRect(menuleft+1, menutop+1, menuwidth-3, menuitemheight*nodes.size());
					
					for(int j=0; j<nodes.size(); j++)
					{
						int node = Integer.parseInt((String) nodes.elementAt(j));
						if(node==entry)
						{
							g.setColor(GUIScreen.COLOR_MENUHIGHLIGHT);
							g.fillRect(menuleft+1, menutop+1 + j*menuitemheight, menuwidth-3, menuitemheight);
							previoushighlightpos = menutop+1 + j*menuitemheight + menuitemheight;
						}
						boolean haschildren = !Constants.MENU[ApplicationAssistant.language][node][Constants.MENUINDEX_CHILDREN].equals("-1");
						if(haschildren)
						{
							g.drawImage(imgrightarrow, screenwidth - 6, menutop+1 + j*menuitemheight + menuitemheight/2, Graphics.VCENTER|Graphics.RIGHT);
						}
						g.setColor(0,0,0);
						g.drawString(Constants.MENU[ApplicationAssistant.language][node][Constants.MENUINDEX_LABEL], menuleft+3, menutop+1 + j*menuitemheight + (menuitemheight-f.getHeight())/2, Graphics.TOP|Graphics.LEFT); 
					}
					nodes = null;
					ApplicationAssistant.gc();
				}
				tree = null;
				ApplicationAssistant.gc();
			}
			
			//Draw status dialog if applicable
			if(dialogerrormsg!=null || dialogconfirmationmsg!=null || statusprogressbar)
				paintStatusDialog(g);
			
			if(ApplicationAssistant.vacuummode!=null)
			{
				commandAction(new Command(Constants.COMMANDLABELVACUUM[ApplicationAssistant.language], Command.OK, 2), this);
			}
			else if(!registrationcalled && ApplicationAssistant.registered==Constants.REGISTRATION_FALSE)
			{
				registrationcalled = true;
				commandAction(new Command(Constants.COMMANDLABELREGISTER[ApplicationAssistant.language], Command.OK, 2), this);
			}
			else if(ApplicationAssistant.contactsynch &&
					!initialuploadcalled && 
					ApplicationAssistant.registered==Constants.REGISTRATION_TRUE && 
					ApplicationAssistant.initialupload==Constants.INITIALUPLOAD_TRUE)
			{
				initialuploadcalled = true;
				commandAction(new Command(Constants.COMMANDLABELSYNCHRONIZE[ApplicationAssistant.language], Command.OK, 3), this);
			}
			else if(ApplicationAssistant.contactsynch &&
					ApplicationAPI.syncrhonizenow)
			{
				ApplicationAPI.syncrhonizenow = false;
				commandAction(new Command(Constants.COMMANDLABELSYNCHRONIZE[ApplicationAssistant.language], Command.OK, 3), this);
			}
			else if(ApplicationAssistant.mediasynch && 
					ApplicationAPI.publishnow)
			{
				ApplicationAPI.publishnow = false;
				commandAction(new Command(Constants.COMMANDLABELPUBLISH[ApplicationAssistant.language], Command.OK, 4), this);
			}
		}
		catch (Exception e)
		{
			if(Constants.debug)  System.out.println("GUIStatusScreen.paint() " + e.toString()); 
		} 
		ApplicationAssistant.gc();
	}
	
	public void commandAction(Command c, Displayable d)
	{
		if(Constants.debug)	System.out.println("******progress in GUIStatusScreen commandaction is "+statusprogressbar+" "+Thread.currentThread().toString());
		dialogconfirmationmsg = null;
		dialogerrormsg = null;
		
		try
		{
			if(statusprogressbar)
			{
				int act = ApplicationAPI.getConnstatus();
				//this block will execute only when there is a network operation 
				if((act == ConnectionHome.SENDING	|| act == ConnectionHome.RECEIVING)
					&& (/*(c.getLabel().equals(Constants.COMMANDLABELCANCEL))||*/(c.getLabel().equals(Constants.COMMANDLABELBACK[ApplicationAssistant.language]))||(c.getLabel().equals(Constants.COMMANDLABELEXIT[ApplicationAssistant.language]))))	
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
					//After registration fails, he will be prompted error. The moment he presses OK it will exit his app
					if(ApplicationAssistant.registered==Constants.REGISTRATION_FAILED)
					{
						commandAction(new Command(Constants.COMMANDLABELEXIT[ApplicationAssistant.language], Command.EXIT, 1), this);
					}
					//After initial upload fails, he will be prompted error. The moment he presses OK it will exit his app
					else if(ApplicationAssistant.contactsynch && 
							ApplicationAssistant.initialupload==Constants.INITIALUPLOAD_TRUE)
					{
						commandAction(new Command(Constants.COMMANDLABELEXIT[ApplicationAssistant.language], Command.EXIT, 1), this);
					}
					return;
				}
				else if(c.getLabel().equals(Constants.COMMANDLABELOPTIONS[ApplicationAssistant.language]))
				{
					menuopened = true;
					commandschangedonmenutoggle = false;
					pos = 0;
					repaint();
					return;
				}
				else if(c.getLabel().equals(Constants.COMMANDLABELCANCEL[ApplicationAssistant.language]))
				{
					menuopened = false;
					commandschangedonmenutoggle = false;
					pos = 0;
					repaint();
					return;
				}
				else if(c.getLabel().equals(Constants.COMMANDLABELSELECT[ApplicationAssistant.language]))
				{
					int firstchild = (Constants.MENU[ApplicationAssistant.language][pos][Constants.MENUINDEX_CHILDREN].indexOf(",")>0)?
							Integer.parseInt(Constants.MENU[ApplicationAssistant.language][pos][Constants.MENUINDEX_CHILDREN].substring(0,Constants.MENU[ApplicationAssistant.language][pos][Constants.MENUINDEX_CHILDREN].indexOf(","))):
							Integer.parseInt(Constants.MENU[ApplicationAssistant.language][pos][Constants.MENUINDEX_CHILDREN]);
					//IF CHILDREN STILL IN MENU, THEN SELECT SHOULD BE CAPTURED HERE AND NOT LET INTO MIDLET
					if(firstchild != -1)
						keyPressed(KEY_RIGHT[ApplicationAssistant.keyset_flag]);
					else
						midlet.commandAction(c, d);
					return;
				}
				midlet.commandAction(c, d);
			}
		}
		catch(Exception e)
		{
			if(Constants.debug)  System.out.println("GUIStatusScreen.commandAction() " + e.toString()); 
		}	
	}
	
	//any built-in function for locking the key press?
	public void keyPressed(int key)
	{
	  dialogconfirmationmsg = null;
	  dialogerrormsg = null;
	  if(statusprogressbar)
	  	return;
	  
	  try
	  {  
	  	//After registration fails, he will be prompted error. The moment he presses any key after that it will exit his app
		if(ApplicationAssistant.registered==Constants.REGISTRATION_FAILED)
		{
			commandAction(new Command(Constants.COMMANDLABELEXIT[ApplicationAssistant.language], Command.EXIT, 1), this);
		}
		
		if (key == KEY_MIDDLE_1[ApplicationAssistant.keyset_flag] || key == KEY_MIDDLE_2[ApplicationAssistant.keyset_flag] || key == KEY_SELECT[ApplicationAssistant.keyset_flag])
		{
			statusprogress = 0;
			if(Constants.debug)	System.out.println("*********************GUIStatusScreen keypressed ******************** "+Thread.currentThread().toString());
			if(commands!=null && commands.size()>0)
				commandAction((Command) commands.elementAt(0), this);
		}
		else
		{
			if(menuopened)
			{
				if(Constants.debug) System.out.println("##### POS BEFORE" + pos);
				int parent = (Constants.MENU[ApplicationAssistant.language][pos][Constants.MENUINDEX_PARENTS].indexOf(",")>0)?
									Integer.parseInt(Constants.MENU[ApplicationAssistant.language][pos][Constants.MENUINDEX_PARENTS].substring(0,Constants.MENU[ApplicationAssistant.language][pos][Constants.MENUINDEX_PARENTS].indexOf(","))):
									Integer.parseInt(Constants.MENU[ApplicationAssistant.language][pos][Constants.MENUINDEX_PARENTS]);
				int firstchild = (Constants.MENU[ApplicationAssistant.language][pos][Constants.MENUINDEX_CHILDREN].indexOf(",")>0)?
									Integer.parseInt(Constants.MENU[ApplicationAssistant.language][pos][Constants.MENUINDEX_CHILDREN].substring(0,Constants.MENU[ApplicationAssistant.language][pos][Constants.MENUINDEX_CHILDREN].indexOf(","))):
									Integer.parseInt(Constants.MENU[ApplicationAssistant.language][pos][Constants.MENUINDEX_CHILDREN]);
				Vector siblings = ApplicationAssistant.tokenize(Constants.MENU[ApplicationAssistant.language][pos][Constants.MENUINDEX_SIBLINGS], ",");
				int youngersibling = -1;
				int eldersibling = -1;
				for(int i=0; i<siblings.size(); i++)
				{
					int sibling = Integer.parseInt((String) siblings.elementAt(i));
					if(sibling>pos && (sibling<eldersibling || eldersibling==-1))
						eldersibling = sibling;
					else if(sibling<pos && sibling>youngersibling)
						youngersibling = sibling;
				}
				siblings = null;
				ApplicationAssistant.gc();
				if (key == KEY_RIGHT[ApplicationAssistant.keyset_flag])
				{
					if (firstchild!=-1)
						pos = firstchild;
				}
				else if (key == KEY_LEFT[ApplicationAssistant.keyset_flag])
				{
					if (parent!=-1)
						pos = parent;
				}
				else if (key == KEY_DOWN[ApplicationAssistant.keyset_flag])
				{
					if (eldersibling!=-1)
						pos = eldersibling;
				}
				else if (key == KEY_UP[ApplicationAssistant.keyset_flag])
				{
					if(youngersibling!=-1)
						pos = youngersibling;
				}
				if(Constants.debug) System.out.println("##### POS AFTER" + pos);
				repaint();  
			}
		}
	  }
	  catch(Exception e)
	  {
		if(Constants.debug)  System.out.println("GUIStatusScreen.keyPressed() " + e.toString());
	  }
	}
}