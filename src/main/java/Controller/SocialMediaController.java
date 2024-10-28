package Controller;

import io.javalin.Javalin;
import io.javalin.http.Context;
import Model.Account;
import Model.Message;
import Service.MessageService;
import Service.AccountService;

import java.sql.SQLException;
// import Service.AccountService;
import java.util.*;

import javax.security.sasl.AuthenticationException;

import DAO.AccountDAO;
import DAO.MessageDAO;

/**
 * TODO: You will need to write your own endpoints and handlers for your controller. The endpoints you will need can be
 * found in readme.md as well as the test cases. You should
 * refer to prior mini-project labs and lecture materials for guidance on how a controller may be built.
 */
public class SocialMediaController {
    private final MessageService messageService;
    private AccountService accountService = null;

    private Account authenticatedAccount  = null; // Used to track login status

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

        //user registration endpoint 
        app.post("register", this::userRegistrationHandler);

        //user Login endpoint
        app.post("login", this::userLoginHandler);

        //create new message endpoint 

        app.post("messages", this::createMessageHandler);

        //get all messages endpoint 

        app.get("messages", this::getAllMessagesHandler);

        //retrieve single message endpoint
        app.get("messages/{message_id}", this::getSingleMessageHandler);

        //Delete message by id endpoint 
        app.delete("messages/{message_id}", this::deleteSingleMessageHandler);

        //update message content by id endpoint 
        app.patch("message/{message_id}", this::updateMessageHandler);

        //retrive all messages by user account id endpoint 
        
        app.get("accounts/{account_id}/messages", this::getAllMessagesByAccountHandler);



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

/* handles the post request to /register 
1.Parse the json request body to create an Account object
 2.Call AccountService to authenticate the user credential before login.
 3.Return a response body that contain a JSON of the account in the response body, 
    including its account_id if it is successful and 401 (unautorized user) if it's not succesful.
 * 
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
private void createMessageHandler (Context context) throws SQLException{
    try {

          // Parse the message and account from the request body
          Message message  = context.bodyAsClass(Message.class);

        // Validate message content
        if (message.getMessage_text() == null  || message.getMessage_text().isBlank() || message.getMessage_text().length() > 255){
            context.status(400).result("Invalid message input! Please try again");
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
        context.status(401).result(e.getMessage());
    }
}

/********************************************get all messages handler*********************************************/

    private void getAllMessagesHandler (Context context) throws SQLException{
        try {
            List <Message> messages  = messageService.getAllMessages();
            context.status(200).json(messages);
            
        } catch (Exception e) {
            context.status(500).result("failed to retrieve messages due to a server error.");
        }
        
    }

//********************************************get a singler message handler*******************************************/

    private void getSingleMessageHandler (Context context){
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
/***********************************Update a message by ID handler ************************************************/

    private void updateMessageHandler (Context context) throws SQLException{

         //extract the message id from the path parameter (URL)
         int message_id = Integer.parseInt(context.pathParam("message_id"));
         String newMessageText = context.bodyAsClass(Message.class).getMessage_text();
         
        try{

        //call service class to update the message
        Message updatedMessage = messageService.updateMessageContent (message_id, newMessageText);

        if (updatedMessage != null){
            context.status(200).json(updatedMessage);    //return updated message on success 
        }else{
            context.status(400).result("Message not found or could not be updated."); //if message is not found
        }

        }catch (IllegalArgumentException e){
            context.status(400).result("Invalid message ID or content.");  //handles invalid input
        }catch (NoSuchElementException e){
            context.status(400).result(e.getMessage());
        }catch (SQLException e){
            context.status(500).result(e.getMessage());
        }

    }
/***********************************Update a message by ID handler ************************************************/
    private void getAllMessagesByAccountHandler (Context context) throws SQLException{
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