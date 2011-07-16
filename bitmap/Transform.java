package bitmap;

import java.io.*;

/**
 * Given a bitmap file, this program prints the transformed version of the
 * bitmaps. (ie. centred and normalised in size)
 */

public class Transform {

  // print out moment information 
  public Transform(String[] args) {
    // load data
    try {
      ClassifiedBitmap[] bitmaps=LetterClassifier.loadLetters(args[0]);

      for (int i = 0; i < bitmaps.length; i++) {
        System.out.println(bitmaps[i].toString());
      }

    } catch (IOException ex) {
      System.err.println("Error loading data.txt: "+ex.getMessage());
    }
  }

  public static void main(String[] args) {
    if (args.length!=1) {
      System.err.println("Usage: Transform <bitmap-file>");
      System.exit(1);
    }
    new Transform(args);
  }

}
