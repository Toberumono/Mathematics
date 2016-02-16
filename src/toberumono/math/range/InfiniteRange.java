package toberumono.math.range;

import java.io.Serializable;

/**
 * A special implementation of {@link Range} that contains every element.
 * 
 * @author Toberumono
 * @param <T>
 *            the type of the value being stored
 */
class InfiniteRange<T extends Comparable<T>> extends Range<T> implements Serializable {
	
	/**
	 * All elements are included this type of {@link Range}.
	 * 
	 * @return {@code true}
	 */
	@Override
	public boolean contains(T item) {
		return true;
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
		return 2;
	}
	
	@Override
	public Range<T> add(Range<T> other) {
		return other instanceof NullElementRange ? new MultipleIntervalRange<>(this, other) : other;
	}
	
	@Override
	public Range<T> subtract(Range<T> other) {
		if (other instanceof MultipleIntervalRange)
			return ((MultipleIntervalRange<T>) other).subtractFrom(this);
		if (other instanceof EmptyRange || other instanceof NullElementRange)
			return this;
		else if (other instanceof InfiniteRange)
			return new EmptyRange<>();
		else if (other instanceof CeilingRange)
			return makeFloor(other);
		else if (other instanceof FloorRange)
			return makeCeiling(other);
		else if (other instanceof SingleIntervalRange)
			return new MultipleIntervalRange<>(makeCeiling(other), makeFloor(other));
		return null;
	}
	
	private FloorRange<T> makeFloor(Range<T> range) {
		//If the subtracted range was inclusive on its upper bound, make the new range exclusive on it's lower bound, otherwise, make the new range inclusive on both bounds. 
		return new FloorRange<>(range.getMax(), range.getInclusivity().includesUpper() ? Inclusivity.UPPER : Inclusivity.BOTH);
	}
	
	private CeilingRange<T> makeCeiling(Range<T> range) {
		//If the subtracted range was inclusive on its lower bound, make the new range exclusive on it's upper bound, otherwise, make the new range inclusive on both bounds. 
		return new CeilingRange<>(range.getMin(), range.getInclusivity().includesLower() ? Inclusivity.LOWER : Inclusivity.BOTH);
	}
}
