package src;

import java.awt.Point;
import java.util.ArrayList;
import java.math.BigInteger;
import src.CarverReversalHelper;

public class ChunkRandomReverser {

    private final int NUM_CHUNKS_ON_AXIS = 1875000;
    private final long MASK48 = ((1L << 48) - 1);
    private final long BEEG = (1L << 48);

    public Point reverseTerrainSeed(long terrainSeed) {
        return reverseTerrainSeed(terrainSeed, -NUM_CHUNKS_ON_AXIS, NUM_CHUNKS_ON_AXIS, -NUM_CHUNKS_ON_AXIS, NUM_CHUNKS_ON_AXIS);
    }

    public Point reverseTerrainSeed(long terrainSeed, int minX, int maxX, int minZ, int maxZ) {
        //12354965 , 2831608 is the smallest vector along which the terrain seed remains unchanged.
        //989088 , 23009024

        // 23009024 , -2831608
        //-989088 , 12354965
        long firstSolutionZ = (211541297333629L * terrainSeed) & MASK48;
        BigInteger trueMaxX = BigInteger.valueOf(minX);
        BigInteger trueMinX = BigInteger.valueOf(maxX);
        BigInteger trueMaxZ = BigInteger.valueOf(maxZ - firstSolutionZ);
        BigInteger trueMinZ = BigInteger.valueOf(minZ - firstSolutionZ);

        //ROunding properly is hard
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
                    return new Point((int)tempX, (int) tempZ);
                }
            }
        }
        return null;
    }

    public long setTerrainSeed(long chunkX, long chunkZ) {
        return (chunkX * 341873128712L + chunkZ * 132897987541L) & MASK48;
    }

    public Point reverseRegionSeed(long regionSeed, long worldSeed, int salt) {
        return reverseTerrainSeed(regionSeed - (worldSeed & MASK48) - salt);
    }

    public long reverseDecoratorSeed(long decoratorSeed, int index, int step) {
        return (decoratorSeed - index - 10000 * step) & MASK48;
    }

    /**
     * Reverses the population seed hash (x*nextLong() + z*nextLong() ^ seed)
     * @param seed the population seed
     * @param x the x chunk coordinate to find the seed at
     * @param z the z chunk coordinate to find the seed at
     * @return list of worldseeds with the given population seed at the desired location
     */
    public ArrayList<Long> reversePopulationSeed(long seed, int x, int z) {
        return reversePopulationSeed( seed & MASK48,  x,  z, false);
    }

    //TODO Make this work in chunks with more than 8 or 16 trailing 0s

    /**
     * Reverses the population seed hash (x*nextLong() + z*nextLong() ^ seed)
     * @param seed the population seed
     * @param x the x chunk coordinate to find the seed at
     * @param z the z chunk coordinate to find the seed at
     * @param beforeRelease13 boolean indicating whether the chunkseed was created by a version of minecraft before
     *                        release 1.13
     * @return list of worldseeds with the given population seed at the desired location
     */
    public ArrayList<Long> reversePopulationSeed(long seed, int x, int z, boolean beforeRelease13) {
        if (beforeRelease13)
            return PopulationReversalHelper.getSeedFromChunkseedPre13(seed & MASK48,x,z);
        return PopulationReversalHelper.getSeedFromChunkseed13Plus(seed & MASK48,x,z);
    }

    /**
     * Reverses seeds from the x*nextLong()^z*nextLong()^seed hash used by mineshafts/caves/strongholds
     * @param carverSeed the output of the hash
     * @param x the x coordinate of the chunk
     * @param z the z coordinate of the chunk
     * @return a list of worldseeds with the given carver seed at the desired location
     */
    public ArrayList<Long> reverseCarverSeed(long carverSeed, int x, int z) {
        CarverReversalHelper c = new CarverReversalHelper();
        return c.reverseCarverSeed(carverSeed & MASK48, x ,z);
    }

    /**
     * A method to locate worldseeds with two population chunkseeds separated by a given vector
     * @param chunkseed1 the first chunkseed
     * @param chunkseed2 the second chunkseed
     * @param chunkDx the x of the second chunkseed minus the x of the first, chunk coordinates
     * @param chunkDz the z of the second chunkseed minus the z of the first, chunk coordinates
     * @return a list of all worldseeds and coords at which the chunkseeds can be found on those worldseeds.
     */
    public ArrayList<src.MultiChunkHelper.Result> getWorldseedFromTwoChunkseeds(long chunkseed1, long chunkseed2, int chunkDx, int chunkDz) {
        src.MultiChunkHelper helper = new src.MultiChunkHelper();
        return helper.getWorldseedFromTwoChunkseeds(chunkseed1, chunkseed2, chunkDx, chunkDz);
    }

    //TODO - Slime chunk Reversal
    //TODO - All trivial reversals.

}
