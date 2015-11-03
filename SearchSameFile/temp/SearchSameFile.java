package mytools;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;

import org.apache.tools.ant.Project;

class ClassInfo implements Serializable {
	private static final long serialVersionUID = 1L;
	String realClassName; //�����ǵ���������
	String packageName; //����+����
	String realPath; //�ļ���·��, �����ļ�����.
}

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

public class SearchSameFile {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		if (args.length != 4) {
			System.out.println("usage: SearchSameFile glassfishĿ¼ productĿ¼ ���ϲ�Ŀ¼ ����(1-�����ظ��Ĳ���ɾ���ظ��� 2-�滻���ڵ��ļ��İ��� 3-����glassfishĿ¼)");
			return;
		}
		
		File glassfishDir = new File(args[0]);
		File productDir = new File(args[1]);
		File mergeDir = new File(args[2]);
		int action = Integer.parseInt(args[3]);
		
		//��glassfishĿ¼���ļ���, ����, Ŀ¼��¼����
		ArrayList<ClassInfo> glassfishAllFile = new ArrayList<ClassInfo>();
		ArrayList<ClassInfo> productAllFile = new ArrayList<ClassInfo>();
		BufferedWriter out = null;
		try {
			// ��ĿԴ�ļ�Ŀ¼, ��package+ClassName--->src dir
			//����ĳһ��Ŀ¼�µ����е�.java�ļ�, ������, ����+����, ʵ��Ŀ¼��¼��glassfishAllFile
			if (action == 3) {
				visitAllFiles(glassfishDir, glassfishAllFile);
		        // Serialize to a file
		        ObjectOutput seriOut = new ObjectOutputStream(new FileOutputStream("./glassfishAllFile.ser"));
		        seriOut.writeObject(glassfishAllFile);
		        seriOut.close();
		        return;
			} 

			if (action == 1) {
				out = new BufferedWriter(new FileWriter("./sameFile.txt"));
		        File file = new File("./glassfishAllFile.ser");
		        ObjectInputStream in = new ObjectInputStream(new FileInputStream(file));
		        // Deserialize the object
		        glassfishAllFile = (ArrayList<ClassInfo>) in.readObject();
		        in.close();
				visitAllFiles(productDir, productAllFile);
				findSameFile(mergeDir, glassfishAllFile, productAllFile, out);
				out.write("***END***\r\n");
				out.close();
			}

			if (action == 2) {
		        File file = new File("./glassfishAllFile.ser");
		        ObjectInputStream in = new ObjectInputStream(new FileInputStream(file));
		        // Deserialize the object
		        glassfishAllFile = (ArrayList<ClassInfo>) in.readObject();
		        in.close();
				replacePackage(mergeDir);
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
					if (comment != null && comment.indexOf("***END***")>=0) break;
					if (comment == null) continue;
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

	private static void findSameFile(File dir, ArrayList<ClassInfo> glassfishAllFile, ArrayList<ClassInfo> productAllFile, BufferedWriter out) throws IOException {
		if (dir.isDirectory()) {
			String[] children = dir.list();
			for (int i = 0; i < children.length; i++) {
				findSameFile(new File(dir, children[i]), glassfishAllFile, productAllFile, out);
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
					String packageName = str.substring(str.indexOf("package ") + "package ".length(), str.length() - 1).trim();
					ClassInfo classInfo = new ClassInfo();
					classInfo.realClassName = dir.getName().substring(0, dir.getName().indexOf(".java"));
					classInfo.packageName = packageName + "." + classInfo.realClassName;
					classInfo.realPath = dir.getAbsolutePath();
					System.out.println("className:" + classInfo.realClassName + ", packageName: " + classInfo.packageName + ", realPath:" + classInfo.realPath);
					for (int i=0; i<productAllFile.size(); i++) {
						ClassInfo productFileInfo = (ClassInfo) productAllFile.get(i);
						if (productFileInfo.realClassName.equals(classInfo.realClassName)) {//�ҵ���ͬ��
							in.close();//���ȹر��ļ�
							boolean result = (new File(classInfo.realPath)).delete();
							out.write("======" + result + "\r\n");
							out.write(classInfo.packageName + "\r\n");
							out.write(productFileInfo.packageName + "\r\n");
							out.write(classInfo.realPath + "\r\n");
							out.write(productFileInfo.realPath + "\r\n");
							break;
						}
					}
					break;
				}
			}
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
					System.out.println("className:" + classInfo.realClassName + ", packageName: " + classInfo.packageName + ", realPath:" + classInfo.realPath);
					result.add(classInfo);
					break;
				}
			}
		}
	}

}
