package com.dharmikgohil.spendra

import android.app.Application
import com.dharmikgohil.spendra.data.local.AppDatabase

class SpendraApplication : Application() {
    val database by lazy { AppDatabase.getDatabase(this) }
}
