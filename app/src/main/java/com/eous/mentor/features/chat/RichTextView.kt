package com.eous.mentor.features.chat

import android.annotation.SuppressLint
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun RichTextView(
    text: String,
    modifier: Modifier = Modifier,
    textColor: String = "#1E293B",
    fontSize: String = "15px",
    alignment: String = "left"
) {
    var webViewHeight by remember { mutableStateOf(40) } // Start with a reasonable minimum height

    // Convert text to Base64 to prevent any quoting/escaping/Unicode parsing issues in JS
    val base64Text = remember(text) {
        try {
            android.util.Base64.encodeToString(
                text.toByteArray(Charsets.UTF_8),
                android.util.Base64.NO_WRAP
            )
        } catch (e: Exception) {
            ""
        }
    }

    val htmlContent = remember(base64Text, textColor, fontSize, alignment) {
        """
        <!DOCTYPE html>
        <html>
        <head>
          <meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no">
          <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/katex@0.16.8/dist/katex.min.css">
          <script src="https://cdn.jsdelivr.net/npm/katex@0.16.8/dist/katex.min.js"></script>
          <script src="https://cdn.jsdelivr.net/npm/katex@0.16.8/dist/contrib/auto-render.min.js"></script>
          <script src="https://cdn.jsdelivr.net/npm/marked@4.3.0/marked.min.js"></script>
          <style>
            html, body {
              margin: 0;
              padding: 0;
              background-color: transparent;
            }
            body {
              font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, Helvetica, Arial, sans-serif;
              font-size: $fontSize;
              line-height: 1.6;
              color: $textColor;
              word-wrap: break-word;
              text-align: $alignment;
              -webkit-user-select: none;
              user-select: none;
            }
            p {
              margin: 0 0 8px 0;
            }
            p:last-child {
              margin-bottom: 0;
            }
            ul, ol {
              margin: 0 0 8px 0;
              padding-left: 20px;
            }
            li {
              margin-bottom: 4px;
            }
            h1, h2, h3, h4, h5, h6 {
              margin: 12px 0 6px 0;
              font-weight: bold;
              color: #0f172a;
            }
            strong {
              font-weight: 700;
              color: #0f172a;
            }
            .katex-display {
              margin: 0.8em 0;
              overflow-x: auto;
              overflow-y: hidden;
            }
          </style>
        </head>
        <body>
          <div id="content" style="padding: 2px; box-sizing: border-box;"></div>
          <!-- Spacer to guarantee bottom padding for scrollHeight measurement -->
          <div style="height: 16px; clear: both;"></div>
          <script>
            function decodeBase64Utf8(base64) {
              const binaryString = atob(base64);
              const len = binaryString.length;
              const bytes = new Uint8Array(len);
              for (let i = 0; i < len; i++) {
                bytes[i] = binaryString.charCodeAt(i);
              }
              return new TextDecoder("utf-8").decode(bytes);
            }

            function sendHeight() {
              const body = document.body;
              if (body) {
                const height = body.scrollHeight || body.offsetHeight;
                if (window.Android) {
                  window.Android.onHeightReceived(height);
                }
              }
            }

            document.addEventListener("DOMContentLoaded", function() {
              try {
                marked.setOptions({
                  breaks: true,
                  gfm: true
                });

                let rawText = decodeBase64Utf8("$base64Text");
                
                // Preprocess: ensure bullet points and numbered list items have double newlines before them
                rawText = rawText.replace(/([^\n])\n(\s*[\*\-\+]\s)/g, "$1\n\n$2");
                rawText = rawText.replace(/([^\n])\n(\s*\d+\.\s)/g, "$1\n\n$2");

                const html = marked.parse(rawText);
                document.getElementById('content').innerHTML = html;
                
                renderMathInElement(document.body, {
                  delimiters: [
                    {left: "$$", right: "$$", display: true},
                    {left: "$", right: "$", display: false},
                    {left: "\\(", right: "\\)", display: false},
                    {left: "\\[", right: "\\]", display: true}
                  ],
                  throwOnError: false
                });
              } catch (e) {
                document.getElementById('content').innerText = e.message;
              }
              
              sendHeight();
              setTimeout(sendHeight, 50);
              setTimeout(sendHeight, 150);
              new ResizeObserver(sendHeight).observe(document.body);
            });

            window.addEventListener("load", sendHeight);

            // Safety interval to handle deferred/late CDN rendering
            let pollCount = 0;
            const pollInterval = setInterval(function() {
              sendHeight();
              pollCount++;
              if (pollCount >= 10) {
                clearInterval(pollInterval);
              }
            }, 100);
          </script>
        </body>
        </html>
        """.trimIndent()
    }

    AndroidView(
        factory = { context ->
            WebView(context).apply {
                settings.javaScriptEnabled = true
                settings.domStorageEnabled = true
                setBackgroundColor(android.graphics.Color.TRANSPARENT)
                
                addJavascriptInterface(object {
                    @android.webkit.JavascriptInterface
                    fun onHeightReceived(height: Int) {
                        post {
                            val density = context.resources.displayMetrics.density
                            // Convert height to dp and add safety padding of 8dp to prevent fractional clip-off
                            val dpHeight = (height / density).toInt().coerceAtLeast(40) + 8
                            if (webViewHeight != dpHeight) {
                                webViewHeight = dpHeight
                            }
                        }
                    }
                }, "Android")

                isVerticalScrollBarEnabled = false
                isHorizontalScrollBarEnabled = false
                settings.useWideViewPort = false
                settings.loadWithOverviewMode = false

                webViewClient = object : WebViewClient() {
                    override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
                        return true // Do not load external links inside our app
                    }
                }
            }
        },
        update = { webView ->
            // Prevent reloading loop when only height changes
            if (webView.tag != htmlContent) {
                webView.tag = htmlContent
                webView.loadDataWithBaseURL("https://localhost", htmlContent, "text/html", "UTF-8", null)
            }
        },
        modifier = modifier
            .fillMaxWidth()
            .height(webViewHeight.dp)
    )
}
