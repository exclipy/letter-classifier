package bitmap;

import java.io.*;

/**
 * This program trains a classifier and saves it in a file to be read when used.
 * @author Mikael Boden
 * @version 1.0
 */

public class TrainClassifier {

  public TrainClassifier(String[] args, int iterations, double eta, int hidden) {
    // create the classifier
    NNClassifier c=new NNClassifier(32, 32, hidden);

    // load data
    try {
      ClassifiedBitmap[] bitmaps=LetterClassifier.loadLetters(args[1]);
      // train it using all available training data
      c.train(bitmaps,iterations,eta);
    } catch (IOException ex) {
      System.err.println("Error loading data.txt: "+ex.getMessage());
    }
    try {
      Classifier.save(c, args[0]);
    } catch (Exception ex) {
      System.err.println("Failed to serialize and save file: "+ex.getMessage());
    }
  }

  public TrainClassifier(String[] args, int iterations, double eta, int hidden, String validation) {
    // create the classifier
    NNClassifier c=new NNClassifier(32, 32, hidden);
    ClassifiedBitmap[] bitmaps;
    ClassifiedBitmap[] bitmaps2;
    // load data
    try {
      bitmaps=LetterClassifier.loadLetters(args[1]);
    } catch (IOException ex) {
      System.err.println("Error loading data.txt: "+ex.getMessage());
      return;
    }
    try {
      bitmaps2=LetterClassifier.loadLetters(validation);
    } catch (IOException ex) {
      System.err.println("Error loading validation.txt: "+ex.getMessage());
      return;
    }
    c.train(bitmaps,bitmaps2,iterations,eta);
    try {
      Classifier.save(c, args[0]);
    } catch (Exception ex) {
      System.err.println("Failed to serialize and save file: "+ex.getMessage());
      return;
    }
  }

  public static void main(String[] args) {
    if (args.length<2) {
      System.err.println("Usage: TrainClassifier <classifier-file> <bitmap-file> [iterations] [eta] [hidden-nodes] [<bitmap-file2>]");
      System.exit(1);
    }
    String[] files = {args[0], args[1]};
    int iterations = 70000;
    double eta = 0.70;
    int hidden = 32;
    String validation = null;
    for (int i = 2; i < args.length; i++) {
      switch(i) {
        case 2:
          iterations = Integer.parseInt(args[2]);
          break;
        case 3:
          eta = Double.parseDouble(args[3]);
          break;
        case 4:
          hidden = Integer.parseInt(args[4]);
          break;
        case 5:
          validation = args[5];
          break;
      }
    }
    if (validation == null) {
        new TrainClassifier(args, iterations, eta, hidden);
    } else { 
        new TrainClassifier(args, iterations, eta, hidden, validation);
    }
    System.out.println("Done.");
  }

}
