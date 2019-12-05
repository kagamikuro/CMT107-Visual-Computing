import Basic.ShaderProg;
import Basic.Transform;
import Basic.Vec4;
import Objects.*;

import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import javax.imageio.ImageIO;
import javax.swing.*;


import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.Arrays;


import com.jogamp.opengl.*;
import com.jogamp.opengl.awt.GLCanvas;
import static com.jogamp.opengl.GL.*;
import com.jogamp.opengl.util.FPSAnimator;

import com.jogamp.opengl.util.GLBuffers;
import com.jogamp.opengl.util.texture.TextureData;
import com.jogamp.opengl.util.texture.TextureIO;

import java.io.File;



public class Application extends JFrame implements GLEventListener, MouseListener, MouseMotionListener, ActionListener,ItemListener,KeyListener {


    //repaint the window by 60 FPS
    final FPSAnimator animator=new FPSAnimator(60, true);

    // The OpenGL profile
    GLProfile glp;

    private IntBuffer textureNames = GLBuffers.newDirectIntBuffer(1);


    // Define a Transformation instance
    // Transformation matrix is initialised as Identity;
    private Transform T = new Transform();


    // Declare a canvas
    private final GLCanvas glcanvas;



    //Declare a combobox for choosing the type of light source
    private String material = "brass";
    String[] a = {"brass","bronze","chrome","gold","pewter","silver","emerald"};
    JComboBox jcombo = new JComboBox(a);


    //Declare some JtextField for lighting
    JButton lightButton = new JButton("set light position");
    JButton cameraButton = new JButton("camera introduction");

    JTextField lightX = new JTextField(String.valueOf(100));
    JTextField lightY = new JTextField(String.valueOf(100));
    JTextField lightZ = new JTextField(String.valueOf(100));

    // Initialize shader lighting parameters
    float light_x = 100.0f;
    float light_y = 100.0f;
    float light_z = 100.0f;
    float[] lightPosition = {light_x, light_y, light_z, 0.0f};
    Vec4 lightAmbient = new Vec4(1.0f, 1.0f, 1.0f, 1.0f);
    Vec4 lightDiffuse = new Vec4(1.0f, 1.0f, 1.0f, 1.0f);
    Vec4 lightSpecular = new Vec4(1.0f, 1.0f, 1.0f, 1.0f);


    //define a initial material(Brass)
    Vec4 materialAmbient = new Vec4(0.329412f, 0.223529f, 0.027451f, 1.0f);
    Vec4 materialDiffuse = new Vec4(0.780392f, 0.568627f, 0.113725f, 1.0f);
    Vec4 materialSpecular = new Vec4(0.992157f, 0.941176f, 0.807843f, 1.0f);
    float  materialShininess = 27.8974f;


    //a flag to show whether the lighting should be changed
    boolean isLightingChange = true;


    //VAO,VBO and EBO
    private int idPoint = 0, numVAOs = 3;
    private int idBuffer = 0, numVBOs = 3;
    private int idElement=0, numEBOs = 3;


    private int[] VAOs = new int[numVAOs];
    private int[] VBOs = new int[numVBOs];
    private int[] EBOs = new int[numEBOs];
    private int[] numElements = new int[numEBOs];


    //Model parameters


    int program;

    private int vNormal;
    private long vertexSize;
    private int vColor;
    private int ModelView;
    private int NormalTransform;
    private int Projection;
    private int vPosition = 0;



    //Transformation parameters


    private float scale = 0.5f;
    private float tx = 0;
    private float ty = 0;
    private float rx = 0;
    private float ry = 0;

    //camera parameters
    private float carema_x = 0;
    private float carema_y = 0;
    private float carema_z = 0;

    //Mouse position
    private int xMouse = 0;
    private int yMouse = 0;


    //texture parameters
    ByteBuffer texImg;
    private int texWidth, texHeight;
    int texture;


    public Application() {
        //The UI design and set listener for components

        JPanel panel = new JPanel();
        panel.setLayout(new FlowLayout(FlowLayout.CENTER));
        panel.setBackground(new Color(0x00CCFF));

        JLabel lsLabel = new JLabel("-Light Source Type-");
        lsLabel.setForeground(Color.BLUE);
        panel.add(lsLabel);


        // Add itemListener for the jcomboBox
        jcombo.addItemListener(this);
        panel.add(jcombo);

        JLabel lpLabel = new JLabel("-Light Position-");
        lpLabel.setForeground(Color.BLUE);
        panel.add(lpLabel);


        panel.add(new Label("      X:     "));
        lightX.setPreferredSize(new Dimension(100,25));
        panel.add(lightX);

        panel.add(new Label("      Y:     "));
        lightY.setPreferredSize(new Dimension(100,25));
        panel.add(lightY);

        panel.add(new Label("      Z:     "));
        lightZ.setPreferredSize(new Dimension(100,25));
        panel.add(lightZ);


        lightButton.addActionListener(this);
        panel.add(lightButton);


        panel.add(new Label("           "));
        JLabel cameraLabel = new JLabel("-How to control camera-");
        cameraLabel.setForeground(Color.BLUE);
        panel.add(cameraLabel);

        cameraButton.addActionListener(this);
        panel.add(cameraButton);

        panel.setPreferredSize(new Dimension(150,800));
        this.add(BorderLayout.EAST,panel);

        //getting the capability object of GL3 profile
        glp = GLProfile.get(GLProfile.GL3);
        GLCapabilities caps = new GLCapabilities(glp);

        //The canvas
        glcanvas = new GLCanvas(caps);

        // Listen for openGL events,mouse , keyboard events
        glcanvas.addKeyListener(this);
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

    /**
     * this method is for copying object data(vertice,normals...) to the buffer
     * @param gl GL pipeline object
     * @param object the specific 3D object
     */

    private void createObject(GL3 gl, SObject object) {
        float [] vertexArray = object.getVertices();
        float [] normalArray = object.getNormals();
        int [] vertexIndexs =object.getIndices();
        float[] texCoord = object.getTextures();

        numElements[idElement] = object.getNumIndices();


        //gl.glGenVertexArrays(numVAOs,VAOs,0);
        gl.glBindVertexArray(VAOs[idPoint]);

        FloatBuffer vertices = FloatBuffer.wrap(vertexArray);
        FloatBuffer normals = FloatBuffer.wrap(normalArray);
        FloatBuffer textures = FloatBuffer.wrap(texCoord);


        //gl.glGenBuffers(numVBOs, VBOs,0);
        gl.glBindBuffer(GL_ARRAY_BUFFER, VBOs[idBuffer]);



        // Create an empty buffer with the size we need
        // and a null pointer for the data values
        vertexSize = vertexArray.length*(Float.SIZE/8);
        long normalSize = normalArray.length*(Float.SIZE/8);
        gl.glBufferData(GL_ARRAY_BUFFER, vertexSize +normalSize,
                null, GL_STATIC_DRAW); // pay attention to *Float.SIZE/8

        // Load the real data separately.  We put the colors right after the vertex coordinates,
        // so, the offset for colors is the size of vertices in bytes
        gl.glBufferSubData( GL_ARRAY_BUFFER, 0, vertexSize, vertices );
        gl.glBufferSubData( GL_ARRAY_BUFFER, vertexSize, normalSize, normals );

        IntBuffer elements = IntBuffer.wrap(vertexIndexs);

        //gl.glGenBuffers(numEBOs, EBOs,0);
        gl.glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, EBOs[idElement]);



        long indexSize = vertexIndexs.length*(Integer.SIZE/8);

        gl.glBufferData(GL_ELEMENT_ARRAY_BUFFER, indexSize,
                elements, GL_STATIC_DRAW); // pay attention to *Float.SIZE/8



    }


    /**
     * this method is for binding object
     * @param gl GL pipeline object
     */
    private void bindObject(GL3 gl){
        gl.glBindVertexArray(VAOs[idPoint]);
        gl.glBindBuffer(GL_ARRAY_BUFFER, VBOs[idBuffer]);
        gl.glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, EBOs[idElement]);
    }



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



        gl.glGenVertexArrays(numVAOs,VAOs,0);
        gl.glGenBuffers(numVBOs, VBOs,0);
        gl.glGenBuffers(numEBOs, EBOs,0);

        //create the first object: a sphere

        SObject sphere = new SSphere(1,40,40);
        idPoint=0;
        idBuffer=0;
        idElement=0;
        createObject(gl, sphere);


        setupShader(gl);



        //******************************************
        //create the second object: a teapot
        //using the Objects.STeapot class provided
        SObject teapot = new STeapot(2);
        idPoint=1;
        idBuffer=1;
        idElement=1;
        createObject(gl, teapot);

        setupShader(gl);


        //******************************************
        //create the third object: a Rugby
        //using the Objects.STeapot class provided
        SObject rugby = new Rugby();
        idPoint=2;
        idBuffer=2;
        idElement=2;
        createObject(gl,rugby);

        setupShader(gl);



        textureMapping(gl);
        // This is necessary. Otherwise, the The color on back face may display
//		    gl.glDepthFunc(GL_LESS);
        gl.glEnable(GL_DEPTH_TEST);
        //gl.glEnable(GL_BLEND);


        //System.out.println("init method called");


    }

    /**
     * Notifies the listener to perform the release of all OpenGL resources per GLContext, such as memory buffers and GLSL programs.
     * @param glAutoDrawable the triggering GLAutoDrawable
     */
    @Override
    public void dispose(GLAutoDrawable glAutoDrawable) {

    }



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
            lightingControl(gl);
            System.out.println("light called");
            isLightingChange = false;


        }


        gl.glClear(GL_COLOR_BUFFER_BIT|GL_DEPTH_BUFFER_BIT);



        //Transformation for the first object (sphere)
        T.initialize();
        //******************************************

        T.translate((float)2,(float)0,(float)-1.9);
        T.scale((float)0.3,(float)0.3,(float)0.3);
        T.rotateX(270);
        T.rotateY(0);


        //Send model_view and normal transformation matrices to shader.
        //Here parameter 'true' for transpose means to convert the row-major
        //matrix to column major one, which is required when vertices'
        //location vectors are pre-multiplied by the model_view matrix.


        //Locate camera
        T.lookAt(carema_x, carema_y, carema_z, 0, 0, -100, 0, 1, 0);  	//Default

        gl.glUniformMatrix4fv( ModelView, 1, true, T.getTransformv(), 0 );
        gl.glUniformMatrix4fv( NormalTransform, 1, true, T.getInvTransformTv(), 0 );

        //bind and draw the first object
        idPoint=0;
        idBuffer=0;
        idElement=0;
        bindObject(gl);

        gl.glDrawElements(GL_TRIANGLES, numElements[idElement], GL_UNSIGNED_INT, 0);



        T.initialize();

        //Locate camera
        T.lookAt(carema_x, carema_y, carema_z, 0, 0, -100, 0, 1, 0);



        T.translate((float)1,(float)2,(float)0);
        T.rotateX(270);
        T.rotateY(0);
        T.scale((float)0.1,(float)0.1,(float)0.1);


        gl.glUniformMatrix4fv( ModelView, 1, true, T.getTransformv(), 0 );
        gl.glUniformMatrix4fv( NormalTransform, 1, true, T.getInvTransformTv(), 0 );

        //to put the second object (teapot) to appropriate place
        idPoint=1;
        idBuffer=1;
        idElement=1;
        bindObject(gl);

        gl.glDrawElements(GL_TRIANGLES, numElements[idElement], GL_UNSIGNED_INT, 0);




        T.initialize();

        //Locate camera
        T.lookAt(carema_x, carema_y, carema_z, 0, 0, -100, 0, 1, 0);  	//Default

        T.translate((float)-1,(float)2,(float)0);
        T.scale((float)0.3,(float)0.3,(float)0.3);


        gl.glUniformMatrix4fv( ModelView, 1, true, T.getTransformv(), 0 );
        gl.glUniformMatrix4fv( NormalTransform, 1, true, T.getInvTransformTv(), 0 );


        //to put the third object (rugby) to appropriate place
        idPoint=2;
        idBuffer=2;
        idElement=2;
        bindObject(gl);

        //to map texture to the third object
        gl.glEnable(GL3.GL_TEXTURE_2D);
        gl.glBindTexture(gl.GL_TEXTURE_2D, textureNames.get(0));

        gl.glDrawElements(GL_TRIANGLES, numElements[idElement], GL_UNSIGNED_INT, 0);


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


    /**
     *The called once every time the mouse moves while a mouse button is pressed.
     * @param e
     */
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
            scale *= Math.pow(1.1, (y-yMouse) * 0.5);
            xMouse = x;
            yMouse = y;


        }
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        xMouse = e.getX();
        yMouse = e.getY();
    }




    public static void main(String[] args) {
        Application app = new Application();

    }

    /**
     *
     * @param e
     */
    @Override
    public void actionPerformed(ActionEvent e) {
        if(e.getSource() == lightButton) {
            try {
                float x = Float.parseFloat(lightX.getText());
                float y = Float.parseFloat(lightY.getText());
                float z = Float.parseFloat(lightZ.getText());
                lightPosition[0] = x;
                lightPosition[1] = y;
                lightPosition[2] = z;
                System.out.println("lighting position changed: x=" + x + " y=" + y + " z=" + z);
                isLightingChange = true;
            }
            catch(Exception ex){
                System.out.println("please input numbers!");
            }
        }
        if(e.getSource() == cameraButton) {

            System.out.println("camera Button");
            JDialog introduction = new JDialog(this);
            introduction.add(new JLabel("Firstly click left part to get the focus,Then control camera:w-Up s-Down a-Left d-Right i-Close k-Away"));
            introduction.setBounds(620,320,750,100);
            introduction.setVisible(true);
        }
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

    /**
     * this method is for getting relevant data from shader files and setup shader
     * @param gl GL pipeline object
     */
    private void setupShader(GL3 gl){
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

    }


    /**
     *  select different kinds of lighting material and set up lighting
     * @param gl GL pipeline object
     */
    private void lightingControl(GL3 gl){

        switch (String.valueOf(material)) {

            case "brass":

                //Brass material
                materialAmbient = new Vec4(0.329412f, 0.223529f, 0.027451f, 1.0f);
                materialDiffuse = new Vec4(0.780392f, 0.568627f, 0.113725f, 1.0f);
                materialSpecular = new Vec4(0.992157f, 0.941176f, 0.807843f, 1.0f);
                materialShininess = 27.8974f;

                break;


            case "bronze":

                //Bronze material
                materialAmbient = new Vec4(0.2125f, 0.1275f, 0.054f, 1.0f);
                materialDiffuse = new Vec4(0.714f, 0.4284f, 0.18144f, 1.0f);
                materialSpecular = new Vec4(0.393548f, 0.271906f, 0.166721f, 1.0f);
                materialShininess = 25.6f;

                break;


            case "chrome":

                //Bronze material
                materialAmbient = new Vec4(0.25f, 0.25f, 0.25f, 1.0f);
                materialDiffuse = new Vec4(0.4f, 0.4f, 0.4f, 1.0f);
                materialSpecular = new Vec4(0.774597f, 0.774597f, 0.774597f, 1.0f);
                materialShininess = 76.8f;

                break;

            case "gold":

                //Gold material
                materialAmbient = new Vec4(0.24725f, 0.1995f, 0.0745f, 1.0f);
                materialDiffuse = new Vec4(0.75164f, 0.60648f, 0.22648f, 1.0f);
                materialSpecular = new Vec4(0.628281f, 0.555802f, 0.366065f, 1.0f);
                materialShininess = 51.2f;

                break;

            case "pewter":

                //Gold material
                materialAmbient = new Vec4(0.105882f, 0.058824f, 0.113725f, 1.0f);
                materialDiffuse = new Vec4(0.427451f, 0.470588f, 0.541176f, 1.0f);
                materialSpecular = new Vec4(0.333333f, 0.333333f, 0.521569f, 1.0f);
                materialShininess = 9.84615f;

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
                materialAmbient = new Vec4(0.0215f, 0.1745f, 0.0215f, 0.55f);
                materialDiffuse = new Vec4(0.07568f, 0.61424f, 0.07568f, 0.55f);
                materialSpecular = new Vec4(0.633f, 0.727811f, 0.633f, 0.55f);
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

        System.out.println(Arrays.toString(lightPosition));
        gl.glUniform4fv(gl.glGetUniformLocation(program, "LightPosition"),
                1, lightPosition, 0);

        gl.glUniform1f(gl.glGetUniformLocation(program, "Shininess"),
                materialShininess);


    }

    /**
     * this method implemented texture mapping though the shader
     * @param gl GL pipeline object
     */
    private void textureMapping(GL3 gl){


        try {
            // Load texture
            glp = GLProfile.get(GLProfile.GL3);

            TextureData textureData = TextureIO.newTextureData(glp, new File("WelshDragon.jpg"), false, TextureIO.JPG);

            // Generate texture name
            gl.glGenTextures(1, textureNames);

            // Bind the texture
            gl.glBindTexture(gl.GL_TEXTURE_2D, textureNames.get(0));

            // Specify the format of the texture
            gl.glTexImage2D(gl.GL_TEXTURE_2D,
                    0,
                    textureData.getInternalFormat(),
                    textureData.getWidth(),
                    textureData.getHeight(),
                    textureData.getBorder(),
                    textureData.getPixelFormat(),
                    textureData.getPixelType(),
                    textureData.getBuffer());

            System.out.println(textureData.toString());

            // Set the sampler parameters
            gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
            gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR_MIPMAP_LINEAR);
            gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_REPEAT);
            gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_REPEAT);

            // Generate mip maps
            gl.glGenerateMipmap(GL_TEXTURE_2D);

            // Deactivate texture
            gl.glBindTexture(GL_TEXTURE_2D, 0);

        }
        catch (IOException io) {
            io.printStackTrace();
        }


    }




    /**
     * get key from keyboard to control the carema
     * @param e key event
     */
    @Override
    public void keyTyped(KeyEvent e) {
        //System.out.println("The charactor you typed : " + "\"" + e.getKeyChar() + "\"");
        if(e.getKeyChar() == 'a')
            carema_x -= 0.1;
        if(e.getKeyChar() == 'd')
            carema_x += 0.1;
        if(e.getKeyChar() == 'w')
            carema_y += 0.1;
        if(e.getKeyChar() == 's')
            carema_y -= 0.1;
        if(e.getKeyChar() == 'i')
            carema_z -= 0.1;
        if(e.getKeyChar() == 'k')
            carema_z += 0.1;
    }

    @Override
    public void keyPressed(KeyEvent e) {

    }

    @Override
    public void keyReleased(KeyEvent e) {

    }
}
