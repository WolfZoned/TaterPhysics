import java.awt.event.MouseEvent;

public class Input {
    public Keys keys;
    public Mouse mouse;

    public Input() {
        keys = new Keys();
        mouse = new Mouse();

    }

    public static class Mouse {
        public boolean left;
        public boolean right;
        public boolean middle;
        public Vec stepChange;
        public Vec pos;
        public Vec oldPos;

        public Mouse() {
            left = false;
            right = false;
            middle = false;
            pos = new Vec(0, 0);
            oldPos = new Vec(0, 0);
            stepChange = new Vec(0, 0);
        }

        public void updatePos(double volatileMouseX, double volatileMouseY) {
            if (oldPos.x != pos.x) { oldPos.x = pos.x; }
            if (oldPos.y != pos.y) { oldPos.y = pos.y; }
            if (pos.x != volatileMouseX) {
                oldPos.x = pos.x;
                pos.x = volatileMouseX;
            }
            if (pos.y != volatileMouseY) {
                oldPos.y = pos.y;
                pos.y = volatileMouseY;
            }
        }

        public void updateStepChange() {
            stepChange.x = pos.x - oldPos.x;
            stepChange.y = pos.y - oldPos.y;
        }
    }

    public static class Keys {
    }
}
