package com.f6s.innovation.esrGenerator.controller;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.text.SimpleDateFormat;

import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.xhtmlrenderer.layout.SharedContext;
import org.xhtmlrenderer.pdf.ITextRenderer;



public class Utils {
	
	private static final Logger logger = LogManager.getLogger(Utils.class);
	
	private static final SimpleDateFormat sdf = new SimpleDateFormat ("yyyy-MM-dd HH:mm");
	
	
	
	public static boolean fileExits(String path, String fileName) {
		boolean res = false;
		FileTime modifiedDate =  getFileModifiedDate( path,  fileName);
		if( modifiedDate!=null) {
			res = true;
		}
		return res;
	}

	public static void deleteDirectory(String path, String fileName) {
		try {
			FileUtils.deleteDirectory(new File(path+ fileName));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	
	public static FileTime getFileModifiedDate(String path, String fileName) {
		FileTime res = null;

		try {
			
	        Path file = Paths.get(path+fileName);
	        BasicFileAttributes attr;
			attr = Files.readAttributes(file, BasicFileAttributes.class);
	        res = attr.lastModifiedTime();
		} catch (IOException e) {
			
//			e.printStackTrace();
		}
        
        return res;
	}

    public static void writeToFile ( String path, String fileName, String str) {
    	
    	String auxPath = "";
    	path = path.toLowerCase();
    	if( path.startsWith("c:") ) {
    		auxPath = path;
    	}else {
    		auxPath  = path;
    	}
    	
        BufferedWriter writer;
        if( str != null) {
		try {
			Utils.checkDirectory(auxPath);
			
			if( checkDirectory(auxPath)) {
				writer = new BufferedWriter(new FileWriter(auxPath+fileName,StandardCharsets.UTF_8));
		        writer.write(str);
		        writer.close();
			}
		} catch (IOException e) { 
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        }
    	
    }
    
    public static String readFile( String path,String fileName) {

        StringBuilder contentBuilder = new StringBuilder();
        try (BufferedReader br = new BufferedReader(new FileReader(path+fileName,StandardCharsets.UTF_8))) 
        {
            String sCurrentLine;
            while ((sCurrentLine = br.readLine()) != null) 
            {
                contentBuilder.append(sCurrentLine).append("\n");
            }
        } 
        catch (IOException e) 
        {
        	System.err.println(e.getLocalizedMessage());
        }
        return contentBuilder.toString();
   	
    	
    }

    public static boolean checkDirectory( String subPath) {
    	boolean res = false;
		try {
			subPath = subPath.replace("\\", "/");
			
			String[] path = subPath.split("/");
			String fullPath = "";
			for(String sp:path) {
				if( fullPath.length()>0) {
					fullPath=fullPath+"/";
				}
				fullPath=fullPath+sp;
				File pathAsFile = new File(fullPath);
				if (!Files.exists(Paths.get(fullPath))) {
					pathAsFile.mkdir();
				}
			}
			res = true;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	return res;
    }
    
    
    public static void convertHtmlFilePdf(String orgPath,String orgFileName,String destPath,String destFileName) {
    	
    	checkDirectory( orgPath);
    	checkDirectory( destPath);
    	 
    	
		File inputHTML = new File( orgPath+orgFileName);
		Document document;
		try {
			document = Jsoup.parse(inputHTML, "UTF-8");
			document.outputSettings().syntax(Document.OutputSettings.Syntax.xml);
			OutputStream outputStream = new FileOutputStream(destPath+destFileName);
			    ITextRenderer renderer = new ITextRenderer();
			    SharedContext sharedContext = renderer.getSharedContext();
			    sharedContext.setPrint(true);
			    sharedContext.setInteractive(false);
			    renderer.setDocumentFromString(document.html());
			    renderer.layout();
			    renderer.createPDF(outputStream)	;		
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }


	
	
/*	
	public static SecretKeySpec getSecretKey( String password) {
		SecretKeySpec res = null;
		
	        if (password == null) {
	            throw new IllegalArgumentException("Run with -Dpassword=<password>");
	        }

	        // The salt (probably) can be stored along with the encrypted data
	        byte[] salt = new String("12345678").getBytes();

	        // Decreasing this speeds down startup time and can be useful during testing, but it also makes it easier for brute force attackers
	        int iterationCount = 40000;
	        // Other values give me java.security.InvalidKeyException: Illegal key size or default parameters
	        int keyLength = 128;
	        try {
				res = createSecretKey(password.toCharArray(), salt, iterationCount, keyLength);
				
			} catch (NoSuchAlgorithmException |InvalidKeySpecException e) {
				
				e.printStackTrace();
			}
	
        return res;
		
	}
	
	
	

    public static SecretKeySpec createSecretKey(char[] password, byte[] salt, int iterationCount, int keyLength) throws NoSuchAlgorithmException, InvalidKeySpecException {
        SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA512");
        PBEKeySpec keySpec = new PBEKeySpec(password, salt, iterationCount, keyLength);
        SecretKey keyTmp = keyFactory.generateSecret(keySpec);
        return new SecretKeySpec(keyTmp.getEncoded(), "AES");
    }

    
    public static String encrypt(String property,  String password) {
    	return encrypt(property, getSecretKey( password));
    }

    
    public static String encrypt(String property, SecretKeySpec key)  {
    	String res = null;
    	
        Cipher pbeCipher;
		try {
			
			pbeCipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
			
	        pbeCipher.init(Cipher.ENCRYPT_MODE, key);
	        AlgorithmParameters parameters = pbeCipher.getParameters();
	        IvParameterSpec ivParameterSpec = parameters.getParameterSpec(IvParameterSpec.class);
	        byte[] cryptoText = pbeCipher.doFinal(property.getBytes("UTF-8"));
	        byte[] iv = ivParameterSpec.getIV();
	        
	        
	        res =  base64Encode(iv) + ":" + base64Encode(cryptoText);
			
			
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        
        return res;
    }

    private static String base64Encode(byte[] bytes) {
        return Base64.getEncoder().encodeToString(bytes);
    }

    public static String decrypt(String property,  String password) {
    	return decrypt(property, getSecretKey( password));
    }


    
    public static String decrypt(String string, SecretKeySpec key)  {
    	String res = null;
    	
    	
		try {
	        String iv = string.split(":")[0];
	        String property = string.split(":")[1];
	        Cipher pbeCipher;
			pbeCipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
	        pbeCipher.init(Cipher.DECRYPT_MODE, key, new IvParameterSpec(base64Decode(iv)));
	        
	        res = new String(pbeCipher.doFinal(base64Decode(property)), "UTF-8");

		} catch ( ArrayIndexOutOfBoundsException e) {
	        //do nothing
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        
        return res;
    }

    private static byte[] base64Decode(String property) throws IOException {
        return Base64.getDecoder().decode(property);
    }
	
    public static void deleteToApplicationFile ( String organization, String pipelineId, int applicationId) {
    	String path =  Utils.getApplicationDetailsPath(organization, pipelineId, applicationId);
    	String fileName = Utils.getApplicationDetailsFilename(organization, pipelineId, applicationId);

       	String auxPath = "";
    	path = path.toLowerCase();
    	if( path.startsWith("c:") ) {
    		auxPath = path;
    	}else {
    		auxPath  = Global.getPath()+path;
    	}
    	
    	try {
			File file = new File(auxPath+fileName); 
			if (file.delete()) { 
			      System.out.println("Deleted the file: " + file.getName());
			} else {
			  System.out.println("Failed to delete the file.");
			}
		} catch (Exception e) {
			System.err.println("Delete the file error - File:" + auxPath+fileName+". "+e.getLocalizedMessage());
		} 
    }
    
    
    
    
    
    
    public static String readResourceFile( String path,String fileName) {

    	
    	
        StringBuilder contentBuilder = new StringBuilder();
        try (BufferedReader br = new BufferedReader(new FileReader(path+fileName))) 
        {
 
            String sCurrentLine;
            while ((sCurrentLine = br.readLine()) != null) 
            {
                contentBuilder.append(sCurrentLine).append("\n");
            }
        } 
        catch (IOException e) 
        {
            e.printStackTrace();
        }
        return contentBuilder.toString();
   	
     	
    }

    
    
    

    
    public static String formatDate(Date date) {
    	String res = "";
    	if( date != null) {
    		res = sdf.format(date);
    	}
    	return res;
    }
 
	
	
	public static String getApplicationDetailsPath(String organization, String pipelineId, int applicationId) {
		return (organization+"/"+pipelineId+"/");
	}
	
	public static String getApplicationDetailsFilename(String organization, String pipelineId, int applicationId) {
		return (applicationId+".json");
	}

	public static String getApplicationHistoryFilename(String organization, String pipelineId, int applicationId) {
		return (applicationId+"_history.json");
	}

	

	public static boolean fileExitsAndWasSavedAfter(String path, String fileName, Date date) {
		boolean res = false;
		FileTime modifiedDate =  getFileModifiedDate( path,  fileName);
		if( modifiedDate!=null) {
			Calendar fileDate =  Calendar.getInstance();
			fileDate.setTimeInMillis(modifiedDate.toMillis());
			res = (fileDate.getTimeInMillis()>date.getTime());
			logger.trace("FileDate:"+formatDate(fileDate.getTime())+" refDate"+formatDate(date)+ " isAfter:"+res);
		}
		return res;
	}
	
    
	public static boolean unzipFile (String fileZip, String destPath) {
		boolean res = false;
        File destDir = new File(destPath);
        byte[] buffer = new byte[1024];
        try {
			ZipInputStream zis = new ZipInputStream(new FileInputStream(fileZip));
			ZipEntry zipEntry = zis.getNextEntry();
			while (zipEntry != null) {
			     File newFile = newFile(destDir, zipEntry);
			     if (zipEntry.isDirectory()) {
			         if (!newFile.isDirectory() && !newFile.mkdirs()) {
			             throw new IOException("Failed to create directory " + newFile);
			         }
			     } else {
			         // fix for Windows-created archives
			         File parent = newFile.getParentFile();
			         if (!parent.isDirectory() && !parent.mkdirs()) {
			             throw new IOException("Failed to create directory " + parent);
			         }
			         
			         // write file content
			         FileOutputStream fos = new FileOutputStream(newFile);
			         int len;
			         while ((len = zis.read(buffer)) > 0) {
			             fos.write(buffer, 0, len);
			         }
			         fos.close();
			     }
			     zipEntry = zis.getNextEntry();
				
			}
			zis.closeEntry();
			zis.close();
			res = true;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        
        
        return res;
    }

		
	public static File newFile(File destinationDir, ZipEntry zipEntry) throws IOException {
	    File destFile = new File(destinationDir, zipEntry.getName());

	    String destDirPath = destinationDir.getCanonicalPath();
	    String destFilePath = destFile.getCanonicalPath();

	    if (!destFilePath.startsWith(destDirPath + File.separator)) {
	        throw new IOException("Entry is outside of the target dir: " + zipEntry.getName());
	    }

	    return destFile;
	}	
	
	public static String country2ccode(String country) {
		String res = "XX";
		
		switch (country) {
			case "Austria": res = "AT"; break;
			case "Belgium": res = "BE"; break;
			case "Bulgaria": res = "BG"; break;
			case "Croatia": res = "HR"; break;
			case "Cyprus": res = "CY"; break;
			case "Czechia": res = "CZ"; break;
			case "Denmark": res = "DK"; break;
			case "Estonia": res = "EE"; break;
			case "France": res = "FR"; break;
			case "Finland": res = "FI"; break;
			case "Germany": res = "DE"; break;
			case "Greece": res = "GR"; break;
			case "Hungary": res = "HU"; break;
			case "Ireland": res = "IE"; break;
			case "Italy": res = "IT"; break;
			case "Latvia": res = "LV"; break;
			case "Lithuania": res = "LT"; break;
			case "Luxembourg": res = "LU"; break;
			case "Malta": res = "MT"; break;
			case "Netherlands": res = "NL"; break;
			case "Poland": res = "PL"; break;
			case "Portugal": res = "PT"; break;
			case "Romania": res = "RO"; break;
			case "Slovakia": res = "SK"; break;
			case "Slovenia": res = "SI"; break;
			case "Spain": res = "ES"; break;
			case "Sweden": res = "SE"; break;
		default:
			break;
		}
		
		return res;
	}
    
	public static String partner2PartnerCode(String partner) {
		String res = "X";
		
		switch (partner) {
		 case "Malta Enterprise IVF" : 													res="ME";break;
		 case "Agenţia de Dezvoltare Regională Nord – Vest" : 							res="NWRDA";break;
		 case "Irish Manufacturing Research" : 											res="IMR";break;
		 case "BIC Bratislava spol. s.r.o" : 											res="BIC";break;
		 case "ARC Consulting EOOD" :													res="ARCC";break;
		 case "Europa Media Szolgaltato non Profitkozhasznu Kft" : 						res="EM";break;
		 case "Technology Centre of the CAS" : 											res="TC CAS";break;
		 case "Rise" : 																	res="RISE";break;
		 case "Netcompany - Intrasoft S.A." : 											res="INTRA";break;
		 case "Regionale Ontwikkelingsmaatschappij InnovationQuarter BV" : 				res="IQ";break;
		 case "Latvijas Tehnologiskais centrs , nodibinajums" : 						res="LTC";break;
		 case "University of Maribor – Rectorate" : 									res="UM";break;
		 case "Lietuvos Inovacijų Centras, VŠĮ – Rekvizitai" :							res="LIC";break;
		 case "MASOC - Mašīnbūves un metālapstrādes rūpniecības asociācija" : 			res="MASOC";break;
		 case "Asociatia Tehimpuls-Centrul Regional De Inovare Si Transfer" : 			res="TEH";break;
		 case "Agencja Rozwoju Mazowsza S.A." : 										res="ARMSA";break;
		 case "Fasttrack Action, Lda" : 												res="FTA";break;
		 case "BEIA GmbH" : 															res="BEIA";break;
		 case "Imp3rove - European Innovation Management Academy EWIV" : 				res="IMP";break;
		 case "European Crowdfunding Network" : 										res="ECN";break;
		 case "Danish Teknologisk Institut" : 											res="DTI";break;
		 case "Associazione Fabbrica Intelligente Lombardia" : 							res="AFIL";break;
		 case "F6S Network Limited" : 													res="F6S";break;
		 case "Vilniaus prekybos, pramonės ir amatų rūmai" : 							res="VCCIC";break;
		 case "Industrial Research Institute for Automation and Measurements PIAP" :	res="PIAP";break;
		 case "RiniGARD d.o.o. za usluge" : 											res="RINI";break;
		 case "Foreningen MADE - Manufacturing Academy of Denmark" : 					res="MADE";break;
		 case "Fundación Tecnalia Research & Innovation" : 								res="TEC";break;
		 case "Pragma - IoΤ ΙΚΕ" : 														res="PRA";break;
		 case "Fundación Para La Formación Técnica En Máquina Herramienta" : 			res="IMH";break;
		 case "Cyprus Digital Innovation Hub (CyDI-Hub) LTD" : 							res="CYDIH";break;
		 case "DIMECC Oy" : 															res="DIM";break;
		 case "Civitta Eesti AS" : 														res="CIV";break;
		 case "Systematic Paris Region" : 												res="SYS";break;
		 case "No" :					 												res="";break;
		default:
			break;
		}
		
		return res;
	}

	
	
	
	static Date string2Date(String dateStr) {
		Date res = null;
		
		try {
			res = sdf.parse(dateStr);
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return res;
	}
	
	
	public static long numberDays(Date startDate, Date reference) {
		long res = 0;
		if( reference!=null) {
		    long diffInMillies = Math.abs(reference.getTime() - startDate.getTime());
		    res= TimeUnit.DAYS.convert(diffInMillies, TimeUnit.MILLISECONDS);
		}
		return res;
	}
	
	public static long numberWeeks(Date startDate, Date reference) {
		long res = numberDays( startDate,  reference);
		return res/7;
	}




	public static String anonimizeName(String name) {
		String names[] = name.split(" ");
		String res = "";
		for(String aux:names) {
			if( aux.length()>0) {
				res = res + aux.substring(0,1)+"** ";
			}
		}
		return res;
	}




	public static String anonimizeEmail(String email) {
		String names[] = email.split("@");
		String res = "*****@";
		if( names.length==2) {
			res = res + names[1];
		}
		return res;
	}

	public static Integer parseInt(String number) {
		Integer res = null;
		
		try {
			if (number != null ) {
				number = number.trim();
				if( number.length()>0) {
					res = 	Integer.parseInt(number);
				}
			}
		} catch (NumberFormatException e) {

		}
		
		return res;
	}
*/

}
