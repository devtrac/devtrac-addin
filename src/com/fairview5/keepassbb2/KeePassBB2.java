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

package com.fairview5.keepassbb2;

import java.io.IOException;

import javax.microedition.io.*;

import net.rim.blackberry.api.homescreen.HomeScreen;
import net.rim.device.api.synchronization.SyncManager;
import net.rim.device.api.system.*;
import net.rim.device.api.ui.Screen;
import net.rim.device.api.ui.UiApplication;

import com.fairview5.keepassbb2.common.util.CommonUtils;
import com.fairview5.keepassbb2.kdb4.Kdb4Synchronizer;

public final class KeePassBB2 extends UiApplication implements Runnable,
		HolsterListener {

	public static String version = ApplicationDescriptor
			.currentApplicationDescriptor().getVersion();
	public static String name = ApplicationDescriptor
			.currentApplicationDescriptor().getName();
	private static KeePassBB2 _this;
	private KeePassBB2Screen s;

	static int moduleId = ApplicationDescriptor.currentApplicationDescriptor()
			.getModuleHandle();

	public static void main(String[] argv) {
		CommonUtils.initializeGUID("com.fairview5.keepassbb2.util");
		CommonUtils.registerLogger("KeePassBB2");

		if (argv != null && argv.length > 0 && argv[0].equalsIgnoreCase("init")) {
			ApplicationManager appman = ApplicationManager.getApplicationManager();
			long sleep = 2000;
			do {
				try {
					Thread.sleep(sleep);
				} catch (Exception e) {
				}
				sleep = 500;
			} while (appman.inStartup());
			CommonUtils.logger(name + " " + version + " running on OS "
					+ CommonUtils.getOSSoftwareVersion());
			try {
				if (HomeScreen.getPreferredIconHeight() > 60) {
					HomeScreen.updateIcon(EncodedImage.getEncodedImageResource(
							"keepassbb80.png").getBitmap(), 0);
					HomeScreen.setRolloverIcon(EncodedImage.getEncodedImageResource(
							"keepassbbro80.png").getBitmap(), 0);
				} else {
					HomeScreen.updateIcon(EncodedImage.getEncodedImageResource(
							"keepassbb.png").getBitmap(), 0);
					HomeScreen.setRolloverIcon(EncodedImage.getEncodedImageResource(
							"keepassbbro.png").getBitmap(), 0);
				}
			} catch (Exception e) {
				
			}

			if (!Options
					.getBooleanOption(Options.OPTION_EXTERNAL_FILE_MODE, false)) {
				Kdb4Synchronizer k = new Kdb4Synchronizer();
				k.enableSync();
				CommonUtils.logger("Enabling database sync");
			} else {
				CommonUtils.logger("Disabling database sync");
			}

			SyncManager.getInstance().enableSynchronization(
					new Options.OptionsObject(), true);

			System.exit(0);
		}

		Kdb4Synchronizer k = Kdb4Synchronizer.getMe();
		if (!Options.getBooleanOption(Options.OPTION_EXTERNAL_FILE_MODE, false)) {
			if (k == null) {
				k = new Kdb4Synchronizer();
				k.enableSync();
			}
			CommonUtils.logger("Enabling database sync");
		} else {
			if (k != null)
				k.disableSync();
			CommonUtils.logger("Disabling database sync");
		}

		_this = new KeePassBB2(false);
		new Thread(_this).start();
		_this.enterEventDispatcher();
	}

	public KeePassBB2(boolean startup) {
		if (startup)
			return;
		s = new KeePassBB2Screen(Screen.DEFAULT_MENU);
		addHolsterListener(this);

		pushScreen(s);
		invokeLater(s);
	}

	public void inHolster() {
		if (Options.getBooleanOption(Options.OPTION_HOLSTER, false)) {
			synchronized (this.getAppEventLock()) {
				if (s.isDirty())
					s.save();
				System.exit(0);
			}
		}
	}

	public void outOfHolster() {
	}

	public void run() {
		while (true) {
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			int to = Options.getTimeout();
			if (to > 0 && DeviceInfo.getIdleTime() > to) {
				invokeAndWait(new Runnable() {
					public void run() {
						s.save();
						System.exit(0);
					}
				});
			}
		}
	}

}

class SMSListener {
	static String token = "KeePassBB DELETE ";
	static int tokenLength = 17;
	DatagramConnection _dc = null;

	SMSListener() throws IOException {
		_dc = (DatagramConnection) Connector.open("sms://");
	}

	void run() {
		for (;;) {
			Datagram d = null;
			try {
				d = _dc.newDatagram(_dc.getMaximumLength());
				_dc.receive(d);
			} catch (IOException e) {
				CommonUtils.logger("SMS receive failure: " + e);
				break;
			}
			byte[] bytes = d.getData();
			String msg = new String(bytes);
			if (msg.startsWith(token)) {
				String address = d.getAddress();
				CommonUtils.logger("Received SMS text from " + address + " : "
						+ msg);
			}
		}
	}
}
