package com.strobertchs.cptfinalgame;

/**
 * Created by supriyamutharasan on 2017-06-01.
 */

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.view.MotionEvent;
import java.util.ArrayList;
import java.util.Random;

/**
 * Created by Supriya, Nathaniel, Tywin on 2017-05-08.
 */


class GameView extends SurfaceView implements Runnable {

    Canvas canvas;
    Thread ourThread = null;
    SurfaceHolder ourHolder;
    volatile boolean playPeaceGame;
    Paint paint;

    // properties of enemies that would like to be inputted for the objects.
    int enemyBreadth = 200;
    int ememyHeight = 200;

    int enemyGapX = 120;
    int enemyGapY = 100;

    int rowOfEnemies = 1;
    int y_offset = 100;
    int x_offset = 50;

    boolean reachedExit = false;

    // Intializing important objects whether it be in a list or independent

    ArrayList<Enemy> enemies;
    Bullet[] bullets = new Bullet[4];

    MainPlayer player;

    dPad leftDpad;
    dPad rightDpad;
    dPad upDpad;
    dPad downDpad;
    dPad centreDpad;
    dPad fireButton;

    Exit exit;
    PlayerBullet playerBullet;

    long lastFrameTime;
    int fps;
    int lives;

    int screenWidth;
    int screenHeight;


    Bitmap bitmap;

    boolean shooting = false;

    public GameView(Context context, int sScreenWidth, int sScreenHeight) {

        super(context);

        screenWidth = sScreenWidth;
        screenHeight = sScreenHeight;

        ourHolder = getHolder();
        paint = new Paint();

        // Installing properties to the objects

        player = new MainPlayer(context, sScreenHeight, sScreenWidth);

        leftDpad = new dPad(screenWidth/6, screenHeight/9, screenWidth/6, screenHeight/9 * 8);

        rightDpad = new dPad(screenWidth/6, screenHeight/9, screenWidth/6 * 3, screenHeight/9 * 8);

        downDpad = new dPad(screenWidth/6, screenHeight/9, screenWidth/6 * 2, screenHeight/9 * 9);

        upDpad = new dPad(screenWidth/6, screenHeight/9, screenWidth/6 * 2, screenHeight/9 * 7);

        centreDpad = new dPad(screenWidth/6, screenHeight/9, screenWidth/6 * 2, screenHeight/9 * 8);

        fireButton = new dPad(screenWidth/6, screenHeight/9, screenWidth/6 * 6, screenHeight/9 * 8);

        exit = new Exit(context, screenWidth/6, screenHeight/50, (screenWidth/2 - (screenWidth/12)), 0);

        enemies = new ArrayList<Enemy>();
        enemies = generateEnemy(context);

        playerBullet = new PlayerBullet(context, screenWidth, screenHeight, -10,-10);

        int index = 0;

        // Make a list bullets so that each enemy have their own bullet. With the help of index, instruct bullets to move down.
        while(index < enemies.size())
        {
            bullets[index] = new Bullet(context, screenWidth, screenHeight, enemies.get(index).getPositionX(),enemies.get(index).getPositionY());
            bullets[index].moveDown();
            index++;
        }

        // setting background image
        Resources res = getResources();
        bitmap = BitmapFactory.decodeResource(res, R.drawable.grass_14);

        //Send the bullet in the random horizontal direction
        Random randomNumber = new Random();


        for(int i=0;i<4;i++) {
            Bullet bullet = bullets[i];
            int bulletDirection = randomNumber.nextInt(3);
            switch(bulletDirection){
                case 0:
                    bullet.moveLeft();
                    break;

                case 1:
                    bullet.moveRight();
                    break;

                case 2:
                    bullet.moveStraight();
                    break;
            }
        }


    }


    /**
     *  purpose: to control and actually run the full game flow
      */


    @Override
    public void run() {
        while (playPeaceGame) {
            for(int i = 0; i < bullets.length ; i++) {
                updateCourt(bullets[i]);
            }
            if(collideWithExit()) {
                drawWinGameOver(player.getScore());
            }
            if(player.getScore() <= 0)
            {
                drawLoseGameOver(player.getScore());
            }
            else
            {
                drawCourt();
            }
            controlFPS();
            DestoyBullet();
        }
    }


    /**Genrates enemy in a array list to fit according to the width of the screen
     *
     * @param context
     * @return returns a list of objects called enemy
     */
    public ArrayList<Enemy> generateEnemy(Context context)
    {
        for(int i = 0; i < rowOfEnemies; i++)
        {
            int x = x_offset;
            int y = i * (ememyHeight + enemyGapY) + y_offset;

            while(x < screenWidth)
            {
                enemies.add(new Enemy(context, enemyBreadth, ememyHeight, x, y));
                x += enemyBreadth + enemyGapX;
            }

        }

        return enemies;
    }

    /**
     * Purpose: Specifies the consequence of the player colliding with the Bullet.
     */
    public void DestoyBullet () {
        for (int i = 0; i < bullets.length; i++) {
            Bullet bullet = bullets[i];
            // if player collides with bullet
            if (bullet.getPositionY() > screenHeight - bullet.getWidth() || collision(bullets[i])) {

                boolean isHit = collision((bullets[i]));

                if(isHit && bullets[i].visible) {
                    player.setPositionX(30);
                    player.setPositionY(screenHeight - 100);
                    player.setScore( player.getScore() - 5);
                }

                //reset the bullet  to the top of the screen
                bullet.setPositionY(enemies.get(i).getPositionY());
                bullet.setPositionX(enemies.get(i).getPositionX());


                //what horizontal direction should we use
                //for the next falling bullet
                bullet.moveDown();
                Random randomNumber = new Random();


                int bulletDirection = randomNumber.nextInt(3);
                switch (bulletDirection) {

                    case 0:
                        bullet.moveLeft();
                        break;
                    case 1:
                        bullet.moveRight();
                        break;
                    case 2:
                        bullet.moveStraight();
                        break;
                }
            }
        }
    }

    /** Method specifies the collision between the player and the exit
     * @return false, true if player collides with the exit
     */
    public boolean collideWithExit ()
    {
        if (exit.getPositionY() + exit.getWidth() >= player.getPositionY() && exit.getPositionY() + exit.getWidth() <= player.getPositionY() + player.getHeight() / 2 && exit.getPositionX() + exit.getWidth() >= player.getPositionX() && exit.getPositionX() <= player.getPositionX() + player.getWidth()) {
            return true;
        }
        if (exit.getPositionY() <= player.getPositionY() + player.getHeight() && exit.getPositionY() >= player.getPositionY() + player.getHeight() / 2 && exit.getPositionX() + exit.getWidth() > player.getPositionX() && exit.getPositionX() < player.getPositionX() + player.getWidth()) {
            return true;
        }

        if (exit.getPositionX() + exit.getWidth() >= player.getPositionX() && exit.getPositionX() + exit.getWidth() <= player.getPositionX() + player.getWidth() / 2 && exit.getPositionY() + exit.getHeight() >= player.getPositionY() && exit.getPositionY() <= player.getPositionY() + player.getHeight()) {
            return true;
        }
        if (exit.getPositionX() <= player.getPositionX() + player.getWidth() && exit.getPositionX() > player.getPositionX() + player.getWidth() && exit.getPositionY() + exit.getHeight() >= player.getPositionY() && exit.getPositionY() <= player.getPositionY() + player.getHeight()) {
            return true;
        }
        return false;
    }

    /** Method specifies collision between the player's bullet and the enemy
     * @param enemy
     * @return false, true if collision happens
     */
    public boolean collideWithplayerBullet(Enemy enemy)
    {
        if (playerBullet.getPositionY() + playerBullet.getWidth() >= enemy.getPositionY() && playerBullet.getPositionY() + playerBullet.getWidth() <= enemy.getPositionY() + enemy.getHeight() / 2 && playerBullet.getPositionX() + playerBullet.getWidth() >= enemy.getPositionX() && playerBullet.getPositionX() <= enemy.getPositionX() + enemy.getWidth()) {
            return true;
        }
        if (playerBullet.getPositionY() <= enemy.getPositionY() + enemy.getHeight() && playerBullet.getPositionY() >= enemy.getPositionY() + enemy.getHeight() / 2 && playerBullet.getPositionX() + playerBullet.getWidth() > enemy.getPositionX() && playerBullet.getPositionX() < enemy.getPositionX() + enemy.getWidth()) {
            return true;
        }

        if (playerBullet.getPositionX() + playerBullet.getWidth() >= enemy.getPositionX() && playerBullet.getPositionX() + playerBullet.getWidth() <= enemy.getPositionX() + enemy.getWidth() / 2 && playerBullet.getPositionY() + playerBullet.getHeight() >= enemy.getPositionY() && playerBullet.getPositionY() <= enemy.getPositionY() + enemy.getHeight()) {
            return true;
        }
        if (playerBullet.getPositionX() <= enemy.getPositionX() + enemy.getWidth() && playerBullet.getPositionX() > enemy.getPositionX() + enemy.getWidth() && playerBullet.getPositionY() + playerBullet.getHeight() >= enemy.getPositionY() && playerBullet.getPositionY() <= enemy.getPositionY() + enemy.getHeight()) {
            return true;
        }
        return false;
    }


    /** Method specifies the general collisions of the player, the bullet
     * @param bullet
     */
    public void updateCourt(Bullet bullet) {
        // move racket only if it is not at any edge of the screen
        if (player.isMovingRight()) {
            if(player.getPositionX() + player.getWidth()< screenWidth){ //move right only if you haven't hit the right edge
                player.updatePosition();
            }
        }

        if (player.isMovingLeft()) {
            if (player.getPositionX() > 0){  //move left only if you haven't hit the left edge
                player.updatePosition();
            }
        }

        if (player.isMovingDown()) {
            if (player.getPositionY() + player.getHeight()/2 < (screenHeight-40)){  //move down only if you haven't hit the bottomedge
                player.updatePosition();
            }
        }

        if (player.isMovingUp()) {
            if (player.getPositionY() > 0){  //move up only if you haven't hit the top edge
                player.updatePosition();
            }
        }


        //if bullet has hit right of screen
        if (bullet.getPositionX() + bullet.getWidth() > screenWidth) {
            bullet.moveLeft();
        }


        //if bullet has hit left of screen
        if (bullet.getPositionX() < 0) {
            bullet.moveRight();
        }

        // Call the method to make the player bullet sprite move
        playerBullet.updatePosition();

        // Call the method to make the  bullet sprite move
        bullet.updatePosition();

        for(int i=0;i<enemies.size();i++) {
            Enemy enemy = enemies.get(i);

            // if the player bullet hits the enemy
            if(collideWithplayerBullet(enemy))
            {
                enemy.visible = false;
                bullets[i].visible = false;
                player.setScore( player.getScore() - 5 );

            }
        }

    }


    /** Method portraits the collision between the player and one of enemy's bullet
     * @param bullet
     * @return false only when collision does not happen
     */
    public boolean  collision(Bullet bullet) {
        if (bullet.getPositionY() + bullet.getWidth() >= player.getPositionY() && bullet.getPositionY() + bullet.getWidth() <= player.getPositionY() + player.getHeight() / 2 && bullet.getPositionX() + bullet.getWidth() >= player.getPositionX() && bullet.getPositionX() <= player.getPositionX() + player.getWidth()) {
            return true;
        }
        if (bullet.getPositionY() <= player.getPositionY() + player.getHeight() && bullet.getPositionY() >= player.getPositionY() + player.getHeight() / 2 && bullet.getPositionX() + bullet.getWidth() > player.getPositionX() && bullet.getPositionX() < player.getPositionX() + player.getWidth()) {
            return true;
        }

        if (bullet.getPositionX() + bullet.getWidth() >= player.getPositionX() && bullet.getPositionX() + bullet.getWidth() <= player.getPositionX() + player.getWidth() / 2 && bullet.getPositionY() + bullet.getHeight() >= player.getPositionY() && bullet.getPositionY() <= player.getPositionY() + player.getHeight()) {
            return true;
        }
        if (bullet.getPositionX() <= player.getPositionX() + player.getWidth() && bullet.getPositionX() > player.getPositionX() + player.getWidth() && bullet.getPositionY() + bullet.getHeight() >= player.getPositionY() && bullet.getPositionY() <= player.getPositionY() + player.getHeight()) {
            return true;
        }
        return false;
    }


    /** Functionality is draw all the objects onto the screen
     */
    public void drawCourt() {
        if (ourHolder.getSurface().isValid()) {
            canvas = ourHolder.lockCanvas();

            //Paint paint = new Paint();
            canvas.drawColor(Color.BLACK);//the background
            paint.setColor(Color.argb(255, 255, 255, 255));

            canvas.drawBitmap(bitmap, 0, 0, paint);
            paint.setTextSize(45);

            canvas.drawText("Score:" + player.getScore() + " fps:" + fps, 20, 40, paint);

            // draw each enemy
            for (int i = 0; i < enemies.size(); i ++)
            {if(enemies.get((i)).visible) {
                enemies.get(i).draw(canvas);
             }
            }

            //Draw the main player
            player.draw(canvas);

            //Draw the bullet
            for(int i = 0; i < bullets.length; i++) {
                if(bullets[i].visible) {
                    bullets[i].draw(canvas);
                }
            }

            // draw all the dpad objects
            upDpad.draw(canvas);
            downDpad.draw(canvas);
            rightDpad.draw(canvas);
            leftDpad.draw(canvas);
            centreDpad.draw(canvas);
            fireButton.draw(canvas);

            // if the player presses the firebutton, it changes the shooting boolean to true
            // Once done, then only will the if statement here make sure that the player bullet object is drawn and goes upwards
            if(shooting = true) {
                playerBullet.draw(canvas);
                playerBullet.moveUp();
            }
            //draw the exit
            exit.draw(canvas);

            // Makes sure that once the player reaches the exit. then only replace the canvas with a Winning Game over screen
            if(collideWithExit())
            {
                Paint myPaint = new Paint();
                canvas.drawPaint(myPaint);
                myPaint.setColor(Color.BLACK);
                myPaint.setTextSize(50);
                canvas.drawText("Congratulations! You won. Your score is " + player.getScore(), 100, (screenHeight / 2), paint);

                player.stop();
            }

            ourHolder.unlockCanvasAndPost(canvas);




        }
    }

    /** Functionality of the method is avoid the screen from seizuring and set the winning screen permamnent
     * @param score
     */
    public void  drawWinGameOver (int score)
    {
        if (ourHolder.getSurface().isValid())
        {
            canvas = ourHolder.lockCanvas();

            Paint myPaint = new Paint();
            canvas.drawPaint(myPaint);
            myPaint.setColor(Color.BLACK);
            myPaint.setTextSize(50);
            canvas.drawText("Congratulations! You won. Your score is " + player.getScore(), 100, (screenHeight / 2), paint);

            ourHolder.unlockCanvasAndPost(canvas);

        }
    }


    /** Method replaces current canvas with a game over screen, had the player got a score of 0
     * @param score
     */
    public void  drawLoseGameOver (int score) {
        if (ourHolder.getSurface().isValid())
        {
            canvas = ourHolder.lockCanvas();

            Paint myPaint = new Paint();
            canvas.drawPaint(myPaint);
            myPaint.setColor(Color.BLACK);
            myPaint.setTextSize(50);
            canvas.drawText("Game Over! Please Try Again. Your score is " + player.getScore(), 100, (screenHeight / 2), paint);

            ourHolder.unlockCanvasAndPost(canvas);

        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent motionEvent) {
        switch (motionEvent.getAction() & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN:

                //right D-pad
                if (motionEvent.getX() <= screenWidth/2 && motionEvent.getX() >= (screenWidth/6 * 2)) {
                    if (motionEvent.getY() <= (screenHeight - screenHeight/9) && motionEvent.getY() >= (screenHeight/9 * 7)) {
                        player.moveRight();
                    }
                }

                //left D-pad
                if (motionEvent.getX() <= screenWidth/6) {
                    if (motionEvent.getY() <= (screenHeight - screenHeight/9) && motionEvent.getY() >= (screenHeight/9 * 7)) {
                        player.moveLeft();
                    }
                }

                //up D-pad
                if (motionEvent.getX() <= (screenWidth/2 - screenWidth/6) && motionEvent.getX() >= screenWidth/6) {
                    if (motionEvent.getY() <= (screenHeight/9 * 7) && motionEvent.getY() >= (screenHeight/9 * 6)) {
                        player.moveUp();
                    }
                }

                //down D-pad
                if(motionEvent.getX() <= (screenWidth/6 * 2) && motionEvent.getX() >= screenWidth/6) {
                    if (motionEvent.getY() <= screenHeight && motionEvent.getY() >= screenHeight - screenHeight/9) {
                        player.moveDown();
                    }
                }

                //fire button
                if (motionEvent.getX() <= screenWidth && motionEvent.getX() >= screenWidth/6 * 5) {
                    if (motionEvent.getY() <= (screenHeight - screenHeight/9) && motionEvent.getY() >= (screenHeight/9 * 7)) {
                        playerBullet.setPositionX(player.getPositionX());
                        playerBullet.setPositionY(player.getPositionY());
                        shooting = true;
                        playerBullet.moveUp();

                    }
                }

                break;

            case MotionEvent.ACTION_UP:
                player.stop();
                break;
        }
        return true;
    }


    public void controlFPS()
    {
        long timeThisFrame = (System.currentTimeMillis() - lastFrameTime);
        long timeToSleep = 15 - timeThisFrame;
        if (timeThisFrame > 0)
        {
            fps = (int) (1000 / timeThisFrame);
        }
        if (timeToSleep > 0)
        {
            try
            {
                ourThread.sleep(timeToSleep);
            }
            catch (InterruptedException e) {}
        }
        lastFrameTime = System.currentTimeMillis();

    }


    public void pause() {
        playPeaceGame = false;
        try {
            ourThread.join();
        } catch (InterruptedException e) {
        }
    }

    public void resume() {
        playPeaceGame = true;
        ourThread = new Thread(this);
        ourThread.start();
    }

}
