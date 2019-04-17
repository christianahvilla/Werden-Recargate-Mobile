package com.mirecargas.Sqlite

import android.content.ContentValues
import android.database.sqlite.SQLiteDatabase
import com.mirecargas.Models.User

class UserDB(private val const: Const = Const(), private val queries: Queries = Queries()) {

    fun saveUser(user: User, setDB: SetDB){
        val db: SQLiteDatabase = setDB.writableDatabase
        val values = ContentValues()
        values.put(const.ROOT, user.root)

        db.insert(const.USER_TABLE, null,values)
        db.close()
    }

    fun getUser(setDB: SetDB): ArrayList<User>{
        val listUser = ArrayList<User>()
        val db = setDB.writableDatabase
        val cursor = db.rawQuery(queries.GET_USER, null)
        if(cursor.moveToFirst()){
            do{
                val user = User()
                user.id = cursor.getInt(cursor.getColumnIndex(const.ID)).toString()
                user.root = cursor.getString(cursor.getColumnIndex(const.ROOT)).toString()
                listUser.add(user)
            }while (cursor.moveToNext())
        }
        cursor.close()
        db.close()
        return listUser
    }

    fun updateUser(user:User, setDB: SetDB){
        val db = setDB.writableDatabase
        val values = ContentValues()
        values.put(const.ROOT, user.root)
        db.update(const.USER_TABLE, values, "id =?", arrayOf(user.id))
        db.close()
    }
}