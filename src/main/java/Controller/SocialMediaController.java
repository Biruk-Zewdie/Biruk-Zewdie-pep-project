package Controller;

import io.javalin.Javalin;
import io.javalin.http.Context;
import Model.Account;
import Model.Message;
import Service.MessageService;
import Service.AccountService;

import java.sql.SQLException;
import java.util.*;

import javax.security.sasl.AuthenticationException;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import DAO.AccountDAO;
import DAO.MessageDAO;

/*
 * Controller class that defines the API endpoints for the social media application.
 * Contains handlers for account registration, login, message creation, retrieval, update, and deletion.
 */

public class SocialMediaController {
    private final MessageService messageService;
    private AccountService accountService = null;
    private Account authenticatedAccount  = null;      // Used to track login status

    public SocialMediaController () {

        // Initializes MessageService with a new DAO instance
        // accountService must be initialized before messageService 
        this.accountService = new AccountService(new AccountDAO());
        this.messageService = new MessageService (new MessageDAO(), this.accountService); 
       
    }
    
    /**
     * In order for the test cases to work, you will need to write the endpoints in the startAPI() method, as the test
     * suite must receive a Javalin object from this method.
     * @return a Javalin app object which defines the behavior of the Javalin controller.
     */
    public Javalin startAPI() {
        Javalin app = Javalin.create();
        
        app.post("register", this::userRegistrationHandler);                              //user registration endpoint 
        app.post("login", this::userLoginHandler);                                        //user Login endpoint
        app.post("messages", this::createMessageHandler);                                 //create new message endpoint 
        app.get("messages", this::getAllMessagesHandler);                                 //get all messages endpoint 
        app.get("messages/{message_id}", this::getSingleMessageHandler);                  //retrieve single message endpoint
        app.delete("messages/{message_id}", this::deleteSingleMessageHandler);            //Delete message by id endpoint 
        app.patch("messages/{message_id}", this::updateMessageHandler);                   //update message content by id endpoint 
        app.get("accounts/{account_id}/messages", this::getAllMessagesByAccountHandler);  //retrive all messages by user account id endpoint 

        return app;
    }

    /**
     * This is an example handler for an example endpoint.
     * @param context The Javalin Context object manages information about both the HTTP request and response.
     */

/*******************************************User registration handler***************************************/
     /*
      * handles the post request to /register 
        1.Parse the json request body to create an Account object
        2.Call AccountService to validate and create the account.
        3.Return the newly created account as json if successful, or a 400 status if validation fails. 
      */
    
     private void userRegistrationHandler(Context context) {

        try {
            // Parse the JSON body to an Account object
            Account account = context.bodyAsClass(Account.class);

            Account newAccount = accountService.createNewAccount(account);

            if (newAccount != null){
                context.status(200).json(newAccount);
            }else {
                context.status(400).result("");
            }

            
        } catch (IllegalArgumentException e) {
            // Handle validation errors and respond with a 400 status
            context.status(400).result(e.getMessage());
        }catch (SQLException e){
            //Handles database error and responds with a 500 status 
            context.status(500).result(e.getMessage());
        }
     
    }

/**************************************************User login handler*******************************************/

/* handles the post request to /login
    1.Parse the json request body to create an Account object
    2.Authenticates the user's credentials through AccountService.
    3.Returns account data in JSON format on successful login or a 401 status if unsuccessful.
 */

    private void userLoginHandler (Context context){

        try {
            Account account = context.bodyAsClass(Account.class);

            //Check if username and password are provided
            if (account.getUsername() == null || account.getUsername().isBlank() || 
                account.getPassword() == null || account.getPassword().isBlank()){
                    context.status(400).result("username and password should be provider");
                    return;
            }

            Account authenticatedAccount  = accountService.authenticateUser(account);

            if (authenticatedAccount  != null){
                this.authenticatedAccount  = authenticatedAccount ;       //Set the logged-in account as authenticated account.
                context.status(200).json(authenticatedAccount );
            }else{
                context.status(401).result("");
            }
            
        } catch (IllegalArgumentException e) {
            context.status(400).result(e.getMessage());
        }catch (SQLException e){
            context.status(500).result(e.getMessage());
        }  
    }

/********************************************Create a new messages handler*****************************************/
/*
     * Handles POST request to /messages endpoint.
     * 1. Parses the JSON request body to create a Message object.
     * 2. Validates the message content length and checks for blank messages.
     * 3. Calls MessageService to save the new message if valid, else returns a 400 status.
     */

private void createMessageHandler (Context context) throws SQLException{
    try {

          // Parse the message and account from the request body
          Message message  = context.bodyAsClass(Message.class);

        // Validate message content
        if (message.getMessage_text() == null  || message.getMessage_text().isBlank() || message.getMessage_text().length() > 255){
            context.status(400).result("");
            return;
        }

        Message createdMessage = messageService.createNewMessage(message, authenticatedAccount);

        if (createdMessage != null){
            context.status(200).json(createdMessage);
        }else{
            context.status(400).result("Client error");
        }
        
    } catch (IllegalArgumentException e) {
        context.status(400).result(e.getMessage());
    }catch (SQLException e){
        context.status(500).result(e.getMessage());
    }catch(AuthenticationException e){
        context.status(400).result(e.getMessage());
    }
}

/********************************************Get all messages handler*********************************************/

    private void getAllMessagesHandler (Context context) throws SQLException{

        /*
        * Handles GET request to /messages endpoint.
        * Retrieves all messages from the MessageService and returns them in JSON format.
        */

        try {
            List <Message> messages  = messageService.getAllMessages();
            context.status(200).json(messages);
        } catch (Exception e) {
            context.status(500).result("failed to retrieve messages due to a server error.");
        }
    }

//********************************************Get a singler message handler*******************************************/

    private void getSingleMessageHandler (Context context){

    /*
     * Handles GET request to /messages/{message_id} endpoint.
     * Retrieves a message by its ID and returns it in JSON format.
     * Returns a 400 status if the ID is invalid and a 500 status for server errors.
     */

        try {
            // Retrieve message ID from the path parameter
            int messageId =  Integer.parseInt(context.pathParam("message_id"));
            Message message = messageService.getMessageById(messageId);

            if (message != null){
                context.status(200).json(message);
            }else{
                context.status(200).result("");
            }
        } catch (NumberFormatException e) {
            context.status(400).result("Invalid message ID format"); 
        } catch (SQLException e){
            context.status(500).result("Internal server error");
        }
    }

    /********************************************Delete single message using message id handler*****************************************/

    private void deleteSingleMessageHandler (Context context) {

    /* Handles DELETE request to /messages/{message_id} endpoint.
     * Deletes a message by its ID. Returns 200 status on success, regardless of whether the message existed.
     */

        try {
            int messageId = Integer.parseInt(context.pathParam("message_id"));

            Message deletedMessage = messageService.deleteMessageById(messageId); 

            context.status(200);  //status code is always set to 200 because delete is idempotent.

            if (deletedMessage != null){
                context.json(deletedMessage);
            }else{
                context.result ("");
            }

        } catch (NumberFormatException e) {
            // handle Invalid message Id exception 
            context.status(400).result("Invalid message Id format.");
        }catch (SQLException e){
            context.status(500).result("Internal server error.");
        }
     

      
    }
/***********************************Update a message by ID handler************************************************/

    private void updateMessageHandler (Context context){

    /*
     * Handles PATCH request to /messages/{message_id} endpoint.
     * Updates a message's content by its ID and returns the updated message in JSON format.
     * Validates message content length and checks for empty content.
     */
        try {
            ObjectMapper mapper = new ObjectMapper();
            Message message = mapper.readValue(context.body(), Message.class);
            int messageId = Integer.parseInt(context.pathParam("message_id"));

            if (message.getMessage_text() == null || message.getMessage_text().isEmpty()) {
                context.status(400).json("");
                return;
            }

            Message updatedMessage = messageService.updateMessageContent(messageId, message);

            if (updatedMessage == null || updatedMessage.getMessage_text().length() >= 255) {
                context.status(400);
            } else {
                context.json(mapper.writeValueAsString(updatedMessage));
            }
            
    } catch (JsonMappingException e) {
        // JSON structure was invalid or did not map to Message class
        context.status(400).result("Invalid JSON structure for message content.");
    } catch (NumberFormatException e) {
        // Provided message ID is not a valid integer
        context.status(400).result("Invalid message ID format.");
    }  catch (JsonProcessingException e) {
        context.status(400).result("Invalid JSON format. Please check the request body.");
    }catch (SQLException e) {
        // Database-related error
        context.status(500).result("Database error occurred while updating message.");
    }
}
/***********************************Update a message by ID handler ************************************************/
    private void getAllMessagesByAccountHandler (Context context) throws SQLException{

    /*
     * Handles GET request to /accounts/{account_id}/messages endpoint.
     * Retrieves all messages associated with a given account ID and returns them in JSON format.
     */

        try {
            int accountId = Integer.parseInt(context.pathParam("account_id"));

            List <Message> messages  =  messageService.getAllMessagesUnderGivenAccount(accountId);

                context.status(200).json(messages);
            
        } catch (SQLException e) {
            e.printStackTrace();
            context.status(500).result ("Failed to retrieve messages due to a database error.");
                
        }catch (Exception e) {
            e.printStackTrace();
            context.status(500).result ("Failed to retrieve messages due to a server error.");
        }
    }
}