package com.techelevator.tenmo.services;

import com.techelevator.tenmo.model.Balance;
import com.techelevator.tenmo.model.User;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.List;

public class TenmoService {

    private final String API_BASE_URL = "http://localhost:8080/";
    private RestTemplate restTemplate = new RestTemplate();
    private String authToken = null;

    public void setAuthToken(String authToken) {
        this.authToken = authToken;
    }

    public BigDecimal retrieveBalance() {
        return restTemplate.exchange(API_BASE_URL + "balance", HttpMethod.GET,
                makeAuthEntity(), Balance.class).getBody().getBalance();
    }

    public User[] retrieveAllUsers() {
        return restTemplate.exchange(API_BASE_URL + "users", HttpMethod.GET,
                makeAuthEntity(), User[].class).getBody();
    }

    public HttpEntity makeAuthEntity() {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(this.authToken);
        return new HttpEntity<>(headers);
    }
}
