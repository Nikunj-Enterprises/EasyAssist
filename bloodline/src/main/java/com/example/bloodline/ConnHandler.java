package com.example.bloodline;

public interface ConnHandler {
    public void OnSuccess(int status, String token);
    public void OnFailure(int status, String message);
}
