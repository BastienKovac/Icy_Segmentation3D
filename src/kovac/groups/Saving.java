package kovac.groups;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import icy.painter.Overlay;
import kovac.gui.MainFrame;
import kovac.res.util.ViewerUtil;
import kovac.shapes.EllipsoidOverlay;

/**
 * This class handles the Saving of the created ellipsoids and group of
 * ellipsoids
 * 
 * @author bastien.kovac
 *
 */
public class Saving {

	/**
	 * The saved ellipsoids (each must have a different name)
	 */
	public static Map<String, EllipsoidOverlay> savedEllipsoids = new HashMap<String, EllipsoidOverlay>();
	/**
	 * The saved groups (each must have a different name)
	 */
	public static Map<String, GroupEllipsoid> savedGroups = new HashMap<String, GroupEllipsoid>();

	/**
	 * Saves the given ellipsoid with the given name
	 * 
	 * @param e
	 *            The ellipsoid to save
	 * @param name
	 *            The name to give to it
	 */
	public static void saveEllipsoid(EllipsoidOverlay e, String name) {
		if (savedEllipsoids.containsKey(name)) {
			throw new RuntimeException("This ellipsoid's name is already used");
		} else {
			savedEllipsoids.put(name, e);
		}
		MainFrame.reload();
	}

	/**
	 * Gets the ellipsoid with given name
	 * 
	 * @param name
	 *            The name
	 * @return The corresponding ellipsoid
	 */
	public static EllipsoidOverlay getEllipsoid(String name) {
		if (!savedEllipsoids.containsKey(name)) {
			throw new RuntimeException("No ellipsoid with such name");
		}
		return savedEllipsoids.get(name);
	}

	/**
	 * @return The number of saved ellipsoid
	 */
	public static int getNumberOfEllipsoids() {
		return savedEllipsoids.size();
	}

	/**
	 * @return The number of saved groups
	 */
	public static int getNumberOfGroups() {
		return savedGroups.size();
	}

	/**
	 * Gets the group with given name
	 * 
	 * @param name
	 *            The name
	 * @return The corresponding group
	 */
	public static GroupEllipsoid getGroupEllipsoid(String name) {
		if (!savedGroups.containsKey(name)) {
			throw new RuntimeException("No group with such name");
		}
		return savedGroups.get(name);
	}

	/**
	 * Saves the given group with the given name
	 * 
	 * @param g
	 *            The group to save
	 * @param name
	 *            The name to give to it
	 */
	public static void saveGroupEllipsoid(GroupEllipsoid g, String name) {
		if (savedEllipsoids.containsKey(name)) {
			throw new RuntimeException("This group's name is already used");
		}
		MainFrame.reload();
	}
	
	public static void hide(EllipsoidOverlay e) {
		for (Overlay o : ViewerUtil.getVTKOverlays()) {
			if (!(o instanceof EllipsoidOverlay))
				continue;
			if (!o.equals(e)) {
				ViewerUtil.removeOverlayFromVTK(o);
			}
		}
	}
	
	public static void hide(GroupEllipsoid g) {
		for (Overlay o : ViewerUtil.getVTKOverlays()) {
			if (!(o instanceof EllipsoidOverlay))
				continue;
			if (!(g.contains(o))) {
				ViewerUtil.removeOverlayFromVTK(o);
			}
		}
	}
	
	public static Collection<EllipsoidOverlay> getAllEllipsoids() {
		return savedEllipsoids.values();
	}
	
}
