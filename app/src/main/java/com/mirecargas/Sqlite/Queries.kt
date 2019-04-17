package com.mirecargas.Sqlite

class Queries {

    val CREATE_TABLE_CUSTOMER =
        "CREATE TABLE `customer` (\n" +
            "\t`id`\tINTEGER PRIMARY KEY AUTOINCREMENT,\n" +
            "\t`name`\tTEXT,\n" +
            "\t`last`\tTEXT,\n" +
            "\t`number`\tTEXT,\n" +
            "\t`carrier`\tTEXT\n" +
                ");"


    val CREATE_TABLE_USER ="CREATE TABLE `user` (\n" +
            "\t`id`\tINTEGER PRIMARY KEY AUTOINCREMENT,\n" +
            "\t`root`\tINTEGER\n" +
            ");"

    val GET_CUSTOMER = "SELECT * FROM customer ORDER BY name"
    val GET_USER = "SELECT * FROM user"
    val FIND_NUMBER = "SELECT * FROM customer where number = "

}