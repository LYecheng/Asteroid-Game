package edu.uchicago.cs.java.finalproject.controller;

import java.awt.*;
import java.awt.event.*;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.sound.sampled.Clip;

import edu.uchicago.cs.java.finalproject.game.model.*;
import edu.uchicago.cs.java.finalproject.game.view.*;
import edu.uchicago.cs.java.finalproject.sounds.Sound;

// ===============================================
// == This Game class is the CONTROLLER
// ===============================================

public class Game implements Runnable, KeyListener {

    // ===============================================
    // FIELDS
    // ===============================================

    public static final Dimension DIM = new Dimension(1300, 700); //the dimension of the game.
    private GamePanel gmpPanel;
    public static Random R = new Random();
    public final static int ANI_DELAY = 10; // milliseconds between screen

    // updates (animation)
    private Thread thrAnim;
    private int nLevel = 1;
    private static int nTick = 0;
    private ArrayList<Tuple> tupMarkForRemovals;
    private ArrayList<Tuple> tupMarkForAdds;
    private boolean bMuted = true;
    private static final long SCORE_GAIN = 10;

    // keyCode reference : http://www.cambiaresearch.com/articles/15/javascript-char-codes-key-codes
    private final int
            PAUSE = 80, // p key
            QUIT = 81, // q key

            LEFT = 37, // rotate left; left arrow
            RIGHT = 39, // rotate right; right arrow

            LEFTM = 65, // move left; a key
            RIGHTM = 68, // move right; d key
            UPM = 87, // move up; w key
            DOWNM = 88, // move down; x key


            START = 83, // s key
            FIRE = 32, // space key
            MUTE = 77, // m key

            HYPER = 72, 					// h key
            SHIELD = 78, 				// n key arrow
            CRUISE = 70; 					// fire special weapon;  F key



    private Clip clpThrust;
    private Clip clpMusicBackground;

    private static final int SPAWN_NEW_SHIP_FLOATER = 150;
    private static final int SPAWN_NEW_SHIELD_FLOATER = 100;
    private static final int SPAWN_NEW_CRUISE_FLOATER = 50;
    private static final int SPAWN_NEW_UFO = 200;

    // ===============================================
    // ==CONSTRUCTOR
    // ===============================================

    public Game() throws IOException {

        gmpPanel = new GamePanel(DIM);
        gmpPanel.addKeyListener(this);

        clpThrust = Sound.clipForLoopFactory("whitenoise.wav");
        clpMusicBackground = Sound.clipForLoopFactory("music-background.wav");
    }

    // ===============================================
    // ==METHODS
    // ===============================================

    public static void main(String args[]) {
        EventQueue.invokeLater(new Runnable() { // uses the Event dispatch thread from Java 5 (refactored)
            public void run() {
                try {
                    Game game = new Game(); // construct itself
                    game.fireUpAnimThread();

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void fireUpAnimThread() { // called initially
        if (thrAnim == null) {
            thrAnim = new Thread(this); // pass the thread a runnable object (this)
            thrAnim.start();
        }
    }

    // implements runnable - must have run method
    public void run() {

        // lower this thread's priority; let the "main" aka 'Event Dispatch'
        // thread do what it needs to do first
        thrAnim.setPriority(Thread.MIN_PRIORITY);

        // and get the current time
        long lStartTime = System.currentTimeMillis();

        // this thread animates the scene
        while (Thread.currentThread() == thrAnim) {
            tick();
            spawnNewShipFloater();
            spawnNewShieldFloater();
            spawnNewCruiseFloater();
            spawnNewUFO();
            gmpPanel.update(gmpPanel.getGraphics()); // update takes the graphics context we must
            // surround the sleep() in a try/catch block
            // this simply controls delay time between
            // the frames of the animation

            //this might be a good place to check for collisions
            checkCollisions();
            //this might be a god place to check if the level is clear (no more foes)
            //if the level is clear then spawn some big asteroids -- the number of asteroids
            //should increase with the level.
            checkNewLevel();

            try {
                // The total amount of time is guaranteed to be at least ANI_DELAY long.  If processing (update)
                // between frames takes longer than ANI_DELAY, then the difference between lStartTime -
                // System.currentTimeMillis() will be negative, then zero will be the sleep time
                lStartTime += ANI_DELAY;
                Thread.sleep(Math.max(0, lStartTime - System.currentTimeMillis()));
            } catch (InterruptedException e) {
                // just skip this frame -- no big deal
                continue;
            }
        } // end while
    } // end run

    private void checkCollisions() {

        //@formatter:off
        //for each friend in movFriends
        //for each foe in movFoes
        //if the distance between the two centers is less than the sum of their radii
        //mark it for removal

        //for each mark-for-removal
        //remove it
        //for each mark-for-add
        //add it
        //@formatter:on

        //we use this ArrayList to keep pairs of movMovables/movTarget for either
        //removal or insertion into our arrayLists later on
        tupMarkForRemovals = new ArrayList<Tuple>();
        tupMarkForAdds = new ArrayList<Tuple>();

        Point pntFriendCenter, pntFoeCenter;
        int nFriendRadiux, nFoeRadiux;

        // 1. check friend VS foe
        for (Movable movFriend : CommandCenter.movFriends) {
            pntFriendCenter = movFriend.getCenter();
            nFriendRadiux = movFriend.getRadius();

            for (Movable movFoe : CommandCenter.movFoes) {

                pntFoeCenter = movFoe.getCenter();
                nFoeRadiux = movFoe.getRadius();

                //detect collision
                if (pntFriendCenter.distance(pntFoeCenter) < (nFriendRadiux + nFoeRadiux)) {
                    //falcon
                    if ((movFriend instanceof Falcon) ){
                        if (!CommandCenter.getFalcon().getProtected()){
                            tupMarkForRemovals.add(new Tuple(CommandCenter.movFriends, movFriend));
                            CommandCenter.spawnFalcon(false);
                            CommandCenter.setScore(CommandCenter.getScore() - 10 * SCORE_GAIN);
                            killFoe(movFoe);
                        }
                    }
                    //not the falcon
                    else if (movFriend instanceof Cruise) {
                            tupMarkForRemovals.add(new Tuple(CommandCenter.movFriends, movFriend));
                            CommandCenter.setScore(CommandCenter.getScore() + SCORE_GAIN * 2);
                            killFoe2(movFoe);
                        }
                    else  {
                            tupMarkForRemovals.add(new Tuple(CommandCenter.movFriends, movFriend));
                            CommandCenter.setScore(CommandCenter.getScore() + SCORE_GAIN);
                            killFoe(movFoe);
                    }//end else
                }//end if
            }//end inner for
        }//end outer for

        //2. check falcon VS floater
        if (CommandCenter.getFalcon() != null){
            Point pntFalCenter = CommandCenter.getFalcon().getCenter();
            int nFalRadiux = CommandCenter.getFalcon().getRadius();
            Point pntFloaterCenter;
            int nFloaterRadiux;

            for (Movable movFloater : CommandCenter.movFloatersLife) {
                pntFloaterCenter = movFloater.getCenter();
                nFloaterRadiux = movFloater.getRadius();

                //detect collision
                if (pntFalCenter.distance(pntFloaterCenter) < (nFalRadiux + nFloaterRadiux)) {
                    tupMarkForRemovals.add(new Tuple(CommandCenter.movFloatersLife, movFloater));
                    // Add another available falcon
                    CommandCenter.setNumFalcons(CommandCenter.getNumFalcons() + 1);
                    Sound.playSound("pacman_eatghost.wav");
                }//end if
            }//end inner for

            for (Movable movFloater : CommandCenter.movFloatersShield) {
                pntFloaterCenter = movFloater.getCenter();
                nFloaterRadiux = movFloater.getRadius();

                //detect collision
                if (pntFalCenter.distance(pntFloaterCenter) < (nFalRadiux + nFloaterRadiux)) {
                    tupMarkForRemovals.add(new Tuple(CommandCenter.movFloatersShield, movFloater));
                    // Add another available falcon
                    CommandCenter.setNumShields(CommandCenter.getNumShields() + 1);
                    Sound.playSound("pacman_eatghost.wav");
                }//end if
            }

            for (Movable movFloater : CommandCenter.movFloatersCruise) {
                pntFloaterCenter = movFloater.getCenter();
                nFloaterRadiux = movFloater.getRadius();

                //detect collision
                if (pntFalCenter.distance(pntFloaterCenter) < (nFalRadiux + nFloaterRadiux)) {
                    tupMarkForRemovals.add(new Tuple(CommandCenter.movFloatersCruise, movFloater));
                    // Add another available falcon
                    CommandCenter.setNumCruise(CommandCenter.getNumCruise() + 1);
                    Sound.playSound("pacman_eatghost.wav");
                }//end if
            }
        }//end if not null

        //remove these objects from their appropriate ArrayLists
        //this happens after the above iterations are done
        for (Tuple tup : tupMarkForRemovals)
            tup.removeMovable();

        //add these objects to their appropriate ArrayLists
        //this happens after the above iterations are done
        for (Tuple tup : tupMarkForAdds)
            tup.addMovable();

        //call garbage collection
        System.gc();

    }//end meth


    private void killFoe(Movable movFoe) {

        if (movFoe instanceof Asteroid){
            //we know this is an Asteroid, so we can cast without threat of ClassCastException
            Asteroid astExploded = (Asteroid)movFoe;
            //big asteroid
            if(astExploded.getSize() == 0) {
                //spawn two medium Asteroids
                tupMarkForAdds.add(new Tuple(CommandCenter.movFoes,new Asteroid(astExploded)));
                tupMarkForAdds.add(new Tuple(CommandCenter.movFoes,new Asteroid(astExploded)));
                tupMarkForRemovals.add(new Tuple(CommandCenter.movFoes, movFoe));
            }
            //medium size aseroid exploded
            else if(astExploded.getSize() == 1) {
                //spawn three small Asteroids
                tupMarkForAdds.add(new Tuple(CommandCenter.movFoes,new Asteroid(astExploded)));
                tupMarkForAdds.add(new Tuple(CommandCenter.movFoes,new Asteroid(astExploded)));
                tupMarkForAdds.add(new Tuple(CommandCenter.movFoes,new Asteroid(astExploded)));
                tupMarkForRemovals.add(new Tuple(CommandCenter.movFoes, movFoe));
            }
            //remove the original Foe
            else {
                tupMarkForRemovals.add(new Tuple(CommandCenter.movFoes, movFoe));
                tupMarkForAdds.add(new Tuple(CommandCenter.movDebris, new Explosion(astExploded)));
            }
        }

        else if (movFoe instanceof UFO) {
            UFO uShip = (UFO)movFoe;
            if (uShip.getLife() == 0) {
                tupMarkForRemovals.add(new Tuple(CommandCenter.movFoes, movFoe));
                tupMarkForAdds.add(new Tuple(CommandCenter.movDebris, new Explosion(uShip)));
                killFoe2(movFoe);
            }
            else {
                uShip.setLife(uShip.getLife() - 1);
                CommandCenter.setScore(CommandCenter.getScore() + SCORE_GAIN * 3);
            }
        }

        //not an asteroid
        else {
            //remove the original Foe
            tupMarkForRemovals.add(new Tuple(CommandCenter.movFoes, movFoe));
        }
    }

    private void killFoe2(Movable movFoe) {
        //we know this is an Asteroid, so we can cast without threat of ClassCastException
        //big asteroid
        tupMarkForRemovals.add(new Tuple(CommandCenter.movFoes, movFoe));
        if (movFoe instanceof UFO) {
            tupMarkForAdds.add(new Tuple(CommandCenter.movDebris, new Explosion((UFO)movFoe)));
        }else {
            tupMarkForAdds.add(new Tuple(CommandCenter.movDebris, new Explosion((Asteroid)movFoe)));
        }
    }

    //some methods for timing events in the game,
    //such as the appearance of UFOs, floaters (power-ups), etc.
    public void tick() {
        if (nTick == Integer.MAX_VALUE)
            nTick = 0;
        else
            nTick++;
    }

    public static int getTick() {
        return nTick;
    }

    private void spawnNewShipFloater() {
        //make the appearance of power-up dependent upon ticks and levels
        //the higher the level the more frequent the appearance
        if (nTick % (SPAWN_NEW_SHIP_FLOATER - nLevel * 7) == 0)
        {
            CommandCenter.movFloatersLife.add(new PowerUp_Life());
        }
    }

    private void spawnNewCruiseFloater() {
        //make the appearance of power-up dependent upon ticks and levels
        //the higher the level the more frequent the appearance
        if (nTick % (SPAWN_NEW_CRUISE_FLOATER - nLevel * 10) == 0)
        {
            CommandCenter.movFloatersCruise.add(new PowerUp_Cruise());
        }
    }

    private void spawnNewShieldFloater() {
        //make the appearance of power-up dependent upon ticks and levels
        //the higher the level the more frequent the appearance
        if (nTick % (SPAWN_NEW_SHIELD_FLOATER - nLevel * 7) == 0)
        {
            CommandCenter.movFloatersShield.add(new PowerUp_Shield());
            System.out.println(1);
        }

    }

    private void spawnNewUFO() {
        if (nTick % (SPAWN_NEW_UFO - nLevel * 7) == 0){
               System.out.println(1);
               CommandCenter.movFoes.add(new UFO());
           }
    }


    // Called when user presses 's'
    private void startGame() {
        CommandCenter.clearAll();
        CommandCenter.initGame();
        CommandCenter.setLevel(0);
        CommandCenter.setPlaying(true);
        CommandCenter.setPaused(false);
        if (!bMuted)
            clpMusicBackground.loop(Clip.LOOP_CONTINUOUSLY);
    }

    //this method spawns new asteroids
    private void spawnAsteroids(int nNum) {
        for (int nC = 0; nC < nNum; nC++) {
            //Asteroids with size of zero are big
            CommandCenter.movFoes.add(new Asteroid(0));
        }
    }

    private boolean isLevelClear(){
        //if there are no more Asteroids on the screen

        boolean bAsteroidFree = true;
        for (Movable movFoe : CommandCenter.movFoes) {
            if (movFoe instanceof Asteroid){
                bAsteroidFree = false;
                break;
            }
        }

        return bAsteroidFree;
    }

    private void checkNewLevel(){

        if (isLevelClear() ){
            if (CommandCenter.getFalcon() !=null)
                CommandCenter.getFalcon().setProtected(true);

            spawnAsteroids(CommandCenter.getLevel() + 2);
            CommandCenter.setNumFalcons(CommandCenter.getNumFalcons() + 1);
            CommandCenter.setLevel(CommandCenter.getLevel() + 1);
        }
    }

    // Varargs for stopping looping-music-clips
    private static void stopLoopingSounds(Clip... clpClips) {
        for (Clip clp : clpClips) {
            clp.stop();
        }
    }

    // ===============================================
    // KEYLISTENER METHODS
    // ===============================================

    @Override
    public void keyPressed(KeyEvent e) {
        Falcon fal = CommandCenter.getFalcon();
        int nKey = e.getKeyCode();

        if (nKey == START && !CommandCenter.isPlaying())
            startGame();

        if (fal != null) {

            switch (nKey) {
                case PAUSE:
                    CommandCenter.setPaused(!CommandCenter.isPaused());
                    if (CommandCenter.isPaused())
                        stopLoopingSounds(clpMusicBackground, clpThrust);
                    else
                        clpMusicBackground.loop(Clip.LOOP_CONTINUOUSLY);
                    break;
                case QUIT:
                    System.exit(0);
                    break;
                case LEFT:
                    fal.rotateLeft();
                    break;
                case RIGHT:
                    fal.rotateRight();
                    break;
                case LEFTM:
                    fal.moveLeft();
                    break;
                case RIGHTM:
                    fal.moveRight();
                    break;
                case UPM:
                    fal.moveUP();
                    break;
                case DOWNM:
                    fal.moveDown();
                    break;
                // possible future use
                case HYPER:
                    CommandCenter.getFalcon().setCenter(new Point(R.nextInt(DIM.width), R.nextInt(DIM.height)));
                case SHIELD:
                    if (CommandCenter.getNumShields() > 0) {
                        CommandCenter.getFalcon().setProtected(true, 2000);
                        CommandCenter.setNumShields(CommandCenter.getNumShields() - 1);
                    }
                    break;
                // case NUM_ENTER:
                default:
                    break;
            }
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
        Falcon fal = CommandCenter.getFalcon();
        int nKey = e.getKeyCode();
        System.out.println(nKey);

        if (fal != null) {
            switch (nKey) {
                case FIRE:
                    CommandCenter.movFriends.add(new Bullet(fal));
                    Sound.playSound("laser.wav");
                    break;
                case CRUISE:
                    if (CommandCenter.getNumCruise() > 0) {
                        CommandCenter.movFriends.add(new Cruise(fal));
                        CommandCenter.setNumCruise(CommandCenter.getNumCruise() - 1);
                        Sound.playSound("laser.wav");
                    }
                    break;

                case LEFT:
                case RIGHT:
                    fal.stopRotating();
                    break;
                case LEFTM:
                case RIGHTM:
                case UPM:
                case DOWNM:
                    fal.stopMoving();
                    break;
                case MUTE:
                    if (!bMuted){
                        stopLoopingSounds(clpMusicBackground);
                        bMuted = !bMuted;
                    }
                    else {
                        clpMusicBackground.loop(Clip.LOOP_CONTINUOUSLY);
                        bMuted = !bMuted;
                    }
                    break;
                default:
                    break;
            }
        }
    }

    @Override
    // Just need it b/c of KeyListener implementation
    public void keyTyped(KeyEvent e) {
    }

}

// ===============================================
// ==A tuple takes a reference to an ArrayList and a reference to a Movable
//This class is used in the collision detection method, to avoid mutating the array list while we are iterating
// it has two public methods that either remove or add the movable from the appropriate ArrayList 
// ===============================================

class Tuple{
    //this can be any one of several CopyOnWriteArrayList<Movable>
    private CopyOnWriteArrayList<Movable> movMovs;
    //this is the target movable object to remove
    private Movable movTarget;

    public Tuple(CopyOnWriteArrayList<Movable> movMovs, Movable movTarget) {
        this.movMovs = movMovs;
        this.movTarget = movTarget;
    }

    public void removeMovable(){
        movMovs.remove(movTarget);
    }

    public void addMovable(){
        movMovs.add(movTarget);
    }

}