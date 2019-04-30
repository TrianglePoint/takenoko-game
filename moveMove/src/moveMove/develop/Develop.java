package moveMove.develop;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;

import javax.swing.JPanel;

import moveMove.Sprite;
import moveMove.Viewer_world;
import moveMove.db.MariaDB;

public class Develop {
	private boolean mode = false;
	private int block_size = 0;
	private final int EDIT_MENU_SIZE = 8; // It is calculate as block_size.
	private int[] resolution = {0, 0};
	private int[] location_center = {0, 0};
	private int[] location_mouseover = {-1, -1}; // For edit the map as mouse.
	private int[] location_clicked = {-1, -1};
	
	private Viewer_world viewer_world = null;
	private Sprite developer = null;
	private Sprite player = null; // Save player at event type basic.
	private Sprite player_just_show = null;
	private Sprite field = null;
	
	private Color color_background = null;
	private Color color_content = null;
	
	private MariaDB mariadb_develop = null;
	private Container_event container_event = null;
	private Edit_box edit_box = null;
	
	public Develop(int block_size, Viewer_world viewer_world, String connect_to_address, 
			Color color_background, Color color_content) {
		// TODO Auto-generated constructor stub
		mode = false;
		this.block_size = block_size;
		this.viewer_world = viewer_world;
		resolution = viewer_world.getRESOLUTION();
		location_center[0] = viewer_world.getRESOLUTION()[0]/2-block_size/2;
		location_center[1] = viewer_world.getRESOLUTION()[1]/2-block_size/2;
		
		this.color_background = color_background;
		this.color_content = color_content;
		
		// TEMP. change to login as typing.
		mariadb_develop = new MariaDB(connect_to_address, "develop", "hoi@@");
	}
	public Sprite mode_change(){
		mode = !(mode);
		setLocation_mouseover(-1, -1);
		setLocation_clicked_as_mouseover();
		
		mariadb_develop.close();
		
		return player;
	}
	public Sprite mode_change(Sprite player, Sprite field){
		mariadb_develop.connect();
		mode = !(mode);
		this.player = player;
		this.field = field;
		String[] name_image_player = {"ojiisann.png", "ojiisann_left.png", 
				"ojiisann_up.png", "ojiisann_right.png"};
		String[] name_image_developer = {"developer.png"};
		player_just_show = new Sprite("player_just_show", "object", name_image_player, player.getLocation_real()[0], player.getLocation_real()[1], player.getDirection(), player.getDialog(), player.getExist_script());
		developer = new Sprite("developer", "player", name_image_developer, player.getLocation_real()[0], player.getLocation_real()[1], 6, "", false);
		
		// Sprite developer is not need go to edge of map. 
		if(field.getSize()[0] < resolution[0]) {
			developer.setLocation_real('x', (field.getSize()[0] - block_size) / 2);
		}else if(developer.getLocation_real()[0] < location_center[0]) {
			developer.setLocation_real('x', location_center[0]);
		}else if(developer.getLocation_real()[0] > field.getSize()[0] - location_center[0] - block_size) {
			developer.setLocation_real('x', field.getSize()[0] - location_center[0] - block_size);
		}
		if(field.getSize()[1] < resolution[1]) {
			developer.setLocation_real('y', (field.getSize()[1] - block_size) / 2);
		}else if(developer.getLocation_real()[1] < location_center[1] || field.getSize()[1] < resolution[1]) {
			developer.setLocation_real('y', location_center[1]);
		}else if(developer.getLocation_real()[1] > field.getSize()[1] - location_center[1] - block_size) {
			developer.setLocation_real('y', field.getSize()[1] - location_center[1] - block_size);
		}
		
		return developer;
	}
	public void draw_grid(int[] resolution, Graphics g) {
		g.setColor(new Color(0, 0, 0, 128));
		for(int i = 1; i < resolution[0]/block_size; i++) {
			g.drawLine(block_size*i, 0, block_size*i, resolution[1]);
		}
		for(int i = 1; i < resolution[1]/block_size; i++) {
			g.drawLine(0, block_size*i, resolution[0], block_size*i);
		}
		g.setColor(new Color(0, 0, 0));
	}
	public boolean isMouseOn() {
		return (location_mouseover[0] != -1);
	}
	public boolean is_on_edit_box() {
		JPanel box = edit_box.getBox();
		if(location_mouseover[0] >= box.getX() && 
				location_mouseover[0] <= box.getX() + block_size*(EDIT_MENU_SIZE-1)) {
			return true;
		}else {
			return false;
		}
	}
	public void draw_selected_area(Graphics g) {
		g.setColor(new Color(0, 0, 0, 128));
		g.fillRect(location_mouseover[0], location_mouseover[1], block_size, block_size);
		g.setColor(new Color(0, 0, 0));
	}
	public boolean isClicked() {
		return (location_clicked[0] != -1);
	}
	public void draw_clicked_area(Graphics g) {
		g.setColor(new Color(0, 0, 0, 192));
		g.fillRect(location_clicked[0], location_clicked[1], block_size, block_size);
		g.setColor(new Color(0, 0, 0));
	}
	public void show_edit_menu() {
		try {
			edit_box.getBox(); // Check that if exist edit_box in viewer_world.
		}catch(NullPointerException npe) { // Create the edit_box.
			edit_box = new Edit_box(resolution, block_size, EDIT_MENU_SIZE, 
					color_background, color_content, mariadb_develop.load_info_sprites(), viewer_world);
			edit_box.getButton_apply().addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					// TODO Auto-generated method stub
					if(mariadb_develop.update_event(edit_box)) {
						reload_field();
						exit_edit_box();
					}else {
						viewer_world.requestFocus();
					}
				}
			});
			edit_box.getButton_cancel().addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					// TODO Auto-generated method stub
					exit_edit_box();
				}
			});
			edit_box.getButton_delete().addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					// TODO Auto-generated method stub
					if(mariadb_develop.delete_event(edit_box)) {
						reload_field();
						exit_edit_box();
					}else {
						viewer_world.requestFocus();
					}
				}
			});
			viewer_world.add(edit_box.getBox());
		}
		int[] location_real_on_view_first = {developer.getLocation_real()[0] - developer.getLocation_view()[0],
				developer.getLocation_real()[1] - developer.getLocation_view()[1]};
		// Update the edit_box.
		container_event = mariadb_develop.load_event(field.getName(), 
				location_real_on_view_first[0] + location_clicked[0], 
				location_real_on_view_first[1] + location_clicked[1]);
		edit_box.update_edit_box(container_event, field.getName(), 
				location_real_on_view_first[0], location_real_on_view_first[1], 
				location_clicked[0], location_clicked[1]);
	}
	
	public boolean keyevent_develop(int keycode, boolean need_repaint) {
		if(isClicked()){
			switch(keycode) {
			case KeyEvent.VK_X:
				exit_edit_box();
				break;
			default:
				need_repaint = false;
			}
		}else {
			switch(keycode) {
			case KeyEvent.VK_D:
				if(developer.getLocation_real()[0] + block_size < field.getSize()[0] - location_center[0]) {
					developer.setLocation_real('x', developer.getLocation_real()[0] + developer.getSize()[0]);
				}
				break;
			case KeyEvent.VK_S:
				if(developer.getLocation_real()[1] + block_size < field.getSize()[1] - location_center[1]) {
					developer.setLocation_real('y', developer.getLocation_real()[1] + developer.getSize()[1]);
				}
				break;
			case KeyEvent.VK_A:
				if(developer.getLocation_real()[0] > location_center[0]) {
					developer.setLocation_real('x', developer.getLocation_real()[0] - developer.getSize()[0]);
				}
				break;
			case KeyEvent.VK_W:
				if(developer.getLocation_real()[1] > location_center[1]) {
					developer.setLocation_real('y', developer.getLocation_real()[1] - developer.getSize()[1]);
				}
				break;
			case KeyEvent.VK_X: // This is... For design the map. maybe, TEMP?
				viewer_world.setPlayer(mode_change());
				viewer_world.setEvent_type("basic");
				break;
			default:
				need_repaint = false;
			} // End of switch(keycode).
		}
		return need_repaint;
	} // End of keyevent_basic().
	
	private void reload_field() {
		field = mariadb_develop.load_field(field.getName());
		viewer_world.setSprites(mariadb_develop.load_sprite(field.getName()));
		viewer_world.repaint();
	}
	private void exit_edit_box() {
		viewer_world.remove(edit_box.getBox());
		edit_box = null;
		location_clicked[0] = -1;
		location_clicked[1] = -1;
	}
	
	public boolean getMode() {
		return mode;
	}
	public int[] getLocation_mouseover() {
		return location_mouseover;
	}
	public Sprite getPlayer_just_show() {
		return player_just_show;
	}
	
	public void setLocation_mouseover(int x, int y) {
		location_mouseover[0] = x;
		location_mouseover[1] = y;
	}
	public void setLocation_clicked_as_mouseover() {
		location_clicked[0] = location_mouseover[0];
		location_clicked[1] = location_mouseover[1];
	}
}
