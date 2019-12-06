package package1;

import java.awt.Component;
import java.awt.Graphics;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.JFrame;



class Application extends Component implements KeyListener {
	
	private  BufferedImage in,out;
	
	public Application() {
		try {
			// load image
			in = ImageIO.read(new File("Daffodil.jpg"));
			//in = ImageIO.read(new File("Room.jpg"));
			//get a deep copy of the input image
			out = Harris.deepCopy(in);
		}catch(IOException e) {
			e.printStackTrace();
		}
		
		addKeyListener(this);
		
		

	}

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		//UI design
		JFrame frame = new JFrame("Image Processing");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);	
		Application app = new Application();	
		frame.add("Center",app);
		frame.pack();
		frame.setSize(600, 700);
		app.requestFocusInWindow();
		frame.setVisible(true);
		
	}

	/**
	 * process the input image by Harris Algorithm
	 */
	void processing() {
		out =  Harris.Apply_Harris(Harris.deepCopy(in));
		repaint();
	}
	
	public void paint(Graphics g) {
		g.drawImage(out, 0, 0, null);
	}

	@Override
	public void keyPressed(KeyEvent e) {
		//press 'P' to process
		if(e.getKeyChar() == 'p'||e.getKeyChar() == 'P')
			processing();
	}

	@Override
	public void keyReleased(KeyEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void keyTyped(KeyEvent e) {
		// TODO Auto-generated method stub
		
	}

}
