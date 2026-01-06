import javax.swing.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.util.ArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

public class Stage {
    public volatile int volatileMouseX;
    public volatile int volatileMouseY;

    public int mouseHoverObjectId;
    public Input input;

    public double gravity;

    public String interactionMode;
    public int interactionStage;

    public int width;
    public int height;
    public int scale;
    public JTextArea textArea;
    private DrawingCanvas canvas;  // Store canvas reference

    public double stepSize;
    public int maxStepsPerFrame;
    public int stepsOnFrame;
    public AtomicBoolean frameRendered;

    public static ArrayList<Object> objects;
    public static final java.lang.Object OBJECTS_LOCK = new java.lang.Object();

    public Stage(int width, int height, int scale, double stepSize, int maxStepsPerFrame) {
        this.width = width;
        this.height = height;
        this.scale = scale;
        this.stepSize = stepSize;
        this.maxStepsPerFrame = maxStepsPerFrame;
        stepsOnFrame = 0;
        initializePhysics();
        initializeCanvas();
        input = new Input();
    }

    public void initializePhysics() {
        objects = ObjectHandler.createInitialObjects();
        mouseHoverObjectId = -1;
        interactionStage = 0;
        interactionMode = "none";
        gravity = 1.81;
    }

    public void initializeCanvas() {
        JFrame frame = new JFrame("test frame title");
        canvas = new DrawingCanvas(width, height, scale);
        frame.setSize(width, height);
        frame.add(canvas);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);

        frame.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                // You can check which button was pressed:
                if (e.getButton() == MouseEvent.BUTTON1) {
                    input.mouse.left = true;
                } else if (e.getButton() == MouseEvent.BUTTON2) {
                    input.mouse.middle = true;
                } else if (e.getButton() == MouseEvent.BUTTON3) {
                    input.mouse.right = true;
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                if (e.getButton() == MouseEvent.BUTTON1) {
                    input.mouse.left = false;
                } else if (e.getButton() == MouseEvent.BUTTON2) {
                    input.mouse.middle = false;
                } else if (e.getButton() == MouseEvent.BUTTON3) {
                    input.mouse.right = false;
                }
            }
        });
        frame.addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                if (e.getX() != volatileMouseX) {
                    volatileMouseX = e.getX();
                }
                if (e.getY()-28 != volatileMouseY) {
                    volatileMouseY = e.getY()-28; //account for title bar
                }
            }
            @Override
            public void mouseDragged(MouseEvent e) {
                if (e.getX() != volatileMouseX) {
                    volatileMouseX = e.getX();
                }
                if (e.getY()-28 != volatileMouseY) {
                    volatileMouseY = e.getY()-28; //account for title bar
                }
            }
        });

        ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
        frameRendered = new AtomicBoolean(false);
        //executor.scheduleAtFixedRate(() -> drawFrame(canvas), 0, 16, TimeUnit.MILLISECONDS);
        executor.scheduleAtFixedRate(() -> SwingUtilities.invokeLater(() -> {
            // Update display data before repainting
            canvas.updateDisplayData(volatileMouseX, volatileMouseY, stepsOnFrame, maxStepsPerFrame);
            canvas.repaint();                 // runs on EDT
            frameRendered.set(true);       // safe publish to other threads
        }), 0, 16, TimeUnit.MILLISECONDS);
    }

    public void runSteps(int steps) {
        for (int i = 0; i < steps; i++) {
            stepsOnFrame++;
            stepPhysics(this.stepSize);
            frameRendered.set(false); //be careful because a frame could theoretically be rendered before this is set to false
            if (stepsOnFrame >= maxStepsPerFrame) {
                while (!frameRendered.get()) {
                    try {
                        Thread.sleep(1);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }

        }
    }

    private void stepPhysics(double stepSize) {
        if (frameRendered.get()) {
            if (frameRendered.get()) {
                stepsOnFrame = 0;
                input.mouse.updatePos(volatileMouseX, volatileMouseY);
                /*if (prevMouseX != mouseX) { prevMouseX = mouseX; }
                if (prevMouseY != mouseY) { prevMouseY = mouseY; }
                if (mouseX != volatileMouseX) {
                    prevMouseX = mouseX;
                    mouseX = volatileMouseX;
                }
                if (mouseY != volatileMouseY) {
                    prevMouseY = mouseY;
                    mouseY = volatileMouseY;
                }*/
            }
            input.mouse.updateStepChange();
            interactions();
            worldForces(stepSize);
            applyVelocity(stepSize);
        }

        synchronized (OBJECTS_LOCK) {
            for (Object obj : objects) {
                obj.updateRelativeCoordinates();
            }
            ObjectHandler.collisionCalcs(objects);
        }
    }

    private void interactions() {
        synchronized (OBJECTS_LOCK) {
            if (interactionMode.equals("none")) {
                int oldHoverId = mouseHoverObjectId;
                mouseHoverObjectId = ObjectHandler.mouseHoveredObject(input.mouse.pos, objects);
                if (oldHoverId != -1 && oldHoverId != mouseHoverObjectId) {
                    objects.get(oldHoverId).outlined = false;
                }
                if (mouseHoverObjectId != -1 && oldHoverId != mouseHoverObjectId) {
                    objects.get(mouseHoverObjectId).outlined = true;
                }
                if (mouseHoverObjectId != -1 && input.mouse.left) {
                    interactionMode = "drag";
                    interactionStage = 1;
                }
            } else if (interactionMode.equals("drag")) {
                if (interactionStage == 1) {
                    if (!input.mouse.left) {
                        interactionMode = "none";
                        interactionStage = 0;
                        return;
                    }
                    Object obj = objects.get(mouseHoverObjectId);
                    obj.pos.increase(input.mouse.stepChange);
                    obj.vel = input.mouse.stepChange.div(stepSize); //i don't get why i have to divide by stepsize here but ok
                }
            }
        }
    }

    private void worldForces(double stepSize) {
        synchronized (OBJECTS_LOCK) {
            for (Object obj : objects) {
                if (obj.mass != 0 && !(interactionMode.equals("drag") && obj.index == mouseHoverObjectId)) {
                    obj.vel.y += gravity * stepSize;
                }
            }
        }
    }

    private void applyVelocity(double stepSize) {
        synchronized (OBJECTS_LOCK) {
            for (Object obj : objects) {
                if (!(interactionMode.equals("drag") && obj.index == mouseHoverObjectId)) {
                    obj.pos = obj.pos.add(obj.vel.mul(stepSize));
                }
                obj.rotation += obj.angularVel * stepSize;
            }
        }
    }
}