package com.radarqr.dating.android.utility

import android.util.Log
import com.amazonaws.AmazonClientException
import com.amazonaws.AmazonServiceException
import com.amazonaws.auth.CognitoCachingCredentialsProvider
import com.amazonaws.mobileconnectors.s3.transferutility.TransferListener
import com.amazonaws.mobileconnectors.s3.transferutility.TransferState
import com.amazonaws.mobileconnectors.s3.transferutility.TransferUtility
import com.amazonaws.regions.Region
import com.amazonaws.services.s3.AmazonS3Client
import com.radarqr.dating.android.app.RaddarApp
import java.io.File
import kotlin.coroutines.Continuation
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

var credentialsProvider: CognitoCachingCredentialsProvider? = null
var s3Client: AmazonS3Client? = null


suspend fun uploadWithTransferUtility(
    file: File,
    deleteFileKey: String
): Pair<String, String> {

    lateinit var result: Continuation<Pair<String, String>>
    credentialsProvider = CognitoCachingCredentialsProvider(
        RaddarApp.appContext,
        AWSKeys.COGNITO_POOL_ID,
        AWSKeys.MY_REGION
    )
    s3Client = AmazonS3Client(credentialsProvider, Region.getRegion(AWSKeys.MY_REGION))


    val transferUtility = TransferUtility.builder()
        .context(RaddarApp.appContext)
        .s3Client(s3Client)
        .build()

    val uploadObserver = transferUtility.upload(
        AWSKeys.BUCKET_NAME,
        /*"$folder/$fileName"*/
        file.name,
        file,
        /* CannedAccessControlList.PublicRead*/
    )
    // Attach a listener to the observer
    uploadObserver.setTransferListener(object : TransferListener {
        override fun onStateChanged(id: Int, state: TransferState) {
            when (state) {
                TransferState.COMPLETED -> {
                    val data = Pair(uploadObserver.key, "$id")
                    deleteFileKey.let {
                        if (deleteFileKey != "" && deleteFileKey != "key") {
                            println("delete image path= $deleteFileKey")
                            s3Client?.deleteObject(AWSKeys.BUCKET_NAME, deleteFileKey)
                        }
                    }
                    println(result)
                    try {
                        result.resume(data)
                    } catch (e: Exception) {

                    }
                }

                TransferState.FAILED, TransferState.CANCELED -> {
                    val data = Pair("Radar", "$id")
                    println("Response: $state")
                    try {
                        result.resume(data)
                    } catch (e: Exception) {

                    }
                }

                else -> {}
            }
        }

        override fun onProgressChanged(id: Int, current: Long, total: Long) {
            val done = (((current.toDouble() / total) * 100.0).toInt())
            Log.d("Radar", "UPLOAD - - ID: $id, percent done = $done")
        }

        override fun onError(id: Int, ex: Exception) {
            Log.d("LOG_TAG", "UPLOAD ERROR - - ID: $id - - EX: ${ex.message.toString()}")
            val data = Pair("Radar", "$id")
            try {
                result.resume(data)
            } catch (e: Exception) {

            }
        }
    })
    return suspendCoroutine { continuation -> result = continuation }
}

suspend fun uploadVenueImageWithTransferUtility(
    file: File,
    deleteFileKey: String
): Pair<String, String> {

    lateinit var result: Continuation<Pair<String, String>>
    credentialsProvider = CognitoCachingCredentialsProvider(
        RaddarApp.appContext,
        AWSKeys.COGNITO_POOL_ID,
        AWSKeys.MY_REGION
    )
    s3Client = AmazonS3Client(credentialsProvider, Region.getRegion(AWSKeys.MY_REGION))


    val transferUtility = TransferUtility.builder()
        .context(RaddarApp.appContext)
        .s3Client(s3Client)
        .build()

    val uploadObserver = transferUtility.upload(
        AWSKeys.BUCKET_NAME_VENUE,
        file.name,
        file,
    )
    // Attach a listener to the observer
    uploadObserver.setTransferListener(object : TransferListener {
        override fun onStateChanged(id: Int, state: TransferState) {
            when (state) {
                TransferState.COMPLETED -> {
                    val data = Pair(uploadObserver.key, "$id")
                    deleteFileKey.let {
                        if (deleteFileKey != "" && deleteFileKey != "key") {
                            println("delete image path= $deleteFileKey")
                            s3Client?.deleteObject(AWSKeys.BUCKET_NAME_VENUE, deleteFileKey)
                        }
                    }
                    println(result)
                    try {
                        result.resume(data)
                    } catch (_: Exception) {

                    }
                }

                TransferState.FAILED, TransferState.CANCELED -> {
                    val data = Pair("Radar", "$id")
                    println("Response: $state")
                    try {
                        result.resume(data)
                    } catch (_: Exception) {

                    }
                }

                TransferState.WAITING -> {}
                TransferState.IN_PROGRESS -> {}
                TransferState.PAUSED -> {}
                TransferState.RESUMED_WAITING -> {}
                TransferState.WAITING_FOR_NETWORK -> {}
                TransferState.PART_COMPLETED -> {}
                TransferState.PENDING_CANCEL -> {}
                TransferState.PENDING_PAUSE -> {}
                TransferState.PENDING_NETWORK_DISCONNECT -> {}
                TransferState.UNKNOWN -> {}
            }
        }

        override fun onProgressChanged(id: Int, current: Long, total: Long) {
            val done = (((current.toDouble() / total) * 100.0).toInt())
            Log.d("Radar", "UPLOAD - - ID: $id, percent done = $done")
        }

        override fun onError(id: Int, ex: Exception) {
            Log.d("LOG_TAG", "UPLOAD ERROR - - ID: $id - - EX: ${ex.message.toString()}")
            val data = Pair("Radar", "$id")
            try {
                result.resume(data)
            } catch (_: Exception) {

            }
        }
    })
    return suspendCoroutine { continuation -> result = continuation }
}

fun String.deleteVenueImage() {
    credentialsProvider = CognitoCachingCredentialsProvider(
        RaddarApp.appContext,
        AWSKeys.COGNITO_POOL_ID,
        AWSKeys.MY_REGION
    )
    s3Client = AmazonS3Client(credentialsProvider, Region.getRegion(AWSKeys.MY_REGION))
    this.let {
        if (it.isNotEmpty()) {
            println("delete image path= $this")
            try {
                s3Client?.deleteObject(AWSKeys.BUCKET_NAME_VENUE, this)
            } catch (e: AmazonClientException) {
                e.printStackTrace()
            } catch (e: AmazonServiceException) {
                e.printStackTrace()
            }

            try {
                s3Client?.deleteObject(AWSKeys.BUCKET_NAME_VENUE_MEDIUM, this)
            } catch (e: AmazonClientException) {
                e.printStackTrace()
            } catch (e: AmazonServiceException) {
                e.printStackTrace()
            }

            try {
                s3Client?.deleteObject(AWSKeys.BUCKET_NAME_VENUE_THUMB, this)
            } catch (e: AmazonClientException) {
                e.printStackTrace()
            } catch (e: AmazonServiceException) {
                e.printStackTrace()
            }
        }
    }
}