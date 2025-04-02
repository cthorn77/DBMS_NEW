import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Scanner;

public class UserInterface {
	private Scanner kb;
	private String[] command;
	private Table table;
	private Database db;
	private String inUse;
	private String tableName;
	private File file;
	private ArrayList<String> items;
	private String condition = "";
	private BinarySearchTree bst = new BinarySearchTree();
	public UserInterface(Scanner kb) {
		this.kb = kb;
		this.inUse = "";
	}
	
	@SuppressWarnings("unlikely-arg-type")
	public void start() throws IOException {
		System.out.println("Enter a command");
		Boolean run = true;
		
		while (run) {
			String choice = kb.nextLine();
			switch(parseString(choice)) {
			case "CREATE_DATABASE":
				command = choice.replace(";", "").split(" ");
				db = new Database(command[2]);
				break;
				
			case "USE_DATABASE":
				command = choice.replace(";", "").split(" ");
				file = new File(command[1]);
				
				if (!file.exists()) {
					System.out.println("Database doesn't exist");
					break;
				}
				
				inUse = command[1] + "/";
				System.out.println("Database in use");
				break;
				
			case "CREATE":
				if (inUse.equals("")) {
					System.out.println("Must select a database");
				} else {
					command = choice.split("\\(");
					command = command[0].split(" ");
					
					table = new Table(command[2], inUse);
					table.assignColumns(choice);
					table.writeColumnsToFile();
					System.out.println("TABLE CREATED");
				}
				
				break;
				
			case "CREATE_PRIMARY":
				if (inUse.equals("")) {
					System.out.println("Must select a database");
				} else {
					command = choice.split("\\(");
					command = command[0].split(" ");
					
					table = new Table(command[2], inUse);
					System.out.println("Primary set!");
					
					table = new Table(command[2], inUse);
					table.assignColumns(choice);
					table.writeColumnsToFile();
					table.writePrimaryKeyToFile(inUse + command[2]);
					System.out.println("TABLE CREATED");
				}
				
				break;
			
			case "INSERT":
				if (inUse.equals("")) {
					System.out.println("Must select a database");
				} else {
					command = choice.split("\\(");
					String intoFile = command[0].split(" ")[2];
					String[] values = command[2].replace(");", "").split(",");
					Boolean isValid = true;
					Table table = new Table(intoFile, inUse);
					String[] dataTypes = table.readDataTypes(inUse + intoFile + ".txt");

					if (dataTypes.length == 0) {
					    System.out.println("❌ Data types array is empty!");
					    isValid = false;
					}

					try {
					    for (int i = 0, j = 0; i < values.length && j < dataTypes.length; i++, j++) {
					        values[i] = values[i].trim(); // ✅ Trim space

					        if (dataTypes[j].trim().equals("INTEGER")) {
					            if (!values[i].matches("^-?\\d+$")) {
					                System.out.println("❌ Invalid INTEGER: " + values[i]);
					                isValid = false;
					                break;
					            }
					        } else if (dataTypes[j].trim().equals("FLOAT")) {
					            if (!values[i].matches("^-?\\d+(\\.\\d+)?$")) {
					                System.out.println("❌ Invalid FLOAT: " + values[i]);
					                isValid = false;
					                break;
					            }
					        } else if (dataTypes[j].trim().equals("TEXT")) {
					            if (values[i].matches("^\\d+$")) {
					                System.out.println("❌ Invalid TEXT: " + values[i]);
					                isValid = false;
					                break;
					            }
					        } else {
					            System.out.println("❌ Unknown data type: " + dataTypes[j]);
					            isValid = false;
					        }
					    }
					} catch (Exception e) {
					    System.out.println("❌ Error gathering data types: " + e.getMessage());
					    e.printStackTrace();
					    isValid = false;
					}

					if (!isValid) {
					    System.out.println("❌ INSERT failed due to invalid data.");
					} else {
					    System.out.println("✅ INSERT successful!");
					    table.writeRecordsToFile(inUse + intoFile + ".txt", values, table, bst);
					}
				}
				
				break;
				
			case "SELECT_FROM":
				
				if (inUse.equals("")) {
					System.out.println("Must select a database");
				} else {
					condition = "";
					command = choice.replace(";", "").split(" ");
					items = new ArrayList<>();
					
					
					//SELECT * FROM Students WHERE Age > 16;
					if (choice.contains("WHERE")) {
						command = choice.split("WHERE");
						//Age > 16
						condition = command[1].trim().replace(";", "");
						command = command[0].trim().split(" ");
						
					}
					
					for (String item : command) {
						if (!item.equals("SELECT") && !item.equals("FROM")) {
							items.add(item);
						}
					}
					
					tableName = items.get(items.size()-1);
					table = new Table(tableName, inUse);
					
					table.selectItems(items, inUse + tableName + ".txt", condition, table, bst);
				}
				
				break;
			
			//SELECT Age, Name FROM Students;
			//SELECT Age, Name FROM Students WHERE Age > 16;
			case "SELECT_MULTIPLE_FROM":
				
				if (inUse.equals("")) {
					System.out.println("Must select a database");
				} else {
					command = choice.replace(";", "").split(" ");
					items = new ArrayList<>();
					
					//SELECT * FROM Students WHERE Age > 16;
					if (choice.contains("WHERE")) {
						command = choice.split("WHERE");
						//Age > 16
						condition = command[1].trim().replace(";", "");
						command = command[0].trim().split(" ");
						
					}
					
					for (int i=1; i<command.length; i++) {
						if (!command[i].trim().equals("FROM")) {
							items.add(command[i].replace(",", "").trim());
						} else {
							continue;
						}
					}
					
					tableName = items.get(items.size()-1);
					table = new Table(tableName, inUse);
					
					table.selectItems(items, inUse + tableName + ".txt", condition, table, bst);
				}
				
				break;
			
			case "DESCRIBE":
				if (inUse.equals("")) {
					System.out.println("Must select a database");
				} else {
					command = choice.replace(";", "").split(" ");
					table = new Table(command[1], inUse);
					table.describeTables(command[1], table);
				}
				
				break;
				
			case "DELETE":
				if (inUse.equals("")) {
					System.out.println("Must select a database");
				} else {
					command = choice.replace(";", "").split(" ");
					
					if (choice.contains("WHERE")) {
						command = choice.split("WHERE");
						condition = command[1].trim().replace(";", "");
						command = command[0].trim().split(" ");
						
					}
					
					table = new Table(command[1], inUse);
					table.deleteRecord(command[1] + ".txt", condition, table);
				}
				
				break;
				
			case "RENAME":
				if (inUse.equals("")) {
					System.out.println("Must select a database");
				} else {
					command = choice.replace(";", "").replace(")", "").split("\\(");
					tableName = command[0].split(" ")[1];
					command = command[1].split(",");
					
					table = new Table(tableName, inUse);
					table.renameAttributes(tableName + ".txt", command);
				}
				
				break;
				
			case "EXIT":
				run = false;
				break;

			default:
				System.out.println("Error: Please enter a proper command.");
			}
		}
	}
	
	//SELECT * FROM Students WHERE Age > 16;
	private static String parseString(String choice) {
	    String[] selectCommand = choice.split(" ");
	    String[] parts = choice.split("WHERE");
	    String[] partsBeforeWhere = parts[0].split(" ");

	    if (choice.startsWith("CREATE DATABASE") && selectCommand.length == 3 && choice.endsWith(";")){
	    	return "CREATE_DATABASE";
	    }
	    else if (choice.startsWith("CREATE TABLE") && !selectCommand[4].contains("PRIMARY") && choice.endsWith(";")) {
	        return "CREATE";
	    } 
	    else if (choice.startsWith("CREATE TABLE") && selectCommand[4].contains("PRIMARY") && selectCommand[3].equals("INTEGER") && choice.endsWith(";")) {
	    	return "CREATE_PRIMARY";
	    }
	    else if (choice.startsWith("INSERT INTO") && choice.contains("VALUES") && choice.endsWith(";")) {
	        return "INSERT";
	    } 
	    else if (choice.equals("EXIT")) {
	        return "EXIT";
	    } 
	    // ✅ Fixing SELECT parsing
	    else if (selectCommand[0].equals("SELECT") && selectCommand[2].equals("FROM") && partsBeforeWhere.length == 4 && choice.endsWith(";")) {
	        return "SELECT_FROM";
	    }
	    else if (selectCommand[0].equals("SELECT") && selectCommand[1].contains(",") && selectCommand.length > 4 && choice.endsWith(";")) {
	    	return "SELECT_MULTIPLE_FROM";
	    }
	    else if (selectCommand[0].equals("USE") && selectCommand.length == 2 && choice.endsWith(";")) {
	    	return "USE_DATABASE";
	    }
	    else if (selectCommand[0].equals("DESCRIBE") && selectCommand.length == 2 && choice.endsWith(";")) {
	    	return "DESCRIBE";
	    }
	    else if (selectCommand[0].equals("DELETE") && selectCommand.length > 1 && choice.endsWith(";")) {
	    	return "DELETE";
	    }
	    else if (selectCommand[0].equals("RENAME") && choice.endsWith(";")) {
	    	return "RENAME";
	    }

	    return "invalid";
	}

}
