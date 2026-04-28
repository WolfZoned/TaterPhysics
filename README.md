# TaterPhysics
A data-driven 2D impulse based physics engine for Java.

---

## How to run the release file (Java 21+)

**Linux/MacOS:** - "./run.sh"

**Windows:** - "run.bat"

**Manually with java:** - "java -jar build/libs/TaterPhysics-1.0.0-all.jar"

(I have not tested on Windows and MacOS, but they should work)

[INSTALL.md](INSTALL.md) has more in depth info

---

## Current Features
- Rendering
- Basic interactions - Throwing shapes and stopping velocity
- Collision resolution - GJK, EPA, and Impulses
- Friction
- Debug tools - Stop/slow step rate - Record and rewind frames

---

## About
This is a 2D Impulse physics engine. It uses published algorithms like GJK and EPA for collision detection, and generates impulse vectors to apply corrective forces for resolving those collisions. I created this to learn Java after my previous platform was no longer a good fit.
Check out the devlogs leading up to the first release on [Flavortown](https://flavortown.hackclub.com/projects/5129)

---

## How to Use
- Press **R** to reset shapes
- Drag shapes to move or throw them
- Press the record button to begin to recording frames, then press the playback button to rewind frames
  - Arrow keys control frames
  - Stoping playback will rewind the engine's state back to that frame
- Press **K** while not hovering a shape to stop all shape's velocities. If you hover a shape while pressing **K**, only that shape's velocity will be stopped.
- Press **P** or the pause button to toggle a paused state
- Press the shape button to create a new shape 
  - The two premade shapes are a square and circle
  - Press the pencil button to draw a custom shape. Click to add points, then click the checkmark or press **ENTER** to finish the shape. Only convex shapes are supported.

The blue square platform is angular static, and is only able to spin.
The orange square is linear static, and is only able to move without rotation.

---

## Needs Porting
- A Customizable Joint System Allowing For:
  - Fixed joints
  - Pivot joints
  - Wheel motor joints
  - Stepped motor joints
  - Rope joints
  - Piston joints
  - And more
- Multiple interaction modes
  - Drawing shapes with custom properties
  - Creating custom joints between 2 objects
  - Adding points to use for joints
---

## Future Plans
This is a port of a previous engine I made. Most of the existing features will probably be ported over before working on new ones.

**Planned Features:**
- Object fusing
- Correct handling of single point objects 
