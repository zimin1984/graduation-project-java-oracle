import java.io.*;
import javax.servlet.*;
import javax.servlet.http.*;
import java.sql.*;

public class Autentification extends HttpServlet {  
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    HttpSession session=request.getSession(true);
    Connection conn=(Connection)session.getValue("conn");

    String userName=request.getParameter("userName"); 
    String password=request.getParameter("password"); 
    RussianSymbolsConverter rsc = new RussianSymbolsConverter();
    userName = rsc.convertString(userName);
    password = rsc.convertString(password);
      
    String dbUser="q", dbPassword="w";
    String connString = "jdbc:oracle:thin:@localhost:1521:orcl";
    
    Statement st; 
    ResultSet rs;
   
    response.setContentType("text/html; charset=windows-1251");
    PrintWriter out = response.getWriter();
    out.println("<HTML><BODY>");
     
     try{
        if(conn==null) {
           DriverManager.registerDriver (new oracle.jdbc.OracleDriver());
           conn = DriverManager.getConnection(connString, dbUser, dbPassword);
           session.putValue("conn",conn);
        }
         
            st = conn.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);          
            if (userName.equalsIgnoreCase("administrator")==true){
               rs=st.executeQuery("select * from users where user_name='administrator'");
               while(rs.next()) {
                  String adminPassword=rs.getString("password");
                  if(adminPassword.equals("no password")==false) {
                     if(adminPassword.equals(password)==false) {                     
                       out.println("<script language=\"JavaScript\">window.alert('Неправильный пароль.');");
                       out.println("window.location='..';</script>");
                       break;
                     }
                  }                
                  session.putValue("userName","administrator");  
                  session.putValue("privilege","3");
                  out.println("<script language=\"JavaScript\">window.location='../html/Frames.htm';</script>");
               }        
            }
            else if(userName.equalsIgnoreCase("anonymous")==true){
               session.putValue("userName","anonymous");  
               out.println("<script language=\"JavaScript\">window.location='../html/Frames.htm';</script>");
            }
            else {
               rs=st.executeQuery("select * from users where user_name='" + userName +"' and password='" + password +"'");
               while(rs.next()) {                  
                  session.putValue("userName",userName);
                  session.putValue("privilege",rs.getString("privilege"));
                  out.println("<script language=\"JavaScript\">window.location='../html/Frames.htm';</script>");
               }             
               out.println("<script language=\"JavaScript\">window.alert('Неправильное имя пользователя или пароль.');");
               out.println("window.location='..';</script>");
            }
    }    
    catch( SQLException ex){
      out.println("<H1 ALIGN=CENTER>Внутренняя ошибка сервера: нет доступа к базе данных.</H1>\n\n"+ ex.toString());
    }
    out.println("</BODY></HTML>");
    out.close();
  }
}    