import java.awt.*;
import java.awt.event.*;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.Arrays;

import javax.swing.*;

import com.jogamp.opengl.*;
import com.jogamp.opengl.awt.GLCanvas;

import static com.jogamp.opengl.GL.*;
import static com.jogamp.opengl.GL2GL3.GL_LINE;

import com.jogamp.opengl.util.FPSAnimator;

public class Application extends JFrame implements GLEventListener, MouseListener, MouseMotionListener, ActionListener,ItemListener {

    final FPSAnimator animator=new FPSAnimator(60, true);

    // Define a Transformation instance
    // Transformation matrix is initialised as Identity;
    private Transform T = new Transform();


    // Declare a canvas
    private final GLCanvas glcanvas;



    //Declare a combobox for choosing the type of light source
    private String material = "brass";
    String[] a = {"brass","gold","silver","emerald"};
    JComboBox jcombo = new JComboBox(a);





    private int idPoint = 0, numVAOs = 3;
    private int idBuffer = 0, numVBOs = 3;
    private int idElement=0, numEBOs = 3;
    private int vPosition = 0;

    private int[] VAOs = new int[numVAOs];
    private int[] VBOs = new int[numVBOs];
    private int[] EBOs = new int[numEBOs];

    //Model parameters
    private int[] numElements = new int[numEBOs];

    private int vNormal;
    private int NormalTransform;
    private long vertexSize;
    private int vColor;

    //material

    int program;
    // Initialize shader lighting parameters
    float[] lightPosition = {100.0f, 100.0f, 100.0f, 0.0f};
    Vec4 lightAmbient = new Vec4(1.0f, 1.0f, 1.0f, 1.0f);
    Vec4 lightDiffuse = new Vec4(1.0f, 1.0f, 1.0f, 1.0f);
    Vec4 lightSpecular = new Vec4(1.0f, 1.0f, 1.0f, 1.0f);



    Vec4 materialAmbient = new Vec4(0.329412f, 0.223529f, 0.027451f, 1.0f);
    Vec4 materialDiffuse = new Vec4(0.780392f, 0.568627f, 0.113725f, 1.0f);
    Vec4 materialSpecular = new Vec4(0.992157f, 0.941176f, 0.807843f, 1.0f);
    float  materialShininess = 27.8974f;

    boolean isLightingChange = true;


    //Transformation parameters
    private int ModelView;
    private int Projection;
    private float scale = 0.5f;
    private float tx = 0;
    private float ty = 0;
    private float rx = 0;
    private float ry = 0;


    //Mouse position
    private int xMouse = 0;
    private int yMouse = 0;

    public Application() {
        JPanel panel = new JPanel();
        jcombo.addActionListener(this);
        jcombo.addItemListener(this);
        panel.add(jcombo);
        this.add(BorderLayout.EAST,panel);

        //getting the capability object of GL3 profile
        GLProfile glp = GLProfile.get(GLProfile.GL3);
        GLCapabilities caps = new GLCapabilities(glp);

        //The canvas
        glcanvas = new GLCanvas(caps);

        // Listen for openGL events,mouse events
        glcanvas.addGLEventListener(this);
        glcanvas.addMouseListener(this);
        glcanvas.addMouseMotionListener(this);

        // Add the canvas into the frame
        add(glcanvas, java.awt.BorderLayout.CENTER); // Add the canvas into the frame

        animator.add(glcanvas);

        //set the window size
        setSize(1000, 800);

        //set the window title
        setTitle("Graphics Rendering Framework"); //window title

        // Display the frame
        setVisible(true);

        // set EXIT_ON_CLOSE action
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        animator.start();
    }


    private void createObject(GL3 gl, SObject object) {
        float [] vertexArray = object.getVertices();
        float [] normalArray = object.getNormals();
        int [] vertexIndexs =object.getIndices();
        numElements[idElement] = object.getNumIndices();

        gl.glGenVertexArrays(numVAOs,VAOs,0);
        gl.glBindVertexArray(VAOs[idPoint]);

        FloatBuffer vertices = FloatBuffer.wrap(vertexArray);
        FloatBuffer normals = FloatBuffer.wrap(normalArray);

        gl.glGenBuffers(numVBOs, VBOs,0);
        gl.glBindBuffer(GL_ARRAY_BUFFER, VBOs[idBuffer]);

        // Create an empty buffer with the size we need
        // and a null pointer for the data values
        long vertexSize = vertexArray.length*(Float.SIZE/8);
        long normalSize = normalArray.length*(Float.SIZE/8);
        gl.glBufferData(GL_ARRAY_BUFFER, vertexSize +normalSize,
                null, GL_STATIC_DRAW); // pay attention to *Float.SIZE/8

        // Load the real data separately.  We put the colors right after the vertex coordinates,
        // so, the offset for colors is the size of vertices in bytes
        gl.glBufferSubData( GL_ARRAY_BUFFER, 0, vertexSize, vertices );
        gl.glBufferSubData( GL_ARRAY_BUFFER, vertexSize, normalSize, normals );

        IntBuffer elements = IntBuffer.wrap(vertexIndexs);

        gl.glGenBuffers(numEBOs, EBOs,0);
        gl.glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, EBOs[idElement]);


        long indexSize = vertexIndexs.length*(Integer.SIZE/8);
        gl.glBufferData(GL_ELEMENT_ARRAY_BUFFER, indexSize,
                elements, GL_STATIC_DRAW); // pay attention to *Float.SIZE/8



    }

    private void bindObject(GL3 gl){
        gl.glBindVertexArray(VAOs[idPoint]);
        gl.glBindBuffer(GL_ARRAY_BUFFER, VBOs[idBuffer]);
        gl.glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, EBOs[idElement]);
    };


    /**
     * Called by the drawable immediately after the OpenGL context is initialized.
     * @param glAutoDrawable the triggering GLAutoDrawable
     */
    @Override
    public void init(GLAutoDrawable glAutoDrawable) {



        // Get the GL pipeline object this
        GL3 gl = glAutoDrawable.getGL().getGL3();

        System.out.println("GL_Version: " + gl.glGetString(GL_VERSION));

        gl.glEnable(GL_CULL_FACE);



        //create the first object: a sphere
        SObject sphere = new SSphere(1,40,40);
        idPoint=0;
        idBuffer=0;
        idElement=0;
        createObject(gl, sphere);



        //******************************************
        //create the second object: a teapot
        //using the STeapot class provided
        SObject teapot = new STeapot(2);
        idPoint=1;
        idBuffer=1;
        idElement=1;
        //createObject(gl, teapot);

        //compile and use the shader program
        ShaderProg shaderproc = new ShaderProg(gl, "Gouraud.vert", "Gouraud.frag");
        program = shaderproc.getProgram();
        //System.out.println("program: "+program);
        gl.glUseProgram(program);

        // Initialize the vertex position attribute in the vertex shader
        vPosition = gl.glGetAttribLocation( program, "vPosition" );
        gl.glEnableVertexAttribArray(vPosition);
        gl.glVertexAttribPointer(vPosition, 3, GL_FLOAT, false, 0, 0L);

        // Initialize the vertex color attribute in the vertex shader.
        // The offset is the same as in the glBufferSubData, i.e., vertexSize
        // It is the starting point of the color data
        vNormal = gl.glGetAttribLocation( program, "vNormal" );
        gl.glEnableVertexAttribArray(vNormal);
        gl.glVertexAttribPointer(vNormal, 3, GL_FLOAT, false, 0, vertexSize);

        //Get connected with the ModelView matrix in the vertex shader
        ModelView = gl.glGetUniformLocation(program, "ModelView");
        NormalTransform = gl.glGetUniformLocation(program, "NormalTransform");
        Projection = gl.glGetUniformLocation(program, "Projection");


        switch (String.valueOf(material)) {

            case "brass":

                //Brass material
                materialAmbient = new Vec4(0.329412f, 0.223529f, 0.027451f, 1.0f);
                materialDiffuse = new Vec4(0.780392f, 0.568627f, 0.113725f, 1.0f);
                materialSpecular = new Vec4(0.992157f, 0.941176f, 0.807843f, 1.0f);
                materialShininess = 27.8974f;

                break;

            case "gold":

                //Gold material
                materialAmbient = new Vec4(0.24725f, 0.1995f, 0.0745f, 1.0f);
                materialDiffuse = new Vec4(0.75164f, 0.60648f, 0.22648f, 1.0f);
                materialSpecular = new Vec4(0.628281f, 0.555802f, 0.366065f, 1.0f);
                materialShininess = 51.2f;

                break;

            case "silver":

                //silver material
                materialAmbient = new Vec4(0.19225f, 0.19225f, 0.19225f, 1.0f);
                materialDiffuse = new Vec4(0.50754f, 0.50754f, 0.50754f, 1.0f);
                materialSpecular = new Vec4(0.508273f, 0.508273f, 0.508273f, 1.0f);
                materialShininess = 51.2f;

                break;


            case "emerald":

                //emerald material
                materialAmbient = new Vec4(0.0215f, 0.1745f, 0.0215f, 1.0f);
                materialDiffuse = new Vec4(0.07568f, 0.61424f, 0.07568f, 1.0f);
                materialSpecular = new Vec4(0.633f, 0.727811f, 0.633f, 1.0f);
                materialShininess = 76.8f;

                break;

            default:
                System.out.println("please choose a correct material");
        }


        Vec4 ambientProduct = lightAmbient.times(materialAmbient);
        float[] ambient = ambientProduct.getVector();
        Vec4 diffuseProduct = lightDiffuse.times(materialDiffuse);
        float[] diffuse = diffuseProduct.getVector();
        Vec4 specularProduct = lightSpecular.times(materialSpecular);
        float[] specular = specularProduct.getVector();

        gl.glUniform4fv(gl.glGetUniformLocation(program, "AmbientProduct"),
                1, ambient, 0);
        gl.glUniform4fv(gl.glGetUniformLocation(program, "DiffuseProduct"),
                1, diffuse, 0);
        gl.glUniform4fv(gl.glGetUniformLocation(program, "SpecularProduct"),
                1, specular, 0);

        gl.glUniform4fv(gl.glGetUniformLocation(program, "LightPosition"),
                1, lightPosition, 0);

        gl.glUniform1f(gl.glGetUniformLocation(program, "Shininess"),
                materialShininess);

        isLightingChange = false;

        // This is necessary. Otherwise, the The color on back face may display
//		    gl.glDepthFunc(GL_LESS);
        gl.glEnable(GL_DEPTH_TEST);
        //gl.glEnable(GL_BLEND);


        System.out.println("init method called");


    }

    /**
     * Notifies the listener to perform the release of all OpenGL resources per GLContext, such as memory buffers and GLSL programs.
     * @param glAutoDrawable the triggering GLAutoDrawable
     */
    @Override
    public void dispose(GLAutoDrawable glAutoDrawable) {

    }

    int teapot_rotateY=1;
    int sphere_rotateX=1;

    /**
     * Called by the drawable to initiate OpenGL rendering by the client.
     * @param glAutoDrawable the triggering GLAutoDrawable
     */
    @Override
    public void display(GLAutoDrawable glAutoDrawable) {

        //System.out.println("display method called");

        // Get the GL pipeline object this
        GL3 gl = glAutoDrawable.getGL().getGL3();

        if(isLightingChange == true) {

            switch (String.valueOf(material)) {

                case "brass":

                    //Brass material
                    materialAmbient = new Vec4(0.329412f, 0.223529f, 0.027451f, 1.0f);
                    materialDiffuse = new Vec4(0.780392f, 0.568627f, 0.113725f, 1.0f);
                    materialSpecular = new Vec4(0.992157f, 0.941176f, 0.807843f, 1.0f);
                    materialShininess = 27.8974f;

                    break;

                case "gold":

                    //Gold material
                    materialAmbient = new Vec4(0.24725f, 0.1995f, 0.0745f, 1.0f);
                    materialDiffuse = new Vec4(0.75164f, 0.60648f, 0.22648f, 1.0f);
                    materialSpecular = new Vec4(0.628281f, 0.555802f, 0.366065f, 1.0f);
                    materialShininess = 51.2f;

                    break;

                case "silver":

                    //silver material
                    materialAmbient = new Vec4(0.19225f, 0.19225f, 0.19225f, 1.0f);
                    materialDiffuse = new Vec4(0.50754f, 0.50754f, 0.50754f, 1.0f);
                    materialSpecular = new Vec4(0.508273f, 0.508273f, 0.508273f, 1.0f);
                    materialShininess = 51.2f;

                    break;


                case "emerald":

                    //emerald material
                    materialAmbient = new Vec4(0.0215f, 0.1745f, 0.0215f, 1.0f);
                    materialDiffuse = new Vec4(0.07568f, 0.61424f, 0.07568f, 1.0f);
                    materialSpecular = new Vec4(0.633f, 0.727811f, 0.633f, 1.0f);
                    materialShininess = 76.8f;

                    break;

                default:
                    System.out.println("please choose a correct material");
            }


            Vec4 ambientProduct = lightAmbient.times(materialAmbient);
            float[] ambient = ambientProduct.getVector();
            Vec4 diffuseProduct = lightDiffuse.times(materialDiffuse);
            float[] diffuse = diffuseProduct.getVector();
            Vec4 specularProduct = lightSpecular.times(materialSpecular);
            float[] specular = specularProduct.getVector();

            gl.glUniform4fv(gl.glGetUniformLocation(program, "AmbientProduct"),
                    1, ambient, 0);
            gl.glUniform4fv(gl.glGetUniformLocation(program, "DiffuseProduct"),
                    1, diffuse, 0);
            gl.glUniform4fv(gl.glGetUniformLocation(program, "SpecularProduct"),
                    1, specular, 0);

            gl.glUniform4fv(gl.glGetUniformLocation(program, "LightPosition"),
                    1, lightPosition, 0);

            gl.glUniform1f(gl.glGetUniformLocation(program, "Shininess"),
                    materialShininess);

            isLightingChange = false;
            System.out.println("light called");

        }

        gl.glClear(GL_COLOR_BUFFER_BIT|GL_DEPTH_BUFFER_BIT);

        //gl.glPolygonMode(GL_FRONT_AND_BACK, GL_LINE);

        //Transformation for the first object (sphere)
        T.initialize();
        //******************************************
        //Add code here to transform (including scale)
        //T.translate((float)-2,(float)0,(float)-1.9);
        //T.scale((float)0.3,(float)0.3,(float)0.3);
        //T.rotateX(270);
        //T.rotateY(0);
        //sphere_rotateX++;
        //the first object (sphere) to appropriate location

        T.scale(scale, scale, scale);
        T.rotateX(rx);
        T.rotateY(ry);
        T.translate(tx, ty, 0);


        //Locate camera
        //T.lookAt(0, 0, 0, 0, 0, -100, 0, 1, 0);  	//Default

        //Send model_view and normal transformation matrices to shader.
        //Here parameter 'true' for transpose means to convert the row-major
        //matrix to column major one, which is required when vertices'
        //location vectors are pre-multiplied by the model_view matrix.
        gl.glUniformMatrix4fv( ModelView, 1, true, T.getTransformv(), 0 );
        gl.glUniformMatrix4fv( NormalTransform, 1, true, T.getInvTransformTv(), 0 );

        //bind and draw the first object
        idPoint=0;
        idBuffer=0;
        idElement=0;
        bindObject(gl);
        gl.glDrawElements(GL_TRIANGLES, numElements[idElement], GL_UNSIGNED_INT, 0);

        //******************************************
        //Add transformation, binding and drawing code here

        //******************************************
        //Add code here to transform (including scale)
        /*
        T.initialize();

        T.translate((float)0.2,(float)0,(float)2.3);
        T.scale((float)0.2,(float)0.2,(float)0.2);
        T.rotateX(270);
        T.rotateY(0);
        //teapot_rotateY++;

        gl.glUniformMatrix4fv( ModelView, 1, true, T.getTransformv(), 0 );
        gl.glUniformMatrix4fv( NormalTransform, 1, true, T.getInvTransformTv(), 0 );
        //to put the second object (teapot) to appropriate place
        idPoint=1;
        idBuffer=1;
        idElement=1;
        bindObject(gl);
        gl.glDrawElements(GL_TRIANGLES, numElements[idElement], GL_UNSIGNED_INT, 0);

        */




    }

    /**
     * Called by the drawable during the first repaint after the component has been resized.
     * @param glAutoDrawable the triggering GLAutoDrawable
     * @param x viewport x-coord in pixel units
     * @param y viewport y-coord in pixel units
     * @param width viewport width in pixel units
     * @param height viewport height in pixel units
     */
    @Override
    public void reshape(GLAutoDrawable glAutoDrawable, int x, int y, int width, int height) {
        GL3 gl = glAutoDrawable.getGL().getGL3(); // Get the GL pipeline object this

        gl.glViewport(x, y, width, height);

        T.initialize();

        //projection
//			T.Ortho(-1, 1, -1, 1, -1, 1);  //Default
        // to avoid shape distortion because of reshaping the viewport
        // the viewport aspect should be the same as the projection aspect
        if(height<1){height=1;}
        if(width<1){width=1;}
        float a = (float) width/ height;   //aspect
///*
        // start of orthographic projection
        if (width< height) {
            T.ortho(-1, 1, -1/a, 1/a, -1, 1);
//				T.frustum(-1, 1, -1/a, 1/a, 0.1f, 1000);
        }
        else{
            T.ortho(-1*a, 1*a, -1, 1, -1, 1);
//				T.frustum(-1*a, 1*a, -1, 1, 0.1f, 1000);
        }
        // end of orthographic projection
//*/

//			T.perspective(60, a, 0.1f, 1000);

        // Convert right-hand to left-hand coordinate system
        T.reverseZ();
        gl.glUniformMatrix4fv( Projection, 1, true, T.getTransformv(), 0 );
    }

    @Override
    public void mouseClicked(MouseEvent e) {

    }

    @Override
    public void mousePressed(MouseEvent e) {

    }

    @Override
    public void mouseReleased(MouseEvent e) {

    }

    @Override
    public void mouseEntered(MouseEvent e) {

    }

    @Override
    public void mouseExited(MouseEvent e) {

    }

    @Override
    public void mouseDragged(MouseEvent e) {
        int x = e.getX();
        int y = e.getY();
        //left button down, move the object
        if((e.getModifiers() & InputEvent.BUTTON1_MASK) != 0){
            // Fill in code here, so that the object moves
            // in the same direction as the mouse motion.
            tx +=(x-xMouse)*0.002;
            ty -=(y-yMouse)*0.002;
            //System.out.println("tx: "+tx+", ty: "+ty);
            xMouse=x;
            yMouse=y;

        }
        //right button down, rotate the object
        if((e.getModifiers() & InputEvent.BUTTON3_MASK) != 0){
            // To mimic using the mouse to rotate the object, we want that
            // when the mouse moves in horizontal (x) direction,
            // the object will rotate around the vertical (y) axis
            ry += (x-xMouse); // the rotation angle around the y axis

            // Add code here to calculate the rotation angle around the x axis
            rx += (y-yMouse);

            xMouse = x;
            yMouse = y;
        }

        //middle button down, scale the object
        if((e.getModifiers() & InputEvent.BUTTON2_MASK) != 0){
            //Add code here so that the object will scale down (shrink)
            // when the mouse moves up (y increases),
            // and it will scale up (expand) when the mouse moves down.

        }
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        xMouse = e.getX();
        yMouse = e.getY();
    }




    public static void main(String[] args) {
        Application app = new Application();

    }//end of main


    @Override
    public void actionPerformed(ActionEvent e) {
        //System.out.println("actionPerformed method called");
    }

    @Override
    public void itemStateChanged(ItemEvent e) {
        //System.out.println("itemStateChanged method called");
        if(e.getStateChange() == ItemEvent.SELECTED) {
            System.out.println((String) e.getItem());
            material = (String) e.getItem();
            isLightingChange = true;
        }
    }
}
