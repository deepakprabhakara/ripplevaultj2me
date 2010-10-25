package vault.gui;

import java.util.Random;
import java.util.Vector;

import javax.microedition.lcdui.Canvas;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Font;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;
import javax.microedition.lcdui.ImageItem;

import vault.app.ApplicationAssistant;
import vault.util.Constants;


public abstract class GUIScreen extends Canvas implements CommandListener {

	protected VaultMIDLET midlet;
	public String screenname;	
	protected final int screenwidth = getWidth();
	protected final int screenheight = getHeight();
	
	protected Vector commands;
	
	protected final int[] KEY_UP = {-1, -1};
	protected final int[] KEY_DOWN = {-2, -6};
	protected final int[] KEY_RIGHT = {-4, -5};
	protected final int[] KEY_LEFT = {-3, -2};
	protected final int[] KEY_SELECT = {-5, -20};
	protected final int[] KEY_MIDDLE_1 = {13, -23}; //The middle Constant for Sony Ericsson P800
	protected final int[] KEY_MIDDLE_2 = { 32, 32};
	
	// MOTOROLA keyset
//	protected final int KEY_DOWN = -6;
//	protected final int KEY_RIGHT = -5;
//	protected final int KEY_LEFT = -2;
//	protected final int KEY_SELECT = -20;
//	protected final int KEY_MIDDLE_1 = -23; //The middle Constant for Motorola

	private final int gaugewidth = getWidth() - 3 /*start*/ - 4 /*end*/ - 4 /*gap between image and text*/ - 28 /*image*/;
	private final int gaugeheight = 8;
	
	protected int statusprogress = 0;
	protected boolean statusprogressbar = false;
	protected String dialogerrormsg = null; //error
	protected String dialogconfirmationmsg = null; //confirmation
	protected String dialogprogressbarlabel = null;
	public boolean firsttime = false;
	public boolean secondtime = false;
	protected String[] keypad;
	protected int pos;

	protected final static int COLORCOMPONENT_BG = 0;
	protected final static int COLORCOMPONENT_HIGHLIGHT = 1;
	protected final static int COLORCOMPONENT_CALLOUT = 2;
	protected final static int COLORCOMPONENT_BAR = 3;
	
	protected final static int COLORCOMPONENT_BG_TEXT = 4;
	protected final static int COLORCOMPONENT_HIGHLIGHT_TEXT = 5;
	protected final static int COLORCOMPONENT_CALLOUT_TEXT = 6;
	protected final static int COLORCOMPONENT_BAR_TEXT = 7;
	
	public final static int [][] COLOR_RGBVALUE = {
		{0xB3DBE2, 0xD5E9FB, 0x245BA5, 0x7BA6CE, 0x000000, 0x000000, 0xFFFFFF, 0x000000}, //blue
		{0xE7FB94, 0xA9C159, 0x9CC318, 0x638210, 0x000000, 0x000000, 0x000000, 0xFFFFFF}, //green
		{0xFFC363, 0xFFD79C, 0xFF6500, 0xDE5900, 0x000000, 0x000000, 0x000000, 0xFFFFFF}, //orange
//		{0xFFB2B5, 0xFFA2A6, 0xFF696B, 0xFF4142, 0x000000, 0x000000, 0x000000, 0xFFFFFF}, //red
//		{0xFFD3F7, 0xFF8DE1, 0xDE049C, 0xBD0284, 0x000000, 0x000000, 0x000000, 0xFFFFFF}, //pink
//		{0xC6B2EF, 0xD6C7F7, 0x6B34D6, 0x5228AD, 0x000000, 0x000000, 0x000000, 0xFFFFFF}, //brown
//		{0xAD714A, 0xFFCF9C, 0x945129, 0x80370B, 0x000000, 0x000000, 0xFFFFFF, 0xFFFFFF}, //purple
	};
	
	public final static int COLOR_MENUHIGHLIGHT = 0xADB2FF;
	public final static int COLOR_BGSPLASH = 0xB3DBE2;
	
	public GUIScreen() 
	{
		statusprogress = 0;
		pos = 0;
		keypad = new String[10];
		keypad[0] = "0 ";
		keypad[1] = "1 ";
		keypad[2] = "2AaBbCc";
		keypad[3] = "3DdEeFf";
		keypad[4] = "4GgHhIi";
		keypad[5] = "5JjKkLl";
		keypad[6] = "6MmNnOo";
		keypad[7] = "7PpQqRrSs";
		keypad[8] = "8TtUuVv";
		keypad[9] = "9WwXxYyZz";
	}

	public String getScreenname()
	{
		return screenname;
	}
	
	public void setColorTheme()
	{
		if(ApplicationAssistant.prevscreenname == null || screenname.equals(Constants.SCREENSTATUS) || screenname.equals(Constants.SCREENSPLASH))
			ApplicationAssistant.colortheme = 0;
		else if(!screenname.equals(ApplicationAssistant.prevscreenname))
			ApplicationAssistant.colortheme = (ApplicationAssistant.colortheme + Math.abs((new Random()).nextInt()))%COLOR_RGBVALUE.length;
		ApplicationAssistant.prevscreenname = screenname;
	}
	
	public void paintStatusDialog(Graphics g)
	{
		try
		{	
			Font f = Font.getFont(Font.FACE_SYSTEM  , Font.STYLE_PLAIN, Font.SIZE_SMALL);
			int height = (f.getHeight()+2) * 3 + 5;
			g.setColor(0,0,0); //shadow
			g.drawRect(0, getHeight()-height-2,getWidth()-2,height);
			g.drawRect(1, getHeight()-height-1,getWidth()-2,height);
			g.setColor(COLOR_RGBVALUE[ApplicationAssistant.colortheme][COLORCOMPONENT_HIGHLIGHT]);
			g.fillRect(1, getHeight()-height-1,getWidth()-3,height-1);
			
			int verticaloffset = getHeight() - height + 4; //where to start writing
			String text = "";
			ImageItem image = null;
			if(statusprogressbar)
			{
				showProgressBarCommands();
				text = dialogprogressbarlabel;
				image = new ImageItem("", Image.createImage(Constants.iconprogress), ImageItem.LAYOUT_DEFAULT, "");
				g.setColor(COLOR_RGBVALUE[ApplicationAssistant.colortheme][COLORCOMPONENT_BAR]);
				g.drawRect(3, verticaloffset + f.getHeight()/2 - gaugeheight/2, gaugewidth, gaugeheight);
				g.fillRect(3, verticaloffset + f.getHeight()/2 - gaugeheight/2, statusprogress, gaugeheight);
				verticaloffset += (f.getHeight() + 2);
			}
			else if(dialogconfirmationmsg != null)
			{
				showInfoCommands();
				text = dialogconfirmationmsg;
				image = new ImageItem("", Image.createImage(Constants.iconconfirmation), ImageItem.LAYOUT_DEFAULT, "");
			}
			else if(dialogerrormsg != null)
			{
				showInfoCommands();
				text = dialogerrormsg;
				image = new ImageItem("", Image.createImage(Constants.iconerror), ImageItem.LAYOUT_DEFAULT, "");
			}

			g.drawImage(image.getImage(), 
					 getWidth() - 3, 
					 getHeight() - height + 1, 
					 Graphics.TOP | Graphics.RIGHT);

			g.setColor(COLOR_RGBVALUE[ApplicationAssistant.colortheme][COLORCOMPONENT_HIGHLIGHT_TEXT]);
			g.setFont(f);
			
			int linenum = 0;
			int linewidth = 0;
			while (text.length() > 0) 
			{
				if(verticaloffset + (f.getHeight()+2) * linenum >= getHeight() - height + 1 + image.getImage().getHeight())
					linewidth = getWidth() -3 -4;
				else
					linewidth = getWidth() -3 -4 -4 - image.getImage().getWidth();
			    String [] tmp = getWordWrappedLine(text, f, linewidth);
			    g.drawString(tmp[0], 3, verticaloffset + (f.getHeight()+2) * linenum, Graphics.TOP | Graphics.LEFT);
			    text = tmp[1];
				++linenum;
				if(verticaloffset + (f.getHeight()+2) * linenum + f.getHeight() > getHeight() - 3)
					break;
			}
			
			dialogconfirmationmsg = null;
			dialogerrormsg = null;
		}
		catch(Exception e)
		{
			if(Constants.debug)  System.out.println("GUIScreen.paintStatusDialog() " + e.toString());
		}
		
	}
			
	protected String [] getWordWrappedLine(String line, Font f, int linewidth)
	{
		String tmpline = "";
		String word = "";
		int cursor = 0;
		int prevcursor = 0;
		while (cursor < line.length()) 
		{
			if (line.charAt(cursor) == ' ')
				++cursor;

			while (cursor < line.length()
					&& line.charAt(cursor) != ' ')
				++cursor;

			word = line.substring(prevcursor, cursor);

			if (f.stringWidth(tmpline + word) <= linewidth) 
			{
				tmpline = tmpline + word;
				prevcursor = cursor;
			} 
			else 
			{
				cursor = prevcursor;
				break;
			}
		}

		if (cursor == line.length())
			line = "";
		else
			line = line.substring(cursor + 1);
		
		return new String [] {tmpline, line};
	}
	
	public void setStatusProgress(int statusprogress)
	{
		//in pixels
		if(statusprogress>(gaugewidth-3))
			return;
		else
		{
			this.statusprogress = statusprogress;
			repaint();
		}
	}
	

	protected void removeCommands()
	{
		for(int i=0; commands!=null && i<commands.size(); i++)
			this.removeCommand((Command) commands.elementAt(i));
		this.commands = null;
		ApplicationAssistant.gc();
	}
	
	protected void addCommands()
	{
		for(int i=0; commands!=null && i<commands.size(); i++)
			this.addCommand((Command) commands.elementAt(i));
	}
	protected abstract void showScreenCommands();
	protected void showProgressBarCommands()
	{
		//if commands are there then no need to remove and readd
		if(this.commands!=null && this.commands.size()==1 && ((Command) commands.elementAt(0)).getLabel().equals(Constants.COMMANDLABELCANCEL[ApplicationAssistant.language]))
			return;
		
		this.removeCommands();
		this.commands = new Vector();
		commands.addElement(new Command(Constants.COMMANDLABELCANCEL[ApplicationAssistant.language], Command.BACK, 1));
		this.addCommands();
	}
	protected void showInfoCommands()
	{
		//if commands are there then no need to remove and readd
		if(this.commands!=null && this.commands.size()==1 && ((Command) commands.elementAt(0)).getLabel().equals(Constants.COMMANDLABELOK[ApplicationAssistant.language]))
			return;
		
		this.removeCommands();
		this.commands = new Vector();
		commands.addElement(new Command(Constants.COMMANDLABELOK[ApplicationAssistant.language], Command.BACK, 1));
		this.addCommands();
	}
	
	public int getSelectedIndex()
	{
		return pos;
	}
	
	protected abstract void cleanUp();
}