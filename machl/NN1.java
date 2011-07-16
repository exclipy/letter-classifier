package machl;
import java.util.*;
import java.io.Serializable;

/**
 * <p>A basic implementation of a single-layered feedforward neural network and backpropagation learning.</p>
 * @author Mikael Boden
 * @version 1.0
 */


/* Here's a list of things one can do:
 *  1. implement batched (epoch-based) weight update
 *  2. add "momentum" to weight adjustments
 *  3. enable more node layers (hidden layers).
 *  4. try alternative output functions (e.g. hyperbolic tangens, or softmax)
 *  5. try alternative error measures for gradient calculations (maximum likelihood)
 *  6. after a hidden layer has been added, a simple recurrent network can be constructed (see Elman, 1990)
 */

public class NN1 implements Serializable {
  double[] o;             // the values produced by each node (indices important, see weights/biases)
  double[] h;             // the values produced by each hidden node (indices important, see weights/biases)
  public double[][] w;    // the trainable weight values [to node][from node]
  public double[][] wh;   // the trainable weight values for the hidden layer [to node][from input]
  public double[] bias;   // the trainable bias values for nodes
  public double[] biash;  // the trainable bias values for hidden nodes
  Random rand;            // a random number generator for initial weight values

  /** Constructs a single-hidden layer neural network structure and initializes weights to
   *  small random values.
   *  @param  nInput  Number of input nodes
   *  @param  nHidden Number of hidden nodes
   *  @param  nOutput Number of output nodes
   *  @param  seed    Seed for the random number generator used for initial weights.
   *
   */
  public NN1(int nInput, int nHidden, int nOutput, int seed) {

    // allocate space for node and weight values
    o=new double[nOutput];
    h=new double[nHidden];
    w=new double[nOutput][nHidden];
    wh=new double[nHidden][nInput];
    bias=new double[nOutput];
    biash=new double[nHidden];

    // initialize weight and bias values
    rand=new Random(seed);
    for (int q=0; q<nHidden; q++) {
      for (int j=0; j<nInput; j++) {
        wh[q][j]=rand.nextGaussian()*.1;
      }
      biash[q]=rand.nextGaussian()*.1;
    }
    for (int k=0; k<nOutput; k++) {
      for (int q=0; q<nHidden; q++) {
        w[k][q]=rand.nextGaussian()*.1;
      }
      bias[k]=rand.nextGaussian()*.1;
    }
  }

  /** The so-called output function. Computes the output value of a node given the summed incoming activation.
   *  You can use anyone you like if it is differentiable.
   *  This one is called the logistic function (a sigmoid) and produces values bounded between 0 and 1.
   *  @param  net The summed incoming activation
   *  @return double
   */
  public double outputFunction(double net) {
    return 1.0/(1.0+Math.exp(-net));
  }

  /** The derivative of the output function.
   *  This one is the derivative of the logistic function which is efficiently computed with respect to the output value
   *  (if you prefer computing it wrt the net value you can do so but it requires more computing power.
   *  @param  x The value by which the gradient is determined.
   *  @return double  the gradient at x.
   */
  public double outputFunctionDerivative(double x) {
    return x*(1.0-x);
  }

  /** Computes the output values of the output nodes in the network given input values.
   *  @param  x  The input values.
   *  @return double[]    The vector of computed output values
   */
  public double[] feedforward(double[] x) {
    // compute the activation of each hidden node (depends on input values)
    for (int q=0; q<h.length; q++) {
      double sum=0; // reset summed activation value
      for (int j=0; j<x.length; j++)
        sum+=x[j]*wh[q][j];
      h[q]=outputFunction(sum+biash[q]);
    }

    // compute the activation of each output node (depends on hidden values)
    for (int k=0; k<o.length; k++) {
      double sum=0; // reset summed activation value
      for (int q=0; q<h.length; q++)
        sum+=h[q]*w[k][q];
      o[k]=outputFunction(sum+bias[k]);
    }
    return o;
  }

  /**
   * Computes the RMS error between actual outputs after feeding through x,
   * compared to desired outputs d.  Does not modify weights.
   */
  public double error(double[] x, double[] d) {
    double[] o = feedforward(x);
    double rmse=0;
    for (int k=0; k<o.length; k++) {
      double diff=d[k]-o[k];
      rmse+=diff*diff;
    }
    return Math.sqrt(rmse/o.length);
  }

  /** Adapts weights in the network given the specification of which values that should appear at the output (target)
   *  when the input has been presented.
   *  The procedure is known as error backpropagation. This implementation is "online" rather than "batched", that is,
   *  the change is not based on the gradient of the global error, merely the local -- pattern-specific -- error.
   *  Variable names used here are in line with the COMP3702 notes.
   *  @param  x  The input values.
   *  @param  d  The desired output values.
   *  @param  eta     The learning rate, always between 0 and 1, typically a small value, e.g. 0.1
   *  @return double  An error value (the root-mean-squared-error).
   */
  public double train(double[] x, double[] d, double eta) {

    // present the input and calculate the outputs
    feedforward(x);

    double[] error=new double[o.length];
    // compute the error of output nodes (explicit target is available -- so quite simple)
    // also, calculate the root-mean-squared-error to indicate progress
    double rmse=0;
    for (int k=0; k<o.length; k++) {
      double diff=d[k]-o[k];
      error[k]=diff*outputFunctionDerivative(o[k]);
      rmse+=diff*diff;
    }
    rmse=Math.sqrt(rmse/o.length);

    // now compute the error of hidden nodes (need to calculate contribution of each hidden node to final output)
    // allocate space for errors of individual nodes
    double[] errorh=new double[h.length];
    for (int q=0; q<h.length; q++) {
      errorh[q]=0;
      for (int k=0; k<o.length; k++) {
        errorh[q]+=error[k]*w[k][q];
      }
      errorh[q]*=outputFunctionDerivative(h[q]);
    }

    // change weights of the output nodes according to errors
    for (int k=0; k<o.length; k++) {
      for (int q=0; q<h.length; q++) {
        w[k][q]+=error[k]*h[q]*eta;
      }
      bias[k]+=error[k]*1.0*eta; // bias can be understood as a weight from a node which is always 1.0.
    }

    // change weights of the hidden nodes according to errors
    for (int q=0; q<h.length; q++) {
      for (int j=0; j<x.length; j++) {
        wh[q][j]+=errorh[q]*x[j]*eta;
      }
      biash[q]+=errorh[q]*1.0*eta; // bias can be understood as a weight from a node which is always 1.0.
    }

    return rmse;
  }

}
