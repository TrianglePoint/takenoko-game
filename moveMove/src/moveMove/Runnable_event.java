package moveMove;

import java.awt.Color;

import javax.swing.JLabel;
import javax.swing.JPanel;

public class Runnable_event implements Runnable {
	private Viewer_world viewer_world = null;
	private String event_type = "";
	private Sprite sprite = null;
	
	private JPanel dialog_box = null; // For every event except special event.
	private JLabel dialog = null;
	
	private JPanel list_box = null; // For merchant, inventory.
	
	public Runnable_event(Viewer_world viewer_world, String event_type, 
			Sprite sprite, JPanel dialog_box, JLabel dialog, JPanel list_box) {
		// TODO Auto-generated constructor stub
		this.viewer_world = viewer_world;
		this.event_type = event_type;
		this.sprite = sprite;
		this.dialog_box = dialog_box;
		this.dialog = dialog;
		this.list_box = list_box;
	}
	public Runnable_event(Viewer_world viewer_world, String event_type, 
			Sprite sprite, JPanel dialog_box, JLabel dialog) {
		// TODO Auto-generated constructor stub
		this.viewer_world = viewer_world;
		this.event_type = event_type;
		this.sprite = sprite;
		this.dialog_box = dialog_box;
		this.dialog = dialog;
	}
	
	@Override
	public void run() {
		// TODO Auto-generated method stub
		switch(event_type) {
		case "inventory":
			try{
				synchronized(viewer_world) {
					viewer_world.wait();
				}
			}catch(Exception e) {
				e.printStackTrace();
			}
			viewer_world.remove(dialog_box);
			viewer_world.remove(list_box);
			viewer_world.repaint();
			break;
		case "merchant":	
			show_dialog(dialog);
			try{
				synchronized(viewer_world) {
					viewer_world.wait();
				}
			}catch(Exception e) {
				e.printStackTrace();
			}
			viewer_world.remove(dialog_box);
			viewer_world.remove(list_box);
			viewer_world.repaint();
			break;
		default:		
			show_dialog(dialog);
			try{
				synchronized(viewer_world) {
					viewer_world.wait();
				}
			}catch(Exception e) {
				e.printStackTrace();
			}
			viewer_world.remove(dialog_box);
			viewer_world.repaint();
		}
		viewer_world.setEvent_type("basic");
	} // End of run().
	
	void show_dialog(JLabel dialog) {
		
		// TEMP. change to animation text at after.
		dialog.setText(sprite.getName() + " : " + sprite.getDialog());
		
	}
}
