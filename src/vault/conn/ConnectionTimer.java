package vault.conn;

import java.util.Timer;
import java.util.TimerTask;

import vault.app.ApplicationAPI;
import vault.util.Constants;

class ConnectionTimer extends TimerTask
{
	private static final long operationtimeout = 180000;
	private static final long activitytimeout = 30000;
	private static final long interval = 1000;
	
	private long globalticker;
	private long ticker;
	private Timer t;

	public ConnectionTimer()
	{
	   this.ticker = 0;
	   this.globalticker = 0;
	   t=new Timer();
	   t.scheduleAtFixedRate(this,0,interval);
	}

	/*
	 * Ticker is for activity timeout.. To say I am inactive for say 15 seconds
	 * Global Ticker is for the entire operation. This we set to something substantial, like 3 minutes.
	 */
	 public void run()
	 {
		 if(Constants.debug) System.out.println("**** ConnectionTimer BEGIN: ticker " + ticker + " globalticker " + globalticker + " connstatus "+ ApplicationAPI.getConnstatus());
	 	 if(ApplicationAPI.getConnstatus() == ConnectionHome.RECEIVING)
		 {
		 	ApplicationAPI.setConnstatus(ConnectionHome.INACTIVE);
		 	ticker = 0;
		 }
		 else if(ApplicationAPI.getConnstatus() == ConnectionHome.INACTIVE) 
		 	ticker += interval;
		 globalticker += interval;
		 
		 if(ticker >= activitytimeout ||
		 	globalticker >= operationtimeout)
		 {
		 	 ApplicationAPI.setConnstatus(ConnectionHome.FAILED);
			 stop();
		 }
		 if(Constants.debug) System.out.println("**** ConnectionTimer END: ticker " + ticker + " globalticker " + globalticker + " connstatus "+ ApplicationAPI.getConnstatus());
	 }

	public void stop()
	{
		if(Constants.debug) System.out.println("**** ConnectionTimer STOP 1");
		try
		{
			if(t!=null)
				t.cancel();
		}
		catch(Exception e)
		{
			if(Constants.debug)  System.out.println("ConectionTimer.stop() " + e.toString()); 
		}
		if(Constants.debug) System.out.println("**** ConnectionTimer STOP 2");
		try
		{
			this.cancel();
		}
		catch(Exception e)
		{
			if(Constants.debug)  System.out.println("ConectionTimer.stop() " + e.toString()); 
		}
		if(Constants.debug) System.out.println("**** ConnectionTimer STOP 3");
	}
}


