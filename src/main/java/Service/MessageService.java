package Service;

import java.sql.SQLException;

import DAO.MessageDAO;
import Model.Account;
import Model.Message;
import java.util.*;

import javax.security.sasl.AuthenticationException;

public class MessageService {

    private final MessageDAO messageDAO;
    private final AccountService accountService;

    // Constructor to initialize MessageDAO and AccountService dependencies
    public MessageService (MessageDAO messageDAO, AccountService accountService){
        this.messageDAO = messageDAO;
        this.accountService = accountService;
    }

/******************Create a new message by existing account using post request and save in the database *******************/

/*In order to a user post a blog the user should be an existing user/account.
    - once the a user is checked as an existing user, the next step is validate the message to be posted which shouldn't be blank and
      the number of characters of the message should be less than 255 characters
    - if the above conditions are satisfied a new message will be created successfully and return a new message object.
    - if not, the it will throw a client side error and return null object.
*/

public Message createNewMessage(Message message, Account account) throws SQLException, AuthenticationException{

    // Retrieve all accounts to verify the user exists
    List <Account> allAccounts = accountService.getAllAccounts();
    boolean userExists  = false;

    // Check if the account exists in the database
    for (Account acc : allAccounts){
        if (acc.getAccount_id() == message.getPosted_by()){
            userExists = true;
            break;
        }
    }
    // Throw an AuthenticationException if the user is not found
    if (!userExists){
        throw new AuthenticationException("");

    }
    // Validate message content (not blank and under 255 characters)
    String messageContent = message.getMessage_text(); 
    if (messageContent == null || messageContent.isBlank() || messageContent.length() > 255){
        return null;
    }
    // Create and save the message if all validations pass
    return messageDAO.CreateNewMessage(message);
}



/************************************Retrieve all messages from a database service **************************************/
public List <Message> getAllMessages () throws SQLException{

    // Call DAO method to retrieve all messages
    return messageDAO.getAllMessages();
}
/*******************************Retreive a message by message id from the database service class************************/

public Message getMessageById (int message_id) throws SQLException{

    // Call DAO to retrieve a specific message by ID
    return messageDAO.getMessageById(message_id);
}
/*********************************Method to validate the new message before update existing message***********************/

    public Message updateMessageContent (int message_id, Message message) throws SQLException {

        //check if the message exists by retrieving it from DAO.
        Message updatedMessage = messageDAO.getMessageById(message_id);

        //if the message don't exist return null else update the message text and return updated message if it exists.
        if (updatedMessage == null){
            return null;
        }else {
            updatedMessage.setMessage_text(message.getMessage_text());
            return updatedMessage;
        }
    }

/************************************************************Delete a message by Id***************************************************/

    public Message deleteMessageById (int message_id) throws SQLException {

        // Call DAO to delete the message by ID.
        return messageDAO.deleteMessageById(message_id);

    }

/**********************************Retrieve all the messages under a given account  ***************************************************/

    public List <Message> getAllMessagesUnderGivenAccount (int account_id) throws SQLException {

        // Call DAO to retrieve all messages associated with the given account ID.
        return messageDAO.getAllMessagesByAccount(account_id);
    }

}
