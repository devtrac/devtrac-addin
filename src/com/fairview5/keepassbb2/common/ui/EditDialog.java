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
package com.fairview5.keepassbb2.common.ui;

import net.rim.device.api.system.Characters;
import net.rim.device.api.ui.*;
import net.rim.device.api.ui.component.*;
import net.rim.device.api.ui.container.*;

public final class EditDialog extends PopupScreen implements FieldChangeListener {

        public static final int D_YES_NO = 0;
        public static final int D_YES_NO_CANCEL = 1;
        public static final int D_OK_CANCEL = 2;
        
  Field efa[];
  RadioButtonGroup rbg;
  ButtonField bfOk;
  ButtonField bfCancel;
  VerticalFieldManager fm = null;
  int rc;

  public EditDialog(String legend) {
    super(new VerticalFieldManager(Manager.VERTICAL_SCROLL | Manager.VERTICAL_SCROLLBAR));
    //super();
    add(new LabelField(legend));
    add(new SeparatorField());
    fm = new VerticalFieldManager();
    add(fm);
    add(new SeparatorField());
    HorizontalFieldManager hfm = new HorizontalFieldManager();
    add(hfm);
    bfOk = new ButtonField("Ok");
    bfOk.setChangeListener(this);
    bfCancel = new ButtonField("Cancel");
    bfCancel.setChangeListener(this);
    hfm.add(bfOk);
    hfm.add(bfCancel);
  }

  public void addField(Field f) {
    if (fm == null) super.add(f);
    else fm.add(f);
  }

  public EditDialog(String legend, Field bef) {
    super(new VerticalFieldManager());
    //super();
    efa = new Field[1];
    efa[0] = bef;
    add(new LabelField(legend));
    add(new SeparatorField());
    add(bef);
    add(new SeparatorField());
    HorizontalFieldManager hfm = new HorizontalFieldManager();
    add(hfm);
    bfOk = new ButtonField("Ok");
    bfOk.setChangeListener(this);
    bfCancel = new ButtonField("Cancel");
    bfCancel.setChangeListener(this);
    hfm.add(bfOk);
    hfm.add(bfCancel);
    bef.setFocus();
  }

  public int doModal() {
    UiApplication.getUiApplication().pushModalScreen(this);
    return (rc);
  }

  public int getRC() {
    return (rc);
  }

  public void makeMenu(Menu menu, int instance) {
    final Field f = getFieldWithFocus();
    if (!(f instanceof TextField)) return;
    ContextMenu ctx = f.getContextMenu();
    menu.add(ctx, true);
  }

public boolean onMenu(int instance) {
  Menu m = new Menu();
  makeMenu(m, instance);
  if (m.getSize() == 0) return(false);
  int ix = m.show();
  if (ix == Menu.CANCELLED) return(false);
//  MenuItem mi = m.getItem(ix);
  return(true);
}

  public boolean keyChar(char c, int status, int time) {
    if (c == Characters.ESCAPE) {
      fieldChanged(bfCancel, FieldChangeListener.PROGRAMMATIC);
      return true;
    } else if (c == Characters.ENTER) {
      fieldChanged(bfOk, FieldChangeListener.PROGRAMMATIC);
      return true;
    }
    return super.keyChar(c, status, time);
  }

  public String getText() {
    return (((TextField) efa[0]).getText());
  }

  public String getText(int index) {
    return (((TextField) efa[0]).getText());
  }

  public void fieldChanged(Field field, int i) {
    if (field.equals(bfOk)) rc = Dialog.OK;
    else if (field.equals(bfCancel)) rc = Dialog.CANCEL;
    close();
  }
}
