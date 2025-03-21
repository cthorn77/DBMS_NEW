import java.io.IOException;
import java.util.Scanner;

public class Main {

	public static void main(String[] args) throws IOException {
//		Table table = new Table("Employee");
//		
		
//		table.assignColumns("CREATE TABLE EMPLOYEE(column1_name DATATYPE,column2_name DATATYPE,column3_name DATA_TYPE);");
		
		Scanner kb = new Scanner(System.in);
		UserInterface ui = new UserInterface(kb);
		ui.start();
		
		kb.close();
	}

}
