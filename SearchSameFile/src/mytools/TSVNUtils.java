package mytools;

import java.io.File;
import java.net.MalformedURLException;

import org.apache.tools.ant.*;
import org.apache.tools.ant.taskdefs.Mkdir;
import org.apache.tools.ant.taskdefs.Taskdef;
import org.apache.tools.ant.types.FileSet;
import org.apache.tools.ant.types.Path;
import org.tigris.subversion.svnant.*;
import org.tigris.subversion.svnant.commands.*;
import org.tigris.subversion.svnclientadapter.SVNUrl;

/**
 *
 */
public class TSVNUtils {
	final static String svnURL = "http://localhost/svn/TongTest";
	  /**
	   * Set to false to use command line client interface instead of JNI JavaHL binding.  
	   * Defaults to true
	   */
	final static private boolean _javahl = true;
	  
	  /**
	   * Set to false to use command line client interface instead of JavaSVN binding.
	   * Defaults to true
	   */
	final static private boolean _javasvn = true;
	  
	  /**
	   * formatter definition used to format/parse dates (e.g. when revision is specified as date).
	   */
	final static  private String _dateFormatter = null;
	  
	  /**
	   * time zone used to format/parse dates (e.g. when revision is specified as date).
	   */
	final static private String _dateTimeZone = null;
	
	final static String user = "liubt";
	
	final static String passsword = "liubt"; 
	  
	private static SvnTask createSvnTask() {
		Project project = TFileUtils.getFileUtils().getMyProject();

		SvnTask svnTask = new SvnTask();
		svnTask.setProject(project);
	    svnTask.setJavahl(_javahl);
	    svnTask.setJavasvn(_javasvn);
	    if (_dateFormatter != null) {
	      svnTask.setDateFormatter(_dateFormatter);
	    }
	    
	    if (_dateTimeZone != null) {
	      svnTask.setDateTimezone(_dateTimeZone);
	    }
        svnTask.setUsername(user);
        svnTask.setPassword(passsword);
        return svnTask;
	}
	/** Creates a new instance of testAnt */
	public static void delete(String file) {
		Project project = TFileUtils.getFileUtils().getMyProject();
		
		SvnTask svnTask = createSvnTask();
		Delete mydelete = new Delete();
		mydelete.setProject(project);
		mydelete.setTask(svnTask);
		mydelete.setFile(new File(file));
		mydelete.setMessage("hello...");
		
		svnTask.addDelete(mydelete);
		svnTask.execute();
	}

	public static void commit(String dir) {
		Project project = TFileUtils.getFileUtils().getMyProject();
		
		SvnTask svnTask = createSvnTask();
		Commit myCommit = new Commit();
		myCommit.setProject(project);
		myCommit.setTask(svnTask);
		myCommit.setDir(new File(dir));
		myCommit.setMessage("hello...");
		
		svnTask.addCommit(myCommit);
		svnTask.execute();
	}

	/*
  protected void update(Workspace workspace, TeamProjectDescription projectDescription) throws VcsException {
    Assert.notNull(workspace);
    Assert.notNull(projectDescription);
    Assert.assertTrue(projectDescription instanceof SvnTeamProjectDescription, "ProjectDescription must be a SvnTeamProjectDescription");

    SvnTeamProjectDescription svnTeamProjectDescription = (SvnTeamProjectDescription)projectDescription;

    SvnTask task = createSvnTask(workspace, svnTeamProjectDescription);
    
    Update update = new Update();
    update.setProject(getAntProject());
    update.setTask(task);
    File destPath = new File(workspace.getAbsolutePath(), svnTeamProjectDescription.getProjectName());
    A4ELogging.debug("Setting dest path for project '%s' to '%s'", new Object[]{svnTeamProjectDescription.getProjectName(),destPath});
    update.setDir(destPath);
    update.setRevision(svnTeamProjectDescription.getRevision());
    task.addUpdate(update);
    
    try {
      task.execute();
    } catch (Exception ex) {
      throw new VcsException("Could not execute 'update': " + ex, ex);
    }
 }
 	 */
	
	static public void main(String[] args) {
		//TSVNUtils.delete("E:/dev/tools/SearchSameFile/temp/work/B.java");
		TSVNUtils.commit("E:/dev/tools/SearchSameFile/temp/work");
	}
}