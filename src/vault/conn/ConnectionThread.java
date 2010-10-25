package vault.conn;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;

import javax.microedition.io.Connector;
import javax.microedition.io.HttpConnection;
import javax.microedition.io.HttpsConnection;

import vault.app.ApplicationAPI;
import vault.app.ApplicationAssistant;
import vault.util.Constants;


public class ConnectionThread extends Thread
{
	private int type;
	private String url;
	private String servleturl;
	
	private DataOutputStream out ;
	private DataInputStream in ;
	private HttpsConnection conn_ssl;
	private HttpConnection conn_normal;
	/*private boolean sessioncontinued;
	private String sessionid;*/
	private boolean connected;
	private byte [] data;
	
	public ConnectionThread(int type, String url, byte [] data/*, boolean sessioncontinued, String sessionid*/)
	{
		this.cleanUp();
		this.type = type;
		this.servleturl = url;
		this.url = (ApplicationAssistant.SSL)? 
						"https://"+ApplicationAssistant.host+/*":"+ApplicationAssistant.port+*/url :
						"http://"+ApplicationAssistant.host+/*":"+ApplicationAssistant.port+*/url;
		if(Constants.debug) System.out.println("Connecting to... " + this.url);
		this.out = null;
		this.in = null;
		this.conn_ssl = null;
		this.conn_normal = null;
//		this.sessionid = null;
		this.data = null;
		ApplicationAssistant.gc();
		
		this.connected = false;
		/*this.sessioncontinued = sessioncontinued;
		if(sessioncontinued)
			this.sessionid = sessionid;*/
		this.data = data;
	}
	
	public void run()
	{
		try
		{
			switch(type)
			{
				case Constants.POST_AND_RECEIVE :
				{
					if(Constants.debug) System.out.println("**** ConnectionThread 1");
					this.connect(Constants.POST_AND_RECEIVE);
					if(Constants.debug) System.out.println("**** ConnectionThread 2");
					ApplicationAPI.setConnstatus(ConnectionHome.SENDING);
					try
					{
						this.send(this.data);
					}
					catch(Exception e)
					{
						this.data = null;
						ApplicationAssistant.gc();
						if(Constants.debug)  System.out.println("ConectionThread.run() " + e.toString());
						throw e;
					}
					if(Constants.debug) System.out.println("**** ConnectionThread 3");
					ApplicationAPI.setConnstatus(ConnectionHome.RECEIVING);
					try
					{
						this.data = this.recieve();
					}
					catch(Exception e)
					{
						this.data = null;
						ApplicationAssistant.gc();
						if(Constants.debug)  System.out.println("ConectionThread.run() " + e.toString());
						throw e;
					}
					if(Constants.debug) System.out.println("**** ConnectionThread 4");
					ApplicationAPI.setConnstatus(ConnectionHome.FINISHED);
					break;
				}
				/*case Constants.GET_AND_RECEIVE */
				/*case Constants.SEND */
			}
		}
		catch(Exception e)
		{
			if(Constants.debug)  System.out.println("ConectionThread.run() " + e.toString()); 
			ApplicationAPI.setConnstatus(ConnectionHome.FAILED);
		}
		finally
		{
			if(Constants.debug)  System.out.println("ConectionThread.run() finally");
			this.cleanUp();
		}
	}
	
	private void connect(int submittype) throws Exception 
	{
		if(Constants.debug) System.out.println("** ConnectionThread.connect()");
		if(Constants.debug) System.out.println("**** ConnectionThread 1.1");
		if(ApplicationAssistant.SSL)
			this.conn_ssl = (HttpsConnection) Connector.open (this.url, Connector.READ_WRITE);
		else
			this.conn_normal = (HttpConnection) Connector.open (this.url, Connector.READ_WRITE);
		if(Constants.debug) System.out.println("**** ConnectionThread 1.2");
		this.connected = true;
		if(ApplicationAssistant.SSL)
			this.conn_ssl.setRequestMethod(HttpConnection.POST);
		else
			this.conn_normal.setRequestMethod(HttpConnection.POST);
		try
		{
			if(ApplicationAssistant.SSL)
				this.conn_ssl.setRequestProperty("Host",ApplicationAssistant.host);
			else
				this.conn_normal.setRequestProperty("Host",ApplicationAssistant.host);
		}
		catch(Exception e)
		{
			if(Constants.debug)  System.out.println("ConectionThread.connect() " + e.toString());
		}
		//this.conn.setRequestProperty("User-Agent", "Profile/MIDP-1.0 Configuration/CLDC-1.0");
		if(ApplicationAssistant.SSL)
		{
			this.conn_ssl.setRequestProperty("User-Agent", "Profile/MIDP-2.0 Configuration/CLDC-1.0");
			this.conn_ssl.setRequestProperty("Content-Language", "en-US");
		}
		else
		{
			this.conn_normal.setRequestProperty("User-Agent", "Profile/MIDP-2.0 Configuration/CLDC-1.0");
			this.conn_normal.setRequestProperty("Content-Language", "en-US");
		}
		try
		{
			if(ApplicationAssistant.SSL)
				this.conn_ssl.setRequestProperty("Http-version","HTTP/1.1");
			else
				this.conn_normal.setRequestProperty("Http-version","HTTP/1.1");
		}
		catch(Exception e)
		{
			if(Constants.debug)  System.out.println("ConectionThread.connect() " + e.toString());
		}
		try
		{
			//this.conn.setRequestProperty("Content-Type", "application/octet-stream");
			if(ApplicationAssistant.SSL)
				this.conn_ssl.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
			else
				this.conn_normal.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
		}
		catch(Exception e)
		{
			if(Constants.debug)  System.out.println("ConectionThread.connect() " + e.toString());
		}
		if(Constants.debug) System.out.println("**** ConnectionThread 1.3");
//		httpConnection.setRequestProperty("Connection","open");//
//		httpConnection.setRequestProperty("Connection","Keep-Alive");//
//		httpConnection.setRequestProperty("Transfer-Encoding","chunked");//		
		/*if(this.sessioncontinued)
		{
			try
			{
				this.conn.setRequestProperty("Cookie", this.sessionid);
			}
			catch(NullPointerException e)
			{}
		}*/
	}
	
	private void send(byte[] data) throws Exception
	{
		if(Constants.debug) System.out.println("** ConnectionThread.send()");
		if(Constants.debug) System.out.println("**** ConnectionThread 2.1");
		if(!this.connected)
			throw new Exception("Connection is not established.");
		if(data == null)
			throw new Exception("The data is null!!!!");

		int contentlength = (data==null)?0:data.length;
		if(ApplicationAssistant.SSL)
			this.conn_ssl.setRequestProperty("Content-Length", ""+contentlength);
		else
			this.conn_normal.setRequestProperty("Content-Length", ""+contentlength);
		if(Constants.debug) System.out.println("**** ConnectionThread 2.2");
		if(ApplicationAssistant.SSL)
			this.out = new DataOutputStream(conn_ssl.openOutputStream());
		else
			this.out = new DataOutputStream(conn_normal.openOutputStream());
		if(Constants.debug) System.out.println("**** ConnectionThread 2.3");
		this.out.write(data);
		if(Constants.debug) System.out.println("**** ConnectionThread 2.4");
		try
		{
			this.out.flush(); //somephones dont support this, so catch it
		}
		catch(Exception e)
		{
			if(Constants.debug)  System.out.println("ConectionThread.send() " + e.toString());
		}
		if(Constants.debug) System.out.println("**** ConnectionThread 2.5");
		this.out.close();
		if(Constants.debug) System.out.println("**** ConnectionThread 2.6");
		this.out = null;
		ApplicationAssistant.gc();
		if(Constants.debug) System.out.println("** ConnectionThread.send() complete");
	}
	
	private byte[] recieve() throws Exception
	{
		if(Constants.debug) System.out.println("** ConnectionThread.receive()");
		if(Constants.debug) System.out.println("**** ConnectionThread 3.1");
		if(!this.connected)
			throw new Exception("Connection is not established.");
		
		/*if (this.sessionid == null)
		{
				//this.sessionid = this.conn.getHeaderField("Set-Cookie");
				int count = 1; 
				this.sessionid = conn.getHeaderField(count);
				try
				{
					while(sessionid != null && count < 20)
					{
						//mb.app.MobilebagLogic.err = mb.app.MobilebagLogic.err+count+sessionid.charAt(2);
						if(sessionid.toLowerCase().indexOf("jsessionid") == -1)
						{
							++count;
							this.sessionid = this.conn.getHeaderField(count);
						}
						else
						{
							break;
						}	
					}
				}
				catch(Exception e)
				{	
					if(Constants.debug)  System.out.println("ConectionThread.receive() " + e.toString());
				}
		}*/

		if(ApplicationAssistant.SSL)
			this.in = new DataInputStream(conn_ssl.openInputStream());
		else
			this.in = new DataInputStream(conn_normal.openInputStream());
		//ApplicationAPI.dialogconfirmation += "1";
		ByteArrayOutputStream data = new ByteArrayOutputStream();
		if(Constants.debug) System.out.println("**** ConnectionThread 3.2");
		int ch = -1;
		if(ApplicationAPI.getConnstatus() != ConnectionHome.FAILED)
			ch = this.in.read();
		if(Constants.debug) System.out.println("**** ConnectionThread 3.3");
		int cnt = 0;
		while(ch==-1 && cnt<10 && ApplicationAPI.getConnstatus() != ConnectionHome.FAILED)
		{
			ch = this.in.read(); //on some phones need to floss 
			++cnt;
		}
		if(Constants.debug) System.out.println("**** ConnectionThread 3.4");
		while (ApplicationAPI.getConnstatus() != ConnectionHome.FAILED && ch != -1 )
		{
			//ApplicationAPI.dialogconfirmation += ((char)ch);
			ApplicationAPI.setConnstatus(ConnectionHome.RECEIVING);
			data.write((char)ch);
			ch = this.in.read();
		}
		if(Constants.debug) System.out.println("**** ConnectionThread 3.5");
		this.in.close();
		if(Constants.debug) System.out.println("**** ConnectionThread 3.6");
		this.in = null;
		ApplicationAssistant.gc();

		//either it received or was interrupted by timer
		if(ApplicationAPI.getConnstatus() == ConnectionHome.FAILED)
			throw new Exception ("Receive timed out");
		return data.toByteArray();
	}

	public void cleanUp()
	{
		this.connected = false;
		try
		{
			if(this.in!=null)
				this.in.skip(in.available());
		}
		catch (Exception e) {}
		try
		{
			if(this.in!=null)
				this.in.close ();
		}
		catch (Exception e)	{}
		try
		{
			if(this.out!=null)
				this.out.flush();
		}
		catch (Exception e)	{}
		try
		{
			if(this.out!=null)
				this.out.close ();
		}
		catch (Exception e) {}
		try
		{
			if(this.conn_normal!=null)
				this.conn_normal.close ();
			if(this.conn_ssl!=null)
				this.conn_ssl.close ();
		}
		catch (Exception e)	{}
		this.in = null;
		this.out = null;
		this.conn_normal = null;
		this.conn_ssl = null;
	    ApplicationAssistant.gc();
	}

	
	public byte[] getData()
	{
		return data;
	}

	/*public String getSessionid()
	{
		return sessionid;
	}*/
}