package org.aspenos.util;

public class TimerTest implements TimerListener {

	private final int TIMER_INTERVAL = 750;
	private final int MAX_BEEPS = 5;
	private int numBeeps;

	public TimerTest() {
		numBeeps = 0;
		Timer t = new Timer(this, TIMER_INTERVAL, false );
		t.start();
	}


	public void timeElapsed(Timer t) {
		if (numBeeps < MAX_BEEPS) {
			System.out.println("beep!\n");
			numBeeps++;
		} else {
			System.out.println("\nStopping the beeps\n");
			t.interrupt();
		}
	}


	public static void main(String args[]) {
		TimerTest tt = new TimerTest();
	}
}
