import java.awt.*;
import java.util.concurrent.ThreadLocalRandom;

public class Options {
    public static class colors {
        public static Color RED = new Color(255, 20, 20, 255);
        public static Color DARK_RED = new Color(125, 0, 0, 255);
        public static Color DARK_GREEN = new Color(0, 100, 0, 255);
        public static Color LIGHT_GREEN = new Color(80, 255, 80, 255);
        public static Color BLUE = new Color(0, 0, 205, 255);
        public static Color YELLOW = new Color(255, 255, 0, 255);
        public static Color MAGENTA = new Color(255, 0, 255, 255);
        public static Color CYAN = new Color(0, 255, 255, 255);
        public static Color GRAY = new Color(128, 128, 128, 255);
        public static Color ORANGE = new Color(255, 128, 0, 255);
        public static Color PURPLE = new Color(128, 0, 255, 255);
        public static Color SKY = new Color(0, 128, 255, 255);
        public static Color VIOLET = new Color(128, 0, 128, 255);

        public static Color[] list = {
                RED, DARK_RED, DARK_GREEN, LIGHT_GREEN, BLUE, YELLOW, MAGENTA, CYAN, GRAY, ORANGE, PURPLE, SKY, VIOLET
        };
        public static Color randomColor() {
            return list[ThreadLocalRandom.current().nextInt(0, list.length)];
        }
    }

    public static class Object {
        public static final boolean logCreation = false;
    }
    public static class AABB {
        public static final boolean render = false;
    }

    public static class GJK {
        public static final boolean debug = false;
        public static final boolean genAndDisplayAllOutsidePoints = false;

        public static final class console {
        }

        public static class render {
            public static final boolean drawOrigin = false;
            public static final boolean drawSuccessfulSimplex = false;
            public static final Color successfulSimplexColor = new Color(161, 255, 255, 255);
            public static final boolean drawFailedSimplex = false;
            public static final Color failedSimplexColor = new Color(255, 0, 255, 255);
            public static final boolean generateAndDisplayAllOutsidePoints = false;
            public static final Color generateAndDisplayAllOutsidePointsColor = new Color(110, 2, 104, 255);
            public static final boolean drawNewPoint = false;
        }
    }

    public static class EPA {
        public static final boolean debug = false;

        public static class render {
            public final static boolean drawSucessfulSimplex = false;
            public final static Color successfulSimplexColor = new Color(0, 124, 189, 255);
            public final static boolean drawFailedSimplex = false;
            public final static Color failedSimplexColor = new Color(255, 0, 255, 255);
            public static final boolean drawMinDistVec = false;
            public static final boolean drawNewVec = false;
        }
    }

    public static class ContactPoints {
        public static final boolean debug = false;

        public static class render {
            public final static boolean drawDoubleContactPoints = true;
            public static final boolean drawMainContactPoint = true;
        }
    }

    public static class Vec {
        public static final boolean debug = false;
    }
}
