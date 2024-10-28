package Service;
import java.sql.SQLException;

import DAO.AccountDAO;
import Model.Account;
import java.util.*;


public class AccountService {

    private final AccountDAO accountDAO;

    public AccountService (AccountDAO accountDAO){
        this.accountDAO = accountDAO;
    }
/******************************Create a new account in the database using a post request*********************************/

    public Account createNewAccount (Account account) throws SQLException {
    /*  check if username is not blank
        the password should be at least 4 characters long
        an account with that username doesn't already exist.
            - step 1: get all accounts 
            - step 2: Iterate through all accounts and 
                - if username found in the list, throw an exception 
                - if it's not, return a new account.
    */
        String username = account.getUsername();
        String password = account.getPassword();


        // check if username already exists.
        List <Account> allAccounts = accountDAO.getAllAccounts(); 
        for (Account existingAccount : allAccounts){
            if (existingAccount.getUsername().equals(username)){
                // throw new IllegalArgumentException("Username already exists");
                return null;
            }
        }

        //Validate that the username is not valid.
        if (username == null || username.isBlank() ){
            // throw new IllegalArgumentException("Username can not be blank");
            return null;
        }

        //validate that the password is at least 4 character long.
        if (password == null || password.length() < 4){
            // throw new IllegalArgumentException ("Password must be at least 4 character long");
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

        //check if the user has already registered 
        List <Account> allUsers  = accountDAO.getAllAccounts();

        for (Account existingUser : allUsers){
            if (existingUser.getUsername().equals(account.getUsername()) && existingUser.getPassword().equals(account.getPassword())){
                return existingUser;
            }
        }

        return null;
    }

/********************************************retreive all Accounts from a database****************************************/
    public List <Account> getAllAccounts () throws SQLException{
        return accountDAO.getAllAccounts();
    }
}


