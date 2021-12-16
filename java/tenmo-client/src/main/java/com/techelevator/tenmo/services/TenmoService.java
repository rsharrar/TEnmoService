package com.techelevator.tenmo.services;

import com.techelevator.tenmo.model.Balance;
import com.techelevator.tenmo.model.Transfer;
import com.techelevator.tenmo.model.User;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientResponseException;
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

    public User retrieveUserByAccountId(int accountId) {
        return restTemplate.exchange(API_BASE_URL + "accounts/" + accountId + "/user", HttpMethod.GET, makeAuthEntity(), User.class).getBody();
    }

    public Transfer createTransfer(Transfer transfer){
        Transfer t = null;
        try{
            t = restTemplate.exchange(API_BASE_URL + "transfer", HttpMethod.POST, makeRequestBody(transfer), Transfer.class).getBody();
        }catch(RestClientResponseException | ResourceAccessException e){
            System.out.println(e.getMessage());
        }
        return t;
    }

    public String update(Transfer transfer){
        String str = "";
        try{
            str = restTemplate.exchange(API_BASE_URL + "review-request", HttpMethod.PUT, makeRequestBody(transfer), String.class).getBody();
        }catch(RestClientResponseException | ResourceAccessException e){
            System.out.println(e.getMessage());
        }
        return str;
    }

    public Transfer getTransferById(int id) {
        Transfer t = null;
        try {
            t = restTemplate.exchange(API_BASE_URL + "transfer/" + id, HttpMethod.GET, makeAuthEntity(), Transfer.class).getBody();
        } catch (RestClientResponseException | ResourceAccessException e) {
            System.out.println(e.getMessage());
        }
        return t;
    }

    public int getAccountIdByUserId(int userId){
        int id = 0;
        try{
            id = restTemplate.exchange(API_BASE_URL + "user/" + userId + "/account", HttpMethod.GET, makeAuthEntity(), Integer.class).getBody();
        }catch(ResourceAccessException | RestClientResponseException e){
            System.out.println(e.getMessage());
        }
        return id;
    }


    public Transfer[] getTransfersByUserId(int statusId) {
        return restTemplate.exchange(API_BASE_URL + "transfers/" + statusId, HttpMethod.GET, makeAuthEntity(), Transfer[].class).getBody();
    }

    public HttpEntity makeRequestBody(Transfer transfer) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(this.authToken);
        headers.setContentType(MediaType.APPLICATION_JSON);
        return new HttpEntity<>(transfer, headers);
    }


    public HttpEntity makeAuthEntity() {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(this.authToken);
        return new HttpEntity<>(headers);
    }
}
