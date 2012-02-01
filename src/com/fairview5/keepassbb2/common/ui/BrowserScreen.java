package com.fairview5.keepassbb2.common.ui;

import java.io.IOException;
import java.util.Vector;

import javax.microedition.io.HttpConnection;

import net.rim.device.api.browser.field.BrowserContent;
import net.rim.device.api.browser.field.BrowserContentChangedEvent;
import net.rim.device.api.browser.field.BrowserContentManager;
import net.rim.device.api.browser.field.Event;
import net.rim.device.api.browser.field.RenderingApplication;
import net.rim.device.api.browser.field.RenderingOptions;
import net.rim.device.api.browser.field.RequestedResource;
import net.rim.device.api.browser.field.UrlRequestedEvent;
import net.rim.device.api.system.Display;
import net.rim.device.api.ui.Field;
import net.rim.device.api.ui.Manager;
import net.rim.device.api.ui.UiApplication;
import net.rim.device.api.ui.component.Dialog;
import net.rim.device.api.ui.container.MainScreen;

import com.fairview5.keepassbb2.common.io.Protocol;
import com.fairview5.keepassbb2.common.util.CommonUtils;

public class BrowserScreen extends MainScreen implements RenderingApplication {

	BrowserContentManager content;
	BrowserScreen me;
	HttpConnection wrapper;

	public static void showUrl(String url) {
		UiApplication.getUiApplication().pushScreen(new BrowserScreen(url));
	}
	
	
	public BrowserScreen(final String url) {

		try {
			me = this;
			content = new BrowserContentManager(
					Field.USE_ALL_HEIGHT | Field.USE_ALL_WIDTH | Manager.NO_SCROLL_RESET | Manager.VERTICAL_SCROLL | Manager.VERTICAL_SCROLLBAR);
			RenderingOptions renderingOptions = content.getRenderingSession().getRenderingOptions();
			renderingOptions.setProperty(RenderingOptions.CORE_OPTIONS_GUID, RenderingOptions.SHOW_IMAGES_IN_HTML, true);
			renderingOptions.setProperty(RenderingOptions.CORE_OPTIONS_GUID, RenderingOptions.SHOW_TABLES_IN_HTML, true);
			renderingOptions.setProperty(RenderingOptions.CORE_OPTIONS_GUID, RenderingOptions.ENABLE_CSS, true);
			renderingOptions.setProperty(RenderingOptions.CORE_OPTIONS_GUID, RenderingOptions.ENABLE_HTML, true);
			renderingOptions.setProperty(RenderingOptions.CORE_OPTIONS_GUID, RenderingOptions.ENABLE_WML, true);
			renderingOptions.setProperty(RenderingOptions.CORE_OPTIONS_GUID, RenderingOptions.ADD_LINK_ADDRESS_MENU_ITEM,
					true);
			renderingOptions.setProperty(RenderingOptions.CORE_OPTIONS_GUID, RenderingOptions.JAVASCRIPT_ENABLED, true);
			add(content);
			Thread thread = new Thread() {
				public void run() {
					try {
						wrapper = new Protocol(url);
						content.setContent(wrapper, me, null);
						SecondaryResourceFetchThread.doneAddingImages();
					} catch (Throwable e) {
						e.printStackTrace();
					}
				}
			};
			thread.start();
		} catch (Throwable e) {
			Dialog.alert("Unable to show help: " + e.getMessage());
		}

	}

	public Object eventOccurred(final Event event) {
		int eventId = event.getUID();

		switch (eventId) {
		case Event.EVENT_URL_REQUESTED: {
			final UrlRequestedEvent urlRequestedEvent = (UrlRequestedEvent) event;
			Thread thread = new Thread() {
				public void run() {
					try {
						if (wrapper != null)
							wrapper.close();
						wrapper = new Protocol(urlRequestedEvent.getURL());
						content.setContent(wrapper, me, event);
						SecondaryResourceFetchThread.doneAddingImages();
					} catch (Throwable e) {
						e.printStackTrace();
					}
				}
			};
			thread.start();
			break;

		}
		case Event.EVENT_BROWSER_CONTENT_CHANGED: {
			// Browser field title might have changed update title.
			BrowserContentChangedEvent browserContentChangedEvent = (BrowserContentChangedEvent) event;

			if (browserContentChangedEvent.getSource() instanceof BrowserContent) {
				BrowserContent browserField = (BrowserContent) browserContentChangedEvent.getSource();
				String newTitle = browserField.getTitle();
				if (newTitle != null) {
					synchronized (UiApplication.getUiApplication().getAppEventLock()) {
						setTitle(newTitle);
					}
				}
			}

			break;

		}
		case Event.EVENT_CLOSE:
			// TODO: close the appication
			break;

		case Event.EVENT_SET_HEADER: // No cache support.
		case Event.EVENT_SET_HTTP_COOKIE: // No cookie support.
		case Event.EVENT_HISTORY: // No history support.
		case Event.EVENT_EXECUTING_SCRIPT: // No progress bar is supported.
		case Event.EVENT_FULL_WINDOW: // No full window support.
		case Event.EVENT_STOP: // No stop loading support.
		default:
		}

		return null;
	}

	public int getAvailableHeight(BrowserContent browserContent) {
		return Display.getHeight();
	}

	public int getAvailableWidth(BrowserContent browserContent) {
		return Display.getWidth();
	}

	public String getHTTPCookie(String url) {
		// TODO Auto-generated method stub
		return null;
	}

	public int getHistoryPosition(BrowserContent browserContent) {
		// TODO Auto-generated method stub
		return 0;
	}

	public HttpConnection getResource(RequestedResource resource, BrowserContent referrer) {
		if (resource == null)
			return null;
		if (resource.isCacheOnly())
			return null;
		String url = resource.getUrl();
		if (url == null)
			return null;

		if (referrer == null) {
			HttpConnection connection = null;
			try {
				connection = new Protocol(resource.getUrl());
			} catch (Throwable e) {
				e.printStackTrace();
			}
			return connection;
		} else {
			CommonUtils.logger("SRFT "+resource.getUrl()+":"+referrer.getURL());
			SecondaryResourceFetchThread.enqueue(resource, referrer);
		}

		return null;
	}

	public void invokeRunnable(Runnable runnable) {
		(new Thread(runnable)).start();
	}

}

class SecondaryResourceFetchThread extends Thread {

	/**
	 * Callback browser field.
	 */
	private BrowserContent _browserField;

	/**
	 * Images to retrieve.
	 */
	private Vector _imageQueue;

	/**
	 * True is all images have been enqueued.
	 */
	private boolean _done;

	/**
	 * Sync object.
	 */
	private static Object _syncObject = new Object();

	/**
	 * Secondary thread.
	 */
	private static SecondaryResourceFetchThread _currentThread;

	/**
	 * Enqueues secondary resource for a browser field.
	 * 
	 * @param resource
	 *           - resource to retrieve.
	 * @param referrer
	 *           - call back browsr field.
	 */
	static void enqueue(RequestedResource resource, BrowserContent referrer) {
		if (resource == null) {
			return;
		}

		synchronized (_syncObject) {

			// Create new thread.
			if (_currentThread == null) {
				_currentThread = new SecondaryResourceFetchThread();
				_currentThread.start();
			} else {
				// If thread alread is running, check that we are adding images for
				// the same browser field.
				if (referrer != _currentThread._browserField) {
					synchronized (_currentThread._imageQueue) {
						// If the request is for a different browser field,
						// clear old elements.
						_currentThread._imageQueue.removeAllElements();
					}
				}
			}

			synchronized (_currentThread._imageQueue) {
				_currentThread._imageQueue.addElement(resource);
			}

			_currentThread._browserField = referrer;
		}
	}

	/**
	 * Constructor
	 * 
	 */
	private SecondaryResourceFetchThread() {
		_imageQueue = new Vector();
	}

	/**
	 * Indicate that all images have been enqueued for this browser field.
	 */
	static void doneAddingImages() {
		synchronized (_syncObject) {
			if (_currentThread != null) {
				_currentThread._done = true;
			}
		}
	}

	public void run() {
		while (true) {
			if (_done) {
				// Check if we are done requesting images.
				synchronized (_syncObject) {
					synchronized (_imageQueue) {
						if (_imageQueue.size() == 0) {
							_currentThread = null;
							break;
						}
					}
				}
			}

			RequestedResource resource = null;

			// Request next image.
			synchronized (_imageQueue) {
				if (_imageQueue.size() > 0) {
					resource = (RequestedResource) _imageQueue.elementAt(0);
					_imageQueue.removeElementAt(0);
				}
			}

			if (resource != null) {

				HttpConnection connection = null;
				try {
					connection = new Protocol(resource.getUrl());
				} catch (IOException e) {
					e.printStackTrace();
				}
				resource.setHttpConnection(connection);

				// Signal to the browser field that resource is ready.
				if (_browserField != null) {
					_browserField.resourceReady(resource);
				}
			}
		}
	}

}
