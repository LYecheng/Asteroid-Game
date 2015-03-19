package edu.uchicago.cs.java.finalproject.game.view;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Panel;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.Buffer;
import java.util.Timer;
import java.util.concurrent.CopyOnWriteArrayList;
import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.Image;
import java.io.File;
import java.io.IOException;



import edu.uchicago.cs.java.finalproject.controller.Game;
import edu.uchicago.cs.java.finalproject.game.model.CommandCenter;
import edu.uchicago.cs.java.finalproject.game.model.Falcon;
import edu.uchicago.cs.java.finalproject.game.model.Movable;


 public class GamePanel extends Panel {
	
	// ==============================================================
	// FIELDS 
	// ============================================================== 
	 
	// The following "off" variables are used for the off-screen double-bufferred image.
	private Dimension dimOff;
	private Image imgOff;
	private Graphics grpOff;
	
	private GameFrame gmf;
	private Font fnt = new Font("SansSerif", Font.BOLD, 12);
	private Font fntBig = new Font("SansSerif", Font.BOLD + Font.ITALIC, 36);
    private Font fntN = new Font("SansSerif", Font.BOLD, 18);
	private FontMetrics fmt; 
	private int nFontWidth;
	private int nFontHeight;
	private String strDisplay = "";

    private BufferedImage myImage;

	// ==============================================================
	// CONSTRUCTOR 
	// ==============================================================
	
	public GamePanel(Dimension dim) throws IOException {
	    gmf = new GameFrame();
        gmf.getContentPane().add(this);
		gmf.pack();
		initView();
		
		gmf.setSize(dim);
		gmf.setTitle("Game Base");
		gmf.setResizable(false);
		gmf.setVisible(true);
		this.setFocusable(true);
	}
	
	
	// ==============================================================
	// METHODS 
	// ==============================================================
	
	@SuppressWarnings("unchecked")
	public void update(Graphics g) {

		if (grpOff == null || Game.DIM.width != dimOff.width
				|| Game.DIM.height != dimOff.height) {
			dimOff = Game.DIM;
			imgOff = createImage(Game.DIM.width, Game.DIM.height);
			grpOff = imgOff.getGraphics();
		}

		//Fill in background with background picture
        String Path = "img/background.jpg";
        try {
            myImage = ImageIO.read(new File(Path));
        } catch (IOException e) {
            e.printStackTrace();
        }
        grpOff.drawImage(myImage,0,0,Game.DIM.width, Game.DIM.height, Color.black, null);

        drawStatus(grpOff);
		
		if (!CommandCenter.isPlaying()) {
			displayTextOnScreen();
		} else if (CommandCenter.isPaused()) {
			strDisplay = "Game Paused";
			grpOff.drawString(strDisplay,
					(Game.DIM.width - fmt.stringWidth(strDisplay)) / 2, Game.DIM.height / 2);
		}
		
		//playing and not paused!
		else {
			//draw them in decreasing level of importance
			//friends will be on top layer and debris on the bottom
			iterateMovables(grpOff, 
					   CommandCenter.movDebris,
			           CommandCenter.movFloatersLife,
                       CommandCenter.movFloatersShield,
			           CommandCenter.movFoes,
			           CommandCenter.movFriends);

            drawNumberShieldsLeft(grpOff);
			drawNumberShipsLeft(grpOff);
			if (CommandCenter.isGameOver()) {
				CommandCenter.setPlaying(false);
				//bPlaying = false;
			}
		}
		//draw the double-Buffered Image to the graphics context of the panel
		g.drawImage(imgOff, 0, 0, this);
	} 

	//for each movable array, process it.
	private void iterateMovables(Graphics g, CopyOnWriteArrayList<Movable>...movMovz){
		
		for (CopyOnWriteArrayList<Movable> movMovs : movMovz) {
			for (Movable mov : movMovs) {

				mov.move();
				mov.draw(g);
				mov.fadeInOut();
				mov.expire();
			}
		}
	}

     private void initView() {
         Graphics g = getGraphics();			// get the graphics context for the panel
         g.setFont(fnt);						// take care of some simple font stuff
         fmt = g.getFontMetrics();
         nFontWidth = fmt.getMaxAdvance();
         nFontHeight = fmt.getHeight();
         g.setFont(fntBig);					// set font info
     }


     // This method draws some text to the middle of the screen before/after a game
     private void displayTextOnScreen() {
         grpOff.setFont(fntBig);
         strDisplay = "Welcome to ASTEROID";
         grpOff.drawString(strDisplay,
                 (Game.DIM.width - fmt.stringWidth(strDisplay) * 3) / 2, Game.DIM.height / 6);

         grpOff.setFont(fnt);
         strDisplay = "use the arrow keys to turn left and right.";
         grpOff.drawString(strDisplay,
                 (Game.DIM.width - fmt.stringWidth(strDisplay)) / 2, Game.DIM.height / 4
                         + nFontHeight );

         strDisplay = "'A', 'D' ,'W' , 'X' to move Left, Right, Up, and Down.";
         grpOff.drawString(strDisplay,
                 (Game.DIM.width - fmt.stringWidth(strDisplay)) / 2, Game.DIM.height / 4
                         + nFontHeight + 40);

         strDisplay = "use the space bar to fire";
         grpOff.drawString(strDisplay,
                 (Game.DIM.width - fmt.stringWidth(strDisplay)) / 2, Game.DIM.height / 4
                         + nFontHeight + 80);

         strDisplay = "'S' to Start";
         grpOff.drawString(strDisplay,
                 (Game.DIM.width - fmt.stringWidth(strDisplay)) / 2, Game.DIM.height / 4
                         + nFontHeight + 120);

         strDisplay = "'P' to Pause";
         grpOff.drawString(strDisplay,
                 (Game.DIM.width - fmt.stringWidth(strDisplay)) / 2, Game.DIM.height / 4
                         + nFontHeight + 160);

         strDisplay = "'Q' to Quit";
         grpOff.drawString(strDisplay,
                 (Game.DIM.width - fmt.stringWidth(strDisplay)) / 2, Game.DIM.height / 4
                         + nFontHeight + 200);
         strDisplay = "'N' for Shield";
         grpOff.drawString(strDisplay,
                 (Game.DIM.width - fmt.stringWidth(strDisplay)) / 2, Game.DIM.height / 4
                         + nFontHeight + 240);

         strDisplay = "'F' for Powerful Missile";
         grpOff.drawString(strDisplay,
                 (Game.DIM.width - fmt.stringWidth(strDisplay)) / 2, Game.DIM.height / 4
                         + nFontHeight + 280);

         strDisplay = "'H' for Hyperspace";
         grpOff.drawString(strDisplay,
                 (Game.DIM.width - fmt.stringWidth(strDisplay)) / 2, Game.DIM.height / 4
                         + nFontHeight + 320);

         grpOff.setFont(fntN);
         strDisplay = "Try to grab Life / Cruise / Shield floaters.";
         grpOff.drawString(strDisplay, (Game.DIM.width - fmt.stringWidth(strDisplay) - 100) / 2, Game.DIM.height / 4
                 + nFontHeight + 400);

         strDisplay = "Don't forget to watch out for UFO!";
         grpOff.drawString(strDisplay, (Game.DIM.width - fmt.stringWidth(strDisplay) - 50) / 2, Game.DIM.height / 4
                 + nFontHeight + 440);
     }

     public GameFrame getFrm() {return this.gmf;}

     public void setFrm(GameFrame frm) {this.gmf = frm;}


     // ==============================================================
     // DRAWING COMPONENTS
     // ==============================================================


     private void drawStatus(Graphics g) {
         g.setColor(Color.white);
         g.setFont(fnt);
         if (CommandCenter.isPlaying()) {
             g.drawString("SCORE :  " + CommandCenter.getScore() +  "    Current Level:  "  + CommandCenter.getLevel() + "    PROTECTED?  " + CommandCenter.getFalcon().getProtected() + "    Time: " + CommandCenter.getFalcon().getFadeValue() + "    Shields: " + CommandCenter.getNumShields() + "    Cruises: " + CommandCenter.getNumCruise(), nFontWidth, nFontHeight);
         } else {
             g.drawString("NIL", nFontWidth, nFontHeight);
         }
     }

	// Draw the number of falcons left on the bottom-right of the screen. 
	private void drawNumberShipsLeft(Graphics g) {
		Falcon fal = CommandCenter.getFalcon();
		double[] dLens = fal.getLengths();
		int nLen = fal.getDegrees().length;
		Point[] pntMs = new Point[nLen];
		int[] nXs = new int[nLen];
		int[] nYs = new int[nLen];
	
		//convert to cartesean points
		for (int nC = 0; nC < nLen; nC++) {
			pntMs[nC] = new Point((int) (10 * dLens[nC] * Math.sin(Math
					.toRadians(90) + fal.getDegrees()[nC])),
					(int) (10 * dLens[nC] * Math.cos(Math.toRadians(90)
							+ fal.getDegrees()[nC])));
		}
		
		//set the color to white
		g.setColor(Color.white);
		//for each falcon left (not including the one that is playing)
		for (int nD = 1; nD < CommandCenter.getNumFalcons(); nD++) {
			//create x and y values for the objects to the bottom right using cartesean points again
			for (int nC = 0; nC < fal.getDegrees().length; nC++) {
				nXs[nC] = pntMs[nC].x + Game.DIM.width - (20 * nD);
				nYs[nC] = pntMs[nC].y + Game.DIM.height - 40;
			}
			g.drawPolygon(nXs, nYs, nLen);
		} 
	}

     private void drawNumberShieldsLeft(Graphics g) {
         int numOfShields = CommandCenter.getNumShields();
         int[] nXs = new int[numOfShields];
         int[] nYs = new int[numOfShields];

         //set the color to white
         g.setColor(Color.white);
         //for each shield left (not including the one that is playing)
         for (int nC = 0; nC < numOfShields; nC++) {
             {
                 nXs[nC] = Game.DIM.width - (20 * nC);
                 nYs[nC] = 20;
             }
             g.drawOval(nXs[nC], nYs[nC], 12, 20 );
             //g.drawPolygon(nXs, nYs, nLen);
         }
     }
}