package toberumono.math.range;

/**
 * A special implementation of {@link Range} that holds only one element.
 * 
 * @author Toberumono
 * @param <T>
 *            the type of the value being stored
 */
class SingleElementRange<T extends Comparable<T>> extends Range<T> {
	private final T element;
	
	/**
	 * Creates a new {@link SingleElementRange} with the given element.
	 * 
	 * @param element
	 *            the maximum value of the {@link Range}
	 */
	SingleElementRange(T element) {
		this.element = element;
	}
	
	/**
	 * Only one element is in this {@link Range}.
	 * 
	 * @return {@code true} iff <tt>item</tt> equals the element in this {@link Range}
	 */
	@Override
	public boolean contains(T item) {
		return item == element || (item != null && item.compareTo(element) == 0);
	}
	
	/**
	 * This implementation of {@link Range} has no minimum.
	 * 
	 * @return {@code null}
	 */
	@Override
	public T getMin() {
		return element;
	}
	
	@Override
	public T getMax() {
		return element;
	}
	
	@Override
	public Inclusivity getInclusivity() {
		return Inclusivity.BOTH;
	}
	
	@Override
	protected int findOverlap(Range<T> other) {
		if (other instanceof EmptyRange)
			return 2;
		if (other instanceof SingleElementRange)
			if (other.contains(element))
				return 2;
		return other.contains(element) ? 3 : 0;
	}
	
	@Override
	public Range<T> add(Range<T> other) {
		if (other instanceof MultipleIntervalRange)
			return other.add(this);
		int overlap = findOverlap(other);
		if (overlap == 2)
			return this;
		return other.add(this); //Range addition is commutative, so...
	}
	
	@Override
	public Range<T> subtract(Range<T> other) {
		if (other instanceof MultipleIntervalRange)
			return ((MultipleIntervalRange<T>) other).subtractFrom(this);
		if (other instanceof EmptyRange)
			return this;
		if (other.contains(element)) //If the other range contains the only element in this range and we are subtracting, then the result is the empty set
			return new EmptyRange<>();
		return this;
	}
	
	@Override
	public String toString() {
		return "[" + element.toString() + "]";
	}
}
