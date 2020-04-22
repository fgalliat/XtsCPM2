// package app;

// import gui.XComponent;
// import hardware.input.InputKey;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.Cursor;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.ClipboardOwner;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.event.FocusEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.FocusListener;
import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.LinkedList;
import java.util.List;

import javax.swing.JLabel;
import javax.swing.JViewport;
import javax.swing.Scrollable;

public /*abstract*/ class BufferedJavaPane extends /*XComponent*/ JLabel implements Scrollable, ClipboardOwner, FocusListener, KeyListener {

	public void paint(Graphics g) {

		g.setColor( Color.darkGray );
		// g.clearRect(0, 0, innerBounds.width, innerBounds.height);
		g.fillRect(0, 0, innerBounds.width, innerBounds.height);

		drawIt(g);
	}

	// ------------- moa -------------
	Rectangle innerBounds = null;

	Color carretColor = Color.orange;

	Color bgColor = null; // new Color(60, 255, 60, 100);
	private final Color lineNrBg = new Color(100, 255, 100, 150);
	private final Color textColor = Color.green;

	// public Rectangle getVisibleRect() {
	// return ref;
	// }

	@Override
	public Dimension getSize() {
		// Rectangle ref = getBounds();
		Rectangle ref = innerBounds;

// System.out.println( ref.width+"x"+ref.height );

		return new Dimension(ref.width, ref.height);
	}

	// public void setMinimumSize(Dimension d) {
	// }

	// public void setPreferredSize(Dimension d) {
	// }

	// @Override
	// public Dimension getPreferredSize() {
	// 	return getSize();
	// }



	public void revalidate() {
	}

	// public abstract void repaint();

	Font currentFont = null;

	// public Font getFont() {
	// 	if (currentFont == null) {
	// 		currentFont = new Font("Lucida Console", Font.BOLD, 10);
	// 	}
	// 	return currentFont;
	// }

	public void setFont(Font ft) {
		// currentFont = ft;
	}

	// public boolean hasFocus() {
	// 	return true;
	// }

	// public void requestFocus() {
	// }

	// public Component getParent() {
	// 	return null;
	// }

	// -------------------------------------------------------

	private String text;
	private FontMetrics metrics;
	private int caretPosition;
	private int selectionStart;
	private int selectionEnd;
	private int pos1;
	private int pos2;
	private int lineToMark;
	private final boolean showCaret;
	private final Color ignoreColor;
	private final Color stringColor;

	private final Color markColor;
	private final Color clampColor;

	private final Color lineNr;
	private int mousePressPos;
	private final List<InnerTextAction> doneActions;
	private final List<InnerTextAction> redoneActions;
	protected final Rectangle car;
	private String blue[][];
	private int charWidth[];
	boolean changed;
	private final char dividerWord[] = { '\n', '.', ' ', ',', '(', ')', '{', '}', '[', ']', '/', '-', '+', '*', '<', '>', '=', '\n', ';', '"', '\'', '&', '|', '!' };

	class InnerTextAction {

		public static final int REMOVE = 0;
		public static final int INSERT = 1;
		int action;
		int position;
		int length;
		String string;

		InnerTextAction(int i, int j, int k, String s) {
			action = i;
			position = j;
			length = k;
			string = s;
		}
	}

	public BufferedJavaPane(Rectangle rect) {
		//super(rect);
		super("");
		setBounds(rect);
		this.innerBounds = rect;
		setPreferredSize(new Dimension(rect.width, rect.height));
		setOpaque(true);
		requestFocus();
		System.out.println("HERE!");

		text = "";
		caretPosition = 0;
		selectionStart = 0;
		selectionEnd = 0;
		pos1 = -1;
		pos2 = -1;
		lineToMark = -1;
		showCaret = true;
		ignoreColor = new Color(0, 150, 0);
		stringColor = Color.red;
		markColor = Color.blue;
		clampColor = new Color(0, 120, 120);
		lineNr = new Color(200, 0, 0);
		mousePressPos = 0;
		doneActions = new LinkedList<InnerTextAction>();
		redoneActions = new LinkedList<InnerTextAction>();
		car = new Rectangle(0, 0, 2, 0);
		changed = false;
		setFont(new Font("MonoSpaced", 0, 12));

		// setCursor(new Cursor(2));
		addKeyListener(this);
		// addMouseMotionListener(this);
		// addFocusListener(this);
		// setFocusTraversalKeysEnabled(false);
		setBackground(Color.darkGray);

		readBlue();
	}

	public BufferedJavaPane(Rectangle bounds, String s) {
		this(bounds);
		text = s;
	}

	public BufferedJavaPane(Rectangle bounds, File file) {
		this(bounds);
		readFromFile(file);
	}

	public String getText() {
		return text;
	}

	public void keyPressed(KeyEvent keyevent) {
	// @Override
	// public void keyPressed(InputKey key) {
		// int kCode = key.code;

		int kCode = keyevent.getKeyCode();

		// TODO
		int kModifier = 0;

		// event.consume evite de passer dans keyTyped

		if ((keyevent.getModifiers() & 2) > 0) {
		// if (key.CTRL_DOWN) {
			if (kCode == 67)
				copy();
			else if (kCode == 86)
				paste();
			else if (kCode == 88)
				cut();
			else if (kCode == 90)
				undo();
			else if (kCode == 89)
				redo();
		} else if ((kModifier & 8) <= 0 && (kModifier & 0x20) <= 0)
			if ((keyevent.getModifiers() & 1) > 0) {
			// if (key.SHIFT_DOWN) {
				if (kCode == 37) {
					if (caretPosition > 0) {
						if (caretPosition == selectionStart)
							selectionStart--;
						else
							selectionEnd--;
						caretPosition--;
						repaint();
						positionVisible();
					}
					// key.consume();
				} else if (kCode == 39) {
					if (caretPosition < text.length()) {
						if (caretPosition == selectionEnd)
							selectionEnd++;
						else
							selectionStart++;
						caretPosition++;
						repaint();
						positionVisible();
					}
					// key.consume();
				} else if (kCode == 38) {
					int i = getPosInLine(caretPosition);
					int k2 = text.lastIndexOf('\n', caretPosition - i - 2) + 1;
					int j4;
					for (j4 = 0; j4 < i && j4 + k2 < text.length() && text.charAt(j4 + k2) != '\n'; j4++)
						;
					int i6 = caretPosition;
					caretPosition = k2 + j4;
					if (i6 == selectionEnd && caretPosition >= selectionStart)
						selectionEnd = caretPosition;
					else if (i6 == selectionEnd) {
						selectionEnd = selectionStart;
						selectionStart = caretPosition;
					} else {
						selectionStart = caretPosition;
					}
					repaint();
					positionVisible();
					// key.consume();
				} else if (kCode == 40) {
					int j = getPosInLine(caretPosition);
					int l2 = text.indexOf('\n', caretPosition);
					if (l2 != -1) {
						l2++;
						int k4;
						for (k4 = 0; k4 < j && k4 + l2 < text.length() && text.charAt(k4 + l2) != '\n'; k4++)
							;
						int j6 = caretPosition;
						caretPosition = l2 + k4;
						if (j6 == selectionStart && caretPosition <= selectionEnd)
							selectionStart = caretPosition;
						else if (j6 == selectionStart) {
							selectionStart = selectionEnd;
							selectionEnd = caretPosition;
						} else {
							selectionEnd = caretPosition;
						}
						repaint();
						positionVisible();
					}
					// key.consume();
				} else if (kCode == 36) {
					int k = caretPosition;
					if (getPosInLine(caretPosition) <= getWhiteAtLineStart(caretPosition))
						caretPosition -= getPosInLine(caretPosition);
					else
						caretPosition -= getPosInLine(caretPosition) - getWhiteAtLineStart(caretPosition);
					if (k == selectionEnd && caretPosition >= selectionStart)
						selectionEnd = caretPosition;
					else if (k == selectionEnd) {
						selectionEnd = selectionStart;
						selectionStart = caretPosition;
					} else {
						selectionStart = caretPosition;
					}
					repaint();
					positionVisible();
					// key.consume();
				} else if (kCode == 35) {
					int l = caretPosition;
					if (getWhiteAtLineStart(caretPosition) <= getPosInLine(caretPosition))
						caretPosition += getLineWidth(caretPosition) - getPosInLine(caretPosition);
					else
						caretPosition += getWhiteAtLineStart(caretPosition) - getPosInLine(caretPosition);
					if (l == selectionStart && caretPosition <= selectionEnd)
						selectionStart = caretPosition;
					else if (l == selectionStart) {
						selectionStart = selectionEnd;
						selectionEnd = caretPosition;
					} else {
						selectionEnd = caretPosition;
					}
					repaint();
					positionVisible();
					// key.consume();
				} else if (kCode == 33) {
					int i1 = caretPosition;
					if (caretPosition - 1000 > 0)
						caretPosition -= 1000;
					else
						caretPosition = 0;
					if (i1 == selectionEnd && caretPosition >= selectionStart)
						selectionEnd = caretPosition;
					else if (i1 == selectionEnd) {
						selectionEnd = selectionStart;
						selectionStart = caretPosition;
					} else {
						selectionStart = caretPosition;
					}
					repaint();
					positionVisible();
					// key.consume();
				} else if (kCode == 34) {
					// int j1 = getPosInLine(caretPosition);
					int i3 = text.indexOf('\n', caretPosition);
					if (i3 != -1) {
						int l4 = caretPosition;
						if (caretPosition < text.length() - 1000)
							caretPosition += 1000;
						else
							caretPosition = text.length();
						if (l4 == selectionStart && caretPosition <= selectionEnd)
							selectionStart = caretPosition;
						else if (l4 == selectionStart) {
							selectionStart = selectionEnd;
							selectionEnd = caretPosition;
						} else {
							selectionEnd = caretPosition;
						}
						repaint();
						positionVisible();
					}
					// key.consume();
				} else if (kCode == 9) {
					if (selectionStart != selectionEnd) {
						int k1 = selectionStart;
						int j3 = selectionEnd;
						for (int i5 = j3; i5 >= k1 - getPosInLine(k1) && i5 >= 0; i5--)
							if (i5 == 0 || text.charAt(i5 - 1) == '\n') {
								for (int k6 = 0; k6 < 4 && text.charAt(i5) == ' '; k6++) {
									remove(i5, 1);
									j3--;
								}

							}

						selectionStart = k1;
						selectionEnd = j3;
					}
					// key.consume();
				}
			} else if (kCode == 37) {
				if (caretPosition > 0) {
					caretPosition--;
					selectionStart = caretPosition;
					selectionEnd = caretPosition;
					repaint();
					positionVisible();
				}
				// key.consume();
			} else if (kCode == 39) {
				if (caretPosition < text.length()) {
					caretPosition++;
					selectionStart = caretPosition;
					selectionEnd = caretPosition;
					repaint();
					positionVisible();
				}
				// key.consume();
			} else if (kCode == 38) {
				int l1 = getPosInLine(caretPosition);
				int k3 = text.lastIndexOf('\n', caretPosition - l1 - 2) + 1;
				int j5;
				for (j5 = 0; j5 < l1 && j5 + k3 < text.length() && text.charAt(j5 + k3) != '\n'; j5++)
					;
				caretPosition = k3 + j5;
				selectionStart = caretPosition;
				selectionEnd = caretPosition;
				repaint();
				positionVisible();
				// key.consume();
			} else if (kCode == 40) {
				int i2 = getPosInLine(caretPosition);
				int l3 = text.indexOf('\n', caretPosition);
				if (l3 != -1) {
					l3++;
					int k5;
					for (k5 = 0; k5 < i2 && k5 + l3 < text.length() && text.charAt(k5 + l3) != '\n'; k5++)
						;
					caretPosition = l3 + k5;
					selectionStart = caretPosition;
					selectionEnd = caretPosition;
					repaint();
					positionVisible();
				}
				// key.consume();
			} else if (kCode == 9) {
				if (selectionStart == selectionEnd) {
					insertTab(caretPosition);
				} else {
					int j2 = selectionStart;
					int i4 = selectionEnd;
					for (int l5 = i4; l5 >= j2 - getPosInLine(j2) && l5 >= 0; l5--)
						if (l5 == 0 || text.charAt(l5 - 1) == '\n') {
							i4 += 4;
							insertTab(l5);
						}

					selectionStart = j2;
					selectionEnd = i4;
				}
				// key.consume();
			} else if (kCode == 127) {
				if (selectionStart == selectionEnd) {
					if (caretPosition < text.length() - 1)
						remove(caretPosition, 1);
				} else {
					remove(selectionStart, selectionEnd - selectionStart);
				}
				selectionStart = caretPosition;
				selectionEnd = caretPosition;
				repaint();
				positionVisible();
				// key.consume();
			} else if (kCode == 36) {
				if (getWhiteAtLineStart(caretPosition) >= getPosInLine(caretPosition))
					caretPosition -= getPosInLine(caretPosition);
				else
					caretPosition -= getPosInLine(caretPosition) - getWhiteAtLineStart(caretPosition);
				selectionStart = caretPosition;
				selectionEnd = caretPosition;
				repaint();
				positionVisible();
				// key.consume();
			} else if (kCode == 35) {
				if (getWhiteAtLineStart(caretPosition) <= getPosInLine(caretPosition))
					caretPosition += getLineWidth(caretPosition) - getPosInLine(caretPosition);
				else
					caretPosition += getWhiteAtLineStart(caretPosition) - getPosInLine(caretPosition);
				selectionStart = caretPosition;
				selectionEnd = caretPosition;
				repaint();
				positionVisible();
				// key.consume();
			} else if (kCode == 33) {
				if (caretPosition > 1000)
					caretPosition -= 1000;
				else
					caretPosition = 0;
				selectionStart = caretPosition;
				selectionEnd = caretPosition;
				repaint();
				positionVisible();
				// key.consume();
			} else if (kCode == 34) {
				if (caretPosition < text.length() - 1000)
					caretPosition += 1000;
				else
					caretPosition = text.length();
				selectionStart = caretPosition;
				selectionEnd = caretPosition;
				repaint();
				positionVisible();
				// key.consume();
			}
	}

	public void keyTyped(KeyEvent keyevent) {
	// @Override
	// public void keyType(InputKey key) {
		// int kModifier = 0;
		// char keyChar = key.ch;
		char keyChar = keyevent.getKeyChar();

		if ((keyevent.getModifiers() & 2) <= 0
		// if (!key.CTRL_DOWN
		// //&& (kModifier & 8) <= 0
		) {
			if (keyChar == '\t') {
				// key.consume();
			} else if (keyChar == '\177') {
				// key.consume();
			} else if (keyChar == '\b') {
				if (selectionStart == selectionEnd && caretPosition > 0) {
					remove(caretPosition - 1, 1);
					selectionStart = caretPosition;
					selectionEnd = caretPosition;
					repaint();
				} else {
					remove(selectionStart, selectionEnd - selectionStart);
					selectionStart = caretPosition;
					selectionEnd = caretPosition;

					repaintFull();

				}
				// key.consume();
			} else if (keyChar == '\n') {
				if (selectionStart != selectionEnd)
					remove(selectionStart, selectionEnd - selectionStart);
				int i = getWhiteAtLineStart(selectionStart);
				if (selectionStart > 0 && text.charAt(selectionStart - 1) == '{')
					i += 4;
				String s = "\n";
				for (int i1 = 0; i1 < i; i1++)
					s = s + " ";

				insertString(caretPosition, s);
			} else if (keyChar == '{') {
				if (selectionStart != selectionEnd)
					remove(selectionStart, selectionEnd - selectionStart);
				int j = getWhiteAtLineStart(selectionStart);
				String s1 = "";
				if (getLineWidth(caretPosition) == j) {
					s1 = s1 + '{';
				} else {
					s1 = s1 + '\n';
					for (int j1 = 0; j1 < j; j1++)
						s1 = s1 + " ";

					s1 = s1 + "{";
				}
				insertString(caretPosition, s1);
			} else if (keyChar == '}') {
				if (selectionStart != selectionEnd)
					remove(selectionStart, selectionEnd - selectionStart);
				int k = getWhiteAtLineStart(caretPosition);
				// int l = getLineWidth(caretPosition);
				int k1 = getPosInLine(caretPosition);
				int l1 = 0;
				int i2 = 1;
				int j2 = getCaretPosition() - 1;
				if (j2 > 0)
					do {
						if (text.charAt(j2) == '}')
							i2++;
						else if (text.charAt(j2) == '{')
							i2--;
						if (i2 != 0)
							j2--;
					} while (j2 > -1 && i2 > 0);
				for (; j2 > 0 && text.charAt(j2 - 1) != '\n'; j2--)
					l1++;

				if (k == k1 && k > l1) {
					remove((caretPosition - k1) + l1, k - l1);
					insertChar('}');
				} else if (k == k1) {
					insertChar('}');
				} else {
					insertChar('\n');
					for (int k2 = 0; k2 < l1; k2++)
						insertChar(' ');

					insertChar('}');
				}
			} else {
				if (selectionStart != selectionEnd)
					remove(selectionStart, selectionEnd - selectionStart);
				insertChar(keyChar);
			}
		}
	}

	private void repaintFull() {
		offScreen.clearRect(0, 0, getBounds().width, getBounds().height);
		drawIt(offScreen);
	}

	private void insertTab(int i) {
		insertString(i, "    ");
	}

	public void keyReleased(KeyEvent keyevent) {
	// @Override
	// public void keyRelease(InputKey key) {
		testOposing();
	}

	private int getPosInLine(int i) {
		int j = text.lastIndexOf('\n', i - 1);
		j++;
		j = i - j;
		return j;
	}

	private int getLineWidth(int i) {
		int j = 0;
		int k = text.indexOf('\n', i);
		int l = 0;
		if (i > 0)
			l = text.lastIndexOf('\n', i - 1) + 1;
		if (k == -1)
			k = text.length();
		j = k - l;
		return j;
	}

	private int getWhiteAtLineStart(int i) {
		int j = 0;
		for (int k = i - getPosInLine(i); k >= 0 && k < text.length() && text.charAt(k) == ' '; k++)
			j++;

		return j;
	}

	private void insertChar(char c) {
		changed = true;
		doneActions.add(0, new InnerTextAction(1, caretPosition, 1, ""));
		text = text.substring(0, caretPosition) + c + text.substring(caretPosition, text.length());
		caretPosition++;
		selectionStart = caretPosition;
		selectionEnd = caretPosition;
		repaint();
		positionVisible();
	}

	public void insertString(int i, String s) {
		changed = true;
		doneActions.add(0, new InnerTextAction(1, i, s.length(), ""));
		text = text.substring(0, i) + s + text.substring(i, text.length());
		if (caretPosition >= i) {
			caretPosition += s.length();
			selectionStart = caretPosition;
			selectionEnd = caretPosition;
		}
		repaint();
		positionVisible();
	}

	public void remove(int i, int j) {
		changed = true;
		if (i >= 0 && i + j <= text.length()) {
			doneActions.add(0, new InnerTextAction(0, i, j, text.substring(i, i + j)));
			text = text.substring(0, i) + text.substring(i + j, text.length());
			if (caretPosition > i && caretPosition <= i + j)
				caretPosition = i;
			else if (caretPosition > i)
				caretPosition -= caretPosition - i;
			repaint();
			positionVisible();
		}
	}

	public void undo() {
		if (doneActions.size() > 0) {
			changed = true;
			InnerTextAction textaction = doneActions.remove(0);
			try {
				if (textaction.action == 1) {
					redoneActions.add(0, new InnerTextAction(0, textaction.position, textaction.length, text.substring(textaction.position, textaction.position + textaction.length)));
					text = text.substring(0, textaction.position) + text.substring(textaction.position + textaction.length, text.length());
				} else {
					redoneActions.add(0, new InnerTextAction(1, textaction.position, textaction.length, ""));
					text = text.substring(0, textaction.position) + textaction.string + text.substring(textaction.position, text.length());
				}
			} catch (Exception exception) {
			}
			repaint();
		}
	}

	public void redo() {
		if (redoneActions.size() > 0) {
			changed = true;
			InnerTextAction textaction = redoneActions.remove(0);
			try {
				if (textaction.action == 1) {
					doneActions.add(0, new InnerTextAction(0, textaction.position, textaction.length, text.substring(textaction.position, textaction.position + textaction.length)));
					text = text.substring(0, textaction.position) + text.substring(textaction.position + textaction.length, text.length());
				} else {
					doneActions.add(0, new InnerTextAction(1, textaction.position, textaction.length, ""));
					text = text.substring(0, textaction.position) + textaction.string + text.substring(textaction.position, text.length());
				}
			} catch (Exception exception) {
			}
			repaint();
		}
	}

	protected int getLine(int i) {
		int j = 0;
		int k = 0;
		for (boolean flag = false; k != -1 && !flag;) {
			k = text.indexOf('\n', k);
			if (i <= k)
				flag = true;
		}

		System.out.println("Line: " + j);
		return j;
	}

	public void cut() {
		if (selectionStart != selectionEnd)
			try {
				Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
				String s = text.substring(selectionStart, selectionEnd);
				clipboard.setContents(new StringSelection(s), this);
				text = text.substring(0, selectionStart) + text.substring(selectionEnd, text.length());
				selectionEnd = selectionStart;
				caretPosition = selectionStart;
				repaint();
				positionVisible();
				repaintFull();
			} catch (Exception exception) {
			}
	}

	public void copy() {
		if (selectionStart != selectionEnd)
			try {
				Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
				String s = text.substring(selectionStart, selectionEnd);
				clipboard.setContents(new StringSelection(s), this);
			} catch (Exception exception) {
			}
	}

	public void paste() {
		if (selectionStart != selectionEnd) {
			text = text.substring(0, selectionStart) + text.substring(selectionEnd, text.length());
			selectionEnd = selectionStart;
			caretPosition = selectionStart;
		}
		Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
		Transferable transferable = clipboard.getContents(this);
		if (transferable != null)
			try {
				insertString(caretPosition, (String) transferable.getTransferData(DataFlavor.stringFlavor));
			} catch (Exception exception) {
			}
	}

	public int countChar(char c, int i, int j) {
		int k = 0;
		for (int l = i; l < i + j; l++)
			if (text.charAt(l) == c)
				k++;

		return c;
	}

	protected BufferedImage dblBuff = null;
	protected Graphics offScreen = null;
	protected BufferedImage dblBuff2 = null;
	protected Graphics offScreen2 = null;
	protected boolean upperBuffer = false;

	// @Override
	public void draw(Graphics g, int x, int y) {
		if (dblBuff == null) {
			dblBuff = new BufferedImage(getBounds().width, getBounds().height, BufferedImage.TYPE_INT_ARGB);
			offScreen = dblBuff.createGraphics();
			dblBuff2 = new BufferedImage(getBounds().width, getBounds().height, BufferedImage.TYPE_INT_ARGB);
			offScreen2 = dblBuff2.createGraphics();
			// to clip component
			// to render in masked time
			drawIt(offScreen);
			offScreen.setColor(Color.green);
			offScreen.drawRect(0, 0, getBounds().width - 1, getBounds().height - 1);
			drawIt(offScreen2);
			offScreen2.setColor(Color.green);
			offScreen2.drawRect(0, 0, getBounds().width - 1, getBounds().height - 1);
		}

		// g.drawImage(upperBuffer ? dblBuff : dblBuff2, getBounds().x + x,
		// getBounds().y + y, null);
		g.drawImage(dblBuff, getBounds().x + x, getBounds().y + y, null);
		upperBuffer = !upperBuffer;
	}

	public void drawIt(Graphics g) {
		g.setFont(getFont());
		metrics = g.getFontMetrics();
		if (charWidth == null) {
			charWidth = new int[256];
			for (int i = 0; i < 256; i++) {
				char c = (char) i;
				if (Character.isDefined(c))
					charWidth[i] = metrics.charWidth(c);
				else
					charWidth[i] = 0;
			}

			charWidth[9] = charWidth[32] * 4;
		}

		// gray margin
		Rectangle rectangle = new Rectangle(0, 0, getBounds().width, getBounds().height);
		if (bgColor != null) {
			g.setColor(bgColor);
			g.fillRect(rectangle.x + 65, rectangle.y, rectangle.width - 65, rectangle.height);
		}
		g.setColor(new Color(180, 180, 180));
		g.fillRect(rectangle.x, rectangle.y, 65, rectangle.height);



		int j = 1;
		int k = 0;
		// boolean flag = false;
		boolean flag2 = false;
		StringBuffer stringbuffer = new StringBuffer();
		byte byte0 = 65;
		int l = byte0;
		int i1 = l;
		int j1;
		for (j1 = metrics.getHeight(); j1 + metrics.getHeight() < rectangle.y;) {
			j1 += metrics.getHeight();
			if (k + 1 >= text.length())
				break;
			k = text.indexOf("\n", k);
			k++;
			j++;
		}

		if (text.lastIndexOf("/*", k) > text.lastIndexOf("*/", k)) {
			int k1 = text.lastIndexOf("/*", k);
			int i2 = getPosInLine(k1);
			int k2 = countChar('"', k1 - i2, i2);
			if (k2 / 2 == k2 / 2)
				flag2 = true;
			else
				flag2 = false;
		}
		for (; k != text.length(); stringbuffer.delete(0, stringbuffer.length())) {
			boolean flag1 = false;
			if (flag2) {
				stringbuffer.append(text.charAt(k));
				for (k++; k < text.length() && !flag1; k++) {
					if (text.charAt(k) == '/' && text.charAt(k - 1) == '*')
						flag1 = true;
					stringbuffer.append(text.charAt(k));
				}

			} else if (stringbuffer.length() == 0 && text.charAt(k) == '"') {
				stringbuffer.append(text.charAt(k));
				for (k++; k < text.length() && !flag1; k++) {
					if (text.charAt(k) == '"') {
						if (k > 0) {
							if (text.charAt(k - 1) == '\\') {
								if (k > 1) {
									if (text.charAt(k - 2) == '\\')
										flag1 = true;
								} else {
									flag1 = true;
								}
							} else {
								flag1 = true;
							}
						} else {
							flag1 = true;
						}
					} else if (text.charAt(k) == '\n')
						flag1 = true;
					stringbuffer.append(text.charAt(k));
				}

			} else if (stringbuffer.length() == 0 && text.charAt(k) == '\'') {
				stringbuffer.append(text.charAt(k));
				for (k++; k < text.length() && !flag1; k++) {
					if (text.charAt(k) == '\'') {
						if (k > 0) {
							if (text.charAt(k - 1) == '\\') {
								if (k > 1) {
									if (text.charAt(k - 2) == '\\')
										flag1 = true;
								} else {
									flag1 = true;
								}
							} else {
								flag1 = true;
							}
						} else {
							flag1 = true;
						}
					} else if (text.charAt(k) == '\n')
						flag1 = true;
					stringbuffer.append(text.charAt(k));
				}

			} else if (text.charAt(k) == '/' && k < text.length() - 1 && text.charAt(k + 1) == '/' && (k == 0 || text.charAt(k - 1) != '*')) {
				stringbuffer.append(text.charAt(k));
				for (k++; k < text.length() && !flag1; k++) {
					if (text.charAt(k) == '\n')
						flag1 = true;
					stringbuffer.append(text.charAt(k));
				}

			} else if (text.charAt(k) == '/' && k < text.length() - 1 && text.charAt(k + 1) == '*' && (k == 0 || text.charAt(k - 1) != '/')) {
				stringbuffer.append(text.charAt(k));
				for (k++; k < text.length() && !flag1; k++) {
					if (text.charAt(k) == '/' && text.charAt(k - 1) == '*')
						flag1 = true;
					stringbuffer.append(text.charAt(k));
				}

			} else if (divider(text.charAt(k))) {
				if (stringbuffer.length() == 0) {
					stringbuffer.append(text.charAt(k));
					k++;
				}
				flag1 = true;
			} else {
				while (k < text.length() && !flag1)
					if (divider(text.charAt(k))) {
						flag1 = true;
					} else {
						stringbuffer.append(text.charAt(k));
						k++;
					}
			}
			if (flag2) {
				g.setColor(ignoreColor);
				flag2 = false;
			} else if (stringbuffer.length() > 1 && stringbuffer.charAt(0) == '/' && stringbuffer.charAt(1) == '*')
				g.setColor(ignoreColor);
			else if (stringbuffer.length() > 1 && stringbuffer.charAt(0) == '/' && stringbuffer.charAt(1) == '/')
				g.setColor(ignoreColor);
			else if (stringbuffer.length() > 0 && stringbuffer.charAt(0) == '"')
				g.setColor(stringColor);
			else if (stringbuffer.length() > 0 && stringbuffer.charAt(0) == '\'')
				g.setColor(stringColor);
			else if (isBlue(stringbuffer.toString()))
				g.setColor(markColor);
			else if (stringbuffer.toString().equals("{") || stringbuffer.toString().equals("}"))
				g.setColor(clampColor);
			else
				g.setColor(textColor);
			for (int l1 = 0; l1 < stringbuffer.length(); l1++) {
				char c1 = stringbuffer.charAt(l1);
				if (j1 + metrics.getHeight() > rectangle.y && (k - stringbuffer.length()) + l1 < selectionEnd && (k - stringbuffer.length()) + l1 >= selectionStart) {
					Color color = g.getColor();
					g.setColor(new Color(210, 210, 210));
					if (l >= rectangle.x && l < rectangle.x + rectangle.width)
						g.fillRect(l, j1 - metrics.getAscent(), charWidth[stringbuffer.charAt(l1)], metrics.getAscent());
					g.setColor(color);
				}
				if (j1 + metrics.getHeight() > rectangle.y && k == pos1 + 1 || k == pos2 + 1) {
					Color color1 = g.getColor();
					g.setColor(Color.pink);
					if (l >= rectangle.x && l < rectangle.x + rectangle.width)
						g.fillRect(l, j1 - metrics.getAscent(), charWidth[stringbuffer.charAt(l1)], metrics.getAscent());
					g.setColor(color1);
				}
				if (j1 + metrics.getHeight() > rectangle.y && hasFocus() && showCaret && caretPosition == (k - stringbuffer.length()) + l1) {
					car.x = l;
					car.y = j1 - metrics.getHeight();
					car.height = metrics.getHeight();
				}
				if (c1 == '\n') {
					Color color2 = g.getColor();
					g.setColor(new Color(180, 180, 180));
					g.fillRect(rectangle.x, j1 - metrics.getHeight(), 65, metrics.getHeight());
					g.setColor(new Color(200, 0, 0));
					g.drawString("" + j, (rectangle.x + 55) - metrics.charsWidth(("" + j).toCharArray(), 0, ("" + j).length()), j1 - metrics.getDescent());
					if (lineToMark == j) {
						g.setColor(Color.green);
						g.fillPolygon(new int[] { rectangle.x + 58, rectangle.x + 63, rectangle.x + 58 }, new int[] { j1 - (int) (metrics.getHeight() / 1.5D), j1 - (int) (metrics.getHeight() / 3D), j1 }, 3);
					}
					g.setColor(color2);
					j1 += metrics.getHeight();
					l = byte0;
					j++;
				} else if (c1 == '\t')
					l += charWidth[32] * 4;
				else if (charWidth[c1] > 0 && j1 + metrics.getHeight() > rectangle.y) {
					if (l >= rectangle.x && l < rectangle.x + rectangle.width)
						g.drawString("" + c1, l, j1 - metrics.getDescent());
					l += charWidth[c1];
				}
			}

			if (l > i1)
				i1 = l;
			if (j1 - metrics.getHeight() > rectangle.y + rectangle.height) {
				for (int j2 = k; j2 != -1; j2 = text.indexOf('\n', j2 + 1))
					j1 += metrics.getHeight();

				k = text.length();
			}
		}

		if (j1 - metrics.getHeight() <= rectangle.y + rectangle.height) {
			g.setColor(lineNrBg);
			g.fillRect(rectangle.x, j1 - metrics.getHeight(), 65, metrics.getHeight());
			g.setColor(lineNr);
			g.drawString("" + j, (rectangle.x + 55) - metrics.charsWidth(("" + j).toCharArray(), 0, ("" + j).length()), j1 - metrics.getDescent());
			if (lineToMark == j) {
				g.setColor(Color.green);
				g.fillPolygon(new int[] { rectangle.x + 58, rectangle.x + 63, rectangle.x + 58 }, new int[] { j1 - (int) (metrics.getHeight() / 1.5D), j1 - (int) (metrics.getHeight() / 3D), j1 }, 3);
			}
		}
		if (hasFocus() && j1 - metrics.getHeight() <= rectangle.y + rectangle.height && showCaret && k == text.length() && k == caretPosition) {
			car.x = l;
			car.y = j1 - metrics.getHeight();
			car.height = metrics.getHeight();
		}
		
		// draws caret
		g.setColor(carretColor);
		g.fillRect(car.x, car.y, car.width, car.height);

		if (j1 + 10 != getSize().height || i1 + 10 != getSize().width) {
			setMinimumSize(new Dimension(i1 + 10 <= getSize().width ? getSize().width : i1 + 10, j1 + 10));
			setPreferredSize(new Dimension(i1 + 10 <= getSize().width ? getSize().width : i1 + 10, j1 + 10));
			revalidate();
		}
	}

	public void scrollToLine(int i) {
		lineToMark = i;
		int j = 1;
		int k;
		for (k = 0; j <= i && k != -1; j++)
			k = text.indexOf('\n', k + 1);

		caretPosition = k;
		requestFocus();
		repaint();
		positionVisible();
	}

	public void testOposing() {
		if (getCaretPosition() > -1 && getCaretPosition() - 1 < text.length() && getCaretPosition() > 0)
			if (text.charAt(getCaretPosition() - 1) == '}') {
				pos1 = getCaretPosition() - 1;
				int i = 0;
				int k1 = getCaretPosition() - 1;
				if (getCaretPosition() > 0)
					do {
						if (text.charAt(k1) == '}')
							i++;
						else if (text.charAt(k1) == '{')
							i--;
						if (i != 0)
							k1--;
					} while (k1 > -1 && i > 0);
				pos2 = k1;
				if (pos2 == -1)
					pos1 = -1;
				repaint();
			} else if (text.charAt(getCaretPosition() - 1) == '{') {
				pos1 = getCaretPosition() - 1;
				int j = 1;
				int l1;
				for (l1 = getCaretPosition() - 1; l1 < text.length() - 1 && j > 0;) {
					l1++;
					if (text.charAt(l1) == '{')
						j++;
					else if (text.charAt(l1) == '}')
						j--;
				}

				if (j > 0) {
					pos2 = -1;
					pos1 = -1;
				} else {
					pos2 = l1;
				}
				repaint();
			} else if (text.charAt(getCaretPosition() - 1) == ')') {
				pos1 = getCaretPosition() - 1;
				int k = 0;
				int i2 = getCaretPosition() - 1;
				if (getCaretPosition() > 0)
					do {
						if (text.charAt(i2) == ')')
							k++;
						else if (text.charAt(i2) == '(')
							k--;
						if (k != 0)
							i2--;
					} while (i2 > -1 && k > 0 && text.charAt(i2) != '\n');
				if (i2 < 0 || text.charAt(i2) == '\n')
					pos2 = -1;
				else
					pos2 = i2;
				if (pos2 == -1)
					pos1 = -1;
				repaint();
			} else if (text.charAt(getCaretPosition() - 1) == '(') {
				pos1 = getCaretPosition() - 1;
				int l = 1;
				int j2;
				for (j2 = getCaretPosition() - 1; j2 < text.length() - 1 && l > 0 && text.charAt(j2) != '\n';) {
					j2++;
					if (text.charAt(j2) == '(')
						l++;
					else if (text.charAt(j2) == ')')
						l--;
				}

				if (l > 0 || text.charAt(j2) == '\n') {
					pos2 = -1;
					pos1 = -1;
				} else {
					pos2 = j2;
				}
				repaint();
			} else if (text.charAt(getCaretPosition() - 1) == ']') {
				pos1 = getCaretPosition() - 1;
				int i1 = 0;
				int k2 = getCaretPosition() - 1;
				if (getCaretPosition() > 0)
					do {
						if (text.charAt(k2) == ']')
							i1++;
						else if (text.charAt(k2) == '[')
							i1--;
						if (i1 != 0)
							k2--;
					} while (k2 > -1 && i1 > 0 && text.charAt(k2) != '\n');
				if (k2 < 0 || text.charAt(k2) == '\n')
					pos2 = -1;
				else
					pos2 = k2;
				if (pos2 == -1)
					pos1 = -1;
				repaint();
			} else if (text.charAt(getCaretPosition() - 1) == '[') {
				pos1 = getCaretPosition() - 1;
				int j1 = 1;
				int l2;
				for (l2 = getCaretPosition() - 1; l2 < text.length() - 1 && j1 > 0 && text.charAt(l2) != '\n';) {
					l2++;
					if (text.charAt(l2) == '[')
						j1++;
					else if (text.charAt(l2) == ']')
						j1--;
				}

				if (j1 > 0 || text.charAt(l2) == '\n') {
					pos2 = -1;
					pos1 = -1;
				} else {
					pos2 = l2;
				}
				repaint();
			} else {
				unmarkOposing();
			}
	}

	public void unmarkOposing() {
		pos1 = -1;
		pos2 = -1;
		repaint();
	}

	public void positionVisible() {
		// if (getParent() instanceof JViewport) {
		// Rectangle rectangle = getVisibleRect();
		// Rectangle rectangle1 = getCaretPos();
		// try {
		// JScrollBar jscrollbar = ((JScrollPane) ((JViewport) getParent())
		// .getParent()).getHorizontalScrollBar();
		// JScrollBar jscrollbar1 = ((JScrollPane) ((JViewport) getParent())
		// .getParent()).getVerticalScrollBar();
		// int i = jscrollbar.getValue();
		// int j = jscrollbar1.getValue();
		// if ((rectangle.x + rectangle.width) - 65 <= rectangle1.x)
		// i = (rectangle1.x + 75) - rectangle.width;
		// else if (rectangle.x > rectangle1.x)
		// i = rectangle1.x - 10;
		// if (rectangle.y + rectangle.height <= rectangle1.y + rectangle1.height)
		// j = (rectangle1.y + 20 + rectangle1.height) - rectangle.height;
		// else if (rectangle.y >= rectangle1.y)
		// j = rectangle1.y - 20;
		// if (i != jscrollbar.getValue())
		// jscrollbar.setValue(i);
		// if (j != jscrollbar1.getValue())
		// jscrollbar1.setValue(j);
		// } catch (Exception exception) {
		// System.out.println("Error: " + exception);
		// }
		// }
	}

	public Rectangle getCaretPos() {
		Rectangle rectangle = new Rectangle(0, 0, 0, 0);
		if (metrics != null) {
			int i = 0;
			int j = metrics.getHeight();
			int k;
			for (k = 0; k < text.length() && k < caretPosition; k++)
				if (text.charAt(k) == '\n') {
					i = k + 1;
					j += metrics.getHeight();
				}

			String s = text.substring(i, k);
			rectangle.x = 0;
			for (int l = 0; l < s.length(); l++)
				rectangle.x += charWidth[s.charAt(l)];

			rectangle.y = j - metrics.getHeight();
			rectangle.height = metrics.getHeight();
			rectangle.width = 2;
			return rectangle;
		} else {
			return rectangle;
		}
	}

	// public void setFont(Font font) {
	// super.setFont(font);
	// repaint();
	// }

	public int getCharPos(int x, int y) {
		if (metrics != null && charWidth != null) {
			int k = 0;
			byte byte0 = 65;
			int l = byte0;
			int i1;
			for (i1 = metrics.getHeight(); i1 + metrics.getHeight() < 0;) {
				i1 += metrics.getHeight();
				if (k + 1 >= text.length())
					break;
				k = text.indexOf('\n', k);
				k++;
			}

			if (y < i1 - metrics.getHeight())
				return -1;
			for (; k < text.length(); k++)
				if (text.charAt(k) == '\n') {
					if (x > l && y < i1 && y >= i1 - metrics.getHeight())
						return k;
					if (x <= byte0 && y < i1 && y >= i1 - metrics.getHeight() && k + 1 < text.length())
						return k;
					i1 += metrics.getHeight();
					l = byte0;
				} else {
					if (l < x && l + charWidth[text.charAt(k)] / 2 >= x && y < i1 && y >= (i1 - metrics.getHeight()) + 2)
						return k;
					if (l < x && l + charWidth[text.charAt(k)] >= x && y < i1 && y >= i1 - metrics.getHeight()) {
						if (k + 1 < text.length())
							return k + 1;
						l += charWidth[text.charAt(k)];
					} else if (x <= byte0 && y < i1 && y >= i1 - metrics.getHeight()) {
						if (k + 1 < text.length())
							return k;
						l += charWidth[text.charAt(k)];
					} else {
						l += charWidth[text.charAt(k)];
					}
				}

		}
		return text.length();
	}

	private boolean divider(char c) {
		boolean flag = false;
		for (int i = 0; (i < dividerWord.length) & (!flag); i++)
			if (c == dividerWord[i])
				flag = true;

		return flag;
	}

	public void readBlue() {
		List<String> vector = new LinkedList<String>();
		blue = new String[10][];
		boolean first = true;
		int colorNum = 0;
		try {
			BufferedReader bufferedreader = new BufferedReader(new FileReader("blueWord.ed"));
			for (String s = bufferedreader.readLine(); s != null; s = bufferedreader.readLine()) {
				if (!s.startsWith(">>")) {
					vector.add(s.trim());
					// System.out.println(colorNum+" "+s);
				} else if (!first) {
					blue[colorNum] = vector.toArray(new String[vector.size()]);
					vector.clear();
					colorNum++;
				} else {
					first = false;
				}
			}

			bufferedreader.close();
		} catch (IOException ioexception) {
			System.out.println("Error: " + ioexception);
		}

		/*
		 * int i = 0; for (int j = 0; j < vector.size(); j++) { if (((String)
		 * vector.get(j)).length() > i) { i = ((String) vector.get(j)).length(); } }
		 * 
		 * blue = new String[10][i 20]; // 10 for 10 colors for (int k = 0; k <
		 * vector.size(); k++) { String s1 = (String) vector.get(k); int l =
		 * (s1.length() - 1) 20; try { while (blue[l] != null) l++; blue[l] = new
		 * String(); blue[l] = s1; } catch (Exception exception) {
		 * System.out.println(exception + "Word: " + s1 + "   I: " + l +
		 * "   length: " + blue.length); } }
		 */
	}

	private boolean isBlue(String s) {
		boolean flag = false;
		for (int clNum = 0; clNum < blue.length; clNum++) {
			if (blue[clNum] == null) {
				continue;
			}
			for (int i = 0; i < blue[clNum].length; i++) {
				if (blue[clNum][i].equals(s)) {
					flag = true;
					break;
				} else {
					i++;
				}
			}
			if (flag) {
				break;
			}
		}

		return flag;
	}

	public int getCaretPosition() {
		return caretPosition;
	}

	public int getSelectionStart() {
		return selectionStart;
	}

	public int getSelectionEnd() {
		return selectionEnd;
	}

	public Dimension getPreferredScrollableViewportSize() {
		return getPreferredSize();
	}

	public int getScrollableUnitIncrement(Rectangle rectangle, int i, int j) {
		return 5;
	}

	public int getScrollableBlockIncrement(Rectangle rectangle, int i, int j) {
		switch (i) {
		case 1: // '\001'
			return rectangle.height;

		case 0: // '\0'
			return rectangle.width;
		}
		throw new IllegalArgumentException("Invalid orientation: " + i);
	}

	public boolean getScrollableTracksViewportWidth() {
		if (getParent() instanceof JViewport)
			return ((JViewport) getParent()).getWidth() > getPreferredSize().width;
		else
			return false;
	}

	public boolean getScrollableTracksViewportHeight() {
		if (getParent() instanceof JViewport)
			return ((JViewport) getParent()).getHeight() > getPreferredSize().height;
		else
			return false;
	}

	public void lostOwnership(Clipboard clipboard, Transferable transferable) {
	}

	public void focusLost(FocusEvent focusevent) {
		repaint();
	}

	public void focusGained(FocusEvent focusevent) {
		repaint();
	}

	public void readFromFile(File file) {
		try {
			StringBuffer stringbuffer = new StringBuffer();
			if (file.exists()) {
				BufferedReader bufferedreader = new BufferedReader(new InputStreamReader(new BufferedInputStream(new FileInputStream(file))));
				String s = bufferedreader.readLine();
				boolean flag = true;
				for (; s != null; s = bufferedreader.readLine())
					if (!flag) {
						stringbuffer.append("\n" + s);
					} else {
						flag = false;
						stringbuffer.append(s);
					}

				bufferedreader.close();
				text = stringbuffer.toString();
			}
		} catch (IOException ioexception) {
			System.out.println("Error: " + ioexception);
		}
		changed = false;
	}

	public void saveToFile(File file) {
		try {
			PrintWriter printwriter = new PrintWriter(new FileWriter(file));
			printwriter.print(text);
			printwriter.close();
		} catch (IOException ioexception) {
			System.out.println("Error: " + ioexception);
		}
		changed = false;
	}

	// ---------------------------------------------------------------------
	// ---------------------------------------------------------------------
	// ---------------------------------------------------------------------

	// @Override
	public void onMouseClick(int x, int y) {

		requestFocus();
		int i = getCharPos(x, y);
		mousePressPos = i;
		if (i != -1) {
			// TODO
			// 1-> SHIFT ?
			// if ((mouseevent.getModifiers() & 1) > 0) {
			if (false) {
				if (caretPosition == selectionStart && i <= selectionEnd) {
					caretPosition = i;
					selectionStart = caretPosition;
				} else if (caretPosition == selectionStart) {
					selectionStart = selectionEnd;
					caretPosition = i;
					selectionEnd = caretPosition;
				} else if (caretPosition == selectionEnd && i >= selectionStart) {
					caretPosition = i;
					selectionEnd = caretPosition;
				} else {
					selectionEnd = selectionStart;
					caretPosition = i;
					selectionStart = caretPosition;
				}
			} else {
				caretPosition = i;
				selectionEnd = caretPosition;
				selectionStart = caretPosition;
			}
			repaint();
			positionVisible();
		}
	}

	// @Override
	public void onMouseDragged(int x, int y) {
		int i = getCharPos(x, y);
		if (mousePressPos != -1) {
			if (mousePressPos <= i) {
				selectionStart = mousePressPos;
				selectionEnd = i;
			} else {
				selectionEnd = mousePressPos;
				selectionStart = i;
			}
			caretPosition = i;
			repaint();
			positionVisible();
		}
		repaint();
	}

	// @Override
	public void onMouseRelease(int x, int y) {
		mousePressPos = -1;
		testOposing();
	}

	// ---------------------------------------------------------------------

}
