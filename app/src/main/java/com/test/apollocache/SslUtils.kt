package com.test.apollocache

import java.security.cert.CertificateException
import javax.net.ssl.SSLContext
import javax.net.ssl.SSLSocketFactory
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager

fun getSsl() =
    Pair(
        FakeX509TrustManager(),
        getUnsafeSocketFactoryForDevelopment().first
    )

fun getUnsafeSocketFactoryForDevelopment(): Pair<SSLSocketFactory, Array<TrustManager>> {

    val trustAllCerts = arrayOf<TrustManager>(object : X509TrustManager {
        @Throws(CertificateException::class)
        override fun checkClientTrusted(
            chain: Array<java.security.cert.X509Certificate>,
            authType: String
        ) {
        }

        @Throws(CertificateException::class)
        override fun checkServerTrusted(
            chain: Array<java.security.cert.X509Certificate>,
            authType: String
        ) {
        }

        override fun getAcceptedIssuers(): Array<java.security.cert.X509Certificate> {
            return arrayOf()
        }
    })

    val sslContext = SSLContext.getInstance("SSL")
    sslContext.init(null, trustAllCerts, java.security.SecureRandom())

    return (sslContext.socketFactory to trustAllCerts)
}

class FakeX509TrustManager : X509TrustManager {
    @Throws(CertificateException::class)
    override fun checkClientTrusted(
        arg0: Array<java.security.cert.X509Certificate?>?,
        arg1: String?
    ) {
    }

    @Throws(CertificateException::class)
    override fun checkServerTrusted(
        arg0: Array<java.security.cert.X509Certificate?>?,
        arg1: String?
    ) {
        // do nothing
    }

    override fun getAcceptedIssuers() =
        _acceptedIssuers

    companion object {
        private val _acceptedIssuers: Array<java.security.cert.X509Certificate> = arrayOf()
    }
}