package org.example.ReadExcel;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.sql.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Iterator;

import static org.apache.poi.ss.usermodel.CellType.STRING;

public class Main {
	public static void main(String[] args) throws FileNotFoundException {
		insert_into_database();
	}

	public static void insert_into_database(){
		try{
			FileInputStream file = new FileInputStream("C:\\week2\\Problem2-Read-Data-from-cv2-main\\src\\main\\Files\\Data.xlsx");

			//Create Workbook instance holding reference to .xlsx file
			XSSFWorkbook wb = new XSSFWorkbook(file);
			XSSFSheet ws = wb.getSheetAt(0);
			DateFormat df1 = new SimpleDateFormat("yyyy-MM-dd");
			DateFormat df2 = new SimpleDateFormat("hh:mm:ss");
			//Iterate through each rows one by one
			Iterator<Row> rowIterator = ws.iterator();
			rowIterator.next(); //skipping 1st row which has headings

			Connection con = DataSourceToLocal.getConnection();
			con.setAutoCommit(false);
			PreparedStatement p = null;
			String sql =
					"insert into interviews (interviewdate, team, panelname, interviewround, skill, interviewtime, candidate_cur_loc, candidate_pref_loc, candidate_name)\n"
							+ "values(?, ?, ?, ?, ?, ?, ?, ?, ?)";
			p = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);

			ResultSet rs = null;

			while (rowIterator.hasNext()){

				Row row = rowIterator.next();
				if(row.getCell(0).getCellType() == STRING || row.getCell(6).getCellType() == STRING){
					continue;
				}
				try{
					String cellDate = df1.format(row.getCell(0).getDateCellValue());
					String team = row.getCell(2).getStringCellValue();
					String panel = row.getCell(3).getStringCellValue();
					String round = row.getCell(4).getStringCellValue();
					String skill = row.getCell(5).getStringCellValue();
					String time = cellDate + " " + df2.format(row.getCell(6).getDateCellValue());
					String candidate_cur_loc = row.getCell(7).getStringCellValue();
					String candidate_pref_loc = row.getCell(8).getStringCellValue();
					String candidate_name = row.getCell(9).getStringCellValue();
//                    System.out.print(cellDate+" "+team+" "+panel+" "+round+" "+skill+" "+time+" ");
//                    System.out.print(candidate_cur_loc+" "+candidate_pref_loc+" "+candidate_name);

					//------------------------------------Inserting Data into Database---------------------------------------------//
					p.setString(1, cellDate);
					p.setString(2, team);
					p.setString(3, panel);
					p.setString(4, round);
					p.setString(5, skill);
					p.setString(6, time);
					p.setString(7, candidate_cur_loc);
					p.setString(8, candidate_pref_loc);
					p.setString(9, candidate_name);
					//---------------------------------------------------------------------------------//

				}catch (NullPointerException newe){
					continue;
				}
				p.addBatch();
//                System.out.println();
			}
			int[] updateCounts = p.executeBatch();
			con.commit();
			con.setAutoCommit(true);
			System.out.println(updateCounts.length + " rows inserted");

			file.close();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
}
