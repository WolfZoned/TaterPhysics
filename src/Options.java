import java.awt.*;

public class Options {


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
}
