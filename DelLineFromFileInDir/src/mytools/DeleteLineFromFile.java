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

public class DeleteLineFromFile {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		if (args.length < 1) {
			System.out.println("DeleteLineFromFile dir");
			return;
		}
		File dir = new File(args[0]);
		visitAllFiles(dir);
	}
	public static void visitAllFiles(File dir) {
		if (dir.isDirectory()) {
			String[] children = dir.list();
			for (int i = 0; i < children.length; i++) {
				visitAllFiles(new File(dir, children[i]));
			}
		} else {
			// 将结果放入到hashtable中
			System.out.println("File<" + dir.getName() + ">");
			if (!dir.getName().endsWith("java"))
				return;
			TFileUtils.getFileUtils().removeLineFromFile(dir.getPath(), "* DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.");
			TFileUtils.getFileUtils().removeLineFromFile(dir.getPath(), "* Copyright 2000-2007 Sun Microsystems, Inc. All rights reserved.");
			TFileUtils.getFileUtils().removeLineFromFile(dir.getPath(), "* The contents of this file are subject to the terms of either the GNU");
			TFileUtils.getFileUtils().removeLineFromFile(dir.getPath(), "* General Public License Version 2 only (\"GPL\") or the Common Development");
			TFileUtils.getFileUtils().removeLineFromFile(dir.getPath(), "* and Distribution License (\"CDDL\") (collectively, the \"License\").  You may");
			TFileUtils.getFileUtils().removeLineFromFile(dir.getPath(), "* not use this file except in compliance with the License.  You can obtain");
			TFileUtils.getFileUtils().removeLineFromFile(dir.getPath(), "* a copy of the License at https://glassfish.dev.java.net/public/CDDL+GPL.html");
			TFileUtils.getFileUtils().removeLineFromFile(dir.getPath(), "* or mq/legal/LICENSE.txt.  See the License for the specific language");
			TFileUtils.getFileUtils().removeLineFromFile(dir.getPath(), "* governing permissions and limitations under the License.");
			TFileUtils.getFileUtils().removeLineFromFile(dir.getPath(), "* When distributing the software, include this License Header Notice in each");
			TFileUtils.getFileUtils().removeLineFromFile(dir.getPath(), "* file and include the License file at mq/legal/LICENSE.txt.  Sun designates");
			TFileUtils.getFileUtils().removeLineFromFile(dir.getPath(), "* this particular file as subject to the \"Classpath\" exception as provided by");
			TFileUtils.getFileUtils().removeLineFromFile(dir.getPath(), "* Sun in the GPL Version 2 section of the License file that accompanied this");
			TFileUtils.getFileUtils().removeLineFromFile(dir.getPath(), "* code.  If applicable, add the following below the License Header, with the");
			TFileUtils.getFileUtils().removeLineFromFile(dir.getPath(), "* fields enclosed by brackets [] replaced by your own identifying information:");
			TFileUtils.getFileUtils().removeLineFromFile(dir.getPath(), "* \"Portions Copyrighted [year] [name of copyright owner]\"");
            TFileUtils.getFileUtils().removeLineFromFile(dir.getPath(), "* Contributor(s):");
            TFileUtils.getFileUtils().removeLineFromFile(dir.getPath(), "* If you wish your version of this file to be governed by only the CDDL or");
            TFileUtils.getFileUtils().removeLineFromFile(dir.getPath(), "* only the GPL Version 2, indicate your decision by adding \"[Contributor]");
            TFileUtils.getFileUtils().removeLineFromFile(dir.getPath(), "* elects to include this software in this distribution under the [CDDL or GPL");
            TFileUtils.getFileUtils().removeLineFromFile(dir.getPath(), "* Version 2] license.\"  If you don't indicate a single choice of license, a");
            TFileUtils.getFileUtils().removeLineFromFile(dir.getPath(), "* recipient has the option to distribute your version of this file under");
            TFileUtils.getFileUtils().removeLineFromFile(dir.getPath(), "* either the CDDL, the GPL Version 2 or  to extend the choice of license to");
            TFileUtils.getFileUtils().removeLineFromFile(dir.getPath(), "* its licensees as provided above.  However, if you add GPL Version 2 code");
            TFileUtils.getFileUtils().removeLineFromFile(dir.getPath(), "* and therefore, elected the GPL Version 2 license, then the option applies");
            TFileUtils.getFileUtils().removeLineFromFile(dir.getPath(), "* only if the new code is made subject to such option by the copyright holder."); 
		}
	}

}
