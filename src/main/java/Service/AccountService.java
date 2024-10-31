package Service;
import java.sql.SQLException;

import DAO.AccountDAO;
import Model.Account;
import java.util.*;


public class AccountService {

    private final AccountDAO accountDAO;

    // Constructor to initialize AccountDAO dependency
    public AccountService (AccountDAO accountDAO){
        this.accountDAO = accountDAO;
    }
/******************************Create a new account in the database using a post request*********************************/

    public Account createNewAccount (Account account) throws SQLException {

        // Get username and password from account
        String username = account.getUsername();
        String password = account.getPassword();

        // Retrieve all accounts from the database
        List <Account> allAccounts = accountDAO.getAllAccounts(); 

        // Check if the username already exists in the database
        for (Account existingAccount : allAccounts){
            if (existingAccount.getUsername().equals(username)){
                return null;    //indicates username already exists
            }
        }

        //Validate that the username is not blank
        if (username == null || username.isBlank() ){
            return null;
        }

        //validate that the password is at least 4 character long.
        if (password == null || password.length() < 4){
            return null;
        }

         // If all validations pass, create the new account
        return accountDAO.createNewAccount(account);
        
    }

/**************************Authenticate a user who want to login using their post request********************************/

/*In order to authenticat a user we should check their username and password exists in our all users accounts list,
 * if the username is available, then we will check the password is valid password 
 *      if password is the same as the password in the list the the user is authenticted to login and return an account
 *      else the user is unauthorized to login and thrown  401 unauthorized error.   
*/

    public Account authenticateUser(Account account) throws SQLException {

        // Retrieve all accounts from the database
        List <Account> allUsers  = accountDAO.getAllAccounts();

        // Check if username and password match any existing account
        for (Account existingUser : allUsers){
            if (existingUser.getUsername().equals(account.getUsername()) && existingUser.getPassword().equals(account.getPassword())){
                return existingUser;    // Return existing account if authentication is successful
            }
        }

        // Return null if authentication fails
        return null;
    }

/********************************************retreive all Accounts from a database****************************************/
    public List <Account> getAllAccounts () throws SQLException{
        // Call DAO method to get all accounts
        return accountDAO.getAllAccounts();
    }
}


