public class RussianSymbolsConverter {  
  public String convertString (String sourceString) { 
    String convertedString = new String();
    for (int i=0; i < sourceString.length(); i++) {       
       String currentChar = sourceString.substring(i, i+1);
       switch (currentChar.hashCode()){
          case 192: convertedString = convertedString.concat("А"); break;
          case 193: convertedString = convertedString.concat("Б"); break;                
          case 194: convertedString = convertedString.concat("В"); break;
          case 195: convertedString = convertedString.concat("Г"); break;
          case 196: convertedString = convertedString.concat("Д"); break;
          case 197: convertedString = convertedString.concat("Е"); break;
          case 168: convertedString = convertedString.concat("Ё"); break;
          case 198: convertedString = convertedString.concat("Ж"); break;
          case 199: convertedString = convertedString.concat("З"); break;
          case 200: convertedString = convertedString.concat("И"); break;
          case 201: convertedString = convertedString.concat("Й"); break;
          case 202: convertedString = convertedString.concat("К"); break;
          case 203: convertedString = convertedString.concat("Л"); break;
          case 204: convertedString = convertedString.concat("М"); break;
          case 205: convertedString = convertedString.concat("Н"); break;
          case 206: convertedString = convertedString.concat("О"); break;
          case 207: convertedString = convertedString.concat("П"); break;
          case 208: convertedString = convertedString.concat("Р"); break;
          case 209: convertedString = convertedString.concat("С"); break;
          case 210: convertedString = convertedString.concat("Т"); break;
          case 211: convertedString = convertedString.concat("У"); break;
          case 212: convertedString = convertedString.concat("Ф"); break;
          case 213: convertedString = convertedString.concat("Х"); break;
          case 214: convertedString = convertedString.concat("Ц"); break;
          case 215: convertedString = convertedString.concat("Ч"); break;
          case 216: convertedString = convertedString.concat("Ш"); break;
          case 217: convertedString = convertedString.concat("Щ"); break;
          case 218: convertedString = convertedString.concat("Ъ"); break;
          case 219: convertedString = convertedString.concat("Ы"); break;
          case 220: convertedString = convertedString.concat("Ь"); break;
          case 221: convertedString = convertedString.concat("Э"); break;
          case 222: convertedString = convertedString.concat("Ю"); break;
          case 223: convertedString = convertedString.concat("Я"); break;
       
          case 224: convertedString = convertedString.concat("а"); break;
          case 225: convertedString = convertedString.concat("б"); break;                
          case 226: convertedString = convertedString.concat("в"); break;
          case 227: convertedString = convertedString.concat("г"); break;
          case 228: convertedString = convertedString.concat("д"); break;
          case 229: convertedString = convertedString.concat("е"); break;
          case 184: convertedString = convertedString.concat("ё"); break;
          case 230: convertedString = convertedString.concat("ж"); break;
          case 231: convertedString = convertedString.concat("з"); break;
          case 232: convertedString = convertedString.concat("и"); break;
          case 233: convertedString = convertedString.concat("й"); break;
          case 234: convertedString = convertedString.concat("к"); break;
          case 235: convertedString = convertedString.concat("л"); break;
          case 236: convertedString = convertedString.concat("м"); break;
          case 237: convertedString = convertedString.concat("н"); break;
          case 238: convertedString = convertedString.concat("о"); break;
          case 239: convertedString = convertedString.concat("п"); break;
          case 240: convertedString = convertedString.concat("р"); break;
          case 241: convertedString = convertedString.concat("с"); break;
          case 242: convertedString = convertedString.concat("т"); break;
          case 243: convertedString = convertedString.concat("у"); break;
          case 244: convertedString = convertedString.concat("ф"); break;
          case 245: convertedString = convertedString.concat("х"); break;
          case 246: convertedString = convertedString.concat("ц"); break;
          case 247: convertedString = convertedString.concat("ч"); break;
          case 248: convertedString = convertedString.concat("ш"); break;
          case 249: convertedString = convertedString.concat("щ"); break;
          case 250: convertedString = convertedString.concat("ъ"); break;
          case 251: convertedString = convertedString.concat("ы"); break;
          case 252: convertedString = convertedString.concat("ь"); break;
          case 253: convertedString = convertedString.concat("э"); break;
          case 254: convertedString = convertedString.concat("ю"); break;
          case 255: convertedString = convertedString.concat("я"); break;
          
          default: convertedString = convertedString.concat(currentChar); break;
       }
    }     
    return convertedString;
  }
}