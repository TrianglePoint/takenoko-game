package moveMove;

import java.awt.Color;
import java.awt.Font;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JLabel;

public class Show_got_money_thread extends Thread{	
	private int got_money = -1;
	private int[] resolution = {-1, -1};
	private int[] location_view = {-1, -1};
	
	private Viewer_world viewer_world = null;
	
	private JLabel money_label = null;
	
	public Show_got_money_thread(int got_money, Viewer_world viewer_world, Color color_background) {
		// TODO Auto-generated constructor stub
		this.got_money = got_money;
		this.viewer_world = viewer_world;
		resolution = viewer_world.getRESOLUTION();
		location_view[0] = resolution[0];
		location_view[1] = resolution[1];
		money_label = new JLabel(" +" + got_money);
		money_label.setBounds(resolution[0], resolution[1]/2, 
				resolution[0]/5, resolution[1]/15);
		money_label.setForeground(Color.YELLOW);
		money_label.setFont(new Font("serif", Font.BOLD, resolution[1]/20));
		money_label.setOpaque(true);
		money_label.setBackground(color_background);
		viewer_world.add(money_label);
	}
	
	@Override
	public void run() {
		// TODO Auto-generated method stub
		super.run();
		try {
			while(money_label.getX() != resolution[0] - money_label.getWidth()/5*4) {
				viewer_world.repaint();
				sleep(1);
				money_label.setBounds(money_label.getX()-1, money_label.getY(), 
						money_label.getWidth(), money_label.getHeight());
			}
			for(int i = 2; money_label.getX() != resolution[0] - money_label.getWidth(); i++) {
				viewer_world.repaint();
				sleep(i/2);
				money_label.setBounds(money_label.getX()-1, money_label.getY(), 
						money_label.getWidth(), money_label.getHeight());
			}
			viewer_world.repaint();
			sleep(2000);
		}catch(InterruptedException ie) {
			// Remove the got money UI as repaint();
		}
		viewer_world.remove(money_label);
		viewer_world.repaint();
	}
	
	public int[] getLocation_view() {
		return location_view;
	}
}
