package mytools;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import org.apache.tools.ant.Project;
import org.apache.tools.ant.taskdefs.Copy;
import org.apache.tools.ant.taskdefs.Delete;
import org.apache.tools.ant.types.FileSet;

class Replace extends org.apache.tools.ant.taskdefs.Replace {

	public Replace() {
		// dummy ant project
		Project antProject = new Project();
		antProject.init();
		setProject(antProject);
		// dummy ant target
		setOwningTarget(null);
	}
	
	public static void replace(String sourceFile, String token, String value) {
		Replace replace = new Replace();
		replace.setFile(new File(sourceFile));
		replace.setToken(token);
		replace.setValue(value);
		replace.execute();
	}
}

/**
 * Class for utility methods for finding files and
 * information about them.
 */
public class TFileUtils {
	static TFileUtils myFileUtils = null;
	static Project antProject = null; 
	
	private TFileUtils() {
		antProject = new Project();
		antProject.init();
		//setProject(antProject);
		// dummy ant target
		//setOwningTarget(null);		
	}
	
	public static TFileUtils getFileUtils() {
		if (myFileUtils == null) {
			myFileUtils = new TFileUtils();
			return myFileUtils;
		}
			
		return myFileUtils;
	}
	
	public Project getMyProject() {
		return antProject;
	}

    /**
     * This method returns the fully qualified names of
     * class files below the given directory.
     */
    public String [] getClassFileNames(File dir) {
        List<String> names = new ArrayList<String>();
        Stack<String> pathStack = new Stack<String>();
        addClassNames(dir, pathStack, names);
        return names.toArray(new String [names.size()]);
    }

    /**
     * Recursive method for adding class names under
     * a given top directory.
     */
    private void addClassNames(File current, Stack<String> stack,
                                      List<String> names) {

        File[] children = current.listFiles();
        if (children == null) {
            return;
        }
        for (File child : children) {
            if (child.isDirectory()) {
                stack.push(child.getName());
                addClassNames(child, stack, names);
                stack.pop();
            } else {
                if (child.getName().endsWith(".class")) {
                    names.add(createFullName(stack, child));
                }
            }
        }
    }

    /*
    * Create a fully-qualified path name.
    */
    private String createFullName(Stack<String> dirs, File classFile) {
        String className = classFile.getName().substring(
            0, classFile.getName().indexOf(".class"));
        if (dirs.empty()) {
            return className;
        }
        StringBuffer fullName = new StringBuffer();
        for (String dir : dirs) {
            fullName.append(dir);
            fullName.append(".");
        }
        fullName.append(className);
        return fullName.toString();
    }

    /**
     * Recursively delete a directory and all its descendants.
     */
    public void deleteRecursive(File dir) {
        Delete d = new Delete();
        d.setProject(antProject);
        d.setDir(dir);
        d.execute();
    }

    public void deleteFile(File file) {
        Delete d = new Delete();
        d.setProject(antProject);
        d.setFile(file);
        d.execute();
    }

    /**
     * Copies a single file.
     */
    public void copyFile(File src, File dest) {
        Copy cp = new Copy();
        cp.setProject(antProject);
        cp.setFile(src);
        cp.setTofile(dest);
        cp.execute();
    }

    /**
     * Copies a whole directory recursively.
     */
    public void copyDir(File src, File dest) {
        Copy cp = new Copy();
        cp.setProject(antProject);
        cp.setTodir(dest);
        FileSet fs = new FileSet();
        fs.setDir(src);
        cp.addFileset(fs);
        cp.execute();
    }

    public File createTmpDir( boolean scheduleDeleteOnVmExit ) throws IOException {
        // create a temporary directory
        File tmpFile = File.createTempFile("wstest","tmp",new File("."));
        tmpFile.delete();
        tmpFile.mkdir();
        if(scheduleDeleteOnVmExit) {
            tmpFile.deleteOnExit();
        }
        return tmpFile;
    }
    
	public void replace(String sourceFile, String token, String value) {
		Replace replace = new Replace();
		replace.setFile(new File(sourceFile));
		replace.setToken(token);
		replace.setValue(value);
		replace.execute();
	}
	
  public void removeLineFromFile(String file, String lineToRemove) {

    try {

      File inFile = new File(file);
      
      if (!inFile.isFile()) {
        System.out.println("Parameter is not an existing file");
        return;
      }
       
      //Construct the new file that will later be renamed to the original filename.
      File tempFile = new File(inFile.getAbsolutePath() + ".tmp");
      
      BufferedReader br = new BufferedReader(new FileReader(file));
      PrintWriter pw = new PrintWriter(new FileWriter(tempFile));
      
      String line = null;

      //Read from the original file and write to the new
      //unless content matches data to be removed.
      while ((line = br.readLine()) != null) {
        
        if (!line.trim().equals(lineToRemove.trim())) {

          pw.println(line);
          pw.flush();
        }
      }
      pw.close();
      br.close();
      
      //Delete the original file
      if (!inFile.delete()) {
        System.out.println("Could not delete file");
        return;
      }
      
      //Rename the new file to the filename the original file had.
      if (!tempFile.renameTo(inFile))
        System.out.println("Could not rename file");
      
    }
    catch (FileNotFoundException ex) {
      ex.printStackTrace();
    }
    catch (IOException ex) {
      ex.printStackTrace();
    }
  }	
    
}