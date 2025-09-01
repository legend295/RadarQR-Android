package com.radarqr.dating.android.utility.introduction

enum class IntroductionScreenType : IIntroductionScreenType {
    SETTINGS {
        override fun title(): String = "Pro Tip!"

        override fun description(): String =
            "Download your profile QR code &\nvisit our “Swag Shop” to see the\ndifferent ways you can print, share,\nand wear your code. Stand out\nwherever you go, not just online!"

        override fun rotation(): Int = 0
    },
    PROFILE {
        override fun title(): String = "Add Close Friends"

        override fun description(): String =
            "Single is FUN with Friends.\n\nClose friends can tag each other\nat “hot Spot” venues & play\nmatchmaker together. Scan or\nsearch for your friends profile,\nthen add them!"

        override fun rotation(): Int = 55
    },
    VENUE_DETAIL_ADD_PHOTO {

        override fun title(): String = "Dating Made Social"

        override fun description(): String =
            "Add photos & Tag close friends to\nmaximize your chances of\nconnecting with singles at your\nfavorite venues!\n\nPhotos delete after 48 hours!"

        override fun rotation(): Int = -55
    },
    VENUE_DETAIL_SINGLES {
        override fun title(): String = "Singles In The Room"

        override fun description(): String =
            "Arrive, Check In, and click here to\nview other singles at the venue\nwith you."

        override fun rotation(): Int = -140
    },
    ROAMING_TIMER {

        override fun title(): String = "Roaming Timer & Check-In"

        override fun description(): String =
            "Easily check - in to your venue &\nupdate your “roaming timer”\nanytime you see these icons at the\ntop of a page."

        override fun rotation(): Int = 40
    },
    HOTSPOT {

        override fun title(): String = "HOT SPOTS!"

        override fun description(): String =
            "Discover WHERE the singles are &\n" +
                    "WHO in the room is single using the flame icon!\n\nFInd Your Venue > Check in > Match!"

        override fun rotation(): Int = 40
    }
}