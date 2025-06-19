package com.flynn.citysearch.data.repository.di

import com.flynn.citysearch.data.repository.CityRepository
import com.flynn.citysearch.data.repository.CityRepositoryInterface
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindCityRepository(
        cityRepository: CityRepository
    ): CityRepositoryInterface
}