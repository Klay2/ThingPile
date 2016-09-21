

import java.io.IOException;
import java.io.PrintWriter;

import java.sql.*;
import java.util.Map;
import java.util.Properties;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.mindrot.jbcrypt.BCrypt;


public class CreateAccount extends HttpServlet{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		// TODO Auto-generated method stub
		
		RequestDispatcher view = req.getRequestDispatcher("/createAccount.html");

        view.forward(req, resp);
		
	}
	
	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		// TODO Auto-generated method stub
		String userNameIn;
		String passIn;
		String hashedPass;
		String lookupUserStr = "SELECT * FROM users WHERE username = ?";
		String createUserStr = "insert into users (username, password, loginattempts, locked) values (?, ?, 0, false)";
		PrintWriter out;
		Connection conn;
		PreparedStatement lookupUserSt;
		PreparedStatement createUserSt;
		int rowsAltered;
		Map<String,String[]> requestParams;
		ResultSet lookupResult;
		String respMessage;
		boolean redirect;
		
		
		//initialize to null so that I can handle a group close in the finally block
		conn = null;
		lookupUserSt = null;
		createUserSt = null;
		lookupResult = null;
		respMessage = "initialized";
		redirect = false;
		
		requestParams = req.getParameterMap();
		
		out = resp.getWriter();
		
		
		if(!requestParams.containsKey("password") || !requestParams.containsKey("username")){
			respMessage = "username and password fields not present";
			
		}else{
		
			//get username and password from form parameter data
			userNameIn = req.getParameter("username").toLowerCase();
			passIn = req.getParameter("password");
			
		
			
			conn = connectDB();
			if(conn == null){
				respMessage = "Database Connection Failed! ";
			
			}else{
			
			
			
				try {
			
			
					lookupUserSt = conn.prepareStatement(lookupUserStr);
					lookupUserSt.setString(1, userNameIn);
				
				
				//TODO:!!!implement using SSL!!
				
					lookupResult = lookupUserSt.executeQuery();
					if(lookupResult.next() == true){
						respMessage = "There is already a user by that name";
					
						
					
					}else{//username does not already exist continue
				
					
					
						hashedPass = BCrypt.hashpw(passIn, BCrypt.gensalt(12));
					
					
					
						createUserSt = conn.prepareStatement(createUserStr);
						createUserSt.setString(1, userNameIn);
						createUserSt.setString(2, hashedPass);
				
						rowsAltered = createUserSt.executeUpdate();
						
						if(rowsAltered < 1){
						
							respMessage = "New user not created for some reason I dont know error database whatever";
						
						}else{
					
							//everything went well... redirect to login
							//TODO: probably need more headers to be more legit or something
							
							redirect = true;
							
						}
			}//end of else new user case
				
		} catch (SQLException e) {
				
			e.printStackTrace(out);
				
			return;
				
		}finally{//the finally close the whole world block
			try{
				if(!conn.isClosed()){
					conn.close();
				}
				if(lookupUserSt != null && !lookupUserSt.isClosed()){
					lookupUserSt.close();
				}
				if(lookupResult != null && !lookupResult.isClosed()){
					lookupResult.close();
				}
			}catch(SQLException e){
				e.printStackTrace(out);
			}
		}
		
		}//end of database connection good
		
		}//end of parameters good	
		
		if(redirect == true){
			resp.setStatus(303);
			resp.setHeader("Location", "http://localhost:8080/ThingPile/login");
			resp.setContentType("text/html");
			
		}else{
			out.println(respMessage);
		}
		return;
	}//end of POST method
	
	
	
	
	//private helper method to connect to the database
	//returns a connection, null if something went wrong
	private Connection connectDB(){
		try{
			Class.forName("org.postgresql.Driver");
		}catch(ClassNotFoundException e){
			e.printStackTrace();
			return null;
		}
		
		String dburl = "jdbc:postgresql://127.0.0.1/klaytest";
		Properties props = new Properties();
		props.setProperty("user","klay");
		props.setProperty("password","PASSWORD");
		//props.setProperty("ssl","true");  TODO:  figure this noise out someday
		try {
			Connection conn = DriverManager.getConnection(dburl, props);
			return conn;
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
		
		
	}
	
	

}
