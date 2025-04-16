import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

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
		
		
		// SELECT Students.Course, Courses.ID FROM Students, Courses WHERE Students.Course = Courses.Title;
		
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
		ArrayList<String> records = new ArrayList<>();
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
				records.add(line);
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
				
				
				if (conditionItems[3].equals("AND")) {
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
				} else {
					for (int i=0; i<records.size(); i++) {
						data = records.get(i).split(" ");
						line = records.get(i);
						if (table.evaluateExpression(condition, data[columnNumber])) {
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
		
		try {
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
		} catch (Exception e) {
			return comparisonItem.equals(conditionItems[2]);
			
		}

	}

	
	public void writePrimaryKeyToFile(String insertedString) throws FileNotFoundException, IOException {
		File file = new File("PrimaryKeys.txt");
		ArrayList<String> records = new ArrayList<>();
		String line;
		
		try (RandomAccessFile raf = new RandomAccessFile(file, "rw")) {
			raf.seek(0);
			while ((line = raf.readLine()) != null) {
				records.add(line);
			}
			
			raf.seek(raf.length());
			if (!records.contains((insertedString + ".txt" + System.lineSeparator()))) {
				raf.writeBytes(insertedString + ".txt" + System.lineSeparator()); 
			}
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
		File[] files = folder.listFiles((dir, name) -> name.endsWith(".txt") && !name.equals("All.txt"));
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
	
	//  DELETE TableName WHERE Age > 16 AND Year = 2005;
	public void deleteRecord(String insertedString, String condition, Table table) throws FileNotFoundException, IOException {
		String[] conditionItems = condition.split(" ");
		ArrayList<String> getItems = new ArrayList<>(); 
		ArrayList<String> records = new ArrayList<>();
		File file = new File(path + insertedString);
		Boolean contains = false;
		String line;
		
		if (!file.exists()) {
			System.out.println("File does not exist");
			return;
		}
		
		if (!condition.equals("")) {
			for (int i = 0; i < conditionItems.length; i++) {
			    conditionItems[i] = conditionItems[i].trim();
			}
			getItems = table.getItems(conditionItems, path + insertedString, table);
			
			if (getItems.isEmpty()) {
				System.out.println("No records match the condition");
				return;
			}
			
			try (RandomAccessFile raf = new RandomAccessFile(path + insertedString, "rw")) {
				raf.seek(0);
				
				while ((line = raf.readLine()) != null) {
					records.add(line);
				}
				
				raf.setLength(0);
				raf.seek(0);
				
				//  Dog, Cat, Bird, Lion, Wolf
				//  Cat, Lion
				
				for (int i=0; i<records.size(); i++) {
					for (int j=0; j<getItems.size(); j++) {
						if (getItems.get(j).equals(records.get(i))) {
							contains = true;
						}
					}
					if (!contains) {
						raf.writeBytes(records.get(i) + System.lineSeparator());
					}
					contains = false;
				}
			}
		} else {
			try (RandomAccessFile raf = new RandomAccessFile(path + insertedString, "rw")) {
				raf.setLength(0);
				raf.close();
				file.delete();
			}
		}
	}
	
	//  RENAME TableName(one,two,three);
	public void renameAttributes(String insertedString, String[] attributes) throws FileNotFoundException, IOException {
		ArrayList<String> records = new ArrayList<>();
		File file = new File(path + insertedString);
		String line;
		String[] attributeParts;
		String[] parts;
		
		if (!file.exists()) {
			System.out.println("File does not exist");
			return;
		}
		
		try (RandomAccessFile raf = new RandomAccessFile(path + insertedString, "rw")) {
			while ((line = raf.readLine()) != null) {
				records.add(line);
			}
			
			raf.setLength(0);
			raf.seek(0);
			
			// {ID=INTEGER, Name=TEXT, GPA=FLOAT}
			
			attributeParts = records.get(0).replace("{", "").replace("}", "").trim().split(", ");
			raf.writeBytes("{");
			
			if (attributes.length != attributeParts.length) {
				System.out.println("Invalid attribute count");
				return;
			}

			for (int j=0; j<attributeParts.length; j++) {
				parts = attributeParts[j].split("=");
				if (j == attributeParts.length-1) {
					raf.writeBytes(attributes[j] + "=" + parts[1] + "}" + System.lineSeparator());
				} else {
					raf.writeBytes(attributes[j] + "=" + parts[1] + ", ");
				}
			}
			
			
			for (int i=1; i<records.size(); i++) {
				raf.writeBytes(records.get(i) + System.lineSeparator());
			}
		}
	}
	
//	UPDATE TableName SET AttrName = Constant [,AttrName = Constant] * [WHERE Condition] ‘;’
//  UPDATE Students SET Name = Marshall WHERE Name = Charlie; 
//  TableName, [AttrName,Constant,Condition];
	
	public void updateTable(String insertedString, String[] data, Table table) throws FileNotFoundException, IOException {
		String attrName = data[0];
		String constant = data[1];
		String condition = data[2];
		String[] conditionItems = condition.split(" ");
		String[] dataTypes;
		int columnNumber = -1;
		String line;
		ArrayList<String> records = new ArrayList<>();
		ArrayList<String> getItems = new ArrayList<>();
		File file = new File(path + insertedString);
		 
		if (!file.exists()) {
			System.out.println("Field does not exist");
			return;
		}
		
		try (RandomAccessFile raf = new RandomAccessFile(file, "rw")) {
			while ((line = raf.readLine()) != null) {
				records.add(line);
			}
			
			getItems = table.getItems(conditionItems, path + insertedString, table);
			dataTypes = records.get(0).replace("}", "").replace("{", "").split(", ");
			
			for (int i=0; i<dataTypes.length; i++) {
				if (dataTypes[i].contains(attrName)) {
					columnNumber = i;
					
					if (dataTypes[i].trim().contains("INTEGER")) {
			            if (!constant.matches("^-?\\d+$")) {
			                System.out.println("❌ Invalid INTEGER: " + constant);
			                return;
			            }
			        } else if (dataTypes[i].trim().contains("FLOAT")) {
			            if (!constant.matches("^-?\\d+(\\.\\d+)?$")) {
			                System.out.println("❌ Invalid FLOAT: " + constant);
			                return;
			            }
			        } else if (dataTypes[i].trim().contains("TEXT")) {
			            if (constant.matches("^\\d+$")) {
			                System.out.println("❌ Invalid TEXT: " + constant);
			                return;
			            }
			        } else {
			            System.out.println("❌ Unknown data type: " + dataTypes[i]);
			            return;
			        }
				}
			}
			
			if (columnNumber == -1) {
				System.out.println("Column does not exist");
				return;
			}
			
			String check = table.checkIsPrimary(path + insertedString.replace(".txt", ""));
			
			if (check.equals("isPrimary") && columnNumber == 0) {
				System.out.println("Not allowed to update primary key values");
				return;
			}
			
			//{Name=TEXT, Age=INTEGER}
			
			raf.setLength(0);
			raf.seek(0);
			
			for (int i=0; i<records.size(); i++) {
				if (!getItems.contains(records.get(i))) {
					raf.writeBytes(records.get(i) + System.lineSeparator());
				} else {
					String[] recordParts = records.get(i).split(" ");
					recordParts[columnNumber] = constant;
					line = "";
					for (int j=0; j<recordParts.length; j++) {
						if (j==recordParts.length-1) {
							line += recordParts[j];
						} else {
							line += recordParts[j] + " ";
						}
					}
					raf.writeBytes(line + System.lineSeparator());
				}
			}
			
		}
	}
	
	public ArrayList<String> selectForLet(ArrayList<String> items, String insertedString, String condition, Table table, BinarySearchTree bst) throws IOException {
		File insertedFile = new File(insertedString);
		ArrayList<String> returnItems = new ArrayList<>(); 
		ArrayList<String> getItems = new ArrayList<>();
		ArrayList<String> selectItems = new ArrayList<>();
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
					
//					System.out.println("📌 Table Schema: " + line);
//					System.out.println("📋 Table Data:");
					
					selectItems.add(line + System.lineSeparator());
					
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
//							System.out.println(addSpaces);
							selectItems.add(addSpaces + System.lineSeparator());
						}
						return selectItems;
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
//							System.out.println(addSpaces);
							selectItems.add(addSpaces + System.lineSeparator());
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
					
//					System.out.print("📌 Table Schema: {");
					String schemaLine = "";

					for (int i=0; i<columnNumbers.size(); i++) {
						if (i == columnNumbers.size()-1) {
//							System.out.print(tableItems[columnNumbers.get(i)].trim() + "}\n");
							schemaLine += tableItems[columnNumbers.get(i)].trim() + "}\n";
						} else {
//							System.out.print(tableItems[columnNumbers.get(i)].trim() + ", ");
							schemaLine += tableItems[columnNumbers.get(i)].trim() + ", ";
						}	
					}
					
					selectItems.add(schemaLine + System.lineSeparator());
					
//					System.out.println("📋 Table Data:");
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
//						            System.out.print(dataParts[colIndex] + "\n");
						            returnItems.add(dataParts[colIndex]);
						        } else {
						            addSpaces = dataParts[colIndex];  // ✅ Get correct column value 
						            returnItems.add(dataParts[colIndex]);
						            // ✅ Add spacing for alignment
						            for (int j = dataParts[colIndex].length(); j < 15; j++) { 
						                addSpaces += " ";
						            }

//						            System.out.print(addSpaces + " ");
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
//						            System.out.print(dataParts[colIndex] + "\n");
						            returnItems.add(dataParts[colIndex]);
						        } else {
						            addSpaces = dataParts[colIndex];  // ✅ Get correct column value 
						            returnItems.add(dataParts[colIndex]);
						            // ✅ Add spacing for alignment
						            for (int j = dataParts[colIndex].length(); j < 15; j++) { 
						                addSpaces += " ";
						            }

//						            System.out.print(addSpaces + " ");
						        }
						    }
						}
						
						if (count == 0) {
							System.out.println("Table is empty");
						}
					}
					
					for (String item : returnItems) {
						selectItems.add(item + System.lineSeparator());
					}
					return selectItems;
				}
			}
			
			
		}
	}
	
	public void letFunction(ArrayList<String> items, String insertedString, String condition, Table table, BinarySearchTree bst, ArrayList<String> nameID) throws FileNotFoundException, IOException {
		File testFile = new File(path + nameID.get(0) + ".txt");
		ArrayList<String> selectItems = table.selectForLet(items, insertedString, condition, table, bst);
		ArrayList<String> holdItems = new ArrayList<>();
		String[] parts;
		String line = "";
		int columnNumber = -1;
		int count;
		int iPlaceholder;
		
		holdItems.add(selectItems.get(0).trim());
		
		if ((count = selectItems.get(0).split(", ").length) > 1) {
			for (int i=1; i<selectItems.size(); i=i+count) {
				line = "";
				iPlaceholder = i;
				for (int j=0; j<count; j++) {
					if (j == count-1) {
						line += selectItems.get(iPlaceholder).trim().replace("\\s+", "");
						iPlaceholder++;
						holdItems.add(line);
					} else {
						line += selectItems.get(iPlaceholder).trim().replace("\\s+", "") + " ";
						iPlaceholder++;
					}
				}
			}
			selectItems = new ArrayList<>(holdItems);
		}
		
		try (RandomAccessFile raf = new RandomAccessFile(testFile, "rw")) {
			
			parts = selectItems.get(0).trim().split(", ");
			
			for (int i=0; i<parts.length; i++) {
				if (parts[i].contains(nameID.get(1))) {
					if (!parts[i].trim().contains("INTEGER")) {
						System.out.println("Key must be of value INTEGER");
			            return;
			        } 
				}
			}
			
			if (!items.contains(nameID.get(1)) && !items.contains("*")) {
				System.out.println("Key must be in the table");
				return;
			}
			
			raf.setLength(0); 
			raf.seek(0);
			
			for (int i=0; i<selectItems.size(); i++) {
				line = selectItems.get(i).trim().replaceAll("\\s+", " ");
				parts = line.split(" ");
				if (i==0) {
					for (int j=0; j<parts.length; j++) {
						if (parts[j].contains(nameID.get(1))) {
							columnNumber = j;
						}
					}
					if (columnNumber == -1) {
						System.out.println("Key doesn't exist in table");
						return;
					}
					
					
					parts = selectItems.get(i).replaceAll("\\s+", " ").replace("{", "").replace("}", "").trim().split(", ");
					line = parts[columnNumber];
					
					raf.writeBytes("{" + line);
					
					for (int j=0; j<parts.length; j++) {
						if (!(j==columnNumber)) {
							raf.writeBytes(", " + parts[j]);
						}
					}
					raf.writeBytes("}" + System.lineSeparator());
					continue;
				}
				
				line = parts[columnNumber];
				
				raf.writeBytes(line);
				
				for (int j=0; j<parts.length; j++) {
					if (!(j==columnNumber)) {
						raf.writeBytes(" " + parts[j]);
					}
				}
				
				raf.writeBytes(System.lineSeparator());
			}
			
//			int count = -1;
//			for (String select : selectItems) {
//				if (count==-0 && !select.contains(nameID.get(1))) {
//					System.out.println("Key must be in the first column of the table");
//					return;
//				}
//				if (count==-1) {
//					raf.writeBytes("{" + select.trim().replaceAll("\\s+", " ").replace("{", "").replace("}", "") + "}" + System.lineSeparator());
//					continue;
//				}
//				line = select.trim().replaceAll("\\s+", " ");
//				raf.writeBytes(select.trim().replaceAll("\\s+", " ") + System.lineSeparator());
//				count++;
//			}
			
			table.writePrimaryKeyToFile(path + nameID.get(0).replace(".txt", ""));
		}
	}
	
	public void selectMultipleTables(ArrayList<String> items, String condition, Table table) throws FileNotFoundException, IOException {
		ArrayList<String> columnNames = new ArrayList<>();
		ArrayList<String> table1items = new ArrayList<>();
		ArrayList<String> table2items = new ArrayList<>();
		ArrayList<String> columns = new ArrayList<>();
		ArrayList<String> columnData = new ArrayList<>();
		ArrayList<Integer> columnNumbers = new ArrayList<>();
		ArrayList<String> conditionColumns = new ArrayList<>();
		ArrayList<Integer> conditionColumnNumbers = new ArrayList<>();
		ArrayList<String> output = new ArrayList<>();
		String[] parts;
		String[] parts2;
		String operator = "";
		
		
		if (!condition.equals("")) {
			
			List<String> operators = List.of(">", "<", ">=", "<=", "=", "!=");
			
			for (String op : operators) {
				if (condition.contains(op)) {
					operator = op;
					break;
				}
			}
			
			parts = condition.trim().split(operator);
			if (parts.length != 2) {
			    System.out.println("❌ Split failed. Operator: " + operator);
			    System.out.println("Condition: " + condition);
			    return;
			}
			
			conditionColumns.add(parts[0].trim().split("\\.")[1]);
			conditionColumns.add(parts[1].trim().split("\\.")[1]);
		}
		
		for (String item : items) {
			if (item.contains(".")) {
				columnNames.add(item.trim());
			}
		}
		
		for (int i=0; i<columnNames.size(); i++) {
			String tableName = columnNames.get(i).split("\\.")[0];
			String tableColumn = columnNames.get(i).split("\\.")[1]; 
			
			columns.add(tableColumn);
			
			File file = new File(path + tableName + ".txt");
			
			try (RandomAccessFile raf = new RandomAccessFile(file, "rw")) {
				String line;
				
				parts = raf.readLine().trim().split(", ");
				
				for (int j=0; j<parts.length; j++) {
					if (parts[j].contains(conditionColumns.get(i))) {
						conditionColumnNumbers.add(j);
					}
					
					if (parts[j].contains(columns.get(i))) {
						columnData.add(parts[j]);
					}
					
					if (parts[j].contains(tableColumn)) {
						columnNumbers.add(j);
					}
				}
				
				if (columnNumbers.isEmpty()) {
					System.out.println("Column does not exist");
					return;
				}
				
				if (table1items.isEmpty()) {
					while ((line = raf.readLine()) != null) {
						table1items.add(line.trim());
					}
				} else {
					while ((line = raf.readLine()) != null) {
						table2items.add(line.trim());
					}
				}
				
				// Done adding items from both tables
					
			}
		}
		
		// evaluateExpression(String[] conditionItems, String comparisonItem)
		String[] arr = new String[3];
		arr[1] = operator;
		String addSpaces = "";

		
		for (int i=0; i<table1items.size(); i++) {
			parts = table1items.get(i).trim().split(" ");
			for (int j=0; j<table2items.size(); j++) {
				 parts2 = table2items.get(j).trim().split(" ");
				 arr[0] = parts[conditionColumnNumbers.get(0)];
				 arr[2] = parts2[conditionColumnNumbers.get(1)];
				
				 if (evaluateExpression(arr, arr[0])) {
					 
					 addSpaces += parts[columnNumbers.get(0)];
					 
					 for (int k=parts[columnNumbers.get(0)].length(); k<15; k++) {
						 addSpaces += " ";
					 }
					 
					 addSpaces += parts2[columnNumbers.get(1)];
					 
					 for (int k=parts2[columnNumbers.get(1)].length(); k<15; k++) {
						 addSpaces += " ";
					 }
					 
					 output.add(addSpaces);
					 addSpaces = "";
				 }
			}
		}
		
		if (output.isEmpty()) {
			System.out.println("No items match the condition");
			return;
		}
		
		System.out.print("📌 Table Schema: {" );
		
		for (int i=0; i<columnData.size(); i++) {
			if (i == columnData.size()-1) {
				System.out.print(columnData.get(i).replace("{", "").replace("}", "") + "}\n");
			} else {
				System.out.print(columnData.get(i).replace("{", "").replace("}", "") + ", ");
			}
		}
		
		System.out.println("📋 Table Data:");
		
		for (String line : output) {
			System.out.println(line);
		}
		
	}

}
