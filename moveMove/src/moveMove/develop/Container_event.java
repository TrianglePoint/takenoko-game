package moveMove.develop;

public class Container_event {
	private String name = "";
	private String name_field = "";
	private String name_sprite = "";
	private String _type = "";
	private int[] location = {0, 0};
	private int direction = 0;
	private String dialog = "";
	public Container_event(String name, String name_field, String name_sprite, String _type, 
			int location_x, int location_y, int direction, String dialog) {
		// TODO Auto-generated constructor stub
		this.name = name;
		this.name_field = name_field;
		this.name_sprite = name_sprite;
		this._type = _type;
		this.location[0] = location_x;
		this.location[1] = location_y;
		this.direction = direction;
		this.dialog = dialog;
	}
	
	public String getName() {
		return name;
	}
	public String getName_field() {
		return name_field;
	}
	public String getName_sprite() {
		return name_sprite;
	}
	public String get_type() {
		return _type;
	}
	public int[] getLocation() {
		return location;
	}
	public int getDirection() {
		return direction;
	}
	public String getDialog() {
		return dialog;
	}
}
