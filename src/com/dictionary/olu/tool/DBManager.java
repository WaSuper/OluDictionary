package com.dictionary.olu.tool;

import java.util.ArrayList;
import java.util.List;

import com.dictionary.olu.WordItem;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class DBManager {

    private SQLiteDatabase mDB;
    private String dbPath;
    private String tableName;
 
    public DBManager(String dbPath) {
    	this.dbPath = dbPath;
    }
 
    public void openDB() {
    	if (mDB == null || !mDB.isOpen()) {
            mDB = SQLiteDatabase.openDatabase(dbPath, null,
                    SQLiteDatabase.OPEN_READWRITE);
            try {
                String sql = "select name from sqlite_master where type='table'";
                Cursor cursor = mDB.rawQuery(sql, null);
                while (cursor.moveToNext()) {
                    String name = cursor.getString(cursor.getColumnIndex("name"));
                    //System.out.println("DBManager - tableName : " + name);
                    if (name.equals("android_metadata")) {
						continue;
					} else {
						tableName = name;
						break;
					}
                }
                cursor.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
    	}
    }
 
    public void closeDB() {
    	if (mDB != null || mDB.isOpen()) {
    		mDB.close();
    	}
    }
    
    public List<WordItem> queryWord() {
    	openDB();
        List<WordItem> wordList = new ArrayList<WordItem>();
    	if (tableName == null || tableName.isEmpty()) {
			return wordList;
		}
        try {
            String sql = " select * from " + tableName;
            Cursor cursor = mDB.rawQuery("pragma table_info( " + tableName + " )", null);
            boolean[] columnExists = {false, false, false, false};
            while (cursor.moveToNext()) {
                String name = cursor.getString(cursor.getColumnIndex("name"));
                if (name.equals("word")) {
                	columnExists[0] = true;
				} else if (name.equals("accent")) {
					columnExists[1] = true;
				} else if (name.equals("type")) {
					columnExists[2] = true;
				} else if (name.equals("mean")) {
					columnExists[3] = true;
				}
            }
            cursor.close();
            cursor = mDB.rawQuery(sql, null);
            while (cursor.moveToNext()) {
            	String word, accent, type, mean;
                word = columnExists[0] ? cursor.getString(cursor.getColumnIndex("word")) : "";
                accent = columnExists[1] ? cursor.getString(cursor.getColumnIndex("accent")) : "";
                type = columnExists[2] ? cursor.getString(cursor.getColumnIndex("type")) : "";
                mean = columnExists[3] ? cursor.getString(cursor.getColumnIndex("mean")) : "";
                WordItem item = new WordItem(word, accent, type, mean);
                wordList.add(item);
            }
            cursor.close();
            return wordList;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

	
}
