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
public class ContactLocalDTO extends Comparable{
	private	String contactid = "";
	private	String checksum = "";
	
	public ContactLocalDTO(String contactid, String checksum)
	{
		this.contactid = contactid;
		this.checksum = checksum;
	}
	
	public int compareTo(Comparable c)
	{
		ContactLocalDTO cldto = (ContactLocalDTO) c;
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
	public String getChecksum() {
		return checksum;
	}
	/**
	 * @param globalid The globalid to set.
	 */
	public void setChecksum(String checksum) {
		this.checksum = checksum;
	}
}
