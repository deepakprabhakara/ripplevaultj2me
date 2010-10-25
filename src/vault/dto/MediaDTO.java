/*
 * Created on Apr 15, 2006
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package vault.dto;

import java.util.Calendar;
import java.util.Date;

import vault.util.Comparable;
import vault.util.Constants;

/**
 * @author Deepak
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class MediaDTO extends Comparable
{
	private String filename;
	private int mediatype;
	private long timestamp;
	private String title;
	private boolean selected;

	//TODO i have not used these yet, but i think they wud be required in logic & when publishing
	//as and when u use it, modify the constructor and also the getters to include these
	private byte[] status = null;
	private String albumname;
	private String description;
	
	private int queuestatus = Constants.MEDIAQUEUESTATUS_NEW;
	
	public int getQueuestatus() {
		return queuestatus;
	}

	public void setQueuestatus(int queuestatus) {
		this.queuestatus = queuestatus;
	}

	public MediaDTO(String filename, int mediatype, long timestamp, String title)
	{
		this.filename = filename;
		this.mediatype = mediatype;
		this.timestamp = timestamp;
		this.title = title;
		this.selected = false;
	}
	
	public int compareTo(Comparable c)
	{
		if(Constants.partialdebug) System.out.println("MediaDTO.compareTo()");
		
		MediaDTO cldto = (MediaDTO) c;
		
		try{
			if(Constants.partialdebug) System.out.println("MediaDTO.compareTo() try" + parentlabel);
			
			Long.parseLong(parentlabel);
			
			if (this.timestamp > cldto.getTimestamp())
				return 1;
			else if (this.timestamp < cldto.getTimestamp())
				return -1;
			else
				return 0;			
		}
		catch(NumberFormatException nfe)
		{
			if(Constants.partialdebug) System.out.println("MediaDTO.compareTo() catch");
			
			if (this.filename.compareTo(cldto.getFilename()) > 0)
				return 1;
			else if (this.filename.compareTo(cldto.getFilename()) < 0)
				return -1;
			else
				return 0;
		}		
	}
	
	/**
	 * @return Returns the filename.
	 */
	public String getFilename() {
		return filename;
	}
	/**
	 * @return Returns the type.
	 */
	public int getMediatype() {
		return mediatype;
	}
	/**
	 * @return Returns the timestamp.
	 */
	public long getTimestamp() {
		return timestamp;
	}
	/**
	 * @return Returns the timestamp.
	 */
	public String getTimestampAsString() {
		StringBuffer ret = new StringBuffer();
		Calendar cal = Calendar.getInstance();
		cal.setTime(new Date(this.timestamp));
		int val = cal.get(Calendar.DAY_OF_MONTH);
		ret.append(((val<10)?"0"+val:""+val));
		ret.append("/");
		val = cal.get(Calendar.MONTH)+1;
		ret.append(((val<10)?"0"+val:""+val));
		ret.append("/");
		val = cal.get(Calendar.YEAR);
		ret.append(((val<10)?"0"+val:""+val));
		ret.append(" - ");
		val = cal.get(Calendar.HOUR_OF_DAY);
		ret.append(((val<10)?"0"+val:""+val));
		ret.append(":");
		val = cal.get(Calendar.MINUTE);
		ret.append(((val<10)?"0"+val:""+val));
		return ret.toString();
	}
	
	/**
	 * @return Returns the title.
	 */
	public String getTitle() {
		return title;
	}

	/**
	 * @return Returns the selected.
	 */
	public boolean isSelected() {
		return selected;
	}
	
	public void toggleSelected() {
		selected = !selected;
	}
	
	/**
	 * @return Returns the status.
	 */
	public byte[] getStatus() {
		return status;
	}
	/**
	 * @param status The status to set.
	 */
	public void setStatus(byte[] status) {
		this.status = status;
	}
	
}
