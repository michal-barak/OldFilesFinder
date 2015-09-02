import java.util.ArrayList;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileOwnerAttributeView;
import java.nio.file.attribute.UserPrincipal;

/**
 * this is a program to find old unused file, over a defined size, given a root directory for unix systems.
 *   this can either find or delete the files
 */

/**
 * @author Michal Barak
 * date: 1-9-2015
 * 
 *
 */
public class OldFilesFinder {

	/**
	 * @param dir  -the initial dir to look into
	 * @param del - if to delete the files
	 * @param size - the size of files to look for 
	 * @param date - the number of days the files are old (minimum)	 * 
	 * @param outfile - list of files
	 */
	
	
	static boolean files_del = false;
	static Long files_size  = 1000000L;
	static Long files_date = 356L;
	static Long files_millis;
	private String user_name;
	
	public static void main(String[] args) {
		
		if (args.length != 5)
		{
			System.out.println("Usage: OldFilesFinder dir delete(true/false) size(in bytes) date(in days) log_file"); 
			return;
		}
		String dir = args[0];
		Path path = Paths.get(dir);
		files_del = Boolean.valueOf(args[1]);
		files_size  = Long.valueOf(args[2]);
		files_date = Long.valueOf(args[3]);
		files_millis = files_date * 24 * 60 * 60 *1000;
		
		OldFilesFinder off = new OldFilesFinder();
		off.findFilesInDir(path, files_del);
		
		
		off.writeList(args[4]);

	}
	private void writeList(String outFileName) {
		
		BufferedWriter bw = null;
		try
		{						
			bw = new BufferedWriter(new FileWriter(outFileName));				
			for (String fileName : fileList){
				bw.write(fileName+ "\n"); 
			}				
			
		}catch(IOException e){			
			System.out.println("error: " + e.getMessage());
			
		}finally{			
			try{bw.close();}catch(Exception e){
				System.out.println("final error: " + e.getMessage());
			}
			
		}
		
	}
	ArrayList<String> fileList = new ArrayList<String>();
	
	
	
	private void findFilesInDir(Path dir, Boolean Delete){
		user_name = System.getProperty("user.name");
		//System.out.println(user_name);
		
		try {
			if (Delete){
				DeleteFile delFile = new DeleteFile();
				Files.walkFileTree(dir, delFile);
			}
			else
			{
				ListFile listFile = new ListFile();
				
					Files.walkFileTree(dir, listFile);
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
public static Boolean isFileOld(BasicFileAttributes attributes){
	if (attributes.isDirectory())
		return false;
	if (attributes.isSymbolicLink())
		return false;
	if (!attributes.isRegularFile())
		return false;
	if (attributes.size() < files_size)
		return false;
	if ((attributes.lastAccessTime().toMillis() < files_millis) || (attributes.creationTime().toMillis() < files_millis) || (attributes.lastModifiedTime().toMillis() < files_millis))
		return false;	
	
	return true;			
	}
	
	class ListFile extends SimpleFileVisitor<Path>{
		@Override
		  public FileVisitResult visitFile(Path file, BasicFileAttributes attributes) {
			if (isFileOld(attributes)){
				fileList.add(file.toString() + "\t" + String.valueOf(attributes.size()));
			}
		    return FileVisitResult.CONTINUE;
		  }
	}
	
	class DeleteFile extends SimpleFileVisitor<Path>{
		@Override
		  public FileVisitResult visitFile(Path file, BasicFileAttributes attributes) {
			if (isFileOld(attributes)){
				fileList.add(file.toString() + "\t" + String.valueOf(attributes.size()));
				try {					
					FileOwnerAttributeView view = Files.getFileAttributeView(file,
					        FileOwnerAttributeView.class);
					UserPrincipal userPrincipal = view.getOwner();
					//System.out.println(userPrincipal.getName() + "\t" + user_name);
					if (user_name.compareTo(userPrincipal.getName()) == 0)
						Files.delete(file);
				} catch (IOException e) {					
					e.printStackTrace();
				}
			}
		    return FileVisitResult.CONTINUE;
		  }
	}
	
	
}
