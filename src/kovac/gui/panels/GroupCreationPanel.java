package kovac.gui.panels;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

import kovac.groups.Saving;
import kovac.gui.res.IntegerField;

/**
 * This panel represents the group creation's window, it works with a CardLayout.
 * Ellipsoids can be chosen to be added to a group manually, or depending on their volume or surface
 * The user has to set an interval minimum/maximum and every ellipsoid whose volume or surface matches this interval will be added to a new group
 * 
 * @author bastien.kovac
 *
 */
/**
 * @author bastien.kovac
 *
 */
public class GroupCreationPanel extends JPanel {

	/**
	 * To avoid warnings
	 */
	private static final long serialVersionUID = 1L;

	// String fields
	final static String VOLUMEPANEL = "Sorting ellipsoids by volume";
	final static String SURFACEPANEL = "Sorting ellipsoids by surface";
	final static String MANUALPANEL = "Sorting ellipsoids manually";

	/**
	 * This enumeration represents the cards from the panel
	 * 
	 * @author bastien.kovac
	 *
	 */
	private enum CARD {
		volume, surface, manual
	}

	// Columns' names
	private static final String[] columnNames = new String[] { "Ellipsoid", "Volume", "Surface" };

	/**
	 * Scroll Pane (To handle scrolling of the Table if there is too many
	 * ellipsoids)
	 */
	private JScrollPane scrollPane;

	// Some labels ...
	private JLabel minValueLabel;
	private JLabel maxValueLabel;

	/**
	 * Minimum and maximum value for the volume
	 */
	private IntegerField minValueVolume, minValueSurface;
	/**
	 * Minimum and maximum value for the surface
	 */
	private IntegerField maxValueVolume, maxValueSurface;

	/**
	 * The created group will have this field's content as name
	 */
	private JTextField groupName;

	/**
	 * JButton to validate the group
	 */
	private JButton validateGroup;

	/**
	 * Combo box to choose how to create the group and to navigate through the
	 * cards
	 */
	private JComboBox<String> criteriasBox;

	/**
	 * Table where we display all the ellipsoids for manual selection
	 */
	private JTable table;

	/**
	 * Builds a new GroupCreationPanel
	 */
	public GroupCreationPanel() {
		super();
		initComponents();
		initListeners();
	}

	/**
	 * Initialize the graphic components
	 */
	private void initComponents() {

		this.setLayout(new BorderLayout());
		this.setBorder(new EmptyBorder(3, 3, 3, 3));

		validateGroup = new JButton("Validate this group");
		validateGroup.setEnabled(false);
		groupName = new JTextField("Group name");

		JPanel buttonPane = new JPanel();
		buttonPane.setLayout(new BoxLayout(buttonPane, BoxLayout.LINE_AXIS));
		buttonPane.setBorder(new EmptyBorder(0, 10, 10, 10));
		buttonPane.add(Box.createHorizontalGlue());
		buttonPane.add(groupName);
		buttonPane.add(Box.createRigidArea(new Dimension(10, 0)));
		buttonPane.add(validateGroup);

		final JPanel cards = new JPanel(new CardLayout());
		cards.add(new VolSurfPanel(false), VOLUMEPANEL);
		cards.add(new VolSurfPanel(true), SURFACEPANEL);
		cards.add(new ManualPanel(), MANUALPANEL);

		JPanel comboBoxPane = new JPanel();
		String comboBoxItems[] = { VOLUMEPANEL, SURFACEPANEL, MANUALPANEL };
		criteriasBox = new JComboBox<String>(comboBoxItems);
		criteriasBox.setEditable(false);
		criteriasBox.addItemListener(new ItemListener() {

			@Override
			public void itemStateChanged(ItemEvent e) {
				CardLayout cl = (CardLayout) (cards.getLayout());
				cl.show(cards, (String) e.getItem());
			}
		});
		comboBoxPane.add(criteriasBox);

		this.add(comboBoxPane, BorderLayout.NORTH);
		this.add(cards, BorderLayout.CENTER);
		this.add(buttonPane, BorderLayout.PAGE_END);

	}

	/**
	 * Initialize the listeners for the graphic components
	 */
	private void initListeners() {

		table.getSelectionModel().addListSelectionListener(new ListSelectionListener() {

			@Override
			public void valueChanged(ListSelectionEvent e) {
				if (table.getSelectedRowCount() > 0) {
					validateGroup.setEnabled(true);
				}
			}
		});

		table.addKeyListener(new KeyListener() {

			@Override
			public void keyTyped(KeyEvent e) {
			}

			@Override
			public void keyReleased(KeyEvent e) {
				if (e.getKeyCode() == KeyEvent.VK_DELETE) {
					table.getSelectionModel().removeSelectionInterval(table.getSelectionModel().getLeadSelectionIndex(),
							table.getSelectionModel().getLeadSelectionIndex());
					if (table.getSelectedRowCount() > 0) {
						validateGroup.setEnabled(true);
					}
				}
			}

			@Override
			public void keyPressed(KeyEvent e) {
			}
		});

		validateGroup.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				CARD choice = null;
				if (((String) criteriasBox.getSelectedItem()).equals(VOLUMEPANEL)) {
					choice = CARD.volume;
				} else {
					if (((String) criteriasBox.getSelectedItem()).equals(SURFACEPANEL)) {
						choice = CARD.surface;
					} else {
						choice = CARD.manual;
					}
				}
				switch (choice) {
				case volume:
					// Treatment TODO
					System.out.println("Volume min : " + Double.valueOf(minValueVolume.getText()));
					System.out.println("Volume max : " + Double.valueOf(maxValueVolume.getText()));
					break;
				case surface:
					// Treatment TODO
					System.out.println("Surface min : " + Double.valueOf(minValueSurface.getText()));
					System.out.println("Surface max : " + Double.valueOf(maxValueSurface.getText()));
					break;
				case manual:
					// Treatment TODO
					String res = "Selected ellipsoids : \n";
					for (int i : table.getSelectedRows()) {
						res += table.getValueAt(i, 0).toString() + "\n";
					}
					System.out.println(res);
					break;
				default:
					throw new RuntimeException("You shouldn't be here");
				}
			}
		});
	}

	/**
	 * This panel represents both the Volume and the Surface card
	 * 
	 * @author bastien.kovac
	 *
	 */
	private class VolSurfPanel extends JPanel {

		/**
		 * To avoid warnings
		 */
		private static final long serialVersionUID = 1L;

		/**
		 * Minimum and maximum field
		 */
		private IntegerField minValue, maxValue;

		/**
		 * Builds a new Panel,
		 * 
		 * @param surface
		 *            True if the panel is supposed to represent the surface
		 *            card, false if it is suppose to represent the volume one
		 */
		public VolSurfPanel(boolean surface) {
			super();
			GroupLayout layout = new GroupLayout(this);
			this.setLayout(layout);

			layout.setAutoCreateGaps(true);
			layout.setAutoCreateContainerGaps(true);

			minValueLabel = new JLabel("Minimum : ");
			maxValueLabel = new JLabel("Maximum : ");

			if (!surface) {
				minValueVolume = new IntegerField();
				maxValueVolume = new IntegerField();

				minValue = minValueVolume;
				maxValue = maxValueVolume;
			} else {
				minValueSurface = new IntegerField();
				maxValueSurface = new IntegerField();

				minValue = minValueSurface;
				maxValue = maxValueSurface;
			}

			minValue.setMinimumOf(maxValue);
			maxValue.setMaximumOf(minValue);

			minValue.getDocument().addDocumentListener(new DocumentListener() {

				@Override
				public void removeUpdate(DocumentEvent e) {
					changed();
				}

				@Override
				public void insertUpdate(DocumentEvent e) {
					changed();
				}

				@Override
				public void changedUpdate(DocumentEvent e) {
					changed();
				}

			});

			maxValue.getDocument().addDocumentListener(new DocumentListener() {

				@Override
				public void removeUpdate(DocumentEvent e) {
					changed();
				}

				@Override
				public void insertUpdate(DocumentEvent e) {
					changed();
				}

				@Override
				public void changedUpdate(DocumentEvent e) {
					changed();
				}

			});

			layout.setHorizontalGroup(layout.createSequentialGroup()
					.addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING).addComponent(minValueLabel)
							.addComponent(maxValueLabel))
					.addGroup(layout.createParallelGroup(GroupLayout.Alignment.TRAILING).addComponent(minValue)
							.addComponent(maxValue)));

			layout.setVerticalGroup(layout.createSequentialGroup()
					.addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE).addComponent(minValueLabel)
							.addComponent(minValue))
					.addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE).addComponent(maxValueLabel)
							.addComponent(maxValue)));

			this.setBorder(new EmptyBorder(30, 10, 10, 10));

		}

		/**
		 * Called on every value change. Enables the validation button if both
		 * fields have a value, and if the maximum/minimum condition is
		 * respected
		 */
		public void changed() {
			if (!minValue.getText().isEmpty() && !maxValue.getText().isEmpty() && maxValue.respectsMax()) {
				validateGroup.setEnabled(true);

			} else {
				validateGroup.setEnabled(false);
			}
		}

	}

	/**
	 * This class represents the manual selection card
	 * 
	 * @author bastien.kovac
	 *
	 */
	private class ManualPanel extends JPanel {

		/**
		 * To avoid warnings
		 */
		private static final long serialVersionUID = 1L;

		/**
		 * Builds a new Panel
		 */
		public ManualPanel() {
			super();

			TableModel model = new TableModel();
			for (String s : Saving.savedEllipsoids.keySet()) {
				model.addRow(new Object[] { s, Saving.getEllipsoid(s).getVolume(), Saving.getEllipsoid(s).getSurface() });
			}

			table = new JTable(model);

			DefaultTableCellRenderer renderer = new DefaultTableCellRenderer();
			renderer.setHorizontalAlignment(JLabel.CENTER);
			renderer.setVerticalAlignment(JLabel.CENTER);

			table.getColumnModel().getColumn(0).setPreferredWidth(100);

			for (int i = 1; i < table.getColumnCount(); i++) {
				table.getColumnModel().getColumn(i).setCellRenderer(renderer);
				table.getColumnModel().getColumn(i).setPreferredWidth(50);
			}

			scrollPane = new JScrollPane(table);
			scrollPane.setPreferredSize(new Dimension(100, 200));

			this.setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
			this.add(Box.createRigidArea(new Dimension(0, 5)));
			this.add(scrollPane);
			this.setBorder(new EmptyBorder(10, 10, 10, 10));

		}

		/**
		 * The nested customized model for the display table
		 * 
		 * @author bastien.kovac
		 *
		 */
		private class TableModel extends DefaultTableModel {

			/**
			 * To avoid warnings
			 */
			private static final long serialVersionUID = 1L;

			/**
			 * Builds a new TableModel
			 */
			public TableModel() {
				super(columnNames, 0);
			}

			@SuppressWarnings("rawtypes")
			@Override
			public Class<?> getColumnClass(int columnIndex) {
				Class res = String.class;
				switch (columnIndex) {
				case 1:
					res = Double.class;
					break;
				case 2:
					res = Double.class;
					break;
				}
				return res;
			}

			@Override
			public boolean isCellEditable(int row, int column) {
				return false;
			}

		}

	}

}
