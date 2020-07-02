package mjtb49.hashreversals;

import kaptainwutax.seedutils.lcg.LCG;
import kaptainwutax.seedutils.mc.ChunkRand;
import kaptainwutax.seedutils.mc.MCVersion;
import kaptainwutax.seedutils.mc.seed.ChunkSeeds;
import kaptainwutax.seedutils.util.math.Mth;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;

public class PopulationReverser {

    private static final long M1 = LCG.JAVA.multiplier;
    private static final long M2 = LCG.JAVA.combine(2).multiplier;
    private static final long A2 = LCG.JAVA.combine(2).addend;
    private static final long M4 = LCG.JAVA.combine(4).multiplier;
    private static final long A4 = LCG.JAVA.combine(4).addend;

    public static int[] MOD_INVERSE = new int[(int)Mth.pow2(16)];
    public static int[] X_TERM = new int[(int)Mth.pow2(16)];
    public static int[] Z_TERM = new int[(int)Mth.pow2(16)];

    static {
        for(int i = 0; i < MOD_INVERSE.length; i++) {
            MOD_INVERSE[i] = (int)Mth.modInverse(i, 16);
            X_TERM[i] = (int)((M2 * ((i ^ M1) & Mth.MASK_16) + A2) >>> 16);
            Z_TERM[i] = (int)((M4 * ((i ^ M1) & Mth.MASK_16) + A4) >>> 16);
        }
    }

    public static List<Long> reverse(long populationSeed, int x, int z, ChunkRand rand, MCVersion version) {
        ArrayList<Long> worldSeeds = new ArrayList<>();

        long c; //a is upper 16 bits, b middle 16 bits, c lower 16 bits of worldSeed.
        long e = populationSeed & Mth.MASK_32; //The algorithm proceeds by solving for worldSeed in 16 bit groups
        long f = populationSeed & Mth.MASK_16; //as such, we need the 16 bit groups of populationSeed for later eqns.

        int freeBits = Long.numberOfTrailingZeros(x | z);
        c = populationSeed & Mth.mask(freeBits);
        c |= freeBits == 64 ? 0 : (x ^ z ^ populationSeed) & Mth.pow2(freeBits++);
        int increment = (int)Mth.pow2(freeBits);

        long firstMultiplier = (M2 * x + M4 * z) & Mth.MASK_16;
        int multTrailingZeroes = Long.numberOfTrailingZeros(firstMultiplier);

        if(multTrailingZeroes >= 16) {
            Hensel.Hash popHash = value -> rand.setPopulationSeed(value, x, z, version);

            if(freeBits >= 16) {
                Hensel.lift(c, freeBits - 16, populationSeed, 32, 16, popHash, worldSeeds);
            } else {
                for(; c < 1L << 16; c += increment) {
                    Hensel.lift(c, 0, populationSeed, 32, 16, popHash, worldSeeds);
                }
            }

            return worldSeeds;
        }

        long firstMultInv = MOD_INVERSE[(int)(firstMultiplier >> multTrailingZeroes)];

        //We need to handle the four different cases of the effect the two |1s have on the seed
        HashSet<Integer> offsets = getOffsets(x, z, version);

        //iterate through all possible lower 16 bits of worldSeed.
        for(; c < (1L << 16); c += increment) {
            //now that we've guessed 16 bits of worldSeed we can undo the mask
            long target = (c ^ f) & Mth.MASK_16;
            long magic = x * X_TERM[(int)c] + z * Z_TERM[(int)c];

            for(int offset: offsets) {
                addWorldSeeds(target - ((magic + offset) & Mth.MASK_16), multTrailingZeroes,
                        firstMultInv, c, e, x, z, populationSeed, worldSeeds, rand, version);
            }
        }

        return worldSeeds;
    }

    private static void addWorldSeeds(long firstAddend, int multTrailingZeroes, long firstMultInv, long c, long e,
                               int x, int z, long populationSeed, ArrayList<Long> worldSeeds, ChunkRand rand, MCVersion version) {
        //Does there exist a set of 16 bits which work for bits 17-32
        if(Long.numberOfTrailingZeros(firstAddend) < multTrailingZeroes)return;

        long mask = Mth.mask(16 - multTrailingZeroes);
        long increment = Mth.pow2(16 - multTrailingZeroes);

        long b = (((firstMultInv * firstAddend) >>> multTrailingZeroes)^(M1 >> 16)) & mask;

        //if the previous multiplier had a power of 2 divisor, we get multiple solutions for b
        for(; b < (1L << 16); b += increment) {
            long k = (b << 16) + c;
            long target2 = (k ^ e) >> 16; //now that we know b, we can undo more of the mask
            long secondAddend = getPartialAddend(k, x, z, 32, version) & Mth.MASK_16;

            //Does there exist a set of 16 bits which work for bits 33-48
            if(Long.numberOfTrailingZeros(target2 - secondAddend) < multTrailingZeroes)continue;

            long a = (((firstMultInv * (target2 - secondAddend)) >>> multTrailingZeroes) ^ (M1 >> 32)) & mask;

            //if the previous multiplier had a power of 2 divisor, we get multiple solutions for a
            for(; a < (1L << 16); a += increment) {
                //lazy check if the test has succeeded
                if(rand.setPopulationSeed((a << 32) + k, x, z, version) != populationSeed)continue;
                worldSeeds.add((a << 32) + k);
            }
        }
    }

    private static HashSet<Integer> getOffsets(int x, int z, MCVersion version) {
        HashSet<Integer> offsets = new HashSet<>();

        if(version.isOlderThan(MCVersion.v1_13)) {
            for(int i = 0; i < 3; i++) for(int j = 0; j < 3; j++)
                offsets.add(x * i + z * j);
        } else {
            for(int i = 0; i < 2; i++) for(int j = 0; j < 2; j++)
                offsets.add(x * i + z * j);
        }

        return offsets;
    }

    private static long getPartialAddend(long partialSeed, int x, int z, int bits, MCVersion version) {
        long mask = Mth.mask(bits);
        long a = (int)(((M2 * ((partialSeed ^ M1) & mask) + A2) & Mth.MASK_48) >>> 16);
        long b = (int)(((M4 * ((partialSeed ^ M1) & mask) + A4) & Mth.MASK_48) >>> 16);

        if(version.isOlderThan(MCVersion.v1_13)) {
            return (long)x * (a / 2 * 2 + 1) + (long)z * (b / 2 * 2 + 1);
        }

        return ((long)x * (a | 1L) + (long)z * (b | 1L)) >>> 16;
    }

    //============================================================================================================//

    private static long getChunkseedPre13(long seed, int x, int z) {
        Random r = new Random(seed);
        long a = r.nextLong()/2*2+1;
        long b = r.nextLong()/2*2+1;
        return ((x*a + z*b)^seed) & ((1L << 48) - 1);
    }

    private static long getPartialAddendPre13(long partialSeed, int x, int z, int bits) {
        long mask = Mth.mask(bits);
        return  ((long)x)*(((int)(((M2 *((partialSeed^ M1)&mask) + A2) & Mth.MASK_48) >>> 16))/2*2+1) +
                ((long)z)*(((int)(((M4 *((partialSeed^ M1)&mask) + A4) & Mth.MASK_48) >>> 16))/2*2+1);
    }

    private static ArrayList<Long> addWorldSeedPre13(long firstAddend, int multTrailingZeroes, long firstMultInv, long c, int x, int z, long chunkseed, ArrayList<Long> worldseeds){
        long bottom32BitsChunkseed = chunkseed & Mth.MASK_32;

        if (Long.numberOfTrailingZeros(firstAddend) >= multTrailingZeroes) { //Does there exist a set of 16 bits which work for bits 17-32
            long b = ((((firstMultInv * firstAddend)>>> multTrailingZeroes)^(M1 >> 16)) & Mth.mask(16 - multTrailingZeroes));
            if (multTrailingZeroes != 0) {
                long smallMask = Mth.mask(multTrailingZeroes);//These are longs but probably can be ints for nearly every chunk -
                long smallMultInverse = smallMask & firstMultInv;
                long target = (((b ^ (bottom32BitsChunkseed >>> 16)) & smallMask) -
                        (getPartialAddendPre13((b << 16) + c, x, z, 32-multTrailingZeroes)>>>16)) & smallMask;
                b += (((target * smallMultInverse) ^ (M1 >>(32 - multTrailingZeroes))) & smallMask) << (16 - multTrailingZeroes);
            }
            long bottom32BitsSeed = (b << 16) + c;
            long target2 = (bottom32BitsSeed ^ bottom32BitsChunkseed) >> 16; //now that we know b, we can undo more of the mask
            long secondAddend = (getPartialAddendPre13(bottom32BitsSeed,x, z,32) >>> 16);
            secondAddend &= Mth.MASK_16;
            long topBits = ((((firstMultInv * (target2 - secondAddend)) >>> multTrailingZeroes) ^ (M1 >> 32)) & Mth.mask(16 - multTrailingZeroes));
            for(; topBits < (1L << 16); topBits += (1L << (16 - multTrailingZeroes))) { //if the previous multiplier had a power of 2 divisor, we get multiple solutions for a
                if ((getChunkseedPre13((topBits << 32) + bottom32BitsSeed, x, z)) == (chunkseed)) { //lazy check if the test has succeeded
                    worldseeds.add((topBits << 32) + bottom32BitsSeed);
                }
            }
        }
        return worldseeds;
    }

    static ArrayList<Long> getSeedFromChunkseedPre13(long chunkseed, int x, int z) {

        ArrayList<Long> worldseeds = new ArrayList<>();

        if (x == 0 && z == 0) {
            worldseeds.add(chunkseed);
            return worldseeds;
        }

        long c; //a is upper 16 bits, b middle 16 bits, c lower 16 bits of worldseed.
        long e = chunkseed & ((1L << 32) - 1); //The algorithm proceeds by solving for worldseed in 16 bit groups
        long f = chunkseed & (((1L << 16) - 1)); //as such, we need the 16 bit groups of chunkseed for later eqns.

        long firstMultiplier = (M2 *x + M4 *z) & Mth.MASK_16;
        int multTrailingZeroes = Long.numberOfTrailingZeros(firstMultiplier); //TODO currently code blows up if this is 8, but you can use it to get bits of seed anyway if it is non zero and you are reversing seeds in bulk
        long firstMultInv = Mth.modInverse(firstMultiplier >> multTrailingZeroes,16);

        int xcount = Long.numberOfTrailingZeros(x);
        int zcount = Long.numberOfTrailingZeros(z);
        int totalCount = Long.numberOfTrailingZeros(x|z);

        HashSet<Integer> possibleRoundingOffsets= new HashSet<>();
        for (int i = 0; i < 3; i++) for (int j = 0; j < 3; j++)
            possibleRoundingOffsets.add(x*i+j*z);

        c = xcount == zcount ? chunkseed & ((1 << (xcount + 1)) - 1): chunkseed & ((1 << (totalCount + 1)) - 1) ^ (1 << totalCount);
        for (; c < (1L << 16); c+= (1 << (totalCount + 1))) { //iterate through all possible lower 16 bits of worldseed.
            //System.out.println(c);
            long target = (c ^ f) & Mth.MASK_16; //now that we've guessed 16 bits of worldseed we can undo the mask
            //We need to handle the four different cases of the effect the two |1s have on the seed
            long magic = x * ((M2 * ((c ^ M1) & Mth.MASK_16) + A2) >>> 16) + z * ((M4 * ((c ^ M1) & Mth.MASK_16) + A4) >>> 16);
            for (int i: possibleRoundingOffsets)
                addWorldSeedPre13(target - ((magic + i) & Mth.MASK_16), multTrailingZeroes, firstMultInv, c, x, z, chunkseed,worldseeds); //case both nextLongs were odd
        }

        return worldseeds;
    }

}
