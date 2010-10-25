package vault.conn;

import vault.app.ApplicationAPI;
import vault.app.ApplicationAssistant;
import vault.util.Constants;

public class ConnectionHome
{
	public static final int SENDING = 2;
	public static final int RECEIVING = 1;
	public static final int INIT = -1;
	public static final int NA = -2;
	public static final int INACTIVE = 0;
	public static final int FAILED = 4;
	public static final int FINISHED = 5;
	//private String sessionid;
	
	private ConnectionThread connThread ;
	private ConnectionTimer  connTimer;

	public byte [] send_and_receive(String url, byte [] data, /*boolean sessioncontinued,*/ int method) throws Exception
	{
		try
		{
			ApplicationAPI.setConnstatus(ConnectionHome.INIT);
			ApplicationAssistant.gc();
			/*if(!sessioncontinued)
				sessionid = null;*/
			connThread = new ConnectionThread(method, url, data /*, sessioncontinued, sessionid*/);
			connTimer = new ConnectionTimer();
			connThread.start(); 
			try
		    {
				while(connThread!=null && 
					  connThread.isAlive() && 
					  ApplicationAPI.getConnstatus()!=ConnectionHome.FINISHED &&
					  ApplicationAPI.getConnstatus()!=ConnectionHome.FAILED);
				
				if(ApplicationAPI.getConnstatus()!=ConnectionHome.FINISHED &&
				   ApplicationAPI.getConnstatus()!=ConnectionHome.FAILED)
					ApplicationAPI.setConnstatus(ConnectionHome.FAILED);
			}
			catch(Exception e)
			{
				if(Constants.debug)  System.out.println("ConnectionHome.send_and_receive() " + e.toString()); 
			}
			try
		    {
				connThread.cleanUp(); 
		    }
		    catch(Exception e)
		    {}
			try
		    {
				connTimer.stop(); 
		    }
		    catch(Exception e)
		    {}
//			ApplicationAPI.dialogconfirmation += " 4";
			connTimer = null;
			byte [] receiveddata = connThread.getData();
//			sessionid = connHandler.getSessionid();
			connThread = null;
			ApplicationAssistant.gc();
			return receiveddata;
		}
		catch(Exception e)
		{
			ApplicationAPI.setConnstatus(ConnectionHome.FAILED);
			return null;
		}
	}
}
