package com.f6s.innovation.esrGenerator.controller;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;



public class Excel {

	
	public static Map<Integer, List<String>> readFile(String fileName) {
		
		Map<Integer, List<String>> data = null;
		
		
		try {
			FileInputStream file = new FileInputStream(new File(fileName));
			Workbook workbook = new XSSFWorkbook(file);
			Sheet sheet = workbook.getSheetAt(0);
			data = new HashMap<>();
			
			int i = 0;
			for (Row row : sheet) {
			    data.put(i, new ArrayList<String>());
			    for (Cell cell : row) {
			        switch (cell.getCellType()) {
			            case STRING: 
			            	data.get(i).add(cell.getRichStringCellValue().getString()); break;
			            case NUMERIC: 
			        		if (DateUtil.isCellDateFormatted(cell)) {
			        			data.get(i).add(cell.getDateCellValue() + "");
			        		} else {
			        			data.get(i).add(cell.getNumericCellValue() + "");
			        		}
			        		break;
			            case BOOLEAN: 
			            	data.get(i).add(cell.getBooleanCellValue() + ""); 
			            	break;
			            case FORMULA: 
			            	data.get(i).add(cell.getCellFormula() + "");
			            	break;
			            default: data.get(i).add(" ");
			        }
			    }
			    i++;
			}
			workbook.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return data;
	}
	

}
