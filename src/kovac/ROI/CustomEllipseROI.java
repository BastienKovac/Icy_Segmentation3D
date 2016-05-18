package kovac.ROI;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

import icy.canvas.IcyCanvas;
import icy.canvas.IcyCanvas2D;
import icy.resource.ResourceUtil;
import icy.roi.ROI2D;
import icy.sequence.DimensionId;
import icy.sequence.Sequence;
import icy.type.point.Point5D;
import icy.type.point.Point5D.Double;
import icy.util.EventUtil;
import icy.util.ShapeUtil;
import kovac.res.enums.Orientation;
import kovac.res.gui.LockedOrthoViewer.LockedOrthoCanvas;
import kovac.res.util.ViewerUtil;
import kovac.shapes.Ellipse;

/**
 * This class is the new ROI defined by the plugin, it is a 2 dimensional
 * ellipse which is drawn by clicking on 3 different points. Its behavior is
 * similar to the DrawEllipse plugin developped by Guillaume Debrito in 2015
 * 
 * @author bastien.kovac
 *
 */
public class CustomEllipseROI extends ROI2D {

	/**
	 * The shape of the ROI (can be a point, a line or an ellipse)
	 */
	private Shape shape;
	/**
	 * Indicates if the ellipse is fully drawn or not
	 */
	private boolean drawn;
	/**
	 * The ellipse's parameters, in order : x coordinate of the center, y
	 * coordinate of the center, semi-width a, semi-height b and angle of
	 * inclination
	 */
	private double[] elParams;
	/**
	 * The index of the ROI, meaning its position in PointsROI's list
	 */
	private int indexROI;

	/**
	 * The ellipse corresponding to the ROI
	 */
	private Ellipse ellipse;

	/**
	 * The dimension in which the ROI is drawn (X,Y), (X,Z) or (Y,Z)
	 */
	private DimensionId dim;

	/**
	 * The class which handles this ROI's geometric transformations
	 */
	private Transformations linkedTransformations;

	/**
	 * Builds a CustomEllipseROI from a starting point
	 * 
	 * @param pt
	 *            The five dimensional starting point
	 */
	public CustomEllipseROI(Point5D pt) {
		setName("Segmentation3D");
		setIcon(ResourceUtil.ICON_ROI_OVAL);
		drawn = false;
		// Dummy value to avoid NullPointerException
		this.shape = new Rectangle2D.Double();
		elParams = null;
		indexROI = PointsROI.getNumberOfDefinedROIs();
		PointsROI.addROI(indexROI);
		linkedTransformations = new Transformations(this);
	}

	/**
	 * Builds a CustomEllipseROI from nothing
	 */
	public CustomEllipseROI() {
		setName("Segmentation3D");
		setIcon(ResourceUtil.ICON_ROI_OVAL);
		drawn = false;
		// Dummy value to avoid NullPointerException
		this.shape = new Rectangle2D.Double();
		elParams = null;
		indexROI = PointsROI.getNumberOfDefinedROIs();
		PointsROI.addROI(indexROI);
		linkedTransformations = new Transformations(this);
	}

	/**
	 * @return This ROI's index
	 */
	public int getIndex() {
		return indexROI;
	}

	/**
	 * Can be called to specify which dimension this ROI is drawn in
	 * 
	 * @param dim
	 *            The dimension in which the ROI is drawn
	 */
	public void setDim(DimensionId dim) {
		this.dim = dim;
		String plane = "";
		switch (dim) {
		case X:
			plane = "(Y;Z)";
			break;
		case Y:
			plane = "(X;Z)";
			break;
		case Z:
			plane = "(X;Y)";
			break;
		default:
			break;
		}
		setName("Segmentation3D, plane " + plane);
	}

	@Override
	public boolean isOverEdge(IcyCanvas canvas, double x, double y) {
		// use bigger stroke for isOver test for easier intersection
		final double strk = painter.getAdjustedStroke(canvas) * 3;
		final Rectangle2D rect = new Rectangle2D.Double(x - (strk * 0.5), y - (strk * 0.5), strk, strk);
		final Rectangle2D roiBounds = getBounds2D();

		// special test for empty object (point or orthogonal line)
		if (roiBounds.isEmpty())
			return rect.intersectsLine(roiBounds.getMinX(), roiBounds.getMinY(), roiBounds.getMaxX(),
					roiBounds.getMaxY());

		// fast intersect test to start with
		if (roiBounds.intersects(rect))
			// use flatten path, intersects on curved shape return incorrect
			// result
			return ShapeUtil.pathIntersects(shape.getPathIterator(null, 0.1), rect);

		return false;
	}

	@Override
	public boolean contains(double x, double y) {
		return shape.contains(x, y);
	}

	@Override
	public boolean contains(double x, double y, double w, double h) {
		return shape.contains(x, y, w, h);
	}

	@Override
	public boolean intersects(double x, double y, double w, double h) {
		return shape.intersects(x, y, w, h);
	}

	@Override
	public Rectangle2D computeBounds2D() {
		return new Rectangle2D.Double();
	}

	@Override
	protected ROIPainter createPainter() {
		return new CustomEllipseROIPainter();
	}

	@Override
	public boolean isActiveFor(IcyCanvas canvas) {
		if (canvas instanceof IcyCanvas2D || canvas instanceof LockedOrthoCanvas) {
			return true;
		}
		return false;
	}

	@Override
	public boolean hasSelectedPoint() {
		// Selected points are not supported
		return false;
	}

	/**
	 * @return True if the ellipse is fully drawn, false if not
	 */
	public boolean isDrawn() {
		return drawn;
	}

	/**
	 * Can be called to inform the ROI it must redraw (Usually after a
	 * transformation)
	 * 
	 * @param flag
	 *            True if the ellipse must be informed it's fully drawn, false
	 *            if not
	 */
	public void setDrawn(boolean flag) {
		drawn = flag;
	}

	/**
	 * This class handles the painting of the CustomEllipseROI
	 * 
	 * @author bastien.kovac
	 *
	 */
	private class CustomEllipseROIPainter extends ROI2DPainter {

		/**
		 * The last point clicked by the user
		 */
		private Point2D lastSelectedPoint;

		/**
		 * The color of the ROI's edge
		 */
		private Color edgeColor;
		/**
		 * The color of the ROI's content
		 */
		private Color fillColor;

		/**
		 * Builds a new CustomEllipseROIPainter
		 */
		public CustomEllipseROIPainter() {
			super();
			lastSelectedPoint = null;

			edgeColor = Color.RED;
			fillColor = new Color(1f, 0f, 0f, .2f);
		}

		@Override
		public void paint(Graphics2D g, Sequence sequence, IcyCanvas canvas) {
			if (!isActiveFor(canvas)) {
				return;
			}
			if (canvas instanceof LockedOrthoCanvas) {
				drawROIOrtho(g, sequence, canvas);
			}
			if (!(canvas instanceof LockedOrthoCanvas) && canvas instanceof IcyCanvas2D) {
				drawROI(g, sequence, canvas);
			}
		}

		@Override
		public void keyPressed(KeyEvent e, Double imagePoint, IcyCanvas canvas) {
			super.keyPressed(e, imagePoint, canvas);
			if (!isActiveFor(canvas)) {
				return;
			}
			if (CustomEllipseROI.this.isSelected()) {
				// Do stuff
				switch (e.getKeyCode()) {
				case KeyEvent.VK_ADD:
					if (EventUtil.isShiftDown(e)) {
						linkedTransformations.scaleEllipse(true);
					} else {
						linkedTransformations.rotate(true);
					}
					break;
				case KeyEvent.VK_SUBTRACT:
					if (EventUtil.isShiftDown(e)) {
						linkedTransformations.scaleEllipse(false);
					} else {
						linkedTransformations.rotate(false);
					}
					break;
				case KeyEvent.VK_LEFT:
					if (EventUtil.isShiftDown(e)) {
						linkedTransformations.changeShape(Orientation.xMinus);
					} else {
						linkedTransformations.moveTo(Orientation.xMinus);
					}
					break;
				case KeyEvent.VK_RIGHT:
					if (EventUtil.isShiftDown(e)) {
						linkedTransformations.changeShape(Orientation.xPlus);
					} else {
						linkedTransformations.moveTo(Orientation.xPlus);
					}
					break;
				case KeyEvent.VK_UP:
					if (EventUtil.isShiftDown(e)) {
						linkedTransformations.changeShape(Orientation.yPlus);
					} else {
						linkedTransformations.moveTo(Orientation.yPlus);
					}
					break;
				case KeyEvent.VK_DOWN:
					if (EventUtil.isShiftDown(e)) {
						linkedTransformations.changeShape(Orientation.yMinus);
					} else {
						linkedTransformations.moveTo(Orientation.yMinus);
					}
					break;
				default:
					break;
				}
			}
			if (e.getKeyCode() == KeyEvent.VK_UP || e.getKeyCode() == KeyEvent.VK_DOWN) {
				if (!ViewerUtil.areSet())
					return;
				ViewerUtil.getOrthCanvas().setPositionZ((int) ViewerUtil.getCurrentOrthCoordinates()[2]);
			}

		}

		@Override
		public void mouseClick(MouseEvent e, Double imagePoint, IcyCanvas canvas) {
			super.mouseClick(e, imagePoint, canvas);
			if (canvas instanceof IcyCanvas2D) {
				// Retrieve selected point
				lastSelectedPoint = imagePoint.toPoint2D();
				if (canvas instanceof LockedOrthoCanvas) {
					
				}
				click2D(e, imagePoint, canvas);
			}
		}

		/**
		 * Handles mouse click event if we are in 2D canvas
		 * 
		 * @param e
		 *            The MouseEvent
		 * @param imagePoint
		 *            The imagePoint
		 * @param canvas
		 *            The canvas
		 */
		private void click2D(MouseEvent e, Double imagePoint, IcyCanvas canvas) {
			// Selection (if ellipse is fully drawn)
			if (drawn) {
				if (CustomEllipseROI.this.contains(lastSelectedPoint)) {
					CustomEllipseROI.this.setSelected(true);
					setSelectedColor(true);
				} else {
					setSelectedColor(false);
					CustomEllipseROI.this.setSelected(false);
				}
			}
			// Check how many points we already stocked
			if (EventUtil.isLeftMouseButton(e)) {
				if (PointsROI.getNumberOfDefinedPoints(indexROI) < 3) {
					PointsROI.addPoint(indexROI, lastSelectedPoint);
				}
			} else {
				if (EventUtil.isRightMouseButton(e)) {
					drawn = false;
					if (!PointsROI.removeLastOne(indexROI)) {
						canvas.getSequence().removeROI(CustomEllipseROI.this);
						PointsROI.clear(indexROI);
					}
				}
			}
		}

		@Override
		protected void drawROI(Graphics2D g, Sequence sequence, IcyCanvas canvas) {
			g.setColor(edgeColor);
			switch (PointsROI.getNumberOfDefinedPoints(indexROI)) {
			case 1:
				CustomEllipseROI.this.shape = new Rectangle2D.Double(PointsROI.getPoint(indexROI, 0).getX(),
						PointsROI.getPoint(indexROI, 0).getY(), 0.5, 0.5);
				// Refresh the Bounding box since we changed the shape
				CustomEllipseROI.this.roiChanged(true);
				g.draw(shape);
				break;
			case 2:
				CustomEllipseROI.this.shape = new Line2D.Double(PointsROI.getPoint(indexROI, 0),
						PointsROI.getPoint(indexROI, 1));
				CustomEllipseROI.this.roiChanged(true);
				g.draw(shape);
				break;
			case 3:
				if (!drawn) {
					ellipse = new Ellipse(PointsROI.getPoint(indexROI, 0), PointsROI.getPoint(indexROI, 1),
							PointsROI.getPoint(indexROI, 2));
					elParams = ellipse.getParams();
					// Ellipse without angle
					Shape baseShape = new Ellipse2D.Double(elParams[0] - elParams[2], elParams[1] - elParams[3],
							2 * elParams[2], 2 * elParams[3]);
					// Rotate it
					CustomEllipseROI.this.shape = AffineTransform
							.getRotateInstance(elParams[4], elParams[0], elParams[1]).createTransformedShape(baseShape);
					CustomEllipseROI.this.roiChanged(true);
					drawn = true;
				}
				g.draw(shape);
				g.setColor(fillColor);
				g.fill(shape);
				break;
			default:
				break;
			}
		}

		/**
		 * This method should be called only to paint the ROI in an OrthoCanvas.
		 * The method will exit if the ROI's dimension isn't set
		 * 
		 * @param g
		 *            The Graphics2D object
		 * @param sequence
		 *            The sequence the ROI is drawn on
		 * @param canvas
		 *            The canvas in which the sequence is displayed
		 */
		protected void drawROIOrtho(Graphics2D g, Sequence sequence, IcyCanvas canvas) {
			// If dim isn't set up, wait
			if (CustomEllipseROI.this.dim == null)
				return;

			sequence = ViewerUtil.getOrthSequence();
			canvas = ViewerUtil.getOrthCanvas();
			
			drawROI(g, sequence, canvas);

		}

		/**
		 * Change the ROI's color depending on if it's selected or not
		 * 
		 * @param selected
		 *            True if the ROI is selected, false if not
		 */
		public void setSelectedColor(boolean selected) {
			if (selected) {
				edgeColor = new Color(1f, 0f, 0f, .8f);
				fillColor = new Color(0.8f, 0.2f, 0f, .2f);
			} else {
				edgeColor = Color.RED;
				fillColor = new Color(1f, 0f, 0f, .2f);
			}
		}

	}

}
