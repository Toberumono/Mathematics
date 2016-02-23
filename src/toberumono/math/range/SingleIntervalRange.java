package toberumono.math.range;

import java.io.Serializable;

/**
 * A basic implementation of {@link Range} for a single interval.
 * 
 * @author Toberumono
 * @param <T>
 *            the type of the value being stored
 */
public class SingleIntervalRange<T extends Comparable<T>> extends Range<T> implements Serializable {
	private final T min, max;
	private final Inclusivity inclusivity;
	
	/**
	 * Creates a new {@link SingleIntervalRange} with {@link Inclusivity#LOWER lower-bound inclusivity}.
	 * 
	 * @param min
	 *            the minimum value of the {@link Range}
	 * @param max
	 *            the maximum value of the {@link Range}
	 */
	public SingleIntervalRange(T min, T max) {
		this(min, max, Inclusivity.LOWER);
	}
	
	/**
	 * Creates a new {@link SingleIntervalRange} with the given <tt>inclusivity</tt>.
	 * 
	 * @param min
	 *            the minimum value of the {@link Range}
	 * @param max
	 *            the maximum value of the {@link Range}
	 * @param inclusivity
	 *            the {@link Inclusivity} of the {@link Range}
	 */
	public SingleIntervalRange(T min, T max, Inclusivity inclusivity) {
		if (min.compareTo(max) > 0) {
			this.min = max;
			this.max = min;
		}
		else {
			this.min = min;
			this.max = max;
		}
		this.inclusivity = inclusivity;
	}
	
	@Override
	public boolean contains(T item) {
		return item != null && inclusivity.between(min, item, max);
	}
	
	@Override
	public T getMin() {
		return min;
	}
	
	@Override
	public T getMax() {
		return max;
	}
	
	@Override
	public Inclusivity getInclusivity() {
		return inclusivity;
	}
	
	@Override
	protected int findOverlap(Range<T> other) {
		if (other instanceof EmptyRange) //All ranges contain the EmptyRange
			return 2;
		if (other instanceof InfiniteRange) //The InfiniteRange contains all other ranges
			return 3;
		if (other instanceof NullElementRange) //A range contains the NullElementRange iff it contains a null element
			return 0;
			
		boolean ocu = other.contains(getMax()), ocl = other.contains(getMin());
		boolean tcu = contains(other.getMax()), tcl = contains(other.getMin());
		
		//Corrects for two ranges that have the same bound but are both exclusive on that bound
		if (!tcu && (getMax() == other.getMax() || (getMax() != null && other.getMax() != null && getMax().compareTo(other.getMax()) == 0)))
			tcu = !getInclusivity().includesUpper() && !other.getInclusivity().includesUpper();
		if (!tcl && (getMin() == other.getMin() || (getMin() != null && other.getMin() != null && getMin().compareTo(other.getMin()) == 0)))
			tcl = !getInclusivity().includesLower() && !other.getInclusivity().includesLower();
			
		if (tcu && tcl) //If this contains both of other's boundaries
			return 2;
		if (ocu && ocl) //If other contains both of this's boundaries but this does not contain both of other's boundaries
			return 3;
		if ((ocu && !ocl) || tcl) //If other contains this's upper boundary but not this's lower boundary or this contains other's lower boundary and not its upper boundary
			return 1;
		if ((ocl && !ocu) || tcu) //If other contains this's lower boundary but not this's upper boundary or this contains other's upper boundary and not its lower boundary
			return 4;
		return 0;
	}
	
	@Override
	public Range<T> add(Range<T> other) {
		if (other instanceof MultipleIntervalRange)
			return other.add(this);
		switch (findOverlap(other)) {
			case 1:
				if (other.getMax() == null)
					return new FloorRange<>(getMin(), getInclusivity().includesLower() ? Inclusivity.BOTH : Inclusivity.UPPER);
				return new SingleIntervalRange<>(getMin(), other.getMax(), Inclusivity.merge(getInclusivity(), other.getInclusivity()));
			case 2:
				return this;
			case 3:
				return other;
			case 4:
				if (other.getMin() == null)
					return new CeilingRange<>(getMax(), getInclusivity().includesUpper() ? Inclusivity.BOTH : Inclusivity.LOWER);
				return new SingleIntervalRange<>(other.getMin(), getMax(), Inclusivity.merge(other.getInclusivity(), getInclusivity()));
			default:
				return new MultipleIntervalRange<>(this, other);
		}
	}
	
	@Override
	public Range<T> subtract(Range<T> other) {
		if (other instanceof MultipleIntervalRange)
			return ((MultipleIntervalRange<T>) other).subtractFrom(this);
		boolean oiu = other.getInclusivity().includesUpper(), oil = other.getInclusivity().includesLower(), tiu = getInclusivity().includesUpper(), til = getInclusivity().includesLower();
		switch (findOverlap(other)) {
			case 1:
				return new SingleIntervalRange<>(getMin(), other.getMin(), til ? (oil ? Inclusivity.LOWER : Inclusivity.BOTH) : (oil ? Inclusivity.NEITHER : Inclusivity.UPPER));
			case 2:
				if (other.findOverlap(this) == 2) //If this and other have the same bounds
					return new EmptyRange<>();
				Range<T> lower = new SingleIntervalRange<>(getMin(), other.getMin(), til ? (oil ? Inclusivity.LOWER : Inclusivity.BOTH) : (oil ? Inclusivity.NEITHER : Inclusivity.UPPER));
				Range<T> upper = new SingleIntervalRange<>(other.getMax(), getMax(), oiu ? (tiu ? Inclusivity.UPPER : Inclusivity.NEITHER) : (tiu ? Inclusivity.BOTH : Inclusivity.LOWER));
				return new MultipleIntervalRange<>(lower, upper);
			case 3:
				return new EmptyRange<>();
			case 4:
				return new SingleIntervalRange<>(other.getMax(), getMax(), oiu ? (tiu ? Inclusivity.UPPER : Inclusivity.NEITHER) : (tiu ? Inclusivity.BOTH : Inclusivity.LOWER));
			default:
				return this;
		}
	}
	
	@Override
	public Range<T> intersection(Range<T> other) {
		if (other instanceof MultipleIntervalRange)
			return other.intersection(this);
		switch (findOverlap(other)) {
			case 1:
				return new SingleIntervalRange<>(other.getMin(), getMax(), Inclusivity.merge(other.getInclusivity(), getInclusivity()));
			case 2:
				return other;
			case 4:
				return new SingleIntervalRange<>(getMin(), other.getMax(), Inclusivity.merge(getInclusivity(), other.getInclusivity()));
			case 3:
				return this;
			default:
				return new EmptyRange<>();
		}
	}
}
