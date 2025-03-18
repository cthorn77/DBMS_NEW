import java.io.IOException;
import java.util.Arrays;
import java.util.Scanner;

public class UserInterface {
	private Scanner kb;
	private String[] command;
	private Table table;
	
	public UserInterface(Scanner kb) {
		this.kb = kb;
	}
	
	public void start() throws IOException {
		System.out.println("Enter a command");
		Boolean run = true;
		
		
		while (true) {
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
				
				System.out.println("DEBUG: DataTypes -> " + Arrays.toString(dataTypes));
				System.out.println("DEBUG: Values -> " + Arrays.toString(values));

				if (dataTypes.length == 0) {
				    System.out.println("❌ Data types array is empty!");
				    isValid = false;
				}

				try {
				    for (int i = 0, j = 0; i < values.length && j < dataTypes.length; i++, j++) {
				        values[i] = values[i].trim(); // ✅ Trim spaces

				        System.out.println("Checking -> " + values[i] + " (Expected: " + dataTypes[j] + ")");

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
			
			case "EXIT":
				run = false;
				break;

			default:
				System.out.println("NO!!!");
			}
		}
	}
	
	private static String parseString(String choice) {
		String[] command = choice.split("\\(");
		
		if (command[0].contains("CREATE TABLE") && command[1].charAt(command[1].length()-1) == ';') {
			return "CREATE";
		} else if (command[0].contains("INSERT INTO") && command[2].charAt(command[2].length()-1) == ';' && command[1].contains("VALUES")) {
			return "INSERT";
		}
		
		return "invalid";
	}
}
