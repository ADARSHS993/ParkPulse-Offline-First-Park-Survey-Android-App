
package com.example.parkplus.di

import com.example.parkplus.data.remote.FakeMockSyncServer
import com.example.parkplus.data.remote.MockSyncServer
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class MockSyncModule {

    @Binds
    @Singleton
    abstract fun bindMockSyncServer(
        implementation: FakeMockSyncServer
    ): MockSyncServer
}
