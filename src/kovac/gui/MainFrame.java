package kovac.gui;

import java.awt.BorderLayout;
import java.awt.GridLayout;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import kovac.gui.panels.EllipsoidPanel;
import kovac.gui.panels.GroupCreationPanel;
import kovac.gui.panels.TopEllipsoidPanel;

public class MainFrame extends JFrame {

	/**
	 * 
	 */
	private static final long serialVersionUID = -8589417946527605841L;

	/**
	 * Top ellipsoid panel
	 */
	private  static TopEllipsoidPanel topEll;
	
	public MainFrame() {
		super("Segmentation 3D");		
		
		this.setLayout(new GridLayout(1, 1, 3, 3));

		JTabbedPane tabbedPane = new JTabbedPane();

		JPanel stats = new JPanel();
		stats.setLayout(new BorderLayout(5, 5));
		topEll = new TopEllipsoidPanel();
		stats.add(topEll, BorderLayout.NORTH);
		stats.add(new EllipsoidPanel(), BorderLayout.SOUTH);
		tabbedPane.add("Statistics", stats);
		
		JPanel groups = new GroupCreationPanel();
		tabbedPane.add("Group creation", groups);
		
		tabbedPane.addChangeListener(new ChangeListener() {
			
			@Override
			public void stateChanged(ChangeEvent arg0) {
				topEll.reloadStringValues();
			}
		});
		this.add(tabbedPane);
		this.setResizable(false);
		this.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		this.setLocationRelativeTo(null);
		this.pack();
	}

	public static void open() {
		JFrame fen = new MainFrame();
		fen.setVisible(true);
	}
	
	public static void reload() {
		if (topEll != null)
			topEll.reloadStringValues();
	}

}
