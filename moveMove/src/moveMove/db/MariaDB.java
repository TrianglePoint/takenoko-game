package moveMove.db;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import javax.imageio.ImageIO;
import javax.swing.JOptionPane;

import moveMove.Clock;
import moveMove.Item;
import moveMove.Sprite;
import moveMove.Viewer_world;
import moveMove.develop.Container_event;
import moveMove.develop.Edit_box;

public class MariaDB {
	
	// Jdbc driver name and url.
	private static final String JDBC_DRIVER = "org.mariadb.jdbc.Driver";
	private String db_url = "";
	
	private String user = "";
	private String password = "";
	
	private Statement stmt = null;
	private PreparedStatement pstmt = null;
	private ResultSet rs = null;
	private Connection conn = null;
	
	public MariaDB(String address, String user, String password){
		db_url = "jdbc:mariadb://" + address;
		this.user = user;
		this.password = password;
	}
	
	public void connect() {
		try {
			// Register jdbc driver.
			Class.forName(JDBC_DRIVER);
			
			// Open a connection.
			System.out.println("Connecting...");
			conn = DriverManager.getConnection(db_url, user, password);
			System.out.println("Connected.");
		}catch(SQLException se) {
			se.printStackTrace();
		}catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	public void create_record(int request_number, String request_name) {
		try {
			String psql = "insert into record (number, name) values (?, ?)";
			pstmt = conn.prepareStatement(psql);
			pstmt.setInt(1, request_number);
			pstmt.setString(2, request_name);
			pstmt.executeQuery();
			System.out.println("Created record " + request_number + " : " + request_name);
			load_amount_inventory(request_number, "health_point");
			load_amount_inventory(request_number, "money");
		}catch(SQLException se) {
			se.printStackTrace();
		}catch(Exception e) {
			e.printStackTrace();
		}
	}
	public Sprite[] load_record(int request_number, Clock clock) {
		Sprite[] player_and_field = {null, null};
		int count_retry = 0;
		try {
			while(count_retry < 3) {
				String psql = "select r.name, s.name_image, s.name_image_left, "
						+ "s.name_image_top, s.name_image_right, "
						+ "r.location_x, r.location_y, r.direction, "
						+ "r.field, r.day, r._time "
						+ "from record r, (select * from sprite where name=?) s "
						+ "where r.number=?";
				pstmt = conn.prepareStatement(psql);
				pstmt.setString(1, "player");
				pstmt.setInt(2, request_number);
				rs = pstmt.executeQuery();
				
				if(rs.next()) {
					String[] name_image = {rs.getString("name_image"), 
							rs.getString("name_image_left"), 
							rs.getString("name_image_top"), 
							rs.getString("name_image_right")};
					player_and_field[0] = new Sprite(rs.getString("name"), "player", name_image, 
							rs.getInt("location_x"), rs.getInt("location_y"), 
							rs.getInt("direction"), "", false);
					clock.setDay(rs.getInt("day"));
					clock.setTime(rs.getInt("_time"));
					System.out.println("Loaded from record " + request_number);
					
					player_and_field[1] = load_field(rs.getString("field"));

					return player_and_field;
				}else {
					System.err.println("ERROR - Can't find record " + request_number + 
							"\ncreate new file on record " + request_number);
					create_record(request_number, "player");
					count_retry += 1;
				}
			}
		}catch(SQLException se) {
			se.printStackTrace();
		}catch(Exception e) {
			e.printStackTrace();
		}
		System.err.println("ERROR - Fail load_record");
		return null;
	}
	public void save_record(Sprite field, Sprite player, int request_number, Clock clock) {
		try {
			String psql = "update record set field=?, location_x=?, location_y=?, "
					+ "direction=?, day=?, _time=?, date=now() where number=?";
			pstmt = conn.prepareStatement(psql);
			pstmt.setString(1, field.getName());
			pstmt.setInt(2, player.getLocation_real()[0]);
			pstmt.setInt(3, player.getLocation_real()[1]);
			pstmt.setInt(4, player.getDirection());
			pstmt.setInt(5, clock.getDay());
			pstmt.setInt(6, clock.getTime());
			pstmt.setInt(7, request_number);
			pstmt.executeQuery();
			System.out.println("Saved to record " + request_number);
		}catch(SQLException se) {
			se.printStackTrace();
		}catch(Exception e) {
			e.printStackTrace();
		}
	}
	public void delete_record(int request_number) {
		try {
			String psql = "delete from inventory where number_record=?";
			pstmt = conn.prepareStatement(psql);
			pstmt.setInt(1, request_number);
			pstmt.executeQuery(); // Delete all of inventory.
			System.out.println("Deleted the inventory of " + request_number);
			psql = "delete from record where number=?";
			pstmt = conn.prepareStatement(psql);
			pstmt.setInt(1, request_number);
			pstmt.executeQuery();
			System.out.println("Deleted the record of " + request_number);
		}catch(SQLException se) {
			se.printStackTrace();
		}catch(Exception e) {
			e.printStackTrace();
		}
	}
	public Sprite load_field(String request_name) {
		Sprite field = null;
		try {
			String psql = "select biome, name_image from _field where name=?";
			pstmt = conn.prepareStatement(psql);
			pstmt.setString(1, request_name);
			rs = pstmt.executeQuery();
			
			if(rs.next()) {
				field = new Sprite(request_name, rs.getString("biome"), rs.getString("name_image"));
			}else {
				System.err.println("ERROR - Can't find map : " + request_name);
			}
		}catch(SQLException se) {
			se.printStackTrace();
		}catch(Exception e) {
			e.printStackTrace();
		}
		return field;
	}
	public List<Sprite> load_sprite(String request_name) {
		List<Sprite> sprites = null;
		try {
			String psql = "select ev.name, ev._type, ev.name_sprite, "
					+ "s.name_image, s.name_image_left, s.name_image_top, s.name_image_right, " +
					"ev.location_x, ev.location_y, ev.direction,"
					+ "ev.dialog, ev.exist_script " +
					"from (select f0.name from _field f0 where f0.name=?) f " + 
					"inner join _event ev on f.name = ev.name_field " +
					"inner join sprite s on ev.name_sprite = s.name";
			pstmt = conn.prepareStatement(psql);
			pstmt.setString(1, request_name);
			rs = pstmt.executeQuery();
			
			sprites = new ArrayList<Sprite>();
			while(rs.next()) {
				if(rs.getString("_type").equals("destroyable")) {
					String name_sprite = rs.getString("name_sprite");
					ResultSet rs_temp = rs;
					rs = null;
					int[] stats = load_stats(name_sprite);
					rs = rs_temp;
					sprites.add(new Sprite(rs.getString("name"), 
							rs.getString("_type"), 
							rs.getString("name_image"), 
							rs.getInt("location_x"), 
							rs.getInt("location_y"), 
							rs.getInt("direction"), 
							rs.getString("dialog"), 
							stats[0], 
							rs.getBoolean("exist_script")));
				}else {
					String[] name_image = {rs.getString("name_image"), 
							rs.getString("name_image_left"), 
							rs.getString("name_image_top"), 
							rs.getString("name_image_right")};
					sprites.add(new Sprite(rs.getString("name"), 
						rs.getString("_type"), 
						name_image, 
						rs.getInt("location_x"), 
						rs.getInt("location_y"), 
						rs.getInt("direction"), 
						rs.getString("dialog"),
						rs.getBoolean("exist_script")));
				}
			}
		}catch(SQLException se) {
			se.printStackTrace();
		}catch(Exception e) {
			e.printStackTrace();
		}
		return sprites;
	}
	public int[] load_stats(String request_name) {
		int[] stats = {-1};
		try {
			String psql = "select health_point "
					+ "from stats where name_sprite=?";
			pstmt = conn.prepareStatement(psql);
			pstmt.setString(1, request_name);
			rs = pstmt.executeQuery();
			rs.next(); // This result is exist at always.
			stats[0] = rs.getInt("health_point");
		}catch(SQLException se) {
			se.printStackTrace();
		}catch(Exception e) {
			e.printStackTrace();
		}
		return stats;
	}
	public List<String[]> load_script(String request_name, int request_group_number){
		List<String[]> scripts = null;
		try {
			String psql = "select _type, value1, value2 "
					+ "from script where name_event=? and group_number=? order by `order`";
			pstmt = conn.prepareStatement(psql);
			pstmt.setString(1, request_name);
			pstmt.setInt(2, request_group_number);
			rs = pstmt.executeQuery();
			scripts = new ArrayList<String[]>();
			while(rs.next()) {
				String[] script = {rs.getString("_type"),
						rs.getString("value1"),
						rs.getString("value2")};
				scripts.add(script);
			}
		}catch(SQLException se) {
			se.printStackTrace();
		}catch(Exception e) {
			e.printStackTrace();
		}
		return scripts;
	}
	public List<Item> load_inventory(int request_number) {
		List<Item> items = null;
		try {	
			String psql = "select i.name, i._type, i.description, i.usable, "
					+ "i.price, i.effect, inven.amount "
					+ "from (select name_item, amount "
					+ "from inventory where number_record=?) inven "
					+ "inner join item i on inven.name_item=i.name";
			pstmt = conn.prepareStatement(psql);
			pstmt.setInt(1, request_number);
			rs = pstmt.executeQuery();
				
			items = new ArrayList<Item>();
			while(rs.next()) {
				items.add(new Item(rs.getString("name"),
						rs.getString("_type"),
						rs.getString("description"), 
						rs.getBoolean("usable"), 
						rs.getInt("price"),
						rs.getInt("effect"),
						rs.getInt("amount")));
			}
			return items;
		}catch(SQLException se) {
			se.printStackTrace();
		}catch(Exception e) {
			e.printStackTrace();
		}
		System.err.println("ERROR - Can't find items of record " + request_number);
		
		return items;
	}
	public int load_amount_inventory(int request_number, String request_name) {
		int amount = -1;  // Health point of player is saved in inventory table.
		int retry = 0;
		try {
			while(retry < 3) {
				String psql = "select amount from inventory "
						+ "where number_record=? and name_item=?";
				pstmt = conn.prepareStatement(psql);
				pstmt.setInt(1, request_number);
				pstmt.setString(2, request_name);
				rs = pstmt.executeQuery();
				
				if(rs.next()) {
					amount = rs.getInt("amount");
					return amount;
				}else {
					stmt = conn.createStatement();
					rs = stmt.executeQuery("select count(*) from inventory");
					rs.next(); // not use "if" because count is return at always.
					int count = rs.getInt("count(*)");
					amount = 0;
					if(request_name.equals("health_point")) {
						amount = load_stats("player")[0];
					}
					psql = "insert into inventory values(?, ?, ?, ?)";
					pstmt = conn.prepareStatement(psql);
					pstmt.setInt(1, count + 1);
					pstmt.setInt(2, request_number);
					pstmt.setString(3, request_name);
					pstmt.setInt(4, amount);
					pstmt.executeQuery();
					retry += 1;
				}
			}
		}catch(SQLException se) {
			se.printStackTrace();
		}catch(Exception e) {
			e.printStackTrace();
		}
		System.err.println("ERROR - Can't find " + request_name + " of record " + request_number);
		
		return amount;
	}
	public boolean update_inventory(int request_number, String request_name, int request_amount) {
		String psql = "";
		try {
			if(request_amount == 0 && !(request_name.equals("money")) && 
					!(request_name.equals("health_point"))) { // Delete item in inventory.
				psql = "delete from inventory "
						+ "where number_record=? and name_item=?";
				pstmt = conn.prepareStatement(psql);
				pstmt.setInt(1, request_number);
				pstmt.setString(2, request_name);
				pstmt.executeQuery();
				stmt = conn.createStatement();
				stmt.executeQuery("set @count = 0");
				stmt.executeQuery("update inventory "
						+ "set inventory.number=@count:=@count+1");
			}else {
				psql = "update inventory set amount=? "
						+ "where number_record=? and name_item=?";
				pstmt = conn.prepareStatement(psql);
				pstmt.setInt(1, request_amount);
				pstmt.setInt(2, request_number);
				pstmt.setString(3, request_name);
				pstmt.executeQuery();
			}
			return true;
		}catch(SQLException se) {
			se.printStackTrace();
		}catch(Exception e) {
			e.printStackTrace();
		}
		return false;
	}
	public Sprite[] load_portal(Sprite[] player_and_field, String request_name) {
		try {
			String psql = "select out_field, out_x, out_y, out_direction "
					+ "from portal where in_event=?";
			pstmt = conn.prepareStatement(psql);
			pstmt.setString(1, request_name);
			rs = pstmt.executeQuery();
			
			if(rs.next()) {
				player_and_field[0].setLocation_real(rs.getInt("out_x"), rs.getInt("out_y"));
				player_and_field[0].setDirection(rs.getInt("out_direction"));
				player_and_field[1] = load_field(rs.getString("out_field"));
			}else {
				System.err.println("ERROR - Can't find destination of portal");
				return null;
			}
		}catch(SQLException se) {
			se.printStackTrace();
		}catch(Exception e) {
			e.printStackTrace();
		}
		return player_and_field;
	}
	public List<Item> load_shop(String request_name) {
		List<Item> items = null;
		try {
			String psql = "select i.name, i._type, i.description, i.usable, i.price, i.effect, oi.amount "
					+ "from (select name from _event ev0 where name=?) ev "
					+ "inner join owned_item oi on ev.name = oi.name_event "
					+ "inner join item i on oi.name_item = i.name";
			pstmt = conn.prepareStatement(psql);
			pstmt.setString(1, request_name);
			rs = pstmt.executeQuery();
			
			items = new ArrayList<Item>();
			while(rs.next()) {
				items.add(new Item(rs.getString("name"),
						rs.getString("_type"),
						rs.getString("description"), 
						rs.getBoolean("usable"), 
						rs.getInt("price"),
						rs.getInt("effect"),
						rs.getInt("amount")));
			}
		}catch(SQLException se) {
			se.printStackTrace();
		}catch(Exception e) {
			e.printStackTrace();
		}
		return items;
	}
	public Item load_item(String request_name){
		Item item = null;
		try {
			String psql = "select name, _type, description, usable, price, effect "
					+ "from item where name=?";
			pstmt = conn.prepareStatement(psql);
			pstmt.setString(1, request_name);
			rs = pstmt.executeQuery();
			rs.next();
			item = new Item(rs.getString("name"), 
					rs.getString("_type"), 
					rs.getString("description"), 
					rs.getBoolean("usable"), 
					rs.getInt("price"), 
					rs.getInt("effect"),
					1);
		}catch(SQLException se) {
			se.printStackTrace();
		}catch(Exception e) {
			e.printStackTrace();
		}
		return item;
	}
	public List<String[]> select_column_in_event(String request_name, String request_name2, 
			String request_column_name, 
			String request_value) {
		List<String[]> result = null;
		try {
			String psql = "";
			// Can't set the column name as pstmt.
			psql = "select " + request_name + ", " + request_name2 + " from _event "
					+ "where " + request_column_name + "=?";
			pstmt = conn.prepareStatement(psql);
			try { // Integer value.
				pstmt.setInt(1, Integer.parseInt(request_value));
			}catch(NumberFormatException nfe) { // String value.
				pstmt.setString(1, request_value);
			}
			rs = pstmt.executeQuery();
			result = new ArrayList<String[]>();
			while(rs.next()) {
				String[] array_str = {rs.getString(request_name), rs.getString(request_name2)};
				result.add(array_str);
			}
		}catch(SQLException se) {
			se.printStackTrace();
			JOptionPane.showMessageDialog(null, "se " + se.getMessage());
		}catch(Exception e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(null, "e " + e.getMessage());
		}
		return result;
	}
	public boolean update_column_in_event(String request_name, String request_column_name, 
			String request_value) {
		try {
			String psql = "";
			// Can't set the column name as pstmt.
			psql = "update _event set " + request_column_name + "=? where name=?";
			pstmt = conn.prepareStatement(psql);
			
			try { // Integer value.
				pstmt.setInt(1, Integer.parseInt(request_value));
			}catch(NumberFormatException nfe) { // String value.
				pstmt.setString(1, request_value);
			}
			pstmt.setString(2, request_name);
			pstmt.executeQuery();
		}catch(SQLException se) {
			se.printStackTrace();
			JOptionPane.showMessageDialog(null, "se " + se.getMessage());
			return false;
		}catch(Exception e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(null, "e " + e.getMessage());
			return false;
		}
		return true;
	}
	public void regeneration_forest(int block_size, 
			String request_name) {
		try {
			String sql = "";
			String psql = "";
			
			// Delete the all bamboo.
			sql = "delete ev from _event ev "
					+ "inner join (select name from _field f0 where f0.biome='forest') f "
					+ "on ev.name_field=f.name "
					+ "where ev._type='destroyable' or ev._type='destroyed'";
			stmt = conn.createStatement();
			stmt.executeQuery(sql);
			stmt = conn.createStatement();
			stmt.executeQuery("set @count = 0");
			stmt.executeQuery("update _event "
					+ "set _event.number=@count:=@count+1");
			
			// Load the field name that biome is forest.
			ArrayList<String[]> field_name_and_image = new ArrayList<String[]>();
			sql = "select name, name_image from _field where biome='forest'";
			stmt = conn.createStatement();
			rs = stmt.executeQuery(sql);
			while(rs.next()) {
				String[] name_and_image = {rs.getString("name"), rs.getString("name_image")};
				field_name_and_image.add(name_and_image);
			}
			
			// Part of insert the bamboo.
			for(int i = 0; i < field_name_and_image.size(); i++) {
				String image_path = System.getProperty("user.dir") + 
						"\\src\\moveMove\\images\\" + field_name_and_image.get(i)[1];
				BufferedImage image = null;
				int[] size = {-1, -1};
				try {
					image = ImageIO.read(new File(image_path));
					size[0] = image.getWidth();
					size[1] = image.getHeight();
				}catch(IOException ioe){
					System.err.println("ERROR - Cannot load the image : " + field_name_and_image.get(i)[1]);
					ioe.printStackTrace();
					return;
				}catch(Exception e) {
					e.printStackTrace();
					return;
				}
				
				boolean[][] possible_insert = 
						new boolean[size[0]/block_size][size[1]/block_size];
				
				// For random.
				ArrayList<int[]> remained_insert = new ArrayList<int[]>();
				
				// Set the all location to true on field.
				for(int w = 0; w < possible_insert.length; w++) {
					for(int h = 0; h < possible_insert[w].length; h++) {
						possible_insert[w][h] = true;
						int[] arr_remain = {w, h};
						remained_insert.add(arr_remain);
					}
				}  // set to false the random generate bamboo.
				List<Sprite> sprites = load_sprite(field_name_and_image.get(i)[0]);
				for(int s = 0; s < sprites.size(); s++) {
					int x = sprites.get(i).getLocation_real()[0]/block_size;
					int y = sprites.get(i).getLocation_real()[1]/block_size;

					// Set to false the already exist location and around.
					possible_insert[x][y] = false;
					try {
						possible_insert[x-1][y-1] = false;
						for(int re = 0; re < remained_insert.size(); re++) {
							if(x-1 == remained_insert.get(re)[0] &&
									y-1 == remained_insert.get(re)[1]) {
								remained_insert.remove(re);
								break;
							}
						}
					}catch(ArrayIndexOutOfBoundsException aioobe){
						// Don't anything.
					}
					try {
						possible_insert[x+1][y-1] = false;
						for(int re = 0; re < remained_insert.size(); re++) {
							if(x+1 == remained_insert.get(re)[0] &&
									y-1 == remained_insert.get(re)[1]) {
								remained_insert.remove(re);
								break;
							}
						}
					}catch(ArrayIndexOutOfBoundsException aioobe){
						// Don't anything.
					}
					try {
						possible_insert[x-1][y+1] = false;
						for(int re = 0; re < remained_insert.size(); re++) {
							if(x-1 == remained_insert.get(re)[0] &&
									y+1 == remained_insert.get(re)[1]) {
								remained_insert.remove(re);
								break;
							}
						}
					}catch(ArrayIndexOutOfBoundsException aioobe){
						// Don't anything.
					}
					try {
						possible_insert[x+1][y+1] = false;
						for(int re = 0; re < remained_insert.size(); re++) {
							if(x+1 == remained_insert.get(re)[0] &&
									y+1 == remained_insert.get(re)[1]) {
								remained_insert.remove(re);
								break;
							}
						}
					}catch(ArrayIndexOutOfBoundsException aioobe){
						// Don't anything.
					}
				}
				
				// Set to false the border line.
				for(int x = 0; x < possible_insert.length; x++) {
					possible_insert[x][0] = false;
					for(int re = 0; re < remained_insert.size(); re++) {
						if(x == remained_insert.get(re)[0] &&
								0 == remained_insert.get(re)[1]) {
							remained_insert.remove(re);
							break;
						}
					}
					possible_insert[x][possible_insert[x].length-1] = false;
					for(int re = 0; re < remained_insert.size(); re++) {
						if(x == remained_insert.get(re)[0] &&
								possible_insert[x].length-1 == remained_insert.get(re)[1]) {
							remained_insert.remove(re);
							break;
						}
					}
				}
				for(int y = 0; y < possible_insert[0].length; y++) {
					possible_insert[0][y] = false;
					for(int re = 0; re < remained_insert.size(); re++) {
						if(0 == remained_insert.get(re)[0] &&
								y == remained_insert.get(re)[1]) {
							remained_insert.remove(re);
							break;
						}
					}
					possible_insert[possible_insert.length-1][y] = false;
					for(int re = 0; re < remained_insert.size(); re++) {
						if(possible_insert.length-1 == remained_insert.get(re)[0] &&
								y == remained_insert.get(re)[1]) {
							remained_insert.remove(re);
							break;
						}
					}
				}
				
				Random random = new Random();
				for(int number = 1; remained_insert.size() != 0; number++) {
					int location_insert = random.nextInt(remained_insert.size());
					stmt = conn.createStatement();
					rs = stmt.executeQuery("select count(*) from _event");
					rs.next();
					int count = rs.getInt("count(*)");
					psql = "insert into _event values(?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
					pstmt = conn.prepareStatement(psql);
					pstmt.setInt(1, count + 1);
					pstmt.setString(2, request_name + " " + number + " in " + 
					field_name_and_image.get(i)[0]);
					pstmt.setString(3, field_name_and_image.get(i)[0]);
					pstmt.setString(4, request_name);
					pstmt.setString(5, "destroyable");
					pstmt.setInt(6, remained_insert.get(location_insert)[0]*block_size);
					pstmt.setInt(7, remained_insert.get(location_insert)[1]*block_size);
					pstmt.setInt(8, 6);
					pstmt.setString(9, "");
					pstmt.setBoolean(10, false);
					pstmt.executeQuery();
					int x = remained_insert.get(location_insert)[0];
					int y = remained_insert.get(location_insert)[1];
					remained_insert.remove(location_insert);
					int[] lot_x = {x-2, x, x+2, x-1, x+1, x-2, x+2, x-1, x+1, x-2, x, x+2};
					int[] lot_y = {y-2, y-2, y-2, y-1, y-1, y, y, y+1, y+1, y+2, y+2, y+2};
					for(int index_xy = 0; index_xy < lot_x.length; index_xy++) {
						for(int re = 0; re < remained_insert.size(); re++) {
							if(lot_x[index_xy] == remained_insert.get(re)[0] &&
									lot_y[index_xy] == remained_insert.get(re)[1]) {
								remained_insert.remove(re);
								break;
							}
						}			
					}
				}
			}
		}catch(SQLException se) {
			se.printStackTrace();
			JOptionPane.showMessageDialog(null, se.getMessage());
		}catch(Exception e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(null, e.getMessage());
		}
		return;
	}
	
	// For develop.
	public Container_event load_event(String request_name, int request_x, int request_y) {
		Container_event container_event = null;
		try {
			String psql = "select name, name_field, name_sprite, _type, "
					+ "location_x, location_y, direction, dialog "
					+ "from _event "
					+ "where name_field=? and location_x=? and location_y=?";
			pstmt = conn.prepareStatement(psql);
			pstmt.setString(1, request_name);
			pstmt.setInt(2, request_x);
			pstmt.setInt(3, request_y);
			rs = pstmt.executeQuery();
			
			if(rs.next()) {
				container_event = new Container_event(
						rs.getString("name"), 
						rs.getString("name_field"), 
						rs.getString("name_sprite"), 
						rs.getString("_type"), 
						rs.getInt("location_x"), 
						rs.getInt("location_y"), 
						rs.getInt("direction"), 
						rs.getString("dialog"));
				return container_event;
			}else {
				return null;
			}
		}catch(SQLException se) {
			se.printStackTrace();
		}catch(Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	public boolean update_event(Edit_box edit_box) {
		if(edit_box.getDirection().getText().equals("")) {
			edit_box.setDirection(6);
		}
		else {
			try {
				int direction = Integer.parseInt(edit_box.getDirection().getText());
				if(direction < 3 || direction > 12 || direction%3 != 0) {
					JOptionPane.showMessageDialog(null, "Wrong direction : " + direction);
					return false;
				}
			}catch(NumberFormatException nfe) {
				JOptionPane.showMessageDialog(null, "Wrong direction : " + 
						edit_box.getDirection().getText());
				return false;
			}
		}
		if(edit_box.getName().getText().equals("")) {
			String text_sprite = edit_box.getSprite().getText();
			String text_field = edit_box.getField().getText();
			int count = load_count_sprite(text_sprite, text_field) + 1;
			edit_box.setName(text_sprite + " " + count + " in " + text_field);
		}
		try {
			String psql = "";
			if(edit_box.is_empty()) {
				stmt = conn.createStatement();
				rs = stmt.executeQuery("select count(*) from _event");
				rs.next();
				int count = rs.getInt("count(*)");
				psql = "insert into _event values(?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
				pstmt = conn.prepareStatement(psql);
				pstmt.setInt(1, count + 1);
				pstmt.setString(2, edit_box.getName().getText());
				pstmt.setString(3, edit_box.getField().getText());
				pstmt.setString(4, edit_box.getSprite().getText());
				pstmt.setString(5, edit_box.getType().getText());
				pstmt.setInt(6, Integer.parseInt(edit_box.getLocation_x().getText()));
				pstmt.setInt(7, Integer.parseInt(edit_box.getLocation_y().getText()));
				pstmt.setInt(8, Integer.parseInt(edit_box.getDirection().getText()));
				pstmt.setString(9, edit_box.getDialog().getText());
				pstmt.setBoolean(10, false);
			}else {
				psql = "update _event set name=?, name_sprite=?, "
						+ "_type=?, direction=?, dialog=? "
						+ "where name_field=? and location_x=? and location_y=?";
				pstmt = conn.prepareStatement(psql);
				pstmt.setString(1, edit_box.getName().getText());
				pstmt.setString(2, edit_box.getSprite().getText());
				pstmt.setString(3, edit_box.getType().getText());
				pstmt.setInt(4, Integer.parseInt(edit_box.getDirection().getText()));
				pstmt.setString(5, edit_box.getDialog().getText());
				pstmt.setString(6, edit_box.getField().getText());
				pstmt.setInt(7, Integer.parseInt(edit_box.getLocation_x().getText()));
				pstmt.setInt(8, Integer.parseInt(edit_box.getLocation_y().getText()));
			}
			pstmt.executeQuery();
		}catch(NumberFormatException nfe) { // "direction" exception.
			nfe.printStackTrace();
			JOptionPane.showMessageDialog(null, "nfe " + nfe.getMessage());
			return false;
		}catch(SQLException se) {
			se.printStackTrace();
			JOptionPane.showMessageDialog(null, "se " + se.getMessage());
			return false;
		}catch(Exception e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(null, "e " + e.getMessage());
			return false;
		}
		return true;
	}
	public boolean delete_event(Edit_box edit_box) {
		try {
			String psql = "";
			if(edit_box.is_empty()) {
				return false;
			}else {
				psql = "delete from _event "
						+ "where name_field=? and location_x=? and location_y=?";
				pstmt = conn.prepareStatement(psql);
				pstmt.setString(1, edit_box.getField().getText());
				pstmt.setInt(2, Integer.parseInt(edit_box.getLocation_x().getText()));
				pstmt.setInt(3, Integer.parseInt(edit_box.getLocation_y().getText()));
				pstmt.executeQuery();
				stmt = conn.createStatement();
				stmt.executeQuery("set @count = 0");
				stmt.executeQuery("update _event "
						+ "set _event.number=@count:=@count+1");
				
				String text_sprite = edit_box.getSprite().getText();
				String text_field = edit_box.getField().getText();
				stmt.executeQuery("set @count = 0");
				psql = "update _event set _event.name=concat(?, ' ', @count:=@count+1, "
						+ " ' in ', ?) where name_sprite=? and name_field=?";
				pstmt = conn.prepareStatement(psql);
				pstmt.setString(1, text_sprite);
				pstmt.setString(2, text_field);
				pstmt.setString(3, text_sprite);
				pstmt.setString(4, text_field);
				pstmt.executeQuery();
			}
		}catch(SQLException se) {
			se.printStackTrace();
			JOptionPane.showMessageDialog(null, se.getMessage());
			return false;
		}catch(Exception e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(null, e.getMessage());
			return false;
		}
		return true;
	}
	public int load_count_sprite(String request_sprite, String request_field) {
		int count = -1;
		try {
			String psql = "select count(*) from _event where name_sprite=? and "
					+ "name_field=?";
			pstmt = conn.prepareStatement(psql);
			pstmt.setString(1, request_sprite);
			pstmt.setString(2, request_field);
			rs = pstmt.executeQuery();
			rs.next();
			count = rs.getInt("count(*)");
		}catch(SQLException se) {
			se.printStackTrace();
		}catch(Exception e) {
			e.printStackTrace();
		}
		return count;
	}
	public List<String[]> load_info_sprites() {
		List<String[]> name_and_imageNames = null;
		try {
			String sql = "select name, name_image "
					+ "from sprite";
			stmt = conn.createStatement();
			rs = stmt.executeQuery(sql);
			name_and_imageNames = new ArrayList<String[]>();
			while(rs.next()) {
				String[] name_and_imageName = {
					rs.getString("name"),
					rs.getString("name_image")
				};
				name_and_imageNames.add(name_and_imageName);
			}
		}catch(SQLException se) {
			se.printStackTrace();
		}catch(Exception e) {
			e.printStackTrace();
		}
		return name_and_imageNames;
	}
	
	public void close() {
		try {
			if(stmt != null) {
				conn.close();
				System.out.println("Closed DB because stmt is not null.");
				stmt = null;
			}
		}catch(SQLException se) {}
		try {
			if(pstmt != null) {
				conn.close();
				System.out.println("Closed DB because pstmt is not null.");
				pstmt = null;
			}
		}catch(SQLException se) {}
		try {
			if(conn != null) {
				conn.close();
				System.out.println("Closed DB because conn is not null.");
				conn = null;
			}
		}catch(SQLException se) {
			se.printStackTrace();
		}
	}
}
