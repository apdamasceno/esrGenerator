package com.f6s.innovation.esrGenerator;

import java.util.List;
import java.util.Map;

import com.f6s.innovation.esrGenerator.controller.Excel;
import com.f6s.innovation.esrGenerator.controller.Utils;

/**
 * Hello world!
 *
 */
public class Generator 
{

	public Generator() {
		// TODO Auto-generated constructor stub
	}

	static String path=".";
	
	public static void main( String[] args )
	{

        final String dir = System.getProperty("user.dir");
        System.out.println("current dir = " + dir);
		path = dir+"\\";
		if( ! Utils.fileExits(path, "data.xlsx") ) {
			System.out.println("Data file not found: data.xlsx");
		}else {
			if( ! Utils.fileExits(path, "template.html") ) {
				System.out.println("Template File not found: template.html");
			}else {
				generateFiles(path,  "data.xlsx", "template.html" );
			}
		}
		Utils.deleteDirectory(path, "temp");
	}

	public static String sanitizeString(String org) {
		String res = "";
		String aux = org;
		aux = aux.replace(" ", "_");
		for( int j = 0; j<aux.length(); j++) {
			char ch = aux.charAt (j);
			if( ch == '_' || (ch>='a' && ch<='z') || (ch>='A' && ch<='Z')|| (ch>='0' && ch<='9')) {
				res = res + ch;
			}
			
		}
		return res;
	}

	

	public static void generateFiles( String path, String fileName, String templateFile) {
		
		Map<Integer, List<String>> data = Excel.readFile(path+fileName);
		
		List<String> headers = data.get(0);
		int numHeaders =0;
		int aux = headers.size();
		
		for( int h=0;h<aux;h++) {
			String auxHeader = headers.get(h).trim();
			if( auxHeader.length()>0) {
				auxHeader = auxHeader;
				numHeaders++;
				headers.set(h,auxHeader);
			}
			
		}
		
		int rows = data.size();
		for(int r=1;r<rows;r++) {
			List<String> rowData = data.get(r);
			String outputName = rowData.get(0);
			if( outputName.trim().length()>0) {
				outputName = sanitizeString(outputName);
				Utils.checkDirectory(path+"temp/");
				Utils.checkDirectory(path+"exp/");
				
				String template = Utils.readFile(path,templateFile);
				String templateArray[] = template.split("§");
				
				for( int h=1;h<numHeaders;h++) {
					String marker = headers.get(h);
					String value = "";
					if( h< rowData.size()) {
						value = rowData.get(h).replace("\n", "<br/>");
					}

					for( int j =0; j<templateArray.length;j++) {
						if( templateArray[j].equalsIgnoreCase(marker) ) {
							templateArray[j]= value;
						}
						
					}
					
//					template = template.replaceAll(marker, value);
				}
				StringBuilder sb = new StringBuilder();
				for( int j =0; j<templateArray.length;j++) {
					sb.append(templateArray[j]);
				}
				
				
				
				Utils.writeToFile(path+"/temp/", outputName+".html",sb.toString());
				Utils.convertHtmlFilePdf(path+"/temp/",outputName+".html", path+"exp/",outputName+".pdf");


			}
		
		}
	}
	
	

}
