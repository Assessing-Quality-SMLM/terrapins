package com.coxphysics.terrapins.models.squirrel.tools;

import com.coxphysics.terrapins.models.squirrel.utils.ArrayUtils;
import ij.process.FloatProcessor;

import java.util.ArrayList;


import static com.coxphysics.terrapins.models.squirrel.utils.ArrayUtils.toDoubleArray;
import static java.lang.Math.pow;
import static java.lang.Math.sqrt;

/**
 * Created by sculley on 11/07/2019.
 */
public class SQUIRRELMathTools_ {

    /**
     * returns the value of the mean of a float array
     * @param numbers
     * @return mean(numbers)
     */
    public static float getAverageValue(float[] numbers){
        double v = 0;
        for(int i=0; i<numbers.length; i++){
            v += numbers[i] / numbers.length;
        }
        return (float) v;
    }

    /**
     * returns the value of the mean of a double array
     * @param numbers
     * @return mean(numbers)
     */
    public static double getAverageValue(double[] numbers){
        double v = 0;
        for(int i=0; i<numbers.length; i++){
            v += numbers[i] / numbers.length;
        }
        return v;
    }


    public static double calculatePPMCC(float[] array1, float[] array2, boolean doMeanSubtraction) {
        double[] dArray1 = ArrayUtils.toDoubleArray(array1);
        double[] dArray2 = ArrayUtils.toDoubleArray(array2);

        return calculatePPMCC(dArray1, dArray2, doMeanSubtraction);
    }

    public static double calculatePPMCC(double[] dArray1, double[] dArray2, boolean doMeanSubtraction){
        if (doMeanSubtraction) {
            dArray1 = dArray1.clone();
            dArray2 = dArray2.clone();
            double mean;
            mean = getAverageValue(dArray1);
            for (int n=0; n<dArray1.length; n++) dArray1[n] -= mean;
            mean = getAverageValue(dArray2);
            for (int n=0; n<dArray2.length; n++) dArray2[n] -= mean;
        }

        double covariance = 0;
        double squareSum1  = 0;
        double squareSum2  = 0;
        for (int n=0; n<dArray1.length; n++) {
            double v0 = dArray1[n];
            double v1 = dArray2[n];
            covariance += v0*v1;
            squareSum1 += v0*v0;
            squareSum2 += v1*v1;
        }
        double similarity = 0;
        if (squareSum1 !=0 && squareSum2 != 0) similarity = covariance / sqrt(squareSum1 * squareSum2);
        return similarity;
    }

    public static double calculatePPMCCNans(FloatProcessor fp1, FloatProcessor fp2, boolean doMeanSubtraction){
        return calculatePPMCCNans((float[]) fp1.getPixels(), (float[]) fp2.getPixels(), doMeanSubtraction);
    }

    public static double calculatePPMCCNans(float[] array1, float[] array2, boolean doMeanSubtraction){
        double[] dArray1 = ArrayUtils.toDoubleArray(array1);
        double[] dArray2 = ArrayUtils.toDoubleArray(array2);

        return calculatePPMCCNans(dArray1, dArray2, doMeanSubtraction);
    }

    public static double calculatePPMCCNans(double[] dArray1, double[] dArray2, boolean doMeanSubtraction){

        if (doMeanSubtraction) {
            dArray1 = dArray1.clone();
            dArray2 = dArray2.clone();
            double mean1=0, mean2=0;
            int countNotNan = 0;

            for(int i=0; i<dArray1.length; i++){
                double v1 = dArray1[i];
                double v2 = dArray2[i];
                if(Double.isNaN(v1) || Double.isNaN(v2)) continue;
                mean1 += v1;
                mean2 += v2;
                countNotNan++;
            }

            mean1 /= countNotNan;
            mean2 /= countNotNan;

            for (int n=0; n<dArray1.length; n++) dArray1[n] -= mean1;
            for (int n=0; n<dArray2.length; n++) dArray2[n] -= mean2;
        }

        double covariance = 0;
        double squareSum1  = 0;
        double squareSum2  = 0;
        int nanCount = 0;
        for (int n=0; n<dArray1.length; n++) {
            double v0 = dArray1[n];
            double v1 = dArray2[n];
            if(Double.isNaN(v0) || Double.isNaN(v1)) {nanCount++; continue;}
            covariance += v0*v1;
            squareSum1 += v0*v0;
            squareSum2 += v1*v1;
        }
        double similarity = 0;
        if (squareSum1 !=0 && squareSum2 != 0) similarity = covariance / sqrt(squareSum1 * squareSum2);
        System.out.println("NaN count is "+nanCount);
        return similarity;
    }

    /**
     * @param array1
     * @param array2
     * @return
     */
    public static double calculateMSE(float[] array1, float[] array2) {
        double MSE = 0;
        int counter = 1;

        for (int n=0; n<array1.length; n++) {
            float v0 = array1[n];
            float v1 = array2[n];
            if (Float.isNaN(v0) || Float.isNaN(v1)) continue;

            MSE += (pow(v0-v1,2)-MSE)/counter;
            counter++;
        }
        return MSE;
    }

    public static float[] purgeBadValues(int[] badIndices, float[] array){

        float[] purgedArray = new float[array.length-badIndices.length];

        int counter = 0;

        for(int i=0; i<array.length; i++){
            boolean isGood = true;

            for(int j=0; j<badIndices.length; j++){
                if(i==badIndices[j]) isGood=false;
            }

            if(isGood){
                purgedArray[counter] = array[i];
                counter++;
            }

        }

        return purgedArray;

    }

    public static int[] getIndicesNegative(float[] array){
        ArrayList<Integer> negativeList = new ArrayList<Integer>();
        for(int i=0; i<array.length; i++){
            if(array[i]<0) negativeList.add(i);
        }
        return arrayListToIntArray(negativeList);
    }

    public static int[] arrayListToIntArray(ArrayList<Integer> arrayList){
        int nElements = arrayList.size();
        int[] array = new int[nElements];

        for(int n=0; n<nElements; n++) array[n] = arrayList.get(n);
        return array;
    }
}
