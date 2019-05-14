/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mygame;

import com.jme3.asset.AssetManager;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.scene.Geometry;
import com.jme3.scene.shape.Sphere;

/**
 *
 * @author vinicius
 */
public class Star {
    
    private int id;
    private float x;
    private float speed;
    private Geometry geom;
    private AssetManager assetManager; 
    

    public Star(int id, float x, float speed, AssetManager assetManager) {
        this.id = id;
        this.x = x;
        this.speed = speed;
        this.assetManager = assetManager;
        
        Sphere sphere = new Sphere(5, 5, 1);
        Material mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        mat.setColor("Color", ColorRGBA.White);
        geom = new Geometry("Star", sphere);
        geom.setMaterial(mat);
    }

    
    public float getX() {
        return x;
    }

    public void setX(float x) {
        this.x = x;
    }

    public float getSpeed() {
        return speed;
    }

    public void setSpeed(float speed) {
        this.speed = speed;
    }

    public Geometry getGeom() {
        return geom;
    }

    public void setGeom(Geometry geom) {
        this.geom = geom;
    }
    
    
    
}
