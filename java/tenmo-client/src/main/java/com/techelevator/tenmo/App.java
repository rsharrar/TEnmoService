package com.techelevator.tenmo;

import com.techelevator.tenmo.model.*;
import com.techelevator.tenmo.services.AuthenticationService;
import com.techelevator.tenmo.services.AuthenticationServiceException;
import com.techelevator.tenmo.services.TenmoService;
import com.techelevator.view.ConsoleService;
import io.cucumber.java.bs.A;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class App {

    private static final String API_BASE_URL = "http://localhost:8080/";

    private static final String MENU_OPTION_EXIT = "Exit";
    private static final String LOGIN_MENU_OPTION_REGISTER = "Register";
    private static final String LOGIN_MENU_OPTION_LOGIN = "Login";
    private static final String[] LOGIN_MENU_OPTIONS = {LOGIN_MENU_OPTION_REGISTER, LOGIN_MENU_OPTION_LOGIN, MENU_OPTION_EXIT};
    private static final String MAIN_MENU_OPTION_VIEW_BALANCE = "View your current balance";
    private static final String MAIN_MENU_OPTION_SEND_BUCKS = "Send TE bucks";
    private static final String MAIN_MENU_OPTION_VIEW_PAST_TRANSFERS = "View your past transfers";
    private static final String MAIN_MENU_OPTION_REQUEST_BUCKS = "Request TE bucks";
    private static final String MAIN_MENU_OPTION_VIEW_PENDING_REQUESTS = "View your pending requests";
    private static final String MAIN_MENU_OPTION_LOGIN = "Login as different user";
    private static final String[] MAIN_MENU_OPTIONS = {MAIN_MENU_OPTION_VIEW_BALANCE, MAIN_MENU_OPTION_SEND_BUCKS, MAIN_MENU_OPTION_VIEW_PAST_TRANSFERS, MAIN_MENU_OPTION_REQUEST_BUCKS, MAIN_MENU_OPTION_VIEW_PENDING_REQUESTS, MAIN_MENU_OPTION_LOGIN, MENU_OPTION_EXIT};

    private AuthenticatedUser currentUser;
    private ConsoleService console;
    private AuthenticationService authenticationService;
    private TenmoService tenmoService;

    public static void main(String[] args) {
        App app = new App(new ConsoleService(System.in, System.out), new AuthenticationService(API_BASE_URL));
        app.run();
    }

    public App(ConsoleService console, AuthenticationService authenticationService) {
        this.console = console;
        this.authenticationService = authenticationService;
        this.tenmoService = new TenmoService();
    }

    public void run() {
        System.out.println("*********************");
        System.out.println("* Welcome to TEnmo! *");
        System.out.println("*********************");

        registerAndLogin();
        mainMenu();
    }

    private void mainMenu() {
        while (true) {
            String choice = (String) console.getChoiceFromOptions(MAIN_MENU_OPTIONS);
            if (MAIN_MENU_OPTION_VIEW_BALANCE.equals(choice)) {
                viewCurrentBalance();
            } else if (MAIN_MENU_OPTION_VIEW_PAST_TRANSFERS.equals(choice)) {
                viewTransferHistory();
            } else if (MAIN_MENU_OPTION_VIEW_PENDING_REQUESTS.equals(choice)) {
                viewPendingRequests();
            } else if (MAIN_MENU_OPTION_SEND_BUCKS.equals(choice)) {
                sendBucks();
            } else if (MAIN_MENU_OPTION_REQUEST_BUCKS.equals(choice)) {
                requestBucks();
            } else if (MAIN_MENU_OPTION_LOGIN.equals(choice)) {
                login();
            } else {
                // the only other option on the main menu is to exit
                exitProgram();
            }
        }
    }

    private void viewCurrentBalance() {
        System.out.println("Your current account balance is: $" + tenmoService.retrieveBalance());
    }

    private void viewTransferHistory() { displayTransfers(2); }

    private void viewPendingRequests() {
		displayTransfers(1);
    }

    private void sendBucks() {
        displayAllOtherUsers();
        Integer idChoice = console.getUserInputInteger("Enter ID of user you are sending to (0 to cancel)");
        if (idChoice == 0) {
            return;
        }
        String amountChoice = console.getUserInput("Enter amount");
        while (!validateCurrencyInput(amountChoice)) {
            System.out.println("Invalid currency input, please try again...");
            amountChoice = console.getUserInput("Enter amount");
        }
        Balance transferBalance = new Balance(new BigDecimal(amountChoice));
        Transfer transfer = new Transfer(new BigDecimal(amountChoice), 2, 2,
                tenmoService.getAccountIdByUserId(idChoice), tenmoService.getAccountIdByUserId(currentUser.getUser().getId()));
        tenmoService.createTransfer(transfer);
    }

    private void requestBucks() {
        displayAllOtherUsers();
		Integer idChoice = console.getUserInputInteger("Enter ID of user you are sending to (0 to cancel)");
		if (idChoice == 0) {
			return;
		}
		String amountChoice = console.getUserInput("Enter amount");
		while (!validateCurrencyInput(amountChoice)) {
			System.out.println("Invalid currency input, please try again...");
			amountChoice = console.getUserInput("Enter amount");
		}
		Balance transferBalance = new Balance(new BigDecimal(amountChoice));
		Transfer transfer = new Transfer(new BigDecimal(amountChoice), 1, 1,
				tenmoService.getAccountIdByUserId(currentUser.getUser().getId()), tenmoService.getAccountIdByUserId(idChoice));
		tenmoService.createTransfer(transfer);
    }

    private boolean validateCurrencyInput(String input) {
        String amountChoiceCopy = input.replaceAll("[^0-9.]", "");
        return input.equals(amountChoiceCopy);
    }

    private void exitProgram() {
        System.exit(0);
    }

    private void registerAndLogin() {
        while (!isAuthenticated()) {
            String choice = (String) console.getChoiceFromOptions(LOGIN_MENU_OPTIONS);
            if (LOGIN_MENU_OPTION_LOGIN.equals(choice)) {
                login();
            } else if (LOGIN_MENU_OPTION_REGISTER.equals(choice)) {
                register();
            } else {
                // the only other option on the login menu is to exit
                exitProgram();
            }
        }
    }

    private boolean isAuthenticated() {
        return currentUser != null;
    }

    private void register() {
        System.out.println("Please register a new user account");
        boolean isRegistered = false;
        while (!isRegistered) //will keep looping until user is registered
        {
            UserCredentials credentials = collectUserCredentials();
            try {
                authenticationService.register(credentials);
                isRegistered = true;
                System.out.println("Registration successful. You can now login.");
            } catch (AuthenticationServiceException e) {
                System.out.println("REGISTRATION ERROR: " + e.getMessage());
                System.out.println("Please attempt to register again.");
            }
        }
    }

    private void login() {
        System.out.println("Please log in");
        currentUser = null;
        while (currentUser == null) //will keep looping until user is logged in
        {
            UserCredentials credentials = collectUserCredentials();
            try {
                currentUser = authenticationService.login(credentials);
                tenmoService.setAuthToken(currentUser.getToken());
            } catch (AuthenticationServiceException e) {
                System.out.println("LOGIN ERROR: " + e.getMessage());
                System.out.println("Please attempt to login again.");
            }
        }
    }

    private UserCredentials collectUserCredentials() {
        String username = console.getUserInput("Username");
        String password = console.getUserInput("Password");
        return new UserCredentials(username, password);
    }

    private void displayAllUsers() {
        System.out.println("------------------------------------");
        System.out.println("Users");
        System.out.println("ID\t\t\tName");
        User[] users = tenmoService.retrieveAllUsers();

        for (User user : users) {
            System.out.println(user.getId() + "\t\t" + user.getUsername());
        }
        System.out.println("------------------------------------");
    }

    private void displayAllOtherUsers() {
        System.out.println("------------------------------------");
        System.out.println("Users");
        System.out.println("ID\t\t\tName");
        User[] users = tenmoService.retrieveAllUsersExceptSelf();

        for (User user : users) {
            System.out.println(user.getId() + "\t\t" + user.getUsername());
        }
        System.out.println("------------------------------------");
    }


    private void displayTransfers(int statusId) {
        System.out.println("------------------------------------");
        System.out.println("Transfers");
        System.out.println("ID\t\tFrom/To\t\t\tAmount");
        System.out.println("------------------------------------");
        Transfer[] transfers = tenmoService.getTransfersByUserId(statusId);
        List<Integer> transferIds = new ArrayList<>();
        for (Transfer transfer : transfers) {
            String direction = "";
            int accountId = 0;
            if (transfer.getInitiatingAccount() == tenmoService.getAccountIdByUserId(currentUser.getUser().getId())) {
                direction = "To:   ";
                accountId = transfer.getRecipientAccount();
            } else {
                direction = "From: ";
                accountId = transfer.getInitiatingAccount();
            }
            transferIds.add(transfer.getTransferId());
            System.out.println(transfer.getTransferId() + "\t" + direction + tenmoService.retrieveUserByAccountId(accountId).getUsername() + "\t\t$" + transfer.getTransferAmount());
        }
        System.out.println("------------------------------------");
        selectTransfer(transferIds, statusId);
    }

    private void selectTransfer(List<Integer> transferIds, int statusId){
        if(statusId == 2){
            Integer transferId = console.getUserInputInteger("Please enter transfer ID to view details (0 to cancel)");
            while (!transferIds.contains(transferId) && transferId != 0) {
                transferId = console.getUserInputInteger("Please enter transfer ID to view details (0 to cancel)");
            }
            displayTransferDetails(transferId);
        }else if(statusId == 1){
            Integer transferId = console.getUserInputInteger("Please enter transfer ID to approve or reject(0 to cancel)");
            while (!transferIds.contains(transferId) && transferId != 0) {
                transferId = console.getUserInputInteger("Please enter transfer ID to approve or reject (0 to cancel)");
            }
            if(transferId == 0) return;
            displayTransferDetails(transferId);
            Transfer transferToUpdate = tenmoService.getTransferById(transferId);
            Integer userApprovalCode = console.getUserInputInteger("Please enter 1 to approve, or 2 to reject (0 to cancel)");
            while(userApprovalCode > 3 && userApprovalCode < 0){
                userApprovalCode = console.getUserInputInteger("Please enter 1 to approve, or 2 to reject (0 to cancel)");
            }
            if(userApprovalCode == 1){
                transferToUpdate.setTransferStatus(2);
                System.out.println(tenmoService.update(transferToUpdate));
                displayTransferDetails(transferId);
            }else if(userApprovalCode == 2){
                transferToUpdate.setTransferStatus(3);
                System.out.println(tenmoService.update(transferToUpdate));
                displayTransferDetails(transferId);
            }
        }
    }

    private void displayTransferDetails(int id) {
        if (id == 0) return;
        Transfer transfer = tenmoService.getTransferById(id);
        System.out.println(System.lineSeparator() + "------------------------------------");
        System.out.println("Transfer Details");
        System.out.println("------------------------------------");
        System.out.println("Id: " + transfer.getTransferId());
        System.out.println("From: " + tenmoService.retrieveUserByAccountId(transfer.getRecipientAccount()).getUsername());
        System.out.println("To: " + tenmoService.retrieveUserByAccountId(transfer.getInitiatingAccount()).getUsername());
        System.out.println("Type: " + (transfer.getTransferType() == 1 ? "Request" : "Send"));
        String status = "";
        switch (transfer.getTransferStatus()) {
            case 1:
                status = "Pending";
                break;
            case 2:
                status = "Approved";
                break;
            case 3:
                status = "Rejected";
                break;
        }
        System.out.println("Status: " + status);
        System.out.println("Amount: $" + transfer.getTransferAmount());
    }
}
