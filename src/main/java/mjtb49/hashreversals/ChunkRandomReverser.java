package mjtb49.hashreversals;

import kaptainwutax.seedutils.mc.MCVersion;
import kaptainwutax.seedutils.mc.pos.CPos;
import kaptainwutax.seedutils.util.UnsupportedVersion;
import kaptainwutax.seedutils.util.math.Mth;

import java.util.ArrayList;
import java.math.BigInteger;

public class ChunkRandomReverser {

    private final int NUM_CHUNKS_ON_AXIS = 1875000;
    private final long BEEG = Mth.pow2(48);

    public CPos reverseTerrainSeed(long terrainSeed) {
        return reverseTerrainSeed(terrainSeed, -NUM_CHUNKS_ON_AXIS, NUM_CHUNKS_ON_AXIS, -NUM_CHUNKS_ON_AXIS, NUM_CHUNKS_ON_AXIS);
    }

    public CPos reverseTerrainSeed(long terrainSeed, int minX, int maxX, int minZ, int maxZ) {
        //12354965 , 2831608 is the smallest vector along which the terrain seed remains unchanged.
        //989088 , 23009024

        // 23009024 , -2831608
        //-989088 , 12354965
        long firstSolutionZ = (211541297333629L * terrainSeed) & Mth.MASK_48;
        BigInteger trueMaxX = BigInteger.valueOf(minX);
        BigInteger trueMinX = BigInteger.valueOf(maxX);
        BigInteger trueMaxZ = BigInteger.valueOf(maxZ - firstSolutionZ);
        BigInteger trueMinZ = BigInteger.valueOf(minZ - firstSolutionZ);

        //Rounding properly is hard
        long maxXT = trueMaxX.multiply(BigInteger.valueOf(23009024)).add(trueMinZ.multiply(BigInteger.valueOf(-989088))).divide(BigInteger.valueOf(BEEG)).longValue();
        long minXT = trueMinX.multiply(BigInteger.valueOf(23009024)).add(trueMaxZ.multiply(BigInteger.valueOf(-989088))).divide(BigInteger.valueOf(BEEG)).longValue();
        long maxZT = trueMinX.multiply(BigInteger.valueOf(-2831608)).add(trueMaxZ.multiply(BigInteger.valueOf(12354965))).divide(BigInteger.valueOf(BEEG)).longValue();
        long minZT = trueMaxX.multiply(BigInteger.valueOf(-2831608)).add(trueMinZ.multiply(BigInteger.valueOf(12354965))).divide(BigInteger.valueOf(BEEG)).longValue();

        //TODO make sure this can't overflow
        for (long i = minXT - 1; i <= maxXT + 1; i++) {
            for (long j = minZT - 1; j <= maxZT + 1; j++) {
                //System.out.println(i+" "+j);
                long tempX = 12354965L * i +  989088L * j; //  +
                long tempZ = 2831608L* i + 23009024L * j + firstSolutionZ;
                if (minX <= tempX && tempX <= maxX && minZ <= tempZ && tempZ <= maxZ) {
                    return new CPos((int)tempX, (int) tempZ);
                }
            }
        }
        return null;
    }

    public long setTerrainSeed(long chunkX, long chunkZ, MCVersion version) {
        return (chunkX * 341873128712L + chunkZ * 132897987541L) & Mth.MASK_48;
    }

    public CPos reverseRegionSeed(long regionSeed, long worldSeed, int salt, MCVersion version) {
        return reverseTerrainSeed(regionSeed - (worldSeed & Mth.MASK_48) - salt);
    }

    public long reverseDecoratorSeed(long decoratorSeed, int index, int step, MCVersion version) {
        if(version.isOlderThan(MCVersion.v1_13)) {
            throw new UnsupportedVersion(version, "decorator seed");
        }

        return (decoratorSeed - index - 10000 * step) & Mth.MASK_48;
    }

    /**
     * Reverses the population seed hash (x*nextLong() + z*nextLong() ^ seed)
     * @param seed the population seed
     * @param x the x chunk coordinate to find the seed at
     * @param z the z chunk coordinate to find the seed at
     * @return list of worldseeds with the given population seed at the desired location
     */
    public ArrayList<Long> reversePopulationSeed(long seed, int x, int z, MCVersion version) {
        //TODO: Kill this eventually.
        if(version.isOlderThan(MCVersion.v1_13)) {
            return PopulationReverser.getSeedFromChunkseedPre13(seed & Mth.MASK_48, x, z);
        }

        return PopulationReverser.reverse(seed & Mth.MASK_48, x, z, version);
    }

    /**
     * Reverses seeds from the x*nextLong()^z*nextLong()^seed hash used by mineshafts/caves/strongholds
     * @param carverSeed the output of the hash
     * @param x the x coordinate of the chunk
     * @param z the z coordinate of the chunk
     * @return a list of worldseeds with the given carver seed at the desired location
     */
    public ArrayList<Long> reverseCarverSeed(long carverSeed, int x, int z) {
        return CarverReversalHelper.reverseCarverSeed(carverSeed & Mth.MASK_48, x ,z);
    }

    /**
     * A method to locate worldseeds with two population chunkseeds separated by a given vector
     * @param chunkseed1 the first chunkseed
     * @param chunkseed2 the second chunkseed
     * @param chunkDx the x of the second chunkseed minus the x of the first, chunk coordinates
     * @param chunkDz the z of the second chunkseed minus the z of the first, chunk coordinates
     * @return a list of all worldseeds and coords at which the chunkseeds can be found on those worldseeds.
     */
    public ArrayList<MultiChunkHelper.Result> getWorldseedFromTwoChunkseeds(long chunkseed1, long chunkseed2, int chunkDx, int chunkDz) {
        MultiChunkHelper helper = new MultiChunkHelper();
        return helper.getWorldseedFromTwoChunkseeds(chunkseed1, chunkseed2, chunkDx, chunkDz);
    }

    //TODO - Slime chunk Reversal
    //TODO - All trivial reversals.

}
