import processing.core.*; 
import processing.xml.*; 

import no.uib.cipr.matrix.*; 
import java.lang.Math.*; 
import org.ini4j.*; 

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

public class Relight extends PApplet {

///
/// CS-583: Introduction to Computer Vision
/// https://www.cs.drexel.edu/~kon/introcompvis/
///
/// Project 3 - Photometric Stereo - Relighting Applet
///
/// @note This was intended to be an applet, but it still uses too much memory.
///       To minimize memory albedos and normals are stored for only the pixels that have a white
///       mask. Throughout the code a 'pixelCount' variable is used in loops to index into the list
///       of these values. The entire set of un-masked values are stored so that relighting is fast.
///
/// @note The items marked TODO must be completed by you.
///


// Needed for the various Matrix and Vector operations

// Needed for functions like Max and Min

// Needed for the .ini file parsing


//
// Constants
//


// Homographic indexes
static final int X_0 = 0;
static final int Y_1 = 1;
static final int Z_2 = 2;

// Whether or not to print debug information
static final boolean DEBUG = true;

// System constants
static final String SLASH = System.getProperty("file.separator");
static final int MAX_IMAGES = 50;

// ini file reading constants including filename and key names
static final String DEFAULT_INI_FILE = "photometricStereo.ini";
static final String CHROME           = "Chrome";
static final String MYPATH           = "path";
static final String OUTPUT           = "output";
static final String SUBJECTS         = "Subjects";
static final String MASK             = ".mask.png";
static final String PATTERN          = ".%d.png";

//
// Global variables (computed by 'setup')
//
PImage mask;
// Bounding box of mask's white region
int minX;
int maxX;
int minY;
int maxY;
// (number of subject pixels) x 3 matrices to store all the albedos and normals
DenseMatrix albedos;
DenseMatrix normals;
boolean uniformAlbedo = false;
String outputPath;


///
/// Set up screen size, read files, initialize globals
///
public void setup()
{
    size(512, 340, P2D);
    load(0);
}


///
/// Calls 'load' to reload a new section when a key (1-9) is pressed
///
public void keyPressed()
{
  if(' ' == key)
  {
      uniformAlbedo = !uniformAlbedo;
  }
//  else if (key == 's')
//  {
//      String file = outputPath + ".relight.png";
//      if (uniformAlbedo)
//      {
//          file = outputPath + ".uniform.png";
//      }
//      save(file);
//      println("Saved to file: " + file);
//  }
  else
  {
      int section = key - '1';
      load(section);
  }
}

///
/// Loads the proper image set into the global variables
///
/// @param index which path (as indexed in the ini file) to load
///
public void load(int index)
{
    // Read .ini file
    Ini ini = new Ini();
    try
    {
        BufferedReader reader = createReader(DEFAULT_INI_FILE);
        if(reader == null)
        {
            throw new FileNotFoundException("user specified file");
        }
        ini.load(reader);
        outputPath = ini.get(SUBJECTS).get(MYPATH, index);
        String lightPath = ini.get(CHROME).get(OUTPUT);
        display("LOADING...");
        load(outputPath, lightPath);
    }
    catch(FileNotFoundException e)
    {
        println("You must have the file '" + e.getMessage() + "' present.");
    }
    catch(IOException e)
    {
        println("Failure reading ini file: " + e.getMessage());
        exit();
    }
    catch(IndexOutOfBoundsException e)
    {
        display("No such image set!");
    }
}



///
/// Loads the model from the images in 'path' given the light directions in the file 'lightPath'
///
public void load(String path, String lightPath)
{
    //
    // Load images, light sources
    //
    PImage[] images = loadImageDirectory(path, path + PATTERN);
    DenseMatrix lights = readPoints3D(lightPath);
    mask = loadImage(path + SLASH + path + MASK);
  
    //
    // TODO: Compute bounding box for subject from mask by setting minX,minY,maxX,maxY from mask
    // TODO: Count number of white pixels in mask
    //
    // Note: we do this to save space, and ensure the subject is centered in the window
    //    
    minX = Integer.MAX_VALUE;
    maxX = Integer.MIN_VALUE;
    minY = Integer.MAX_VALUE;
    maxY = Integer.MIN_VALUE;
    int pixelCount = 0;
    for (int i = 0; i < mask.width; i++)
    {
        for (int j = 0; j < mask.height; j++)
        {
            if ( red(mask.get(i, j))/255.0f > 0 || green(mask.get(i, j))/255 > 0 || blue(mask.get(i, j))/255 > 0 )
            {                
                minX = min(minX, i);
                minY = min(minY, j);
                maxX = max(maxX, i);
                maxY = max(maxY, j);
                pixelCount = pixelCount + 1;
            }
        }
    }
  
    //
    // TODO: Compute all normals and save in a [pixelCount x 3] matrix - 'normals'
    // TODO: Compute all albedos and save in a [pixelCount x 3] matrix - 'albedos'
    //
    int subjectWidth  = maxX - minX + 1;
    int subjectHeight = maxY - minY + 1;
    normals = new DenseMatrix(pixelCount, 3);
    albedos = new DenseMatrix(pixelCount, 3);
    // Keep track of pixel index in mask (note, we do all this to minimize the waste of space)
    // For each pixel that has a white mask value, calculate the normal and albedo
    // Hint: use red(mask.get(x,y)) > 0  to test if the mask is white
    int cnt = 0;
    for ( int i = minX; i <= maxX; i++ )    
    {
        for ( int j = minY; j <= maxY; j++ )
        {
            if ( red(mask.get(i, j))/255.0f > 0 || green(mask.get(i, j))/255 > 0 || blue(mask.get(i, j))/255 > 0 )
            {
                DenseVector Nor = computeOneNormal(images, lights, i, j);
                DenseVector Alb = computeOneAlbedo(images, lights, i, j, Nor);
                normals.set(cnt, 0, Nor.get(0));
                normals.set(cnt, 1, Nor.get(1));
                normals.set(cnt, 2, Nor.get(2));
                albedos.set(cnt, 0, Alb.get(0));
                albedos.set(cnt, 1, Alb.get(1));
                albedos.set(cnt, 2, Alb.get(2));
                cnt++;
            }
        }
    }
}

//
// TODO: Copy 'computeOneAlbedo' and 'computeOneNormal' from your successful Stereo.pde
//       since they are used above
//

///
/// Given the pixel values, light directions, and normal computes the albedo for a given location
/// (assumes Lambertian reflection)
///
/// @param images array of images of a subject lit from different directions
/// @param lights light sources corresponding to each image
/// @param x horizontal component pixel where normal should be calculated
/// @param y vertical component pixel where normal should be calculated
/// @param normal surface nurmal at x,y
/// @returns a 3-dimensional vector which is the surface normal at (x,y)
///
public DenseVector computeOneAlbedo(PImage[] images, DenseMatrix lights, int x, int y, DenseVector normal)
{
    //
    // TODO: Compute the surface normal at pixel (x,y)
    // (Try the weighted least-squares explained in the class for handling shadows.)
    //
    DenseVector r = new DenseVector(3);
    int NoI = images.length;  // number of images
    DenseVector RouR = new DenseVector(1);
    DenseVector RouG = new DenseVector(1);
    DenseVector RouB = new DenseVector(1);
    DenseMatrix Sn = new DenseMatrix(NoI,1);
    DenseMatrix normalM = new DenseMatrix(3,1);
    normalM.set(0, 0, normal.get(0));
    normalM.set(1, 0, normal.get(1));
    normalM.set(2, 0, normal.get(2));
    lights.mult(normalM, Sn);

    DenseVector IR = new DenseVector(NoI);
    DenseVector IG = new DenseVector(NoI);
    DenseVector IB = new DenseVector(NoI);
    for (int i = 0; i < NoI; i++)
    {
        int Color = images[i].get(x, y);
        IR.set(i, red(Color)/255.0f);
        IG.set(i, green(Color)/255.0f);
        IB.set(i, blue(Color)/255.0f);
    }
    Sn.solve(IR, RouR);
    Sn.solve(IG, RouG);
    Sn.solve(IB, RouB);    
    r.set(0, RouR.get(0));
    r.set(1, RouG.get(0));
    r.set(2, RouB.get(0));
    return r;
}

///
/// Given the pixel values and light directions computes the normal to the surface
/// (assumes Lambertian reflection)
///
/// @param images array of images of a subject lit from different directions
/// @param lights light sources corresponding to each image
/// @param x horizontal component pixel where normal should be calculated
/// @param y vertical  component pixel where normal should be calculated
/// @returns a 3-dimensional vector which is the surface normal at (x,y)
///
public DenseVector computeOneNormal(PImage[] images,  DenseMatrix lights, int x, int y)
{
    //
    // TODO: Compute the surface normal at pixel (x,y)
    // (Try the weighted least-squares explained in the class for handling shadows.)
    //
    DenseVector normal = new DenseVector(3);
    
    int NoI = images.length;  // number of images
    DenseVector I = new DenseVector(NoI);  // more than three lights
    DenseMatrix S = lights.copy();
    for (int i = 0; i < NoI; i++)
    {       
        float IntensityPixel = 0.3f*red(images[i].get(x, y))/255 + 0.59f*green(images[i].get(x, y))/255 + 0.11f*blue(images[i].get(x, y))/255;
        I.set(i, Math.pow(IntensityPixel, 2));      // trick for handling shadows
        S.set(i, 0, IntensityPixel * S.get(i, 0));  // weight each equation by the pixel brighteness/intensity
        S.set(i, 1, IntensityPixel * S.get(i, 1));  // weight each equation by the pixel brighteness/intensity
        S.set(i, 2, IntensityPixel * S.get(i, 2));  // weight each equation by the pixel brighteness/intensity
    }

    DenseMatrix ST  = new DenseMatrix( S.numColumns(), S.numRows() );    
    DenseMatrix STS = new DenseMatrix( S.numColumns(), S.numColumns() );
    S.transpose(ST);
    S.transAmult(S, STS);
    DenseMatrix STS_inv_ST = new DenseMatrix(S.numColumns(), S.numRows());
    try
    {
        // based on lecture notes
        // I = S * n;
        // ST * I = STS * n;
        // n = (STS)^-1 * ST * I;
        STS.solve(ST, STS_inv_ST);    // STS^-1 * ST
        STS_inv_ST.mult(I, normal);   // (STS)^-1 * ST * I
        double RoSoS = Math.sqrt( Math.pow(normal.get(0),2) + Math.pow(normal.get(1),2) + Math.pow(normal.get(2),2) );  // root of sum of squares
        normal.set( 0, normal.get(0) / RoSoS );
        normal.set( 1, normal.get(1) / RoSoS );
        normal.set( 2, normal.get(2) / RoSoS );
    }
    catch(MatrixSingularException e)
    {
        normal.zero();
    }
        
    return normal;
}

///
/// Main function
///
public void draw()
{
    background(0);
    // Make up a light diroction from mouse
    float theta = map(mouseX, 0, width, -PI/2, PI/2);
    float phi = map(mouseY, 0, height, -PI/2, PI/2);
  
    DenseVector lightDirection = new DenseVector( new double[]{sin(theta) * cos(phi), cos(theta) * sin(phi), cos(theta)});
    lightDirection.scale(1/lightDirection.norm(DenseVector.Norm.Two));
    int pixelCount = 0;
    PImage result = createImage(width, height, RGB);
    for(int x = minX; x <= maxX; x++)    
    {
        for(int y = minY; y <= maxY; y++)
        {
            if(red(mask.get(x,y)) == 0)
            {
                continue;
            }
            //
            // TODO: Compute the color at (x,y)
            //
            int c;
      
            DenseVector normal = new DenseVector(3);
            normal.set( 0, normals.get(pixelCount, 0) );
            normal.set( 1, normals.get(pixelCount, 1) );
            normal.set( 2, normals.get(pixelCount, 2) );

            DenseVector I;  // instensity
            if (uniformAlbedo)
            {
                I = new DenseVector(3);
                I.set( 0, 0.7f );
                I.set( 1, 0.7f );
                I.set( 2, 0.7f );
            }
            else
            {
                I = new DenseVector(3);
                I.set( 0, albedos.get(pixelCount, 0) );
                I.set( 1, albedos.get(pixelCount, 1) );
                I.set( 2, albedos.get(pixelCount, 2) );
            }
            double Sn = normal.dot(lightDirection);            
            I.set( 0, I.get(0) * Sn * 255 );
            I.set( 1, I.get(1) * Sn * 255 );
            I.set( 2, I.get(2) * Sn * 255 );
            
            c = color(Math.round(I.get(0)), Math.round(I.get(1)), Math.round(I.get(2)));

            pixelCount = pixelCount + 1;
      
            // Center the object by shifting which pixel we set
            int i = (x - minX) + (width - (maxX - minX))/2;
            int j = (y - minY) + (height - (maxY - minY))/2;
            //set(i,j, c);            
            result.set(i, j, c);            
        }
    }
    image(result,0,0);
}






///
/// Prints a message to the screen, waits a second before continuing
///
public void display(String message)
{
    textFont(loadFont("Ziggurat-HTF-Black-32.vlw"), 32);
    background(0);
    fill(200);
    text(message, 10, height - 10);
    repaint();
    delay(1000);
}

///
/// Loads all images that fit the given printf 'pattern' in the given 'directory'
///
/// @param directory directory to look in
/// @param pattern pattern with %d (or modification) in it somewhere
/// @returns an array of PImages for the files that met the pattern
/// @pre the 'directory' must not contain more than MAX_IMAGES images that meet the pattern
/// @pre the images must be ordered sequentially starting with 0
/// @see http://java.sun.com/j2se/1.5.0/docs/api/java/util/Formatter.html
///
public PImage[] loadImageDirectory(String directory, String pattern)
{
    println("Loading all images like '" + directory + SLASH + pattern + "'");
    ArrayList images = new ArrayList();
    String sanityCheck = "";
    for(int index = 0; index < MAX_IMAGES; index++)
    {
        String filename =  String.format(pattern, index).toString();
        if(filename == sanityCheck)
        {
            println("Your pattern is bad! It must contain %d or some other integer placeholder!");
            return null;
        }
        PImage image = loadImage(directory + SLASH + filename);
        if(null == image)
        {
            if(index == 0)
            {
                println("No images were found, check to make sure there are images named correctly.");
            }
            else
            {
                println("Loaded " + index + " images (don't worry about the above error).");
            }
            return (PImage[])images.toArray(new PImage[0]);
        }
        images.add(image);
    }
    println("Do you really have so many images? Change MAX_IMAGES to a higher value if you do, or fix your bug.");
    return null;
}

///
/// Reads points into a an Nx3 matrix from a file with N lines
/// File may have commas, or not, should only have 3 values (X, Y and Z) per line
///
/// @param filename - Text file with X, Y, Z tuples on each line
/// @pre file exists
/// @returns an Nx3 matrix with the values filled in
///
public DenseMatrix readPoints3D(String filename)
{
    String[] lines = loadStrings(filename);
    DenseMatrix matrix = new DenseMatrix(lines.length, 3);
    for(int l = 0; l < lines.length; l++)
    {
        String[] values = splitTokens(lines[l], "\t, ");
        matrix.set(l,X,PApplet.parseFloat(values[X]));
        matrix.set(l,Y,PApplet.parseFloat(values[Y]));
        matrix.set(l,Z,PApplet.parseFloat(values[Z]));
    }
    return matrix;
}
  static public void main(String args[]) {
    PApplet.main(new String[] { "--bgcolor=#F0F0F0", "Relight" });
  }
}
