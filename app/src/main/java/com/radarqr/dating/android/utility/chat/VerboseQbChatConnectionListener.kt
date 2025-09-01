package com.radarqr.dating.android.utility.chat

import android.annotation.SuppressLint
import android.util.Log
import android.view.View
import com.google.android.material.snackbar.Snackbar
import org.jivesoftware.smack.ConnectionListener
import org.jivesoftware.smack.XMPPConnection


open class VerboseQbChatConnectionListener(private val rootView: View) : ConnectionListener {
    private val TAG = VerboseQbChatConnectionListener::class.java.simpleName
    private var snackbar: Snackbar? = null

    override fun connected(connection: XMPPConnection) {
        Log.i(TAG, "connected()")
    }

    override fun authenticated(connection: XMPPConnection, authenticated: Boolean) {
        Log.i(TAG, "authenticated()")
    }

    override fun connectionClosed() {
        Log.i(TAG, "connectionClosed()")
    }

    override fun connectionClosedOnError(e: Exception) {
        Log.i(TAG, "connectionClosedOnError(): " + e.localizedMessage)
    }

    @SuppressLint("StringFormatInvalid")
    override fun reconnectingIn(seconds: Int) {
        if (seconds % 5 == 0 && seconds != 0) {
            Log.i(TAG, "reconnectingIn(): $seconds")
        }
    }

    override fun reconnectionSuccessful() {
        Log.i(TAG, "reconnectionSuccessful()")
    }

    override fun reconnectionFailed(error: Exception) {
        Log.i(TAG, "reconnectionFailed(): " + error.localizedMessage)
    }
}