package com.secondsight.sample.sbsapi;

import android.app.Activity;
import android.os.Bundle;
import android.webkit.WebView;

public class WebViewActivity1 extends Activity {
	
	private static final String HTML = "<html><head><script type=\"text/javascript\">"
			+ "function onTrackDataChanged(azimuth, inclination) {"
			+ "document.getElementById('content').innerHTML=\"azimuth: \" + azimuth + \"<br>inclination:\" + inclination;"
			+ "}"
			+ "</script></head><body>" 
			+ "<span id=\"content\">Data will be updated automatically in landscape mode</span>"
			+ "</body></html>";
	
	private WebView mWebView;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		mWebView = new WebView(this);
		mWebView.loadData(HTML, "text/html", "UTF-8");
		mWebView.setSbsJSCallback("onTrackDataChanged");
		setContentView(mWebView);
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
