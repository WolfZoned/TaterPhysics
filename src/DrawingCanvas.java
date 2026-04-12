import java.awt.*;
import java.util.ArrayList;
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

    public static ArrayList<CanvasMemory> canvasMemories = new ArrayList<>();
    public static int lastIterationNumber = 0;

    public static class CanvasMemory {
        ArrayList<Vec.coloredLine> lines;
        ArrayList<Vec.coloredVec> points;
        ArrayList<Object> objects;
        int stepsOnFrame = 0;
        double mouseX;
        double mouseY;

        public CanvasMemory(ArrayList<Vec.coloredLine> lines, ArrayList<Vec.coloredVec> points, ArrayList<Object> objects, int stepsOnFrame, double mouseX, double mouseY) {
            this.lines = lines;
            this.points = points;
            this.objects = objects;
            this.stepsOnFrame = stepsOnFrame;
            this.mouseX = mouseX;
            this.mouseY = mouseY;
        }
    }

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
            drawDrawnVecs(g2d, Stage.drawn.lines, Stage.drawn.points);
        }
    }

    private void drawDrawnVecs(Graphics2D g2d, ArrayList<Vec.coloredLine> lines, ArrayList<Vec.coloredVec> points) {
        if (!(lines == null || lines.isEmpty())) {
            for (Vec.coloredLine line : lines) {
                g2d.setColor(line.color);
                g2d.setStroke(new BasicStroke(line.width));
                g2d.drawLine((int) line.start.x, (int) line.start.y, (int) line.end.x, (int) line.end.y);
            }
        }
        if (Options.GJK.render.drawOrigin) {
            g2d.setColor(Color.BLUE);
            g2d.fillOval(Stage.drawn.stageWidth / 2 - 2, Stage.drawn.stageHeight / 2 - 2, 4, 4);
        }
        if (!(points == null || points.isEmpty())) {
            for (Vec.coloredVec point : points) {
                g2d.setColor(point.color);
                g2d.fillOval((int) point.vec.x - 2 * point.size, (int) point.vec.y - 2 * point.size, 4 * point.size, 4 * point.size);
            }
        }
    }

    private static ArrayList<Vec.coloredLine> copyLines(ArrayList<Vec.coloredLine> source) {
        ArrayList<Vec.coloredLine> snapshot = new ArrayList<>();
        if (source == null) {
            return snapshot;
        }
        for (Vec.coloredLine line : source) {
            snapshot.add(new Vec.coloredLine(line.start.copy(), line.end.copy(), line.color, line.width));
        }
        return snapshot;
    }

    private static ArrayList<Vec.coloredVec> copyPoints(ArrayList<Vec.coloredVec> source) {
        ArrayList<Vec.coloredVec> snapshot = new ArrayList<>();
        if (source == null) {
            return snapshot;
        }
        for (Vec.coloredVec point : source) {
            snapshot.add(new Vec.coloredVec(point.vec.copy(), point.color, point.name, point.size));
        }
        return snapshot;
    }

    private static ArrayList<Object> copyObjects(ArrayList<Object> source) {
        ArrayList<Object> snapshot = new ArrayList<>();
        if (source == null) {
            return snapshot;
        }
        for (Object obj : source) {
            if (obj == null) {
                continue;
            }
            Object copy = obj.clone();
            snapshot.add(copy);
        }
        return snapshot;
    }

    @Override
    protected void paintComponent(Graphics g) {
        //super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;

        if (Stage.renderMemory == -1) {
            paintFrame(g2d, -1, Stage.objects, mouseX, mouseY, stepsOnFrame, null, null);
            if (Stage.rememberCanvas && Stage.iterationNumber != lastIterationNumber) {
                ArrayList<Object> objectSnapshot;
                ArrayList<Vec.coloredLine> lineSnapshot;
                ArrayList<Vec.coloredVec> pointSnapshot;
                synchronized (Stage.OBJECTS_LOCK) {
                    objectSnapshot = copyObjects(Stage.objects);
                }
                synchronized (Stage.DRAWN_LOCK) {
                    lineSnapshot = copyLines(Stage.drawn.lines);
                    pointSnapshot = copyPoints(Stage.drawn.points);
                }
                canvasMemories.add(new CanvasMemory(lineSnapshot, pointSnapshot, objectSnapshot, stepsOnFrame, mouseX, mouseY));
            }
            lastIterationNumber = Stage.iterationNumber;
        } else if (Stage.renderMemory >= canvasMemories.size()){
            paintFrame(g2d, canvasMemories.size(), Stage.objects, mouseX, mouseY, stepsOnFrame, null, null);
        } else {
            CanvasMemory memory = canvasMemories.get(Stage.renderMemory);
            paintFrame(g2d, Stage.renderMemory, memory.objects, memory.mouseX, memory.mouseY, memory.stepsOnFrame, memory.lines, memory.points);
        }

        if (!Stage.rememberCanvas && Stage.renderMemory == -1) {
            canvasMemories.clear();
        }
    }

    private void paintFrame(Graphics2D g2d, int memoryIndex, ArrayList<Object> objects, double mouseX, double mouseY, int stepsOnFrame, ArrayList<Vec.coloredLine> lines, ArrayList<Vec.coloredVec> points) {

        RenderingHints rh = new RenderingHints(
                RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHints(rh);

        objectRenderer1.renderObjects(g2d, objects);
        if (lines == null || points == null) {
            drawDrawnVecs(g2d);
        } else {
            drawDrawnVecs(g2d, lines, points);
        }

        // Optionally draw debug info directly on the canvas instead of using textArea
        g2d.setColor(Color.BLACK);
        g2d.drawString("Mouse X: " + mouseX + "   Mouse Y: " + mouseY, 10, 20);
        g2d.drawString("Rel Mouse X: " + (mouseX - (width / 2)) + "   Rel Mouse Y: " + (mouseY - (height / 2)), 10, 40);
        g2d.drawString(stepsOnFrame + " steps this frame out of max " + maxStepsPerFrame + " at " + Stage.maxFps + " (max) fps", 10, 60);
        int renderHeight = 0;
        if (Stage.mouseHoverObjectId != -1) {
            g2d.drawString("Mouse is hovering object " + Stage.mouseHoverObjectId, 10, 80);
            renderHeight += 20;
            if (Stage.mouseHoverPointId != -1) {
                g2d.drawString("Mouse is hovering point " + Stage.mouseHoverPointId, 10, 80 + renderHeight);
                renderHeight += 20;
            }
        }
        if (memoryIndex != -1) {
            g2d.drawString("Rendering memory from iteration " + memoryIndex, 10, 80 + renderHeight);
            renderHeight += 20;
        }
        Image image = new ImageIcon("src/trashcan.png").getImage();
        double imageWidth = image.getWidth(null);
        double imageHeight = image.getHeight(null);
        g2d.drawImage(image, width - 90, height - 135, (int) (imageWidth * 4), (int) (imageHeight * 4), null);
        //IO.println("rendered at size" + (int) (imageWidth * 4) + "   " + (int) (imageHeight * 4));
    }
}