package mygame;

import com.jme3.app.SimpleApplication;
import com.jme3.bounding.BoundingVolume;
import com.jme3.collision.CollisionResults;
import com.jme3.effect.ParticleEmitter;
import com.jme3.effect.ParticleMesh;
import com.jme3.input.KeyInput;
import com.jme3.input.MouseInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.AnalogListener;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.input.controls.MouseButtonTrigger;
import com.jme3.light.DirectionalLight;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.renderer.RenderManager;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.shape.Box;
import com.jme3.scene.shape.Sphere;
import com.jme3.system.AppSettings;
import com.jme3.texture.Texture;
import java.util.ArrayList;
import java.util.Random;
import java.time.Instant;
import java.time.Duration;



/**
 * This is the Main Class of your Game. You should only do initialization here.
 * Move your Logic into AppStates or Controls
 * @author normenhansen
 */
public class Main extends SimpleApplication {
    
    private int starCount = 0, saucerDir = 1;
    private Node starNode = new Node("starNode");
    private ArrayList<Star> stars = new ArrayList<Star>();
    Random random = new Random();
    private Player player;
    private long gameTick = 1, nextSaucer = 50;
    private Instant start, end;
    private boolean saucerActive = false;

    
    
    public static void main(String[] args) {
        Main app = new Main();
        app.showSettings = false;
        AppSettings settings = new AppSettings(true);
        settings.setResolution(1024,768);
        app.setSettings(settings);
        app.start();
    }

    
    @Override
    public void simpleInitApp() {
        start = Instant.now();
        
        flyCam.setEnabled(false);
        
        DirectionalLight sun = new DirectionalLight();
        sun.setDirection((new Vector3f(-0.05f, -0.05f, -0.5f)).normalizeLocal());
        sun.setColor(ColorRGBA.White);
        rootNode.addLight(sun);
        
        rootNode.attachChild(starNode);
        
        for (int i = 0; i < 75; i++) addStar(-200 + random.nextInt(400));
        
        initPlayer();
        initEnemies();
        initKeys();
        initShot();
        
        
    }

    public void print(Object o)
    {
        System.out.println(o);
    }
    
    @Override
    public void simpleUpdate(float tpf) {
        
        end = Instant.now();
        
        if(Duration.between(start, end).toMillis() > 100)
        {
            gameTick++;
            start = Instant.now();
            print(gameTick);
        }
        if(nextSaucer - gameTick == 0 && !saucerActive)
        {
            print("go");
            saucerActive = true;
            saucerDir *= -1;
        }
        
        
        if(saucerActive && rootNode.getChild("UFO").getLocalTranslation().x <= 6 && rootNode.getChild("UFO").getLocalTranslation().x >= -6)
        {
            System.out.println(rootNode.getChild("UFO").getLocalTranslation().x);
            rootNode.getChild("UFO").move(saucerDir*tpf*4, 0, 0);
            rootNode.getChild("UFO").rotate(0, saucerDir*tpf*4, 0);
        
        }
        else if(rootNode.getChild("UFO").getLocalTranslation().x > 6 || rootNode.getChild("UFO").getLocalTranslation().x < -6)
        {
            rootNode.getChild("UFO").setLocalTranslation(saucerDir*6, 3.5f, 0);
            saucerActive = false;
            nextSaucer = gameTick+50;
        }
        
        
        for (Star s : stars)
        {
            s.getGeom().move(0, -(s.getSpeed()*tpf*50), 0);
            if (s.getGeom().getLocalTranslation().getY() < -150) s.getGeom().setLocalTranslation(-200 + random.nextInt(400), 150, -100 - random.nextInt(300));
        }
        //rootNode.getChild("UFO").rotate(0, tpf, 0);
        
        if(player.isShooting())
        {
            rootNode.getChild("Shot").move(0, tpf*10, 0);
            if(rootNode.getChild("Shot").getLocalTranslation().y > 6) 
            {
                rootNode.getChild("Shot").setLocalTranslation(0, -20, 0);
                player.setShooting(false);
            }
            
            CollisionResults results = new CollisionResults();
            BoundingVolume bv = rootNode.getChild("UFO").getWorldBound();
            rootNode.getChild("Shot").collideWith(bv, results);
            if (results.size() > 0)
            {
                explode(rootNode.getChild("UFO").getLocalTranslation());
                rootNode.getChild("UFO").setLocalTranslation(saucerDir * 6, 3.5f, 0);
                saucerActive = false;
                nextSaucer = gameTick + 50;

            }
            
            
        }
        
    }
   
    @Override
    public void simpleRender(RenderManager rm) {
        //TODO: add render code
    } 
    
    private ActionListener actionListener = new ActionListener() {
        
        public void onAction(String name, boolean keyPressed, float tpf) {
            
            if(name.equals("Shoot") && keyPressed)
            {
                if(!player.isShooting()) playerShoot();
            }
            
        }
        
    };
    
    private AnalogListener analogListener = new AnalogListener() {
        public void onAnalog(String name, float keyPressed, float tpf) {
            
            if(name.equals("Right"))
            {
                if(rootNode.getChild("Ship").getLocalTranslation().x < 5)
                    rootNode.getChild("Ship").move(tpf*2, 0, 0);
               
            }
            
            if(name.equals("Left"))
            {
                if(rootNode.getChild("Ship").getLocalTranslation().x > -5)
                    rootNode.getChild("Ship").move(-tpf*2, 0, 0);
               
            }
           
            
        }
    };
    
    private void initKeys() {
        inputManager.addMapping("Left",
                new KeyTrigger(KeyInput.KEY_LEFT));
 
        inputManager.addMapping("Right",
                new KeyTrigger(KeyInput.KEY_RIGHT));
        
        inputManager.addListener(analogListener, "Left");
        inputManager.addListener(analogListener, "Right");
        
        inputManager.addMapping("Shoot", new KeyTrigger(KeyInput.KEY_SPACE));
        inputManager.addListener(actionListener, "Shoot");
    }
    
    public void addStar(float x)
    {
        Star star = new Star(starCount++, x, 1f+random.nextFloat()*2, assetManager);
        starNode.attachChild(star.getGeom());
        stars.add(star);
        star.getGeom().setLocalTranslation(x, 100 + random.nextInt(100), (-100 - random.nextInt(300)));
        
    }
    
    public void playerShoot()
    {
        player.setShooting(true);
        rootNode.getChild("Shot").setLocalTranslation(rootNode.getChild("Ship").getLocalTranslation().x, -2.5f, 0);
        
    }
    
    public void spawnSaucer()
    {
        
    }
    
    public void initPlayer()
    {
        player = new Player(assetManager);
        player.getShip().setName("Ship");
        rootNode.attachChild(player.getShip());
    }
    
    public void initEnemies()
    {
        Spatial ufo;
        ufo = assetManager.loadModel("Models/ufo.j3o");
        ufo.setName("UFO");
        
        Material ufoMat = new Material(assetManager,
                "Common/MatDefs/Misc/Unshaded.j3md");
        Texture ufoTex = assetManager.loadTexture(
                "Textures/UFO_col.tga");
        ufoMat.setTexture("ColorMap", ufoTex);
        ufo.scale(10f);
        ufo.setMaterial(ufoMat);
        ufo.move(6, 3.5f, 0);
        rootNode.attachChild(ufo);
    }
    
    public void initShot()
    {
        /* A colored lit cube. Needs light source! */
        Box boxMesh = new Box(0.05f, .15f, 0.05f);
        Geometry boxGeo = new Geometry("Shot", boxMesh);
        Material boxMat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        boxMat.setColor("Color", ColorRGBA.White);
        boxGeo.setMaterial(boxMat);
        boxGeo.move(0, -20, 0);
        rootNode.attachChild(boxGeo);
    }
    
    public void explode(Vector3f position)
    {
        ParticleEmitter debrisEffect = new ParticleEmitter("Debris", ParticleMesh.Type.Triangle, 10);
        debrisEffect.setLocalTranslation(position);
        Material debrisMat = new Material(assetManager, "Common/MatDefs/Misc/Particle.j3md");
        debrisMat.setTexture("Texture", assetManager.loadTexture("Textures/Debris.png"));
        debrisEffect.setMaterial(debrisMat);
        debrisEffect.setImagesX(3);
        debrisEffect.setImagesY(3); // 3x3 texture animation
        debrisEffect.setRotateSpeed(4);
        debrisEffect.setSelectRandomImage(true);
        debrisEffect.getParticleInfluencer().setInitialVelocity(new Vector3f(0, 4, 0));
        debrisEffect.setParticlesPerSec(0);
        debrisEffect.setStartColor(new ColorRGBA(1f, 1f, 1f, 1f));
        debrisEffect.setGravity(0f, 2f, 0f);
        debrisEffect.setHighLife(0.25f);
        debrisEffect.getParticleInfluencer().setVelocityVariation(.60f);
        rootNode.attachChild(debrisEffect);
        debrisEffect.emitAllParticles();
    }
}