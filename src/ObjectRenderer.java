import java.awt.*;
import java.awt.geom.*;
import java.util.ArrayList;

public class ObjectRenderer {
    private int width;
    private int height;
    private int scale;

    public ObjectRenderer(int w, int h, int s) {
        width = w;
        height = h;
        scale = s;
    }

    public void renderObjects(Graphics2D g2d, ArrayList<Object> objects) {
        AffineTransform originalTransform = g2d.getTransform();
        // Synchronize to prevent concurrent modification while iterating
        synchronized (Stage.OBJECTS_LOCK) {
            for (Object obj : objects) {
                renderObject(g2d, obj);
                g2d.setTransform(originalTransform);
            }
            for (Object ghost : ScreenElements.GhostObject.ghostObjects) {
                renderObject(g2d, ghost);
                g2d.setTransform(originalTransform);
            }
            for (Object obj : objects) {
                g2d.setColor(new Color(67, 67, 67, 255));
                g2d.setStroke(new BasicStroke(1));
                g2d.drawLine((int) obj.pos.x, (int) obj.pos.y, (int) (obj.pos.x + new Vec(20,20).rotate(obj.rotation).x), (int) (obj.pos.y + new Vec(20,20).rotate(obj.rotation).y));
            }
            for (Object ghost : ScreenElements.GhostObject.ghostObjects) {
                g2d.setColor(new Color(0, 0, 0, 255));
                g2d.setStroke(new BasicStroke(1));
                g2d.drawLine((int) ghost.pos.x, (int) ghost.pos.y, (int) (ghost.pos.x + new Vec(20,20).rotate(ghost.rotation).x), (int) (ghost.pos.y + new Vec(20,20).rotate(ghost.rotation).y));
            }
            for (Object obj : objects) {
                Point2D.Double center = new Point2D.Double(obj.pos.x, obj.pos.y);
                g2d.setColor(new Color(0, 0, 0, 255));
                g2d.fill(new Ellipse2D.Double(center.x - 2, center.y - 2, 4, 4));
            }
            for (Object ghost : ScreenElements.GhostObject.ghostObjects) {
                Point2D.Double center = new Point2D.Double(ghost.pos.x, ghost.pos.y);
                g2d.setColor(new Color(0, 0, 0, 255));
                g2d.fill(new Ellipse2D.Double(center.x - 2, center.y - 2, 4, 4));
            }
        }
    }

    private void renderObject(Graphics2D g2d, Object obj) {
        if (obj == null) {
            // Do nothing
        } else if (obj.type.equals("polygon")) {
            drawBoundingBox(g2d, obj.boundingBox, obj.boundingBox.boxColor);
            drawPolygon(g2d, obj.color, obj.rel, obj.pos, obj.outlined);
            if (obj.index == Stage.mouseHoverObjectId && Stage.mouseHoverPointId != -1) {
                g2d.setColor(new Color(56, 65, 174));
                g2d.fillOval((int) obj.pos.add(obj.rel[Stage.mouseHoverPointId]).x - 5, (int) obj.pos.add(obj.rel[Stage.mouseHoverPointId]).y - 5, 10, 10);
            }
        } else if  (obj.type.equals("circle")) {
            drawBoundingBox(g2d, obj.boundingBox, obj.boundingBox.boxColor);
            drawCircle(g2d, obj.color, obj.radius, obj.pos);
        }
    }

    private void drawBoundingBox(Graphics2D g2d, Object.BoundingBox bb, Color debugColor) {
        if (Options.AABB.render) {
            Path2D.Double box = new Path2D.Double();
            box.moveTo(bb.xMin, bb.yMin);
            box.lineTo(bb.xMax, bb.yMin);
            box.lineTo(bb.xMax, bb.yMax);
            box.lineTo(bb.xMin, bb.yMax);
            box.closePath();
            g2d.setColor(debugColor);
            g2d.draw(box);
        }
    }

    private void drawPolygon(Graphics2D g2d, Color color, Vec[] rel, Vec pos, boolean outlined) {
        Path2D.Double p = new Path2D.Double();
        for (int i = 0; i < rel.length; i++) {
            if (i == 0) {
                p.moveTo(rel[i].x + pos.x, rel[i].y + pos.y);
            } else {
                p.lineTo(rel[i].x + pos.x, rel[i].y + pos.y);
            }
        }
        p.closePath();
        g2d.setColor(color);
        g2d.fill(p);
        if (outlined) {
            g2d.setStroke(new BasicStroke(4f));
            g2d.setColor(new Color(51, 51, 51, 255));
            g2d.draw(p);
            g2d.setStroke(new BasicStroke(1f));
        }
    }

    private void drawCircle(Graphics2D g2d, Color color, double radius, Vec pos) {
        g2d.setColor(color);
        g2d.fill(new Ellipse2D.Double(pos.x - radius, pos.y - radius, 2 * radius, 2 * radius));
    }
    /*private void drawRectangle(Graphics2D g2d, double x, double y, double w, double h, double rotation) {
        Rectangle2D.Double r = new Rectangle2D.Double(x, y, w, h);
        g2d.rotate(rotation, x + w / 2, y + h / 2);
        g2d.fill(r);
    }*/
}
