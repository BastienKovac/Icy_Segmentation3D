package kovac.res.gui;

import icy.canvas.IcyCanvas;
import icy.gui.viewer.Viewer;
import icy.painter.Overlay;
import icy.sequence.DimensionId;
import kovac.res.util.ViewerUtil;
import kovac.shapes.AxisOverlay;
import plugins.adufour.viewers.OrthoViewer;

/**
 * This class extends the base OrthoViewer to modify some of its behaviors
 * 
 * @author bastien.kovac
 *
 */
public class CustomOrthoViewer extends OrthoViewer {

	@Override
	public String getCanvasClassName() {
		return CustomOrthoViewer.class.getName();
	}

	@Override
	public IcyCanvas createCanvas(Viewer viewer) {
		return new CustomOrthoCanvas(viewer);
	}

	/**
	 * This class represents the new Canvas associated with the new viewer
	 * 
	 * @author bastien.kovac
	 *
	 */
	public class CustomOrthoCanvas extends OrthoViewer.OrthoCanvas {

		/**
		 * To avoid warnings
		 */
		private static final long serialVersionUID = 1L;

		/**
		 * The current Overlay representing, with 3 lines, the current position
		 * on the VTK viewer
		 */
		private Overlay currentAxis;

		/**
		 * Builds a new CustomOrthoCanvas from a viewer
		 * 
		 * @param viewer
		 *            The viewer
		 */
		public CustomOrthoCanvas(Viewer viewer) {
			super(viewer);
		}
		
		/**
		 * Refreshes the coordinates of the displayed axis
		 */
		public void refreshAxis() {
			ViewerUtil.removeAllLinesOverlayFromVTK();
			currentAxis = new AxisOverlay("Axis", imageCoordinates());
			ViewerUtil.addOverlayToVTK(currentAxis);
		}

		/**
		 * @return The current observed position within the image
		 */
		public double[] imageCoordinates() {
			return new double[] { posX, posY, posZ };
		}

		/**
		 * @return The current rotation of the canvas
		 */
		public double[] imageRotation2D() {
			return new double[] { getRotationX(), getRotationY() };
		}

		@Override
		protected void mousePositionChanged(DimensionId dim, int x, int y) {
			super.mousePositionChanged(dim, x, y);
			ViewerUtil.removeOverlayFromVTK(currentAxis);
			currentAxis = new AxisOverlay("Axis", imageCoordinates());
			ViewerUtil.addOverlayToVTK(currentAxis);
		}

	}

}
