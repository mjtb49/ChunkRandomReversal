package src;

import java.util.ArrayList;
import java.util.Random;

public class CarverReversalHelper {

    private static long makeMask(int bits) {
        return (1L << bits) - 1;
    }

    private static final long m1 = 25214903917L; //the next 5 lines are constants for the lcg created by calling java lcg multiple times.

    private static final long m2 = 205749139540585L;
    private static final long addend2 = 277363943098L;

    private static final long m4 = 55986898099985L;
    private static final long addend4 = 49720483695876L;

    private static int countTrailingZeroes(long v) {
        int c;  // output: c will count v's trailing zero bits,
        // so if v is 1101000 (base 2), then c will be 3
        v = (v ^ (v - 1)) >> 1;  // Set v's trailing 0s to 1s and zero rest
        for (c = 0; v !=0; c++)
        {
            v >>>= 1;
        }
        return c;
    }

    private long getPartialCarverSeed(long bitsOfSeed, int x, int z) {
        long a = (((m2*(bitsOfSeed ^ m1)+addend2) & makeMask(48) )>>>16);
        long b = (((m4*(bitsOfSeed ^ m1)+addend4) & makeMask(48) )>>>16);
        return ((x*a ^ z*b) ^ bitsOfSeed) & makeMask(32);
    }

    private long getCarverSeed(long worldSeed, int chunkX, int chunkZ) {
        Random r = new Random(worldSeed);
        long long6 = r.nextLong();
        long long8 = r.nextLong();
        return (chunkX * long6 ^ chunkZ * long8 ^ worldSeed) & makeMask(48);
    }

    private int countMatchingLowOrderBits(long a, long b) {
        return countTrailingZeroes(a ^ b);
    }

    ArrayList<Long> reverseCarverSeed(long carverSeed, int x, int z) {


        ArrayList<Long> results = new ArrayList<>();

        if (x == 0 && z == 0) {
            results.add(carverSeed);
            return results;
        }
        ArrayList<Long> possibleBottomBits = new ArrayList<>();
        for (long c = 0; c < (1L <<16); c++) {
            //for (long c = 17411; c <= 17411; c++) {
            possibleBottomBits.add(c);
            int bitsWhichMatch = 0;
            while (!possibleBottomBits.isEmpty() && bitsWhichMatch < 32) {
                for (int i = possibleBottomBits.size()-1; i >= 0; i--) {
                    if (countMatchingLowOrderBits(getPartialCarverSeed(possibleBottomBits.get(i)
                            | (1L << (16 + bitsWhichMatch)),x,z),carverSeed) > bitsWhichMatch)
                    {
                        possibleBottomBits.add(possibleBottomBits.get(i) | (1L << (16 + bitsWhichMatch)));
                    }
                    if (!(countMatchingLowOrderBits(getPartialCarverSeed(possibleBottomBits.get(i),x,z),carverSeed) > bitsWhichMatch)) {
                        possibleBottomBits.remove(i);
                    }
                }
                bitsWhichMatch+=1;
            }
            for (int i = possibleBottomBits.size()-1; i >= 0; i--) {
                long seed = possibleBottomBits.remove(i);
                if (getCarverSeed(seed,x,z) == carverSeed) {
                    results.add(seed & makeMask(48));
                }
            }
        }

        return results;
    }
}
