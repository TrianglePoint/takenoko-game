package moveMove;

import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

import moveMove.db.MariaDB;
import moveMove.develop.Develop;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

// Paint the map and sprite on Viewer_world.
public class Viewer_world extends JPanel{
	private Cut_string cut_string = null; // Used cut the string.
	
	private JFrame root_frame = null; // Used move the location of frame.
	private int[] location_mouse_on_frame = {-1, -1};
	
	// The Size of block = (40, 40).
	private final int BLOCK_SIZE = 40;
	
	private MariaDB mariadb = null;
	
	private Thread thread_cutscene = null;

	// resolution is should set that be an odd number when divide with BLOCK_SIZE.
	// map size too.
	private final int[] RESOLUTION = {840, 600};
	
	private Clock clock = null; // The game that exist the time.
	private JLabel label_clock = null;
	
	private Sprite field = null;
	private Sprite player = null;
	private List<Sprite> sprites = null;
	private List<Item> items = null;

	private JPanel health_point_box = null; // Of player.
	private JPanel health_point = null;
	
	private final int KEY_ATTACK = 90; // Key 'Z'.
	
	private JPanel dialog_box = null; // For every event except special event.
	private JLabel dialog = null;
	private JLabel next_label = null;
	
	private JPanel select_box = null; // For select event.
	private JLabel select_yes = null;
	private JLabel select_no = null;
	private int select_yes_or_no = -1;
	
	private JPanel list_box = null; // For merchant, and inventory.
	private List<JLabel> list_item = null;
	private final int MAX_LIST_LENGTH = 7;
	private int list_length_view = -1;
	private int index_item_real = -1;
	private int index_item_view = -1;
	private JLabel money_label = null;
	private int money = 0;
	
	private Show_got_money_thread show_got_money_thread = null;
	
	private final int CONDITION_GOOD_ENDING = 200000;
	
	private int fade_alpha = -1; // For fade in, out.
	
	private Loading_thread loading_thread = null;
	private boolean need_loading = false;

	private final int[] VALUE_COLOR_BACKGROUND = {33, 33, 33, 128};
	private final int[] VALUE_COLOR_CONTENT = {200, 200, 200, 255};
	private final int[] VALUE_COLOR_SELECTED = {255, 212, 128, 255};
	private Color color_background = new Color(VALUE_COLOR_BACKGROUND[0], VALUE_COLOR_BACKGROUND[1], VALUE_COLOR_BACKGROUND[2], VALUE_COLOR_BACKGROUND[3]);
	private Color color_content = new Color(VALUE_COLOR_CONTENT[0], VALUE_COLOR_CONTENT[1], VALUE_COLOR_CONTENT[2], VALUE_COLOR_CONTENT[3]);
	private Color color_selected = new Color(VALUE_COLOR_SELECTED[0], VALUE_COLOR_SELECTED[1], VALUE_COLOR_SELECTED[2], VALUE_COLOR_SELECTED[3]);
	
	private final int[] VALUE_COLOR_GREEN = {51, 255, 51, 255}; // Used health point bar.
	private final int[] VALUE_COLOR_YELLOW = {255, 255, 51, 255};
	private final int[] VALUE_COLOR_RED = {255, 51, 51, 255};
	private Color color_green = new Color(VALUE_COLOR_GREEN[0], VALUE_COLOR_GREEN[1], VALUE_COLOR_GREEN[2], VALUE_COLOR_GREEN[3]);
	private Color color_yellow = new Color(VALUE_COLOR_YELLOW[0], VALUE_COLOR_YELLOW[1], VALUE_COLOR_YELLOW[2], VALUE_COLOR_YELLOW[3]);
	private Color color_red = new Color(VALUE_COLOR_RED[0], VALUE_COLOR_RED[1], VALUE_COLOR_RED[2], VALUE_COLOR_RED[3]);
	private final double[] VALUE_COLOR_AS_HEALTH_PERCENT = {0.05, 0.30};
	
	private int number_record = 0;
	
	private String event_type = "";
	private boolean dont_access_clock = false;
	
	private String connect_to_address = "localhost:3307/movemove"; 
	
	private Develop develop = null;
	
	public Viewer_world(JFrame root_frame) {
		cut_string = new Cut_string();
		
		this.root_frame = root_frame;
		
		mariadb = new MariaDB(connect_to_address, "player", "hoiLair");

		develop = new Develop(BLOCK_SIZE, this, connect_to_address, color_background, color_content);
		
//		System.out.println("w: " + size_screen[0]
//				+ "\nh : " + size_screen[1] + 
//				"\nx : " +  location_screen[0]+
//				"\ny : " + location_screen[1]);
//		setBounds(location_screen[0], location_screen[1], size_screen[0], size_screen[1]);
		setBounds(0, 0, RESOLUTION[0], RESOLUTION[1]);
		
		setLayout(null);
		
		requestFocus();
		setFocusable(true);

		play_intro();
		
		addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				// TODO Auto-generated method stub
				int keycode = e.getKeyCode();
				boolean need_repaint = true; 

				switch(event_type) {
				case "develop":
					need_repaint = develop.keyevent_develop(keycode, need_repaint);
					break;
				case "intro":
					need_repaint = keyevent_intro(keycode, need_repaint);
					break;
				case "inventory":
					need_repaint = keyevent_inventory(keycode, need_repaint);
					break;
				case "merchant":
					need_repaint = keyevent_merchant(keycode, need_repaint);
					break;
				case "dialog":
					need_repaint = keyevent_dialog(keycode, need_repaint);
					break;
				case "select":
					need_repaint = keyevent_select(keycode, need_repaint);
					break;
				case "immovable":
					// Can't control.
					break;
				case "basic":
					need_repaint = keyevent_basic(player, keycode, need_repaint);
					break;
				default:
					need_repaint = false;
				}
				
				if(need_repaint) {
					repaint();
				}
			} // End of keyPressed().
		}); // End of addKeyListener().
		addMouseListener(new MouseListener() {
			@Override
			public void mouseReleased(MouseEvent e) {
				// TODO Auto-generated method stub
				location_mouse_on_frame[0] = -1;
				location_mouse_on_frame[1] = -1;
			}
			
			@Override
			public void mousePressed(MouseEvent e) {
				// TODO Auto-generated method stub	 
				location_mouse_on_frame[0] = e.getX();
				location_mouse_on_frame[1] = e.getY();
			}
			
			@Override
			public void mouseExited(MouseEvent e) {
				// TODO Auto-generated method stub
				if(develop.getMode()) {
					develop.setLocation_mouseover(-1, -1);
					repaint();
				}
			}
			
			@Override
			public void mouseEntered(MouseEvent e) {
				// TODO Auto-generated method stub	
			}
			
			@Override
			public void mouseClicked(MouseEvent e) {
				// TODO Auto-generated method stub
				if(develop.isMouseOn()) {
					if(field.getSize()[0] < RESOLUTION[0] && 
							(develop.getLocation_mouseover()[0] < field.getLocation_view()[0] || 
									develop.getLocation_mouseover()[0] >= 
									field.getLocation_view()[0] + field.getSize()[0])) {
						return;
					}
					if(field.getSize()[1] < RESOLUTION[1] && 
							(develop.getLocation_mouseover()[1] < field.getLocation_view()[1] || 
									develop.getLocation_mouseover()[1] >= 
									field.getLocation_view()[1] + field.getSize()[1])) {
						return;
					}
					develop.setLocation_clicked_as_mouseover();
					develop.show_edit_menu();
					repaint();
				}
			}
		});
		addMouseMotionListener(new MouseMotionListener() {
			@Override
			public void mouseMoved(MouseEvent e) {
				// TODO Auto-generated method stub
				if(develop.getMode()) {
					int location_previous_x = develop.getLocation_mouseover()[0];
					int location_previous_y = develop.getLocation_mouseover()[1];
					develop.setLocation_mouseover(e.getX() / BLOCK_SIZE * BLOCK_SIZE, 
							e.getY() / BLOCK_SIZE * BLOCK_SIZE);
					if(develop.isClicked() && develop.is_on_edit_box()) {
						develop.setLocation_mouseover(-1, -1);
						repaint();
					}else {
						if(develop.getLocation_mouseover()[0] != location_previous_x || 
								develop.getLocation_mouseover()[1] != location_previous_y) {
							repaint();
						}
					}
				}
			}
			@Override
			public void mouseDragged(MouseEvent e) {
				// TODO Auto-generated method stub
				root_frame.setBounds(e.getXOnScreen() - location_mouse_on_frame[0], 
						e.getYOnScreen() - location_mouse_on_frame[1], RESOLUTION[0], RESOLUTION[1]);
			}
		});
	} // End of constructor.
	private boolean keyevent_intro(int keycode, boolean need_repaint) {
		switch(keycode) {
		case KeyEvent.VK_Z:
			Thread t = new Thread(new Runnable() {
				@Override
				public void run() {
					// TODO Auto-generated method stub
					load_record();
				}
			});
			t.start();
			setEvent_type("");
			thread_cutscene.interrupt();
			need_repaint = false; // repaint() is executed by thread. 
			break;
		case KeyEvent.VK_ESCAPE:
			System.exit(0);
			break;
		default:	
			need_repaint = false;
		}
		return need_repaint;
	}
	private boolean keyevent_inventory(int keycode, boolean need_repaint) {
		switch(keycode) {
		case KeyEvent.VK_UP:
			if(index_item_real > 0) {
				index_item_real -= 1;
				if(index_item_view > 0) {			
					list_item.get(index_item_view).setBackground(color_content);	
					index_item_view -= 1;
					list_item.get(index_item_view).setBackground(color_selected);	
				}else {
					if(list_length_view < MAX_LIST_LENGTH) {
						list_length_view += 1;
					}
					for(int v = 0, r = index_item_real; v < list_length_view; v++, r++) {
						String effect = "";
						if(items.get(r).getType().equals("food")) {
							effect = ", Hp " + items.get(r).getEffect();
						}
						list_item.get(v).setText("  " + items.get(r).getName() + " - Usable : " +  
								items.get(r).getUsable() + ", Amount : " + 
								items.get(r).getAmount() + effect);
					}
				}
				dialog.setText(items.get(index_item_real).getDescription());
			}
			break;
		case KeyEvent.VK_DOWN:
			if(index_item_real < items.size() - 1) {
				index_item_real += 1;
				if(index_item_view < list_length_view - 1) {
					list_item.get(index_item_view).setBackground(color_content);
					index_item_view += 1;
					list_item.get(index_item_view).setBackground(color_selected);
				}else {
					for(int v = list_length_view - 1, r = index_item_real; v >= 0 ; v--, r--) {	
						String effect = "";
						if(items.get(r).getType().equals("food")) {
							effect = ", Hp " + items.get(r).getEffect();
						}
						list_item.get(v).setText("  " + items.get(r).getName() + " - Usable : " +  
								items.get(r).getUsable() + ", Amount : " + 
								items.get(r).getAmount() + effect);
					}
				}
				dialog.setText(items.get(index_item_real).getDescription());
			}
			break;
		case KeyEvent.VK_Z:
			if(items.size() > 0 && items.get(index_item_real).getUsable()) {
				if(items.get(index_item_real).getType().equals("food")) {
					if(player.getHealth_point()[0] == player.getHealth_point()[1]) {
						mariadb.close();
						dialog.setText("Already full the hp.");
						break;
					}
				}
				mariadb.connect();
				if(mariadb.update_inventory(number_record, 
						items.get(index_item_real).getName(), 
						mariadb.load_amount_inventory(number_record, 
								items.get(index_item_real).getName()) - 1)) {
					if(items.get(index_item_real).getType().equals("food")) {
						set_health_point(items.get(index_item_real).getEffect());
					}
					items.get(index_item_real).setAmount(
							items.get(index_item_real).getAmount() - 1);
					if(items.get(index_item_real).getAmount() == 0) {
						items.remove(index_item_real);
						if(index_item_real + list_length_view - index_item_view == items.size() + 1) {
							list_item.get(list_length_view - 1).setText("");
							if(items.size() > 0 && index_item_view == list_length_view - 1) {
								list_item.get(index_item_view).setBackground(color_content);
								index_item_view -= 1;
								index_item_real -= 1;
								list_item.get(index_item_view).setBackground(color_selected);
								
							}
							list_length_view -= 1;
						}
						for(int v = index_item_view, r = index_item_real; v < list_length_view; v++, r++) {
							String effect = "";
							if(items.get(r).getType().equals("food")) {
								effect = ", Hp " + items.get(r).getEffect();
							}
							list_item.get(v).setText("  " + items.get(r).getName() + " - Usable : " +  
									items.get(r).getUsable() + ", Amount : " + 
									items.get(r).getAmount() + effect);
						}
					}else {
						String effect = "";
						if(items.get(index_item_real).getType().equals("food")) {
							effect = ", Hp " + items.get(index_item_real).getEffect();
						}
						list_item.get(index_item_view).setText("  " + items.get(index_item_real).getName() + " - Usable : " +  
								items.get(index_item_real).getUsable() + ", Amount : " + 
								items.get(index_item_real).getAmount() + effect);
					}
				}
				mariadb.close();
				dialog.setText("Yum yum!");
			}
			break;
		case KeyEvent.VK_I:
		case KeyEvent.VK_X:
			synchronized(this) {
				try {
					notify();
				}catch (Exception e) {
					e.printStackTrace();
				}
			}
			need_repaint = false;
			break;
		default:	
			need_repaint = false;
		}
		return need_repaint;
	}
	private boolean keyevent_merchant(int keycode, boolean need_repaint) {
		switch(keycode) {
		case KeyEvent.VK_UP:
			if(index_item_real != -1 && index_item_real > 0) {
				index_item_real -= 1;
				if(index_item_view > 0) {
					list_item.get(index_item_view).setBackground(color_content);
					index_item_view -= 1;
					list_item.get(index_item_view).setBackground(color_selected);
				}else {
					for(int v = 0, r = index_item_real; v < list_length_view; v++, r++) {
						String effect = "";
						if(items.get(r).getType().equals("food")) {
							effect = ", Hp " + items.get(r).getEffect();
						}
						list_item.get(v).setText("  " + items.get(r).getName() + " - Usable : " +  
								items.get(r).getUsable() + ", Price : " + 
								items.get(r).getPrice() + effect);
					}
				}
				dialog.setText(items.get(index_item_real).getDescription());
			}
			break;
		case KeyEvent.VK_DOWN:
			if(index_item_real != -1 && index_item_real < items.size() - 1) {
				index_item_real += 1;
				if(index_item_view < list_length_view - 1) {
					list_item.get(index_item_view).setBackground(color_content);
					index_item_view += 1;
					list_item.get(index_item_view).setBackground(color_selected);
				}else {
					for(int v = list_length_view - 1, r = index_item_real; v >= 0 ; v--, r--) {
						String effect = "";
						if(items.get(r).getType().equals("food")) {
							effect = ", Hp " + items.get(r).getEffect();
						}
						list_item.get(v).setText("  " + items.get(r).getName() + " - Usable : " +  
								items.get(r).getUsable() + ", Price : " + 
								items.get(r).getPrice() + effect);
					}
				}
				dialog.setText(items.get(index_item_real).getDescription());
			}
			break;
		case KeyEvent.VK_Z:
			if(index_item_real == -1) {
				index_item_real = 0;
				index_item_view = 0;
				list_item.get(index_item_view).setBackground(color_selected);
				
				dialog.setText(items.get(index_item_real).getDescription());
			}else {
				int price = items.get(index_item_real).getPrice();
				mariadb.connect();
				if(money >= price && mariadb.update_inventory(number_record, "money", 
						money - price)) {
					mariadb.update_inventory(number_record, 
							items.get(index_item_real).getName(), 
							mariadb.load_amount_inventory(number_record, items.get(index_item_real).getName()) + 1);
					money -= price;
					money_label.setText("" + money);
					dialog.setText("Thanks!");
				}else {
					dialog.setText("No money, no item.");
				}
				mariadb.close();
			}
			break;
		case KeyEvent.VK_X:
			synchronized(this) {
				try {
					notify();
				}catch (Exception e) {
					e.printStackTrace();
				}
			}
			need_repaint = false;
			break;
		default:	
			need_repaint = false;
		}
		return need_repaint;
	}
	private boolean keyevent_dialog(int keycode, boolean need_repaint) {
		switch(keycode) {
		case KeyEvent.VK_Z:
			synchronized(this) {
				try {
					notify();
				}catch(Exception e) {
					e.printStackTrace();
				}
			}
			need_repaint = false; // repaint() is executed by thread. 
			break;
		default:	
			need_repaint = false;
		}
		return need_repaint;
	}
	private boolean keyevent_select(int keycode, boolean need_repaint) {
		switch(keycode) {
		case KeyEvent.VK_UP:
			if(select_yes_or_no == 2) {
				select_no.setBackground(color_content);
				select_yes.setBackground(color_selected);
				select_yes_or_no = 1;
			}
			break;
		case KeyEvent.VK_DOWN:
			if(select_yes_or_no == 1) {
				select_yes.setBackground(color_content);
				select_no.setBackground(color_selected);
				select_yes_or_no = 2;
			}
			break;
		case KeyEvent.VK_Z:
			synchronized(this) {
				try {
					notify();
					if(select_yes_or_no == 1) { // Only one exist select event. it is sleep.
						clock.do_sleep();
					}
				}catch(Exception e) {
					e.printStackTrace();
				}
			}
			need_repaint = false;
			break;
		default:
			need_repaint = false;
		}
		return need_repaint;
	}
	private boolean keyevent_basic(Sprite actor, int keycode, boolean need_repaint) {
		boolean can_move = true;
		switch(keycode) {
		case KeyEvent.VK_RIGHT:
			if(actor.getLocation_real()[0] + BLOCK_SIZE < field.getSize()[0]) {
				for(int i = 0; i < sprites.size(); i++) {							
					// actor can't move to location of other object at common case.
					if(actor.getLocation_real()[1] == sprites.get(i).getLocation_real()[1] && 
							actor.getLocation_real()[0] + BLOCK_SIZE == sprites.get(i).getLocation_real()[0]) {
						if(sprites.get(i).getType().equals("money")) {
							mariadb.connect();
							int got_money = mariadb.load_item(sprites.get(i).getName()).getPrice();
							mariadb.update_inventory(number_record, "money", 
									mariadb.load_amount_inventory(number_record, "money") + 
									got_money);
							if(show_got_money_thread != null) {
								show_got_money_thread.interrupt();
								show_got_money_thread = null;
							}
							show_got_money_thread = new Show_got_money_thread(
									got_money, this, color_background);
							show_got_money_thread.start();
							mariadb.close();
							sprites.remove(i);
						}else if(sprites.get(i).getType().equals("destroyed")) {}
						else{
							can_move = false;
						}
						break;
					}
				}
				if(can_move) {
					actor.setLocation_real('x', actor.getLocation_real()[0] + actor.getSize()[0]);
				}
			}
			actor.setDirection(3);
			break;
		case KeyEvent.VK_DOWN:
			if(actor.getLocation_real()[1] + BLOCK_SIZE < field.getSize()[1]) {
				for(int i = 0; i < sprites.size(); i++) {							
					// actor can't move to location of other object at common case.
					if(actor.getLocation_real()[0] == sprites.get(i).getLocation_real()[0] && 
							actor.getLocation_real()[1] + BLOCK_SIZE == sprites.get(i).getLocation_real()[1]) {
						if(sprites.get(i).getType().equals("money")) {
							mariadb.connect();
							int got_money = mariadb.load_item(sprites.get(i).getName()).getPrice();
							mariadb.update_inventory(number_record, "money", 
									mariadb.load_amount_inventory(number_record, "money") + 
									got_money);
							if(show_got_money_thread != null) {
								show_got_money_thread.interrupt();
								show_got_money_thread = null;
							}
							show_got_money_thread = new Show_got_money_thread(
									got_money, this, color_background);
							show_got_money_thread.start();
							mariadb.close();
							sprites.remove(i);
						}else if(sprites.get(i).getType().equals("destroyed")) {}
						else{
							can_move = false;
						}
						break;
					}
				}
				if(can_move) {
					actor.setLocation_real('y', actor.getLocation_real()[1] + actor.getSize()[1]);
				}
			}
			actor.setDirection(6);
			break;
		case KeyEvent.VK_LEFT:
			if(actor.getLocation_real()[0] > 0) {
				for(int i = 0; i < sprites.size(); i++) {							
					// actor can't move to location of other object at common case.
					if(actor.getLocation_real()[1] == sprites.get(i).getLocation_real()[1] && 
							actor.getLocation_real()[0] - BLOCK_SIZE == sprites.get(i).getLocation_real()[0]) {
						if(sprites.get(i).getType().equals("money")) {
							mariadb.connect();
							int got_money = mariadb.load_item(sprites.get(i).getName()).getPrice();
							mariadb.update_inventory(number_record, "money", 
									mariadb.load_amount_inventory(number_record, "money") + 
									got_money);
							if(show_got_money_thread != null) {
								show_got_money_thread.interrupt();
								show_got_money_thread = null;
							}
							show_got_money_thread = new Show_got_money_thread(
									got_money, this, color_background);
							show_got_money_thread.start();
							mariadb.close();
							sprites.remove(i);
						}else if(sprites.get(i).getType().equals("destroyed")) {}
						else{
							can_move = false;
						}
						break;
					}
				}
				if(can_move) {
					actor.setLocation_real('x', actor.getLocation_real()[0] - actor.getSize()[0]);
				}
			}
			actor.setDirection(9);
			break;
		case KeyEvent.VK_UP:
			if(actor.getLocation_real()[1] > 0) {
				for(int i = 0; i < sprites.size(); i++) {							
					// actor can't move to location of other object at common case.
					if(actor.getLocation_real()[0] == sprites.get(i).getLocation_real()[0] && 
							actor.getLocation_real()[1] - BLOCK_SIZE == sprites.get(i).getLocation_real()[1]) {
						if(sprites.get(i).getType().equals("money")) {
							mariadb.connect();
							int got_money = mariadb.load_item(sprites.get(i).getName()).getPrice();
							mariadb.update_inventory(number_record, "money", 
									mariadb.load_amount_inventory(number_record, "money") + 
									got_money);
							if(show_got_money_thread != null) {
								show_got_money_thread.interrupt();
								show_got_money_thread = null;
							}
							show_got_money_thread = new Show_got_money_thread(
									got_money, this, color_background);
							show_got_money_thread.start();
							mariadb.close();
							sprites.remove(i);
						}else if(sprites.get(i).getType().equals("destroyed")) {}
						else{
							can_move = false;
						}
						break;
					}
				}
				if(can_move) {
					actor.setLocation_real('y', actor.getLocation_real()[1] - actor.getSize()[1]);
				}
			}
			actor.setDirection(12);
			break;
		case KeyEvent.VK_Z:
			int direction = actor.getDirection();
			
			switch(direction) {
			case 3:
				for(int i = 0; i < sprites.size(); i++) {
					if(actor.getLocation_real()[1] == sprites.get(i).getLocation_real()[1] && 
							actor.getLocation_real()[0] + BLOCK_SIZE == sprites.get(i).getLocation_real()[0]) {
						sprites.get(i).setDirection(9);
						need_repaint = execute_event(actor, sprites.get(i));
						break;
					}
				}
				break;
			case 6:
				for(int i = 0; i < sprites.size(); i++) {
					if(actor.getLocation_real()[0] == sprites.get(i).getLocation_real()[0] && 
							actor.getLocation_real()[1] + BLOCK_SIZE == sprites.get(i).getLocation_real()[1]) {
						sprites.get(i).setDirection(12);
						need_repaint = execute_event(actor, sprites.get(i));
						break;
					}
				}
				break;
			case 9:
				for(int i = 0; i < sprites.size(); i++) {
					if(actor.getLocation_real()[1] == sprites.get(i).getLocation_real()[1] && 
							actor.getLocation_real()[0] - BLOCK_SIZE == sprites.get(i).getLocation_real()[0]) {
						sprites.get(i).setDirection(3);
						need_repaint = execute_event(actor, sprites.get(i));
						break;
					}
				}
				break;
			case 12:
				for(int i = 0; i < sprites.size(); i++) {
					if(actor.getLocation_real()[0] == sprites.get(i).getLocation_real()[0] && 
							actor.getLocation_real()[1] - BLOCK_SIZE == sprites.get(i).getLocation_real()[1]) {
						sprites.get(i).setDirection(6);
						need_repaint = execute_event(actor, sprites.get(i));
						break;
					}
				}
				break;
			default:
				System.err.println("ERROR - Incorrect actor's direction : " + direction);
			} // End of switch(direction).
			break;
		case KeyEvent.VK_I:
			open_inventory();
			break;
		
		case KeyEvent.VK_D: // This is... For design the map. maybe, TEMP?
			player = develop.mode_change(player, field);
			setEvent_type("develop");
			break;
			
		// TEMP.
		case KeyEvent.VK_ESCAPE:
			mariadb.connect();
			mariadb.save_record(field, player, number_record, clock);
			mariadb.close();
			System.exit(0);
			break;
			
		default:
			need_repaint = false;
		} // End of switch(keycode).
		return need_repaint;
	} // End of keyevent_basic().
	
	@Override
	protected void paintComponent(Graphics g) {
		// TODO Auto-generated method stub
		super.paintComponent(g);
		
		// Keep the order of sprite.
		calculate_draw(g, field);
		calculate_draw(g, player);
		for(int i = 0; i < sprites.size(); i++) {
			calculate_draw(g, sprites.get(i));
		}
		if(fade_alpha != -1) {
			g.setColor(new Color(0, 0, 0, fade_alpha));
			g.fillRect(0, 0, RESOLUTION[0], RESOLUTION[1]);
			g.setColor(Color.BLACK);
		}
		if(need_loading) {
			if(loading_thread == null) {
				String[] loading_images = {"takenoko.png", "takenoko_left.png", 
						"takenoko_up.png", "takenoko_right.png"};
				loading_thread = new Loading_thread(loading_images, this);
				loading_thread.start();
			}else {
				g.setColor(new Color(0, 0, 0));
				g.fillRect(0, 0, RESOLUTION[0], RESOLUTION[1]);
				g.setColor(Color.BLACK);
				g.drawImage(loading_thread.getImage_as_direction(), 
						RESOLUTION[0] / 20 * 17, RESOLUTION[1] / 5 * 4,
						loading_thread.getSize()[0], loading_thread.getSize()[1], null);
			}
		}else {
			// Remove the loading image.
			if(loading_thread != null) {
				loading_thread.interrupt();
				loading_thread = null;
			}
		}
		if(develop.getMode()) {
			calculate_draw(g, develop.getPlayer_just_show());
			develop.draw_grid(RESOLUTION, g);
			if(develop.isMouseOn()) {
				develop.draw_selected_area(g);
			}
			if(develop.isClicked()) {
				develop.draw_clicked_area(g);
			}
		}
		
		
//		if(player.getLocation_view()[0] == RESOLUTION[0]/2-BLOCK_SIZE/2)
//			System.out.println("You stand the center of y-axis!");
//		if(player.getLocation_view()[1] == RESOLUTION[1]/2-BLOCK_SIZE/2)
//			System.out.println("You stand the center of x-axis!");
	}
	
	// Calculate and draw with real location of the sprite.
	private void calculate_draw(Graphics g, Sprite sprite) {
		int[] location_center = {RESOLUTION[0]/2-BLOCK_SIZE/2, RESOLUTION[1]/2-BLOCK_SIZE/2};
		
		switch(sprite.getType()) {
		case "player":
			// player and field is should draw at always.
			if(field.getSize()[0] < RESOLUTION[0]) {
				sprite.setLocation_view('x', (RESOLUTION[0] - field.getSize()[0]) / 2 + 
						sprite.getLocation_real()[0]);
			}
			else if(player.getLocation_real()[0] < location_center[0]) {
				sprite.setLocation_view('x', sprite.getLocation_real()[0]);
			}else if(player.getLocation_real()[0] > (field.getSize()[0] - location_center[0] - BLOCK_SIZE)) {
				sprite.setLocation_view('x', RESOLUTION[0] - field.getSize()[0] + player.getLocation_real()[0]);
			}else {
				sprite.setLocation_view('x', location_center[0]);
			}
			if(field.getSize()[1] < RESOLUTION[1]) {
				sprite.setLocation_view('y', (RESOLUTION[1] - field.getSize()[1]) / 2 + 
						sprite.getLocation_real()[1]);
			}
			else if(player.getLocation_real()[1] < location_center[1]) {
				sprite.setLocation_view('y', sprite.getLocation_real()[1]);
			}else if(player.getLocation_real()[1] > (field.getSize()[1] - location_center[1] - BLOCK_SIZE)) {
				sprite.setLocation_view('y', RESOLUTION[1] - field.getSize()[1] + player.getLocation_real()[1]);
			}else {
				sprite.setLocation_view('y', location_center[1]);
			}
			g.drawImage(sprite.getImage_as_direction(), sprite.getLocation_view()[0], sprite.getLocation_view()[1],
					sprite.getSize()[0], sprite.getSize()[1], null);
			break;
		case "field":
			if(field.getSize()[0] < RESOLUTION[0]) {
				g.setColor(new Color(0, 0, 0));
				g.fillRect(0, 0, RESOLUTION[0], RESOLUTION[1]);
				g.setColor(Color.BLACK);
				sprite.setLocation_view('x', (RESOLUTION[0] - sprite.getSize()[0]) / 2);
			}
			else if(player.getLocation_real()[0] < location_center[0]) {
				sprite.setLocation_view('x', 0);
			}else if(player.getLocation_real()[0] > (field.getSize()[0] - location_center[0] - BLOCK_SIZE)) {
				sprite.setLocation_view('x', RESOLUTION[0] - sprite.getSize()[0]);
			}else {
				sprite.setLocation_view('x', location_center[0] - player.getLocation_real()[0]);
			}
			if(field.getSize()[1] < RESOLUTION[1]) {
				sprite.setLocation_view('y', (RESOLUTION[1] - sprite.getSize()[1]) / 2);
			}
			else if(player.getLocation_real()[1] < location_center[1]) {
				sprite.setLocation_view('y', 0);
			}else if(player.getLocation_real()[1] > (field.getSize()[1] - location_center[1] - BLOCK_SIZE)) {
				sprite.setLocation_view('y', RESOLUTION[1] - sprite.getSize()[1]);
			}else {
				sprite.setLocation_view('y', location_center[1] - player.getLocation_real()[1]);
			}
			g.drawImage(sprite.getImage().get(0), sprite.getLocation_view()[0], sprite.getLocation_view()[1],
					sprite.getSize()[0], sprite.getSize()[1], null);
			break;
		case "destroyed":
			if(player.getLocation_real()[0] == sprite.getLocation_real()[0] && 
			player.getLocation_real()[1] == sprite.getLocation_real()[1]) {
				break; // Sprite is not draw if player over sprite.
			}
		case "wall":
			if(sprite.getName_image().get(0).equals("transparent_wall.png") && !(develop.getMode())) {
				break;
			}
		default:
			// Draw the sprite when close with player.
			// it is mean sprite exist on visible field at current.
			
			// A real point of start and end on visible field.
			int[] location_real_on_view_first = {player.getLocation_real()[0] - player.getLocation_view()[0],
					player.getLocation_real()[1] - player.getLocation_view()[1]};
			int[] location_real_on_view_last = {location_real_on_view_first[0] + RESOLUTION[0] - BLOCK_SIZE,
					location_real_on_view_first[1] + RESOLUTION[1] - BLOCK_SIZE};
			
			// player can see this sprite.
			if(sprite.getLocation_real()[0] >= location_real_on_view_first[0] && 
					sprite.getLocation_real()[0] <= location_real_on_view_last[0] &&
					sprite.getLocation_real()[1] >= location_real_on_view_first[1] &&
					sprite.getLocation_real()[1] <= location_real_on_view_last[1]) {
				sprite.setLocation_view(sprite.getLocation_real()[0] - location_real_on_view_first[0], 
						sprite.getLocation_real()[1] - location_real_on_view_first[1]);
				g.drawImage(sprite.getImage_as_direction(), sprite.getLocation_view()[0], sprite.getLocation_view()[1],
						sprite.getSize()[0], sprite.getSize()[1], null);
			}
		} // End of switch(sprite.getType()).
	} // End of calculate_draw().
	
	public void fade_in(int period) { // Run in Thread.
		fade_alpha = 255;
		int gap = period / 255;
		while(fade_alpha >= 0) {
			repaint();
			try {
				Thread.sleep(gap);
			}catch (Exception e) {
				e.printStackTrace();
			}
			fade_alpha -= 1;
		}
		try {
			label_clock.setVisible(true);
			health_point_box.setVisible(true);
		}catch(NullPointerException npe) {}
		fade_alpha = -1;
	}
	public void fade_out(int period) { // Run in Thread.
		if(fade_alpha == -1) {
			fade_alpha = 0;
		}
		try {
			label_clock.setVisible(false);
			health_point_box.setVisible(false);
		}catch(NullPointerException npe) {}
		int gap = period / 255;
		while(fade_alpha <= 255) {
			repaint();
			try {
				Thread.sleep(gap);
			}catch (Exception e) {
				e.printStackTrace();
			}
			fade_alpha += 1;
		}
		fade_alpha = -1;
	}
	private void play_intro() {
		mariadb.connect();
		field = mariadb.load_field("the forest of bamboo(intro)");
		sprites = mariadb.load_sprite(field.getName());
		String[] name_image = {"transparent_box.png", 
				"transparent_box.png", 
				"transparent_box.png", 
				"transparent_box.png"};
		player = new Sprite("camera", "player", name_image, 40, 40, 6, "", false);
		mariadb.close();
		thread_cutscene = new Thread(new Runnable() {
			@Override
			public void run() {
				fade_alpha = 255;
				repaint();
				try {
					Thread.sleep(1000);
					fade_in(1000);
					setEvent_type("intro");
					field.setShining_stack(field.getSHINING_POINT() - 1);
					for(int i = 0; i < sprites.size(); i++) {
						if(sprites.get(i).getExist_script()) {
							// Only exist the one event on this field.
							execute_script(sprites.get(i), "intro");	
							break;
						}
					}
				}catch(InterruptedException ie) {
				}catch(Exception e) {
					e.printStackTrace();
					return;
				}finally {
					if(event_type == "intro") {
						load_record();
					}
				}
			}
		});
		thread_cutscene.start();
	}
	public void play_ending() {
		remove(label_clock);
		remove(health_point_box);
		mariadb.connect();
		money = mariadb.load_amount_inventory(number_record, "money");
		String temp_ending = "";
		if(money >= CONDITION_GOOD_ENDING) {
			// Good ending.
			field = mariadb.load_field("home outer(good ending)");
			temp_ending = "(good ending)";
		}else {
			// Bad ending.
			field = mariadb.load_field("home outer(bad ending)");
			temp_ending = "(bad ending)";
		}
		final String HOW_ENDING = temp_ending; 
		sprites = mariadb.load_sprite(field.getName());
		String[] name_image = {"transparent_box.png", 
				"transparent_box.png", 
				"transparent_box.png", 
				"transparent_box.png"};
		player = new Sprite("camera", "player", name_image, 120, 40, 6, "", false);
		mariadb.close();
		thread_cutscene = new Thread(new Runnable() {
			@Override
			public void run() {
				fade_alpha = 255;
				repaint();
				try {
					Thread.sleep(1000);
					fade_in(2000);
					setEvent_type("ending");
					for(int i = 0; i < sprites.size(); i++) {
						if(sprites.get(i).getName().equals("竹の子"+HOW_ENDING)) {
							execute_script(sprites.get(i), "ending");
							break;
						}
					}
					fade_out(2000);
					mariadb.connect();
					mariadb.delete_record(number_record);
					mariadb.close();
					System.exit(0);
				}catch(InterruptedException ie) {
				}catch(Exception e) {
					e.printStackTrace();
					return;
				}
			}
		});
		thread_cutscene.start();
	}
	private void load_record() { // Run in thread.
		setEvent_type("");
		label_clock = create_label_clock();
		clock = new Clock(label_clock, this);
		health_point_box = create_health_point_box();
		health_point = create_health_point(health_point_box);
		health_point_box.add(health_point);
		Thread t = new Thread(new Runnable() {
			@Override
			public void run() {
				// TODO Auto-generated method stub
				fade_out(1000);
				mariadb.connect();
				number_record = 1;
				Sprite[] player_and_field = {null, null}; // This is only used at load the record.
				player_and_field = mariadb.load_record(number_record, clock);
				player = player_and_field[0];
				player.setHealth_point(1, mariadb.load_stats("player")[0]);
				player.setHealth_point(0, 
						mariadb.load_amount_inventory(number_record, "health_point"));
				set_health_point(0);
				field = player_and_field[1];
				sprites = mariadb.load_sprite(field.getName());
				mariadb.close();
				if(clock.getTime() == 0 && clock.getDay() == 1 && 
						field.getName().equals("home inner")) { // If new game.
					regeneration_sprites();
				}
				repaint();
				fade_in(1000);
				add(label_clock);
				add(health_point_box);
				repaint(); // For show the health_point.
				setEvent_type("basic");
				clock.start();
				if(clock.getTime() == 0 && clock.getDay() == 1 && 
						field.getName().equals("home inner")) { // If new game.
					setEvent_type("dialog");
					for(int i = 0; i < sprites.size(); i++) {
						if(sprites.get(i).getType().equals("merchant")) {
							sprites.get(i).setType("object");
							execute_script(sprites.get(i), event_type);
							sprites.get(i).setType("merchant");
							setEvent_type("basic");
							break;
						}
					}
				}
			}
		});
		t.start();
	}
	private void execute_script(Sprite sprite, String event_type_original) { // Run in Thread.
		String name_event = sprite.getName();
		int group_number = 1; // First run, load the script that group number is 1.
		do {
			if(!(sprite.getExist_script())) {
				System.err.println("ERROR - " + sprite.getName() + " is not have script.");
				return;
			}
			mariadb.connect();
			List<String[]> scripts = mariadb.load_script(name_event, group_number);
			mariadb.close();
			group_number = -1;
			for(int i = 0; i < scripts.size(); i++) {
				switch(scripts.get(i)[0]) {
				case "action":
					switch(scripts.get(i)[1]) {
					case "attack":
						keyevent_basic(sprite, KEY_ATTACK, false);
						break;
					default:
						System.err.println("ERRROR - scripts[" + i + "] Wrong action : " + 
								scripts.get(i)[1]);
						return;
					}
					break;
				case "move":
					int range = -1;
					try{
						range = Integer.parseInt(scripts.get(i)[2]);
					}catch(NumberFormatException nfe) {
						System.err.println("ERROR - scripts[" + i + "] Not numbers : " + 
								scripts.get(i)[2]);
						return;
					}
					for(int j = 0; j < range; j++) {
						switch(scripts.get(i)[1]) {
						case "right":
							keyevent_basic(sprite, 39, false);
							break;
						case "down":
							keyevent_basic(sprite, 40, false);
							break;
						case "left":
							keyevent_basic(sprite, 37, false);
							break;
						case "up":
							keyevent_basic(sprite, 38, false);
							break;
						default:
							System.err.println("ERROR - scripts[" + i + "] Wrong direction : " + 
									scripts.get(i)[1]);
							return;
						}
						repaint();
						try {
							Thread.sleep(350);
						}catch(InterruptedException ie) {
							return;
						}catch (Exception e) {
							e.printStackTrace();
							return;
						}
					}
					break;
				case "turn":
					switch(scripts.get(i)[1]) {
					case "right":
						sprite.setDirection(3);
						break;
					case "down":
						sprite.setDirection(6);
						break;
					case "left":
						sprite.setDirection(9);
						break;
					case "up":
						sprite.setDirection(12);
						break;
					default:
						System.err.println("ERROR - scripts[" + i + "] Wrong direction : " + 
								scripts.get(i)[1]);
						return;
					}
					repaint();
					break;
				case "say":
					dialog_box = create_dialog_box();
					add(dialog_box);
					dialog = create_dialog(dialog_box);
					dialog_box.add(dialog);
					String name_in_html = "";
					if(sprite.getType().equals("object")) {
						name_in_html = "<html>" + sprite.getName() + "<br />";
					}else {
						name_in_html = "<html>";
					}
					name_in_html += "&nbsp;&nbsp;&nbsp;&nbsp;";
					dialog.setText(name_in_html + scripts.get(i)[1] + "</html>");
					try {
						if(scripts.get(i + 1)[0].equals("select")) {
							break;
						}
					}catch(IndexOutOfBoundsException ioobe) {
						// None select script. end of scripts.
					}
					try{
						if(Integer.parseInt(scripts.get(i)[2]) != -1) {
							repaint();
							Thread.sleep(Integer.parseInt(scripts.get(i)[2]));
						}else {
							synchronized (this) {
								next_label = create_next_label_in_dialog_box(dialog_box);
								dialog_box.add(next_label);
								repaint();
								wait();
							}
						}
					}catch(NumberFormatException nfe) {
						System.err.println("ERROR - scripts[" + i + "] Not numbers : " + 
								scripts.get(i)[2]);
						return;
					}catch(InterruptedException ie) {
						remove(dialog_box);
						repaint();
						return;
					}catch(Exception e) {
						e.printStackTrace();
						return;
					}
					remove(dialog_box);
					break;
				case "select":
					select_box = create_select_box();
					add(select_box);
					select_no.setBackground(color_selected);
					select_yes_or_no = 2;
					repaint();
					setEvent_type("select");
					try {
						synchronized (this) {
							wait();
						}
					}catch(Exception e) {
						e.printStackTrace();
						return;
					}
					setEvent_type(event_type_original);
					try {
						group_number = Integer.parseInt(scripts.get(i)[select_yes_or_no]);
					}catch(NumberFormatException nfe) {
						System.err.println("ERROR - scripts[" + i + "] Not numbers : " + 
								scripts.get(i)[2]);
						return;
					}
					remove(dialog_box);
					remove(select_box);
					break;
				case "sleep":
					try {
						Thread.sleep(Integer.parseInt(scripts.get(i)[1]));
					}catch(InterruptedException ie) {
						return;
					}catch(Exception e) {
						e.printStackTrace();
						return;
					}
					break;
				case "call":
					name_event = scripts.get(i)[1];
					if(!(name_event.equals(sprite.getName()))){
						// Did call the other sprite?
						for(int s = 0; s < sprites.size(); s++) {
							if(name_event.equals(sprites.get(s).getName())) {
								sprite = sprites.get(s); // Change the sprite.
								break;
							}
						}
					}
					group_number = Integer.parseInt(scripts.get(i)[2]);
					break;
				case "remove":
					sprites.remove(sprite);
					break;
				default:
					System.err.println("ERROR - scripts[" + i + "]Wrong type : " + 
							scripts.get(i)[0]);
					return;
				} // End of switch(scripts.get(i)[0]).
				repaint();
			} // End of for loop.
		}while(group_number != -1);
	} // End of execute_script(Sprite sprite).
	private void open_inventory() {
		Runnable r = null;
		
		setEvent_type("inventory");
		mariadb.connect();
		items = mariadb.load_inventory(number_record);
		mariadb.close();
		for(int i = 0; i < items.size(); i++) {
			if(items.get(i).getName().equals("health_point") || 
					items.get(i).getName().equals("money")) {
				if(items.get(i).getName().equals("money")) {
					money = items.get(i).getAmount();
				}
				items.remove(i);
				i--;
			}
		}
		list_box = create_inventory_box(items);
		add(list_box);
		index_item_real = 0;
		index_item_view = 0;
		
		dialog_box = create_dialog_box();
		add(dialog_box);
		dialog = create_dialog(dialog_box);
		dialog_box.add(dialog);
		r = new Runnable_event(this, event_type, player, dialog_box, dialog, list_box);

		list_item.get(index_item_view).setBackground(color_selected);
		
		if(items.size() > 0) {
			dialog.setText(items.get(index_item_real).getDescription());
		}
		
		Thread t = new Thread(r);
		t.start();
	}
	
	
	public void regeneration_sprites() {
		setNeed_loading(true);
		mariadb.connect();
		mariadb.regeneration_forest(BLOCK_SIZE, "bamboo");
		mariadb.close();
		setNeed_loading(false);
	}
	private boolean execute_shining_event(String except_name) {
		// A real point of start and end on visible field.
		int[] location_real_on_view_first = {player.getLocation_real()[0] - player.getLocation_view()[0],
				player.getLocation_real()[1] - player.getLocation_view()[1]};
		int[] location_real_on_view_last = {location_real_on_view_first[0] + RESOLUTION[0] - BLOCK_SIZE,
				location_real_on_view_first[1] + RESOLUTION[1] - BLOCK_SIZE};
		
		List<Integer> visible_sprites = new ArrayList<Integer>();
		for(int i = 0; i < sprites.size(); i++) {
			// player can see this sprite.
			if(sprites.get(i).getType().equals("destroyable") && 
					sprites.get(i).getLocation_real()[0] >= location_real_on_view_first[0] && 
					sprites.get(i).getLocation_real()[0] <= location_real_on_view_last[0] &&
					sprites.get(i).getLocation_real()[1] >= location_real_on_view_first[1] &&
					sprites.get(i).getLocation_real()[1] <= location_real_on_view_last[1]) {
				visible_sprites.add(i);
			}
		}
		if(visible_sprites.size() == 0) {
			return false;
		}
		Random random = new Random();
		int will_shining = -1;
		while(visible_sprites.size() != 0) {
			int index_visible = random.nextInt(visible_sprites.size());
			will_shining = visible_sprites.get(index_visible);
			if(!(sprites.get(will_shining).getName().equals(except_name)) && 
					!(sprites.get(will_shining).isShining())) { 
				break;
			}
			visible_sprites.remove(index_visible);
		}
		if(visible_sprites.size() == 0) {
			return false;
		}
		sprites.get(will_shining).become_shining();
		return true;
	}
	private boolean execute_event(Sprite actor, Sprite sprite) {
		Thread t = null;
		Runnable r = null;
		
		switch(sprite.getType()) {
		case "destroyable":
			if(actor.getType().equals("player")) {
				if(actor.getHealth_point()[0] == 0) {
					player.setDirection(12);
					repaint();
					dont_access_clock = true;
					setEvent_type("immovable");
					break; // Can't action if tired.
				}
				set_health_point(-3);
			}
			sprite.change_Health_point_current(-(actor.getOffense_power()));
			if(sprite.getHealth_point()[0] == 0) {
				if(field.shining_stack_up()) {
					if(execute_shining_event(sprite.getName())) {
						field.setShining_stack(0);
					}
				}
				String[] name_type_image = {
						"felled_" + cut_string.cut_extension_name(sprite.getName_image().get(0)), 
						"money", 
						"felled_" + sprite.getName_image().get(0)
						};
				if(sprite.isShining()) {
					name_type_image[0] = "gold"; 
					name_type_image[2] = "gold.png";
					sprite.gone_shine();
					sprite.setImage("destroyed_" + cut_string.cut_before_underbar(sprite.getName_image().get(0)));
				}else {
					sprite.setImage("destroyed_" + sprite.getName_image().get(0));
				}
				sprite.setType("destroyed");
				
				if(event_type.equals("basic")) {
					mariadb.connect();
					//mariadb // Create the function that bamboo change type, and image.
					if(mariadb.update_column_in_event(sprite.getName(), "_type", "destroyed")) {
						mariadb.update_column_in_event(sprite.getName(), "name_sprite", 
								cut_string.cut_extension_name(sprite.getName_image().get(0)));
					}
					mariadb.close();
				}
				String[] name_image = {name_type_image[2]};
				sprites.add(new Sprite(name_type_image[0], name_type_image[1], 
						name_image, 
						sprite.getLocation_real()[0]-BLOCK_SIZE, 
						sprite.getLocation_real()[1]-BLOCK_SIZE, 6, "", false));
				sprites.add(new Sprite(name_type_image[0], name_type_image[1], 
						name_image, 
						sprite.getLocation_real()[0]+BLOCK_SIZE, 
						sprite.getLocation_real()[1]-BLOCK_SIZE, 6, "", false));
				sprites.add(new Sprite(name_type_image[0], name_type_image[1], 
						name_image, 
						sprite.getLocation_real()[0]-BLOCK_SIZE, 
						sprite.getLocation_real()[1]+BLOCK_SIZE, 6, "", false));
				sprites.add(new Sprite(name_type_image[0], name_type_image[1], 
						name_image, 
						sprite.getLocation_real()[0]+BLOCK_SIZE, 
						sprite.getLocation_real()[1]+BLOCK_SIZE, 6, "", false));
				return true;
			}
			break;
		case "merchant":
			setEvent_type("merchant");		
			mariadb.connect();
			items = mariadb.load_shop(sprite.getName());
			money = mariadb.load_amount_inventory(number_record, "money");
			mariadb.close();
			list_box = create_shop_box(items); 
			add(list_box);
			index_item_real = -1;
			
			dialog_box = create_dialog_box();
			add(dialog_box);
			dialog = create_dialog(dialog_box);
			dialog_box.add(dialog);
			r = new Runnable_event(this, event_type, sprite, dialog_box, dialog, list_box);
			break;
		case "portal":
			t = new Thread(new Runnable() {
				@Override
				public void run() {
					// TODO Auto-generated method stub
					setEvent_type("portal");
					fade_out(500);
					setNeed_loading(true);
					mariadb.connect();
					Sprite[] player_and_field = {player, null};
					player_and_field = mariadb.load_portal(player_and_field, sprite.getName());
					if(player_and_field != null) {
						player = player_and_field[0];
						field = player_and_field[1];
						sprites = mariadb.load_sprite(field.getName());
					}
					mariadb.close();
					setNeed_loading(false);
					repaint();
					fade_in(500);
					setEvent_type("basic");
				}
			});
			t.start();
			break;
		case "wall":
			// Nothing occur.
			break;
		default:
			if(sprite.getExist_script()) {
				setEvent_type("dialog");
				t = new Thread(new Runnable() {
					@Override
					public void run() {
						// TODO Auto-generated method stub
						execute_script(sprite, "dialog");
						setEvent_type("basic");
					}
				});
				t.start();
			}
			break;
		} // End of switch(sprite.getType()).
		if(r != null) {
			t = new Thread(r);
			t.start();
			return true;
		}
		return false;
	} // End of execute_event.
	private JLabel create_label_clock() {
		JLabel label_clock = new JLabel();
		label_clock.setBounds(RESOLUTION[0]/5*4, 0, RESOLUTION[0]/5, RESOLUTION[1]/20);
		label_clock.setOpaque(true);
		label_clock.setBackground(color_content);
		label_clock.setFont(new Font("serif", Font.PLAIN, RESOLUTION[1]/20));
		return label_clock;
	}
	private JPanel create_health_point_box() {
		JPanel health_point_box = new JPanel();

		int width = RESOLUTION[0]/25;
		health_point_box.setBounds(RESOLUTION[0]-width, RESOLUTION[1]/5*4, 
				width, RESOLUTION[1]/5);
		health_point_box.setOpaque(true);
		health_point_box.setBackground(color_content);
		health_point_box.setLayout(null);
		
		return health_point_box;
	}
	private JPanel create_health_point(JPanel parent_box) {
		JPanel health_point = new JPanel();	
		
		health_point.setBounds(0, 0, parent_box.getWidth(), parent_box.getHeight());
		health_point.setOpaque(true);
		
		return health_point;
	}
	private void set_health_point(int want_calculate_number) { // For player.
		player.setHealth_point(0, player.getHealth_point()[0]+want_calculate_number);
		if(want_calculate_number != 0) { 
			mariadb.connect();
			mariadb.update_inventory(number_record, "health_point", 
					player.getHealth_point()[0]);
			mariadb.close();
		}
		
		int[] health_point_value = player.getHealth_point();
		double health_point_percent = 
				(double)health_point_value[0]/(double)health_point_value[1];
		if(health_point_percent <= VALUE_COLOR_AS_HEALTH_PERCENT[1]) {
			if(health_point_percent <= VALUE_COLOR_AS_HEALTH_PERCENT[0]) { // Red color.
				health_point.setBackground(color_red);
			}else { // Yellow color.
				health_point.setBackground(color_yellow);
			}
		}else { // Green color.
			health_point.setBackground(color_green);
		}
		int health_point_height = 
				(int)(health_point_box.getHeight()*health_point_percent);
		int health_point_y = health_point_box.getHeight() - health_point_height;
		health_point.setBounds(
				health_point.getX(), 
				health_point_y, 
				health_point.getWidth(), 
				health_point_height);
		
		if(player.getHealth_point()[0] == 0) {
			clock.be_tired();
		}
	}
	public void set_health_point(double want_percent, boolean want_warp_to_home) { // For player.
		player.setHealth_point(0, (int)((double)player.getHealth_point()[1] * 
				want_percent));
		mariadb.connect();
		mariadb.update_inventory(number_record, "health_point", 
				player.getHealth_point()[0]);
		mariadb.close();
		
		int[] health_point_value = player.getHealth_point();
		double health_point_percent = 
				(double)health_point_value[0]/(double)health_point_value[1];
		if(health_point_percent <= VALUE_COLOR_AS_HEALTH_PERCENT[1]) {
			if(health_point_percent <= VALUE_COLOR_AS_HEALTH_PERCENT[0]) { // Red color.
				health_point.setBackground(color_red);
			}else { // Yellow color.
				health_point.setBackground(color_yellow);
			}
		}else { // Green color.
			health_point.setBackground(color_green);
		}
		int health_point_height = 
				(int)(health_point_box.getHeight()*health_point_percent);
		int health_point_y = health_point_box.getHeight() - health_point_height;
		health_point.setBounds(
				health_point.getX(), 
				health_point_y, 
				health_point.getWidth(), 
				health_point_height);
		
		if(want_warp_to_home) { // Player is tired, warp to home as mystery power.
			mariadb.connect();
			Sprite[] player_and_field = {player, null};
			player_and_field = mariadb.load_portal(player_and_field, "go from everywhere to home inner");
			if(player_and_field != null) {
				player = player_and_field[0];
				field = player_and_field[1];
				sprites = mariadb.load_sprite(field.getName());
			}
			mariadb.close();
			repaint();
		}
	}
	private JPanel create_dialog_box() {
		JPanel dialog_box = new JPanel();
		dialog_box.setBounds(RESOLUTION[0]/20, RESOLUTION[1]/10*7, 
				RESOLUTION[0]/10*9, RESOLUTION[1]/4);
		dialog_box.setLayout(null);
		dialog_box.setOpaque(true);
		dialog_box.setBackground(color_background);
		
		return dialog_box;
	}
	private JLabel create_dialog(JPanel parent_box) {
		JLabel dialog = new JLabel();
		dialog.setBounds(parent_box.getWidth()/20, parent_box.getHeight()/20, 
				parent_box.getWidth()/10*9, parent_box.getHeight()/10*9);
		dialog.setOpaque(true);
		dialog.setBackground(color_content);
		dialog.setFont(new Font("serif", Font.PLAIN, parent_box.getHeight()/20*3));
		
		return dialog;
	}
	private JLabel create_next_label_in_dialog_box(JPanel parent_box) {
		JLabel label = new JLabel(" Z");
		label.setBounds(parent_box.getWidth()/25*24, parent_box.getHeight()/20*16, 
				parent_box.getWidth()/30, parent_box.getHeight()/5);
		label.setOpaque(true);
		label.setBackground(color_content);
		label.setFont(new Font("serif", Font.PLAIN, parent_box.getHeight()/20*3));
		return label;
	}
	private JPanel create_select_box() { // Include yes, no label too.
		JPanel select_box = new JPanel();
		select_box.setBounds(RESOLUTION[0]/4*3, RESOLUTION[1]/5*3, 
				RESOLUTION[0]/5, RESOLUTION[1]/10);
		select_box.setLayout(null);
		select_box.setOpaque(true);
		select_box.setBackground(color_background);
		
		select_yes = new JLabel("Yes");
		select_yes.setHorizontalAlignment(JLabel.CENTER);
		select_yes.setBounds(select_box.getWidth()/20, select_box.getHeight()/20, 
				select_box.getWidth()/10*9, select_box.getHeight()/20*9);
		select_yes.setOpaque(true);
		select_yes.setBackground(color_content);
		select_yes.setFont(new Font("serif", Font.PLAIN, select_yes.getHeight()/4*3));
		select_box.add(select_yes);
		
		select_no = new JLabel("No");
		select_no.setHorizontalAlignment(JLabel.CENTER);
		select_no.setBounds(select_box.getWidth()/20, select_box.getHeight()/2, 
				select_box.getWidth()/10*9, select_box.getHeight()/20*9);
		select_no.setOpaque(true);
		select_no.setBackground(color_content);
		select_no.setFont(new Font("serif", Font.PLAIN, select_yes.getHeight()/4*3));
		select_box.add(select_no);
		
		return select_box;
	}
	private JPanel create_inventory_box(List<Item> items) {
		JPanel inventory_box = new JPanel();
		inventory_box.setLayout(null);
		inventory_box.setBounds(RESOLUTION[0]/20, RESOLUTION[1]/20, 
				RESOLUTION[0]/10*9, RESOLUTION[1]/25*16);
		inventory_box.setOpaque(true);
		inventory_box.setBackground(color_background);
		money_label = create_money_label(inventory_box.getWidth()/5*4, 0, 
				inventory_box.getWidth()/10, inventory_box.getHeight()/10);
		inventory_box.add(money_label);
		
		money_label.setText("" + money);

		list_length_view = MAX_LIST_LENGTH;
		list_item = new ArrayList<JLabel>();
		for(int i = 0; i < list_length_view; i++) {
			JLabel label = new JLabel();
			label.setBounds(inventory_box.getWidth()/20, inventory_box.getHeight()/10 + 
					(inventory_box.getHeight()/10 + inventory_box.getHeight()/40) * i, 
					inventory_box.getWidth()/10*9, inventory_box.getHeight()/10);
			
			label.setOpaque(true);
			label.setBackground(color_content);		
			label.setFont(new Font("Serif", Font.PLAIN, label.getHeight()/2));
			list_item.add(label);
			inventory_box.add(label);
		}
		if(items.size() < list_length_view) {
			list_length_view = items.size();
		}
		for(int i = 0; i < list_length_view; i++) {
			String effect = "";
			if(items.get(i).getType().equals("food")) {
				effect = ", Hp " + items.get(i).getEffect();
			}
			list_item.get(i).setText("  " + items.get(i).getName() + " - Usable : " +  
					items.get(i).getUsable() + ", Amount : " + 
					items.get(i).getAmount() + effect);
		}

		return inventory_box;
	}
	private JPanel create_shop_box(List<Item> items) {
		JPanel shop_box = new JPanel();
		shop_box.setLayout(null);
		shop_box.setBounds(RESOLUTION[0]/20, RESOLUTION[1]/20, 
				RESOLUTION[0]/10*9, RESOLUTION[1]/25*16);
		shop_box.setOpaque(true);
		shop_box.setBackground(color_background);
		money_label = create_money_label(shop_box.getWidth()/5*4, 0, 
				shop_box.getWidth()/10, shop_box.getHeight()/10);
		shop_box.add(money_label);
		
		money_label.setText("" + money);
		
		list_length_view = MAX_LIST_LENGTH;
		list_item = new ArrayList<JLabel>();
		for(int i = 0; i < list_length_view; i++) {
			JLabel label = new JLabel();
			label.setBounds(shop_box.getWidth()/20, shop_box.getHeight()/10 + 
					(shop_box.getHeight()/10 + shop_box.getHeight()/40) * i, 
					shop_box.getWidth()/10*9, shop_box.getHeight()/10);
			
			// TEMP.
			label.setOpaque(true);
			label.setBackground(color_content);
			
			label.setFont(new Font("Serif", Font.PLAIN, label.getHeight()/2));
			list_item.add(label);
			shop_box.add(label);
		}
		if(items.size() < list_length_view) {
			if(items.size() == 0) {
				list_length_view = 1;
			}else {
				list_length_view = items.size();
			}
		}
		for(int i = 0; i < list_length_view; i++) {
			String effect = "";
			if(items.get(i).getType().equals("food")) {
				effect = ", Hp " + items.get(i).getEffect();
			}
			list_item.get(i).setText("  " + items.get(i).getName() + " -  Usable : " +  
					items.get(i).getUsable() + ", Price : " + 
					items.get(i).getPrice() + effect);
		}

		return shop_box;
	}
	private JLabel create_money_label(int x, int y, int width, int height) {
		JLabel money_label = new JLabel();
		money_label.setBounds(x, y, width, height);
		
		// TEMP.
		money_label.setOpaque(true);
		money_label.setBackground(color_content);
		
		money_label.setFont(new Font("Serif", Font.PLAIN, money_label.getHeight()/2));
		
		return money_label;
	}
	
	public int[] getRESOLUTION() {
		return RESOLUTION;
	}
	
	public void setPlayer(Sprite player) {
		this.player = player;
	}
	public void setSprites(List<Sprite> sprites) {
		this.sprites = sprites;
	}
	public void setNeed_loading(boolean need_loading) {
		this.need_loading = need_loading;
		repaint();
	}
	public void setEvent_type(String event_type) {
		this.event_type = event_type;
		if(show_got_money_thread != null) {
			show_got_money_thread.interrupt();
			show_got_money_thread = null;
		}
		if(clock == null || dont_access_clock) {
			return;
		}
		if(event_type.equals("basic") || event_type.equals("immovable")) { // Resume the clock.
			synchronized (clock) {
				while(!(("" + clock.getState()).equals("BLOCKED") ||
						("" + clock.getState()).equals("NEW"))) {
					// Refer the catch(ie1) on Clock.java.
					clock.notify();
				}
			}
		}else { // Stop the clock.
			clock.interrupt();
		}
	}
	public void setDont_access_clock(boolean dont_access_clock) {
		this.dont_access_clock = dont_access_clock;
	}
}
