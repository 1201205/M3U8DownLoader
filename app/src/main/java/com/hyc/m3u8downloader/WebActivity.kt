package com.hyc.m3u8downloader

import android.annotation.SuppressLint
import android.os.Build
import android.os.Bundle
import android.support.annotation.RequiresApi
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.KeyEvent
import android.webkit.*
import java.net.URL
import java.net.URLConnection
import java.util.ArrayList

class WebActivity : AppCompatActivity() {
    lateinit var webView: WebView
    val paraser=UrlParaser()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.actiivty_web)
        webView = findViewById<WebView>(R.id.wb_content)
        init()
        paraser.start()
    }

    override fun onBackPressed() {
        if (webView.canGoBack()) {
            webView.goBack()
        } else {
            super.onBackPressed()
        }
    }
    val list = ArrayList<String>()
    @SuppressLint("SetJavaScriptEnabled")
    private fun init() {
        webView.webViewClient = object : WebViewClient() {
            override fun onLoadResource(view: WebView?, url: String?) {
                Log.e("hyc-iii", url)
                paraser.addUrl(url!!)
                super.onLoadResource(view, url)
            }
        }
//        webView.webViewClient=Client()

        var ws = webView.settings
        ws.setJavaScriptEnabled(true);
        ws.setAllowFileAccess(true);
        ws.setDatabaseEnabled(true);
        ws.setDomStorageEnabled(true);
        ws.setSaveFormData(false);
        ws.setAppCacheEnabled(false);
        ws.setCacheMode(WebSettings.LOAD_DEFAULT);
        ws.setLoadWithOverviewMode(false);//<==== 一定要设置为false，不然有声音没图像
        ws.setUseWideViewPort(true);
        //javascriptInterface = new JavascriptInterface();
        //mWebView.addJavascriptInterface(javascriptInterface, "java2js_laole918");

        webView.loadUrl("https://youku.com")

    }

    class Client: WebViewClient() {

        override fun onLoadResource(view: WebView?, url: String?) {
            Log.e("hyc-iii",url)
            super.onLoadResource(view, url)
        }
//        override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
//            return super.shouldOverrideUrlLoading(view, request)
//        }
//
//        override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
//            return super.shouldOverrideUrlLoading(view, url)
//        }
//
//        override fun shouldInterceptRequest(view: WebView?, url: String?): WebResourceResponse {
//            return super.shouldInterceptRequest(view, url)
//        }
//        override fun shouldOverrideKeyEvent(view: WebView?, event: KeyEvent?): Boolean {
//            return super.shouldOverrideKeyEvent(view, event)
//        }
//        @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
//        override fun shouldInterceptRequest(view: WebView?, request: WebResourceRequest?): WebResourceResponse {
//            Log.e("hyc-iii",request!!.url!!.toString())
////            return getResponse(request!!.url!!.toString(),view)
//            return super.shouldInterceptRequest(view, request)
//        }
//
//        override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
//            return true
//        }
//        override fun shouldInterceptRequest(view: WebView?, url: String?): WebResourceResponse {
//            Log.e("hyc-iii",url)
//return super.shouldInterceptRequest(view, url)
////            return getResponse(url!!,view)
//        }
        private fun getResponse(url: String,view: WebView?):WebResourceResponse{
            val url2=URL(url)
            val connect= url2.openConnection() ?: return  super.shouldInterceptRequest(view,url)
            return try {
                WebResourceResponse(connect.contentType,connect.getHeaderField("encoding"),connect.getInputStream())
            }catch (e:Exception){
                WebResourceResponse(null,null,null)
            }
        }

    }
}