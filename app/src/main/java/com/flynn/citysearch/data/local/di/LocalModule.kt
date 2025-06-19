package com.flynn.citysearch.data.local.di

import android.content.Context
import androidx.room.Room
import com.flynn.citysearch.data.local.AppDatabase
import com.flynn.citysearch.data.local.CityDao
import com.flynn.citysearch.data.local.LocalDataSource
import com.flynn.citysearch.data.local.LocalDataSourceImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object LocalModule {

    private const val DB_NAME = "cities.db"

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            DB_NAME
        ).build()
    }

    @Provides
    fun provideCityDao(db: AppDatabase): CityDao = db.cityDao()

    @Provides
    @Singleton
    fun provideLocalDataSource(
        cityDao: CityDao
    ): LocalDataSource = LocalDataSourceImpl(cityDao)
}
