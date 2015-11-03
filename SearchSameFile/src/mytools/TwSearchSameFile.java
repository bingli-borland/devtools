package mytools;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Properties;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.PathTokenizer;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.taskdefs.Copy;
import org.apache.tools.ant.types.FileSet;

class TwClassInfo implements Serializable {
	private static final long serialVersionUID = 1L;
	String realClassName; //仅仅是单纯的类名
	String packageName; //包名+类名
	String realPath; //文件的路径, 包括文件名称.
}

class TwCopyFiles extends Task {
    
    private File todir;
    private String files;

    public void execute() throws BuildException {
            
            PathTokenizer tokenizer = new PathTokenizer (getFiles ());
            while (tokenizer.hasMoreTokens ()) {
                File f = getProject().resolveFile(tokenizer.nextToken());
                Copy cp = (Copy) getProject ().createTask ("copy");
                cp.setTodir (getToDir ());
                if (f.isDirectory ()) {
                    FileSet fset = new FileSet ();
                    fset.setDir (f);
                    cp.addFileset (fset);
                } else {
                    cp.setFile (f);
                }
                cp.execute ();
            }
    }

    public String getFiles() {
        return this.files;
    }
    
    public void setFiles (String files) {
        this.files = files;
    }
    
    public File getToDir() {
        return this.todir;
    }
    
    public void setToDir (File todir) {
        this.todir = todir;
    }
}

public class TwSearchSameFile {
	final static String[] myWorkDir = { "CDOther", "iiop", "naming", "ejb" };
	final static String mySplit = "%";

	/**
	 * 生成某一个目录下的所有的类新的信息, 放在数组中, 包括类名, 包名+类名, 实际的目录.
	 * 生成所有的完全重复的(包名+类名相同), 格式为 "包名""类名"=实际目录1|实际目录2|实际目录3|...
	 * 删除指定目录下的文件, 目前包括CDOther, iiop, naming, ejb. 
	 * 	  如果其他的非我们开发的目录存在相同的, 如何处理? 暂定记录在文件中, 不处理.
	 *    对于需要删除的文件. 将文件挪到另外一个目录下, 不实际删除, 注意保留原来的目录结构.
	 *  
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		if (args.length != 3) {
			System.out.println("usage: SearchSameFile product目录 新的删除的备份目录 动作(1-生成重复 2-删除)");
			return;
		}

		String productDir = (args[0]);
		String delBackupDir = (args[1]);
		int action = Integer.parseInt(args[2]);

		//将glassfish目录的文件名, 包名, 目录记录下来
		ArrayList<ClassInfo> productAllFile = new ArrayList<ClassInfo>();
		BufferedWriter out = null;
		try {
			// 编目源文件目录, 将package+ClassName--->src dir
			if (action == 1) {
				visitAllFiles(new File(productDir), productAllFile);
				// Serialize to a file
				ObjectOutput seriOut = new ObjectOutputStream(new FileOutputStream("./productAllFile.ser"));
				seriOut.writeObject(productAllFile);
				seriOut.close();
				out = new BufferedWriter(new FileWriter("./sameFileTemp.txt"));
				findSameFile(productAllFile, out);
				out.close();
				Properties props = new Properties();
				props.load(new FileInputStream("./sameFileTemp.txt"));
				Enumeration en = props.propertyNames();
				while (en.hasMoreElements()) {
					String key = (String) en.nextElement();
					String property = props.getProperty(key);
					String[] myClassDirPath = property.split(mySplit);
					System.out.println("dirpath chang=" + myClassDirPath.length);
					if (myClassDirPath.length<=1) {
						props.remove(key);
					}
				}
				OutputStream os = new FileOutputStream("./sameFile.txt"); 
				props.store(os, "");
				return;
			}

			if (action == 2 || action == 3) {
				//BufferedReader inSameFile = new BufferedReader(new FileReader("./sameFile.txt"));
				Properties props = new Properties();
				props.load(new FileInputStream("./sameFile.txt"));
				Enumeration en = props.propertyNames();
				while (en.hasMoreElements()) {
					String key = (String) en.nextElement();
					String property = props.getProperty(key);
					String[] myClassDirPath = property.split(mySplit);
					for (int i=0; i<myClassDirPath.length; i++) {
						String dirPath = myClassDirPath[i];
						for (int j=0; j<myWorkDir.length; j++) {//判断是否在工作目录内
							if (dirPath.startsWith(productDir+"/" + myWorkDir[j])) {
								System.out.println("找到相同的, 删除工作目录下的<" + dirPath + ">");
								//copy到备份目录下, 并且保持原有的目录结构.
								String realDir = dirPath.substring(productDir.length(), dirPath.length());
								realDir = realDir.replace('\\', '/');
								TFileUtils.getFileUtils().copyFile(new File(dirPath), new File(delBackupDir + "/" + realDir));
								//在我的工作目录内, 删除这个文件
								//-------目前不删除原来的文件...TFileUtils.getFileUtils().deleteFile(new File(dirPath));
								if (action == 3)
									TSVNUtils.delete(dirPath);
							}
						}
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	private static void replacePackage(File dir) {
		if (dir.isDirectory()) {
			String[] children = dir.list();
			for (int i = 0; i < children.length; i++) {
				replacePackage(new File(dir, children[i]));
			}
		} else {
			// 将结果放入到hashtable中
			if (!dir.getName().endsWith("java"))
				return;
			try {
				BufferedReader inSameFile = new BufferedReader(new FileReader("./sameFile.txt"));
				do {
					String comment = inSameFile.readLine();//注释
					System.out.println("comment: " + comment);
					if (comment != null && comment.indexOf("***END***") >= 0)
						break;
					if (comment == null)
						continue;
					String myPackage = inSameFile.readLine();//目前目录的包名
					String productPackage = inSameFile.readLine();//产品目录的包名
					inSameFile.readLine();//目前文件的真实目录
					inSameFile.readLine();//产品文件的真实目录
					System.out.println("file: " + dir.getAbsolutePath() + ", 原始: " + myPackage + ", 替换为: " + productPackage);
					Replace.replace(dir.getAbsolutePath(), myPackage, productPackage);
				} while (true);
				inSameFile.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	private static void findSameFile(ArrayList<ClassInfo> productAllFile, BufferedWriter out) throws IOException {
		for (int i = 0; i < productAllFile.size(); i++) {
			ClassInfo productFileInfo = (ClassInfo) productAllFile.get(i);
			out.write(productFileInfo.packageName + "=");
			for (int j = 0; j < productAllFile.size(); j++) {
				ClassInfo compareClassInfo = (ClassInfo) productAllFile.get(j);
				if (compareClassInfo.packageName.equals(productFileInfo.packageName)) {//找到相同的
					out.write(compareClassInfo.realPath + mySplit);
				}
			}
			out.write("\r\n");
		}
	}

	public static void visitAllFiles(File dir, ArrayList result) throws IOException {
		if (dir.isDirectory()) {
			String[] children = dir.list();
			for (int i = 0; i < children.length; i++) {
				visitAllFiles(new File(dir, children[i]), result);
			}
		} else {
			// 将结果放入到hashtable中
			if (!dir.getName().endsWith("java"))
				return;
			BufferedReader in = new BufferedReader(new FileReader(dir));
			String str;
			while ((str = in.readLine()) != null) {
				str = str.trim();
				if (str.startsWith("package ")) {// 找到package
					String packageName = str.substring(str.indexOf("package ") + "package ".length(), str.length() - 1);
					ClassInfo classInfo = new ClassInfo();
					classInfo.realClassName = dir.getName().substring(0, dir.getName().indexOf(".java"));
					classInfo.packageName = packageName + "." + classInfo.realClassName;
					classInfo.realPath = dir.getAbsolutePath();
					classInfo.realPath = classInfo.realPath.replace('\\', '/');
					System.out.println("className:" + classInfo.realClassName + ", packageName: " + classInfo.packageName + ", realPath:" + classInfo.realPath);
					result.add(classInfo);
					break;
				}
			}
		}
	}

}
