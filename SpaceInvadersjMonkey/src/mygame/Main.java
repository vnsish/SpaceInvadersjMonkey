package mygame;

import com.jme3.animation.AnimChannel;
import com.jme3.animation.AnimControl;
import com.jme3.animation.AnimEventListener;
import com.jme3.animation.LoopMode;
import com.jme3.app.SimpleApplication;
import com.jme3.bounding.BoundingVolume;
import com.jme3.collision.CollisionResults;
import com.jme3.effect.ParticleEmitter;
import com.jme3.effect.ParticleMesh;
import com.jme3.font.BitmapText;
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
 *
 * @author normenhansen
 */
public class Main extends SimpleApplication
        implements AnimEventListener {

    private int starCount = 0, saucerDir = 1;
    private Node starNode = new Node("starNode");
    private Node shootables = new Node("shootables");
    private ArrayList<Star> stars = new ArrayList<Star>();
    Random random = new Random();
    private Player player;
    private long gameTick = 1, nextSaucer = 50, tickSpeed = 5;
    private Instant start, end;
    private boolean saucerActive = false, gameRunning = false;
    float[] movetable = new float[]{0.25f, 0.25f, 0.25f, 0.25f, 0.25f, 0.25f, 0.25f, 0.25f, 0};
    float[] movetable_y = new float[]{0, 0, 0, 0, 0, 0, 0, 0, -0.25f};
    int movetable_cur = 0, mov_dir = 1;

    public static void main(String[] args) {
        Main app = new Main();
        app.showSettings = false;
        AppSettings settings = new AppSettings(true);
        settings.setResolution(1024, 768);
        app.setSettings(settings);
        app.setDisplayStatView(false);
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
        for (int i = 0; i < 75; i++) {
            addStar(-200 + random.nextInt(400));
        }

        initKeys();
        initGui();

    }

    public void startGame() {
        gameRunning = true;
        
        guiNode.detachChildNamed("start");

        rootNode.detachChildNamed("shootables");
        rootNode.detachChildNamed("UFO");
        rootNode.detachChildNamed("Ship");

        shootables = new Node("shootables");

        gameTick = 1;
        nextSaucer = 50;
        tickSpeed = 5;
        movetable_cur = 0;
        mov_dir = 1;
        starCount = 0;
        saucerDir = 1;

        rootNode.attachChild(shootables);

        initPlayer();
        initEnemies();
        initShot();

    }

    public void print(Object o) {
        System.out.println(o);
    }

    @Override
    public void simpleUpdate(float tpf) {

        for (Star s : stars) {
            s.getGeom().move(0, -(s.getSpeed() * tpf * 50), 0);
            if (s.getGeom().getLocalTranslation().getY() < -150) {
                s.getGeom().setLocalTranslation(-200 + random.nextInt(400), 150, -100 - random.nextInt(300));
            }
        }

        if (gameRunning) {
            end = Instant.now();

            if (Duration.between(start, end).toMillis() > 100) {
                gameTick++;
                start = Instant.now();
                //print(gameTick);

                if (gameTick % tickSpeed == 0) {
                    if (movetable_cur == movetable.length) {
                        movetable_cur = 0;
                        mov_dir *= -1;
                    }

                    print(movetable[movetable_cur]);

                    shootables.move((float) movetable[movetable_cur] * mov_dir, movetable_y[movetable_cur++], 0);
                }
            }

            if (nextSaucer - gameTick == 0 && !saucerActive) {
                saucerActive = true;
                saucerDir *= -1;
            }

            if (saucerActive && rootNode.getChild("UFO").getLocalTranslation().x <= 6 && rootNode.getChild("UFO").getLocalTranslation().x >= -6) {
                //System.out.println(rootNode.getChild("UFO").getLocalTranslation().x);
                rootNode.getChild("UFO").move(saucerDir * tpf * 4, 0, 0);
                rootNode.getChild("UFO").rotate(0, saucerDir * tpf * 4, 0);

            } else if (rootNode.getChild("UFO").getLocalTranslation().x > 6 || rootNode.getChild("UFO").getLocalTranslation().x < -6) {
                rootNode.getChild("UFO").setLocalTranslation(saucerDir * 6, 3.5f, 0);
                saucerActive = false;
                nextSaucer = gameTick + 50;
            }

            if (player.isShooting()) {
                rootNode.getChild("Shot").move(0, tpf * 10, 0);
                if (rootNode.getChild("Shot").getLocalTranslation().y > 6) {
                    rootNode.getChild("Shot").setLocalTranslation(0, -20, 0);
                    player.setShooting(false);
                }

                CollisionResults results = new CollisionResults();
                BoundingVolume bv = rootNode.getChild("UFO").getWorldBound();
                rootNode.getChild("Shot").collideWith(bv, results);
                if (results.size() > 0) {
                    explode(rootNode.getChild("UFO").getLocalTranslation());
                    rootNode.getChild("UFO").setLocalTranslation(saucerDir * 6, 3.5f, 0);
                    saucerActive = false;
                    nextSaucer = gameTick + 50;

                }

            }
        }

    }

    public void onAnimCycleDone(AnimControl control, AnimChannel channel, String animName) {
        // unused
    }

    public void onAnimChange(AnimControl control, AnimChannel channel, String animName) {
        // unused
    }

    @Override
    public void simpleRender(RenderManager rm) {
        //TODO: add render code
    }

    private ActionListener actionListener = new ActionListener() {

        public void onAction(String name, boolean keyPressed, float tpf) {

            if (name.equals("Shoot") && keyPressed) {
                if (!player.isShooting()) {
                    playerShoot();
                }
            }
            if (name.equals("Restart") && keyPressed) {
                startGame();
                System.out.println("started");
            }

        }

    };

    private AnalogListener analogListener = new AnalogListener() {
        public void onAnalog(String name, float keyPressed, float tpf) {

            if (name.equals("Right")) {
                if (rootNode.getChild("Ship").getLocalTranslation().x < 5) {
                    rootNode.getChild("Ship").move(tpf * 3, 0, 0);
                }

            }

            if (name.equals("Left")) {
                if (rootNode.getChild("Ship").getLocalTranslation().x > -5) {
                    rootNode.getChild("Ship").move(-tpf * 3, 0, 0);
                }

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

        inputManager.addMapping("Restart", new KeyTrigger(KeyInput.KEY_F2));
        inputManager.addListener(actionListener, "Restart");
    }

    public void addStar(float x) {
        Star star = new Star(starCount++, x, 1f + random.nextFloat() * 2, assetManager);
        starNode.attachChild(star.getGeom());
        stars.add(star);
        star.getGeom().setLocalTranslation(x, 100 + random.nextInt(100), (-100 - random.nextInt(300)));

    }

    public void playerShoot() {
        player.setShooting(true);
        rootNode.getChild("Shot").setLocalTranslation(rootNode.getChild("Ship").getLocalTranslation().x, -2.5f, 0);

    }

    public void spawnSaucer() {
        Spatial ufo;
        ufo = assetManager.loadModel("Models/ufo.j3o");
        ufo.setName("UFO");

        Material ufoMat = new Material(assetManager,
                "Common/MatDefs/Misc/Unshaded.j3md");
        Texture ufoTex = assetManager.loadTexture(
                "Textures/ufo.tga");
        ufoMat.setTexture("ColorMap", ufoTex);
        ufo.scale(10f);
        ufo.setMaterial(ufoMat);
        ufo.move(6, 3.5f, 0);
        rootNode.attachChild(ufo);
    }

    public void initPlayer() {
        player = new Player(assetManager);
        player.getShip().setName("Ship");
        rootNode.attachChild(player.getShip());
    }

    public void initEnemies() {
        spawnSaucer();
        for (int x = -5; x <= 3; x += 1) {
            spawnAlien(1, x, 2.5f);
            spawnAlien(2, x, 1.5f);
            spawnAlien(3, x, 0.5f);

        }
    }
    
    public void initGui()
    {
        BitmapText hudText = new BitmapText(guiFont, false);
        hudText.setSize(guiFont.getCharSet().getRenderedSize()*2);      // font size
        hudText.setName("start");
        hudText.setColor(ColorRGBA.White);                             // font color
        hudText.setText("Press F2 to start");             // the text
        hudText.setLocalTranslation(370, 500, 0); // position
        guiNode.attachChild(hudText);
    }

    public void spawnAlien(int type, float x, float y) {
        Spatial a;
        Texture alienTex;

        switch (type) {

            case 1:
                a = assetManager.loadModel("Models/alien1.j3o");
                alienTex = assetManager.loadTexture("Textures/alien1.tga");
                break;
            case 2:
                a = assetManager.loadModel("Models/alien2.j3o");
                alienTex = assetManager.loadTexture("Textures/alien2.tga");
                break;
            case 3:
                a = assetManager.loadModel("Models/alien4.j3o");
                alienTex = assetManager.loadTexture("Textures/alien4.tga");
                break;
            default:
                a = assetManager.loadModel("Models/alien1.j3o");
                alienTex = assetManager.loadTexture("Textures/alien1.tga");
        }

        a.setName("Alien");
        Material alienMat = new Material(assetManager,
                "Common/MatDefs/Misc/Unshaded.j3md");
        alienMat.setTexture("ColorMap", alienTex);
        a.setMaterial(alienMat);
        a.scale(10f);
        a.setLocalTranslation(x, y, 0);
        shootables.attachChild(a);
    }

    public void initShot() {
        /* A colored lit cube. Needs light source! */
        Box boxMesh = new Box(0.05f, .15f, 0.05f);
        Geometry boxGeo = new Geometry("Shot", boxMesh);
        Material boxMat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        boxMat.setColor("Color", ColorRGBA.White);
        boxGeo.setMaterial(boxMat);
        boxGeo.move(0, -20, 0);
        rootNode.attachChild(boxGeo);
    }

    public void explode(Vector3f position) {
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
