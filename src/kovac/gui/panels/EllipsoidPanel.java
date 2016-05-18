package kovac.gui.panels;

import javax.swing.JPanel;

/**
 * This panel is here to handle every information about a selected ellipsoid or
 * group of ellipsoid. It supports some basic operations : change of name, color
 * ... When an ellipsoid or group is selected, every other one is hidden from
 * the VTK viewer to allow better visibility
 * 
 * @author bastien.kovac
 *
 */
public class EllipsoidPanel extends JPanel {
	
	// TODO -> Everything

	/**
	 * To avoid warnings
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Builds a new EllipsoidPanel for the given Ellipsoid
	 */
	public EllipsoidPanel() {
		super();
		initComponents();
		initListeners();
	}

	/**
	 * Initialize the graphic components of the Panel
	 */
	private void initComponents() {

	}

	/**
	 * Initialize the listeners for the graphic components of the panel
	 */
	private void initListeners() {
		
	}

}
