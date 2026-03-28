import java.awt.*;

public class Options {
    public static class AABB {
        public static boolean render = false;
    }
    public static class GJK {
        public static boolean debug = false;
        public static class console {
        }

        public static class render {
            public static boolean drawSuccessfulSimplex = true;
            public static Color successfulSimplexColor = new Color(161, 255, 255, 255);
            public static boolean drawFailedSimplex = true;
            public static Color failedSimplexColor = new Color(255, 0, 255, 255);
            public static boolean generateAndDisplayAllOutsidePoints = true;
            public static Color generateAndDisplayAllOutsidePointsColor = new Color(110, 2, 104, 255);
        }
    }

    public static class EPA {
        public static boolean debug = false;

        public static class render {
            public static boolean drawSucessfulSimplex = true;
            public static Color successfulSimplexColor = new Color(0, 124, 189, 255);
            public static boolean drawFailedSimplex = true;
            public static Color failedSimplexColor = new Color(255, 0, 255, 255);
        }
    }
}
