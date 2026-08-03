package com.eous.mentor.features.pro

data class ProState(
    val features: List<String> = listOf(
        "Unlimited AI Tutor Chats (24/7 access)",
        "Custom interactive smart quizzes",
        "Textbook scan and solution guide helper",
        "Detailed diagnostic learning analytics"
    ),
    val price: String = "$9.99/mo"
)
