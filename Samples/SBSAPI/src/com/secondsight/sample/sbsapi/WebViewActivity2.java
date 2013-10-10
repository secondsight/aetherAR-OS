package com.secondsight.sample.sbsapi;

import android.app.Activity;
import android.os.Bundle;
import android.webkit.WebChromeClient;
import android.webkit.WebView;

public class WebViewActivity2 extends Activity {
	
	private static final String HTML = "<html><head><script type=\"text/javascript\">"
			+ "function updateTrackData() {"
//			+ "document.getElementById('content').innerHTML='azimuth: ' + 1.0 + '<br>inclination:' + 1.0;"
//			+ "document.getElementById('content').innerHTML='Hello';"
			+ "document.getElementById('content').innerHTML='azimuth: ' + window.SBS.getAzimuth() + '<br>inclination:' + window.SBS.getInclination();"
			+ "}"
			+ "</script></head><body>" 
			+ "<button onclick='javascript:updateTrackData();'>Update Head Tracking Data</button></p>"
			+ "<span id=\"content\"/>"
			+ "</body></html>";
	
	private WebView mWebView;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		mWebView = new WebView(this);
		mWebView.loadData(HTML, "text/html", "UTF-8");
		mWebView.setSbsInterfaceEnabled(true);
		setContentView(mWebView);

		mWebView.setWebChromeClient(new WebChromeClient());  
	}

	@Override
	protected void onPause() {
		mWebView.onPause();
		super.onPause();
	}

	@Override
	protected void onResume() {
		super.onResume();
		mWebView.onResume();
	}
	
	
}
