package com.eous.mentor.features.search

data class SearchState(
    val searchQuery: String = "",
    val recentSearches: List<String> = emptyList()
)
