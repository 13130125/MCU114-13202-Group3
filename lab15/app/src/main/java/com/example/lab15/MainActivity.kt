package com.example.lab15

import android.database.sqlite.SQLiteDatabase
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ListView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.lad15.MyDBHelper

class MainActivity : AppCompatActivity() {
    private lateinit var dbHelper: MyDBHelper
    private lateinit var db: SQLiteDatabase // [cite: 159]
    private lateinit var adapter: ArrayAdapter<String>
    private val items = ArrayList<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        dbHelper = MyDBHelper(this)
        db = dbHelper.writableDatabase

        val edBrand = findViewById<EditText>(R.id.edBrand)
        val edYear = findViewById<EditText>(R.id.edYear)
        val edPrice = findViewById<EditText>(R.id.edPrice)
        val listView = findViewById<ListView>(R.id.listView)

        adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, items)
        listView.adapter = adapter




        findViewById<Button>(R.id.btnInsert).setOnClickListener {
            if (edBrand.text.isEmpty() || edYear.text.isEmpty() || edPrice.text.isEmpty()) {
                Toast.makeText(this, "請輸入完整資料", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            try {

                db.execSQL("INSERT INTO ${MyDBHelper.TABLE_NAME} (brand, year, price) VALUES (?, ?, ?)",
                    arrayOf(edBrand.text.toString(), edYear.text.toString(), edPrice.text.toString()))

                Toast.makeText(this, "新增成功: ${edBrand.text}", Toast.LENGTH_SHORT).show()
                cleanEditText(edBrand, edYear, edPrice)
            } catch (e: Exception) {
                Toast.makeText(this, "新增失敗 (可能廠牌重複)", Toast.LENGTH_SHORT).show()
            }
        }


        findViewById<Button>(R.id.btnUpdate).setOnClickListener {
            if (edBrand.text.isEmpty() || edYear.text.isEmpty() || edPrice.text.isEmpty()) {
                Toast.makeText(this, "請輸入要修改的廠牌及新資料", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            try {
                db.execSQL("UPDATE ${MyDBHelper.TABLE_NAME} SET year = ?, price = ? WHERE brand = ?",
                    arrayOf(edYear.text.toString(), edPrice.text.toString(), edBrand.text.toString()))

                Toast.makeText(this, "修改成功", Toast.LENGTH_SHORT).show()
                cleanEditText(edBrand, edYear, edPrice)
            } catch (e: Exception) {
                Toast.makeText(this, "修改失敗", Toast.LENGTH_SHORT).show()
            }
        }


        findViewById<Button>(R.id.btnDelete).setOnClickListener {
            if (edBrand.text.isEmpty()) {
                Toast.makeText(this, "請輸入要刪除的廠牌", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            try {
                db.execSQL("DELETE FROM ${MyDBHelper.TABLE_NAME} WHERE brand = ?",
                    arrayOf(edBrand.text.toString()))

                Toast.makeText(this, "刪除成功", Toast.LENGTH_SHORT).show()
                cleanEditText(edBrand, edYear, edPrice)
            } catch (e: Exception) {
                Toast.makeText(this, "刪除失敗", Toast.LENGTH_SHORT).show()
            }
        }

        findViewById<Button>(R.id.btnQuery).setOnClickListener {
            val c = if (edBrand.text.isEmpty()) {

                db.rawQuery("SELECT * FROM ${MyDBHelper.TABLE_NAME}", null)
            } else {

                db.rawQuery("SELECT * FROM ${MyDBHelper.TABLE_NAME} WHERE brand = ?", arrayOf(edBrand.text.toString()))
            }

            items.clear()
            c.moveToFirst()


            for (i in 0 until c.count) {

                items.add("廠牌:${c.getString(0)}\t年份:${c.getInt(1)}\t價格:${c.getInt(2)}")
                c.moveToNext()
            }
            adapter.notifyDataSetChanged()
            c.close()
        }
    }

    private fun cleanEditText(vararg edits: EditText) {
        for (edit in edits) edit.setText("")
    }

    override fun onDestroy() {
        super.onDestroy()
        db.close()
    }
}