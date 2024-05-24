package com.example.myapplication;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import java.util.Locale;
import java.util.Stack;

import androidx.activity.EdgeToEdge;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class MainActivity extends AppCompatActivity {
    private TextView display;
    // Numeric Buttons
    private Button btn0, btn1, btn2, btn3, btn4, btn5, btn6, btn7, btn8, btn9;
    // Operation Buttons
    private Button sum, diff, multiply, division, remainder, floatPoint, equal, clear, parentheses;
    // Other Variables
    private String fullExpression = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        initializeViews();
        setNumericButtonListeners();
        setOperationButtonListeners();
    }
    private void initializeViews() {
        display = findViewById(R.id.display);
        btn0 = findViewById(R.id.btn0);
        btn1 = findViewById(R.id.btn1);
        btn2 = findViewById(R.id.btn2);
        btn3 = findViewById(R.id.btn3);
        btn4 = findViewById(R.id.btn4);
        btn5 = findViewById(R.id.btn5);
        btn6 = findViewById(R.id.btn6);
        btn7 = findViewById(R.id.btn7);
        btn8 = findViewById(R.id.btn8);
        btn9 = findViewById(R.id.btn9);
        sum = findViewById(R.id.sum);
        diff = findViewById(R.id.diff);
        multiply = findViewById(R.id.multiply);
        division = findViewById(R.id.division);
        remainder = findViewById(R.id.remainder);
        floatPoint = findViewById(R.id.float1);
        equal = findViewById(R.id.equal);
        clear = findViewById(R.id.clear);
    }

    private void setNumericButtonListeners() {
        View.OnClickListener listener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Button button = (Button) v;
                fullExpression += button.getText().toString();
                display.setText(fullExpression);
            }
        };

        btn0.setOnClickListener(listener);
        btn1.setOnClickListener(listener);
        btn2.setOnClickListener(listener);
        btn3.setOnClickListener(listener);
        btn4.setOnClickListener(listener);
        btn5.setOnClickListener(listener);
        btn6.setOnClickListener(listener);
        btn7.setOnClickListener(listener);
        btn8.setOnClickListener(listener);
        btn9.setOnClickListener(listener);
        floatPoint.setOnClickListener(listener);
    }
    private void setOperationButtonListeners() {
        Button addButton = findViewById(R.id.sum);
        Button subtractButton = findViewById(R.id.diff);
        Button multiplyButton = findViewById(R.id.multiply);
        Button divideButton = findViewById(R.id.division);
        Button remainderButton = findViewById(R.id.remainder);
        Button clearButton = findViewById(R.id.clear);
        Button equalsButton = findViewById(R.id.equal);

        addButton.setOnClickListener(v -> performOperation("+"));

        subtractButton.setOnClickListener(v -> performOperation("-"));

        multiplyButton.setOnClickListener(v -> performOperation("*"));

        divideButton.setOnClickListener(v -> performOperation("/"));

        remainderButton.setOnClickListener(v -> performOperation("%"));


        equalsButton.setOnClickListener(v -> {
            processEquals();
        });

        clearButton.setOnClickListener(v -> {
            display.setText("0");
            fullExpression = "";
        });
    }

    private void processEquals() {
        if (!isValidExpression(fullExpression)) {
            display.setText("Error");
        } else {
            try {
                double result = evaluateFullExpression(fullExpression);
                display.setText(String.format(Locale.getDefault(), "%.2f", result)); // Format for readability
            } catch (Exception e) {
                display.setText("Error");
            }
        }
        fullExpression = ""; // Reset fullExpression
    }

    private boolean isValidExpression(String expression) {
        if (expression == null || expression.isEmpty()) return false;

        // Check for consecutive operators (other than negation)
        if (expression.matches(".*[+*/%]{2,}.*") || expression.matches("^[+*/%].*") || expression.matches(".*[+*/%]$")) {
            return false;
        }

        // Check for improperly placed decimal points
        String[] parts = expression.split("[+*/%-]");
        for (String part : parts) {
            if (part.isEmpty()) continue; // This allows for the "-" to be at the start (negative number)
            if (part.indexOf('.') != part.lastIndexOf('.')) { // More than one decimal point in a number
                return false;
            }
        }

        return true;
    }
    private double evaluateFullExpression(String expression) {
        Stack<Double> values = new Stack<>();
        Stack<Character> ops = new Stack<>();

        for (int i = 0; i < expression.length(); i++) {
            char c = expression.charAt(i);

            if (Character.isDigit(c) || c == '.' ||
                    (c == '-' && (i == 0 || "+-*/%".indexOf(expression.charAt(i - 1)) != -1))) {
                StringBuilder sb = new StringBuilder();
                // Include the negative sign in the number if it's a unary operator
                if (c == '-') {
                    sb.append('-');
                    i++;
                }
                // Continue as long as the next characters are digits or a decimal point
                while (i < expression.length() && (Character.isDigit(expression.charAt(i)) || expression.charAt(i) == '.')) {
                    sb.append(expression.charAt(i++));
                }
                i--; // Decrement i as the for loop increments it
                values.push(Double.parseDouble(sb.toString()));
            } else if (c == '+' || c == '*' || c == '/' || c == '%' || (c == '-' && !values.isEmpty())) {
                // For '-' as subtraction, make sure it's not the unary operator by checking if values stack is not empty
                while (!ops.isEmpty() && hasPrecedence(c, ops.peek())) {
                    values.push(applyOperation(ops.pop(), values.pop(), values.pop()));
                }
                ops.push(c);
            }
        }
        // Apply the remaining operations in the ops stack
        while (!ops.isEmpty()) {
            values.push(applyOperation(ops.pop(), values.pop(), values.pop()));
        }
        // The final result should be the only value in the values stack
        return values.pop();
    }


    private boolean hasPrecedence(char op1, char op2) {
        if ((op1 == '*' || op1 == '/' || op1 == '%') && (op2 == '+' || op2 == '-')) return false;
        else return true;
    }

    private double applyOperation(char op, double b, double a) {
        switch (op) {
            case '+':
                return a + b;
            case '-':
                return a - b;
            case '*':
                return a * b;
            case '%':
                return a % b;
            case '/':
                if (b == 0) throw new UnsupportedOperationException("Cannot divide by zero.");
                return a / b;
        }
        return 0;
    }

    private void performOperation(String operation) {
        if (operation.equals("-") && (fullExpression.isEmpty() ||
                "+-*/%".contains("" + fullExpression.charAt(fullExpression.length() - 1)))) {
            // It's a negation if '-' is pressed at the beginning or after an operator.
            fullExpression += operation;
        } else if (!operation.equals("-") || !fullExpression.endsWith(operation)) {
            // Avoid consecutive subtraction operators (for simplicity) unless it's for negation.
            fullExpression += operation;
        }
        display.setText(fullExpression);
    }
}