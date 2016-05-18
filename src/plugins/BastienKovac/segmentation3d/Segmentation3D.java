package plugins.BastienKovac.segmentation3d;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.JFileChooser;
import javax.swing.JSeparator;
import javax.swing.filechooser.FileSystemView;

import icy.file.xml.XMLPersistentHelper;
import icy.gui.dialog.ConfirmDialog;
import icy.gui.dialog.MessageDialog;
import icy.gui.viewer.Viewer;
import icy.main.Icy;
import icy.sequence.Sequence;
import icy.sequence.SequenceUtil;
import icy.system.thread.ThreadUtil;
import kovac.res.enums.Methods;
import kovac.res.quadric.PersistentSetQuadric;
import kovac.res.util.ViewerUtil;
import kovac.shapes.EllipsoidOverlay;
import kovac.shapes.Points;
import plugins.adufour.ezplug.EzButton;
import plugins.adufour.ezplug.EzGUI;
import plugins.adufour.ezplug.EzPlug;
import plugins.adufour.ezplug.EzVar;
import plugins.adufour.ezplug.EzVarBoolean;
import plugins.adufour.ezplug.EzVarEnum;
import plugins.adufour.ezplug.EzVarFile;
import plugins.adufour.ezplug.EzVarInteger;
import plugins.adufour.ezplug.EzVarListener;
import plugins.adufour.ezplug.EzVarSequence;

/**
 * Main class of the Plugin
 * 
 * @author bastien.kovac
 * 
 */
public class Segmentation3D extends EzPlug {

	private static Methods chosenMethod;
	private static boolean hasSetUpImage;
	private static EzGUI currentUI;

	private static EzVarSequence sequenceChoice;
	private static EzVarEnum<Methods> methodChoice;
	private static EzButton xmlSaving;
	private static EzVarFile xmlLoading;
	private static EzButton confirmSequence;
	private static EzVarBoolean lock;
	private static EzVarInteger minimumNumberOfPoints;
	private static EzVarBoolean displayPoints;

	private static ActionListener confirmListener;
	private static EzVarListener<Methods> methodListener;
	private static EzVarListener<Boolean> lockListener;
	private static EzVarListener<File> loadingListener;
	private static ActionListener savingListener;

	private static File selectedFile;
	private static PersistentSetQuadric savedEllipsoids = new PersistentSetQuadric();

	@Override
	public void execute() {
		switch (getChosenMethod()) {
		case Ellipses:
			break;
		case Points:
			Points.createEllipsoid();
			break;
		default:
			break;
		}
	}

	@Override
	public void clean() {
		ViewerUtil.clear();
		savedEllipsoids.clear();
	}

	@Override
	protected void initialize() {

		initializeListeners();
		initializeGUI();

		addEzComponent(sequenceChoice);
		addEzComponent(methodChoice);
		addEzComponent(displayPoints);
		addComponent(new JSeparator(JSeparator.HORIZONTAL));
		addEzComponent(confirmSequence);
		addComponent(new JSeparator(JSeparator.VERTICAL));
		addEzComponent(lock);
		addEzComponent(minimumNumberOfPoints);
		addComponent(new JSeparator(JSeparator.HORIZONTAL));
		addEzComponent(xmlSaving);
		addEzComponent(xmlLoading);

		lock.addVarChangeListener(lockListener);
		methodChoice.addVarChangeListener(methodListener);
		xmlLoading.addVarChangeListener(loadingListener);

		lock.setEnabled(false);
		setRunEnabled(false);

	}

	/**
	 * Initialize the graphic components of the UI
	 */
	private void initializeGUI() {
		sequenceChoice = new EzVarSequence("Sequence");
		sequenceChoice.setToolTipText("Choose the sequence to work on");
		methodChoice = new EzVarEnum<Methods>("Method", Methods.values(), Methods.Points);
		methodChoice.setToolTipText("Select the method you want to use, clicking points or drawing ellipses");
		xmlSaving = new EzButton("Save ellipsoids", savingListener);
		xmlLoading = new EzVarFile("Load ellipsoids", getInstallFolder());
		xmlLoading.setToolTipText("Load previously saved ellipsoids as a XML file");

		confirmSequence = new EzButton("Confirm sequence", confirmListener);
		lock = new EzVarBoolean("Lock", false);
		lock.setToolTipText("Look the OrthoView at the current position");
		minimumNumberOfPoints = new EzVarInteger("Min. number of points", 10, 10, 300, 5);
		minimumNumberOfPoints.setToolTipText("Minimum number of points required to calculate an ellipsoid");
		displayPoints = new EzVarBoolean("Display points", false);
		displayPoints.setToolTipText("Display the clicked points on the VTK renderer. May slow down the plugin");

		currentUI = getUI();
	}

	/**
	 * Initialize the listeners for the UI
	 */
	private void initializeListeners() {

		savingListener = new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				JFileChooser jfc = selectedFile == null ? new JFileChooser(FileSystemView.getFileSystemView())
						: new JFileChooser(selectedFile);
				if (jfc.showSaveDialog(currentUI.getContentPane()) != JFileChooser.APPROVE_OPTION)
					return;
				selectedFile = jfc.getSelectedFile();
				XMLPersistentHelper.saveToXML(savedEllipsoids, selectedFile);
			}
		};

		loadingListener = new EzVarListener<File>() {

			@Override
			public void variableChanged(EzVar<File> source, File newValue) {
				selectedFile = newValue;
				XMLPersistentHelper.loadFromXML(savedEllipsoids, selectedFile);
				if (ViewerUtil.areSet()) {
					ViewerUtil.removeOverlays();
					for (EllipsoidOverlay o : savedEllipsoids.createOverlays()) {
						ViewerUtil.addOverlayToVTK(o);
					}
				}
			}
		};

		confirmListener = new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				if (ViewerUtil.areSet()) {
					ViewerUtil.minimizeViewers();
					lock.setValue(false);
				}
				setUpViews();
				if (hasSetUpImage) {
					confirmSequence.setText("Change sequence");
					lock.setEnabled(true);
				}

			}
		};

		methodListener = new EzVarListener<Methods>() {

			@Override
			public void variableChanged(EzVar<Methods> source, Methods newValue) {
				chosenMethod = newValue;
				if (chosenMethod == Methods.Ellipses) {
					minimumNumberOfPoints.setVisible(false);
					displayPoints.setVisible(false);
				} else {
					minimumNumberOfPoints.setVisible(true);
					displayPoints.setVisible(true);
				}
			}
		};

		lockListener = new EzVarListener<Boolean>() {

			@Override
			public void variableChanged(EzVar<Boolean> source, Boolean newValue) {
				if (!ViewerUtil.areSet())
					return;
				ViewerUtil.switchView(newValue);
			}
		};
	}

	/**
	 * Sets up the views from a base Sequence. Opens one orthoViewer and one vtk
	 * 3d viewer
	 */
	private void setUpViews() {
		if (ViewerUtil.getBaseSeq() == sequenceChoice.getValue()) {
			return;
		}
		checker(sequenceChoice.getValue());
		final Sequence seq = SequenceUtil.getCopy(sequenceChoice.getValue());
		// Get base viewer
		final Viewer baseViewer = Icy.getMainInterface().getActiveViewer();
		ViewerUtil.setBaseSeq(sequenceChoice.getValue());
		if (baseViewer != null) {

			ThreadUtil.invokeLater(new Runnable() {

				@Override
				public void run() {

					ViewerUtil.setVTK(new Viewer(seq));
					ViewerUtil.goToVTK();
					ViewerUtil.getVTK().setTitle("3D VTK rendering");
					ViewerUtil.getVTK().setSize(500, 500);

					ViewerUtil.setOrth(new Viewer(seq));
					ViewerUtil.goToCustom();
					ViewerUtil.getOrth().setTitle("Orthogonal View");
					ViewerUtil.getOrth().setSize(500, 500);

					baseViewer.close();

					ViewerUtil.linkViewers();
					hasSetUpImage = true;

				}
			});
		}
	}

	/**
	 * This method checks if the given sequence is appropriate for the plugin,
	 * it must : - Have a Z (or T) size superior to 1 - If T > 1 but Z = 1, the
	 * user is asked if he agrees to convert the sequence to stack, if not, an
	 * exception is thrown
	 * 
	 * @param baseSeq
	 *            The sequence given as argument
	 * @throws IllegalArgumentException
	 *             The exception thrown in case of wrong dimensions
	 */
	private void checker(Sequence baseSeq) throws IllegalArgumentException {
		if (baseSeq == null) {
			MessageDialog.showDialog("This plugin needs an image to work");
			return;
		}
		// Check for a XYZ image
		if (baseSeq.getSizeZ() < 2) {
			if (baseSeq.getSizeT() > 2) {
				if (!ConfirmDialog.confirm(
						"This plugin requires a stacked image to work, would you like to convert your image to stack ?")) {
					return;
				}
				SequenceUtil.convertToStack(baseSeq);
			}
		}
	}

	/**
	 * Enables or disables the run button of the plugin's UI
	 * 
	 * @param flag
	 *            True to enable the button, false to disable it
	 */
	public static void setRunEnabled(boolean flag) {
		currentUI.setRunButtonEnabled(flag);
	}

	/**
	 * @return The method chosen by the user
	 */
	public static Methods getChosenMethod() {
		return chosenMethod;
	}

	/**
	 * @return The minimum number of points the user defined
	 */
	public static int getMinNumbPoints() {
		return minimumNumberOfPoints.getValue();
	}

	/**
	 * Change the text of the confirmSequence EzButton
	 * 
	 * @param text
	 *            New text
	 */
	public static void setTextSequence(String text) {
		confirmSequence.setText(text);
	}

	/**
	 * Lock or unlock the checkBox used to switch between Custom and Locked
	 * OrthoCanvas
	 * 
	 * @param flag
	 *            True to go to locked, false to go to custom
	 */
	public static void setLocked(boolean flag) {
		lock.setValue(flag);
	}

	/**
	 * @return True if we have to display the points with the ellipse in VTK,
	 *         false if not
	 */
	public static Boolean isDiplayingPoints() {
		return displayPoints.getValue();
	}
	
	public static void addSavedEllipsoid(EllipsoidOverlay e) {
		savedEllipsoids.addQuadric(e.getQuadricExpression());
	}

}
