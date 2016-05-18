package kovac.ROI;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;

/**
 * This class is here to store the points representing a CustomEllipseROI Each
 * ROI is represented by an array of 3 points, and stored in a
 * list.
 * 
 * @author bastien.kovac
 */
public class PointsROI {

	/**
	 * The list of current ROIs
	 */
	private static List<PointsSet> rois = new ArrayList<PointsSet>();

	/**
	 * Add an ROI to the list
	 * 
	 * @param roiIndex
	 *            The index of the added ROI
	 */
	public static void addROI(int roiIndex) {
		PointsSet set = new PointsSet();
		rois.add(roiIndex, set);
	}

	/**
	 * @return The number of ROIs currently stored
	 */
	public static int getNumberOfDefinedROIs() {
		return rois.size();
	}

	/**
	 * Return the number of points that are actually defined for the given ROI
	 * 
	 * @param roiIndex
	 *            The index of the ROI
	 * @return The number of non-null points
	 */
	public static int getNumberOfDefinedPoints(int roiIndex) {
		if (roiIndex >= rois.size()) {
			return 0;
		}
		return rois.get(roiIndex).getNumberOfDefinedPoints();
	}

	/**
	 * Add a point to a given ROI
	 * 
	 * @param roiIndex
	 *            The index of the ROI
	 * @param pt
	 *            The point to add
	 */
	public static void addPoint(int roiIndex, Point2D pt) {
		rois.get(roiIndex).addPoint(pt);
	}

	/**
	 * Get one specific point from the given ROI
	 * 
	 * @param roiIndex
	 *            The index of the ROI
	 * @param ptIndex
	 *            The index of the point to retrieve
	 * @return The point with index ptIndex of the given ROI
	 */
	public static Point2D getPoint(int roiIndex, int ptIndex) {
		return rois.get(roiIndex).getPoint(ptIndex);
	}

	/**
	 * Clear the points of the given ROI, replacing every one of them by null
	 * (to keep array size)
	 * 
	 * @param roiIndex
	 *            The index of the ROI
	 */
	public static void clear(int roiIndex) {
		rois.get(roiIndex).clear();
	}

	/**
	 * Remove the last defined (not null) point of the given ROI Use this to
	 * implement cancel operations
	 * 
	 * @param roiIndex
	 *            The index of the ROI
	 * @return True if there was a point to remove, false if not
	 */
	public static boolean removeLastOne(int roiIndex) {
		if (roiIndex == rois.size() - 1) {
			if (!rois.get(roiIndex).removeLastOne()) {
				return false;
			}
		}
		return true;
	}

	/**
	 * This class stores 3 two dimensional points as an array
	 * 
	 * @author bastien.kovac
	 */
	private static class PointsSet {

		/**
		 * The array of points
		 */
		private Point2D[] points;
		/**
		 * The number of points
		 */
		private int nbPoints;

		/**
		 * Builds a new PointsSet with an empty array of size 3
		 */
		private PointsSet() {
			this.points = new Point2D[3];
			this.nbPoints = 0;
		}

		/**
		 * Add the given point, does nothing if there is already 3 points stored
		 * 
		 * @param pt
		 *            The points to add
		 */
		private void addPoint(Point2D pt) {
			if (nbPoints < 3) {
				points[nbPoints] = pt;
				nbPoints++;
			}
		}

		/**
		 * Get the point with given index.
		 * 
		 * @param ptIndex
		 *            The index of the point
		 * @return The point
		 */
		private Point2D getPoint(int ptIndex) {
			return points[ptIndex];
		}

		/**
		 * @return The number of non-null points in the array
		 */
		private int getNumberOfDefinedPoints() {
			int res = 0;
			for (Point2D p : points) {
				if (p != null) {
					res++;
				}
			}
			return res;
		}

		/**
		 * Remove the last defined point of the array
		 * 
		 * @return True if a point has been successfully removed, false if not
		 */
		private boolean removeLastOne() {
			boolean hasBeenRemoved = false;
			for (int i = points.length - 1; i >= 0; i--) {
				if (points[i] == null) {
					continue;
				}
				points[i] = null;
				hasBeenRemoved = true;
				break;
			}
			return hasBeenRemoved;
		}

		/**
		 * Empties the array (fill it with null values) and set nbPoints to 0
		 */
		private void clear() {
			for (int i = 0; i < points.length; i++) {
				points[i] = null;
			}
			nbPoints = 0;
		}

	}

}
