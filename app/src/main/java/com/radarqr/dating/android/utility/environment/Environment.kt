package com.radarqr.dating.android.utility.environment

enum class Environment : IEnvironment {

    DEVELOPMENT {
        override fun getUrl(): String = "http://112.196.26.154:3800/images/hobbies/"
        override fun getShareUrl(): String = "https://profile.gosalworks.com/user/"
    },
    STAGING {
        override fun getUrl(): String = "http://112.196.26.154:3801/images/hobbies/"
        override fun getShareUrl(): String =
            "http://radar-test.trantorglobal.com/user/"/*"http://radar.trantorglobal.com/user/"*/
    },
    PRODUCTION {
        override fun getUrl(): String = "https://api.radarqr.com/images/hobbies/"
        override fun getShareUrl(): String = "https://profile.radarqr.com/user/"
    },
    RELEASE {
        override fun getUrl(): String = "https://api.radarqr.com/images/hobbies/"
        override fun getShareUrl(): String = "https://profile.radarqr.com/user/"
    }

}