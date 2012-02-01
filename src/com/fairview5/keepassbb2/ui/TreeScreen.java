/*
KeePass for BlackBerry
Copyright 2007 Fairview 5 Engineering, LLC <george.joseph@fairview5.com>

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

package com.fairview5.keepassbb2.ui;

import net.rim.device.api.system.*;
import net.rim.device.api.ui.*;
import net.rim.device.api.ui.component.*;
import net.rim.device.api.util.CharacterUtilities;

import com.fairview5.keepassbb2.KeePassBB2Screen;
import com.fairview5.keepassbb2.Options;
import com.fairview5.keepassbb2.kdb4.*;

public final class TreeScreen extends TreeField {
	private KeePassBB2Screen ks;
	private TreeField tf;
	public static int NUM_ICONS = 69;
	public static Bitmap mIcon[] = new Bitmap[NUM_ICONS];

	static {
		int i;
		try {
			for (i = 0; i < NUM_ICONS; i++) {
				mIcon[i] = EncodedImage.getEncodedImageResource(i + "_gt.png")
						.getBitmap();
			}
		} catch (Exception e) {
		}
	}

	public TreeScreen(KeePassBB2Screen ks) {
		super(new TreeFieldCallback() {
			public void drawTreeItem(TreeField tf, Graphics graphics, int node,
					int y, int width, int indent) {

				Font pl, it;

				Kdb4Object cookie = (Kdb4Object) tf.getCookie(node);
				Bitmap ic = null;
				int ix = cookie.getIntDataElement("IconID");
				String name = "";
				if (cookie instanceof Kdb4Entry) {

					if (Options.getBooleanOption(Options.OPTION_TAG_EXPIRED_ENTRIES,
							false)) {
						if (((Kdb4Entry) cookie).isExpired())
							name = "[exp] ";
					}

					name += ((Kdb4Entry) cookie).getStringAttribute("Title")
							.getValue();
					if (Options.getBooleanOption(Options.OPTION_SHOW_USERID_IN_TREE,
							false)) {
						name += (": " + ((Kdb4Entry) cookie).getStringAttribute(
								"UserName").getValue());
					}
				} else if (cookie instanceof Kdb4Group) {
					name = ((Kdb4Group) cookie).getStringDataElement("Name");
				}

				if (ix >= 0 && ix < TreeScreen.NUM_ICONS)
					ic = TreeScreen.mIcon[ix];

				if (ic != null
						&& !Options.getBooleanOption(Options.OPTION_HIDEICONS, false)) {
					graphics.drawBitmap(indent, y, ic.getWidth(), ic.getHeight(),
							ic, 0, 0);
					indent += ic.getWidth();
				}

				graphics.drawText(name, indent, y, Graphics.ELLIPSIS, width);

			}
		}, Field.FOCUSABLE);
		this.ks = ks;
		tf = this;
		setEmptyString("No Database Loaded", DrawStyle.HCENTER);
		setIndentWidth(20);
	}

	public boolean keyChar(char c, int status, int time) {
		int nix = tf.getCurrentNode();
		if (nix < 0)
			return (false);
		Object o = tf.getCookie(nix);

		if (c == Characters.ENTER) {
			if (o instanceof Kdb4Entry) {
				miOpenEntry.run();
				return (true);
			}
			if (o instanceof Kdb4Group) {
				miOpenGroup.run();
				return (true);
			}
		}
		if (Options.getBooleanOption(Options.OPTION_SEARCHFYAT, true)
				&& tf.getNodeCount() > 0
				&& (CharacterUtilities.isLetter(c) || CharacterUtilities.isDigit(c) || CharacterUtilities
						.isPunctuation(c))) {
			SearchScreen sc = new SearchScreen(ks);
			sc.ef.insert(c + "");
			sc.ef.setCursorPosition(1);
			UiApplication.getUiApplication().pushModalScreen(sc);
		}
		return super.keyChar(c, status, time);
	}

	void addMI(Menu menu, MenuItem mi, int ordinal, int priority) {
		mi.setOrdinal(ordinal);
		mi.setPriority(priority);
		menu.add(mi);
	}

	public void makeMenu(Menu ctx) {
		final int nix = tf.getCurrentNode();

		if (nix >= 0) {
			final Object o = tf.getCookie(tf.getCurrentNode());
			if (o instanceof Kdb4Entry) {
				addMI(ctx, miOpenEntry, 100, 0);
				addMI(ctx, miCopyPassword, 110, 0);
				addMI(ctx, miCopyUserid, 111, 0);
				addMI(ctx, miDeleteEntry, 120, 0);
			}
			if (o instanceof Kdb4Group) {
				ctx.add(MenuItem.separator(190));
				addMI(ctx, miNewEntry, 200, 0);
				ctx.add(MenuItem.separator(205));
				if (tf.getExpanded(nix)) {
					addMI(ctx, miCollapseGroup, 210, 0);
				} else {
					addMI(ctx, miExpandGroup, 220, 0);
				}
				addMI(ctx, miOpenGroup, 240, 0);
				if (((Kdb4Group) o).parent instanceof Kdb4Group)
					addMI(ctx, miDeleteGroup, 250, 0);
				addMI(ctx, miNewSubGroup, 260, 0);
			}
		}
	}

	protected boolean navigationClick(int status, int time) {
		if ((status & KeypadListener.STATUS_ALT) != 0) {
			return super.navigationClick(status, time);
		}
		boolean isTouch = !((status & KeypadListener.STATUS_TRACKWHEEL) != 0 || Trackball
				.isSupported());
		final int nix = tf.getCurrentNode();
		if (nix >= 0) {
			Object o = tf.getCookie(nix);
			if ((o instanceof Kdb4Entry)) {
				miOpenEntry.run();
				return (true);
			}

			if ((o instanceof Kdb4Group && !isTouch)) {
				tf.setExpanded(nix, !tf.getExpanded(nix));
				return (true);
			}
		}
		return super.navigationClick(status, time);
	}

	private void cleanup() {
		ks.setTitle();
		if (Options.getBooleanOption(Options.OPTION_AUTOSAVECHANGES, true)) {
			ks.save();
		}
	}

	MenuItem miSearch = new MenuItem("Find", 0, 0) {
		public void run() {
			SearchScreen sc = new SearchScreen(ks);
			UiApplication.getUiApplication().pushModalScreen(sc);
		}
	};

	MenuItem miDeleteEntry = new MenuItem("Delete", 0, 0) {
		public void run() {
			int nix = tf.getCurrentNode();
			final Kdb4Entry o = (Kdb4Entry) tf.getCookie(nix);

			if (Options.getBooleanOption(Options.OPTION_DELETEPROMPT, true)) {
				int rc = Dialog.ask(Dialog.D_DELETE, "Delete Entry" + " "
						+ o.getStringDynamicAttribute("Title"));
				if (rc != Dialog.DELETE)
					return;
			}
			Kdb4Object parent = o.parent;
			parent.childElements.removeObject(o);
			parent.setDirty();
			tf.deleteSubtree(nix);
			cleanup();
		}
	};

	public MenuItem miOpenEntry = new MenuItem("Edit/View", 0, 0) {
		public void run() {
			int nix = tf.getCurrentNode();
			final Kdb4Entry o = (Kdb4Entry) tf.getCookie(nix);
			Kdb4EntryScreen es = new Kdb4EntryScreen(o);
			if (es.doModal() == Dialog.OK)
				cleanup();
		}
	};
	MenuItem miCopyPassword = new MenuItem("Copy Password", 0, 0) {
		public void run() {
			int nix = tf.getCurrentNode();
			final Kdb4Entry o = (Kdb4Entry) tf.getCookie(nix);
			Clipboard.getClipboard().put(o.getStringDynamicAttribute("Password"));
		}
	};
	MenuItem miCopyUserid = new MenuItem("Copy Userid", 0, 0) {
		public void run() {
			int nix = tf.getCurrentNode();
			final Kdb4Entry o = (Kdb4Entry) tf.getCookie(nix);
			Clipboard.getClipboard().put(o.getStringDynamicAttribute("UserName"));
		}
	};

	MenuItem miOpenGroup = new MenuItem("Open", 0, 0) {
		public void run() {
			int nix = tf.getCurrentNode();
			final Kdb4Group o = (Kdb4Group) tf.getCookie(nix);
			Kdb4GroupScreen es = new Kdb4GroupScreen(o);
			if (es.doModal() == Dialog.OK)
				cleanup();
		}
	};

	MenuItem miCollapseGroup = new MenuItem("Collapse", 0, 0) {
		public void run() {
			int nix = tf.getCurrentNode();
			tf.setExpanded(nix, false);
		}
	};

	MenuItem miExpandGroup = new MenuItem("Expand", 0, 0) {
		public void run() {
			int nix = tf.getCurrentNode();
			tf.setExpanded(nix, true);
		}
	};

	MenuItem miDeleteGroup = new MenuItem("Delete", 0, 0) {
		public void run() {
			int nix = tf.getCurrentNode();
			final Kdb4Group o = (Kdb4Group) tf.getCookie(nix);
			if (Options.getBooleanOption(Options.OPTION_DELETEPROMPT, true)) {
				int rc = Dialog.ask(Dialog.D_DELETE, "Delete Group "
						+ o.getStringDataElement("Name"));
				if (rc != Dialog.DELETE)
					return;
			}
			Kdb4Object parent = o.parent;
			parent.childElements.removeObject(o);
			parent.setDirty();
			tf.deleteSubtree(nix);
			cleanup();
		}
	};

	MenuItem miNewEntry = new MenuItem("New Entry", 0, 0) {
		public void run() {
			try {
				int nix = tf.getCurrentNode();
				final Kdb4Group o = (Kdb4Group) tf.getCookie(nix);
				Kdb4Entry pwe = new Kdb4Entry();
				Kdb4EntryScreen pes = new Kdb4EntryScreen(pwe);
				int rc = pes.doModal();
				if (rc != Dialog.OK) {
					return;
				}
				o.addEntry(pwe);
				cleanup();
				tf.addChildNode(nix, pwe);
			} catch (Throwable e) {
				Dialog.alert(e.getMessage());
			}
		}
	};

	MenuItem miNewSubGroup = new MenuItem("New Group", 0, 0) {
		public void run() {
			try {
				int nix = tf.getCurrentNode();
				final Kdb4Group ppwg = (Kdb4Group) tf.getCookie(nix);
				Kdb4Group pwg = new Kdb4Group();
				Kdb4GroupScreen pgs = new Kdb4GroupScreen(pwg);
				int rc = pgs.doModal();
				if (rc != Dialog.OK) {
					return;
				}
				ppwg.addGroup(pwg);
				cleanup();
				tf.addChildNode(nix, pwg);
			} catch (Throwable e) {
				Dialog.alert(e.getMessage());
			}
		}
	};

}
