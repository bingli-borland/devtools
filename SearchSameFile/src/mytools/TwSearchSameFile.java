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
	String realClassName; //�����ǵ���������
	String packageName; //����+����
	String realPath; //�ļ���·��, �����ļ�����.
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
	 * ����ĳһ��Ŀ¼�µ����е����µ���Ϣ, ����������, ��������, ����+����, ʵ�ʵ�Ŀ¼.
	 * �������е���ȫ�ظ���(����+������ͬ), ��ʽΪ "����""����"=ʵ��Ŀ¼1|ʵ��Ŀ¼2|ʵ��Ŀ¼3|...
	 * ɾ��ָ��Ŀ¼�µ��ļ�, Ŀǰ����CDOther, iiop, naming, ejb. 
	 * 	  ��������ķ����ǿ�����Ŀ¼������ͬ��, ��δ���? �ݶ���¼���ļ���, ������.
	 *    ������Ҫɾ�����ļ�. ���ļ�Ų������һ��Ŀ¼��, ��ʵ��ɾ��, ע�Ᵽ��ԭ����Ŀ¼�ṹ.
	 *  
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		if (args.length != 3) {
			System.out.println("usage: SearchSameFile productĿ¼ �µ�ɾ���ı���Ŀ¼ ����(1-�����ظ� 2-ɾ��)");
			return;
		}

		String productDir = (args[0]);
		String delBackupDir = (args[1]);
		int action = Integer.parseInt(args[2]);

		//��glassfishĿ¼���ļ���, ����, Ŀ¼��¼����
		ArrayList<ClassInfo> productAllFile = new ArrayList<ClassInfo>();
		BufferedWriter out = null;
		try {
			// ��ĿԴ�ļ�Ŀ¼, ��package+ClassName--->src dir
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
						for (int j=0; j<myWorkDir.length; j++) {//�ж��Ƿ��ڹ���Ŀ¼��
							if (dirPath.startsWith(productDir+"/" + myWorkDir[j])) {
								System.out.println("�ҵ���ͬ��, ɾ������Ŀ¼�µ�<" + dirPath + ">");
								//copy������Ŀ¼��, ���ұ���ԭ�е�Ŀ¼�ṹ.
								String realDir = dirPath.substring(productDir.length(), dirPath.length());
								realDir = realDir.replace('\\', '/');
								TFileUtils.getFileUtils().copyFile(new File(dirPath), new File(delBackupDir + "/" + realDir));
								//���ҵĹ���Ŀ¼��, ɾ������ļ�
								//-------Ŀǰ��ɾ��ԭ�����ļ�...TFileUtils.getFileUtils().deleteFile(new File(dirPath));
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
			// ��������뵽hashtable��
			if (!dir.getName().endsWith("java"))
				return;
			try {
				BufferedReader inSameFile = new BufferedReader(new FileReader("./sameFile.txt"));
				do {
					String comment = inSameFile.readLine();//ע��
					System.out.println("comment: " + comment);
					if (comment != null && comment.indexOf("***END***") >= 0)
						break;
					if (comment == null)
						continue;
					String myPackage = inSameFile.readLine();//ĿǰĿ¼�İ���
					String productPackage = inSameFile.readLine();//��ƷĿ¼�İ���
					inSameFile.readLine();//Ŀǰ�ļ�����ʵĿ¼
					inSameFile.readLine();//��Ʒ�ļ�����ʵĿ¼
					System.out.println("file: " + dir.getAbsolutePath() + ", ԭʼ: " + myPackage + ", �滻Ϊ: " + productPackage);
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
				if (compareClassInfo.packageName.equals(productFileInfo.packageName)) {//�ҵ���ͬ��
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
			// ��������뵽hashtable��
			if (!dir.getName().endsWith("java"))
				return;
			BufferedReader in = new BufferedReader(new FileReader(dir));
			String str;
			while ((str = in.readLine()) != null) {
				str = str.trim();
				if (str.startsWith("package ")) {// �ҵ�package
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
