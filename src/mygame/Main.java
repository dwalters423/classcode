package mygame;

import com.jme3.app.SimpleApplication;
import com.jme3.app.state.VideoRecorderAppState;
import com.jme3.bullet.BulletAppState;
import com.jme3.bullet.collision.shapes.CollisionShape;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.bullet.util.CollisionShapeFactory;
import com.jme3.font.BitmapFont;
import com.jme3.font.BitmapText;
import com.jme3.font.Rectangle;
import com.jme3.input.MouseInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.MouseButtonTrigger;
import com.jme3.material.Material;
import com.jme3.material.RenderState.BlendMode;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.renderer.RenderManager;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.shape.Box;
import com.jme3.scene.shape.Sphere;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

public class Main extends SimpleApplication {
    
    private Node cubeNode = new Node();
    private Node sphereNode = new Node();
    private BulletAppState physicsState = new BulletAppState();
    private Vector3f earthGravity = new Vector3f (0f,-9.81f,0f);
    private RigidBodyControl cubeControl, 
            sphere1Controller = new RigidBodyControl (10.0f),
            sphere2Controller = new RigidBodyControl (10.0f),
            sphere3Controller = new RigidBodyControl (10.0f);
    private RandomPhysics randomPhy = new RandomPhysics();
    private BitmapText sphere1Text;
    private BitmapText sphere2Text;
    private BitmapText sphere3Text;
    private String filePath = "SphereLocations.txt";
    private BufferedWriter writer;
    private long initialTime;

    public static void main(String[] args) {
        Main app = new Main();
        app.start();
    }

    @Override
    public void simpleInitApp() {
             
        
      //initiates the buffered writer for file writing and catches IOException  
        initialTime = System.currentTimeMillis();
        
      //adds the Physics state to the application, sets the speed and global 
      //gravity variable.
        stateManager.attach(physicsState);
        physicsState.setSpeed(5f);
        physicsState.getPhysicsSpace().setGravity(earthGravity);
        
      //sets the view up. To disable flyCam, setEnabled(false) and comment setMoveSpeed.
        flyCam.setEnabled(true);
        flyCam.setMoveSpeed(300);
        cam.setLocation(new Vector3f(0,0f,-345f));
        cam.lookAtDirection(new Vector3f(0f,0f,0f), new Vector3f (0f,-1f,0f));
    
      //creates Cube environment
        initCube();
        
      //creates Balls
        initSpheres();   
        
      //creates the GUI for display of sphere locations
        initGUI();
        
      //implements ActionListeners for keys
        initActionListeners();
        //stateManager.attach(new VideoRecorderAppState());
        
    } //end simpleInitApp
    
    
  /* initCube() Creates the cube geometry and sets the physics state of the 
   * cube for the scene. 
   */  
    private void initCube() {
        
      //creates the material for the floor,
        Material floorMat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        floorMat.setTexture("ColorMap",assetManager.loadTexture("Textures/floortexture.jpg"));
        floorMat.setColor("Color", ColorRGBA.LightGray);
        
      //creates the material for the walls,
        Material wallMat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        wallMat.setTexture("ColorMap",assetManager.loadTexture("Textures/bricktexture.jpg"));
        wallMat.setColor("Color", ColorRGBA.LightGray);
        
      //creates a transparent material for the backwall to act as a viewport for the camera.  
        Material backWallMat = new Material (assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        backWallMat.setColor("Color", new ColorRGBA(1,0,0,0.0f)); 
        backWallMat.getAdditionalRenderState().setBlendMode(BlendMode.Alpha);
        
           
      //creates the floorBox  
        Box floorBox = new Box(Vector3f.ZERO, 100,0,100);
        Geometry floor = new Geometry ("Floor", floorBox);
        floor.setMaterial(floorMat);
        floor.setLocalTranslation(0,-100,0);
        cubeNode.attachChild(floor);
        
      //creates the wall boxes and attaches them to the cubeNode
        Box rightWallBox = new Box(Vector3f.ZERO, 0,100,100);
        Geometry rightWall = new Geometry ("Right Wall", rightWallBox);
        rightWall.setMaterial(wallMat);
        rightWall.setLocalTranslation(100,0,0); //should this be Control.setPhysicsLocation()
        cubeNode.attachChild(rightWall);
        
        Box leftWallBox = new Box(Vector3f.ZERO, 0,100,100);
        Geometry leftWall = new Geometry ("Left Wall", leftWallBox);
        leftWall.setMaterial(wallMat);
        leftWall.setLocalTranslation(-100,0,0);
        cubeNode.attachChild(leftWall);
        
        Box frontWallBox = new Box(Vector3f.ZERO, 100,100,0);
        Geometry frontWall = new Geometry ("Front Wall", frontWallBox);
        frontWall.setMaterial(wallMat);
        frontWall.setLocalTranslation(0,0,100);
        cubeNode.attachChild(frontWall);
        
        Box backWallBox = new Box(Vector3f.ZERO, 100,100,0);
        Geometry backWall = new Geometry ("Back Wall", backWallBox);
        backWall.setMaterial(backWallMat);
        backWall.setLocalTranslation (0,0,-100);
        cubeNode.attachChild(backWall);
        
        Box roofBox = new Box(Vector3f.ZERO, 100,0,100);
        Geometry roof = new Geometry ("Roof", roofBox);
        roof.setMaterial(wallMat);
        roof.setLocalTranslation(0,100,0);
        cubeNode.attachChild(roof);
        
      //creates the physics for the cubeNode and adds it to the physics state
        CollisionShape cubeCollisionShape = CollisionShapeFactory.createMeshShape((Node)cubeNode);
        cubeControl = new RigidBodyControl(cubeCollisionShape,0); //ensure mass==0
        cubeControl.setRestitution(0.7f); //sets the 'bounciness' of the cube
        cubeControl.setFriction(0.8f);
        cubeNode.addControl(cubeControl);
        physicsState.getPhysicsSpace().add(cubeControl);
        
      //attaches the cube to the scene
        rootNode.attachChild(cubeNode);
        
    } //end initCube()
    
   /* initSpheres() Creates the Spheres and sets the physics state of the balls
    * for the scene
    */
    private void initSpheres(){
    
      //creates Sphere mesh
        Sphere ballMesh = new Sphere(10,10,10);
        
      //creates Sphere material
        Material sphereMat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        sphereMat.setTexture("ColorMap", assetManager.loadTexture("Textures/pokeball.jpg"));
        sphereMat.setColor("Color", ColorRGBA.White);
        
        
      //creates 3 Spheres with associated physics controls
        Geometry sphere1 = new Geometry ("Sphere 1",ballMesh);
        sphere1.setMaterial(sphereMat);
        sphere1.addControl(sphere1Controller);
        sphere1Controller.setPhysicsLocation(randomPhy.getRandomVector3f(100, 100, 100)); //location (random)
        sphere1Controller.setRestitution(0.7f); //bounciness
        sphere1Controller.setFriction(.5f);
        sphereNode.attachChild(sphere1);
        
        Geometry sphere2 = new Geometry ("Sphere 2",ballMesh);
        sphere2.setMaterial(sphereMat);
        sphere2.addControl(sphere2Controller);
        sphere2Controller.setPhysicsLocation(randomPhy.getRandomVector3f(100, 100, 100)); //location (random)
        sphere2Controller.setRestitution(0.7f); //bounciness
        sphere2Controller.setFriction(.5f);
        sphereNode.attachChild(sphere2);
        
        Geometry sphere3 = new Geometry ("Sphere 3",ballMesh);
        sphere3.setMaterial(sphereMat);
        sphere3.addControl(sphere3Controller);  
        sphere3Controller.setPhysicsLocation(randomPhy.getRandomVector3f(100, 100, 100)); //location(random)
        sphere3Controller.setRestitution(0.7f); //bounciness
        sphere3Controller.setFriction(.5f);
        sphereNode.attachChild(sphere3);
        
      //Adds the sphere spatials to the physics state
        physicsState.getPhysicsSpace().add(sphere1);
        physicsState.getPhysicsSpace().add(sphere2);
        physicsState.getPhysicsSpace().add(sphere3);
          
        
      //attaches all spheres to the RootNode so they appear on the screen
        rootNode.attachChild(sphereNode);
 
        
    } //end initSpheres
    
    /*
     * initGUI() creates the GUI for the scene, displaying the spheres locations
     */
    private void initGUI(){
        
        BitmapFont font = assetManager.loadFont("Interface/Fonts/Default.fnt");
        sphere1Text = new BitmapText(font, false);
        sphere1Text.setBox(new Rectangle(0, 0, settings.getWidth(), settings.getHeight()));
        sphere1Text.setSize(font.getPreferredSize());
        sphere1Text.setLocalTranslation(0, sphere1Text.getHeight(), 0);
        guiNode.attachChild(sphere1Text);
        
        sphere2Text = new BitmapText(font, false);
        sphere2Text.setBox(new Rectangle(0, 0, settings.getWidth(), settings.getHeight()));
        sphere2Text.setSize(font.getPreferredSize());
        sphere2Text.setLocalTranslation(200, sphere2Text.getHeight(), 0);
        guiNode.attachChild(sphere2Text); 
        
        sphere3Text = new BitmapText(font, false);
        sphere3Text.setBox(new Rectangle(0, 0, settings.getWidth(), settings.getHeight()));
        sphere3Text.setSize(font.getPreferredSize());
        sphere3Text.setLocalTranslation(400, sphere3Text.getHeight(), 0);
        guiNode.attachChild(sphere3Text);   
    }
    
    private void initActionListeners () {
        
      //add Mapping for input keys here.
        inputManager.addMapping("Add velocity", new MouseButtonTrigger(MouseInput.BUTTON_LEFT));
        inputManager.addMapping("Slow down", new MouseButtonTrigger(MouseInput.BUTTON_RIGHT));
      //add all key actions here and implement their action
        
        ActionListener mouseListener = new ActionListener() {
            public void onAction (String name, boolean keyPressed, float tpf) {
                
              if (name.equals("Add velocity")){  
                sphere1Controller.setLinearVelocity(randomPhy.getRandomVector3f(200, 200, 200));
                sphere2Controller.setLinearVelocity(randomPhy.getRandomVector3f(200, 200, 200));
                sphere3Controller.setLinearVelocity(randomPhy.getRandomVector3f(200, 200, 200));
              }
              if (name.equals("Slow down") && keyPressed) {
                physicsState.setSpeed(.05f); 
              }
              if (name.equals("Slow down") && !keyPressed) {
                  physicsState.setSpeed(5f);
              }
            } //end onAction
        }; //end mouseListener      
        
        //add the action listener to the inputManager
            inputManager.addListener(mouseListener, "Add velocity");
            inputManager.addListener(mouseListener, "Slow down");
        
    } //end initActionListeners()
    
    @Override
    public void simpleUpdate(float tpf) {
               
      //Updates the GUI with real time X,Y,Z coordinates for spheres.  
        String textString1 = guiStringBuilder(sphere1Controller.getPhysicsLocation());
        sphere1Text.setText("Sphere 1:\n" + textString1);
        
        String textString2 = guiStringBuilder(sphere2Controller.getPhysicsLocation());
        sphere2Text.setText("Sphere 2:\n" + textString2);
        
        String textString3 = guiStringBuilder(sphere3Controller.getPhysicsLocation());
        sphere3Text.setText("Sphere 3:\n" + textString3);
        
      //runs the writeFile() method every second by checking if 1000 milliseconds
      //have passed by.
//        long currentTimer = System.currentTimeMillis() - initialTime;
//        if (currentTimer >= 1000) {
//            writeFile();
//        }
                        
    } //end simpleUpdate
    
    private void writeFile () {     
        
        try {
            
            FileWriter file = new FileWriter(filePath,true);
            writer = new BufferedWriter(file);
            String textString1 = "Position: " + fileStringBuilder(sphere1Controller.getPhysicsLocation())
                 + " Velocity: " + fileStringBuilder(sphere1Controller.getLinearVelocity());
            writer.write("Sphere 1: ");
            writer.write("Time: " + "(" + System.currentTimeMillis() + ") ");
            writer.write(textString1);
            writer.newLine();
            
            String textString2 = "Position: " + fileStringBuilder(sphere2Controller.getPhysicsLocation())
                + " Velocity: " + fileStringBuilder(sphere2Controller.getLinearVelocity());
            writer.write("Sphere 2: ");
            writer.write("Time: " + "(" + System.currentTimeMillis() + ") ");
            writer.write(textString2);
            writer.newLine();
           
            String textString3 = "Position: " + fileStringBuilder(sphere3Controller.getPhysicsLocation())
                     + " Velocity: " + fileStringBuilder(sphere3Controller.getLinearVelocity());
            writer.write("Sphere 3: ");
            writer.write("Time: " + "(" + System.currentTimeMillis() + ") ");
            writer.write(textString3);
            writer.newLine();
            writer.newLine();
            writer.flush();
            initialTime = System.currentTimeMillis();
         }
        catch (IOException e) {
            System.out.println("IOException caught");
            System.out.println(e.getStackTrace());
         }
    } //end writeFile();
    
    
    /*
     * guiStringBuilder returns a string that displays X, Y and Z coordinates
     * given a Vector3f and uses \n to move to a new line (does not work with
     * BufferedWriter)
     */
    private String guiStringBuilder (Vector3f location) {
        
        float X = location.getX();
        float Y = location.getY();
        float Z = location.getZ();
        return "X: " + X + "\nY: " + Y + "\nZ: " + Z;
    }
    /*
     * fileStringBuilder is to be used with writeFile(). Creates and returns a
     * string describing x, y, z values for a specific Vector3f.s
     */
    private String fileStringBuilder (Vector3f location) {
        
        float X = location.getX();
        float Y = location.getY();
        float Z = location.getZ();
        return "(" + X + "," + Y + "," + Z + ")";
        
    }

    @Override
    public void simpleRender(RenderManager rm) {
        //TODO: add render code
    }    
}
