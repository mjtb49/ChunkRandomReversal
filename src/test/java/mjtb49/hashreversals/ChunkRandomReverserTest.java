package mjtb49.hashreversals;

import kaptainwutax.seedutils.mc.MCVersion;
import org.junit.Assert;
import org.junit.Test;

import static org.junit.Assert.*;
import kaptainwutax.seedutils.mc.ChunkRand;

import java.util.Random;

public class ChunkRandomReverserTest {

    @Test
    public void reverseTerrainSeed() {
        Random r = new Random();
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
    public void reversePopulationSeed() {
    }

    @Test
    public void reverseCarverSeed() {
    }

    @Test
    public void getWorldseedFromTwoChunkseeds() {
    }
}