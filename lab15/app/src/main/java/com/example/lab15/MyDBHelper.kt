package com.example.lad15
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper


class MyDBHelper(context: Context) : SQLiteOpenHelper(context, "carDB.db", null, 1) {

    companion object {
        const val TABLE_NAME = "carTable" // 資料表名稱
    }

    override fun onCreate(db: SQLiteDatabase) {

        val sql = "CREATE TABLE IF NOT EXISTS $TABLE_NAME " +
                "(brand TEXT PRIMARY KEY NOT NULL, " +
                "year INTEGER NOT NULL, " +
                "price INTEGER NOT NULL)"
        db.execSQL(sql)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
       
        val sql = "DROP TABLE IF EXISTS $TABLE_NAME"
        db.execSQL(sql)
        onCreate(db)
    }
}