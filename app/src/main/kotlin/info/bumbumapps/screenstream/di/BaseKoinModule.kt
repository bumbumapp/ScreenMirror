package info.bumbumapps.screenstream.di

import androidx.datastore.core.handlers.ReplaceFileCorruptionHandler
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.preferencesDataStoreFile
import com.elvishew.xlog.XLog
import com.ironz.binaryprefs.BinaryPreferencesBuilder
import info.bumbumapps.screenstream.data.settings.Settings
import info.bumbumapps.screenstream.data.settings.SettingsImpl
import info.bumbumapps.screenstream.data.settings.SettingsReadOnly
import info.bumbumapps.screenstream.data.settings.old.SettingsDataMigration
import info.bumbumapps.screenstream.service.helper.NotificationHelper
import org.koin.android.ext.koin.androidApplication
import org.koin.dsl.bind
import org.koin.dsl.module

val baseKoinModule = module {

    single<com.ironz.binaryprefs.Preferences> {
        BinaryPreferencesBuilder(androidApplication())
            .supportInterProcess(true)
            .memoryCacheMode(BinaryPreferencesBuilder.MemoryCacheMode.EAGER)
            .exceptionHandler { ex -> XLog.e(ex) }
            .build()
    }

    single {
        PreferenceDataStoreFactory.create(
            corruptionHandler = ReplaceFileCorruptionHandler { ex -> XLog.e(ex); emptyPreferences() },
            migrations = listOf(SettingsDataMigration(androidApplication(), get())),
//            scope = CoroutineScope(Dispatchers.IO + SupervisorJob()),
            produceFile = { androidApplication().preferencesDataStoreFile("user_settings") }
        )
    }

    single<Settings> { SettingsImpl(get()) } bind SettingsReadOnly::class

    single { NotificationHelper(androidApplication()) }
}