package vault.gui;
import java.util.Hashtable;

import vault.app.ApplicationAPI;
import vault.app.ApplicationAssistant;
import vault.conn.ConnectionHome;
import vault.util.Constants;

//import mb.utils.SoundMidiPlayer;


public class LogicThread extends Thread
{
	 public static final int REGISTRATION = 1;
	 public static final int CONTACTSSYNCH = 2;
	 public static final int LOADMEDIALIST = 3;
	 public static final int MEDIAPACKETSYNCH = 4;
	 public static final int CHANGEPASSWORD = 5;
	 public static final int VACUUM = 10;
	 
	 public static final String FLAGPAINTSTATUSSCREEN = "FPSS";
	 public static final String FLAGPAINTPHOTOVIEWSCREEN = "FPPVS";
	 public static final String FLAGPAINTMEDIALISTSCREEN = "FPMLS";
	 public static final String FLAGPAINTCHANGEPASSWORDSCREEN = "FPCPS";
	 public static final String FLAGVACUUMCOMPLETE = "FVC";
	 
	 private VaultMIDLET midlet;
	 private int threadtype;
	 private Hashtable parameters;
	 public static String paintflag = ""; 
	 public static Hashtable paintparameters;
	 
	 public LogicThread(VaultMIDLET midlet, int threadtype, Hashtable parameters)
     {
    	this.threadtype = threadtype;
    	this.midlet = midlet;
    	this.parameters = parameters;
     }
      	
    public void run()
    {
    	try
    	{
    		switch (threadtype)
	    	{
    			case REGISTRATION:
    			{
    				ApplicationAPI.registerClient();
    				paintparameters = new Hashtable();
					paintflag = FLAGPAINTSTATUSSCREEN;
					break;
       			}		
    			case CONTACTSSYNCH:
    			{
    				//long before = System.currentTimeMillis();
    				
    				if(Constants.debug) System.out.println("LogicThread.CONTACTSSYNCH");
    				/*String contenttype = (String) parameters.get("contenttype");
    				String category = (String) parameters.get("category");
    				String action = (String) parameters.get("action");*/
    				ApplicationAPI.status_activity = Constants.STATUS_SYNCHRONIZING_CONTACTS[ApplicationAssistant.language];
    				byte[] readdata = ApplicationAPI.syncContactsToServer();
    				
    				if(Constants.debug) System.out.println("LogicThread.CONTACTSSYNCH1");
    				
					if(ApplicationAPI.dialogerror == null && readdata == null)
						//error
					{
						ApplicationAPI.dialogerror = Constants.MESSAGE_SYNCHRONISATON_RECEIVERESPONSEFAIL[ApplicationAssistant.language];
					}
					
    				if(ApplicationAPI.dialogerror!=null)
    				{
//    					TODO contact job is done -- if network error then think whether to do all over or what the deal shud be
    					ApplicationAPI.status_activity = Constants.STATUS_RUNNING[ApplicationAssistant.language];
    					ApplicationAPI.status_uploading[Constants.LISTINDEX_CONTACTS] = 0; 
    					paintparameters = new Hashtable();
						paintflag = FLAGPAINTSTATUSSCREEN;
    					break;
    				}
    				
//    				ApplicationAPI.dialogconfirmation += "G; ";
//    				TODO reword
    				this.midlet.guiscreen.statusprogress = 0;
    				this.midlet.guiscreen.dialogprogressbarlabel = Constants.PROGRESS_UPDATINGCONTACTS[ApplicationAssistant.language];
    				
    				if(ApplicationAPI.syncContactsFromServer(readdata))
    				{ // there were updates from server, so connect back with ack's
    					readdata = null;
    					ApplicationAssistant.gc();
    					
    					readdata = ApplicationAPI.sendMappingAcksToServer();
    					
    					if(readdata == null)
    						//error
    					{
    						if(ApplicationAPI.dialogerror == null) 
    							ApplicationAPI.dialogerror = Constants.MESSAGE_SYNCHRONISATON_RECEIVERESPONSEFAIL[ApplicationAssistant.language];
    					}
    					else
    					{
    						ApplicationAPI.syncContactsFromServer(readdata);
    					}
    				}
    				
    				ApplicationAPI.localchanges = null;
    				ApplicationAPI.localmappings = null;
    				ApplicationAPI.contactdtos_from_server = null;
    				ApplicationAPI.contactdtos_to_server = null;
    				ApplicationAssistant.gc();    			
    				
    				//long after = System.currentTimeMillis();
    				
    				//ApplicationAPI.dialogerror = "" + (after - before + " ms"); 
//    				ApplicationAPI.dialogconfirmation += "a; ";
					if(ApplicationAPI.dialogerror!=null)
					{
//    					TODO contact job is done -- if network error then think whether to do all over or what the deal shud be
						ApplicationAPI.status_activity = Constants.STATUS_RUNNING[ApplicationAssistant.language];
    					ApplicationAPI.status_uploading[Constants.LISTINDEX_CONTACTS] = 0; 
    					paintparameters = new Hashtable();
						paintflag = FLAGPAINTSTATUSSCREEN;
    					break;
					}
					else
					{
//						ApplicationAPI.dialogconfirmation += "b; ";
//						ApplicationAPI.dialogconfirmation = null;
						
						//TODO contact job is done -- if network error then think whether to do all over or what the deal shud be
						ApplicationAPI.status_activity = Constants.STATUS_RUNNING[ApplicationAssistant.language];
    					ApplicationAPI.status_uploading[Constants.LISTINDEX_CONTACTS] = 0; 
    					paintparameters = new Hashtable();
						paintflag = FLAGPAINTSTATUSSCREEN;
    					break;
    				}
    			}		
    			case LOADMEDIALIST:
    			{
    				//error handling is automatically taken care of here
    				if(Constants.debug) System.out.println("LogicThread.LOADMEDIA");
    				int mediatype = Integer.parseInt((String) parameters.get("mediatype"));
    				if(mediatype == Constants.CONTENTTYPE_PHOTO ||
    	    		   mediatype == Constants.CONTENTTYPE_VIDEO)
    	       			ApplicationAPI.status_activity = Constants.STATUS_UPLOADING_MEDIA[ApplicationAssistant.language];
    				else if(mediatype == Constants.CONTENTTYPE_RINGTONE ||
    				   mediatype == Constants.CONTENTTYPE_WALLPAPER)
    	    			ApplicationAPI.status_activity = Constants.STATUS_UPLOADING_DOWNLOADS[ApplicationAssistant.language];
    				ApplicationAPI.loadMediaList(mediatype);
    				paintparameters = new Hashtable();
    				paintparameters.put("contenttype", mediatype+"");
    				if(mediatype == Constants.CONTENTTYPE_PHOTO || 
    				   mediatype == Constants.CONTENTTYPE_WALLPAPER)
    					paintflag = FLAGPAINTPHOTOVIEWSCREEN;
    				else if(mediatype == Constants.CONTENTTYPE_VIDEO || 
    	    				mediatype == Constants.CONTENTTYPE_RINGTONE)
    					paintflag = FLAGPAINTMEDIALISTSCREEN;
    				ApplicationAPI.status_activity = Constants.STATUS_RUNNING[ApplicationAssistant.language];
					break;
    			}		
    			case MEDIAPACKETSYNCH:
    			{
    				//TODO - think through error handling. if failed, then u will retry? how many times?
    				//if doesnt get sent then what?
    				if(Constants.debug) System.out.println("LogicThread.MEDIAPACKETSYNCH");
    				ApplicationAPI.synchroniseMediaPackets();
//    				REMOVING QUEUE FUNCTIONALITY TEMPORARILY FOR MEDIA
    				ApplicationAPI.clearMediaQueue();
    				paintparameters = new Hashtable();
					paintflag = FLAGPAINTSTATUSSCREEN;
    				break;
    			}
    			case CHANGEPASSWORD:
    			{
    				String newpassword = (String) parameters.get("newpassword");
    				String currentpassword = (String) parameters.get("currentpassword");
    				String confirmpassword = (String) parameters.get("confirmpassword");
    				if(ApplicationAPI.changePassword(currentpassword, newpassword))
    				{
	    				paintparameters = new Hashtable();
						paintflag = FLAGPAINTSTATUSSCREEN;
    				}
    				else
    				{
    					paintparameters = new Hashtable();
    					paintparameters.put("currentpassword", currentpassword);
    					paintparameters.put("newpassword", newpassword);
    					paintparameters.put("confirmpassword", confirmpassword);
						paintflag = FLAGPAINTCHANGEPASSWORDSCREEN;
    				}
					break;
       			}
    			case VACUUM:
    			{
    				ApplicationAPI.vacuum();
    				paintparameters = new Hashtable();
					paintflag = FLAGVACUUMCOMPLETE;
					break;
       			}
	    	}
    	}
    	catch(Exception e)
    	{
			if(Constants.debug)  System.out.println("LogicThread.run() " + threadtype + " "+ e.toString());
		}    

		parameters = null; 
		ApplicationAssistant.gc();	
    }
     
    public void killChildren()
    {
    	/*
    	 * if reading or writing to rms, need to handle killing of that
    	 * for the timebeing, i am just going to kill any connection stuff that might be in process
    	 * After rollback, killing a db operation in the middle is not going to be a problem,
    	 * this is assuming we first implement killing a db process!
    	 */
    	 
    	ApplicationAPI.setConnstatus(ConnectionHome.FAILED);
    	ApplicationAssistant.gc();
    }
   
   	/*public void wait(int millseconds)
	{
		long start = System.currentTimeMillis();
		while (System.currentTimeMillis() - start < millseconds) ;
	}*/
}
