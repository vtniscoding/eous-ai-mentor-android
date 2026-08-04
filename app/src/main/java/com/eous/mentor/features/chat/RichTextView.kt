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
    alignment: String = "left",
    isCompact: Boolean = false
) {
    var webViewHeight by remember { mutableStateOf(if (isCompact) 20 else 40) } // Start with a reasonable minimum height

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

    val htmlContent = remember(base64Text, textColor, fontSize, alignment, isCompact) {
        val bottomMargin = if (isCompact) "0px" else "10px"
        val contentPadding = if (isCompact) "0px" else "2px 2px 16px 2px"
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
            *, *:before, *:after {
              box-sizing: border-box;
            }
            html, body {
              margin: 0;
              padding: 0;
              background-color: transparent;
              width: 100%;
            }
            body {
              font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, Helvetica, Arial, sans-serif;
              font-size: $fontSize;
              line-height: 1.4;
              color: $textColor;
              word-wrap: break-word;
              overflow-wrap: break-word;
              text-align: $alignment;
              -webkit-user-select: none;
              user-select: none;
            }
            p {
              margin: 0 0 $bottomMargin 0;
            }
            p:last-child {
              margin-bottom: 0;
            }
            ul, ol {
              margin: 0 0 10px 0;
              padding-left: 22px;
            }
            li {
              margin-bottom: 6px;
            }
            h1, h2, h3, h4, h5, h6 {
              margin: 14px 0 8px 0;
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
              padding: 4px 0;
            }
            .conclusion-box {
              margin-top: 14px;
              padding: 14px 16px;
              background-color: #F0FDF4;
              border: 1.5px dashed #10B981;
              border-radius: 12px;
              color: #047857;
              font-weight: 600;
            }
            .conclusion-title {
              color: #10B981;
              font-size: 13px;
              font-weight: 700;
              text-transform: uppercase;
              letter-spacing: 0.5px;
              margin-bottom: 4px;
            }
          </style>
        </head>
        <body>
          <div id="content" style="padding: $contentPadding;"></div>
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
              const contentEl = document.getElementById('content');
              if (contentEl) {
                const height = Math.max(
                  document.documentElement.offsetHeight,
                  document.body.offsetHeight,
                  contentEl.getBoundingClientRect().height
                );
                if (window.Android && height > 0) {
                  window.Android.onHeightReceived(Math.ceil(height));
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

                // Highlight Conclusion block gracefully
                rawText = rawText.replace(
                  /(?:^|\n)(?:\*\*)?(Conclusion:|Final conclusion:|Final answer:|Summary:)(?:\*\*)?\s*([\s\S]+?)$/i,
                  '<div class="conclusion-box"><div class="conclusion-title">$1</div>$2</div>'
                );

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
              requestAnimationFrame(sendHeight);
              setTimeout(sendHeight, 100);
              setTimeout(sendHeight, 300);
            });
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
                
                // Hardware acceleration for ultra-smooth rendering
                setLayerType(android.view.View.LAYER_TYPE_HARDWARE, null)
                
                addJavascriptInterface(object {
                    @android.webkit.JavascriptInterface
                    fun onHeightReceived(height: Int) {
                        post {
                            val density = context.resources.displayMetrics.density
                            val dpHeight = if (isCompact) {
                                (height / density).toInt().coerceAtLeast(18) + 4
                            } else {
                                (height / density).toInt().coerceAtLeast(60) + 28
                            }
                            if (Math.abs(webViewHeight - dpHeight) > 3) {
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

                    override fun onPageFinished(view: WebView?, url: String?) {
                        super.onPageFinished(view, url)
                        view?.evaluateJavascript("sendHeight();", null)
                    }
                }
            }
        },
        update = { webView ->
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

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun RichChatThreadView(
    qaPairs: List<Pair<com.eous.mentor.domain.model.ChatMessage, com.eous.mentor.domain.model.ChatMessage?>>,
    isThinking: Boolean,
    inputText: String,
    pendingImageUrl: String?,
    modifier: Modifier = Modifier,
    onSupportChipClicked: (String) -> Unit = {},
    onNavigateToQuizzes: () -> Unit = {}
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val mascotBase64 = remember {
        val drawable = androidx.core.content.ContextCompat.getDrawable(context, com.eous.mentor.R.drawable.ic_aianswer)
        if (drawable != null) {
            val bitmap = android.graphics.Bitmap.createBitmap(
                drawable.intrinsicWidth.takeIf { it > 0 } ?: 150,
                drawable.intrinsicHeight.takeIf { it > 0 } ?: 150,
                android.graphics.Bitmap.Config.ARGB_8888
            )
            val canvas = android.graphics.Canvas(bitmap)
            drawable.setBounds(0, 0, canvas.width, canvas.height)
            drawable.draw(canvas)
            val outputStream = java.io.ByteArrayOutputStream()
            bitmap.compress(android.graphics.Bitmap.CompressFormat.PNG, 100, outputStream)
            android.util.Base64.encodeToString(outputStream.toByteArray(), android.util.Base64.NO_WRAP)
        } else {
            ""
        }
    }

    val chatDataJson = remember(qaPairs, isThinking, inputText, pendingImageUrl) {
        val array = org.json.JSONArray()
        qaPairs.forEachIndexed { index, pair ->
            val isLast = index == qaPairs.lastIndex
            val isPairThinking = isLast && pair.second == null && isThinking

            val item = org.json.JSONObject()
            item.put("question", pair.first.content)
            item.put("questionImage", pair.first.image ?: "")

            if (isPairThinking) {
                item.put("isThinking", true)
            } else if (pair.second != null) {
                val parsed = AnswerParser.parse(pair.second!!.content, pair.second!!.subject)
                item.put("answer", parsed.explanation)
                item.put("quizId", pair.second!!.quiz_id ?: "")
                if (isLast && !isThinking) {
                    item.put("showSupportChips", true)
                }
            }
            array.put(item)
        }

        if (qaPairs.isEmpty() && isThinking) {
            val item = org.json.JSONObject()
            item.put("question", inputText)
            item.put("questionImage", pendingImageUrl ?: "")
            item.put("isThinking", true)
            array.put(item)
        }

        android.util.Base64.encodeToString(
            array.toString().toByteArray(Charsets.UTF_8),
            android.util.Base64.NO_WRAP
        )
    }

    val htmlContent = remember(chatDataJson) {
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
            *, *:before, *:after { box-sizing: border-box; }
            html, body {
              margin: 0; padding: 0;
              background-color: transparent;
              width: 100%; height: 100%;
              font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, sans-serif;
              -webkit-user-select: none; user-select: none;
            }
            #container {
              padding: 24px 24px 120px 24px;
            }
            .question-wrapper {
              position: relative;
              margin-top: 32px;
            }
            .question-card {
              background-color: #F5F3FF;
              border: 1px solid #DDD6FE;
              border-radius: 22px;
              padding: 16px;
              position: relative;
              z-index: 2;
            }
            .question-mascot {
              position: absolute;
              top: -36px;
              right: 24px;
              width: 56px;
              height: 52px;
              background-image: url('data:image/png;base64,$mascotBase64');
              background-size: contain;
              background-repeat: no-repeat;
              background-position: center bottom;
              z-index: 1;
            }
            .question-title {
              color: #7F43D4;
              font-size: 14px;
              font-weight: 700;
              margin-bottom: 8px;
            }
            .question-img {
              width: 100%;
              max-height: 200px;
              object-fit: cover;
              border-radius: 12px;
              margin-bottom: 8px;
            }
            .question-text {
              color: #1E293B;
              font-size: 15px;
              font-weight: 500;
            }
            .answer-header {
              color: #64748B;
              font-size: 14px;
              font-weight: 700;
              margin-top: 16px;
              margin-bottom: 8px;
            }
            .answer-content {
              color: #1E293B;
              font-size: 15px;
              line-height: 1.65;
              word-wrap: break-word;
            }
            .answer-content p { margin: 0 0 10px 0; }
            .answer-content p:last-child { margin-bottom: 0; }
            .katex-display { margin: 0.8em 0; overflow-x: auto; overflow-y: hidden; padding: 4px 0; }
            .conclusion-box {
              margin-top: 14px; padding: 14px 16px;
              background-color: #F0FDF4; border: 1.5px dashed #10B981;
              border-radius: 12px; color: #047857; font-weight: 600; font-size: 14px;
            }
            .conclusion-title {
              color: #10B981; font-size: 12px; font-weight: 700; text-transform: uppercase; margin-bottom: 4px;
            }
            .thinking {
              display: flex; align-items: center; justify-content: center;
              padding: 16px 0; color: #64748B; font-size: 14px; font-weight: 500;
            }
            .spinner {
              width: 16px; height: 16px; border: 2px solid #E2E8F0;
              border-top-color: #7F43D4; border-radius: 50%;
              animation: spin 1s linear infinite; margin-right: 8px;
            }
            @keyframes spin { to { transform: rotate(360deg); } }
            
            .suggestion-title {
              color: #64748B;
              font-size: 13px;
              font-weight: 700;
              margin-top: 16px;
              margin-bottom: 8px;
            }
            .support-chips {
              display: flex; gap: 8px; overflow-x: auto;
              padding: 4px 0 10px 0;
            }
            .support-chips::-webkit-scrollbar { display: none; }
            .support-chip {
              background-color: #F5F3FF; border: 1px solid #DDD6FE;
              border-radius: 20px; padding: 8px 14px; color: #3B2A6B;
              font-size: 14px; font-weight: 500; white-space: nowrap; cursor: pointer;
            }
            
            .quiz-card {
              border: 1px solid #C084FC; background-color: #FAF5FF;
              border-radius: 16px; padding: 16px; text-align: center; margin-top: 16px;
            }
            .quiz-title { color: #7E22CE; font-weight: 700; font-size: 15px; }
            .quiz-desc { color: #6B21A8; font-size: 13px; margin: 4px 0 12px 0; }
            .quiz-btn {
              background-color: #7F43D4; color: white; font-weight: 700;
              border-radius: 20px; padding: 10px 24px; border: none; font-size: 14px; cursor: pointer;
            }
          </style>
        </head>
        <body>
          <div id="container"></div>
          <script>
            function decodeBase64Utf8(base64) {
              const binaryString = atob(base64);
              const len = binaryString.length;
              const bytes = new Uint8Array(len);
              for (let i = 0; i < len; i++) { bytes[i] = binaryString.charCodeAt(i); }
              return new TextDecoder("utf-8").decode(bytes);
            }
            
            function onChipClick(action) {
                if (window.Android) window.Android.onSupportChipClicked(action);
            }
            function onQuizClick() {
                if (window.Android) window.Android.onNavigateToQuizzes();
            }

            document.addEventListener("DOMContentLoaded", function() {
              marked.setOptions({ breaks: true, gfm: true });
              let chatData = JSON.parse(decodeBase64Utf8("$chatDataJson"));
              let html = "";
              
              chatData.forEach(pair => {
                  html += `<div class="question-wrapper">
                             <div class="question-mascot"></div>
                             <div class="question-card">
                               <div class="question-title">Your question:</div>`;
                  if (pair.questionImage) {
                      html += `<img class="question-img" src="${"$"}{pair.questionImage}" />`;
                  }
                  html += `<div class="question-text">${"$"}{pair.question}</div>
                             </div>
                           </div>`;
                  
                  if (pair.isThinking) {
                      html += `<div class="thinking"><div class="spinner"></div> Eous is thinking...</div>`;
                  } else if (pair.answer) {
                      html += `<div class="answer-header">Eous:</div>`;
                      
                      let rawText = pair.answer;
                      rawText = rawText.replace(/([^\n])\n(\s*[\*\-\+]\s)/g, "$1\n\n$2");
                      rawText = rawText.replace(/([^\n])\n(\s*\d+\.\s)/g, "$1\n\n$2");
                      rawText = rawText.replace(
                        /(?:^|\n)(?:\*\*)?(Conclusion:|Final conclusion:|Final answer:|Summary:)(?:\*\*)?\s*([\s\S]+?)(?=\n\n|$)/gi,
                        '\n<div class="conclusion-box"><div class="conclusion-title">$1</div>$2</div>\n'
                      );
                      
                      let parsedHtml = marked.parse(rawText);
                      html += `<div class="answer-content">${"$"}{parsedHtml}</div>`;
                      
                      if (pair.quizId) {
                          html += `<div class="quiz-card">
                                     <div class="quiz-title">Practice Quiz is ready!</div>
                                     <div class="quiz-desc">Test your understanding with a quick practice quiz.</div>
                                     <button class="quiz-btn" onclick="onQuizClick()">Start Quiz</button>
                                   </div>`;
                      }
                      
                      if (pair.showSupportChips) {
                          html += `<div class="suggestion-title">Suggestion:</div>
                                   <div class="support-chips">
                                     <div class="support-chip" onclick="onChipClick('Simplify the explanation')">Simplify Explanation</div>
                                     <div class="support-chip" onclick="onChipClick('Give another solution')">Another Solution</div>
                                     <div class="support-chip" onclick="onChipClick('Generate a practice quiz on this topic')">Practice with Quizzes</div>
                                   </div>`;
                      }
                  }
              });
              
              document.getElementById('container').innerHTML = html;
              
              renderMathInElement(document.getElementById('container'), {
                delimiters: [
                  {left: "$$", right: "$$", display: true},
                  {left: "$", right: "$", display: false},
                  {left: "\\(", right: "\\)", display: false},
                  {left: "\\[", right: "\\]", display: true}
                ],
                throwOnError: false
              });
              
              setTimeout(() => { window.scrollTo(0, document.body.scrollHeight); }, 100);
            });
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
                settings.allowFileAccess = true
                settings.allowContentAccess = true
                setBackgroundColor(android.graphics.Color.TRANSPARENT)
                
                setLayerType(android.view.View.LAYER_TYPE_HARDWARE, null)
                
                addJavascriptInterface(object {
                    @android.webkit.JavascriptInterface
                    fun onSupportChipClicked(action: String) {
                        post { onSupportChipClicked(action) }
                    }
                    
                    @android.webkit.JavascriptInterface
                    fun onNavigateToQuizzes() {
                        post { onNavigateToQuizzes() }
                    }
                }, "Android")

                webViewClient = object : WebViewClient() {
                    override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
                        return true
                    }
                }
            }
        },
        update = { webView ->
            if (webView.tag != chatDataJson) {
                webView.tag = chatDataJson
                webView.loadDataWithBaseURL("https://localhost", htmlContent, "text/html", "UTF-8", null)
            }
        },
        modifier = modifier.fillMaxWidth() // Usually caller handles fillMaxSize
    )
}
