package moveMove;

import java.awt.Color;

import javax.swing.JLabel;

import moveMove.db.MariaDB;

public class Clock extends Thread{
	private int day = -1; 
	private int time = -1; // 1 time is 1 minute.
	
	private final int DEADLINE = 7;
	
	private long start_t = -1; // Used the pause the clock.
	private long remain_t = -1;
	private final int ONE_TIME_LENGTH = 1000;
	
	private final int MORNING = 8; // The start of the day.
	private final int NIGHT = 24; // The end of the day.
	private final int ALERT_TIME = 22; // Player should sleep before NIGHT.
										// set red color to clock after ALERT_TIME.
	private boolean do_not_sleep = false;
	private boolean is_tired = false;
	
	private JLabel label_clock = null; // Display the time.
	private Viewer_world viewer_world = null;
	
	public Clock(JLabel label_clock, Viewer_world viewer_world) {
		// TODO Auto-generated constructor stub
		this.label_clock = label_clock;
		this.viewer_world = viewer_world;
	}
	
	@Override
	public void run() {
		// TODO Auto-generated method stub
		super.run();
		do{ // Start of while(end_day()).
			do_not_sleep = true;
			is_tired = true;
			while(do_not_sleep && MORNING*60 + time <= NIGHT*60) {
				label_clock.setText(get_clock_display());
				remain_t = ONE_TIME_LENGTH;
				while(do_not_sleep) {
					try {
						start_t = System.currentTimeMillis();
						if(remain_t <= 0) {
							break;
						}
						sleep(remain_t);
						break;
					} catch (InterruptedException ie) {
						// TODO Auto-generated catch block
						remain_t -= (System.currentTimeMillis() - start_t);
						synchronized(this) {
							while(true) {
								try {
									wait();
									break;
								} catch (InterruptedException ie1) {
									/* The case that request to stop the clock, 
									 * when already stopped the clock.
									 * 
									 * this case is for example, 
									 * the case that push 'i' like spamming as very fast speed.
									 */
								}
							}
						}
					} // End of catch(InterruptedException ie)
				} // End of while(true) : sleep().
				time++;
			} // End of while(MORNING*60 + time <= NIGHT*60).
		}while(end_day());
		viewer_world.play_ending(); // play ending event.
	}
	boolean end_day() {
		viewer_world.setDont_access_clock(true);
		viewer_world.setEvent_type("immovable");
		viewer_world.fade_out(2000);
		viewer_world.regeneration_sprites();
		if(day == DEADLINE) {
			
			//TEMP.
			viewer_world.set_health_point(1.0, true);
			day = 1;
			time = 0;
			
			return false;
		}
		if(is_tired) {
			viewer_world.set_health_point(0.5, is_tired); // 50% health point.
		}else {
			viewer_world.set_health_point(1.0, is_tired); // 100% health point.
		}
		day++;
		time = 0;
		viewer_world.fade_in(2000);
		viewer_world.setEvent_type("basic");
		viewer_world.setDont_access_clock(false);
		
		return true; // Progress the day.
	}
	String get_clock_display() {
		String clock_display = "Day " + day + " ";
		int hour = MORNING + time/60;
		if(hour >= ALERT_TIME) {
			label_clock.setForeground(Color.RED);
		}else if(day == DEADLINE) {
			label_clock.setForeground(Color.ORANGE);
		}else {
			label_clock.setForeground(Color.BLACK);
		}
		if(day == DEADLINE) {
			label_clock.setBackground(Color.BLACK);
		}else {
			label_clock.setBackground(null);
		}
		int minute = time%60;
		
		if(hour < 10){
			clock_display += "0";
		}
		clock_display += hour + " : ";
		if(minute < 10) {
			clock_display += "0";
		}
		clock_display += minute;
		
		return clock_display;
	}
	public void do_sleep() {
		do_not_sleep = false;
		is_tired = false;
	}
	public void be_tired() {
		do_not_sleep = false;
	}
	
	public int getDay() {
		return day;
	}
	public int getTime() {
		return time;
	}
	
	public void setDay(int day) {
		this.day = day;
	}
	public void setTime(int time) {
		this.time = time;
	}
}
