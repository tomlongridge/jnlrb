package linegenerator.core;

import java.awt.geom.Point2D;

/**
 * Represents a point on a method line.
 * 
 * @author Tom
 */
public class LinePoint extends Point2D {
	
	/** Enumeration of the compass points for use in placing place bell labels. */
	public enum Direction {
		/** East. */
		EAST,
		/** West. */
		WEST,
		/** North. */
		NORTH,
		/** South. */
		SOUTH,
		/** North East. */
		NORTHEAST,
		/** South East. */
		SOUTHEAST,
		/** South West. */
		SOUTHWEST,
		/** North West. */
		NORTHWEST
	}
	
	/** The label for the place bell (if on a lead end). */
	private String m_Label;
	/** The X coordinate of the point. */
	private double m_X;
	/** The Y coordinate of the point. */
	private double m_Y;
	private double m_TrebleCross;
	
	/**
	 * Constructor.
	 * @param pr_X the x coordinate
	 * @param pr_Y the y coordinate
	 */
	public LinePoint(final double pr_X, final double pr_Y) {
		m_X = pr_X;
		m_Y = pr_Y;
		m_Label = null;
		m_TrebleCross = 0d;
	}
	
	/** 
	 * Constructor.
	 * @param pr_X the x coordinate
	 * @param pr_Y the y coordinate
	 * @param pr_Label the label for the place bell
	 */
	public LinePoint(final double pr_X, final double pr_Y, final String pr_Label) {
		m_X = pr_X;
		m_Y = pr_Y;
		m_Label = pr_Label;
		m_TrebleCross = 0d;
	}
	
	/**
	 * Constructor.
	 * @param pr_Point the point to base the new object on
	 * @param pr_Distance the distance from the original point
	 * @param pr_Direction the direction from the original point
	 */
	public LinePoint(final LinePoint pr_Point, final double pr_Distance, final Direction pr_Direction) {

		m_X = pr_Point.getX();
		m_Y = pr_Point.getY();
		m_Label = pr_Point.getLabel();
		m_TrebleCross = 0d;
		
		switch (pr_Direction) {
		
		case NORTH:
			m_Y -= pr_Distance;
			break;
		case NORTHEAST:
			m_X += pr_Distance;
			m_Y -= pr_Distance;
			break;
		case EAST:
			m_X += pr_Distance;
			break;
		case SOUTHEAST:
			m_X += pr_Distance;
			m_Y += pr_Distance;
			break;
		case SOUTH:
			m_Y += pr_Distance;
			break;
		case SOUTHWEST:
			m_X -= pr_Distance;
			m_Y += pr_Distance;
			break;
		case WEST:
			m_X -= pr_Distance;
			break;
		case NORTHWEST:
			m_X -= pr_Distance;
			m_Y -= pr_Distance;
			break;
		default:
			break;
		}
		
	}

	/** {@inheritDoc} */
	@Override
	public final double getX() {
		return m_X;
	}

	/** {@inheritDoc} */
	@Override
	public final double getY() {
		return m_Y;
	}

	/** {@inheritDoc} */
	@Override
	public final void setLocation(final double pr_X, final double pr_Y) {
		m_X = pr_X;
		m_Y = pr_Y;
	}

	/**
	 * @return the label (or null if not a lead end)
	 */
	public final String getLabel() {
		return m_Label;
	}
	
	/**
	 * @return whether the point is a lead end
	 */
	public final boolean isLeadEnd() {
		return m_Label != null;
	}

	public double getTrebleCross() {
		return m_TrebleCross;
	}

	public void setTrebleCross(double pr_TrebleCross) {
		this.m_TrebleCross = pr_TrebleCross;
	}

}
