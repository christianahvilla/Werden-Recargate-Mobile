package com.mirecargas.Sqlite

import android.content.ContentValues
import android.database.sqlite.SQLiteDatabase
import com.mirecargas.Models.Customer

class CustomerDB(private val queries: Queries = Queries(), private val const: Const = Const()) {

    fun saveCustomer(customer: Customer, setDB: SetDB){
        val db: SQLiteDatabase = setDB.writableDatabase
        val values = ContentValues()
        values.put(const.NAME, customer.name)
        values.put(const.LAST, customer.last)
        values.put(const.NUMBER, customer.number)
        values.put(const.CARRIER, customer.carrier)

        db.insert(const.CUSTOMER_TABLE, null,values)
        db.close()
    }

    fun getCustomer(setDB: SetDB): ArrayList<Customer>{
        val listCustomer = ArrayList<Customer>()
        val db = setDB.writableDatabase
        val cursor = db.rawQuery(queries.GET_CUSTOMER, null)
        if(cursor.moveToFirst()){
            do{
                val customer = Customer()
                customer.id = cursor.getInt(cursor.getColumnIndex(const.ID)).toString()
                customer.name = cursor.getString(cursor.getColumnIndex(const.NAME))
                customer.last = cursor.getString(cursor.getColumnIndex(const.LAST))
                customer.number = cursor.getString(cursor.getColumnIndex(const.NUMBER))
                customer.carrier = cursor.getString(cursor.getColumnIndex(const.CARRIER))
                listCustomer.add(customer)
            }while (cursor.moveToNext())
        }
        cursor.close()
        db.close()
        return listCustomer
    }

    fun findNumber(number: String,setDB: SetDB): Boolean{
        val db = setDB.writableDatabase
        val cursor = db.rawQuery(queries.FIND_NUMBER + number, null)
        if(cursor.moveToFirst()){
            cursor.close()
            db.close()
            return true
        }
        cursor.close()
        db.close()
        return false
    }

    fun delCustomer(customer: Customer, setDB: SetDB){
        val db = setDB.writableDatabase

        db.delete(const.CUSTOMER_TABLE, "id=?", arrayOf(customer.id))

        db.close()
    }
}