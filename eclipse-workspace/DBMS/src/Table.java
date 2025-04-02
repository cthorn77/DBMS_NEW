import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;

public class Table {
	private ArrayList<String> columns;
	private HashMap<String, String> dataType;
	private File file;
	private String name;
	private String path;
	
	public Table(String name, String path) {
		this.name = name;
		this.path = path;
		this.columns = new ArrayList<>();
		this.dataType = new HashMap<>();
		this.file = new File(path + this.name + ".txt");
	}
	
	public void insertRecord(String text) throws IOException {
		try (RandomAccessFile raf = new RandomAccessFile(file, "rw")) {
			raf.writeBytes(text);
		}
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
	
	public void writeRecordsToFile(String insertedString, String[] values, Table table, BinarySearchTree bst) throws FileNotFoundException, IOException {
		File insertedFile = new File(insertedString);
		long offset;
		int key;
		try (RandomAccessFile raf = new RandomAccessFile(insertedFile, "rw")) {
			raf.seek(raf.length());
			
			String result = table.checkIsPrimary(path + table.getName());
			
			
			if (result.equals("isPrimary")) {
				ArrayList<String> primaryValues;
				ArrayList<String> insertedValues = new ArrayList<>();
				
				for (String value : values) {
					insertedValues.add(value);
				}
				
				primaryValues = table.selectInsert(insertedValues, insertedString);
				
				for (String value : primaryValues) {
					if (value.equals(values[0])) {
						System.out.println("Another record contains this primary key already");
						return;
					}
				}
				offset = raf.getFilePointer();
				key = Integer.parseInt(values[0]);
				bst.insert(key, offset);
			}
			
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
	
	//SELECT * FROM Students WHERE Age > 16;
	public ArrayList<String> selectItems(ArrayList<String> items, String insertedString, String condition, Table table, BinarySearchTree bst) throws IOException {
		File insertedFile = new File(insertedString);
		ArrayList<String> returnItems = new ArrayList<>(); 
		ArrayList<String> getItems = new ArrayList<>();
		String[] conditionItems = condition.split(" ");
		
		for (int i = 0; i < conditionItems.length; i++) {
		    conditionItems[i] = conditionItems[i].trim();
		}
		
		if (!insertedFile.exists()) {
			System.out.println("❌ File not found: " + insertedString);
			return new ArrayList<String>();
		}
		
		try (RandomAccessFile raf = new RandomAccessFile(insertedFile, "rw")) {
			String line;
			int count;
			String[] parts;
			String addSpaces = "";
			String tableName = insertedString.replace(".txt", "");
			String check = table.checkIsPrimary(tableName);
			
			
			
			if (items.isEmpty()) {
				System.out.println("No columns selected");
				return new ArrayList<String>();
			}
			
			else {
				if (items.get(0).equals("*")) {
					raf.seek(0);
					line = raf.readLine();
					
					if (line == null) {
						System.out.println("❌ File is empty.");
						return new ArrayList<String>();
					}
					
					System.out.println("📌 Table Schema: " + line);
					System.out.println("📋 Table Data:");
					
					if (!condition.equals("")) {
						getItems = table.getItems(conditionItems, insertedString, table);
						
						if (getItems.isEmpty()) {
							System.out.println("No records match the condition");
							return new ArrayList<>();
						}
						
						for (int i=0; i<getItems.size(); i++) {
							parts = getItems.get(i).split(" ");
							addSpaces = "";
							for (int j=0; j<parts.length; j++) {
								addSpaces += parts[j];
								for (int k=parts[j].length(); k<15; k++) {
									addSpaces += " ";
								}
							}
							System.out.println(addSpaces);
						}
						return new ArrayList<>();
					}
					
					else {
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
						
						return new ArrayList<String>();	
					}
					
				} else {
					String[] tableItems;
					raf.seek(0);
					line = raf.readLine();
					returnItems = new ArrayList<>();
					
					if (line == null) {
						System.out.println("❌ File is empty.");
						return new ArrayList<String>();
					}
					
					tableItems = line.replace("{", "").replace("}", "").split(",");
					ArrayList<Integer> columnNumbers = new ArrayList<>();
					String[] dataParts;
					
					for (int i=0; i<tableItems.length; i++) {
						String columnName = tableItems[i].split("=")[0].trim();
						for (int j=0; j<items.size(); j++) {
//							System.out.println("Comparing: '" + columnName + "' with '" + items.get(j).trim() + "'");
							if (columnName.equals(items.get(j).trim())) {
								columnNumbers.add(i);
							}
						}
					}
					
					if (columnNumbers.isEmpty()) {
						System.out.println("Column does not exist");
						return new ArrayList<String>();
					}
					
					System.out.print("📌 Table Schema: {");

					for (int i=0; i<columnNumbers.size(); i++) {
						if (i == columnNumbers.size()-1) {
							System.out.print(tableItems[columnNumbers.get(i)].trim() + "}\n");
						} else {
							System.out.print(tableItems[columnNumbers.get(i)].trim() + ", ");
						}	
					}
					
					System.out.println("📋 Table Data:");
					count = 0;
	 				
					if (!condition.equals("")) {
						getItems = table.getItems(conditionItems, insertedString, table);
						
						if (getItems.isEmpty()) {
							System.out.println("No records match the condition");
							return new ArrayList<>();
						}
						
						for (int initial=0; initial<getItems.size(); initial++) {
							dataParts = getItems.get(initial).split(" ");
							addSpaces = "";
							
							for (int i = 0; i < columnNumbers.size(); i++) { 
						        int colIndex = columnNumbers.get(i);  // ✅ Get correct column index

						        if (colIndex >= dataParts.length) {  // ✅ Prevent index error
						            System.out.println("❌ Error: Column index " + colIndex + " out of bounds for row: " + line);
						            break;
						        }
						        
						        if (i == columnNumbers.size() - 1) {  // ✅ Last column, print new line
						            System.out.print(dataParts[colIndex] + "\n");
						            returnItems.add(dataParts[colIndex]);
						        } else {
						            addSpaces = dataParts[colIndex];  // ✅ Get correct column value 
						            returnItems.add(dataParts[colIndex]);
						            // ✅ Add spacing for alignment
						            for (int j = dataParts[colIndex].length(); j < 15; j++) { 
						                addSpaces += " ";
						            }

						            System.out.print(addSpaces + " ");
						        }
						    }
						}
					}
					
					else {
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
						            returnItems.add(dataParts[colIndex]);
						        } else {
						            addSpaces = dataParts[colIndex];  // ✅ Get correct column value 
						            returnItems.add(dataParts[colIndex]);
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
					return returnItems;
				}
			}
			
			
		}
	}
	
	public ArrayList<String> selectInsert(ArrayList<String> items, String insertedString) throws IOException {
		File insertedFile = new File(insertedString);
		ArrayList<String> returnItems = new ArrayList<>(); 
		
		if (!insertedFile.exists()) {
			System.out.println("❌ File not found: " + path + insertedString);
			return new ArrayList<String>();
		}
		
		try (RandomAccessFile raf = new RandomAccessFile(insertedFile, "rw")) {
			String line;
			String[] parts;
			
			if (items.isEmpty()) {
				System.out.println("No columns selected");
				return new ArrayList<String>();
			}
			
			raf.seek(0);
			raf.readLine();
			while ((line = raf.readLine()) != null) {
				parts = line.split(" ");
				returnItems.add(parts[0]);
			}
			
			return returnItems;
		}
	}
	//								0  1 2   3   4  5  6
	//SELECT * FROM Students WHERE Age > 16 AND Age < 21;
	//[Age,>,16]
	public ArrayList<String> getItems(String[] conditionItems, String insertedString, Table table) throws FileNotFoundException, IOException {
		String[] data;
		String line;
		String[] condition = new String[3];
		ArrayList<String> returnItems = new ArrayList<>();
		ArrayList<String> returnItemsMultiCondition = new ArrayList<>();
		int columnNumber = -1;
		
		
		try (RandomAccessFile raf = new RandomAccessFile(insertedString, "rw")) {
			raf.seek(0);
			line = raf.readLine();
			data = line.replace("{", "").replace("}", "").split(", ");
			
			for (int i=0; i<data.length; i++) {
				if (data[i].contains(conditionItems[0])) {
					columnNumber = i;
				}
			}
			
			if (columnNumber == -1) {
				System.out.println("Column does not exist");
				return new ArrayList<>();
			}
			
			while ((line = raf.readLine()) != null) {
				data = line.split(" ");
				if (table.evaluateExpression(conditionItems, data[columnNumber])) {
					returnItems.add(line);
				}
			}
			
			if (conditionItems.length > 3) {
				raf.seek(0);
				line = raf.readLine();
				data = line.replace("{", "").replace("}", "").split(", ");
				
				for (int i=0; i<data.length; i++) {
					if (data[i].contains(conditionItems[4])) {
						columnNumber = i;
					}
				}
				
				if (columnNumber == -1) {
					System.out.println("Column does not exist");
					return new ArrayList<>();
				}
				
				condition[0] = conditionItems[4]; 
				condition[1] = conditionItems[5]; 
				condition[2] = conditionItems[6]; 
				
				for (int i=0; i<returnItems.size(); i++) {
					data = returnItems.get(i).split(" ");
					line = returnItems.get(i);
					if (table.evaluateExpression(condition, data[columnNumber])) {
						if (conditionItems[3].equals("AND")) {
							if (returnItems.contains(line)) {
								returnItemsMultiCondition.add(line);
							}
						} else {
							if (!returnItems.contains(line)) {
								returnItems.add(line);
							}
						}
					}
				}
			}
			
			if (!returnItemsMultiCondition.isEmpty()) {
				return returnItemsMultiCondition;
			}
			
			return returnItems;
		}
	}
	
	public boolean evaluateExpression(String[] conditionItems, String comparisonItem) {
		switch (conditionItems[1]) {
		case ">":
			return Double.parseDouble(comparisonItem) > Double.parseDouble(conditionItems[2]);
			
		case "<":
			return Double.parseDouble(comparisonItem) < Double.parseDouble(conditionItems[2]);
			
		case ">=":
			return Double.parseDouble(comparisonItem) >= Double.parseDouble(conditionItems[2]);
			
		case "<=":
			return Double.parseDouble(comparisonItem) <= Double.parseDouble(conditionItems[2]);
			
		case "=":
			return Double.parseDouble(comparisonItem) == Double.parseDouble(conditionItems[2]);
			
		case "!=":
			return Double.parseDouble(comparisonItem) != Double.parseDouble(conditionItems[2]);
			
		default:
			return false;
		}
	}

	
	public void writePrimaryKeyToFile(String insertedString) throws FileNotFoundException, IOException {
		File file = new File("PrimaryKeys.txt");
		
		try (RandomAccessFile raf = new RandomAccessFile(file, "rw")) {
			raf.seek(raf.length());
			raf.writeBytes(insertedString + ".txt" + System.lineSeparator()); 
		}
	}
	
	public String getName() {
		return name;
	}
	
	public String checkIsPrimary(String insertedString) throws FileNotFoundException, IOException {
		try (RandomAccessFile raf = new RandomAccessFile("PrimaryKeys.txt", "rw")) {
			raf.seek(0);
			String line;
			
			while ((line = raf.readLine()) != null) {
				if (line.equals(insertedString + ".txt")) {
					return "isPrimary";
				}
			}
			return "";
		}
	}
	
	public void describeTables(String tableName, Table table) throws FileNotFoundException, IOException {
		File folder = new File(path.replace("/", ""));
		File[] files = folder.listFiles();
		String[] items;
		String[] itemParts;
		String check = "";
		String addSpaces = "";
		String line;
		
		if (tableName.equals("ALL")) {
			if (files != null) {
				for (File file : files) {
					addSpaces = "";
					System.out.println(file.getName());
					try (RandomAccessFile raf = new RandomAccessFile(file, "rw")) {
					raf.seek(0);
					items = raf.readLine().replace("{", "").replace("}", "").split(", ");
					check = table.checkIsPrimary(path + file.getName().replace(".txt", ""));
					for (int i=0; i<items.length; i++) {
						itemParts = items[i].split("=");
						line = itemParts[0] + ":";
						addSpaces += line;
						for (int j=line.length(); j<15; j++) {
							addSpaces += " ";
						}
						line = itemParts[1];
						addSpaces += line;
						for (int j=line.length(); j<15; j++) {
							addSpaces += " ";
						}
						if (i==0 && check.equals("isPrimary")) {
							addSpaces += "PRIMARY KEY\n";
						} else {
							addSpaces += "\n";
						}
					}
					System.out.println(addSpaces);
					}
				}
			}
			folder.delete();
		} else {
			File file = new File(path + tableName + ".txt");
			System.out.println(file.getName());
			try (RandomAccessFile raf = new RandomAccessFile(file, "rw")) {
				raf.seek(0);
				if ((line = raf.readLine()) == null) {
					System.out.println("Not a proper file");
					return;
				}
				items = line.replace("{", "").replace("}", "").split(", ");
				check = table.checkIsPrimary(path + file.getName().replace(".txt", ""));
				for (int i=0; i<items.length; i++) {
					itemParts = items[i].split("=");
					line = itemParts[0] + ":";
					addSpaces += line;
					for (int j=line.length(); j<15; j++) {
						addSpaces += " ";
					}
					line = itemParts[1];
					addSpaces += line;
					for (int j=line.length(); j<15; j++) {
						addSpaces += " ";
					}
					if (i==0 && check.equals("isPrimary")) {
						addSpaces += "PRIMARY KEY\n";
					} else {
						addSpaces += "\n";
					}
				}
				System.out.println(addSpaces);
			}
		}
	}

}
