

/*Uses Bcrypt for password saltin' and hashin' :Copyright (c) 2006 Damien Miller <djm@mindrot.org>
 * 
 */

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import java.sql.*;
import java.time.Instant;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;





public class webApp extends HttpServlet {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	//SecureRandom sr = SecureRandom.getInstance("SHA1PRNG", "SUN"); create
	//sr.nextBytes(byte[]);seeds self
	//sr.nextBytes(bye[]); fills byte array with random bytes
	//convert byte[] to string
	
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		// TODO Auto-generated method stub
		//resp.setStatus(303);
		//resp.setHeader("Location", "http://localhost:8080/ThingPile/app");
		//resp.setContentType("text/html");
		Cookie cookies[];
		Map<String,String> cookieMap;
		Connection conn;
		PreparedStatement lookupSessionSt;
		String lookupSessionStr = ("SELECT * FROM sessions WHERE id = ? AND timestamp > ? AND loggedout = false");//going to have to include something regarding timestamp here i think...fuck 
		//otherwise I'll get every single session from this user ohhhhh log
		ResultSet getSessionResults;
		ResultSetMetaData getSessionResMeta;
		int columns;
		ArrayList<HashMap<String,Object>> sessionResultList;
		Instant currentTime;
		Timestamp oldestTimestamp;
		boolean redirect;
		String respMessage;
		
		conn = null;
		lookupSessionSt = null;
		getSessionResults = null;
		cookies = null;
		redirect = false;
		respMessage = "initialized";
		
		
		
		
		PrintWriter out;
		out = resp.getWriter();
		
		cookies = req.getCookies();
		if(cookies == null){//no cookies found
			 //TODO:reroute to login
			redirect = true;
			
		}else{//cookies found find session cookie
			
			cookieMap = new HashMap<String,String>();
			
			for(int i = 0; i < cookies.length; i++){
				cookieMap.put(cookies[i].getName(), cookies[i].getValue());
			}
			
			if(!cookieMap.containsKey("wat")){//there is no session cookie so leave
				
				redirect = true;
			}else{//session cookie found proceed
				conn = connectDB();
				
				if(conn == null){	
					respMessage ="error connecting to db";

				}else{
				
					try {
					
						lookupSessionSt = conn.prepareStatement(lookupSessionStr);
						currentTime = Instant.now();
						oldestTimestamp = new Timestamp(currentTime.toEpochMilli() -(1000  * 60 * 60));//now - 1 hours ago is the oldest timestamp allowable
						lookupSessionSt.setString(1, cookieMap.get("wat"));//get session id from cookie
						lookupSessionSt.setTimestamp(2, oldestTimestamp);//filter by oldest allowable session time
						
						//TODO:  lookup get all sessions matching... what if multiple results like multiple logins wat do ? is there even a problem?
						getSessionResults = lookupSessionSt.executeQuery();
						sessionResultList = new ArrayList<HashMap<String,Object>>();
						getSessionResMeta = getSessionResults.getMetaData();
						columns = getSessionResMeta.getColumnCount();
						
						while(getSessionResults.next()){
							HashMap<String,Object> row = new HashMap<String,Object>(columns);
							for(int i = 1; i <= columns;i++){
								row.put(getSessionResMeta.getColumnName(i), getSessionResults.getObject(i));
							}
							sessionResultList.add(row);
						}
						
						if(sessionResultList.isEmpty()){
							redirect = true;
						}else{//now I have a session  thats valid  TODO:  figure out how to load things... also update timestamp
							//out.println("session is good, user: "+(String)sessionResultList.get(0).get("username"));//dont reroute or do anything
							RequestDispatcher view = req.getRequestDispatcher("/ThingPile.html");

					        view.forward(req, resp);
							return;
							
							
							
						}
						
					
					
					
					} catch (SQLException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}finally{//TODO: close everything ever!
						try{
							if(!conn.isClosed()){
								conn.close();
							}
							if(lookupSessionSt != null && !lookupSessionSt.isClosed()){
								lookupSessionSt.close();
							}
							if(getSessionResults != null && !getSessionResults.isClosed()){
								getSessionResults.close();
							}
						}catch(SQLException e){
							e.printStackTrace(out);
						}
						
					}
				
				
				}//end of else contains sessionid cookie
				
				
				
				
				
				
			}//end of session cookie found
			
				
				
		}//end of cookies found
			
		
		if(redirect == true){
			resp.setStatus(303);
			resp.setHeader("Location", "http://localhost:8080/ThingPile/login");
			resp.setContentType("text/html");
			
		}
		else{
			out.println(respMessage);
		}
		
		
		return;
		
		
		
		
	}//end of GET method
	
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
