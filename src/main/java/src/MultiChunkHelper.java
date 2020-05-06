package src;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Random;

public class MultiChunkHelper {

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
        return (1L << bits) - 1;
    }

    private static long modInverse(long x, int mod) { //Fast method for modular inverse mod powers of 2
        long inv = 0;
        long b = 1;
        for (int i = 0; i < mod; i++) {
            if ((b & 1)==1) {
                inv |= 1L << i;
                b = (b - x) >> 1;
            } else {
                b >>= 1;
            }
        }
        return inv;
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

    private static long getPartialChunkseed(long partialSeed, int x, int z, int bits) {
        long mask = makeMask(bits + 16);
        long mask2 = makeMask(bits);
        return  (((long)x)*(((int)(((m2*((partialSeed^m1)&mask) + addend2) & ((1L<<48)-1)) >>> 16))|1) +
                ((long)z)*(((int)(((m4*((partialSeed^m1)&mask) + addend4) & ((1L<<48)-1)) >>> 16))|1)) ^ partialSeed & mask2;
    }

    private static long getChunkseed13Plus(long seed, int x, int z) {
        Random r = new Random(seed);
        long a = r.nextLong()|1;
        long b = r.nextLong()|1;
        return ((x*a + z*b)^seed) & ((1L << 48) - 1);
    }

    ArrayList<Result> getWorldseedFromTwoChunkseeds(long chunkseed1, long chunkseed2, int chunkDx, int chunkDz) {
        results = new ArrayList<>();
        this.dx = chunkDx*16;
        this.dz = chunkDz*16;
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

    private static BigDecimal norm(BigDecimal x, BigDecimal z) {
        return x.multiply(x).add(z.multiply(z));
    }

    private static ArrayList<Result> findSeedsInWB(long target, long seed) {

        ArrayList<Result> validPositions = new ArrayList<>();

        long goal = (target ^ seed) & makeMask(48);
        Random r = new Random(seed);
        long a = r.nextLong()|1;
        long b = r.nextLong()|1;
        long binv = modInverse(b, 48);

        //a1 b1
        //a2 b2

        BigDecimal a1 = BigDecimal.valueOf(1); //TODO make this 16 and multiply lower line by 16 and make sure no break
        BigDecimal b1 = BigDecimal.valueOf((-a*binv) & makeMask(48));
        BigDecimal a2 = BigDecimal.valueOf(0);
        BigDecimal b2 = BigDecimal.valueOf(1L << 48);

        do {
            if (norm(a1,b1).compareTo( norm(a2, b2)) > 0) {
                BigDecimal temp1 = a1;
                BigDecimal temp2 = b1;
                a1 = a2;
                b1 = b2;
                a2 = temp1;
                b2 = temp2;
            }
            BigDecimal mu = a2.multiply(a1).add(b2.multiply(b1)).divide(norm(a1,b1), RoundingMode.HALF_UP).setScale(0, RoundingMode.HALF_UP);
            a2 = a2.subtract(mu.multiply(a1));
            b2 = b2.subtract(mu.multiply(b1));
        } while (norm(a1,b1).compareTo(norm(a2,b2)) > 0);

        BigDecimal initialZ = new BigDecimal(goal * binv).remainder(BigDecimal.valueOf(1L << 48));
        //System.out.println("det " +a1.multiply(b2).subtract(b1.multiply(a2)));
        BigDecimal det = a1.multiply(b2).subtract(b1.multiply(a2));
        //System.out.println(initialZ);
        BigDecimal minX = BigDecimal.valueOf(-NUM_CHUNKS_IN_WB);
        BigDecimal maxX = BigDecimal.valueOf(NUM_CHUNKS_IN_WB);
        BigDecimal minZ = BigDecimal.valueOf(-NUM_CHUNKS_IN_WB).subtract(initialZ);
        BigDecimal maxZ = BigDecimal.valueOf(NUM_CHUNKS_IN_WB).subtract(initialZ);

        BigDecimal Point1X = b2.multiply(minX).subtract(a2.multiply(minZ)).divide(det, RoundingMode.HALF_UP);
        BigDecimal Point1Z = a1.multiply(minZ).subtract(b1.multiply(minX)).divide(det, RoundingMode.HALF_UP);

        BigDecimal Point2X = b2.multiply(minX).subtract(a2.multiply(maxZ)).divide(det, RoundingMode.HALF_UP);
        BigDecimal Point2Z = a1.multiply(maxZ).subtract(b1.multiply(minX)).divide(det, RoundingMode.HALF_UP);

        BigDecimal Point3X = b2.multiply(maxX).subtract(a2.multiply(minZ)).divide(det, RoundingMode.HALF_UP);
        BigDecimal Point3Z = a1.multiply(minZ).subtract(b1.multiply(maxX)).divide(det, RoundingMode.HALF_UP);

        BigDecimal Point4X = b2.multiply(maxX).subtract(a2.multiply(maxZ)).divide(det, RoundingMode.HALF_UP);
        BigDecimal Point4Z = a1.multiply(maxZ).subtract(b1.multiply(maxX)).divide(det, RoundingMode.HALF_UP);

        BigDecimal trueMinX = Point1X.min(Point2X.min(Point3X.min(Point4X)));
        BigDecimal trueMaxX = Point1X.max(Point2X.max(Point3X.max(Point4X)));
        BigDecimal trueMinZ = Point1Z.min(Point2Z.min(Point3Z.min(Point4Z)));
        BigDecimal trueMaxZ = Point1Z.max(Point2Z.max(Point3Z.max(Point4Z)));

        for (long x = trueMinX.longValue() - 2; x <= trueMaxX.longValue() + 2;x++) {
            for (long z = trueMinZ.longValue() - 2; z <= trueMaxZ.longValue() + 2;z++) {
                long xOfChunk = x*a1.longValue() + z*a2.longValue();
                long zOfChunk = x*b1.longValue() + z*b2.longValue() + initialZ.longValue();
                if (xOfChunk < NUM_CHUNKS_IN_WB && xOfChunk > -NUM_CHUNKS_IN_WB) {
                    if (zOfChunk < NUM_CHUNKS_IN_WB && zOfChunk > -NUM_CHUNKS_IN_WB) {
                        if((xOfChunk % 16 == 0) && ((zOfChunk % 16) == 0)) {
                            Result result = new Result(seed, (int) xOfChunk, (int) zOfChunk);
                            validPositions.add(result);
                        }
                    }
                }
            }
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
    }
}



