import java.util.HashSet;
import java.util.Set;

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
        public boolean oldLeft;
        public boolean oldRight;
        public boolean oldMiddle;
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
        private final Set<Integer> pressedKeys = new HashSet<>();

        public void press(int keyCode) {
            pressedKeys.add(keyCode);
        }

        public void release(int keyCode) {
            pressedKeys.remove(keyCode);
        }

        public boolean isPressed(int keyCode) {
            return pressedKeys.contains(keyCode);
            //use input.keys.isPressed(KeyEvent.VK_W)
        }

        public Set<Integer> getAllPressed() {
            return new HashSet<>(pressedKeys);
        }

        public void clear() {
            pressedKeys.clear();
        }
    }
}
