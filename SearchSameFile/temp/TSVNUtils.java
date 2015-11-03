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
	final static String svnURL = "http://x5.com/repo/branches/B2006_1_1/equicom-admin-common-web";

	/** Creates a new instance of testAnt */
	public static void delete(String destPath) {
		Project project = TFileUtils.getFileUtils().getMyProject();

		Target checkoutTask = new Target();
		checkoutTask.setName("checkout");
		project.addTarget(checkoutTask);
		Taskdef td = new Taskdef();
		//td.setName("useSvn");
		td.setResource("org.tigris.subversion.svnant.SvnTask");
		//td.setClassname("org.tigris.subversion.svnant.SvnTask");

		Checkout co = new Checkout();
		co.setProject(project);
		SVNUrl urlS = null;
		try {
			urlS = new SVNUrl(svnURL);
		} catch (MalformedURLException e1) {
			e1.printStackTrace();
		}
		co.setUrl(urlS);
		File f = new File(destPath);//Ä¿±êÂ·¾¶
		co.setDestpath(f);
		td.setProject(project);
		td.execute();
	}
}