package com.example.test3.ui.home;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import com.example.test3.R;

public class HomeFragment extends Fragment {

    private WebView webView;

    public View onCreateView(@NonNull LayoutInflater inflater,
            ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_home, container, false);

        webView = root.findViewById(R.id.webview);
        setupWebView();

        return root;
    }

    private void setupWebView() {
        // 启用 JavaScript
        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setDomStorageEnabled(true);
        webSettings.setUseWideViewPort(true);
        webSettings.setLoadWithOverviewMode(true);
        webSettings.setMediaPlaybackRequiresUserGesture(false);

        // 设置 User Agent
        String userAgent = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/137.0.0.0 Safari/537.36 Edg/137.0.0.0";
        webSettings.setUserAgentString(userAgent);

        // 使用自定义 WebViewClient
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                // 注入JavaScript来修改页面
                injectCustomCSS();
                hideNonVideoElements();
            }
        });

        // 设置 WebChromeClient 以支持视频播放
        webView.setWebChromeClient(new WebChromeClient());

        // 加载抖音视频页面
        webView.loadUrl("https://www.douyin.com");
    }

    private void injectCustomCSS() {
        String css = "" +
        // 隐藏顶部导航栏
                ".header-container { display: none !important; }" +
                // 隐藏侧边栏
                ".left-container { display: none !important; }" +
                // 隐藏右侧推荐栏
                ".right-container { display: none !important; }" +
                // 隐藏底部栏
                ".footer-container { display: none !important; }" +
                // 让视频容器占满整个屏幕
                ".main-container { width: 100% !important; padding: 0 !important; }" +
                ".video-container { width: 100% !important; margin: 0 !important; }";

        String script = "var style = document.createElement('style');" +
                "style.type = 'text/css';" +
                "style.innerHTML = '" + css + "';" +
                "document.head.appendChild(style);";

        webView.evaluateJavascript(script, null);
    }

    private void hideNonVideoElements() {
        String script = "" +
                "function hideElements() {" +
                "  // 隐藏非视频元素" +
                "  var nonVideoElements = document.querySelectorAll(':not(.video-feed):not(.video-container):not(.video-player)');"
                +
                "  nonVideoElements.forEach(function(element) {" +
                "    if (!element.closest('.video-feed') && !element.closest('.video-container') && !element.closest('.video-player')) {"
                +
                "      element.style.display = 'none';" +
                "    }" +
                "  });" +
                "}" +
                "hideElements();" +
                // 监听动态加载的内容
                "var observer = new MutationObserver(hideElements);" +
                "observer.observe(document.body, { childList: true, subtree: true });";

        webView.evaluateJavascript(script, null);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (webView != null) {
            webView.destroy();
        }
    }
}