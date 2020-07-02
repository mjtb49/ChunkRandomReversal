import kaptainwutax.seedutils.mc.MCVersion;
import kaptainwutax.seedutils.mc.seed.ChunkSeeds;
import mjtb49.hashreversals.PopulationReverser;

public class PopReversalTest {

	public static void main(String[] args) {
		for(long i = 0; i < 1000; i++) {
			int x = 123;
			int z = -115;
			long popSeed = ChunkSeeds.getPopulationSeed(i, x << 4, z << 4, MCVersion.v1_15);
			System.out.println(PopulationReverser.reverse(popSeed, x << 4, z << 4, MCVersion.v1_15));
		}
	}

}
