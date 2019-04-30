package moveMove;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;
import javax.swing.JOptionPane;

public class Sprite {
	private String name = "";
	private String type = ""; // "player", "field".
	private String biome = ""; // For field.
	
	// Order of direction : bottom, left, top, right.
	private List<BufferedImage> image = null;
	private List<String> name_image = null;
	
	private int[] size = {0, 0};
	private int[] location_real = {0, 0};
	private int[] location_view = {0, 0};
	private int direction = 0; // Except a field, Sprite is look to somewhere at always. (1 ~ 12, like clock)
	private String dialog = ""; // For all except field.
	
	private final int SHINING_POINT = 3; // Used the field sprite.
	private int shining_stack = -1; // Used the shining event.
	
	private boolean is_shining = false; // Used destroyable sprite.
	
	private int[] health_point = {-1, -1};
	private int offense_power = -1;
	
	private boolean exist_script = false;
	
	// If field.
	public Sprite(String name, String biome, String image_name){	
		this.name = name;			
		this.type = "field";
		this.biome = biome;
		String image_path = System.getProperty("user.dir") + "\\src\\moveMove\\images\\" + image_name;

		this.image = new ArrayList<BufferedImage>();
		this.name_image = new ArrayList<String>();
		try { // Field sprite have only one image.
			this.image.add(ImageIO.read(new File(image_path)));
			this.size[0] = this.image.get(0).getWidth();
			this.size[1] = this.image.get(0).getHeight();
			name_image.add(image_name);
		}catch(IOException ioe){
			System.err.println("ERROR - Cannot load the image : " + image_name);
			ioe.printStackTrace();
		}catch(Exception e) {
			e.printStackTrace();
		}
		shining_stack = 0;
	} // End of constructor for field.
	
	// If destroyable sprite.
	public Sprite(String name, String type, String image_name, int location_x, int location_y, 
			int direction, String dialog, int health_point, boolean exist_script){
		this.name = name;	
		this.type = type;
		String image_path = System.getProperty("user.dir") + "\\src\\moveMove\\images\\" + image_name;

		this.image = new ArrayList<BufferedImage>();
		this.name_image = new ArrayList<String>();
		try { // destroyable sprite have only one image too(on direction).
			this.image.add(ImageIO.read(new File(image_path)));
			this.size[0] = this.image.get(0).getWidth();
			this.size[1] = this.image.get(0).getHeight();
			name_image.add(image_name);
		}catch(IOException ioe){
			System.err.println("ERROR - Cannot load the image : " + image_name);
			ioe.printStackTrace();
		}catch(Exception e) {
			e.printStackTrace();
		}
		
		this.location_real[0] = location_x;
		this.location_real[1] = location_y;
		this.direction = direction;
		this.dialog = dialog;
		this.exist_script = exist_script;
		this.health_point[1] = health_point;
		this.health_point[0] = this.health_point[1];
	} // End of constructor.	
	
	// If all except field.
	public Sprite(String name, String type, String[] image_name, int location_x, int location_y, 
			int direction, String dialog, boolean exist_script){
		
		this.name = name;		
		this.type = type;

		this.image = new ArrayList<BufferedImage>();
		this.name_image = new ArrayList<String>();
		int i = 0;
		try {
			for(; i < 4; i++) { // down, left, up, right image.
				int j = i;
				try {
					if(image_name[i].equals("")) {
						j = 0;
					}
				}catch(IndexOutOfBoundsException ioobe) {
					j = 0;
				}
				String image_path = System.getProperty("user.dir") + 
						"\\src\\moveMove\\images\\" + image_name[j];
				this.image.add(ImageIO.read(new File(image_path)));
				name_image.add(image_name[j]);
			}
			this.size[0] = this.image.get(0).getWidth();
			this.size[1] = this.image.get(0).getHeight();
		}catch(IOException ioe){
			System.err.println("ERROR - Cannot load the image : " + image_name[i]);
			ioe.printStackTrace();
		}catch(Exception e) {
			e.printStackTrace();
		}
		
		this.location_real[0] = location_x;
		this.location_real[1] = location_y;
		this.direction = direction;
		this.dialog = dialog;
		offense_power = 1;
		this.exist_script = exist_script;
	} // End of constructor.
	
	// For field sprite.
	public boolean shining_stack_up() {
		if(!(type.equals("field"))) {
			System.err.println("ERROR - Sprite \"" + name + "\" is not field.");
			return false;
		}
		shining_stack++;
		if(shining_stack >= SHINING_POINT) {
			shining_stack = SHINING_POINT;
			return true; // If true, time to run of the shining event.
		}
		return false;
	}
	
	// Start: For destroyable sprite, and maybe for player.
	public void change_Health_point_current(int plus_and_minus) {
		health_point[0] += plus_and_minus;
		if(health_point[0] > health_point[1]) { // Full HP!
			health_point[0] = health_point[1];
		}else if(health_point[0] <= 0) { // Death.
			health_point[0] = 0;
		}
	}
	public boolean isShining() {
		return is_shining;
	}
	public void become_shining() {
		if(!(type.equals("destroyable"))) {
			System.err.println("ERROR - The type of the sprite \"" + name + 
					"\" is not destroyable.");
			return;
		}
		setImage("shining_" + name_image.get(0));
		is_shining = true;
	}
	public void gone_shine() {
		if(is_shining) {
			is_shining = false;
			return;
		}
		System.err.println("ERROR - Sprite \"" + name + 
				"\" is not shining.");
	}
	// End : For destroyable sprite.
	
	public BufferedImage getImage_as_direction() {
		int d = -1;
		switch(direction){
			case 6:
				d = 0;
				break;
			case 9:
				d = 1;
				break;
			case 12:
				d = 2;
				break;
			case 3:
				d = 3;
				break;
			default:
				System.err.println("ERROR(getImage_as_direction()) - Wrong direction : " +
						direction);
		}
		try {
			return image.get(d);
		}catch(IndexOutOfBoundsException ioobe) {
			return image.get(0); // If only exist the one image(bottom image).
		}
	}
	
	public String getName() {
		return name;
	}
	public String getType() {
		return type;
	}
	public List<BufferedImage> getImage() {
		return image;
	}
	public List<String> getName_image() {
		return name_image;
	}
	public int[] getSize() {
		return size;
	}
	public int[] getLocation_real() {
		return location_real;
	}
	public int[] getLocation_view() {
		return location_view;
	}
	public int getDirection() {
		return direction;
	}
	public String getDialog() {
		return dialog;
	}
	public int getSHINING_POINT() {
		return SHINING_POINT;
	}
	public int[] getHealth_point() {
		return health_point;
	}
	public int getOffense_power() {
		return offense_power;
	}
	public boolean getExist_script(){
		return exist_script;
	}
	
	public void setType(String type) {
		this.type = type;
	}
	void setImage(String image_name) {
		String image_path = System.getProperty("user.dir") + "\\src\\moveMove\\images\\" + image_name;
		try {
			this.image.set(0, (ImageIO.read(new File(image_path))));
			this.size[0] = this.image.get(0).getWidth();
			this.size[1] = this.image.get(0).getHeight();
			name_image.set(0, (image_name));
		}catch(IOException ioe){
			System.err.println("ERROR - Cannot load the image : " + image_name);
			ioe.printStackTrace();
		}catch(Exception e) {
			e.printStackTrace();
		}
		this.image.get(0);
	}
	public void setLocation_real(char direction, int location) {
		if(direction == 'x') {
			this.location_real[0] = location;
		}else{
			this.location_real[1] = location;
		}
	}
	public void setLocation_real(int location_x, int location_y) {
		this.location_real[0] = location_x;
		this.location_real[1] = location_y;
	}
	public void setLocation_view(char direction, int location) {
		if(direction == 'x') {
			this.location_view[0] = location;
		}else{
			this.location_view[1] = location;
		}
	}
	public void setLocation_view(int location_x, int location_y) {
		this.location_view[0] = location_x;
		this.location_view[1] = location_y;
	}
	public void setDirection(int direction) {
		this.direction = direction;
	}
	public void setHealth_point(int current_or_max, int health_point) {
		switch(current_or_max) {
		case 0:
			if(health_point < 0) {
				// Current health point is should bigger or equal than 0.
				health_point = 0;
			}else if(health_point > this.health_point[1]) {
				// Current health point is should smaller or equal than max health point.
				health_point = this.health_point[1];
			}
			break;
		case 1:
			if(health_point < 1) {
				// Max health point is should bigger than 0.
				health_point = 1;
			}
		}
		this.health_point[current_or_max] = health_point;
	}
	
	// For field sprite.
	public void setShining_stack(int shining_stack) {
		if(!(type.equals("field"))) {
			System.err.println("ERROR - Sprite \"" + name + "\" is not field.");
			return;
		}
		this.shining_stack = shining_stack;
	}
}
