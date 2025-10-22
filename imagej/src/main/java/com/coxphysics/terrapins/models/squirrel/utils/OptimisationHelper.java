package com.coxphysics.terrapins.models.squirrel.utils;

import java.text.DecimalFormat;
import java.util.ArrayList;

import org.apache.commons.math3.analysis.UnivariateFunction;
import org.apache.commons.math3.optim.MaxEval;
import org.apache.commons.math3.optim.nonlinear.scalar.GoalType;
import org.apache.commons.math3.optim.univariate.BrentOptimizer;
import org.apache.commons.math3.optim.univariate.SearchInterval;
import org.apache.commons.math3.optim.univariate.UnivariateObjectiveFunction;
import org.apache.commons.math3.optim.univariate.UnivariateOptimizer;
import org.apache.commons.math3.optim.univariate.UnivariatePointValuePair;

import ij.process.FloatProcessor;

import static com.coxphysics.terrapins.models.squirrel.utils.ArrayUtils.toDoubleArray;


public class OptimisationHelper {

    public static class SigmaOptimiseFunction implements UnivariateFunction {

        private final FloatProcessor fpSR, fpRef, fpOnes;
        private final int w_SR, h_SR, w_Ref, h_Ref;
        private final double[] pixelsRef;
        private final ArrayList<Double> sigmaList = new ArrayList<Double>();
        private final ArrayList<Double> errorList = new ArrayList<Double>();
        private final DecimalFormat df = new DecimalFormat("00.00");

        public SigmaOptimiseFunction(FloatProcessor fpSR, FloatProcessor fpRef){
            this.fpSR = fpSR;
            this.fpRef = fpRef;
            w_SR = fpSR.getWidth();
            h_SR = fpSR.getHeight();
            w_Ref = fpRef.getWidth();
            h_Ref = fpRef.getHeight();
            pixelsRef = toDoubleArray((float[]) fpRef.getPixels());
            fpOnes = new FloatProcessor(w_SR, h_SR);
            fpOnes.set(1);
        }
        
        public FloatProcessor affineAndBlurAndDownscale(FloatProcessor fp, double alpha, double beta, double sigma) {
            fp = (FloatProcessor) fp.duplicate();
            if (alpha != 1) fp.multiply(alpha);
            if (beta != 0) fp.add(beta);
            fp.blurGaussian(sigma);
            return (FloatProcessor) fp.resize(w_Ref, h_Ref);
        }

        public FloatProcessor blurAndDownscale(FloatProcessor fp, double sigma) {
            return affineAndBlurAndDownscale(fp, 1, 0, sigma);
        }
        
        public double[] optimalAlphaAndBeta(double sigma) {
            FloatProcessor blurredFpSR = blurAndDownscale(fpSR, sigma);
            FloatProcessor blurredOnes = blurAndDownscale(fpOnes, sigma);
            return calculateAlphaBeta(toDoubleArray(blurredFpSR), pixelsRef, toDoubleArray(blurredOnes));
        }

        public double value(double sigma) {
            double[] aB = optimalAlphaAndBeta(sigma);
            double alpha = aB[0], beta = aB[1];

            FloatProcessor finalFpSR = affineAndBlurAndDownscale(fpSR, alpha, beta, sigma);
            double error = calculateRMSE(pixelsRef, toDoubleArray(finalFpSR));
            
            //IJ.showStatus("Optimising... sigma="+df.format(sigma)+", alpha="+df.format(aB[0])+", beta="+df.format(aB[1])+". Error="+df.format(error));
            sigmaList.add(sigma);
            errorList.add(error);
            return error;
        }
    }

    public static double calculateRMSE(double[] array1, double[] array2){
        // belongs in MathsHelper
        int N = array1.length;
        double MSE = 0;

        for(int i=0; i<N; i++){
            MSE += (array1[i]-array2[i])*(array1[i]-array2[i]);
        }
        MSE /= N;

        return Math.sqrt(MSE);
    }

    public static double[] calculateAlphaBeta(double[] xA, double[] y, double[] oneA){
         /*
            xA = scaled and translated super-resolution
            oneA = scaled ones
            y =  reference
            */

        double N = 0;
        for(int i=0; i<oneA.length; i++){
            N += oneA[i]*oneA[i];
        }

        double nPixels = xA.length;
        assert(nPixels==y.length);
        assert(nPixels==oneA.length);


        double xATxA = 0, xAT1A = 0, yTxA = 0, yT1A = 0;

        for(int i=0; i<nPixels; i++){
            yTxA += y[i]*xA[i];
            yT1A += y[i]*oneA[i];
            xAT1A += xA[i]*oneA[i];
            xATxA += xA[i]*xA[i];
        }

        double numerator = N*yTxA - yT1A*xAT1A;
        double denominator = N*xATxA  - xAT1A*xAT1A;
        double alphaHat = numerator/denominator;
        double betaHat = yT1A/N - alphaHat*(xAT1A/N);

        return new double[] {alphaHat, betaHat};
    }

    public static double[] linearMatching(FloatProcessor fpSR, FloatProcessor fpRef, double boundary, double maxSigma) {

        /// setup optimizer
        SigmaOptimiseFunction f = new SigmaOptimiseFunction(fpSR, fpRef);
        UnivariateOptimizer optimizer = new BrentOptimizer(1e-10, 1e-14);

        /// run optimizer
        //long startTime = System.currentTimeMillis();
        UnivariatePointValuePair result = optimizer.optimize(new MaxEval(1000),
                new UnivariateObjectiveFunction(f), GoalType.MINIMIZE, new SearchInterval(0, boundary));
        //long stopTime = System.currentTimeMillis();
        double nIterations = optimizer.getIterations();
        //IJ.log("optimizer completed in "+(stopTime-startTime+"ms"));

        // get optimal sigma (clipped at maxSigma)
        double sigma = result.getPoint();
        double optimalSigma = Math.min(sigma, maxSigma);
        // get optimal alpha and beta (based on sigma)
        //double[] aB = f.optimalAlphaAndBeta(sigma); //TODO: should this be optimalSigma
        double[] aB = f.optimalAlphaAndBeta(optimalSigma);
        double alpha = aB[0], beta = aB[1];

        // why return sigma = -1 if alpha is negative?
        //return new double[]{alpha, beta, (alpha < 0 ? -1 : sigma)};
        return new double[]{alpha, beta, optimalSigma, sigma, nIterations};
    }

}
