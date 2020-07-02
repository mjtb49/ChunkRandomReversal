package mjtb49.hashreversals;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.util.ArrayList;

public class FindSolutionsInBox {

    private static TwoDimBigMatrix lagrangeGauss(TwoDimBigMatrix matrix) {
        TwoDimBigMatrix r = new TwoDimBigMatrix(matrix);
        do {
            if (r.getRow(0).normSq().compareTo( r.getRow(1).normSq()) > 0) {
                r = r.swapRows();
            }
            BigDecimal mu = r.getRow(0).dot(r.getRow(1))
                    .divide(r.getRow(0).normSq(), RoundingMode.HALF_UP).setScale(0, RoundingMode.HALF_UP);
            TwoDimBigVector temp = r.getRow(0).scale(mu.negate());
            r.setRow(1, r.getRow(1).add(temp));
        } while (r.getRow(0).normSq().compareTo(r.getRow(1).normSq()) > 0);
        return r;
    }

    /**
     * Solves for a*x + b*z = target - c (modulo mod) under the condition that min < (x,z) < max
     */
    public static ArrayList<TwoDimBigVector> findSolutionsInBox(long a, long b, long target, long mod, TwoDimBigVector min, TwoDimBigVector max) {
        target = (target) % mod; //from here on we can think of this as solving a*x + b*z = target
        BigInteger bigMod = BigInteger.valueOf(mod);
        //TODO b is not necessarily invertible
        BigInteger binv = BigInteger.valueOf(b).modInverse(bigMod);

        //We find all solutions by constructing this trivial solution then adding vectors until it is within the bounds
        BigDecimal newZCenter = new BigDecimal(binv.multiply(BigInteger.valueOf(target)).mod(bigMod));
        TwoDimBigVector initialSolution = new TwoDimBigVector(BigDecimal.ZERO, newZCenter);

        //affine transformation to update the region we are targeting to have our initial solution at 0 0
        min = min.add(new TwoDimBigVector(BigDecimal.ZERO, newZCenter.negate()));
        max = max.add(new TwoDimBigVector(BigDecimal.ZERO, newZCenter.negate()));

        //the rows generate the set of vectors we can add to x and z and leave a*x+b*z unchanged
        TwoDimBigMatrix basis = new TwoDimBigMatrix();
        basis.setRow(0, new TwoDimBigVector(BigDecimal.ZERO, new BigDecimal(mod)));
        basis.setRow(1, new TwoDimBigVector(BigDecimal.ONE, new BigDecimal(binv.multiply(BigInteger.valueOf(a).negate()))));

        //You don't strictly have to do this but it is much faster
        TwoDimBigMatrix reducedBasis = lagrangeGauss(basis);
        TwoDimBigMatrix inv = reducedBasis.inverse();

        //Overkill - gets mins and maxes for what x and z in the space transformed by inv can be
        BigDecimal[] transformedMins = {BigDecimal.ZERO, BigDecimal.ZERO};
        BigDecimal[] transformedMaxes = {BigDecimal.ZERO, BigDecimal.ZERO};

        for (int row = 0; row < 2; row++) {
            for (int col = 0; col < 2; col++) {
                BigDecimal element = inv.getElement(row,col);
                if (element.signum() >= 0) {
                    transformedMaxes[col] = transformedMaxes[col].add(element.multiply(max.getElement(row)));
                    transformedMins[col] = transformedMins[col].add(element.multiply(min.getElement(row)));
                } else {
                    transformedMaxes[col] = transformedMaxes[col].add(element.multiply(min.getElement(row)));
                    transformedMins[col] = transformedMins[col].add(element.multiply(max.getElement(row)));
                }
            }
        }

        //We can finally iterate over solutions
        //TODO if we ever have to use large non powers of two as mod this will break!! - also if you are using large non powers of two as mod wtf?
        ArrayList<TwoDimBigVector> validCoords = new ArrayList<>();
        for (long x = transformedMins[0].longValue() - 2; x < transformedMaxes[0].longValue() + 2; x++) {
            for (long z = transformedMins[1].longValue() - 2; z < transformedMaxes[1].longValue() + 2; z++) {
                TwoDimBigVector coords = (new TwoDimBigVector(BigDecimal.valueOf(x), BigDecimal.valueOf(z))).multiply(reducedBasis);
                if (min.le(coords) && coords.le(max)) {
                    validCoords.add(coords.add(initialSolution));
                }
            }
        }
        return validCoords;
    }
}
