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

    public MessageService (MessageDAO messageDAO, AccountService accountService){
        this.messageDAO = messageDAO;
        this.accountService = accountService;
    }

/******************Create a new message by existing account using post request and save in the database *******************/

/*In order to a user create a message the user should be logged in with a valid credential (existing user).
    - once the a user is logged in the message to be posted shouldn't be blank.
    - the number of characters of the message should be less than 255 characters
if the above conditions are satisfied a new message will be created successfully and return a new message object.
if not, the it will throw a client side error and return null object.
*/

public Message createNewMessage(Message message, Account account) throws SQLException, AuthenticationException{

    List <Account> allAccounts = accountService.getAllAccounts();

    boolean userExists  = false;

    for (Account acc : allAccounts){
        if (acc.getAccount_id() == message.getPosted_by()){
            userExists = true;
            break;
        }
    }
    
    if (!userExists){
        throw new AuthenticationException("The user must be a real, existing user to post a new message");
    }

    String messageContent = message.getMessage_text(); 
    if (messageContent == null || messageContent.isBlank() || messageContent.length() > 255){
        return null;
    }

    return messageDAO.CreateNewMessage(message);
}



/************************************retreive all messages from a database service **************************************/
public List <Message> getAllMessages () throws SQLException{
    return messageDAO.getAllMessages();
}
/*******************************retreive a message by message id from the database service class************************/

public Message getMessageById (int message_id) throws SQLException{

    // Call the DAO to retrieve the message
    return messageDAO.getMessageById(message_id);

}
/**************************Logic to validate the new message before update existing message***********************/

    public Message updateMessageContent (int message_id, String newMessageText) throws SQLException {

        if (newMessageText == null || newMessageText.isBlank()|| newMessageText.length() > 255){
            throw new IllegalArgumentException ("Message content cannot be empty or over 255 characters");
        }
        
        //check if the message exists
        Message existingMessage = messageDAO.getMessageById(message_id);
        if (existingMessage == null){
            // throw new NoSuchElementException ("Message with ID " + message_id + " not found.");
            return null;
        }

        //update the message
        boolean isUpdated = messageDAO.updateMessageById(message_id, newMessageText);
        return isUpdated? getMessageById(message_id) : null;

    }


/************************************************************Delete a message by Id***************************************************/
    public Message deleteMessageById (int message_id) throws SQLException {
        return messageDAO.deleteMessageById(message_id);

    }



/************************************************************Retrieve all the messages under a given account  ***************************************************/
    public List <Message> getAllMessagesUnderGivenAccount (int account_id) throws SQLException {
        return messageDAO.getAllMessagesByAccount(account_id);

    }

}
