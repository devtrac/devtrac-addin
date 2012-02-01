/**
 * BlackBerry File Explorer Screen
 * Copyright (c) 2008 Fairview 5 Engineering, LLC
 * 
    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
 
 */

package com.fairview5.keepassbb2.common.file;

import java.io.IOException;
import java.util.Enumeration;
import java.util.Vector;

import net.rim.device.api.system.*;
import net.rim.device.api.ui.*;
import net.rim.device.api.ui.component.*;
import net.rim.device.api.ui.container.*;

import com.fairview5.keepassbb2.common.util.CommonUtils;


public final class FileExplorerScreen extends MainScreen implements FocusChangeListener {
	protected static Bitmap dirIcon = EncodedImage.getEncodedImageResource("dir.png").getBitmap();
	protected static Bitmap fileIcon = EncodedImage.getEncodedImageResource("file.png").getBitmap();

	FEXList lf;
	boolean saveAs;
	KFile currentNode;
	int rc = Dialog.CANCEL;
	LabelField lfTitle = new LabelField("Explorer", DrawStyle.ELLIPSIS | DrawStyle.TRUNCATE_BEGINNING);
	EditField efName;

	public FileExplorerScreen() throws IOException {
		this(false, "/");
	}

	public FileExplorerScreen(boolean sa, String startingPath) throws IOException {
		// super(new VerticalFieldManager(Manager.NO_HORIZONTAL_SCROLL |
		// Manager.NO_VERTICAL_SCROLL));
		super(Manager.NO_HORIZONTAL_SCROLL | Manager.NO_VERTICAL_SCROLL);
		saveAs = sa;
		add(lfTitle);
		add(new SeparatorField());
		KFile f = new KFile(startingPath);
		if (!f.isDirectory() && !f.isRoot())
			f = f.getParent();
		lf = new FEXList(f);
		lf.setFocusListener(this);
		VerticalFieldManager vfm = new VerticalFieldManager(Manager.NO_HORIZONTAL_SCROLL | Manager.VERTICAL_SCROLL
				| Manager.USE_ALL_HEIGHT);
		vfm.add(lf);
		add(vfm);
		long style = Field.USE_ALL_WIDTH | TextField.NO_NEWLINE | (saveAs ? 0 : Field.NON_FOCUSABLE); 
		efName = new EditField("Name: ", null, 128, style) {
			public boolean keyChar(char c, int status, int time) {
				if (c == Characters.ENTER) {
					try {
						KFile newf = new KFile(currentNode, efName.getText());
						if (newf.exists()) {
							if (Dialog.ask(Dialog.D_YES_NO, "File already exists.  Are you sure?", Dialog.NO) != Dialog.YES)
								return true;
						}
						currentNode = newf;
					} catch (IOException e) {
						Dialog.alert("Error creating file: " + e.getMessage());
						return true;
					}
					rc = Dialog.OK;
					close();
					return (true);
				} else if (c == Characters.ESCAPE) {
					rc = Dialog.CANCEL;
					close();
					return (true);
				} else {
					return super.keyChar(c, status, time);
				}
			}
		};
		VerticalFieldManager vfm2 = new VerticalFieldManager(Manager.NO_HORIZONTAL_SCROLL | Manager.NO_VERTICAL_SCROLL);
		vfm2.add(new SeparatorField());
		vfm2.add(efName);
		if (saveAs) setStatus(vfm2);
	}

	public void setTitle(String string) {
		lfTitle.setText(string);
	}

	public int doModal() {
		UiApplication.getUiApplication().pushModalScreen(this);
		return (rc);
	}

	public String getSelectedFileName() {
		return (currentNode.getFullName());
	}

	public KFile getSelectedFileObject() throws IOException {
		return currentNode;
	}

	public void makeMenu(Menu m, int instance) {
		lf.makeContextMenu(m);
	}

	public void focusChanged(Field field, int eventType) {
		if (field == lf) {
			int ix = lf.getSelectedIndex();
			if (ix < 0)
				return;
			KFile f = (KFile) lf.get(lf, ix);
			if (f.isDirectory()) efName.setText("");
			else efName.setText(f.getName());
		}
	}

	class FEXList extends ObjectListField {

		public FEXList(KFile start) {
			selectAction(start);
		}

		public void drawListRow(ListField ilf, Graphics g, int ix, int y, int width) {
			KFile f = (KFile) this.get(ilf, ix);
			Bitmap ic = f.isDirectory() ? FileExplorerScreen.dirIcon : FileExplorerScreen.fileIcon;
			g.drawBitmap(0, y, ic.getWidth(), ic.getHeight(), ic, 0, 0);
			g.drawText(f.getName(), ic.getWidth() + 2, y, Graphics.ELLIPSIS, width);
		}

		public boolean keyChar(char c, int status, int time) {
			if (c == Characters.ENTER || c == Characters.SPACE) {
				int ix = getSelectedIndex();
				if (ix < 0)
					return false;
				KFile f = (KFile) get(this, ix);
				return (selectAction(f));
			} else if (c == Characters.ESCAPE) {
				return up();
			} else {
				return super.keyChar(c, status, time);
			}
		}

		public boolean trackwheelClick(int status, int time) {
			return (selectAction((KFile) get(this, getSelectedIndex())));
		}

		public boolean up() {
			if (currentNode.isRoot()) {
				close();
				return true;
			}
			KFile ln = currentNode;
			try {
				selectAction(currentNode.getParent());
			} catch (IOException e) {
				Dialog.alert("Can't navigate up: " + e.getMessage());
			}
			efName.setText("");
			setSelectedIndex(ln.getIntCookie());
			return true;
		}

		void addMI(boolean condition, Menu menu, MenuItem mi, int ordinal, int priority) {
			if (condition) {
				mi.setOrdinal(ordinal);
				mi.setPriority(priority);
				menu.add(mi);
				return;
			}
		}
		
		
		protected void makeContextMenu(Menu ctx) {
			KFile f = null;
			int ix = getSelectedIndex();
			if (ix >= 0)
				f = (KFile) get(this, ix);

				addMI(!currentNode.isRoot(), ctx, miNavUp, 100, 0);
			
			ctx.add(MenuItem.separator(110));

			if (f != null) {
				addMI(f!= null, ctx, miOpen, 120, 0);
			}

			if (currentNode.canWrite()) {
				addMI(saveAs, ctx, miNewFile, 130, 0);
				addMI(saveAs, ctx, miNewFolder, 140, 0);
				addMI(f != null, ctx, miRename, 150, 0);
				addMI(f != null, ctx, miDelete, 160, 0);
			}			
			ctx.add(MenuItem.separator(170));
			addMI(true, ctx, miClose, 180, 0);
		}

		MenuItem miOpen = new MenuItem("Open", 0, 0) {
			public void run() {
				int ix = getSelectedIndex();
				KFile f = (KFile) get(lf, ix);
				selectAction(f);
			}
		};

		MenuItem miDelete = new MenuItem("Delete", 0, 0) {
			public void run() {
				int rc = Dialog.ask(Dialog.D_DELETE);
				if (rc != Dialog.DELETE)
					return;
				int ix = getSelectedIndex();
				KFile f = (KFile) get(lf, ix);
				try {
					f.delete();
					selectAction(currentNode);
				} catch (IOException e) {
					Dialog.alert("Unable to delete: " + e.getMessage());
				}
			}
		};

		MenuItem miNavUp = new MenuItem("Up", 0, 0) {
			public void run() {
				up();
			}
		};

		MenuItem miClose = new MenuItem("Close", 0, 0) {
			public void run() {
				close();
			}
		};

		MenuItem miNewFolder = new MenuItem("New Folder", 0, 0) {
			public void run() {
				NewDialog nd = new NewDialog("New Folder");
				if (nd.doModal() != Dialog.OK)
					return;
				try {
					KFile newf = new KFile(currentNode, nd.getText() + "/");
					newf.mkdir();
					selectAction(newf);
				} catch (IOException e) {
					Dialog.alert("Error creating folder: " + e.getMessage());
				}
			}
		};

		MenuItem miRename = new MenuItem("Rename", 0, 0) {
			public void run() {
				int ix = getSelectedIndex();
				KFile f = (KFile) get(lf, ix);
				String on = f.getName();
				if (f.isDirectory())
					on = on.substring(0, on.length() - 1);
				NewDialog nd = new NewDialog("New Name", on);
				if (nd.doModal() != Dialog.OK)
					return;
				try {
					f.rename(nd.getText());
					selectAction(currentNode);
				} catch (IOException e) {
					Dialog.alert("Error renaming: " + e.getMessage());
				}
			}
		};

		MenuItem miNewFile = new MenuItem("New File", 0, 0) {
			public void run() {
				NewDialog nd = new NewDialog("New File");
				if (nd.doModal() != Dialog.OK)
					return;
				try {
					KFile newf = new KFile(currentNode, nd.getText());
					if (newf.exists()) {
						if (Dialog.ask(Dialog.D_YES_NO, "File already exists.  Are you sure?", Dialog.NO) != Dialog.YES)
							return;
					}
					currentNode = newf;
				} catch (IOException e) {
					Dialog.alert("Error creating file: " + e.getMessage());
					return;
				}
				rc = Dialog.OK;
				close();
				return;
			}
		};

		public boolean selectAction(KFile f) {
			setTitle(f.getFullName());
			currentNode = f;
			if (!f.isDirectory()) {
				if (saveAs && f.exists()) {
					if (Dialog.ask(Dialog.D_YES_NO, "File already exists.  Are you sure?", Dialog.NO) != Dialog.YES)
						return false;
				}
				rc = Dialog.OK;
				close();
				return true;
			}
			Enumeration eee = null;

			set(null);

			try {

				eee = f.list();

				Vector dirs = new Vector();
				Vector files = new Vector();
				while (eee.hasMoreElements()) {
					String fn = (String) eee.nextElement();
					try {
						KFile ffn = new KFile(f, fn);
						if (ffn.isDirectory())
							dirs.addElement(ffn);
						else
							files.addElement(ffn);
					} catch (Throwable t) {
						String msg = t.getMessage();
						if (msg == null) msg = t.toString();
						CommonUtils.logger(msg+": Skipping "+fn);
					}
				}
				Object[] entries = new Object[dirs.size() + files.size()];
				for (int i = 0; i < dirs.size(); i++) {
					entries[i] = dirs.elementAt(i);
					((KFile) entries[i]).setCookie(i);
				}
				for (int i = dirs.size(), j = 0; i < dirs.size() + files.size(); i++, j++) {
					entries[i] = files.elementAt(j);
					((KFile) entries[i]).setCookie(i);
				}
				set(entries);
				return (true);
			} catch (Throwable e) {
				String msg = e.getMessage();
				if (msg == null) msg = e.toString();
				Dialog.alert(msg+": "+f.getFullName());
			}
			return (false);
		}
	}

}

class NewDialog extends PopupScreen implements FieldChangeListener {
	EditField ef;
	int rc = Dialog.CANCEL;

	public NewDialog(String title) {
		this(title, null);
	}

	public NewDialog(String title, String value) {
		super(new VerticalFieldManager(Manager.NO_HORIZONTAL_SCROLL | Manager.NO_VERTICAL_SCROLL));
		add(new LabelField(title));
		add(new SeparatorField());
		ef = new EditField(null, value);
		add(ef);
		add(new SeparatorField());
		HorizontalFieldManager hfm = new HorizontalFieldManager();
		ButtonField bfOK = new ButtonField("Ok", ButtonField.CONSUME_CLICK);
		bfOK.setChangeListener(this);
		ButtonField bfCancel = new ButtonField("Cancel",
				ButtonField.CONSUME_CLICK);
		bfCancel.setChangeListener(this);
		hfm.add(bfOK);
		hfm.add(bfCancel);
		add(hfm);
	}

	public int doModal() {
		UiApplication.getUiApplication().pushModalScreen(this);
		return rc;
	}

	public String getText() {
		return ef.getText();
	}

	public boolean keyChar(char c, int status, int time) {
		if (c == Characters.ENTER) {
			rc = Dialog.OK;
			close();
			return (true);
		} else if (c == Characters.ESCAPE) {
			rc = Dialog.CANCEL;
			close();
			return (true);
		} else {
			return super.keyChar(c, status, time);
		}
	}

	public void fieldChanged(Field field, int context) {
		if (((ButtonField) field).getLabel().equals("Ok")) {
			rc = Dialog.OK;
		}
		close();
	}
}
