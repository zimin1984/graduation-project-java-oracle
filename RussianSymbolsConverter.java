public class RussianSymbolsConverter {  
  public String convertString (String sourceString) { 
    String convertedString = new String();
    for (int i=0; i < sourceString.length(); i++) {       
       String currentChar = sourceString.substring(i, i+1);
       switch (currentChar.hashCode()){
          case 192: convertedString = convertedString.concat("�"); break;
          case 193: convertedString = convertedString.concat("�"); break;                
          case 194: convertedString = convertedString.concat("�"); break;
          case 195: convertedString = convertedString.concat("�"); break;
          case 196: convertedString = convertedString.concat("�"); break;
          case 197: convertedString = convertedString.concat("�"); break;
          case 168: convertedString = convertedString.concat("�"); break;
          case 198: convertedString = convertedString.concat("�"); break;
          case 199: convertedString = convertedString.concat("�"); break;
          case 200: convertedString = convertedString.concat("�"); break;
          case 201: convertedString = convertedString.concat("�"); break;
          case 202: convertedString = convertedString.concat("�"); break;
          case 203: convertedString = convertedString.concat("�"); break;
          case 204: convertedString = convertedString.concat("�"); break;
          case 205: convertedString = convertedString.concat("�"); break;
          case 206: convertedString = convertedString.concat("�"); break;
          case 207: convertedString = convertedString.concat("�"); break;
          case 208: convertedString = convertedString.concat("�"); break;
          case 209: convertedString = convertedString.concat("�"); break;
          case 210: convertedString = convertedString.concat("�"); break;
          case 211: convertedString = convertedString.concat("�"); break;
          case 212: convertedString = convertedString.concat("�"); break;
          case 213: convertedString = convertedString.concat("�"); break;
          case 214: convertedString = convertedString.concat("�"); break;
          case 215: convertedString = convertedString.concat("�"); break;
          case 216: convertedString = convertedString.concat("�"); break;
          case 217: convertedString = convertedString.concat("�"); break;
          case 218: convertedString = convertedString.concat("�"); break;
          case 219: convertedString = convertedString.concat("�"); break;
          case 220: convertedString = convertedString.concat("�"); break;
          case 221: convertedString = convertedString.concat("�"); break;
          case 222: convertedString = convertedString.concat("�"); break;
          case 223: convertedString = convertedString.concat("�"); break;
       
          case 224: convertedString = convertedString.concat("�"); break;
          case 225: convertedString = convertedString.concat("�"); break;                
          case 226: convertedString = convertedString.concat("�"); break;
          case 227: convertedString = convertedString.concat("�"); break;
          case 228: convertedString = convertedString.concat("�"); break;
          case 229: convertedString = convertedString.concat("�"); break;
          case 184: convertedString = convertedString.concat("�"); break;
          case 230: convertedString = convertedString.concat("�"); break;
          case 231: convertedString = convertedString.concat("�"); break;
          case 232: convertedString = convertedString.concat("�"); break;
          case 233: convertedString = convertedString.concat("�"); break;
          case 234: convertedString = convertedString.concat("�"); break;
          case 235: convertedString = convertedString.concat("�"); break;
          case 236: convertedString = convertedString.concat("�"); break;
          case 237: convertedString = convertedString.concat("�"); break;
          case 238: convertedString = convertedString.concat("�"); break;
          case 239: convertedString = convertedString.concat("�"); break;
          case 240: convertedString = convertedString.concat("�"); break;
          case 241: convertedString = convertedString.concat("�"); break;
          case 242: convertedString = convertedString.concat("�"); break;
          case 243: convertedString = convertedString.concat("�"); break;
          case 244: convertedString = convertedString.concat("�"); break;
          case 245: convertedString = convertedString.concat("�"); break;
          case 246: convertedString = convertedString.concat("�"); break;
          case 247: convertedString = convertedString.concat("�"); break;
          case 248: convertedString = convertedString.concat("�"); break;
          case 249: convertedString = convertedString.concat("�"); break;
          case 250: convertedString = convertedString.concat("�"); break;
          case 251: convertedString = convertedString.concat("�"); break;
          case 252: convertedString = convertedString.concat("�"); break;
          case 253: convertedString = convertedString.concat("�"); break;
          case 254: convertedString = convertedString.concat("�"); break;
          case 255: convertedString = convertedString.concat("�"); break;
          
          default: convertedString = convertedString.concat(currentChar); break;
       }
    }     
    return convertedString;
  }
}