package moveMove;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;

public class Loading_thread extends Thread{
	private List<BufferedImage> image = null;
	private int[] size = {-1, -1};
	private int direction = -1;
	
	private Viewer_world viewer_world = null;
	
	public Loading_thread(String[] image_name, Viewer_world viewer_world) {
		// TODO Auto-generated constructor stub
		image = new ArrayList<BufferedImage>();
		direction = 6;
		this.viewer_world = viewer_world;
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
				image.add(ImageIO.read(new File(image_path)));
			}
			size[0] = image.get(0).getWidth();
			size[1] = image.get(0).getHeight();
		}catch(IOException ioe) {
			System.err.println("ERROR - Cannot load the image : " + image_name[i]);
			ioe.printStackTrace();
		}catch(Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
		super.run();
		try {
			while(true) {
				viewer_world.repaint();
				sleep(500);
				direction = (direction + 3) % 12;
				if(direction == 0) {
					direction = 12;
				}
			}
		}catch(InterruptedException ie) {
			// Remove the loading image as repaint().
		}
	}
	
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
	public int[] getSize() {
		return size;
	}
}
