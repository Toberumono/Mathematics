package toberumono.math.range;

import java.util.regex.Pattern;

/**
 * Enumerates the possible combinations of inclusion for {@link Range Ranges}. Mostly used with
 * {@link Range#contains(Comparable)}.
 * 
 * @author Toberumono
 */
public enum Inclusivity {
	/**
	 * Neither the upper nor lower bound is included in the range. (x, y)
	 */
	NEITHER(new char[]{'(', ')'}) {
		@Override
		public <T extends Comparable<T>> boolean between(T min, T item, T max) {
			return (min == null || min.compareTo(item) == -1) && (max == null || max.compareTo(item) == 1);
		}
		
		@Override
		public boolean includesUpper() {
			return false;
		}
		
		@Override
		public boolean includesLower() {
			return false;
		}
	},
	/**
	 * Only the lower bound is included in the range. [x, y)
	 */
	LOWER(new char[]{'[', ')'}) {
		@Override
		public <T extends Comparable<T>> boolean between(T min, T item, T max) {
			return (min == null || min.compareTo(item) < 1) && (max == null || max.compareTo(item) == 1);
		}
		
		@Override
		public boolean includesUpper() {
			return false;
		}
		
		@Override
		public boolean includesLower() {
			return true;
		}
	},
	/**
	 * Only the upper bound is included in the range. (x, y]
	 */
	UPPER(new char[]{'(', ']'}) {
		@Override
		public <T extends Comparable<T>> boolean between(T min, T item, T max) {
			return (min == null || min.compareTo(item) == -1) && (max == null || max.compareTo(item) > -1);
		}
		
		@Override
		public boolean includesUpper() {
			return true;
		}
		
		@Override
		public boolean includesLower() {
			return false;
		}
	},
	/**
	 * Both the upper and lower bounds are included in the range. [x, y]
	 */
	BOTH(new char[]{'[', ']'}) {
		@Override
		public <T extends Comparable<T>> boolean between(T min, T item, T max) {
			return (min == null || min.compareTo(item) < 1) && (max == null || max.compareTo(item) > -1);
		}
		
		@Override
		public boolean includesUpper() {
			return true;
		}
		
		@Override
		public boolean includesLower() {
			return true;
		}
	};
	
	private static final Pattern spaces = Pattern.compile("\\s");
	
	private final char[] boundary;
	
	private Inclusivity(char[] boundary) {
		this.boundary = boundary;
	}
	
	/**
	 * Determines whether {@code item} is between {@code min} and {@code max} with this {@link Inclusivity}.
	 * 
	 * @param min
	 *            the minimum value of the range
	 * @param item
	 *            the item being tested
	 * @param max
	 *            the maximum value of the range
	 * @param <T>
	 *            The type of item being tested
	 * @return {@code true} iff {@code item} is between {@code min} and {@code max}
	 */
	public abstract <T extends Comparable<T>> boolean between(T min, T item, T max);
	
	/**
	 * Produces a {@link String} representation of the range denoted by {@code min} and {@code max} with the appropriate
	 * {@link Inclusivity} markers.
	 * 
	 * @param min
	 *            the minimum value of the range
	 * @param max
	 *            the maximum value of the range
	 * @return a {@link String} representation of the range denoted by {@code min} and {@code max} with the appropriate
	 *         {@link Inclusivity} markers
	 */
	public String rangeToString(Object min, Object max) {
		String minStr = min == null ? "-\u221E" : min.toString(), maxStr = (max == null ? "\u221E" : max.toString());
		StringBuilder sb = new StringBuilder(minStr.length() + maxStr.length() + 8); //Allocate space for the entire final string
		sb.append(boundary[0]);
		if (spaces.matcher(minStr).find())
			sb.append('"').append(minStr).append('"');
		else
			sb.append(minStr);
		sb.append(", ");
		if (spaces.matcher(maxStr).find())
			sb.append('"').append(maxStr).append('"');
		else
			sb.append(maxStr);
		return sb.append(boundary[1]).toString();
	}
	
	/**
	 * @return {@code true} iff the upper bound is included in this {@link Inclusivity}
	 */
	public abstract boolean includesUpper();
	
	/**
	 * @return {@code true} iff the lower bound is included in this {@link Inclusivity}
	 */
	public abstract boolean includesLower();
	
	/**
	 * Merges the two {@link Inclusivity Inclusivities} to produce an {@link Inclusivity} with the lower bound of
	 * {@code lower} and the upper bound of {@code upper} (i.e. {@link #LOWER}, {@link #UPPER} &rarr; {@link #BOTH}).
	 * 
	 * @param lower
	 *            the lower {@link Range Range's} {@link Inclusivity}
	 * @param upper
	 *            the upper {@link Range Range's} {@link Inclusivity}
	 * @return the result of merging {@code lower} and {@code upper}
	 */
	public static Inclusivity merge(Inclusivity lower, Inclusivity upper) {
		if (lower == UPPER || lower == NEITHER)
			return (upper == LOWER || upper == NEITHER) ? NEITHER : UPPER;
		else
			return (upper == LOWER || upper == NEITHER) ? LOWER : BOTH;
	}
	
	/**
	 * Determines the appropriate inclusivity from the {@link String} representation of a {@link Range}.
	 * 
	 * @param range
	 *            the {@link Range} as a {@link String} (must start with either ( or [ and end with either ) or ])
	 * @return {@code null} if the {@link String} is invalid, otherwise it returns the appropriate {@link Inclusivity}
	 */
	public static Inclusivity fromString(String range) {
		range = range.trim();
		char sc = range.charAt(0), ec = range.charAt(range.length() - 1);
		if (sc == '[') {
			if (ec == ']')
				return BOTH;
			else if (ec == ')')
				return LOWER;
			return null;
		}
		else if (sc == '(') {
			if (ec == ']')
				return UPPER;
			else if (ec == ')')
				return NEITHER;
			return null;
		}
		return null;
	}
}
