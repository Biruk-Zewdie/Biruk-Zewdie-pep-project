package DAO;

import java.sql.*;
import java.util.*;

import Model.Message;
import Util.ConnectionUtil;

/*This class is DAO for message and I have six methods to manipulate and retrive message in our database.

1. Create a new message if the user is registered user and post a valid message text.
        - The name of the method would be createNewMessage and it takes Message object from Message class to insert 
          message details into our database column. 
        - The validation of the message text will be handled in our service class.
*
2. Retrieve all the messages from our database and put it in our list  and return it in order to be accessible for Authorized users.
        - The name of the method is getAllmessages.
* 
3. Retrieve a single message using message ID from a database.
        - The name of the method would be getMessageById which uses the message id as an input and
          return that specific message if the request is successful and if not return null.
*
4. Remove a message using message ID from the database if the message existed. 
        - The name of the message is deleteMessageById which uses message id as an input and retrieve a message by its id
          then if the message existed in our database, it will delete it and return the deleted message if not it will return null.
*
5. Update a message by using message ID in our database.
        - The name of the method would be updateMessageById which will take 2 parameters message id and message object as an input and 
          if message is available in our database it wll update a the message text and return null, if not it will throw an error.
        - If a message exists in our database, it will replace old message text with a new text.
        - The new message text should be validated. The validation of the message text will be handled by our MessageService class.
*
6. The last method will be retrive all messages under an account. 
        - the name of the method the method would be getAllMessagesByAccount which will take account ID as an input and retrieve all 
            messages by whom the message is posted by.
        - The retrieved messages will be stored in list returned. 
 */

 public class MessageDAO {

/**************************************Create a new message **********************************************/   

    public Message CreateNewMessage (Message message) throws SQLException{

        Connection connection = ConnectionUtil.getConnection ();

        String sql = "INSERT INTO message (posted_by, message_text, time_posted_epoch) VALUES (?,?,?)";

        PreparedStatement preparedStatement = connection.prepareStatement (sql, Statement.RETURN_GENERATED_KEYS);
        preparedStatement.setInt(1,message.getPosted_by());
        preparedStatement.setString (2, message.getMessage_text());
        preparedStatement.setLong(3, message.getTime_posted_epoch());

        int rowsAffected = preparedStatement.executeUpdate();

        if (rowsAffected > 0){
            ResultSet primaryKeyResultSet = preparedStatement.getGeneratedKeys();
            if (primaryKeyResultSet.next()){
                int generatedMessageId = primaryKeyResultSet.getInt(1);
                return (new Message(
                    generatedMessageId, 
                    message.getPosted_by(), 
                    message.getMessage_text(), 
                    message.getTime_posted_epoch()
                ));
            }
        }
        return null; //if the message creation fail.
    }

/***************************************Retrieve all the messages***********************************************/

    public List<Message> getAllMessages() throws SQLException{

        Connection connection = ConnectionUtil.getConnection();

        List <Message> allMessages = new ArrayList<>();

        String sql = "SELECT * FROM Message";
        PreparedStatement preparedStatement = connection.prepareStatement(sql);
        ResultSet rs = preparedStatement.executeQuery();

        while (rs.next()){
            Message message = new Message (
                rs.getInt ("message_id"),
                rs.getInt("posted_by"),
                rs.getString("message_text"),
                rs.getLong("time_posted_epoch"));

            //adding all the messages into our list
                allMessages.add(message);
        }
        return allMessages;
    }
/************************************Retrieve a message by ID*********************************************/

    public Message getMessageById (int message_id) throws SQLException {
        
        Connection connection = ConnectionUtil.getConnection();
        
        String sql = "SELECT * FROM message WHERE message_id = ?";
        
        PreparedStatement preparedStatement = connection.prepareStatement(sql);
        preparedStatement.setInt(1, message_id);
        
        ResultSet rs = preparedStatement.executeQuery();

        if (rs.next()){
            return new Message(
                rs.getInt("message_id"),
                rs.getInt ("posted_by"),
                rs.getString("message_text"),
                rs.getLong("time_posted_epoch"));
        }
        return null; 
    } 

/*************************************Delete a message by ID**************************************************/
    
    public Message deleteMessageById (int message_id) throws SQLException {

        Message existingMessage = getMessageById(message_id);
        if (existingMessage == null){
            return null;
        }

        Connection connection = ConnectionUtil.getConnection();
        String deleteSql = "DELETE FROM message WHERE message_id = ?";
        PreparedStatement deletedStatement = connection.prepareStatement(deleteSql);
        deletedStatement.setInt(1, message_id);

        int rowsDeleted = deletedStatement.executeUpdate();
            
    
        return rowsDeleted > 0? existingMessage : null;
    }

/*******************************************Update message by ID***************************************************/

    public Message updateMessageById (int message_id, Message message) throws SQLException{
        
        Connection connection = ConnectionUtil.getConnection();

        String updateSql = "UPDATE message SET message_text = ? WHERE message_id = ?";
        
        PreparedStatement updateStatement = connection.prepareStatement(updateSql);

        updateStatement.setString (1, message.getMessage_text());
        updateStatement.setInt (2, message_id);
        updateStatement.executeUpdate();

       return null;
    }

/**************************************Retrieve all messages under a given account*******************************************/

    public List <Message> getAllMessagesByAccount (int account_id) throws SQLException {

        //since posted by refers to account_id, account_id is equal to posted_by 
        List <Message> allMessagesInThisAccount = new ArrayList<>(); 
        Connection connection = ConnectionUtil.getConnection();

        String sql = "SELECT * FROM message WHERE posted_by = ?";

        PreparedStatement preparedStatement = connection.prepareStatement(sql);
        preparedStatement.setInt(1, account_id);

        ResultSet rs = preparedStatement.executeQuery();

        while (rs.next()){
            Message message = new Message(
                rs.getInt("message_id"),
                rs.getInt("posted_by"),
                rs.getString("message_text"),
                rs.getLong("time_posted_epoch")
            );
            allMessagesInThisAccount.add(message);
        }
        return allMessagesInThisAccount;

    }

}