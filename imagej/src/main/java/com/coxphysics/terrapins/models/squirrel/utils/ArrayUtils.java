package com.coxphysics.terrapins.models.squirrel.utils;

import java.util.*;
import java.util.List;

import ij.process.FloatProcessor;

public class ArrayUtils {

    public static int ravel(int x, int y, int w) {
        return y * w + x;
    }

    public static int[] unravel(int ind, int w) { return new int[]{ind % w, ind / w}; }

    public static double[] doSort(double[] array){        
        Arrays.sort(array);
        return array;
//        int i = 1;
//        double x;
//        int j;
//
//        while(i < array.length){
//            x = array[i];
//            j = i-1;
//            while(j>=0 && array[j] > x){
//                array[j+1] = array[j];
//                j--;
//            }
//            array[j+1] = x;
//            i++;
//        }
//        return array;
    }

    public static double[] toDoubleArray(float[] array){
        if (array == null) return null;
        int n = array.length;
        double[] dArray = new double[n];
        for(int i=0; i<n; i++) dArray[i] = array[i];
        return dArray;
    }
    
    public static double[] toDoubleArray(FloatProcessor fp) {
        return toDoubleArray((float[]) fp.getPixels());
    }

    public static Integer[] randomIntArray(int size, int bound){
        assert size<bound;
        Random random = new Random();
        ArrayList<Integer> list = new ArrayList<>();
        int i=0;
        while(list.size()<size){
            int testInt = random.nextInt(bound);
            if(list.contains(testInt)) continue;
            list.add(testInt);
        }

        return list.toArray(new Integer[size]);
    }

    public static float[] doubleArrayToFloatArray(double[] array){
        float[] array_ = new float[array.length];
        for(int i=0; i<array.length; i++) array_[i] = (float) array[i];
        return array_;
    }

    public static double[] toDoubleArray(ArrayList<Double> list){
        double[] array = new double[list.size()];
        for(int i=0; i<array.length; i++) array[i] = list.get(i);
        return array;
    }

    public static float[] toFloatArray(ArrayList<Float> list){
        float[] array = new float[list.size()];
        for(int i=0; i<array.length; i++) array[i] = list.get(i);
        return array;
    }

    public static int[] toIntegerArray(ArrayList<Integer> list){
        int[] array = new int[list.size()];
        for(int i=0; i<array.length; i++) array[i] = list.get(i);
        return array;
    }

    public static ArrayList<Double> toDoubleArrayList(double[] array){
        int nElements = array.length;
        ArrayList<Double> list = new ArrayList<>();
        for(int n=0; n<nElements; n++) list.add(array[n]);
        return  list;
    }

    public static double median(double[] array){
        double[] sortedArray = array.clone();
        Arrays.sort(sortedArray);
        int numEls = sortedArray.length;
        int m = numEls/2;
        double median;
        if(numEls % 2 == 0) median = (sortedArray[m-1]+sortedArray[m])/2;
        else median = sortedArray[m];

        return median;
    }

    // lifted from stardist-imagej code
    public static List<Integer> argsortDescending(final List<Float> list) {
        Integer[] indices = new Integer[list.size()];
        for (int i = 0; i < indices.length; i++) indices[i] = i;
        Arrays.sort(indices, new Comparator<Integer>() {
            @Override
            public int compare(Integer i, Integer j) {
                return -Float.compare(list.get(i), list.get(j));
            }
        });
        return Arrays.asList(indices);
    }
}
