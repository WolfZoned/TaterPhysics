import java.awt.*;
import java.awt.event.KeyEvent;
import javax.swing.*;
public class DrawingCanvas extends JComponent {


    private int width;
    private int height;
    private int scale;
    private ObjectRenderer objectRenderer1;

    // Store display data internally instead of accessing Stage
    public int mouseX = 0;
    public int mouseY = 0;
    public int stepsOnFrame = 0;
    public int maxStepsPerFrame = 0;

    public DrawingCanvas(int w, int h, int s) {
        scale = s;
        width = w;
        height = h;
        objectRenderer1 = new ObjectRenderer(width, height, scale);
    }

    // Method to update display data from Stage (called before repaint)
    public void updateDisplayData(int mouseX, int mouseY, int stepsOnFrame, int maxStepsPerFrame) {
        this.mouseX = mouseX;
        this.mouseY = mouseY;
        this.stepsOnFrame = stepsOnFrame;
        this.maxStepsPerFrame = maxStepsPerFrame;
    }

    public void drawDrawnVecs(Graphics2D g2d) {
        synchronized (Stage.DRAWN_LOCK) {
            if (!(Stage.drawn.lines == null || Stage.drawn.lines.isEmpty())) {
                for (Vec.coloredLine line : Stage.drawn.lines) {
                    g2d.setColor(line.color);
                    g2d.setStroke(new BasicStroke(line.width));
                    g2d.drawLine((int) line.start.x, (int) line.start.y, (int) line.end.x, (int) line.end.y);
                }
            }
            g2d.setColor(Color.BLUE);
            g2d.fillOval(Stage.drawn.stageWidth / 2 - 2, Stage.drawn.stageHeight / 2 - 2, 4, 4);
            if (!(Stage.drawn.points == null || Stage.drawn.points.isEmpty())) {
                for (Vec.coloredVec point : Stage.drawn.points) {
                    g2d.setColor(point.color);

                    g2d.fillOval((int) point.vec.x - 2*point.size, (int) point.vec.y - 2*point.size, 4*point.size, 4*point.size);
                }
            }
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;

        RenderingHints rh = new RenderingHints(
                RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHints(rh);

        objectRenderer1.renderObjects(g2d);
        drawDrawnVecs(g2d);

        // Optionally draw debug info directly on the canvas instead of using textArea
        g2d.setColor(Color.BLACK);
        g2d.drawString("Mouse X: " + mouseX + "   Mouse Y: " + mouseY, 10, 20);
        g2d.drawString("Rel Mouse X: " + (mouseX-(width/2)) + "   Rel Mouse Y: " + (mouseY-(height/2)), 10, 40);
        g2d.drawString(stepsOnFrame + " steps this frame out of max " + maxStepsPerFrame + " at " + Stage.maxFps + " (max) fps", 10, 60);
        if (Stage.mouseHoverObjectId != -1) {
            g2d.drawString("Mouse is hovering object " + Stage.mouseHoverObjectId, 10, 80);
            if (Stage.mouseHoverPointId != -1) {
                g2d.drawString("Mouse is hovering point " + Stage.mouseHoverPointId, 10, 100);
            }
        }
    }
}