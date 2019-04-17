package com.mirecargas.Sqlite

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class SetDB(ctx: Context, private val queries: Queries = Queries()) : SQLiteOpenHelper(ctx, "recargate", null, 1) {

    companion object {
        private var instance: SetDB? = null

        @Synchronized
        fun getInstance(ctx: Context): SetDB {
            if (instance == null) {
                instance = SetDB(ctx.applicationContext)
            }
            return instance!!
        }
    }

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(queries.CREATE_TABLE_CUSTOMER)
        db.execSQL(queries.CREATE_TABLE_USER)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {

    }

}

// Access property for Context
val Context.database: SetDB
    get() = SetDB.getInstance(applicationContext)
