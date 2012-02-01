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
import net.rim.device.api.ui.UiApplication;
import net.rim.device.api.ui.component.LabelField;
import net.rim.device.api.ui.container.PopupScreen;
import net.rim.device.api.ui.container.VerticalFieldManager;

public final class StatusDialog extends PopupScreen {

  StatusDialog sd;

  public StatusDialog(String legend) {
    super(new VerticalFieldManager());
    if (legend != null) add(new LabelField(legend));
    sd = this;
  }

  public StatusDialog() {
    super(new VerticalFieldManager());
    sd = this;
  }

  public void close() {
    UiApplication.getUiApplication().invokeLater(new Runnable() {
      public void run() {
        UiApplication.getUiApplication().popScreen(sd);
      }
    });
  }

  public boolean keyChar(char c, int status, int time) {
    if (c == Characters.ESCAPE) {
      close();
      return true;
    }
    return super.keyChar(c, status, time);

  }


}

