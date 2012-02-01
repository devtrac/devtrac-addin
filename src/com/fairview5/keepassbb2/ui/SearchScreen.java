/*
KeePass for BlackBerry
Copyright 2007,2008 Fairview 5 Engineering, LLC <george.joseph@fairview5.com>

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

import java.util.Vector;

import net.rim.device.api.system.Characters;
import net.rim.device.api.system.Clipboard;
import net.rim.device.api.ui.*;
import net.rim.device.api.ui.component.*;
import net.rim.device.api.ui.container.MainScreen;
import net.rim.device.api.ui.container.VerticalFieldManager;

import com.fairview5.keepassbb2.KeePassBB2Screen;
import com.fairview5.keepassbb2.Options;
import com.fairview5.keepassbb2.kdb4.*;

public class SearchScreen extends MainScreen implements FieldChangeListener {
	private KeePassBB2Screen ks;

	public EditField ef = new EditField("Find: ", null);
	VerticalFieldManager vfm = new VerticalFieldManager(Manager.VERTICAL_SCROLL
			| Manager.VERTICAL_SCROLLBAR);

	ObjectListField lf = new ObjectListField() {
		boolean showGroup = Options.getBooleanOption(
				Options.OPTION_SHOW_GROUP_SEARCH, false);
		boolean tagExpired = Options.getBooleanOption(
				Options.OPTION_TAG_EXPIRED_ENTRIES, false);

		public void drawListRow(ListField lf, Graphics g, int ix, int y, int width) {
			Kdb4Entry ke = (Kdb4Entry) ((ObjectListField) lf).get(lf, ix);

			String name = ((tagExpired && ke.isExpired()) ? "[exp] " : "")
					+ (showGroup ? ke.parent.toString() + ": " : "") + ke.toString();

			g.drawText(name, 0, y, Graphics.ELLIPSIS, width);
		}
	};

	Kdb4EntryList kel = new Kdb4EntryList();

	public SearchScreen(KeePassBB2Screen ks) {
		super(Manager.NO_VERTICAL_SCROLL | Manager.NO_VERTICAL_SCROLLBAR);
		this.ks = ks;
		Kdb4File.current.kdb4.getEntries(kel);
		add(ef);
		ef.setChangeListener(this);
		add(new SeparatorField());
		vfm.add(lf);
		add(vfm);
	}

	public boolean isDirty() {
		return (false);
	}

	public boolean keyChar(char c, int status, int time) {
		// if (c == Characters.ENTER || c == Characters.SPACE) {
		if (c == Characters.ENTER) {
			int nix = lf.getSelectedIndex();
			final Object o = lf.get(lf, nix);
			if (o instanceof Kdb4Entry) {
				openEntryMenuItem((Kdb4Entry) o, nix, 0).run();
				return (true);
			}
		}
		return super.keyChar(c, status, time);
	}

	public void makeMenu(Menu menu, int instance) {

		int nix = lf.getSelectedIndex();
		if (nix >= 0) {
//			ContextMenu ctx = getContextMenu(instance);
			final Object o = lf.get(lf, nix);
			if ((o instanceof Kdb4Entry)) {
				menu.add(openEntryMenuItem((Kdb4Entry) o, nix, 1));
				menu.add(copyPasswordMenuItem((Kdb4Entry) o, nix, 1));
			}
//			menu.add(ctx, true);
		}
		super.makeMenu(menu, Menu.INSTANCE_DEFAULT);
	}

	public boolean onMenu(int instance) {
		int nix = lf.getSelectedIndex();
		if (nix >= 0) {
			final Object o = lf.get(lf, nix);
			if ((o instanceof Kdb4Entry && Options.getBooleanOption(
					Options.OPTION_SINGLECLICKENTRY, false))) {
				openEntryMenuItem((Kdb4Entry) o, nix, 0).run();
				return (true);
			}
		}
		return (super.onMenu(instance));
	}

	public MenuItem openEntryMenuItem(final Kdb4Entry o, final int nix, int pos) {
		return (new MenuItem("Edit/View", pos, 0) {
			public void run() {
				Kdb4EntryScreen es = new Kdb4EntryScreen(o);
				if (es.doModal() != Dialog.OK)
					return;
				ks.setTitle();
				if (Options.getBooleanOption(Options.OPTION_AUTOSAVECHANGES, true)) {
					ks.save();
				}
			}
		});
	}

	public MenuItem copyPasswordMenuItem(final Kdb4Entry o, final int nix,
			int pos) {
		return (new MenuItem("Copy Password", pos, 0) {
			public void run() {
				Clipboard.getClipboard().put(
						o.getStringDynamicAttribute("Password"));
			}
		});
	}

	public void fieldChanged(Field field, int context) {
		if (field != ef)
			return;
		String sc = ef.getText().toLowerCase();

		Vector v = new Vector();

		for (int i = 0; i < kel.size(); i++) {
			Kdb4Entry ke = kel.getEntry(i);
			String[] keywords = ke.keywordList;
			for (int j = 0; j < keywords.length; j++) {
				if (keywords[j].toLowerCase().indexOf(sc) > -1) {
					v.addElement(ke);
					break;
				}
			}
		}
		Object[] a = new Object[v.size()];
		v.copyInto(a);
		lf.set(a);

	}

}
