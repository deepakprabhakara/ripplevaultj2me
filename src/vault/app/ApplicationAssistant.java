package vault.app;

import java.util.Vector;

import vault.util.Comparable;
import vault.util.Constants;

public abstract class ApplicationAssistant
{
	public static String host = "";
	public static String port = "";	
	public static String contacts_url = "";
	public static String registration_url = "";
	public static String media_pkt_upload_url = "";
	public static String media_gallery_url = "";
	public static String sub_status_check = "";
	public static String phone_model = "";
	public static String userid = "";
	public static String password = "";
	public static String encodedhp = "";
	public static String hp = "";
	public static String appid = "";
	public static String screensize = "";
	public static int language = 0;
	public static String version = "";
	public static String vendor = "";
	public static int keyset_flag = 0;
	public static boolean contactsynch = true; //whether or not to synch contacts
	public static boolean mediasynch = true; //whether or not to synch media
	public static boolean smssynch = true; //whether or not to synch SMS
	public static long lastsynchtime = 0;
	public static long nextscheduledsynchtime = 0;
	public static long scheduleperiod = 0;
	public static int registered = Constants.REGISTRATION_FALSE;
	public static int initialupload = Constants.INITIALUPLOAD_TRUE;
	public static long laststatussynctimestamp = 0;
	public static boolean SSL = false; //whether or not to connect using SSL
	public static String vacuummode = null;
	
	//billing_reg_type, user_status not stored as not required currently
	
	public static String prevscreenname = null;
	public static int colortheme = 0;
	
	public static int screenwidth = 0; //this will be set when the first screen is painted
	public static int screenheight = 0; //this will be set when the first screen is painted
	
	//Misc Utils
	public static String[] vectorToString(Vector v)
	{
		String[] s = new String[v.size()];
		
		for(int i = 0; i< v.size(); ++i)
			s[i] = (String)v.elementAt(i);
		
		return s;
	}
	
	public static byte[] nonNull(Object o)
	{
		if (o != null)
			return (byte[]) o;
		else
			return new byte[0];
	}
	
	public static void sort(Comparable elements[])
	{
	    if(Constants.debug) System.out.println("*** sorting");
		for (int ik = 0; ik < elements.length - 1; ik++)
		{
			for (int jk = 0; jk < elements.length - 1; jk++)
			{
				if (elements[jk + 1].compareTo(elements[jk]) == -1)
				{
					Comparable tmp = elements[jk];
					elements[jk] = elements[jk + 1];
					elements[jk + 1] = tmp;
				}
			}
		}
	}
	
	//Byte Utils
	public static byte[] toBytes(int num, int bytes)
	{
		byte[] data = new byte[bytes];
		for (int i = 0; i < bytes; i++)
		{
			int temp = 0xFF & num;
			data[i] = (byte) (temp < 0 ? 256 - temp : temp);
			num = num >>> 8;
		}
		return data;
	}	
	/**
	 * Converts an integer into an array of bytes => byte[0] contains the low order byte.
	 * @param num
	 * @param bytes The number of bytes to take from num starting from lowest order byte.
	 * @return byte[] Array of length 'bytes'
	 */
	/**
	 * 0 : 0 - 7
	 * 1 : 8 - 15
	 */
	public static int toInt(byte[] b)
	{
		int tmp = b[0];
		if (tmp < 0)
			tmp = 256 + tmp;
		int tmp2 = b[1];
		if (tmp2 < 0)
			tmp2 = 256 + tmp2;
		tmp2 = tmp2 << 8;
		return (tmp2 & 0xFFFF) | tmp;
	}
	/**
	 * 0 : 0 - 7
	 * 1 : 8 - 15
	 * .
	 * .
	 * .
	 * .
	 * 6 : 48 - 55
	 * 7 : 56 - 63
	 **/
	public static long toLong(byte[] b)
	{
		long[] pow = new long[8];
		pow[0] = 1; //2^0
		pow[1] = 256; //2^8
		pow[2] = 65536; //2^16
		for (int i = 3; i < 8; i++)
		    pow[i] = pow[i - 1] * pow[1];
		
		long ret = 0;
		for (int i = 0; i < 8; i++)
		{
			int tmp = b[i];
			if (tmp < 0)
				tmp += 256;
			long ltmp = tmp * pow[i];
			ret += ltmp;
		}
		pow = null;
		ApplicationAssistant.gc();
		return ret;
	}
	/**
	 * Reverses an array of bytes.
	 * @param data
	 * @return byte[]
	 */
	public static byte[] reverse(byte[] data)
	{
		int l = data.length;
		int h = l / 2;
		for (int i = 0; i < h; i++)
		{
			byte temp = data[i];
			data[i] = data[l - i - 1];
			data[l - i - 1] = temp;
		}
		return data;
	}
	
	public static long getDec(String hex)
    {
        long tmp = 0L;
        for(int i = 0; i < hex.length(); i++)
        {
            char ch = hex.charAt(hex.length() - 1 - i);
            tmp += pow(16,i) * getDigit("" + ch);
        }

        return tmp;
    }
	
	private static int getDigit(String ch)
	{
		ch = ch.toUpperCase();
		if(ch.equals("A"))
			return 10;
		else if(ch.equals("B"))
			return 11;
		else if(ch.equals("C"))
			return 12;
		else if(ch.equals("D"))
			return 13;
		else if(ch.equals("E"))
			return 14;
		else if(ch.equals("F"))
			return 15;
		else
			return Integer.parseInt(ch);
	}
	
	private static long pow(long num, long exponent)
	{
		long pow = 1;
		for(int i=0; i<exponent; i++)
			pow *= num;
		return pow;
	}
	
	public static Vector tokenize(String str, String token)
	{
		Vector tokens = new Vector();
		while(str.length()>0)
		{
			if(str.equals(token))
				str = "";
			else if(str.indexOf(token)>0)
			{
				tokens.addElement(str.substring(0, str.indexOf(token)));
				if(str.length()>str.indexOf(token)+1)
					str = str.substring(str.indexOf(token)+1);
				else
					str = "";
			}
			else
			{
				tokens.addElement(str);
				str = "";
			}
		}
		return tokens;
	}
	
	public static long getCheckSumField(String identify) 
	{ 
		if(Constants.debug) System.out.println("ApplicationAPI.getCheckSumField");
		long sum = 0;
		byte[] message = identify.getBytes();
		if(Constants.debug) System.out.println("ApplicationAPI.getCheckSumField1");
		
		for(int i = 0; i< message.length; i++) 
		{ 
			if(i % 2 == 0)
				sum += (message[i] * 256); // may be < 0
			else
				sum += (int)message[i] & 0xFF; 
			//			if(sum >= 65535) 
			//				sum -= 0x10000; //65535 -> -1 
			//			if(sum < 0x00000) 
			//				sum += 0x10000; 
		}
		if(Constants.debug) System.out.println("ApplicationAPI.getCheckSumField2");
		return sum; 
	}
	
	/**
	 * MIDP 2.0
	 * as long as theres atleast 30kb, no need to garbage collect in case of MIDP 2,
	 * as its an expensive operation in this implementation
	 **/
	public static final void gc()
	{
		ApplicationAssistant.gc(false);
	}
	public static final void gc(boolean force)
	{
//		if(Constants.debug) System.out.println("PRE-GC Heap used: " + (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory())+"; Heap free: " + Runtime.getRuntime().freeMemory());
		if(/*Constants.midp1 ||*/ java.lang.Runtime.getRuntime().freeMemory()<120000 || force) 
			System.gc();	
//		if(Constants.debug) System.out.println("POST-GC Heap used: " + (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory())+"; Heap free: " + Runtime.getRuntime().freeMemory()+"\n");
	}
	
	public static void pause(long milliseconds)
	{
		long start = System.currentTimeMillis();
		while (System.currentTimeMillis() - start < milliseconds) ;
		/*try 
		{ 
			Thread.sleep(seconds * 1000); 
		}
	    catch (InterruptedException ie) 
	    {}*/
	}
}

	/*private static boolean contains(int[] tmp, int s)
	{
		for (int i = 0; i < tmp.length; i++)
		{
			if (tmp[i] == s)
				return true;
		}
		return false;
	}*/
	/*public static int strcmp(String str1, String str2)
	{
	    str1 = str1.toLowerCase();
	    str2 = str2.toLowerCase();
	    
	    for(int i=0; i<str1.length()&&i<str2.length(); i++)
	    {
	        if(str1.charAt(i)>str2.charAt(i))
	            return 1;
	        else if(str1.charAt(i)<str2.charAt(i))
	            return -1;
	        else
	            return 0;
	    }
	    
	    if(str1.length()>str2.length())
	        return 1;
	    else if(str1.length()<str2.length())
	        return -1;
	    else 
	        return 0;
	}*/

	//0: 0..7, 1: 8..15 ... 7: 56..63
	/*public static byte[] toBytes(long num) {
		byte [] output = new byte [8];
	
		for(int i=0; i<8; i++)
		{
			long t = (num % 256);
			if(t>127)
				t -= 256;

			output [i] = (byte) t;
			num = num - (num % 256);
			num = num/256;
		}
		
		return output;
//		int low = (int) num;
//		int high = (int) (num >>> 32);
//		byte[] output = new byte[8];
//		System.arraycopy(getBytes(low), 0, output, 0, 4);
//		System.arraycopy(getBytes(high), 0, output, 4, 4);
//		return output;
	}*/
	//0: 0..7, 1: 8..15 ... 7: 56..63
	/*public static byte[] customisedGetBytes(long quo, long rem) {
		long num = quo*1000 +  rem;
		byte [] output = new byte [8];

		for(int i=0; i<8; i++)
		{
			long t = (num % 256);
			if(t>127)
				t -= 256;

			output [i] = (byte) t;
			num = num - (num % 256);
			num = num/256;
		}
		
		return output;
	}*/

