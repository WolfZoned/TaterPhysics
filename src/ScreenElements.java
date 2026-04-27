import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Objects;


public class ScreenElements {
    public static void render(Graphics2D g2d, Vec stageSize) {
        Button.renderButtons(g2d, stageSize);
    }
    public static class GhostObject {
        public static ArrayList<Object> ghostObjects = new ArrayList<>();
        public static Object objectConstructor;

        public static void createRectangleObjectConstructor(Color color, Vec pos, Vec size, double rotation) {
            objectConstructor = Object.createRectangle(color, pos.x, pos.y, rotation, size.x, size.y, new Vec(), false, false);
            ghostObjects.add(objectConstructor);
        }

        public static void createPolygonObjectConstructor(Color color, Vec pos, Vec size, int points, double rotation) {
            objectConstructor = Object.createPolygon(color, pos.x, pos.y, rotation, size.x, size.y, points, new Vec(), false, false);
            ghostObjects.add(objectConstructor);
        }

        public static void createCircleObjectConstructor(Color color, Vec pos, double radius, double rotation) {
            objectConstructor = Object.createCircle(color, pos.x, pos.y, rotation, radius, new Vec(), false, false);
            ghostObjects.add(objectConstructor);
        }

        public static void createDrawnObjectConstructor(Color color, Vec pos, double rotation, Vec[] points) {
            objectConstructor = Object.createCustom(color, pos.x, pos.y, rotation, points, new Vec(), false, false);
            ghostObjects.add(objectConstructor);
        }
    }

    public static class shapeCreationShit {
        public static ArrayList<Vec> drawnPoints = new ArrayList<>();
        //public static String shapeType;
        private static Object initializedShape;
        //public static void initializePolygon(String shapeType) {
        //    initializedShape = Object.createPolygon(Options.colors.randomColor(), 0, 0, 0, 30, 30, drawnPoints.size(), new Vec(), false, false);
        //}
            //else if (shapeType.equals("circle")) {

            //} else if (shapeType.equals("draw")) {

            //}


        public static void createShape(String shapeType, Vec stageSize) {
            if (shapeType.equals("polygon")) {
                //create polygon with polygonPoints
                Stage.objects.add(Object.createRectangle(Options.colors.randomColor(), stageSize.div(2).x, stageSize.div(2).y, 0.0, 30, 30, new Vec(0, 0.4), false, false));
            } else if (shapeType.equals("circle")) {
                //create circle with polygonPoints.get(0) as center and distance to polygonPoints.get(1) as radius
                Stage.objects.add(Object.createCircle(Options.colors.randomColor(), stageSize.div(2).x, stageSize.div(2).y, 0, 30, new Vec(0, 0.3), false, false));
            } else if (shapeType.equals("drawn")) {
                //create custom shape with polygonPoints as points
                Vec[] pointsArray = drawnPoints.toArray(new Vec[0]);
                if (!TaterMath.isConvex(pointsArray)) {
                    IO.println("Error: Drawn polygon must be convex.");
                    drawnPoints.clear();
                    return;
                }
                if (TaterMath.isClockwise(pointsArray)) {
                    //reverse points to be counterclockwise
                    for (int i = 0; i < pointsArray.length / 2; i++) {
                        Vec temp = pointsArray[i];
                        pointsArray[i] = pointsArray[pointsArray.length - 1 - i];
                        pointsArray[pointsArray.length - 1 - i] = temp;
                    }
                }
                Stage.objects.add(Object.createCustom(Options.colors.randomColor(), stageSize.div(2).x, stageSize.div(2).y, 0, pointsArray, new Vec(0, 0.3), false, false));
                drawnPoints.clear();
            } else {
                IO.println("Error: Invalid shape type");
            }
        }
    }

    public static class Button {
        public Image image;
        public volatile boolean rendered;
        private final boolean leftAligned;
        private final boolean topAligned;
        private final boolean horizontalCenterAlligned;
        private int scale;
        private final Vec pos;
        private final Vec size;
        private static ArrayList<Button> buttons = new ArrayList<>();

        private static final int buttonSpacing = 100;
        private static final int smallButtonSpacing = 70;

        public static String addMode = "none";

        //public static Button trashcan = new Button("trashcan", new Vec(90, 135), 4, false, false);
        //public static Button add = new Button("add", new Vec(20, 115), 4, true, false);
        public static Button add = new Button("shape/add", new Vec(20, 115), 4, true, true, false);

        public static Button circleAdd = new Button("shape/circle", new Vec(20, 115 + buttonSpacing), 4, false, true, false);
        public static Button rectangleAdd = new Button("shape/rectangle", new Vec(20, 115 + buttonSpacing * 2), 4, false, true, false);
        public static Button drawAdd = new Button("shape/draw", new Vec(20, 115 + buttonSpacing * 3), 4, false, true, false);

        public static Button cancel = new Button("cancel", new Vec(20, 115), 4, false, true, false);
        public static Button checkmark = new Button("checkmark", new Vec(20, 115 + buttonSpacing), 4, false, true, false);

        public static Button pause = new Button("play", new Vec(110 + buttonSpacing, 115), 4, false, false, false);
        public static Button play = new Button("pause", new Vec(110 + buttonSpacing, 115), 4, true, false, false);


        //public static Button circleAdd = new Button("circle", new Vec(20, 115 + addButtonSpacing), 4, false, true, false);
        //public static Button rectangleAdd = new Button("stop", new Vec(20, 115 + addButtonSpacing * 2), 4, false, true, false);
        //public static Button drawAdd = new Button("draw", new Vec(20, 115 + addButtonSpacing * 3), 4, false, true, false);


        public static Button startRecording = new Button("recording/record", new Vec(110, 115), 4, true, false, false);
        public static Button stopRecording = new Button("recording/stop", new Vec(110, 115), 4, false, false, false);
        public static Button playRecording = new Button("recording/playVideo", new Vec(110, 115 + buttonSpacing), 4, false, false, false);

        public static Button leftArrow = new Button("arrows/leftArrow", new Vec(-smallButtonSpacing, 115), 4, true);
        public static Button leftArrowPressed = new Button("arrows/leftArrowPressed", new Vec(-smallButtonSpacing, 115), 4, true);
        public static Button upArrow = new Button("arrows/upArrow", new Vec(0, 115 + smallButtonSpacing), 4, true);
        public static Button upArrowPressed = new Button("arrows/upArrowPressed", new Vec(0, 115 + smallButtonSpacing), 4, true);
        public static Button rightArrow = new Button("arrows/rightArrow", new Vec(smallButtonSpacing, 115), 4, true);
        public static Button rightArrowPressed = new Button("arrows/rightArrowPressed", new Vec(smallButtonSpacing, 115), 4, true);
        public static Button downArrow = new Button("arrows/downArrow", new Vec(0, 115), 4, true);
        public static Button downArrowPressed = new Button("arrows/downArrowPressed", new Vec(0, 115), 4, true);

        public Button(String imageName, Vec pos, int size, boolean rendered, boolean leftAligned, boolean topAligned) {
            this.rendered = rendered;
            this.image = new ImageIcon("src/images/" + imageName + ".png").getImage();
            this.pos = pos;
            this.size = new Vec(this.image.getWidth(null) * size, this.image.getHeight(null) * size);
            this.leftAligned = leftAligned;
            this.topAligned = topAligned;
            this.horizontalCenterAlligned = false;
            this.scale = size;
            buttons.add(this);
        }

        public Button(String imageName, Vec pos, int size, boolean rendered) {
            this.rendered = rendered;
            this.image = new ImageIcon("src/images/" + imageName + ".png").getImage();
            this.pos = pos;
            this.size = new Vec(this.image.getWidth(null) * size, this.image.getHeight(null) * size);
            this.leftAligned = false;
            this.topAligned = false;
            this.horizontalCenterAlligned = true;
            this.scale = size;
            buttons.add(this);
        }

        public void render(Graphics2D g2d, Vec stageSize) {
            if (rendered) {
                Vec screenPos = calcScreenPos(stageSize);
                g2d.drawImage(image, (int) screenPos.x, (int) screenPos.y, (int) size.x, (int) size.y, null);
            }
        }

        public boolean hovered(Vec mousPos, Vec stageSize, boolean worksWhileInvisible) {
            //IO.println("Checking hover for button at " + pos + " with size " + size + " against mouse position " + mousPos);
            Vec screenPos = calcScreenPos(stageSize);
            return TaterMath.isPointInsideBox(mousPos, screenPos, screenPos.add(size)) && (rendered || worksWhileInvisible);
        }

        public boolean hovered(Vec mousPos, Vec stageSize) {
            return hovered(mousPos, stageSize, false);
        }

        private Vec calcScreenPos(Vec stageSize) {
            if (horizontalCenterAlligned) {
                return new Vec(
                    stageSize.x / 2 - size.x / 2 + pos.x,
                    stageSize.y - pos.y
                );
            } else {
                return new Vec(
                    leftAligned ? pos.x : stageSize.x - pos.x,
                    topAligned ? pos.y : stageSize.y - pos.y
                );
            }
        }

        public static void renderButtons(Graphics2D g2d, Vec stageSize) {
            leftArrow.rendered = false; leftArrowPressed.rendered = false;  rightArrow.rendered = false; rightArrowPressed.rendered = false; upArrow.rendered = false; upArrowPressed.rendered = false; downArrow.rendered = false; downArrowPressed.rendered = false;
            if (Stage.recordCanvas) {
                startRecording.rendered = false;
                stopRecording.rendered = true;
                if (Stage.recordedCanvasPlayback) {
                    playRecording.rendered = false;
                    leftArrow.rendered = true; rightArrow.rendered = true; upArrow.rendered = true; downArrow.rendered = true;
                    leftArrowPressed.rendered = Stage.input.keys.isPressed(KeyEvent.VK_LEFT); rightArrowPressed.rendered = Stage.input.keys.isPressed(KeyEvent.VK_RIGHT); upArrowPressed.rendered = Stage.input.keys.isPressed(KeyEvent.VK_UP); downArrowPressed.rendered = Stage.input.keys.isPressed(KeyEvent.VK_DOWN);
                } else {
                    playRecording.rendered = true;
                }
            } else {
                startRecording.rendered = true;
                stopRecording.rendered = false;
                playRecording.rendered = false;
            }
            if (Stage.paused) {
                pause.rendered = true;
                play.rendered = false;
            } else {
                pause.rendered = false;
                play.rendered = true;
            }
            if (Button.buttons == null) {
                return;
            }
            for (Button button : buttons) {
                button.render(g2d, stageSize);
            }
        }

        public static boolean buttonClicked(Vec mousePos, boolean escapePressed, Vec stageSize) { //happens every click, not guarenteed to actually touch a button
            if (startRecording.hovered(mousePos, stageSize, true)) { //start and stope are same size, so we can always check start instead of swapping.
                if (Stage.recordCanvas) {
                    Stage.recordedCanvasPlayback = false;
                    Stage.recordCanvas = false;
                    //sets Stage.objects to the currently selected recorded frame
                    if (Stage.recordedCanvasIndex != -1 && Stage.recordedCanvasIndex < Stage.recordedCanvas.size()) {
                        Stage.objects = Stage.recordedCanvas.get(Stage.recordedCanvasIndex).objects;
                    }
                } else {
                    Stage.recordCanvas = true;
                }
            }
            if (playRecording.hovered(mousePos, stageSize)) {
                if (!Stage.recordedCanvas.isEmpty()) {
                    Stage.recordedCanvasPlayback = true;
                }
            }
            if (pause.hovered(mousePos, stageSize, true)) { //pause and play are same size, so we can always check pause instead of swapping.
                if (Stage.paused) {
                    Stage.paused = false;
                    Stage.pausedByToggle = false;
                } else {
                    Stage.paused = true;
                    Stage.pausedByToggle = true;
                }
            }
            if (addMode.equals("none")) {
                if (add.hovered(mousePos, stageSize)) {
                    addMode = "select";
                    add.rendered = false;
                    circleAdd.rendered = true;
                    rectangleAdd.rendered = true;
                    drawAdd.rendered = true;
                    cancel.rendered = true;
                    checkmark.rendered = false;
                    return true;
                }
            } else if (addMode.equals("select")) {
                if (escapePressed || cancel.hovered(mousePos, stageSize)) {
                    addMode = "none";
                    add.rendered = true;
                    circleAdd.rendered = false;
                    rectangleAdd.rendered = false;
                    drawAdd.rendered = false;
                    cancel.rendered = false;
                    checkmark.rendered = false;
                    checkmark.rendered = false;
                    return false;
                }
                if (circleAdd.hovered(mousePos, stageSize)) {
                    if (Objects.equals(Stage.interactionMode, "none")) {
                        //addMode = "circle";
                        shapeCreationShit.createShape("circle", stageSize);
                        addMode = "none";
                        circleAdd.rendered = false;
                        rectangleAdd.rendered = false;
                        drawAdd.rendered = false;
                        checkmark.rendered = false;
                        cancel.rendered = false;
                        add.rendered = true;
                        return true;
                    }
                }
                if (rectangleAdd.hovered(mousePos, stageSize)) {
                    if (Objects.equals(Stage.interactionMode, "none")) {
                        //addMode = "polygon";
                        shapeCreationShit.createShape("polygon", stageSize);
                        addMode = "none";
                        circleAdd.rendered = false;
                        rectangleAdd.rendered = false;
                        drawAdd.rendered = false;
                        checkmark.rendered = false;
                        cancel.rendered = false;
                        add.rendered = true;
                        return true;
                    }
                }
                if (drawAdd.hovered(mousePos, stageSize)) {
                    if (Objects.equals(Stage.interactionMode, "none")) {
                        Stage.interactionMode = "drawPolygonPoints";
                        addMode = "draw";
                        circleAdd.rendered = false;
                        rectangleAdd.rendered = false;
                        drawAdd.rendered = false;
                        checkmark.rendered = true;
                        return true;
                    }
                }
            //} else if (addMode.equals("circle") && (checkmark.hovered(mousePos, stageSize) || cancel.hovered(mousePos, stageSize))) {
            //    if (checkmark.hovered(mousePos, stageSize)) {
            //        //GhostObject.createCircleObjectConstructor(new Color(125, 125, 125), stageSize.div(2), 30, 0);
            //        Stage.objects.add(Object.createCircle(Options.colors.randomColor(), stageSize.div(2).x, stageSize.div(2).y, 0, 30, new Vec(0, 0.3), false, false));
            //    }
            //    addMode = "none";
            //    add.rendered = true;
            //    cancel.rendered = false;
            //    checkmark.rendered = false;
            //    return true;
            //} else if (addMode.equals("polygon") && (checkmark.hovered(mousePos, stageSize, false) || cancel.hovered(mousePos, stageSize))) {
            //    if (checkmark.hovered(mousePos, stageSize)) {
            //        //GhostObject.createPolygonObjectConstructor(new Color(125, 125, 125), stageSize.div(2), new Vec(30, 30), 3, 0);
            //        //Stage.objects.add(Object.createRectangle(Options.colors.randomColor() , 450, 550, 0.0, 30, 30, Stage.objects.size(), new Vec(0, 0.4), false, false));
            //        //Stage.interactionMode = "addPolygon";
            //    }
            //    addMode = "none";
            //    add.rendered = true;
            //    cancel.rendered = false;
            //    checkmark.rendered = false;
            //    return true;
            //} else if (addMode.equals("draw")) {
            //    if (checkmark.hovered(mousePos, stageSize)) {
            //        shapeCreationShit.createShape("drawn", stageSize);
            //    }
            //    addMode = "none";
            //    Button.add.rendered = true;
            //    Button.cancel.rendered = false;
            //    checkmark.rendered = false;
            //    //GhostObject.createDrawnObjectConstructor(new Color(125, 125, 125), stageSize.div(2), 30, 0);
            //    return true;
            }
            return false;
        }
    }
}
