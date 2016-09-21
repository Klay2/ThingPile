

import java.io.IOException;
import java.io.PrintWriter;
import java.security.SecureRandom;
import java.sql.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.mindrot.jbcrypt.BCrypt;

public class login extends HttpServlet {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		// TODO Auto-generated method stub
		
		RequestDispatcher view = req.getRequestDispatcher("/login.html");

        view.forward(req, resp);
		
	}
	
	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		Connection conn;
		String userNameIn;
		String passIn;
		String methodIn;
		int attempts;
		int rowsAltered;
		Map<String,String[]> requestParams;
		PrintWriter out;
		Cookie sessionCk;
		SecureRandom randy;
		int randyInt;
		String sessId;
		Instant currTime;
		Timestamp currTimeStmp;
		PreparedStatement lookupUserStmt;
		PreparedStatement incAttemptsSt;
		PreparedStatement lockAccntSt;
		PreparedStatement resetAttemptsSt;
		PreparedStatement createSessionSt;
		ResultSet getUserRes;
		ArrayList<HashMap<String,Object>> resultList;
		String lookupUserStr = "SELECT * FROM users WHERE username = ?";
		String incAttemptsStr = "UPDATE users SET loginattempts = loginattempts + 1 WHERE username = ?";
		String lockAccntStr =  "UPDATE users SET locked = TRUE WHERE username = ?";
		String resetAttemptsStr = "UPDATE users SET loginattempts = 0 WHERE username = ?";
		String createSessionStr = "INSERT INTO sessions (id, username, timestamp, method, token, loggedout) VALUES (?, ?, ?, ?, ?, false)";
		boolean redirect;
		String respMessage;
		
		
		
		//TODO: add header that indicates whether its a redirect or an error/status message
		
		//initializing closeables to null for an easier close finally block
		lookupUserStmt = null;
		incAttemptsSt = null;
		lockAccntSt = null;
		resetAttemptsSt = null;
		createSessionSt = null;
		getUserRes = null;
		redirect = false;
		respMessage = "initialized";
		
		
		
		out = resp.getWriter();

		
		requestParams = req.getParameterMap();
		
		//check for all the necessary parameters immediately
		
		if(!requestParams.containsKey("method") || !requestParams.containsKey("username")){
			resp.setStatus(400);//bad parameters...
			respMessage = "did not find  method or username on post";
			
		}else{
		
		methodIn = req.getParameter("method");
		
		if(methodIn.equals("standard")  && !requestParams.containsKey("password")){
			resp.setStatus(400);
			respMessage = "did not find password parameter on postfor method standard";
		
		}else{
		if(methodIn.equals("facebook") && !requestParams.containsKey("token")){
			resp.setStatus(400);
			respMessage = "did not find token parameter on post for method facebook";

		}
		
		
		conn = connectDB();
		if(conn == null){
			respMessage = "Database Connection Failed! ):";
			
		}
		else{		
		
		
		try {
			
			userNameIn = req.getParameter("username");
			
			
			
			
			if(methodIn.equals("standard")){
				//find user in usertable
				userNameIn = userNameIn.toLowerCase();
				passIn = req.getParameter("password");
				lookupUserStmt = conn.prepareStatement(lookupUserStr);//lookup user and get results
				lookupUserStmt.setString(1,userNameIn);
				getUserRes = lookupUserStmt.executeQuery();//lookup login name
			
				resultList = new ArrayList<HashMap<String,Object>>();
			
				ResultSetMetaData md = getUserRes.getMetaData();
				int columns = md.getColumnCount();
			
				//get Results and put in a list of maps, each result row is  a slot in the list and each column
				//is a key-object pair in a map, key is the string name of the column
				while(getUserRes.next()){
					HashMap<String,Object> row = new HashMap<String,Object>(columns);
					for(int i = 1; i <= columns; i++){
						row.put(md.getColumnName(i),getUserRes.getObject(i));
					}
					resultList.add(row);
				}	
			
				if(resultList.isEmpty()){//redirect 1 badusername/password
					respMessage = "Bad username or password";//no such user
				
				}else{//case: user found
				
				
					attempts = (Integer)resultList.get(0).get("loginattempts");
				
					if(attempts == 4){//successful login but user is locked out
					
						respMessage = "Your account is locked. -dosomething";//redirect 2 locked out
					
					}else{//not locked out, check password, reset login attempts
				
						if(!BCrypt.checkpw(passIn, (String)resultList.get(0).get("password"))){//check if password matches
							//password does not match
					
							if(attempts < 4){//number of login attempts are still under maximum
								
								rowsAltered = 0;//increment login attempts
								incAttemptsSt = conn.prepareStatement(incAttemptsStr);
								incAttemptsSt.setString(1, userNameIn);
								rowsAltered = incAttemptsSt.executeUpdate();
								
								
								if(rowsAltered < 1){
									respMessage = "error increasing login attempts";

								}else{
								
									respMessage = "Bad username or password";//redirect 3 password not match
								}
							
							}else{//attempts has been increased to value 4.. lockout the user!
								rowsAltered = 0;
								lockAccntSt = conn.prepareStatement(lockAccntStr);
								lockAccntSt.setString(1, userNameIn);
								rowsAltered = lockAccntSt.executeUpdate();
								if(rowsAltered < 1){
									respMessage = "error locking account";

								}else{
							
									//lockout message and return
									respMessage = "Your account has been locked. -dosomething";//redirect 4 lock out
								
								}
							}
							
						
						
						}else{//case:  password DOES  match reset login attempts
							
							rowsAltered = 0;
							resetAttemptsSt = conn.prepareStatement(resetAttemptsStr);//successful login try to reset login attempts
							resetAttemptsSt.setString(1, userNameIn);
							rowsAltered = resetAttemptsSt.executeUpdate();
						
							if(rowsAltered < 1){
								respMessage="error resetting login attempts";
								
							}else{
								redirect = true;
								
							}//end of case: successful login standard method
						
					
						}//end of case: password matches
				
				
					}//end of case: user found and not locked out
				
			
				}//end of case: user found
				
				
			}else{//end of case:method = standard  METHOD= facebook
				if(userNameIn != null && !userNameIn.equals("undefined") && !req.getParameter("token").equals("undefined")){
					redirect = true;
				}
				else{
					respMessage ="error with facebook login";
				}
				
				
			}
			
			
			
			//Create new Session
			//random generate a number, convert to string and use it for a session identifier token
			if(redirect == true){
				randy = new SecureRandom();
				randy.nextInt();
				randyInt = randy.nextInt();
				sessId = Integer.toString(randyInt);
		
				// TODO: consider also using non cookie method sometime
				sessionCk = new Cookie("wat",sessId);
				//TODO: add secure when using full https!!
				sessionCk.setHttpOnly(true);
				sessionCk.setMaxAge(60 * 60 * 4); //lasts 4 hours
				//sessionCk.setDomain(".app.localhost"); TODO: fix this... or something get to work for production code
				resp.addCookie(sessionCk);
		
			
				currTime = Instant.now();//createTimestamp
				currTimeStmp = new Timestamp(currTime.toEpochMilli());
		
				rowsAltered = 0;
				createSessionSt = conn.prepareStatement(createSessionStr);
				createSessionSt.setString(1, sessId);
				createSessionSt.setString(2,userNameIn);
				createSessionSt.setTimestamp(3, currTimeStmp);
				createSessionSt.setString(4, methodIn);
			
				if(methodIn.equals("facebook")){
					createSessionSt.setString(5, req.getParameter("token"));
				}else{
					createSessionSt.setString(5, "null");
				}
				rowsAltered = createSessionSt.executeUpdate();
				if(rowsAltered == 0){
					redirect = false;
					respMessage = "session not successfully created for some reason";
				}
		//TODO:  jbcrypt hash the session string and store it into the db!!!!
		
			
				
				
			}//end redirect = true
				
			
		}catch (SQLException e) {
			
			e.printStackTrace();
			return;
		}finally{
			//TODO:  close everything everywhere 
			try{
				if(!conn.isClosed()){
					conn.close();
				}
				if(lookupUserStmt != null && !lookupUserStmt.isClosed()){
					lookupUserStmt.close();
				}
				if(incAttemptsSt != null && !incAttemptsSt.isClosed()){
					incAttemptsSt.close();
				}
				if(lockAccntSt != null && !lockAccntSt.isClosed()){
					lockAccntSt.close();
				}
				if(resetAttemptsSt != null && !resetAttemptsSt.isClosed()){
					resetAttemptsSt.close();
				}
				if(createSessionSt != null && !createSessionSt.isClosed()){
					createSessionSt.close();
				}
				if(getUserRes != null && !getUserRes.isClosed()){
					getUserRes.close();
				}
			}catch(SQLException e){
				e.printStackTrace(out);
			}
		}//end finally
		
		
		
		}//end db connection if/else
		}//end standard/password parameter if/else
		}//end method/username parameter check if/else
		
		if(redirect == true){
			resp.setStatus(303);
			resp.setHeader("Location", "http://localhost:8080/ThingPile");
			resp.addHeader("redirect", "true");
			resp.setContentType("text/html");
			//out.println("redirected go");
			
			
		}else{
			resp.addHeader("redirect", "false");
			out.println(respMessage);
			
		}
		
		return;
		
		
		
		
		
		
	}//endPOST
	
	
	
	
	
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
