package kovac.gui.panels;

import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Map;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import kovac.groups.GroupEllipsoid;
import kovac.groups.Saving;
import kovac.shapes.EllipsoidOverlay;

/**
 * This panel represents the top panel of the statistics page, it shows the
 * number of created ellipsoids, and two combo boxes, one to select an
 * ellipsoid, the other to select a group of ellipsoid
 * 
 * @author bastien.kovac
 *
 */
public class TopEllipsoidPanel extends JPanel {

	/**
	 * To avoid warnings
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Displays the number of created ellipsoids
	 */
	private JLabel numberOfEllipsoid;

	/**
	 * Lists all existing group of ellipsoids
	 */
	private JComboBox<String> comboListGroup;
	/**
	 * Lists all existing ellipsoids
	 */
	private JComboBox<String> comboListEllipsoid;

	public TopEllipsoidPanel() {
		super();
		initComponents();
		initListeners();
	}

	/**
	 * Initialize the graphic components of the Panel
	 */
	private void initComponents() {

		this.setLayout(new GridLayout(3, 1, 3, 5));
		this.setBorder(new EmptyBorder(3, 3, 3, 3));

		numberOfEllipsoid = new JLabel(Saving.savedEllipsoids.size() + " ellipsoid(s) created");
		this.add(numberOfEllipsoid);

		comboListGroup = new JComboBox<String>();
		comboListEllipsoid = new JComboBox<String>();

		reloadStringValues();
		this.add(comboListGroup);
		this.add(comboListEllipsoid);

	}

	/**
	 * Initialize the listeners for the graphic components of the Panel
	 */
	private void initListeners() {

		comboListEllipsoid.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				String choice = (String) comboListEllipsoid.getSelectedItem();
				if (choice.equals("All"))
					return;
				Map<String, EllipsoidOverlay> map = Saving.savedEllipsoids;
				EllipsoidOverlay ellipsoidSelected = map.get(choice);
				Saving.hide(ellipsoidSelected);
			}
		});

		comboListGroup.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				String choice = (String) comboListGroup.getSelectedItem();
				if (choice.equals("All"))
					return;
				GroupEllipsoid groupSelected = Saving.savedGroups.get(choice);
				Saving.hide(groupSelected);
			}
		});

	}

	/**
	 * Updates the content of the two combo-boxes (Called after adding or
	 * removing an ellipsoid or a group of one)
	 */
	public void reloadStringValues() {
		numberOfEllipsoid = new JLabel(Saving.savedEllipsoids.size() + " ellipsoid(s) created");
		comboListGroup.removeAllItems();
		comboListEllipsoid.removeAllItems();
		comboListGroup.addItem("All");
		comboListEllipsoid.addItem("All");
		for (String s : Saving.savedGroups.keySet()) {
			comboListGroup.addItem(s);
		}
		for (String s : Saving.savedEllipsoids.keySet()) {
			comboListEllipsoid.addItem(s);
		}
	}

}
