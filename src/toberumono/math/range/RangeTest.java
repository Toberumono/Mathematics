package toberumono.math.range;

import java.util.function.Function;
import java.util.regex.Pattern;

import org.junit.Assert;
import org.junit.Test;

@SuppressWarnings("javadoc")
public class RangeTest {
	private static final Pattern DEFAULT_INFINITY_MARKERS = Pattern.compile("-?(\u221E|infty|infinity)", Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);
	private static final Function<String, Double> DOUBLE_CONVERTER = s -> {
		try {
			return Double.parseDouble(s);
		}
		catch (NumberFormatException e) {
			if (DEFAULT_INFINITY_MARKERS.matcher(s).matches()) {
				if (s.charAt(0) == '-')
					return Double.NEGATIVE_INFINITY;
				else
					return Double.POSITIVE_INFINITY;
			}
			return null;
		}
	};
	
	@Test
	public void parsing() {
		String[][] tests = {{"(1, 2)+[2, 3)", "(1.0, 3.0)"}, {"(-\u221E, 55.1]", "(-\u221E, 55.1]"}, {"[]", "[]"}, {"(\"2.0\")", "[2.0]"}, {"(null)", "[null]"}};
		for (int i = 0; i < tests.length; i++) {
			Range<Double> range = Range.parse(tests[i][0], DOUBLE_CONVERTER);
			System.out.println(range.toString());
			Assert.assertEquals("Test: " + i, tests[i][1], range.toString());
		}
	}
}
