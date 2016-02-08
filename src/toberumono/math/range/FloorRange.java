package toberumono.math.range;

/**
 * A special implementation of {@link Range} that has no upper bound.
 * 
 * @author Toberumono
 * @param <T>
 *            the type of the value being stored
 */
class FloorRange<T extends Comparable<T>> extends Range<T> {
	private final T floor;
	private final Inclusivity inclusivity;
	
	/**
	 * Creates a new {@link FloorRange} with {@link Inclusivity#BOTH upper and lower-bound inclusivity}.
	 * 
	 * @param floor
	 *            the minimum value of the {@link Range}
	 */
	FloorRange(T floor) {
		this(floor, Inclusivity.LOWER);
	}
	
	/**
	 * Creates a new {@link FloorRange} with the given <tt>inclusivity</tt>.
	 * 
	 * @param floor
	 *            the minimum value of the {@link Range}
	 * @param inclusivity
	 *            the {@link Inclusivity} of the {@link Range}
	 */
	FloorRange(T floor, Inclusivity inclusivity) {
		this.floor = floor;
		this.inclusivity = inclusivity == Inclusivity.BOTH ? Inclusivity.LOWER : (inclusivity == Inclusivity.UPPER ? Inclusivity.NEITHER : inclusivity);
	}
	
	@Override
	public boolean contains(T item) {
		return item != null && inclusivity.between(floor, item, null);
	}
	
	/**
	 * This implementation of {@link Range} has no minimum.
	 * 
	 * @return {@code null}
	 */
	@Override
	public T getMin() {
		return floor;
	}
	
	@Override
	public T getMax() {
		return null;
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
			return getMin() == null ? 2 : 0;
			
		boolean ocl = other.contains(getMin()), ocu = other.getMax() == null;
		boolean tcl = contains(other.getMin()), tcu = other.getMax() == null || contains(other.getMax());
		
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
		if (getMin().equals(other.getMax()))
			return 4;
		return overlap;
	}
	
	@Override
	public Range<T> add(Range<T> other) {
		if (other instanceof MultipleIntervalRange)
			return other.add(this);
		switch (findMergeability(other)) {
			case 1:
			case 2:
				return this;
			case 3:
				return other;
			case 4:
				if (other.getMin() != null)
					return new FloorRange<>(other.getMin(), other.getInclusivity().includesLower() ? Inclusivity.BOTH : Inclusivity.UPPER);
				return new InfiniteRange<>();
			default:
				return new MultipleIntervalRange<>(this, other); //If there is no overlap, we cannot merge the two ranges
		}
	}
	
	@Override
	public Range<T> subtract(Range<T> other) {
		if (other instanceof MultipleIntervalRange)
			return ((MultipleIntervalRange<T>) other).subtractFrom(this);
		boolean oiu = other.getInclusivity().includesUpper(), oil = other.getInclusivity().includesLower(), til = getInclusivity().includesLower();
		switch (findOverlap(other)) {
			case 2: //Case 2 is a special case of Case 1 wherein the bounds are exactly equal, and therefore falls through to it.
				if (other.findOverlap(this) == 2) //If this and other have the same bounds
					return new EmptyRange<>();
			case 1: //Case 1
				//The resulting range cannot include anything in the subtracted range
				Range<T> lower = new SingleIntervalRange<>(getMin(), other.getMin(), oil ? (til ? Inclusivity.LOWER : Inclusivity.NEITHER) : (til ? Inclusivity.BOTH : Inclusivity.UPPER));
				if (other instanceof CeilingRange)
					return lower;
				return new MultipleIntervalRange<>(lower, new FloorRange<>(other.getMax(), oiu ? Inclusivity.UPPER : Inclusivity.BOTH)); //If other includes upper, then the remaining floor range is exclusive
			case 4: //If other's upper bound is greater than this's lower bound, shift the floor to other's upper bound
				if (other.getMax() != null)
					return new FloorRange<>(other.getMax(), oiu ? Inclusivity.UPPER : Inclusivity.BOTH);
				return new EmptyRange<>();
			case 3: //If other contains this, subtracting other results in the EmptyRange
				return new EmptyRange<>();
			default: //If there is no overlap, then subtraction does nothing
				return this;
		}
	}
	
}
