package com.radarqr.dating.android.utility

import android.content.Context
import android.os.NetworkOnMainThreadException
import android.webkit.MimeTypeMap
import com.amazonaws.HttpMethod
import com.amazonaws.services.s3.AmazonS3
import com.amazonaws.services.s3.model.GeneratePresignedUrlRequest
import com.amazonaws.services.s3.model.ResponseHeaderOverrides
import study.amazons3integration.aws.AmazonUtil
import java.io.File
import java.util.*


object S3Utils {
    /**
     * Method to generate a presignedurl for the image
     * @param applicationContext context
     * @param path image path
     * @return presignedurl
     */
    @JvmStatic
    fun generates3ShareUrl(applicationContext: Context?, path: String): String {
        val f = File(path)
        val s3client: AmazonS3 = AmazonUtil.getS3Client(applicationContext)!!
        val expiration = Date()
        var msec = expiration.time
        msec += 1000 * 60 * 60 * 24 * 7.toLong()
        expiration.time = msec
        val overrideHeader = ResponseHeaderOverrides()
        overrideHeader.contentType = getMimeType(path)
        val mediaUrl = f.name
        /* val generatePreSignedUrlRequest =
             GeneratePresignedUrlRequest(AWSKeys.BUCKET_NAME, mediaUrl)
         generatePreSignedUrlRequest.method = HttpMethod.GET // Default.
         generatePreSignedUrlRequest.expiration = expiration
         generatePreSignedUrlRequest.responseHeaders = overrideHeader
         val url = s3client.generatePresignedUrl(generatePreSignedUrlRequest)
 */
        return mediaUrl.toString()
    }


    fun generatesShareUrl(applicationContext: Context?, path: String?): String {
        val s3client: AmazonS3 = AmazonUtil.getS3Client(applicationContext)!!
        val expiration = Date()
        var msec = expiration.time
        msec += 1000 * 60 * 60 * 24 * 7.toLong() // Expire after one day
        expiration.time = msec
        val overrideHeader = ResponseHeaderOverrides()
        overrideHeader.contentType = getMimeType(path)
        val generatePreSignedUrlRequest =
            GeneratePresignedUrlRequest(AWSKeys.BUCKET_NAME, path)
        generatePreSignedUrlRequest.method = HttpMethod.GET // Default.
        generatePreSignedUrlRequest.expiration = expiration
        generatePreSignedUrlRequest.responseHeaders = overrideHeader
        val url = try {
            s3client.generatePresignedUrl(generatePreSignedUrlRequest)
        } catch (e: Exception) {
            ""
        }

        return url.toString()
    }

    fun generatesVenueShareUrl(applicationContext: Context?, path: String?): String {
        val s3client: AmazonS3 = AmazonUtil.getS3Client(applicationContext)!!
        val expiration = Date()
        var msec = expiration.time
        msec += 1000 * 60 * 60 * 24 * 7.toLong() // Expire after one day
        expiration.time = msec
        val overrideHeader = ResponseHeaderOverrides()
        overrideHeader.contentType = getMimeType(path)
        val generatePreSignedUrlRequest =
            GeneratePresignedUrlRequest(AWSKeys.BUCKET_NAME_VENUE, path)
        generatePreSignedUrlRequest.method = HttpMethod.GET // Default.
        generatePreSignedUrlRequest.expiration = expiration
        generatePreSignedUrlRequest.responseHeaders = overrideHeader
        val url = try {
            s3client.generatePresignedUrl(generatePreSignedUrlRequest)
        } catch (e: Exception) {
            ""
        }

        return url.toString()
    }

    fun generatesThumbShareUrl(applicationContext: Context?, path: String?): String {
        val s3client: AmazonS3 = AmazonUtil.getS3Client(applicationContext)!!
        val expiration = Date()
        var msec = expiration.time
        msec += 1000 * 60 * 60 * 24 * 7.toLong() // Expire after one day
        expiration.time = msec
        val overrideHeader = ResponseHeaderOverrides()
        overrideHeader.contentType = getMimeType(path)
        val generatePreSignedUrlRequest =
            GeneratePresignedUrlRequest(AWSKeys.BUCKET_NAME_MEDIUM, path)
        generatePreSignedUrlRequest.method = HttpMethod.GET // Default.
        generatePreSignedUrlRequest.expiration = expiration
        generatePreSignedUrlRequest.responseHeaders = overrideHeader
        val url = try {
            s3client.generatePresignedUrl(generatePreSignedUrlRequest)
        } catch (e: Exception) {
            ""
        }

        return url.toString()
    }

    /*@JvmStatic
    fun generatesThumbShareUrl(applicationContext: Context?, path: String?): String {
//        val f = File(path)
        val s3client: AmazonS3 = AmazonUtil.getS3Client(applicationContext)!!
        val expiration = Date()
        var msec = expiration.time
        msec += 1000 * 60 * 60 * 7.toLong() // 1 hour.
        expiration.time = msec
        val overrideHeader = ResponseHeaderOverrides()
        overrideHeader.contentType = getMimeType(path)
        val mediaUrl = path
        val generatePresignedUrlRequest = GeneratePresignedUrlRequest(
            AWSKeys.BUCKET_NAME_MEDIUM,
            mediaUrl
        )
        generatePresignedUrlRequest.method = HttpMethod.GET // Default.
        generatePresignedUrlRequest.expiration = expiration
        generatePresignedUrlRequest.responseHeaders = overrideHeader
        val url = s3client.generatePresignedUrl(generatePresignedUrlRequest)
//        Log.e("Generated Url - ", url.toString())
        return url.toString()
    }*/

    fun getMimeType(url: String?): String? {
        var type: String? = null
        val extension = MimeTypeMap.getFileExtensionFromUrl(url)
        if (extension != null) {
            type = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension)
        }
        return type
    }
}