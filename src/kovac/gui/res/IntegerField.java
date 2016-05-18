package kovac.gui.res;

import javax.swing.JTextField;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.PlainDocument;

/**
 * This class is a customized IntegerField which only accepts numbers, and
 * customizes their appearance. It also can be used to link two instances of
 * this object, one as the minimum and the other as the maximum, to ease
 * verifications
 * 
 * @author bastien.kovac
 *
 */
public class IntegerField extends JTextField {

	/**
	 * To avoid warnings
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * True if the field is minimum of another one, false if not
	 */
	private boolean isMin;

	/**
	 * The field which is the maximum of this one
	 */
	private IntegerField maximum;
	/**
	 * The field which is the maximum of this one
	 */
	private IntegerField minimum;

	/**
	 * Creates a new IntegerField, which isn't minimum of any other one
	 */
	public IntegerField() {
		super();
		this.isMin = false;
	}

	/**
	 * Sets this field as minimum of another one
	 * 
	 * @param max
	 *            The field which will be the maximum one
	 */
	public void setMinimumOf(IntegerField max) {
		this.isMin = true;
		this.maximum = max;
	}

	/**
	 * Sets this field as maximum of another one
	 * 
	 * @param min
	 *            The field which will be the minimum one
	 */
	public void setMaximumOf(IntegerField min) {
		this.minimum = min;
	}

	/**
	 * Checks if this field respects being the maximum (Meaning its value must
	 * be equal or superior the minimum one). Throws an exception if this field
	 * isn't maximum of any other field
	 * 
	 * @return True if the condition is checked, false if not
	 */
	public boolean respectsMax() {
		if (minimum == null) {
			throw new RuntimeException("Minimum not initialized");
		}
		return (Double.valueOf(getText()) >= Double.valueOf(minimum.getText()));
	}

	@Override
	protected Document createDefaultModel() {
		return new UpperCaseDocument();
	}

	/**
	 * The DocumentClass to handle only number inputs
	 * 
	 * @author bastien.kovac
	 *
	 */
	private class UpperCaseDocument extends PlainDocument {

		/**
		 * To avoid warnings
		 */
		private static final long serialVersionUID = 1L;

		@Override
		public void insertString(int offs, String str, AttributeSet a) throws BadLocationException {

			if (str == null) {
				return;
			}

			char[] chars = str.toCharArray();
			boolean ok = true;
			boolean comma = (IntegerField.this.getText().contains("."));

			for (int i = 0; i < chars.length; i++) {

				try {
					if (comma || chars[i] != ',' && chars[i] != '.') {
						Integer.parseInt(String.valueOf(chars[i]));
					}
					if (!comma) {
						if (chars[i] == ',') {
							chars[i] = '.';
							comma = true;
						}
						if (chars[i] == '.') {
							comma = true;
						}
					}
				} catch (NumberFormatException exc) {
					ok = false;
					break;
				}
			}

			if (ok) {
				super.insertString(offs, new String(chars), a);
				if (isMin) {
					maximum.setText(IntegerField.this.getText());
				}
			}

		}
	}

}
