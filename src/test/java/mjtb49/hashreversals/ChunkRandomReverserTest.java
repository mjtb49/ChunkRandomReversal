package mjtb49.hashreversals;

import kaptainwutax.seedutils.mc.MCVersion;
import org.junit.Assert;
import org.junit.Test;

import kaptainwutax.seedutils.mc.ChunkRand;

import java.util.Random;

public class ChunkRandomReverserTest {
    private final long TESTING_SEED = 923452555189237913L;
    @Test
    public void reverseTerrainSeed() {
        Random r = new Random(TESTING_SEED);
        ChunkRand cr = new ChunkRand();
        ChunkRandomReverser device = new ChunkRandomReverser();
        for (int i = 0; i < 1000; i++) {
            int x = r.nextInt(2*1875000) - 1875000;
            int z = r.nextInt(2*1875000) - 1875000;
            long tseed = cr.setTerrainSeed(x,z,MCVersion.v1_16);
            Assert.assertEquals(device.reverseTerrainSeed(tseed).getX(), x);
            Assert.assertEquals(device.reverseTerrainSeed(tseed).getZ(), z);
        }
    }

    @Test
    public void reversePopulationSeedPost13() {
        ChunkRand cr = new ChunkRand();
        Random r = new Random(TESTING_SEED + 1);
        ChunkRandomReverser device = new ChunkRandomReverser();
        for (int i = 0; i < 100; i++) {
            long seed = r.nextLong() & ((1L << 48) - 1);
            int x = r.nextInt(2*1875000) - 1875000;
            int z = r.nextInt(2*1875000) - 1875000;
            long cseed = cr.setPopulationSeed(seed,x,z,MCVersion.v1_16);
            Assert.assertTrue(device.reversePopulationSeed(cseed,x,z,MCVersion.v1_16).contains(seed));
        }
    }

    @Test
    public void reversePopulationSeedPre13() {
        ChunkRand cr = new ChunkRand();
        Random r = new Random(TESTING_SEED + 1);
        ChunkRandomReverser device = new ChunkRandomReverser();
        for (int i = 0; i < 100; i++) {
            long seed = r.nextLong() & ((1L << 48) - 1);
            int x = r.nextInt(2*100) - 100;
            int z = r.nextInt(2*100) - 100;
            long cseed = cr.setPopulationSeed(seed,x,z,MCVersion.v1_12);
            Assert.assertTrue(device.reversePopulationSeed(cseed,x,z,MCVersion.v1_12).contains(seed));
        }
    }

    @Test
    public void reverseCarverSeed() {
        ChunkRand cr = new ChunkRand();
        Random r = new Random(TESTING_SEED + 10);
        ChunkRandomReverser device = new ChunkRandomReverser();
        for (int i = 0; i < 100; i++) {
            long seed = r.nextLong() & ((1L << 48) - 1);
            int x = r.nextInt(2*1875000) - 1875000;
            int z = r.nextInt(2*1875000) - 1875000;
            long cseed = cr.setCarverSeed(seed,x,z,MCVersion.v1_16);
            Assert.assertTrue(device.reverseCarverSeed(cseed,x,z,MCVersion.v1_16).contains(seed));
        }
    }

    @Test
    public void getWorldseedFromTwoChunkseeds() {
        ChunkRand cr = new ChunkRand();
        ChunkRandomReverser device = new ChunkRandomReverser();
        Random r = new Random(TESTING_SEED + 100);
        for (int i = 0; i < 10; i++) {
            long seed = r.nextLong() & ((1L << 48) - 1);
            int x1 = r.nextInt(2*1875000) - 1875000;
            int z1 = r.nextInt(2*1875000) - 1875000;
            int x2 = r.nextInt(2*1875000) - 1875000;
            int z2 = r.nextInt(2*1875000) - 1875000;
            long cs1 = cr.setPopulationSeed(seed,x1*16,z1*16,MCVersion.v1_16);
            long cs2 = cr.setPopulationSeed(seed,x2*16,z2*16,MCVersion.v1_16);
            boolean foundSeed = false;
            for (MultiChunkHelper.Result result: device.getWorldseedFromTwoChunkseeds(cs1, cs2, x2 - x1, z2 - z1)) {
                foundSeed |= (result.getBitsOfSeed() == seed) && (x1*16 == result.getX()) && (z1 * 16 == result.getZ());
            }
            Assert.assertTrue(foundSeed);
        }
    }
}