package bitmap;

import java.util.*;

/**
 * <p>A Bitmap holds a matrix of bits (true or false, on or off, 1 or 0).</p>
 * @author Mikael Boden
 * @version 1.0
 */

public class Bitmap {
  private final float NORMALIZED_RADIUS = 0.30f; // radius of gyration to normalize the bitmap to, as a fraction of width
  private float[][] map;

  /**
   * Create a binary map consisting of a specified number of rows and columns
   * @param nRows number of rows
   * @param nCols number of columns
   */
  public Bitmap(int nRows, int nCols) {
    map=new float[nRows][nCols];
  }

  /**
   * <p>Create a binary map from a string consisting of a row and column number, and then the bits/values of the map
   * ('0' or '1').</p>
   * Format:<p>nRows nCols value_row_1_col_1 value_row_1_col_2 ... value_row_nRows_col_nCols</p>
   * @param spec the string specification
   */
  public Bitmap(String spec) {
    StringTokenizer tok=new StringTokenizer(spec,"\t ,");
    int ntok=tok.countTokens();
    if (ntok<2)
      throw new RuntimeException("Bitmap is not correctly specified. Incorrect row and column number: "+spec);
    try {
      int nRows=Integer.parseInt(tok.nextToken());
      int nCols=Integer.parseInt(tok.nextToken());
      if (ntok-2<nRows*nCols)
        throw new RuntimeException("Bitmap is not correctly specified. Insufficient number of bits: "+spec);
      map=new float[nRows][nCols];
      for (int r=0; r<map.length; r++)
        for (int c=0; c<map[r].length; c++)
          map[r][c]=Float.parseFloat(tok.nextToken());
    } catch (NumberFormatException ex) {
      throw new RuntimeException("Bitmap is not correctly specified. Bits not correctly formatted: "+spec);
    }
    preprocess();
  }

  /**
   * Determine the number of rows that are used by the bitmap
   * @return the number of rows
   */
  public int getRows() {
    return map.length;
  }

  /**
   * Determine the number of columns that are used by the bitmap
   * @return the number of columns
   */
  public int getCols() {
    if (map.length<=0)
      return 0;
    else
      return map[0].length;
  }

  /**
   * Set the bit in the specified position in the binary map to a specified value
   * @param row the row
   * @param col the column
   * @param value the value
   */
  public void set(int row, int col, boolean value) {
    if (row>=0 && row<map.length)
     if (col>=0 && col<map[row].length) {
       if (value)
         map[row][col]=1.0f;
       else
         map[row][col]=0.0f;
     }
  }

  /**
   * Get the bit in the specified position in the binary map
   * @param row the row
   * @param col the column
   * @return the value
   */
  public boolean get(int row, int col) {
    if (row>=0 && row<map.length)
     if (col>=0 && col<map[row].length)
       return map[row][col] > 0;
    return false;
  }

  /**
   * Reset the bitmap
   */
  public void blank() {
    for (int r=0; r<map.length; r++)
      for (int c=0; c<map[r].length; c++)
        map[r][c]=0.0f;
  }

  /**
   * Prints out the bitmap as a text string
   * @return the text string representing the bitmap
   */
  public String toString() {
    StringBuffer buf=new StringBuffer(map.length+" "+(map.length>0?map[0].length+" ":"0 "));
    for (int r=0; r<map.length; r++) {
      for (int c=0; c<map[r].length; c++)
        buf.append(Float.toString(map[r][c]) + " ");
    }
    return buf.toString().trim();
  }

  /**
   * Convert the map to a one-dimensional array of booleans
   * @return the boolean array representing the bitmap
   */
  public boolean[] toBooleanArray() {
    if (map.length<=0)
      return null;
    boolean[] arr=new boolean[map.length*map[0].length];
    for (int r=0; r<map.length; r++)
      for (int c=0; c<map[r].length; c++)
        arr[r*map[0].length+c]=(map[r][c] > 0);
    return arr;
  }

  /**
   * Convert the map to a one-dimensional array of doubles
   * @return the double array representing the bitmap
   */
  public double[] toDoubleArray() {
    if (map.length<=0)
      return null;
    double[] arr=new double[map.length*map[0].length];
    for (int r=0; r<map.length; r++)
      for (int c=0; c<map[r].length; c++)
        arr[r*map[0].length+c]=(double)map[r][c];
    return arr;
  }

  /**
   * Convert the map to an array of the 7 Hu Moment Invariants.  Not used in
   * current implementation.
   * @return the double array representing the moment characteristics of the bitmap.
   */
  public double[] toMomentArray() {
    int m = 0;  // area
    int Mx = 0; // first moment in x direction
    int My = 0; // first moment in y direction

    int h = getRows();
    int w = getCols();

    // calculate m, Mx and My
    for (int i = 0; i < h; i++) {
      for (int j = 0; j < w; j++) {
        if (map[i][j] > 0.0) {
          m++;
          My += i;
          Mx += j;
        }
      }
    }

    double Cx = (double)Mx/(double)m; // centroid, x
    double Cy = (double)My/(double)m; // centroid, y

    double n11 = 0;
    double n20 = 0;
    double n02 = 0;
    double n30 = 0;
    double n03 = 0;
    double n21 = 0;
    double n12 = 0;

    for (int i = 0; i < h; i++) {
      for (int j = 0; j < w; j++) {
        if (map[i][j] > 0.0) {
          double iRel = (double)i-Cx;
          double jRel = (double)j-Cy;
          n11 += centralMoment(jRel, iRel, 1, 1);
          n20 += centralMoment(jRel, iRel, 2, 0);
          n02 += centralMoment(jRel, iRel, 0, 2);
          n30 += centralMoment(jRel, iRel, 3, 0);
          n03 += centralMoment(jRel, iRel, 0, 3);
          n21 += centralMoment(jRel, iRel, 2, 1);
          n12 += centralMoment(jRel, iRel, 1, 2);
        }
      }
    }
    n11 /= Math.pow(m, 2);
    n20 /= Math.pow(m, 2);
    n02 /= Math.pow(m, 2);
    n30 /= Math.pow(m, 2.5);
    n03 /= Math.pow(m, 2.5);
    n21 /= Math.pow(m, 2.5);
    n12 /= Math.pow(m, 2.5);

    double[] moment = new double[7];
    moment[0] = n20 + n02;
    moment[1] = (n20 - n02)*(n20 - n02) + 4*n11*n11;
    moment[2] = (n30 - 3*n12)*(n30 - 3*n12) + (3*n21 - n03)*(3*n21 - n03);
    moment[3] = (n30 + n12)*(n30 + n12) + (n21 + n03)*(n21 + n03);
    moment[4] = (n30-3*n12)*(n30+n12)*((n30+n12)*(n30+n12) - 3*(n21+n03)*(n21+n03)) +
                (3*n21-n03)*(n21+n03)*(3*(n30+n12)*(n30+n12) - (n21+n03)*(n21+n03));
    moment[5] = (n20-n02)*((n30+n12)*(n30+n12) - (n21+n03)*(n21+n03)) +
                4*n11*(n30+n12)*(n21+n03);
    moment[6] = (3*n21-n03)*(n30+n12)*((n30+n12)*(n30+n12) - 3*(n21+n03)*(n21+n03)) -
                (n30-3*n12)*(n21+n03)*(3*(n30+n12)*(n30+n12) - (n21+n03)*(n21+n03));

    moment[0] = moment[0]/50000 - 1;
    moment[1] = moment[1]/1e9 - 0.5;
    moment[2] = moment[2]/1e11 - 1;
    moment[3] = moment[3]/3e10 - 1;
    moment[4] = moment[4]/1e21 - 1;
    moment[5] = moment[5]/1e15;
    moment[6] = moment[6]/1e21 - 1;
    return moment;
  }

  /**
   * Calculate the p-qth central moment of a particle wit displacement (x, y).
   * Not used in current implementation.
   * @param x x-displacement of particle
   * @param y y-displacement of particle
   * @param p x-order of moment
   * @param q y-order of moment
   * @return p-qth central moment of a particle wit displacement (x, y)
   */
  private double centralMoment(double x, double y, int p, int q) {
    return Math.pow(x,p) * Math.pow(y,q);
  }

  /**
   * Shifts the image such that its center of mass is in the centre, and scales
   * it such that the radius of gyration is NORMALIZED_RADIUS times the width
   * of the image, using bilinear filtering.
   * Code adapted from:
   * http://www.codeproject.com/cs/media/imageprocessing4.asp?select=1312950
   */
  public void preprocess() {
    int h = getRows();
    int w = getCols();

    float m = 0, Mx = 0, My = 0, I = 0;
    // calculate m, Mx, My, I
    for (int i = 0; i < h; i++) {
      for (int j = 0; j < w; j++) {
        m += map[i][j];
        My += i*map[i][j];
        Mx += j*map[i][j];
        I += (i*i+j*j)*map[i][j];
      }
    }

    int Cx = (int)(Mx/m); // centroid, x
    int Cy = (int)(My/m); // centroid, y
    int shiftx = w/2 - Cx;
    int shifty = h/2 - Cy;

    //                   desired radius        / current radius of gyration
    float scaleFactor = (NORMALIZED_RADIUS * h) /(float) Math.sqrt(I / m - Cx*Cx - Cy*Cy);

    float[][] map2=new float[h][w];
    // bilinear scaling
    for (int i = 0; i < h; i++) {
      for (int j = 0; j < w; j++) {
        // calculate location of source pixel, before scaling and shifting
        int floor_x = (int)Math.floor((j-shiftx-Cx)/scaleFactor)+(int)Cx;
        int floor_y = (int)Math.floor((i-shifty-Cy)/scaleFactor)+(int)Cy;

        if (floor_x >= 0 && floor_x < w &&
            floor_y >= 0 && floor_y < h) { // if source pixel is within boundary
          int ceil_x = floor_x + 1;
          if (ceil_x >= w) ceil_x = floor_x;
          int ceil_y = floor_y + 1;
          if (ceil_y >= h) ceil_y = floor_y;
          float fraction_x = (j-shiftx-Cx)/scaleFactor+Cx - floor_x;
          float fraction_y = (i-shifty-Cy)/scaleFactor+Cy - floor_y;
          float one_minus_x = 1.0f - fraction_x;
          float one_minus_y = 1.0f - fraction_y;

          // the four surrounding source pixels
          float c1 = map[floor_y][floor_x];
          float c2 = map[floor_y][ceil_x];
          float c3 = map[ceil_y][floor_x];
          float c4 = map[ceil_y][ceil_x];

          /// the bilinear average
          map2[i][j] = one_minus_y*(one_minus_x*c1 + fraction_x*c2) +
                       fraction_y*(one_minus_x*c3 + fraction_x*c4);
        }

      }
    }
    this.map = map2;
  }

}
