/**
 * pboon: need to have some new header
 */

//
// This file is part of Corina.
//
// Corina is free software; you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation; either version 2 of the License, or
// (at your option) any later version.
//
// Corina is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with Corina; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
//
// Copyright 2001 Ken Harris <kbh7@cornell.edu>
//
package org.tridas.io.util;


/**
 * A range of years. Ranges are immutable; all otherwise-destructive operations
 * on a Range return a new Range.
 *
 * <p>
 * Unfortunately, use of this data structure in the class Sample often violates
 * the single-instance storage principle: we hope that the usage of Range and
 * Sample will always keep <code>Sample.data.size() == Range.span()</code>, but
 * there aren't any built-in ways to do this, so it's up to you. (If you add an
 * element to sample.data, increase sample.range by one, for example.) The
 * problem is that the <code>end</code> field is a duplicate of
 * <code>Sample.data.size() + start</code>.
 * </p>
 *
 * @see SafeIntYear
 * @see TreeRingData
 * @author Ken Harris &lt;kbh7 <i style="color: gray">at</i> cornell <i
 *         style="color: gray">dot</i> edu&gt;
 * @version $Id: Range.java 1073 2008-04-27 08:34:27Z lucasm $
 */
public class YearRange implements Comparable {

	/** Starting year of the Range. */
	private SafeIntYear start;

	/** Ending year of the range. */
	private SafeIntYear end;

	/**
	 * Construct a new empty range, starting at <code>Year.DEFAULT</code>.
	 *
	 * @see SafeIntYear
	 */
	public YearRange() {
		// this is only used by GraphFrame (which shouldn't use it)
		// -- and Sample
		start = SafeIntYear.DEFAULT;
		end = start.add(-1);
	}

	/**
	 * Construct a new range, from y<sub>1</sub> to y<sub>2</sub>. (Neither year
	 * may be <code>null</code>.) If y<sub>2</sub> &lt; y<sub>1</sub>, it is an
	 * empty interval.
	 *
	 * @param y1
	 *            starting year
	 * @param y2
	 *            ending year
	 */
	public YearRange(SafeIntYear y1, SafeIntYear y2) {
		// null argument?
		if (y1 == null || y2 == null)
			throw new NullPointerException();

		this.start = y1;
		this.end = y2;

		// empty interval?
		if (start.compareTo(end) > 0) {
			start = SafeIntYear.DEFAULT;
			end = start.add(-1);
		}
	}

	/**
	 * Construct a range, given a starting year and span.
	 *
	 * @param y
	 *            the starting year
	 * @param span
	 *            the number of years
	 */
	public YearRange(SafeIntYear y, int span) {
		this.start = y;
		this.end = y.add(span - 1);
	}

	/**
	 * Construct a range from a String.
	 *
	 * @param s
	 *            the String
	 */
	public YearRange(String s) {
		// (Grid.GridHandler.startElement is the only place this is used)

		// (ignore outside whitespace)
		String t = s.trim();

		// find the first dash that isn't t[0]
		int dash = t.indexOf('-', 1);

		// -- there must be a dash! --
		if (dash == -1)
			throw new IllegalArgumentException();

		// y1 is everything before, y2 is everything after
		String y1 = t.substring(0, dash);
		String y2 = t.substring(dash + 1);

		// construct years
		start = new SafeIntYear(y1);
		end = new SafeIntYear(y2);
	}

	/**
	 * Get the starting year of this range.
	 *
	 * @return the starting year
	 */
	public SafeIntYear getStart() {
		return start;
	}

	/**
	 * Get the ending year of this range.
	 *
	 * @return the ending year
	 */
	public SafeIntYear getEnd() {
		return end;
	}

	/**
	 * Set the starting year of the range, and adjust the ending year to
	 * maintain the same length.
	 *
	 * @param y
	 *            new starting year for the range
	 * @see #redateEndTo
	 */
	public YearRange redateStartTo(SafeIntYear y) {
		return redateBy(y.diff(start));
	}

	/**
	 * Redate a range by a certain number of years. Usually, you'll use
	 * redateStartTo() or redateEndTo(), which are more convenient.
	 *
	 * @param dy
	 *            the number of years to shift this range by
	 */
	public YearRange redateBy(int dy) {
		return new YearRange(start.add(dy), end.add(dy));
	}

	/**
	 * Set the ending year of the range, and adjust the start year to maintain
	 * the same length.
	 *
	 * @param y
	 *            new ending year for the range
	 * @see #redateStartTo
	 */
	public YearRange redateEndTo(SafeIntYear y) {
		return redateBy(y.diff(end));
	}

	/**
	 * Return the number of years spanned by this range. For example, the range
	 * 1001 - 1005 spans 5 years.
	 *
	 * @return the span of this range (difference between start and end,
	 *         inclusive)
	 */
	public int span() {
		return end.diff(start) + 1;
	}

	/**
	 * Compute the number of rows this Range will take to display, assuming rows
	 * are marked off as the row() method does.
	 *
	 * @return the number of rows this range spans
	 */
	public int rows() {
		return getEnd().row() - getStart().row() + 1;
	}

	/**
	 * Return a simple string representation of the range, like "1001 - 1036".
	 *
	 * @return a string representation of the range
	 */
	@Override
	public String toString() {
		// this tends to get called a lot, so we'll memoize it.
		if (memo == null)
			memo = start + " - " + end; // use \u2014 EM DASH?
		return memo;
	}

	private String memo = null;

	/**
	 * Return a string representation of the range, including the span, like
	 * "(1001 - 1036, n=36)".
	 *
	 * @return a string representation of the range, including span
	 */
	public String toStringWithSpan() {
		return "(" + start + " - " + end + ", n=" + span() + ")";
		// use \u2014 EM DASH?
	}

	/**
	 * Return true if (and only if) the given year is inside the range,
	 * inclusive.
	 *
	 * @param y
	 *            year to check
	 * @return true if <code>y</code> is in the range, else false
	 */
	public boolean contains(SafeIntYear y) {
		return (start.compareTo(y) <= 0) && (y.compareTo(end) <= 0);
	}

	/**
	 * Return true if (and only if) the given range is completely inside the
	 * range, inclusive.
	 *
	 * @param r
	 *            range to check
	 * @return true if <code>r</code> is entirely in the range, else false
	 */
	public boolean contains(YearRange r) {
		return contains(r.start) && contains(r.end);
	}

	/**
	 * Return true, iff this year is the start of a row. (Year 1 is considered
	 * the start of that row.)
	 *
	 * @return true, iff this year is the start of a row
	 */
	public boolean startOfRow(SafeIntYear y) {
		return y.equals(start) || y.column() == 0 || y.isYearOne();
	}

	/**
	 * Return true, iff this year is the end of a row.
	 *
	 * @return true, iff this year is the end of a row
	 */
	public boolean endOfRow(SafeIntYear y) {
		return y.equals(end) || y.column() == 9;
	}

	/**
	 * Return the number of years overlap between this range and the given
	 * range.
	 *
	 * @param r
	 *            range to compare
	 * @return number of years overlap
	 */
	public int overlap(YearRange r) {
		return intersection(r).span();
	}

	/**
	 * The intersection of this range with r. If they don't overlap, returns an
	 * empty range (1 - -1).
	 *
	 * @see #union
	 * @param r
	 *            the range to intersect with this range
	 * @return the intersection of this and r
	 */
	public YearRange intersection(YearRange r) {
		return new YearRange(SafeIntYear.max(start, r.start), SafeIntYear.min(end, r.end));
	}

	/**
	 * The union of this range with r. Since there is no concept of
	 * "range with a gap" in Corina, it assumes they overlap.
	 *
	 * @see #intersection
	 * @param r
	 *            the range to union with this range
	 * @return the union of this and r
	 */
	public YearRange union(YearRange r) {
		return new YearRange(SafeIntYear.min(start, r.start), SafeIntYear.max(end, r.end));
	}

	/**
	 * Compare two ranges for equality.
	 *
	 * @param r
	 *            range to compare with this
	 * @return true, if the ranges are equal, else false
	 */
	@Override
	public boolean equals(Object o) {
		if (o instanceof YearRange) {
			YearRange r = (YearRange) o;
			return start.equals(r.start) && end.equals(r.end);
		} else {
			// not even a Range, can't be equal
			return false;
		}
	}

	/**
	 * A hash code for the Range. (Since I define equals(), I need to define
	 * hashCode().)
	 *
	 * @return a hash code for this Range
	 */
	@Override
	public int hashCode() {
		return start.hashCode() + 2 * end.hashCode();
	}

	/**
	 * Compares this and o, for placing in fallback order. Fallback order sorts
	 * ranges by their ending year, latest to earliest, and then by their
	 * length, longest to shortest. (This is usually what people want when
	 * looking at bargraphs.)
	 *
	 * @param o
	 *            Object to compare
	 * @return >0, ==0, or <0 if this is greater-than, equal-to, or less-than o
	 * @throws ClassCastException
	 *             if o is not a Range
	 */
	public int compareTo(Object o) {
		YearRange r2 = (YearRange) o;

		int c1 = end.compareTo(r2.end);
		if (c1 != 0)
			return c1;

		// negative, because fallback puts longest samples first
		int c2 = -start.compareTo(r2.start);
		return c2;
	}
}