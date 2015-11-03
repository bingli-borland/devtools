package mytools;

import org.apache.tools.ant.Project;
import org.apache.tools.ant.taskdefs.Copy;
import org.apache.tools.ant.taskdefs.Delete;
import org.apache.tools.ant.types.FileSet;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

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
    
}