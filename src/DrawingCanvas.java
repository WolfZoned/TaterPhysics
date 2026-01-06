import java.awt.*;
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

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;

        RenderingHints rh = new RenderingHints(
                RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHints(rh);

        // Render objects
        objectRenderer1.renderObjects(g2d);

        // Optionally draw debug info directly on the canvas instead of using textArea
        g2d.setColor(Color.BLACK);
        g2d.drawString("Mouse X: " + mouseX + "   Mouse Y: " + mouseY, 10, 20);
        g2d.drawString(stepsOnFrame + " steps this frame out of max " + maxStepsPerFrame, 10, 40);
    }
}
