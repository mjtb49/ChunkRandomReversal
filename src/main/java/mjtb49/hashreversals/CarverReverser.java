package mjtb49.hashreversals;

import kaptainwutax.seedutils.mc.ChunkRand;
import kaptainwutax.seedutils.mc.MCVersion;
import kaptainwutax.seedutils.mc.seed.ChunkSeeds;
import kaptainwutax.seedutils.util.math.Mth;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class CarverReverser {

    public static List<Long> reverse(long carverSeed, int x, int z, ChunkRand rand, MCVersion version) {
        ArrayList<Long> result = new ArrayList<>();
        Hensel.Hash carverHash = value -> rand.setCarverSeed(value, x, z, version);

        int freeBits = Long.numberOfTrailingZeros(x | z);
        long c = carverSeed & Mth.mask(freeBits);

        if(freeBits >= 16) {
            Hensel.lift(c, freeBits - 16, carverSeed, 32, 16, carverHash, result);
        } else {
            for(int increment = (int)Mth.pow2(freeBits); c < 1L << 16; c += increment) {
                Hensel.lift(c, 0, carverSeed, 32, 16, carverHash, result);
            }
        }

        return result;
    }

    //==========================================================================================//

    private static long makeMask(int bits) {
        return (1L << bits) - 1;
    }

    private static final long m1 = 25214903917L; //the next 5 lines are constants for the lcg created by calling java lcg multiple times.

    private static final long m2 = 205749139540585L;
    private static final long addend2 = 277363943098L;

    private static final long m4 = 55986898099985L;
    private static final long addend4 = 49720483695876L;

    public static ArrayList<Long> reverseCarverSeed(long carverSeed, int x, int z) {
        ArrayList<Long> results = new ArrayList<>();

        if (x == 0 && z == 0) {
            results.add(carverSeed);
            return results;
        }
        ArrayList<Long> possibleBottomBits = new ArrayList<>();
        for (long c = 0; c < 1L << 16; c+=1) {
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

                //System.out.println(possibleBottomBits);
                bitsWhichMatch+=1;
            }
            for (int i = possibleBottomBits.size()-1; i >= 0; i--) {
                long seed = possibleBottomBits.remove(i);
                if(getCarverSeed(seed,x,z) == carverSeed) {
                    results.add(seed & makeMask(48));
                }
            }
        }

        return results;
    }

    private static long getPartialCarverSeed(long bitsOfSeed, int x, int z) {
        long a = (((m2*(bitsOfSeed ^ m1)+addend2) & makeMask(48) )>>>16);
        long b = (((m4*(bitsOfSeed ^ m1)+addend4) & makeMask(48) )>>>16);
        return ((x*a ^ z*b) ^ bitsOfSeed) & makeMask(32);
    }

    private static long getCarverSeed(long worldSeed, int chunkX, int chunkZ) {
        Random r = new Random(worldSeed);
        long long6 = r.nextLong();
        long long8 = r.nextLong();
        return (chunkX * long6 ^ chunkZ * long8 ^ worldSeed) & makeMask(48);
    }

    private static int countMatchingLowOrderBits(long a, long b) {
        return Long.numberOfTrailingZeros(a ^ b);
    }

}