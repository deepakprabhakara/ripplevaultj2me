/*
 * Created on Apr 15, 2006
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package vault.dto;

import vault.util.Comparable;

/**
 * @author Deepak
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class ContactMappingDTO extends Comparable{
	private	String contactid = "";
	private	String globalid = "";
	private int type;
	private String checksum = "";
	
	public ContactMappingDTO(String contactid, String globalid, String type)
	{
		this.contactid = contactid;
		this.globalid = globalid;
		this.type = Integer.parseInt(type);
	}

	public int compareTo(Comparable c)
	{
		ContactMappingDTO cldto = (ContactMappingDTO) c;
		if (this.contactid.compareTo(cldto.getContactid()) > 0)
			return 1;
		else if (this.contactid.compareTo(cldto.getContactid()) < 0)
			return -1;
		else
			return 0;
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
	 * @return Returns the type.
	 */
	public int getType() {
		return type;
	}
	/**
	 * @param type The type to set.
	 */
	public void setType(int type) {
		this.type = type;
	}
	/**
	 * @return Returns the checksum.
	 */
	public String getChecksum() {
		return checksum;
	}
	/**
	 * @param checksum The checksum to set.
	 */
	public void setChecksum(String checksum) {
		this.checksum = checksum;
	}
}
