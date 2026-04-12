# TaterPhysics
A 2D impulse based physics engine for Java.

# Current Features
Rendering\
Basic interactions - Throwing shapes and stopping velocity\
Collision resolution - GJK and EPA\
Friction\
Debug tools - Stop/slow step rate - Record and rewind frames

# How to Use
Press **R** to reset shapes\
Drag shapes to move or throw them

Hold **Z** to record frames, then hold **M** to look through the frames\
--Arrow keys control frames\
--Hold **Z** until **M** is held, then **Z** can be released

Press **K** while not hovering a shape to stop all shape's velocities. If you hover a shape while pressing **K**, only that shape's velocity will be stopped.

The red platform beneath the objects is a giant square without gravity.

# Needs Porting
Circles

A Customizable Joint System Allowing For:\
--Fixed joints\
--Pivot joints\
--Wheel motor joints\
--Stepped motor joints\
--Rope joints\
--Piston joints\
--And more

Multiple interaction modes\
--Drawing shapes with custom properties\
--Creating custom joints between 2 objects\
--Moving shapes\
--Adding points to use for joints

# Plans
This is a port of a previous engine I made. Most of the existing features will probably be ported over before working on new ones.

Planned Features\
--Object fusing\
--Correct handling of single point objects 
