package kovac.ROI;

import java.awt.geom.Point2D;

import kovac.res.enums.AxisToScale;
import kovac.res.enums.Orientation;

/**
 * This class is here to handle CustomEllipseROI transformations and should only
 * be used in 2D Canvas
 * 
 * @author bastien.kovac
 * 
 */
public class Transformations {

	/**
	 * The roi to transform
	 */
	private CustomEllipseROI roi;

	/**
	 * Builds a transformation from given ROI
	 * 
	 * @param roi
	 *            The roi to transform
	 */
	public Transformations(CustomEllipseROI roi) {
		this.roi = roi;
	}

	/**
	 * Applies a 0.5° rotation to the ellipse representing the ROI
	 * 
	 * @param clockWise
	 *            True if the rotation must be clockwise, false if not
	 */
	public void rotate(boolean clockWise) {
		roi.setDrawn(false);
		double theta = 0.5;
		if (!clockWise) {
			theta *= -1;
		}
		double newX, newY;
		Point2D pt1 = PointsROI.getPoint(roi.getIndex(), 0);
		Point2D pt2 = PointsROI.getPoint(roi.getIndex(), 1);
		Point2D pt3 = PointsROI.getPoint(roi.getIndex(), 2);
		double xCenter = (pt1.getX() + pt2.getX()) / 2;
		double yCenter = (pt1.getY() + pt2.getY()) / 2;
		PointsROI.clear(roi.getIndex());
		// pt1
		newX = Math.cos(Math.toRadians(theta)) * (pt1.getX() - xCenter)
				- Math.sin(Math.toRadians(theta)) * (pt1.getY() - yCenter) + xCenter;
		newY = Math.sin(Math.toRadians(theta)) * (pt1.getX() - xCenter)
				+ Math.cos(Math.toRadians(theta)) * (pt1.getY() - yCenter) + yCenter;
		pt1 = new Point2D.Double(newX, newY);
		PointsROI.addPoint(roi.getIndex(), pt1);

		// pt2
		newX = Math.cos(Math.toRadians(theta)) * (pt2.getX() - xCenter)
				- Math.sin(Math.toRadians(theta)) * (pt2.getY() - yCenter) + xCenter;
		newY = Math.sin(Math.toRadians(theta)) * (pt2.getX() - xCenter)
				+ Math.cos(Math.toRadians(theta)) * (pt2.getY() - yCenter) + yCenter;
		pt2 = new Point2D.Double(newX, newY);
		PointsROI.addPoint(roi.getIndex(), pt2);

		// pt3
		newX = Math.cos(Math.toRadians(theta)) * (pt3.getX() - xCenter)
				- Math.sin(Math.toRadians(theta)) * (pt3.getY() - yCenter) + xCenter;
		newY = Math.sin(Math.toRadians(theta)) * (pt3.getX() - xCenter)
				+ Math.cos(Math.toRadians(theta)) * (pt3.getY() - yCenter) + yCenter;
		pt3 = new Point2D.Double(newX, newY);
		PointsROI.addPoint(roi.getIndex(), pt3);

		roi.roiChanged(true);
	}

	/**
	 * Applies translation of 1 pixel to the ellipse representing the ROI
	 * 
	 * @param o
	 *            The direction in which the translation must be applied
	 */
	public void moveTo(Orientation o) {
		roi.setDrawn(false);
		Point2D pt1 = PointsROI.getPoint(roi.getIndex(), 0);
		Point2D pt2 = PointsROI.getPoint(roi.getIndex(), 1);
		Point2D pt3 = PointsROI.getPoint(roi.getIndex(), 2);
		PointsROI.clear(roi.getIndex());
		switch (o) {
		case xMinus:
			pt1 = new Point2D.Double(pt1.getX() - 1.0, pt1.getY());
			pt2 = new Point2D.Double(pt2.getX() - 1.0, pt2.getY());
			pt3 = new Point2D.Double(pt3.getX() - 1.0, pt3.getY());
			break;
		case xPlus:
			pt1 = new Point2D.Double(pt1.getX() + 1.0, pt1.getY());
			pt2 = new Point2D.Double(pt2.getX() + 1.0, pt2.getY());
			pt3 = new Point2D.Double(pt3.getX() + 1.0, pt3.getY());
			break;
		case yMinus:
			pt1 = new Point2D.Double(pt1.getX(), pt1.getY() + 1.0);
			pt2 = new Point2D.Double(pt2.getX(), pt2.getY() + 1.0);
			pt3 = new Point2D.Double(pt3.getX(), pt3.getY() + 1.0);
			break;
		case yPlus:
			pt1 = new Point2D.Double(pt1.getX(), pt1.getY() - 1.0);
			pt2 = new Point2D.Double(pt2.getX(), pt2.getY() - 1.0);
			pt3 = new Point2D.Double(pt3.getX(), pt3.getY() - 1.0);
			break;
		default:
			System.out.println("What are you doing here ?");
			break;
		}
		PointsROI.addPoint(roi.getIndex(), pt1);
		PointsROI.addPoint(roi.getIndex(), pt2);
		PointsROI.addPoint(roi.getIndex(), pt3);
		roi.roiChanged(true);
	}

	/**
	 * Applies scaling to the ellipse representing the ROI, with a 0.05% factor
	 * 
	 * @param plus
	 *            True to scale up the ellipse, false to scale it down
	 */
	public void scaleEllipse(boolean plus) {
		double deplacement = 1.05;
		if (!plus)
			deplacement = 0.95;

		scaleAxis(AxisToScale.PseudoX, deplacement);
		scaleAxis(AxisToScale.PseudoY, deplacement);

	}

	/**
	 * Applies a deformation to the ellipse representing the ROI, by only
	 * scaling one of the pseudo axis (X or Y)
	 * 
	 * @param o
	 *            Represents both the axis to scale and if it must be scaled
	 *            down or up
	 */
	public void changeShape(Orientation o) {

		// Changing shape == scaling only one axis
		double deplacement = 1.05;
		if (o == Orientation.xMinus || o == Orientation.yMinus)
			deplacement = 0.9;

		if (o == Orientation.xMinus || o == Orientation.xPlus)
			scaleAxis(AxisToScale.PseudoX, deplacement);
		else
			scaleAxis(AxisToScale.PseudoY, deplacement);

	}

	/**
	 * Scale a given axis by a given factor
	 * 
	 * @param p
	 *            The axis to scale
	 * @param deplacement
	 *            The factor to scale it with
	 */
	private void scaleAxis(AxisToScale p, double deplacement) {

		roi.setDrawn(false);

		Point2D pt1 = PointsROI.getPoint(roi.getIndex(), 0);
		Point2D pt2 = PointsROI.getPoint(roi.getIndex(), 1);
		Point2D pt3 = PointsROI.getPoint(roi.getIndex(), 2);

		PointsROI.clear(roi.getIndex());

		Point2D ptCenter = new Point2D.Double((pt1.getX() + pt2.getX()) / 2, (pt1.getY() + pt2.getY()) / 2);

		switch (p) {
		case PseudoX:
			double newXpt1 = pt1.getX();
			double newYpt1 = pt1.getY();

			double newXpt2 = pt2.getX();
			double newYpt2 = pt2.getY();

			// First translation
			newXpt1 -= ptCenter.getX();
			newYpt1 -= ptCenter.getY();

			newXpt2 -= ptCenter.getX();
			newYpt2 -= ptCenter.getY();

			/// Scaling
			newXpt1 *= deplacement;
			newYpt1 *= deplacement;

			newXpt2 *= deplacement;
			newYpt2 *= deplacement;

			// Translate back
			newXpt1 += ptCenter.getX();
			newYpt1 += ptCenter.getY();

			newXpt2 += ptCenter.getX();
			newYpt2 += ptCenter.getY();

			pt1 = new Point2D.Double(newXpt1, newYpt1);
			pt2 = new Point2D.Double(newXpt2, newYpt2);

			PointsROI.addPoint(roi.getIndex(), pt1);
			PointsROI.addPoint(roi.getIndex(), pt2);
			PointsROI.addPoint(roi.getIndex(), pt3);

			roi.roiChanged(true);

			break;

		case PseudoY:

			double newXpt3 = pt3.getX();
			double newYpt3 = pt3.getY();

			newXpt3 -= ptCenter.getX();
			newYpt3 -= ptCenter.getY();

			newXpt3 *= deplacement;
			newYpt3 *= deplacement;

			newXpt3 += ptCenter.getX();
			newYpt3 += ptCenter.getY();

			pt3 = new Point2D.Double(newXpt3, newYpt3);

			PointsROI.addPoint(roi.getIndex(), pt1);
			PointsROI.addPoint(roi.getIndex(), pt2);
			PointsROI.addPoint(roi.getIndex(), pt3);

			roi.roiChanged(true);

			break;
		default:
			break;

		}
	}

}
