package kovac.groups;

import java.util.HashSet;
import java.util.Set;

import icy.painter.Overlay;
import kovac.shapes.EllipsoidOverlay;

/**
 * This class handles a group of unique ellipsoids. It has the same behavior as
 * an individual ellipsoid, only applied to all which belong to the group.
 * 
 * @author bastien.kovac
 *
 */
public class GroupEllipsoid {

	/**
	 * This set handles the saving of the ellipsoids and makes sure they are
	 * unique (uniqueness is defined by the ellipsoids' name)
	 */
	private Set<EllipsoidOverlay> ellipsoids;
	/**
	 * The name of the group
	 */
	private String name;

	/**
	 * Builds a new empty group of ellipsoids with the given name
	 * 
	 * @param name
	 *            The name of the group
	 */
	public GroupEllipsoid(String name) {
		this.ellipsoids = new HashSet<EllipsoidOverlay>();
		this.name = name;
		Saving.savedGroups.put(name, this);
	}

	/**
	 * Adds an ellipsoid to the group
	 * 
	 * @param e
	 *            The ellipsoid to add
	 */
	public void addEllipsoid(EllipsoidOverlay e) {
		this.ellipsoids.add(e);
	}

	/**
	 * Gets the ellipsoid with the given name from the group if it exists.
	 * Return null if it doesn't exist
	 * 
	 * @param name
	 *            The name of the wanted ellipsoid
	 * @return The corresponding ellipsoid
	 */
	public EllipsoidOverlay getEllipsoid(String name) {
		EllipsoidOverlay res = null;
		for (EllipsoidOverlay e : ellipsoids) {
			if (e.getName().equals(name)) {
				res = e;
			}
		}
		return res;
	}

	/**
	 * Sets the color of every ellipsoid to the given value
	 * 
	 * @param rgbColor
	 *            The RGB value of the color
	 */
	public void setColor(double... rgbColor) {
		if (rgbColor.length != 3) {
			throw new RuntimeException("Needs three arguments to create color");
		}
		for (EllipsoidOverlay e : ellipsoids) {
			e.setColor(rgbColor);
		}
	}

	/**
	 * Change the name of the group to the given value
	 * 
	 * @param name
	 *            The name to give to the group
	 */
	public void setName(String name) {
		Saving.savedGroups.remove(this.name);
		this.name = name;
		Saving.savedGroups.put(name, this);
	}

	/**
	 * @return One ellipsoid from the group
	 */
	public EllipsoidOverlay getOneEllipsoid() {
		return this.ellipsoids.iterator().next();
	}

	/**
	 * @return The total volume of all ellipsoids of the group
	 */
	public double getTotalVolume() {
		double res = 0;
		for (EllipsoidOverlay e : ellipsoids) {
			res += e.getVolume();
		}
		return res;
	}

	/**
	 * @return The total surface of all ellipsoids of the group
	 */
	public double getTotalSurface() {
		double res = 0;
		for (EllipsoidOverlay e : ellipsoids) {
			res += e.getSurface();
		}
		return res;
	}

	/**
	 * @return The name of the group
	 */
	public String getName() {
		return name;
	}

	/**
	 * Checks if a given Overlay is saved in the group's set
	 * 
	 * @param o
	 *            The overlay
	 * @return True if the overlay is in the set, false if it is not
	 */
	public boolean contains(Overlay o) {
		boolean res = false;
		for (EllipsoidOverlay e : ellipsoids) {
			if (o.equals(e)) {
				res = true;
			}
		}
		return res;
	}

}
