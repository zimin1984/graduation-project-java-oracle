import java.util.*;

public class DateFormatConverter {  
  public String convertToDate (String sourceString) { 
    String convertedString = sourceString.replaceFirst(" 0:0:0","/");
    StringTokenizer date=new StringTokenizer(convertedString,"/");
    String month,day,year;
    month=date.nextToken();
    day=date.nextToken();
    year=date.nextToken();                    
    convertedString = day + "." + month + "." + year;
   
    return convertedString;
  }
}