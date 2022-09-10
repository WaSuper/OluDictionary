package com.dictionary.olu.tool;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import com.dictionary.olu.WordItem;

import jxl.Sheet;
import jxl.Workbook;

public class ExcelUtils {

	public static List<WordItem> readExcelToWords(String filePath) {	
		List<WordItem> itemList = new ArrayList<WordItem>();
		Workbook workbook = null;
        try {
            // 加载Excel文件
        	InputStream is = new FileInputStream(filePath);
            // 获取workbook
        	workbook = Workbook.getWorkbook(is);
        	if (workbook.getSheets().length > 0) {
				Sheet sheet = workbook.getSheet(0);
				if (sheet.getColumns() >= 4) {
					for (int i = 1; i < sheet.getRows(); i++) {
						String word = sheet.getCell(0, i).getContents();
						String accent = sheet.getCell(1, i).getContents();
						String type = sheet.getCell(2, i).getContents();
						String mean = sheet.getCell(3, i).getContents();
						WordItem item = new WordItem(word, accent, type, mean);
						itemList.add(item);
					}
				}
			}
        	workbook.close();// 关闭工作簿
        } catch(Exception e) {
        	e.printStackTrace();
        	if (workbook != null) {
				workbook.close();
			}
        	return null;
        }
        return itemList;
	}
	
}
