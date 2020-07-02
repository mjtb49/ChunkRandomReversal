package mjtb49.hashreversals;

import kaptainwutax.seedutils.util.math.Mth;

import java.util.List;

public class Hensel {

	public static void lift(long value, int bit, long target, int bits, int offset, Hash hash, List<Long> result) {
		if(bit == bits) {
			if((hash.hash(value) & Mth.mask(bit + offset)) == (target & Mth.mask(bit + offset))) {
				result.add(value);
			}

			return;
		}

		if((hash.hash(value) & Mth.mask(bit)) == (target & Mth.mask(bit))) {
			lift(value, bit + 1, target, bits, offset, hash, result);
			lift(value | Mth.pow2(bit + offset), bit + 1, target, bits, offset, hash, result);
		}
	}

	@FunctionalInterface
	public interface Hash {
		long hash(long value);
	}

}
