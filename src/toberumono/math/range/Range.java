package toberumono.math.range;

import java.util.function.BiFunction;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import toberumono.structures.sexpressions.ConsCell;
import toberumono.structures.sexpressions.ConsType;

/**
 * A generalized structure for storing ranges of values.<br>
 * All instances of {@link Range} are immutable.
 * 
 * @author Toberumono
 * @param <T>
 *            the type of the value being stored
 */
public abstract class Range<T extends Comparable<T>> {
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
	
	public Range<T> intersection(Range<T> other) {
		throw new UnsupportedOperationException(); //TODO implement
	}
	
	@Override
	public String toString() {
		return getInclusivity().rangeToString(getMin(), getMax());
	}
	
	public static <T extends Comparable<T>> Range<T> parse(String range, Function<String, T> converter) {
		return parse(range, converter, DEFAULT_INFINITY_MARKERS);
	}
	
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
				System.out.println(m.group(15));
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
		while (!ranges.isLastConsCell()) {
			head = ranges.getNextConsCell();
			if (head.getCarType() == RANGE)
				ranges.setCar(((Range<T>) ranges.getCar()).add((Range<T>) head.getCar()), RANGE);
			else if (head.getCarType() == OPERATION) {
				BiFunction<Range<T>, Range<T>, Range<T>> operation = (BinaryOperator<Range<T>>) head.getCar();
				head = head.remove();
				//TODO check for invalid chained operations
				ranges.setCar(operation.apply((Range<T>) ranges.getCar(), (Range<T>) head.getCar()), RANGE);
			}
			head.remove();
		}
		return (Range<T>) ranges.getCar();
	}
}
