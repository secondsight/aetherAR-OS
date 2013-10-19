package com.secondsight.sample.sbsapi;

import android.app.Activity;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.LinearLayout;

public class WebViewActivity3 extends Activity {
	
	private static final String HTML = "<html><body>" 
			+ "<button onclick='javascript:window.SBS.disableSBS();'>Disable SBS</button></p>"
			+ "<button onclick='javascript:window.SBS.enableSBS();'>Enable SBS</button></p>"
			+ "<span><b>Note: </b>SBS will be re-enabled automatically after calling loadData() or loadUrl() with another domain.</span>"
			+ "</p></body></html>";
	
	private WebView mWebView;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		mWebView = new WebView(this);
		mWebView.loadDataWithBaseURL("http://www.testsite1.com", HTML, "text/html", "UTF-8", "http://www.testsite1.com");

		try {
		    mWebView.setSbsInterfaceEnabled(true);
		} catch (Throwable e){}
		
		
		Button btn = new Button(this);
		btn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {   
        		mWebView.loadDataWithBaseURL("http://www.testsite2.com", HTML, "text/html", "UTF-8", "http://www.testsite2.com");          
            }		    
		});
		btn.setText("Load test site 2");
		
		LinearLayout layout = new LinearLayout(this);
		layout.setOrientation(LinearLayout.VERTICAL);
		layout.addView(mWebView);
		layout.addView(btn);
		
		setContentView(layout);

		mWebView.setWebChromeClient(new WebChromeClient());  
		
		
		Uri uri = Uri.parse("https://xsxa/sda");
		if (uri.isHierarchical()) {
			Log.d("Alfred", uri.getScheme());
		}
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
