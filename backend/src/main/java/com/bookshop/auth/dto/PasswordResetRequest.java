package com.bookshop.auth.dto;

public record PasswordResetRequest(String token,String newPassword) {
}
