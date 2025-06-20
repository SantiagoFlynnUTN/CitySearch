package com.flynn.citysearch.data.remote.di

import com.flynn.citysearch.BuildConfig
import com.flynn.citysearch.data.remote.CityApiService
import com.flynn.citysearch.data.remote.NominatimApiService
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideMoshi(): Moshi {
        return Moshi.Builder()
            .add(KotlinJsonAdapterFactory())
            .build()
    }

    @Provides
    @Singleton
    fun provideHttpClient(): OkHttpClient {
        val logging = HttpLoggingInterceptor()
        logging.level = HttpLoggingInterceptor.Level.BODY

        return OkHttpClient.Builder()
            .addInterceptor(logging)
            .build()
    }

    @Provides
    @Singleton
    fun provideRetrofit(
        moshi: Moshi,
        client: OkHttpClient
    ): Retrofit {
        return Retrofit.Builder()
            .baseUrl(BuildConfig.CITIES_API_URL)
            .client(client)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
    }

    @Provides
    @Singleton
    fun provideCityApiService(retrofit: Retrofit): CityApiService {
        return retrofit.create(CityApiService::class.java)
    }

    @Provides
    @Singleton
    fun provideNominatimApiService(
        moshi: Moshi
    ): NominatimApiService {
        return Retrofit.Builder()
            .baseUrl(NOMINATIM_BASE_URL)
            .client(
                OkHttpClient.Builder()
                    .addInterceptor { chain ->
                        val request = chain.request().newBuilder()
                            .header(USER_AGENT_HEADER, USER_AGENT_VALUE)
                            .build()
                        chain.proceed(request)
                    }
                    .build()
            )
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(NominatimApiService::class.java)
    }

    private const val NOMINATIM_BASE_URL = "https://nominatim.openstreetmap.org/"
    private const val USER_AGENT_HEADER = "User-Agent"
    private const val USER_AGENT_VALUE = "CitySearchApp/1.0"
}
