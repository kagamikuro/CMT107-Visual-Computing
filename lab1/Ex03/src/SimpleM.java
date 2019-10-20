import java.nio.FloatBuffer;

import javax.swing.JFrame;

import com.jogamp.opengl.GL3;
import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.GLCapabilities;
import com.jogamp.opengl.GLEventListener;
import com.jogamp.opengl.GLProfile;
import com.jogamp.opengl.awt.GLCanvas;

import static com.jogamp.opengl.GL.*;


public class SimpleM extends JFrame implements GLEventListener{

//	private final GLJPanel canvas = new GLJPanel(); // Declare a canvas
	private final GLCanvas canvas; // Declare a canvas

	private int idPoint = 0, numVAOs = 1;
	private int idBuffer = 0, numVBOs = 1;
	private int vPosition =0;
	
	private final int numVertices = 4;

	private int[] VAOs = new int[numVAOs];
	private int[] VBOs = new int[numVBOs];

	public SimpleM() {
        GLProfile glp = GLProfile.get(GLProfile.GL3);
        GLCapabilities caps = new GLCapabilities(glp);
        canvas = new GLCanvas(caps);

        canvas.addGLEventListener(this); // Listen for openGL events
        add(canvas, java.awt.BorderLayout.CENTER); // Add the canvas into the frame              
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); // Exit when click close
        setSize(500, 500);  //set the window size
        setTitle("Modulised Simple Graphics"); //window title
        setVisible(true); // Display the frame
	}

	public static void main(String[] args) {
		JFrame f = new SimpleM();

	}

	@Override
	public void display(GLAutoDrawable drawable) {
		GL3 gl = drawable.getGL().getGL3(); // Get the GL pipeline object this 
		
		gl.glClear(GL_COLOR_BUFFER_BIT);

		gl.glPointSize(5);                                                                                                                                                                                                                                                                                                                                  
		gl.glDrawArrays(GL_LINES, 0, numVertices);
	}

	@Override
	public void dispose(GLAutoDrawable drawable) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void init(GLAutoDrawable drawable) {
		GL3 gl = drawable.getGL().getGL3(); // Get the GL pipeline object this 

		gl.glGenVertexArrays(numVAOs,VAOs,0);
		gl.glBindVertexArray(VAOs[idPoint]);
		
		float [] vertexArray = {
				0f, 0f ,// put it on centre
				0.5f,0.5f,
				-0.5f,-0.5f
		};

		FloatBuffer vertices = 
				FloatBuffer.allocate(vertexArray.length).put(vertexArray);
		vertices.rewind(); //very important, otherwise, no correct result.
		
		gl.glGenBuffers(numVBOs, VBOs,0);
		gl.glBindBuffer(GL_ARRAY_BUFFER, VBOs[idBuffer]);
		gl.glBufferData(GL_ARRAY_BUFFER, vertexArray.length*(Float.SIZE/8), 
				vertices, GL_STATIC_DRAW); // pay attention to *Float.SIZE/8

		ShaderProg shaderproc = new ShaderProg(gl, "Pass.vert", "Pass.frag");
		int program = shaderproc.getProgram();
		gl.glUseProgram(program);
		
		gl.glVertexAttribPointer(vPosition, 2, GL_FLOAT, false, 0, 0L);
		gl.glEnableVertexAttribArray(vPosition);
		
	}

	@Override
	public void reshape(GLAutoDrawable drawable, int x, int y, int width,
			int height) {
		// TODO Auto-generated method stub
		
	}

}
