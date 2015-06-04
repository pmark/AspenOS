package org.aspenos.util;

public class Timer extends Thread
{
	TimerListener listener;
	long interval;

	public Timer(TimerListener t)
	{
		listener = t;
		interval = 60000;
		setDaemon(true);
	}

	public Timer(TimerListener t, long i)
	{
		listener = t;
		interval = i;
		setDaemon(true);
	}

	public Timer(TimerListener t, long i, boolean makeDaemon)
	{
		listener = t;
		interval = i;
		setDaemon(makeDaemon);
	}

	public void run()
	{
		//System.out.println("Timer.run: starting");
		try
		{
			while (!interrupted())
			{
				//System.out.println("Timer.run: sleeping");
				sleep(interval);
				listener.timeElapsed(this);
			}
		}
		catch (InterruptedException e)
		{ }
	}

	public void setInterval(long i)
	{ interval = i; }

	public long getInterval()
	{ return interval; }

}
