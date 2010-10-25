package vault.db;
import java.util.Hashtable;
import java.util.Vector;

import javax.microedition.rms.RecordEnumeration;
import javax.microedition.rms.RecordStore;
import javax.microedition.rms.RecordStoreNotFoundException;

import vault.app.ApplicationAssistant;
import vault.util.Constants;


/*
* CONTACTSYNCHMAPS Store
* column1 : String luid, String guid, String mapsoperation
* 
* CONTACTS Store
* column1 : String luid, String checksum
*  
* Config Store
* column1 : String pk, String value
* 
* contents:
* host
* port
* contacts_url
* registration_url
* media_pkt_upload_url
* media_gallery_url
* sub_status_check
* phone_model
* encodedhp
* userid
* password
* appid
* version
*   
* Counts Store
* column1 : String item, String count
* 
* contents:
* contacts
* photos
* videos
**/

public class DBManager
{
	/*
	 * Database Manager (optimise and simplify rms access)
	 * 
	 * Each database has columns, and each columns have fields.
	 * All fields in a column have their field lengths mentioned before the start of the field data
	 * Each column is stored in a rms record
	 * Each database record is a tuple of as many rms records as are the number of columns
	 *
	 * if u r wondering why not just one column with delimited fields, read on:
	 * the rms access speeds for a device is for one record. 
	 * each record in rms is given a space in multiples of 16 octets, 
	 * so 4 fields of 4 octets each put together in one column makes things 4 times faster,
	 * while it would slow down things to put two fields of, say 20 octets each 
	 * (so put those kinda fields in diff columns) 
	 */
	
	private Vector columns; //vector of string [] s
	private RecordStore rs;
	private String rsname;
	private int numfields;
	
	public DBManager(Vector columns, String dbname)
	{
		this.columns = columns;
		this.rsname = dbname;
		numfields = 0;
		for(int i=0; i<columns.size(); i++)
			numfields += ((String []) columns.elementAt(i)).length;
	}
	
	public String open() throws Exception
	{
		if(Constants.debug)	System.out.println("Opening recordstore : " + this.rsname);
		try
		{
			this.rs = RecordStore.openRecordStore(this.rsname, false);
			return Constants.SUCCESS;
		}
		catch (RecordStoreNotFoundException e)
		{
			this.rs = RecordStore.openRecordStore(this.rsname, true);
			return Constants.SUCCESSCONDITIONAL;
		}
	}
	
	public void close() throws Exception
	{
		if(Constants.debug)	System.out.println("Close recordstore : " + this.rsname);
		this.rs.closeRecordStore();
	}

	public Hashtable[] findAll() throws Exception
	{
		RecordEnumeration re = this.rs.enumerateRecords(null, null, false);
		Vector result = new Vector();
		while (re.hasNextElement())
		{
			int i = re.nextRecordId();
			if (columns.size()==1 || i % columns.size() == 1) 
			//if number of columns is 1 or if the current column is the one with pk (i.e first column of a tuple)
			{
				Hashtable ht = new Hashtable();
				for (int j = 0; j < columns.size(); j++)
				{
					byte[] tmp = this.rs.getRecord(i + j);
					String [] fields = (String []) columns.elementAt(j);
					if (tmp == null)
					{
						for(int k=0; k<fields.length; k++)
							ht.put(fields[k], new byte[0]);
					}
					else
					{
						Vector tokens = decodeColumn(tmp);
						for(int k=0; k<fields.length; k++)
							ht.put(fields[k], (byte []) tokens.elementAt(k));
					}
				}
				ht.put("ID", i+"");
				result.addElement(ht);
			}
		}
		Hashtable htresult[] = new Hashtable[result.size()];
		for (int i = 0; i < htresult.length; i++)
			htresult[i] = (Hashtable) result.elementAt(i);
		result = null;
		ApplicationAssistant.gc();
		return htresult;
	}

	public Hashtable[] findByField(String fieldname, String fieldvalue) throws Exception
	{
		int [] column_and_field_index = this.getColumnAndFieldIndex(fieldname);
		int columnindex = column_and_field_index[0];
		int fieldindex = column_and_field_index[1];
		if(Constants.debug) System.out.println("**** column index " + columnindex);
		if(Constants.debug) System.out.println("**** field index " + fieldindex);
		RecordEnumeration re = this.rs.enumerateRecords(null, null, false);
		Vector result = new Vector();
		while (re.hasNextElement())
		{
			int i = re.nextRecordId();
			if (columns.size()==1 || i % columns.size() == 1) 
			//if number of columns is 1 or if the current column is the one with pk (i.e first column of a tuple)
			{
				if(Constants.debug) System.out.println("**** pri key record num " + i);
				boolean satisfied = true;
				String fielddata = "";
				byte[] tmp = this.rs.getRecord(i + columnindex);
				if(Constants.debug) System.out.println("**** target record " + (i+columnindex));
				if (tmp != null)
				{
					Vector tokens = decodeColumn(tmp);
					tmp = (byte []) tokens.elementAt(fieldindex);
					fielddata = new String(tmp);
					if(Constants.debug) System.out.println("**** data read " + fielddata);
				}
				else
				{
					satisfied = false;
				}
				if (!fielddata.equals(fieldvalue))
				{
					satisfied = false;
				}
				if (satisfied)
				{
					if(Constants.debug) System.out.print("CORRECT");
					Hashtable ht = new Hashtable();
					for (int j = 0; j < columns.size(); j++)
					{
						byte[] temp = this.rs.getRecord(i + j);
						String [] fields = (String []) columns.elementAt(j);
						if (temp == null)
						{
							for(int k=0; k<fields.length; k++)
								ht.put(fields[k], new byte[0]);
						}
						else
						{
							Vector tokens = decodeColumn(temp);
							for(int k=0; k<fields.length; k++)
								ht.put(fields[k], (byte []) tokens.elementAt(k));
						}
					}
					ht.put("ID", i+"");
					result.addElement(ht);
				}
			}
		}
		Hashtable htresult[] = new Hashtable[result.size()];
		for (int i = 0; i < htresult.length; i++)
			htresult[i] = (Hashtable) result.elementAt(i);
		result = null;
		ApplicationAssistant.gc();
		return htresult;
	}
	
	public Hashtable findByPrimaryKey(String pkvalue) throws Exception
	{
		RecordEnumeration re = this.rs.enumerateRecords(null, null, false);
		Hashtable ht = null;
		while (re.hasNextElement())
		{
			int i = re.nextRecordId();
			if (columns.size()==1 || i % columns.size() == 1) 
			//if number of columns is 1 or if the current column is the one with pk (i.e first column of a tuple)
			{
				if(Constants.debug) System.out.println("**** pri key record num " + i);
				boolean satisfied = true;
				String fielddata = "";
				byte[] tmp = this.rs.getRecord(i);
				if(Constants.debug) System.out.println("**** target record " + i);
				if (tmp != null)
				{
					Vector tokens = decodeColumn(tmp);
					tmp = (byte []) tokens.elementAt(0);
					fielddata = new String(tmp);
					if(Constants.debug) System.out.println("**** data read " + fielddata);
				}
				else
				{
					satisfied = false;
				}
				if (!fielddata.equals(pkvalue))
				{
					satisfied = false;
				}
				if (satisfied)
				{
					if(Constants.debug) System.out.print("CORRECT");
					ht = new Hashtable();
					for (int j = 0; j < columns.size(); j++)
					{
						byte[] temp = this.rs.getRecord(i + j);
						String [] fields = (String []) columns.elementAt(j);
						if (temp == null)
						{
							for(int k=0; k<fields.length; k++)
								ht.put(fields[k], new byte[0]);
						}
						else
						{
							Vector tokens = decodeColumn(temp);
							for(int k=0; k<fields.length; k++)
								ht.put(fields[k], (byte []) tokens.elementAt(k));
						}
					}
					ht.put("ID", i+"");
					break;
				}
			}
		}
		ApplicationAssistant.gc();
		return ht;
	}
	
	public Hashtable findByRecordId(int recordid) throws Exception
	{
		Hashtable ht = new Hashtable();
		for (int j = 0; j < columns.size(); j++)
		{
			byte[] temp = this.rs.getRecord(recordid + j);
			String [] fields = (String []) columns.elementAt(j);
			if (temp == null)
			{
				for(int k=0; k<fields.length; k++)
					ht.put(fields[k], new byte[0]);
			}
			else
			{
				Vector tokens = decodeColumn(temp);
				for(int k=0; k<fields.length; k++)
					ht.put(fields[k], (byte []) tokens.elementAt(k));
			}
		}
		ht.put("ID", recordid+"");
		return ht;
	}
	
	public int createDBRecord(byte[][] fields) throws Exception
	{
		int recordid = -1;
		if(Constants.debug) System.out.println("number of records*fields : " + fields.length);
		int numfields = 0;
		for(int i=0; i<this.columns.size(); i++)
			numfields +=((String []) this.columns.elementAt(i)).length;
		if(Constants.debug) System.out.println("number of fields : " + numfields);
		if ((fields.length % numfields) != 0)
			return recordid;
		if(Constants.debug) System.out.print("DB Size before : " + this.rs.getSize());
		int i=0;
		while(i < fields.length)
		{
			for(int j=0; j<this.columns.size(); j++)
			{
				Vector tokens = new Vector();
				for(int k=0; k<((String []) this.columns.elementAt(j)).length; k++)
				{
					tokens.addElement(fields[i]);
					++i;
				}
				byte [] data = encodeColumn(tokens);		
				int ret = this.rs.addRecord(data, 0, data.length);
				if(j==0)
					recordid = ret;
			}
		}
		if(Constants.debug) System.out.println(" and after : " + this.rs.getSize());
		return recordid;
	}
	
	public boolean delete(String pk) throws Exception
	{
		int recordnum = getRecordId(pk);
		if (recordnum < 0)
			return false;
		return delete(recordnum);
	}
	
	public boolean delete(int recordid) throws Exception
	{
		for (int i = 0; i < this.columns.size(); i++)
			this.rs.deleteRecord(recordid + i);
		return true;
	}
	
	public boolean updateField(String pk, String fieldname, byte[] data) throws Exception
	{
		int [] column_and_field_index = this.getColumnAndFieldIndex(fieldname);
		int columnindex = column_and_field_index[0];
		int fieldindex = column_and_field_index[1];
		if(Constants.debug) System.out.println("**** column index " + columnindex);
		if(Constants.debug) System.out.println("**** field index " + fieldindex);
		
		if (columnindex < 0 || fieldindex < 0)
			return false;
			
		int recordnum = getRecordId(pk);
		Vector tokens = decodeColumn(this.rs.getRecord(recordnum + columnindex));
		tokens.setElementAt(data, fieldindex);
		data = encodeColumn(tokens);
		
		this.rs.setRecord((recordnum + columnindex), data, 0, data.length);
		return true;
	}
	
	public int countAll() throws Exception
	{
		try
		{
			return this.rs.getNumRecords() / columns.size();
		}
		catch(Exception e)
		{
			if(Constants.debug) System.out.println("DBManager.countAll() " + e.toString());
			return -1;
		}
	}
	
	private int getRecordId(String pk)
	{
		try
		{
			RecordEnumeration re = this.rs.enumerateRecords(null, null, false);
			while (re.hasNextElement())
			{
				int i = re.nextRecordId();
				if (columns.size()==1 || i % columns.size() == 1) 
				//if number of columns is 1 or ifthe current column is the one with pk (i.e first column of a tuple)
				{
					byte[] tmp = this.rs.getRecord(i);
					Vector tokens = decodeColumn(tmp);
					tmp = (byte []) tokens.elementAt(0); //first field of the column
					
					if ((new String(tmp)).equals(pk))
						return i;
				}
			}
		}
		catch (Exception e)
		{
			if(Constants.debug)  System.out.println("DBManager.getRecordId() " + e.toString());
		}
		return -1;
	}
	
	private int[] getColumnAndFieldIndex(String fieldname)
	{
		try
		{
			for (int i = 0; i < this.columns.size(); i++)
			{
				String [] fields = (String []) columns.elementAt(i);
				for(int j=0; j<fields.length; j++)
					if (fields[j].equals(fieldname))
						return new int [] {i,j};
			}
		}
		catch (Exception e)
		{
			if(Constants.debug)  System.out.println("DBManager.getColumnIndex() " + e.toString());
		}
		return new int [] {-1,-1};
	}

	private Vector decodeColumn(byte [] column)
	{
		Vector fields = new Vector();
		int cursor = 0;
		while(cursor<column.length)
		{
			byte[] tmpbytes = new byte[2];
			tmpbytes[1] = column[cursor++];
			tmpbytes[0] = column[cursor++];
			int fieldlength = ApplicationAssistant.toInt(tmpbytes);
			tmpbytes = null;
			ApplicationAssistant.gc();
			
			tmpbytes = new byte [fieldlength];
			System.arraycopy(column, cursor, tmpbytes, 0, fieldlength);
			fields.addElement(tmpbytes);
			tmpbytes = null;
			ApplicationAssistant.gc();
			cursor += fieldlength;
		}
		return fields;
	}
	
	private byte [] encodeColumn(Vector fields)
	{
		int numbytes = 0;
		for(int i=0; i<fields.size(); i++)
			numbytes+= ((byte []) fields.elementAt(i)).length;
		numbytes += (fields.size()*2); //fieldlengths
		
		byte [] column = new byte [numbytes];
		int cursor = 0;
		for(int i=0; i<fields.size(); i++)
		{
			byte [] field = (byte []) fields.elementAt(i);
			if(field == null) field = new byte [0];
			column[cursor++] = ApplicationAssistant.toBytes(field.length, 2)[1]; 	
			column[cursor++] = ApplicationAssistant.toBytes(field.length, 2)[0]; 	
			
			System.arraycopy(field, 0, column, cursor, field.length);
			cursor += field.length;
		}
		return column;
	}
	
	public int getNumfields()
	{
		return numfields;
	}
	
	public String [] getFieldNames()
	{
		String [] fieldnames = new String[numfields];
		int cursor = 0;
		for(int i=0; i<columns.size(); i++)
		{
			String [] fields = (String []) columns.elementAt(i);
			for(int j=0; j<fields.length; j++)
				fieldnames[cursor++] = fields[j]; 
		}
		return fieldnames;
	}	
	public int getAvailableSpace() throws Exception
	{
		return this.rs.getSizeAvailable();
	}

	public void destroy() throws Exception
	{
		//System.out.println("Destroying ... " + this.rsname);
		try
		{
			RecordStore.deleteRecordStore(this.rsname);
		}
		catch (Exception e)
		{
		}
	}	
}

//public boolean deleteAll() throws Exception
//{
//	RecordEnumeration re = this.rs.enumerateRecords(null, null, false);
//	while (re.hasNextElement())
//		this.rs.deleteRecord(re.nextRecordId());
//	return true;
//}
//


//public void enquire() throws Exception
//{
//	System.out.println("Enquiring ... " + this.rsname);
//	System.out.println("Size of RS : " + this.rs.getSize());
//	System.out.println("Size Available : " + this.rs.getSizeAvailable());
//	System.out.println("Number of Records : " + this.rs.getNumRecords());
//}
//public int getOccupiedSpace() throws Exception
//{
//	//System.out.println(this.rs.getName() + " " + this.rs.getSize());
//	return this.rs.getSize();
//}
//public Hashtable[] findByPrimaryKeys(Vector pks) throws Exception
//{
//	int[] recordnums = getRecordIds(pks);
//	int numvalidmrefs = 0;
//	for (int i = 0; i < recordnums.length; i++)
//		if (recordnums[i] > 0)
//			++numvalidmrefs;
//	Hashtable ht[] = new Hashtable[numvalidmrefs];
//	int cursor = 0;
//	if (numvalidmrefs > 0)
//	{
//		for (int k = 0; k < recordnums.length; k++)
//		{
//			if (recordnums[k] < 0)
//				continue;
//			Vector recordids = new Vector();
//			ht[cursor] = new Hashtable();
//			if(Constants.debug) System.out.println("Getting record " + recordnums[k]);
//			for (int i = 0; i < this.columns.size(); i++)
//			{
//				byte[] tmp = this.rs.getRecord(recordnums[k] + i);
//				String [] fields = (String []) columns.elementAt(i);
//				if (tmp == null)
//				{
//					for(int j=0; j<fields.length; j++)
//						ht[cursor].put(fields[j], new byte[0]);
//				}
//				else
//				{
//					Vector tokens = decodeColumn(tmp);
//					for(int j=0; j<fields.length; j++)
//						ht[cursor].put(fields[j], (byte []) tokens.elementAt(j));
//				}
//				recordids.addElement((recordnums[k] + i)+"");
//			}
//			ht[cursor].put("ID", recordids);
//			++cursor;
//		}
//	}
//	return ht;
//}
//
//private int[] getRecordIds(Vector pks) throws Exception
//{
//	try
//	{
//		RecordEnumeration re = this.rs.enumerateRecords(null, null, false);
//		int[] result = new int[pks.size()];
//		for (int i = 0; i < result.length; i++)
//		{
//			result[i] = -1;
//		}
//		int cursor = 0;
//		while (re.hasNextElement() && cursor < pks.size())
//		{
//			int i = re.nextRecordId();
//			if (columns.size()==1 || i % columns.size() == 1) 
//			//if number of columns is 1 or if the current column is the one with pk (i.e first column of a tuple)
//			{
//				byte[] tmp = this.rs.getRecord(i);
//				Vector tokens = decodeColumn(tmp);
//				tmp = (byte []) tokens.elementAt(0); //first field of the column
//				
//				String data = new String(tmp);
//				for (int k = 0; k < pks.size(); k++)
//				{
//					if (data.equals((String) pks.elementAt(k)))
//					{
//						result[cursor++] = i;
//						//System.out.print("CORRECT");
//						break;
//					}
//				}
//			}
//		}
//		return result;
//	}
//	catch (Exception e)
//	{
//		if(Constants.debug)  System.out.println("DBManager.getRecordIds() " + e.toString());
////		e.printStackTrace();
//	}
//	return null;
//}