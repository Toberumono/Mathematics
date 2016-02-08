package toberumono.math.range;

/**
 * A special implementation of {@link Range} that has no elements.
 * 
 * @author Toberumono
 * @param <T>
 *            the type of the value being stored
 */
public class EmptyRange<T extends Comparable<T>> extends Range<T> {
	
	/**
	 * All elements are excluded this type of {@link Range}.
	 * 
	 * @return {@code false}
	 */
	@Override
	public boolean contains(T item) {
		return false;
	}
	
	/**
	 * This implementation of {@link Range} has no minimum.
	 * 
	 * @return {@code null}
	 */
	@Override
	public T getMin() {
		return null;
	}
	
	/**
	 * This implementation of {@link Range} has no maximum.
	 * 
	 * @return {@code null}
	 */
	@Override
	public T getMax() {
		return null;
	}
	
	@Override
	public Inclusivity getInclusivity() {
		return Inclusivity.NEITHER;
	}

	@Override
	public int findOverlap(Range<T> other) {
		return other instanceof EmptyRange ? 2 : 3;
	}

	@Override
	public Range<T> add(Range<T> range) {
		return range;
	}

	@Override
	public Range<T> subtract(Range<T> range) {
		return this;
	}
	
	@Override
	public String toString() {
		return "[]";
	}
}
