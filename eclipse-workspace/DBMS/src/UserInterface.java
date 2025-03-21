import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Scanner;

public class UserInterface {
	private Scanner kb;
	private String[] command;
	private Table table;
	private String tableName;
	private ArrayList<String> items;
	
	public UserInterface(Scanner kb) {
		this.kb = kb;
	}
	
	@SuppressWarnings("unlikely-arg-type")
	public void start() throws IOException {
		System.out.println("Enter a command");
		Boolean run = true;
		
		
		while (run) {
			String choice = kb.nextLine();
			switch(parseString(choice)) {
			case "CREATE":
				command = choice.split("\\(");
				command = command[0].split(" ");
				
				table = new Table(command[2]);
				table.assignColumns(choice);
				table.writeColumnsToFile();
				System.out.println("TABLE CREATED");
				break;
			
			case "INSERT":
				command = choice.split("\\(");
				String intoFile = command[0].split(" ")[2];
				String[] values = command[2].replace(");", "").split(",");
				Boolean isValid = true;
				Table table = new Table(intoFile);
				String[] dataTypes = table.readDataTypes(intoFile + ".txt");

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
				    table.writeRecordsToFile(intoFile + ".txt", values);
				    break;
				}
			
			case "SELECT_FROM":
				command = choice.replace(";", "").split(" ");
				items = new ArrayList<>();
				
				for (String item : command) {
					if (!item.equals("SELECT") && !items.equals("FROM")) {
						items.add(item);
					}
				}
				
				tableName = items.get(items.size()-1);
				table = new Table(tableName);
				
				table.selectItems(items, tableName + ".txt");
				
				break;
			
			case "SELECT_MULTIPLE_FROM":
				command = choice.replace(";", "").split(" ");
				items = new ArrayList<>();
				
				for (int i=1; i<command.length; i++) {
					if (!command[i].trim().equals("FROM")) {
						items.add(command[i].replace(",", "").trim());
					} else {
						continue;
					}
				}
				
				tableName = items.get(items.size()-1);
				table = new Table(tableName);
				
				table.selectItems(items, tableName + ".txt");
				
				break;
				
			case "EXIT":
				run = false;
				break;

			default:
				System.out.println("NO!!!");
			}
		}
	}
	
	private static String parseString(String choice) {
	    String[] selectCommand = choice.split(" ");

	    if (choice.startsWith("CREATE TABLE") && choice.endsWith(";")) {
	        return "CREATE";
	    } else if (choice.startsWith("INSERT INTO") && choice.contains("VALUES") && choice.endsWith(";")) {
	        return "INSERT";
	    } else if (choice.equals("EXIT")) {
	        return "EXIT";
	    } 
	    // ✅ Fixing SELECT parsing
	    else if (selectCommand[0].equals("SELECT") && selectCommand[2].equals("FROM") && selectCommand.length == 4 && choice.endsWith(";")) {
	        return "SELECT_FROM";
	    }
	    else if (selectCommand[0].equals("SELECT") && selectCommand[1].contains(",") && selectCommand.length > 4 && choice.endsWith(";")) {
	    	return "SELECT_MULTIPLE_FROM";
	    }

	    return "invalid";
	}

}
