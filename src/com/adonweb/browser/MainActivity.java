package com.adonweb.browser;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import android.os.AsyncTask;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TabHost;
import android.widget.TabHost.TabSpec;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.graphics.Bitmap;

public class MainActivity extends Activity implements View.OnClickListener, View.OnKeyListener {
  
  private EditText _url;
  private EditText _output;
  private WebView _webView;
  private TabHost _tabhost;
  private ProgressBar _progress;
  private Button _go;
  
  AsyncTask<Void, Void, Void> _task = null;
  
  private WebViewClient _wvclient = new WebViewClient() {
    
    @Override
    public void onPageStarted (WebView view, String url, Bitmap favicon) {

      if (null!=_progress) {
        _progress.setVisibility(View.VISIBLE);
      }
      
      if (null!=_go) {
        _go.setVisibility(View.INVISIBLE);
        _go.setEnabled(false);
      }

      if (null!=_output) {
        _output.setText("Loading: "+url+" ...");
      }
      
      final String __url = url;
      
      // Kill if thread started
      if (null!=_task) {
        _task.cancel(true);
      }
      
      // Run as async
      _task = new AsyncTask<Void, Void, Void>() {
        @Override
        protected Void doInBackground(Void... params) {
          String headers = "";
          String httpContent = "";
          HttpGet httpGet = new HttpGet(__url);
          
          HttpClient httpClient = new DefaultHttpClient();
          httpClient.getParams().setParameter("http.protocol.version", HttpVersion.HTTP_1_1);
          httpClient.getParams().setParameter("http.socket.timeout", Integer.valueOf(45000));
          httpClient.getParams().setParameter("http.useragent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_8_4)"+
                                                                " AppleWebKit/537.36 (KHTML, like Gecko) Chrome/27.0.1453.116 Safari/537.36");

          try {
            HttpResponse response = httpClient.execute(httpGet);
            httpContent = inputStreamToString(response.getEntity().getContent()).toString();

            // Headers to string
            headers = "Status: "+response.getStatusLine().getStatusCode()+"\n";
            for (Header h : response.getAllHeaders()) {
              headers += h.getName()+": "+h.getValue()+"\n";
            }
          } catch (Exception e) {
            e.printStackTrace();
            httpContent = e.getMessage();
          }
          
          // Set result
          final String _content = headers+"\n\n"+httpContent;
          runOnUiThread(new Runnable() {
            @Override
            public void run() {
              if (null!=_output) {
                _output.setText(_content);
              }
            }
          });

          return null;
        }
      };
      _task.execute();

      // Set current url
      if (null!=_url) {
        _url.setText(url);
      }

      // Call super method
      super.onPageStarted(view, url, favicon);
    }
    
    @Override
    public void onPageFinished(WebView view, String url) {
      
      if (null!=_progress) {
        _progress.setVisibility(View.GONE);
      }
      
      if (null!=_go) {
        _go.setVisibility(View.VISIBLE);
        _go.setEnabled(true);
      }

      super.onPageFinished(view, url);
    }
    
    private StringBuilder inputStreamToString(InputStream is) throws IOException {
      String line = "";
      StringBuilder total = new StringBuilder();
      
      // Wrap a BufferedReader around the InputStream
      BufferedReader rd = new BufferedReader(new InputStreamReader(is));

      // Read response until the end
      while ((line = rd.readLine()) != null) { 
          total.append(line); 
      }
      
      // Return full string
      return total;
  }
  };

  @SuppressLint("SetJavaScriptEnabled")
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    
    _url = (EditText)findViewById(R.id.url);
    _url.setOnKeyListener(this);
    
    _output = (EditText)findViewById(R.id.textOutput);
    _progress = (ProgressBar)findViewById(R.id.progress);
    
    _go = (Button)findViewById(R.id.button_go);
    _go.setOnClickListener(this);
    
    // Init tabhost
    _tabhost = (TabHost)findViewById(R.id.tabHost);
    _tabhost.setup();

    TabSpec spec = _tabhost.newTabSpec("TAB 1");
    spec.setIndicator(getResources().getString(R.string.tab_address));
    spec.setContent(R.id.tab1);
    _tabhost.addTab(spec);

    spec = _tabhost.newTabSpec("TAB 2");
    spec.setIndicator(getResources().getString(R.string.tab_webview));
    spec.setContent(R.id.tab2);
    _tabhost.addTab(spec);
    
    // Init web view
    _webView = (WebView)findViewById(R.id.webView);
    _webView.getSettings().setJavaScriptEnabled(true);
    _webView.getSettings().setAppCacheEnabled(false);
    _webView.setWebViewClient(_wvclient);

//    getWindow().requestFeature(Window.FEATURE_PROGRESS);
//    final Activity activity = this;
//    _webView.setWebChromeClient(new WebChromeClient() {
//      public void onProgressChanged(WebView view, int progress) {
//        // Activities and WebViews measure progress with different scales.
//        // The progress meter will automatically disappear when we reach 100%
//        activity.setProgress(progress * 1000);
//      }
//    });
  }

  @Override
  public void onClick(View v) {
    String url = _url.getText().toString();
    if (!url.startsWith("http://") && !url.startsWith("https://") && !url.startsWith("ftp://")) {
      url = "http://"+url;
    }
    _webView.loadUrl(url);
  }

  @Override
  public boolean onKey(View v, int keyCode, KeyEvent event) {
    if (KeyEvent.ACTION_UP == event.getAction() && KeyEvent.KEYCODE_ENTER == keyCode) {
      onClick(v);
      return true;
    }
    return false;
  }
  
}
