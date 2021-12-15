package com.techelevator.tenmo.services;

import com.techelevator.tenmo.model.Balance;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;

public class TenmoService {

    private RestTemplate restTemplate = new RestTemplate();
    private String authToken = null;

    public void setAuthToken(String authToken) {
        this.authToken = authToken;
    }

    public BigDecimal retrieveBalance() {

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(this.authToken);
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        Balance balance = restTemplate.exchange("http://localhost:8080/balance", HttpMethod.GET,
                entity, Balance.class).getBody();

        return balance.getBalance();
    }
}
