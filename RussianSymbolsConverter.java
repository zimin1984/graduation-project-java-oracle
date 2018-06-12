public class RussianSymbolsConverter {  
  public String convertString (String sourceString) { 
    String convertedString = new String();
    for (int i=0; i < sourceString.length(); i++) {       
       String currentChar = sourceString.substring(i, i+1);
       switch (currentChar.hashCode()){
          case 192: convertedString = convertedString.concat("À"); break;
          case 193: convertedString = convertedString.concat("Á"); break;                
          case 194: convertedString = convertedString.concat("Â"); break;
          case 195: convertedString = convertedString.concat("Ã"); break;
          case 196: convertedString = convertedString.concat("Ä"); break;
          case 197: convertedString = convertedString.concat("Å"); break;
          case 168: convertedString = convertedString.concat("¨"); break;
          case 198: convertedString = convertedString.concat("Æ"); break;
          case 199: convertedString = convertedString.concat("Ç"); break;
          case 200: convertedString = convertedString.concat("È"); break;
          case 201: convertedString = convertedString.concat("É"); break;
          case 202: convertedString = convertedString.concat("Ê"); break;
          case 203: convertedString = convertedString.concat("Ë"); break;
          case 204: convertedString = convertedString.concat("Ì"); break;
          case 205: convertedString = convertedString.concat("Í"); break;
          case 206: convertedString = convertedString.concat("Î"); break;
          case 207: convertedString = convertedString.concat("Ï"); break;
          case 208: convertedString = convertedString.concat("Ð"); break;
          case 209: convertedString = convertedString.concat("Ñ"); break;
          case 210: convertedString = convertedString.concat("Ò"); break;
          case 211: convertedString = convertedString.concat("Ó"); break;
          case 212: convertedString = convertedString.concat("Ô"); break;
          case 213: convertedString = convertedString.concat("Õ"); break;
          case 214: convertedString = convertedString.concat("Ö"); break;
          case 215: convertedString = convertedString.concat("×"); break;
          case 216: convertedString = convertedString.concat("Ø"); break;
          case 217: convertedString = convertedString.concat("Ù"); break;
          case 218: convertedString = convertedString.concat("Ú"); break;
          case 219: convertedString = convertedString.concat("Û"); break;
          case 220: convertedString = convertedString.concat("Ü"); break;
          case 221: convertedString = convertedString.concat("Ý"); break;
          case 222: convertedString = convertedString.concat("Þ"); break;
          case 223: convertedString = convertedString.concat("ß"); break;
       
          case 224: convertedString = convertedString.concat("à"); break;
          case 225: convertedString = convertedString.concat("á"); break;                
          case 226: convertedString = convertedString.concat("â"); break;
          case 227: convertedString = convertedString.concat("ã"); break;
          case 228: convertedString = convertedString.concat("ä"); break;
          case 229: convertedString = convertedString.concat("å"); break;
          case 184: convertedString = convertedString.concat("¸"); break;
          case 230: convertedString = convertedString.concat("æ"); break;
          case 231: convertedString = convertedString.concat("ç"); break;
          case 232: convertedString = convertedString.concat("è"); break;
          case 233: convertedString = convertedString.concat("é"); break;
          case 234: convertedString = convertedString.concat("ê"); break;
          case 235: convertedString = convertedString.concat("ë"); break;
          case 236: convertedString = convertedString.concat("ì"); break;
          case 237: convertedString = convertedString.concat("í"); break;
          case 238: convertedString = convertedString.concat("î"); break;
          case 239: convertedString = convertedString.concat("ï"); break;
          case 240: convertedString = convertedString.concat("ð"); break;
          case 241: convertedString = convertedString.concat("ñ"); break;
          case 242: convertedString = convertedString.concat("ò"); break;
          case 243: convertedString = convertedString.concat("ó"); break;
          case 244: convertedString = convertedString.concat("ô"); break;
          case 245: convertedString = convertedString.concat("õ"); break;
          case 246: convertedString = convertedString.concat("ö"); break;
          case 247: convertedString = convertedString.concat("÷"); break;
          case 248: convertedString = convertedString.concat("ø"); break;
          case 249: convertedString = convertedString.concat("ù"); break;
          case 250: convertedString = convertedString.concat("ú"); break;
          case 251: convertedString = convertedString.concat("û"); break;
          case 252: convertedString = convertedString.concat("ü"); break;
          case 253: convertedString = convertedString.concat("ý"); break;
          case 254: convertedString = convertedString.concat("þ"); break;
          case 255: convertedString = convertedString.concat("ÿ"); break;
          
          default: convertedString = convertedString.concat(currentChar); break;
       }
    }     
    return convertedString;
  }
}