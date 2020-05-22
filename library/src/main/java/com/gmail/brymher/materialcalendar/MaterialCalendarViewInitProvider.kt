package com.gmail.brymher.materialcalendar;

import android.content.ContentProvider
import android.content.ContentValues
import android.content.Context
import android.content.pm.ProviderInfo
import android.database.Cursor
import android.net.Uri
import com.jakewharton.threetenabp.AndroidThreeTen


/**
 * With this in place, we don't have to have the user install 310Abp by himself.
 */
class MaterialCalendarViewInitProvider : ContentProvider() {
    override fun onCreate(): Boolean {
        // The interesting piece here.
        AndroidThreeTen.init(context)
        return true
    }

    override fun attachInfo(
        context: Context,
        providerInfo: ProviderInfo
    ) {
        if (providerInfo == null) {
            throw NullPointerException("MaterialCalendarViewInitProvider ProviderInfo cannot be null.")
        }
        // So if the authorities equal the library internal ones, the developer forgot to set his applicationId
        check(MCV_AUTHORITY != providerInfo.authority) {
            ("Incorrect provider authority in manifest. Most likely due to a "
                    + "missing applicationId variable in application\'s build.gradle.")
        }
        super.attachInfo(context, providerInfo)
    }

    override fun query(
        uri: Uri,
        projection: Array<String>?,
        selection: String?,
        selectionArgs: Array<String>?,
        sortOrder: String?
    ): Cursor? {
        return null
    }

    override fun getType(uri: Uri): String? {
        return null
    }

    override fun insert(
        uri: Uri,
        values: ContentValues?
    ): Uri? {
        return null
    }

    override fun delete(
        uri: Uri,
        selection: String?,
        selectionArgs: Array<String>?
    ): Int {
        return 0
    }

    override fun update(
        uri: Uri,
        values: ContentValues?,
        selection: String?,
        selectionArgs: Array<String>?
    ): Int {
        return 0
    }

    companion object {
        private const val MCV_AUTHORITY =
            "com.gmail.brymher.materialcalender.materialcalendarviewinitprovider"
    }
}
