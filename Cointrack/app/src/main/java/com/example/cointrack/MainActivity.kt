package com.example.cointrack.ui

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.example.cointrack.R
import com.example.cointrack.data.Transaction
import java.util.Calendar
import java.util.Locale
import android.text.InputType
import java.text.SimpleDateFormat
import android.text.TextWatcher
import android.text.Editable


class MainActivity : AppCompatActivity() {

    private val transactionViewModel: TransactionViewModel by viewModels()
    private lateinit var transactionAdapter: TransactionAdapter

    // UI 元件
    private lateinit var todayExpenseTextView: TextView
    private lateinit var monthRemainingTextView: TextView
    private lateinit var fabAddTransaction: FloatingActionButton
    private lateinit var recyclerView: RecyclerView
    private lateinit var btnAnalysis: Button
    private lateinit var btnSetBudget: Button

    private lateinit var etSearchQuery: EditText


    private val categories = listOf("餐飲", "交通", "娛樂", "學習", "其他")
    private val dateTimeFormat = SimpleDateFormat("yyyy/MM/dd HH:mm", Locale.getDefault())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        todayExpenseTextView = findViewById(R.id.tv_today_expense)
        monthRemainingTextView = findViewById(R.id.tv_month_remaining)
        fabAddTransaction = findViewById(R.id.fab_add_transaction)
        recyclerView = findViewById(R.id.recyclerview_transactions)
        btnAnalysis = findViewById(R.id.btn_analysis)
        btnSetBudget = findViewById(R.id.btn_set_budget)

        etSearchQuery = findViewById(R.id.et_search_query)

        setupRecyclerView()
        observeViewModel()
        setupListeners()
        setupSearchListener()
    }

    private fun setupRecyclerView() {

        transactionAdapter = TransactionAdapter(onLongClick = { transaction ->
            showDeleteConfirmationDialog(transaction)
        })
        recyclerView.adapter = transactionAdapter
    }

    private fun observeViewModel() {
        transactionViewModel.todayTotalExpense.observe(this, Observer { expense ->
            val expenseValue = expense ?: 0.0
            todayExpenseTextView.text = String.format(Locale.getDefault(), "$%.2f", expenseValue)
            resources.getColor(R.color.red, theme)
        })

        transactionViewModel.monthRemaining.observe(this, Observer { remaining ->
            monthRemainingTextView.text = String.format(Locale.getDefault(), "$%.2f", remaining)
            val colorId = if (remaining >= 0) R.color.green else R.color.red
            monthRemainingTextView.setTextColor(resources.getColor(colorId, theme))
        })


        transactionViewModel.filteredTransactions.observe(this, Observer { transactions ->
            transactions?.let {
                transactionAdapter.submitList(it)
            }
        })
    }


    private fun setupSearchListener() {
        etSearchQuery.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

                transactionViewModel.setSearchQuery(s.toString())
            }

            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun setupListeners() {
        fabAddTransaction.setOnClickListener {
            showAddTransactionDialog()
        }

        btnAnalysis.setOnClickListener {
            val intent = Intent(this, ChartActivity::class.java)
            startActivity(intent)
        }

        btnSetBudget.setOnClickListener {
            showSetBudgetDialog()
        }

    }


    private fun showDeleteConfirmationDialog(transaction: Transaction) {
        val amountDisplay = if (transaction.isExpense) {
            "支出 -${String.format("%.2f", transaction.amount)}"
        } else {
            "收入 +${String.format("%.2f", transaction.amount)}"
        }

        AlertDialog.Builder(this)
            .setTitle("確認刪除紀錄")
            .setMessage("您確定要刪除 [${transaction.category}: ${amountDisplay}] 這筆紀錄嗎？")
            .setPositiveButton("刪除", DialogInterface.OnClickListener { dialog, which ->
                transactionViewModel.deleteTransaction(transaction) // 呼叫 ViewModel 刪除
                Toast.makeText(this, "紀錄已刪除", Toast.LENGTH_SHORT).show()
            })
            .setNegativeButton("取消", null)
            .show()
    }


    private fun showSetBudgetDialog() {
        val etBudget = EditText(this).apply {
            inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL
            hint = "輸入本月預算金額 (例如: 15000)"
            val currentBudgetAmount = transactionViewModel.currentBudget.value?.amount ?: 0.0
            if (currentBudgetAmount > 0) {
                setText(currentBudgetAmount.toString())
            }
        }

        AlertDialog.Builder(this)
            .setTitle("設定本月預算")
            .setView(etBudget)
            .setPositiveButton("儲存", DialogInterface.OnClickListener { dialog, which ->
                val amountText = etBudget.text.toString()
                val amount = amountText.toDoubleOrNull()

                if (amount == null || amount < 0) {
                    Toast.makeText(this, "預算金額無效，必須是數字且不能為負值", Toast.LENGTH_SHORT).show()
                    return@OnClickListener
                }

                transactionViewModel.saveBudget(amount)

                Toast.makeText(this, "本月預算已設定為 $${String.format("%.2f", amount)}", Toast.LENGTH_SHORT).show()
            })
            .setNegativeButton("取消", null)
            .show()
    }


    private fun showAddTransactionDialog() {
        val container = LayoutInflater.from(this).inflate(R.layout.dialog_add_transaction, null)
        val etAmount = container.findViewById<EditText>(R.id.et_amount)
        val rgType = container.findViewById<RadioGroup>(R.id.rg_transaction_type)
        val rgCategory = container.findViewById<RadioGroup>(R.id.rg_category)
        val etCustomCategory = container.findViewById<EditText>(R.id.et_custom_category) // 取得自定義輸入框

        val tvSelectedDate = container.findViewById<TextView>(R.id.tv_selected_date)
        val btnSelectTime = container.findViewById<Button>(R.id.btn_select_time)

        val selectedCalendar = Calendar.getInstance()

        tvSelectedDate.text = dateTimeFormat.format(selectedCalendar.time)

        btnSelectTime.setOnClickListener {

            val datePickerDialog = DatePickerDialog(
                this,
                { _, year, month, dayOfMonth ->
                    selectedCalendar.set(Calendar.YEAR, year)
                    selectedCalendar.set(Calendar.MONTH, month)
                    selectedCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)

                    val timePickerDialog = TimePickerDialog(
                        this,
                        { _, hourOfDay, minute ->
                            selectedCalendar.set(Calendar.HOUR_OF_DAY, hourOfDay)
                            selectedCalendar.set(Calendar.MINUTE, minute)
                            tvSelectedDate.text = dateTimeFormat.format(selectedCalendar.time)
                        },
                        selectedCalendar.get(Calendar.HOUR_OF_DAY),
                        selectedCalendar.get(Calendar.MINUTE),
                        false
                    )
                    timePickerDialog.show()
                },
                selectedCalendar.get(Calendar.YEAR),
                selectedCalendar.get(Calendar.MONTH),
                selectedCalendar.get(Calendar.DAY_OF_MONTH)
            )
            datePickerDialog.show()
        }

        rgType.check(R.id.rb_expense)


        categories.forEachIndexed { index, category ->
            val rb = RadioButton(this).apply {
                id = index + 100
                text = category
            }
            rgCategory.addView(rb)
        }

        rgCategory.check(categories.firstOrNull()?.let { 100 } ?: -1)


        rgCategory.setOnCheckedChangeListener { group, checkedId ->
            val selectedCategoryText = group.findViewById<RadioButton>(checkedId)?.text?.toString()
            if (selectedCategoryText == "其他") {
                etCustomCategory.visibility = View.VISIBLE
            } else {
                etCustomCategory.visibility = View.GONE
                etCustomCategory.setText("")
            }
        }


        AlertDialog.Builder(this)
            .setTitle("快速記帳")
            .setView(container)
            .setPositiveButton("儲存", DialogInterface.OnClickListener { dialog, which ->
                val amountText = etAmount.text.toString()
                val amount = amountText.toDoubleOrNull()

                if (amount == null || amount <= 0) {
                    Toast.makeText(this, "請輸入有效金額", Toast.LENGTH_SHORT).show()
                    return@OnClickListener
                }

                val isExpense = rgType.checkedRadioButtonId == R.id.rb_expense


                val checkedCategoryIndex = rgCategory.checkedRadioButtonId - 100
                val selectedCategory = if (checkedCategoryIndex in categories.indices) {
                    categories[checkedCategoryIndex]
                } else {
                    ""
                }


                val finalCategory = if (selectedCategory == "其他" && etCustomCategory.text.isNotBlank()) {
                    etCustomCategory.text.toString()
                } else if (selectedCategory == "其他") {
                    "其他 (未定義)"
                } else {
                    selectedCategory
                }

                if (finalCategory.isBlank()) {
                    Toast.makeText(this, "請選擇或輸入類別名稱", Toast.LENGTH_SHORT).show()
                    return@OnClickListener
                }

                val transactionTimestamp = selectedCalendar.timeInMillis

                val newTransaction = Transaction(
                    amount = amount,
                    isExpense = isExpense,
                    category = finalCategory,
                    timestamp = transactionTimestamp
                )

                transactionViewModel.addTransaction(newTransaction)

                Toast.makeText(this, "新增成功：${finalCategory} ${if (isExpense) "-" else "+"}${amount}", Toast.LENGTH_LONG).show()
            })
            .setNegativeButton("取消", null)
            .show()
    }
}