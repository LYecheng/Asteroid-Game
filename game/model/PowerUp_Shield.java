package edu.uchicago.cs.java.finalproject.game.model;

import edu.uchicago.cs.java.finalproject.controller.Game;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by LiYecheng on 11/29/14.
 */
public class PowerUp_Shield extends Sprite {
    private int nSpin;

    public PowerUp_Shield() {

        super();

        ArrayList<Point> pntCs = new ArrayList<Point>();
        // top of ship
        pntCs.add(new Point(5, 5));
        pntCs.add(new Point(4,0));
        pntCs.add(new Point(5, -5));
        pntCs.add(new Point(0,-4));
        pntCs.add(new Point(-5, -5));
        pntCs.add(new Point(-4,0));
        pntCs.add(new Point(-5, 5));
        pntCs.add(new Point(0,4));

        assignPolarPoints(pntCs);

        setExpire(500);
        setRadius(50);
        setColor(Color.YELLOW);

        int nX = Game.R.nextInt(10);
        int nY = Game.R.nextInt(10);
        int nS = Game.R.nextInt(5);

        //set random DeltaX
        if (nX % 2 == 0)
            setDeltaX(nX);
        else
            setDeltaX(-nX);

        //set rnadom DeltaY
        if (nY % 2 == 0)
            setDeltaX(nY);
        else
            setDeltaX(-nY);

        //set random spin
        if (nS % 2 == 0)
            setSpin(nS);
        else
            setSpin(-nS);

        //random point on the screen
        setCenter(new Point(Game.R.nextInt(Game.DIM.width), Game.R.nextInt(Game.DIM.height)));

        //random orientation
        setOrientation(Game.R.nextInt(360));

    }

    public void move() {
        super.move();
        setOrientation(getOrientation() + getSpin());
    }

    public int getSpin() {
        return this.nSpin;
    }

    public void setSpin(int nSpin) {
        this.nSpin = nSpin;
    }

    //override the expire method - once an object expires, then remove it from the arrayList.
    @Override
    public void expire() {
        if (getExpire() == 0)
            CommandCenter.movFloatersShield.remove(this);
        else
            setExpire(getExpire() - 1);
    }

    @Override
    public void draw(Graphics g) {
        super.draw(g);
        //fill this polygon (with whatever color it has)
        g.fillPolygon(getXcoords(), getYcoords(), dDegrees.length);
        //now draw a white border
        g.setColor(Color.CYAN);
        g.drawPolygon(getXcoords(), getYcoords(), dDegrees.length);
    }
}
