package mjtb49.hashreversals;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.Arrays;

public class TwoDimBigVector {
    private final int SIZE = 2;
    private BigDecimal[] vector;

    protected TwoDimBigVector() {
        vector = new BigDecimal[SIZE];
        Arrays.fill(vector, BigDecimal.ZERO);
    }

    protected TwoDimBigVector(BigDecimal a, BigDecimal b) {
        vector = new BigDecimal[SIZE];
        vector[0] = a;
        vector[1] = b;
    }

    protected BigDecimal getElement(int index) {
        return vector[index];
    }

    protected void setElement(int index, BigDecimal element) {
        vector[index] = element;
    }

    protected void setElement(int index, long element) {
        vector[index] = BigDecimal.valueOf(element);
    }

    protected BigDecimal dot(TwoDimBigVector other) {
        BigDecimal result = BigDecimal.ZERO;
        for (int i = 0; i < SIZE; i++) {
            result = result.add(vector[i].multiply(other.getElement(i)));
        }
        return result;
    }

    protected TwoDimBigVector multiply(TwoDimBigMatrix matrix) {
        TwoDimBigVector result = new TwoDimBigVector();
        result.setElement(0,this.dot(matrix.getCol(0)));
        result.setElement(1,this.dot(matrix.getCol(1)));
        return result;
    }

    protected TwoDimBigVector scale(BigDecimal scalar) {
        TwoDimBigVector result = new TwoDimBigVector();
        for (int i = 0; i < SIZE; i++) {
            result.setElement(i,vector[i].multiply(scalar));
        }
        return result;
    }

    protected TwoDimBigVector add(TwoDimBigVector other) {
        TwoDimBigVector result = new TwoDimBigVector();
        for (int i = 0; i < SIZE; i++) {
            result.setElement(i,vector[i].add(other.getElement(i)));
        }
        return result;
    }

    protected boolean le(TwoDimBigVector other) {
        return ((vector[0].compareTo(other.getElement(0))) <= 0) &&
                ((vector[1].compareTo(other.getElement(1))) <= 0);
    }

    protected BigDecimal normSq() {
        return (vector[0].pow(2).add(vector[1].pow(2)));
    }

    public String toString(){
        return "[ "+vector[0]+" "+vector[1]+" ]";
    }
}
