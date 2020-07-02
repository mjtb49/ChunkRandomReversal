package mjtb49.hashreversals;

import kaptainwutax.seedutils.util.math.Mth;

import java.util.List;

public class Hensel {

	public static void lift(long value, int bit, long target, int bits, int offset, Hash hash, List<Long> result) {
		if(bit >= bits) {
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

	public static void slowLift(long value, int bit, long target, int bits, int offset, Hash hash, List<Long> result) {
		int l = result.size();

		for(result.add(value); bit < bits && result.size() != l; bit++) {
			for(int j = result.size() - 1; j >= l; j--) {
				long v = result.get(j);
				long mask = Mth.mask(bit + 1);

				if((hash.hash(v | Mth.pow2(bit + offset)) & mask) == (target & mask)) {
					result.add(v | Mth.pow2(bit + offset));
				}

				if((hash.hash(v) & mask) != (target & mask)) {
					result.remove(j);
				}
			}
		}

		long mask = Mth.mask(bits + offset);

		for(int i = result.size() - 1; i >= l; i--) {
			long v = result.get(i);

			if((hash.hash(v) & mask) != (target & mask)) {
				result.remove(i);
			}
		}
	}

	@FunctionalInterface
	public interface Hash {
		long hash(long value);
	}

}
