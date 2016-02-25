import processing.core.*; 
import processing.xml.*; 

import saito.objloader.*; 
import processing.core.*; 
import processing.core.PApplet; 
import java.lang.Math.*; 

import java.applet.*; 
import java.awt.Dimension; 
import java.awt.Frame; 
import java.awt.event.MouseEvent; 
import java.awt.event.KeyEvent; 
import java.awt.event.FocusEvent; 
import java.awt.Image; 
import java.io.*; 
import java.net.*; 
import java.text.*; 
import java.util.*; 
import java.util.zip.*; 
import java.util.regex.*; 

public class PiecesAlignment3D extends PApplet {




// Needed for the various Matrix and Vector operations
//import no.uib.cipr.matrix.*;
// Needed for functions like Max and Min


OBJModel []Models = new OBJModel[10];
OBJModel []MBoundaries = new OBJModel[10];
OBJModel initTrans_Cup;    // this is not an obj model, it's just the init translations for every piece

OBJModel []Others = new OBJModel[9];
OBJModel []OBoundaries = new OBJModel[9];
OBJModel initTrans_Others;    // this is not an obj model, it's just the init translations for every piece

int []NoMBPts = new int[10];     // number of boundary points
int []NoOBPts = new int[9];     // number of boundary points

BoundingBox []oldBoxM = new BoundingBox[10];
BoundingBox []newBoxM = new BoundingBox[10];
PVector []oldBoxMCenter = new PVector[10];
PVector []newBoxMCenter = new PVector[10];

BoundingBox []oldBoxO = new BoundingBox[9];
PVector []oldBoxOCenter = new PVector[9];

float rotX_scene, rotY_scene, rotZ_scene;   // rotations apply to the whole scene
float trX_scene, trY_scene, trZ_scene;      // translations apply to the whole scene

float rotX_cup, rotY_cup, rotZ_cup;         // rotations apply to the whole cup
float trX_cup, trY_cup, trZ_cup;            // translations apply to the whole cup

PVector []Ro  = new PVector[10];            // rotations apply to the pieces of cup
PVector []Tr  = new PVector[10];            // translations apply to the pieces of cup
PVector []dTr = new PVector[10];            // translations steps apply to the pieces of cup

float cox, coy, coz;

float sval = (float)1.0f;

PVector t2o;

// these booleans will be used to turn on and off bits of the OBJModel
boolean bTexture = true;
boolean bStroke = false;
boolean bMaterial = true;
boolean mouseflag = false;
boolean START = false;

static final float Sf = 1;    // scale factor
static final long LoopSec = 70621468;

int Rec_color;

PFont myFont;
//    boolean rotAll = false;
    
    //
    // Functions
    //
    ///
    /// Set up screen size, turn off re-drawing loop
    ///
public void setup()
{
    size(1024, 768, P3D);
    Rec_color = color( 0, 122, 173 );
    t2o = new PVector(0, 0, 0);
    rotX_scene = (float)-0.01f;
    rotY_scene = (float)0.43f;
    rotZ_scene = 0;
    trX_scene = 0;
    trY_scene = 0;
    trZ_scene = 0;
    rotX_cup = 0;
    trZ_cup = 0;
    for ( int i = 0; i < Ro.length; i++ )
    {
        Ro[i]  = new PVector( 0, 0, 0 );
        Tr[i]  = new PVector( 0, 0, 0 );
        dTr[i] = new PVector( 0, 0, 0 );
    }

    float sumx = 0;
    float sumy = 0;
    float sumz = 0;
    
    initTrans_Cup = new OBJModel(this, "initTranslation_Cup.obj", "relative", TRIANGLES);
    for ( int i = 0; i < Models.length; i++ )
    {
        Models[i]     = new OBJModel(this, "Cup_wo_light_TL_p"+i+"_dec.obj", "relative", TRIANGLES);
        MBoundaries[i] = new OBJModel(this, "Cup_wo_light_TL_p"+i+"_boundary.obj", "relative", TRIANGLES);
        NoMBPts[i] = MBoundaries[i].getVertexCount();
        Models[i].scale(Sf);
        MBoundaries[i].scale(Sf);
        oldBoxM[i] = new BoundingBox(this, Models[i]);
        oldBoxMCenter[i] = oldBoxM[i].getCenter();
        sumx = sumx + oldBoxMCenter[i].x;
        sumy = sumy + oldBoxMCenter[i].y;
        sumz = sumz + oldBoxMCenter[i].z;
    }
    // calculate the centroid
    t2o.x = sumx/10;
    t2o.y = sumy/10;
    t2o.z = sumz/10;
    // translate to centroid
    for ( int i = 0; i < Models.length; i++ )
    {
        Models[i].translate(t2o);
        MBoundaries[i].translate(t2o);
        // recalculate bounding box and its center
        oldBoxM[i] = new BoundingBox(this, Models[i]);
        oldBoxMCenter[i] = oldBoxM[i].getCenter();

        // translate everyone to origin
        Models[i].translate(oldBoxMCenter[i]);
        MBoundaries[i].translate(oldBoxMCenter[i]);

        // translate everyone to its init position
//            PVector iTr = initTrans_Cup.getVertex(i).mult(Sf);
        Models[i].translate( PVector.mult(initTrans_Cup.getVertex(i), Sf) );
        MBoundaries[i].translate( PVector.mult(initTrans_Cup.getVertex(i), Sf) );

        // calcualte new bounding box and its center
        newBoxM[i] = new BoundingBox(this, Models[i]);
        newBoxMCenter[i] = newBoxM[i].getCenter();
        dTr[i] = PVector.sub(oldBoxMCenter[i], newBoxMCenter[i]);
        println( oldBoxMCenter[i] + " - " + newBoxMCenter[i] + " = " + dTr[i] );
    }

    initTrans_Others = new OBJModel(this, "initTranslation_Others.obj", "relative", TRIANGLES);
    for ( int i = 0; i < Others.length; i++ )
    {
        Others[i]      = new OBJModel(this, "All_others_p"+i+"_dec.obj", "relative", TRIANGLES);
        OBoundaries[i] = new OBJModel(this, "All_others_p"+i+"_boundary.obj", "relative", TRIANGLES);
        NoOBPts[i] = OBoundaries[i].getVertexCount();
        Others[i].scale(Sf);
        OBoundaries[i].scale(Sf);
        oldBoxO[i] = new BoundingBox(this, Others[i]);
        oldBoxOCenter[i] = oldBoxO[i].getCenter();
        Others[i].translate(oldBoxOCenter[i]);
        OBoundaries[i].translate(oldBoxOCenter[i]);

        Others[i].translate( PVector.mult(initTrans_Others.getVertex(i), Sf) );
        OBoundaries[i].translate( PVector.mult(initTrans_Others.getVertex(i), Sf) );
    }

    // turning on the debug output (it's all the stuff that spews out in the black box down the bottom)
//        Models[0].enableDebug();
//    int Delay = 5;
//    int i = Delay;
////    println(millis());
//    for ( long cnt = 1; cnt < (Delay+1)*LoopSec+1; cnt++ )
//    {
//      if (cnt%LoopSec == 0)
//      {        
        myFont = createFont("Arial Bold", 12);
//        textFont(myFont); 
//        text("word", 15, 60, -30); 
//        fill(0, 102, 153);
//        text("word", 15, 60);
//        println("Program will start in " + i + " seconds.");
//        i--;
//      }
//    }
////    println(millis());
//    String[] fontList = PFont.list();
//    println(fontList);
    noStroke();
}


///
/// Animation_3D function
///
public void draw()
{    
//    if (START == false)
//    {
//      int Delay = 5;
//      int i = Delay;
//  //    println(millis());
//      for ( long cnt = 1; cnt < (Delay+1)*LoopSec+1; cnt++ )
//      {
//        if (cnt%LoopSec == 0)
//        {
//          background(255,0,0);
//          lights();
//          println("Program will start in " + i + " seconds.");
//          i--;
//        }
//      }
//      START = true;
//    }
    
    background(40);
    lights();
    ambientLight(100, 100, 100);
    
    textFont(myFont); 
    noFill();    	
    stroke(255,255,255);
    rectMode(CORNER);
    strokeWeight(1);
    rect(10, 10, 125, 35);
    noStroke();
    fill(Rec_color);
    textMode(SCREEN);
    text("Press any key or Click here to restart", 15, 15, 120, 70 );


    //this will do nothing until the model_0 material is turned off
    //fill(255,0,255);
    pushMatrix();
    translate(width/2+trX_scene, height/2+trY_scene, trZ_scene);

//        if (mouseflag == true)
//        {
//            noFill();
//            stroke(255,255,255);
//            strokeWeight(1);
//            //ellipseMode(CENTER);
//            ellipse(0, 0, 300, 300);
//            noStroke();
//        }

    rotateX(rotY_scene);
    rotateY(rotX_scene);
    rotateZ(rotZ_scene);

//        if(mousePressed)
//        {
//            sval += 0.005;
//        }
//        else
//        {
//            sval -= 0.005;
//        }
    if (sval<=1.65f)
    {
      sval += 0.0015f;
    }
    scale(sval);

    for ( int i = 0; i < Others.length; i++ )
    {
        if (rotZ_scene > 2.5f)
        {
            drawBoundary( OBoundaries[i], NoOBPts[i], color(200, 0, 0) );
        }
        Others[i].draw();
    }

    pushMatrix();
    translate(0, 0, -310);
    fill(180);
    box(600, 600, 600);
    noFill();
    popMatrix();

    pushMatrix();
    translate(0, 0, trZ_cup);
    rotateX(rotX_cup);
    
    for ( int i = 0; i < Models.length; i++ )
    {
        pushMatrix();
        translate(Tr[i].x, Tr[i].y, Tr[i].z);
//            rotateX(Ro[i].x);
//            rotateY(Ro[i].y);
//            rotateZ(Ro[i].z);
        if (rotZ_scene > 2.5f)
        {
            drawBoundary( MBoundaries[i], NoMBPts[i], color(0, 200, 0) );
        }
        Models[i].draw();
        popMatrix();
    }
    popMatrix();

    popMatrix();
    if (rotZ_scene > 3.5f)
    {

        for ( int i = 0; i < Models.length; i++ )
        {
            if ( abs(Tr[i].x) < abs(dTr[i].x) )
            {
                Tr[i].x += dTr[i].x/200;
                Tr[i].y += dTr[i].y/200;
                Tr[i].z += dTr[i].z/200;
                rotX_cup += (float)-1.25f/(200*10);
                trZ_cup += (float)130/(200*10);
            }
            else
            {
                Tr[i].x = dTr[i].x;
                Tr[i].y = dTr[i].y;
                Tr[i].z = dTr[i].z;
                rotX_cup = (float)-1.25f;
            }
        }
    }        
//        trY_scene -= 0.1;
//        trZ_scene += 10;
    rotZ_scene += 0.03f;
    if (rotZ_scene > 4.8f)
    {
      if (rotY_scene < 1.17f)
      {
        rotY_scene += 0.0025f;
      }
    }
}

public void drawBoundary( OBJModel boundary, int NoV, int bcolor )
{
    stroke(bcolor);
    strokeWeight(2);
    for (int i = 0; i < NoV-1; i ++)
    {
      line( boundary.getVertex(i).x,   boundary.getVertex(i).y,   boundary.getVertex(i).z,
            boundary.getVertex(i+1).x, boundary.getVertex(i+1).y, boundary.getVertex(i+1).z );
    }
    noStroke();
}

public void mouseDragged()
{
    mouseflag = true;
    if (sqrt(pow(mouseX-width/2,2) + pow(mouseY-height/2,2)) < 300 )
    {
        rotZ_scene += (mouseX - pmouseX) * 0.01f;
    }
    else
    {
        rotX_scene += (mouseX - pmouseX) * 0.01f;
        rotY_scene -= (mouseY - pmouseY) * 0.01f;
    }

//        trX_scene += (mouseX - pmouseX);
//        trY_scene += (mouseY - pmouseY);
}

public void mouseReleased()
{
    mouseflag = false;
    println("rotation    = [ " + rotX_scene + " " + rotY_scene + " ]");
    println("translation = [ " + trX_scene + " " + trY_scene + " ]");
    println(rotZ_scene);
//        trX_scene = 0;
//        trY_scene = 0;
//        rotX_scene = 0;
//        rotY_scene = 0;
}

public void mousePressed()
{
    START = true;
//        sval += 0.005;
    println("Scale factor = " + sval);
    if (mouseX > 10 && mouseX < 125 && mouseY > 10 && mouseY < 35)
    {
//      rotX_scene = (float)-0.01;
//      rotY_scene = (float)1.17;
      rotX_scene = (float)-0.01f;
      rotY_scene = (float)0.43f;
      rotZ_scene = 0;
      trX_scene = 0;
      trY_scene = 0;
      trZ_scene = 0;
      rotX_cup = 0;
      trZ_cup = 0;
      for ( int i = 0; i < Ro.length; i++ )
      {
          Ro[i]  = new PVector( 0, 0, 0 );
          Tr[i]  = new PVector( 0, 0, 0 );
      }
    }
}

public void mouseMoved()
{
    if (mouseX > 10 && mouseX < 125 && mouseY > 10 && mouseY < 35)
    {
      Rec_color = color( 0, 255, 255 );
    }
    else
    {
      Rec_color = color( 0, 122, 173 );
    }
}

public void keyPressed()
{
    // press anykey to restart
//    if(key == 'r') 
//    {
//      rotX_scene = (float)-0.01;
//      rotY_scene = (float)1.17;
      rotX_scene = (float)-0.01f;
      rotY_scene = (float)0.43f;
      rotZ_scene = 0;
      trX_scene = 0;
      trY_scene = 0;
      trZ_scene = 0;
      rotX_cup = 0;
      trZ_cup = 0;
      for ( int i = 0; i < Ro.length; i++ )
      {
          Ro[i]  = new PVector( 0, 0, 0 );
          Tr[i]  = new PVector( 0, 0, 0 );
      }
//    }
}
  static public void main(String args[]) {
    PApplet.main(new String[] { "--bgcolor=#F0F0F0", "PiecesAlignment3D" });
  }
}
