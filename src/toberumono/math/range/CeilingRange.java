package toberumono.math.range;

import java.io.Serializable;

/**
 * A special implementation of {@link Range} that has no lower bound.
 * 
 * @author Toberumono
 * @param <T>
 *            the type of the value being stored
 */
class CeilingRange<T extends Comparable<T>> extends Range<T> implements Serializable {
	private final T ceiling;
	private final Inclusivity inclusivity;
	
	/**
	 * Creates a new {@link CeilingRange} with {@link Inclusivity#UPPER upper-bound inclusivity}.
	 * 
	 * @param ceiling
	 *            the maximum value of the {@link Range}
	 */
	CeilingRange(T ceiling) {
		this(ceiling, Inclusivity.UPPER);
	}
	
	/**
	 * Creates a new {@link CeilingRange} with the given <tt>inclusivity</tt>.
	 * 
	 * @param ceiling
	 *            the maximum value of the {@link Range}
	 * @param inclusivity
	 *            the {@link Inclusivity} of the {@link Range}
	 */
	CeilingRange(T ceiling, Inclusivity inclusivity) {
		this.ceiling = ceiling;
		this.inclusivity = inclusivity == Inclusivity.BOTH ? Inclusivity.UPPER : (inclusivity == Inclusivity.LOWER ? Inclusivity.NEITHER : inclusivity);
	}
	
	@Override
	public boolean contains(T item) {
		return item != null && inclusivity.between(null, item, ceiling);
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
		return ceiling;
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
			return getMax() == null ? 2 : 0;
		
		boolean ocu = other.contains(getMax()), ocl = other.getMin() == null;
		boolean tcu = contains(other.getMax()), tcl = other.getMin() == null || contains(other.getMin());
		
		//Corrects for two ranges that have the same bound but are both exclusive on that bound
		if (!tcu && (getMax() == other.getMax() || (getMax() != null && other.getMax() != null && getMax().compareTo(other.getMax()) == 0)))
			tcu = !getInclusivity().includesUpper() && !other.getInclusivity().includesUpper();
		if (!tcl && (getMin() == other.getMin() || (getMin() != null && other.getMin() != null && getMin().compareTo(other.getMin()) == 0)))
			tcl = !getInclusivity().includesLower() && !other.getInclusivity().includesLower();
		
		if (tcu && tcl) //If this contains other's boundaries
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
	protected int findMergeability(Range<T> other) {
		int overlap = findOverlap(other);
		if (overlap > 0)
			return overlap;
		if (getMax().equals(other.getMin()))
			return 1;
		return overlap;
	}
	
	@Override
	public Range<T> add(Range<T> other) {
		if (other instanceof MultipleIntervalRange)
			return other.add(this);
		switch (findMergeability(other)) {
			case 1:
				if (other.getMax() != null)
					return new CeilingRange<>(other.getMax(), other.getInclusivity().includesUpper() ? Inclusivity.BOTH : Inclusivity.LOWER);
				return new InfiniteRange<>();
			case 2:
			case 4:
				return this;
			case 3:
				return other;
			default:
				return new MultipleIntervalRange<>(this, other);
		}
	}
	
	@Override
	public Range<T> subtract(Range<T> other) {
		if (other instanceof MultipleIntervalRange)
			return ((MultipleIntervalRange<T>) other).subtractFrom(this);
		boolean oiu = other.getInclusivity().includesUpper(), oil = other.getInclusivity().includesLower(), tiu = getInclusivity().includesUpper();
		switch (findOverlap(other)) {
			case 1:
				if (other.getMin() != null)
					return new CeilingRange<>(other.getMin(), oil ? Inclusivity.LOWER : Inclusivity.BOTH);
				return new EmptyRange<>();
			case 2:
				if (other.findOverlap(this) == 2) //If this and other have the same bounds
					return new EmptyRange<>();
			case 4: //case 4 is really just a subset of case 2 here.
				Range<T> upper = new SingleIntervalRange<>(other.getMax(), getMax(), oiu ? (tiu ? Inclusivity.UPPER : Inclusivity.NEITHER) : (tiu ? Inclusivity.BOTH : Inclusivity.LOWER));
				if (other instanceof CeilingRange) //If other has no lower bound
					return upper;
				return new MultipleIntervalRange<>(new CeilingRange<>(other.getMin(), oil ? Inclusivity.LOWER : Inclusivity.BOTH), upper);
			case 3: //If other contains this, subtracting other results in the EmptyRange
				return new EmptyRange<>();
			default: //If there is no overlap, then subtraction does nothing
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
			case 4:
				return other;
			case 3:
				return this;
			default:
				return new EmptyRange<>();
		}
	}
}
