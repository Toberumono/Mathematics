package toberumono.math.range;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import toberumono.structures.collections.lists.SortedList;

class MultipleIntervalRange<T extends Comparable<T>> extends Range<T> implements Serializable {
	private final List<Range<T>> ranges;
	private Inclusivity netInclusivity;
	
	private MultipleIntervalRange() {
		this.ranges = new SortedList<>(this::compare);
		this.netInclusivity = Inclusivity.NEITHER;
	}
	
	/**
	 * This must only be called with {@link Range Ranges} that cannot be merged.
	 * 
	 * @param ranges
	 *            a {@link Collection} containing the {@link Range Ranges} to use
	 */
	MultipleIntervalRange(Collection<Range<T>> ranges) {
		this();
		ranges.addAll(ranges);
		if (this.ranges.size() > 1)
			netInclusivity = Inclusivity.merge(this.ranges.get(0).getInclusivity(), this.ranges.get(ranges.size() - 1).getInclusivity());
		else if (this.ranges.size() == 1)
			netInclusivity = this.ranges.get(0).getInclusivity();
	}
	
	/**
	 * This must only be called with {@link Range Ranges} that cannot be merged.
	 * 
	 * @param range1
	 *            the first {@link Range}
	 * @param range2
	 *            the second {@link Range}
	 */
	MultipleIntervalRange(Range<T> range1, Range<T> range2) {
		this();
		ranges.add(range1);
		ranges.add(range2);
		netInclusivity = Inclusivity.merge(range1.getInclusivity(), range2.getInclusivity());
	}
	
	private MultipleIntervalRange(List<Range<T>> ranges, Inclusivity netInclusivity) {
		this.ranges = ranges;
		this.netInclusivity = netInclusivity;
	}
	
	private int compare(Range<T> a, Range<T> b) { //Only works on lists where all overlapping ranges have been merged
		if (a instanceof InfiniteRange || b instanceof InfiniteRange)
			return 0;
		if (a.getMax() == null) {
			if (b.getMax() != null)
				return 1;
			if (!b.contains(a.getMin()))
				return -1;
			if (a.contains(b.getMin()))
				return 0;
			return 1;
		}
		if (a.getMin() == null) {
			if (b.getMin() != null)
				return -1;
			if (!b.contains(a.getMax()))
				return 1;
			if (a.contains(b.getMax()))
				return 0;
			return -1;
		}
		if (b.getMin() == null)
			return 1;
		if (b.getMax() == null)
			return -1;
		int cmp = a.getMin().compareTo(b.getMin());
		if (cmp == 0)
			return a.getMax().compareTo(b.getMax());
		else if (cmp == -1)
			return -1;
		else
			return 1;
	}
	
	@Override
	public boolean contains(T item) {
		for (Range<T> range : ranges)
			if (range.contains(item))
				return true;
		return false;
	}
	
	@Override
	public T getMin() {
		if (ranges.size() == 0)
			return null;
		return ranges.get(0).getMin();
	}
	
	@Override
	public T getMax() {
		if (ranges.size() == 0)
			return null;
		return ranges.get(ranges.size() - 1).getMax();
	}
	
	@Override
	public Inclusivity getInclusivity() {
		return netInclusivity;
	}
	
	@Override
	protected int findOverlap(Range<T> other) {
		return -1; //This method is irrelevant given how addition and subtraction work with MutlipleIntervalRange instances
	}
	
	@Override
	public Range<T> add(Range<T> other) {
		if (other instanceof EmptyRange)
			return this;
		if (other instanceof InfiniteRange)
			return other;
		List<Range<T>> nr = new ArrayList<>(ranges);
		if (other instanceof MultipleIntervalRange)
			for (Range<T> range : ((MultipleIntervalRange<T>) other).ranges)
				addRange(nr, range);
		else
			addRange(nr, other);
		if (nr.size() == 0)
			return new EmptyRange<>();
		if (nr.size() == 1)
			return nr.get(0);
		return new MultipleIntervalRange<>(nr);
	}
	
	private void addRange(List<Range<T>> ranges, Range<T> range) {
		ranges.add(range);
		int mid = ranges.indexOf(range);
		Range<T> newRange;
		while (mid >= 1 && ranges.size() > 1 && ranges.get(mid).findMergeability(ranges.get(mid - 1)) != 0) {
			newRange = ranges.remove(mid).add(ranges.remove(mid - 1));
			ranges.add(--mid, newRange);
		}
		while (mid < ranges.size() - 1 && ranges.size() > 1 && ranges.get(mid).findMergeability(ranges.get(mid + 1)) != 0) {
			newRange = ranges.remove(mid).add(ranges.remove(mid));
			ranges.add(mid, newRange);
		}
	}
	
	@Override
	public Range<T> subtract(Range<T> other) {
		if (other instanceof EmptyRange)
			return this;
		if (other instanceof InfiniteRange)
			return new EmptyRange<>();
		List<Range<T>> nr = new ArrayList<>(ranges);
		if (other instanceof MultipleIntervalRange)
			for (Range<T> range : ((MultipleIntervalRange<T>) other).ranges)
				subtractRange(nr, range);
		else
			subtractRange(nr, other);
		if (nr.size() == 0)
			return new EmptyRange<>();
		if (nr.size() == 1)
			return nr.get(0);
		return new MultipleIntervalRange<>(nr);
	}
	
	private void subtractRange(List<Range<T>> ranges, Range<T> range) {
		Range<T> nr;
		for (int i = 0; i < ranges.size();) {
			nr = ranges.remove(i).subtract(range);
			if (nr instanceof EmptyRange)
				continue;
			else if (nr instanceof MultipleIntervalRange) {
				ranges.addAll(i, ((MultipleIntervalRange<T>) nr).ranges);
				i += ((MultipleIntervalRange<T>) nr).ranges.size();
			}
			else {
				ranges.add(i, nr);
				i++;
			}
		}
	}
	
	public Range<T> subtractFrom(Range<T> range) {
		List<Range<T>> nr = new ArrayList<>();
		if (range instanceof MultipleIntervalRange)
			nr.addAll(((MultipleIntervalRange<T>) range).ranges);
		else
			nr.add(range);
		for (Range<T> r : ranges) {
			Range<T> resr;
			for (int i = 0; i < nr.size();) {
				resr = nr.remove(i).subtract(r);
				if (resr instanceof EmptyRange)
					continue;
				else if (resr instanceof MultipleIntervalRange) {
					nr.addAll(i, ((MultipleIntervalRange<T>) resr).ranges);
					i += ((MultipleIntervalRange<T>) resr).ranges.size();
				}
				else {
					nr.add(i, resr);
					i++;
				}
			}
		}
		if (nr.size() == 0)
			return new EmptyRange<>();
		if (nr.size() == 1)
			return nr.get(0);
		return new MultipleIntervalRange<>(nr, Inclusivity.merge(nr.get(0).getInclusivity(), nr.get(nr.size() - 1).getInclusivity()));
	}
	
	@Override
	public String toString() {
		if (ranges.size() == 0)
			return "[]";
		StringBuilder output = new StringBuilder();
		for (Range<T> range : ranges)
			output.append(" \u222A ").append(range.toString());
		return output.toString().substring(3);
	}
}
