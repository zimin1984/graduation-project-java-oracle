import java.io.*;
import javax.servlet.*;
import javax.servlet.http.*;
import java.sql.*;

public class WebPagesGenerator extends HttpServlet {  
  Statement st,st2;
  ResultSet rs,rs2;
  DateFormatConverter dfc = new DateFormatConverter();
  RussianSymbolsConverter rsc = new RussianSymbolsConverter();
  
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    HttpSession session=request.getSession(false);
    Connection conn=(Connection)session.getValue("conn");  
    String userName=(String)session.getValue("userName");      
   
    response.setContentType("text/html; charset=windows-1251");
    PrintWriter out = response.getWriter();
    out.println("<html><head><link href=\"../html/css.htm\" type=\"text/css\" rel=\"Stylesheet\">"+
    "<script language=\"JavaScript\" src=\"../html/JavaScriptFunctions.js\"></script></head>");

    if(userName==null){
      out.println("<script language=\"JavaScript\">window.alert('Сначала следует пройти аутентификацию.');");
      out.println("top.location='..';</script>");
    }   

    String actionType=request.getParameter("actionType");
    if(actionType.equals("add_work")==true){   
          out.println("<body onload=\"window.open('../servlet/ModifyDatabase?actionType=delete_temptasks','_blank');\"><table class=\"HeaderOfTable\" border=\"0\" cellPadding=\"10\" cellSpacing=\"0\" >"+          
            "<tr>"+
              "<td width=\"90%\" bgColor=\"#ffffff\"></td>"+
              "<td><nobr>Ввод новой работы</nobr></td>"+
            "</tr>"+
          "</table>"+
          "<table bgColor=\"#ffffff\" border=\"7\" bordercolor=\"#666699\" width=\"100%\" height=\"65%\">"+
            "<td valign=\"top\">"+
              "<br>"+
              "<form method=\"post\" action=\"../servlet/ModifyDatabase\">"+          
                "<table class=\"TextInTable\" width=\"95%\" border=\"1\" bordercolor=\"#ffffff\" cellPadding=\"2\" cellSpacing=\"0\" align=\"center\">"+
                "<tr>"+
                  "<td class=\"HeaderOfTable\">Наименование работы </td> "+
                  "<td><input type=\"text\" name=\"workName\" size=\"100\"></td>"+
                "</tr>"+
                "<tr><td class=\"HeaderOfTable\">Координатор</td>"+
                    "<td><select name=\"coordinator\" size=\"1\">"+
                        "<option selected value=\"0\">-- Выберите сотрудника подразделения --");
                        try{   
                          st = conn.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);                    
                          rs=st.executeQuery("select * from workers");
                          while(rs.next()) {
                            String fio=rs.getString("fio");
                            out.println("<option value=\"" + fio + "\">" + fio);
                         }          
                       }    
                       catch( SQLException ex){
                          out.println("<H1 ALIGN=CENTER>Внутренняя ошибка сервера: нет доступа к базе данных.</H1>\n\n"+ ex.toString());
                       }                        
                     out.println("</select></td>"+
                "</tr>"+
                "<tr><td class=\"HeaderOfTable\">Статус</td>"+
                    "<td><select name=\"status\" size=\"1\">"+
                         "<option selected value=\"0\">-- Выберите статус --"+
                         "<option value=\"Открыто\">Открыто"+
                         "<option value=\"В работе, без отставания от плана\">В работе, без отставания от плана"+
                         "<option value=\"В работе, с запаздыванием\">В работе, с запаздыванием"+
                         "<option value=\"Приостановлено\">Приостановлено"+
                         "<option value=\"Решено\">Решено"+
                         "<option value=\"Закрыто\">Закрыто"+
                         "<option value=\"Отменено\">Отменено"+
                    "</select></td>"+
                "</tr>"+
                "</table>"+
                "<br>"+
                "<center>"+
                  "<input type=\"button\" value=\"Добавить задачу\" onclick=\"addTask();\">&nbsp;&nbsp;&nbsp;&nbsp;"+
                  "<input type=\"submit\" value=\"Сохранить работу\" onclick=\"saveHTML();\">"+                  
                  "<input type=\"hidden\" name=\"htmlCodeForSave\" id=\"_htmlCodeForSave\">"+                  
                  "<input type=\"hidden\" name=\"actionType\" value=\"add_work\">"+
                  "<input type=\"hidden\" id=\"_workId\" name=\"workId\" value=\"null\">"+
                "</center>"+
              "</form>"+
              "<h3 align=\"center\">Задачи, входящие в состав работы</h1>"+
              "<table class=\"TextInTable\" id=\"taskTable\" width=\"95%\"  border=\"1\" bordercolor=\"#666699\" cellPadding=\"1\" cellSpacing=\"0\" align=\"center\">"+
              "<tr><td id=\"task1\">"+
                "<input type=\"hidden\" value=\"0\" id=\"task1hidden\">"+
                "<a id=\"hyperLinkId_task1\" href=\"#\" onclick=\"javascript:editTask('task1');\">Задача 1</a>"+
                "&nbsp;<a href=\"javascript:addSubTask('task1','&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;');\">"+
                  "&nbsp;&nbsp;<img src=\"../images/add_subtask.gif\" border=\"0\">"+
                "</a>"+
                "&nbsp;<a href=\"javascript:deleteSubTask('task1');\">"+
                  "&nbsp;&nbsp;<img src=\"../images/delete_task.gif\" border=\"0\">"+
                "</a>"+
              "</td></tr>"+
              "</table>"+
            "</td>"+
          "</table>");
    }
    
    else  if(actionType.equals("correct_works")==true){
      out.println("<body><table class=\"HeaderOfTable\"  border=\"0\" cellPadding=\"10\" cellSpacing=\"0\" >"+
      "<tr>"+
        "<td bgColor=\"#ffffff\"  width=\"90%\"></td>"+
        "<td><nobr>Корректировка работ</nobr></td>"+
      "</tr>"+
      "</table>"+
      "<table bgColor=\"#ffffff\" border=\"7\" bordercolor=\"#666699\" width=\"100%\" height=\"65%\">"+
        "<td valign=\"top\"  align=\"center\">"+
          "<table class=\"TextInTable\" width=\"90%\" border=\"1\" bordercolor=\"#ffffff\" cellPadding=\"2\" cellSpacing=\"0\">"+
          "<tr><td colspan=\"3\" align=\"center\" bgColor=\"#ffffff\"><font size=\"3\">Данному пользователю для корректировки доступны следующие работы</font> </td> </tr>"+
          "<tr>"+
            "<td class=\"HeaderOfTable\">Идентификатор</td>"+
            "<td class=\"HeaderOfTable\"  width=\"79%\">Наименование работы</td>"+
            "<td class=\"HeaderOfTable\">Действие</td>"+
          "</tr>");
           try{   
              st = conn.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);                    
              String privilege=(String)session.getValue("privilege");
              String selectedDepartment=(String)session.getValue("department");              
              rs=st.executeQuery("select department_id from departments where department_name='"+selectedDepartment+"'");
              String selectedDepartmentId="";
              while(rs.next()) {  
                selectedDepartmentId=rs.getString("department_id");
              }
              st = conn.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);     
              if(privilege.equals("1")==true){
                rs=st.executeQuery("select * from works where department_id='"+selectedDepartmentId+"' and vladelets='"+userName+"'");
              }
              else {
                rs=st.executeQuery("select * from works where department_id='"+selectedDepartmentId+"'");
              }              
              while(rs.next()) {                        
                out.println("<tr><td>" + rs.getString("work_id") + "</td>");           
                out.println("<td>" + rs.getString("work_name") + "</td>");                
                out.println("<td>"+
                "<a href=\"javascript:window.location='../servlet/WebPagesGenerator?actionType=edit_work&workId="+ rs.getString("work_id") +"';\">Редактировать</a>"+
                "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;"+
                "<a href=\"javascript: if(window.confirm('Удалить выбранную работу?')==1) "+
                          "actionAndRefresh('../servlet/ModifyDatabase?actionType=delete_work&workId="+ rs.getString("work_id") +"');\">Удалить</a>"+
                "</td>"+
                "</tr>");
              }          
            }    
            catch( SQLException ex){
                out.println("<H1 ALIGN=CENTER>Внутренняя ошибка сервера: нет доступа к базе данных.</H1>\n\n"+ ex.toString());
            }
          out.println("</table>"+
        "</td>"+
      "</table>");
    } 
    
    else if(actionType.equals("edit_work")==true){
      boolean isEnabled=true;
      out.println("<body onload=\"window.open('../servlet/ModifyDatabase?actionType=delete_temptasks','_blank');\"><table class=\"HeaderOfTable\" border=\"0\" cellPadding=\"10\" cellSpacing=\"0\" >"+
      "<tr>"+
        "<td width=\"90%\" bgColor=\"#ffffff\"></td>"+
        "<td><nobr>Редактирование работы</nobr></td>"+
      "</tr>"+
      "</table>"+
      "<table bgColor=\"#ffffff\" border=\"7\" bordercolor=\"#666699\" width=\"100%\" height=\"65%\">"+
        "<td valign=\"top\">"+
          "<br>"+
          "<form method=\"post\" action=\"../servlet/ModifyDatabase\">"+
            "<table class=\"TextInTable\" width=\"95%\" border=\"1\" bordercolor=\"#ffffff\" cellPadding=\"2\" cellSpacing=\"0\" align=\"center\">");
              String workId=request.getParameter("workId");      
              try{
                st = conn.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);  
                rs=st.executeQuery("select * from works where work_id='"+ workId +"'");
                out.println("<input type=\"hidden\" id=\"_workId\" name=\"workId\" value=\"" + workId +"\">");
                while(rs.next()) {
                  st2 = conn.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE); 
                  String selectedDepartment=(String)session.getValue("department");
                  rs2 = st2.executeQuery("select enabled_department,privilege from users where user_name='"+userName+"'");        
                  while(rs2.next()) {                                        
                    if(rs2.getString("enabled_department").equals("все")==false){
                       if(rs2.getString("enabled_department").equals(selectedDepartment)==false){
                          out.println("<script>window.alert('Вам не разрешено редактировать работы в выбранном подразделении. Выберите разрешенное подразделение.');"+
                             "history.back(); </script>");                
                          isEnabled=false;
                          break;
                       }
                       if(rs2.getString("privilege").equals("1")==true){
                         if(rs.getString("vladelets").equals(userName)==false){
                           out.println("<script>window.alert('Вам не разрешено редактировать выбранную работу, т.к. Вы не являетесь ее владельцем.');"+
                             "history.back(); </script>");                
                           isEnabled=false;
                         }
                       }
                   }
                 }
                  if(isEnabled==true){
                    out.println("<tr><td class=\"HeaderOfTable\">Наименование работы </td> "+
                      "<td><input type=\"text\" name=\"workName\" size=\"100\" value=\""+ rs.getString("work_name") +"\"></td>"+
                    "</tr>"+
                    "<tr><td class=\"HeaderOfTable\">Координатор</td>"+
                      "<td><select name=\"coordinator\" size=\"1\">"+
                           "<option selected value=\""+rs.getString("coordinator")+"\">"+rs.getString("coordinator"));
                           try{   
                            st2 = conn.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);                    
                            rs2=st2.executeQuery("select * from workers");
                            while(rs2.next()) {
                              String fio=rs2.getString("fio");
                              out.println("<option value=\"" + fio + "\">" + fio);
                            }          
                          }    
                          catch( SQLException ex){
                            out.println("<H1 ALIGN=CENTER>Внутренняя ошибка сервера: нет доступа к базе данных.</H1>\n\n"+ ex.toString());
                          }                     
                      out.println("</select></td>"+
                    "</tr>"+
                    "<tr><td class=\"HeaderOfTable\">Статус</td>"+
                        "<td><select name=\"status\" size=\"1\">"+
                             "<option selected value=\""+rs.getString("status")+"\">"+rs.getString("status")+
                             "<option value=\"Открыто\">Открыто"+
                             "<option value=\"В работе, без отставания от плана\">В работе, без отставания от плана"+
                             "<option value=\"В работе, с запаздыванием\">В работе, с запаздыванием"+
                             "<option value=\"Приостановлено\">Приостановлено"+
                             "<option value=\"Решено\">Решено"+
                             "<option value=\"Закрыто\">Закрыто"+
                             "<option value=\"Отменено\">Отменено"+
                        "</select></td>"+
                    "</tr>");
                  }
                }
              } catch( SQLException ex){
                out.println("<H1 ALIGN=CENTER>Внутренняя ошибка сервера: нет доступа к базе данных.</H1>\n\n"+ ex.toString());
              }  
              if(isEnabled==true){
                 out.println("</table>"+
                 "<br>"+
                 "<center>"+
                    "<input type=\"button\" value=\"Добавить задачу\" onclick=\"addTask();\">&nbsp;&nbsp;&nbsp;&nbsp;"+
                    "<input type=\"submit\" value=\"Сохранить работу\" onclick=\"saveHTML();\">"+                   
                    "<input type=\"hidden\" name=\"htmlCodeForSave\" id=\"_htmlCodeForSave\">"+                  
                    "<input type=\"hidden\" name=\"actionType\" value=\"save_modified_work\">"+
                "</center>"+
              "</form>"+
              "<h3 align=\"center\">Задачи, входящие в состав работы</h1>"+
              "<table class=\"TextInTable\" id=\"taskTable\" width=\"95%\"  border=\"1\" bordercolor=\"#666699\" cellPadding=\"0\" cellSpacing=\"0\" align=\"center\">");              
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
               out.println(htmlCode);
             }
            out.println("</table>"+          
         "</td>"+
       "</table>");   
    }
    
     else if(actionType.equals("edit_task")==true){
       out.println("<body bgColor=\"#c3c3c3\">");
       String taskName=request.getParameter("taskName");  
       String tdId=request.getParameter("td_id");        
       if(taskName.indexOf(" (с ")==-1){ // добавление задачи в базу
         out.println("<form action=\"../servlet/ModifyDatabase\" method=\"post\" target=\"_blank\">"+
           "<input type=\"hidden\" name=\"actionType\" value=\"add_task\">"+
           "<input type=\"hidden\" name=\"executorsHidden\">"+
           "<input type=\"hidden\" name=\"td_id\" value=\""+tdId+"\">"+
           "<table border=\"1\">"+
           "<tr><td>Наименование задачи(или подзадачи)</td>"+
               "<td><input type=\"text\" id=\"_taskName\" name=\"taskName\" size=\"60\"></td>"+
           "</tr>"+
           "<tr><td>Содержание задачи(или подзадачи)</td>"+
              "<td><textarea name=\"taskContent\" rows=\"4\" cols=\"60\" wrap=\"soft\"></textarea></td>"+
           "</tr>"+
           "<tr><td>Место выполнения</td>"+
               "<td><select name=\"place\" size=\"1\">"+
                   "<option selected value=\"0\">-- Выберите место --</option>");
                    try{   
                      st = conn.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);                    
                      rs=st.executeQuery("select * from places");
                      while(rs.next()) {
                        String placeName=rs.getString("place_name");
                        out.println("<option value=\"" + placeName + "\">" + placeName);
                      }          
                    }    
                    catch( SQLException ex){
                      out.println("<H1 ALIGN=CENTER>Внутренняя ошибка сервера: нет доступа к базе данных.</H1>\n\n"+ ex.toString());
                    }
               out.println("</select></td>"+
           "</tr>"+
           "<tr><td>Исполнители</td>"+
             "<td>"+
               "<table cellspacing=\"7\" cellpadding=\"7\">"+
               "<tr>"+
                "<td>"+
                  "<select id=\"listbox\" size=\"5\">");
                     try{   
                      st = conn.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);                    
                      rs=st.executeQuery("select * from workers");
                      while(rs.next()) {
                        String fio=rs.getString("fio");
                        out.println("<option value=\"" + fio + "\">" + fio);
                      }          
                    }    
                    catch( SQLException ex){
                      out.println("<H1 ALIGN=CENTER>Внутренняя ошибка сервера: нет доступа к базе данных.</H1>\n\n"+ ex.toString());
                    }                    
                  out.println("</select>"+
                "</td>"+
             "<td>"+
               "<input type=\"button\" value=\"Назначить\" onclick=\"addExecutor();\" >"+
               "<br><br>"+
               "<input type=\"button\" value=\"Освободить\" onclick=\"deleteExecutor();\" >"+
            "</td>"+
           "</tr>"+
           "<tr><td colspan=\"2\">"+
             "<table border=\"1\" bordercolor=\"#000000\">"+
               "<tr><td>Список назначенных исполнителей</td></tr>"+
               "<tr><td id=\"selectedExecutors\" align=\"center\"></td></tr>"+
               "<tr><td id=\"isEmpty\" align=\"center\">-- Список пуст --</td></tr>"+               
             "</table>"+
             "</td></tr>"+
             "</table>"+
           "</td>"+
         "</tr>"+
         "<tr> <td>Планируемое время выполнения</td>"+
                 "<td>"+
                    "Дата начала (формат даты: ДД.ММ.ГГГГ)<br>"+
                    "<input type=\"text\" id=\"_beginDate\" name=\"beginDate\">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<A href=\"#\" onclick=\"javascript:showCalendar('beginDate');\"><img title=\"Выбрать дату\" src=\"../images/list.gif\" border=\"0\"></A>"+
                    "<br>"+
                    "Дата окончания (формат даты: ДД.ММ.ГГГГ)<br>"+
                    "<input type=\"text\" id=\"_endDate\" name=\"endDate\">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<A href=\"#\" onclick=\"javascript:showCalendar('endDate');\"><img title=\"Выбрать дату\" src=\"../images/list.gif\" border=\"0\"></A>"+
                 "</td>"+
            "</tr>"+
            "<tr><td>Выполнено на сегодняшний день (от 0 до 100%)</td>"+
                "<td><input type=\"text\" name=\"vypolneno\" size=\"1\"></td>"+
            "</tr>"+
            "<tr><td>Причины задержек в выполнении задачи или её невыполнения</td>"+
              "<td><textarea name=\"taskProblems\" rows=\"4\" cols=\"60\" wrap=\"soft\"></textarea></td>"+
           "</tr>"+
            "<tr><td colspan=\"2\" align=\"center\"> <input type=\"submit\" id=\"ok\" value=\"&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;OK&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;\" onclick=\"javascript:editTaskOK();\">"+            
            "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<input type=\"button\" id=\"cancel\" value=\"&nbsp;&nbsp;Отмена&nbsp;&nbsp;\" onclick=\"javascript:editTaskCancel();\">"+            
            "</td></tr>"+
         "</table>"+
         "</form>");
       }
       else{ 
         taskName = taskName.substring(0, taskName.indexOf(" (с "));
         boolean isTaskExistsInTempTable=false;
         try{
           st = conn.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE); 
           
           rs=st.executeQuery("select * from temp_tasks where td_id='"+ tdId +"'");
           while(rs.next()) {
             isTaskExistsInTempTable=true;
             String taskId=rs.getString("task_id");             
             String taskContent=rs.getString("task_content");
             if(taskContent==null){taskContent="";}
             String place=rs.getString("place");
             if(place.equals("0")==true){place="";}
             String executors=rs.getString("executors");
             if(executors==null){executors="";}
             executors=executors.replaceAll("<br><br>","<br>");
             String beginDate=rs.getString("begin_date");
             if(beginDate==null){beginDate="";} else
             beginDate = dfc.convertToDate(beginDate);
             String endDate=rs.getString("end_date"); 
             if(endDate==null){endDate="";} else
             endDate = dfc.convertToDate(endDate);
             String vypolneno=rs.getString("vypolneno");    
             if(vypolneno==null){vypolneno="";}
             String taskProblems=rs.getString("task_problems");    
             if(taskProblems==null){taskProblems="";}
             out.println("<form action=\"../servlet/ModifyDatabase\" method=\"post\" target=\"_blank\">"+
             "<input type=\"hidden\" name=\"actionType\" value=\"save_modified_temptask\">"+
             "<input type=\"hidden\" name=\"executorsHidden\">"+
             "<input type=\"hidden\" name=\"td_id\" value=\""+tdId+"\">"+
             "<input type=\"hidden\" name=\"taskId\" value=\""+taskId+"\">"+
             "<table border=\"1\">"+
             "<tr><td>Наименование задачи(или подзадачи)</td>"+
                 "<td><input type=\"text\" id=\"_taskName\" size=\"60\" name=\"taskName\" value=\""+taskName+"\"></td>"+
             "</tr>"+
             "<tr><td>Содержание задачи(или подзадачи)</td>"+
                "<td><textarea name=\"taskContent\" rows=\"4\" cols=\"60\" wrap=\"soft\">"+taskContent+"</textarea></td>"+
             "</tr>"+
             "<tr><td>Место выполнения</td>"+
                 "<td><select name=\"place\" size=\"1\">"+
                     "<option selected value=\""+place+"\">"+place+"</option>");
                      try{   
                      st = conn.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);                    
                      rs=st.executeQuery("select * from places");
                      while(rs.next()) {
                        String placeName=rs.getString("place_name");
                        out.println("<option value=\"" + placeName + "\">" + placeName);
                      }          
                    }    
                    catch( SQLException ex){
                      out.println("<H1 ALIGN=CENTER>Внутренняя ошибка сервера: нет доступа к базе данных.</H1>\n\n"+ ex.toString());
                    }
                 out.println("</select></td>"+
             "</tr>"+
             "<tr><td>Исполнители</td>"+
               "<td>"+
                 "<table cellspacing=\"7\" cellpadding=\"7\">"+
                 "<tr>"+
                  "<td>"+
                    "<select id=\"listbox\" size=\"5\">");
                    try{   
                      st = conn.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);                    
                      rs=st.executeQuery("select * from workers");
                      while(rs.next()) {
                        String fio=rs.getString("fio");
                        out.println("<option value=\"" + fio + "\">" + fio);
                      }          
                    }    
                    catch( SQLException ex){
                      out.println("<H1 ALIGN=CENTER>Внутренняя ошибка сервера: нет доступа к базе данных.</H1>\n\n"+ ex.toString());
                    }           
                    out.println("</select>"+
                  "</td>"+
               "<td>"+
                 "<input type=\"button\" value=\"Назначить\" onclick=\"addExecutor();\" >"+
                 "<br><br>"+
                 "<input type=\"button\" value=\"Освободить\" onclick=\"deleteExecutor();\" >"+
              "</td>"+
             "</tr>"+
             "<tr><td colspan=\"2\">"+
                 "<table border=\"1\" bordercolor=\"#000000\">"+
                   "<tr><td>Список назначенных исполнителей</td></tr>"+
                   "<tr><td id=\"selectedExecutors\" align=\"center\"></td></tr>"+
                   "<tr><td id=\"isEmpty\" align=\"center\">-- Список пуст --</td></tr>"+                   
                 "</table>"+
                 "<script>fillTableFromDatabase('"+executors+"');</script>"+
                 "</td></tr>"+
                 "</table>"+
               "</td>"+
             "</tr>"+
             "<tr> <td>Планируемое время выполнения</td>"+
                "<td>"+
                    "Дата начала (формат даты: ДД.ММ.ГГГГ)<br>"+
                    "<input type=\"text\" id=\"_beginDate\" name=\"beginDate\" value=\""+beginDate+"\">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<A href=\"#\" onclick=\"javascript:showCalendar('beginDate');\"><img title=\"Выбрать дату\" src=\"../images/list.gif\" border=\"0\"></A>"+
                    "<br>"+
                    "Дата окончания (формат даты: ДД.ММ.ГГГГ)<br>"+
                    "<input type=\"text\" id=\"_endDate\" name=\"endDate\" value=\""+endDate+"\">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<A href=\"#\" onclick=\"javascript:showCalendar('endDate');\"><img title=\"Выбрать дату\" src=\"../images/list.gif\" border=\"0\"></A>"+
                 "</td>"+
              "</tr>"+
              "<tr><td>Выполнено на сегодняшний день, (от 0 до 100%)</td>"+
                  "<td><input type=\"text\" name=\"vypolneno\" size=\"1\" value=\""+vypolneno+"\"></td>"+
              "</tr>"+
              "<tr><td>Причины задержек в выполнении задачи или её невыполнения</td>"+
              "<td><textarea name=\"taskProblems\" rows=\"4\" cols=\"60\" wrap=\"soft\">"+taskProblems+"</textarea></td>"+
              "</tr>"+
              "<tr><td colspan=\"2\" align=\"center\"> <input type=\"submit\" id=\"ok\" value=\"&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;OK&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;\" onclick=\"javascript:editTaskOK();\">"+
              "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<input type=\"button\" id=\"cancel\" value=\"&nbsp;&nbsp;Отмена&nbsp;&nbsp;\" onclick=\"javascript:editTaskCancel();\">"+
              "</td></tr>"+
             "</table>"+
             "</form>");
           }
            
           if(isTaskExistsInTempTable==false)  {
             String workId=request.getParameter("workId");  
             rs=st.executeQuery("select * from tasks where td_id='"+ tdId +"' and work_id="+workId);
             while(rs.next()) {
               String taskId=rs.getString("task_id");
               String taskContent=rs.getString("task_content");
               String place=rs.getString("place");
               String executors=rs.getString("executors");
               executors=executors.replaceAll("<br><br>","<br>");
               String beginDate=rs.getString("begin_date");
               beginDate = dfc.convertToDate(beginDate);
               String endDate=rs.getString("end_date"); 
               endDate = dfc.convertToDate(endDate);
               String vypolneno=rs.getString("vypolneno");    
               String taskProblems=rs.getString("task_problems");
               out.println("<form action=\"../servlet/ModifyDatabase\" method=\"post\" target=\"_blank\">"+
               "<input type=\"hidden\" name=\"actionType\" value=\"save_modified_task\">"+
               "<input type=\"hidden\" name=\"executorsHidden\">"+
               "<input type=\"hidden\" name=\"td_id\" value=\""+tdId+"\">"+
               "<input type=\"hidden\" name=\"taskId\" value=\""+taskId+"\">"+
               "<input type=\"hidden\" name=\"workId\" value=\""+workId+"\">"+
               "<table border=\"1\">"+
               "<tr><td>Наименование задачи(или подзадачи)</td>"+
                   "<td><input type=\"text\" id=\"_taskName\" size=\"60\" name=\"taskName\" value=\""+taskName+"\"></td>"+
               "</tr>"+
               "<tr><td>Содержание задачи(или подзадачи)</td>"+
                  "<td><textarea name=\"taskContent\" rows=\"4\" cols=\"60\" wrap=\"soft\">"+taskContent+"</textarea></td>"+
               "</tr>"+
               "<tr><td>Место выполнения</td>"+
                   "<td><select name=\"place\" size=\"1\">"+
                       "<option selected value=\""+place+"\">"+place+"</option>");
                        try{   
                         st = conn.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);                    
                         rs=st.executeQuery("select * from places");
                         while(rs.next()) {
                           String placeName=rs.getString("place_name");
                           out.println("<option value=\"" + placeName + "\">" + placeName);
                      }          
                    }    
                    catch( SQLException ex){
                      out.println("<H1 ALIGN=CENTER>Внутренняя ошибка сервера: нет доступа к базе данных.</H1>\n\n"+ ex.toString());
                    }
                   out.println("</select></td>"+
               "</tr>"+
               "<tr><td>Исполнители</td>"+
                 "<td>"+
                   "<table cellspacing=\"7\" cellpadding=\"7\">"+
                   "<tr>"+
                    "<td>"+
                      "<select id=\"listbox\" size=\"5\">");
                      try{   
                        st = conn.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);                    
                        rs=st.executeQuery("select * from workers");
                        while(rs.next()) {
                          String fio=rs.getString("fio");
                          out.println("<option value=\"" + fio + "\">" + fio);
                       }          
                    }    
                    catch( SQLException ex){
                      out.println("<H1 ALIGN=CENTER>Внутренняя ошибка сервера: нет доступа к базе данных.</H1>\n\n"+ ex.toString());
                    }          
                      out.println("</select>"+
                    "</td>"+
                 "<td>"+
                   "<input type=\"button\" value=\"Назначить\" onclick=\"addExecutor();\" >"+
                   "<br><br>"+
                   "<input type=\"button\" value=\"Освободить\" onclick=\"deleteExecutor();\" >"+
                "</td>"+
               "</tr>"+
               "<tr><td colspan=\"2\">"+
                   "<table border=\"1\" bordercolor=\"#000000\">"+
                     "<tr><td>Список назначенных исполнителей</td></tr>"+
                     "<tr><td id=\"selectedExecutors\" align=\"center\"></td></tr>"+
                     "<tr><td id=\"isEmpty\" align=\"center\">-- Список пуст --</td></tr>"+
                   "</table>"+
                   "<script>fillTableFromDatabase('"+executors+"');</script>"+
                   "</td></tr>"+
                   "</table>"+
                 "</td>"+
               "</tr>"+
               "<tr> <td>Планируемое время выполнения</td>"+
                  "<td>"+
                      "Дата начала (формат даты: ДД.ММ.ГГГГ)<br>"+
                      "<input type=\"text\" id=\"_beginDate\" name=\"beginDate\" value=\""+beginDate+"\">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<A href=\"#\" onclick=\"javascript:showCalendar('beginDate');\"><img title=\"Выбрать дату\" src=\"../images/list.gif\" border=\"0\"></A>"+
                      "<br>"+
                      "Дата окончания (формат даты: ДД.ММ.ГГГГ)<br>"+
                      "<input type=\"text\" id=\"_endDate\" name=\"endDate\" value=\""+endDate+"\">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<A href=\"#\" onclick=\"javascript:showCalendar('endDate');\"><img title=\"Выбрать дату\" src=\"../images/list.gif\" border=\"0\"></A>"+
                   "</td>"+
                "</tr>"+
                "<tr><td>Выполнено на сегодняшний день, (от 0 до 100%)</td>"+
                    "<td><input type=\"text\" name=\"vypolneno\" size=\"1\" value=\""+vypolneno+"\"></td>"+
                "</tr>"+
                "<tr><td>Причины задержек в выполнении задачи или её невыполнения</td>"+
                  "<td><textarea name=\"taskProblems\" rows=\"4\" cols=\"60\" wrap=\"soft\">"+taskProblems+"</textarea></td>"+
                "</tr>"+
                "<tr><td colspan=\"2\" align=\"center\"> <input type=\"submit\" id=\"ok\" value=\"&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;OK&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;\" onclick=\"javascript:editTaskOK();\">"+                
                "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<input type=\"button\" id=\"cancel\" value=\"&nbsp;&nbsp;Отмена&nbsp;&nbsp;\" onclick=\"javascript:editTaskCancel();\">"+
                "</td></tr>"+
               "</table>"+
               "</form>");
             }
           }
         } catch( SQLException ex){
           out.println("<H1 ALIGN=CENTER>Внутренняя ошибка сервера: нет доступа к базе данных.</H1>\n\n"+ ex.toString());
         }   
       }
     }
     
     else if(actionType.equals("reports_form")==true){
     out.println("<body>"+
       "<table class=\"HeaderOfTable\" border=\"0\" cellPadding=\"10\" cellSpacing=\"0\" >"+
         "<tr>"+
           "<td width=\"95%\" bgColor=\"#ffffff\"></td>"+
           "<td><nobr>Поиск работ</nobr></td>"+
         "</tr>"+
       "</table>"+
       "<table bgColor=\"#ffffff\" border=\"7\" bordercolor=\"#666699\" width=\"100%\" height=\"65%\">"+
         "<td>"+
           "<form action=\"../servlet/WebPagesGenerator\" method=\"post\">"+
             "<input type=\"hidden\" name=\"actionType\" value=\"reports_table\">"+
             "<table class=\"TextInTable\" width=\"40%\"  border=\"1\" bordercolor=\"#ffffff\" cellPadding=\"2\" cellSpacing=\"0\" align=\"center\">"+
               "<tr bgColor=\"#ffffff\">"+
                 "<td align=\"right\"><input type=\"button\" value=\"Найти работы\" onclick=\"window.location='../servlet/WebPagesGenerator?actionType=reports_table';\"></td>"+
                 "<td><input type=\"reset\" value=\"Очистить форму\"></td>"+
               "</tr>"+
               "<tr>"+
                 "<td bgColor=\"#ffffff\" colspan=\"2\" align=\"center\"><br><font size=\"3\">Выберите параметры, по которым будет произведен поиск</font></td>"+
               "</tr>"+
               "<tr>"+
                 "<td class=\"HeaderOfTable\">Идентификатор работы</td>"+
                 "<td><select  name=\"workId\" size=\"1\">"+
                      "<option  selected value=\"-- Не имеет значения --\">-- Не имеет значения --");
                      String selectedDepartment=(String)session.getValue("department");              
                      try{   
                        st = conn.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);                    
                        rs=st.executeQuery("select work_id from works where department_id=(select department_id from departments where department_name='"+selectedDepartment+"')");
                        while(rs.next()) {                                                  
                          out.println("<option  value=\""+rs.getString("work_id")+"\">"+rs.getString("work_id"));
                        }          
                      }    
                      catch( SQLException ex){
                        out.println("<H1>Внутренняя ошибка сервера: нет доступа к базе данных.</H1>\n\n"+ ex.toString());
                      }                 
                 out.println("</select></td>"+
               "</tr>"+
               "<tr>"+
                 "<td class=\"HeaderOfTable\">Наименование работы</td>"+
                 "<td><select  name=\"workName\" size=\"1\">"+
                      "<option  selected value=\"-- Не имеет значения --\">-- Не имеет значения --");
                      try{   
                        st = conn.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);                    
                        rs=st.executeQuery("select distinct work_name from works  where department_id=(select department_id from departments where department_name='"+selectedDepartment+"')");
                        while(rs.next()) {                                                  
                          out.println("<option  value=\""+rs.getString("work_name")+"\">"+rs.getString("work_name"));
                        }          
                      }    
                      catch( SQLException ex){
                        out.println("<H1>Внутренняя ошибка сервера: нет доступа к базе данных.</H1>\n\n"+ ex.toString());
                      }                 
                 out.println("</select></td>"+
              "</tr>"+
              "<tr><td class=\"HeaderOfTable\">Координатор</td>"+
                  "<td><select  name=\"coordinator\" size=\"1\">"+
                      "<option  selected value=\"-- Не имеет значения --\">-- Не имеет значения --");
                       try{   
                        st = conn.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);                    
                        rs=st.executeQuery("select distinct coordinator from works  where department_id=(select department_id from departments where department_name='"+selectedDepartment+"')"); 
                        while(rs.next()) {                                                  
                          out.println("<option  value=\""+rs.getString("coordinator")+"\">"+rs.getString("coordinator"));
                        }          
                      }    
                      catch( SQLException ex){
                        out.println("<H1>Внутренняя ошибка сервера: нет доступа к базе данных.</H1>\n\n"+ ex.toString());
                      }    
                  out.println("</select></td>"+
             "</tr>"+
             "<tr><td class=\"HeaderOfTable\">Статус</td>"+
                 "<td ><select name=\"status\" size=\"1\">"+
                      "<option  selected value=\"-- Не имеет значения --\">-- Не имеет значения --"+
                      "<option value=\"Открыто\">Открыто"+
                      "<option value=\"В работе, без отставания от плана\">В работе, без отставания от плана"+
                      "<option value=\"В работе, с запаздыванием\">В работе, с запаздыванием"+
                      "<option value=\"Приостановлено\">Приостановлено"+
                      "<option value=\"Решено\">Решено"+
                      "<option value=\"Закрыто\">Закрыто"+
                      "<option value=\"Отменено\">Отменено"+
                 "</select></td>"+
             "</tr>"+
             "<tr><td class=\"HeaderOfTable\">Место выполнения</td>"+
                  "<td><select name=\"place\" size=\"1\">"+
                      "<option  selected value=\"-- Не имеет значения --\">-- Не имеет значения --");
                      try{   
                        st = conn.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);                    
                        rs=st.executeQuery("select distinct place from tasks");  
                        while(rs.next()) {                                                  
                          out.println("<option  value=\""+rs.getString("place")+"\">"+rs.getString("place"));
                        }          
                      }    
                      catch( SQLException ex){
                        out.println("<H1>Внутренняя ошибка сервера: нет доступа к базе данных.</H1>\n\n"+ ex.toString());
                      }                      
                  out.println("</select></td>"+          
            "</tr>"+
            "<tr><td class=\"HeaderOfTable\">Исполнитель</td>"+
                "<td><select  name=\"executor\" size=\"1\">"+
                    "<option  selected value=\"-- Не имеет значения --\">-- Не имеет значения --");  
                    try{   
                        st = conn.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);                    
                        rs=st.executeQuery("select fio from workers  where department_id=(select department_id from departments where department_name='"+selectedDepartment+"')");
                        while(rs.next()) {                                                  
                          out.println("<option  value=\""+rs.getString("fio")+"\">"+rs.getString("fio"));
                        }          
                      }    
                      catch( SQLException ex){
                        out.println("<H1>Внутренняя ошибка сервера: нет доступа к базе данных.</H1>\n\n"+ ex.toString());
                      }    
                out.println("</select></td>"+
           "</tr>"+
          "</table>"+
         "</form>"+
        "</td>"+
       "</table>");
     }
     
     else if(actionType.equals("reports_diagrams")==true){
       String workId=request.getParameter("workId");
       String buffer = new String();
       int numberOfTasks=0,numberOfTasksMinus19=0;
       out.println("<body>"+
         "<table class=\"TextInTable\" style=\"background:#ffffff;\" border=\"1\" bordercolor=\"#ffffff\" cellPadding=\"2\" cellSpacing=\"0\">"+
           "<tr>"+
             "<td class=\"HeaderOfTable\" rowspan=\"2\" width=\"250\" align=\"center\">Задачи</td>");
             try{   
               st = conn.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);                    
               rs=st.executeQuery("select max(end_date)-min(begin_date) from tasks where work_id='"+workId+"'");  
               while(rs.next()) {                                                                
                 int numberOfDays=Integer.parseInt( rs.getString("max(end_date)-min(begin_date)") );
                 for(int i=0; i < numberOfDays+7; i=i+7){
                    rs=st.executeQuery("select round((select min(begin_date) from tasks where work_id='"+workId+"'),'d')+"+ i +" from dual");                     
                    while(rs.next()) {                        
                       String firstDayOfWeek=rs.getString(1);
                      firstDayOfWeek=dfc.convertToDate(firstDayOfWeek);
                      buffer=buffer.concat("<td class=\"HeaderOfTable\" colspan=\"7\">"+firstDayOfWeek+"</td>");                      
                    }
                 }
                  buffer=buffer.concat("</tr>"+
                  "<tr>");
                 for(int i=0; i < numberOfDays+7; i=i+7){                                    
                   buffer=buffer.concat("<td class=\"HeaderOfTable\">Пн</td>"+
                   "<td class=\"HeaderOfTable\">Вт&nbsp;</td>"+
                   "<td class=\"HeaderOfTable\">Ср</td>"+
                   "<td class=\"HeaderOfTable\">Чт&nbsp;</td>"+
                   "<td class=\"HeaderOfTable\">Пт&nbsp;</td>"+
                   "<td style=\"background:#cd5c5c;\" class=\"HeaderOfTable\">Сб</td>"+
                   "<td style=\"background:#cd5c5c;\" class=\"HeaderOfTable\">Вс</td>");
                 }
               }                
             }    
             catch( SQLException ex){
               out.println("<H1>Внутренняя ошибка сервера: нет доступа к базе данных.</H1>\n\n"+ rsc.convertString(ex.toString()));
             }   
             buffer = buffer + "</tr>";
             out.println(buffer);
            try{               
               st = conn.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);                    
               rs=st.executeQuery("select task_name, vypolneno, end_date-begin_date+1, begin_date-(select round((select min(begin_date) from tasks where work_id='"+workId+"'),'d') from dual), sysdate-begin_date, end_date-begin_date-1, task_content, place, executors,td_id,task_problems from tasks where work_id='"+workId+"' order by td_id");              
               while(rs.next()) {   
                 if((numberOfTasks - numberOfTasksMinus19)==19){
                    numberOfTasksMinus19 = numberOfTasks;
                    out.println("<tr><td border=\"1\" bordercolor=\"#000000\" bgcolor=\"#eeeedd\" rowspan=\"2\" width=\"250\">&nbsp;</td>"+buffer);
                 }
                 String spaces = "";
                 int tdIdLength=rs.getString("td_id").length();
                 String newTdId=rs.getString("td_id").replaceAll("_","");
                 int newTdIdLength=newTdId.length();                
                 for (int i=0; i < tdIdLength -  newTdIdLength; i++)
                     spaces = spaces.concat("&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;");
                 if((tdIdLength -  newTdIdLength)==0)
                     spaces = spaces.concat("<img src=\"../images/bullet_task.gif\">&nbsp;");
                 else spaces = spaces.concat("<img src=\"../images/bullet_subtask.gif\">&nbsp;");
                 out.println("<tr><td border=\"1\" bordercolor=\"#000000\" bgcolor=\"#eeeedd\"><nobr>"+spaces+"<a href=\"#\" onclick=\"javascript:showTask('"+
                      rs.getString("task_content")+"','"+rs.getString("place")+"','"+rs.getString("executors")+"','"+rs.getString("task_problems")+"');\">"+
                      rs.getString("task_name")+"</a></nobr></td>");                                        
                   if(rs.getString(4).equals("0")==false){
                     out.println("<td colspan=\""+rs.getString(4)+"\"></td>");
                   }
                   String format=new String();
                   if(rs.getString("vypolneno").length()==1)
                     format="0.0"+rs.getString("vypolneno");
                   if(rs.getString("vypolneno").length()==2)                   
                     format="0."+rs.getString("vypolneno");
                   if(rs.getString("vypolneno").length()==3)
                     format="1";
                   double vypolnenoPercents=Double.parseDouble(format);
                   double taskPlannedDays=Double.parseDouble( rs.getString("end_date-begin_date+1") );
                   double vypolnenoDays = vypolnenoPercents * taskPlannedDays;
                   int intVypolnenoDays=(int)vypolnenoDays;
                   if((vypolnenoDays - intVypolnenoDays)>=0.5) intVypolnenoDays++;
                   double restDays = taskPlannedDays - vypolnenoDays -1;
                   int intRestDays=(int)restDays;
                   int intTaskPlannedDays=(int)taskPlannedDays;
                   if((restDays - intRestDays)>0.5) intRestDays++;
                   double daysFromBeginDateToCurrentDate=Double.parseDouble( rs.getString("sysdate-begin_date") );
                   String taskColor=new String();
                   if(daysFromBeginDateToCurrentDate > vypolnenoDays  && rs.getString("vypolneno").equals("100")==false)
                      taskColor="red";                  
                   else taskColor="blue";                 
                   if(rs.getString("end_date-begin_date+1").equals("1")){
                     if(intVypolnenoDays==0)
                        out.println("<td style=\"COLOR: #000000;  FONT-SIZE: 9pt; FONT-WEIGHT: bold;\" align=\"center\" background=\"../images/empty_one_day_"+taskColor+".bmp\">"+rs.getString("vypolneno")+"</td>");
                     else
                        out.println("<td style=\"COLOR: #ffffff;  FONT-SIZE: 9pt; FONT-WEIGHT: bold;\" align=\"left\" background=\"../images/filled_"+taskColor+".bmp\">"+rs.getString("vypolneno")+"</td>");
                   }
                   else if(rs.getString("end_date-begin_date+1").equals("2")){
                     if(intVypolnenoDays==0){
                        out.println("<td style=\"COLOR: #000000;  FONT-SIZE: 9pt; FONT-WEIGHT: bold;\" align=\"right\" background=\"../images/begin_"+taskColor+".bmp\">"+rs.getString("vypolneno")+"</td>");
                        out.println("<td style=\"COLOR: #000000;  FONT-SIZE: 9pt; FONT-WEIGHT: bold;\" align=\"left\" background=\"../images/end_"+taskColor+".bmp\">%</td>");
                     }
                     else{
                        if(vypolnenoPercents >= 0.7)
                           out.println("<td style=\"COLOR: #ffffff;  FONT-SIZE: 9pt; FONT-WEIGHT: bold;\" align=\"right\" background=\"../images/filled_"+taskColor+".bmp\" colspan=\"2\">"+rs.getString("vypolneno")+"%</td>");
                        else{
                           out.println("<td style=\"COLOR: #ffffff;  FONT-SIZE: 9pt; FONT-WEIGHT: bold;\" align=\"right\" background=\"../images/filled_"+taskColor+".bmp\">"+rs.getString("vypolneno")+"</td>");
                           out.println("<td style=\"COLOR: #ffffff;  FONT-SIZE: 9pt; FONT-WEIGHT: bold;\" align=\"right\" background=\"../images/end_"+taskColor+".bmp\"></td>");
                        }
                     }
                   }
                   else if(rs.getString("vypolneno").equals("100")==true){                     
                     out.println("<td style=\"COLOR: #ffffff;  FONT-SIZE: 9pt; FONT-WEIGHT: bold;\" align=\"right\" colspan=\""+
                     rs.getString("end_date-begin_date+1")+"\" background=\"../images/filled_blue.bmp\">100&nbsp%</td>");
                   }
                   else if(intVypolnenoDays==0){                  
                     out.println("<td style=\"COLOR: #000000;  FONT-SIZE: 9pt; FONT-WEIGHT: bold;\" align=\"right\" background=\"../images/begin_"+taskColor+".bmp\">"+rs.getString("vypolneno")+"</td>"+
                     "<td style=\"COLOR: #000000;  FONT-SIZE: 9pt; FONT-WEIGHT: bold;\" align=\"left\" colspan=\""+
                     rs.getString("end_date-begin_date-1")+"\" background=\"../images/empty_"+taskColor+".bmp\">%</td>"+
                     "<td background=\"../images/end_"+taskColor+".bmp\"></td>");
                   }                  
                   else{
                     out.println("<td style=\"COLOR: #ffffff;  FONT-SIZE: 9pt; FONT-WEIGHT: bold;\" align=\"right\" colspan=\""+
                     intVypolnenoDays +"\" background=\"../images/filled_"+taskColor+".bmp\">"+rs.getString("vypolneno")+"%</td>");
                     if(intRestDays!=0)                     
                        out.println("<td colspan=\""+intRestDays+"\" background=\"../images/empty_"+taskColor+".bmp\"></td>");
                     if(intVypolnenoDays != intTaskPlannedDays)
                       out.println("<td background=\"../images/end_"+taskColor+".bmp\"></td>");               
                   }
                 out.println("</tr>");       
                 numberOfTasks++;
               }                
             }    
             catch( SQLException ex){
               out.println("<H1>Внутренняя ошибка сервера: нет доступа к базе данных.</H1>\n\n"+ rsc.convertString(ex.toString()));
             }   
         out.println("</table>");
     }
     
     else if(actionType.equals("menu")==true){
       out.println("<body leftMargin=\"0\" rightMargin=\"0\" topMargin=\"0\" vLink=\"#ffffff\" marginheight=\"0\" marginwidth=\"0\">");               
       out.println("<table bgColor=\"#666699\">"+
       "<tr>"+
         "<td><img src=\"../images/om.bmp\" height=\"90\"  width=\"200\"></td>"+
         "<td>"+
            "<font color=\"#ccccff\" face=\"arial,helvetica\"><b>Система АСУ ЦСВТ</b></font><br>"+
            "<font color=\"#ffffff\" face=\"arial,helvetica\" size=\"4\"><b>Модуль планировщик работ подразделений ИВЦ ОЖД</b></font> "+
         "</td>"+
           "<td width=\"10%\">"+
             "<a style=\"COLOR: #ffffff; \" href=\"javascript:top.location='http://10.39.6.102:7778/'\"><nobr>Главная страница АСУ ЦСВТ</nobr></a><br><br>"+
             "<a style=\"COLOR: #ffffff; \" href=\"javascript:top.location='..'\"><nobr>Смена пользователя</nobr></a>"+
           "</td>"+
       "</tr>"+
       "<tr>"+
         "<td width=\"20%\"></td>"+
         "<td>"+
           "<form action=\"../servlet/WebPagesGenerator?actionType=selecting_menu_item\" method=\"post\" target=\"work_frame\">"+
             "<p> <select id=\"_department\" name=\"department\" size=\"1\">"+
             "<option selected value=\"0\">-- Выберите подразделение ИВЦ --");
     
        try{   
           st = conn.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);                    
           rs=st.executeQuery("select * from departments");
           while(rs.next()) {
              String departmentName=rs.getString("department_name");
              out.println("<option value=\"" + departmentName + "\">" + departmentName + "</option>");
           }          
       }    
       catch( SQLException ex){
         out.println("<H1 ALIGN=CENTER>Внутренняя ошибка сервера: нет доступа к базе данных.</H1>\n\n"+ ex.toString());
       }
       out.println("</select></p><br>"+
           "</td>"+
           "</tr>"+
           "</table>"+
           "<input type=\"hidden\" name=\"buttonName\">"+
           "<table bgColor=\"#C0C0C0\" cellSpacing=0 cellPadding=0>"+
           "<tr>"+
             "<td><input type=\"submit\" value=\"&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;Поиск работ&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;\" onclick=\"document.all('buttonName').setAttribute('value','reports',0);\"></td>");  
             if(userName.equals("anonymous")==false){
                out.println("<td><input type=\"submit\" value=\"Ввод новой работы\" onclick=\"document.all('buttonName').setAttribute('value','add_work',0);\"></td>"+
                "<td><input type=\"submit\" value=\"Корректировка работ\" onclick=\"document.all('buttonName').setAttribute('value','correct_work',0);\"></td>");   
                if(userName.equals("administrator")==true){
                   out.println("<td><input type=\"submit\" value=\"Корректировка НСИ\" onclick=\"document.all('buttonName').setAttribute('value','nsi',0);\"></td>"+
                   "<td><input type=\"submit\" value=\"Администрирование\" onclick=\"document.all('buttonName').setAttribute('value','admin',0);\"></td>");
                }
             }
             out.println("<td width=\"95%\"></td>"+
          "</tr>"+
          "</table>"+
        "</form>");
     }
     
     else if(actionType.equals("add_user")==true){
     out.println("<body>"+
     "<table class=\"HeaderOfTable\"  border=\"0\" cellPadding=\"10\" cellSpacing=\"0\">"+
       "<tr>"+
         "<td bgColor=\"#ffffff\"  width=\"90%\"></td>"+
         "<td><nobr>Администрирование</nobr></td>"+
      "</tr>"+
     "</table>"+     
     "<table   bgColor=\"#ffffff\" border=\"7\" bordercolor=\"#666699\" width=\"100%\" height=\"65%\">"+
     "<td  valign=\"top\"  align=\"center\">"+
      "<form action=\"../servlet/ModifyDatabase?actionType=add_user\" method=\"post\">"+
       "<table class=\"TextInTable\" width=\"90%\" border=\"1\" bordercolor=\"#ffffff\" cellPadding=\"2\" cellSpacing=\"0\">"+
         "<tr><td colspan=\"2\" bgColor=\"#ffffff\">&nbsp;</td></tr>"+
         "<tr>"+
            "<td class=\"HeaderOfTable\">Имя пользователя</td>"+
            "<td><input type=\"text\" name=\"newUserName\"></td>"+
         "</tr>"+
         "<tr>"+
            "<td class=\"HeaderOfTable\">Пароль</td>"+
            "<td><input type=\"password\" name=\"newUserPassword\"></td>"+
         "</tr>"+
         "<tr>"+
            "<td class=\"HeaderOfTable\">Подразделение</td>"+
            "<td><select name=\"enabledDepartment\" size=\"1\">"+
              "<option selected value=\"0\">-- Выберите подразделение ИВЦ --");     
              try{   
                st = conn.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);                    
                rs=st.executeQuery("select * from departments");
                while(rs.next()) {
                  String departmentName=rs.getString("department_name");
                  out.println("<option value=\"" + departmentName + "\">" + departmentName);
                }          
             }    
             catch( SQLException ex){
               out.println("<H1 ALIGN=CENTER>Внутренняя ошибка сервера: нет доступа к базе данных.</H1>\n\n"+ ex.toString());
             }
           out.println("</select></td>"+
         "</tr>"+
         "<tr>"+
            "<td class=\"HeaderOfTable\">Привилегия</td>"+
            "<td><select name=\"privilege\" size=\"1\">"+
              "<option value=\"1\">Доступ только к своим работам своего подразделения (для руководителей рабочих групп)"+
              "<option value=\"2\">Доступ ко всем работам своего подразделения (для начальников подразделений)"+
              "<option value=\"3\">Доступ ко всем работам всех подразделений (для высшего руководства ИВЦ)"+
            "</select></td>"+
         "</tr>"+
       "</table>"+
       "<input type=\"submit\" value=\"Добавить пользователя\">"+
       "</form>"+
       "</td>"+
      "</table>");
     }
     
     else if(actionType.equals("correct_users")==true){
       out.println("<body>"+
         "<table class=\"HeaderOfTable\"  border=\"0\" cellPadding=\"10\" cellSpacing=\"0\" >"+
           "<tr>"+
             "<td bgColor=\"#ffffff\"  width=\"90%\"></td>"+
             "<td><nobr>Администрирование</nobr></td>"+
           "</tr>"+
        "</table>"+
        "<table   bgColor=\"#ffffff\" border=7 bordercolor=\"#666699\" width=\"100%\" height=\"65%\">"+
        "<td  valign=\"top\"  align=\"center\">"+
          "<table  class=\"TextInTable\" width=\"70%\"    border=\"1\" bordercolor=\"#ffffff\" cellPadding=\"2\" cellSpacing=\"0\">"+
             "<tr><td colspan=\"2\" align=\"center\" bgColor=\"#ffffff\"><font size=\"3\">Список имеющихся в системе пользователей</font></td> </tr>"+
             "<tr>"+
               "<td class=\"HeaderOfTable\"  width=\"69%\">Имя пользователя</td> "+
               "<td class=\"HeaderOfTable\">Действие</td>"+
             "</tr>");
              try{   
                st = conn.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);                    
                rs=st.executeQuery("select * from users");
                while(rs.next()) {
                  String user=rs.getString("user_name");
                  out.println("<tr><td >"+user+"</td>"+
                    "<td ><a href=\"javascript:window.location='../servlet/WebPagesGenerator?actionType=edit_user&userId="+ rs.getString("user_id")+"';\">Редактировать</A>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;"+
                         "<a href=\"javascript: if(window.confirm('Удалить пользователя?')==1) "+
                          "actionAndRefresh('../servlet/ModifyDatabase?actionType=delete_user&userId="+ rs.getString("user_id") +"');\">Удалить</a>"+
                    "</td>"+
                  "</tr>");
                }          
             }    
             catch( SQLException ex){
               out.println("<H1 ALIGN=CENTER>Внутренняя ошибка сервера: нет доступа к базе данных.</H1>\n\n"+ ex.toString());
             }
          out.println("</table>"+
        "</td>"+
      "</table>");
     }
     
     else if(actionType.equals("edit_user")==true){
     String userId=request.getParameter("userId");
     String enabledDepartment="";
     out.println("<body>"+
     "<table class=\"HeaderOfTable\"  border=\"0\" cellPadding=\"10\" cellSpacing=\"0\">"+
       "<tr>"+
         "<td bgColor=\"#ffffff\"  width=\"90%\"></td>"+
         "<td><nobr>Администрирование</nobr></td>"+
      "</tr>"+
     "</table>"+     
     "<table   bgColor=\"#ffffff\" border=\"7\" bordercolor=\"#666699\" width=\"100%\" height=\"65%\">"+
     "<td  valign=\"top\"  align=\"center\">"+
      "<form action=\"../servlet/ModifyDatabase?actionType=save_modified_user\" method=\"post\">"+
       "<table class=\"TextInTable\" width=\"90%\" border=\"1\" bordercolor=\"#ffffff\" cellPadding=\"2\" cellSpacing=\"0\">"+
         "<tr><td colspan=\"2\" bgColor=\"#ffffff\">&nbsp;</td></tr>"+
         "<input type=\"hidden\" name=\"userId\" value=\""+userId+"\"");
          try{  
            st = conn.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);  
            rs=st.executeQuery("select * from users where user_id='"+userId+"'");
            while(rs.next()) {            
              out.println("<tr>"+
                "<td class=\"HeaderOfTable\">Имя пользователя</td>"+
                "<td><input type=\"text\" name=\"newUserName\" value=\""+rs.getString("user_name")+"\"></td>"+
              "</tr>"+                        
              "<tr>"+
                "<td class=\"HeaderOfTable\">Пароль</td>"+
                "<td><input type=\"password\" name=\"newUserPassword\" value=\""+rs.getString("password")+"\"></td>"+
              "</tr>"+
              "<tr>"+
                "<td class=\"HeaderOfTable\">Привилегия</td>"+
                "<td><select name=\"privilege\" size=\"1\">");
                  if(rs.getString("privilege").equals("1")==true){
                    out.println("<option selected value=\"1\">Доступ только к своим работам своего подразделения (для руководителей рабочих групп)"+
                    "<option value=\"2\">Доступ ко всем работам своего подразделения (для начальников подразделений)"+
                    "<option value=\"3\">Доступ ко всем работам всех подразделений (для высшего руководства ИВЦ)");
                  }
                  else if(rs.getString("privilege").equals("2")==true){
                    out.println("<option value=\"1\">Доступ только к своим работам своего подразделения (для руководителей рабочих групп)"+
                    "<option selected value=\"2\">Доступ ко всем работам своего подразделения (для начальников подразделений)"+
                    "<option value=\"3\">Доступ ко всем работам всех подразделений (для высшего руководства ИВЦ)");
                  }
                  else if(rs.getString("privilege").equals("3")==true){
                    out.println("<option value=\"1\">Доступ только к своим работам своего подразделения (для руководителей рабочих групп)"+
                    "<option value=\"2\">Доступ ко всем работам своего подразделения (для начальников подразделений)"+
                    "<option selected value=\"3\">Доступ ко всем работам всех подразделений (для высшего руководства ИВЦ)");
                  }
                out.println("</select></td>"+
             "</tr>");
             enabledDepartment=rs.getString("enabled_department");
            }   
            out.println("<tr>"+
             "<td class=\"HeaderOfTable\">Подразделение</td>"+
             "<td><select name=\"enabledDepartment\" size=\"1\">"+
                  "<option selected value=\"0\">"+enabledDepartment);
            st = conn.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE); 
            rs=st.executeQuery("select * from departments");
            while(rs.next()) {             
              out.println("<option value=\"" + rs.getString("department_name") + "\">" + rs.getString("department_name"));
            }                                         
            out.println("</select></td>"+
            "</tr>");              
         }    
         catch( SQLException ex){
           out.println("<H1 ALIGN=CENTER>Внутренняя ошибка сервера: нет доступа к базе данных.</H1>\n\n"+ ex.toString());
         }
       out.println("</table>"+
       "<input type=\"submit\" value=\"Сохранить изменения\">"+
       "</form>"+
       "</td>"+
      "</table>");
     }
      
      else if(actionType.equals("put_department_in_session")==true){
        String department=request.getParameter("department"); 
        session.putValue("department",department);     
        out.println("<body><script>window.close();</script>");
      }
      
      else if(actionType.equals("correct_workers")==true){
        out.println("<body><table class=\"HeaderOfTable\"  border=\"0\" cellPadding=\"10\" cellSpacing=\"0\" >"+
        "<tr>"+
           "<td bgColor=\"#ffffff\" width=\"90%\"></td>"+
           "<td><nobr>Корректировка НСИ</nobr></td>"+
        "</tr>"+
        "</table>"+
        "<table bgColor=\"#ffffff\" border=\"7\" bordercolor=\"#666699\" width=\"100%\" height=\"65%\">"+
           "<td valign=\"top\" align=\"center\">"+
              "<table class=\"TextInTable\" width=\"90%\" border=\"1\" bordercolor=\"#ffffff\" cellPadding=\"2\" cellSpacing=\"0\">"+
                "<tr><td colspan=\"6\" align=\"center\" bgColor=\"#ffffff\">"+
                  "<input type=\"button\" value=\"Добавить сотрудника\" onclick=\"javascript:window.location='../servlet/WebPagesGenerator?actionType=add_worker';\"><br><br>"+
                  "<font size=\"3\">В базе данных имеются следующие сотрудники<font>"+
                "</td></tr>"+
                "<tr>"+
                   "<td class=\"HeaderOfTable\">ФИО сотрудника</td>"+
                   "<td class=\"HeaderOfTable\">Должность</td>"+
                   "<td class=\"HeaderOfTable\">Телефон</td>"+
                   "<td class=\"HeaderOfTable\">Рабочая группа</td>"+
                   "<td class=\"HeaderOfTable\">Подразделение</td>"+              
                   "<td class=\"HeaderOfTable\">Действие</td>"+
                "</tr>"+
                "<tr>");
                String workerId="";
                try{   
                  st = conn.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);                    
                  rs=st.executeQuery("select * from workers");
                  while(rs.next()) {
                    workerId=rs.getString("worker_id");                
                    out.println("<td>" + rs.getString("fio") + "</td>");           
                    out.println("<td>" + rs.getString("doljnost") + "</td>");
                    out.println("<td>" + rs.getString("phone") + "</td>");                        
                    out.println("<td>" + rs.getString("work_group") + "</td>");  
                    st2 = conn.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);                    
                    rs2=st2.executeQuery("select * from departments where department_id='"+ 
                       rs.getString("department_id") +"'");
                    while(rs2.next()) {                  
                       out.println("<td>" + rs2.getString("department_name") + "</td>");      
                    }   
                    out.println("<td>"+
                    "<a href=\"javascript:window.location='../servlet/WebPagesGenerator?actionType=edit_worker&worker_id="+ workerId +"';\">Редактировать</a>"+
                    "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;"+
                    "<a href=\"javascript: if(window.confirm('Удалить выбранного сотрудника?')==1) "+
                              "window.location='../servlet/ModifyDatabase?actionType=delete_worker&worker_id="+ workerId +"';\">Удалить</a>"+
                    "</td>"+
                    "</tr>");
                  }          
                }    
                catch( SQLException ex){
                    out.println("<H1 ALIGN=CENTER>Внутренняя ошибка сервера: нет доступа к базе данных.</H1>\n\n"+ ex.toString());
                }             
              out.println("</table>"+
           "</td>"+
        "</table>");
      }
       
      else if(actionType.equals("edit_worker")==true){
        out.println("<body>");
        if(userName.equalsIgnoreCase("administrator")==false){
          out.println("<h1 align=center>Ошибка: у Вас нет права изменять НСИ.</h1>");
        }
        else{     
          out.println("<table class=\"HeaderOfTable\" border=\"0\" cellPadding=\"10\" cellSpacing=\"0\">"+
          "<tr>"+
            "<td width=\"95%\" bgColor=\"#ffffff\"></td>"+
            "<td><nobr>Корретировка НСИ</nobr></td>"+
          "</tr>"+
          "</table>"+
          "<table bgColor=\"#ffffff\" border=\"7\" bordercolor=\"#666699\" width=\"100%\" height=\"65%\">"+
            "<td align=\"center\" valign=\"top\">"+
              "<form action=\"../servlet/ModifyDatabase\" method=\"post\">"+
                "<table class=\"TextInTable\" width=\"40%\" border=\"1\" bordercolor=\"#ffffff\" cellPadding=\"2\" cellSpacing=\"0\">"+
                  "<tr bgColor=\"#ffffff\">"+
                    "<td align=\"right\"> <input type=\"submit\" value=\"Сохранить\"> </td>"+
                    "<td> <input type=\"reset\" value=\"Очистить форму\"> </td>"+
                  "</tr>"+
                  "<tr>"+
                    "<td bgColor=\"#ffffff\" colspan=\"2\" align=\"center\"><br><font size=\"3\">Введите новые данные в поля, которые надо изменить</font></td>"+
                  "</tr>"+
                  "<tr>"+
                    "<td class=\"HeaderOfTable\">ФИО сотрудника</td>");
                      String workerId = request.getParameter("worker_id");  
                      try{   
                        st = conn.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);                    
                        rs=st.executeQuery("select * from workers where worker_id='"+ workerId +"'");
                        while(rs.next()) {                
                          out.println("<input type=\"hidden\" name=\"worker_id\" value=\"" + workerId +"\">"+
                          "<input type=\"hidden\" name=\"actionType\" value=\"save_modified_worker\">"+                         
                          "<td><input type=\"text\" name=\"fio\" value=\""+ rs.getString("fio") +"\"></td>"+
                          "</tr>"+
                          "<tr>"+
                            "<td class=\"HeaderOfTable\">Должность</td>"+
                            "<td><select name=\"doljnost\" size=\"1\">"+
                              "<option selected value=\""+ rs.getString("doljnost") +"\">"+ rs.getString("doljnost")+
                              "<option value=\"начальник отдела\">начальник отдела"+
                              "<option value=\"зам. начальника отдела\">зам. начальника отдела"+
                              "<option value=\"инженер\">инженер"+
                              "<option value=\"техник\">техник"+
                            "</select></td>"+
                          "</tr>"+
                          "<tr> <td class=\"HeaderOfTable\">Телефон</td>"+
                            "<td><input type=\"text\" name=\"phone\" value=\""+ rs.getString("phone") +"\"></td>"+
                          "</tr>"+
                          "<tr><td class=\"HeaderOfTable\">Рабочая группа</td>"+
                            "<td><input type=\"text\" name=\"work_group\" value=\""+ rs.getString("work_group") +"\"></td>"+
                          "</tr>"+
                          "<tr><td class=\"HeaderOfTable\">Подразделение</td>"+
                            "<td><select name=\"department\" size=\"1\">");  
                              String selectedDepartmentId=rs.getString("department_id");
                              st2 = conn.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);                    
                              rs2=st2.executeQuery("select * from departments");
                              while(rs2.next()) {                                              
                                if(selectedDepartmentId.equals(rs2.getString("department_id"))==true)
                                  out.println("<option selected value=\""+rs2.getString("department_name")+"\">"+rs2.getString("department_name"));
                                else
                                  out.println("<option value=\""+rs2.getString("department_name")+"\">"+rs2.getString("department_name"));
                              }
                        }
                      }    
                      catch( SQLException ex){
                         out.println("<H1 ALIGN=CENTER>Внутренняя ошибка сервера: нет доступа к базе данных.</H1>\n\n"+ ex.toString());
                      }
                      out.println("</select></td>"+
                  "</tr>"+
                "</table>"+
              "</form>"+
            "</td>"+
          "</table>");
      }
     }
     
     else if(actionType.equals("add_worker")==true){
       out.println("<body>");
       if(userName.equalsIgnoreCase("administrator")==false){
         out.println("<h1 align=center>Ошибка: у Вас нет права изменять НСИ.</h1>");
       }
       else{
         out.println("<table class=\"HeaderOfTable\" border=\"0\" cellPadding=\"10\" cellSpacing=\"0\">"+
         "<tr>"+
           "<td width=\"95%\" bgColor=\"#ffffff\"></td>"+
           "<td><nobr>Корретировка НСИ</nobr></td>"+
         "</tr>"+
         "</table>"+
         "<table bgColor=\"#ffffff\" border=\"7\" bordercolor=\"#666699\" width=\"100%\" height=\"65%\">"+
           "<td align=\"center\" valign=\"top\">"+
             "<form action=\"../servlet/ModifyDatabase\" method=\"post\">"+
               "<table class=\"TextInTable\" width=\"40%\" border=\"1\" bordercolor=\"#ffffff\" cellPadding=\"2\" cellSpacing=\"0\">"+
                 "<tr bgColor=\"#ffffff\">"+
                   "<td align=\"right\"> <input type=\"submit\" value=\"Сохранить\"> </td>"+
                   "<td> <input type=\"reset\" value=\"Очистить форму\"> "+
                       "<input type=\"hidden\" name=\"actionType\" value=\"add_worker\"></td>"+                   
                 "</tr>"+
                 "<tr>"+
                   "<td bgColor=\"#ffffff\" colspan=\"2\" align=\"center\"><br><font size=\"3\">Введите данные о новом сотруднике</font></td>"+
                 "</tr>"+
                 "<tr>"+
                   "<td class=\"HeaderOfTable\">ФИО сотрудника</td>"+
                   "<td><input type=\"text\" name=\"fio\"></td>"+
                 "</tr>"+
                 "<tr>"+
                   "<td class=\"HeaderOfTable\">Должность</td>"+
                   "<td><select name=\"doljnost\" size=\"1\">"+
                     "<option selected value=\"0\">-- Не выбрано --"+
                     "<option value=\"начальник отдела\">начальник отдела"+
                     "<option value=\"зам. начальника отдела\">зам. начальника отдела"+
                     "<option value=\"инженер\">инженер"+
                     "<option value=\"техник\">техник"+
                   "</select></td>"+
                 "</tr>"+
                 "<tr> <td class=\"HeaderOfTable\">Телефон</td>"+
                      "<td><input type=\"text\" name=\"phone\"></td>"+
                 "</tr>"+
                 "<tr><td class=\"HeaderOfTable\">Рабочая группа</td>"+
                     "<td><input type=\"text\" name=\"work_group\"></td>"+
                 "</tr>"+
                 "<tr><td class=\"HeaderOfTable\">Подразделение</td>"+
                     "<td><select name=\"department\" size=\"1\">"+                  
                          "<option selected value=\"0\">-- Не выбрано --");
                          try{   
                            st = conn.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);                    
                            rs=st.executeQuery("select * from departments");
                            while(rs.next()) {
                              String departmentName=rs.getString("department_name");
                              out.println("<option value=\"" + departmentName + "\">" + departmentName);
                            }          
                          }    
                          catch( SQLException ex){
                            out.println("<H1 ALIGN=CENTER>Внутренняя ошибка сервера: нет доступа к базе данных.</H1>\n\n"+ ex.toString());
                          }
                     out.println("</select></td>"+
                 "</tr>"+
               "</table>"+
             "</form>"+
           "</td>"+
         "</table>");
       }
     }
     
     else if(actionType.equals("correct_places")==true){
       out.println("<body>"+
         "<table class=\"HeaderOfTable\"  border=\"0\" cellPadding=\"10\" cellSpacing=\"0\" >"+
           "<tr>"+
             "<td bgColor=\"#ffffff\"  width=\"90%\"></td>"+
             "<td><nobr>Корректировка НСИ</nobr></td>"+
           "</tr>"+
        "</table>"+
        "<table   bgColor=\"#ffffff\" border=\"7\" bordercolor=\"#666699\" width=\"100%\" height=\"65%\">"+
        "<td  valign=\"top\"  align=\"center\">"+
          "<table  class=\"TextInTable\" width=\"70%\"    border=\"1\" bordercolor=\"#ffffff\" cellPadding=\"2\" cellSpacing=\"0\">"+
             "<tr><td colspan=\"3\" align=\"center\" bgColor=\"#ffffff\">"+
             "<input type=\"button\" value=\"Добавить место проведения работ\" onclick=\"javascript:window.location='../servlet/WebPagesGenerator?actionType=add_place';\"><br><br>"+
             "<font size=\"3\">Список имеющихся в базе данных мест проведения работ</font></td> </tr>"+
             "<tr>"+
               "<td class=\"HeaderOfTable\">Наименование места</td> "+
               "<td class=\"HeaderOfTable\">Обслуживающее подразделение</td>"+
               "<td class=\"HeaderOfTable\">Действие</td>"+
             "</tr>");
              try{   
                st = conn.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);                    
                rs=st.executeQuery("select * from places");
                while(rs.next()) {                
                  out.println("<tr><td >"+rs.getString("place_name")+"</td>"+
                  "<td >"+rs.getString("department_name")+"</td>"+
                    "<td ><a href=\"javascript:window.location='../servlet/WebPagesGenerator?actionType=edit_place&placeId="+ rs.getString("place_id")+"';\">Редактировать</A>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;"+
                         "<a href=\"javascript: if(window.confirm('Удалить это место проведения работ?')==1) "+
                          "actionAndRefresh('../servlet/ModifyDatabase?actionType=delete_place&placeId="+ rs.getString("place_id") +"');\">Удалить</a>"+
                    "</td>"+
                  "</tr>");
                }          
             }    
             catch( SQLException ex){
               out.println("<H1 ALIGN=CENTER>Внутренняя ошибка сервера: нет доступа к базе данных.</H1>\n\n"+ ex.toString());
             }
          out.println("</table>"+
        "</td>"+
      "</table>");
     }
      
     else if(actionType.equals("correct_departments")==true){
       out.println("<body>"+
         "<table class=\"HeaderOfTable\"  border=\"0\" cellPadding=\"10\" cellSpacing=\"0\" >"+
           "<tr>"+
             "<td bgColor=\"#ffffff\"  width=\"90%\"></td>"+
             "<td><nobr>Корректировка НСИ</nobr></td>"+
           "</tr>"+
        "</table>"+
        "<table   bgColor=\"#ffffff\" border=\"7\" bordercolor=\"#666699\" width=\"100%\" height=\"65%\">"+
        "<td  valign=\"top\"  align=\"center\">"+
          "<table  class=\"TextInTable\" width=\"70%\"    border=\"1\" bordercolor=\"#ffffff\" cellPadding=\"2\" cellSpacing=\"0\">"+
             "<tr><td colspan=\"2\" align=\"center\" bgColor=\"#ffffff\">"+
             "<input type=\"button\" value=\"Добавить подразделение\" onclick=\"javascript:window.location='../servlet/WebPagesGenerator?actionType=add_department';\"><br><br>"+
             "<font size=\"3\">Список имеющихся в базе данных подразделений</font></td> </tr>"+
             "<tr>"+
               "<td class=\"HeaderOfTable\">Наименование подразделения</td> "+               
               "<td class=\"HeaderOfTable\">Действие</td>"+
             "</tr>");
              try{   
                st = conn.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);                    
                rs=st.executeQuery("select * from departments");
                while(rs.next()) {                
                  out.println("<tr><td >"+rs.getString("department_name")+"</td>"+                  
                    "<td ><a href=\"javascript:window.location='../servlet/WebPagesGenerator?actionType=edit_department&departmentId="+ rs.getString("department_id")+"';\">Редактировать</A>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;"+
                         "<a href=\"javascript: if(window.confirm('Удалить это подразделение?')==1) "+
                          "actionAndRefresh('../servlet/ModifyDatabase?actionType=delete_department&departmentId="+ rs.getString("department_id") +"');\">Удалить</a>"+
                    "</td>"+
                  "</tr>");
                }          
             }    
             catch( SQLException ex){
               out.println("<H1 ALIGN=CENTER>Внутренняя ошибка сервера: нет доступа к базе данных.</H1>\n\n"+ ex.toString());
             }
          out.println("</table>"+
        "</td>"+
      "</table>");
     }
      
      else if(actionType.equals("add_place")==true){
        out.println("<body><table class=\"HeaderOfTable\" border=\"0\" cellPadding=\"10\" cellSpacing=\"0\">"+
          "<tr>"+
            "<td width=\"95%\" bgColor=\"#ffffff\"></td>"+
            "<td><nobr>Корретировка НСИ</nobr></td>"+
          "</tr>"+
          "</table>"+
          "<table bgColor=\"#ffffff\" border=\"7\" bordercolor=\"#666699\" width=\"100%\" height=\"65%\">"+
            "<td align=\"center\" valign=\"top\">"+
              "<form action=\"../servlet/ModifyDatabase\" method=\"post\">"+
                "<table class=\"TextInTable\" width=\"40%\" border=\"1\" bordercolor=\"#ffffff\" cellPadding=\"2\" cellSpacing=\"0\">"+
                  "<tr bgColor=\"#ffffff\">"+
                    "<td align=\"right\"> <input type=\"submit\" value=\"Сохранить\"> </td>"+
                    "<td> <input type=\"reset\" value=\"Очистить форму\"> </td>"+
                  "</tr>"+
                  "<tr>"+
                    "<td bgColor=\"#ffffff\" colspan=\"2\" align=\"center\"><br><font size=\"3\">Введите данные о новом месте проведения работ</font></td>"+
                  "</tr>"+
                  "<tr>"+
                    "<td class=\"HeaderOfTable\">Наименование места</td>");
                      String placeId = request.getParameter("placeId");
                      try{                                                                
                        out.println("<input type=\"hidden\" name=\"placeId\" value=\"" + placeId +"\">"+
                        "<input type=\"hidden\" name=\"actionType\" value=\"add_place\">"+                         
                        "<td><input type=\"text\" name=\"placeName\"></td>"+
                        "</tr>"+                        
                        "<tr><td class=\"HeaderOfTable\">Обслуживающее подразделение</td>"+
                            "<td><select name=\"departmentName\" size=\"1\">"+
                                "<option selected value=\"--Не выбрано--\">--Не выбрано--");
                                st2 = conn.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);                    
                                rs2=st2.executeQuery("select * from departments");
                                while(rs2.next()) {                                                                            
                                  out.println("<option value=\""+rs2.getString("department_name")+"\">"+rs2.getString("department_name"));
                                }                     
                      }    
                      catch( SQLException ex){
                         out.println("<H1 ALIGN=CENTER>Внутренняя ошибка сервера: нет доступа к базе данных.</H1>\n\n"+ ex.toString());
                      }
                      out.println("</select></td>"+
                  "</tr>"+
                "</table>"+
              "</form>"+
            "</td>"+
          "</table>");      
     }
     
      else if(actionType.equals("edit_place")==true){
        out.println("<body><table class=\"HeaderOfTable\" border=\"0\" cellPadding=\"10\" cellSpacing=\"0\">"+
          "<tr>"+
            "<td width=\"95%\" bgColor=\"#ffffff\"></td>"+
            "<td><nobr>Корретировка НСИ</nobr></td>"+
          "</tr>"+
          "</table>"+
          "<table bgColor=\"#ffffff\" border=\"7\" bordercolor=\"#666699\" width=\"100%\" height=\"65%\">"+
            "<td align=\"center\" valign=\"top\">"+
              "<form action=\"../servlet/ModifyDatabase\" method=\"post\">"+
                "<table class=\"TextInTable\" width=\"40%\" border=\"1\" bordercolor=\"#ffffff\" cellPadding=\"2\" cellSpacing=\"0\">"+
                  "<tr bgColor=\"#ffffff\">"+
                    "<td align=\"right\"> <input type=\"submit\" value=\"Сохранить\"> </td>"+
                    "<td> <input type=\"reset\" value=\"Очистить форму\"> </td>"+
                  "</tr>"+
                  "<tr>"+
                    "<td bgColor=\"#ffffff\" colspan=\"2\" align=\"center\"><br><font size=\"3\">Введите новые данные в поля, которые надо изменить</font></td>"+
                  "</tr>"+
                  "<tr>"+
                    "<td class=\"HeaderOfTable\">Наименование места</td>");
                      String placeId = request.getParameter("placeId");
                      try{   
                        st = conn.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);                    
                        rs=st.executeQuery("select * from places where place_id='"+ placeId +"'");
                        while(rs.next()) {                
                          out.println("<input type=\"hidden\" name=\"placeId\" value=\"" + placeId +"\">"+
                          "<input type=\"hidden\" name=\"actionType\" value=\"save_modified_place\">"+                         
                          "<td><input type=\"text\" name=\"placeName\" value=\""+ rs.getString("place_name") +"\"></td>"+
                          "</tr>"+                        
                          "<tr><td class=\"HeaderOfTable\">Обслуживающее подразделение</td>"+
                            "<td><select name=\"departmentName\" size=\"1\">");  
                              String selectedDepartment=rs.getString("department_name");
                              st2 = conn.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);                    
                              rs2=st2.executeQuery("select * from departments");
                              while(rs2.next()) {                                              
                                if(selectedDepartment.equals(rs2.getString("department_name"))==true)
                                  out.println("<option selected value=\""+rs2.getString("department_name")+"\">"+rs2.getString("department_name"));
                                else
                                  out.println("<option value=\""+rs2.getString("department_name")+"\">"+rs2.getString("department_name"));
                              }
                        }
                      }    
                      catch( SQLException ex){
                         out.println("<H1 ALIGN=CENTER>Внутренняя ошибка сервера: нет доступа к базе данных.</H1>\n\n"+ ex.toString());
                      }
                      out.println("</select></td>"+
                  "</tr>"+
                "</table>"+
              "</form>"+
            "</td>"+
          "</table>");      
     }
      
      else if(actionType.equals("edit_department")==true){
        out.println("<body><table class=\"HeaderOfTable\" border=\"0\" cellPadding=\"10\" cellSpacing=\"0\">"+
          "<tr>"+
            "<td width=\"95%\" bgColor=\"#ffffff\"></td>"+
            "<td><nobr>Корретировка НСИ</nobr></td>"+
          "</tr>"+
          "</table>"+
          "<table bgColor=\"#ffffff\" border=\"7\" bordercolor=\"#666699\" width=\"100%\" height=\"65%\">"+
            "<td align=\"center\" valign=\"top\">"+
              "<form action=\"../servlet/ModifyDatabase\" method=\"post\">"+
                "<table class=\"TextInTable\" width=\"40%\" border=\"1\" bordercolor=\"#ffffff\" cellPadding=\"2\" cellSpacing=\"0\">"+
                  "<tr bgColor=\"#ffffff\">"+
                    "<td align=\"right\"> <input type=\"submit\" value=\"Сохранить\"> </td>"+
                    "<td> <input type=\"reset\" value=\"Очистить форму\"> </td>"+
                  "</tr>"+
                  "<tr>"+
                    "<td bgColor=\"#ffffff\" colspan=\"2\" align=\"center\"><br><font size=\"3\">Введите новое наименование подразделения</font></td>"+
                  "</tr>"+
                  "<tr>"+
                    "<td class=\"HeaderOfTable\">Наименование подразделения</td>");
                      String departmentId = request.getParameter("departmentId");
                      try{   
                        st = conn.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);                    
                        rs=st.executeQuery("select * from departments where department_id='"+ departmentId +"'");
                        while(rs.next()) {                
                          out.println("<input type=\"hidden\" name=\"departmentId\" value=\"" + departmentId +"\">"+
                          "<input type=\"hidden\" name=\"actionType\" value=\"save_modified_department\">"+                         
                          "<td><input type=\"text\" name=\"departmentName\" value=\""+ rs.getString("department_name") +"\"></td>"+
                          "</tr>");                          
                        }
                      }    
                      catch( SQLException ex){
                         out.println("<H1 ALIGN=CENTER>Внутренняя ошибка сервера: нет доступа к базе данных.</H1>\n\n"+ ex.toString());
                      }
                      out.println("</table>"+
              "</form>"+
            "</td>"+
          "</table>");      
     }
      
      else if(actionType.equals("add_department")==true){
        out.println("<body><table class=\"HeaderOfTable\" border=\"0\" cellPadding=\"10\" cellSpacing=\"0\">"+
          "<tr>"+
            "<td width=\"95%\" bgColor=\"#ffffff\"></td>"+
            "<td><nobr>Корретировка НСИ</nobr></td>"+
          "</tr>"+
          "</table>"+
          "<table bgColor=\"#ffffff\" border=\"7\" bordercolor=\"#666699\" width=\"100%\" height=\"65%\">"+
            "<td align=\"center\" valign=\"top\">"+
              "<form action=\"../servlet/ModifyDatabase\" method=\"post\">"+
                "<table class=\"TextInTable\" width=\"40%\" border=\"1\" bordercolor=\"#ffffff\" cellPadding=\"2\" cellSpacing=\"0\">"+
                  "<tr bgColor=\"#ffffff\">"+
                    "<td align=\"right\"> <input type=\"submit\" value=\"Сохранить\"> </td>"+
                    "<td> <input type=\"reset\" value=\"Очистить форму\"> </td>"+
                  "</tr>"+
                  "<tr>"+
                    "<td bgColor=\"#ffffff\" colspan=\"2\" align=\"center\"><br><font size=\"3\">Введите наименование подразделения</font></td>"+
                  "</tr>"+
                  "<tr>"+
                    "<td class=\"HeaderOfTable\">Наименование подразделения</td>"+
                    "<input type=\"hidden\" name=\"departmentId\">"+
                    "<input type=\"hidden\" name=\"actionType\" value=\"add_department\">"+                 
                    "<td><input type=\"text\" name=\"departmentName\"></td>"+
                  "</tr>"+
                "</table>"+
              "</form>"+
            "</td>"+
          "</table>");      
      }  
    }
  }
   public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    HttpSession session=request.getSession(false);
    Connection conn=(Connection)session.getValue("conn");  
    String userName=(String)session.getValue("userName");  
    RussianSymbolsConverter rsc = new RussianSymbolsConverter();
    response.setContentType("text/html; charset=windows-1251");
    PrintWriter out = response.getWriter();
    out.println("<html><head><link href=\"../html/css.htm\" type=\"text/css\" rel=\"Stylesheet\">"+
    "<script language=\"JavaScript\" src=\"../html/JavaScriptFunctions.js\"></script></head>");   
    if(userName==null){
      out.println("<script language=\"JavaScript\">window.alert('Сначала следует пройти аутентификацию.');");
      out.println("top.location='..';</script>");
    }   
    else{
      String actionType=request.getParameter("actionType");     
      if(actionType.equals("reports_table")==true){
        out.println("<body>"+
          "<TABLE class=\"HeaderOfTable\"  border=0 cellPadding=\"10\" cellSpacing=\"0\" >"+
          "<TR>"+
            "<TD bgColor=\"#ffffff\"  width=\"95%\"></td>"+
            "<TD><nobr>Поиск работ</nobr></TD>"+
          "</tr>"+
          "</table>"+
          "<table   bgColor=\"#ffffff\" border=7 bordercolor=\"#666699\" width=\"100%\" height=\"65%\">"+
          "<td  valign=\"top\"  align=\"center\">"+
            "<table class=\"TextInTable\" width=\"97%\"    border=\"1\" bordercolor=\"#ffffff\" cellPadding=\"2\" cellSpacing=\"0\">"+
              "<tr><td colspan=\"9\" align=\"center\" bgColor=\"#ffffff\"><font size=\"3\">По заданным параметрам найдены следующие работы</font></td> </tr>"+
              "<tr>"+
                "<td class=\"HeaderOfTable\">Идентификатор</td>"+
                "<td class=\"HeaderOfTable\">Наименование</td>"+
                "<td class=\"HeaderOfTable\">Координатор</td>"+
                "<td class=\"HeaderOfTable\">Статус</td>"+                                                                             
                "<td class=\"HeaderOfTable\">Дата начала</td>"+
                "<td class=\"HeaderOfTable\">Дата окончания</td>"+
              "</tr>");
              
        String workId=request.getParameter("workId");
        workId=rsc.convertString(workId);  
        String workName=request.getParameter("workName");        
        workName=rsc.convertString(workName);      
        String coordinator=request.getParameter("coordinator");
        coordinator=rsc.convertString(coordinator);   
        String status=request.getParameter("status");
        status=rsc.convertString(status);   
        String place=request.getParameter("place");        
        place=rsc.convertString(place);  
        String executor=request.getParameter("executor");
        executor=rsc.convertString(executor);  
       
        
        String queryForReport=new String("select * from works");
        String svyazka=new String(" where");
        if(workId.equals("-- Не имеет значения --")==false){
          queryForReport = queryForReport.concat(svyazka + " work_id='"+workId+"'");          
          svyazka=" and";
        }
        if(workName.equals("-- Не имеет значения --")==false){
          queryForReport = queryForReport.concat(svyazka + " work_name='"+workName+"'");
          if(svyazka.equals(" where")==true)
            svyazka=" and";
        }       
        if(coordinator.equals("-- Не имеет значения --")==false){
          queryForReport = queryForReport.concat(svyazka + " coordinator='"+coordinator+"'");
          if(svyazka.equals(" where")==true)
            svyazka=" and";
        }
        if(status.equals("-- Не имеет значения --")==false){
          queryForReport = queryForReport.concat(svyazka + " status='"+status+"'");
          if(svyazka.equals(" where")==true)
            svyazka=" and";
        }
        if(place.equals("-- Не имеет значения --")==false){
          try{   
            st = conn.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);                    
            rs=st.executeQuery("select distinct work_id from tasks where place='"+place+"'");              
            boolean isFirstRS=true;
            while(rs.next()) {     
              if(isFirstRS==true){
                queryForReport = queryForReport.concat(svyazka + " (work_id='"+rs.getString("work_id")+"'");                
                svyazka=" or";
                isFirstRS=false;
              }
              else
                queryForReport = queryForReport.concat(svyazka + " work_id='"+rs.getString("work_id")+"'");              
            }  
            if(isFirstRS==false)
              queryForReport = queryForReport.concat(")");
              
            svyazka=" and";
          }    
          catch( SQLException ex){
            out.println("<H1>Внутренняя ошибка сервера: нет доступа к базе данных.</H1>\n\n"+ ex.toString());
          }              
        }
        if(executor.equals("-- Не имеет значения --")==false){
          try{   
            st = conn.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);                    
            rs=st.executeQuery("select distinct work_id, instr(executors,'"+executor+"') from tasks");              
            boolean isFirstRS=true;
            while(rs.next()) {     
              if(rs.getString(2).equals("0")==false){
                if(isFirstRS==true){
                  queryForReport = queryForReport.concat(svyazka + " (work_id='"+rs.getString("work_id")+"'");                
                  svyazka=" or";
                  isFirstRS=false;
                }
                else
                  queryForReport = queryForReport.concat(svyazka + " work_id='"+rs.getString("work_id")+"'");              
              }
            }   
            if(isFirstRS==false)
              queryForReport = queryForReport.concat(")");
              
            svyazka=" and";
          }    
          catch( SQLException ex){
            out.println("<H1>Внутренняя ошибка сервера: нет доступа к базе данных.</H1>\n\n"+ ex.toString());
          }              
        }
      
        try{   
          st = conn.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);                    
          String selectedDepartment=(String)session.getValue("department");                       
          rs=st.executeQuery(queryForReport);              
        rs=st.executeQuery("select * from works where department_id=(select department_id from departments where department_name='"+selectedDepartment+"')");           
          while(rs.next()) {                                                  
            out.println("<tr>"+
              "<td>"+rs.getString("work_id")+"</td>"+
              "<td><a href=\"../servlet/WebPagesGenerator?actionType=reports_diagrams&workId="+rs.getString("work_id")+"\">"+rs.getString("work_name")+"</A></td>"+              
              "<td>"+rs.getString("coordinator")+"</td>"+
              "<td>"+rs.getString("status")+"</td>");
              st2 = conn.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);                    
              rs2=st2.executeQuery("select  min(begin_date), max(end_date) from tasks where work_id="+ rs.getString("work_id"));
              while(rs2.next()) {                  
                 out.println("<td>" + dfc.convertToDate(rs2.getString("min(begin_date)")) + "</td>");  
                 out.println("<td>" + dfc.convertToDate(rs2.getString("max(end_date)")) + "</td>");              
              }                            
              out.println("</tr>");
          } 
        }    
        catch( SQLException ex){
            out.println("<H1>Внутренняя ошибка сервера: нет доступа к базе данных.</H1>\n\n"+ ex.toString());
        }    
        out.println("</table>"+
        "</td>"+
       "</table>");
      }
      
      
      else if(actionType.equals("selecting_menu_item")==true){
        String department=request.getParameter("department"); 
        department=rsc.convertString(department);
        String buttonName=request.getParameter("buttonName"); 
        boolean isEnabled=true;
        out.println("<body><script>");
        if(department.equals("0")==true && buttonName.equals("admin")==false && buttonName.equals("nsi")==false){
           out.println("window.alert('Сначала следует выбрать подразделение.');");
           isEnabled=false;
        }
        else if(buttonName.equals("admin")==false && buttonName.equals("nsi")==false && buttonName.equals("reports")==false){
           try{
             st = conn.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);        
             rs = st.executeQuery("select enabled_department from users where user_name='"+userName+"'");        
             while(rs.next()) {                                        
               if(rs.getString("enabled_department").equals("все")==false){
                 if(rs.getString("enabled_department").equals(department)==false){
                   out.println("window.alert('Вам не разрешено изменять работы в выбранном подразделении. Выберите разрешенное подразделение.');");
                   isEnabled=false;
                 }
               }
             }  
           } catch( SQLException ex){
             out.println("<H1 ALIGN=CENTER>Внутренняя ошибка сервера: нет доступа к базе данных.</H1>\n\n"+ rsc.convertString(ex.toString()));
           } 
        } 
        if(isEnabled==true){
           session.putValue("department",department);                
           if(buttonName.equals("add_work")==true) out.println("window.location='../servlet/WebPagesGenerator?actionType=add_work';");          
           else if(buttonName.equals("correct_work")==true) out.println("window.location='../servlet/WebPagesGenerator?actionType=correct_works';");           
           else if(buttonName.equals("reports")==true) out.println("window.location='../servlet/WebPagesGenerator?actionType=reports_form';");
           else if(buttonName.equals("nsi")==true) out.println("window.location='../html/nsi.htm';");
           else if(buttonName.equals("admin")==true) out.println("window.location='../html/Admin.htm';");                   
        }
         out.println("</script>");
      }          
    }
    out.println("</body></html>");  
    out.close();
   }
}