/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mygame;

import com.jme3.asset.AssetManager;
import com.jme3.scene.Spatial;

/**
 *
 * @author vinicius
 */
public class Player {
    
    private Spatial ship;
    private boolean shooting = false;

    public boolean isShooting() {
        return shooting;
    }

    public void setShooting(boolean shooting) {
        this.shooting = shooting;
    }

    public Spatial getShip() {
        return ship;
    }

    public void setShip(Spatial ship) {
        this.ship = ship;
    }
    
    public Player(AssetManager assetManager){
        ship = assetManager.loadModel("Models/ship.j3o");
        ship.scale(0.5f);
        ship.move(0, -3, 0);
        ship.rotate(90, 0, 0);
        
    }
    
}
