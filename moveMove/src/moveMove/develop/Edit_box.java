package moveMove.develop;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.List;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import moveMove.Viewer_world;

public class Edit_box {
	private JPanel box = null;
	
	private JLabel label_name = null;
	private JLabel label_field = null;
	private JLabel label_sprite = null;
	private JLabel label_type = null;
	private JLabel label_location_x = null;
	private JLabel label_location_y = null;
	private JLabel label_direction = null;
	private JLabel label_dialog = null;
	
	private JTextField name = null;
	private JLabel field = null;
	private JLabel sprite = null;
	private JTextField type = null;
	private JLabel location_x = null;
	private JLabel location_y = null;
	private JTextField direction = null;
	private JTextField dialog = null;
	private JPanel panel_sprite = null;
	
	private JButton button_apply = null;
	private JButton button_cancel = null;
	private JButton button_delete = null;
	
	private int[] resolution = {0, 0};
	private int block_size = 0;
	private int edit_menu_size = 0;
	
	private boolean is_empty = false; // Is empty that place on clicked?
	
	public Edit_box(int[] resolution, int block_size, int edit_menu_size, 
			Color color_background, Color color_content, 
			List<String[]> name_and_imageNames, Viewer_world viewer_world) {
		// TODO Auto-generated constructor stub
		box = new JPanel();
		box.setLayout(null);
		box.setOpaque(true);
		box.setBackground(color_background);
		// First set size. location will set after.
		box.setBounds(0, 0, 
				block_size*edit_menu_size, resolution[1]);
		
		this.resolution = resolution;
		this.block_size = block_size;
		this.edit_menu_size = edit_menu_size;
		
		label_name = new JLabel("Name");
		label_field = new JLabel("Field");
		label_sprite = new JLabel("Sprite");
		label_type = new JLabel("Type(*)");
		label_location_x = new JLabel("loc_x");
		label_location_y = new JLabel("loc_y");
		label_direction = new JLabel("direction");
		label_dialog = new JLabel("dialog");
		
		name = new JTextField();
		field = new JLabel();
		sprite = new JLabel();
		type = new JTextField();
		location_x = new JLabel();
		location_y = new JLabel();
		direction = new JTextField();
		dialog = new JTextField();
		panel_sprite = new JPanel();
		
		button_apply = new JButton("Apply");
		button_cancel = new JButton("Cancel");
		button_delete = new JButton("Delete"); // Immediately.
		
		field.setOpaque(true);
		field.setBackground(color_content);
		sprite.setOpaque(true);
		sprite.setBackground(color_content);
		location_x.setOpaque(true);
		location_x.setBackground(color_content);
		location_y.setOpaque(true);
		location_y.setBackground(color_content);
		panel_sprite.setOpaque(true);
		panel_sprite.setBackground(color_content);
		
		for(int i = 1; i <= 11; i++) {
			switch(i) {
			case 1:
				label_name.setBounds(box.getWidth()/20, 
						(box.getHeight()/20 + box.getHeight()/40) * i, 
						box.getWidth()/5, box.getHeight()/20);
				label_name.setForeground(Color.GRAY);
				box.add(label_name);
				name.setBounds(box.getWidth()/10*3, 
						(box.getHeight()/20 + box.getHeight()/40) * i, 
						box.getWidth()/20*13, box.getHeight()/20);
				box.add(name);
				break;
			case 2:
				label_field.setBounds(box.getWidth()/20, 
						(box.getHeight()/20 + box.getHeight()/40) * i, 
						box.getWidth()/5, box.getHeight()/20);
				label_field.setForeground(Color.GRAY);
				box.add(label_field);
				field.setBounds(box.getWidth()/10*3, 
						(box.getHeight()/20 + box.getHeight()/40) * i, 
						box.getWidth()/20*13, box.getHeight()/20);
				box.add(field);
				break;
			case 3:
				label_sprite.setBounds(box.getWidth()/20, 
						(box.getHeight()/20 + box.getHeight()/40) * i, 
						box.getWidth()/5, box.getHeight()/20);
				label_sprite.setForeground(Color.GRAY);
				box.add(label_sprite);
				sprite.setBounds(box.getWidth()/10*3, 
						(box.getHeight()/20 + box.getHeight()/40) * i, 
						box.getWidth()/20*13, box.getHeight()/20);
				box.add(sprite);
				break;
			case 4:
				label_type.setBounds(box.getWidth()/20, 
						(box.getHeight()/20 + box.getHeight()/40) * i, 
						box.getWidth()/5, box.getHeight()/20);
				label_type.setForeground(Color.GRAY);
				box.add(label_type);
				type.setBounds(box.getWidth()/10*3, 
						(box.getHeight()/20 + box.getHeight()/40) * i, 
						box.getWidth()/20*13, box.getHeight()/20);
				box.add(type);
				break;
			case 5:
				label_location_x.setBounds(box.getWidth()/20, 
						(box.getHeight()/20 + box.getHeight()/40) * i, 
						box.getWidth()/20*3, box.getHeight()/20);
				label_location_x.setForeground(Color.GRAY);
				box.add(label_location_x);
				location_x.setBounds(box.getWidth()/4, 
						(box.getHeight()/20 + box.getHeight()/40) * i, 
						box.getWidth()/4, box.getHeight()/20);
				box.add(location_x);
				break;
			case 6:
				label_location_y.setBounds(box.getWidth()/2, 
						(box.getHeight()/20 + box.getHeight()/40) * (i - 1), 
						box.getWidth()/20*3, box.getHeight()/20);
				label_location_y.setForeground(Color.GRAY);
				box.add(label_location_y);
				location_y.setBounds(box.getWidth()/10*7, 
						(box.getHeight()/20 + box.getHeight()/40) * (i - 1), 
						box.getWidth()/4, box.getHeight()/20);
				box.add(location_y);
				break;
			case 7:
				label_direction.setBounds(box.getWidth()/20, 
						(box.getHeight()/20 + box.getHeight()/40) * (i - 1), 
						box.getWidth()/5, box.getHeight()/20);
				label_direction.setForeground(Color.GRAY);
				box.add(label_direction);
				direction.setBounds(box.getWidth()/10*3, 
						(box.getHeight()/20 + box.getHeight()/40) * (i - 1), 
						box.getWidth()/20*13, box.getHeight()/20);
				box.add(direction);
				break;
			case 8:
				label_dialog.setBounds(box.getWidth()/20, 
						(box.getHeight()/20 + box.getHeight()/40) * (i - 1), 
						box.getWidth()/5, box.getHeight()/20);
				label_dialog.setForeground(Color.GRAY);
				box.add(label_dialog);
				dialog.setBounds(box.getWidth()/10*3, 
						(box.getHeight()/20 + box.getHeight()/40) * (i - 1), 
						box.getWidth()/20*13, box.getHeight()/20);
				box.add(dialog);
				break;
			case 9:
				panel_sprite.setBounds(box.getWidth()/20, 
						(box.getHeight()/20 + box.getHeight()/40) * (i - 1), 
						box.getWidth()/10*9, box.getHeight()/4);
				panel_sprite.setLayout(null);
				int max_column = panel_sprite.getWidth()/block_size;
				int padding_left = panel_sprite.getWidth()%block_size/2;
				for(int j = 0; j < name_and_imageNames.size(); j+= max_column) {
					for(int k = 0; k < max_column; k++) {
						String image_path = "";
						ImageIcon imageIcon = null;
						BufferedImage image = null;
						try {
							image_path = System.getProperty("user.dir") + 
									"\\src\\moveMove\\images\\" + name_and_imageNames.get(j+k)[1];
						}catch(IndexOutOfBoundsException ioobe) {
							// Done load the image.
							break;
						}
						JButton btn_image = new JButton();
						try {
							image = ImageIO.read(new File(image_path));
							btn_image.setIcon(new ImageIcon(image));
						} catch (IOException ioe) {
							System.err.println("ERROR - Cannot load the image : " + 
									name_and_imageNames.get(j+k)[1]);
						}
						btn_image.setBounds(padding_left+block_size*k, j/max_column*block_size, 
								block_size, block_size);
						
						int j0 = j;
						int k0 = k;
						btn_image.addActionListener(new ActionListener() {
							@Override
							public void actionPerformed(ActionEvent e) {
								// TODO Auto-generated method stub
								sprite.setText(name_and_imageNames.get(j0+k0)[0]);
								viewer_world.requestFocus();
							}
						});
						panel_sprite.add(btn_image);
					}
				}
				box.add(panel_sprite);
				break;
			case 10:
				button_apply.setBounds(box.getWidth()/20, 
						(box.getHeight()/20 + box.getHeight()/40) * (i + 2), 
						box.getWidth()/10*3, box.getHeight()/20);
				box.add(button_apply);
				button_cancel.setBounds(box.getWidth()/20*7, 
						(box.getHeight()/20 + box.getHeight()/40) * (i + 2), 
						box.getWidth()/10*3, box.getHeight()/20);
				box.add(button_cancel);
				button_delete.setBounds(box.getWidth()/20*13, 
						(box.getHeight()/20 + box.getHeight()/40) * (i + 2), 
						box.getWidth()/10*3, box.getHeight()/20);
				box.add(button_delete);
			}// End of switch(i).
		}// End of for loop.
	}
	public void update_edit_box(Container_event container_event, String name_field, 
			int location_real_on_view_first_x, int location_real_on_view_first_y, 
			int loc_x, int loc_y) {		
		if(loc_x >= resolution[0] - block_size*edit_menu_size) { // Set location to left.
			box.setBounds(0, 0, 
					block_size*edit_menu_size, resolution[1]);
		}else {
			// Set location to right.
			box.setBounds(resolution[0] - block_size*edit_menu_size, 0, 
					block_size*edit_menu_size, resolution[1]);
		}
		
		if(container_event != null) {
			name.setText(container_event.getName());
			field.setText(container_event.getName_field());
			sprite.setText(container_event.getName_sprite());
			type.setText(container_event.get_type());
			location_x.setText("" + container_event.getLocation()[0]);
			location_y.setText("" + container_event.getLocation()[1]);
			direction.setText("" + container_event.getDirection());
			dialog.setText(container_event.getDialog());
			is_empty = false;
		}else {
			name.setText("");
			field.setText(name_field);
			sprite.setText("");
			type.setText("");
			location_x.setText("" + (location_real_on_view_first_x + loc_x));
			location_y.setText("" + (location_real_on_view_first_y + loc_y));
			direction.setText("");
			dialog.setText("");
			is_empty = true;
		}
	}
	
	public JPanel getBox() {
		return box;
	}
	public JTextField getName() {
		return name;
	}
	public JLabel getField() {
		return field;
	}
	public JLabel getSprite() {
		return sprite;
	}
	public JTextField getType() {
		return type;
	}
	public JLabel getLocation_x() {
		return location_x;
	}
	public JLabel getLocation_y() {
		return location_y;
	}
	public JTextField getDirection() {
		return direction;
	}
	public JTextField getDialog() {
		return dialog;
	}
	public JButton getButton_apply() {
		return button_apply;
	}
	public JButton getButton_cancel() {
		return button_cancel;
	}
	public JButton getButton_delete() {
		return button_delete;
	}
	public boolean is_empty() {
		return is_empty;
	}
	
	public void setBox(JPanel box) {
		this.box = box;
	}
	public void setName(String name) {
		this.name.setText(name);
	}
	public void setDirection(int direction) {
		this.direction.setText("" + direction);
	}
}
