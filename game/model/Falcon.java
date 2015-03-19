package edu.uchicago.cs.java.finalproject.game.model;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import edu.uchicago.cs.java.finalproject.controller.Game;


public class Falcon extends Sprite {

	// ==============================================================
	// FIELDS 
	// ==============================================================
	
	private final int MOVE = 20;
    private BufferedImage ship;

	final int DEGREE_STEP = 7;

	private boolean bShield = false;
	private boolean bFlame = false;
	private boolean bProtected; //for fade in and out

	private boolean bTurningRight = false;
	private boolean bTurningLeft = false;

	private boolean bLeft = false;
	private boolean bRight = false;
	private boolean bUp = false;
	private boolean bDown = false;

	private int nShield;
			
	private final double[] FLAME = { 23 * Math.PI / 24 + Math.PI / 2, Math.PI + Math.PI / 2, 25 * Math.PI / 24 + Math.PI / 2 };

	private int[] nXFlames = new int[FLAME.length];
	private int[] nYFlames = new int[FLAME.length];

	private Point[] pntFlames = new Point[FLAME.length];


	// ==============================================================
	// CONSTRUCTOR 
	// ==============================================================
	
	public Falcon() {
		super();

		ArrayList<Point> pntCs = new ArrayList<Point>();
		
		// top of ship
		pntCs.add(new Point(0, 18)); 
		
		//right points
		pntCs.add(new Point(3, 3)); 
		pntCs.add(new Point(12, 0)); 
		pntCs.add(new Point(13, -2)); 
		pntCs.add(new Point(13, -4)); 
		pntCs.add(new Point(11, -2)); 
		pntCs.add(new Point(4, -3)); 
		pntCs.add(new Point(2, -10)); 
		pntCs.add(new Point(4, -12)); 
		pntCs.add(new Point(2, -13)); 

		//left points
		pntCs.add(new Point(-2, -13)); 
		pntCs.add(new Point(-4, -12));
		pntCs.add(new Point(-2, -10)); 
		pntCs.add(new Point(-4, -3)); 
		pntCs.add(new Point(-11, -2));
		pntCs.add(new Point(-13, -4));
		pntCs.add(new Point(-13, -2)); 
		pntCs.add(new Point(-12, 0)); 
		pntCs.add(new Point(-3, 3)); 
		

		assignPolarPoints(pntCs);

		setColor(Color.white);
		
		//put falcon in the middle.
		setCenter(new Point(Game.DIM.width / 2, Game.DIM.height / 2));
		
		//with random orientation
		setOrientation(Game.R.nextInt(360));
		
		//this is the size of the falcon
		setRadius(60);

		//these are falcon specific
		setProtected(true);
		setFadeValue(0);
	}

    public boolean isDown() {
        return bDown;
    }

    public void setDown(boolean bDown) {
        this.bDown = bDown;
    }

    public boolean isUp() {
        return bUp;
    }

    public void setUp(boolean bUp) {
        this.bUp = bUp;
    }

    public boolean isRight() {
        return bRight;
    }

    public void setRight(boolean bRight) {
        this.bRight = bRight;
    }

    public boolean isLeft() {
        return bLeft;
    }

    public void setLeft(boolean bLeft) {
        this.bLeft = bLeft;
    }

    // ==============================================================
	// METHODS 
	// ==============================================================

	public void move() {
		//super.move();
        Point pnt = getCenter();
        double dX = pnt.x + getDeltaX();
        double dY = pnt.y + getDeltaY();

        //this just keeps the sprite inside the bounds of the frame
        if (pnt.x > getDim().width) {
            setCenter(new Point(getDim().width - 40, pnt.y));
        }
        else if (pnt.x < 0) {
            setCenter(new Point(40, pnt.y));
        }
        else if (pnt.y > getDim().height) {
            setCenter(new Point(pnt.x, getDim().height - 40));
        }
        else if (pnt.y < 0) {
            setCenter(new Point(pnt.x, 40));
        }
        else {
            setCenter(new Point((int) dX, (int) dY));
        }

        // Change falcon location
		if (bLeft) {
		    setCenter(new Point(getCenter().x - MOVE, getCenter().y));
            setOrientation(180);
            bFlame = true;
		}

        if (bRight) {
            setCenter(new Point(getCenter().x + MOVE, getCenter().y));
            setOrientation(0);
            bFlame = true;
        }

        if (bUp) {
            setCenter(new Point(getCenter().x, getCenter().y - MOVE));
            setOrientation(270);
            bFlame = true;
        }

        if (bDown) {
            setCenter(new Point(getCenter().x, getCenter().y + MOVE));
            setOrientation(90);
            bFlame = true;
        }

        // Change falcon orientation
        if (bTurningLeft) {
            if (getOrientation() <= 0 && bTurningLeft)
                setOrientation(360);
            setOrientation(getOrientation() - DEGREE_STEP);
        }

        if (bTurningRight) {
			if (getOrientation() >= 360 && bTurningRight)
				setOrientation(0);
			setOrientation(getOrientation() + DEGREE_STEP);
		}

	} //end move

	public void rotateLeft() {
		bTurningLeft = true;
	}

	public void rotateRight() {
		bTurningRight = true;
	}


    public void moveLeft() {
        bLeft = true;
    }

    public void moveRight() {
        bRight = true;
    }

    public void moveUP() {
        bUp = true;
    }

    public void moveDown() {
        bDown = true;
    }

    public void stopMoving() {
        bDown = false;
        bUp = false;
        bRight = false;
        bLeft = false;
        bFlame = false;
    }

	public void stopRotating() {
		bTurningRight = false;
		bTurningLeft = false;
	}

	private int adjustColor(int nCol, int nAdj) {
		if (nCol - nAdj <= 0) {
			return 0;
		} else {
			return nCol - nAdj;
		}
	}

	public void draw(Graphics g) {

		//does the fading at the beginning or after hyperspace
		Color colShip;
		if (getFadeValue() == 255) {
			colShip = Color.white;
		} else {
			colShip = new Color(adjustColor(getFadeValue(), 200), adjustColor(
					getFadeValue(), 175), getFadeValue());
		}

		//shield on
		if (bShield && nShield > 0) {

			g.setColor(Color.cyan);
			g.drawOval(getCenter().x - getRadius(),
					getCenter().y - getRadius(), getRadius() * 2,
					getRadius() * 2);

		} //end if shield

		//thrusting
		if (bFlame) {
			g.setColor(colShip);
			//the flame
			for (int nC = 0; nC < FLAME.length; nC++) {
				if (nC % 2 != 0) //odd
				{
					pntFlames[nC] = new Point((int) (getCenter().x + 2
							* getRadius()
							* Math.sin(Math.toRadians(getOrientation())
									+ FLAME[nC])), (int) (getCenter().y - 2
							* getRadius()
							* Math.cos(Math.toRadians(getOrientation())
									+ FLAME[nC])));

				} else //even
				{
					pntFlames[nC] = new Point((int) (getCenter().x + getRadius()
							* 1.1
							* Math.sin(Math.toRadians(getOrientation())
									+ FLAME[nC])),
							(int) (getCenter().y - getRadius()
									* 1.1
									* Math.cos(Math.toRadians(getOrientation())
											+ FLAME[nC])));

				} //end even/odd else

			} //end for loop

			for (int nC = 0; nC < FLAME.length; nC++) {
				nXFlames[nC] = pntFlames[nC].x;
				nYFlames[nC] = pntFlames[nC].y;

			} //end assign flame points

			//g.setColor( Color.white );
			g.fillPolygon(nXFlames, nYFlames, FLAME.length);

		} //end if flame

		drawShipWithColor(g, colShip);
	} //end draw()


	public void drawShipWithColor(Graphics g, Color col) {
		super.draw(g);
		g.setColor(col);
		g.fillPolygon(getXcoords(), getYcoords(), dDegrees.length);
	}

	public void fadeInOut() {
		if (getProtected()) {
			setFadeValue(getFadeValue() + 3);
		}
		if (getFadeValue() == 255) {
			setProtected(false);
		}
	}
	
	public void setProtected(boolean bParam) {
		if (bParam) {
			setFadeValue(0);
		}
		bProtected = bParam;
	}

	public void setProtected(boolean bParam, int n) {
		if (bParam && n % 3 == 0) {
			setFadeValue(n);
		} else if (bParam) {
			setFadeValue(0);
		}
		bProtected = bParam;
	}	

	public boolean getProtected() {return bProtected;}
	
} //end class
