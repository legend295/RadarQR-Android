package com.radarqr.dating.android.constant


object Constants {

    //preference keys


    const val ADS_COUNT = 3
    const val COUNT = "current_count"
    const val VENUE_ID: String = "venue_id"
    const val VENUE_NAME: String = "venue_name"
    const val IS_UPDATING: String = "is_updating"
    const val VENUE_HANDLE: String = "venue_handle"
    const val IS_MY_VENUE = "is_my_venue"
    const val IS_VENUE_CHECKED_IN = "is_venue_checked_in"
    val USERDATA = "bundle_user_data"
    const val COUNTRYCODE = "country_code"
    const val MOBILE = "mobile"
    val IMAGE_URI = "image_uri"


    //Socket Exceptions

    val SOCKET_ERROR = "Socket timeout"
    val CONNECTION_ERROR =
        "You are not connected to the internet. Please check your connection and try again."
    val SESSION_ENDED = "Session ended you need to login again."


    //Type keys
    const val TYPE = "type"
    const val FROM = "from"
    const val ADS_FROM = "ads_from"
    const val FROM_ADD_MORE_PICTURE_SCREEN = "from_add_more_picture_screen"
    const val LOGIN_TYPE = "login_type"
    const val INTERESTED_IN_FRAGMENT = 1
    const val ACCOUNT_DETAILS_FRAGMENT = 2
    const val FROM_EDIT = 3
    const val FROM_VENUE_PHOTO = 6
    const val FROM_ADD_MORE_PICTURE = 5
    const val FROM_REGISTER = 4
    const val REPORT = 999
    const val FROM_MOBILE = "is_from_mobile"
    const val FROM_SOCIAL = "is_from_social"

    //Intents
    const val EXTRA_DATA = "extra_data"
    const val EXTRA = "extra"
    const val POSITION = "position"
    const val TAG = "tag"
    const val IS_DEEP_LINK = "is_deep_link"
    const val SHARED_USER_ID = "shared_user_id"
    const val FROM_HOME = "from_home"
    const val FROM_LIKE = "from_like"

    // Chat type keys
    const val ONLINE = "online"
    const val VENUE = "venue"
    const val IN_PERSON = "inperson"
    const val ALL = "all"

    const val DEVICE_TOKEN = "device_token"

    //Notification data
    const val NOTIFICATION_TYPE = "type"
    const val USER_ID = "user_id"
    const val CATEGORY = "category"
    const val IS_NOTIFICATION = "is_notification"

    // Notification Type
    const val LIKE_REQUEST = "Like_Request"
    const val MATCH_REQUEST = "Match_Request"
    const val ROAMING_TIMER_NOTIFICATION = "roaming_timer_notification"
    const val FRIEND_INVITE = "Friend_Invite"
    const val FRIEND_INVITE_ACCEPTED = "Friend_Invite_Accepted"

    //ChatImageConstatns
    const val TRUE = "true"
    const val FALSE = "false"

    const val MAN = "man"
    const val CAPITAL_MAN = "Man"
    const val CAPITAL_MEN = "Men"
    const val WOMAN = "woman"
    const val NON_BINARY = "non-binary"
    const val CAPITAL_WOMEN = "Women"
    const val CAPITAL_WOMAN = "Woman"
    const val EVERYONE = "Everyone"
    const val MALE = "Male"
    const val FEMALE = "Female"

    // notification
    const val CHAT_NOTIFICATION = "chat_notification"
    const val NOTIFICATION = "notification"
    const val MESSAGE = "message"
    const val CHAT_MESSAGE = "QB_MESSAGE"

    const val UN_MATCH_MESSAGE =
        "You two had a match in the past. Please contact support for more information."
    const val MATCH_MESSAGE = "You two already have a match. Check your connections."
    const val REQUEST_SENT = "A request has already been sent."
    const val REQUEST_RECEIVE = "You already have received a like request from user."
    const val DECLINE =
        "Your previous like request was declined. You cannot connect again. Please contact support for more information."

    const val PRIVACY_POLICY = "privacy_policy"
    const val RADAR_WEBSITE = "radar_website"
    const val TERMS_OF_SERVICES = "terms_of_services"
    const val HELP_CENTER = "help_center"
    const val DOWNLOAD_MY_DATA = "download_my_data"
    const val RUN_DAY_AI = "run_day_ai"

    const val JOB = "job"
    const val EDUCATION = "education"

    // Report types
    const val NONE = "none"
    const val LIST = "list"
    const val EDIT_TEXT = "edittext"

    const val USER_REPORTED = 403
    const val TOO_MANY_ATTEMPTS = 429
    const val DIALOG_ID = "dialog_id"
    const val NAME = "name"
    const val PROFILE_PIC = "profile_pic"
    const val SENDER_ID = "sender_id"

    // chat constants
    const val DIALOGS_PER_PAGE = 100
    private const val EXTRA_QB_DIALOG = "qb_dialog"
    const val EXTRA_QB_USERS = "qb_users"
    const val MINIMUM_CHAT_OCCUPANTS_SIZE = 1
    const val PRIVATE_CHAT_OCCUPANTS_SIZE = 2
    const val EXTRA_CHAT_NAME = "chat_name"
    const val USERS_PAGE_SIZE = 100
    const val MIN_SEARCH_QUERY_LENGTH = 3
    const val SEARCH_DELAY = 600L

    const val MP4 = ".mp4"
    const val MKV = ".mkv"
    const val THUMB = "_thumb.webp"

    //Signup type
    const val FACEBOOK = "Facebook"
    const val GOOGLE = "Google"
    const val PHONE = "Phone"

    const val IS_USER_VERIFIED = "is_user_verified"
    const val SOCIAL_ID_FACEBOOK = "social_id_facebook"

    const val IS_PROFILE_PAUSED = "is_profile_paused"

    const val LAT = "lat"
    const val EDIT_LAT = "edit_lat"
    const val LNG = "lng"
    const val EDIT_LNG = "edit_lng"

    /*Urls*/
    const val ORDER_PROFILE_QR =
        "https://www.zazzle.com/qr_cards_square_2_5_x_2_5_square_business_card-256189976254144388"

    //        "https://www.zazzle.com/qr_cards_square_2_5_x_2_5_square_business_card-256898607596849632"
    const val SWAG_SHOP_URL = "https://www.radarqr.com/swag-shop1"
    const val SUBSCRIPTION_URL = "https://www.radarqr.com/radarqr-hot-spot"

    //
    const val SUPPORT_EMAIL = "support@radarqr.com"

    const val ROAMING_TIMER_NOTIFICATION_REQUEST_CODE = 0


    object Children {
        const val HAVE_AND_OPEN_TO_MORE_BACKEND_VALUE = "haveandopentomore"
        const val HAVE_AND_DO_NOT_WANT_MORE_BACKEND_VALUE = "haveanddontwantmore"
        const val DO_NOT_HAVE_BUT_WANT_BACKEND_VALUE = "donthavebutwant"
        const val DO_NOT_WANT_BACKEND_VALUE = "dontwant"
        const val OPEN_MINDED_BACKEND_VALUE = "openminded"
        const val UNDECIDED_BACKEND_VALUE = "undecided"
        const val OPEN_TO_ALL_BACKEND_VALUE = "opentoall"

        const val HAVE_AND_OPEN_TO_MORE_FRONTEND_VALUE = "Have & open to more"
        const val HAVE_AND_DO_NOT_WANT_MORE_FRONTEND_VALUE = "Have & don't want more"
        const val DO_NOT_HAVE_BUT_WANT_FRONTEND_VALUE = "Don't have but want"
        const val DO_NOT_WANT_FRONTEND_VALUE = "Don't want"
        const val OPEN_MINDED_FRONTEND_VALUE = "Open minded"
        const val UNDECIDED_FRONTEND_VALUE = "Undecided"
        const val OPEN_TO_ALL_FRONTEND_VALUE = "Open to all"
    }

    object EducationLevel {
        const val HIGH_SCHOOL_FRONT_END_VALUE = "High school"
        const val UNDER_GRAD_FRONT_END_VALUE = "Undergrad"
        const val POST_GRAD_FRONT_END_VALUE = "Postgrad"
        const val DO_NOT_WANT_TO_SAY_FRONT_END_VALUE = "Don't want to say"

        const val HIGH_SCHOOL_BACKEND_END_VALUE = "High School"
        const val UNDER_GRAD_BACKEND_END_VALUE = "Undergrad"
        const val POST_GRAD_BACKEND_END_VALUE = "Postgrad"
    }

    object VenueStatus {
//        const val APPROVED = 1
//        const val SEND_FOR_APPROVAL = 4
//        const val SUBMITTED = 2
//        const val ACTION_NEEDED = 3

        const val APPROVED = 1
        const val DISAPPROVED = 2
        const val SEND_FOR_APPROVAL = 4
        const val IN_PROGRESS = 3
        const val VENUE_PAUSED = 5
    }

    object IntroductionConstants {
        const val SETTINGS = "settings"
        const val HOTSPOT = "hotspot"
        const val PROFILE = "profile"
        const val VENUE_DETAIL_SINGLES = "venue_detail_singles"
        const val VENUE_DETAIL_ADD_PHOTO = "venue_detail_photo"
        const val ROAMING_TIMER = "roaming_timer"
    }

    const val IOS_APNS_KEY = "ios_apns"
    const val IOS_APNS_VALUE = "1"
    const val IOS_SOUND_KEY = "ios_sound"
    const val IOS_SOUND_VALUE = "radarNotSound.wav"

    object MixPanelFrom {
        const val ACCOUNT = "account"
        const val RECOMMENDATION_ADS = "advertisements"
        const val FROM_RECALL_POPUP = "recall"
        const val FROM_SHOOT_YOUR_SHOT_POPUP = "shoot your shot"
        const val FROM_SEE_TAGGED_USER = "tagged users"
        const val RECOMMENDATION_LIMIT_REACHED = "daily limit"
        const val PREFERENCES_ADVANCE_FILTER_POPUP = "preferences"
        const val LIKE = "likes"
        const val MARK_YOUR_FAVORITE_POPUP = "favorite chat"
        const val SETTINGS = "settings"
        const val RECOMMENDATION = "recommendation"
        const val NEAR_YOU = "near you"
        const val PROFILE = "profile"
        const val QR_CODE = "qrcode"
        const val CHAT = "chat"
        const val HOTSPOTS = "hotspots"
        const val CLOSE_FRIENDS = "close friends"
        const val ACTION_LIKE = "like"
        const val ACTION_LIKE_MESSAGE = "like with message"
        const val ACTION_DISLIKE = "dislike"
        const val ACTION_UNDO = "undo"
        const val ACTION_ACCEPT = "accept"
        const val ACTION_ACCEPT_MESSAGE = "accept with message"
        const val ACTION_REJECT = "reject"
        const val OTHERS = "others"
        const val CHEMISTRY_SWIPE = "chemistry swipe"
        const val SWAG_SHOP = "swag shop"
        const val LIKES = "likes"
        const val TAGGED = "tagged"
        const val HOTSPOT_MAP = "hotspot map"
        const val NA = "NA"
        const val MANUAL = "manual"
    }
}
