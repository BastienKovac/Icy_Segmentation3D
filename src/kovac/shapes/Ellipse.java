package kovac.shapes;

import java.awt.geom.Point2D;

import icy.sequence.DimensionId;

/**
 * This class is here to represent an Ellipse
 * 
 * @author bastien.kovac
 * 
 */
public class Ellipse {

	/**
	 * The (x;y) coordinates of the center
	 */
	private double xCenter, yCenter;
	/**
	 * In usual notation, semiWidth represents a and semiHeight represents b
	 */
	private double semiWidth, semiHeight;
	/**
	 * The inclination angle of the ellipse
	 */
	private double angleWithAxis;
	/**
	 * The dimension in which the ellipse is contained (Meaning the plane from
	 * the OrthoView -> (X,Y), (X,Z) or (Y,Z)
	 */
	private DimensionId dim;
	/**
	 * The three points which define the ellipse
	 */
	private Point2D first, second, third;

	/**
	 * Constructor, throw a RuntimeException if the number of given points is
	 * not 3
	 * 
	 * @param points
	 *            The 3 points necessary to build the ellipse
	 */
	public Ellipse(Point2D... points) {
		if (points.length != 3) {
			throw new RuntimeException("Invalid number of arguments");
		}
		this.first = points[0];
		this.second = points[1];
		this.third = points[2];
		generateEllipseCoordinates();
	}

	/**
	 * Generate the ellipse parameters from the three defining points
	 * 
	 * @param first
	 *            The first point
	 * @param second
	 *            The second point
	 * @param third
	 *            The third point
	 */
	private void generateEllipseCoordinates() {
		double q1, q2;

		double x1 = first.getX();
		double y1 = first.getY();

		double x2 = second.getX();
		double y2 = second.getY();

		double x3 = third.getX();
		double y3 = third.getY();

		// middle ellipse
		xCenter = (x1 + x2) / 2;
		yCenter = (y1 + y2) / 2;
		semiWidth = (Math.sqrt(((x2 - x1) * (x2 - x1)) + ((y2 - y1) * (y2 - y1)))) / 2;
		double tmp1 = (y2 - yCenter);
		double tmp2 = (x2 - xCenter);
		double tmp3 = tmp1 / tmp2;
		angleWithAxis = Math.atan(tmp3);
		q1 = (1 / semiWidth)
				* ((Math.cos(angleWithAxis) * (x3 - xCenter)) + (Math.sin(angleWithAxis) * (y3 - yCenter)));
		q2 = (Math.cos(angleWithAxis) * (y3 - yCenter)) - (Math.sin(angleWithAxis) * (x3 - xCenter));
		semiHeight = Math.sqrt((q2 * q2) / (1 - (q1 * q1)));
	}

	/**
	 * @return The dimension (plane) in which the ellipse is contained
	 */
	public DimensionId getDim() {
		return dim;
	}

	/**
	 * Sets the ellipse defining dimension
	 * 
	 * @param dim
	 *            The dimension (plane) in which the ellipse is contained
	 */
	public void setDim(DimensionId dim) {
		this.dim = dim;
	}

	/**
	 * @return All the ellipse's parameters as an array
	 */
	public double[] getParams() {
		return new double[] { xCenter, yCenter, semiWidth, semiHeight, angleWithAxis };
	}

}
