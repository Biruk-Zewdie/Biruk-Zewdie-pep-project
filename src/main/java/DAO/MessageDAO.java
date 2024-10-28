package DAO;

import java.sql.*;
import java.util.*;

import Model.Message;
import Util.ConnectionUtil;

/*In this message DAO class I will write six methods to manipulate and retrive data from
our database.
1. create a new massage that the user requested a post request after the user has successfully loged in.
the name of the method would be createNewMessage and it takes Message object from Message.java to insert 
message fileds into our database column. 
*
2. retrive all the messages from our database and put it in our list and 
return it in order to be accessible for Authorized users.
the name of this method is getAllmessages.
* 
3. retrive a single message using message ID from a database.
the name of the method is getMessageById which uses the message id as an input and
return that specific message if the request is successful and if not return null.
*
4. Remove a single message using message ID from the database if the message existed. 
so the first step is to retrieve a message by id then if the message existed delete it if not return null. 
the name of the message would be deleteMessageById which uses the message id as an input.
*
5. Update a message by using message ID in our database.
the name of the method would be updateMessageById which will take a message object 
from which we pick message ID and retrieve a message content.
If a message exists in our database, it will replace old message content with a new message.
The validation of a new text message content will be handled in MessageService class.

 
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
    /*******************************Retrieve a message by ID*********************************************/

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

    /**********************************Delete a message by ID**************************************************/
    
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

    /*****************************Update message by ID************************************/

    public boolean updateMessageById (int message_id, String newMessageText) throws SQLException{

        Connection connection = ConnectionUtil.getConnection();
            String updateSql = "Update message SET message_text = ? WHERE message_id = ?";
            PreparedStatement updateStatement = connection.prepareStatement(updateSql);
            updateStatement.setString(1, newMessageText);
            updateStatement.setInt (2, message_id);
            int rowsUpdated = updateStatement.executeUpdate();

            if (rowsUpdated > 0){
                return true;
            }else {
                return false;
            }
    }

    /*****************************retrieve all messages under a given account************************************/

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