package com.example.mainactivity

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity


class MainActivity : AppCompatActivity() {
    // 宣告全域變數
    private var txtShow: TextView? = null
    private var btnZero: Button? = null
    private var btnOne: Button? = null
    private var btnTwo: Button? = null
    private var btnThree: Button? = null
    private var btnFour: Button? = null
    private var btnFive: Button? = null
    private var btnSix: Button? = null
    private var btnSeven: Button? = null
    private var btnEight: Button? = null
    private var btnNine: Button? = null
    private var btnClear: Button? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // 取得資源類別檔中的介面元件
        txtShow = findViewById<View?>(R.id.txtShow) as TextView
        btnZero = findViewById<View?>(R.id.btnZero) as Button
        btnOne = findViewById<View?>(R.id.btnOne) as Button
        btnTwo = findViewById<View?>(R.id.btnTwo) as Button
        btnThree = findViewById<View?>(R.id.btnThree) as Button
        btnFour = findViewById<View?>(R.id.btnFour) as Button
        btnFive = findViewById<View?>(R.id.btnFive) as Button
        btnSix = findViewById<View?>(R.id.btnSix) as Button
        btnSeven = findViewById<View?>(R.id.btnSeven) as Button
        btnEight = findViewById<View?>(R.id.btnEight) as Button
        btnNine = findViewById<View?>(R.id.btnNine) as Button
        btnClear = findViewById<View?>(R.id.btnClear) as Button

        // 設定 button 元件 Click 事件共用   myListner
        btnZero!!.setOnClickListener(myListner)
        btnOne!!.setOnClickListener(myListner)
        btnTwo!!.setOnClickListener(myListner)
        btnThree!!.setOnClickListener(myListner)
        btnFour!!.setOnClickListener(myListner)
        btnFive!!.setOnClickListener(myListner)
        btnSix!!.setOnClickListener(myListner)
        btnSeven!!.setOnClickListener(myListner)
        btnEight!!.setOnClickListener(myListner)
        btnNine!!.setOnClickListener(myListner)
        btnClear!!.setOnClickListener(myListner)
    }

    // 定義  onClick() 方法
    private val myListner: View.OnClickListener = object : View.OnClickListener {
        override fun onClick(v: View) {
            val s = txtShow!!.getText().toString()
            when (v.getId()) {
                R.id.btnZero -> {
                    txtShow!!.setText(s + "0")
                }

                R.id.btnOne -> {
                    txtShow!!.setText(s + "1")
                }

                R.id.btnTwo -> {
                    txtShow!!.setText(s + "2")
                }

                R.id.btnThree -> {
                    txtShow!!.setText(s + "3")
                }

                R.id.btnFour -> {
                    txtShow!!.setText(s + "4")
                }

                R.id.btnFive -> {
                    txtShow!!.setText(s + "5")
                }

                R.id.btnSix -> {
                    txtShow!!.setText(s + "6")
                }

                R.id.btnSeven -> {
                    txtShow!!.setText(s + "7")
                }

                R.id.btnEight -> {
                    txtShow!!.setText(s + "8")
                }

                R.id.btnNine -> {
                    txtShow!!.setText(s + "9")
                }

                R.id.btnClear -> {
                    txtShow!!.setText("電話號碼：")
                }
            }
        }
    }
}