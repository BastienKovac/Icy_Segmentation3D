package kovac.shapes;

import javax.swing.JPanel;

import Jama.Matrix;
import icy.painter.Overlay;
import icy.painter.VtkPainter;
import kovac.groups.Saving;
import kovac.gui.panels.EllipsoidPanel;
import kovac.res.quadric.QuadricExpression;
import kovac.res.util.ViewerUtil;
import vtk.vtkActor;
import vtk.vtkContourFilter;
import vtk.vtkPolyDataMapper;
import vtk.vtkProp;
import vtk.vtkQuadric;
import vtk.vtkSampleFunction;
import vtk.vtkTransform;

/**
 * This class is here to display an Ellipsoid as a three dimensional VTK Object,
 * in the VTK view
 * 
 * @author bastien.kovac
 *
 */
public class EllipsoidOverlay extends Overlay implements VtkPainter {

	/**
	 * The vtkActor corresponding to the displayed ellipsoid
	 */
	private vtkActor ellipsoidActor;
	/**
	 * The ellipsoid to display
	 */
	private Ellipsoid ellipsoid;
	/**
	 * This is the quadratic expression of the ellipsoid, used to display it
	 */
	private QuadricExpression quadric;
	/**
	 * The name of the Overlay
	 */
	private String name;
	/**
	 * True if this Overlay is saved in kovac.groups.Saving, false if it isn't
	 */
	public boolean isSaved;

	/**
	 * Builds an EllipsoidOverlay from a QuadricExpression
	 * 
	 * @param q
	 *            The quadric expression
	 */
	public EllipsoidOverlay(QuadricExpression q) {
		super("Ellipsoid");
		this.isSaved = false;
		this.quadric = q;
		initQuadric();
	}

	/**
	 * Initialize the vtkActor from 10 quadratic coefficients
	 */
	private void initQuadric() {

		Matrix q = quadric.getCoefficients();
		q.print(3, 4);
		vtkQuadric quadricVTK = new vtkQuadric();
		double[] coeffs = q.getColumnPackedCopy();
		quadricVTK.SetCoefficients(coeffs[0], coeffs[1], coeffs[2], coeffs[3], coeffs[4], coeffs[5], coeffs[6],
				coeffs[7], coeffs[8], coeffs[9]);

		vtkSampleFunction sample = new vtkSampleFunction();
		sample.SetImplicitFunction(quadricVTK);

		double[] sizes = ViewerUtil.getSizes();
		double xMin = -sizes[0] / 2, yMin = -sizes[1] / 2, zMin = -sizes[2] / 2;
		double xMax = sizes[0] / 2, yMax = sizes[1] / 2, zMax = sizes[2] / 2;

		sample.SetSampleDimensions(50, 50, 50);
		sample.SetModelBounds(xMin, xMax, yMin, yMax, zMin, zMax);
		sample.ComputeNormalsOn();

		vtkContourFilter contourFilter = new vtkContourFilter();
		contourFilter.SetInputConnection(sample.GetOutputPort());
		contourFilter.GenerateValues(1, 0.0, 0.0);
		contourFilter.Update();

		vtkPolyDataMapper mapper = new vtkPolyDataMapper();
		mapper.SetInputConnection(contourFilter.GetOutputPort());

		ellipsoidActor = new vtkActor();
		ellipsoidActor.SetMapper(mapper);
		setColor(255, 0, 0);
		ellipsoidActor.GetProperty().SetOpacity(0.5);
		// Translate back to center of image
		vtkTransform translation = new vtkTransform();
		ellipsoidActor.SetUserTransform(translation);
		translation.Translate(sizes[0] / 2, sizes[1] / 2, sizes[2] / 2);

	}

	@Override
	public vtkProp[] getProps() {
		return new vtkProp[] { this.ellipsoidActor };
	}

	@Override
	public void setName(String name) {
		if (isSaved) {
			Saving.savedEllipsoids.remove(this.name);
			Saving.savedEllipsoids.put(name, this);
		}
		this.name = name;
		super.setName(this.name);
	}

	@Override
	public String getName() {
		return this.name;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		EllipsoidOverlay other = (EllipsoidOverlay) obj;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		return true;
	}

	/**
	 * Saves the ellipsoidOverlay in kovac.groups.Saving
	 */
	public void validate() {
		Saving.saveEllipsoid(this, name);
		this.isSaved = true;
	}

	/**
	 * Change the color of the vtkActor
	 * 
	 * @param rgb
	 *            The RGB values of the color, throw an exception if different
	 *            from 3
	 */
	public void setColor(double... rgb) {
		if (rgb.length != 3) {
			throw new IllegalArgumentException("Needs 3 arguments to create color");
		}
		this.ellipsoidActor.GetProperty().SetColor(rgb);
	}

	/**
	 * @return The volume of the ellipsoid
	 */
	public double getVolume() {
		if (this.ellipsoid == null)
			return Double.NaN;
		return this.ellipsoid.getVolume();
	}

	/**
	 * @return The surface of the ellipsoid
	 */
	public double getSurface() {
		if (this.ellipsoid == null)
			return Double.NaN;
		return this.ellipsoid.getSurface();
	}

	@Override
	public JPanel getOptionsPanel() {
		return new EllipsoidPanel();
	}

	/**
	 * @return The current color of the vtkActor
	 */
	public double[] getColor() {
		return this.ellipsoidActor.GetProperty().GetColor();
	}

	public QuadricExpression getQuadricExpression() {
		return quadric;
	}

}
