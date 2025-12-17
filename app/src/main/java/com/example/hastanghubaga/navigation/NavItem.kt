package com.example.hastanghubaga.navigation

sealed class NavItem(val route: String) {

    data object HOME : NavItem("home")

    data object SUPPLEMENT_DETAIL :
        NavItem("supplement/{id}") {
        fun route(id: Long) = "supplement/$id"
    }

    data object MEAL_DETAIL :
        NavItem("meal/{id}") {
        fun route(id: Long) = "meal/$id"
    }

    data object ACTIVITY_DETAIL :
        NavItem("activity/{id}") {
        fun route(id: Long) = "activity/$id"
    }
}