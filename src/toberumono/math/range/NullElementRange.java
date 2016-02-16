package toberumono.math.range;

import java.io.Serializable;

/**
 * A special implementation of {@link Range} that holds the null element.
 * 
 * @author Toberumono
 * @param <T>
 *            the type of the value being stored
 */
class NullElementRange<T extends Comparable<T>> extends Range<T> implements Serializable {
	
	NullElementRange() {}

	/**
	 * Only one element is in this {@link Range}.
	 * 
	 * @return {@code true} iff <tt>item</tt> equals the element in this {@link Range}
	 */
	@Override
	public boolean contains(T item) {
		return item == null;
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
	
	@Override
	public T getMax() {
		return null;
	}
	
	@Override
	public Inclusivity getInclusivity() {
		return Inclusivity.BOTH;
	}

	@Override
	protected int findOverlap(Range<T> other) {
		return 0;
	}
	
	@Override
	public Range<T> add(Range<T> other) {
		if (other instanceof MultipleIntervalRange)
			return other.add(this);
		if (other instanceof NullElementRange)
			return this;
		return new MultipleIntervalRange<>(this, other);
	}
	
	@Override
	public Range<T> subtract(Range<T> other) {
		if (other instanceof MultipleIntervalRange)
			return ((MultipleIntervalRange<T>) other).subtractFrom(this);
		if (other instanceof EmptyRange)
			return this;
		if (other instanceof NullElementRange)
			return new EmptyRange<>();
		return this;
	}
	
	@Override
	public String toString() {
		return "[null]";
	}
}
