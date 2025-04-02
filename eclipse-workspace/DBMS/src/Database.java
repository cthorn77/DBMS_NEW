import java.io.File;

public class Database {
	private String name;
	private File folder;
	
	public Database(String name) {
		this.name = name;
		this.folder = new File(name);
		
		if (!folder.exists()) {
			boolean created = folder.mkdir();
			
			if (created) {
				System.out.println("Database folder '" + name + "' created");
			} else {
				System.out.println("Failed to create the database folder");
			}
		} else {
			System.out.println("Database '" + name + "' already exists");
		}
	}
	
	public String getPath() {
		return folder.getAbsolutePath();
	}
	
	public String getName() {
		return name;
	}
	
}
