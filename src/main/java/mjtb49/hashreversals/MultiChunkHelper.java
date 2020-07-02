package mjtb49.hashreversals;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Random;

public class MultiChunkHelper {

    //TODO I use these values a lot - maybe a math class??
    private static final long m1 = 25214903917L; //the next 5 lines are constants for the lcg created by calling java lcg multiple times.

    private static final long m2 = 205749139540585L;
    private static final long addend2 = 277363943098L;

    private static final long m4 = 55986898099985L;
    private static final long addend4 = 49720483695876L;
    private static final int NUM_CHUNKS_IN_WB = 1875000 * 16;
    private long k1;
    private long k2;
    private int dx;
    private int dz;
    private ArrayList<Result> results;
    private ArrayList<Long> seeds;

    MultiChunkHelper() {
        seeds = new ArrayList<>();
        results = new ArrayList<>();
    }

    private static long makeMask(int bits) {
        if (bits == 64)
            return -1;
        else
            return (1L << bits) - 1;
    }

    private static long getA( long partialSeed, int bits) {
        long mask = makeMask(bits + 16);
        long mask2 = makeMask(bits);
        return ((((int)(((m2*((partialSeed^m1)&mask) + addend2) & ((1L<<48)-1)) >>> 16))|1)) & mask2;
    }

    private static long getB( long partialSeed, int bits) {
        long mask = makeMask(bits + 16);
        long mask2 = makeMask(bits);
        return ((((int)(((m4*((partialSeed^m1)&mask) + addend4) & ((1L<<48)-1)) >>> 16))|1)) & mask2;
    }

    private static long getChunkseed13Plus(long seed, int x, int z) {
        Random r = new Random(seed);
        long a = r.nextLong()|1;
        long b = r.nextLong()|1;
        return ((x*a + z*b)^seed) & ((1L << 48) - 1);
    }

    ArrayList<Result> getWorldseedFromTwoChunkseeds(long chunkseed1, long chunkseed2, int blockDx, int blockDz) {
        results = new ArrayList<>();
        this.dx = blockDx;
        this.dz = blockDz;
        k1 = chunkseed1;
        k2 = chunkseed2;
        for (long c = 0; c < (1L << 17); c++) {
            growSolution(c,17);
        }
        for (long seed: seeds)
            results.addAll(findSeedsInWB(chunkseed1,seed));
        return results;
    }

    private void growSolution(long c, int bitsOfSeedKnown) {

        //System.out.println(bitsOfSeedKnown);
        if(bitsOfSeedKnown == 48) {
            if((((k2^c) - (k1^c)) & makeMask(48)) == ((getChunkseed13Plus(c,dx,dz) ^ c) & makeMask(48))) {
                seeds.add(c);
            }
            return;
        }
        int bitsOfVectorKnown = bitsOfSeedKnown - 16;
        //solve k2^c - k1^c = <dx, dz>*<a,b> mod 1 << bitsOfVectorKnown + 1
        if ((((k2^c) - (k1^c)) & makeMask(bitsOfVectorKnown+1)) == ((dx*getA(c,bitsOfVectorKnown+1) +dz*getB(c,bitsOfVectorKnown+1)& makeMask(bitsOfVectorKnown+1)))) {
            growSolution(c,bitsOfSeedKnown+1);
        }
        c += 1L << bitsOfSeedKnown;
        if ((((k2^c) - (k1^c)) & makeMask(bitsOfVectorKnown+1)) == ((dx*getA(c,bitsOfVectorKnown+1) +dz*getB(c,bitsOfVectorKnown+1)& makeMask(bitsOfVectorKnown+1)))) {
            growSolution(c,bitsOfSeedKnown+1);
        }
    }

    private static ArrayList<Result> findSeedsInWB(long target, long seed) {

        ArrayList<Result> validPositions = new ArrayList<>();

        long goal = (target ^ seed) & makeMask(48);
        Random r = new Random(seed);
        long a = (r.nextLong()|1);
        long b = (r.nextLong()|1);

        TwoDimBigVector mins = new TwoDimBigVector(-NUM_CHUNKS_IN_WB, -NUM_CHUNKS_IN_WB);
        TwoDimBigVector maxes = new TwoDimBigVector(NUM_CHUNKS_IN_WB, NUM_CHUNKS_IN_WB);

        for (TwoDimBigVector v : FindSolutionsInBox.findSolutionsInBox(a,b,goal,(1L << 48), mins, maxes)) {
            int x = (int) v.getElement(0).longValue();
            int z = (int) v.getElement(1).longValue();
            if (x % 16 == 0 && z % 16 == 0)
                validPositions.add(new Result(seed, x, z));
        }

        return validPositions;
    }

    static final class Result {
        private long bitsOfSeed;
        private int x;
        private int z;

        Result(long bitsOfSeed, int x, int z) {
            this.bitsOfSeed =bitsOfSeed;
            this.x = x;
            this.z = z;
        }

        public int getX() {
            return x;
        }

        public int getZ() {
            return z;
        }

        public long getBitsOfSeed() {
            return bitsOfSeed;
        }

        public String toString() {
            return bitsOfSeed+" at "+x+" "+z;
        }
    }
}



