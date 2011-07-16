package bitmap;

import java.io.*;

/**
 * This program uses the classifier to determine the class of each bitmap in a file.
 */

public class RunClassifier {

  public RunClassifier(String[] args) {
    // create the classifier
    Classifier c=null;
    try {
      c=Classifier.load(args[0]);
    } catch (IOException ex) {
      System.err.println("Load of classifier failed: "+ex.getMessage());
      System.exit(2);
    } catch (ClassNotFoundException ex) {
      System.err.println("Loaded classifier does not match available classes: "+ex.getMessage());
      System.exit(3);
    }
    if (c!=null) {
      // load data
      try {
        Bitmap[] bitmaps=LetterClassifier.loadUnclassifiedLetters(args[1]);
        run(c, bitmaps);
      } catch (IOException ex) {
        System.err.println("Error loading bitmap file: "+ex.getMessage());
      }
    }
  }

  public static void run(Classifier c, Bitmap[] bitmaps) {
    for (int i=0; i<bitmaps.length; i++) {
      System.out.println(c.index((Bitmap)bitmaps[i]));
    }
  }

  public static void main(String[] args) {
    if (args.length!=2) {
      System.err.println("Usage: RunClassifier <classifier-file> <bitmap-file>");
      System.exit(1);
    }
    new RunClassifier(args);
  }

}
