package kovac.res.gui;

import icy.gui.frame.IcyFrameEvent;
import icy.gui.frame.IcyFrameListener;
import icy.gui.viewer.Viewer;
import plugins.BastienKovac.segmentation3d.Segmentation3D;

/**
 * This class implements IcyFrameListener and is used to link two viewers
 * together. Closing one will cause the other to close as well, and will reset
 * the plugin's UI (by changing the confirm sequence button's text and setting
 * the lock to false)
 * 
 * @author bastien.kovac
 *
 */
public class LinkListener implements IcyFrameListener {

	private Viewer viewerLinked;

	/**
	 * Builds a new LinkListener, with a given viewer which will be linked to
	 * the component to which this listener is added
	 * 
	 * @param v
	 *            The Viewer to link
	 */
	public LinkListener(Viewer v) {
		super();
		viewerLinked = v;
	}

	@Override
	public void icyFrameOpened(IcyFrameEvent e) {
	}

	@Override
	public void icyFrameClosing(IcyFrameEvent e) {
	}

	@Override
	public void icyFrameClosed(IcyFrameEvent e) {
		if (viewerLinked == null)
			return;
		viewerLinked.close();
		Segmentation3D.setLocked(false);
		Segmentation3D.setTextSequence("Confirm sequence");
	}

	@Override
	public void icyFrameIconified(IcyFrameEvent e) {
	}

	@Override
	public void icyFrameDeiconified(IcyFrameEvent e) {
	}

	@Override
	public void icyFrameActivated(IcyFrameEvent e) {
	}

	@Override
	public void icyFrameDeactivated(IcyFrameEvent e) {
	}

	@Override
	public void icyFrameInternalized(IcyFrameEvent e) {
	}

	@Override
	public void icyFrameExternalized(IcyFrameEvent e) {
	}

}
