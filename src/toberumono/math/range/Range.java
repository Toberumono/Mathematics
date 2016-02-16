package toberumono.math.range;

import java.io.Serializable;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import toberumono.structures.sexpressions.ConsCell;
import toberumono.structures.sexpressions.ConsType;

/**
 * A generalized structure for storing ranges of values.<br>
 * All subclasses of {@link Range} must be immutable.
 * 
 * @author Toberumono
 * @param <T>
 *            the type of the value being stored
 */
public abstract class Range<T extends Comparable<T>> implements Serializable {
	/**
	 * All quoted values must adhere to the String specifications for Java.
	 */
	private static final String quotedString = "\"((\\\\[tbnrf'\"\\\\]|[^\"\\\\])+?)\"";
	/**
	 * To extract data:
	 * <table>
	 * <tr><td>group</td><td>&rarr;</td><td>contents</td></tr>
	 * <tr><td>1</td><td>&rarr;</td><td>first value (use groups 2 & 4, not this group)</td></tr>
	 * <tr><td>2</td><td>&rarr;</td><td>first value if quoted</td></tr>
	 * <tr><td>3</td><td>&rarr;</td><td>repetition subgroup (use group 2, not this group)</td></tr>
	 * <tr><td>4</td><td>&rarr;</td><td>first value if not quoted</td></tr>
	 * <tr><td>5</td><td>&rarr;</td><td>second value (use groups 6 & 8, not this group)</td></tr>
	 * <tr><td>6</td><td>&rarr;</td><td>second value if quoted</td></tr>
	 * <tr><td>7</td><td>&rarr;</td><td>repetition subgroup (use group 6, not this group)</td></tr>
	 * <tr><td>8</td><td>&rarr;</td><td>second value if not quoted</td></tr>
	 * </table>
	 */
	private static final String SINGLE_INTERVAL_STRING = "[\\(\\[]\\s*?(" + quotedString + "|([^\\s,]+?))\\s*?," + "\\s*?(" + quotedString + "|([^\\s\\)\\]]+?))\\s*?[\\)\\]]";
	private static final String SINGLE_ELEMENT_STRING = "[\\(\\[]\\s*?(" + quotedString + "|([^\\s\\)\\]]*?))\\s*?[\\)\\]]";
	private static final String UNION = "([\u222AuU]|union)", INTERSECTION = "([\u2229iI]|intersect|intersection)", ADDITION = "(\\+)", SUBTRACTION = "(-)";
	private static final Pattern RANGE_ELEMENT =
			Pattern.compile("(" + SINGLE_INTERVAL_STRING + "|" + UNION + "|" + INTERSECTION + "|" + ADDITION + "|" + SUBTRACTION + "|" + SINGLE_ELEMENT_STRING + ")");
	private static final ConsType RANGE = new ConsType("Range");
	private static final ConsType OPERATION = new ConsType("Operation");
	/**
	 * Default pattern that matches the possible methods of indicating an infinite value on a bound of a {@link Range}.
	 */
	public static final Pattern DEFAULT_INFINITY_MARKERS = Pattern.compile("[\\+\\-]?(\u221E|inf|infty|infinity)", Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);
	
	/**
	 * Determines if <tt>item</tt> is in the {@link Range}
	 * 
	 * @param item
	 *            the item to test
	 * @return {@code true} iff <tt>item</tt> is in the {@link Range}
	 */
	public abstract boolean contains(T item);
	
	/**
	 * @return the minimum value in the {@link Range}
	 */
	public abstract T getMin();
	
	/**
	 * @return the maximum value in the {@link Range}
	 */
	public abstract T getMax();
	
	/**
	 * @return the {@link Inclusivity} at the boundaries of the {@link Range}
	 */
	public abstract Inclusivity getInclusivity();
	
	/**
	 * <ul>
	 * <li>0 = no overlap</li>
	 * <li>1 = overlap on this range's upper bound</li>
	 * <li>2 = this range contains or has the same outer bounds the other range</li>
	 * <li>3 = the other range contains this range</li>
	 * <li>4 = overlap on this range's lower bound</li>
	 * </ul>
	 * 
	 * @param other
	 *            the range that we are testing for overlap with
	 * @return either 0, 1, 2, 3, or 4 as appropriate
	 */
	protected abstract int findOverlap(Range<T> other);
	
	/**
	 * <ul>
	 * <li>0 = no overlap</li>
	 * <li>1 = overlap on this range's upper bound</li>
	 * <li>2 = this range contains or has the same outer bounds the other range</li>
	 * <li>3 = the other range contains this range</li>
	 * <li>4 = overlap on this range's lower bound</li>
	 * </ul>
	 * 
	 * @param other
	 *            the range that we are testing for mergeability with
	 * @return either 0, 1, 2, 3, or 4 as appropriate
	 */
	protected int findMergeability(Range<T> other) {
		return findOverlap(other);
	}
	
	/**
	 * Adds {@code other} to the {@link Range} and returns the result.
	 * 
	 * @param other
	 *            the {@link Range} to add
	 * @return the result of adding {@code other} to the {@link Range}
	 */
	public abstract Range<T> add(Range<T> other);
	
	/**
	 * Subtracts {@code other} from the {@link Range} and returns the result.
	 * 
	 * @param other
	 *            the {@link Range} to subtract
	 * @return the result of subtracting {@code other} from the {@link Range}
	 */
	public abstract Range<T> subtract(Range<T> other);
	
	/**
	 * Computes the intersection between the {@link Range} and the given {@link Range} and returns the results.
	 * 
	 * @param other
	 *            the {@link Range} with which to compute the intersection
	 * @return the intersection between the {@link Range} and the given {@link Range}
	 */
	public abstract Range<T> intersection(Range<T> other);
	
	@Override
	public boolean equals(Object other) {
		if (!(other instanceof Range))
			return false;
		Range<?> o = (Range<?>) other;
		return o.getInclusivity().equals(getInclusivity()) && (getMax() == o.getMax() || (getMax() != null && getMax().equals(o.getMax()))) &&
				(getMin() == o.getMin() || (getMin() != null && getMin().equals(o.getMin())));
	}
	
	@Override
	public String toString() {
		return getInclusivity().rangeToString(getMin(), getMax());
	}
	
	/**
	 * Converts the given {@link String} into a {@link Range} using the given {@code converter}.<br>
	 * This is a convenience method that forwards to {@link #parse(String, Function, Pattern)} with
	 * {@link #DEFAULT_INFINITY_MARKERS} for the {@code infinityMarkers}.
	 * 
	 * @param range
	 *            the {@link String} representation of the range
	 * @param converter
	 *            a {@link Function} that converts a {@link String} into an object of type {@code T}
	 * @return the {@link Range} that the {@link String} described
	 */
	public static <T extends Comparable<T>> Range<T> parse(String range, Function<String, T> converter) {
		return parse(range, converter, DEFAULT_INFINITY_MARKERS);
	}
	
	/**
	 * Converts the given {@link String} into a {@link Range} using the given {@code converter}.
	 * 
	 * @param range
	 *            the {@link String} representation of the range
	 * @param converter
	 *            a {@link Function} that converts a {@link String} into an object of type {@code T}
	 * @param infinityMarkers
	 *            the {@link Pattern} to be used to identify values that are equivalent to infinity
	 * @return the {@link Range} that the {@link String} described
	 */
	public static <T extends Comparable<T>> Range<T> parse(String range, Function<String, T> converter, Pattern infinityMarkers) {
		ConsCell ranges = new ConsCell(), head = ranges;
		Matcher m = RANGE_ELEMENT.matcher(range);
		while (m.find()) {
			if (m.group(2) != null) {
				Inclusivity inc = Inclusivity.fromString(m.group());
				String lower = m.group(3) != null ? m.group(3) : m.group(5);
				String upper = m.group(7) != null ? m.group(7) : m.group(9);
				boolean linf = infinityMarkers.matcher(lower).matches(), uinf = infinityMarkers.matcher(upper).matches();
				Range<T> rng = null;
				if (linf)
					if (uinf)
						rng = new InfiniteRange<>();
					else
						rng = new CeilingRange<>(converter.apply(upper), inc);
				else if (uinf)
					rng = new FloorRange<>(converter.apply(lower), inc);
				else
					rng = new SingleIntervalRange<>(converter.apply(lower), converter.apply(upper), inc);
				head = head.append(new ConsCell(rng, RANGE));
			}
			else if (m.group(10) != null)
				head = head.append(new ConsCell((BinaryOperator<Range<T>>) Range::add, OPERATION));
			else if (m.group(11) != null)
				head = head.append(new ConsCell((BinaryOperator<Range<T>>) Range::intersection, OPERATION));
			else if (m.group(12) != null)
				head = head.append(new ConsCell((BinaryOperator<Range<T>>) Range::add, OPERATION));
			else if (m.group(13) != null)
				head = head.append(new ConsCell((BinaryOperator<Range<T>>) Range::subtract, OPERATION));
			else if (m.group(14) != null) {
				String element = m.group(15) != null ? m.group(15) : m.group(17);
				Range<T> rng = null;
				if (element.length() == 0)
					rng = new EmptyRange<>();
				else if (element.equals("null"))
					rng = new NullElementRange<>();
				else
					rng = new SingleElementRange<>(converter.apply(element));
				head = head.append(new ConsCell(rng, RANGE));
			}
		}
		return computeRanges(ranges);
	}
	
	@SuppressWarnings("unchecked")
	private static <T extends Comparable<T>> Range<T> computeRanges(ConsCell ranges) {
		if (!ranges.hasLength(1))
			return new EmptyRange<>();
		Range<T> nr;
		if (ranges.getCarType() == OPERATION) {
			//TODO should we treat the first argument as the empty range, or throw an error?
			//TODO test that the next one is a range.  Maybe use a ClassCastException?
			nr = ((BinaryOperator<Range<T>>) ranges.getCar()).apply(new EmptyRange<>(), (Range<T>) (ranges = ranges.getNextConsCell()).getCar());
		}
		else
			nr = (Range<T>) ranges.getCar();
		if (!ranges.hasLength(2))
			return nr;
		while (!(ranges = ranges.getNextConsCell()).isNull()) {
			if (ranges.getCarType() == RANGE)
				nr = nr.add((Range<T>) ranges.getCar());
			else {
				((BinaryOperator<Range<T>>) ranges.getCar()).apply(nr, (Range<T>) (ranges = ranges.getNextConsCell()).getCar()); //TODO test that the next one is a range.  Maybe use a ClassCastException?
			}
		}
		return nr;
	}
}
