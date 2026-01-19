package com.agriconnect.agri_connect.utils;

import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.regex.Pattern;

/**
 * Utility class for form validation with real-time error display
 */
public class FormValidator {

    // Regex patterns
    private static final Pattern PHONE_PATTERN = Pattern.compile("^(0|\\+84)[0-9]{9,10}$");
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@(.+)$");
    private static final Pattern PASSWORD_PATTERN = Pattern.compile("^.{6,}$"); // At least 6 characters

    /**
     * Add real-time validation to a TextInputEditText
     */
    public static void addPhoneValidation(TextInputEditText editText, TextInputLayout layout) {
        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                String text = s.toString().trim();
                if (text.isEmpty()) {
                    layout.setError(null);
                } else if (!PHONE_PATTERN.matcher(text).matches()) {
                    layout.setError("Số điện thoại không hợp lệ");
                } else {
                    layout.setError(null);
                }
            }
        });
    }

    public static void addEmailValidation(TextInputEditText editText, TextInputLayout layout) {
        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                String text = s.toString().trim();
                if (text.isEmpty()) {
                    layout.setError(null);
                } else if (!EMAIL_PATTERN.matcher(text).matches()) {
                    layout.setError("Email không hợp lệ");
                } else {
                    layout.setError(null);
                }
            }
        });
    }

    public static void addPasswordValidation(TextInputEditText editText, TextInputLayout layout) {
        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                String text = s.toString();
                if (text.isEmpty()) {
                    layout.setError(null);
                } else if (text.length() < 6) {
                    layout.setError("Mật khẩu phải có ít nhất 6 ký tự");
                } else {
                    layout.setError(null);
                }
            }
        });
    }

    public static void addRequiredValidation(TextInputEditText editText, TextInputLayout layout, String fieldName) {
        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                String text = s.toString().trim();
                if (text.isEmpty()) {
                    layout.setError(fieldName + " không được để trống");
                } else {
                    layout.setError(null);
                }
            }
        });
    }

    public static void addConfirmPasswordValidation(TextInputEditText confirmEditText,
            TextInputLayout confirmLayout, TextInputEditText passwordEditText) {
        confirmEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                String confirmText = s.toString();
                String passwordText = passwordEditText.getText() != null ? passwordEditText.getText().toString() : "";

                if (confirmText.isEmpty()) {
                    confirmLayout.setError(null);
                } else if (!confirmText.equals(passwordText)) {
                    confirmLayout.setError("Mật khẩu xác nhận không khớp");
                } else {
                    confirmLayout.setError(null);
                }
            }
        });
    }

    public static void addNumberValidation(TextInputEditText editText, TextInputLayout layout,
            double minValue, double maxValue, String fieldName) {
        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                String text = s.toString().trim();
                if (text.isEmpty()) {
                    layout.setError(null);
                    return;
                }

                try {
                    double value = Double.parseDouble(text.replace(",", ""));
                    if (value < minValue) {
                        layout.setError(fieldName + " phải lớn hơn " + (int) minValue);
                    } else if (value > maxValue) {
                        layout.setError(fieldName + " phải nhỏ hơn " + (int) maxValue);
                    } else {
                        layout.setError(null);
                    }
                } catch (NumberFormatException e) {
                    layout.setError(fieldName + " phải là số");
                }
            }
        });
    }

    /**
     * Check if all required fields are valid before submit
     */
    public static boolean isFormValid(TextInputLayout... layouts) {
        for (TextInputLayout layout : layouts) {
            if (layout.getError() != null) {
                return false;
            }
            EditText editText = layout.getEditText();
            if (editText != null && editText.getText().toString().trim().isEmpty()) {
                return false;
            }
        }
        return true;
    }
}
