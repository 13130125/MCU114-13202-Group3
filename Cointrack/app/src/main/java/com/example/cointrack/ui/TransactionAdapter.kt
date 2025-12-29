package com.example.cointrack.ui

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.cointrack.R
import com.example.cointrack.data.Transaction
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


class TransactionAdapter(private val onLongClick: (Transaction) -> Unit) :
    ListAdapter<Transaction, TransactionAdapter.TransactionViewHolder>(TransactionDiff()) {


    private val dateFormat = SimpleDateFormat("yyyy/MM/dd HH:mm", Locale.getDefault())

    inner class TransactionViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val categoryIcon: TextView = itemView.findViewById(R.id.tv_category_icon)
        val categoryName: TextView = itemView.findViewById(R.id.tv_category_name)
        val transactionDate: TextView = itemView.findViewById(R.id.tv_transaction_date)
        val amount: TextView = itemView.findViewById(R.id.tv_amount)

        fun bind(transaction: Transaction) {

            itemView.setOnLongClickListener {
                onLongClick(transaction)
                true
            }


            categoryName.text = transaction.category
            categoryIcon.text = transaction.category.firstOrNull()?.toString() ?: "?"


            val amountText = if (transaction.isExpense) {
                amount.setTextColor(Color.parseColor("#D32F2F")) // 紅色 (支出)
                String.format(Locale.getDefault(), "-%.2f", transaction.amount)
            } else {
                amount.setTextColor(Color.parseColor("#388E3C")) // 綠色 (收入)
                String.format(Locale.getDefault(), "+%.2f", transaction.amount)
            }
            amount.text = amountText

            // 設置時間
            transactionDate.text = dateFormat.format(Date(transaction.timestamp))
        }


    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TransactionViewHolder {

        val view: View = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_transaction, parent, false)
        return TransactionViewHolder(view)
    }

    override fun onBindViewHolder(holder: TransactionViewHolder, position: Int) {
        val current = getItem(position)
        holder.bind(current)
    }

    class TransactionDiff : DiffUtil.ItemCallback<Transaction>() {
        override fun areItemsTheSame(oldItem: Transaction, newItem: Transaction): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Transaction, newItem: Transaction): Boolean {
            return oldItem == newItem
        }
    }
}