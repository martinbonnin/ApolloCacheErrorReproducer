package com.test.apollocache

import android.content.Context
import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.api.Operation
import com.apollographql.apollo.api.Query
import com.apollographql.apollo.api.cache.http.HttpCachePolicy
import com.apollographql.apollo.cache.http.ApolloHttpCache
import com.apollographql.apollo.cache.http.DiskLruHttpCacheStore
import com.apollographql.apollo.coroutines.toDeferred
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import java.io.File
import java.util.concurrent.TimeUnit
import javax.net.ssl.HostnameVerifier

fun createApollo(context: Context): ApolloClient {
    val cacheFile = File(context.cacheDir, APOLLO_CACHE_DIRECTORY_NAME)
    val cacheStore = DiskLruHttpCacheStore(cacheFile, CACHE_SIZE_IN_BYTES)

    return ApolloClient
        .builder()
        .serverUrl(BASE_APOLLO_URL)
        .httpCache(ApolloHttpCache(cacheStore))
        .okHttpClient(createApolloOkHttpClient())
        .build()
}

fun createApolloOkHttpClient(): OkHttpClient {
    val (manager, factory) = getSsl()
    val httpLoggingInterceptor = HttpLoggingInterceptor()
        .setLevel(HttpLoggingInterceptor.Level.BODY)
    val okHttpBuilder = OkHttpClient.Builder()
        .addInterceptor(httpLoggingInterceptor)
        .connectTimeout(CONNECTION_TIMEOUT, TimeUnit.MILLISECONDS)
        .readTimeout(READ_TIMEOUT, TimeUnit.MILLISECONDS)

    //okHttpBuilder.sslSocketFactory(factory, manager)
    //okHttpBuilder.hostnameVerifier(HostnameVerifier { _, _ -> true })
    return okHttpBuilder.build()
}

suspend fun <A : Operation.Data, B : Any, C : Operation.Variables> ApolloClient.runApiCall(
    call: suspend () -> Query<A, B, C>
): Flow<B?> = flow {

//    val queryCall = query(call.invoke()).toBuilder()
//        .httpCachePolicy(HttpCachePolicy.CACHE_FIRST)
//        .build()
//        .await()

    val queryCall = query(call.invoke())
        .httpCachePolicy(HttpCachePolicy.CACHE_FIRST)
        .toDeferred()
        .await()

    emit(queryCall.data)
}