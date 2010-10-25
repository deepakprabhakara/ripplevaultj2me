package vault.gui;
import java.io.IOException;
import java.util.Hashtable;
import java.util.Timer;
import java.util.TimerTask;

import javax.microedition.io.ConnectionNotFoundException;
import javax.microedition.io.Connector;
import javax.microedition.io.PushRegistry;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Display;
import javax.microedition.lcdui.Displayable;
import javax.microedition.midlet.MIDlet;

import javax.wireless.messaging.BinaryMessage;
import javax.wireless.messaging.Message;
import javax.wireless.messaging.MessageConnection;
import javax.wireless.messaging.MessageListener;
import javax.wireless.messaging.TextMessage;

import vault.app.ApplicationAPI;
import vault.app.ApplicationAssistant;
import vault.util.Constants;

public class VaultMIDLET extends MIDlet implements CommandListener, MessageListener 
{
	public GUIScreen guiscreen;
	public static Display display;
	private LogicThread logicthread = null;
	
	private Timer timer = null;
	private GaugeTask gaugetask = null;
	
    /** SMS message connection for inbound text messages. */
    MessageConnection smsconn;
    private boolean startInVacuumMode = false;
	
	public VaultMIDLET()
	{
		super();
		display = Display.getDisplay(this);
	}

	public void notifyIncomingMessage(MessageConnection conn) 
	{
		startInVacuumMode = true;
		if(guiscreen==null)
			run();
		processIncomingMessage();
	}

	public void startApp()
	{
		String smsPort = getAppProperty("SMS-Port");
		String smsConnection = "sms://:" + smsPort;
		if (smsconn == null) {
			try {
				smsconn = (MessageConnection) Connector.open(smsConnection);
				smsconn.setMessageListener(this);
			} catch (IOException ioe) {
				if(Constants.debug)  System.out.println("VaultMIDLET.startApp() " + ioe.toString());
			}
		}
		
	    String [] connections = PushRegistry.listConnections(true); 
	    for (int i = 0; connections!=null && i < connections.length; i++) 
	    {
			if(connections[i].equals(smsConnection))
			{
				startInVacuumMode = true;
				break;
	    	}
		}
	    
		run();
	}
	public void pauseApp()
	{
	}
	void quit()
	{
		destroyApp(true);
		
		try{
			scheduleMIDlet();
		}catch(Exception e){}
		
		notifyDestroyed();
	}
	private void processIncomingMessage()
	{
		try {
			Message msg = smsconn.receive();
			if (msg != null) {
				String smsstring = "";
				if (msg instanceof TextMessage) {
					smsstring = ((TextMessage)msg).getPayloadText();
					smsstring.toLowerCase();
				}
				else if(msg instanceof BinaryMessage) {           	            
					smsstring = new String(((BinaryMessage)msg).getPayloadData());
					smsstring.toLowerCase();
				}
				
				smsstring = smsstring.trim();
				if(smsstring.startsWith(Constants.VACUUM_CONTACTS_COMMAND + " "))
					initiateVacuum(Constants.VACUUM_CONTACTS_COMMAND, smsstring);
				else if(smsstring.startsWith(Constants.VACUUM_PHOTOS_COMMAND + " "))
					initiateVacuum(Constants.VACUUM_PHOTOS_COMMAND, smsstring);
				else if(smsstring.startsWith(Constants.VACUUM_VIDEOS_COMMAND + " "))
					initiateVacuum(Constants.VACUUM_VIDEOS_COMMAND, smsstring);
				else if(smsstring.startsWith(Constants.VACUUM_MEDIA_COMMAND + " "))
					initiateVacuum(Constants.VACUUM_MEDIA_COMMAND, smsstring);
				else if(smsstring.startsWith(Constants.VACUUM_RINGTONES_COMMAND + " "))
					initiateVacuum(Constants.VACUUM_RINGTONES_COMMAND, smsstring);
				else if(smsstring.startsWith(Constants.VACUUM_WALLPAPERS_COMMAND + " "))
					initiateVacuum(Constants.VACUUM_WALLPAPERS_COMMAND, smsstring);
				else if(smsstring.startsWith(Constants.VACUUM_CONTENT_COMMAND + " "))
					initiateVacuum(Constants.VACUUM_CONTENT_COMMAND, smsstring);
				else if(smsstring.startsWith(Constants.VACUUM_COMMAND + " ")) //HAS to be last condition as its superset
					initiateVacuum(Constants.VACUUM_COMMAND, smsstring);
				
				smsstring = null;
				msg = null;
			}
		} catch (IOException e) {
			if(Constants.debug)  System.out.println("VaultMIDLET.notifyIncomingMessage() " + e.toString());
		}		
	}
	
	public void destroyApp(boolean destroy)
	{
		cleanUp();
//		try{
//			scheduleMIDlet();
//		}catch(Exception e){}
	}
	public void cleanUp()
	{
		if(Constants.debug) System.out.println("VaultAPI.cleanUp()");
		ApplicationAPI.contactdtos_from_server = null;
		guiscreen = null;
		ApplicationAssistant.gc();
	}
	public void run()
	{
		if(Constants.debug) System.out.println("VaultAPI.run()");
		try
		{
			Hashtable properties = new Hashtable();
			properties.put("jadip", getAppProperty("MIDlet-Jar-URL"));
			properties.put("version", getAppProperty("MIDlet-Version"));
			properties.put("vendor", getAppProperty("MIDlet-Vendor"));
			properties.put("contacts_url", getAppProperty("contacts_url"));
			properties.put("registration_url", getAppProperty("registration_url"));
			properties.put("media_pkt_upload_url", getAppProperty("media_pkt_upload_url"));
			properties.put("media_gallery_url", getAppProperty("media_gallery_url"));
			properties.put("sub_status_check", getAppProperty("sub_status_check"));
			properties.put("phone_model", getAppProperty("phone_model"));
			properties.put("userid", getAppProperty("userid"));
			properties.put("password", getAppProperty("password"));
			properties.put("hp", getAppProperty("hp"));
			properties.put("appid", getAppProperty("appid"));
			properties.put("screensize", getAppProperty("screensize"));
			String language = getAppProperty("language");
			if(language.equals("en"))
				language = Constants.LANGUAGE_ENGLISH + "";
			else if(language.equals("zh"))
			language = Constants.LANGUAGE_CHINESE + "";
			properties.put("language", language);
			String tmp = getAppProperty("contactsynch");
			if(tmp==null) tmp = "true";
			properties.put("contactsynch", tmp);
			tmp = getAppProperty("mediasynch");
			if(tmp==null) tmp = "true";
			properties.put("mediasynch", tmp);
			tmp = getAppProperty("smssynch");
			if(tmp==null) tmp = "true";
			properties.put("smssynch", tmp);
			tmp = getAppProperty("SSL");
			if(tmp==null) tmp = "false";
			properties.put("SSL", tmp);
			tmp = getAppProperty("keyset");
			if(tmp==null) tmp = "default";
			properties.put("keyset", tmp);
			
			ApplicationAPI.initialise(properties);
//			InputStream in = getClass().getResourceAsStream(Constants.resourcefile_userdata);
			ApplicationAssistant.gc();
		}
		catch (Exception e)
		{
			if(Constants.debug)  System.out.println("VaultAPI.run() " + e.toString());
		}
		
		if(!startInVacuumMode)
			paintSplashScreen();
	}
	public void commandAction(Command c, Displayable d)
	{
		try
		{
			if(guiscreen!=null)
				ApplicationAssistant.prevscreenname = guiscreen.getScreenname();
				
			if (c.getLabel().equals(Constants.COMMANDLABELBACK[ApplicationAssistant.language]))
				callBackCapture();
			else if (c.getLabel().equals(Constants.COMMANDLABELSELECT[ApplicationAssistant.language]))
				callSelectCapture();
			else if (c.getLabel().equals(Constants.COMMANDLABELEXIT[ApplicationAssistant.language]))
				quit();
			else if (c.getLabel().equals(Constants.COMMANDLABELNEXT[ApplicationAssistant.language]) &&
					 guiscreen.getScreenname().equals(Constants.SCREENSPLASH))
				paintStatusScreen();
			else if (c.getLabel().equals(Constants.COMMANDLABELPUBLISH[ApplicationAssistant.language]) &&
					 (guiscreen.getScreenname().equals(Constants.SCREENPHOTOVIEW) ||
					  guiscreen.getScreenname().equals(Constants.SCREENMEDIALIST)))
			{
				ApplicationAPI.publishnow = true;
				paintStatusScreen();
			}
			else if (c.getLabel().equals(Constants.COMMANDLABELCONFIRM[ApplicationAssistant.language]))
				callConfirmCapture();
			//virtual command
			else if (c.getLabel().equals(Constants.COMMANDLABELVACUUM[ApplicationAssistant.language]))
			{
				performVacuum();
				return;
			}
			else if (c.getLabel().equals(Constants.COMMANDLABELREGISTER[ApplicationAssistant.language]))
			{
				performRegistration();
				return;
			}
			//virtual command
			else if (c.getLabel().equals(Constants.COMMANDLABELSYNCHRONIZE[ApplicationAssistant.language]))
			{
				performContactSynchronisation();
				return;
			}
			//virtual command
			else if(c.getLabel().equals(Constants.COMMANDLABELPUBLISH[ApplicationAssistant.language]) &&
					guiscreen.getScreenname().equals(Constants.SCREENSTATUS))
			{
				performPublish();
				return;
			}
		}
		catch(Exception e)
		{
			if(Constants.debug)  System.out.println("VaultMIDLET.commandAction() " + e.toString()); 
		}
	}
	
	public void callSelectCapture()
	{
		try
		{
			int index = guiscreen.getSelectedIndex();
			if (guiscreen.getScreenname().equals(Constants.SCREENSTATUS))
			{
				if(index == 4)
				{
					if(ApplicationAssistant.contactsynch)
					{
						ApplicationAPI.syncrhonizenow = true;
						paintStatusScreen();
					}
					else
						paintHelpScreen(Constants.MESSAGE_UNAVAILABLE_FUNCTION[ApplicationAssistant.language]);
				}
				else if(index == 8)
				{
					if(ApplicationAssistant.contactsynch)
						paintContactSettingsScreen();
					else
						paintHelpScreen(Constants.MESSAGE_UNAVAILABLE_FUNCTION[ApplicationAssistant.language]);
				}
//				else if(index == 11)
//					paintGeneralSettingsScreen();
				else if(index == 16)
					paintChangePasswordScreen("", "", "");
				else if(index == 10)
				{
					if(ApplicationAssistant.mediasynch)
						performLoadMedia(Constants.CONTENTTYPE_PHOTO);
					else
						paintHelpScreen(Constants.MESSAGE_UNAVAILABLE_FUNCTION[ApplicationAssistant.language]);
				}
				else if(index == 11)
				{
					if(ApplicationAssistant.mediasynch)
						performLoadMedia(Constants.CONTENTTYPE_VIDEO);
					else
						paintHelpScreen(Constants.MESSAGE_UNAVAILABLE_FUNCTION[ApplicationAssistant.language]);
				}
				else if(index == 12)
					performLoadMedia(Constants.CONTENTTYPE_RINGTONE);
				else if(index == 13)
					performLoadMedia(Constants.CONTENTTYPE_WALLPAPER);			
				else if(index == 14 || index == 15)
				{
					if(ApplicationAssistant.smssynch)
						paintHelpScreen(Constants.MESSAGE_SMS_BACKUP[ApplicationAssistant.language]);
					else
						paintHelpScreen(Constants.MESSAGE_UNAVAILABLE_FUNCTION[ApplicationAssistant.language]);
				}
				else if(index == 2)
				{
					ApplicationAPI.dialogconfirmation = Constants.apptitle[ApplicationAssistant.language] + " " + ApplicationAssistant.version + " (c)" + ApplicationAssistant.vendor;
					paintStatusScreen();
				}
				else if(index == 3)
				{					
					paintHelpScreen(Constants.MESSAGE_HELP[ApplicationAssistant.language]);										
				}
				return;			
			}
		}
		catch(Exception e)
		{
			if(Constants.debug)  System.out.println("VaultMIDLET.callselectCapture() " + e.toString());
		}
	}
	private void callBackCapture()
	{
		if(guiscreen.getScreenname().equals(Constants.SCREENPHOTOVIEW))
		{
			paintStatusScreen();			
		}
		else if(guiscreen.getScreenname().equals(Constants.SCREENMEDIALIST))
		{
			paintStatusScreen();			
		}
		else if(guiscreen.getScreenname().equals(Constants.SCREENHELP))
		{
			paintStatusScreen();		
		}	
		else if(guiscreen.getScreenname().equals(Constants.SCREENCHANGEPASSWORD))
		{
			paintStatusScreen();		
		}	
	}
	
	private void callConfirmCapture()
	{
		if(guiscreen.getScreenname().equals(Constants.SCREENCHANGEPASSWORD))
		{
			performChangePassword(((GUIChangePasswordScreen) guiscreen).getNewpassword(), 
					((GUIChangePasswordScreen) guiscreen).getConfirmpassword(),
					((GUIChangePasswordScreen) guiscreen).getCurrentpassword());
			return;
		}
		else if (guiscreen.getScreenname().equals(Constants.SCREENCONTACTSETTINGS))
		{
			//manual synch
			if(((GUIContactSettingsScreen) guiscreen).getSelected_synchmode()==1)
			{
				ApplicationAPI.updateNextScheduledSynchTime(0);
			}
			else
			{
				//back is clicked, update synchtime if required
				if(ApplicationAssistant.scheduleperiod!=((GUIContactSettingsScreen) guiscreen).getScheduleperiod()*24*3600*1000)
				{
					ApplicationAPI.updateNextScheduledSynchTime(((GUIContactSettingsScreen) guiscreen).getScheduleperiod()*24*3600*1000);
				}
			}
			paintStatusScreen();
			return;
		}
		else if (guiscreen.getScreenname().equals(Constants.SCREENGENERALSETTINGS))
		{
			ApplicationAPI.updateLanguage(((GUIGeneralSettingsScreen) guiscreen).getSelected_language());
			ApplicationAPI.status_activity = Constants.STATUS_RUNNING[ApplicationAssistant.language];
			paintStatusScreen();
			return;
		}
	}
	
	public void paintSplashScreen()
	{
		if(Constants.debug) System.out.println("VaultMIDLET.paintSplashScreen()");
		try
		{
			clearScreen();
			guiscreen = new GUISplashScreen(this);
			guiscreen.dialogerrormsg = ApplicationAPI.dialogerror;
			guiscreen.dialogconfirmationmsg = ApplicationAPI.dialogconfirmation;
			ApplicationAPI.dialogerror = null;
			ApplicationAPI.dialogconfirmation = null;
			display.setCurrent(guiscreen);	
		}
		catch (Exception e)
		{
			if(Constants.debug)  System.out.println("VaultMIDLET.paintSplashScreen() " + e.toString());
		}
	}
	
	public void paintStatusScreen()
	{
		if(Constants.debug) System.out.println("VaultMIDLET.paintStatusScreen()");
		try
		{
			clearScreen();
			guiscreen = new GUIStatusScreen(this);
			guiscreen.dialogerrormsg = ApplicationAPI.dialogerror;
			guiscreen.dialogconfirmationmsg = ApplicationAPI.dialogconfirmation;
			ApplicationAPI.dialogerror = null;
			ApplicationAPI.dialogconfirmation = null;
			display.setCurrent(guiscreen);	
		}
		catch (Exception e)
		{
			if(Constants.debug)  System.out.println("VaultMIDLET.paintStatusScreen() " + e.toString());
		}
	}
	
	public void paintContactSettingsScreen()
	{
		if(Constants.debug) System.out.println("VaultMIDLET.paintContactSettingsScreen()");
		try
		{
			clearScreen();
			guiscreen = new GUIContactSettingsScreen(this);
			guiscreen.dialogerrormsg = ApplicationAPI.dialogerror;
			guiscreen.dialogconfirmationmsg = ApplicationAPI.dialogconfirmation;
			ApplicationAPI.dialogerror = null;
			ApplicationAPI.dialogconfirmation = null;
			display.setCurrent(guiscreen);
		}
		catch (Exception e)
		{
			if(Constants.debug)  System.out.println("VaultMIDLET.paintContactSettingsScreen() " + e.toString());
		}
	}
	
	public void paintGeneralSettingsScreen()
	{
		if(Constants.debug) System.out.println("VaultMIDLET.paintGeneralSettingsScreen()");
		try
		{
			clearScreen();
			guiscreen = new GUIGeneralSettingsScreen(this);
			guiscreen.dialogerrormsg = ApplicationAPI.dialogerror;
			guiscreen.dialogconfirmationmsg = ApplicationAPI.dialogconfirmation;
			ApplicationAPI.dialogerror = null;
			ApplicationAPI.dialogconfirmation = null;
			display.setCurrent(guiscreen);
		}
		catch (Exception e)
		{
			if(Constants.debug)  System.out.println("VaultMIDLET.paintGeneralSettingsScreen() " + e.toString());
		}
	}
	
	public void paintChangePasswordScreen(String currentpassword, String newpassword, String confirmpassword)
	{
		if(Constants.debug) System.out.println("VaultMIDLET.paintChangePasswordScreen()");
		try
		{
			clearScreen();
			guiscreen = new GUIChangePasswordScreen(this, currentpassword, newpassword, confirmpassword);
			guiscreen.dialogerrormsg = ApplicationAPI.dialogerror;
			guiscreen.dialogconfirmationmsg = ApplicationAPI.dialogconfirmation;
			ApplicationAPI.dialogerror = null;
			ApplicationAPI.dialogconfirmation = null;
			display.setCurrent(guiscreen);
		}
		catch (Exception e)
		{
			if(Constants.debug)  System.out.println("VaultMIDLET.paintChangePasswordScreen() " + e.toString());
		}
	}
	
	public void paintHelpScreen(String [] txtarray)
	{
		if(Constants.debug) System.out.println("VaultMIDLET.paintHelpScreen()");
		try
		{
			clearScreen();
			guiscreen = new GUIHelpScreen(this, txtarray);
			guiscreen.dialogerrormsg = ApplicationAPI.dialogerror;
			guiscreen.dialogconfirmationmsg = ApplicationAPI.dialogconfirmation;
			ApplicationAPI.dialogerror = null;
			ApplicationAPI.dialogconfirmation = null;
			display.setCurrent(guiscreen);	
		}
		catch (Exception e)
		{
			if(Constants.debug)  System.out.println("VaultMIDLET.paintHelpScreen() " + e.toString());
		}
	}
	
	public void paintPhotoViewScreen(int contenttype)
	{
		if(Constants.debug) System.out.println("VaultMIDLET.paintPhotoViewScreen()");
		try
		{
			clearScreen();
			guiscreen = new GUIContentViewScreen(this, contenttype);
			guiscreen.dialogerrormsg = ApplicationAPI.dialogerror;
			guiscreen.dialogconfirmationmsg = ApplicationAPI.dialogconfirmation;
			ApplicationAPI.dialogerror = null;
			ApplicationAPI.dialogconfirmation = null;
			display.setCurrent(guiscreen);
		}
		catch (Exception e)
		{
			if(Constants.debug)  System.out.println("VaultMIDLET.paintPhotoViewScreen() " + e.toString());
		}
	}
	
	public void paintMediaListScreen(int contenttype)
	{
		if(Constants.debug) System.out.println("VaultMIDLET.paintMediaListScreen()");
		try
		{
			clearScreen();
			guiscreen = new GUIContentListScreen(this, contenttype);
			guiscreen.dialogerrormsg = ApplicationAPI.dialogerror;
			guiscreen.dialogconfirmationmsg = ApplicationAPI.dialogconfirmation;
			ApplicationAPI.dialogerror = null;
			ApplicationAPI.dialogconfirmation = null;
			display.setCurrent(guiscreen);
		}
		catch (Exception e)
		{
			if(Constants.debug)  System.out.println("VaultMIDLET.paintMediaListScreen() " + e.toString());
		}
	}
	
	public void performRegistration()
	{
		if(Constants.debug) System.out.println("VaultMIDLET.performRegistration()");
		try
		{
			if(Constants.debug) System.out.println("VaultMIDLET.performRegistration()");
		    Hashtable parameters = new Hashtable();
			
			logicthread = new LogicThread(this, LogicThread.REGISTRATION, parameters);
			logicthread.start();
			startTimer(100000, Constants.PROGRESS_REGISTERING[ApplicationAssistant.language]);
		}
		catch (Exception e)
		{
			if(Constants.debug)  System.out.println("VaultMIDLET.performRegistration() " + e.toString());
		}
	}

	public void performChangePassword(String newpassword, String confirmpassword, String currentpassword)
	{
		if(Constants.debug) System.out.println("VaultMIDLET.performChangePassword()");
		try
		{
			if(Constants.debug) System.out.println("VaultMIDLET.performChangePassword()");
		    Hashtable parameters = new Hashtable();
			parameters.put("newpassword", newpassword);
			parameters.put("currentpassword", currentpassword);
			parameters.put("confirmpassword", confirmpassword);
			
			logicthread = new LogicThread(this, LogicThread.CHANGEPASSWORD, parameters);
			logicthread.start();
			startTimer(100000, Constants.PROGRESS_SYNCHRONIZING[ApplicationAssistant.language]);
		}
		catch (Exception e)
		{
			if(Constants.debug)  System.out.println("VaultMIDLET.performChangePassword() " + e.toString());
		}
	}
	
	public void performContactSynchronisation()
	{
		if(Constants.debug) System.out.println("VaultMIDLET.performContactSynchronisation()");
		try
		{
			if(Constants.debug) System.out.println("VaultMIDLET.performContactSynchronisation()");
		    Hashtable parameters = new Hashtable();
						
			logicthread = new LogicThread(this, LogicThread.CONTACTSSYNCH, parameters);
			logicthread.start();
			//TODO finetune this number based on how it ticks on the phone
			if(ApplicationAssistant.initialupload == Constants.INITIALUPLOAD_TRUE)
				startTimer(500000, Constants.PROGRESS_SYNCHRONIZINGCONTACTS[ApplicationAssistant.language]);
			else
				startTimer(100000, Constants.PROGRESS_SYNCHRONIZINGCONTACTS[ApplicationAssistant.language]);
		}
		catch (Exception e)
		{
			if(Constants.debug)  System.out.println("VaultMIDLET.performContactSynchronisation() " + e.toString());
		}
	}
	
	public void performLoadMedia(int mediatype)
	{
		if(Constants.debug) System.out.println("VaultMIDLET.performLoadMedia()");
		try
		{
			if(Constants.debug) System.out.println("VaultMIDLET.performLoadMedia()");
		    Hashtable parameters = new Hashtable();
			parameters.put("mediatype", mediatype+"");
			
			logicthread = new LogicThread(this, LogicThread.LOADMEDIALIST, parameters);
			logicthread.start();
			if(mediatype==Constants.CONTENTTYPE_PHOTO)
				startTimer(100000, Constants.PROGRESS_LOADINGPHOTOS[ApplicationAssistant.language]);
			else if(mediatype==Constants.CONTENTTYPE_VIDEO)
				startTimer(100000, Constants.PROGRESS_LOADINGVIDEOS[ApplicationAssistant.language]);
			else if(mediatype==Constants.CONTENTTYPE_RINGTONE)
				startTimer(100000, Constants.PROGRESS_LOADINGRINGTONES[ApplicationAssistant.language]);
			else if(mediatype==Constants.CONTENTTYPE_WALLPAPER)
				startTimer(100000, Constants.PROGRESS_LOADINGWALLPAPERS[ApplicationAssistant.language]);
		}
		catch (Exception e)
		{
			if(Constants.debug)  System.out.println("VaultMIDLET.performLoadMedia() " + e.toString());
		}
	}
	
	public void performPublish()
	{
		if(Constants.debug) System.out.println("VaultMIDLET.performPublish()");
		try
		{
			if(Constants.debug) System.out.println("VaultMIDLET.performPublish()");
		    Hashtable parameters = new Hashtable();
			
			logicthread = new LogicThread(this, LogicThread.MEDIAPACKETSYNCH, parameters);
			logicthread.start();
			int numpacketstoupload = ApplicationAPI.numpacketstoupload;
			ApplicationAPI.numpacketstoupload = 0;
			//TODO finetune this number based on how it ticks on the phone
			startTimer(numpacketstoupload * 25000, Constants.PROGRESS_PUBLISHING[ApplicationAssistant.language]);
		}
		catch (Exception e)
		{
			if(Constants.debug)  System.out.println("VaultMIDLET.performPublish() " + e.toString());
		}
	}
	
	public void initiateVacuum(String operation, String smsstring)
	{
		if(Constants.debug) System.out.println("VaultMIDLET.initiateVacuum()");
		try
		{
			String password = smsstring.substring((operation + " ").length()); //already trimmed, dont trim again - expensive
			/*incorrect password OR
			  unregistered client OR
			  client which hasnt done init backup
			
			  If app was invoked by Vacuum command and not already running, just exit. Else, do nothing
			*/ 
			/*if(!password.equals(ApplicationAssistant.password) ||
			   ApplicationAssistant.registered!=Constants.REGISTRATION_TRUE ||
			   ApplicationAssistant.initialupload==Constants.INITIALUPLOAD_TRUE)
			{
				if(guiscreen==null)
					quit();
				return;
			}*/
			
			//Genuine Vacuum command
			//Stop whatever other operation was going on and initiate vacuum
			clearScreen();
			stopLogicThread();
			ApplicationAssistant.vacuummode = operation;
			guiscreen = new GUIStatusScreen(this);
			guiscreen.dialogerrormsg = null;
			guiscreen.dialogconfirmationmsg = null;
			ApplicationAPI.dialogerror = null;
			ApplicationAPI.dialogconfirmation = null;
			display.setCurrent(guiscreen);
		}
		catch (Exception e)
		{
			if(Constants.debug)  System.out.println("VaultMIDLET.initiateVacuum() " + e.toString());
		}
	}
	
	public void performVacuum()
	{
		if(Constants.debug) System.out.println("VaultMIDLET.performVacuum()");
		try
		{
			if(Constants.debug) System.out.println("VaultMIDLET.performVacuum()");
		    Hashtable parameters = new Hashtable();
			
			logicthread = new LogicThread(this, LogicThread.VACUUM, parameters);
			logicthread.start();
			startTimer(100000, Constants.PROGRESS_VACUUMING[ApplicationAssistant.language]);
		}
		catch (Exception e)
		{
			if(Constants.debug)  System.out.println("VaultMIDLET.performVacuum() " + e.toString());
		}
	}
	
	public void clearScreen()
	{
		if(Constants.debug) System.out.println("******clear screen******" + Thread.currentThread().toString());
		try
		{
			cancelTimer();
			if (guiscreen != null)
			{
				guiscreen.statusprogressbar = false;
				guiscreen.dialogprogressbarlabel = null;
				guiscreen.statusprogress = 0;
				guiscreen.dialogerrormsg = null;
				guiscreen.dialogconfirmationmsg = null;
				guiscreen.cleanUp();
				guiscreen = null;
				ApplicationAssistant.gc();
			}
		}
		catch(Exception e)
		{
			if(Constants.debug)  System.out.println("VaultMIDLET.clearscreen() " + e.toString());
		}
	}
	
	private void startTimer(int limit, String label)
	{
		try
		{
			cancelTimer();
			int MAX = 100;
			int cursorspeed = (limit / MAX > 0) ? limit / MAX : 1;
			if (guiscreen == null)
				return;
			guiscreen.statusprogress = 0;
			guiscreen.statusprogressbar = true;
			guiscreen.dialogerrormsg = null;
			guiscreen.dialogconfirmationmsg = null;
			timer = new Timer();
			gaugetask = new GaugeTask(label, MAX);
			timer.scheduleAtFixedRate(gaugetask, 0, cursorspeed);
		}
		catch(Exception e)
		{
			if(Constants.debug)  System.out.println("VaultMIDLET.startTimer() " + e.toString());
		}
	}
	public void cancelTimer()
	{
		try
		{
			if (gaugetask != null)
			{
				if (timer != null)
				{
					timer.cancel();
					timer = null;
				}
				gaugetask.cancel();
				gaugetask = null;
			}
			ApplicationAssistant.gc();
		}
		catch (Exception e)
		{
			if(Constants.debug)  System.out.println("VaultMIDLET.cancelTimer() " + e.toString());
		}
	}
	public void stopLogicThread()
	{
		try
		{
			if (logicthread != null)
			{
				logicthread.killChildren(); //kill connection threads it might have forked
				ApplicationAssistant.gc();
			}
			cancelTimer();
		}
		catch(Exception e)
		{
			if(Constants.debug)  System.out.println("VaultMIDLET.stopLogicThread() " + e.toString());
		}
	}
	
	/**
	 *  Schedule this MIDlet's launch.
	 *  @param deltatime the length of time in
	 *  milliseconds before expiration.
	 */
	private void scheduleMIDlet()
	    throws ClassNotFoundException, ConnectionNotFoundException, SecurityException 
	{
		if(ApplicationAssistant.contactsynch)
		{
			if(ApplicationAssistant.nextscheduledsynchtime != 0) //not manual synch
			{
			    String cn = this.getClass().getName();
			    long t = PushRegistry.registerAlarm(cn, ApplicationAssistant.nextscheduledsynchtime);
			}
		}
	}
	
	private class GaugeTask extends TimerTask
	{
		int current = 0;
		int MAX;
		public GaugeTask(String label, int max)
		{
			this.current = 0;
			this.MAX = max;
			if (guiscreen != null)
				guiscreen.dialogprogressbarlabel = label;
		}
		public void cleanUp()
		{
			try
			{
				LogicThread.paintflag = "";
				LogicThread.paintparameters=null;
				cancel();
				if(timer!=null)
					timer.cancel();
				timer = null;
				
				ApplicationAssistant.gc();
			}
			catch(Exception e)
			{
				if(Constants.debug)  System.out.println("VaultMIDLET.GaugeTask.cleanup() " + e.toString()); 
			}
		}
		public void run()
		{
			//if(Constants.debug) System.out.println("run + " + LogicThread.paintflag);
			try
			{
				if (guiscreen != null)
				{
					if (LogicThread.paintflag.equals(LogicThread.FLAGPAINTSTATUSSCREEN))
					{
						cleanUp();
						paintStatusScreen();
						return;
					}
					else if (LogicThread.paintflag.equals(LogicThread.FLAGPAINTPHOTOVIEWSCREEN))
					{
						int contenttype = Integer.parseInt((String) LogicThread.paintparameters.get("contenttype"));
						cleanUp();
						paintPhotoViewScreen(contenttype);
						return;
					}
					else if (LogicThread.paintflag.equals(LogicThread.FLAGPAINTMEDIALISTSCREEN))
					{
						int contenttype = Integer.parseInt((String) LogicThread.paintparameters.get("contenttype"));
						cleanUp();
						paintMediaListScreen(contenttype);
						return;
					}
					else if (LogicThread.paintflag.equals(LogicThread.FLAGPAINTCHANGEPASSWORDSCREEN))
					{
						String currentpassword = (String) LogicThread.paintparameters.get("currentpassword");
						String newpassword = (String) LogicThread.paintparameters.get("newpassword");
						String confirmpassword = (String) LogicThread.paintparameters.get("confirmpassword");
						cleanUp();
						paintChangePasswordScreen(currentpassword, newpassword, confirmpassword);
						return;
					}
					else if (LogicThread.paintflag.equals(LogicThread.FLAGVACUUMCOMPLETE))
					{
						quit();
						return;
					}
					
					//if no flag, i.e no new screen to paint, just ticking in old screen
					if (current < 3 * MAX)
					{
						guiscreen.setStatusProgress(current / 6);
						current += 6 * 4;
					}
					else if (current < 5 * MAX)
					{
						guiscreen.setStatusProgress(current / 6);
						current += 4 * 4;
					}
					else if (current < 6 * MAX)
					{
						guiscreen.setStatusProgress(current / 6);
						current += 2 * 4;
					}
				}
				else
				{
					cleanUp();
				}
				ApplicationAssistant.gc();
			}
			catch(Exception e)
			{
				if(Constants.debug)  System.out.println("VaultMIDLET.GaugeTask.run() " + e.toString());
			}
		}
	}
}