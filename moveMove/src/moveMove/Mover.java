package moveMove;

import javax.swing.JFrame;

public class Mover extends JFrame{
	public Mover() {
		setUndecorated(true); // Delete a menu bar.
		// We can set size and location of inner content when did typing the code below.
		setLayout(null);
		Viewer_world viewer_world = new Viewer_world(this);
		add(viewer_world);
		setSize(viewer_world.getRESOLUTION()[0], viewer_world.getRESOLUTION()[1]);
		setResizable(false);
		
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setLocationRelativeTo(null); // set to center the main screen.
		setVisible(true);
	}
	public static void main(String[] args) {
		new Mover();
	}
}
