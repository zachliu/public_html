/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package project_2_netbeans;

import processing.core.*;
// Needed for the various Matrix and Vector operations
import no.uib.cipr.matrix.*;
// Needed for functions like Max and Min
import java.lang.Math.*;

import java.io.*;
import java.util.*;

// Needed for the .ini file parsing
import org.ini4j.Ini;
import org.ini4j.Profile.Section;

/**
 *
 * @author Zexi Liu
 */
public class Mosaicing extends PApplet
{
    ///
    /// CS-583: Introduction to Computer Vision
    /// https://www.cs.drexel.edu/~kon/introcompvis/
    ///
    /// Project 2 - Panorama/Mosaicing
    ///

    private static final long serialVersionUID = 1L;
    // ini file reading constants including filenames and section names
    static final String DEFAULT_INI_FILE  = "panorama.ini";
    static final String Sec_Cam_Para      = "Camera Parameters";
    static final String FOCAL_LENGTH      = "focal_length";
    static final String RADIAL_DISTORTION = "radial_distortion";
    static final String Sec_Images        = "Images";
    static final String OUTPUTIMAGE       = "result";
    static final String INPUTIMAGES       = "image";
    static final String Sec_LK            = "Lucas-Kanade";
    static final String LK_LEVELS         = "levels";
    static final String LK_STEPS          = "steps";
    static final String Sec_Pano          = "Panorama";
    static final String PAIR              = "pair";


    // Prefix to append to each warped (cylindrically projected) image
    static final String WARPED = "WARPED_";

    // ini file that Lucas-Kanade generates: calculated translations
    static final String LK_TRANS = "Lucas_Kanade_Output.ini";

    static final boolean DO_WARP     = false;
    static final boolean DO_LK       = false;
    static final boolean DO_PANORAMA = true;

    static final boolean BR_ADJ      = true;   // do bright adjustment

    // Homographic indexes
    static final int X_0 = 0;
    static final int Y_1 = 1;
    static final int Z_2 = 2;

    // print debug information (false = not to print)
    static final boolean DEBUG = true;

    // How long to wait in between intermediate screen displays in ms
    static final int FRAME_DELAY = 200;     // .2 second
    static final int FINAL_DELAY = 2000;    // 2 seconds
    static final int MS_PER_SEC  = 1000;    // 1000 ms / sec

    // Size of red dot to draw. Used in showPoints
    static final int DOT_SIZE = 10;

    // BWS(Blending Window Size)
    static final int BWS = 120;
    //
    // Functions
    //
    public Mosaicing()
    {
    }

    public static void main(String[] args)
    {
        // must match the name of your class = packageName.className
        PApplet.main( new String[]{"project_2_netbeans.Mosaicing"} );
    }

    ///
    /// Set up screen size, turn off re-drawing loop
    ///
    @Override
    public void setup()
    {
        size( 800, 600 );
//        size(screen.width, screen.height); // fullscreen
        noLoop();
//        background(255);
    }


    ///
    /// Mosaicing function
    ///
    @Override
    public void draw()
    {
        // Read .ini file
        Ini ini = new Ini();
        try
        {
            BufferedReader reader = createReader(DEFAULT_INI_FILE);
            if(reader == null)
            {
                // No default file, ask the user
                reader = createReader(selectInput("Please choose an INI file"));
                if(reader == null)
                {
                    throw new FileNotFoundException("user specified file");
                }
            }
            ini.load(reader);

            Section section = (Section) ini.get(Sec_Images);
            String panoramaImage = section.get(OUTPUTIMAGE);
            String[] images = (String[]) section.getAll(INPUTIMAGES).toArray(new String[0]);

            //
            // Warp images all images in IMAGES section using parameters in PARAMETERS section.
            // (call warpCylindrical for each image)
            //
            if (DO_WARP)
            {
                section = (Section) ini.get(Sec_Cam_Para);
                float focalLength = Float.parseFloat((String)section.get(FOCAL_LENGTH));
                List distortionStrings = section.getAll(RADIAL_DISTORTION);
                float[] radialDistortion = new float[2];
                radialDistortion[0] = Float.parseFloat((String)distortionStrings.get(0));
                radialDistortion[1] = Float.parseFloat((String)distortionStrings.get(1));

                for (int i = 0; i < images.length; i++)
                {
                    String sourceFile = images[i];
                    String resultFile = WARPED + sourceFile;
                    PImage source = loadImage(sourceFile);
                    PImage result = warpCylindrical(source, focalLength, radialDistortion);
                    result.save(resultFile);
//                    showAndPause(source, FRAME_DELAY);
                    showAndPause(result, FRAME_DELAY);
                }
            }
            else
            {
                println("Skipping warping step.");
            }


            //
            // Run Lucas-Kanade on all PAIRs in the PANORAMA section
            // (Call lucasKanade for each image pair, and save the results to a file)
            //
            if (DO_LK)
            {
                // (Suggestion) Create an ini file to write LK results
                PrintWriter pointWriter = createWriter(LK_TRANS);
                pointWriter.println("[" + Sec_Pano + "]");
                // (Suggestion) Write result similarly to pair format suggested in ini file
                // pair = [id1] [id2] [translation x] [translation y]
                int levels = Integer.parseInt(ini.get(Sec_LK, LK_LEVELS));
                int steps  = Integer.parseInt(ini.get(Sec_LK, LK_STEPS));

                // Read all the pairs= entries into Pair objects
                Pair[] pairs = readPairs(ini);
                for (int i = 0; i < pairs.length; i++)
                {
                    Pair p = pairs[i];
                    PImage im1 = loadImage(WARPED + images[p.id1]);
                    PImage im2 = loadImage(WARPED + images[p.id2]);

                    lucasKanade(im1, im2, levels, steps, p.trans);

                    // Write result in an ini file with this format:
                    // pair = [id1] [id2] [translation x] [translation y]
                    pointWriter.println(PAIR + " = " + p.id1 + " " + p.id2 + " "
                        + p.trans.get(X_0) + " " + p.trans.get(Y_1));

                }
                pointWriter.close();
            }
            else
            {
                println("Skipping Lucas-Kanade step.");
            }


            //
            // Stitch image together
            // (Read the Lucas-Kanade generated file into an array of images, and a matrix of translations
            //  then call makePanorama)
            //
            if (DO_PANORAMA)
            {
                // (Suggestion) read the LK generated ini file
                Ini LKini = new Ini();
                BufferedReader LKreader = createReader(LK_TRANS);
                if(LKreader == null)
                {
                    throw new FileNotFoundException(LK_TRANS);
                }
                LKini.load(LKreader);

                // Read all the pairs entries into Pair objects
                Pair[] pairs = readPairs(LKini);
                PImage[] pimages = new PImage[images.length];
                DenseVector[] translations = new DenseVector[images.length];
                for (int i = 0; i < images.length; i++)
                {
                    pimages[i] = loadImage(WARPED + images[i]);
                    translations[pairs[i].id1] = pairs[i].trans;
                }
                PImage panorama = makePanorama(pimages, translations);
                panorama.save(panoramaImage);
            }
            else
            {
                println("Skipping panorama creation step.");
            }
        }
        catch(FileNotFoundException e)
        {
            println("You must have the file '" + e.getMessage() + "' present.");
        }
        catch(IOException e)
        {
            System.err.println("Failure reading ini file: " + e.getMessage());
            exit();
        }
        println("Done.");
    }

    // Pair object to represent the pair= ini file entry
    private class Pair
    {
        public int id1, id2;
        public DenseVector trans;
    }

    // Read the Panorama section into a list of Pair objects
    private Pair[] readPairs(Ini ini)
    {
        Section section = (Section) ini.get(Sec_Pano);
        String[] lines = (String[]) section.getAll(PAIR).toArray(new String[0]);
        Pair[] pairs = new Pair[lines.length];
        for (int i = 0; i < pairs.length; i++)
        {
            Pair p        = new Pair();
            String[] line = lines[i].split("\\s+");
            p.id1         = Integer.parseInt(line[0]);
            p.id2         = Integer.parseInt(line[1]);
            double tx     = Double.parseDouble(line[2]);
            double ty     = Double.parseDouble(line[3]);
            p.trans       = new DenseVector(new double[] {tx, ty});
            pairs[i]      = p;
        }
        return pairs;
    }


    ///
    /// Warps the given 'image' to cylindrical coordinates using the camera parameter
    /// @param image Image to warp
    /// @param focalLength Focal distance (in pixels)
    /// @param distortionParameters Ordered radial distortion parameters
    /// @returns 'image' has been warped to cylindrical coordinates and has had radial distortion removed
    ///
    public PImage warpCylindrical(PImage image, float focalLength, float[] radialCoefficients)
    {
        PImage result = createImage(image.width, image.height, RGB);

        float s  = focalLength;
        float xc = image.width/2;
        float yc = image.height/2;
        float k1 = radialCoefficients[0];
        float k2 = radialCoefficients[1];

        //
        // Loop for all cylindrical coordinates
        //
        for (int yi = 0; yi < image.height; yi++)
        {
            for (int xi = 0; xi < image.width; xi++)
            {
                //
                // Convert to cylindrical coordinates (theta & h)
                //
                float theta = (xi-xc)/s;
                float h     = (yi-yc)/s;

                //
                // Convert to 3D cylindrical points (x_hat, y_hat)
                //
                float xHat = sin(theta);
                float yHat = h;
                float zHat = cos(theta);

                //
                // Inverse map back to (x,y) points
                //
                float x = xHat/zHat;
                float y = yHat/zHat;

                //
                // Consider radial distortion
                //
                float r2 = x*x + y*y;
                float r4 = r2*r2;
                float distortion = 1 + k1*r2 + k2*r4;
                float xd = x*distortion;
                float yd = y*distortion;

                //
                // Convert (x,y) to original image coordinates (x', y')
                //
                float xp = xd*focalLength + xc;
                float yp = yd*focalLength + yc;

                //
                // Interpolate
                //
                int c = getColorBilinear(image, xp, yp);

                //
                // Write color information
                //
                result.set(xi, yi, c);
            }
        }
        return result;
    }

    ///
    /// Given a point 'p' and an image 'source' returns the bilinearly interpolated color
    ///
    /// @param source Image from which to draw pixel values
    /// @returns a color value bilinearly interpolated - will be BLACK if p is not within source size
    ///
    public int getColorBilinear(PImage source, double x, double y)
    {
        int Red, Green, Blue;

        //  Compute the Red,Green,Blue pixel values for image coordinates p in source image
        //  using bilinear interpolation
        int i = (int) Math.floor(x);
        int j = (int) Math.floor(y);
        double a = x - i;
        double b = y - j;

        no.uib.cipr.matrix.Vector weights;
        weights = new DenseVector(new double[]
        {
            (1-a)*(1-b),
            a *(1-b),
            a *   b,
            (1-a)*   b,
        });

        int ij = source.get(i, j);
        int i1j = source.get(i+1, j);
        int ij1 = source.get(i, j+1);
        int i1j1 = source.get(i+1, j+1);
        Matrix colors;
        colors = new DenseMatrix
        (
            new double[][]
            {
                { red(ij),   red(i1j),   red(i1j1),   red(ij1)   },
                { green(ij), green(i1j), green(i1j1), green(ij1) },
                { blue(ij),  blue(i1j),  blue(i1j1),  blue(ij1)  },
            }
        );

        no.uib.cipr.matrix.Vector newColor = new DenseVector(3);
        colors.mult(weights, newColor);

        Red   = round((float) newColor.get(0));
        Green = round((float) newColor.get(1));
        Blue  = round((float) newColor.get(2));

        return color(Red,Green,Blue);
    }



    ///
    /// Runs the Lucas-Kanade motion estimation algorithm
    ///
    /// @param left first image
    /// @param right second image
    /// @param levels number of levels in the pyramid
    /// @param steps number of iterations to run at each level
    /// @param translation initial translation guess (see @post)
    /// @post parameter translation has been updated to reflect L-K motion estimation
    ///
    public void lucasKanade(PImage image1, PImage image2, int levels, int steps, DenseVector translation)
    {
        //
        // Build the pyramids
        //
        PImage[] pyramid1 = getPyramid(image1, levels);
        PImage[] pyramid2 = getPyramid(image2, levels);

        debug("Level\tStep\tTranslation");

        // Reduce the initial translation to the scale of the smallest level
        translation = translation.scale(1.0/pow2(levels-1));

        //
        // Loop through pyramids from coarse to fine
        //
        for (int i = levels-1; i >= 0; i--)
        {
            //
            // Run iterative Lucas Kanade steps times
            //
            for (int j = 0; j < steps; j++)
            {
                lucasKanadeStep(pyramid1[i], pyramid2[i], translation);
                debug(i + "\t" + j + "\t" + translation.get(X_0) + "\t" + translation.get(Y_1));
//                showAndPause(blendImages(pyramid2[i], pyramid1[i], translation, 0, BWS), FRAME_DELAY);
            }

            //
            // Initialize the starting translation vector at the next level by scaling it
            //
            translation = translation.scale(2.0);
        }

        // Undo the last doubling
        translation = translation.scale(0.5);

    }


    ///
    /// Runs a single Lucas-Kanade motion estimation iteration
    ///
    /// @param image1 first image
    /// @param image2 second image
    /// @param translation initial translation guess (see @post)
    /// @post parameter translation has been updated to reflect L-K motion estimation
    ///
    public void lucasKanadeStep(PImage image1, PImage image2, DenseVector translation)
    {

        //
        // Translate first by current translation vector
        //
        image1 = translate(image1, translation);

        //
        // Build ATA and Atb
        //
        double sumXX = 0;
        double sumYY = 0;
        double sumXY = 0;
        double sumXT = 0;
        double sumYT = 0;
        for (int y = 0; y < image1.height; y++)
        {
            for (int x = 0; x < image1.width; x++)
            {
                double p1 = brightness(image1.get(x, y));
                double p2 = brightness(image2.get(x, y));
                double n  = brightness(image1.get(x, y-1));
                double s  = brightness(image1.get(x, y+1));
                double e  = brightness(image1.get(x+1, y));
                double w  = brightness(image1.get(x-1, y));
                double ne = brightness(image1.get(x+1, y-1));
                double se = brightness(image1.get(x+1, y+1));
                double nw = brightness(image1.get(x-1, y-1));
                double sw = brightness(image1.get(x-1, y+1));
                if (p1 >= 1 && p2 >= 1 && s >= 1 && e >= 1 && n >= 1 && w >= 1)
                {
                    // Convolve 3x3 filter of +1s on right -1s on left
                    double Ix = (ne+e+se-nw-w-sw)/6;
                    // Convolve 3x3 filter of +1s on bottom -1s on top
                    double Iy = (se+s+sw-ne-n-nw)/6;
                    double It = p2-p1;
                    sumXX += Ix*Ix;
                    sumYY += Iy*Iy;
                    sumXY += Ix*Iy;
                    sumXT += Ix*It;
                    sumYT += Iy*It;
                }
            }
        }

        DenseMatrix AtA = new DenseMatrix
        (
            new double [][]
            {
                { sumXX, sumXY },
                { sumXY, sumYY },
            }
        );
        
        DenseVector Atb = new DenseVector(new double [] { -sumXT, -sumYT });
        
        //
        // Solve for u,v
        //
        DenseVector uv = new DenseVector(2);
        AtA.solve(Atb, uv);

        //
        // Update translation vector
        //
        translation.add(uv);

    }


    ///
    /// Builds a Gaussian pyramid
    ///
    /// @param image pano resolution (base) image
    /// @param levels number of levels (including base) to return
    /// @returns Gaussian pyramid (index 0 is base)
    ///
    public PImage[] getPyramid(PImage image, int levels)
    {
        PImage[] pyramid = new PImage[levels];

        pyramid[0] = copyImage(image);
        for (int i = 1; i < levels; i++)
        {
            PImage temp = copyImage(image);
            int s = pow2(i);
            int r = pow2(i-1);

            //
            // Blur the image
            //
            temp.filter(BLUR, r);

            //
            // Sub-sample image
            //
            PImage pi = createImage(image.width/s, image.height/s, RGB);
            for (int y = 0; y < pi.height; y++)
            {
                for (int x = 0; x < pi.width; x++)
                {
                    pi.set(x, y, temp.get(s*x, s*y));
                }
            }

            pyramid[i] = pi;
        }

        return pyramid;
    }


    ///
    /// Creates a panorama from the list of ordered images and relative translations
    ///
    /// @param images sequential list of images
    ///
    public PImage makePanorama(PImage[] images, DenseVector[] translations)
    {
        //
        // Do the blending (call blend_images)
        //


        //
        // Account for drift (do the global warp which involves inverse warping)
        //


        //
        // Crop the images
        //
        DenseVector transSum = new DenseVector(2);
        for (int i = 0; i < translations.length; i++)
        {
            transSum.add(translations[i]);
        }
        // Vertical drift per column
        double a = transSum.get(Y_1)/transSum.get(X_0);

        // Vertical drift the first image
        PImage last = images[images.length-1];
        PImage pano = createImage(last.width, last.height, RGB);
        for (int x = 0; x < pano.width; x++)
        {
            float ax = (float)a*x;
            for (int y = 0; y < pano.height; y++)
            {
                float yp = y+ax;
                int yy = (int)floor(yp);
                float alpha = yp-yy;
                int c = lerpColor(last.get(x, yy), last.get(x, yy+1), alpha);
                pano.set(x, y, c);
            }
        }

        // Blend and drift the remaining images using bilinear interpolation
        transSum = new DenseVector(2);
        for (int i = 0; i < images.length; i++)
        {
            transSum.add(translations[i]);
            pano = blendImages(pano, images[i], transSum, a, BWS, i, images.length-1);
            showAndPause(pano, FRAME_DELAY);
        }

        //
        // Crop the images
        //
        int y0 = (int)Math.ceil(Math.abs(transSum.get(Y_1)));
        int x0 = last.width/2;
        int w = pano.width-last.width;
        int h = pano.height-y0;
        PImage pano_final = createImage(w, h, RGB);
        for (int y = 0; y < h; y++)
        {
            for (int x = 0; x < w; x++)
            {
                pano_final.set(x, y, pano.get(x+x0, y+y0));
            }
        }

        showAndPause(pano_final, FRAME_DELAY);
        return pano_final;
    }


    //
    ///
    /// Linearly blends two images with a specified window width
    ///
    /// @param image1 first image
    /// @param image2 second image
    /// @param translation amount to translate second image
    /// @param dy vertical drift adjustment amount
    /// @param width width of blending bar
    /// @returns a new image that's a linear blend of the two images passed
    ///          the blending bar should be 'width' pixels wide
    ///
    public PImage blendImages(PImage image1, PImage image2, DenseVector translation, double dy, int width, int Index, int NoI)
    {
        //
        // Fill in this function such that it linearly blends image and image translated by translation with a window width
        //
        int offX = (int)Math.floor(translation.get(X_0));
//        int offY = (int)Math.floor(translation.get(Y_1));
        PImage result = createImage(offX+image2.width, image2.height, RGB);
        // Start of blending, halfway between the 2 image overlap
        int tx = (int) Math.floor((translation.get(X_0)+image1.width)/2.0);


        double gray1 = 0;
        int cnt1 = 0;
        for (int y = 0; y < result.height; y++)
        {
            for (int x = offX; x < image1.width; x++)
            {
                gray1 = gray1 +
                        (
                            0.2989 * red(   image1.get(x, y) ) +
                            0.5870 * green( image1.get(x, y) ) +
                            0.1140 * blue(  image1.get(x, y) )
                        );
                cnt1 = cnt1 + 1;
            }
        }

        double gray2 = 0;
        int cnt2 = 0;
        for (int y = 0; y < result.height; y++)
        {
            for (int x = 0; x < image1.width - offX; x++)
            {
                int y2 = round((float)(y - translation.get(Y_1) + dy*x));
                gray2 = gray2 +
                        (
                            0.2989 * red(   image2.get(x, y2) ) +
                            0.5870 * green( image2.get(x, y2) ) +
                            0.1140 * blue(  image2.get(x, y2) )
                        );
                cnt2 = cnt2 + 1;
            }
        }

        double gray1_ave = gray1 / cnt1;
        double gray2_ave = gray2 / cnt2;
        double b_ratio = 0;
        if (BR_ADJ)
        {
            b_ratio = gray1_ave / gray2_ave;
        }
        else
        {
            b_ratio = 1;
        }
        if (Index == NoI && b_ratio != 1)
        {
            for (int y = 0; y < result.height; y++)
            {
                // Section 1, 0 ~ half image 1
                for (int x = 0; x < result.width/2; x++)
                {
                    result.set(x, y, image1.get(x, y));
                }
                // Section 2, half image 1 ~ start of blending
                for (int x = result.width/2; x < tx; x++)
                {
                    int p1 = image1.get(x, y);

                    float inv_b_ratio = ((float)1) / ((float)b_ratio) - 1;
                    float coeff = (((float)x-(float)result.width/2)/((float)tx-(float)result.width/2));

                    float Red_adj   = ((inv_b_ratio) * coeff + 1) * red(   p1 );
                    float Green_adj = ((inv_b_ratio) * coeff + 1) * green( p1 );
                    float Blue_adj  = ((inv_b_ratio) * coeff + 1) * blue(  p1 );

                    int p1_adj = color(Red_adj, Green_adj, Blue_adj);

                    result.set(x, y, p1_adj);
                }
                // Section 3, blending
                for (int dx = 0; dx < width; dx++)
                {
                    int x = tx + dx;
                    float alpha = (dx + 0.5f) / width ;
                    int p1 = image1.get(x, y);

                    float Red_adj   = (float)(1/b_ratio) * ((x-result.width/2)/(tx-result.width/2)) * red(   p1 );
                    float Green_adj = (float)(1/b_ratio) * ((x-result.width/2)/(tx-result.width/2)) * green( p1 );
                    float Blue_adj  = (float)(1/b_ratio) * ((x-result.width/2)/(tx-result.width/2)) * blue(  p1 );

                    int p1_adj = color(Red_adj, Green_adj, Blue_adj);

                    double x2 = x - translation.get(X_0);
                    double y2 = y - translation.get(Y_1) + dy*x;
                    int p2 = getColorBilinear(image2, x2, y2);

                    int c = lerpColor(p1_adj, p2, alpha);
                    
                    result.set(x, y, c);
                }
                // Section 4, only image 2
                for (int x = tx + width; x < result.width; x++)
                {
                    double x2 = x-translation.get(X_0);
                    double y2 = y-translation.get(Y_1) + dy*x;
                    int p2 = getColorBilinear(image2, x2, y2);

                    result.set(x, y, p2);
                }
            }
        }
        else
        {
            for (int y = 0; y < result.height; y++)
            {
                // Section 1, only image 1
                for (int x = 0; x < tx; x++)
                {
                    result.set(x, y, image1.get(x, y));
                }
                // Section 2, blending
                for (int dx = 0; dx < width; dx++)
                {
                    int x = tx + dx;
                    float alpha = (dx + 0.5f) / width ;
                    int p1 = image1.get(x, y);
                    double x2 = x - translation.get(X_0);
                    double y2 = y - translation.get(Y_1) + dy*x;
                    int p2 = getColorBilinear(image2, x2, y2);

                    float Red_adj   = (float)b_ratio * red(p2);
                    float Green_adj = (float)b_ratio * green(p2);
                    float Blue_adj  = (float)b_ratio * blue(p2);

                    int p2_adj = color(Red_adj, Green_adj, Blue_adj);

                    int c = lerpColor(p1, p2_adj, alpha);
                    result.set(x, y, c);
                }
                // Section 3, only image 2
                for (int x = tx + width; x < result.width; x++)
                {
                    double x2 = x-translation.get(X_0);
                    double y2 = y-translation.get(Y_1) + dy*x;
                    int p2 = getColorBilinear(image2, x2, y2);

                    float Red_adj   = (float)b_ratio * red(p2);
                    float Green_adj = (float)b_ratio * green(p2);
                    float Blue_adj  = (float)b_ratio * blue(p2);

                    int p2_adj = color(Red_adj, Green_adj, Blue_adj);

                    result.set(x, y, p2_adj);
                }
            }
        }
        return result;
    }


    //
    // -- Some utility functions ----
    //

    private int pow2(int i)
    {
        return 1<<i;
    }

    private void debug(String s)
    {
      if (DEBUG) 
      {
          println(s);
      }
    }

    ///
    /// Translates an image by a vector
    ///
    public PImage translate(PImage i, DenseVector translation)
    {
        int tx = (int)Math.round(translation.get(X_0));
        int ty = (int)Math.round(translation.get(Y_1));
        PImage translated =
        createImage(i.width + max(0, tx), i.height + max(0, ty), RGB);

        for(int row = 0; row < i.height; row++)
        {
            for(int column = 0; column < i.width; column++)
            {
                int x = column + tx;
                int y = row + ty;
                translated.set(x, y, i.get(column, row));
            }
        }

        return translated;
    }

    ///
    /// Copies an image
    ///
    /// @param i Image to copy
    /// @returns a new image with the same pixels as 'i'
    ///
    public PImage copyImage(PImage i)
    {
        PImage newImage = createImage(i.width, i.height, RGB);
        newImage.copy(i, 0, 0, i.width, i.height, 0, 0, i.width, i.height);
        return newImage;
    }


    ///
    /// Reads points into a an Nx2 matrix from a file with N lines
    /// File may have commas, or not, should only have 2 values (X_0 and Y_1) per line
    ///
    /// @param filename - Text file with X_0 and Y_1 pairs on each line
    /// @pre file exists
    /// @returns an Nx2 matrix with the values filled in
    ///
    public Matrix readPoints(String filename)
    {
        String[] lines = loadStrings(filename);
        DenseMatrix matrix = new DenseMatrix(lines.length, 2);
        for(int l = 0; l < lines.length; l++)
        {
            String[] values = splitTokens(lines[l], ", ");
            matrix.set(l,X_0,PApplet.parseFloat(values[X_0]));
            matrix.set(l,Y_1,PApplet.parseFloat(values[Y_1]));
        }
        return matrix;
    }


    ///
    /// Wrapper that displays an image and waits for the specified number of milliseconds
    ///
    /// @param i image to show
    /// @param ms milliseconds to wait
    ///
    public void showAndPause(PImage i, int ms)
    {
        showAndPause(i, ms, new DenseMatrix(0,0));
    }


    ///
    /// Displays an image with highlighted points then pauses for the specified number of milliseconds
    ///
    /// @param i image to show
    /// @param ms milliseconds to wait
    /// @param points points to circle in red
    ///
    public void showAndPause(PImage i, int ms, Matrix points)
    {
        background(0);

        float divideBy = 1.0f;
        Matrix newPoints = points.copy();
        if(i.width > width || i.height > height)
        {
            divideBy = max(PApplet.parseFloat(i.width)/width, PApplet.parseFloat(i.height)/height);
            newPoints.scale(1.0f/divideBy);
        }

        int originX = floor((width-i.width/divideBy)/2);
        int originY = floor((height-i.height/divideBy)/2);

        for(int r = 0; r < points.numRows(); r++)
        {
            newPoints.set(r, X_0, newPoints.get(r, X_0) + originX);
            newPoints.set(r, Y_1, newPoints.get(r, Y_1) + originY);
        }

        // Show the picture scaled appropriately
        image(i, originX, originY, i.width/divideBy, i.height/divideBy);

        // Show the points
        showPoints(newPoints);
        repaint();
        delay(ms);
    }


    ///
    /// Makes small circles on the pallet image to show where the points in 'points' are
    ///
    /// @param points points to make circles at
    /// @param x horizontal offset (used for centering images, see showAndPause)
    /// @param y vertical offset (used for centering images, see showAndPause)
    ///
    public void showPoints(Matrix points)
    {
        stroke(0,0,0);
        fill(255,0,0);
        for(int r = 0; r < points.numRows(); r++)
        {
            ellipse((float) points.get(r,X_0), (float)points.get(r,Y_1), DOT_SIZE, DOT_SIZE);
        }
    }    
}

