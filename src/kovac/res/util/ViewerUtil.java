package kovac.res.util;

import java.util.List;

import icy.canvas.IcyCanvas;
import icy.gui.dialog.MessageDialog;
import icy.gui.viewer.Viewer;
import icy.painter.Overlay;
import icy.sequence.DimensionId;
import icy.sequence.Sequence;
import kovac.res.gui.CustomOrthoViewer;
import kovac.res.gui.LinkListener;
import kovac.res.gui.LockedOrthoViewer;
import kovac.res.gui.CustomOrthoViewer.CustomOrthoCanvas;
import kovac.shapes.AxisOverlay;
import plugins.adufour.viewers.OrthoViewer.OrthoCanvas;
import plugins.adufour.viewers.OrthoViewer.OrthoCanvas.OrthoView;
import plugins.kernel.canvas.VtkCanvas;

/**
 * This class is here both to store the views the plugin works on
 * (Simultaneously an OrthoView and a 3D VTK view) and to ease the operations on
 * these views
 * 
 * @author bastien.kovac
 *
 */
public class ViewerUtil {

	/**
	 * The stored views
	 */
	private static Viewer vOrth, vVTK;
	/**
	 * The base sequence that was used to launch the plugin
	 */
	private static Sequence baseSeq;

	/**
	 * Set a Viewer as the OrthoViewer
	 * 
	 * @param v
	 *            The viewer
	 */
	public static void setOrth(Viewer v) {
		vOrth = v;
	}

	/**
	 * Set a Viewer as the VTK viewer
	 * 
	 * @param v
	 *            The viewer
	 */
	public static void setVTK(Viewer v) {
		vVTK = v;
	}

	/**
	 * Used to link the two viewers with one another (Should only be called
	 * after the two viewers are set up, the method will return if one of them
	 * is null)
	 */
	public static void linkViewers() {
		if (!areSet())
			return;
		vOrth.addFrameListener(new LinkListener(vVTK));
		vVTK.addFrameListener(new LinkListener(vOrth));
	}

	/**
	 * Set a Sequence as the base sequence <b>Should only be called once during
	 * the plugin execution</b>
	 * 
	 * @param s
	 *            The sequence
	 */
	public static void setBaseSeq(Sequence s) {
		baseSeq = s;
	}

	/**
	 * @return The orthogonal viewer
	 */
	public static Viewer getOrth() {
		return vOrth;
	}

	/**
	 * @return The VTK viewer
	 */
	public static Viewer getVTK() {
		return vVTK;
	}

	/**
	 * @return The base sequence
	 */
	public static Sequence getBaseSeq() {
		return baseSeq;
	}

	/**
	 * @return The canvas from the orthogonal viewer
	 */
	public static CustomOrthoCanvas getOrthCanvas() {
		if (!(vOrth.getCanvas() instanceof OrthoCanvas)) {
			MessageDialog.showDialog(
					"This plugin does not work for regular 2D Canvas, the sequence will now return to OrthoViewer");
			vOrth.setCanvas(CustomOrthoCanvas.class.getName());
		}
		return (CustomOrthoCanvas) vOrth.getCanvas();
	}

	/**
	 * @return The canvas from the VTK viewer
	 */
	public static IcyCanvas getVTKCanvas() {
		return vVTK.getCanvas();
	}

	/**
	 * Return the OrthoView from the orthogonal viewer corresponding to the
	 * given dimension
	 * 
	 * @param dim
	 *            The wanted dimension
	 * @return The corresponding OrthoView
	 */
	public static OrthoView getView(DimensionId dim) {
		switch (dim) {
		case X:
			return getOrthCanvas().getZYView();
		case Y:
			return getOrthCanvas().getXZView();
		case Z:
			return getOrthCanvas().getXYView();
		default:
			return null;
		}
	}

	/**
	 * @return The sequence from the orthogonal viewer
	 */
	public static Sequence getOrthSequence() {
		return vOrth.getSequence();
	}

	/**
	 * @return The sequence from the VTK viewer
	 */
	public static Sequence getVTKSequence() {
		return vVTK.getSequence();
	}

	/**
	 * @return The list of all overlays from the sequence of the VTK viewer
	 */
	public static List<Overlay> getVTKOverlays() {
		return getVTKSequence().getOverlays();
	}

	/**
	 * Sets the orthogonal viewer to the specified location (should be used to
	 * transition between Custom and Locked OrthoCanvas, so the position isn't
	 * reset on every change)
	 * 
	 * @param coords
	 *            The coordinates of the point we want to observe
	 */
	public static void setPositionOrth(double... coords) {
		vOrth.getCanvas().setPositionX((int) coords[0]);
		vOrth.getCanvas().setPositionY((int) coords[1]);
		vOrth.getCanvas().setPositionZ((int) coords[2]);
	}

	/**
	 * Sets the rotation of the orthogonal viewer to the given values (should be
	 * used to transition between Custom and Locked OrthoCanvas, so the rotation
	 * isn't reset on every change)
	 * 
	 * @param rotation
	 *            The value of the rotation (first X, then Y)
	 */
	public static void setRotationOrth(double... rotation) {
		vOrth.getCanvas().setRotationX(rotation[0]);
		vOrth.getCanvas().setRotationY(rotation[1]);
	}

	/**
	 * Switch the orthogonal viewer's current canvas to a CustomOrthoCanvas
	 */
	public static void goToCustom() {
		vOrth.setCanvas(CustomOrthoViewer.class.getName());
	}

	/**
	 * Switch the orthogonal viewer's current canvas to a LockedOrthoCanvas
	 */
	public static void goToLocked() {
		vOrth.setCanvas(LockedOrthoViewer.class.getName());
	}

	/**
	 * Switch the VTK viewer's current canvas to a VtkCanvas (usually only
	 * called when starting the plugin)
	 */
	public static void goToVTK() {
		vVTK.setCanvas(VtkCanvas.class.getName());
	}

	/**
	 * Remove the given Overlay from the VTK sequence
	 * 
	 * @param o
	 *            The overlay to remove
	 */
	public static void removeOverlayFromVTK(Overlay o) {
		getVTKSequence().removeOverlay(o);
	}

	/**
	 * Remove all LineOverlay from the VTK sequence, used to update and/or
	 * delete the three axis
	 */
	public static void removeAllLinesOverlayFromVTK() {
		for (Overlay o : getVTKOverlays()) {
			if (o instanceof AxisOverlay) {
				removeOverlayFromVTK(o);
			}
		}
	}
	
	public static void removeOverlays() {
		for (Overlay o : getVTKOverlays())
			removeOverlayFromVTK(o);
	}

	/**
	 * Add the given Overlay to the VTK sequence
	 * 
	 * @param o
	 *            The Overlay to add
	 */
	public static void addOverlayToVTK(Overlay o) {
		getVTKSequence().addOverlay(o);
	}

	/**
	 * @return The scale of the base sequence as an array of pixel sizes [x, y,
	 *         z]
	 */
	public static double[] getScale() {
		return baseSeq.getPixelSize();
	}

	/**
	 * @return An array representing the size of the base sequence in each
	 *         direction [x, y, z]
	 */
	public static double[] getSizes() {
		return new double[] { baseSeq.getSizeX(), baseSeq.getSizeY(), baseSeq.getSizeZ() };
	}

	/**
	 * @return The current position of the orthogonal viewer
	 */
	public static double[] getCurrentOrthCoordinates() {
		return getOrthCanvas().imageCoordinates();
	}

	/**
	 * @return The current rotation values of the orthogonal viewer
	 */
	public static double[] getCurrentOrthRotation() {
		return getOrthCanvas().imageRotation2D();
	}

	/**
	 * @return True if both vOrt and vVTK are set, false if not
	 */
	public static boolean areSet() {
		return (vOrth != null && vVTK != null);
	}

	/**
	 * Switches between custom and locked OrthoView
	 * 
	 * @param lock
	 *            True if we must lock the viewer, false if not
	 */
	public static void switchView(boolean lock) {
		double[] coordinates = getCurrentOrthCoordinates();
		double[] rotation = getCurrentOrthRotation();
		if (lock) {
			goToLocked();
			setPositionOrth(coordinates);
			setRotationOrth(rotation);
		} else {
			goToCustom();
			setPositionOrth(coordinates);
			setRotationOrth(rotation);
		}
		getOrthCanvas().refreshAxis();
	}

	/**
	 * Called to minimize the orthogonal and VTK viewers
	 */
	public static void minimizeViewers() {
		vOrth.setMinimized(true);
		vVTK.setMinimized(true);
	}
	
	/**
	 * Called to clear the viewers and the base sequence
	 */
	public static void clear() {
		vVTK = null;
		vOrth = null;
		baseSeq = null;
	}

}
