package ir.sbu.nearbyplaces.api

import android.util.Log
import ir.sbu.nearbyplaces.utils.LiveDataCallAdapterFactory
import ir.sbu.nearbyplacesmap.utils.BASE_URL
import okhttp3.OkHttpClient

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitBuilder
{
    private val httpClient: OkHttpClient by lazy {
        val httpClientBuilder = OkHttpClient.Builder()
            .addInterceptor{chain ->
                val original = chain.request()
                val requestBuilder = original.newBuilder()
                requestBuilder.method(original.method(), original.body())
                val request = requestBuilder.build()
                Log.d(
                    "RETROFIT_REQUEST",
                    "request is:$request"
                )
                chain.proceed(request)
            }
        return@lazy httpClientBuilder.build()
    }

    private val retrofitBuilder : Retrofit.Builder by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .addCallAdapterFactory(LiveDataCallAdapterFactory())
            .client(httpClient)
    }

    val apiService : ApiService by lazy {
        retrofitBuilder
            .build()
            .create(ApiService::class.java)
    }
}