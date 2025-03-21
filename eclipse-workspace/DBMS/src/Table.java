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
	
	public void selectItems(ArrayList<String> items, String insertedString) throws IOException {
		File insertedFile = new File(insertedString);
		
		if (!insertedFile.exists()) {
			System.out.println("❌ File not found: " + insertedString);
			return;
		}
		
		try (RandomAccessFile raf = new RandomAccessFile(insertedFile, "rw")) {
			String line;
			int count;
			String[] parts;
			String addSpaces = "";
			
			if (items.isEmpty()) {
				System.out.println("No columns selected");
				return;
			}
			
			if (items.get(0).equals("*")) {
				raf.seek(0);
				line = raf.readLine();
				
				if (line == null) {
					System.out.println("❌ File is empty.");
					return;
				}
				
				System.out.println("📌 Table Schema: " + line);
				System.out.println("📋 Table Data:");
				
				count=0;
				while ((line=raf.readLine()) != null) {
					count++;
					parts = line.split(" ");
					addSpaces = "";
					for (int i=0; i<parts.length; i++) {
						addSpaces += parts[i];
						for (int j=parts[i].length(); j<15; j++) {
							addSpaces += " ";
						}
					}
					System.out.println(addSpaces);
				}
				
				if (count == 0) {
					System.out.println("Table is empty");
				}
			} else if (items.size() == 1) {
				String[] tableItems;
				raf.seek(0);
				line = raf.readLine();
				
				if (line == null) {
					System.out.println("❌ File is empty.");
					return;
				}
				
				tableItems = line.replace("{", "").replace("}", "").split(",\\s*");
				int columnNumber = -1;
				String[] dataParts;
				
				for (int i=0; i<tableItems.length; i++) {
					if (tableItems[i].contains(items.get(0))) {
						columnNumber = i;
					}
				}
				
				if (columnNumber == -1) {
					System.out.println("Column does not exist");
					return;
				}
				
				System.out.println("📌 Table Schema: " + tableItems[columnNumber]);
				System.out.println("📋 Table Data:");
				
				count = 0;
				
				while ((line=raf.readLine()) != null) {
					count++;
					dataParts = line.split(" ");
					addSpaces = "";
					for (int i=0; i<dataParts.length; i++) {
						addSpaces += dataParts[i];
						for (int j=dataParts[i].length(); j<15; j++) {
							addSpaces += " ";
						}
					}
					System.out.println(addSpaces);
				}
				
				if (count == 0) {
					System.out.println("Table is empty");
				}
				
			} else {
				String[] tableItems;
				raf.seek(0);
				line = raf.readLine();
				
				if (line == null) {
					System.out.println("❌ File is empty.");
					return;
				}
				
				tableItems = line.replace("{", "").replace("}", "").split(",");
				ArrayList<Integer> columnNumbers = new ArrayList<>();
				String[] dataParts;
				
				for (int i=0; i<tableItems.length; i++) {
					for (int j=0; j<items.size(); j++) {
						if (tableItems[i].contains(items.get(j))) {
							columnNumbers.add(i);
						}
					}
				}
				
				if (columnNumbers.isEmpty()) {
					System.out.println("Column does not exist");
					return;
				}
				
				System.out.print("📌 Table Schema: {");

				for (int i=0; i<columnNumbers.size(); i++) {
					if (i == columnNumbers.size()-1) {
						System.out.print(tableItems[columnNumbers.get(i)] + "}\n");
					} else {
						System.out.print(tableItems[columnNumbers.get(i)].trim() + ", ");
					}	
				}
				
				System.out.println("📋 Table Data:");
 				
				count = 0;
				while ((line = raf.readLine()) != null) {
				    count++;
				    dataParts = line.split(" ");  // ✅ Split row data
				    addSpaces = "";

				    for (int i = 0; i < columnNumbers.size(); i++) { 
				        int colIndex = columnNumbers.get(i);  // ✅ Get correct column index

				        if (colIndex >= dataParts.length) {  // ✅ Prevent index error
				            System.out.println("❌ Error: Column index " + colIndex + " out of bounds for row: " + line);
				            break;
				        }
				        
				        if (i == columnNumbers.size() - 1) {  // ✅ Last column, print new line
				            System.out.print(dataParts[colIndex] + "\n");
				        } else {
				            addSpaces = dataParts[colIndex];  // ✅ Get correct column value

				            // ✅ Add spacing for alignment
				            for (int j = dataParts[colIndex].length(); j < 15; j++) { 
				                addSpaces += " ";
				            }

				            System.out.print(addSpaces + " ");
				        }
				    }
				}
				
				if (count == 0) {
					System.out.println("Table is empty");
				}
			}
		}
	}

}
