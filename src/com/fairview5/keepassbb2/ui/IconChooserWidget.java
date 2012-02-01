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


import net.rim.device.api.system.Bitmap;
import net.rim.device.api.system.Characters;
import net.rim.device.api.ui.*;
import net.rim.device.api.ui.component.*;
import net.rim.device.api.ui.container.HorizontalFieldManager;
import net.rim.device.api.ui.container.PopupScreen;


public class IconChooserWidget extends HorizontalFieldManager implements FieldChangeListener {
  ButtonField bfIcon;
  BitmapField bmfIcon;
  int initialIcon;
  int selectedIcon;
  Bitmap[] bitmaps;

  public IconChooserWidget(Bitmap[] ba, int initialIcon) {
    this.initialIcon = initialIcon;
    this.selectedIcon = initialIcon;
    this.bitmaps = ba;
    bfIcon = new ButtonField("Choose icon: ");
    bfIcon.setChangeListener(this);
    if (initialIcon < 0 || initialIcon >= TreeScreen.NUM_ICONS) initialIcon = 0;

    bmfIcon = new BitmapField(bitmaps[initialIcon]);
    bmfIcon.setSpace(5, 5);
    add(bfIcon);
    add(bmfIcon);

  }

  public void fieldChanged(Field field, int ix) {
    if (field.equals(bfIcon)) {
      IconChooser ic = new IconChooser(initialIcon);
      int rc = ic.doModal();
      if (rc != Dialog.OK) {
        bfIcon.setDirty(false);
        return;
      }
      int id = ic.selectedIcon;
      if (id != initialIcon) bfIcon.setDirty(true);
      selectedIcon = id;
      bmfIcon.setBitmap(bitmaps[selectedIcon]);
    }
  }

  public int getSelectedIcon() {
    return(selectedIcon);
  }

  class IconChooser extends PopupScreen {
    int selectedIcon;
    int rc = Dialog.CANCEL;
    IconChooser(int initialIcon) {
      super(new HorizontalFieldManager(Manager.HORIZONTAL_SCROLL | Manager.HORIZONTAL_SCROLLBAR));
      for (int i = 0; i < bitmaps.length; i++) {
        BitmapField bf = new BitmapField(bitmaps[i], Field.FOCUSABLE);
        bf.setSpace(5, 5);
        add(bf);
      }
      getField(initialIcon).setFocus();
    }

    public int doModal() {
      UiApplication.getUiApplication().pushModalScreen(this);
      return (rc);
    }

    public boolean keyChar(char c, int status, int time) {
      if (c == Characters.ENTER || c == Characters.SPACE) {
        selectedIcon = this.getFieldWithFocusIndex();
        rc = Dialog.OK;
        UiApplication.getUiApplication().popScreen(this);
        return(true);
      }
      if (c == Characters.ESCAPE) {
         selectedIcon = this.getFieldWithFocusIndex();
         rc = Dialog.CANCEL;
         UiApplication.getUiApplication().popScreen(this);
         return(true);
       }
      return super.keyChar(c, status, time);
    }

    public boolean navigationClick(int statis, int time) {
      selectedIcon = this.getFieldWithFocusIndex();
      rc = Dialog.OK;
      close();
      return(true);
    }

  }

}
