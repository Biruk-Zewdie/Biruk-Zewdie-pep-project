package DAO;

import java.util.*;
import java.sql.*;

import Model.Account;

import Util.ConnectionUtil;

/*In this Account DAO class I will write two important method 
 * 1. retrive all the users accounts from the database and put on the list on the memory
 * in order to Authenticate a user credential by comparing the saved users with a login request from the user 
 *  before the user login into the our API.
 * 
 * The name of our method would be getAllAccounts.
 * 
 * 2. create a new account for a user who submit a new account credentials for signup/registration so that we will insert a new user data 
 * in to our database. 
 * 
 * The name of the second method would be CreateNewAccount.
 * This account takes a an Account object to get username and password from the Account class 
 * 
  */
public class AccountDAO {

// Please refrain from using a 'try-with-resources' block when connecting to your database. 
// The ConnectionUtil provided uses a singleton, and using a try-with-resources will cause issues in the tests.
// so i will handle that exception or declare it in the method signature using the throws keyword.

    public List<Account> getAllAccounts() throws SQLException{
        Connection connection = ConnectionUtil.getConnection();


        List <Account> accounts = new ArrayList<>();

        String sql = "SELECT * From account";

        PreparedStatement preparedStatement = connection.prepareStatement(sql);
        ResultSet rs = preparedStatement.executeQuery();

        while(rs.next()){
            Account account = new Account(rs.getInt("account_id"), rs.getString("username"), rs.getString ("password"));
            accounts.add(account);
            
        }

        return accounts;
    }

    public Account createNewAccount (Account account) throws SQLException {
        Connection connection = ConnectionUtil.getConnection();

        String sql = "INSERT INTO account (username, password) VALUES (?, ?)";

        PreparedStatement preparedStatement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
        preparedStatement.setString(1, account.getUsername());
        preparedStatement.setString(2,account.getPassword());
        preparedStatement.executeUpdate();

        ResultSet primaryKeyResultSet = preparedStatement.getGeneratedKeys();
        
        if (primaryKeyResultSet.next()){
            int generated_account_id = (int) primaryKeyResultSet.getLong(1);
            return new Account(generated_account_id, account.username, account.password);
        }

        return null;
    } 

}
