import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

public class Stage {
    public volatile int volatileMouseX;
    public volatile int volatileMouseY;

    public static int mouseHoverObjectId;
    public static int mouseHoverPointId;
    public static Input input;
    public static boolean pausedByToggle;
    public boolean pauseKeyPressed;
    public static boolean paused;

    //public double gravity;

    public String interactionMode;
    public int interactionStage;
    private Vec interactionMouseOffset;

    public static ArrayList<DrawingCanvas.CanvasMemory> recordedCanvas = new ArrayList<>();
    public static volatile boolean recordCanvas;
    public static volatile boolean recordedCanvasPlayback;
    public static volatile int iterationNumber;
    public static volatile int recordedCanvasIndex;

    public volatile int width;
    public volatile int height;
    public int scale;
    public JTextArea textArea;
    private DrawingCanvas canvas;  // Store canvas reference

    public double stepSize;
    public int maxStepsPerFrame;
    public int stepsOnFrame;
    public AtomicBoolean frameRendered;
    public static int maxFps;

    public static ArrayList<Object> objects;
    public static final java.lang.Object OBJECTS_LOCK = new java.lang.Object();
    public static final java.lang.Object DRAWN_LOCK = new java.lang.Object();

    public static class drawn {
        static int stageWidth;
        static int stageHeight;
        public static void SetVarsCauseImDumbAndDontKnowABetterWayToDoThis(int width, int height) {
            stageWidth = width;
            stageHeight = height;
        }
        public static ArrayList <Vec.coloredVec> points = new ArrayList<>();
        public static volatile ArrayList <Vec.coloredLine> lines = new ArrayList<>();
        //public static void addCenteredPoint(Vec.coloredVec coloredVec) { addCenteredPoint(coloredVec, 1); }
        //public static void addCenteredLine(Vec.coloredLine coloredLine) { addCenteredLine(coloredLine, 1); }
        public static void addCenteredPoint(Vec.coloredVec coloredVec) {
            Vec vec = coloredVec.vec;
            Color color = coloredVec.color;
            points.add(new Vec.coloredVec(vec.add((double) stageWidth /2, (double) stageHeight /2), color, coloredVec.size));
        }
        public static void addCenteredLine(Vec.coloredLine coloredLine) {
            Vec start = coloredLine.start;
            Vec end = coloredLine.end;
            Color color = coloredLine.color;
            lines.add(new Vec.coloredLine(start.add((double) stageWidth /2, (double) stageHeight /2), end.add((double) stageWidth /2, (double) stageHeight /2), color, coloredLine.width));
        }
    }

    public Stage(int width, int height, int scale, double stepSize, int maxStepsPerFrame, int fps) {
        this.width = width;
        this.height = height;
        this.scale = scale;
        this.stepSize = stepSize;
        this.maxStepsPerFrame = maxStepsPerFrame;
        maxFps = fps;
        stepsOnFrame = 0;
        initializePhysics();
        initializeCanvas(maxFps);
        input = new Input();
    }

    public void initializePhysics() {
        recordedCanvasIndex = -1;
        objects = ObjectHandler.createInitialObjects();
        mouseHoverObjectId = -1;
        interactionStage = 0;
        interactionMode = "none";
        interactionMouseOffset = new Vec(0, 0);
        //gravity = 1.81;
        //gravity = 0.0;
        paused = false;
        pauseKeyPressed = false;
    }

    public void initializeCanvas(int fps) {
        JFrame frame = new JFrame("test frame title");
        canvas = new DrawingCanvas(width, height, scale);
        frame.setSize(width, height);
        frame.add(canvas);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
        frame.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                width = canvas.getWidth();
                height = canvas.getHeight();

                // If you need to update any cached values:
                drawn.SetVarsCauseImDumbAndDontKnowABetterWayToDoThis(width, height);
            }
        });


        drawn.SetVarsCauseImDumbAndDontKnowABetterWayToDoThis(width, height);

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
        frame.addKeyListener(new KeyListener() {
            @Override
            public void keyTyped(KeyEvent e) {}
            @Override
            public void keyPressed(KeyEvent e) {
                input.keys.press(e.getKeyCode());
            }
            @Override
            public void keyReleased(KeyEvent e) {
                input.keys.release(e.getKeyCode());
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
        }), 0, (int) (1000/ (double) fps), TimeUnit.MILLISECONDS);
    }

    public void runSteps(int steps) {
        for (int i = 0; i < steps; i++) {
            checkPaused();
            if (input.keys.isPressed(KeyEvent.VK_O)) {
                TaterMath.wait(200);
            }
            if (input.keys.isPressed(KeyEvent.VK_I)) {
                TaterMath.wait(50);
            }
            //while (paused) {
            //    try {
            //        Thread.sleep(1);
            //    } catch (InterruptedException e) {
            //        e.printStackTrace();
            //    }
            //}
            //IO.println("");
            //IO.println(stepsOnFrame);
            stepsOnFrame++;
            if (!paused) { iterationNumber++; }
            stepPhysics(this.stepSize, paused);
            if (stepsOnFrame >= maxStepsPerFrame) {
                while (!frameRendered.get()) {
                    TaterMath.wait(1);
                }
            }
            //IO.println(stepsOnFrame);

        }
    }

    private void stepPhysics(double stepSize, boolean paused) {
        // Consume the render tick once so physics behavior does not diverge on non-rendered substeps.
        boolean newFrame = frameRendered.getAndSet(false);
        if (newFrame) {
            if (!paused) {
                synchronized (DRAWN_LOCK) {
                    drawn.points.clear();
                    drawn.lines.clear();
                }
            }
            stepsOnFrame = 0;
            input.mouse.updatePos(volatileMouseX, volatileMouseY);
            /*if (prevMouseX != mouseX) { prevMouseX = mouseX; }
            if (prevMouseY != mouseY) { prevMouseY = mouseY; }
            if (mouseX != volatileMouseX) {
                prevMouseX = mouseX;//h
                mouseX = volatileMouseX;
            }
            if (mouseY != volatileMouseY) {
                prevMouseY = mouseY;
                mouseY = volatileMouseY;
            }*/
        }

        input.mouse.updateStepChange();
        interactions(paused, newFrame); //also deletes old drawn lines and points, since this is the first object lock
        if (!paused) {
            worldForces(stepSize);
            applyVelocity(stepSize);
        }

        if (!paused) {
            synchronized (OBJECTS_LOCK) {
                for (Object obj : objects) {
                    if (obj.type.equals("polygon")) {
                        obj.updateRelativeCoordinates();
                    }
                }
                ObjectHandler.collisionCalcs(objects);
            }
            applyDeferredPosChanges();
        }
        input.mouse.oldLeft = input.mouse.left;
        input.mouse.oldRight = input.mouse.right;
        input.mouse.oldMiddle = input.mouse.middle;
    }

    private void applyDeferredPosChanges() {
        synchronized (OBJECTS_LOCK) {
            for (Object obj : objects) {
                if (obj.posChange.x != 0 || obj.posChange.y != 0) {
                    obj.pos = obj.pos.add(obj.posChange);
                    obj.posChange.set(0, 0);
                }
                if (obj.type.equals("polygon")) {
                    obj.boundingBox.polygonCalc(obj.rel, obj.pos);
                } else if (obj.type.equals("circle")) {
                    obj.boundingBox.circleCalc(obj.radius, obj.pos);
                }
            }
        }
    }

    private void checkPaused() {
        if (recordedCanvasPlayback) {
            if (recordedCanvasIndex == -1) {
                recordedCanvasIndex = 0;
            } else if (input.keys.isPressed(KeyEvent.VK_UP) && recordedCanvasIndex > 0) {
                recordedCanvasIndex--;
            } else if (input.keys.isPressed(KeyEvent.VK_DOWN) && recordedCanvasIndex < recordedCanvas.size() - 1) {
                recordedCanvasIndex++;
            } else if (input.keys.isPressed(KeyEvent.VK_LEFT) && recordedCanvasIndex > 0) {
                recordedCanvasIndex--;
                TaterMath.wait(100);
            } else if (input.keys.isPressed(KeyEvent.VK_RIGHT) && recordedCanvasIndex < recordedCanvas.size() - 1) {
                recordedCanvasIndex++;
                TaterMath.wait(100);
            }
        } else {
            recordedCanvasIndex = -1;
        }

        if (recordedCanvasIndex == -1) {
            if (input.keys.isPressed(KeyEvent.VK_P) && !pausedByToggle && !pauseKeyPressed) {
                pausedByToggle = true;
            } else if (input.keys.isPressed(KeyEvent.VK_P) && pausedByToggle && !pauseKeyPressed) {
                pausedByToggle = false;
            }
            pauseKeyPressed = input.keys.isPressed(KeyEvent.VK_P);
            paused = pausedByToggle || input.keys.isPressed(KeyEvent.VK_SPACE);
        } else {
            paused = true;
        }
        // Hold R to capture rendered frames into canvas memory.
    }

    private void interactions(boolean paused, boolean newFrame) {
        synchronized (OBJECTS_LOCK) {
            if (input.keys.isPressed(KeyEvent.VK_R)) {
                objects = ObjectHandler.createInitialObjects();
            }
            if (interactionMode.equals("none")) {
                if (!buttonInteractions()) {
                    int oldHoverId = mouseHoverObjectId;
                    mouseHoverObjectId = ObjectHandler.mouseHoveredObject(input.mouse.pos, objects);
                    if (input.keys.isPressed(KeyEvent.VK_SHIFT) && mouseHoverObjectId != -1) {
                        mouseHoverPointId = objects.get(mouseHoverObjectId).closestPoint(input.mouse.pos);
                    } else {
                        mouseHoverPointId = -1;
                    }
                    if (oldHoverId != -1 && oldHoverId != mouseHoverObjectId) {
                        objects.get(oldHoverId).outlined = false;
                    }
                    if (mouseHoverObjectId != -1 && oldHoverId != mouseHoverObjectId) {
                        objects.get(mouseHoverObjectId).outlined = true;
                    }
                    if (mouseHoverObjectId != -1 && input.mouse.left) {
                        interactionMode = "drag";
                        interactionStage = 1;
                        interactionMouseOffset = objects.get(mouseHoverObjectId).pos.sub(input.mouse.pos);
                        //} else if (!input.mouse.oldLeft && input.mouse.left && Button.trashcan.hovered(input.mouse.pos, new Vec(width, height))) {
                        //    objects = ObjectHandler.createInitialObjects();
                    }
                }
            } else if (interactionMode.equals("drag")) {
                if (interactionStage == 1) {
                    if (!input.mouse.left) {
                        interactionMode = "none";
                        interactionStage = 0;
                        return;
                    }
                    Object obj = objects.get(mouseHoverObjectId);
                    //if (newFrame) { obj.pos.increase(input.mouse.stepChange); }
                    if (newFrame) { obj.pos = input.mouse.pos.add(interactionMouseOffset); }
                    obj.vel = input.mouse.stepChange.div(stepSize); //i don't get why i have to divide by stepsize here but ok
                }
            }
        }
    }

    private boolean buttonInteractions() {
        if (!input.mouse.left || input.mouse.oldLeft) {
            return false;
        }
        return ScreenElements.Button.buttonClicked(input.mouse.pos, input.keys.isPressed(KeyEvent.VK_ESCAPE), new Vec(width, height));
    }

    private void worldForces(double stepSize) {
        synchronized (OBJECTS_LOCK) {
            for (Object obj : objects) {
                /*if (obj.mass != 0 && !(interactionMode.equals("drag") && obj.index == mouseHoverObjectId)) {
                    obj.vel.y += gravity * stepSize;
                }*/
                obj.vel = obj.vel.add(obj.setAcceleration);
            }
        }
    }

    private void applyVelocity(double stepSize) {
        synchronized (OBJECTS_LOCK) {
            for (Object obj : objects) {
                if (obj.velChange.x != 0 || obj.velChange.y != 0) {
                    obj.vel = obj.vel.add(obj.velChange);
                    obj.velChange.set(0, 0);
                }
                if (obj.angularVelChange != 0) {
                    obj.angularVel += obj.angularVelChange;
                    obj.angularVelChange = 0;
                }
            }

            for (Object obj : objects) {
                if (input.keys.isPressed(KeyEvent.VK_K)) {
                    if (mouseHoverObjectId == -1) {
                        obj.vel = new Vec(0, 0);
                        obj.angularVel = 0;
                    } else if (obj.index == mouseHoverObjectId) {
                        obj.vel = new Vec(0, 0);
                        obj.angularVel = 0;
                    }
                }
                if (!(interactionMode.equals("drag") && obj.index == mouseHoverObjectId)) {
                    obj.pos = obj.pos.add(obj.vel.mul(stepSize));
                }
                obj.rotation += obj.angularVel * stepSize;
            }
        }
    }
}