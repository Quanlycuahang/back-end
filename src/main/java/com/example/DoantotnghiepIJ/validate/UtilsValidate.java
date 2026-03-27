package com.example.DoantotnghiepIJ.validate;

public class UtilsValidate {
    public static void validateEmail(String email) {
        if (email == null || !email.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            throw new RuntimeException("Invalid email format");
        }
    }

    public static void validatePhone(String phone) {
        if (phone == null || !phone.matches("^(0|\\+84)[0-9]{9}$")) {
            throw new RuntimeException("Invalid phone number");
        }
    }
}
