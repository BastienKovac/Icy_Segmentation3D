package kovac.shapes;

import java.util.ArrayList;
import java.util.List;

import icy.painter.Overlay;
import icy.type.point.Point3D;
import kovac.groups.Saving;
import kovac.maths.EllipsoidAlgorithm;
import kovac.res.util.ViewerUtil;
import plugins.BastienKovac.segmentation3d.Segmentation3D;

/**
 * This class is here to save and display the clicked points from
 * LockedOrthoCanvas
 * 
 * @author bastien.kovac
 * 
 */
public class Points {

	/**
	 * Saves the clicked points
	 */
	private static List<Point3D> points = new ArrayList<Point3D>();
	/**
	 * Saves the created overlays
	 */
	private static List<Overlay> overlays = new ArrayList<Overlay>();
	/**
	 * All group of points created
	 */
	private static List<GroupPointsOverlay> groups = new ArrayList<GroupPointsOverlay>();

	/**
	 * Add a point to the saving list
	 * 
	 * @param p
	 *            The point to save
	 */
	public static void addPoint(Point3D p) {
		if (Segmentation3D.isDiplayingPoints())
			displayPoint(p);
		points.add(p);
		if (points.size() >= Segmentation3D.getMinNumbPoints()) {
			Segmentation3D.setRunEnabled(true);
		}
	}

	/**
	 * Clears the point overlays displayed in the VTK view, removing them from
	 * the sequence
	 */
	public static void clearPointsOverlays() {
		for (Overlay o : overlays) {
			if (o instanceof PointOverlay)
				ViewerUtil.removeOverlayFromVTK(o);
		}
	}

	/**
	 * Displays a point as an Overlay on the VTK view
	 * 
	 * @param p
	 *            The point to display
	 */
	public static void displayPoint(Point3D p) {
		Overlay o = new PointOverlay("Point " + points.size(), p);
		ViewerUtil.addOverlayToVTK(o);
		overlays.add(o);
	}

	/**
	 * Calls the algorithm creating the ellipsoid fitting the saved points
	 */
	public static void createEllipsoid() {
		EllipsoidAlgorithm algo = new EllipsoidAlgorithm(points);
		EllipsoidOverlay ellipsoid = (EllipsoidOverlay) algo.generateEllipsoid();
		Segmentation3D.addSavedEllipsoid(ellipsoid);
		ViewerUtil.addOverlayToVTK(ellipsoid);
		ellipsoid.setName("Ellipsoid " + Saving.getNumberOfEllipsoids());
		ellipsoid.validate();
		if (Segmentation3D.isDiplayingPoints()) {
			GroupPointsOverlay group = new GroupPointsOverlay("Group number " + groups.size(), points);
			groups.add(group);
			ViewerUtil.addOverlayToVTK(group);
		}
		clearPointsOverlays();
		points.clear();
	}

}
