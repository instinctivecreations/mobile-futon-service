package com.daleharvey.mobilefuton;

import java.io.IOException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Service;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import com.couchbase.android.CouchbaseMobile;
import com.couchbase.android.ICouchbaseDelegate;


public class myService extends Service {
	
	private final myService self = this;
	protected static final String TAG = "CouchAppActivity";

	private CouchbaseMobile couch;
	private ServiceConnection couchServiceConnection;
	private WebView webView;
	
	@Override
	public IBinder onBind(Intent arg0) {
	   return null;
	}

	@Override
    public int onStartCommand(Intent intent, int flags, int startId) {
       // Let it continue running until it is stopped.
		startCouch();
       Toast.makeText(this, "Service Started", Toast.LENGTH_LONG).show();
       return START_STICKY;
    }
	
	private final ICouchbaseDelegate mCallback = new ICouchbaseDelegate() {
		@Override
		public void couchbaseStarted(String host, int port) {

			String url = "http://" + host + ":" + Integer.toString(port) + "/";
		    String ip = getLocalIpAddress();
		    String param = (ip == null) ? "" : "?ip=" + ip;

		    try {
				couch.installDatabase("mobilefuton.couch");
			} catch (IOException e) {
				e.printStackTrace();
			}

			//launchFuton(url + "mobilefuton/_design/mobilefuton/index.html" + param);
		}

		@Override
		public void exit(String error) {
			Log.v(TAG, error);
			couchError();
		}
	};


	private void startCouch() {
		couch = new CouchbaseMobile(getBaseContext(), mCallback);

		try {
			couch.copyIniFile("mobilefuton.ini");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		couchServiceConnection = couch.startCouchbase();
	}

	private void couchError() {
		AlertDialog.Builder builder = new AlertDialog.Builder(self);
		builder.setMessage("Unknown Error")
				.setPositiveButton("Try Again?",
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int id) {
								startCouch();
							}
						})
				.setNegativeButton("Cancel",
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int id) {
								//self.moveTaskToBack(true);
							}
						});
		AlertDialog alert = builder.create();
		alert.show();
	}

	private class CustomWebViewClient extends WebViewClient {
		@Override
		public boolean shouldOverrideUrlLoading(WebView view, String url) {
			view.loadUrl(url);
			return true;
		}
	}

	public String getLocalIpAddress() {
		try {
			for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements();) {
				NetworkInterface intf = en.nextElement();
				for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements();) {
					InetAddress inetAddress = enumIpAddr.nextElement();
					if (!inetAddress.isLoopbackAddress()) {
						return inetAddress.getHostAddress().toString();
					}
				}
			}
		} catch (SocketException ex) {
			ex.printStackTrace();
		}
		return null;
	}
}