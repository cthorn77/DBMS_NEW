import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;

public class Table {
	private ArrayList<String> columns;
	private HashMap<String, String> dataType;
	private File file;
	private int offset;
	private String name;
	
	public Table(String name) {
		this.name = name;
		this.columns = new ArrayList<>();
		this.dataType = new HashMap<>();
		this.file = new File(this.name + ".txt");
		this.offset = 0;
	}
	
	public void insertRecord(String text) throws IOException {
		RandomAccessFile raf = new RandomAccessFile(file, "rw");
		raf.writeBytes(text);
		
		text = text.replace(");", "");
		
		String[] parts = text.split("\\(");
		
		
		for (String part : parts) {
			System.out.println(part);
		}
	}
	
	public void assignColumns(String text) {
		//Split the command into 2 pieces
		String[] command = text.split("\\(");
		command[1] = command[1].replace(");", "");
		//Make column names separate from command
		String[] columnNames = command[1].split(","); 
		
		//Check for valid data types before adding
		for (int i = 0; i<columnNames.length; i++) {
			if (columnNames[i].split(" ")[1].equals("INTEGER")) {
				columns.add(columnNames[i].split(" ")[0]);
				dataType.put(columnNames[i].split(" ")[0], "INTEGER");
			} else if (columnNames[i].split(" ")[1].equals("TEXT")) {
				columns.add(columnNames[i].split(" ")[0]);
				dataType.put(columnNames[i].split(" ")[0], "TEXT");
			} else if (columnNames[i].split(" ")[1].equals("FLOAT")) {
				columns.add(columnNames[i].split(" ")[0]);
				dataType.put(columnNames[i].split(" ")[0], "FLOAT");
			}
		}
	}
	
	public void writeColumnsToFile() throws IOException {
		try (RandomAccessFile raf = new RandomAccessFile(file, "rw")) {
			raf.setLength(0);
			raf.writeBytes("{");
			
			for (String column : columns) {
				if (column == columns.get(columns.size()-1)) {
					raf.writeBytes(column + "=" + dataType.get(column));
				} else {
					raf.writeBytes(column + "=" + dataType.get(column) + ", ");
				}
			}
					
			raf.writeBytes("}\n");
			
		} catch (Exception e) {
			System.out.println("Failed to add columns to file.");
		}
	}
	
	public void writeRecordsToFile(String insertedString, String[] values) throws FileNotFoundException, IOException {
		File insertedFile = new File(insertedString);
		try (RandomAccessFile raf = new RandomAccessFile(insertedFile, "rw")) {
			raf.seek(raf.length());
			
			for (String value : values) {
				raf.writeBytes(value + " ");
			}
			
			raf.writeBytes(System.lineSeparator());
			
		} catch (Exception e) {
			System.out.println("Failed to add record to file.");
		}
	}
	
	public String[] readDataTypes(String insertedString) throws IOException {
	    File insertedFile = new File(insertedString);

	    if (!insertedFile.exists()) {
	        System.out.println("❌ File not found: " + insertedString);
	        return new String[0];
	    }

	    try (RandomAccessFile raf = new RandomAccessFile(insertedFile, "r")) {
	        raf.seek(0);
	        String info = raf.readLine(); // ✅ Read first line

	        if (info == null || info.isEmpty()) {
	            System.out.println("❌ File is empty.");
	            return new String[0];
	        }

	        info = info.replace("{", "").replace("}", ""); // ✅ Remove braces
	        String[] parts = info.split(",\\s*"); // ✅ Split by `, ` to separate columns

	        for (int i = 0; i < parts.length; i++) {
	            parts[i] = parts[i].split("=")[1]; // ✅ Extract only the data type (TEXT, INTEGER, FLOAT)
	        }

	        return parts; // ✅ Returns ["TEXT", "INTEGER", "INTEGER"]
	    } catch (Exception e) {
	        System.out.println("❌ Could not retrieve data types: " + e.getMessage());
	        e.printStackTrace();
	        return new String[0];
	    }
	}

}
