import java.awt.*;
import java.awt.geom.*;

public class ObjectRenderer {
    private int width;
    private int height;
    private int scale;

    public ObjectRenderer(int w, int h, int s) {
        width = w;
        height = h;
        scale = s;
    }

    public void renderObjects(Graphics2D g2d) {
        AffineTransform originalTransform = g2d.getTransform();
        // Synchronize to prevent concurrent modification while iterating
        synchronized (Stage.OBJECTS_LOCK) {
            for (Object obj : Stage.objects) {
                renderObject(g2d, obj);
                g2d.setTransform(originalTransform);
            }
        }
    }

    private void renderObject(Graphics2D g2d, Object obj) {
        if (obj == null) {
            // Do nothing
        } else if (obj.type.equals("polygon")) {
            drawPolygon(g2d, obj.color, obj.rel, obj.pos, obj.outlined, obj.boundingBox, obj.boundingBox.boxColor);
        }
    }

    private void drawPolygon(Graphics2D g2d, Color color, Vec[] rel, Vec pos, boolean outlined, Object.BoundingBox bb, Color debugColor) {
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
        Point2D.Double center = new Point2D.Double(pos.x, pos.y);
        g2d.setColor(new Color(0, 0, 0, 255));
        g2d.fill(new Ellipse2D.Double(center.x - 2, center.y - 2, 4, 4));

        Path2D.Double box = new Path2D.Double();
        box.moveTo(bb.xMin, bb.yMin);
        box.lineTo(bb.xMax, bb.yMin);
        box.lineTo(bb.xMax, bb.yMax);
        box.lineTo(bb.xMin, bb.yMax);
        box.closePath();
        g2d.setColor(debugColor);
        g2d.draw(box);
    }
    /*private void drawRectangle(Graphics2D g2d, double x, double y, double w, double h, double rotation) {
        Rectangle2D.Double r = new Rectangle2D.Double(x, y, w, h);
        g2d.rotate(rotation, x + w / 2, y + h / 2);
        g2d.fill(r);
    }*/
}
