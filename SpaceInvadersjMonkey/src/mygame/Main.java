package mygame;

import com.jme3.app.SimpleApplication;
import com.jme3.light.DirectionalLight;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.renderer.RenderManager;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.shape.Box;
import com.jme3.scene.shape.Sphere;
import java.util.ArrayList;
import java.util.Random;


/**
 * This is the Main Class of your Game. You should only do initialization here.
 * Move your Logic into AppStates or Controls
 * @author normenhansen
 */
public class Main extends SimpleApplication {
    
    private int starCount = 0;
    private Node starNode = new Node("starNode");
    private ArrayList<Star> stars = new ArrayList<Star>();
    Random random = new Random();

    public static void main(String[] args) {
        Main app = new Main();
        app.showSettings = false;
        app.start();
    }

    
    @Override
    public void simpleInitApp() {
        /**
         * A white, directional light source
         */
        DirectionalLight sun = new DirectionalLight();
        sun.setDirection((new Vector3f(-0.5f, -0.5f, -0.5f)).normalizeLocal());
        sun.setColor(ColorRGBA.White);
        rootNode.addLight(sun);
        
        rootNode.attachChild(starNode);
        
        for (int i = 0; i < 75; i++) addStar(-200 + random.nextInt(400));
        
    }

    @Override
    public void simpleUpdate(float tpf) {
        for (Star s : stars)
        {
            s.getGeom().move(0, -(s.getSpeed()*tpf*50), 0);
            if (s.getGeom().getLocalTranslation().getY() < -150) s.getGeom().setLocalTranslation(s.getGeom().getLocalTranslation().getX(), 150, s.getGeom().getLocalTranslation().getZ());
        }
    }
    
    public void addStar(float x)
    {
        Star star = new Star(starCount++, x, 1f+random.nextFloat()*2, assetManager);
        starNode.attachChild(star.getGeom());
        stars.add(star);
        star.getGeom().setLocalTranslation(x, 100, (-100 - random.nextInt(300)));
        
    }

    @Override
    public void simpleRender(RenderManager rm) {
        //TODO: add render code
    }
}
