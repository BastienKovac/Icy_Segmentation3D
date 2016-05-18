package kovac.res.gui;

import icy.canvas.IcyCanvas;
import icy.gui.viewer.Viewer;
import icy.roi.ROI;
import icy.sequence.DimensionId;
import icy.type.point.Point3D;
import icy.type.point.Point5D;
import kovac.ROI.CustomEllipseROI;
import kovac.res.util.ViewerUtil;
import kovac.shapes.Points;
import plugins.BastienKovac.segmentation3d.Segmentation3D;

/**
 * This class extends CustomOrthoViewer and is used to "lock" the canvas,
 * meaning that you can't navigate within the image anymore
 * 
 * @author bastien.kovac
 *
 */
public class LockedOrthoViewer extends CustomOrthoViewer {

	@Override
	public String getCanvasClassName() {
		return LockedOrthoCanvas.class.getName();
	}

	@Override
	public IcyCanvas createCanvas(Viewer viewer) {
		return new LockedOrthoCanvas(viewer);
	}

	/**
	 * This class represents the canvas associated with this viewer
	 * 
	 * @author bastien.kovac
	 *
	 */
	public class LockedOrthoCanvas extends CustomOrthoCanvas {

		/**
		 * To avoid warnings
		 */
		private static final long serialVersionUID = 1L;

		/**
		 * Coordinates of the user's mouse when clicking
		 */
		private double xClick, yClick, zClick;
		/**
		 * The ROIs in each dimension
		 */
		private CustomEllipseROI roiXY, roiXZ, roiYZ;

		/**
		 * The dimension the user's mouse is when clicking
		 */
		private DimensionId currentDimension;

		/**
		 * Creates canvas from viewer
		 * 
		 * @param viewer
		 *            The viewer
		 */
		public LockedOrthoCanvas(Viewer viewer) {
			super(viewer);
		}

		@Override
		protected void mousePositionChanged(DimensionId dim, int x, int y) {
			currentDimension = dim;
			switch (dim) {
			case X:
				xClick = imageCoordinates()[0];
				yClick = (y * getSequence().getSizeY()) / getZYView().getBounds().getHeight();
				zClick = (x * getSequence().getSizeZ()) / getZYView().getBounds().getWidth();
				break;
			case Z:
				xClick = (x * getSequence().getSizeX()) / getXYView().getBounds().getWidth();
				yClick = (y * getSequence().getSizeY()) / getXYView().getBounds().getHeight();
				zClick = imageCoordinates()[2];
				break;
			case Y:
				xClick = (x * getSequence().getSizeX()) / getXZView().getBounds().getWidth();
				yClick = imageCoordinates()[1];
				zClick = (y * getSequence().getSizeZ()) / getXZView().getBounds().getHeight();
				break;
			default:
				break;
			}
			switch (Segmentation3D.getChosenMethod()) {
			case Ellipses:
				createROIs();
				break;
			case Points:
				Points.addPoint(new Point3D.Double(xClick, yClick, zClick));
				break;
			default:
				break;
			}
			refresh();
		}
		
		@Override
		public void repaint() {
			super.repaint();
		}

		/**
		 * @return True if all three ROIs are drawn, false if not
		 */
		public boolean allDrawn() {
			return (roiXY.isDrawn() && roiXZ.isDrawn() && roiYZ.isDrawn());
		}

		/**
		 * @return The last clicked coordinates on the canvas
		 */
		public double[] getClickCoordinates() {
			return new double[] { xClick, yClick, zClick };
		}

		/**
		 * Create the ROI in the current dimension (if its drawing hasn't
		 * already started)
		 */
		public void createROIs() {
			switch (currentDimension) {
			case X:
				if (roiYZ != null)
					return;

				roiYZ = (CustomEllipseROI) ROI.create(CustomEllipseROI.class.getName(),
						new Point5D.Double(xClick, yClick, zClick, -1, -1));
				roiYZ.setDim(currentDimension);
				ViewerUtil.getOrthSequence().addROI(roiYZ);
				roiYZ.roiChanged(true);

				break;
			case Z:
				if (roiXY != null)
					return;
				roiXY = (CustomEllipseROI) ROI.create(CustomEllipseROI.class.getName(),
						new Point5D.Double(xClick, yClick, zClick, -1, -1));
				roiXY.setDim(currentDimension);
				ViewerUtil.getOrthSequence().addROI(roiXY);
				roiXY.roiChanged(true);

				break;
			case Y:
				if (roiXZ != null)
					return;

				roiXZ = (CustomEllipseROI) ROI.create(CustomEllipseROI.class.getName(),
						new Point5D.Double(xClick, yClick, zClick, -1, -1));
				roiXZ.setDim(currentDimension);
				ViewerUtil.getOrthSequence().addROI(roiXZ);
				roiXZ.roiChanged(true);
	
				break;
			default:
				break;
			}
		}

	}

}
