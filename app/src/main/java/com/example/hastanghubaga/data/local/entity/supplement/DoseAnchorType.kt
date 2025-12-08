package com.example.hastanghubaga.data.local.entity.supplement

enum class DoseAnchorType {
    MIDNIGHT,        // current behavior
    WAKEUP,      // user set time
    BREAKFAST,
    LUNCH,
    DINNER,
    CAFFEINE,
    BEFORE_WORKOUT,
    AFTER_WORKOUT,
    CUSTOM_EVENT,     // for something user defines later
    ANYTIME
}
