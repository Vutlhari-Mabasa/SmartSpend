package com.example.savings.data

import android.annotation.SuppressLint
import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.text.InputType
import android.widget.*
import androidx.appcompat.app.AlertDialog
import com.google.firebase.auth.FirebaseAuth
import java.util.*

// Dialog for creating or editing an expense
class ExpenseFormDialog(
    context: Context,
    private val expense: Expense?,
    private val onSave: (Map<String, Any>) -> Unit,
    private val onPickImage: (((Uri) -> Unit) -> Unit)? = null // Callback for image picking
) : AlertDialog(context) {

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        // Layout for the dialog
        val layout = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(40, 40, 40, 40)
        }

        // Input for expense description
        val descriptionInput = EditText(context).apply { hint = "Description" }
        // Input for expense amount (in Rands)
        val amountInput = EditText(context).apply {
            hint = "Amount (R)"
            inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL
        }

        // List of categories for the spinner
        val categories = listOf(
            "Food",
            "Transport",
            "Health",
            "Shopping",
            "Housing Fund",
            "Emergency Fund",
            "Bills",
            "Entertainment",
            "Utilities",
            "Other"
        )
        // Spinner for selecting category
        val categorySpinner = Spinner(context).apply {
            adapter = ArrayAdapter(context, android.R.layout.simple_spinner_dropdown_item, categories)
        }

        // Date picker for expense date
        val datePicker = DatePicker(context)
        // Input for image URL (optional, filled by image picker)
        val imageInput = EditText(context).apply {
            hint = "Image URL (optional)"
            isFocusable = false
        }

        // Set up image picker click
        imageInput.setOnClickListener {
            onPickImage?.invoke { uri ->
                imageInput.setText(uri.toString())
            }
        }

        // Add all views to the layout
        layout.apply {
            addView(descriptionInput)
            addView(amountInput)
            addView(categorySpinner)
            addView(datePicker)
            addView(imageInput)
        }

        setView(layout)
        setTitle(if (expense == null) "Add Expense" else "Edit Expense")
        // Save button collects all input and calls onSave
        setButton(BUTTON_POSITIVE, "Save") { _, _ ->
            val cal = Calendar.getInstance()
            cal.set(datePicker.year, datePicker.month, datePicker.dayOfMonth)

            val expenseMap = mutableMapOf<String, Any>(
                "description" to descriptionInput.text.toString(),
                "amount" to amountInput.text.toString().toDouble(),
                "category" to categorySpinner.selectedItem.toString(),
                "date" to cal.timeInMillis,
                "userId" to (FirebaseAuth.getInstance().currentUser?.uid ?: "")
            )

            val imageUrl = imageInput.text.toString()
            if (imageUrl.isNotBlank()) {
                expenseMap["imageUrl"] = imageUrl
            }

            onSave(expenseMap)
        }
        setButton(BUTTON_NEGATIVE, "Cancel") { _, _ -> }
        super.onCreate(savedInstanceState)

        // If editing, pre-fill fields with existing expense data
        expense?.let {
            descriptionInput.setText(it.description)
            amountInput.setText(it.amount.toString())
            imageInput.setText(it.imageUrl ?: "")
            categorySpinner.setSelection(categories.indexOf(it.category))
            val cal = Calendar.getInstance().apply { timeInMillis = it.date }
            datePicker.updateDate(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH))
        }
    }
}
