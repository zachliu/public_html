===========================================
cs636: Advanced Rendering Techniques
Assignment #3 (v.3.1, see Version Conrol): 
	3. Add an acceleration technique
===========================================

by Zexi Liu
on May 8th, 2012

Language used:	C++
OS used:	Windows 7
Compiler used:	Microsoft Visual Studio 2010 Ultimate
		Version 10.0.40219.1 SP1Rel
Compile/link:	taken care of by IDE

External Libraries used:
# tiffconf.h
# tiffio.h

Features of the program:
* It implements a perspective ray tracer that intersects rays with spheres and triangle meshes.
* It uses constant coloring to shade the objects.
* It reads scene definations from a txt file (scene.txt).
* It outputs final results to an TIFF file.
* Diffuse/specular shading model with point lights.
* 3D rigid transfromation through Instance or at the loading stage.
* Regular gird acceleration technique.
* Directional light.

New Features:
* 3D checker, plane checker, and sphere checker.

Version Conrol:
v.1.2	Assignment 1 and 2
v.3.0	Assignment 3 + directional lights
v.3.1	v.3.0 + checker texture
