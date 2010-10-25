package vault.app;

//import mb.entity.ContentElement;
//import mb.entity.TypeElement;
import java.util.Hashtable;
import java.util.Vector;

import javax.microedition.pim.Contact;
import javax.microedition.pim.ContactList;

import vault.dto.ContactDTO;
import vault.util.Constants;

public class XMLHome
{	
	//synch type constants
	public final static String SYNC_MSGT_INIT_EU = "init_eu";
	public final static String SYNC_MSGT_SYNC = "sync";
	public final static String SYNC_MSGT_POLL = "poll";
	public final static String SYNC_MSGT_PROFUPDATE = "prof_update";	
	
	public static String getHeaderXML(String msgtype, String currec, String totrec, String lstime, String msg) throws Exception
	{
		StringBuffer xml = new StringBuffer();
		
		try
		{			
			xml.append("<h><msgt>");
			xml.append(msgtype);
			xml.append("</msgt><msg>");
			xml.append(msg);
			xml.append("</msg>");
			xml.append("<uid>");
			xml.append(ApplicationAssistant.userid);
			xml.append("</uid>");
			xml.append("<hp>");
			xml.append(ApplicationAssistant.hp);
			xml.append("</hp>");
			xml.append("<did>");
			xml.append(ApplicationAssistant.appid);
			xml.append("</did>");
			xml.append("<currec>");
			xml.append(currec);
			xml.append("</currec>");
			xml.append("<totrec>");
			xml.append(totrec);
			xml.append("</totrec>");
			xml.append("<loc>en_us</loc>");
			xml.append("<ctime>");
			xml.append(System.currentTimeMillis());
			xml.append("</ctime>");
			xml.append("<lstime>");
			xml.append(lstime);
			xml.append("</lstime>");
			xml.append("<synctype><mod>cs</mod><gran>contactlevel</gran>");
			xml.append("<adde>true</adde><upde>true</upde><dele>true</dele></synctype>");
			xml.append("</h>");
			
			if(Constants.debug) System.out.println("encode: " + xml.toString());
			return xml.toString();
		}
		catch (Exception e)
		{
			xml = null;
			ApplicationAssistant.gc();
			
			if(Constants.debug) System.out.println("XMLHome.getHeaderXML() " + e.toString());
		}
		return null;
	}
	
	public static byte[] getRegistrationXML() throws Exception
	{
		StringBuffer xml = new StringBuffer();
		
		try
		{
			xml.append("<root>");
			xml.append(getHeaderXML("eu", "1", "1", "0", ""));				
					
			xml.append("<b><hp>");
			xml.append(ApplicationAssistant.hp);
			xml.append("</hp>");
			xml.append("<username>");
			xml.append(ApplicationAssistant.userid);
			xml.append("</username>");
			xml.append("<password>");
			xml.append(ApplicationAssistant.password);
			xml.append("</password>");
			xml.append("<phone_model>");
			xml.append(ApplicationAssistant.phone_model);
			xml.append("</phone_model>");
			xml.append("</b></root>");		
			
			return xml.toString().getBytes("UTF-8");
		}
		catch (Exception e)
		{
			xml = null;
			ApplicationAssistant.gc();
			
			if(Constants.debug) System.out.println("XMLHome.getRegistrationXML() " + e.toString());
		}
		return null;		
	}
	
	public static byte[] getChangePasswordXML(String currentpassword, String newpassword) throws Exception
	{
		StringBuffer xml = new StringBuffer();
		
		try
		{
			xml.append("<root>");
			xml.append(getHeaderXML("change_pw", "1", "1", "0", ""));				
					
			xml.append("<b><hp>");
			xml.append(ApplicationAssistant.hp);
			xml.append("</hp>");
			xml.append("<newpassword>");
			xml.append(newpassword);
			xml.append("</newpassword>");
			xml.append("<password>");
			xml.append(currentpassword);
			xml.append("</password>");
			xml.append("</b></root>");		
			
			return xml.toString().getBytes("UTF-8");
		}
		catch (Exception e)
		{
			xml = null;
			ApplicationAssistant.gc();
			
			if(Constants.debug) System.out.println("XMLHome.getRegistrationXML() " + e.toString());
		}
		return null;		
	}
	
	public static byte[] getStatusSubscriptionXML() throws Exception
	{
		StringBuffer xml = new StringBuffer();
		try
		{
			xml.append("<root>");
			xml.append(getHeaderXML(XMLHome.SYNC_MSGT_PROFUPDATE, "1", "1", "0", ""));				
			xml.append("<b><last-sync-timestamp>");
			xml.append(ApplicationAssistant.laststatussynctimestamp);
			xml.append("</last-sync-timestamp>");
			xml.append("<user-prof><param name=\"payment-type\" value=\"");
			xml.append("");
			xml.append("\"/>");
			xml.append("</user-prof><sys-prof></sys-prof>");
			xml.append("<settings></settings>");
			xml.append("</b></root>");
			
			return xml.toString().getBytes("UTF-8");
		}
		catch(Exception e)
		{
			xml = null;
			ApplicationAssistant.gc();
			
			if(Constants.debug) System.out.println("XMLHome.getRegistrationXML() " + e.toString());
		}
		return null;
	}

	public static String getPublishXML(String filename, String description, String album, int mediatype, int packetnum, int totalpackets) throws Exception
	{
		if(Constants.debug) System.out.println("XMLHome.getPublishXML(): " + filename + " " + description + " " + album + " " + mediatype + " " + packetnum + " " + totalpackets);
		
		StringBuffer xml = new StringBuffer();
		
		try
		{
			String msgt = "";
			String currec = "";
			String extension = "";
			int pos;
			
			if(mediatype == Constants.CONTENTTYPE_PHOTO)
				msgt = "photopacketbackup";
			else if(mediatype == Constants.CONTENTTYPE_VIDEO)
				msgt = "videopacketbackup";
			else if(mediatype == Constants.CONTENTTYPE_RINGTONE)
				msgt = "ringtonepacketbackup";
			else if(mediatype == Constants.CONTENTTYPE_WALLPAPER)
				msgt = "greetingpacketbackup";
			
			xml.append("<root>");
			
			pos = filename.indexOf(".");
			if(pos > 0){
				currec = filename.substring(0, pos + 1);
				extension = filename.substring(pos);
			}
			
			currec = currec + (packetnum + 1) + extension;
			
			xml.append(getHeaderXML(msgt, currec, "" + totalpackets, "0", ""));				
					
			xml.append("<b>");
			
			xml.append("<folder><name>");
			xml.append(album);
			xml.append("</name></folder>");
			xml.append("<description>");
			xml.append(description);
			xml.append("</description>");
			xml.append("</b></root>");		
			
			return xml.toString();
		}
		catch (Exception e)
		{
			xml = null;
			ApplicationAssistant.gc();
			
			if(Constants.debug) System.out.println("XMLHome.getPublishXML() " + e.toString());
		}
		return null;		
	}
	
	public static String getTelephoneFieldXML(Vector items, String type)
	{
		String tag;
		StringBuffer buffer = new StringBuffer();
		
		// Get subtags
		if (type.equals("mblsh"))
			tag = "mblh";
		else if (type.equals("mblsb"))
			tag = "mblb";
		else if (type.equals("mbls"))
			tag = "mbl";
		else if (type.equals("telsh"))
			tag = "telh";
		else if (type.equals("telsb"))
			tag = "telb";
		else if (type.equals("tels"))
			tag = "tel";
		else if (type.equals("fxsh"))
			tag = "fxh";
		else if (type.equals("fxsb"))
			tag = "fxb";
		else if (type.equals("fxs"))
			tag = "fx";
		else if (type.equals("emlsh"))
			tag = "emlh";
		else if (type.equals("emlsb"))
			tag = "emlb";
		else if (type.equals("emls"))
			tag = "eml";
		else if (type.equals("ttls"))
			tag = "ttl";
		else
			tag = "id";

		buffer.append("<");
		buffer.append(type);
		buffer.append(">");

		// Loop through the list and append item values to buffer
		for (int i = 0; i < items.size(); i++) {			
				buffer.append("<");
				buffer.append(tag);
				buffer.append(" id=\"");
				buffer.append(i);
				buffer.append("\">");
				buffer.append((String)items.elementAt(i));
				buffer.append("</");
				buffer.append(tag);
				buffer.append(">");
		}

		buffer.append("</");
		buffer.append(type);
		buffer.append(">");
		
		return buffer.toString();
	}

	public static String getAddressFieldXML(ContactDTO contact)
	{
		StringBuffer buffer = new StringBuffer();
		
		buffer.append("<addr><![CDATA[");
		
		try{
			if(contact.getAddressCountry() != ""){
				buffer.append("|C1|");
				buffer.append(contact.getAddressCountry());
			}
			if(contact.getAddressExtra() != ""){
				buffer.append("|E2|");
				buffer.append(contact.getAddressExtra());
			}
			if(contact.getAddressLocality() != ""){
				buffer.append("|L3|");
				buffer.append(contact.getAddressLocality());
			}
			if(contact.getAddressPobox() != ""){
				buffer.append("|P4|");
				buffer.append(contact.getAddressPobox());
			}
			if(contact.getAddressPostalcode() != ""){
				buffer.append("|P5|");
				buffer.append(contact.getAddressPostalcode());
			}
			if(contact.getAddressRegion() != ""){
				buffer.append("|R6|");
				buffer.append(contact.getAddressRegion());
			}
			if(contact.getAddressStreet() != ""){
				buffer.append("|S7|");
				buffer.append(contact.getAddressStreet());
			}
		}
		catch(Exception e)
		{
			buffer = null;
			buffer = new StringBuffer();
			
			buffer.append("<addr><![CDATA[");	
		}
		buffer.append("]]></addr>");
		
		return buffer.toString();
	}
	
	public static String getContactsSyncXML(ContactDTO contact) throws Exception
	{
		StringBuffer xml = new StringBuffer();
		//String xml = "";
		
		try
		{
			if(contact.getChangetype() == Constants.CONTACTS_SERVERADD) //delete
			{
				xml.append("<mapsa><ct id=\"");
				xml.append(contact.getContactid());
				xml.append("\"><gcid>");
				xml.append(contact.getGlobalid());
				xml.append("</gcid></ct></mapsa>");
			}
			else if(contact.getChangetype() == Constants.CONTACTS_SERVERDELETE) //delete
			{
				xml.append("<mapsd><ct id=\"");
				xml.append(contact.getContactid());
				xml.append("\"><gcid>");
				xml.append(contact.getGlobalid());
				xml.append("</gcid></ct></mapsd>");				
			}
			else if(contact.getChangetype() == Constants.CONTACTS_DELETE) //delete
			{
				xml.append("<delete><ct t=\"");
				xml.append(contact.getTimestamp());
				xml.append("\" id=\"");
				xml.append(contact.getContactid());
				xml.append("\">");
				xml.append("</ct></delete>");				
			}
			else
			{				
				if(contact.getChangetype() == Constants.CONTACTS_ADD)//add
				{
					xml.append("<add>");
				}
				else //update
				{
					xml.append("<update>");
				}
				
				 xml.append("<ct t=\"");
				 xml.append(contact.getTimestamp());
				 xml.append("\" id =\"");
				 xml.append(contact.getContactid());
				 xml.append("\"><fn><![CDATA[");
				 xml.append(contact.getFirstname());
				 xml.append("]]></fn><ln><![CDATA[");
				 xml.append(contact.getLastname());
				 xml.append("]]></ln><cn><![CDATA[");
				 xml.append(contact.getCompanyname());
				 xml.append("]]></cn>");
				 
			if (contact.getJobtitles().size() > 0) {
				xml.append(getTelephoneFieldXML(contact.getJobtitles(), "ttls"));
			}
			if (contact.getFaxes().size() > 0) {
				xml.append(getTelephoneFieldXML(contact.getFaxes(), "fxs"));
			}
			if (contact.getMobiles().size() > 0) {
				xml.append(getTelephoneFieldXML(contact.getMobiles(), "mbls"));
			}			
			if (contact.getHometelephones().size() > 0) {
				xml.append(getTelephoneFieldXML(contact.getHometelephones(), "telsh"));
			}			
			if (contact.getWorktelephones().size() > 0) {
				xml.append(getTelephoneFieldXML(contact.getWorktelephones(), "telsb"));
			}			
			if (contact.getTelephones().size() > 0) {
				xml.append(getTelephoneFieldXML(contact.getTelephones(), "tels"));
			}			
			if (contact.getHomeemails().size() > 0) {
				xml.append(getTelephoneFieldXML(contact.getHomeemails(), "emlsh"));
			}			
			if (contact.getWorkemails().size() > 0) {
				xml.append(getTelephoneFieldXML(contact.getWorkemails(), "emlsb"));
			}			
			if (contact.getEmails().size() > 0) {
				xml.append(getTelephoneFieldXML(contact.getEmails(), "emls"));
			}			

			xml.append(getAddressFieldXML(contact));					

			xml.append("<dob><![CDATA[");
			xml.append(contact.getBirthday());
			xml.append("]]></dob><nn><![CDATA[");
			xml.append(contact.getNickname());
			xml.append("]]></nn><nots><not id=\"1\"><![CDATA[");
			xml.append(contact.getNote());
			xml.append("]]></not></nots><urls><url id=\"1\"><![CDATA[");
			xml.append(contact.getUrl());
			xml.append("]]></url></urls>");
			
			xml.append("</ct>");
			
			if(contact.getChangetype() == 0)//add
			{
				xml.append("</add>");
			}
			else //update
			{
				xml.append("</update>");
			}
			
			}
			if(Constants.debug) System.out.println("getContactsSyncXML: " + xml.toString());
			return xml.toString();//.getBytes();
		}
		catch (Exception e)
		{
			xml = null;
			ApplicationAssistant.gc();
			
			if(Constants.debug) System.out.println("XMLHome.getHeaderXML() " + e.toString());
		}
		return null;
	}
	
	public static Vector[] parseContactsSynchXML(byte[] readdata) throws Exception
	{
			if(Constants.debug) System.out.println("ApplicationAPI.parseContactsSynchXML()");

			String xml = new String(readdata, "UTF-8");
			//ptr.Copy(iBuffer);


			String tagname;
			String tagvalue;
			int changetype=0;

			Vector records[] = new Vector[3];
			records[0] = new Vector();
			records[1] = new Vector();
			records[2] = new Vector();
			
			ContactDTO record = new ContactDTO();

			if(Constants.debug) System.out.println("ApplicationAPI.parseContactsSynchXML1");
			
			boolean parsecomplete = false;
			while(true) {
				if(Constants.debug) System.out.println("ApplicationAPI.parseContactsSynchXML2");
				
				int len = xml.length();
				int starttag = xml.indexOf("<");

				if (starttag < 0)
					break;

				if (starttag + 1 == len)
					break;

				int endtag = xml.indexOf(">");

				if (endtag < 0)
				{
					break;
				}

				if (xml.indexOf("/") == starttag + 1) {
					if (starttag + 2 == len)
						break;
					
					tagname = xml.substring(starttag + 2, endtag);

					if (starttag >= 0) {
						tagvalue = xml.substring(0, starttag);
						
						if(tagvalue == null)
							tagvalue = "";
						
						if(Constants.debug) System.out.println("tagvalue = " + tagvalue);
						//record = addDataToRecord(tagname, tagvalue, record);

						if(Constants.debug) System.out.println("ApplicationAPI.addDataToRecord " + tagname + tagvalue);
						
						if (tagname.equals("fn")){							
							record.setFirstname(convertXMLTags(tagvalue));
						}
						else if (tagname.equals("ln")){
							record.setLastname(convertXMLTags(tagvalue));
						}
						else if (tagname.equals("cn")){
							record.setCompanyname(convertXMLTags(tagvalue));
						}
						//else if (tagname.equals("mblh"))
						//	record.adiHomeMobiles->Add(tagValue.AllocL());
						//else if (tagName.Compare(_L("mblb")) == 0)
						//	record->iWorkMobiles->Add(tagValue.AllocL());
						else if (tagname.equals("mbl"))
							record.addMobiles(tagvalue);
						else if (tagname.equals("telh"))
							record.addHometelephones(tagvalue);
						else if (tagname.equals("telb"))
							record.addWorktelephones(tagvalue);
						else if (tagname.equals("tel"))
							record.addTelephones(tagvalue);
						//else if (tagname.equals("fxh"))
						//	record->iHomeFaxes->Add(tagValue.AllocL());
						//else if (tagname.equals("fxb"))
						//	record->iWorkFaxes->Add(tagValue.AllocL());
						else if (tagname.equals("fx"))
							record.addFaxes(tagvalue);
						else if (tagname.equals("emlh"))
							record.addHomeemails(tagvalue);
						else if (tagname.equals("emlb"))
							record.addWorkemails(tagvalue);
						else if (tagname.equals("eml"))
							record.addEmails(tagvalue);
						else if (tagname.equals("ttl"))
							record.addJobtitles(tagvalue);
						else if (tagname.equals("gcid"))
							record.setGlobalid(tagvalue);
						else if (tagname.equals("id")) {
							//TInt64 intVal;
							if (tagvalue.length() <= 0) {
								record.setContactid("-1");
							}
							else{
								record.setContactid(tagvalue);
							}
						}
						else if (tagname.equals("con"))
						{
							record.setAddressCountry(convertXMLTags(tagvalue));
						}
						else if (tagname.equals("extad"))
						{
							record.setAddressExtra(convertXMLTags(tagvalue));
						}
						else if (tagname.equals("city"))
						{
							record.setAddressLocality(convertXMLTags(tagvalue));
						}
						else if (tagname.equals("po"))
						{
							record.setAddressPobox(convertXMLTags(tagvalue));
						}
						else if (tagname.equals("pc"))
						{
							record.setAddressPostalCode(convertXMLTags(tagvalue));
						}
						else if (tagname.equals("regn"))
						{
							record.setAddressRegion(convertXMLTags(tagvalue));
						}
						else if (tagname.equals("strt1"))
						{
							record.setAddressStreet(convertXMLTags(tagvalue));
						}						
						else if (tagname.equals("dob"))
						{
							record.setBirthday(convertXMLTags(tagvalue));
						}						
						else if (tagname.equals("nn"))
						{
							record.setNickname(convertXMLTags(tagvalue));
						}						
						else if (tagname.equals("not"))
						{
							record.setBirthday(convertXMLTags(tagvalue));
						}						
						else if (tagname.equals("url"))
						{
							record.setUrl(convertXMLTags(tagvalue));
						}												
					}
					if (tagname.equals("ct"))
					{
						if(Constants.debug) System.out.println("\n\nEND OF CONTACT, ADD TO HEAP NOW\n\n");
						
						records[record.getChangetype()].addElement(record);
						record = null;
						record = new ContactDTO();
						parsecomplete = true;
					}
				}
				else {
					//tagname = xml.substring(starttag + 1, endtag - starttag - 1);
					tagname = xml.substring(starttag + 1, endtag);

					if(Constants.debug) System.out.println("tagname = " + tagname);
					
					if (tagname.equals("add")|| tagname.equals("update")
						 || tagname.equals("delete")) {

						if (tagname.equals("add"))
							changetype = 0;
						if (tagname.equals("update"))
							changetype = 1;
						if (tagname.equals("delete"))
							changetype = 2;
					}

					if (tagname.equals("ct")) {
						record.setChangetype(changetype);				
						//records->Add(record);
						parsecomplete = false;
					}
				}

				if (endtag + 1 >= len)
					break;

				//xml = xml.substring(endtag + 1, len - endtag - 1);
				xml = xml.substring(endtag + 1, len);
			}

			if (!parsecomplete)
			{
				if (record != null)
				{
					record = null;
				}
			}
			
			if(Constants.debug) System.out.println("ApplicationAPI.parseContactsSynchXMLEND");

			return records;
	}
	
	private static String convertXMLTags(String tagname)
	{
		String s;
		s = ReplaceTag(tagname, "&amp;", "&");
		s = ReplaceTag(s, "&lt;", "<");
		s = ReplaceTag(s, "&gt;", ">");
		s = ReplaceTag(s, "&quot;", "\"");
		s = ReplaceTag(s, "&apos;", "\'");
		
		return s;
	}
	
	private static String ReplaceTag(String tagname, String fromtag, String totag)
	{
		if (tagname.length() <= 0)
			return tagname;

		int pos;

		while(true)
		{
			pos = tagname.indexOf(fromtag);
			
			if (pos < 0)
				break;

			String part1 = tagname.substring(0, pos);
			String part2 = tagname.substring(pos+fromtag.length());
			
			tagname = part1 + totag + part2;			
		}	
				
		return tagname;
	}

	public static Hashtable parseStatusSubscriptionXml(byte[] readdata) throws Exception
	{
		final String PARAM_BEGIN_TAG = "<param><value>";
		final String PARAM_END_TAG = "</value></param>";
		final String LASTSYNCTIME_BEGIN_TAG = "<last-sync-timestamp>";
		final String LASTSYNCTIME_END_TAG = "</last-sync-timestamp>";
		
		if(Constants.partialdebug) System.out.println("ApplicationAPI.parseStatusSubscriptionXml()");
		String xml = new String(readdata, "UTF-8");
		if(Constants.partialdebug) System.out.println("ApplicationAPI.parseStatusSubscriptionXml() " + xml);
		Hashtable hashtable = new Hashtable();
		
		String tag = "last-sync-timestamp";
		String value = xml.substring(xml.indexOf(LASTSYNCTIME_BEGIN_TAG)+LASTSYNCTIME_BEGIN_TAG.length(), 
				  					 xml.indexOf(LASTSYNCTIME_END_TAG));
		hashtable.put(tag, value);
		if(Constants.debug) System.out.println("tag: " + tag + "; value: " + value);
		while(xml.indexOf(PARAM_BEGIN_TAG)!=-1) 
		{
			value = xml.substring(xml.indexOf(PARAM_BEGIN_TAG)+PARAM_BEGIN_TAG.length(), 
 					 			  xml.indexOf(PARAM_END_TAG));

			tag = value.substring(0,value.indexOf("=")).trim();
			value = value.substring(value.indexOf("=")+1).trim();
			hashtable.put(tag, value);
			if(Constants.debug) System.out.println("tag: " + tag + "; value: " + value);
			xml = xml.substring(xml.indexOf(PARAM_END_TAG)+PARAM_END_TAG.length());
		}
		if(Constants.partialdebug) System.out.println("ApplicationAPI.parseStatusSubscriptionXmlEND");
		return hashtable;
	}	
	
	/*
	private static ContactDTO addDataToRecord(String tagname, String tagvalue, ContactDTO record)
	{
		if(Constants.debug) System.out.println("ApplicationAPI.addDataToRecord " + tagname + tagvalue);
		
		if (tagname.equals("fn"))
			record.setFirstname(tagvalue);
		else if (tagname.equals("ln"))
			record.setLastname(tagvalue);
		else if (tagname.equals("cn"))
			record.setCompanyname(tagvalue);
		//else if (tagname.equals("mblh"))
		//	record.adiHomeMobiles->Add(tagValue.AllocL());
		//else if (tagName.Compare(_L("mblb")) == 0)
		//	record->iWorkMobiles->Add(tagValue.AllocL());
		else if (tagname.equals("mbl"))
			record.addMobiles(tagvalue);
		else if (tagname.equals("telh"))
			record.addHometelephones(tagvalue);
		else if (tagname.equals("telb"))
			record.addWorktelephones(tagvalue);
		else if (tagname.equals("tel"))
			record.addTelephones(tagvalue);
		//else if (tagname.equals("fxh"))
		//	record->iHomeFaxes->Add(tagValue.AllocL());
		//else if (tagname.equals("fxb"))
		//	record->iWorkFaxes->Add(tagValue.AllocL());
		else if (tagname.equals("fx"))
			record.addFaxes(tagvalue);
		else if (tagname.equals("emlh"))
			record.addHomeemails(tagvalue);
		else if (tagname.equals("emlb"))
			record.addWorkemails(tagvalue);
		else if (tagname.equals("eml"))
			record.addEmails(tagvalue);
		else if (tagname.equals("ttl"))
			record.addJobtitles(tagvalue);
		else if (tagname.equals("gcid"))
			record.setGlobalid(tagvalue);
		else if (tagname.equals("id")) {
			//TInt64 intVal;
			if (tagvalue.length() <= 0) {
				record.setContactid("-1");
			}
			else{
				record.setContactid(tagvalue);
			}
		}
		
		return record;
	}
	
	private static String readString() throws Exception
	{
		byte tmp = data[bytecursor++];
		if(tmp != (byte) 0x03)
			throw new Exception ("");
		if(Constants.debug) System.out.print("<string ");
		
		byte[] tmpbytes = new byte[2];
		tmpbytes[1] = data[bytecursor++];
		tmpbytes[0] = data[bytecursor++];
		int fieldlength = ApplicationAssistant.toInt(tmpbytes);
		if(Constants.debug)  System.out.print("length="+fieldlength + ">");
		
		byte [] fielddata = new byte [fieldlength];
		if(fieldlength>0)
		{
			System.arraycopy(data, bytecursor, fielddata, 0, fieldlength);
			bytecursor += fieldlength;
		}
		String field = new String(fielddata);
		tmpbytes = null;
		fielddata = null;
		ApplicationAssistant.gc();
		if(Constants.debug) System.out.print(field);
		
		tmp = data[bytecursor++];
		if(tmp != (byte) 0x00)
			throw new Exception ("");
		
		if(Constants.debug) System.out.println("</string>");
		if(Constants.debug) System.out.println("ApplicationAPI.addDataToRecord END");
		
		return field;
	}
	
	private static byte [] readBytes() throws Exception
	{
		byte tmp = data[bytecursor++];
		if(tmp != (byte) 0xC3)
			throw new Exception ("");
		if(Constants.debug) System.out.print("<byte ");
		
		byte[] tmpbytes = new byte[2];
		tmpbytes[1] = data[bytecursor++];
		tmpbytes[0] = data[bytecursor++];
		int fieldlength = ApplicationAssistant.toInt(tmpbytes);
		if(Constants.debug)  System.out.print("length="+fieldlength + ">");
		
		byte [] fielddata = new byte [fieldlength];
		System.arraycopy(data, bytecursor, fielddata, 0, fieldlength);
		bytecursor += fieldlength;
		
		tmpbytes = null;
		ApplicationAssistant.gc();
		
		tmp = data[bytecursor++];
		if(tmp != (byte) 0x00)
			throw new Exception ("");
		if(Constants.debug) System.out.println("</byte>");
		
		return fielddata;
	}
	*/
	/*private static int readShortInt() throws Exception
	{
		byte tmp = data[bytecursor++];
		if(tmp != (byte) 0x80)
			throw new Exception ("");
		if(Constants.debug) System.out.print("<shortint>");
		
		byte[] tmpbytes = new byte[1];
		tmpbytes[0] = data[bytecursor++];
		int field = (int) tmpbytes[0];
		if(Constants.debug)  System.out.print(field);
	
		tmpbytes = null;
		LogicBase.gc();
		
		tmp = data[bytecursor++];
		if(tmp != (byte) 0x00)
			throw new Exception ("");
		if(Constants.debug) System.out.println("</shortint>");
		
		return field;
	}
	
	private static int readInt() throws Exception
	{
		byte tmp = data[bytecursor++];
		if(tmp != (byte) 0x82)
			throw new Exception ("");
		if(Constants.debug) System.out.print("<int>");
		
		byte[] tmpbytes = new byte[2];
		tmpbytes[1] = data[bytecursor++];
		tmpbytes[0] = data[bytecursor++];
		int field = ApplicationAssistant.toInt(tmpbytes);
		if(Constants.debug)  System.out.print(field);
		
		tmpbytes = null;
		ApplicationAssistant.gc();
		
		tmp = data[bytecursor++];
		if(tmp != (byte) 0x00)
			throw new Exception ("");
		if(Constants.debug) System.out.println("</int>");
		
		return field;
	}
	
	private static long readLong() throws Exception
	{
		byte tmp = data[bytecursor++];
		if(tmp != (byte) 0x81)
			throw new Exception ("");
		if(Constants.debug) System.out.print("<long>");
		
		byte[] tmpbytes = new byte[8];
		for (int i = 0; i < tmpbytes.length; i++)
			tmpbytes[i] = data[bytecursor++];
		long field = ApplicationAssistant.toLong(ApplicationAssistant.reverse(tmpbytes));
		if(Constants.debug)  System.out.print(field);
		
		tmpbytes = null;
		ApplicationAssistant.gc();
		
		tmp = data[bytecursor++];
		if(tmp != (byte) 0x00)
			throw new Exception ("");
		if(Constants.debug) System.out.println("</long>");
		
		return field;
	}	
	
	private static void cleanUp()
	{
		data = null;
		ApplicationAssistant.gc();
	}
	*/
}