package vault.app;

import java.io.DataInputStream;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import javax.microedition.io.Connector;
import javax.microedition.io.InputConnection;
import javax.microedition.io.file.FileConnection;
import javax.microedition.pim.Contact;
import javax.microedition.pim.ContactList;
import javax.microedition.pim.FieldEmptyException;
import javax.microedition.pim.FieldFullException;
import javax.microedition.pim.PIM;
import javax.microedition.pim.PIMException;
import javax.microedition.pim.PIMItem;

import vault.conn.ConnectionHome;
import vault.db.DBManager;
import vault.dto.ContactDTO;
import vault.dto.ContactLocalDTO;
import vault.dto.ContactMappingDTO;
import vault.dto.MediaDTO;
import vault.util.Constants;

public class ApplicationAPI extends Object
{
	/**
	 * OBJECTS TO BE CACHED IN HEAP DURING PROCESS FLOW
	 * ex. List of photos in previous screen, contacts received from server etc
	 */
	//Contact Synch Objects. These are init by XML home when objects received from server and internally here when objects read from DB 
	//public static Vector contactdtos_to_server = null;
	public static Vector[] contactdtos_from_server = null;
	public static ContactDTO [] contactdtos_to_server = null;
	public static Vector[] localchanges = null;
	public static Vector[] localmappings = null;
	public static Vector mediadtos = null;
	private static int polltimeout = 0;	
	/**
	 * DATABASE INTERFACES
	 */
	private static DBManager dbmanager_contactsynchmaps;
	private static DBManager dbmanager_contacts;
	private static DBManager dbmanager_config;
	private static DBManager dbmanager_counts;
	private static DBManager dbmanager_media;
	private static DBManager dbmanager_uploadedmedia;
	
	/**
	 * APPLICATION STATE IDENTIFIERS
	 */
	//Generic params for underlying layers to inform GUI to pop up a dialog with error or confirmation
	public static String dialogconfirmation = null;
	public static String dialogerror = null;
	
	//Status screen params for underlying layer to update, so GUI can update user of activity. These holders
	//will be init during app start, and will always hold the last stored value. When no operation in progress
	//status screen will show the last known (=current) status
	public static String status_activity = null;
	public static int [] status_uploading = new int [] {0, 0, 0, 0, 0}; //queue count for contacts, photos, videos, ringtones, wallpapers
	public static int [] status_uploaded = new int [] {0, 0, 0, 0, 0}; //vault count for contacts, photos, videos, ringtones, wallpapers
	public static String status_uploaderror = null;
	
	/**
	 * CONNECTION SEMAPHORE
	 */
	private static Integer connstatus = null;
	
	public static boolean syncrhonizenow = false;
	public static boolean publishnow = false;
	public static int numpacketstoupload = 0; //this number gives an indication to the midlet to tick proportional to the number of packets that need uploaded
	
	public static synchronized void setConnstatus(int status)
	{
		connstatus = new Integer(status);
	}
	
	public static synchronized int getConnstatus()
	{
		if(connstatus==null)
			return ConnectionHome.NA;
		else
			return connstatus.intValue();
	}
		
	public static void initialise(Hashtable properties)
	{
		if(Constants.debug) System.out.println("VaultAPI.initialise()");
		initialiseDBs();
		initConfig(properties);
		initStatus();
		
		if(ApplicationAssistant.nextscheduledsynchtime != 0) //manual otherwise
		{
			//if next synch time < now, 
			//	1) if(lastsynchtime < next synchtime) synch now
			//	2) reset schedule time
			long currenttime = System.currentTimeMillis();
			if(currenttime >= ApplicationAssistant.nextscheduledsynchtime)
			{
				if(ApplicationAssistant.nextscheduledsynchtime > ApplicationAssistant.lastsynchtime)
					ApplicationAPI.syncrhonizenow = true;
				updateNextScheduledSynchTime(-1);
			}
		}
	}
	
	//REFER TO vault.db.DBManager TO SEE DB SCHEMA
	private static void initialiseDBs()
	{
		if(Constants.debug) System.out.println("VaultAPI.initialiseDBs()");
		try
		{
			String CONTACTSYNCHMAPSSTORE = "contactsynchmaps";
			Vector CONTACTSYNCHMAPSSTORE_FIELDS = new Vector();
			String fieldnames [] = new String [] {"luid", "guid", "mapsoperation"};
			CONTACTSYNCHMAPSSTORE_FIELDS.addElement(fieldnames);
			fieldnames = null;
			ApplicationAssistant.gc();
			dbmanager_contactsynchmaps = new DBManager(CONTACTSYNCHMAPSSTORE_FIELDS, CONTACTSYNCHMAPSSTORE);
			CONTACTSYNCHMAPSSTORE_FIELDS = null;
			ApplicationAssistant.gc();
			
			String CONTACTSSTORE = "contacts";
			Vector CONTACTSSTORE_FIELDS = new Vector();
			fieldnames = new String [] {"luid", "checksum"};
			CONTACTSSTORE_FIELDS.addElement(fieldnames);
			fieldnames = null;
			ApplicationAssistant.gc();
			dbmanager_contacts = new DBManager(CONTACTSSTORE_FIELDS, CONTACTSSTORE);
			CONTACTSSTORE_FIELDS = null;
			ApplicationAssistant.gc();

			String CONFIGSTORE = "config";
			Vector CONFIGSTORE_FIELDS = new Vector();
			fieldnames = new String [] {"pk", "value"};
			CONFIGSTORE_FIELDS.addElement(fieldnames);
			fieldnames = null;
			ApplicationAssistant.gc();
			dbmanager_config = new DBManager(CONFIGSTORE_FIELDS, CONFIGSTORE);
			CONFIGSTORE_FIELDS = null;
			ApplicationAssistant.gc();
			
			String COUNTSSTORE = "counts";
			Vector COUNTSSTORE_FIELDS = new Vector();
			fieldnames = new String [] {"item", "count"};
			COUNTSSTORE_FIELDS.addElement(fieldnames);
			fieldnames = null;
			ApplicationAssistant.gc();
			dbmanager_counts = new DBManager(COUNTSSTORE_FIELDS, COUNTSSTORE);
			COUNTSSTORE_FIELDS = null;
			ApplicationAssistant.gc();
			
			String MEDIASTORE = "media";
			Vector MEDIASTORE_FIELDS = new Vector();
			fieldnames = new String [] {"filepath", "mediatype", "status"};
			MEDIASTORE_FIELDS.addElement(fieldnames);
			fieldnames = null;
			ApplicationAssistant.gc();
			fieldnames = new String [] {"album", "desc"};
			MEDIASTORE_FIELDS.addElement(fieldnames);
			fieldnames = null;
			ApplicationAssistant.gc();
			dbmanager_media = new DBManager(MEDIASTORE_FIELDS, MEDIASTORE);
			MEDIASTORE_FIELDS = null;
			ApplicationAssistant.gc();

			//REMOVING QUEUE FUNCTIONALITY TEMPORARILY FOR MEDIA
			ApplicationAPI.clearMediaQueue();
			
			String UPLOADEDSTORE = "uploadedmedia";
			Vector UPLOADEDSTORE_FIELDS = new Vector();
			fieldnames = new String [] {"filepath", "mediatype"};
			UPLOADEDSTORE_FIELDS.addElement(fieldnames);
			fieldnames = null;
			ApplicationAssistant.gc();
			dbmanager_uploadedmedia = new DBManager(UPLOADEDSTORE_FIELDS, UPLOADEDSTORE);
			UPLOADEDSTORE_FIELDS = null;
			ApplicationAssistant.gc();			
		}
		catch(Exception e)
		{
			if(Constants.debug) System.out.println("VaultAPI.initialiseDBs() " + e.toString()); 			
		}
	}
	
	private static void initConfig(Hashtable properties)
	{
		if(Constants.debug) System.out.println("VaultAPI.initConfig()");
		try
		{
			if (dbmanager_config.open().equals(Constants.SUCCESSCONDITIONAL))
			{
				throw new Exception("no config db");
			}
			loadConfigFromDB();
		}
		catch (Exception e)
		{
			if(Constants.debug) System.out.println("VaultAPI.initConfig() " + e.toString());
			loadConfigFromJAR(properties);
			writeConfigToDB();
		}
		if(Constants.debug)
		{
			System.out.println("host:" + ApplicationAssistant.host);
			System.out.println("post:" + ApplicationAssistant.port);
			System.out.println("contacts_url:" + ApplicationAssistant.contacts_url);
			System.out.println("registration_url:" + ApplicationAssistant.registration_url);
			System.out.println("media_pkt_upload_url:" + ApplicationAssistant.media_pkt_upload_url);
			System.out.println("media_gallery_url:" + ApplicationAssistant.media_gallery_url);
			System.out.println("sub_status_check:" + ApplicationAssistant.sub_status_check);
			System.out.println("phone_model:" + ApplicationAssistant.phone_model);
			System.out.println("hp:" + ApplicationAssistant.hp);
			System.out.println("encodedhp:" + ApplicationAssistant.encodedhp);
			System.out.println("userid:" + ApplicationAssistant.userid);
			System.out.println("password:" + ApplicationAssistant.password);
			System.out.println("appid:" + ApplicationAssistant.appid);
			System.out.println("screensize:" + ApplicationAssistant.screensize);
			System.out.println("language:" + ApplicationAssistant.language);
			System.out.println("contactsynch:" + ApplicationAssistant.contactsynch);
			System.out.println("mediasynch:" + ApplicationAssistant.mediasynch);
			System.out.println("smssynch:" + ApplicationAssistant.smssynch);
			System.out.println("version:" + ApplicationAssistant.version);
			System.out.println("vendor:" + ApplicationAssistant.vendor);
			System.out.println("lastsynchtime:" + ApplicationAssistant.lastsynchtime);
			System.out.println("registered:" + ApplicationAssistant.registered);
			System.out.println("initialupload:" + ApplicationAssistant.initialupload);
			System.out.println("nextscheduledsynchtime:" + ApplicationAssistant.nextscheduledsynchtime);
			System.out.println("scheduleperiod:" + ApplicationAssistant.scheduleperiod);
			System.out.println("laststatussynctimestamp:" + ApplicationAssistant.laststatussynctimestamp);
			System.out.println("SSL:" + ApplicationAssistant.SSL);
			System.out.println("keyset:" + ApplicationAssistant.keyset_flag);
		}
		
	}
	
	private static void loadConfigFromJAR(Hashtable properties)
	{
		if(Constants.debug) System.out.println("VaultAPI.loadConfigFromJAR()");
		/**
		 FROM JAD
		 	jadip (host & port)
			contacts_url: /ewtech/rippleservlet
			registration_url: /ewtech/rippleservlet
			upgrade_url: /ewtechwap/downloadStart.do
			media_pkt_upload_url: /ewtech/mediauploadpktservlet
			media_gallery_url: /ewtechwap/secure/mediaGallery.do
			sharing_url: /ewtech/sharingservlet
			album_synch_url: /ewtech/albumsyncingservlet
			sub_status_check: /ewtech/status
			phone_model: Nokia6230i
			billing_reg_type: p
			userid: naidu
			password: c1h85a
			hp: D62B9C05C1
			appid: 932433783
			screensize: w208h208
			contactsynch: true
		*/
		try
		{
			Enumeration keys = properties.keys();
			Enumeration values = properties.elements();
			while(keys.hasMoreElements())
			{
				String property = (String) keys.nextElement();
				String value = (String) values.nextElement();
				if(Constants.debug) System.out.println("**** " + property + ": " + value);
				
				if(property.equals("contacts_url"))
					ApplicationAssistant.contacts_url = value;
				else if(property.equals("registration_url"))
					ApplicationAssistant.registration_url = value;
				else if(property.equals("media_pkt_upload_url"))
					ApplicationAssistant.media_pkt_upload_url = value;
				else if(property.equals("media_gallery_url"))
					ApplicationAssistant.media_gallery_url = value;
				else if(property.equals("sub_status_check"))
					ApplicationAssistant.sub_status_check = value;
				else if(property.equals("phone_model"))
					ApplicationAssistant.phone_model = value;
				else if(property.equals("hp"))
				{
					ApplicationAssistant.encodedhp = value;
					ApplicationAssistant.hp = decodeHp(value);
				}
				else if(property.equals("userid"))
					ApplicationAssistant.userid = value;
				else if(property.equals("password"))
					ApplicationAssistant.password = value;
				else if(property.equals("appid"))
					ApplicationAssistant.appid = value;
				else if(property.equals("screensize"))
					ApplicationAssistant.screensize = value;
				else if(property.equals("language"))
					ApplicationAssistant.language = Integer.parseInt(value);
				else if(property.equals("contactsynch"))
					ApplicationAssistant.contactsynch = (value.equals("true"))?true:false;
				else if(property.equals("mediasynch"))
					ApplicationAssistant.mediasynch = (value.equals("true"))?true:false;
				else if(property.equals("smssynch"))
					ApplicationAssistant.smssynch = (value.equals("true"))?true:false;
				else if(property.equals("version"))
					ApplicationAssistant.version = value;
				else if(property.equals("vendor"))
					ApplicationAssistant.vendor = value;
				else if(property.equals("SSL"))
					ApplicationAssistant.SSL = (value.equals("true"))?true:false;
				else if(property.equals("keyset")){
					if(value.equals(Constants.KEYSET_DEFAULT))
						ApplicationAssistant.keyset_flag = 0;
					else if(value.equals(Constants.KEYSET_MOTOROLA))
						ApplicationAssistant.keyset_flag = 1;
					else
						ApplicationAssistant.keyset_flag = 0;
				}
				else if(property.equals("jadip"))
				{
					try
					{
						String jadip = (String) properties.get("jadip");
						jadip = jadip.substring("http://".length());
						if(jadip.indexOf(":")>0)
						{
							ApplicationAssistant.host = jadip.substring(0, jadip.indexOf(":"));
							ApplicationAssistant.port = jadip.substring(jadip.indexOf(":")+1, jadip.indexOf("/"));
						}
						else
						{
							ApplicationAssistant.host = jadip.substring(0, jadip.indexOf("/"));
							ApplicationAssistant.port = "80";
						}
					}
					catch(Exception e)
					{
						if(Constants.debug)  System.out.println("VaultAPI.loadConfigFromJAR() " + e.toString());
						ApplicationAssistant.host = "203.208.254.132";
						ApplicationAssistant.port = "80";
					}
				}
				property = null;
				value = null;
				ApplicationAssistant.gc();
			}
			ApplicationAssistant.gc();
		}
		catch(Exception ex)
		{
			if(Constants.debug) System.out.println("VaultAPI.loadConfigFromJAR() " + ex.toString());
		}
	}
	
	private static void loadConfigFromDB()
	{
		if(Constants.debug) System.out.println("VaultAPI.loadConfigFromDB()");
		try
		{
			Hashtable[] result = null;
			try
			{
				result = dbmanager_config.findAll();
				ApplicationAssistant.gc();
			}
			catch (Exception e)
			{
				if(Constants.debug) System.out.println("VaultAPI.loadConfigFromDB() " + e.toString()); 
				result = new Hashtable[0];
			}
			for(int i=0; i<result.length; i++)
			{
				String pk = new String((byte []) result[i].get("pk"));
				String value = new String((byte[]) result[i].get("value"));
				if(pk.equals("host"))
					ApplicationAssistant.host = value;
				else if(pk.equals("port"))
					ApplicationAssistant.port = value;
				else if(pk.equals("contacts_url"))
					ApplicationAssistant.contacts_url = value;
				else if(pk.equals("registration_url"))
					ApplicationAssistant.registration_url = value;
				else if(pk.equals("media_pkt_upload_url"))
					ApplicationAssistant.media_pkt_upload_url = value;
				else if(pk.equals("media_gallery_url"))
					ApplicationAssistant.media_gallery_url = value;
				else if(pk.equals("sub_status_check"))
					ApplicationAssistant.sub_status_check = value;
				else if(pk.equals("phone_model"))
					ApplicationAssistant.phone_model = value;
				else if(pk.equals("encodedhp"))
				{
					ApplicationAssistant.encodedhp = value;
					ApplicationAssistant.hp = decodeHp(value);
				}
				else if(pk.equals("userid"))
					ApplicationAssistant.userid = value;
				else if(pk.equals("password"))
					ApplicationAssistant.password = value;
				else if(pk.equals("appid"))
					ApplicationAssistant.appid = value;
				else if(pk.equals("screensize"))
					ApplicationAssistant.screensize = value;
				else if(pk.equals("language"))
					ApplicationAssistant.language = Integer.parseInt(value);
				else if(pk.equals("contactsynch"))
					ApplicationAssistant.contactsynch = (value.equals("true"))?true:false;
				else if(pk.equals("mediasynch"))
					ApplicationAssistant.mediasynch = (value.equals("true"))?true:false;
				else if(pk.equals("smssynch"))
					ApplicationAssistant.smssynch = (value.equals("true"))?true:false;
				else if(pk.equals("version"))
					ApplicationAssistant.version = value;
				else if(pk.equals("vendor"))
					ApplicationAssistant.vendor = value;
				else if(pk.equals("lastsynchtime"))
					ApplicationAssistant.lastsynchtime = Long.parseLong(value);
				else if(pk.equals("registered"))
					ApplicationAssistant.registered = (value.equals("true"))?Constants.REGISTRATION_TRUE:Constants.REGISTRATION_FALSE;
				else if(pk.equals("initialupload"))
					ApplicationAssistant.initialupload = (value.equals("true"))?Constants.INITIALUPLOAD_TRUE:Constants.INITIALUPLOAD_FALSE;
				else if(pk.equals("nextscheduledsynchtime"))
					ApplicationAssistant.nextscheduledsynchtime = Long.parseLong(value);
				else if(pk.equals("scheduleperiod"))
					ApplicationAssistant.scheduleperiod = Long.parseLong(value);
				else if(pk.equals("laststatussynctimestamp"))
					ApplicationAssistant.laststatussynctimestamp = Long.parseLong(value);
				else if(pk.equals("SSL"))
					ApplicationAssistant.SSL = (value.equals("true"))?true:false;
				else if(pk.equals("keyset"))
					ApplicationAssistant.keyset_flag = Integer.parseInt(value);
				
			}
			result = null;
			ApplicationAssistant.gc();
		}
		catch(Exception e)
		{
			if(Constants.debug) System.out.println("VaultAPI.loadConfigFromDB() " + e.toString()); 			
		}
		finally
		{
			try
			{
				dbmanager_config.close();
			}
			catch(Exception ex)
			{
				if(Constants.debug) System.out.println("VaultAPI.loadConfigFromDB() " + ex.toString());
			}
			ApplicationAssistant.gc();
		}
	}
	
	private static void writeConfigToDB()
	{
		if(Constants.debug) System.out.println("VaultAPI.writeConfigToDB()");
		try
		{
			byte[][] data = new byte[2][];
			data[0] = "host".getBytes(); //host is the pk
			data[1] = ApplicationAssistant.host.getBytes();
			dbmanager_config.createDBRecord(data);
			
			data[0] = "port".getBytes();
			data[1] = ApplicationAssistant.port.getBytes();
			dbmanager_config.createDBRecord(data);

			data[0] = "contacts_url".getBytes();
			data[1] = ApplicationAssistant.contacts_url.getBytes();
			dbmanager_config.createDBRecord(data);
			
			data[0] = "registration_url".getBytes();
			data[1] = ApplicationAssistant.registration_url.getBytes();
			dbmanager_config.createDBRecord(data);
			
			data[0] = "media_pkt_upload_url".getBytes();
			data[1] = ApplicationAssistant.media_pkt_upload_url.getBytes();
			dbmanager_config.createDBRecord(data);
			
			data[0] = "media_gallery_url".getBytes();
			data[1] = ApplicationAssistant.media_gallery_url.getBytes();
			dbmanager_config.createDBRecord(data);
			
			data[0] = "sub_status_check".getBytes();
			data[1] = ApplicationAssistant.sub_status_check.getBytes();
			dbmanager_config.createDBRecord(data);
			
			data[0] = "phone_model".getBytes();
			data[1] = ApplicationAssistant.phone_model.getBytes();
			dbmanager_config.createDBRecord(data);
			
			data[0] = "encodedhp".getBytes();
			data[1] = ApplicationAssistant.encodedhp.getBytes();
			dbmanager_config.createDBRecord(data);
			//we dont write hp to db, as it can be decoded from this
			
			data[0] = "userid".getBytes();
			data[1] = ApplicationAssistant.userid.getBytes();
			dbmanager_config.createDBRecord(data);
			
			data[0] = "password".getBytes();
			data[1] = ApplicationAssistant.password.getBytes();
			dbmanager_config.createDBRecord(data);

			data[0] = "appid".getBytes();
			data[1] = ApplicationAssistant.appid.getBytes();
			dbmanager_config.createDBRecord(data);
			
			data[0] = "screensize".getBytes();
			data[1] = ApplicationAssistant.screensize.getBytes();
			dbmanager_config.createDBRecord(data);

			data[0] = "language".getBytes();
			data[1] = (ApplicationAssistant.language + "").getBytes();
			dbmanager_config.createDBRecord(data);
			
			data[0] = "contactsynch".getBytes();
			data[1] = (ApplicationAssistant.contactsynch)?"true".getBytes():"false".getBytes();
			dbmanager_config.createDBRecord(data);
			
			data[0] = "mediasynch".getBytes();
			data[1] = (ApplicationAssistant.mediasynch)?"true".getBytes():"false".getBytes();
			dbmanager_config.createDBRecord(data);
			
			data[0] = "smssynch".getBytes();
			data[1] = (ApplicationAssistant.smssynch)?"true".getBytes():"false".getBytes();
			dbmanager_config.createDBRecord(data);

			data[0] = "version".getBytes();
			data[1] = ApplicationAssistant.version.getBytes();
			dbmanager_config.createDBRecord(data);
			
			data[0] = "vendor".getBytes();
			data[1] = ApplicationAssistant.vendor.getBytes();
			dbmanager_config.createDBRecord(data);

			data[0] = "lastsynchtime".getBytes();
			data[1] = "0".getBytes();
			dbmanager_config.createDBRecord(data);
			
			data[0] = "registered".getBytes();
			data[1] = "false".getBytes();
			dbmanager_config.createDBRecord(data);

			data[0] = "initialupload".getBytes();
			data[1] = "true".getBytes();
			dbmanager_config.createDBRecord(data);			

			ApplicationAssistant.scheduleperiod = 7*24*3600*1000;
			data[0] = "scheduleperiod".getBytes();
			data[1] = (ApplicationAssistant.scheduleperiod+"").getBytes();
			dbmanager_config.createDBRecord(data);
			
			ApplicationAssistant.nextscheduledsynchtime = System.currentTimeMillis() + ApplicationAssistant.scheduleperiod;
			data[0] = "nextscheduledsynchtime".getBytes();
			data[1] = (ApplicationAssistant.nextscheduledsynchtime + "").getBytes();
			dbmanager_config.createDBRecord(data);

			ApplicationAssistant.laststatussynctimestamp = 0;	
			data[0] = "laststatussynctimestamp".getBytes();
			data[1] = "0".getBytes();
			dbmanager_config.createDBRecord(data);
			
			data[0] = "SSL".getBytes();
			data[1] = (ApplicationAssistant.SSL)?"true".getBytes():"false".getBytes();
			dbmanager_config.createDBRecord(data);

			data[0] = "keyset".getBytes();
			data[1] = (ApplicationAssistant.keyset_flag + "").getBytes();
			dbmanager_config.createDBRecord(data);
			
			data = null;
			ApplicationAssistant.gc();
		}
		catch(Exception e)
		{
			if(Constants.debug) System.out.println("VaultAPI.writeConfigToDB() " + e.toString()); 			
		}
		finally
		{
			try
			{
				dbmanager_config.close();
			}
			catch(Exception ex)
			{
				if(Constants.debug) System.out.println("VaultAPI.writeConfigToDB() " + ex.toString());
			}
			ApplicationAssistant.gc();
		}
	}
	
	private static void initStatus()
	{
		if(Constants.debug) System.out.println("VaultAPI.initStatus()");
		try
		{
			ApplicationAPI.status_activity = Constants.STATUS_RUNNING[ApplicationAssistant.language];			
			if (dbmanager_counts.open().equals(Constants.SUCCESSCONDITIONAL))
			{
				throw new Exception("no counts db");
			}
			loadCountsFromDB();
		}
		catch (Exception e)
		{
			if(Constants.debug) System.out.println("VaultAPI.initStatus() " + e.toString());
			writeCountsToDB(false);
		}
	}
	
	private static void loadCountsFromDB()
	{
		if(Constants.debug) System.out.println("VaultAPI.loadCountsFromDB()");
		try
		{
			Hashtable[] result = null;
			try
			{
				result = dbmanager_counts.findAll();
			}
			catch (Exception e)
			{
				if(Constants.debug) System.out.println("VaultAPI.loadCountsFromDB() " + e.toString()); 
				result = new Hashtable[0];
			}
			finally
			{
				try
				{
					dbmanager_counts.close();
				}
				catch(Exception ex)
				{
					if(Constants.debug) System.out.println("VaultAPI.loadCountsFromDB() " + ex.toString());
				}
				ApplicationAssistant.gc();
			}
			for(int i=0; i<result.length; i++)
			{
				String item = new String((byte []) result[i].get("item"));
				if(Constants.debug) System.out.println(item);
				int count = Integer.parseInt(new String((byte[]) result[i].get("count")));
				if(Constants.debug) System.out.println(count);
				if(item.equals("contacts"))
					status_uploaded [Constants.LISTINDEX_CONTACTS] = count;
				else if(item.equals("photos"))
					status_uploaded [Constants.LISTINDEX_PHOTOS] = count;
				else if(item.equals("videos"))
					status_uploaded [Constants.LISTINDEX_VIDEOS] = count;
				else if(item.equals("ringtones"))
					status_uploaded [Constants.LISTINDEX_RINGTONES] = count;
				else if(item.equals("wallpapers"))
					status_uploaded [Constants.LISTINDEX_WALLPAPERS] = count;
			}
			result = null;
			ApplicationAssistant.gc();
			
			try
			{
				dbmanager_media.open();
				result = dbmanager_media.findAll();
			}
			catch (Exception e)
			{
				if(Constants.debug) System.out.println("VaultAPI.loadCountsFromDB() " + e.toString()); 
				result = new Hashtable[0];
			}
			finally
			{
				try
				{
					dbmanager_media.close();
				}
				catch(Exception ex)
				{
					if(Constants.debug) System.out.println("VaultAPI.loadCountsFromDB() " + ex.toString());
				}
				ApplicationAssistant.gc();
			}
			for(int i=0; i<result.length; i++)
			{
				int mediat = Integer.parseInt(new String((byte []) result[i].get("mediatype")));
				if(Constants.debug) System.out.println(mediat);
				if(mediat == Constants.CONTENTTYPE_PHOTO)
					++status_uploading [Constants.LISTINDEX_PHOTOS];
				else if(mediat == Constants.CONTENTTYPE_VIDEO)
					++status_uploading [Constants.LISTINDEX_VIDEOS];
				else if(mediat == Constants.CONTENTTYPE_RINGTONE)
					++status_uploading [Constants.LISTINDEX_RINGTONES];
				else if(mediat == Constants.CONTENTTYPE_WALLPAPER)
					++status_uploading [Constants.LISTINDEX_WALLPAPERS];
			}
			result = null;
			ApplicationAssistant.gc();
			
		}
		catch(Exception e)
		{
			if(Constants.debug) System.out.println("VaultAPI.loadCountsFromDB() " + e.toString()); 			
		}
	}
	
	private static void writeCountsToDB(boolean countsexist)
	{
		if(Constants.debug) System.out.println("VaultAPI.writeCountsToDB()");
		try
		{
			if(!countsexist)
			{
				byte[][] data = new byte[2][];
				data[0] = "contacts".getBytes();
				data[1] = (status_uploaded [Constants.LISTINDEX_CONTACTS]+"").getBytes();
				dbmanager_counts.createDBRecord(data);
				
				data[0] = "photos".getBytes();
				data[1] = (status_uploaded [Constants.LISTINDEX_PHOTOS]+"").getBytes();
				dbmanager_counts.createDBRecord(data);
	
				data[0] = "videos".getBytes();
				data[1] = (status_uploaded [Constants.LISTINDEX_VIDEOS]+"").getBytes();
				dbmanager_counts.createDBRecord(data);
					
				data[0] = "ringtones".getBytes();
				data[1] = (status_uploaded [Constants.LISTINDEX_RINGTONES]+"").getBytes();
				dbmanager_counts.createDBRecord(data);
				
				data[0] = "wallpapers".getBytes();
				data[1] = (status_uploaded [Constants.LISTINDEX_WALLPAPERS]+"").getBytes();
				dbmanager_counts.createDBRecord(data);
					
				data = null;
				ApplicationAssistant.gc();
			}
			else
			{
				dbmanager_counts.open();
				dbmanager_counts.updateField("contacts", "count", (status_uploaded [Constants.LISTINDEX_CONTACTS]+"").getBytes());
				dbmanager_counts.updateField("photos", "count", (status_uploaded [Constants.LISTINDEX_PHOTOS]+"").getBytes());
				dbmanager_counts.updateField("videos", "count", (status_uploaded [Constants.LISTINDEX_VIDEOS]+"").getBytes());
				dbmanager_counts.updateField("ringtones", "count", (status_uploaded [Constants.LISTINDEX_RINGTONES]+"").getBytes());
				dbmanager_counts.updateField("wallpapers", "count", (status_uploaded [Constants.LISTINDEX_WALLPAPERS]+"").getBytes());
			}
		}
		catch(Exception e)
		{
			if(Constants.debug) System.out.println("VaultAPI.writeCountsToDB() " + e.toString()); 			
		}
		finally
		{
			try
			{
				dbmanager_counts.close();
			}
			catch(Exception ex)
			{
				if(Constants.debug) System.out.println("VaultAPI.writeCountsToDB() " + ex.toString());
			}
			ApplicationAssistant.gc();
		}
	}
	
	private static void updateStatus(Hashtable statustable)
	{
		if(Constants.debug) System.out.println("VaultAPI.updateStatus()");
		try
		{
			dbmanager_config.open();
			//billing_reg_type, user_status not stored as not required currently
			//not updating userid/hp either, unless i understand when that cud happen 
			Enumeration statuskeys = statustable.keys();
			Enumeration statusvalues = statustable.elements();
			while(statusvalues.hasMoreElements())
			{
				String key = (String) statuskeys.nextElement();
				String value = (String) statusvalues.nextElement();
				if(key.equals("last-sync-timestamp"))
				{
					ApplicationAssistant.laststatussynctimestamp = Long.parseLong(value);
					dbmanager_config.updateField("laststatussynctimestamp", "value", (value+"").getBytes());
				}
				else if(key.equals("package"))
				{
					int packageid = Integer.parseInt(value);
					switch (packageid)
					{
						case 2: {
							ApplicationAssistant.contactsynch = true;
							ApplicationAssistant.mediasynch = false;
							ApplicationAssistant.smssynch = false;
							break;
						}
						case 3: {
							ApplicationAssistant.contactsynch = false;
							ApplicationAssistant.mediasynch = true;
							ApplicationAssistant.smssynch = false;
							break;
						}
						case 4: {
							ApplicationAssistant.contactsynch = false;
							ApplicationAssistant.mediasynch = false;
							ApplicationAssistant.smssynch = true;
							break;
						}
						case 5: {
							ApplicationAssistant.contactsynch = true;
							ApplicationAssistant.mediasynch = true;
							ApplicationAssistant.smssynch = false;
							break;
						}
						case 6: {
							ApplicationAssistant.contactsynch = true;
							ApplicationAssistant.mediasynch = false;
							ApplicationAssistant.smssynch = true;
							break;
						}
						case 7: {
							ApplicationAssistant.contactsynch = false;
							ApplicationAssistant.mediasynch = true;
							ApplicationAssistant.smssynch = true;
							break;
						}
						default: {
							ApplicationAssistant.contactsynch = true;
							ApplicationAssistant.mediasynch = true;
							ApplicationAssistant.smssynch = true;
						};
					};
					
					dbmanager_config.updateField("contactsynch", "value", (ApplicationAssistant.contactsynch)?"true".getBytes():"false".getBytes());
					dbmanager_config.updateField("mediasynch", "value", (ApplicationAssistant.mediasynch)?"true".getBytes():"false".getBytes());
					dbmanager_config.updateField("smssynch", "value", (ApplicationAssistant.smssynch)?"true".getBytes():"false".getBytes());
				}
			}
			statuskeys = null;
			statusvalues = null;
			ApplicationAssistant.gc();
		}
		catch(Exception e)
		{
			if(Constants.debug) System.out.println("VaultAPI.updateStatus() " + e.toString()); 			
		}
		finally
		{
			try
			{
				dbmanager_config.close();
			}
			catch(Exception ex)
			{
				if(Constants.debug) System.out.println("VaultAPI.updateStatus() " + ex.toString());
			}
			ApplicationAssistant.gc();
		}
	}
	
	private static String decodeHp(String encodedhp)
	{
		final long MSISDN_ADDITION_FOR_ENC = 10101010;
		return (ApplicationAssistant.getDec(encodedhp) - MSISDN_ADDITION_FOR_ENC) + "";
	}
	
	/**
	 * @param uploadedcount
	 * this is an array of the form { 34, -1, 2}
	 * this will update 
	 * ApplicationAPI.status_uploaded[Constants.LISTINDEX_CONTACTS] = 34
	 * ApplicationAPI.status_uploaded[Constants.LISTINDEX_VIDEOS] = 2
	 * it will leave photo count intact as its -1
	 * 
	 * When this function is invoked, make sure u invoke it with cumulative value 
	 */
	public static void updateUploadedCount(int [] uploadedcount)
	{
		for(int i=0; i<uploadedcount.length; i++)
			if(uploadedcount[i]>=0)
				ApplicationAPI.status_uploaded[i] = uploadedcount[i];
		writeCountsToDB(true);
	}
	
	public static void updateLastSynchTime(long lastsynchtime)
	{
		ApplicationAssistant.lastsynchtime = lastsynchtime;
		try
		{
			dbmanager_config.open();
			dbmanager_config.updateField("lastsynchtime", "value", (lastsynchtime+"").getBytes());
		}
		catch(Exception e)
		{
			if(Constants.debug) System.out.println("VaultAPI.updateLastSynchTime() " + e.toString()); 			
		}
		finally
		{
			try
			{
				dbmanager_config.close();
			}
			catch(Exception ex)
			{
				if(Constants.debug) System.out.println("VaultAPI.updateLastSynchTime() " + ex.toString());
			}
			ApplicationAssistant.gc();
		}
	}
	
	public static void updateToRegistered()
	{
		ApplicationAssistant.registered = Constants.REGISTRATION_TRUE;
		try
		{
			dbmanager_config.open();
			dbmanager_config.updateField("registered", "value", "true".getBytes());
		}
		catch(Exception e)
		{
			if(Constants.debug) System.out.println("VaultAPI.updateToRegistered() " + e.toString()); 			
		}
		finally
		{
			try
			{
				dbmanager_config.close();
			}
			catch(Exception ex)
			{
				if(Constants.debug) System.out.println("VaultAPI.updateToRegistered() " + ex.toString());
			}
			ApplicationAssistant.gc();
		}
	}

	public static void updateToInitialUploadComplete()
	{
		ApplicationAssistant.initialupload = Constants.INITIALUPLOAD_FALSE;
		try
		{
			dbmanager_config.open();
			dbmanager_config.updateField("initialupload", "value", "false".getBytes());
		}
		catch(Exception e)
		{
			if(Constants.debug) System.out.println("VaultAPI.updateToInitiaUploadComplete() " + e.toString()); 			
		}
		finally
		{
			try
			{
				dbmanager_config.close();
			}
			catch(Exception ex)
			{
				if(Constants.debug) System.out.println("VaultAPI.updateToInitiaUploadComplete() " + ex.toString());
			}
			ApplicationAssistant.gc();
		}
	}
	
	public static void updateLanguage(int language)
	{
		ApplicationAssistant.language = language;
		try
		{
			dbmanager_config.open();
			dbmanager_config.updateField("language", "value", (language+"").getBytes());
		}
		catch(Exception e)
		{
			if(Constants.debug) System.out.println("VaultAPI.updateLanguage() " + e.toString()); 			
		}
		finally
		{
			try
			{
				dbmanager_config.close();
			}
			catch(Exception ex)
			{
				if(Constants.debug) System.out.println("VaultAPI.updateLanguage() " + ex.toString());
			}
			ApplicationAssistant.gc();
		}
	}
	
	public static void updateNextScheduledSynchTime(long scheduleperiod)
	{
		long currenttime = System.currentTimeMillis();
		//schedule period has been changed
		if(scheduleperiod != -1) 
		{
			ApplicationAssistant.scheduleperiod = scheduleperiod;
			if(scheduleperiod == 0) //manual synch chosen
				ApplicationAssistant.nextscheduledsynchtime = 0;
			else
				ApplicationAssistant.nextscheduledsynchtime = currenttime + scheduleperiod;
			try
			{
				dbmanager_config.open();
				dbmanager_config.updateField("scheduleperiod", "value", (ApplicationAssistant.scheduleperiod+"").getBytes());
				dbmanager_config.updateField("nextscheduledsynchtime", "value", (ApplicationAssistant.nextscheduledsynchtime+"").getBytes());
			}
			catch(Exception e)
			{
				if(Constants.debug) System.out.println("VaultAPI.updateNextScheduledSynchTime() " + e.toString()); 			
			}
			finally
			{
				try
				{
					dbmanager_config.close();
				}
				catch(Exception ex)
				{
					if(Constants.debug) System.out.println("VaultAPI.updateNextScheduledSynchTime() " + ex.toString());
				}
				ApplicationAssistant.gc();
			}
		}
		//schedule period hasnt been changed -- just need to increment nextsynchtime to the next slot
		else
		{
			while(ApplicationAssistant.nextscheduledsynchtime < currenttime)
			{
				ApplicationAssistant.nextscheduledsynchtime += ApplicationAssistant.scheduleperiod;
			}
		
			try
			{
				dbmanager_config.open();
				dbmanager_config.updateField("nextscheduledsynchtime", "value", (ApplicationAssistant.nextscheduledsynchtime+"").getBytes());
			}
			catch(Exception e)
			{
				if(Constants.debug) System.out.println("VaultAPI.updateNextScheduledSynchTime() " + e.toString()); 			
			}
			finally
			{
				try
				{
					dbmanager_config.close();
				}
				catch(Exception ex)
				{
					if(Constants.debug) System.out.println("VaultAPI.updateNextScheduledSynchTime() " + ex.toString());
				}
				ApplicationAssistant.gc();
			}
		}
	}
	
	private static void getLocalContacts() throws SecurityException, Exception 
	{
		if(Constants.debug) System.out.println("VaultAPI.getLocalContacts()");
		
		PIM pim = PIM.getInstance();
		ContactList clist;
		Enumeration contacts;

		Hashtable contactlocaldtos;
		Hashtable contactmappingdtos;
		
		Vector contactidpattern = new Vector();
		
		localchanges = null;
		localmappings = null;
		
		localchanges = new Vector[3];
		localchanges[Constants.CONTACTS_ADD] = new Vector();
		localchanges[Constants.CONTACTS_UPDATE] = new Vector();
		localchanges[Constants.CONTACTS_DELETE] = new Vector();
		
		localmappings = new Vector[3];
		localmappings[Constants.CONTACTS_ADD] = new Vector();
		localmappings[Constants.CONTACTS_UPDATE] = new Vector();
		localmappings[Constants.CONTACTS_DELETE] = new Vector();
		
		dbmanager_contacts.open();

		Hashtable result [] = dbmanager_contacts.findAll();
				
		dbmanager_contacts.close();
		
		contactlocaldtos = hashtableToContactLocalDTOHashtable(result);
		
		result = null;
		ApplicationAssistant.gc();
		
		dbmanager_contactsynchmaps.open();

		result = dbmanager_contactsynchmaps.findAll();
				
		dbmanager_contactsynchmaps.close();
		
		contactmappingdtos = hashtableToContactMappingDTOHashtable(result);
		
		result = null;
		ApplicationAssistant.gc();
		
		
//		Retrieve contact values
//		The countValues() method returns the number of data values currently set in a particular field.
		try 
		{
		   clist = (ContactList) pim.openPIMList(PIM.CONTACT_LIST, PIM.READ_ONLY);
		   contacts = clist.items();
		} 
		catch(Exception e) 
		{
		    throw new SecurityException("Unable to open contact list");
		}

		if(Constants.debug) System.out.println("VaultAPI.getLocalContacts()1");
		
		int addedcount = 0;
		int count = 0;
		Vector contactdtos = new Vector();
		while(contacts.hasMoreElements())
		{
			if(Constants.debug) System.out.println("VaultAPI.getLocalContacts()2");			
			
			count++;
			Contact c = (Contact) contacts.nextElement();
			
				{
					String contactid="";
					if (clist.isSupportedField(Contact.UID))
						contactid = c.getString(Contact.UID, 0);
					else
						continue;
					
					contactidpattern.addElement(contactid);

					{
						if(Constants.debug) System.out.println("VaultAPI.getLocalContacts() " + "cid " + contactid);						
						
						int type = isContactInLocalDb(contactid, c, contactlocaldtos, contactmappingdtos);
						//ADD
						if(type == 0){ //ADD
							if(Constants.debug) System.out.println("VaultAPI.getLocalContacts() " + "added" + contactid);							
							
							status_uploading[Constants.LISTINDEX_CONTACTS]++;
							addedcount++;
							ContactDTO cdto = new ContactDTO(c, Constants.CONTACTS_ADD);
							contactdtos.addElement(cdto);
							ContactLocalDTO cmdto = new ContactLocalDTO(cdto.getContactid(), "" + cdto.getCheckSumString());
							
							localchanges[Constants.CONTACTS_ADD].addElement(cmdto);
							
							if(Constants.debug) System.out.println("VaultAPI.getLocalContacts()3");							
						}
						else if(type == 1) //UPDATE
						{
							if(Constants.partialdebug) System.out.println("VaultAPI.getLocalContacts() " + "updated" + contactid);
							
							ContactDTO cdto = new ContactDTO(c, Constants.CONTACTS_UPDATE);
							contactdtos.addElement(cdto);
							
							ContactLocalDTO cmdto = new ContactLocalDTO(cdto.getContactid(), "" + cdto.getCheckSumString());
							
							localchanges[Constants.CONTACTS_UPDATE].addElement(cmdto);							
						}
						//DELETE cant be obtained here. finish ADD and UPDATE and check for DELETE outside						
					}
				}
				if(Constants.debug) System.out.println("VaultAPI.getLocalContacts()4");				
		}

//		ApplicationAPI.dialogconfirmation += "1";
		if(Constants.debug) System.out.println("VaultAPI.getLocalContacts()5");
		
		int dbcount = 0;// get db count from RMS
				
		dbcount = contactlocaldtos.size();

		if(Constants.debug) System.out.println("VaultAPI.getLocalContacts()6");		
		
		if(Constants.debug) System.out.println("addedcount = " + addedcount);
		if(Constants.debug) System.out.println("dbcount = " + dbcount);
		if(Constants.debug) System.out.println("contactscount = " + count);
		
		if(addedcount + dbcount != count) //now DELETE check is possible
		{//iterate our DB here and check if contactid is in native DB

			if(Constants.debug) System.out.println("VaultAPI.getLocalContacts()7");			
			
			Enumeration enumer = contactlocaldtos.keys();
			
			while(enumer.hasMoreElements())//for(int i = 0; i < contactlocaldtos.size(); ++i)
			{
				String tmpluid = (String)enumer.nextElement();
				
			   if(Constants.debug) System.out.println("CheckDeletion");
			   if(Constants.debug) System.out.println("luid = " + tmpluid);
			   if(Constants.debug) System.out.println("contactidpattern=" + contactidpattern);
				
			   if(contactidpattern.contains(tmpluid))
			   {// contact is present, so no deletion
			   }
			   else
			   {// deleted contact
				   	if(Constants.debug) System.out.println("VaultAPI.getLocalContacts() " + "delete" + tmpluid);			   	
				   	
				   	status_uploading[Constants.LISTINDEX_CONTACTS]++;
				   	
				   	ContactDTO cdto = new ContactDTO();
				   	cdto.setContactid(tmpluid);
				   	cdto.setChangetype(Constants.CONTACTS_DELETE);
				   	contactdtos.addElement(cdto);
				   	
				   	ContactLocalDTO cldto = new ContactLocalDTO(cdto.getContactid(), "" + cdto.getCheckSumString());
				   	
				   	localchanges[Constants.CONTACTS_DELETE].addElement(cldto);			   	
			   }
			}			
		}

		contactlocaldtos = null;
		ApplicationAssistant.gc();
				
		// check for MAPSA, MAPSD

		if(Constants.debug) System.out.println("VaultAPI.getLocalContacts()8");		
//		ApplicationAPI.dialogconfirmation += "2";
		
		Enumeration enumer = contactmappingdtos.keys();
		
		while(enumer.hasMoreElements())
		{
			String cid = (String)enumer.nextElement();
			ContactMappingDTO cmdto = (ContactMappingDTO)contactmappingdtos.get(cid);
		   
			String tmpluid = cmdto.getContactid();
			String tmpguid = cmdto.getGlobalid();
			int mapop = cmdto.getType();
		   
			ContactDTO cdto = new ContactDTO();
			cdto.setContactid(tmpluid);
			cdto.setGlobalid(tmpguid);
			cdto.setChangetype(mapop);
			contactdtos.addElement(cdto);
			
			if(cmdto.getType() == Constants.CONTACTS_SERVERADD)
				localmappings[Constants.CONTACTS_ADD].addElement(cmdto);
			else if(cmdto.getType() == Constants.CONTACTS_SERVERDELETE)
				localmappings[Constants.CONTACTS_DELETE].addElement(cmdto);
			
			cid = null;
			cmdto = null;
			cdto = null;
		}

		contactmappingdtos = null;
		ApplicationAssistant.gc();
//		ApplicationAPI.dialogconfirmation += "3";
		
		if(Constants.debug) System.out.println("VaultAPI.getLocalContacts()9");		
		
		if(Constants.debug) System.out.println("VaultAPI.getLocalContacts()10");		
		
		contactdtos_to_server = new ContactDTO[contactdtos.size()];
		for(int i=0; i<contactdtos.size(); i++)
		{
			contactdtos_to_server[i] = (ContactDTO) contactdtos.elementAt(i);
		}
		contactdtos = null;
		ApplicationAssistant.gc();
//		ApplicationAPI.dialogconfirmation += "4";
		
		clist.close();
//		ApplicationAPI.dialogconfirmation += "5";
		
		if(Constants.debug) System.out.println("VaultAPI.getLocalContacts()11");		
		
	}

	private static int isContactInLocalDb(String cid, Contact c, Hashtable contactlocaldtos, Hashtable contactmappingdtos) throws Exception
	{
		try{
			if(Constants.debug) System.out.println("VaultAPI.isContactInLocalDb");			
	
			int isthere = 0;
						
			if(Constants.debug) System.out.println("VaultAPI.isContactInLocalDb1");
			
			if(Constants.debug) System.out.println("VaultAPI " + "tuid " + cid);
			
			if(!contactlocaldtos.containsKey(cid)) //doesnt exist
			{
				if(Constants.debug) System.out.println("VaultAPI " + "tuid " + cid + "NOTTHERE");
				
				isthere = 0;
			}
			else
			{
				if(Constants.debug) System.out.println("VaultAPI " + "tuid " + cid + "THERE");
				
				isthere = 1;
			}
			
			if(Constants.debug) System.out.println("VaultAPI.isContactInLocalDb3");			
			
			if(isthere == 0) // not there in local DB but could have just been added from server, check for this condition
			{
				if(Constants.debug) System.out.println("VaultAPI.isContactInLocalDb4");			
								
				if(!contactmappingdtos.containsKey(cid))
				{
					isthere = 0;
				}
				else
				{
					ContactDTO cdto = new ContactDTO(c, Constants.CONTACTS_ADD);
					((ContactMappingDTO)contactmappingdtos.get(cid)).setChecksum("" + cdto.getCheckSumString());
					
					isthere = 2;
				}
				
			}
			else // check for update
			{
				if(Constants.debug) System.out.println("VaultAPI.isContactInLocalDb4a");
				
				ContactDTO cdto = new ContactDTO(c, Constants.CONTACTS_ADD);
				
				if(Constants.debug) System.out.println("VaultAPI.isContactInLocalDb4b");
				
				String checksum = "" + cdto.getCheckSumString();
				
				if(Constants.debug) System.out.println("VaultAPI.isContactInLocalDb4c");
				
				String dbchecksum = ((ContactLocalDTO)contactlocaldtos.get(cid)).getChecksum();//new String((byte[])result.get("checksum"));
				
				if(Constants.debug) System.out.println("VaultAPI.isContactInLocalDb4d");
			
				if(Constants.partialdebug) System.out.println("VaultAPI.isContactInLocalDb checksum = " + checksum);
				if(Constants.partialdebug) System.out.println("VaultAPI.isContactInLocalDb dbchecksum = " + dbchecksum);
				
				if(!checksum.equals(dbchecksum)) //update
				{
					isthere = 1;
				}
				else
				{
					isthere = 2;
				}
			}
			
			if(Constants.debug) System.out.println("VaultAPI.isContactInLocalDb5");
			
			//dbmanager_contacts.close();
			return isthere;
		}
		catch(Exception e)
		{
			if(Constants.debug) System.out.println("VaultAPI " + e.toString());
			
			throw new Exception("Error Reading RMS store");
			//return 0;
			
		}
	}
		
	public static byte[] syncContactsToServer()
	{
		if(Constants.debug) System.out.println("VaultAPI.syncContactsToServerBEGIN");
		
		//status servlet call
		try{
			byte[] xml = XMLHome.getStatusSubscriptionXML();
			
			ConnectionHome connHome = new ConnectionHome();
			byte [] readdata = connHome.send_and_receive(ApplicationAssistant.sub_status_check, xml, /*false,*/ Constants.POST_AND_RECEIVE);
			connHome = null;
			xml = null;
			ApplicationAssistant.gc();
			
			if (ApplicationAPI.getConnstatus() == ConnectionHome.FAILED)
			{
				if (Constants.debug) System.out.println("VaultAPI.syncContactsToServer() connection screwed");
				ApplicationAPI.dialogerror = Constants.MESSAGE_GPRSERROR[ApplicationAssistant.language];
				readdata = null;
				ApplicationAssistant.gc();
				return null;
			}
			
			Hashtable hashtable = XMLHome.parseStatusSubscriptionXml(readdata);
			readdata = null;
			ApplicationAssistant.gc();
			
			String statuscheck = (String)hashtable.get("user_status");
			if(statuscheck.equals("valid") || statuscheck.equals("pendingconfirmation"))
			{
				updateStatus(hashtable);
				hashtable = null;
				ApplicationAssistant.gc();
			}
			else
			{
				hashtable = null;
				ApplicationAssistant.gc();
				ApplicationAPI.dialogerror = Constants.MESSAGE_STATUSSYNCH_ACCOUNTSUSPENDED[ApplicationAssistant.language];
				return null;
			}
		}
		catch(Exception e)
		{
			if(Constants.debug) System.out.println("VaultAPI.syncContactsToServer() "+ e.toString());
			ApplicationAPI.dialogerror = Constants.MESSAGE_SYNCHRONISATON_RECEIVERESPONSEFAIL[ApplicationAssistant.language];
			
			return null;
		}
		
		if(Constants.debug) System.out.println("VaultAPI.syncContactsToServer");
		
		status_uploading[Constants.LISTINDEX_CONTACTS] = 0;
		
		StringBuffer xml = new StringBuffer();
		contactdtos_to_server = null;
		
		try{
			if(Constants.debug) System.out.println("VaultAPI.syncContactsToServer1");			

//			ApplicationAPI.dialogconfirmation = "a";
			try
			{
				getLocalContacts();
			}
			catch(Exception e) 
			{
			    throw new SecurityException("Unable to open contact list");
			}
//			ApplicationAPI.dialogconfirmation += " b";
			if(Constants.debug) System.out.println("VaultAPI.syncContactsToServer2");
			
			String msgtype = "";
			if(ApplicationAssistant.initialupload == Constants.INITIALUPLOAD_TRUE)
				msgtype = XMLHome.SYNC_MSGT_INIT_EU;
			else
				msgtype = XMLHome.SYNC_MSGT_SYNC;
			
			xml.append("<root>");
			xml.append(XMLHome.getHeaderXML(msgtype, "1", "1", ApplicationAssistant.lastsynchtime + "", ""));
			xml.append("<b>");
				
			for(int i=0; i<contactdtos_to_server.length;++i)
				xml.append(XMLHome.getContactsSyncXML(contactdtos_to_server[i]));
			if(contactdtos_to_server.length == 0)
				xml.append("<add></add><update></update><delete></delete>");
			
			xml.append("</b></root>");
			
			contactdtos_to_server = null;
			ApplicationAssistant.gc();
			
			if(Constants.debug) System.out.println("VaultAPI.syncContactsToServer3");

//			ApplicationAPI.dialogconfirmation += " c";
			
			ConnectionHome connHome = new ConnectionHome();
			byte [] readdata = connHome.send_and_receive(ApplicationAssistant.contacts_url, xml.toString().getBytes("UTF-8"), /*false, */Constants.POST_AND_RECEIVE);
			connHome = null;
			xml = null;
			
//			ApplicationAPI.dialogconfirmation += " d";
//			if(true) return null;
			ApplicationAssistant.gc();
			if(Constants.debug) System.out.println("VaultAPI.syncContactsToServer4");
			if (ApplicationAPI.getConnstatus() == ConnectionHome.FAILED)
			{				
				if (Constants.debug) System.out.println("VaultAPI.syncContactsToServer() connection screwed");
				readdata = null;
				ApplicationAssistant.gc();
				return null;
			}
			
			String response = new String(readdata, "UTF-8");
			readdata = null;
			ApplicationAssistant.gc();
			
			int start = response.indexOf("<ack>");
			if(Constants.debug) System.out.println("VaultAPI.syncContactsToServer5");
			
			if(start >= 0)
			{
				if(Constants.debug) System.out.println("VaultAPI.syncContactsToServer6");
				String ack_message="";
				int end = response.indexOf("</ack>");
				
				//ack_message = response.substring(start + "<ack>".length(), end + 1);
				ack_message = response.substring(start + "<ack>".length(), end);
				response = null;
				ApplicationAssistant.gc();				
				if(processContactsAckResponse(ack_message))
				{
					ack_message = null;
					ApplicationAssistant.gc();
					byte [] ret = contactsSendCompleteN();
					if(ret!=null && ApplicationAssistant.initialupload == Constants.INITIALUPLOAD_TRUE)
						updateToInitialUploadComplete();
						
					return ret; 
				}
				else 
				{
					ack_message = null;
					ApplicationAssistant.gc();
					return null;
				}
			}
			else
			{
				response = null;
				ApplicationAssistant.gc();
				return null;
			}
						
		}
		catch (SecurityException e)
		{
			contactdtos_to_server = null;
			xml = null;
			ApplicationAssistant.gc();
			
			ApplicationAPI.dialogerror = Constants.MESSAGE_SYNCHRONISATON_SECURITYERROR[ApplicationAssistant.language];
			if (Constants.debug) System.out.println("VaultAPI.syncContactsToServer() "+ e.toString());
			return null;
		}
		catch (Exception e)
		{
			contactdtos_to_server = null;
			xml = null;
			ApplicationAssistant.gc();
			
			ApplicationAPI.dialogerror = Constants.MESSAGE_SYNCHRONISATON_SENDREQUESTFAIL[ApplicationAssistant.language];
			
			if (Constants.debug) System.out.println("VaultAPI.syncContactsToServer() "+ e.toString());
			return null;
		}
	}
	
	private static boolean processContactsAckResponse(String response)// throws Exception
	{
		if(Constants.debug) System.out.println("VaultAPI.processContactsAckResponse");
		
		int pos = response.indexOf("cpr_sync");
		if (pos >= 0) {
			if(Constants.debug) System.out.println("VaultAPI.processContactsAckResponse = cpr_sync");
/*			response = response.substring()
			packetNum.Copy(&ackMessage[8], ackMessage.Length() - 8);
			TLex lex(packetNum);
			lex.Val(packNumber);

			TBuf<30> tempstr;
			tempstr.Copy(_L("sync"));		
			
			iAppUi.iContactsHandler->iChangesDB->SetMessageType(tempstr);

			iAppUi.iContactsHandler->SendCompleteI(packNumber);*/
			return true;
		}

		pos = response.indexOf("lpr_sync");
		int pos1 = response.indexOf("lpr_eu");
		
		if (pos >= 0 || pos1 >= 0) {
			if(Constants.debug) System.out.println("VaultAPI.processContactsAckResponse = lpr_sync");			
			//contactsSendCompleteN();
			return true;
		}

		pos = response.indexOf("wait_time");
		if (pos >= 0) {
			if(Constants.debug) System.out.println("VaultAPI.processContactsAckResponse " + response);
			
			response = response.substring("wait_time".length(), response.length());

			if(Constants.debug) System.out.println("VaultAPI.processContactsAckResponse waittime=" + response);
			
			polltimeout = Integer.parseInt(response);
			return true;
		}

		pos = response.indexOf("invalid_req");
		if (pos >= 0) {
			if(Constants.debug) System.out.println("VaultAPI.processContactsAckResponse invalid_req");
			
			return false;			
			//throw new Exception("Invalid Sync Request");
			//return;
		}	
		
		return false;
	}
	
	private static byte[] contactsSendCompleteN() throws Exception
	{
		if(Constants.debug) System.out.println("VaultAPI.contactsSendCompleteN");
		StringBuffer pollxml = new StringBuffer();
		
		pollxml.append("<root>");
		pollxml.append(XMLHome.getHeaderXML(XMLHome.SYNC_MSGT_POLL, "1", "1", ApplicationAssistant.lastsynchtime + "", "p" + 1));
		pollxml.append("<b></b></root>");
		
		ConnectionHome connHome = new ConnectionHome();
		byte [] readdata = connHome.send_and_receive(ApplicationAssistant.contacts_url, pollxml.toString().getBytes("UTF-8"), /*false, */Constants.POST_AND_RECEIVE);
		connHome = null;
		pollxml = null;
		ApplicationAssistant.gc();
		
		if(Constants.debug) System.out.println("VaultAPI.contactsSendCompleteN AAA");
		if (ApplicationAPI.getConnstatus() == ConnectionHome.FAILED)
		{
			ApplicationAPI.dialogerror = Constants.MESSAGE_GPRSERROR[ApplicationAssistant.language];
			if (Constants.debug) System.out.println("VaultAPI.contactsSendCompleteN() connection screwed");
			readdata = null;
			ApplicationAssistant.gc();
			return null;
		}
		String response = new String(readdata, "UTF-8");
		int start = response.indexOf("<ack>");
		if(Constants.debug) System.out.println("VaultAPI.contactsSendCompleteN AAA1");
		
		if(start >= 0)
		{
			if(Constants.debug) System.out.println("VaultAPI.contactsSendCompleteN AAA2");
			
			String ack_message="";
			int end = response.indexOf("</ack>");
			
			ack_message = response.substring(start + "<ack>".length(), end);
			
			readdata = null;
			ApplicationAssistant.gc();		
			response = null;
			ApplicationAssistant.gc();
			if(processContactsAckResponse(ack_message))
			{
				ack_message = null;
				ApplicationAssistant.gc();
				return contactsWaitTimePoll(polltimeout);
			}
			else
			{
				ack_message = null;
				ApplicationAssistant.gc();
				return null;
			}
		}
		else
		{
			if(Constants.debug) System.out.println("VaultAPI.contactsSendCompleteN AAA3");
			response = null;
			ApplicationAssistant.gc();
			return readdata;
		}
		
	}
		
	private static byte[] contactsWaitTimePoll(int polltimeout) throws Exception
	{
		if(Constants.debug) System.out.println("VaultAPI.contactsSendCompleteN BBB");

		ApplicationAssistant.pause(polltimeout*1000);
	    
		if(Constants.debug) System.out.println("VaultAPI.contactsSendCompleteN BBB1");
	      
		return contactsSendCompleteN();
	}
	
	public static boolean syncContactsFromServer(byte[] readdata)// throws Exception //Vector[] servercontacts
	{
		try{
		if(Constants.debug) System.out.println("VaultAPI.syncContactsFromServer");
		
		contactdtos_from_server = null;
		ApplicationAssistant.gc();

		contactdtos_from_server = XMLHome.parseContactsSynchXML(readdata);

		if(Constants.debug) System.out.println("VaultAPI.syncContactsFromServer1");
		
		if(contactdtos_from_server[0].size() + contactdtos_from_server[1].size() + contactdtos_from_server[2].size() == 0)
		{
			if(Constants.debug) System.out.println("VaultAPI.syncContactsFromServer2");
			contactsSyncComplete();
			return false;
		}
		else
		{			
			if(Constants.debug) System.out.println("VaultAPI.syncContactsFromServer3");
				
			contactsSyncComplete();
				
			dbmanager_contactsynchmaps.open();
			
			localchanges = null;
			localmappings = null;
			
			localchanges = new Vector[3];
			localchanges[Constants.CONTACTS_ADD] = new Vector();
			localchanges[Constants.CONTACTS_UPDATE] = new Vector();
			localchanges[Constants.CONTACTS_DELETE] = new Vector();
			
			localmappings = new Vector[3];
			localmappings[Constants.CONTACTS_ADD] = new Vector();
			localmappings[Constants.CONTACTS_UPDATE] = new Vector();
			localmappings[Constants.CONTACTS_DELETE] = new Vector();
			
			if(Constants.debug) System.out.println("VaultAPI.syncContactsFromServer4");

			PIM pim = PIM.getInstance();
			
			ContactList clist;
			Enumeration contacts;
//				Retrieve contact values
//				The countValues() method returns the number of data values currently set in a particular field.
			try 
			{
			    clist = (ContactList) pim.openPIMList(PIM.CONTACT_LIST, PIM.READ_WRITE);
				//clist = (ContactList) pim.openPIMList(PIM.CONTACT_LIST, PIM.WRITE_ONLY);
				contacts = clist.items();
			} 
			catch(Exception e) 
			{
				if(Constants.debug) System.out.println("VaultAPI.syncContactsFromServer55 " + e.toString());
			    //security or other exception
				throw new SecurityException("sychContactsFromServer failed!");
			}
			
			if(Constants.debug) System.out.println("VaultAPI.syncContactsFromServer5");
			
			if(Constants.debug) System.out.println("VaultAPI.syncContactsFromServer5");
			while(contacts.hasMoreElements())
			{
				if(Constants.debug) System.out.println("VaultAPI.syncContactsFromServer6");
				Contact c = (Contact) contacts.nextElement();
				for(int loopindex = 0; loopindex < contactdtos_from_server[1].size(); ++loopindex)
				{
					if(Constants.debug) System.out.println("VaultAPI.syncContactsFromServer7 updating contact");
						
					if(c.getString(Contact.UID, 0).equals(((ContactDTO)contactdtos_from_server[1].elementAt(loopindex)).getContactid()))
					{// match for update
						
						if(Constants.debug) System.out.println("VaultAPI.syncContactsFromServer9");
						ContactDTO cdto = (ContactDTO) contactdtos_from_server[1].elementAt(loopindex);
														
						//Contact contact = clist.createContact();
					    
						if(Constants.debug) System.out.println("VaultAPI.syncContactsFromServer10");						
					    
						if(clist.isSupportedField(Contact.ORG)){
							try{
								c.setString(Contact.ORG, 0, Contact.ATTR_NONE, cdto.getCompanyname());
							}
							catch(FieldEmptyException e)
							{
								try{
									c.addString(Contact.ORG, Contact.ATTR_NONE, cdto.getCompanyname());
								}
								catch(Exception ex)
								{
									if(Constants.debug) System.out.println("VaultAPI ORG" + ex.toString());
								}
							}							
							catch(Exception e){
								if(Constants.debug) System.out.println("VaultAPI ORG" + e.toString());
							}
						}
							
						if(Constants.debug) System.out.println("VaultAPI.syncContactsFromServer111");
						
						Vector fieldvec = cdto.getJobtitles();
						
						if(clist.isSupportedField(Contact.TITLE))
							if(fieldvec.size() > 0){
								try{
									c.setString(Contact.TITLE, 0, Contact.ATTR_NONE, (String)fieldvec.elementAt(0));
								}
								catch(FieldEmptyException e)
								{
									try{
										c.addString(Contact.TITLE, Contact.ATTR_NONE, (String)fieldvec.elementAt(0));
									}
									catch(Exception ex)
									{
										if(Constants.debug) System.out.println("VaultAPI JOBTITLE" + e.toString());
									}
								}															
								catch(Exception e)
								{
									if(Constants.debug) System.out.println("VaultAPI JOBTITLE" + e.toString());
								}
							}

					    if(Constants.debug) System.out.println("VaultAPI.syncContactsFromServer12");

					    fieldvec = null;
					    fieldvec = cdto.getMobiles();
					    
					    if(Constants.partialdebug) System.out.println("VaultAPI.syncContactsFromServer12");
					    
					    try{
					    int phoneNumbers = c.countValues(Contact.TEL);
					    for(int i = 0; i < phoneNumbers; ++i){
					    	if(Constants.partialdebug) System.out.println("VaultAPI.syncContactsFromServer12 i = " + i);
					    	c.removeValue(Contact.TEL, 0);
					    }
					    }
					    catch(Exception e)
						{
					    	if(Constants.partialdebug) System.out.println("VaultAPI.syncContactsFromServer12 " + e.toString());
						}
					    
					    if(Constants.partialdebug) System.out.println("VaultAPI.syncContactsFromServer12");
					    
					    if (fieldvec.size() > 0)
					    {
							for (int index = 0; index < fieldvec.size(); ++index){
								if(!((String)fieldvec.elementAt(index)).equals(""))
								{
									try{
										c.addString(Contact.TEL, Contact.ATTR_MOBILE, (String)fieldvec.elementAt(index));					
					    			}
									catch(FieldFullException e)
									{
										try{
											c.setString(Contact.TEL, index, Contact.ATTR_MOBILE, (String)fieldvec.elementAt(index));
										}
										catch(Exception ex)
										{
											if(Constants.debug) System.out.println("VaultAPI TEL" + e.toString());
										}
									}																								
									catch(Exception e){
										if(Constants.debug) System.out.println("VaultAPI TEL" + e.toString());
									}
								}
							}
						}

					    if(Constants.debug) System.out.println("VaultAPI.syncContactsFromServer13");

					    fieldvec = null;
					    fieldvec = cdto.getTelephones();
					    
					    if (fieldvec.size() > 0)
					    {
							for (int index = 0; index < fieldvec.size(); ++index){
								if(!((String)fieldvec.elementAt(index)).equals(""))					    
								{
									try{
										c.addString(Contact.TEL, Contact.ATTR_NONE, (String)fieldvec.elementAt(index));										
					    			}
									catch(FieldFullException e)
									{
										try{
											c.setString(Contact.TEL, index, Contact.ATTR_NONE, (String)fieldvec.elementAt(index));
										}
										catch(Exception ex)
										{
											if(Constants.debug) System.out.println("VaultAPI TEL NONE" + e.toString());
										}
									}																																	
									catch(Exception e){
										if(Constants.debug) System.out.println("VaultAPI TEL NONE" + e.toString());
									}

								}
							}
						}
							
					    if(Constants.debug) System.out.println("VaultAPI.syncContactsFromServer14");
					    
					    fieldvec = null;
					    fieldvec = cdto.getHometelephones();
					    
					    if (fieldvec.size() > 0)
					    {
							for (int index = 0; index < fieldvec.size(); ++index){
								if(!((String)fieldvec.elementAt(index)).equals(""))
								{					    
									try{
										c.addString(Contact.TEL, Contact.ATTR_HOME, (String)fieldvec.elementAt(index));
					    			}
									catch(FieldFullException e)
									{
										try{
											c.setString(Contact.TEL, index, Contact.ATTR_HOME, (String)fieldvec.elementAt(index));											
										}
										catch(Exception ex)
										{
											if(Constants.debug) System.out.println("VaultAPI TEL HOME" + e.toString());
										}
									}																																	
									catch(Exception e){
										if(Constants.debug) System.out.println("VaultAPI TEL HOME" + e.toString());
									}
					    
								}
							}
						}
							
					    if(Constants.debug) System.out.println("VaultAPI.syncContactsFromServer15");
					
					    fieldvec = null;
					    fieldvec = cdto.getWorktelephones();
					    
					    if (fieldvec.size() > 0)
					    {
							for (int index = 0; index < fieldvec.size(); ++index){
								if(!((String)fieldvec.elementAt(index)).equals(""))
								{					    
									try{
										c.addString(Contact.TEL, Contact.ATTR_WORK, (String)fieldvec.elementAt(index));
								    }
									catch(FieldFullException e)
									{
										try{
											c.setString(Contact.TEL, index, Contact.ATTR_WORK, (String)fieldvec.elementAt(index));											
										}
										catch(Exception ex)
										{
											if(Constants.debug) System.out.println("VaultAPI TEL WORK" + e.toString());
										}
									}																																										
									catch(Exception e){
										if(Constants.debug) System.out.println("VaultAPI TEL WORK" + e.toString());
									}
					    
								}
							}
						}
							
					    if(Constants.debug) System.out.println("VaultAPI.syncContactsFromServer16");

					    fieldvec = null;
					    fieldvec = cdto.getEmails();

					    if(Constants.partialdebug) System.out.println("VaultAPI.syncContactsFromServer12EMAIL");
					    try{
					    int emailcount = c.countValues(Contact.EMAIL);
					    for(int i = 0; i < emailcount; ++i)
					    	c.removeValue(Contact.EMAIL, 0);
					    
					    if(Constants.partialdebug) System.out.println("VaultAPI.syncContactsFromServer12EMAIL");
					    
					    if (fieldvec.size() > 0)
					    {
							for (int index = 0; index < fieldvec.size(); ++index){
								if(!((String)fieldvec.elementAt(index)).equals(""))
								{					    
									try{
										c.addString(Contact.EMAIL, Contact.ATTR_NONE, (String)fieldvec.elementAt(index));
					    			}
									catch(FieldFullException e)
									{
										try{
											c.setString(Contact.EMAIL, index, Contact.ATTR_NONE, (String)fieldvec.elementAt(index));											
										}
										catch(Exception ex)
										{
											if(Constants.debug) System.out.println("VaultAPI EMAIL HOME" + e.toString());
										}
									}																																																			
									catch(Exception e){
										if(Constants.debug) System.out.println("VaultAPI EMAIL NONE" + e.toString());
									}
									
								}
							}
						}
							
					    if(Constants.debug) System.out.println("VaultAPI.syncContactsFromServer17");
					    
					    fieldvec = null;
					    fieldvec = cdto.getHomeemails();
					    
					    if (fieldvec.size() > 0)
					    {
							for (int index = 0; index < fieldvec.size(); ++index){
								if(!((String)fieldvec.elementAt(index)).equals(""))
								{
									try{
										c.addString(Contact.EMAIL, Contact.ATTR_HOME, (String)fieldvec.elementAt(index));										
					    			}
									catch(FieldFullException e)
									{
										try{
											c.setString(Contact.EMAIL, index, Contact.ATTR_HOME, (String)fieldvec.elementAt(index));
										}
										catch(Exception ex)
										{
											if(Constants.debug) System.out.println("VaultAPI EMAIL HOME" + e.toString());
										}
									}																																																												
									catch(Exception e){
										if(Constants.debug) System.out.println("VaultAPI EMAIL HOME" + e.toString());
									}
								}
							}
						}

					    if(Constants.debug) System.out.println("VaultAPI.syncContactsFromServer18");

					    fieldvec = null;
					    fieldvec = cdto.getWorkemails();
					    
					    if (fieldvec.size() > 0)
					    {
							for (int index = 0; index < fieldvec.size(); ++index){
								if(!((String)fieldvec.elementAt(index)).equals(""))
								{					    
									try{
										c.addString(Contact.EMAIL, Contact.ATTR_WORK, (String)fieldvec.elementAt(index));
					    			}
									catch(FieldFullException e)
									{
										try{
											c.setString(Contact.EMAIL, index, Contact.ATTR_WORK, (String)fieldvec.elementAt(index));											
										}
										catch(Exception ex)
										{
											if(Constants.debug) System.out.println("VaultAPI EMAIL WORK" + e.toString());
										}
									}																																																																					
									catch(Exception e){
										if(Constants.debug) System.out.println("VaultAPI EMAIL WORK" + e.toString());
									}							
								}
							}
						}			    
					    }
					    catch(Exception e)
					    {
					    	if(Constants.debug) System.out.println("VaultAPI EMAIL mostly unsupported:::" + e.toString());
					    }
					    
					    if(Constants.debug) System.out.println("VaultAPI.syncContactsFromServer19");

					    fieldvec = null;
					    fieldvec = cdto.getFaxes();
					    
					    if (fieldvec.size() > 0)
					    {
							for (int index = 0; index < fieldvec.size(); ++index){
								if(!((String)fieldvec.elementAt(index)).equals(""))
								{					    
									try{
										c.addString(Contact.TEL, Contact.ATTR_FAX, (String)fieldvec.elementAt(index));
					    			}
									catch(FieldFullException e)
									{
										try{
											c.setString(Contact.TEL, index, Contact.ATTR_FAX, (String)fieldvec.elementAt(index));											
										}
										catch(Exception ex)
										{
											if(Constants.debug) System.out.println("VaultAPI FAX" + e.toString());
										}
									}																																																																														
									catch(Exception e){
										if(Constants.debug) System.out.println("VaultAPI FAX" + e.toString());
									}
								}
							}
						}			    
					    
					    if(Constants.debug) System.out.println("VaultAPI.syncContactsFromServer20");

						if(clist.isSupportedField(Contact.NAME))
						{
							if(Constants.debug) System.out.println("NAME FIELD IS SUPPORTED");
								
							String[] n = new String[clist.stringArraySize(Contact.NAME)];
							
							if(Constants.debug) System.out.println("STAGE1");
							
							if(clist.isSupportedArrayElement(Contact.NAME, Contact.NAME_GIVEN)){
								if(Constants.debug) System.out.println("STAGE1");
								if(Constants.partialdebug) System.out.println("NAME_GIVEN FIELD IS SUPPORTED");
								if(Constants.partialdebug) System.out.println("\nfname=" + cdto.getFirstname());
								n[Contact.NAME_GIVEN] = cdto.getFirstname();
								if(Constants.debug) System.out.println("STAGE2");
							}
							if(clist.isSupportedArrayElement(Contact.NAME, Contact.NAME_FAMILY)){
								if(Constants.debug) System.out.println("STAGE3");
								if(Constants.partialdebug) System.out.println("NAME_FAMILY FIELD IS SUPPORTED");
								if(Constants.partialdebug) System.out.println("\nlname=" + cdto.getLastname());
								n[Contact.NAME_FAMILY] = cdto.getLastname();
								if(Constants.debug) System.out.println("STAGE4");
							}
				
							try{
								if(Constants.partialdebug) System.out.println("setstring");
								if(Constants.debug) System.out.println("STAGE5");
								c.setStringArray(Contact.NAME, 0, PIMItem.ATTR_NONE, n);
								if(Constants.debug) System.out.println("STAGE6");
							}
							catch(FieldEmptyException e)
							{
								if(Constants.partialdebug) System.out.println("addstring");
								if(Constants.debug) System.out.println("STAGE7");
								c.addStringArray(Contact.NAME, PIMItem.ATTR_NONE, n);
								if(Constants.debug) System.out.println("STAGE8");
							}
							catch(IndexOutOfBoundsException  e)
							{
								if(Constants.partialdebug) System.out.println("Wrong index when setting string array " + e.toString());
								if(Constants.debug) System.out.println("STAGE9");
								c.addStringArray(Contact.NAME, PIMItem.ATTR_NONE, n);
								if(Constants.debug) System.out.println("STAGE10");
							}
							
						}
						else{
							if(Constants.debug) System.out.println("NAME FIELD IS NOT SUPPORTED");
							
							if(clist.isSupportedField(Contact.FORMATTED_NAME))
							if(!(cdto.getFirstname() + cdto.getLastname()).equals("")){

								try{
									c.setString(Contact.FORMATTED_NAME, 0, Contact.ATTR_NONE, (cdto.getLastname() + " " + cdto.getFirstname()).trim());
								}
								catch(FieldEmptyException e)
								{
									c.addString(Contact.FORMATTED_NAME, Contact.ATTR_NONE, (cdto.getLastname() + " " + cdto.getFirstname()).trim());
								}
								
							}
						}
						
						if(Constants.debug) System.out.println("VaultAPI.syncContactsFromServer11");

						if(clist.isSupportedField(Contact.ADDR))
						{
							if(Constants.debug) System.out.println("ADDR FIELD IS SUPPORTED");
								
							String[] addr = new String[clist.stringArraySize(Contact.ADDR)];
							if(clist.isSupportedArrayElement(Contact.ADDR, Contact.ADDR_COUNTRY))
							{
								addr[Contact.ADDR_COUNTRY] = cdto.getAddressCountry();
							}

							if(clist.isSupportedArrayElement(Contact.ADDR, Contact.ADDR_EXTRA))
							{
								addr[Contact.ADDR_EXTRA] = cdto.getAddressExtra();
							}

							if(clist.isSupportedArrayElement(Contact.ADDR, Contact.ADDR_LOCALITY))
							{
								addr[Contact.ADDR_LOCALITY] = cdto.getAddressLocality();
							}

							if(clist.isSupportedArrayElement(Contact.ADDR, Contact.ADDR_POBOX))
							{
								addr[Contact.ADDR_POBOX] = cdto.getAddressPobox();
							}

							if(clist.isSupportedArrayElement(Contact.ADDR, Contact.ADDR_POSTALCODE))
							{
								addr[Contact.ADDR_POSTALCODE] = cdto.getAddressPostalcode();
							}

							if(clist.isSupportedArrayElement(Contact.ADDR, Contact.ADDR_REGION))
							{
								addr[Contact.ADDR_REGION] = cdto.getAddressRegion();
							}

							if(clist.isSupportedArrayElement(Contact.ADDR, Contact.ADDR_STREET))
							{
								addr[Contact.ADDR_STREET] = cdto.getAddressStreet();
							}
							
							if(addr != null && addr.length <= clist.stringArraySize(Contact.ADDR))
							{
								if(Constants.debug) System.out.println("STAGE1");
													
								try{
									if(Constants.partialdebug) System.out.println("setstring");
									if(Constants.debug) System.out.println("STAGE5");
									c.setStringArray(Contact.ADDR, 0, PIMItem.ATTR_NONE, addr);
									if(Constants.debug) System.out.println("STAGE6");
								}
								catch(FieldEmptyException e)
								{
									if(Constants.partialdebug) System.out.println("addstring");
									if(Constants.debug) System.out.println("STAGE7");
									c.addStringArray(Contact.ADDR, PIMItem.ATTR_NONE, addr);
									if(Constants.debug) System.out.println("STAGE8");
								}
								catch(IndexOutOfBoundsException  e)
								{
									if(Constants.partialdebug) System.out.println("Wrong index when setting string array " + e.toString());
									if(Constants.debug) System.out.println("STAGE9");
									c.addStringArray(Contact.ADDR, PIMItem.ATTR_NONE, addr);
									if(Constants.debug) System.out.println("STAGE10");
								}
							}
						}

						if(clist.isSupportedField(Contact.BIRTHDAY)){
							try{
								c.setDate(Contact.BIRTHDAY, 0, Contact.ATTR_NONE, cdto.getBirthday());
							}
							catch(FieldEmptyException e)
							{
								try{
									c.addDate(Contact.BIRTHDAY, Contact.ATTR_NONE, cdto.getBirthday());
								}
								catch(Exception ex)
								{
									if(Constants.debug) System.out.println("VaultAPI BIRTHDAY" + ex.toString());
								}
							}							
							catch(Exception e){
								if(Constants.debug) System.out.println("VaultAPI BIRTHDAY" + e.toString());
							}
						}

						if(clist.isSupportedField(Contact.NICKNAME)){
							try{
								c.setString(Contact.NICKNAME, 0, Contact.ATTR_NONE, cdto.getNickname());
							}
							catch(FieldEmptyException e)
							{
								try{
									c.addString(Contact.NICKNAME, Contact.ATTR_NONE, cdto.getNickname());
								}
								catch(Exception ex)
								{
									if(Constants.debug) System.out.println("VaultAPI NICKNAME" + ex.toString());
								}
							}							
							catch(Exception e){
								if(Constants.debug) System.out.println("VaultAPI NICKNAME" + e.toString());
							}
						}
						
						if(clist.isSupportedField(Contact.NOTE)){
							try{
								c.setString(Contact.NOTE, 0, Contact.ATTR_NONE, cdto.getNote());
							}
							catch(FieldEmptyException e)
							{
								try{
									c.addString(Contact.NOTE, Contact.ATTR_NONE, cdto.getNote());
								}
								catch(Exception ex)
								{
									if(Constants.debug) System.out.println("VaultAPI NOTE" + ex.toString());
								}
							}							
							catch(Exception e){
								if(Constants.debug) System.out.println("VaultAPI NOTE" + e.toString());
							}
						}
						
						if(clist.isSupportedField(Contact.URL)){
							try{
								c.setString(Contact.URL, 0, Contact.ATTR_NONE, cdto.getUrl());
							}
							catch(FieldEmptyException e)
							{
								try{
									c.addString(Contact.URL, Contact.ATTR_NONE, cdto.getUrl());
								}
								catch(Exception ex)
								{
									if(Constants.debug) System.out.println("VaultAPI URL" + ex.toString());
								}
							}							
							catch(Exception e){
								if(Constants.debug) System.out.println("VaultAPI URL" + e.toString());
							}
						}
						
					    try
						{
					    	c.commit();
						}	
						catch(Exception e) 
						{
							if(Constants.debug) System.out.println("VaultAPI.syncContactsFromServer55 " + e.toString());
						    //security or other exception
							throw new SecurityException("sychContactsFromServer failed!");
						}
						
						ContactMappingDTO cmdto = new ContactMappingDTO(cdto.getContactid(), cdto.getGlobalid(), "1");
						cmdto.setChecksum("" + cdto.getCheckSumString());
						
						localmappings[Constants.CONTACTS_UPDATE].addElement(cmdto);
						
					}
				}
				
				for(int index = 0; index < contactdtos_from_server[2].size(); ++index)
				{//match for delete
					if(Constants.debug) System.out.println("VaultAPI.syncContactsFromServer7 deleting contact");
					
					if(c.getString(Contact.UID, 0).equals(((ContactDTO)contactdtos_from_server[2].elementAt(index)).getContactid()))
					{// match for delete
						if(Constants.debug) System.out.println("VaultAPI.syncContactsFromServer7 deleting contact: MATCH");
						
						clist.removeContact(c);
						byte[][] data = new byte[3][];
						data[0] = ((ContactDTO)contactdtos_from_server[2].elementAt(index)).getContactid().getBytes();
						data[1] = ((ContactDTO)contactdtos_from_server[2].elementAt(index)).getGlobalid().getBytes();
						data[2] = (Constants.CONTACTS_SERVERDELETE + "").getBytes();
						dbmanager_contactsynchmaps.createDBRecord(data);
						
						ContactMappingDTO cmdto = new ContactMappingDTO(new String(data[0]), "", "2");
						
						localmappings[Constants.CONTACTS_DELETE].addElement(cmdto);
									  
						data = null;
					}
				}
				
			}
			
			if(Constants.debug) System.out.println("VaultAPI.syncContactsFromServer8");
			
			if(Constants.debug) System.out.println("VaultAPI.contactdtos_from_server ADD size=" + contactdtos_from_server[0].size());
			
			for(int loopindex = 0; loopindex < contactdtos_from_server[0].size(); ++loopindex)
			{// add contacts
				if(Constants.debug) System.out.println("VaultAPI.syncContactsFromServer9");
				ContactDTO cdto = (ContactDTO) contactdtos_from_server[0].elementAt(loopindex);
				
				Contact contact = clist.createContact();
			    
				if(Constants.debug) System.out.println("VaultAPI.syncContactsFromServer10");
				
				if(clist.isSupportedField(Contact.NAME))
				{
					if(Constants.debug) System.out.println("NAME FIELD IS SUPPORTED");
						
					String[] n = new String[clist.stringArraySize(Contact.NAME)];
											
					if(clist.isSupportedArrayElement(Contact.NAME, Contact.NAME_GIVEN)){
						if(Constants.debug) System.out.println("NAME_GIVEN FIELD IS SUPPORTED");
						if(Constants.debug) System.out.println("\nfname=" + cdto.getFirstname());
						n[Contact.NAME_GIVEN] = cdto.getFirstname();
					}
					if(clist.isSupportedArrayElement(Contact.NAME, Contact.NAME_FAMILY)){
						if(Constants.debug) System.out.println("NAME_FAMILY FIELD IS SUPPORTED");
						if(Constants.debug) System.out.println("\nlname=" + cdto.getLastname());
						n[Contact.NAME_FAMILY] = cdto.getLastname();
					}
					
					contact.addStringArray(Contact.NAME, PIMItem.ATTR_NONE, n);
				}
				else{
					if(Constants.debug) System.out.println("NAME FIELD IS NOT SUPPORTED");
						
					if(clist.isSupportedField(Contact.FORMATTED_NAME))
					if(!(cdto.getFirstname() + cdto.getLastname()).equals(""))
						contact.addString(Contact.FORMATTED_NAME, PIMItem.ATTR_NONE, (cdto.getLastname() + " " + cdto.getFirstname()).trim());
				}
				
				if(Constants.debug) System.out.println("VaultAPI.syncContactsFromServer11");
			    
				if(clist.isSupportedField(Contact.ORG)){
					try{
						contact.addString(Contact.ORG, PIMItem.ATTR_NONE, cdto.getCompanyname());
					}
					catch(Exception e){
						if(Constants.debug) System.out.println("VaultAPI ORG" + e.toString());
					}
				}
					
				if(Constants.debug) System.out.println("VaultAPI.syncContactsFromServer111");
				
				Vector fieldvec;
				fieldvec = cdto.getJobtitles();
				
				if(clist.isSupportedField(Contact.TITLE))
					if(fieldvec.size() > 0){
						try{
							contact.addString(Contact.TITLE, PIMItem.ATTR_NONE, (String)fieldvec.elementAt(0));
						}
						catch(Exception e)
						{
							if(Constants.debug) System.out.println("VaultAPI JOBTITLE" + e.toString());
						}
					}

			    if(Constants.debug) System.out.println("VaultAPI.syncContactsFromServer12");
			    
			    fieldvec = null;
			    fieldvec = cdto.getMobiles();
			    
			    if (fieldvec.size() > 0)
			    {
					for (int index = 0; index < fieldvec.size(); ++index){
						if(!((String)fieldvec.elementAt(index)).equals(""))
						{
							try{
								contact.addString(Contact.TEL, Contact.ATTR_MOBILE, (String)fieldvec.elementAt(index));
			    			}
							catch(Exception e){
								if(Constants.debug) System.out.println("VaultAPI TEL" + e.toString());
							}
						}
					}
				}

			    if(Constants.debug) System.out.println("VaultAPI.syncContactsFromServer13");

			    fieldvec = null;
			    fieldvec = cdto.getTelephones();
			    
			    if (fieldvec.size() > 0)
			    {
					for (int index = 0; index < fieldvec.size(); ++index){
						if(!((String)fieldvec.elementAt(index)).equals(""))
						{
							try{
								contact.addString(Contact.TEL, Contact.ATTR_NONE, (String)fieldvec.elementAt(index));
			    			}	
							catch(Exception e){
								if(Constants.debug) System.out.println("VaultAPI TEL NONE" + e.toString());
							}

						}
					}
				}
					
			    if(Constants.debug) System.out.println("VaultAPI.syncContactsFromServer14");

			    fieldvec = null;
			    fieldvec = cdto.getHometelephones();
			    
			    if (fieldvec.size() > 0)
			    {
					for (int index = 0; index < fieldvec.size(); ++index){
						if(!((String)fieldvec.elementAt(index)).equals(""))
						{
							try{
								contact.addString(Contact.TEL, Contact.ATTR_HOME, (String)fieldvec.elementAt(index));
			    			}
							catch(Exception e){
								if(Constants.debug) System.out.println("VaultAPI TEL HOME" + e.toString());
							}
			    
						}
					}
				}
					
			    if(Constants.debug) System.out.println("VaultAPI.syncContactsFromServer15");

			    fieldvec = null;
			    fieldvec = cdto.getWorktelephones();
			    
			    if (fieldvec.size() > 0)
			    {
					for (int index = 0; index < fieldvec.size(); ++index){
						if(!((String)fieldvec.elementAt(index)).equals(""))			    
						{
							try{
								contact.addString(Contact.TEL, Contact.ATTR_WORK, (String)fieldvec.elementAt(index));
						    }
							catch(Exception e){
								if(Constants.debug) System.out.println("VaultAPI TEL WORK" + e.toString());
							}
			    
						}
					}
				}
					
			    if(Constants.debug) System.out.println("VaultAPI.syncContactsFromServer16");

			    fieldvec = null;
			    fieldvec = cdto.getEmails();
			    
			    if (fieldvec.size() > 0)
			    {
					for (int index = 0; index < fieldvec.size(); ++index){
						if(!((String)fieldvec.elementAt(index)).equals(""))			    
						{			    
							try{
								contact.addString(Contact.EMAIL, Contact.ATTR_NONE, (String)fieldvec.elementAt(index));
			    			}
							catch(Exception e){
								if(Constants.debug) System.out.println("VaultAPI EMAIL NONE" + e.toString());
							}
							
						}
					}
				}
					
			    if(Constants.debug) System.out.println("VaultAPI.syncContactsFromServer17");

			    fieldvec = null;
			    fieldvec = cdto.getHomeemails();
			    
			    if (fieldvec.size() > 0)
			    {
					for (int index = 0; index < fieldvec.size(); ++index){
						if(!((String)fieldvec.elementAt(index)).equals(""))			    			    
						{
							try{
								contact.addString(Contact.EMAIL, Contact.ATTR_HOME, (String)fieldvec.elementAt(index));
			    			}
							catch(Exception e){
								if(Constants.debug) System.out.println("VaultAPI EMAIL HOME" + e.toString());
							}
						}
					}
				}

			    if(Constants.debug) System.out.println("VaultAPI.syncContactsFromServer18");

			    fieldvec = null;
			    fieldvec = cdto.getWorkemails();
			    
			    if (fieldvec.size() > 0)
			    {
					for (int index = 0; index < fieldvec.size(); ++index){
						if(!((String)fieldvec.elementAt(index)).equals(""))			    			    
						{			    
							try{
								contact.addString(Contact.EMAIL, Contact.ATTR_WORK, (String)fieldvec.elementAt(index));
			    			}			    
							catch(Exception e){
								if(Constants.debug) System.out.println("VaultAPI EMAIL WORK" + e.toString());
							}							
						}
					}
				}			    

			    if(Constants.debug) System.out.println("VaultAPI.syncContactsFromServer19");

			    fieldvec = null;
			    fieldvec = cdto.getFaxes();
			    
			    if (fieldvec.size() > 0)
			    {
					for (int index = 0; index < fieldvec.size(); ++index){
						if(!((String)fieldvec.elementAt(index)).equals(""))			    			    
						{			    			    
							try{
								contact.addString(Contact.TEL, Contact.ATTR_FAX, (String)fieldvec.elementAt(index));
			    			}			    
							catch(Exception e){
								if(Constants.debug) System.out.println("VaultAPI EMAIL HOME" + e.toString());
							}
						}
					}
				}			    
			    
				if(clist.isSupportedField(Contact.ADDR))
				{
					if(Constants.debug) System.out.println("ADDR FIELD IS SUPPORTED");
						
					String[] addr = new String[clist.stringArraySize(Contact.ADDR)];
					if(clist.isSupportedArrayElement(Contact.ADDR, Contact.ADDR_COUNTRY))
					{
						addr[Contact.ADDR_COUNTRY] = cdto.getAddressCountry();
					}

					if(clist.isSupportedArrayElement(Contact.ADDR, Contact.ADDR_EXTRA))
					{
						addr[Contact.ADDR_EXTRA] = cdto.getAddressExtra();
					}

					if(clist.isSupportedArrayElement(Contact.ADDR, Contact.ADDR_LOCALITY))
					{
						addr[Contact.ADDR_LOCALITY] = cdto.getAddressLocality();
					}

					if(clist.isSupportedArrayElement(Contact.ADDR, Contact.ADDR_POBOX))
					{
						addr[Contact.ADDR_POBOX] = cdto.getAddressPobox();
					}

					if(clist.isSupportedArrayElement(Contact.ADDR, Contact.ADDR_POSTALCODE))
					{
						addr[Contact.ADDR_POSTALCODE] = cdto.getAddressPostalcode();
					}

					if(clist.isSupportedArrayElement(Contact.ADDR, Contact.ADDR_REGION))
					{
						addr[Contact.ADDR_REGION] = cdto.getAddressRegion();
					}

					if(clist.isSupportedArrayElement(Contact.ADDR, Contact.ADDR_STREET))
					{
						addr[Contact.ADDR_STREET] = cdto.getAddressStreet();
					}
					
					if(addr != null && addr.length <= clist.stringArraySize(Contact.ADDR))
					{
						if(Constants.debug) System.out.println("STAGE1");
											
						try{
							if(Constants.partialdebug) System.out.println("setstring");
							if(Constants.debug) System.out.println("STAGE5");
							contact.addStringArray(Contact.ADDR, PIMItem.ATTR_NONE, addr);
							if(Constants.debug) System.out.println("STAGE6");
						}
						catch(Exception e)
						{
							if(Constants.partialdebug) System.out.println("addstring");
							if(Constants.debug) System.out.println("STAGE7: " + e.toString());
						}
					}
				}

				if(clist.isSupportedField(Contact.BIRTHDAY)){
					try{
						contact.addDate(Contact.BIRTHDAY, PIMItem.ATTR_NONE, cdto.getBirthday());
					}
					catch(Exception e){
						if(Constants.debug) System.out.println("VaultAPI ORG" + e.toString());
					}
				}

				if(clist.isSupportedField(Contact.NICKNAME)){
					try{
						contact.addString(Contact.NICKNAME, PIMItem.ATTR_NONE, cdto.getNickname());
					}
					catch(Exception e){
						if(Constants.debug) System.out.println("VaultAPI ORG" + e.toString());
					}
				}
				
				if(clist.isSupportedField(Contact.NOTE)){
					try{
						contact.addString(Contact.NOTE, PIMItem.ATTR_NONE, cdto.getNote());
					}
					catch(Exception e){
						if(Constants.debug) System.out.println("VaultAPI ORG" + e.toString());
					}
				}
				
				if(clist.isSupportedField(Contact.URL)){
					try{
						contact.addString(Contact.URL, PIMItem.ATTR_NONE, cdto.getUrl());
					}
					catch(Exception e){
						if(Constants.debug) System.out.println("VaultAPI ORG" + e.toString());
					}
				}
							    
			    if(Constants.debug) System.out.println("VaultAPI.syncContactsFromServer20");
			    
			    try
				{
			    	contact.commit();
				}	
				catch(Exception e) 
				{
					if(Constants.debug) System.out.println("VaultAPI.syncContactsFromServer55 " + e.toString());
				    //security or other exception
					throw new SecurityException("sychContactsFromServer failed!");
				}
				
			    if(Constants.debug) System.out.println("VaultAPI.syncContactsFromServer21 CONTACTID = " + contact.getString(Contact.UID, 0));
			    if(Constants.debug) System.out.println("VaultAPI.syncContactsFromServer21 GLOBALID = " + ((ContactDTO)contactdtos_from_server[0].elementAt(loopindex)).getGlobalid());
			    if(Constants.debug) System.out.println("VaultAPI.syncContactsFromServer21 SERVERADD = " + (Constants.CONTACTS_SERVERADD + ""));
			    
				byte[][] data = new byte[3][];
				data[0] = contact.getString(Contact.UID, 0).getBytes();//((ContactDTO)servercontacts[0].elementAt(index)).getContactid().getBytes();
				data[1] = ((ContactDTO)contactdtos_from_server[0].elementAt(loopindex)).getGlobalid().getBytes();
				data[2] = (Constants.CONTACTS_SERVERADD + "").getBytes();
				dbmanager_contactsynchmaps.createDBRecord(data);

				ContactMappingDTO cmdto = new ContactMappingDTO(new String(data[0]), new String(data[1]), "0");
				cmdto.setChecksum("" + cdto.getCheckSumString());
				
				localmappings[Constants.CONTACTS_ADD].addElement(cmdto);
				
				data = null;
			    
			}
			
			clist.close();
			dbmanager_contactsynchmaps.close();
			
			contactdtos_from_server = null;
			ApplicationAssistant.gc();

			return true;
		}
		}
		catch(SecurityException e)
		{
			contactdtos_from_server = null;
			ApplicationAssistant.gc();
			
			ApplicationAPI.dialogerror = Constants.MESSAGE_SYNCHRONISATON_SECURITYERROR[ApplicationAssistant.language];
			if (Constants.debug) System.out.println("VaultAPI.syncContactsToServer() "+ e.toString());
			
		}
		catch(Exception e)
		{
			contactdtos_from_server = null;
			ApplicationAssistant.gc();
			
			ApplicationAPI.dialogerror = Constants.MESSAGE_SYNCHRONISATON_RECEIVERESPONSEFAIL[ApplicationAssistant.language];
			if(Constants.debug) System.out.println("VaultAPI.syncContactsFromServer55 " + e.toString());			
		}
		return false;
	}
	
	private static void contactsSyncComplete() throws Exception
	{
		if(Constants.debug) System.out.println("VaultAPI.contactsSyncComplete");
		
		int contactscount[] = new int[status_uploaded.length];
		contactscount[0] = 0;
		for(int i=1; i<contactscount.length; i++)
			contactscount[i] = -1;
				
		// delete contacts and contactsynch db. but repopulate contacts db
		dbmanager_contacts.open();
				
		if(Constants.debug) System.out.println("VaultAPI.contactsSyncComplete1");
					
		for(int i = 0; i < localchanges[Constants.CONTACTS_ADD].size(); ++i)
		{
			if(Constants.debug) System.out.println("INSIDE");
			
			ContactLocalDTO cldto = (ContactLocalDTO)localchanges[Constants.CONTACTS_ADD].elementAt(i);
			
			if(Constants.partialdebug) System.out.println("\nCHECKSUM = " + cldto.getChecksum());
			
			byte[][] data = new byte[2][];
	   		data[0] = cldto.getContactid().getBytes();
			data[1] = cldto.getChecksum().getBytes();
			
			dbmanager_contacts.createDBRecord(data);
			
			data = null;
		}

		if(Constants.debug) System.out.println("VaultAPI.contactsSyncComplete2");
		
		for(int i = 0; i < localchanges[Constants.CONTACTS_UPDATE].size(); ++i)
		{
			if(Constants.debug) System.out.println("INSIDE");
			ContactLocalDTO cldto = (ContactLocalDTO)localchanges[Constants.CONTACTS_UPDATE].elementAt(i);
			
			if(Constants.partialdebug) System.out.println("\nCHECKSUM = " + cldto.getChecksum());
			
			dbmanager_contacts.updateField(cldto.getContactid(), "checksum", cldto.getChecksum().getBytes());
		}

		for(int i = 0; i < localchanges[Constants.CONTACTS_DELETE].size(); ++i)
		{
			if(Constants.debug) System.out.println("INSIDE");
			ContactLocalDTO cldto = (ContactLocalDTO)localchanges[Constants.CONTACTS_DELETE].elementAt(i);
			
			if(Constants.partialdebug) System.out.println("\nCHECKSUM = " + cldto.getChecksum());
			
			dbmanager_contacts.delete(cldto.getContactid());
		}

		if(Constants.debug) System.out.println("VaultAPI.contactsSyncComplete3");
		
		for(int i = 0; i < localmappings[Constants.CONTACTS_ADD].size(); ++i)
		{
			if(Constants.debug) System.out.println("INSIDE");
			ContactMappingDTO cmdto = (ContactMappingDTO)localmappings[Constants.CONTACTS_ADD].elementAt(i);
			
			if(Constants.partialdebug) System.out.println("\nCHECKSUM = " + cmdto.getChecksum());
			
			byte[][] data = new byte[2][];
	   		data[0] = cmdto.getContactid().getBytes();
			data[1] = cmdto.getChecksum().getBytes();
			
			dbmanager_contacts.createDBRecord(data);
			
			data = null;
		}

		if(Constants.debug) System.out.println("VaultAPI.contactsSyncComplete4");

		for(int i = 0; i < localmappings[Constants.CONTACTS_UPDATE].size(); ++i)
		{
			if(Constants.debug) System.out.println("INSIDE");
			ContactMappingDTO cldto = (ContactMappingDTO)localmappings[Constants.CONTACTS_UPDATE].elementAt(i);
			
			if(Constants.partialdebug) System.out.println("\nCHECKSUM = " + cldto.getChecksum());
			
			dbmanager_contacts.updateField(cldto.getContactid(), "checksum", cldto.getChecksum().getBytes());
		}
		
		for(int i = 0; i < localmappings[Constants.CONTACTS_DELETE].size(); ++i)
		{
			if(Constants.debug) System.out.println("INSIDE");
			ContactMappingDTO cmdto = (ContactMappingDTO)localmappings[Constants.CONTACTS_DELETE].elementAt(i);
			
			if(Constants.partialdebug) System.out.println("\nCHECKSUM = " + cmdto.getChecksum());
			
			dbmanager_contacts.delete(cmdto.getContactid());
		}
		
		if(Constants.debug) System.out.println("VaultAPI.contactsSyncComplete5");
		
		//update contacts count		
		contactscount[0] = dbmanager_contacts.countAll();
		if(Constants.debug) System.out.println("VaultAPI.contactsSyncComplete5a + count = " + contactscount[0]);
		//if(contactscount)
		
		dbmanager_contacts.close();
		
		if(Constants.debug) System.out.println("VaultAPI.contactsSyncComplete6");
		
		updateUploadedCount(contactscount);
		updateLastSynchTime(System.currentTimeMillis());
		
		if(Constants.debug) System.out.println("VaultAPI.contactsSyncComplete7");

		dbmanager_contactsynchmaps.destroy();
	}
	
	public static void registerClient()
	{
		try{
			byte[] xml = XMLHome.getRegistrationXML();
			
			ConnectionHome connHome = new ConnectionHome();
			byte [] readdata = connHome.send_and_receive(ApplicationAssistant.registration_url, xml, /*false, */Constants.POST_AND_RECEIVE);
			connHome = null;
			xml = null;
			ApplicationAssistant.gc();
			
			if (ApplicationAPI.getConnstatus() == ConnectionHome.FAILED)
			{
				if (Constants.debug) System.out.println("VaultAPI.registerClient() connection screwed");
				ApplicationAPI.dialogerror = Constants.MESSAGE_GPRSERROR[ApplicationAssistant.language];
				ApplicationAssistant.registered = Constants.REGISTRATION_FAILED;
				readdata = null;
				ApplicationAssistant.gc();
				return;
			}
			
			String response = new String(readdata, "UTF-8");
			readdata = null;
			ApplicationAssistant.gc();
			if(response.indexOf("suc_reg") >= 0)
			{
				ApplicationAPI.updateToRegistered();
//				ApplicationAPI.dialogconfirmation = Constants.MESSAGE_REGISTRATION_SUCCESS;
			}
			else if(response.indexOf("fail_reg_existing_msisdn") >= 0)
			{
				ApplicationAPI.dialogerror = Constants.MESSAGE_REGISTRATION_FAIL_EXISTING_MSISDN[ApplicationAssistant.language];
				ApplicationAssistant.registered = Constants.REGISTRATION_FAILED;
			}
			else
			{
				ApplicationAPI.dialogerror = Constants.MESSAGE_REGISTRATION_FAIL[ApplicationAssistant.language];
				ApplicationAssistant.registered = Constants.REGISTRATION_FAILED;
			}
			response = null;
			ApplicationAssistant.gc();
		}
		catch(Exception e)
		{
			if(Constants.debug) System.out.println("VaultAPI.registerClient() "+ e.toString());
			ApplicationAPI.dialogerror = Constants.MESSAGE_REGISTRATION_FAIL[ApplicationAssistant.language];
			ApplicationAssistant.registered = Constants.REGISTRATION_FAILED;
		}
	}	

	public static boolean changePassword(String currentpassword, String newpassword)
	{
		boolean ret = false;
		//UPDATE SERVER
		try
		{
			byte[] xml = XMLHome.getChangePasswordXML(currentpassword, newpassword);
			
			ConnectionHome connHome = new ConnectionHome();
			byte [] readdata = connHome.send_and_receive(ApplicationAssistant.registration_url, xml, /*false, */Constants.POST_AND_RECEIVE);
			connHome = null;
			xml = null;
			ApplicationAssistant.gc();
			
			if (ApplicationAPI.getConnstatus() == ConnectionHome.FAILED)
			{
				if (Constants.debug) System.out.println("VaultAPI.changePassword() connection screwed");
				ApplicationAPI.dialogerror = Constants.MESSAGE_GPRSERROR[ApplicationAssistant.language];
				readdata = null;
				ApplicationAssistant.gc();
				return ret;
			}
			
			String response = new String(readdata, "UTF-8");
			readdata = null;
			ApplicationAssistant.gc();
			if(response.indexOf("suc_reg") >= 0)
			{
				//UPDATE CLIENT
				try
				{
					dbmanager_config.open();
					dbmanager_config.updateField("password", "value", newpassword.getBytes());
					ApplicationAssistant.password = newpassword;
					ApplicationAPI.dialogconfirmation = Constants.MESSAGE_PASSWORD_CHANGED[ApplicationAssistant.language];
					ret = true;
				}
				catch(Exception e)
				{
					if(Constants.debug) System.out.println("VaultAPI.changePassword() " + e.toString()); 			
				}
				finally
				{
					try
					{
						dbmanager_config.close();
					}
					catch(Exception ex)
					{
						if(Constants.debug) System.out.println("VaultAPI.changePassword() " + ex.toString());
					}
					ApplicationAssistant.gc();
				}
			}
			else if(response.indexOf("fail_reg") >= 0)
			{
				ApplicationAPI.dialogerror = Constants.MESSAGE_PASSWORD_INCORRECT[ApplicationAssistant.language];
			}
			else
			{
				ApplicationAPI.dialogerror = Constants.MESSAGE_SYNCHRONISATON_SENDREQUESTFAIL[ApplicationAssistant.language];
			}
			response = null;
			ApplicationAssistant.gc();
		}
		catch(Exception e)
		{
			if(Constants.debug) System.out.println("VaultAPI.changePassword() "+ e.toString());
			ApplicationAPI.dialogerror = Constants.MESSAGE_SYNCHRONISATON_SENDREQUESTFAIL[ApplicationAssistant.language];
		}
		return ret;
	}
	
	public static byte[] sendMappingAcksToServer()
	{
		if(Constants.debug) System.out.println("VaultAPI.sendMappingAcksToServer");
	
		status_uploading[Constants.LISTINDEX_CONTACTS] = 0;
		
		String xml = "";

		try{
			if(Constants.debug) System.out.println("VaultAPI.sendMappingAcksToServer1");			
			
			try
			{
				getMappings();
			}
			catch(Exception e) 
			{
			    throw new SecurityException("Unable to open contact list");
			}
			if(Constants.debug) System.out.println("VaultAPI.sendMappingAcksToServer2");
			
			String msgtype = "";
			if(ApplicationAssistant.initialupload == Constants.INITIALUPLOAD_TRUE)
				msgtype = XMLHome.SYNC_MSGT_INIT_EU;
			else
				msgtype = XMLHome.SYNC_MSGT_SYNC;
			
			xml += "<root>";
			xml += XMLHome.getHeaderXML(msgtype, "1", "1", ApplicationAssistant.lastsynchtime + "", "");
			xml += "<b>";
				
			for(int i=0; i<contactdtos_to_server.length;++i)
				xml += XMLHome.getContactsSyncXML(contactdtos_to_server[i]);
			if(contactdtos_to_server.length == 0)
				xml += "<add></add><update></update><delete></delete>";
			
			xml += "</b></root>";
			
			contactdtos_to_server = null;
			ApplicationAssistant.gc();
			
			if(Constants.debug) System.out.println("VaultAPI.sendMappingAcksToServer3");
			
			ConnectionHome connHome = new ConnectionHome();
			byte [] readdata = connHome.send_and_receive(ApplicationAssistant.contacts_url, xml.getBytes("UTF-8"), /*false, */Constants.POST_AND_RECEIVE);
			connHome = null;
			xml = null;
			
			ApplicationAssistant.gc();

			if(Constants.debug) System.out.println("VaultAPI.sendMappingAcksToServer4");
			
			if (ApplicationAPI.getConnstatus() == ConnectionHome.FAILED)
			{				
				if (Constants.debug) System.out.println("VaultAPI.sendMappingAcksToServer() connection screwed");
				readdata = null;
				ApplicationAssistant.gc();
				return null;
			}
			
			String response = new String(readdata, "UTF-8");
			readdata = null;
			ApplicationAssistant.gc();
			
			int start = response.indexOf("<ack>");
			
			if(Constants.debug) System.out.println("VaultAPI.sendMappingAcksToServer5");
			
			if(start >= 0)
			{
				if(Constants.debug) System.out.println("VaultAPI.sendMappingAcksToServer6");
				
				String ack_message="";
				int end = response.indexOf("</ack>");
				
				//ack_message = response.substring(start + "<ack>".length(), end + 1);
				ack_message = response.substring(start + "<ack>".length(), end);
				response = null;
				ApplicationAssistant.gc();				
				if(processContactsAckResponse(ack_message))
				{
					ack_message = null;
					ApplicationAssistant.gc();
					
					byte [] ret = contactsSendCompleteN();
					//if(ret!=null && ApplicationAssistant.initialupload == Constants.INITIALUPLOAD_TRUE)
					//	updateToInitialUploadComplete();
						
					return ret; 
				}
				else 
				{
					ack_message = null;
					ApplicationAssistant.gc();
					return null;
				}
			}
			else
			{
				response = null;
				ApplicationAssistant.gc();
				return null;
			}			
		}
		catch (SecurityException e)
		{
			contactdtos_to_server = null;
			xml = null;
			ApplicationAssistant.gc();
			
			ApplicationAPI.dialogerror = Constants.MESSAGE_SYNCHRONISATON_SECURITYERROR[ApplicationAssistant.language];
			if (Constants.debug) System.out.println("VaultAPI.sendMappingAcksToServer() "+ e.toString());
			return null;
		}
		catch (Exception e)
		{
			contactdtos_to_server = null;
			xml = null;
			ApplicationAssistant.gc();
			
			ApplicationAPI.dialogerror = Constants.MESSAGE_SYNCHRONISATON_SENDREQUESTFAIL[ApplicationAssistant.language];
			
			if (Constants.debug) System.out.println("VaultAPI.sendMappingAcksToServer() "+ e.toString());
			return null;
		}
	}

	private static void getMappings() throws SecurityException, Exception 
	{
		if(Constants.debug) System.out.println("VaultAPI.getMappings");
		
		Hashtable contactmappingdtos;

		dbmanager_contactsynchmaps.open();

		Hashtable result [] = dbmanager_contactsynchmaps.findAll();
				
		dbmanager_contactsynchmaps.close();
		
		contactmappingdtos = hashtableToContactMappingDTOHashtable(result);
		
		result = null;
		ApplicationAssistant.gc();
				
		if(Constants.debug) System.out.println("VaultAPI.getMappings1");
		
		Vector contactdtos = new Vector();

		if(Constants.debug) System.out.println("VaultAPI.getMappings2");
			
		// check for MAPSA, MAPSD

		if(Constants.debug) System.out.println("VaultAPI.getMappings3");		
		
		Enumeration enumer = contactmappingdtos.keys();
		
		while(enumer.hasMoreElements())
		{
			String cid = (String)enumer.nextElement();
			ContactMappingDTO cmdto = (ContactMappingDTO)contactmappingdtos.get(cid);
		   
			String tmpluid = cmdto.getContactid();
			String tmpguid = cmdto.getGlobalid();
			int mapop = cmdto.getType();
		   
			ContactDTO cdto = new ContactDTO();
			cdto.setContactid(tmpluid);
			cdto.setGlobalid(tmpguid);
			cdto.setChangetype(mapop);
			contactdtos.addElement(cdto);
						
			cid = null;
			cmdto = null;
			cdto = null;
		}
		
		contactmappingdtos = null;

		ApplicationAssistant.gc();
		
		if(Constants.debug) System.out.println("VaultAPI.getMappings4");		
		
		if(Constants.debug) System.out.println("VaultAPI.getMappings5");		
		
		contactdtos_to_server = null;
		
		contactdtos_to_server = new ContactDTO[contactdtos.size()];
		for(int i=0; i<contactdtos.size(); i++)
		{
			contactdtos_to_server[i] = (ContactDTO) contactdtos.elementAt(i);
		}
		contactdtos = null;
		ApplicationAssistant.gc();
		
		if(Constants.debug) System.out.println("VaultAPI.getMappings6");		

	}
	
	public static Hashtable hashtableToContactLocalDTOHashtable(Hashtable[] result)
	{
		Hashtable v = new Hashtable();
		
		for(int i = 0; i< result.length; ++i){
			ContactLocalDTO cldto = new ContactLocalDTO(new String((byte[])result[i].get("luid")), new String((byte[])result[i].get("checksum")));
			v.put(new String((byte[])result[i].get("luid")), cldto);
		}
		
		return v;
	}

	public static Hashtable hashtableToContactMappingDTOHashtable(Hashtable[] result)
	{
		Hashtable v = new Hashtable();
		
		for(int i = 0; i< result.length; ++i){
			ContactMappingDTO cmdto = new ContactMappingDTO(new String((byte[])result[i].get("luid")), new String((byte[])result[i].get("guid")), new String((byte[]) result[i].get("mapsoperation")));
			v.put(new String((byte[])result[i].get("luid")), cmdto);			
		}
		
		return v;
	}
		
	public static void loadMediaList(int mediatype)
	{
		//read the filesystem and load into ApplicationAPI.mediadtos
		//as you can see mediadtos do not have the byte [] holder, cos this is not read at this point
		//just read the media meta data
		try
		{
			if(Constants.partialdebug) System.out.println("VaultAPI.loadMediaList()");
			ApplicationAPI.mediadtos = new Vector();
			Vector queuedmediarecords = new Vector();
			dbmanager_media.open();
			Hashtable[] result = dbmanager_media.findAll();
			dbmanager_media.close();
			for(int i=0; i<result.length; i++)
			{
				String filepath = new String((byte []) result[i].get("filepath"));
//				int mediat = Integer.parseInt(new String((byte []) result[i].get("mediatype")));
//				byte[] status = (byte []) result[i].get("status");
//				String album = new String((byte []) result[i].get("album"));
//				String desc = new String((byte []) result[i].get("desc"));
				
//				MediaDTO mdto = new MediaDTO(filepath, mediat, 0, "");
//				mdto.setStatus(status);
				
				if(Constants.partialdebug) System.out.println("VaultAPI.loadMediaList() MEDIA DB contains" + filepath);
				
				//mediarecords.addElement(mdto);				
				queuedmediarecords.addElement(filepath);
			}			

			result = null;
			ApplicationAssistant.gc();
			Vector uploadedmediarecords = new Vector();
			dbmanager_uploadedmedia.open();
			result = dbmanager_uploadedmedia.findAll();
			dbmanager_uploadedmedia.close();
			for(int i=0; i<result.length; i++)
			{
				String filepath = new String((byte []) result[i].get("filepath"));
				//int mediat = Integer.parseInt(new String((byte []) result[i].get("mediatype")));
				//MediaDTO mdto = new MediaDTO(filepath, mediat, 0, "");
				if(Constants.partialdebug) System.out.println("VaultAPI.loadMediaList() UPLOADED DB contains" + filepath);
				//uploadedmediarecords.addElement(mdto);				
				uploadedmediarecords.addElement(filepath);
			}			
			
			result = null;
			ApplicationAssistant.gc();
			
			Vector uniquepaths = getUniqueNonnullPaths(mediatype); 
			Vector uniquefilenames = new Vector();
			if(mediatype == Constants.CONTENTTYPE_PHOTO)
			{				  
				  for(int i=0; i<uniquepaths.size(); i++)
				  {
					  String path = (String) uniquepaths.elementAt(i);
					  if(Constants.partialdebug) System.out.println("VaultAPI.loadMediaList() Photos " + path);
					  try
					  {
						  uniquefilenames = recursivelyloadMediaListForFolder(path, mediatype, queuedmediarecords, uploadedmediarecords, uniquefilenames);
					  }
					  catch(Exception e)
					  {
						  if(Constants.debug) System.out.println("VaultAPI.loadMediaList() LOADING FAILED BUT continue " + path + " " + e.toString());
					  }
				  }
			}
			else if(mediatype == Constants.CONTENTTYPE_VIDEO)
			{				
				 for(int i=0; i<uniquepaths.size(); i++)
				 {
					  String path = (String) uniquepaths.elementAt(i);
					  if(Constants.partialdebug) System.out.println("VaultAPI.loadMediaList() Videos " + path);
					  try
					  {
						  uniquefilenames = recursivelyloadMediaListForFolder(path, mediatype, queuedmediarecords, uploadedmediarecords, uniquefilenames);
					  }
					  catch(Exception e)
					  {
						  if(Constants.debug) System.out.println("VaultAPI.loadMediaList() LOADING FAILED BUT continue " + path + " " + e.toString());
					  }
				 }
			}
			else if(mediatype == Constants.CONTENTTYPE_RINGTONE)
			{				
				 for(int i=0; i<uniquepaths.size(); i++)
				 {
					  String path = (String) uniquepaths.elementAt(i);
					  if(Constants.partialdebug) System.out.println("VaultAPI.loadMediaList() Ringtones " + path);
					  try
					  {
						  uniquefilenames = recursivelyloadMediaListForFolder(path, mediatype, queuedmediarecords, uploadedmediarecords, uniquefilenames);
					  }
					  catch(Exception e)
					  {
						  if(Constants.debug) System.out.println("VaultAPI.loadMediaList() LOADING FAILED BUT continue " + path + " " + e.toString());
					  }
				 }
			}
			else if(mediatype == Constants.CONTENTTYPE_WALLPAPER)
			{				
				 for(int i=0; i<uniquepaths.size(); i++)
				 {
					  String path = (String) uniquepaths.elementAt(i);
					  if(Constants.partialdebug) System.out.println("VaultAPI.loadMediaList() Wallpapers " + path);
					  try
					  {
						  uniquefilenames = recursivelyloadMediaListForFolder(path, mediatype, queuedmediarecords, uploadedmediarecords, uniquefilenames);
					  }
					  catch(Exception e)
					  {
						  if(Constants.debug) System.out.println("VaultAPI.loadMediaList() LOADING FAILED BUT continue " + path + " " + e.toString());
					  }
				 }
			}
			uniquefilenames = null;
			ApplicationAssistant.gc();
			 
			try
			{ // trap sort internally and return unsorted DTO's if sorting fails
				if(Constants.partialdebug) System.out.println("VaultAPI.loadMediaList() sort");
				if(ApplicationAPI.mediadtos.size() > 0)
				{
					if(Constants.partialdebug) System.out.println("VaultAPI.loadMediaList() sort");
					MediaDTO [] sortmediadto = new MediaDTO[ApplicationAPI.mediadtos.size()];
					for(int i = 0; i < ApplicationAPI.mediadtos.size(); ++i)
					{//SORT according to timestamp
						if(Constants.partialdebug) System.out.println("VaultAPI.loadMediaList() sort1");
						
						sortmediadto[i] = (MediaDTO)ApplicationAPI.mediadtos.elementAt(i);
						sortmediadto[i].parentlabel = "" + sortmediadto[i].getTimestamp(); 
					}
					
					ApplicationAssistant.sort(sortmediadto);
					ApplicationAPI.mediadtos = null;
					ApplicationAssistant.gc();
					ApplicationAPI.mediadtos = new Vector();
					for(int i = 0; i < sortmediadto.length; ++i)
					{
						ApplicationAPI.mediadtos.addElement(sortmediadto[i]);
					}
				}
			}
			catch(Exception e)
			{
				if(Constants.debug) System.out.println("VaultAPI.loadMediaList() SORTING FAILED BUT continue" + e.toString());
			}
		}
		catch(Exception e)
		{
			//unexpected error?
			if(Constants.debug) System.out.println("VaultAPI.loadMediaList() " + e.toString());
		}
	}
	
	private static Vector getUniqueNonnullPaths(int mediatype)
	{
		Vector uniquenonnullpaths = new Vector();
		try
		{
			if(mediatype == Constants.CONTENTTYPE_PHOTO)
			{
				for(int i=0; i<Constants.PHOTO_GALLERY_PATH.length; i++)
				{
					if(Constants.PHOTO_GALLERY_PATH[i]==null)
						continue;
					if(!uniquenonnullpaths.contains(Constants.PHOTO_GALLERY_PATH[i]))
						uniquenonnullpaths.addElement(Constants.PHOTO_GALLERY_PATH[i]);
				}
			}
			else if(mediatype == Constants.CONTENTTYPE_VIDEO)
			{
				for(int i=0; i<Constants.VIDEO_GALLERY_PATH.length; i++)
				{
					if(Constants.VIDEO_GALLERY_PATH[i]==null)
						continue;
					if(!uniquenonnullpaths.contains(Constants.VIDEO_GALLERY_PATH[i]))
						uniquenonnullpaths.addElement(Constants.VIDEO_GALLERY_PATH[i]);
				}
			}
			else if(mediatype == Constants.CONTENTTYPE_RINGTONE)
			{
				for(int i=0; i<Constants.RINGTONE_GALLERY_PATH.length; i++)
				{
					if(Constants.RINGTONE_GALLERY_PATH[i]==null)
						continue;
					if(!uniquenonnullpaths.contains(Constants.RINGTONE_GALLERY_PATH[i]))
						uniquenonnullpaths.addElement(Constants.RINGTONE_GALLERY_PATH[i]);
				}
			}
			else if(mediatype == Constants.CONTENTTYPE_WALLPAPER)
			{
				for(int i=0; i<Constants.WALLPAPER_GALLERY_PATH.length; i++)
				{
					if(Constants.WALLPAPER_GALLERY_PATH[i]==null)
						continue;
					if(!uniquenonnullpaths.contains(Constants.WALLPAPER_GALLERY_PATH[i]))
						uniquenonnullpaths.addElement(Constants.WALLPAPER_GALLERY_PATH[i]);
				}
			}
		}
		catch(Exception e)
		{
			//unexpected error?
			if(Constants.debug) System.out.println("VaultAPI.getUniqueNonnullPaths() " + e.toString());
		}
		return uniquenonnullpaths;
	}
	
	private static Vector recursivelyloadMediaListForFolder(String directory, int mediatype, Vector queuedmediarecords, Vector uploadedmediarecords, Vector uniquefilenames)
	{
		try
		{  
		  Vector subfolders = new Vector();
		  if(Constants.debug) System.out.println("VaultAPI.recursivelyloadMediaListForFolder() ###########" + directory);
		  FileConnection fc = (FileConnection) Connector.open(directory, Connector.READ);
          Enumeration listOfFiles = fc.list("*", false);
		  while (listOfFiles.hasMoreElements())
          {
          	String currentFile = (String) listOfFiles.nextElement();
   	        
          	if(currentFile.endsWith(Constants.FILE_SEPARATOR)) //add all subfolders, and iterate through them, once this folder connection is closed
          	{
          		subfolders.addElement(directory + currentFile);
          		continue;
          	}
          	
          	if((mediatype == Constants.CONTENTTYPE_PHOTO && 
          			   (currentFile.toLowerCase().endsWith(".jpg") || 
          			    currentFile.toLowerCase().endsWith(".jpeg"))) ||
          	   (mediatype == Constants.CONTENTTYPE_RINGTONE && 
          			   (currentFile.toLowerCase().endsWith(".amr") || 
          			    currentFile.toLowerCase().endsWith(".mid") || 
          			    currentFile.toLowerCase().endsWith(".midi") || 
          			    currentFile.toLowerCase().endsWith(".mp3") || 
          			    currentFile.toLowerCase().endsWith(".sp.mid") || 
          			    currentFile.toLowerCase().endsWith(".smaf") || 
          			    currentFile.toLowerCase().endsWith(".mmf") || 
          			    currentFile.toLowerCase().endsWith(".wav") || 
          			    currentFile.toLowerCase().endsWith(".aac") || 
          			    currentFile.toLowerCase().endsWith(".imy"))) ||
          	   (mediatype == Constants.CONTENTTYPE_WALLPAPER && 
          			   (currentFile.toLowerCase().endsWith(".jpg") || 
          			    currentFile.toLowerCase().endsWith(".jpeg") || 
          			    currentFile.toLowerCase().endsWith(".bmp") || 
          			    currentFile.toLowerCase().endsWith(".png") || 
          			    currentFile.toLowerCase().endsWith(".gif"))) ||		
          	   (mediatype == Constants.CONTENTTYPE_VIDEO && 
          			   (currentFile.toLowerCase().endsWith(".3gp") || 
          				currentFile.toLowerCase().endsWith(".mp4"))))
          	{
          		if(Constants.partialdebug) System.out.println("VaultAPI.loadMediaList() " + mediatype + " file = " + currentFile);
          		if(uniquefilenames.contains(currentFile))
          			continue;
          		uniquefilenames.addElement(currentFile);
          		FileConnection imagefc = (FileConnection) Connector.open(directory + currentFile, Connector.READ);
          		MediaDTO mdto = new MediaDTO(directory + currentFile, mediatype, imagefc.lastModified(), currentFile);	           
          			              		
          		if(queuedmediarecords.contains(currentFile)) // media is queued
          		{
          			if(Constants.partialdebug) System.out.println("VaultAPI.loadMediaList() QUEUED");
          			mdto.setQueuestatus(Constants.MEDIAQUEUESTATUS_QUEUED);
          		}
          		else if(uploadedmediarecords.contains(currentFile))
          		{
          			if(Constants.partialdebug) System.out.println("VaultAPI.loadMediaList() UPLOADED");
          			mdto.setQueuestatus(Constants.MEDIAQUEUESTATUS_UPLOADED);
          		}
          		else
          		{
          			if(Constants.partialdebug) System.out.println("VaultAPI.loadMediaList() NEW");
          			mdto.setQueuestatus(Constants.MEDIAQUEUESTATUS_NEW);
          		}
          		
        		long filesize = imagefc.fileSize();	      
          		
          		int numpackets = 0;
          		if(filesize % Constants.PUBLISH_PACKETSIZE == 0)
          		{
          			numpackets = (int)(filesize / Constants.PUBLISH_PACKETSIZE);	              			
          		}
          		else
          		{
          			numpackets = (int)(filesize / Constants.PUBLISH_PACKETSIZE);
          			numpackets += 1;
          		}
          		byte[] status = new byte[numpackets];
          		for(int j = 0; j < numpackets; ++j)
          		{
          			status[j] = 0;
          		}
          		
          		mdto.setStatus(status);
          		
          		ApplicationAPI.mediadtos.addElement(mdto);
          		
          		imagefc.close();
          	}
          }
          
          fc.close();
          
          for(int i=0; i<subfolders.size(); i++)
          {
        	  uniquefilenames = recursivelyloadMediaListForFolder((String) subfolders.elementAt(i), mediatype, queuedmediarecords, uploadedmediarecords, uniquefilenames);
          }
		}
		catch(Exception e)
		{
			if(Constants.debug) System.out.println("VaultAPI.loadMediaListForFolder() "+ e.toString());
		}
		return uniquefilenames;
	}
	
	public static void synchroniseMediaPackets()
	{
		//status servlet call
		try{
			byte[] xml = XMLHome.getStatusSubscriptionXML();
			
			ConnectionHome connHome = new ConnectionHome();
			byte [] readdata = connHome.send_and_receive(ApplicationAssistant.sub_status_check, xml, /*false,*/ Constants.POST_AND_RECEIVE);
			connHome = null;
			xml = null;
			ApplicationAssistant.gc();
			
			if (ApplicationAPI.getConnstatus() == ConnectionHome.FAILED)
			{
				if (Constants.debug) System.out.println("VaultAPI.synchroniseMediaPackets() connection screwed");
				ApplicationAPI.dialogerror = Constants.MESSAGE_GPRSERROR[ApplicationAssistant.language];
				readdata = null;
				ApplicationAssistant.gc();
				return;
			}
			
			Hashtable hashtable = XMLHome.parseStatusSubscriptionXml(readdata);
			readdata = null;
			ApplicationAssistant.gc();
			
			String statuscheck = (String)hashtable.get("user_status");
			if(statuscheck.equals("valid") || statuscheck.equals("pendingconfirmation"))
			{
				updateStatus(hashtable);
				hashtable = null;
				ApplicationAssistant.gc();
			}
			else
			{
				ApplicationAPI.dialogerror = Constants.MESSAGE_STATUSSYNCH_ACCOUNTSUSPENDED[ApplicationAssistant.language];
				hashtable = null;
				ApplicationAssistant.gc();
				return;
			}
		}
		catch(Exception e)
		{
			if(Constants.debug) System.out.println("VaultAPI.synchroniseMediaPackets() "+ e.toString());
			ApplicationAPI.dialogerror = Constants.MESSAGE_PUBLISH_UPLOADFAILED[ApplicationAssistant.language];
			
			return;
		}
			
		ApplicationAPI.status_uploading[Constants.LISTINDEX_PHOTOS] = 0;
		ApplicationAPI.status_uploading[Constants.LISTINDEX_VIDEOS] = 0;
		ApplicationAPI.status_uploading[Constants.LISTINDEX_RINGTONES] = 0;
		ApplicationAPI.status_uploading[Constants.LISTINDEX_WALLPAPERS] = 0;

//		ApplicationAPI.dialogconfirmation = "1; ";
		try
		{
			//Vector mediarecords = new Vector();
			Hashtable mediarecords = new Hashtable();
			dbmanager_media.open();
			Hashtable[] result = dbmanager_media.findAll();
			dbmanager_media.close();
//			ApplicationAPI.dialogconfirmation += "2; ";
			for(int i=0; i<result.length; i++)
			{
				String filepath = new String((byte []) result[i].get("filepath"));
				int mediatype = Integer.parseInt(new String((byte []) result[i].get("mediatype")));
				byte[] status = (byte []) result[i].get("status");
				String album = new String((byte []) result[i].get("album"));
				String desc = new String((byte []) result[i].get("desc"));
				
				MediaDTO mdto = new MediaDTO(filepath, mediatype, 0, "");
				mdto.setStatus(status);
				
				//mediarecords.addElement(mdto);
				mediarecords.put(filepath, mdto);
				
				if(mediatype == Constants.CONTENTTYPE_PHOTO)
					++ApplicationAPI.status_uploading[Constants.LISTINDEX_PHOTOS];
				else if(mediatype == Constants.CONTENTTYPE_VIDEO)
					++ApplicationAPI.status_uploading[Constants.LISTINDEX_VIDEOS];
				else if(mediatype == Constants.CONTENTTYPE_RINGTONE)
					++ApplicationAPI.status_uploading[Constants.LISTINDEX_RINGTONES];
				else if(mediatype == Constants.CONTENTTYPE_WALLPAPER)
					++ApplicationAPI.status_uploading[Constants.LISTINDEX_WALLPAPERS];
				
			}
//			ApplicationAPI.dialogconfirmation += "3; ";
			result = null;
			ApplicationAssistant.gc();
			
			for(int i=0; ApplicationAPI.mediadtos!=null && i<ApplicationAPI.mediadtos.size(); i++)
			{
				MediaDTO mdto = (MediaDTO) ApplicationAPI.mediadtos.elementAt(i);
				if(mdto.isSelected())
				{
//					ApplicationAPI.dialogconfirmation += "4; ";
					if(!mediarecords.contains(mdto.getFilename())){
//						ApplicationAPI.dialogconfirmation += "5; ";
						//mediarecords.addElement(mdto);
						mediarecords.put(mdto.getFilename(), mdto);
						
						byte[][] data = new byte[5][];
						data[0] = mdto.getFilename().getBytes(); //host is the pk
						data[1] = (mdto.getMediatype() + "").getBytes();
						data[2] = mdto.getStatus();
						data[3] = "Backup".getBytes();
						data[4] = "".getBytes();
						
						dbmanager_media.open();
						dbmanager_media.createDBRecord(data);
						dbmanager_media.close();
						
						if(mdto.getMediatype() == Constants.CONTENTTYPE_PHOTO)
							++ApplicationAPI.status_uploading[Constants.LISTINDEX_PHOTOS];
						else if(mdto.getMediatype() == Constants.CONTENTTYPE_VIDEO)
							++ApplicationAPI.status_uploading[Constants.LISTINDEX_VIDEOS];
						else if(mdto.getMediatype() == Constants.CONTENTTYPE_RINGTONE)
							++ApplicationAPI.status_uploading[Constants.LISTINDEX_RINGTONES];
						else if(mdto.getMediatype() == Constants.CONTENTTYPE_WALLPAPER)
							++ApplicationAPI.status_uploading[Constants.LISTINDEX_WALLPAPERS];
						
						data = null;
//						ApplicationAPI.dialogconfirmation += "6; ";
					}
				}
			}
			
			ApplicationAPI.mediadtos = null;
			ApplicationAssistant.gc();
			
			Vector uploadedmediarecords = new Vector();
			dbmanager_uploadedmedia.open();
			result = dbmanager_uploadedmedia.findAll();
			dbmanager_uploadedmedia.close();
			
			for(int i=0; i<result.length; i++)
			{
				String filepath = new String((byte []) result[i].get("filepath"));
				//int mediat = Integer.parseInt(new String((byte []) result[i].get("mediatype")));
				
				//MediaDTO mdto = new MediaDTO(filepath, mediat, 0, "");
				
				if(Constants.partialdebug) System.out.println("VaultAPI.synchroniseMediaPackets() UPLOADED DB contains" + filepath);
				
				uploadedmediarecords.addElement(filepath);				
			}			

			result = null;
			ApplicationAssistant.gc();
				
			Enumeration enumer = mediarecords.keys();
			
//			ApplicationAPI.dialogconfirmation += "7; ";
			for(int i=0; i<mediarecords.size(); i++)
			{
//				ApplicationAPI.dialogconfirmation += "8; ";
				String key = (String)enumer.nextElement();
				
				//MediaDTO mdto = (MediaDTO) mediarecords.elementAt(i);
				MediaDTO mdto = (MediaDTO) mediarecords.get(key);
				
				byte[] statusbytes = mdto.getStatus();
				
				for(int j = 0; j < statusbytes.length; j++)
				{
//					ApplicationAPI.dialogconfirmation += "9; ";
					if(statusbytes[j] == 0) //unsent packet
					{
//						ApplicationAPI.dialogconfirmation += "10; ";
						byte[] xmlbyte;
						byte[] mediafilebyte;
						byte[] masterxmlbyte;
						String xml = XMLHome.getPublishXML(mdto.getFilename(), "", "Backup", mdto.getMediatype(), j, statusbytes.length);
//						ApplicationAPI.dialogconfirmation += "a; ";
						String publishxml = xml.length() + "-" + xml;
						
						xmlbyte = publishxml.getBytes();
//						ApplicationAPI.dialogconfirmation += "b; ";
						mediafilebyte = getMediaPacketBytes(mdto.getFilename(), mdto.getMediatype(), j);
//						ApplicationAPI.dialogconfirmation += "c; ";
						masterxmlbyte = new byte[xmlbyte.length + mediafilebyte.length];
//						ApplicationAPI.dialogconfirmation += "i; ";
						System.arraycopy(xmlbyte, 0, masterxmlbyte, 0, xmlbyte.length);
//						ApplicationAPI.dialogconfirmation += "ii; ";
						System.arraycopy(mediafilebyte, 0, masterxmlbyte, xmlbyte.length, mediafilebyte.length);
//						ApplicationAPI.dialogconfirmation += "d; ";
						
						xml = null;
						publishxml = null;
						xmlbyte = null;
						mediafilebyte = null;
						ApplicationAssistant.gc();

//						ApplicationAPI.dialogconfirmation += "11; ";
						ConnectionHome connHome = new ConnectionHome();
						byte [] readdata = connHome.send_and_receive(ApplicationAssistant.media_pkt_upload_url, masterxmlbyte, /*false, */Constants.POST_AND_RECEIVE);
						masterxmlbyte = null;
						connHome = null;
						ApplicationAssistant.gc();

//						ApplicationAPI.dialogconfirmation += "12; ";
						if (ApplicationAPI.getConnstatus() == ConnectionHome.FAILED)
						{
							if (Constants.debug) System.out.println("VaultAPI.synchroniseMediaPackets() connection screwed");
							ApplicationAPI.dialogerror = Constants.MESSAGE_GPRSERROR[ApplicationAssistant.language];
							readdata = null;
							ApplicationAssistant.gc();
							return;
						}

//						ApplicationAPI.dialogconfirmation += "13; ";
						String response = new String(readdata);
						readdata = null;
						ApplicationAssistant.gc();

//						ApplicationAPI.dialogconfirmation += "14; ";
						if(response.indexOf("<ack>packet</ack>") >= 0)
						{
//							ApplicationAPI.dialogconfirmation += "15; ";
							statusbytes[j] = 1;
							//TODO - BUG MEDIA1
							/**
							 * Ravinder says:
							 * Some minions of the demon are bent on screwing me here. This is the deal:
							 * First upload, any number of n media can be backed up. After this, for any 
							 * number of m attempts, single media upload works fine. But in any of these 
							 * attempts, if 2 or more media are backed up in one go, the 1st packet of 2nd
							 * media fails at this point when trying to update its status. To be precise, it
							 * fails at this.rs.getRecord in DBManager.updateField
							 * 
							 * Handling this cunningly but gracefully. So worst case status is not updated,
							 * but if all packets of this media succeed, the entire record is deleted anyways,
							 * so it doesnt matter whether the status was updated or not. Issue arises if user 
							 * exits or gprs conks out in the middle of the operation, in which case the packets
							 * that were already uploaded would be re-uploaded, but server is smart enuff to handle
							 * this.
							 * 
							 * Lets go with this, but bugger do come back and fix it
							 */
							try
							{
								dbmanager_media.open();
								dbmanager_media.updateField(mdto.getFilename(), "status", statusbytes);
							}
							catch(Exception e)
							{}
							finally
							{
								try
								{
									dbmanager_media.close();
								}
								catch(Exception ex)
								{}
							}
							response = null;
							ApplicationAssistant.gc();
						}
						else
						{
//							ApplicationAPI.dialogconfirmation += "16; ";
							response = null;
							ApplicationAssistant.gc();
							throw new Exception("Bad media response from server");
						}
					}
				}
//				ApplicationAPI.dialogconfirmation = "1; ";
				//TODO refer to bug MEDIA 1 above
				try
				{
//					ApplicationAPI.dialogconfirmation += "2; ";
					dbmanager_media.open();
//					ApplicationAPI.dialogconfirmation += "3; ";
					dbmanager_media.delete(mdto.getFilename());
//					ApplicationAPI.dialogconfirmation += "4; ";
				}
				catch(Exception e)
				{}
				finally
				{
					try
					{
//						ApplicationAPI.dialogconfirmation += "5; ";
						dbmanager_media.close();
//						ApplicationAPI.dialogconfirmation += "6; ";
					}
					catch(Exception ex)
					{}
				}
				
//				ApplicationAPI.dialogconfirmation += "7; ";
				if(!uploadedmediarecords.contains(mdto.getFilename()))
				{					
//					ApplicationAPI.dialogconfirmation += "8; ";
					byte[][] data = new byte[2][];
					data[0] = mdto.getFilename().getBytes(); //host is the pk
					data[1] = (mdto.getMediatype() + "").getBytes();
//					ApplicationAPI.dialogconfirmation += "9; ";
					if (Constants.partialdebug) System.out.println("VaultAPI.synchroniseMediaPackets() add to uploaded DB" + mdto.getFilename());

					try
					{
//						ApplicationAPI.dialogconfirmation += "0, ";
						dbmanager_uploadedmedia.open();
//						ApplicationAPI.dialogconfirmation += "1, ";
						dbmanager_uploadedmedia.createDBRecord(data);
//						ApplicationAPI.dialogconfirmation += "2, ";
					}
					catch(Exception e)
					{}
					finally
					{
						try
						{
//							ApplicationAPI.dialogconfirmation += "3, ";
							dbmanager_uploadedmedia.close();
//							ApplicationAPI.dialogconfirmation += "4, ";
						}
						catch(Exception ex)
						{}
					}
					data = null;
					ApplicationAssistant.gc();
				}
				
//				ApplicationAPI.dialogconfirmation += "5, ";
				if(mdto.getMediatype()==Constants.CONTENTTYPE_PHOTO){
//					ApplicationAPI.dialogconfirmation += "6, ";
					--ApplicationAPI.status_uploading[Constants.LISTINDEX_PHOTOS];
					++ApplicationAPI.status_uploaded[Constants.LISTINDEX_PHOTOS];
//					ApplicationAPI.dialogconfirmation += "7, ";
					updateUploadedCount(ApplicationAPI.status_uploaded);
//					ApplicationAPI.dialogconfirmation += "8, ";
				}
				else if(mdto.getMediatype()==Constants.CONTENTTYPE_VIDEO){
					--ApplicationAPI.status_uploading[Constants.LISTINDEX_VIDEOS];
					++ApplicationAPI.status_uploaded[Constants.LISTINDEX_VIDEOS];
					updateUploadedCount(ApplicationAPI.status_uploaded);
				}
				else if(mdto.getMediatype()==Constants.CONTENTTYPE_RINGTONE){
					--ApplicationAPI.status_uploading[Constants.LISTINDEX_RINGTONES];
					++ApplicationAPI.status_uploaded[Constants.LISTINDEX_RINGTONES];
					updateUploadedCount(ApplicationAPI.status_uploaded);
				}
				else if(mdto.getMediatype()==Constants.CONTENTTYPE_WALLPAPER){
					--ApplicationAPI.status_uploading[Constants.LISTINDEX_WALLPAPERS];
					++ApplicationAPI.status_uploaded[Constants.LISTINDEX_WALLPAPERS];
					updateUploadedCount(ApplicationAPI.status_uploaded);
				}		
			}
			
			//TODO refer to BUG MEDIA 1 above
			try
			{
//				ApplicationAPI.dialogconfirmation += "9, ";
				dbmanager_media.destroy();
//				ApplicationAPI.dialogconfirmation += "0; ";
				String MEDIASTORE = "media";
				Vector MEDIASTORE_FIELDS = new Vector();
				String fieldnames [] = new String [] {"filepath", "mediatype", "status"};
				MEDIASTORE_FIELDS.addElement(fieldnames);
				fieldnames = null;
				ApplicationAssistant.gc();
				fieldnames = new String [] {"album", "desc"};
				MEDIASTORE_FIELDS.addElement(fieldnames);
				fieldnames = null;
				ApplicationAssistant.gc();
				dbmanager_media = new DBManager(MEDIASTORE_FIELDS, MEDIASTORE);
				MEDIASTORE_FIELDS = null;
				ApplicationAssistant.gc();
//				ApplicationAPI.dialogconfirmation += "1; ";
			}
			catch(Exception e)
			{}
			
			mediarecords = null;
			uploadedmediarecords = null;
			ApplicationAssistant.gc();
			
//			dbmanager_media.close();
//			dbmanager_uploadedmedia.close();
		}
		catch(Exception e)
		{
			//unexpected error?
			mediadtos = null;
			ApplicationAssistant.gc();
			
			dialogerror = Constants.MESSAGE_PUBLISH_UPLOADFAILED[ApplicationAssistant.language];
			if(Constants.debug) System.out.println("VaultAPI.synchroniseMediaPackets() " + e.toString());
		}
		finally
		{
			try{
				dbmanager_media.close();
			}
			catch(Exception e)
			{
				if(Constants.debug) System.out.println("VaultAPI.synchroniseMediaPackets() " + e.toString());
			}
			try{
				dbmanager_uploadedmedia.close();
			}
			catch(Exception e)
			{
				if(Constants.debug) System.out.println("VaultAPI.synchroniseMediaPackets() " + e.toString());
			}
		}
	}
	
	public static void clearMediaQueue()
	{
		//REMOVING QUEUE FUNCTIONALITY TEMPORARILY FOR MEDIA
		try
		{
			ApplicationAPI.status_uploading[Constants.LISTINDEX_VIDEOS] = 0;
			ApplicationAPI.status_uploading[Constants.LISTINDEX_PHOTOS] = 0;
			
			dbmanager_media.destroy();
			String MEDIASTORE = "media";
			Vector MEDIASTORE_FIELDS = new Vector();
			String fieldnames [] = new String [] {"filepath", "mediatype", "status"};
			MEDIASTORE_FIELDS.addElement(fieldnames);
			fieldnames = null;
			ApplicationAssistant.gc();
			fieldnames = new String [] {"album", "desc"};
			MEDIASTORE_FIELDS.addElement(fieldnames);
			fieldnames = null;
			ApplicationAssistant.gc();
			dbmanager_media = new DBManager(MEDIASTORE_FIELDS, MEDIASTORE);
			MEDIASTORE_FIELDS = null;
			ApplicationAssistant.gc();
		}
		catch(Exception e)
		{
			if(Constants.debug) System.out.println("VaultAPI.clearMediaQueue() " + e.toString());
		}
	}
	
	public static byte [] getPhotoBytes(String filename) throws Exception
	{
		if(Constants.debug) System.out.println("VaultAPI.getPhotoImage() filename = " + filename);
		
		InputConnection fileConn = null;
        DataInputStream dis = null;
        byte [] imgbytes = null;
        try
		{
			fileConn = (InputConnection) Connector.open(filename,Connector.READ); 
	        dis = fileConn.openDataInputStream();
	        imgbytes = new byte [dis.available()];
	        dis.readFully(imgbytes);
	        dis.close();
	        fileConn.close();
	    }
		catch(Exception e)
		{
			if(Constants.debug) System.out.println("VaultAPI.getPhotoImage() " + e.toString());
		}
		return imgbytes;
	}
	
	public static byte[] getMediaPacketBytes(String filename, int mediatype, int packetnum) throws Exception
	{
		if(Constants.debug) System.out.println("VaultAPI.getMediaBytes() filename = " + filename + " " + packetnum);
		
		InputConnection fileConn = null;
        DataInputStream dis = null;
        
        byte[] im = new byte[Constants.PUBLISH_PACKETSIZE];
        byte[] mediadata = null;
//        ApplicationAPI.dialogconfirmation += "A; ";
        if(Constants.partialdebug1) System.out.println("VaultAPI.getMediaBytes() filename = " + filename + " " + packetnum);
        
		try
		{
//			ApplicationAPI.dialogconfirmation += "B; ";
			fileConn = (InputConnection)Connector.open(filename,Connector.READ);			
//			ApplicationAPI.dialogconfirmation += "C; ";
			if(Constants.partialdebug1) System.out.println("VaultAPI.getMediaBytes() 1");
			
			dis = fileConn.openDataInputStream();
//			ApplicationAPI.dialogconfirmation += "D; ";
	        if(Constants.partialdebug1) System.out.println("VaultAPI.getMediaBytes() 2");
	        
	        int readcount = dis.read(im);
//	        ApplicationAPI.dialogconfirmation += "E; ";
	        int count = 0;
	        
	        while(readcount > 0 && count < packetnum)
	        {
		        im = null;
		        im = new byte[Constants.PUBLISH_PACKETSIZE];
		        
		        ApplicationAssistant.gc();
	        	
	        	readcount = dis.read(im);
	        	++count;
			}
//	        ApplicationAPI.dialogconfirmation += "F; ";
	        mediadata = new byte[readcount];
	        System.arraycopy(im, 0, mediadata, 0, readcount);
//	        ApplicationAPI.dialogconfirmation += "G; ";
	        im = null;
	        ApplicationAssistant.gc();
	        
	        dis.close();
//	        ApplicationAPI.dialogconfirmation += "H; ";
	        fileConn.close();
//	        ApplicationAPI.dialogconfirmation += "I; ";
		}
		catch(Exception e)
		{
//			ApplicationAPI.dialogconfirmation += filename + "; ";
			if(Constants.debug) System.out.println("VaultAPI.getMediaBytes() " + e.toString());
		}
		
		ApplicationAssistant.gc();
		
		return mediadata;//baos.toByteArray();
	}
	
	/* Antitheft */
	public static void vacuum()
	{
		if(ApplicationAssistant.vacuummode.equals(Constants.VACUUM_COMMAND))
		{
			vacuumContacts();
			vacuumMedia(Constants.CONTENTTYPE_PHOTO);
			vacuumMedia(Constants.CONTENTTYPE_VIDEO);
			vacuumMedia(Constants.CONTENTTYPE_RINGTONE);
			vacuumMedia(Constants.CONTENTTYPE_WALLPAPER);
		}
		else if(ApplicationAssistant.vacuummode.equals(Constants.VACUUM_CONTACTS_COMMAND))
			vacuumContacts();
		else if(ApplicationAssistant.vacuummode.equals(Constants.VACUUM_PHOTOS_COMMAND))
			vacuumMedia(Constants.CONTENTTYPE_PHOTO);
		else if(ApplicationAssistant.vacuummode.equals(Constants.VACUUM_VIDEOS_COMMAND))
			vacuumMedia(Constants.CONTENTTYPE_VIDEO);
		else if(ApplicationAssistant.vacuummode.equals(Constants.VACUUM_MEDIA_COMMAND))
		{
			vacuumMedia(Constants.CONTENTTYPE_PHOTO);
			vacuumMedia(Constants.CONTENTTYPE_VIDEO);
		}
		else if(ApplicationAssistant.vacuummode.equals(Constants.VACUUM_RINGTONES_COMMAND))
			vacuumMedia(Constants.CONTENTTYPE_RINGTONE);
		else if(ApplicationAssistant.vacuummode.equals(Constants.VACUUM_WALLPAPERS_COMMAND))
			vacuumMedia(Constants.CONTENTTYPE_WALLPAPER);
		else if(ApplicationAssistant.vacuummode.equals(Constants.VACUUM_CONTENT_COMMAND))
		{
			vacuumMedia(Constants.CONTENTTYPE_RINGTONE);
			vacuumMedia(Constants.CONTENTTYPE_WALLPAPER);
		}	 
	}

	private static boolean vacuumContacts()
	{
		if(Constants.debug) System.out.println("VaultAPI.vacuumContacts");
		
		PIM pim = PIM.getInstance();
		
		ContactList clist;
		Enumeration contacts;
//			Retrieve contact values
//			The countValues() method returns the number of data values currently set in a particular field.
		try 
		{
		    clist = (ContactList) pim.openPIMList(PIM.CONTACT_LIST, PIM.READ_WRITE);
			//clist = (ContactList) pim.openPIMList(PIM.CONTACT_LIST, PIM.WRITE_ONLY);
			contacts = clist.items();
		} 
		catch(Exception e) 
		{
			if(Constants.debug) System.out.println("VaultAPI.vacuumContacts1 " + e.toString());
		    //security or other exception
			throw new SecurityException("vacuumContacts failed!");
		}
		
		if(Constants.debug) System.out.println("VaultAPI.vacuumContacts2");
		
		while(contacts.hasMoreElements())
		{
			if(Constants.debug) System.out.println("VaultAPI.vacuumContacts3");
			Contact c = (Contact) contacts.nextElement();
			try {
				clist.removeContact(c);
			} catch (PIMException e) {
				if(Constants.debug) System.out.println("VaultAPI.vacuumContacts4" + e.toString());			
				}
		}
		
		try {
			clist.close();
		} catch (PIMException e) {
			if(Constants.debug) System.out.println("VaultAPI.vacuumContacts5" + e.toString());		
			}
		return true;
	}
	
	private static boolean vacuumMedia(int mediatype)
	{
		Vector uniquepaths = getUniqueNonnullPaths(mediatype); 
		for(int i=0; i<uniquepaths.size(); i++)
		{
			String path = (String) uniquepaths.elementAt(i);
			if(Constants.partialdebug) System.out.println("VaultAPI.loadMediaList() Media " + path);
			try
			{
				recursivelyDeleteMedia(path, mediatype);
			}
			catch(Exception e)
			{
				if(Constants.debug) System.out.println("VaultAPI.loadMediaList() LOADING FAILED BUT continue " + path + " " + e.toString());
			}
		}
		ApplicationAssistant.gc();
		
		return true;
	}
	
	private static boolean recursivelyDeleteMedia(String directory, int mediatype)
	{
		try
		{  
		  Vector subfolders = new Vector();
		  if(Constants.debug) System.out.println("VaultAPI.recursivelyDeleteMedia() ###########" + directory);
		  FileConnection fc = (FileConnection) Connector.open(directory, Connector.READ);
          Enumeration listOfFiles = fc.list("*", false);
		  while (listOfFiles.hasMoreElements())
          {
          	String currentFile = (String) listOfFiles.nextElement();
   	        
          	if(currentFile.endsWith(Constants.FILE_SEPARATOR)) //add all subfolders, and iterate through them, once this folder connection is closed
          	{
          		subfolders.addElement(directory + currentFile);
          		continue;
          	}
          	
          	if((mediatype == Constants.CONTENTTYPE_PHOTO && 
          			   (currentFile.toLowerCase().endsWith(".jpg") || 
          			    currentFile.toLowerCase().endsWith(".jpeg"))) ||
          	   (mediatype == Constants.CONTENTTYPE_RINGTONE && 
          			   (currentFile.toLowerCase().endsWith(".amr") || 
          			    currentFile.toLowerCase().endsWith(".mid") || 
          			    currentFile.toLowerCase().endsWith(".midi") || 
          			    currentFile.toLowerCase().endsWith(".mp3") || 
          			    currentFile.toLowerCase().endsWith(".sp.mid") || 
          			    currentFile.toLowerCase().endsWith(".smaf") || 
          			    currentFile.toLowerCase().endsWith(".mmf") || 
          			    currentFile.toLowerCase().endsWith(".wav") || 
          			    currentFile.toLowerCase().endsWith(".aac") || 
          			    currentFile.toLowerCase().endsWith(".imy"))) ||
          	   (mediatype == Constants.CONTENTTYPE_WALLPAPER && 
          			   (currentFile.toLowerCase().endsWith(".jpg") || 
          			    currentFile.toLowerCase().endsWith(".jpeg") || 
          			    currentFile.toLowerCase().endsWith(".bmp") || 
          			    currentFile.toLowerCase().endsWith(".png") || 
          			    currentFile.toLowerCase().endsWith(".gif"))) ||		
          	   (mediatype == Constants.CONTENTTYPE_VIDEO && 
          			   (currentFile.toLowerCase().endsWith(".3gp") || 
          				currentFile.toLowerCase().endsWith(".mp4"))))
          	{
          		if(Constants.partialdebug) System.out.println("VaultAPI.recursivelyDeleteMedia() " + mediatype + " file = " + currentFile);

          		FileConnection imagefc = null;
          		try
          		{
	          		imagefc = (FileConnection) Connector.open(directory + currentFile, Connector.WRITE);
          			imagefc.delete();
          			imagefc.close();
	          	}
          		catch(Exception e)
          		{
          			if(Constants.debug) System.out.println("VaultAPI.recursivelyDeleteMedia() "+ e.toString());
          		}
          	}
          }
          
          fc.close();
          
          for(int i=0; i<subfolders.size(); i++)
          {
        	  recursivelyDeleteMedia((String) subfolders.elementAt(i), mediatype);
          }
		}
		catch(Exception e)
		{
			if(Constants.debug) System.out.println("VaultAPI.recursivelyDeleteMedia() "+ e.toString());
		}
		return true;
	}
	
}

	
/*	
 	public static int getAvailableSpace() 
	{
		try 
		{
			//any database, cos all of them talk abt the common rms space left
			dbmanager_objectstore.open();
			int size = dbmanager_objectstore.getAvailableSpace();
			dbmanager_objectstore.close();
			if(Constants.debug) System.out.println("*** AVAILABLE RMS : " + size);
			return size;
		} 
		catch (Exception e) 
		{
			return 0;
		}
	}
	
 	public static void clearDB()
	{
		try
		{
			for(int i = 0; i<dgsmmsg.length ; i++)
				dgsmmsg[i].destroy();
			for(int i = 0; i<hgsmmsg.length ; i++)
				hgsmmsg[i].destroy();
			for(int i = 0; i<contentcache.length ; i++)
				contentcache[i].destroy();
			
			System.out.println("Cleared Database");
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
*/
