package moveMove;

public class Item {
	private String name = ""; // Load by item table.
	private String type = "";
	private String description = "";
	private boolean usable = false;
	private int price = 0;
	private int effect = 0;
	
	private int amount = 0; // Load by inventory table.
	
	public Item(String name, String type, String description, 
			boolean usable, int price, int effect, int amount) {
		// TODO Auto-generated constructor stub
		this.name = name;
		this.type = type;
		this.description = description;
		this.usable = usable;
		this.price = price;
		this.effect = effect;
		this.amount = amount;
	}
	
	public String getName() {
		return name;
	}
	public String getType() {
		return type;
	}
	public String getDescription() {
		return description;
	}
	public boolean getUsable() {
		return usable;
	}
	public int getPrice() {
		return price;
	}
	public int getEffect() {
		return effect;
	}
	public int getAmount() {
		return amount;
	}
	
	public void setAmount(int amount) {
		this.amount = amount;
	}
}
