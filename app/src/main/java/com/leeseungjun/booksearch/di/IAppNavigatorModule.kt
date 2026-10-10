package com.leeseungjun.booksearch.di

import com.leeseungjun.booksearch.navigation.AppNavigator
import core.navigation.INavigator
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityRetainedComponent

@Module
@InstallIn(ActivityRetainedComponent::class)
interface IAppNavigatorModule {
    @Binds
    fun bindNavigator(impl: AppNavigator): INavigator
}
