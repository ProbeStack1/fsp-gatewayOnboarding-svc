package com.probestack.forgesphere.onboarding.dto;

import java.time.Instant;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ApiResponse<T> {

    private String timestamp; // ISO-8601

    private String status; // SUCCESS / FAILURE

    private String message;

    private T data;
    

    public static <T> ApiResponse<T> success(String message, T data) {
        ApiResponse<T> resp = new ApiResponse<>();
        resp.setTimestamp(Instant.now().toString());
        resp.setStatus("SUCCESS");
        resp.setMessage(message);
        resp.setData(data);
        return resp;
    }



    public static <T> ApiResponse<T> failure(String message, T data) {
        ApiResponse<T> resp = new ApiResponse<>();
        resp.setTimestamp(Instant.now().toString());
        resp.setStatus("FAILURE");
        resp.setMessage(message);
        resp.setData(data);
        return resp;
    }
}


