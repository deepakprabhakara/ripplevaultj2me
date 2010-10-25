/*
 * Created on Apr 15, 2006
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package vault.dto;

import java.util.Vector;

import javax.microedition.pim.Contact;

import vault.app.ApplicationAssistant;
import vault.util.Constants;
//import javax.microedition.pim.PIMItem;

/**
 * @author Deepak
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class ContactDTO {
	private	long checksum = -1;
	private	String contactid = "";
	private int changetype;

	//private long timestamp;
    private String firstname = "";
    private String lastname = "";
    private String companyname = "";
    private String globalid = "";
	
    private Vector jobtitles = new Vector();

	private Vector mobiles = new Vector();
	//private String[] homemobiles;
	//private String[] workmobiles;

	private Vector telephones = new Vector();
	private Vector hometelephones = new Vector();
	private Vector worktelephones = new Vector();

	private Vector emails = new Vector();
	private Vector homeemails = new Vector();
	private Vector workemails = new Vector();

	private Vector faxes = new Vector();
	//private String[] homefaxes;
	//private String[] workfaxes;

	private class AddressField{
		public String country = "";
		public String extra = "";
		public String locality = "";
		public String pobox = "";
		public String postalcode = "";
		public String region = "";
		public String street = "";		
	}
	
	private AddressField address;
	private long birthday;
	private String nickname;
	private String note;
	private String url;
	
	public long getCheckSumString()
	{
		StringBuffer checksumstring = new StringBuffer();
		
		if(checksum == -1)
		{
			if(Constants.debug) System.out.println("ContactDTO.getCheckSumString1");
			
			checksumstring.append(firstname);
			checksumstring.append("#");
			checksumstring.append(lastname);
			checksumstring.append("#");
			checksumstring.append(companyname);
			checksumstring.append("#");
						
			if(Constants.debug) System.out.println("ContactDTO.getCheckSumString2");
						
			for(int i = 0; i< jobtitles.size() ;++i){
				checksumstring.append((String)jobtitles.elementAt(i));
				checksumstring.append("#");
			}
						
			if(Constants.debug) System.out.println("ContactDTO.getCheckSumString3");
						
			for(int i = 0; i< mobiles.size() ;++i){
				checksumstring.append((String)mobiles.elementAt(i));
				checksumstring.append("#");
			}
						
			if(Constants.debug) System.out.println("ContactDTO.getCheckSumString4");
			
			for(int i = 0; i< telephones.size() ;++i){
				checksumstring.append((String)telephones.elementAt(i));
				checksumstring.append("#");
			}
						
			if(Constants.debug) System.out.println("ContactDTO.getCheckSumString5");
						
			for(int i = 0; i< hometelephones.size() ;++i){
				checksumstring.append((String)hometelephones.elementAt(i));
				checksumstring.append("#");
			}
						
			if(Constants.debug) System.out.println("ContactDTO.getCheckSumString6");
			
			for(int i = 0; i< worktelephones.size() ;++i){
				checksumstring.append((String)worktelephones.elementAt(i));
				checksumstring.append("#");
			}
						
			if(Constants.debug) System.out.println("ContactDTO.getCheckSumString7");
						
			for(int i = 0; i< emails.size() ;++i){
				checksumstring.append((String)emails.elementAt(i));
				checksumstring.append("#");
			}
						
			if(Constants.debug) System.out.println("ContactDTO.getCheckSumString8");
						
			for(int i = 0; i< hometelephones.size() ;++i){
				checksumstring.append((String)hometelephones.elementAt(i));
				checksumstring.append("#");
			}
						
			if(Constants.debug) System.out.println("ContactDTO.getCheckSumString9");
						
			for(int i = 0; i< worktelephones.size() ;++i){
				checksumstring.append((String)worktelephones.elementAt(i));
				checksumstring.append("#");
			}
						
			if(Constants.debug) System.out.println("ContactDTO.getCheckSumString10");
						
			for(int i = 0; i< faxes.size() ;++i){
				checksumstring.append((String)faxes.elementAt(i));
				checksumstring.append("#");
			}

			checksumstring.append(address.country);
			checksumstring.append("#");
			checksumstring.append(address.extra);
			checksumstring.append("#");
			checksumstring.append(address.locality);
			checksumstring.append("#");
			checksumstring.append(address.pobox);
			checksumstring.append("#");
			checksumstring.append(address.postalcode);
			checksumstring.append("#");
			checksumstring.append(address.region);
			checksumstring.append("#");
			checksumstring.append(address.street);
			checksumstring.append("#");
						
			checksumstring.append(birthday);
			checksumstring.append("#");
			checksumstring.append(nickname);
			checksumstring.append("#");
			checksumstring.append(note);
			checksumstring.append("#");
			checksumstring.append(url);
			checksumstring.append("#");
			
			if(Constants.debug) System.out.println("ContactDTO.getCheckSumString11");
			
			if(Constants.partialdebug) System.out.println("ContactDTO.getCheckSumString8 " + checksumstring.toString());
			
			checksum = ApplicationAssistant.getCheckSumField(checksumstring.toString());
			checksumstring = null;
			ApplicationAssistant.gc();
			
			return checksum;
		}
		else
			return checksum;
	}
	
	public ContactDTO()
	{
		
	}
	public ContactDTO(Contact contact, int type)
	{		
		changetype = type;
		int[] fields = contact.getFields();
//		PIMList pimlist = contact.getPIMList();
//		int[] fields = pimlist.getSupportedFields();
		for(int k=0;k<fields.length;++k)
		{
			switch(fields[k])
			{
				case Contact.UID:
				{
					if(Constants.partialdebug) System.out.println("\nContactDTO UID found");
					try
					{
						contactid = contact.getString(Contact.UID, 0);
					}
					catch(Exception e)
					{}
					break;
				}
				case Contact.ORG:
				{
					if(Constants.partialdebug) System.out.println("\nContactDTO ORG found");
					try
					{
						companyname = contact.getString(Contact.ORG, 0);
					}
					catch(Exception e)
					{}

					if(companyname == null)
						companyname = "";
				
					break;
				}
				case Contact.TITLE:
				{
					if(Constants.partialdebug) System.out.println("\nContactDTO TITLE found");
					
					//jobtitles = new String[1];//contact.countValues(Contact.TITLE)
					try
					{
						jobtitles.addElement(contact.getString(Contact.TITLE, 0));
					}
					catch(Exception e)
					{}
					break;
				}
				case Contact.TEL:
				{
					if(Constants.partialdebug) System.out.println("\nContactDTO TEL found");
					int phoneAttributes = contact.countValues(Contact.TEL);
//					int [] phoneAttributes = pimlist.getSupportedAttributes(Contact.TEL);
					if(Constants.partialdebug) System.out.println("\nContactDTO TEL found. counts = " + phoneAttributes);
//					if(Constants.partialdebug) System.out.println("\nContactDTO TEL found. counts = " + phoneAttributes.length);
					if(phoneAttributes==0)
//					if(phoneAttributes.length==0)
					{
						if(Constants.partialdebug) System.out.println("\nContactDTO TEL found getAttributes = DEFAULT 0");
						try
						{
							telephones.addElement(contact.getString(Contact.TEL, 0));
						}
						catch(Exception e)
						{}
					}
					
					for(int i = 0; i < phoneAttributes; i++) {
//					for(int i = 0; i < phoneAttributes.length; i++) {
						if(Constants.partialdebug) System.out.println("\nContactDTO TEL found index = " + phoneAttributes);
//						if(Constants.partialdebug) System.out.println("\nContactDTO TEL found index = " + phoneAttributes[i]);
						
						int attrib = contact.getAttributes(Contact.TEL,i);
						if(Constants.partialdebug) System.out.println("\nContactDTO TEL found getAttributes = " + attrib);
						if(Constants.debug) System.out.println("\nContactDTO TEL found Contact.ATTR_HOME = " + Contact.ATTR_HOME);
						if(Constants.debug) System.out.println("\nContactDTO TEL found Contact.ATTR_FAX = " + Contact.ATTR_FAX);
						if(Constants.debug) System.out.println("\nContactDTO TEL found Contact.ATTR_MOBILE = " + Contact.ATTR_MOBILE);
						if(Constants.debug) System.out.println("\nContactDTO TEL found Contact.ATTR_WORK = " + Contact.ATTR_WORK);
						if(Constants.debug) System.out.println("\nContactDTO TEL found Contact.ATTR_NONE = " + Contact.ATTR_NONE);
						if(Constants.debug) System.out.println("\nContactDTO TEL found Contact.ATTR_OTHER = " + Contact.ATTR_OTHER);
						if(Constants.debug) System.out.println("\nContactDTO TEL found Contact.ATTR_AUTO = " + Contact.ATTR_AUTO);
						if(Constants.debug) System.out.println("\nContactDTO TEL found Contact.ATTR_PREFERRED = " + Contact.ATTR_PREFERRED);
						if(Constants.debug) System.out.println("\nContactDTO TEL found Contact.ATTR_ASST = " + Contact.ATTR_ASST);
						
						if(attrib == Contact.ATTR_FAX || attrib == (Contact.ATTR_PREFERRED + Contact.ATTR_FAX))
						{
							try
							{
								faxes.addElement(contact.getString(Contact.TEL, i));									
							}
							catch(Exception e)
							{}
						}
						else if(attrib == Contact.ATTR_MOBILE || attrib == (Contact.ATTR_PREFERRED + Contact.ATTR_MOBILE))
						{
							try
							{
								mobiles.addElement(contact.getString(Contact.TEL, i));															
							}
							catch(Exception e)
							{}
						}
						else if(attrib == Contact.ATTR_WORK || attrib == (Contact.ATTR_PREFERRED + Contact.ATTR_WORK))
						{
							try
							{
								worktelephones.addElement(contact.getString(Contact.TEL, i));																							
							}
							catch(Exception e)
							{}
						}
						else if(attrib == Contact.ATTR_NONE || attrib == (Contact.ATTR_PREFERRED + Contact.ATTR_NONE))
						{
							try
							{
								telephones.addElement(contact.getString(Contact.TEL, i));																								
							}
							catch(Exception e)
							{}
						}
						else if(attrib == Contact.ATTR_OTHER || attrib == (Contact.ATTR_PREFERRED + Contact.ATTR_OTHER))
						{
							try
							{
								telephones.addElement(contact.getString(Contact.TEL, i));																								
							}
							catch(Exception e)
							{}
						}
						else if(attrib == Contact.ATTR_AUTO || attrib == (Contact.ATTR_PREFERRED + Contact.ATTR_AUTO))
						{
							try
							{
								telephones.addElement(contact.getString(Contact.TEL, i));																								
							}
							catch(Exception e)
							{}
						}
						else if(attrib == Contact.ATTR_PREFERRED)
						{
							try
							{
								telephones.addElement(contact.getString(Contact.TEL, i));																								
							}
							catch(Exception e)
							{}
						}
						else if(attrib == Contact.ATTR_ASST || attrib == (Contact.ATTR_PREFERRED + Contact.ATTR_ASST))
						{
							try
							{
								telephones.addElement(contact.getString(Contact.TEL, i));																								
							}
							catch(Exception e)
							{}
						}
						else if(attrib == Contact.ATTR_HOME || attrib == (Contact.ATTR_PREFERRED + Contact.ATTR_HOME))
						{
							try
							{
								hometelephones.addElement(contact.getString(Contact.TEL, i));
							}
							catch(Exception e)
							{}
						}
					}
					break;
				}
				case Contact.EMAIL:
				{
					if(Constants.partialdebug) System.out.println("\nContactDTO EMAIL found");
					int emailAttributes = contact.countValues(Contact.EMAIL);
//					int [] emailAttributes = pimlist.getSupportedAttributes(Contact.EMAIL);
					if(Constants.partialdebug) System.out.println("\nContactDTO EMAIL found. counts = " + emailAttributes);
//					if(Constants.partialdebug) System.out.println("\nContactDTO EMAIL found. counts = " + emailAttributes.length);
					if(emailAttributes==0)
//					if(emailAttributes.length==0)
					{
						if(Constants.partialdebug) System.out.println("\nContactDTO EMAIL found getAttributes = DEFAULT 0");
						try
						{
							emails.addElement(contact.getString(Contact.EMAIL, 0));
						}
						catch(Exception e)
						{}
					}
					
					for(int i = 0; i < emailAttributes; i++) {
//					for(int i = 0; i < emailAttributes.length; i++) {
						int attrib = contact.getAttributes(Contact.EMAIL,i);
						if(Constants.partialdebug) System.out.println("\nContactDTO EMAIL found getAttributes = " + attrib);
						
						if(Constants.debug) System.out.println("\nContactDTO EMAIL found Contact.ATTR_WORK = " + Contact.ATTR_WORK);
						if(Constants.debug) System.out.println("\nContactDTO EMAIL found Contact.ATTR_HOME = " + Contact.ATTR_HOME);
						if(Constants.debug) System.out.println("\nContactDTO EMAIL found Contact.ATTR_NONE = " + Contact.ATTR_NONE);
						if(Constants.debug) System.out.println("\nContactDTO EMAIL found Contact.ATTR_OTHER = " + Contact.ATTR_OTHER);
						if(Constants.debug) System.out.println("\nContactDTO EMAIL found Contact.ATTR_AUTO = " + Contact.ATTR_AUTO);
						if(Constants.debug) System.out.println("\nContactDTO EMAIL found Contact.ATTR_PREFERRED = " + Contact.ATTR_PREFERRED);
						
						if(attrib == Contact.ATTR_HOME || attrib == (Contact.ATTR_PREFERRED + Contact.ATTR_HOME))
						{
							try
							{
								homeemails.addElement(contact.getString(Contact.EMAIL, i));					
							}
							catch(Exception e)
							{}
						}
						else if(attrib == Contact.ATTR_WORK || attrib == (Contact.ATTR_PREFERRED + Contact.ATTR_WORK))
						{
							try
							{
								workemails.addElement(contact.getString(Contact.EMAIL, i));
							}
							catch(Exception e)
							{}
						}
						else if(attrib == Contact.ATTR_NONE || attrib == (Contact.ATTR_PREFERRED + Contact.ATTR_NONE))
						{
							try
							{
								emails.addElement(contact.getString(Contact.EMAIL, i));								
							}
							catch(Exception e)
							{}
						}
						else if(attrib == Contact.ATTR_OTHER || attrib == (Contact.ATTR_PREFERRED + Contact.ATTR_OTHER))
						{
							try
							{
								emails.addElement(contact.getString(Contact.EMAIL, i));
							}
							catch(Exception e)
							{}
						}
						else if(attrib == Contact.ATTR_AUTO || attrib == (Contact.ATTR_PREFERRED + Contact.ATTR_AUTO))
						{
							try
							{
								emails.addElement(contact.getString(Contact.EMAIL, i));
							}
							catch(Exception e)
							{}
						}
						else if(attrib == Contact.ATTR_PREFERRED)
						{
							try
							{
								telephones.addElement(contact.getString(Contact.EMAIL, i));
							}
							catch(Exception e)
							{}
						}						
					}						
//					contactid = contact.getString(Contact.UID, 0);
					break;
				}
				//case Contact.:
					//contactid = contact.getString(Contact.UID, 0);
					//break;
				case Contact.FORMATTED_NAME:
				{
					if(Constants.partialdebug) System.out.print("NAME_FORMATTED FOUND");
					if(firstname == "" && lastname == "")
					{
						firstname = contact.getString(Contact.FORMATTED_NAME, 0);
						if(firstname == null)
							firstname = "";
						
						lastname = "";
					}
					break;
				}
				case Contact.NAME:
				{
					if(Constants.partialdebug) System.out.print("NAME FIELD FOUND");
					//if(firstname == "" && lastname == "")
					//{
						String[] n = contact.getStringArray(Contact.NAME, 0);
						firstname = n[Contact.NAME_GIVEN];
						lastname = n[Contact.NAME_FAMILY];
						
						if(firstname == null)
							firstname = "";
						if(lastname == null)
							lastname = "";
						
					//}
					break;
				}
				case Contact.ADDR:
				{
					if(Constants.partialdebug) System.out.println("\nContactDTO ADDR found");
					try
					{
						String[] addr = contact.getStringArray(Contact.ADDR, 0);
						
						address.country = addr[Contact.ADDR_COUNTRY];
						address.extra = addr[Contact.ADDR_EXTRA];
						address.locality = addr[Contact.ADDR_LOCALITY];
						address.pobox = addr[Contact.ADDR_POBOX];
						address.postalcode = addr[Contact.ADDR_POSTALCODE];
						address.region = addr[Contact.ADDR_REGION];
						address.street = addr[Contact.ADDR_STREET];
						
						if(address.country == null)
							address.country = "";
						if(address.extra == null)
							address.extra = "";
						if(address.locality == null)
							address.locality = "";
						if(address.pobox == null)
							address.pobox = "";
						if(address.postalcode == null)
							address.postalcode = "";
						if(address.region == null)
							address.region = "";
						if(address.street == null)
							address.street = "";
						
					}
					catch(Exception e)
					{}					
					break;
				}
				case Contact.BIRTHDAY:
				{
					if(Constants.partialdebug) System.out.println("\nContactDTO BIRTHDAY found");
					try
					{
						birthday = contact.getDate(Contact.BIRTHDAY, 0);
					}
					catch(Exception e)
					{}					
					break;
				}
				case Contact.NICKNAME:
				{
					if(Constants.partialdebug) System.out.println("\nContactDTO NICKNAME found");
					try
					{
						nickname = contact.getString(Contact.NICKNAME, 0);
					}
					catch(Exception e)
					{}					
					break;
				}
				case Contact.NOTE:
				{
					if(Constants.partialdebug) System.out.println("\nContactDTO NOTE found");
					try
					{
						note = contact.getString(Contact.NOTE, 0);
					}
					catch(Exception e)
					{}					
					break;
				}
				case Contact.URL:
				{
					if(Constants.partialdebug) System.out.println("\nContactDTO URL found");
					try
					{
						url = contact.getString(Contact.URL, 0);
					}
					catch(Exception e)
					{}					
					break;
				}				
			    default:
			    	break;
			}
			
		}	    
	}

	
	/**
	 * @return Returns the changetype.
	 */
	public int getChangetype() {
		return changetype;
	}
	/**
	 * @param changetype The changetype to set.
	 */
	public void setChangetype(int changetype) {
		this.changetype = changetype;
	}
	/**
	 * @return Returns the companyname.
	 */
	public String getCompanyname() {
		return companyname;
	}
	/**
	 * @param companyname The companyname to set.
	 */
	public void setCompanyname(String companyname) {
		this.companyname = companyname;
	}
	/**
	 * @return Returns the contactid.
	 */
	public String getContactid() {
		return contactid;
	}
	/**
	 * @param contactid The contactid to set.
	 */
	public void setContactid(String contactid) {
		this.contactid = contactid;
	}
	/**
	 * @return Returns the emails.
	 */
	public Vector getEmails() {
		return emails;
	}
	/**
	 * @param emails The emails to set.
	 */
	public void addEmails(String emails) {
		this.emails.addElement(emails);
	}	
	/**
	 * @return Returns the faxes.
	 */
	public Vector getFaxes() {
		return faxes;
	}
	/**
	 * @param faxes The faxes to set.
	 */
	public void addFaxes(String faxes) {
		this.faxes.addElement(faxes);
	}	
	/**
	 * @return Returns the firstname.
	 */
	public String getFirstname() {
		return firstname;
	}
	/**
	 * @param firstname The firstname to set.
	 */
	public void setFirstname(String firstname) {
		this.firstname = firstname;
	}
	/**
	 * @return Returns the globalid.
	 */
	public String getGlobalid() {
		return globalid;
	}
	/**
	 * @param globalid The globalid to set.
	 */
	public void setGlobalid(String globalid) {
		this.globalid = globalid;
	}
	/**
	 * @return Returns the homeemails.
	 */
	public Vector getHomeemails() {
		return homeemails;
	}
	/**
	 * @param homeemails The homeemails to set.
	 */
	public void addHomeemails(String homeemails) {
		this.homeemails.addElement(homeemails);
	}	
	/**
	 * @return Returns the hometelephones.
	 */
	public Vector getHometelephones() {
		return hometelephones;
	}
	/**
	 * @param hometelephones The hometelephones to set.
	 */
	public void addHometelephones(String hometelephones) {
		this.hometelephones.addElement(hometelephones);
	}		
	/**
	 * @return Returns the jobtitles.
	 */
	public Vector getJobtitles() {
		return jobtitles;
	}
	/**
	 * @param jobtitles The jobtitles to set.
	 */
	public void addJobtitles(String jobtitles) {
		this.jobtitles.addElement(jobtitles);
	}	
	/**
	 * @return Returns the lastname.
	 */
	public String getLastname() {
		return lastname;
	}
	/**
	 * @param lastname The lastname to set.
	 */
	public void setLastname(String lastname) {
		this.lastname = lastname;
	}
	/**
	 * @return Returns the mobiles.
	 */
	public Vector getMobiles() {
		return mobiles;
	}
	/**
	 * @param mobiles The mobiles to set.
	 */
	public void addMobiles(String mobiles) {
		this.mobiles.addElement(mobiles);
	}		
	/**
	 * @return Returns the telephones.
	 */
	public Vector getTelephones() {
		return telephones;
	}
	/**
	 * @param telephones The telephones to set.
	 */
	public void addTelephones(String telephones) {
		this.telephones.addElement(telephones);
	}	
	/**
	 * @return Returns the timestamp.
	 */
	public long getTimestamp() {
		//return timestamp;
		return System.currentTimeMillis();
	}
	/**
	 * @param timestamp The timestamp to set.
	 */
//	public void setTimestamp(long timestamp) {
//		this.timestamp = timestamp;
//	}
	/**
	 * @return Returns the workemails.
	 */
	public Vector getWorkemails() {
		return workemails;
	}
	/**
	 * @param workemails The workemails to set.
	 */
	public void addWorkemails(String workemails) {
		this.workemails.addElement(workemails);
	}		
	/**
	 * @return Returns the worktelephones.
	 */
	public Vector getWorktelephones() {
		return worktelephones;
	}
	/**
	 * @param worktelephones The worktelephones to set.
	 */
	public void addWorktelephones(String worktelephones) {
		this.worktelephones.addElement(worktelephones);
	}

	public String getAddressCountry() {
		return address.country;
	}

	public void setAddressCountry(String country) {
		this.address.country = country;
	}
	
	public String getAddressExtra() {
		return address.extra;
	}

	public void setAddressExtra(String extra) {
		this.address.extra = extra;
	}
	
	public String getAddressLocality() {
		return address.locality;
	}
	
	public void setAddressLocality(String locality) {
		this.address.locality = locality;
	}
	
	public String getAddressPobox() {
		return address.pobox;
	}
	
	public void setAddressPobox(String pobox) {
		this.address.pobox = pobox;
	}
	
	public String getAddressPostalcode() {
		return address.postalcode;
	}
	
	public void setAddressPostalCode(String postalcode) {
		this.address.postalcode = postalcode;
	}
	
	public String getAddressRegion() {
		return address.region;
	}
	
	public void setAddressRegion(String region) {
		this.address.region = region;
	}
	
	public String getAddressStreet() {
		return address.street;
	}
	
	public void setAddressStreet(String street) {
		this.address.street = street;
	}
	/*
	public void setAddress(String address) {
		Vector add = new Vector();
		
		while(true)
		{
			
			address.startsWith("|C1|")
			buffer.append("|C1|");
			buffer.append(items[Contact.ADDR_COUNTRY]);
			buffer.append("|E2|");
			buffer.append(items[Contact.ADDR_EXTRA]);
			buffer.append("|L3|");
			buffer.append(items[Contact.ADDR_LOCALITY]);
			buffer.append("|P4|");
			buffer.append(items[Contact.ADDR_POBOX]);
			buffer.append("|P5|");
			buffer.append(items[Contact.ADDR_POSTALCODE]);
			buffer.append("|R6|");
			buffer.append(items[Contact.ADDR_REGION]);
			buffer.append("|S7|");
			buffer.append(items[Contact.ADDR_STREET]);
			
			int i = address.indexOf("#-#");
			
			if(i < 0)
				break;
			else
			{
				add.addElement(address.substring(0,i));
				address = address.substring(i + 3);
			}
		}
		if(add.size() > 0)
			this.address = ApplicationAssistant.vectorToString(add);
	} */

	public long getBirthday() {
		return birthday;
	}

	public void setBirthday(String birthday) {
		this.birthday = Long.parseLong(birthday);
	}

	public String getNickname() {
		return nickname;
	}

	public void setNickname(String nickname) {
		this.nickname = nickname;
	}

	public String getNote() {
		return note;
	}

	public void setNote(String note) {
		this.note = note;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}	
}
