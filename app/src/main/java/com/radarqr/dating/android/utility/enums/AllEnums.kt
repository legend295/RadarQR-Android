package com.radarqr.dating.android.utility.enums

import com.radarqr.dating.android.R

enum class SubscriptionPopUpType : ISubscriptionPopUpType {
    MARK_FAVORITE {
        override fun getTitle(): String = "Mark Your Favorites!"
        override fun getIcon(): Int = R.drawable.ic_black_round_heart

        override fun getFirstMessage(): String =
            "Sort your favorites while you narrow down your best match."

        override fun getSecondMessage(): String = ""
    },
    LIKE {
        override fun getTitle(): String = "See Who Likes You"
        override fun getIcon(): Int = R.drawable.ic_black_round_heart_teal

        override fun getFirstMessage(): String = "See who made the first move\nto match with you!"

        override fun getSecondMessage(): String = ""
    },
    PREFERENCES {
        override fun getTitle(): String = "Advanced Filters"
        override fun getIcon(): Int = R.drawable.ic_advanced_filters

        override fun getFirstMessage(): String =
            "Find the perfect fit by setting your Non-Negotiables."

        override fun getSecondMessage(): String = ""
    },
    RECOMMENDATION_LIMIT_REACHED {
        override fun getTitle(): String = "Daily Free Limit Reached"
        override fun getIcon(): Int = R.drawable.ic_daily_limit_reached

        override fun getFirstMessage(): String =
            "Upgrade to PLUS to enjoy unlimited swipes every day!"

        override fun getSecondMessage(): String = ""
    },
    SEND_LIKE_WITH_MESSAGE {
        override fun getTitle(): String = "Shoot Your Shot!"
        override fun getIcon(): Int = R.drawable.ic_shoot_with_message

        override fun getFirstMessage(): String =
            "Boost Your Chances of Standing Out by Showing Interest First! Take advantage of this exciting feature! Upgrade to PLUS to send the First Comment."

        override fun getSecondMessage(): String = ""
    },
    RECOMMENDATION_UNDO {
        override fun getTitle(): String = "Recall"
        override fun getIcon(): Int = R.drawable.ic_undo_black

        override fun getFirstMessage(): String =
            "Miss someone great? Take a second look with the ability to recall."

        override fun getSecondMessage(): String = ""
    },
    SEE_TAGGED_USERS{
        override fun getTitle(): String = "See Tagged Users"
        override fun getIcon(): Int = R.drawable.ic_black_tagged_subscription

        override fun getFirstMessage(): String =
            "Goodbye Missed Connections! Easily access profiles of tagged individuals with this convenient feature."

        override fun getSecondMessage(): String = ""
    }
}

interface ISubscriptionPopUpType {
    fun getTitle(): String
    fun getIcon(): Int

    fun getFirstMessage(): String
    fun getSecondMessage(): String
}