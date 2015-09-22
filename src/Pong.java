import com.jogamp.opengl.*;
import com.jogamp.opengl.awt.GLCanvas;
import com.jogamp.opengl.util.FPSAnimator;
import com.jogamp.opengl.util.awt.TextRenderer;

import java.awt.*;
import java.awt.event.*;
import java.util.Random;

/**
 * Program: Pong
 * @author Rubin Trailor
 *
 * This program recreates the classic game Pong. The program uses shapes and animation from
 * JOGL (Java OpenGL) to test its basic uses.
 */
public class Pong implements GLEventListener {

    private static float y1 = 0;
    private static float y2 = 0;
    private float cx = 0, cy = 0;
    private float ctx, cty;
    private static boolean p1IsMovingUp = false, p1IsMovingDown = false, p2IsMovingUp = false, p2IsMovingDown = false;
    private float movementVelocity = 0.025f;
    private final int MAX_SCORE = 11;
    int p1Score = 0;
    int p2Score = 0;
    TextRenderer p1ScoreText;
    TextRenderer p2ScoreText;
    private boolean gameover = false;

    /**
     * Initialization of score text and random direction for ball.
     */
    public void init(GLAutoDrawable drawable) {
        p1ScoreText = new TextRenderer(new Font("SansSerif", Font.BOLD, 72));
        p2ScoreText = new TextRenderer(new Font("SansSerif", Font.BOLD, 72));
        randomDirection();
    }

    /**
     * Renders all necessary objects and checks if the game is over.
     */
    public void display(GLAutoDrawable drawable) {
        final GL2 gl = drawable.getGL().getGL2();

        gl.glClear(GL2.GL_COLOR_BUFFER_BIT | GL.GL_DEPTH_BUFFER_BIT);
        gl.glLoadIdentity();

        gameover = checkGameOver();

        if (gameover) {
            gameOver();
            return;
        }

        p1ScoreText.beginRendering(1024, 768);
        p1ScoreText.setColor(1.0f, 1.0f, 1.0f, 1.0f);
        p1ScoreText.draw(Integer.toString(p1Score), 392, 708);
        p1ScoreText.endRendering();

        p2ScoreText.beginRendering(1024, 768);
        p2ScoreText.setColor(1.0f, 1.0f, 1.0f, 1.0f);
        p2ScoreText.draw(Integer.toString(p2Score), 582, 708);
        p2ScoreText.endRendering();

        playerOne(drawable);
        playerTwo(drawable);
        net(drawable);
        ball(drawable);

        cx += ctx;
        cy += cty;
    }

    /**
     * Renders Player One and updates the player's y-coordinate depending on user input.
     * @param drawable
     */
    public void playerOne(GLAutoDrawable drawable) {
        final GL2 gl = drawable.getGL().getGL2();
        
        if (p1IsMovingUp && y1 <= 0.85) {
        	y1 += movementVelocity;
        } else if (p1IsMovingDown && y1 >= -0.85) {
        	y1 -= movementVelocity;
        }
    
        gl.glBegin(GL2.GL_QUADS);
        gl.glVertex2f(-0.9f, -0.1f + y1);
        gl.glVertex2f(-0.9f, 0.1f + y1);
        gl.glVertex2f(-0.95f, 0.1f + y1);
        gl.glVertex2f(-0.95f, -0.1f + y1);
        gl.glEnd();
    }

    /**
     * Renders Player Two and updates the player's y-coordinate depending on user input.
     * @param drawable
     */
    public void playerTwo(GLAutoDrawable drawable) {
        final GL2 gl = drawable.getGL().getGL2();
        
        if (p2IsMovingUp && y2 <= 0.85) {
        	y2 += movementVelocity;
        } else if (p2IsMovingDown && y2 >= -0.85) {
        	y2 -= movementVelocity;
        }
        
        gl.glBegin(GL2.GL_QUADS);
        gl.glVertex2f(0.9f, -0.1f + y2);
        gl.glVertex2f(0.9f, 0.1f + y2);
        gl.glVertex2f(0.95f, 0.1f + y2);
        gl.glVertex2f(0.95f, -0.1f + y2);
        gl.glEnd();
    }

    /**
     * Renders the net that divides the court into two.
     * @param drawable
     */
    private void net(GLAutoDrawable drawable) {
        final GL2 gl = drawable.getGL().getGL2();

        float placement = 0.95f;

        for (int i = 0; i < 20; i++) {
            gl.glBegin(GL2.GL_QUADS);
            gl.glVertex2f(0.01f, 0.025f + placement);
            gl.glVertex2f(-0.01f, 0.025f + placement);
            gl.glVertex2f(-0.01f, -0.025f + placement);
            gl.glVertex2f(0.01f, -0.025f + placement);
            gl.glEnd();
            
            placement -= 0.1;
        }
    }

    /**
     * Renders the ball based on its current x and y-coordinates.
     * @param drawable
     */
    public void ball(GLAutoDrawable drawable) {
        final GL2 gl = drawable.getGL().getGL2();

        checkCollision();

        float radius = 0.02f;

        gl.glBegin(GL2.GL_POLYGON);
        for (int i = 0; i <= 180; i++) {
            double angle = 2 * ((Math.PI * i) / 180);
            double x = Math.cos(angle) * radius;
            double y = Math.sin(angle) * radius;
            gl.glVertex2d(x + cx, y + cy);
        }
        gl.glEnd();
    }

    /**
     * Checks if the ball has collided with either of the users' paddles or if the
     * ball has reached either players' score zone.
     */
    private void checkCollision() {

        if (cx > 0.95) {
            reset();
            p1Score += 1;
        }

        if (cx < -0.95) {
            reset();
            p2Score += 1;
        }

        if (cx <= -0.9 && ((cy >= -0.1f + y1) && (cy <= 0.1f + y1))) {
            ctx *= -1;
        }

        if (cx >= 0.9 && ((cy >= -0.1f + y2) && (cy <= 0.1f + y2))) {
            ctx *= -1;
        }

        if (cy >= 1 || cy <= -1) {
            cty *= -1;
        }
    }

    /**
     * Resets the ball's x and y-coordinates and each players' y-coordinates.
     * Used after every point scored.
     */
    private void reset() {
        cx = 0;
        cy = 0;
        y1 = 0;
        y2 = 0;
        randomDirection();
    }

    /**
     * If the game is over this method is called to display the game over screen and
     * to display the winner.
     */
    private void gameOver() {
        TextRenderer prompt = new TextRenderer(new Font("SansSerif", Font.BOLD, 72));
        String winner = "";

        if (p1Score == MAX_SCORE) {
            winner = "Player One";
        }
        else {
            winner = "Player Two";
        }

        String line1 = "Game Over!";
        String line2 = String.format("%s wins!", winner);

        prompt.beginRendering(1024, 768);
        prompt.setColor(1.0f, 1.0f, 1.0f, 1.0f);
        prompt.draw(line1, 300, 500);
        prompt.draw(line2, 200, 300);
        prompt.endRendering();
    }

    /**
     * Checks if either player has reached the winning score.
     * @return
     */
    private boolean checkGameOver() {
        if (p1Score == MAX_SCORE || p2Score == MAX_SCORE) {
            return true;
        }
        else {
            return false;
        }
    }

    /**
     * Randomly selects a direction for the ball to go.
     */
    private void randomDirection() {
        Random random = new Random();
        int direction = random.nextInt(3);

        if (direction == 0) {
            ctx = 0.01f;
            cty = 0.01f;
        }

        if (direction == 1) {
            ctx = -0.01f;
            cty = 0.01f;
        }

        if (direction == 2) {
            ctx = -0.01f;
            cty = -0.01f;
        }

        if (direction == 3) {
            ctx = 0.01f;
            cty = -0.01f;
        }

    }

    /**
     * This JOGL method is not used for this program.
     */
    public void reshape(GLAutoDrawable drawable, int i, int i1, int i2, int i3) {
    }
    
    /**
     * This JOGL method is not used for this program.
     */
    public void dispose(GLAutoDrawable drawable) {
    }

    public static void main(String[] args) {
        GLProfile glp = GLProfile.getDefault();
        GLCapabilities capabilities = new GLCapabilities(glp);
        GLCanvas canvas = new GLCanvas(capabilities);
        Pong pong = new Pong();
        canvas.addGLEventListener(pong);

        Frame frame = new Frame("Pong");
        frame.setSize(1024, 768);
        frame.setResizable(false);
        frame.add(canvas);
        frame.addKeyListener(new PlayerOneListener());
        frame.addKeyListener(new PlayerTwoListener());
        frame.setVisible(true);

        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                super.windowClosing(e);
                System.exit(0);
            }
        });

        FPSAnimator animator = new FPSAnimator(canvas, 60);
        animator.start();

    }

    /**
     * Key Listener for Player One's movement.
     */
    private static class PlayerOneListener implements KeyListener {
        @Override
        public void keyTyped(KeyEvent e) {
        }

        @Override
        public void keyPressed(KeyEvent e) {
            int key = e.getKeyCode();

            if (key == KeyEvent.VK_W && y1 <= 0.85) {
                p1IsMovingUp = true;
            }

            if (key == KeyEvent.VK_S && y1 >= -0.85) {
                p1IsMovingDown = true;
            }
        }

        @Override
        public void keyReleased(KeyEvent e) {
        	int key = e.getKeyCode();

            if (key == KeyEvent.VK_W) {
            	p1IsMovingUp = false;
            }

            if (key == KeyEvent.VK_S) {
                p1IsMovingDown = false;
            }
        }
    }

    /**
     * Key Listener for Player Two's movement.
     */
    private static class PlayerTwoListener implements KeyListener {
        @Override
        public void keyTyped(KeyEvent e) {
        }

        @Override
        public void keyPressed(KeyEvent e) {
            int key = e.getKeyCode();

            if (key == KeyEvent.VK_UP && y2 <= 0.85) {
            	p2IsMovingUp = true;
            }

            if (key == KeyEvent.VK_DOWN && y2 >= -0.85) {
                p2IsMovingDown = true;
            }
        }

        @Override
        public void keyReleased(KeyEvent e) {
        	int key = e.getKeyCode();

            if (key == KeyEvent.VK_UP) {
            	p2IsMovingUp = false;
            }

            if (key == KeyEvent.VK_DOWN) {
                p2IsMovingDown = false;
            }
        }
    }
}
