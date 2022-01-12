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
import java.util.Locale;

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

    private void viewTransferHistory() { displayTransfers(2, "Completed Transfers"); }

    private void viewPendingRequests() {
		displayTransfers(1, "Transfer Requests");
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
			System.out.println("\nInvalid currency input, please try again...");
			amountChoice = console.getUserInput("\nEnter amount");
		}
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
        System.out.println("-----");
        System.out.println(String.format("| %-8s | %-15s|","ID", "Name"));
        User[] users = tenmoService.retrieveAllUsers();

        for (User user : users) {
            System.out.println(String.format("| %-8s | %-15s|", user.getId(), user.getUsername()));
        }
        System.out.println("------------------------------------");
    }

    private void displayAllOtherUsers() {
        System.out.println("----------------------------");
        System.out.println(String.format("| %-25s|", "Users"));
        System.out.println("----------------------------");
        System.out.println(String.format("| %-8s| %-15s|","ID", "Name"));
        System.out.println("----------------------------");
        User[] users = tenmoService.retrieveAllUsersExceptSelf();

        for (User user : users) {
            System.out.println(String.format("| %-8s| %-15s|", user.getId(), user.getUsername()));
        }
        System.out.println("----------------------------");
    }


    private void displayTransfers(int statusId, String header) {
        Transfer[] transfers = tenmoService.getTransfersByUserId(statusId);

        if (transfers.length == 0) {
            System.out.println("There are no " + header.toLowerCase() + " to display.");
        } else {
            System.out.println("---------------------------------------------");
            System.out.println(String.format("| %-42s|", header));
            System.out.println("---------------------------------------------");
            System.out.println(String.format("| %-8s| %-20s| %-10s|", "ID", "To/From", "Amount"));
            System.out.println("---------------------------------------------");
            List<Integer> transferIds = new ArrayList<>();
            for (Transfer transfer : transfers) {
                String direction = "";
                int accountId = 0;
                if (transfer.getInitiatingAccount() == tenmoService.getAccountIdByUserId(currentUser.getUser().getId())) {
                    direction = "To:";
                    accountId = transfer.getRecipientAccount();
                } else {
                    direction = "From:";
                    accountId = transfer.getInitiatingAccount();
                }
                transferIds.add(transfer.getTransferId());
                System.out.println(String.format("| %-8s| %-6s%-14s| %-10s|", transfer.getTransferId(), direction, tenmoService.retrieveUserByAccountId(accountId).getUsername(), "$" + transfer.getTransferAmount()));
            }
            System.out.println("---------------------------------------------");
            selectTransfer(transferIds, statusId);
        }
    }

    private void selectTransfer(List<Integer> transferIds, int statusId){
        if(statusId == 2){
            Integer transferId = console.getUserInputInteger("\nPlease enter transfer ID to view details (0 to cancel)");
            while (!transferIds.contains(transferId) && transferId != 0) {
                transferId = console.getUserInputInteger("\nPlease enter transfer ID to view details (0 to cancel)");
            }
            displayTransferDetails(transferId);
        }else if(statusId == 1){
            Integer transferId = console.getUserInputInteger("\nPlease enter transfer ID to approve or reject (0 to cancel)");
            while (!transferIds.contains(transferId) && transferId != 0) {
                transferId = console.getUserInputInteger("\nPlease enter transfer ID to approve or reject (0 to cancel)");
            }
            if(transferId == 0) return;
            displayTransferDetails(transferId);
            Transfer transferToUpdate = tenmoService.getTransferById(transferId);
            Integer userApprovalCode = console.getUserInputInteger("\nPlease enter 1 to approve, or 2 to reject (0 to cancel)");
            while(userApprovalCode > 3 && userApprovalCode < 0){
                userApprovalCode = console.getUserInputInteger("\nPlease enter 1 to approve, or 2 to reject (0 to cancel)");
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
        System.out.println(System.lineSeparator() + "-------------------------------");
        System.out.println(String.format("| %-28s|", "Transfer Details"));
        System.out.println("-------------------------------");
        System.out.println(String.format("| %7s %-20s|", "Id:", transfer.getTransferId()));
        System.out.println(String.format("| %7s %-20s|", "From:", tenmoService.retrieveUserByAccountId(transfer.getRecipientAccount()).getUsername()));
        System.out.println(String.format("| %7s %-20s|", "To:", tenmoService.retrieveUserByAccountId(transfer.getInitiatingAccount()).getUsername()));
        System.out.println(String.format("| %7s %-20s|", "Type:", (transfer.getTransferType() == 1 ? "Request" : "Send")));
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
        System.out.println(String.format("| %7s %-20s|", "Status:", status));
        System.out.println(String.format("| %7s%-19s|", "Amount: $", transfer.getTransferAmount()));
        System.out.println("-------------------------------");

    }
}
