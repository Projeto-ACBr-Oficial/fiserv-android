package com.mjtech.store

import android.app.Application
import coil3.ImageLoader
import coil3.SingletonImageLoader
import coil3.request.crossfade
import com.mjtech.fiserv.msitef.di.msitefModule
import com.mjtech.store.data.mock.di.mockDataModule
import com.mjtech.store.di.appModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class OpenStoreApplication : Application(), SingletonImageLoader.Factory {

    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@OpenStoreApplication)
            modules(
                appModule,
                mockDataModule,
                msitefModule
            )
        }
    }

    override fun newImageLoader(context: android.content.Context): ImageLoader {
        return ImageLoader.Builder(context)
            .crossfade(true)
            .build()
    }
}