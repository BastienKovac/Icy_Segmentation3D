package kovac.res.quadric;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.w3c.dom.Element;
import org.w3c.dom.Node;

import icy.file.xml.XMLPersistent;
import icy.util.XMLUtil;
import kovac.shapes.EllipsoidOverlay;

public class PersistentSetQuadric implements XMLPersistent {

	private static String[] ID_COEFFS = new String[] { "a1", "a2", "a3", "a4", "a5", "a6", "a7", "a8", "a9", "a10" };
	private static String ID_CLASSNAME = "classname";
	private static String ID_QUADRIC = "quadricExpression";
	private static String ID_ID = "id";

	private Set<QuadricExpression> quadrics;

	public PersistentSetQuadric() {
		this.quadrics = new HashSet<QuadricExpression>();
	}

	public PersistentSetQuadric(QuadricExpression firstOne) {
		this.quadrics = new HashSet<QuadricExpression>();
		this.quadrics.add(firstOne);
	}

	public void addQuadric(QuadricExpression quadric) {
		quadrics.add(quadric);
	}

	public void removeQuadric(QuadricExpression quadric) {
		quadrics.remove(quadric);
	}

	public void clear() {
		quadrics.clear();
	}
	
	public List<EllipsoidOverlay> createOverlays() {
		List<EllipsoidOverlay> ellipsoids = new ArrayList<EllipsoidOverlay>();
		
		for (QuadricExpression quad : quadrics)
			ellipsoids.add(new EllipsoidOverlay(quad));
		
		return ellipsoids;
	}

	@Override
	public boolean loadFromXML(Node node) {
		if (node == null)
			return false;

		Node parent = XMLUtil.getElement(node, ID_CLASSNAME);
		double[] coeffs = null;
		for (Node n : XMLUtil.getChildren(parent, ID_QUADRIC)) {
			coeffs = new double[10];
			Element quad = (Element) n;
			String uniqueID = quad.getAttribute(ID_ID);
			QuadricExpression loadedQuad = new QuadricExpression(uniqueID);
			for (int i = 0; i < coeffs.length; i++) {
				coeffs[i] = XMLUtil.getElementDoubleValue(quad, ID_COEFFS[i], 0);
			}
			loadedQuad.setCoefficients(coeffs);
			quadrics.add(loadedQuad);
		}

		return true;
	}

	@Override
	public boolean saveToXML(Node node) {
		if (node == null)
			return false;

		Element parent = XMLUtil.addElement(node, ID_CLASSNAME, getClass().getName());

		for (QuadricExpression quad : quadrics) {
			Element quadric = XMLUtil.addElement(parent, ID_QUADRIC);
			XMLUtil.setAttributeValue(quadric, ID_ID, quad.getID());

			double[] coeffs = quad.getSimpleArray();

			for (int i = 0; i < coeffs.length; i++)
				XMLUtil.addElement(quadric, ID_COEFFS[i], Double.toString(coeffs[i]));
		}

		return true;
	}

}
