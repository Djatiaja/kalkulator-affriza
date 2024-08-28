package com.example.kalkulator4

import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.kalkulator4.databinding.ActivityMainBinding
import java.util.Stack

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private var currentInput: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupButtonListeners()
        setupWindowInsets()
    }

    private fun setupButtonListeners() {
        val buttonIds = listOf(
            R.id.n0, R.id.n1, R.id.n2, R.id.n3, R.id.n4,
            R.id.n5, R.id.n6, R.id.n7, R.id.n8, R.id.n9
        )

        for (id in buttonIds) {
            findViewById<Button>(id)?.setOnClickListener {
                onNumberClicked((it as Button).text.toString())
            }
        }

        binding.plus.setOnClickListener { onOperatorClicked("+") }
        binding.minus.setOnClickListener { onOperatorClicked("-") }
        binding.multiple.setOnClickListener { onOperatorClicked("*") }
        binding.div.setOnClickListener { onOperatorClicked("/") }
        binding.equal.setOnClickListener { calculateResult() }
        binding.restart.setOnClickListener { resetCalculator() }
        binding.hapus.setOnClickListener { onHapusClicked() }
    }

    private fun setupWindowInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun onNumberClicked(number: String) {
        currentInput += number
        binding.OPERASI.text = currentInput
    }

    private fun onOperatorClicked(op: String) {
        if (currentInput.isNotEmpty() && !currentInput.endsWith(" ")) {
            currentInput += " $op "
            binding.OPERASI.text = currentInput
        }
    }

    private fun calculateResult() {
        try {
            val postfix = infixToPostfix(currentInput)
            val result = evaluatePostfix(postfix)
            Toast.makeText(this, result.toString(), Toast.LENGTH_SHORT).show()
            binding.HASIL.text = result.toString()
            resetInput(result.toString())
        } catch (e: Exception) {
            binding.HASIL.text = "Error"
        }
    }

    private fun resetCalculator() {
        currentInput = ""
        binding.OPERASI.text = ""
        binding.HASIL.text = ""
    }

    private fun resetInput(result: String) {
        currentInput = result
        binding.OPERASI.text = currentInput
    }

    private fun onHapusClicked() {
        if (currentInput.isNotEmpty()) {
            currentInput = currentInput.trimEnd()
            val lastChar = currentInput.lastOrNull()

            if (lastChar == ' ') {
                currentInput = currentInput.dropLast(3) // Remove operator and surrounding spaces
            } else {
                currentInput = currentInput.dropLast(1)
            }

            // Ensure the last character is not an operator and there are no trailing spaces
            currentInput = currentInput.trimEnd()

            binding.OPERASI.text = currentInput
        }
    }

    private fun infixToPostfix(infix: String): List<String> {
        val precedence = mapOf(
            "+" to 1,
            "-" to 1,
            "*" to 2,
            "/" to 2
        )
        val output = mutableListOf<String>()
        val operators = Stack<String>()
        val tokens = infix.split(" ")

        for (token in tokens) {
            when {
                token.isNumber() -> output.add(token)
                token.isOperator() -> {
                    while (operators.isNotEmpty() && precedence[operators.peek()] ?: 0 >= precedence[token] ?: 0) {
                        output.add(operators.pop())
                    }
                    operators.push(token)
                }
            }
        }
        while (operators.isNotEmpty()) {
            output.add(operators.pop())
        }
        return output
    }

    private fun evaluatePostfix(postfix: List<String>): Double {
        val stack = Stack<Double>()

        for (token in postfix) {
            when {
                token.isNumber() -> stack.push(token.toDouble())
                token.isOperator() -> {
                    val b = stack.pop()
                    val a = stack.pop()
                    val result = when (token) {
                        "+" -> a + b
                        "-" -> a - b
                        "*" -> a * b
                        "/" -> a / b
                        else -> 0.0
                    }
                    stack.push(result)
                }
            }
        }
        return stack.pop()
    }

    private fun String.isNumber(): Boolean = this.toDoubleOrNull() != null
    private fun String.isOperator(): Boolean = this in listOf("+", "-", "*", "/")
}
