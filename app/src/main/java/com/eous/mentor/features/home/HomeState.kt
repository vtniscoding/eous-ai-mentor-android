package com.eous.mentor.features.home

import com.eous.mentor.domain.model.HomeData

data class HomeState(
    val stats: HomeData = HomeData(),
    val isLoading: Boolean = true,
    val isLoggedOut: Boolean = false
)
