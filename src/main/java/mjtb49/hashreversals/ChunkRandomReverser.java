package mjtb49.hashreversals;

import kaptainwutax.seedutils.mc.ChunkRand;
import kaptainwutax.seedutils.mc.MCVersion;
import kaptainwutax.seedutils.mc.pos.CPos;
import kaptainwutax.seedutils.util.UnsupportedVersion;
import kaptainwutax.seedutils.util.math.Mth;

import java.util.ArrayList;
import java.math.BigInteger;
import java.util.List;

public class ChunkRandomReverser {

    private final int NUM_CHUNKS_ON_AXIS = 1875000;
    private final long BEEG = Mth.pow2(48);

    public CPos reverseTerrainSeed(long terrainSeed) {
        return reverseTerrainSeed(terrainSeed, -NUM_CHUNKS_ON_AXIS, NUM_CHUNKS_ON_AXIS, -NUM_CHUNKS_ON_AXIS, NUM_CHUNKS_ON_AXIS);
    }

    public CPos reverseTerrainSeed(long terrainSeed, int minX, int maxX, int minZ, int maxZ) {
        ArrayList<TwoDimBigVector> results = FindSolutionsInBox.findSolutionsInBox(341873128712L, 132897987541L, terrainSeed, (1L << 48), new TwoDimBigVector(minX,minZ), new TwoDimBigVector(maxX, maxZ));
        switch (results.size()) {
            case 0:
                return null;
            case 1:
                return results.get(0).toCpos();
            default:
                throw new IndexOutOfBoundsException("Bounds too large to indentify a unique seed. If this is actually a problem for some horrifying future version of minecraft open a github issue but as of right now this should never run so I am legally allowed to write a long and funny error message instead of something more helpful.");
        }
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
     * @param populationSeed the population seed
     * @param x the x chunk coordinate to find the seed at
     * @param z the z chunk coordinate to find the seed at
     * @return list of worldseeds with the given population seed at the desired location
     */
    public List<Long> reversePopulationSeed(long populationSeed, int x, int z, MCVersion version) {
        //TODO: Kill this eventually.
        if(version.isOlderThan(MCVersion.v1_13)) {
            return PopulationReverser.getSeedFromChunkseedPre13(populationSeed & Mth.MASK_48, x, z);
        }

        return PopulationReverser.reverse(populationSeed & Mth.MASK_48, x, z, new ChunkRand(), version);
    }

    /**
     * Reverses seeds from the x*nextLong()^z*nextLong()^seed hash used by mineshafts/caves/strongholds
     * @param carverSeed the output of the hash
     * @param x the x coordinate of the chunk
     * @param z the z coordinate of the chunk
     * @return a list of worldseeds with the given carver seed at the desired location
     */
    public List<Long> reverseCarverSeed(long carverSeed, int x, int z, MCVersion version) {
        return CarverReverser.reverse(carverSeed & Mth.MASK_48, x, z, new ChunkRand(), version);
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
        return helper.getWorldseedFromTwoChunkseeds(chunkseed1, chunkseed2, 16*chunkDx, 16*chunkDz);
    }

    //TODO - Slime chunk Reversal
    //TODO - All trivial reversals.

}
