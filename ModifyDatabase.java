import java.io.*;
import javax.servlet.*;
import javax.servlet.http.*;
import java.sql.*;

public class ModifyDatabase extends HttpServlet {
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    HttpSession session=request.getSession(false);
    Connection conn=(Connection)session.getValue("conn");  
    String userName=(String)session.getValue("userName");  
    Statement st,st2; 
    ResultSet rs,rs2;
    PreparedStatement pst;     
    RussianSymbolsConverter rsc = new RussianSymbolsConverter();
    DateFormatConverter dfc = new DateFormatConverter();
    response.setContentType("text/html; charset=windows-1251");
    PrintWriter out = response.getWriter();
    out.println("<html><body>");
    if(userName==null){
      out.println("<script language=\"JavaScript\">window.alert('Сначала следует пройти аутентификацию.');");
      out.println("top.location='..';</script>");
    }   
    else{
      String actionType=request.getParameter("actionType");     
      if(actionType.equals("add_work")==true){
        boolean isEnabled=true;
        String workName = request.getParameter("workName");  
        workName=rsc.convertString(workName);
        String coordinator = request.getParameter("coordinator");  
        coordinator=rsc.convertString(coordinator);      
        String status = request.getParameter("status");  
        status=rsc.convertString(status);
        String htmlCodeForSave=request.getParameter("htmlCodeForSave");   
        htmlCodeForSave=rsc.convertString(htmlCodeForSave);              
        String nextWorkId=new String();
        try{               
          st = conn.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE); 
          String selectedDepartment=(String)session.getValue("department");
          rs = st.executeQuery("select enabled_department from users where user_name='"+userName+"'");        
          while(rs.next()) {                                        
            if(rs.getString("enabled_department").equals("all")==false){
              if(rs.getString("enabled_department").equals(selectedDepartment)==false){
                out.println("<script>window.alert('Вам не разрешено добавлять работы в выбранное подразделение. Выберите разрешенное подразделение.');"+
                   "history.back(); </script>");                
                isEnabled=false;
              }             
            }
          }
         if(isEnabled==true){           
           rs = st.executeQuery("select works_sequence.nextval from dual");
           while(rs.next()) {                                          
             nextWorkId=rs.getString("nextval");
           }
          
           pst=conn.prepareStatement("insert into works (work_id, work_name, coordinator, status,vladelets,department_id) values (?,?,?,?,?,?)");
           pst.setObject(1,(Object)nextWorkId,Types.INTEGER);  
           pst.setObject(2,(Object)workName,Types.VARCHAR);  
           pst.setObject(3,(Object)coordinator,Types.VARCHAR);  
           pst.setObject(4,(Object)status,Types.VARCHAR); 
           pst.setObject(5,(Object)userName,Types.VARCHAR);             
           String selectedDepartment=(String)session.getValue("department");                
           rs = st.executeQuery("select department_id from departments where department_name='"+selectedDepartment+"'");
           while(rs.next()) {     
             pst.setObject(6,(Object)rs.getString("department_id"),Types.INTEGER); 
           }
           
           pst.executeUpdate();     
          
           rs = st.executeQuery("select * from temp_tasks");
           while(rs.next()) {                
             String taskName=rs.getString("task_name");         
             String taskContent=rs.getString("task_content");      
             String place=rs.getString("place");      
             String executors=rs.getString("executors");      
             String beginDate=rs.getString("begin_date"); 
             beginDate = dfc.convertToDate(beginDate);
             String endDate=rs.getString("end_date");      
             endDate = dfc.convertToDate(endDate);
             String vypolneno=rs.getString("vypolneno");               
             String tdId=rs.getString("td_id");               
             String taskProblems=rs.getString("task_problems"); 
             pst=conn.prepareStatement("insert into tasks (task_id, task_name, task_content, place, executors, begin_date, end_date, vypolneno, td_id,task_problems, work_id) values (tasks_sequence.nextval,?,?,?,?,to_date(?),to_date(?),?,?,?,?)");
             pst.setObject(1,(Object)taskName,Types.VARCHAR);  
             pst.setObject(2,(Object)taskContent,Types.VARCHAR);  
             pst.setObject(3,(Object)place,Types.VARCHAR);  
             pst.setObject(4,(Object)executors,Types.VARCHAR);  
             pst.setObject(5,(Object)beginDate,Types.VARCHAR);  
             pst.setObject(6,(Object)endDate,Types.VARCHAR);  
             pst.setObject(7,(Object)vypolneno,Types.VARCHAR);  
             pst.setObject(8,(Object)tdId,Types.VARCHAR);  
             pst.setObject(9,(Object)taskProblems,Types.VARCHAR);  
             pst.setObject(10,(Object)nextWorkId,Types.INTEGER);  
             pst.executeUpdate();       
           } 
           st.executeUpdate("delete from temp_tasks");   
         }
        } catch( SQLException ex){
           out.println("<H1 ALIGN=CENTER>Внутренняя ошибка сервера: нет доступа к базе данных.</H1>\n\n"+ rsc.convertString(ex.toString()));
        } 
        try {      
           File f=new File("UsersWorkspace",nextWorkId+".htm");
           FileOutputStream fos=new FileOutputStream(f);
           OutputStreamWriter output = new OutputStreamWriter(fos,"windows-1251");   
           output.write(htmlCodeForSave);
           output.flush(); 
        } 
        catch(Exception e) {
          out.println("<h1 align=center>Внутренняя ошибка приложения: нет доступа к файлам с данными.</h1>\n\n"+ e.toString());
        }        
         out.println("<script>window.location='../servlet/WebPagesGenerator?actionType=correct_works';</script>");
        
      }
      
      else if(actionType.equals("add_task")==true){           
        String taskName=request.getParameter("taskName");
        taskName=rsc.convertString(taskName);        
        String taskContent=request.getParameter("taskContent");
        taskContent=rsc.convertString(taskContent);
        String place=request.getParameter("place");
        place=rsc.convertString(place);
        String executors=request.getParameter("executorsHidden");
        executors=rsc.convertString(executors);
        String beginDate=request.getParameter("beginDate");
        String endDate=request.getParameter("endDate");
        String vypolneno=request.getParameter("vypolneno");   
        vypolneno=vypolneno.replaceAll("%","");
        String tdId=request.getParameter("td_id");
        String taskProblems=request.getParameter("taskProblems");
        taskProblems=rsc.convertString(taskProblems);
         
        try{      
          pst=conn.prepareStatement("insert into temp_tasks (task_id, task_name, task_content, place, executors, begin_date, end_date, vypolneno, td_id,task_problems,work_id) values (temp_tasks_sequence.nextval,?,?,?,?,to_date(?),to_date(?),?,?,?,0)");
          pst.setObject(1,(Object)taskName,Types.VARCHAR);  
          pst.setObject(2,(Object)taskContent,Types.VARCHAR);  
          pst.setObject(3,(Object)place,Types.VARCHAR);  
          pst.setObject(4,(Object)executors,Types.VARCHAR);  
          pst.setObject(5,(Object)beginDate,Types.VARCHAR);  
          pst.setObject(6,(Object)endDate,Types.VARCHAR);  
          pst.setObject(7,(Object)vypolneno,Types.VARCHAR);  
          pst.setObject(8,(Object)tdId,Types.VARCHAR);  
          pst.setObject(9,(Object)taskProblems,Types.VARCHAR);  
        
         pst.executeUpdate();          
         out.println("<script>window.close();</script>");
       }
       catch(SQLException ex){ 
         out.println("<h1 align=center>Внутренняя ошибка сервера: нет доступа к базе данных.</h1>\n\n"+ ex.toString());
      }
    }
    
     else if(actionType.equals("save_modified_task")==true){       
       String taskId = request.getParameter("taskId");
       String taskName = request.getParameter("taskName");
       taskName=rsc.convertString(taskName);
       String taskContent = request.getParameter("taskContent");  
       taskContent=rsc.convertString(taskContent);
       String place = request.getParameter("place");
       place=rsc.convertString(place);
       String executors=request.getParameter("executorsHidden");
       executors=rsc.convertString(executors);
       String beginDate = request.getParameter("beginDate");  
       String endDate = request.getParameter("endDate");
       String vypolneno = request.getParameter("vypolneno");      
       vypolneno=vypolneno.replaceAll("%","");       
       String tdId=request.getParameter("td_id");
       String workId=request.getParameter("workId");
       String taskProblems=request.getParameter("taskProblems");
       taskProblems=rsc.convertString(taskProblems);
     
       try {       
         pst=conn.prepareStatement("update tasks set task_name=? where task_id='"+ taskId +"'");
         pst.setObject(1,(Object)taskName,Types.VARCHAR);  
         pst.executeUpdate();  
         pst=conn.prepareStatement("update tasks set task_content=? where task_id='"+ taskId +"'");
         pst.setObject(1,(Object)taskContent,Types.VARCHAR);  
         pst.executeUpdate();    
         pst=conn.prepareStatement("update tasks set place=? where task_id='"+ taskId +"'");         
         pst.setObject(1,(Object)place,Types.VARCHAR);  
         pst.executeUpdate();  
         pst=conn.prepareStatement("update tasks set executors=? where task_id='"+ taskId +"'");
         pst.setObject(1,(Object)executors,Types.VARCHAR);  
         pst.executeUpdate();  
         pst=conn.prepareStatement("update tasks set begin_date=to_date(?) where task_id='"+ taskId +"'");
         pst.setObject(1,(Object)beginDate,Types.VARCHAR);  
         pst.executeUpdate();    
         pst=conn.prepareStatement("update tasks set end_date=to_date(?) where task_id='"+ taskId +"'");
         pst.setObject(1,(Object)endDate,Types.VARCHAR);  
         pst.executeUpdate();  
         pst=conn.prepareStatement("update tasks set vypolneno=? where task_id='"+ taskId +"'");         
         pst.setObject(1,(Object)vypolneno,Types.VARCHAR);  
         pst.executeUpdate();    
         pst=conn.prepareStatement("update tasks set td_id=? where task_id='"+ taskId +"'");
         pst.setObject(1,(Object)tdId,Types.VARCHAR);  
         pst.executeUpdate();    
         pst=conn.prepareStatement("insert into temp_tasks (task_id, task_name, task_content, place, executors, begin_date, end_date, vypolneno, td_id,task_problems,work_id) values (temp_tasks_sequence.nextval,?,?,?,?,to_date(?),to_date(?),?,?,?,?)");
         pst.setObject(1,(Object)taskName,Types.VARCHAR);  
         pst.setObject(2,(Object)taskContent,Types.VARCHAR);  
         pst.setObject(3,(Object)place,Types.VARCHAR);  
         pst.setObject(4,(Object)executors,Types.VARCHAR);  
         pst.setObject(5,(Object)beginDate,Types.VARCHAR);  
         pst.setObject(6,(Object)endDate,Types.VARCHAR);  
         pst.setObject(7,(Object)vypolneno,Types.VARCHAR);  
         pst.setObject(8,(Object)tdId,Types.VARCHAR);                            
         pst.setObject(9,(Object)taskProblems,Types.VARCHAR);                            
         pst.setObject(10,(Object)workId,Types.INTEGER);                            
         pst.executeUpdate();      
         out.println("<script>window.close();</script>");
       }
       catch(SQLException ex){ 
         out.println("<h1 align=center>Внутренняя ошибка сервера: нет доступа к базе данных.</h1>\n\n"+ ex.toString());
       }
     }
    
     else if(actionType.equals("save_modified_temptask")==true){       
       String taskId = request.getParameter("taskId");
       String taskName = request.getParameter("taskName");
       taskName=rsc.convertString(taskName);
       String taskContent = request.getParameter("taskContent");  
       taskContent=rsc.convertString(taskContent);
       String place = request.getParameter("place");
       place=rsc.convertString(place);
       String executors=request.getParameter("executorsHidden");
       executors=rsc.convertString(executors);
       String beginDate = request.getParameter("beginDate");  
       String endDate = request.getParameter("endDate");
       String vypolneno = request.getParameter("vypolneno");      
       vypolneno=vypolneno.replaceAll("%","");
       String tdId=request.getParameter("td_id");
       String taskProblems=request.getParameter("taskProblems");
       taskProblems=rsc.convertString(taskProblems);
     
       try {       
         pst=conn.prepareStatement("update temp_tasks set task_name=? where task_id='"+ taskId +"'");
         pst.setObject(1,(Object)taskName,Types.VARCHAR);  
         pst.executeUpdate();  
         pst=conn.prepareStatement("update temp_tasks set task_content=? where task_id='"+ taskId +"'");
         pst.setObject(1,(Object)taskContent,Types.VARCHAR);  
         pst.executeUpdate();    
         pst=conn.prepareStatement("update temp_tasks set place=? where task_id='"+ taskId +"'");
         pst.setObject(1,(Object)place,Types.VARCHAR);  
         pst.executeUpdate();  
         pst=conn.prepareStatement("update temp_tasks set executors=? where task_id='"+ taskId +"'");
         pst.setObject(1,(Object)executors,Types.VARCHAR);  
         pst.executeUpdate();  
         pst=conn.prepareStatement("update temp_tasks set begin_date=to_date(?) where task_id='"+ taskId +"'");
         pst.setObject(1,(Object)beginDate,Types.VARCHAR);  
         pst.executeUpdate();    
         pst=conn.prepareStatement("update temp_tasks set end_date=to_date(?) where task_id='"+ taskId +"'");
         pst.setObject(1,(Object)endDate,Types.VARCHAR);  
         pst.executeUpdate();  
         pst=conn.prepareStatement("update temp_tasks set vypolneno=? where task_id='"+ taskId +"'");
         pst.setObject(1,(Object)vypolneno,Types.VARCHAR);  
         pst.executeUpdate();    
         pst=conn.prepareStatement("update temp_tasks set td_id=? where task_id='"+ taskId +"'");
         pst.setObject(1,(Object)tdId,Types.VARCHAR);  
         pst.executeUpdate();    
         pst=conn.prepareStatement("update temp_tasks set task_problems=? where task_id='"+ taskId +"'");
         pst.setObject(1,(Object)taskProblems,Types.VARCHAR);  
         pst.executeUpdate();    
         out.println("<script>window.close();</script>");
       }
       catch(SQLException ex){ 
         out.println("<h1 align=center>Внутренняя ошибка сервера: нет доступа к базе данных.</h1>\n\n"+ ex.toString());
       }
     }
    
    else if(actionType.equals("save_modified_work")==true){      
      String workId = request.getParameter("workId");
      String workName = request.getParameter("workName");  
      workName=rsc.convertString(workName);
      String coordinator = request.getParameter("coordinator");  
      coordinator=rsc.convertString(coordinator);      
      String status = request.getParameter("status");  
      status=rsc.convertString(status);
      String htmlCodeForSave=request.getParameter("htmlCodeForSave");   
      htmlCodeForSave=rsc.convertString(htmlCodeForSave);
      try {       
        pst=conn.prepareStatement("update works set work_name=? where work_id='"+ workId +"'");
        pst.setObject(1,(Object)workName,Types.VARCHAR);  
        pst.executeUpdate();  
        pst=conn.prepareStatement("update works set coordinator=? where work_id='"+ workId +"'");
        pst.setObject(1,(Object)coordinator,Types.VARCHAR);  
        pst.executeUpdate();  
        pst=conn.prepareStatement("update works set status=? where work_id='"+ workId +"'");
        pst.setObject(1,(Object)status,Types.VARCHAR);  
        pst.executeUpdate();       
        st = conn.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);  
        rs = st.executeQuery("select * from temp_tasks");
        while(rs.next()) {                
          String taskName=rs.getString("task_name");         
          String taskContent=rs.getString("task_content");      
          String place=rs.getString("place");      
          String executors=rs.getString("executors");      
          String beginDate=rs.getString("begin_date"); 
          beginDate = dfc.convertToDate(beginDate);
          String endDate=rs.getString("end_date");      
          endDate = dfc.convertToDate(endDate);
          String vypolneno=rs.getString("vypolneno");               
          String tdId=rs.getString("td_id");               
          String taskProblems=rs.getString("task_problems");    
          String wId=rs.getString("work_id");       
          if(wId.equals(workId)==true){
             pst=conn.prepareStatement("update tasks set task_name=? where td_id='"+tdId+"' and work_id='"+workId+"'");
             pst.setObject(1,(Object)taskName,Types.VARCHAR);  
             pst.executeUpdate();  
             pst=conn.prepareStatement("update tasks set task_content=? where td_id='"+tdId+"' and work_id='"+workId+"'");
             pst.setObject(1,(Object)taskContent,Types.VARCHAR);  
             pst.executeUpdate();    
             pst=conn.prepareStatement("update tasks set place=? where td_id='"+tdId+"' and work_id='"+workId+"'");
             pst.setObject(1,(Object)place,Types.VARCHAR);  
             pst.executeUpdate();  
             pst=conn.prepareStatement("update tasks set executors=? where td_id='"+tdId+"' and work_id='"+workId+"'");
             pst.setObject(1,(Object)executors,Types.VARCHAR);  
             pst.executeUpdate();  
             pst=conn.prepareStatement("update tasks set begin_date=to_date(?) where td_id='"+tdId+"' and work_id='"+workId+"'");
             pst.setObject(1,(Object)beginDate,Types.VARCHAR);  
             pst.executeUpdate();    
             pst=conn.prepareStatement("update tasks set end_date=to_date(?) where td_id='"+tdId+"' and work_id='"+workId+"'");
             pst.setObject(1,(Object)endDate,Types.VARCHAR);  
             pst.executeUpdate();  
             pst=conn.prepareStatement("update tasks set vypolneno=? where td_id='"+tdId+"' and work_id='"+workId+"'");
             pst.setObject(1,(Object)vypolneno,Types.VARCHAR);  
             pst.executeUpdate();
             pst=conn.prepareStatement("update tasks set task_problems=? where td_id='"+tdId+"' and work_id='"+workId+"'");
             pst.setObject(1,(Object)taskProblems,Types.VARCHAR);  
             pst.executeUpdate();
          }      
          else{           
            pst=conn.prepareStatement("insert into tasks (task_id, task_name, task_content, place, executors, begin_date, end_date, vypolneno, td_id, task_problems, work_id) values (tasks_sequence.nextval,?,?,?,?,to_date(?),to_date(?),?,?,?,?)");
            pst.setObject(1,(Object)taskName,Types.VARCHAR);  
            pst.setObject(2,(Object)taskContent,Types.VARCHAR);  
            pst.setObject(3,(Object)place,Types.VARCHAR);  
            pst.setObject(4,(Object)executors,Types.VARCHAR);  
            pst.setObject(5,(Object)beginDate,Types.VARCHAR);  
            pst.setObject(6,(Object)endDate,Types.VARCHAR);  
            pst.setObject(7,(Object)vypolneno,Types.VARCHAR);  
            pst.setObject(8,(Object)tdId,Types.VARCHAR);  
            pst.setObject(9,(Object)taskProblems,Types.VARCHAR);  
            pst.setObject(10,(Object)workId,Types.INTEGER);  
            pst.executeUpdate();      
           }
          }
          st.executeUpdate("delete from temp_tasks");
        try {               
          File f=new File("UsersWorkspace",workId+".htm");
          FileOutputStream fos=new FileOutputStream(f);         
          OutputStreamWriter output = new OutputStreamWriter(fos,"windows-1251");   
          output.write(htmlCodeForSave);
          output.flush(); 
        } 
        catch(Exception e) {
          out.println("<h1>Внутренняя ошибка приложения: нет доступа к файлам с данными.</h1>\n\n"+ e.toString());
        }        
                
        out.println("<script>window.location='../servlet/WebPagesGenerator?actionType=correct_works';</script>");
      }
      catch(SQLException ex){ 
        out.println("<h1 align=center>Внутренняя ошибка сервера: нет доступа к базе данных.</h1>\n\n"+ ex.toString());
      }
      out.println("<script>window.location='../servlet/WebPagesGenerator?actionType=correct_works';</script>");
    }
    
    else if(actionType.equals("add_user")==true){    
       String newUserName = request.getParameter("newUserName");  
       newUserName=rsc.convertString(newUserName);
       String newUserPassword = request.getParameter("newUserPassword");  
       newUserPassword=rsc.convertString(newUserPassword);
       String enabledDepartment = request.getParameter("enabledDepartment");  
       enabledDepartment=rsc.convertString(enabledDepartment);
       String privilege = request.getParameter("privilege");  
       if(privilege.equals("3")==true) enabledDepartment="all";
       try{      
          pst=conn.prepareStatement("insert into users (user_id, user_name, password, enabled_department, privilege) values (users_sequence.nextval,?,?,?,?)");
          pst.setObject(1,(Object)newUserName,Types.VARCHAR);  
          pst.setObject(2,(Object)newUserPassword,Types.VARCHAR);  
          pst.setObject(3,(Object)enabledDepartment,Types.VARCHAR);  
          pst.setObject(4,(Object)privilege,Types.INTEGER);          
          pst.executeUpdate();          
          out.println("<script>window.location='../servlet/WebPagesGenerator?actionType=correct_users';</script>");
       }
       catch(SQLException ex){ 
         out.println("<h1 align=center>Внутренняя ошибка сервера: нет доступа к базе данных.</h1>\n\n"+ ex.toString());
       }
     }   
     
     else if(actionType.equals("save_modified_user")==true){      
      String userId = request.getParameter("userId");
      String newUserName = request.getParameter("newUserName");  
      newUserName=rsc.convertString(newUserName);
      String newUserPassword = request.getParameter("newUserPassword");  
      newUserPassword=rsc.convertString(newUserPassword);      
      String enabledDepartment = request.getParameter("enabledDepartment");  
      enabledDepartment=rsc.convertString(enabledDepartment);
      String privilege = request.getParameter("privilege");  
      if(privilege.equals("3")==true) enabledDepartment="all";
      try {       
        pst=conn.prepareStatement("update users set user_name=? where user_id='"+ userId +"'");
        pst.setObject(1,(Object)newUserName,Types.VARCHAR);  
        pst.executeUpdate();  
        pst=conn.prepareStatement("update users set password=? where user_id='"+ userId +"'");
        pst.setObject(1,(Object)newUserPassword,Types.VARCHAR);  
        pst.executeUpdate();  
        pst=conn.prepareStatement("update users set enabled_department=? where user_id='"+ userId +"'");
        pst.setObject(1,(Object)enabledDepartment,Types.VARCHAR);  
        pst.executeUpdate();     
        pst=conn.prepareStatement("update users set privilege=? where user_id='"+ userId +"'");
        pst.setObject(1,(Object)privilege,Types.INTEGER);  
        pst.executeUpdate();    
        out.println("<script>window.location='../servlet/WebPagesGenerator?actionType=correct_users';</script>");
        }
       catch(SQLException ex){ 
         out.println("<h1 align=center>Внутренняя ошибка сервера: нет доступа к базе данных.</h1>\n\n"+ ex.toString());
       }
     }
     
    else if(actionType.equals("add_place")==true){    
       String placeName = request.getParameter("placeName");  
       placeName=rsc.convertString(placeName);
       String departmentName = request.getParameter("departmentName");  
       departmentName=rsc.convertString(departmentName);
       
       try{      
          pst=conn.prepareStatement("insert into places (place_id, place_name, department_name) values (places_sequence.nextval,?,?)");
          pst.setObject(1,(Object)placeName,Types.VARCHAR);  
          pst.setObject(2,(Object)departmentName,Types.VARCHAR);            
          pst.executeUpdate();          
          out.println("<script>window.location='../servlet/WebPagesGenerator?actionType=correct_places';</script>");
       }
       catch(SQLException ex){ 
         out.println("<h1 align=center>Внутренняя ошибка сервера: нет доступа к базе данных.</h1>\n\n"+ ex.toString());
       }
     }   
      
    else if(actionType.equals("add_department")==true){    
       String departmentName = request.getParameter("departmentName");  
       departmentName=rsc.convertString(departmentName);       
       
       try{      
          pst=conn.prepareStatement("insert into departments (department_id, department_name) values (departments_sequence.nextval,?)");
          pst.setObject(1,(Object)departmentName,Types.VARCHAR);            
          pst.executeUpdate();          
          out.println("<script>window.location='../servlet/WebPagesGenerator?actionType=correct_departments';</script>");
       }
       catch(SQLException ex){ 
         out.println("<h1 align=center>Внутренняя ошибка сервера: нет доступа к базе данных.</h1>\n\n"+ ex.toString());
       }
     }   
     
     else if(actionType.equals("save_modified_place")==true){      
      String placeId = request.getParameter("placeId");
      String placeName = request.getParameter("placeName");  
      placeName=rsc.convertString(placeName);
      String departmentName = request.getParameter("departmentName");  
      departmentName=rsc.convertString(departmentName);
       
      try {       
        pst=conn.prepareStatement("update places set place_name=? where place_id='"+ placeId +"'");
        pst.setObject(1,(Object)placeName,Types.VARCHAR);  
        pst.executeUpdate();  
        pst=conn.prepareStatement("update places set department_name=? where place_id='"+ placeId +"'");
        pst.setObject(1,(Object)departmentName,Types.VARCHAR);  
        pst.executeUpdate();  
        
        out.println("<script>window.location='../servlet/WebPagesGenerator?actionType=correct_places';</script>");
        }
       catch(SQLException ex){ 
         out.println("<h1 align=center>Внутренняя ошибка сервера: нет доступа к базе данных.</h1>\n\n"+ ex.toString());
       }
     }
     
     else if(actionType.equals("save_modified_department")==true){      
      String departmentId = request.getParameter("departmentId");      
      String departmentName = request.getParameter("departmentName");  
      departmentName=rsc.convertString(departmentName);
       
      try {               
        pst=conn.prepareStatement("update departments set department_name=? where department_id='"+ departmentId +"'");
        pst.setObject(1,(Object)departmentName,Types.VARCHAR);  
        pst.executeUpdate();          
        out.println("<script>window.location='../servlet/WebPagesGenerator?actionType=correct_departments';</script>");
        }
       catch(SQLException ex){ 
         out.println("<h1 align=center>Внутренняя ошибка сервера: нет доступа к базе данных.</h1>\n\n"+ ex.toString());
       }
     }
     
     else if(actionType.equals("save_modified_worker")==true){
       if(userName.equalsIgnoreCase("administrator")==false){
         out.println("<h1 align=center>Ошибка: у Вас нет права изменять НСИ.</h1>");
       }
       else{         
         String workerId = request.getParameter("worker_id");
         String fio = request.getParameter("fio");  
         fio=rsc.convertString(fio);
         String doljnost = request.getParameter("doljnost");  
         doljnost=rsc.convertString(doljnost);
         String phone = request.getParameter("phone");  
         String work_group = request.getParameter("work_group");  
         String department = request.getParameter("department");  
         department=rsc.convertString(department);
         try {       
           pst=conn.prepareStatement("update workers set fio=? where worker_id='"+ workerId +"'");
           pst.setObject(1,(Object)fio,Types.VARCHAR);  
           pst.executeUpdate();  
           pst=conn.prepareStatement("update workers set doljnost=? where worker_id='"+ workerId +"'");        
           pst.setObject(1,(Object)doljnost,Types.VARCHAR);  
           pst.executeUpdate();  
           pst=conn.prepareStatement("update workers set phone=? where worker_id='"+ workerId +"'");        
           pst.setObject(1,(Object)phone,Types.VARCHAR);  
           pst.executeUpdate(); 
           pst=conn.prepareStatement("update workers set work_group=? where worker_id='"+ workerId +"'");  
           pst.setObject(1,(Object)work_group,Types.VARCHAR);  
           pst.executeUpdate(); 
           st = conn.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE); 
           rs=st.executeQuery("select department_id from departments where department_name='" + department +"'");         
           while(rs.next()) {
             department = rs.getString("department_id");          
           }
           pst=conn.prepareStatement("update workers set department_id=? where worker_id='"+ workerId +"'");  
           pst.setObject(1,(Object)department,Types.INTEGER);     
           pst.executeUpdate();                  
           out.println("<script language=\"JavaScript\">window.location='../servlet/WebPagesGenerator?actionType=correct_workers';</script>");
         }
         catch(SQLException ex){ 
           out.println("<h1 align=center>Внутренняя ошибка сервера: нет доступа к базе данных.</h1>\n\n"+ ex.toString());
         }
       }
      } 
      
      else if(actionType.equals("add_worker")==true){
        if(userName.equalsIgnoreCase("administrator")==false){
          out.println("<h1 align=center>Ошибка: у Вас нет права изменять НСИ.</h1>");
        }
        else{
          String fio = request.getParameter("fio");  
          fio=rsc.convertString(fio);
          String doljnost = request.getParameter("doljnost");  
          doljnost=rsc.convertString(doljnost);
          String phone = request.getParameter("phone");  
          String work_group = request.getParameter("work_group");  
          String department = request.getParameter("department");  
          department=rsc.convertString(department);
          try {       
            pst=conn.prepareStatement("insert into workers (worker_id, fio, doljnost, phone, work_group, department_id) values (workers_sequence.nextval,?,?,?,?,?)");
            pst.setObject(1,(Object)fio,Types.VARCHAR);  
            pst.setObject(2,(Object)doljnost,Types.VARCHAR);  
            pst.setObject(3,(Object)phone,Types.VARCHAR);  
            pst.setObject(4,(Object)work_group,Types.VARCHAR);  
        
            st = conn.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE); 
            rs=st.executeQuery("select department_id from departments where department_name='" + department +"'");         
            while(rs.next()) {
              department = rs.getString("department_id");          
            }
            pst.setObject(5,(Object)department,Types.INTEGER);     
            pst.executeUpdate();  
                
            out.println("<script language=\"JavaScript\">window.location='../servlet/WebPagesGenerator?actionType=correct_workers';</script>");
          }
          catch(SQLException ex){ 
            out.println("<h1 align=center>Внутренняя ошибка сервера: нет доступа к базе данных.</h1>\n\n"+ ex.toString());
          }
        }
      }
    }    
    out.println("</body></html>");
    out.close();
  }
  
   public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    HttpSession session=request.getSession(false);
    Connection conn=(Connection)session.getValue("conn");  
    String userName=(String)session.getValue("userName");  
    Statement st,st2; 
    ResultSet rs,rs2;
    PreparedStatement pst;     
    RussianSymbolsConverter rsc = new RussianSymbolsConverter();
    response.setContentType("text/html; charset=windows-1251");
    PrintWriter out = response.getWriter();
    out.println("<html><body>");
    if(userName==null){
      out.println("<script language=\"JavaScript\">window.alert('Сначала следует пройти аутентификацию.');");
      out.println("top.location='..';</script>");
    }   
    else{
      String actionType=request.getParameter("actionType");     
      if(actionType.equals("delete_work")==true){
        boolean isEnabled=true;
        String workId=request.getParameter("workId");      
        try {     
          st = conn.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE); 
          rs=st.executeQuery("select * from works where work_id='"+ workId +"'");
          while(rs.next()) {
             st2 = conn.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE); 
             String selectedDepartment=(String)session.getValue("department");
             rs2 = st2.executeQuery("select enabled_department,privilege from users where user_name='"+userName+"'");        
             while(rs2.next()) {                                        
               if(rs2.getString("enabled_department").equals("all")==false){
                  if(rs2.getString("enabled_department").equals(selectedDepartment)==false){
                    out.println("<script>window.alert('Вам не разрешено удалять работы в выбранном подразделении. Выберите разрешенное подразделение.');"+
                        "history.back(); </script>");                
                    isEnabled=false;
                    break;
                  }
                  if(rs2.getString("privilege").equals("1")==true){
                      if(rs.getString("vladelets").equals(userName)==false){
                         out.println("<script>window.alert('Вам не разрешено удалять выбранную работу, т.к. Вы не являетесь ее владельцем.');"+
                             "history.back(); </script>");                
                         isEnabled=false;
                      }
                  }
              }
            }
          }
          if(isEnabled==true){
            st = conn.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);
            st.executeUpdate("delete from works where work_id='" + workId +"'");          
          }
          out.println("<script>window.close();</script>");
        }
        catch(SQLException ex){ 
          out.println("<h1 align=center>Внутренняя ошибка сервера: нет доступа к базе данных.</h1>\n\n"+ ex.toString());
        }       
      }
      
      if(actionType.equals("delete_user")==true){
        String userId=request.getParameter("userId");      
        try {     
          st = conn.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE); 
          st.executeUpdate("delete from users where user_id='" + userId +"'");          
          out.println("<script>window.close();</script>");
        }
        catch(SQLException ex){ 
          out.println("<h1 align=center>Внутренняя ошибка сервера: нет доступа к базе данных.</h1>\n\n"+ ex.toString());
        }       
      }
       
      if(actionType.equals("delete_place")==true){
        String placeId=request.getParameter("placeId");      
        try {     
          st = conn.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE); 
          st.executeUpdate("delete from places where place_id='" + placeId +"'");          
          out.println("<script>window.close();</script>");
        }
        catch(SQLException ex){ 
          out.println("<h1 align=center>Внутренняя ошибка сервера: нет доступа к базе данных.</h1>\n\n"+ ex.toString());
        }       
      }
      
      if(actionType.equals("delete_department")==true){
        String departmentId=request.getParameter("departmentId");      
        try {     
          st = conn.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE); 
          st.executeUpdate("delete from departments where department_id='" + departmentId +"'");          
          out.println("<script>window.close();</script>");
        }
        catch(SQLException ex){ 
          out.println("<h1 align=center>Внутренняя ошибка сервера: нет доступа к базе данных.</h1>\n\n"+ ex.toString());
        }       
      }
      
      else if(actionType.equals("delete_temptasks")==true){      
        try {     
          st = conn.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE); 
          st.executeUpdate("delete from temp_tasks");       
          out.println("<script>window.close();</script>");
        }
        catch(SQLException ex){ 
          out.println("<h1 align=center>Внутренняя ошибка сервера: нет доступа к базе данных.</h1>\n\n"+ ex.toString());
        }     
      }   
      
      else if(actionType.equals("delete_task")==true){
       String tdId=request.getParameter("td_id");
       String workId=request.getParameter("workId");
       try{   
         st = conn.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);                    
         rs=st.executeQuery("select td_id, instr(td_id,'"+tdId+"') from temp_tasks"); 
         boolean isInTempTasksDeleted=false;
         while(rs.next()) {                                                  
            if(rs.getString(2).equals("0")==false){           
              st2 = conn.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);         
              st2.executeUpdate("delete from temp_tasks where td_id='"+rs.getString("td_id")+"'");  
              isInTempTasksDeleted=true;
            }
         }
         if( isInTempTasksDeleted==false){
           rs=st.executeQuery("select td_id, instr(td_id,'"+tdId+"') from tasks where work_id="+workId); 
           while(rs.next()) {                                                  
              if(rs.getString(2).equals("0")==false){           
                st2 = conn.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);         
                st2.executeUpdate("delete from tasks where td_id='"+rs.getString("td_id")+"' and work_id="+workId);   
              }
           }  
            File f=new File("UsersWorkspace",workId+".htm");
            if(f==null) 
              out.println("<h1>Внутренняя ошибка приложения: нет доступа к файлу с данными о задачах.</h1>");
          
            BufferedReader inStream=new BufferedReader(new FileReader(f));
            String theString=new String();
            String htmlCode=new String();          
            try {   
              while(true){
                theString=inStream.readLine();  
                htmlCode=htmlCode.concat(theString);
              }
            }
            catch(NullPointerException npe) {          
              inStream.close();                 
              htmlCode=htmlCode.replaceAll("add_work","edit_work&workId="+workId);
              htmlCode=htmlCode.concat("<script>document.all(\""+tdId+"\").innerHTML = \"\";</script>");
              FileOutputStream fos=new FileOutputStream(f);
              OutputStreamWriter output = new OutputStreamWriter(fos,"windows-1251");   
              output.write(htmlCode);
              output.flush(); 
            }
         }
         out.println("<script>window.close();</script>");
       }    
       catch( SQLException ex){
         out.println("<H1>Внутренняя ошибка сервера: нет доступа к базе данных.</H1>\n\n"+ ex.toString());
       }   
     }
     
     else if(actionType.equals("delete_worker")==true){
       String workerId = request.getParameter("worker_id");  
       if(userName.equalsIgnoreCase("administrator")==false){
         out.println("<h1 align=center>Ошибка: у Вас нет права изменять НСИ.</h1>");
       }
       else{
         try {     
           st = conn.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE); 
           st.executeUpdate("delete from workers where worker_id='" + workerId +"'");     
           out.println("<script language=\"JavaScript\">window.location='../servlet/WebPagesGenerator?actionType=correct_workers';</script>");
         }
         catch(SQLException ex){ 
           out.println("<h1 align=center>Внутренняя ошибка сервера: нет доступа к базе данных.</h1>\n\n"+ ex.toString());
         }
       }
     }
    }
    out.println("</body></html>");
    out.close();
   }
}