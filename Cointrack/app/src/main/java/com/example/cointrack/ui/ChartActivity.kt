package com.example.cointrack.ui

import android.graphics.Color
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import com.example.cointrack.R
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.formatter.PercentFormatter
import java.util.Locale


class ChartActivity : AppCompatActivity() {


    private val transactionViewModel: TransactionViewModel by viewModels()
    private lateinit var pieChart: PieChart

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chart)


        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "支出分析"


        pieChart = findViewById(R.id.pie_chart)

        setupPieChart()
        observeMonthlyExpense()
    }


    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }



    private fun setupPieChart() {
        pieChart.setUsePercentValues(true)
        pieChart.description.isEnabled = false
        pieChart.setExtraOffsets(5f, 10f, 5f, 5f)
        pieChart.dragDecelerationFrictionCoef = 0.95f
        pieChart.isDrawHoleEnabled = true
        pieChart.setHoleColor(Color.WHITE)
        pieChart.transparentCircleRadius = 61f

        pieChart.setDrawEntryLabels(true)
        pieChart.setEntryLabelColor(Color.BLACK)
        pieChart.setEntryLabelTextSize(12f)

        pieChart.legend.isEnabled = true
    }



    private fun observeMonthlyExpense() {
        transactionViewModel.getMonthlyCategoryExpense().observe(this, Observer { categoryExpenses ->
            if (categoryExpenses.isEmpty()) {
                pieChart.clear()
                pieChart.centerText = "本月無支出數據"
                pieChart.invalidate()
                return@Observer
            }


            val entries = ArrayList<PieEntry>()
            var totalExpense = 0.0

            categoryExpenses.forEach { (_, amount) ->
                totalExpense += amount
            }

            categoryExpenses.forEach { (category, amount) ->
                val percentage = (amount / totalExpense * 100).toFloat()
                entries.add(PieEntry(percentage, category))
            }

            val dataSet = PieDataSet(entries, "支出類別").apply {
                sliceSpace = 3f
                selectionShift = 5f
                setColors(
                    Color.rgb(255, 102, 0),
                    Color.rgb(51, 153, 255),
                    Color.rgb(153, 51, 255),
                    Color.rgb(50, 205, 50),
                    Color.rgb(204, 204, 204),
                    Color.rgb(255, 204, 51)
                )

                xValuePosition = PieDataSet.ValuePosition.INSIDE_SLICE
                yValuePosition = PieDataSet.ValuePosition.INSIDE_SLICE

            }

            val data = PieData(dataSet).apply {
                setValueFormatter(PercentFormatter(pieChart))
                setValueTextSize(14f)
                setValueTextColor(Color.BLACK)
            }

            pieChart.data = data
            pieChart.centerText = String.format(Locale.getDefault(), "本月總支出\n$%.2f", totalExpense)
            pieChart.invalidate()
        })
    }
}