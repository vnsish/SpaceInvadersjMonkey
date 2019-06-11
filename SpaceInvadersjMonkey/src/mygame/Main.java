package mygame;

import com.jme3.animation.AnimChannel;
import com.jme3.animation.AnimControl;
import com.jme3.animation.AnimEventListener;
import com.jme3.animation.LoopMode;
import com.jme3.app.SimpleApplication;
import com.jme3.audio.AudioData.DataType;
import com.jme3.audio.AudioNode;
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
import java.util.List;

/**
 * This is the Main Class of your Game. You should only do initialization here.
 * Move your Logic into AppStates or Controls
 *
 * @author normenhansen
 */
public class Main extends SimpleApplication
        implements AnimEventListener {

    private int starCount = 0, saucerDir = 1, points = 0, defeated, enemycount = 0;
    private Node starNode = new Node("starNode");
    private Node shootables = new Node("shootables");
    private ArrayList<Star> stars = new ArrayList<Star>();
    Random random = new Random();
    private Player player;
    private long gameTick = 1, nextSaucer = 50, tickSpeed = 8, nextShot = 30;
    private Instant start, end;
    private boolean saucerActive = false, enemyShooting = false, gameRunning = false;
    float[] movetable = new float[]{0.25f, 0.25f, 0.25f, 0.25f, 0.25f, 0.25f, 0.25f, 0.25f, 0};
    float[] movetable_y = new float[]{0, 0, 0, 0, 0, 0, 0, 0, -0.25f};
    int movetable_cur = 0, mov_dir = 1;
    AudioNode explosion_audio, shot_audio;
    BitmapText pointsText, gameoverText, hudText, victoryText;

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
        initAudio();

    }

    public void startGame() {
        
        
        
        guiNode.detachChildNamed("start");
        guiNode.detachChildNamed("gameover");
        guiNode.detachChildNamed("victory");
        
        pointsText.setText("Points: 0");

        rootNode.detachChildNamed("shootables");
        rootNode.detachChildNamed("UFO");
        rootNode.detachChildNamed("Ship");
        
        initPlayer();
        initShot();
        
        shootables = new Node("shootables");
        
        player.setShooting(false);
        rootNode.getChild("Shot").setLocalTranslation(0, -20, 0);
        enemyShooting = false;
        rootNode.getChild("EnemyShot").setLocalTranslation(0, 20, 0);

        
        gameTick = 1;
        nextSaucer = 100;
        nextShot = 30;
        tickSpeed = 14;
        defeated = 0;
        enemycount = 0;
        points = 0;
        movetable_cur = 0;
        mov_dir = 1;
        starCount = 0;
        saucerDir = 1;

        rootNode.attachChild(shootables);

        initEnemies();
        
        gameRunning = true;

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

            if (Duration.between(start, end).toMillis() > 50) {
                gameTick++;
                start = Instant.now();

                if (gameTick % tickSpeed == 0) {
                    if (movetable_cur == movetable.length) {
                        movetable_cur = 0;
                        mov_dir *= -1;
                    }

                    shootables.move((float) movetable[movetable_cur] * mov_dir, movetable_y[movetable_cur++], 0);
                }
                
                if  (gameTick % nextShot == 0 && !enemyShooting) enemyShoot();
            }

            if (nextSaucer - gameTick == 0 && !saucerActive) {
                saucerActive = true;
                saucerDir *= -1;
            }

            if (saucerActive && rootNode.getChild("UFO").getLocalTranslation().x <= 6 && rootNode.getChild("UFO").getLocalTranslation().x >= -6) {
                rootNode.getChild("UFO").move(saucerDir * tpf * 4, 0, 0);
                rootNode.getChild("UFO").rotate(0, saucerDir * tpf * 4, 0);

            } else if (rootNode.getChild("UFO").getLocalTranslation().x > 6 || rootNode.getChild("UFO").getLocalTranslation().x < -6) {
                rootNode.getChild("UFO").setLocalTranslation(saucerDir * 6, 3.5f, 0);
                saucerActive = false;
                nextSaucer = gameTick + 50;
            }
            
            for (Spatial p: shootables.getChildren())
            {
                if(checkCollision(player.getShip(), p))
                   {
                       explode(player.getShip().getLocalTranslation().x, player.getShip().getLocalTranslation().y);
                       rootNode.detachChildNamed("Ship");
                       guiNode.attachChild(gameoverText);
                       guiNode.attachChild(hudText);
                       gameRunning = false;
                   }
            }
            
            if(player.getShip().getLocalTranslation().x < (rootNode.getChild("EnemyShot").getLocalTranslation().x + 0.25f)
                && player.getShip().getLocalTranslation().x > (rootNode.getChild("EnemyShot").getLocalTranslation().x - 0.25f)
                && player.getShip().getLocalTranslation().y < (rootNode.getChild("EnemyShot").getLocalTranslation().y + 0.25f)
                && player.getShip().getLocalTranslation().y > (rootNode.getChild("EnemyShot").getLocalTranslation().y - 0.25f))
            {
                explode(player.getShip().getLocalTranslation().x, player.getShip().getLocalTranslation().y);
                rootNode.detachChildNamed("Ship");
                guiNode.attachChild(gameoverText);
                guiNode.attachChild(hudText);
                gameRunning = false;
            }
            
            if(shootables.getLocalTranslation().y < -5.5f)
            {
                       guiNode.attachChild(gameoverText);
                       guiNode.attachChild(hudText);
                       gameRunning = false;
            }
            
            if(shootables.getChildren().size() == 0)
            {
                guiNode.attachChild(victoryText);
                guiNode.attachChild(hudText);
                gameRunning = false;
            }
            
            if(enemyShooting)
            {
                rootNode.getChild("EnemyShot").move(0, -tpf*5, 0);
                
                if(rootNode.getChild("EnemyShot").getLocalTranslation().y < -6) 
                {
                    rootNode.getChild("EnemyShot").setLocalTranslation(0, 20, 0);
                    nextShot = gameTick + (tickSpeed*5);
                    enemyShooting = false;
                }
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
                    explode(rootNode.getChild("UFO").getLocalTranslation().x, 
                            rootNode.getChild("UFO").getLocalTranslation().y);
                    rootNode.getChild("UFO").setLocalTranslation(saucerDir * 6, 3.5f, 0);
                    saucerActive = false;
                    nextSaucer = gameTick + 100;
                    
                    points += 50;
                    pointsText.setText(String.format("Points: %d", points));
                }
                
                for(Spatial p : shootables.getChildren())
                {
                   if(checkCollision(rootNode.getChild("Shot"), p))
                   {
                       explode(p.getLocalTranslation().x + shootables.getLocalTranslation().x, 
                               p.getLocalTranslation().y + shootables.getLocalTranslation().y);
                       shootables.detachChild(p);              
                       player.setShooting(false);
                       rootNode.getChild("Shot").setLocalTranslation(0, -20, 0);
                       defeated++;
                       if(defeated % 2 == 0) tickSpeed--;
                       points += 10;
                       pointsText.setText(String.format("Points: %d", points));
                   }
                }
            }
        }

    }
    
    public boolean checkCollision(Spatial a, Spatial b)
    {
        return a.getLocalTranslation().x < (b.getLocalTranslation().x + 0.25f  + shootables.getLocalTranslation().x)
                && a.getLocalTranslation().x > (b.getLocalTranslation().x - 0.25f  + shootables.getLocalTranslation().x)
                && a.getLocalTranslation().y < (b.getLocalTranslation().y + 0.25f  + shootables.getLocalTranslation().y)
                && a.getLocalTranslation().y > (b.getLocalTranslation().y - 0.25f  + shootables.getLocalTranslation().y);
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

            if (name.equals("Shoot") && keyPressed && gameRunning) {
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

            if (name.equals("Right")&& gameRunning) {
                if (rootNode.getChild("Ship").getLocalTranslation().x < 5) {
                    rootNode.getChild("Ship").move(tpf * 3, 0, 0);
                }

            }

            if (name.equals("Left") && gameRunning) {
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
        shot_audio.playInstance();
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
    
    public void enemyShoot() {
        
        List<Spatial> allEnemies = shootables.getChildren();
        int shooting = random.nextInt(allEnemies.size());
        print(allEnemies.size());
        print(shooting);
        rootNode.getChild("EnemyShot").setLocalTranslation(allEnemies.get(shooting).getLocalTranslation().x + shootables.getLocalTranslation().x,
        allEnemies.get(shooting).getLocalTranslation().y + shootables.getLocalTranslation().y,
        allEnemies.get(shooting).getLocalTranslation().z + shootables.getLocalTranslation().z);
        enemyShooting = true;
        
    }
    
    public void initGui()
    {
        hudText = new BitmapText(guiFont, false);
        hudText.setSize(guiFont.getCharSet().getRenderedSize()*2);      // font size
        hudText.setName("start");
        hudText.setColor(ColorRGBA.White);                             // font color
        hudText.setText("Press F2 to start");             // the text
        hudText.setLocalTranslation(380, 500, 0); // position
        guiNode.attachChild(hudText);
        
        pointsText = new BitmapText(guiFont, false);
        pointsText.setSize(guiFont.getCharSet().getRenderedSize()*2);
        pointsText.setName("points");
        pointsText.setColor(ColorRGBA.White);                             // font color
        pointsText.setText("Points: 0");
        pointsText.setLocalTranslation(850, 50, 0);
        guiNode.attachChild(pointsText);
        
        gameoverText = new BitmapText(guiFont, false);
        gameoverText.setSize(guiFont.getCharSet().getRenderedSize()*2);
        gameoverText.setName("gameover");
        gameoverText.setColor(ColorRGBA.White);                             // font color
        gameoverText.setText("GAME OVER");
        gameoverText.setLocalTranslation(400, 550, 0);
        
        victoryText = new BitmapText(guiFont, false);
        victoryText.setSize(guiFont.getCharSet().getRenderedSize()*2);
        victoryText.setName("victory");
        victoryText.setColor(ColorRGBA.White);                             // font color
        victoryText.setText("YOU WIN");
        victoryText.setLocalTranslation(420, 550, 0);
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

        a.setName(String.format("Alien%d", enemycount++));
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
        
        Geometry enemyShot = new Geometry("EnemyShot", boxMesh);
        Material enemyShotMat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        enemyShotMat.setColor("Color", ColorRGBA.Yellow);
        enemyShot.setMaterial(enemyShotMat);
        enemyShot.move(0, 20, 0);
        rootNode.attachChild(enemyShot);
    }

    public void initAudio() {
        /* gun shot sound is to be triggered by a mouse click. */
        shot_audio = new AudioNode(assetManager, "Sounds/shot.wav", DataType.Buffer);
        shot_audio.setPositional(false);
        shot_audio.setLooping(false);
        shot_audio.setVolume(2);
        rootNode.attachChild(shot_audio);
        
        explosion_audio = new AudioNode(assetManager, "Sounds/explosion.wav", DataType.Buffer);
        explosion_audio.setPositional(false);
        explosion_audio.setLooping(false);
        explosion_audio.setVolume(2);
        rootNode.attachChild(shot_audio);
        
        AudioNode bgm = new AudioNode(assetManager, "Sounds/bgm.ogg", DataType.Buffer);
        bgm.setPositional(false);
        bgm.setLooping(false);
        bgm.setVolume(1);
        rootNode.attachChild(bgm);
        bgm.play();
    }
    
    public void explode(float x, float y) {
        ParticleEmitter debrisEffect = new ParticleEmitter("Debris", ParticleMesh.Type.Triangle, 10);
        debrisEffect.setLocalTranslation(x, y, 0);
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
        debrisEffect.setEndSize(0.5f);
        debrisEffect.getParticleInfluencer().setVelocityVariation(.60f);
        rootNode.attachChild(debrisEffect);
        explosion_audio.playInstance();
        debrisEffect.emitAllParticles();
    }
}
