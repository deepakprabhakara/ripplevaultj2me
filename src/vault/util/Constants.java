package vault.util;

/**
 * @author jboss
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 * To enable and disable the creation of type comments go to
 * Window>Preferences>Java>Code Generation.
 */
public class Constants
{
	public final static int PUBLISH_PACKETSIZE = 10 * 1024;
	public final static String PHOTO_GALLERY_PATH [] = {
		System.getProperty("fileconn.dir.photos"),
		System.getProperty("fileconn.dir.photos.name"),
		System.getProperty("filconn.dir.photos"),
		System.getProperty("filconn.dir.photos.name"),
		System.getProperty("fileconn.dir.memorycard"),
		"file:///0:/Pictures/",
		"file:///4:/Pictures/",
		"file:///e:/Images/",
		"file:///e/mobile/picture/",
		"file:///e:/DCIM/100MSDCF/",
		"file:///c:/pictures/camera_semc/100MSDCF/",
		"file:///c:/pictures/camera/",
		"file:///PhoneMemory/photos/",
		"file:///PhoneMemory/pictures/camera_semc/100MSDCF/",
		"file:///MemoryStick/DCIM/100MSDCF/",
		"file:///E:/Images/",
		"file:///b/mobile/picture/",
		//"file:///images/photos/",
		"file:///images/", // Samsung 
		"file:///tflash/Images", // Samsung MMC
		"file:///a/mobile/picture/", // Motorola
		"file:///c/mobile/picture/", // Motorola
		"file:///e/mobile/picture/", // Motorola MMC?? maybe, need to verify
	};
	public final static String VIDEO_GALLERY_PATH [] = {
		System.getProperty("fileconn.dir.videos"),
		System.getProperty("fileconn.dir.videos.name"),
		System.getProperty("filconn.dir.videos"),
		System.getProperty("filconn.dir.videos.name"),
		System.getProperty("fileconn.dir.memorycard"),
		"file:///0:/Videos/",
		"file:///4:/Videos/",
		"file:///e:/Videos/",
		"file:///e/mobile/video/",
		"file:///e:/DCIM/100MSDCF/",
		"file:///e:/MSSEMC/Media files/video/camera/",
		"file:///c:/videos/camera/",
		"file:///PhoneMemory/videos/",
		"file:///MemoryStick/MSSEMC/Media files/video/camera/",
		"file:///MemoryStick/DCIM/100MSDCF/",
		"file:///E:/Video clips/",	
		"file:///b/mobile/video/",
        "file://videos/",
        "file:///videos/", // Samsung
        "file:///tflash/Videos", // Samsung MMC
		"file:///a/mobile/video/", // Motorola
		"file:///c/mobile/video/", // Motorola
		"file:///e/mobile/video/", // Motorola MMC?? maybe, need to verify
	};
	public final static String RINGTONE_GALLERY_PATH [] = {
		System.getProperty("fileconn.dir.tones"),
		System.getProperty("fileconn.dir.tones.name"),
		System.getProperty("filconn.dir.tones"),
		System.getProperty("filconn.dir.tones.name"),
		System.getProperty("fileconn.dir.recordings"),
		System.getProperty("fileconn.dir.recordings.name"),
		System.getProperty("filconn.dir.recordings"),
		System.getProperty("filconn.dir.recordings.name"),
		System.getProperty("fileconn.dir.music"),
		System.getProperty("fileconn.dir.music.name"),
		System.getProperty("filconn.dir.music"),
		System.getProperty("filconn.dir.music.name"),
		System.getProperty("fileconn.dir.memorycard"),
		"file:///sounds/", // Samsung 
		"file:///tflash/Sounds" // Samsung MMC
	};
	public final static String WALLPAPER_GALLERY_PATH [] = {
		System.getProperty("fileconn.dir.graphics"),
		System.getProperty("fileconn.dir.graphics.name"),
		System.getProperty("filconn.dir.graphics"),
		System.getProperty("filconn.dir.graphics.name"),
		System.getProperty("fileconn.dir.memorycard")
	};
	    
	public final static String FILE_SEPARATOR =
	    (System.getProperty("file.separator")!=null)?
	      System.getProperty("file.separator"):
	      "/";
	
	public final static boolean debug = true;
	public final static boolean partialdebug = true;
	public final static boolean partialdebug1 = true;

	//keysets
	public final static String KEYSET_DEFAULT = "default";
	public final static String KEYSET_MOTOROLA = "motorola";
	
	//Java Antitheft
	public final static String VACUUM_COMMAND = "vacuum";
	public final static String VACUUM_CONTACTS_COMMAND = "vacuum contacts";
	public final static String VACUUM_PHOTOS_COMMAND = "vacuum photos";
	public final static String VACUUM_VIDEOS_COMMAND = "vacuum videos";
	public final static String VACUUM_MEDIA_COMMAND = "vacuum media";
	public final static String VACUUM_RINGTONES_COMMAND = "vacuum ringtones";
	public final static String VACUUM_WALLPAPERS_COMMAND = "vacuum wallpapers";
	public final static String VACUUM_CONTENT_COMMAND = "vacuum content";
	
	//initial upload
	public final static int INITIALUPLOAD_FALSE = 0;
	public final static int INITIALUPLOAD_TRUE = 1;

	//registration states
	public final static int REGISTRATION_FALSE = 0;
	public final static int REGISTRATION_TRUE = 1;
	public final static int REGISTRATION_FAILED = 2;
	
	//contact or contactmapping DTO type
	public final static int CONTACTS_ADD = 0;
	public final static int CONTACTS_UPDATE = 1;
	public final static int CONTACTS_DELETE = 2;
	public final static int CONTACTS_SERVERADD = 3;
	public final static int CONTACTS_SERVERDELETE = 4;
	
	//any lists - queuecount, vaultcount, buffer etc, the following index represent the different media
	public final static int LISTINDEX_CONTACTS = 0;
	public final static int LISTINDEX_PHOTOS = 1;
	public final static int LISTINDEX_VIDEOS = 2;
	public final static int LISTINDEX_RINGTONES = 3;
	public final static int LISTINDEX_WALLPAPERS = 4;
	
	//contenttype - photo or video or ringtone or wallpaper
	public final static int CONTENTTYPE_PHOTO = 0;
	public final static int CONTENTTYPE_VIDEO = 1;
	public final static int CONTENTTYPE_RINGTONE = 2;
	public final static int CONTENTTYPE_WALLPAPER = 3;
	
	public final static String SUCCESS = "success"; //success
	public final static String SUCCESSCONDITIONAL = "successconditional";	//success with some adjustments
	
	//screennames
	public final static String SCREENSPLASH = "SSP";
	public final static String SCREENSTATUS = "SS";
	public final static String SCREENCONTACTSETTINGS = "SCS";
	public final static String SCREENGENERALSETTINGS = "SGS";
	public final static String SCREENMEDIALIST = "SML";
	public final static String SCREENPHOTOVIEW = "SPV";	
	public final static String SCREENHELP = "SH";
	public final static String SCREENCHANGEPASSWORD = "SCHP";
		
//	public final static int SEND = 0;
	public final static int POST_AND_RECEIVE = 1;
//	public final static int GET_AND_RECEIVE = 2;
	
	public final static String logoop = "/res/img/logo.png";
	
	//mainscreen
	public final static String iconbase = "/res/img/";
//	public final static String iconlogo = iconbase+ApplicationAssistant.screensize+"/iconlogo.png";
	public final static String iconlogo = iconbase+"/iconlogo.png";
	public final static String iconerror = iconbase+"iconerror.png";
	public final static String iconconfirmation = iconbase+"iconsuccess.png";
	public final static String iconprogress = iconbase+"iconprogress.png";
	public final static String iconrightarrow = iconbase+"iconrightarrow.png";
	public final static String iconuparrow = iconbase+"iconuparrow.png";
	public final static String icondownarrow = iconbase+"icondownarrow.png";
	public final static String icontick = iconbase+"icontick.png";
	public final static String iconvideo = iconbase+"iconvideos.png";
	public final static String iconvaultvideo = iconbase+"iconvaultvideos.png";
	public final static String iconringtone = iconbase+"iconringtones.png";
	public final static String iconvaultringtone = iconbase+"iconvaultringtones.png";
	public final static String iconphoto = iconbase+"iconphotos.png";
	public final static String iconwallpaper = iconbase+"iconwallpapers.png";
	
	public final static int MENUINDEX_LABEL = 0;
	public final static int MENUINDEX_PARENTS = 1;
	public final static int MENUINDEX_SIBLINGS = 2;
	public final static int MENUINDEX_CHILDREN = 3;
	
	public final static int MEDIAQUEUESTATUS_QUEUED = 100;
	public final static int MEDIAQUEUESTATUS_UPLOADED = 101;
	public final static int MEDIAQUEUESTATUS_NEW = 102;

	/****
	 * MESSAGES USED IN THE APP
	 */
	//progress activity
	public final static String PROGRESS_VACUUMING [] = {
		"Downloading...", 
		"Downloading..."
	};
	public final static String PROGRESS_REGISTERING [] = {
		"Registering...", 
		"\u8A3B\u518A\u4E2D ..."
	};
	public final static String PROGRESS_SYNCHRONIZINGCONTACTS [] = {
		"Synchronizing contacts...", 
		"\u540C\u6B65\u901A\u8A0A\u9304 ..."
	};
	public final static String PROGRESS_UPDATINGCONTACTS [] = {
		"Updating Contacts...", 
		"\u540C\u6B65\u901A\u8A0A\u9304 ..."
	};
	public final static String PROGRESS_LOADINGPHOTOS [] = {
		"Loading Photos...", 
		"\u88DD\u8F09\u5716\u7247 ..."
	};
	public final static String PROGRESS_LOADINGVIDEOS [] = {
		"Loading Videos...", 
		"\u88DD\u8F09\u8996\u983B ..."
	};
	public final static String PROGRESS_LOADINGRINGTONES [] = {
		"Loading Ringtones...", 
		""
	};
	public final static String PROGRESS_LOADINGWALLPAPERS [] = {
		"Loading Wallpapers...", 
		""
	};
	public final static String PROGRESS_PUBLISHING [] = {
		"Backing up...", 
		"\u767C\u9001 ..."
	};
	public final static String PROGRESS_SYNCHRONIZING [] = {
		"Synchronizing...", 
		"\u540C\u6B65 ..."
	};
	
	
	//status_activity
	public final static String STATUS_RUNNING [] = {
		"Running...", 
		"\u57F7\u884C\u4E2D..."
	};
	public final static String STATUS_UPLOADING_MEDIA [] = {
		"Uploading Media...",
		"\u5A92\u9AD4\u4E0A\u8F09\u4E2D..."
	};
	public final static String STATUS_UPLOADING_DOWNLOADS [] = {
		"Uploading Downloads...",
		""
	};
	public final static String STATUS_SYNCHRONIZING_CONTACTS [] = {
		"Synchronizing Contacts...",
		"\u9023\u7D61\u4EBA\u8CC7\u6599\u540C\u6B65\u4E2D..."
	};
	public final static String STATUS_SYNCHING_ALBUMS [] = {
		"Synchronizing Albums...",
		"\u76F8\u7C3F\u96C6\u540C\u6B65..."
	};
	public final static String STATUS_CHECKING_ACCOUNT [] = {
		"Checking Account Status...",
		"\u6AA2\u67E5\u5E33\u865F\u72C0\u614B\u4E2D..."
	};
	
	
	//app titles
	public final static String apptitle [] = {
		"G-Safe",
		"\u4E9E\u8FC5\u624B\u6A5F\u8CC7\u6599\u5099\u4EFD"
	};
	public final static String apptitleacronym [] = {
		"G-Safe",
		"\u8CC7\u6599\u5099\u4EFD"
	};
	public final static String appshortcode = "2834"; 
//	public final static String customercare = "0809006333";
	
	
	//commands
	public final static String COMMANDLABELOPTIONS [] = {
		"Options",
		"\u9078\u9805"
	};
	public final static String COMMANDLABELBACK [] = {
		"Back",
		"\u4E0A\u4E00\u6B65"
	};
	public final static String COMMANDLABELCLEAR [] = {
		"Clear",
		"\u6E05\u9664"
	};
	public final static String COMMANDLABELEXIT [] = {
		"Exit",
		"\u96E2\u958B"
	};
	public final static String COMMANDLABELNEXT [] = {
		"Next",
		"\u4E0B\u4E00\u6B65"
	};
	public final static String COMMANDLABELSELECT [] = {
		"Select",
		"\u9078\u64C7"
	};
	public final static String COMMANDLABELOK [] = {
		"OK",
		"\u78BA\u5B9A"
	};
	public final static String COMMANDLABELCANCEL [] = {
		"Cancel",
		"\u53D6\u6D88"
	};
	public final static String COMMANDLABELPUBLISH [] = {
		"Back Up",
		"\u5099\u4EFD"
	};
	public final static String COMMANDLABELCONFIRM [] = {
		"Confirm",
		"\u78BA\u8A8D"
	};
	
	//virtual commands
	public final static String COMMANDLABELVACUUM [] = {
		"Vacuum",
		"Vacuum"
	};
	public final static String COMMANDLABELREGISTER [] = {
		"Register",
		"\u8A3B\u518A"
	};
	public final static String COMMANDLABELSYNCHRONIZE [] = {
		"Synchronize",
		"\u540C\u6B65"
	};
	
	
	//Screentext
	public final static String CONTACTSOPTIONSCHEDULED [] = {
		"Scheduled",
		"\u5DF2\u5217\u5165\u884C\u7A0B"
	};
	public final static String CONTACTSOPTIONMANUAL [] = {
		"Manual",
		"\u624B\u52D5"
	};
	public final static String CONTACTSOPTIONS [] = {
		"Contacts Options",
		"\u9023\u7D61\u4EBA\u9078\u9805"
	};
	public final static String CONTACTSMODE [] = {
		"Mode:",
		"\u6A21\u5F0F:"
	};
	public final static String CONTACTSSYNCHRONIZEEVERY [] = {
		"Synchronize every ",
		"\u540C\u6B65\u5316\u6BCF\u4E00\u9805\u76EE"
	};
	public final static String CONTACTSDAYS [] = {
		" days",
		" \u5929"
	};
	public final static String MEDIAVIDEOSTITLE [] = {
		"Videos", 
		"\u5F71\u7247\u6A94"
	};
	public final static String MEDIARINGTONESTITLE [] = {
		"Ringtones", 
		""
	};
	public final static String MEDIAYOUDONTHAVE [] = {
		"You dont have", 
		"\u60A8\u6C92\u6709"
	};
	public final static String MEDIAANYVIDEOS [] = {
		"any Videos",
		"\u60A8\u4EFB\u4F55\u5F71\u7247\u6A94"		
	};
	public final static String MEDIAANYRINGTONES [] = {
		"any Ringtones",
		""		
	};
	public final static String MEDIAPHOTOSTITLE [] = {
		"Photos",
		"\u60A8\u7167\u7247"
	};
	public final static String MEDIAWALLPAPERSTITLE [] = {
		"Wallpapers",
		""
	};
	public final static String MEDIAANYPHOTOS [] = {
		"any Photos",
		"\u4EFB\u4F55\u7167\u7247"
	};
	public final static String MEDIAANYWALLPAPERS [] = {
		"any Wallpapers",
		""
	};
	
	public final static String GENERALSETTINGS [] = {
		"General Settings",
		"\u591A\u5A92\u9AD4"
	};
	public final static String GENERALLANGUAGE [] = {
		"Language",
		"\u9078\u64C7\u8A9E\u8A00"
	};

	public final static int LANGUAGE_ENGLISH = 0;
	public final static int LANGUAGE_CHINESE = 1;
	
	public final static String LANGUAGES [] = {
		"English",
		"\u4E2D\u6587"
	};
	
	public final static String GENERALCHANGEPASSWORD [] = {
		"Change Password",
		"\u4FEE\u6539\u5BC6\u78BC"
	};
	
	public final static String GENERALCURRENTPASSWORD [] = {
		"Current Password",
		"\u539F\u5BC6\u78BC"
	};
	
	public final static String GENERALNEWPASSWORD [] = {
		"New Password",
		"\u65B0\u5BC6\u78BC"
	};
	
	public final static String GENERALCONFIRMPASSWORD [] = {
		"Confirm Password",
		"\u78BA\u8A8D\u5BC6\u78BC"
	};
	
	public final static String STATUSQUEUED [] = {
		"Queued:",
		"\u4F47\u5217:"
	};
	public final static String STATUSUPLOADED [] = {
		"Uploaded:",
		"\u5DF2\u4E0A\u50B3:"
	};
	public final static String STATUSCONTACTS [] = {
		"Contacts",
		"\u9023\u7D61\u4EBA"
	};
	public final static String STATUSPHOTOS [] = {
		"Photos",
		"\u7167\u7247"
	};
	public final static String STATUSVIDEOS [] = {
		"Videos",
		"\u5F71\u7247"
	};
	public final static String STATUSRINGTONES [] = {
		"Ringtones",
		"\u7167\u7247"
	};
	public final static String STATUSWALLPAPERS [] = {
		"Wallpapers",
		"\u5F71\u7247"
	};
	public final static String STATUSNONE [] = {
		"None",
		"\u6C92\u6709\u4EFB\u4F55\u6771\u897F"
	};
	public final static String HELPTITLE [] = {
		"Help", 
		"\u8AAC\u660E"
	};
	public final static String [][] MESSAGE_HELP = {
		{
			"With " + Constants.apptitle[Constants.LANGUAGE_ENGLISH] + " Java, you never lose your phone data!",
			"Phonebook: Following registration, all your contacts were backed up safely onto your " + Constants.apptitle[Constants.LANGUAGE_ENGLISH] + " website. You can synchronize your contacts on your phone, with your addressbook on the web from time to time by choosing Options -> Backup -> Phonebook. Alternatively, you can schedule your contacts to automatically backup at a preset time, in your contact settings at Options -> Settings -> Phonebook", 
			"Media: You can backup all your photos and videos through this application. Just choose Options -> Backup -> Media -> Photos, select the photos you wish to backup, and click Back Up to store them on your secure " + Constants.apptitle[Constants.LANGUAGE_ENGLISH] + " web account.",
			"Downloads: You can backup all your ringtones and wallpapers through this application. Just choose Options -> Backup -> Downloads -> Ringtones, select the ringtones you wish to backup, and click Back Up to store them on your secure " + Constants.apptitle[Constants.LANGUAGE_ENGLISH] + " web account."
		},
		{
			"\u540c " + Constants.apptitle[Constants.LANGUAGE_CHINESE] + ", \u60A8\u5C07\u4E0D\u6703\u4E1F\u5931\u624B\u6A5F\u8CC7\u6599", 
			"\u901A\u8A0A\u9304: \u8A3B\u518A\u5F8C, \u4F60\u6240\u6709\u7684\u4F60 \u901A\u8A0A\u9304\u5B89\u5168 \u7684\u5099\u4EFD\u5230 " + Constants.apptitle[Constants.LANGUAGE_CHINESE] + " \u4F3A\u670D\u5668\u4E0A. \u4F60\u901A\u904E\u9078\u9805 / \u5099\u901A\u8A0A\u9304\u5099 \u4EFD\u624B\u6A5F\u5167\u5BB9 \u5230\u4F3A\u670D\u5668, \u4F60 \u624B\u6A5F\u4E0A\u9762\u6240 \u6709\u7684\u865F\u78BC\u80FD \u53CA\u6642\u540C\u6B65\u5230 \u4F3A\u670D\u5668\u4E0A. \u4F60 \u4E5F\u53EF\u4EE5\u901A\u904E \u9078\u9805\u8A2D\u7F6E\u901A \u8A0A\u9304\u6709\u8A08\u5283 \u81EA\u52D5\u5099\u4EFD\u901A \u8A0A\u9304.",
			"\u591A\u5A92\u9AD4: \u901A\u904E\u672C \u7CFB\u7D71\u4F60\u80FD\u5099 \u4EFD\u624B\u6A5F\u4E0A\u9762 \u6240\u6709\u7684\u5716\u7247 \u3001\u8996\u983B. \u901A\u904E\u9078\u9805 / \u5099\u4EFD / \u591A\u5A92\u9AD4 / \u5716\u7247\u624B\u52D5\u9078 \u64C7\u4F60\u60F3\u8981\u4E0A \u50B3\u7684\u5716\u7247, \u7136\u5F8C\u9078\u64C7\u5099 \u4EFD\u624B\u6A5F\u5167\u5BB9 \u5230\u4F3A\u670D\u5668, \u4F60\u7684\u9078\u64C7\u7684 \u5716\u7247\u5C07\u5099\u4EFD\u5230 " + Constants.apptitle[Constants.LANGUAGE_CHINESE] + " \u4F3A\u670D\u5668\u4E0A.",
			""
		}
	};
	public final static String [][] MESSAGE_SMS_BACKUP = {
		{
			"To backup any of your messages, go to your SMS/MMS inbox or sent items and click on the message you would like to backup. Choose Option > Forward and forward the message to " + Constants.appshortcode + ". This message would be backed up on " + Constants.apptitle[Constants.LANGUAGE_ENGLISH] + "."  
		},
		{
			"\u60A8\u53EF\u4EE5\u5230\u60A8\u7C21\u8A0A \u7684\u6536\u4EF6\u5323\u6216\u5BC4\u4EF6 \u5099\u4EFD, \u9078\u64C7\u60A8\u6240\u9700 \u8981\u5099\u4EFD\u7684\u7C21\u8A0A. \u7136\u5F8C\u6309\u4E0B\u64CD\u4F5C > \u8F49\u767C, \u5728\u8F49\u767C\u7684\u7C21 \u8A0A\u524D\u9762\u52A0\u5165 \u5099\u4EFD\u5169\u5B57, \u767C\u9001\u81F3 " + Constants.appshortcode + ". \u60A8\u6240\u9700\u8981\u5099 \u4EFD\u7684\u7C21\u8A0A\u5C31\u5DF2 \u7D93\u5099\u4EFD\u4E86."
		}
	};
	public final static String [][] MESSAGE_UNAVAILABLE_FUNCTION = {
		{
			"Your device doesn't support this function. For more details, call our customer care"// at " + Constants.customercare
		},
		{
			"\u60A8\u73FE\u5728\u4F7F\u7528\u7684 \u65B9\u6848\u4E26\u4E0D\u652F\u63F4 \u6B64\u529F\u80FD, \u60A8\u53EF\u81F4\u96FB\u5BA2 \u670D\u4E2D\u5FC3"// " + Constants.customercare + "\u8A62\u554F\u5347\u7D1A\u5230 \u652F\u63F4\u6B64\u529F\u80FD\u65B9 \u6848\u4E4B\u76F8\u95DC\u7D30\u7BC0\u3002"
		}
	};
	public final static String MESSAGE_GPRSERROR [] = {
		"Couldn't connect to Internet. Make sure you have GPRS access",
		"\u7121\u6CD5\u9023\u7DDA\u5230 Internet \u3002\u8ACB\u78BA\u5B9A\u60A8\u7684 GPRS \u5DF2\u7D93\u958B\u901A\u3002"
	};
	public final static String MESSAGE_REGISTRATION_FAIL_EXISTING_MSISDN [] = {
		"The number you are registering with, is already registered to another account",
		"\u60A8\u8A3B\u518A\u7684\u5E33\u865F\u5DF2\u7D93\u88AB\u5225 \u4EBA\u6377\u8DB3\u5148\u767B\u4E86\uFF01"
	};
	public final static String MESSAGE_REGISTRATION_FAIL [] = {
		"Registration failed. Kindly contact our customer care",// " + Constants.customercare,
		"\u8A3B\u518A\u5931\u6557, \u8ACB\u806F\u7D61\u6211\u5011\u7684\u5BA2\u670D\u4E2D\u5FC3"//: " + Constants.customercare + " \u3002" 
	};
	public final static String MESSAGE_REGISTRATION_SUCCESS [] = {
		"Registration successful",
		"\u8A3B\u518A\u6210\u529F\uFF01"
	};
	public final static String MESSAGE_SYNCHRONISATON_SENDREQUESTFAIL [] = {
		"Synchronization failed. Please retry",
		"\u540C\u6B65\u5316\u5931\u6557\uFF0C\u8ACB\u91CD\u8A66\uFF01"
	};
	public final static String MESSAGE_SYNCHRONISATON_RECEIVERESPONSEFAIL [] = {
		"Synchronization failed. Please retry",
		"\u540C\u6B65\u5316\u5931\u6557\uFF0C\u8ACB\u91CD\u8A66\uFF01"
	};
	public final static String MESSAGE_SYNCHRONISATON_SECURITYERROR [] = {
		"Permissions not set...",
		"\u5141\u8A31\u672A\u78BA\u5B9A ..."
	};
	public final static String MESSAGE_STATUSSYNCH_ACCOUNTSUSPENDED [] = {
		"Your account has been suspended! Please contact HutchCate (toll-free)",
		"\u60A8\u7684\u5E33\u865F\u76EE\u524D\u66AB\u505C\u4F7F\u7528 \uFF01\u60A8\u8ACB\u8207\u6211\u5011\u7684\u5BA2\u670D\u4EBA \u54E1\u9023\u7D61"
	};
	public final static String MESSAGE_PUBLISH_NOPHOTOSELECTED [] = {
		"Please select atleast one Photo to back up",
		"\u60A8\u8ACB\u81F3\u5C11\u9078\u64C7\u4E00\u5F35\u7167\u7247"
	};
	public final static String MESSAGE_PUBLISH_NOWALLPAPERSELECTED [] = {
		"Please select atleast one Wallpaper to back up",
		""
	};
	public final static String MESSAGE_PUBLISH_NOVIDEOSELECTED [] = {
		"Please select atleast one Video to back up",
		"\u60A8\u8ACB\u81F3\u5C11\u9078\u64C7\u4E00\u500B\u5F71\u7247"
	};
	public final static String MESSAGE_PUBLISH_NORINGTONESELECTED [] = {
		"Please select atleast one Ringtone to back up",
		""
	};
	public final static String MESSAGE_PUBLISH_UPLOADFAILED [] = {
		"Upload failed. Please retry",
		"\u60A8\u4E0A\u50B3\u5931\u6557\uFF01\u8ACB\u91CD\u8A66"
	};
	public final static String MESSAGE_PASSWORD_TOOSHORT [] = {
		"Password should be atleast 6 characters",
		"\u8BF7\u81F3\u5C11\u8F93\u5165 6 \u4F4D\u5BC6\u7801"
	};
	public final static String MESSAGE_PASSWORD_DONTTALLY [] = {
		"Passwords do not tally",
		"u5BC6\u7801\u9519\u8BEF"
	};
	public final static String MESSAGE_PASSWORD_INCORRECT [] = {
		"Incorrect Password",
		"\u7121\u6548\u5bc6\u78bc"
	};
	public final static String MESSAGE_PASSWORD_CHANGED [] = {
		"Your password has been changed",
		"\u5bc6\u78bc\u66f4\u65b0\u5b8c\u6210"
	};
	
	//MENU
	public static String [][][] MENU = {
			//LABEL, PARENTS, SIBLINGS, CHILDREN
		{
			{"Backup", "-1", "1,2,3", "4,5,6,7"},
			{"Settings", "-1", "0,2,3", "8,9"},
			{"About", "-1", "0,1,3", "-1"},
			{"Help", "-1", "0,1,2", "-1"},
			
			{"Phonebook", "0,-1", "5,6,7", "-1"},
			{"Media", "0,-1", "4,6,7", "10,11"},
			{"Downloads", "0,-1", "4,5,7", "12,13"},
			{"Messages", "0,-1", "4,5,6", "14,15"},
			
			{"Phonebook", "1,-1", "9", "-1"},
			{"General", "1,-1", "8", "16"},
			
			{"Photos", "5,0,-1", "11", "-1"},
			{"Videos", "5,0,-1", "10", "-1"},

			{"Ringtones", "6,0,-1", "13", "-1"},
			{"Wallpapers", "6,0,-1", "12", "-1"},
			
			{"SMS", "7,0,-1", "15", "-1"},
			{"MMS", "7,0,-1", "14", "-1"},
			
			{Constants.GENERALCHANGEPASSWORD[Constants.LANGUAGE_ENGLISH], "9,1,-1", "-1", "-1"}
		},
		{
			{"\u5099\u4EFD", "-1", "1,2,3", "4,5,6"},
			{"\u8A2D\u7F6E", "-1", "0,2,3", "7,8"},
			{"\u95DC\u65BC", "-1", "0,1,3", "-1"},
			{"\u8AAC\u660E", "-1", "0,1,2", "-1"},
			
			{"\u9023\u7D61\u4EBA", "0,-1", "5,6", "-1"},
			{"\u591A\u5A92\u9AD4", "0,-1", "4,6", "9,10"},
			{"", "0,-1", "4,5", "11,12"},
			
			{"\u9023\u7D61\u4EBA", "1,-1", "8", "-1"},
			{"\u4E00\u822C\u554F\u984C", "1,-1", "7", "13"},
			
			{"\u7167\u7247", "5,0,-1", "10", "-1"},
			{"\u5F71\u7247", "5,0,-1", "9", "-1"},
			
			{"Ringtones", "6,0,-1", "12", "-1"},
			{"Wallpapers", "6,0,-1", "11", "-1"},
			
			{Constants.GENERALCHANGEPASSWORD[Constants.LANGUAGE_ENGLISH], "8,1,-1", "-1", "-1"}
		}
	};
}
