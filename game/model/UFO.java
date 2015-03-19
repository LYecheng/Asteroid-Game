package edu.uchicago.cs.java.finalproject.game.model;

import edu.uchicago.cs.java.finalproject.controller.Game;

import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * Created by LiYecheng on 12/01/14.
 */
public class UFO extends Sprite {
    private int uLife = 5;
    private int nSpin;

    //radius of a large asteroid
    private final int RAD = 100;
    //nSize determines if the Asteroid is Large (0), Medium (1), or Small (2)
    //when you explode a Large asteroid, you should spawn 2 or 3 medium asteroids
    //same for medium asteroid, you should spawn small asteroids
    //small asteroids get blasted into debris

    public UFO(){

        //call Sprite constructor
        super();

        setLife(5);

        ArrayList<Point> pntCs = new ArrayList<Point>();
        // top of UFO
        pntCs.add(new Point(0, 60));

        //left points
        pntCs.add(new Point(-55, 5));
        pntCs.add(new Point(-50, 0));
        pntCs.add(new Point(-20, 20));
        pntCs.add(new Point(-15, 10));
        pntCs.add(new Point(-5, 15));

        // bottom of UFO
        pntCs.add(new Point(0, 10));

        //right points
        pntCs.add(new Point(5, 15));
        pntCs.add(new Point(15, 10));
        pntCs.add(new Point(20, 20));
        pntCs.add(new Point(50, 0));
        pntCs.add(new Point(55, 5));

        assignPolarPoints(pntCs);
        setColor(new Color(Game.R.nextInt(256)));

        //with random orientation
        setOrientation(Game.R.nextInt(360));
        //setExpire(100);
        setRadius(RAD);

        //the spin will be either plus or minus 0-9
        int nSpin = Game.R.nextInt(10);
        if(nSpin % 2 ==0)
            nSpin = -nSpin;
        setSpin(nSpin);

        //random delta-x
        int nDX = Game.R.nextInt(10);
        if(nDX % 2 ==0)
            nDX = -nDX;
        setDeltaX(nDX);

        //random delta-y
        int nDY = Game.R.nextInt(10);
        if(nDY % 2 ==0)
            nDY = -nDY;
        setDeltaY(nDY);
    }

    public void expire() {
        if (getExpire() == 0)
            CommandCenter.movFloatersLife.remove(this);
        else
            setExpire(getExpire() - 1);
    }

    //overridden
    public void move(){

        if (Game.getTick() % 2 == 0) {
            super.move();
            setOrientation(getOrientation() + getSpin());
        }

    }

    public int getSpin() {
        return this.nSpin;
    }

    public void setSpin(int nSpin) {
        this.nSpin = nSpin;
    }

    public int getLife(){
        return uLife;
    }

    public void setLife(int uLife) {
         this.uLife = uLife;
    }

    @Override
    public void draw(Graphics g) {
        super.draw(g);

        if(uLife == 5)
            g.setColor(Color.GREEN);
        else if (uLife == 4)
            g.setColor(Color.BLUE);
        else if (uLife == 3)
            g.setColor(Color.ORANGE);
        else if (uLife == 2)
            g.setColor(Color.PINK);
        else if(uLife == 1)
            g.setColor(Color.RED);

        g.fillPolygon(getXcoords(), getYcoords(), dDegrees.length);
        g.setColor(Color.CYAN);
        g.drawPolygon(getXcoords(), getYcoords(), dDegrees.length);
    }
}
