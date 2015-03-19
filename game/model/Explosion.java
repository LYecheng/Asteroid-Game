package edu.uchicago.cs.java.finalproject.game.model;

import edu.uchicago.cs.java.finalproject.controller.Game;

import java.awt.*;

/**
 * Created by LiYecheng on 11/23/14.
 */
public class Explosion extends MovableAdapter {

    private Point pntCenter;
    private int nExpiry;
    private int nRadius;

    public Explosion(Asteroid asteroid) {
        pntCenter = asteroid.getCenter();
        nExpiry = 20;
        nRadius = 200;
    }

    public Explosion(UFO ufo) {
        pntCenter = ufo.getCenter();
        nExpiry = 20;
        nRadius = 200;
    }

    @Override
    public void expire() {
        super.expire();
        nExpiry--;
        nRadius -= 10;
        if (nExpiry < 1){
            CommandCenter.getMovDebris().remove(this);
        }

    }

    @Override
    public void draw(Graphics g) {

        super.draw(g);
       for (int i = 25; i > 0; i--) {
            g.setColor(new Color(Game.R.nextInt(256),Game.R.nextInt(256),Game.R.nextInt(256)));
            g.fillOval(pntCenter.x - Game.R.nextInt(nRadius) / 3, pntCenter.y - Game.R.nextInt(nRadius) / 3 , nRadius/5, nRadius/5);
       }
    }
}
