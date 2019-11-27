public class Cube extends SObject  {

    private final double EPS = 1E-8;
    private int level; // division level

    public Cube() {
        super();
        level = 0;
        update();
    }


    protected void genData() {

        numVertices = 36;

        float [] vertexArray = {
                // front face
                1,  1,  1,    // triangle 1
                -1,  1,  1,
                -1, -1,  1,
                1,  1,  1,    // triangle 2
                -1, -1,  1,
                1, -1,  1,
                // back face
                1,  1, -1,    // triangle 1
                -1, -1, -1,
                -1,  1, -1,
                1,  1, -1,    // triangle 2
                1, -1, -1,
                -1, -1, -1,
                // left face
                -1,  1,  1,    // triangle 1
                -1,  1, -1,
                -1, -1,  1,
                -1,  1, -1,    // triangle 2
                -1, -1, -1,
                -1, -1,  1,
                // right face
                1,  1,  1,    // triangle 1
                1, -1,  1,
                1,  1, -1,
                1,  1, -1,    // triangle 2
                1, -1,  1,
                1, -1, -1,
                // top face
                1,  1,  1,    // triangle 1
                1,  1, -1,
                -1,  1,  1,
                -1,  1,  1,    // triangle 2
                1,  1, -1,
                -1,  1, -1,
                // bottom face
                1, -1,  1,    // triangle 1
                -1, -1,  1,
                1, -1, -1,
                -1, -1,  1,    // triangle 2
                -1, -1, -1,
                1, -1, -1
        };

      vertices = vertexArray;


    }







}
