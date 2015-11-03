/*
 * Copyright (C) 2010 Atlassian
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package tongtech.tools;

import com.google.common.collect.Iterables;
import com.tongtech.svntools.ObjectFactory;
import com.tongtech.svntools.Peoplestat;
import com.tongtech.svntools.Peoplestats;
import com.tongtech.svntools.Svnstat;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import java.io.*;
import java.math.BigDecimal;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.joda.time.DateTime;

import javax.xml.bind.Unmarshaller;
import javax.xml.transform.*;

/**
 * A sample code how to use JRJC library
 *
 * @since v0.1
 */
public class CodeLineByPeople {
    private static Logger logger = LogManager.getLogger(CodeLineByPeople.class);
    final static String []usernames = new String[] {"caoyang", "chengcc", "chengwb", "hedb", "helei",
            "huangqian", /*"jianghuana", */"lanyong", /*"linjinga", "liuhf", */"liurl", "liyin1",
            "luohq", "qinsx", "sunlei", "tangcheng", "tanjiana", /*"yanjie", */"yanym", "yaoli", "zhaojl",
            "zhongcy", "zhukang"};
    //final static String []usernames = new String[] {"lanyong", "liurl", "chengcc"};
    final static String []urls = new String[] {"http://10.10.22.206:8080/svn/sailfish",
            "http://10.10.22.206:8080/svn/Betta", "http://10.10.22.206:8080/svn/tongcomfra",
            "http://10.10.22.206:8080/svn/Cellular_CEP", "http://10.10.22.206:8080/svn/TI_EDGE",
            "http://10.10.22.206:8080/svn/Cellular_BAM", "http://10.10.22.206:8080/svn/JMSV2"};
    final static String []working_dirs = new String[]{"e:/dev/sailfish", "e:/dev/betta",
            "e:/dev/proxy_server", "e:/dev/TI_CEP", "e:/dev/TI_EDGE", "e:/dev/BAM", "e:/dev/JMSV2"};
    final static String []PRJNAME = new String[]{"LLMC", "LLMJ", "PROXY", "CEP", "EDGE", "BAM", "JMSV2"};
    final static String prjIdentify = "code-line-by-people";
    final static String [][]svnurlusers = new String[][] {
            {},/*LLM C*/
            {},/*LLM J*/
            {},/*Proxy*/
            {},/*CEP*/
            {},/*EDGE*/
            {},/*BAM*/
            {} /*JMSV2*/
    };
    HashMap<String, Peoplestat> users = new HashMap();
    String resourceDir = "E:/dev/DevTools/DevTools/trunk/CddevJavaTools/resources/";
    String workingDir = "E:/dev/DevTools/DevTools/trunk/CddevJavaTools/temp/";
    String pythonprogram = "C:/Python32/python";

    String xslFile = prjIdentify + ".xsl";
    String mypythonprg = "diffbyauthordate.py";

    public static void main(String[] args) throws URISyntaxException, JSONException, IOException {
        String outputxml = "";
        logger.info("start...");
        if (args.length < 5) {
            System.out.println("usage: tongtech.tools.StatusByTestResult command<byday, byweek, bymonth, byinput> reportdir resourcedir workdingdir pythondir");
            return;
        }
        CodeLineByPeople ex = new CodeLineByPeople();
        String command = args[0];
        String reportdir = args[1];
        ex.resourceDir = args[2];
        ex.workingDir = args[3];
        ex.pythonprogram = args[4];
        ex.xslFile = ex.resourceDir + ex.xslFile;
        ex.mypythonprg = ex.resourceDir + ex.mypythonprg;
        String startdate = "", enddate = "";
        if (command.equals("byinput")) {
            if (args.length < 7) {
                System.out.println("usage: tongtech.tools.StatusByTestResult command<byinput> reportdir resourcedir workdingdir pythondir startdate(2013-7-31) enddate");
                return;
            }
            startdate = args[5];
            enddate = args[6];
            String byinput_outputxml = reportdir + "/" + prjIdentify;
            ex.handleByDate(command, startdate, enddate, reportdir, byinput_outputxml + ".xml");
            ex.outputHTML(reportdir, byinput_outputxml);
        } else if(command.equals("auto")) {//根据日期生成报告
            DateTime now = new DateTime();
            //outputxml = ex.handleByDayWeekMonth("byday", reportdir);
            //ex.outputHTML(reportdir, outputxml);
            if (now.getDayOfWeek() == 1) {//周一
                outputxml = ex.handleByDayWeekMonth("byweek", reportdir);
                ex.outputHTML(reportdir, outputxml);
            }
            if (now.getDayOfMonth() == 1) {//每个月的第一天
                outputxml = ex.handleByDayWeekMonth("bymonth", reportdir);
                ex.outputHTML(reportdir, outputxml);
            }
        } else if (command.startsWith("by")) {
            outputxml = ex.handleByDayWeekMonth(command, reportdir);
            ex.outputHTML(reportdir, outputxml);
        }
    }

    public void outputHTML(String reportdir, String outputxml) {
        try {
            String myxslfile = xslFile;
            System.out.println("xsl=" + myxslfile + ", xml=" + outputxml);
            TransformerFactory tFactory = TransformerFactory.newInstance();
            Transformer transformer = tFactory.newTransformer(new javax.xml.transform.stream.StreamSource(myxslfile));

            String myxmlfile = outputxml;
            System.out.println("xml=" + myxmlfile);
            transformer.transform
                    (new javax.xml.transform.stream.StreamSource
                            (myxmlfile + ".xml"),
                            new javax.xml.transform.stream.StreamResult
                                    ( new FileOutputStream(myxmlfile + ".html")));
        }
        catch (Exception e) {
            logger.error("xsl transform error.", e);
            e.printStackTrace( );
        }
    }

    public String handleByDayWeekMonth(String command, String reportdir) throws JSONException, IOException, URISyntaxException {
        String startdate = "", enddate = "";
        DateTime now = new DateTime();
        now = now.millisOfSecond().withMinimumValue();
        DateTime jodastartdate;
        if (command.equals("byday")) {
            jodastartdate = now.minusDays(1);
        } else if (command.equals("byweek")) {
            jodastartdate = now.minusWeeks(1);
        } else if (command.equals("bymonth")) {
            jodastartdate = now.minusMonths(1);
        } else {
            System.out.println("usage: tongtech.tools.StatusByTestResult command<byinput> projectName startdate(2013-7-31) enddate");
            return "";
        }
        startdate = jodastartdate.toLocalDate().toString();
        enddate = now.toLocalDate().toString();
        String outputxml = reportdir + "/" + prjIdentify;
        handleByDate(command, startdate, enddate, reportdir, outputxml + ".xml");
        return outputxml;
    }

    //
    public void handleByDate(String command, String startdate, String enddate, String reportDir, String outputxml) throws IOException, JSONException, URISyntaxException {
        JAXBContext jc = null;
        Svnstat newsvnstat = null, oldsvnstat = null;
        ObjectFactory factory= new ObjectFactory();
        newsvnstat = factory.createSvnstat();
        try {
            jc = JAXBContext.newInstance(ObjectFactory.class);
            Unmarshaller um = jc.createUnmarshaller();
            //Unmarshal XML contents of the file myDoc.xml into your Java
            try {
                oldsvnstat = (Svnstat)um.unmarshal(new java.io.FileInputStream(outputxml));
            } catch (Exception e) {
                logger.info("not exist. " + outputxml);
                oldsvnstat = null;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        String currentWorkingDir = workingDir + startdate + "." + enddate;
        File myfile = new File(currentWorkingDir);
        myfile.mkdirs();

        Peoplestats peoplestats = factory.createPeoplestats();
        peoplestats.setStartdate(startdate);
        peoplestats.setEnddate(enddate);
        for (int j=0; j<usernames.length; j++) {
            Peoplestat peoplestat = factory.createPeoplestat();
            peoplestat.setCommitcount(0);
            peoplestat.setSrcadded(0);
            peoplestat.setSrcdeleted(0);
            peoplestat.setTestadded(0);
            peoplestat.setTestdeleted(0);
            peoplestat.setName(usernames[j]);
            users.put(usernames[j], peoplestat);
        }

        for (int i=0; i<urls.length; i++) {
            String url = urls[i];
            String workingdir = working_dirs[i];
            String prjname = PRJNAME[i];
            for (int j=0; j<usernames.length; j++) {
                String author = usernames[j];
                Peoplestat peoplestat = users.get(author);
                String outputfile = currentWorkingDir + "/" + author + "." + prjname + "." + startdate + "." + enddate + "." + System.currentTimeMillis() + ".diff";
                generateDiff(url, workingdir, author, startdate, enddate, outputfile);
                File difffile = new File(outputfile);
                if (difffile.length() < 10) {
                    logger.info("no diff info, deleted." + outputfile);
                    difffile.delete();
                } else {
                    statByDiff(peoplestat, outputfile);
                }
            }
        }

        for (int j=0; j<usernames.length; j++) {
            Peoplestat peoplestat = users.get(usernames[j]);
            peoplestats.getPeoplestat().add(peoplestat);
        }
        newsvnstat.getPeoplestats().add(peoplestats);
        if (oldsvnstat != null) {
            newsvnstat.getPeoplestats().addAll(oldsvnstat.getPeoplestats());
        }

        try {
            Marshaller marshaller = jc.createMarshaller();
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);

            marshaller.marshal(newsvnstat, new PrintWriter(outputxml));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static boolean isNumeric(String str)
    {
        try
        {
            double d = Double.parseDouble(str);
        }
        catch(NumberFormatException nfe)
        {
            return false;
        }
        return true;
    }

    public void statByDiff(Peoplestat peoplestat, String difffile) {
        int delete_count = 0, test_delete_count = 0, add_count = 0, test_add_count = 0;
        int status = 0;
        boolean is_test = false, found_aa = false;
        try {
            FileInputStream fis = new FileInputStream(difffile);
            BufferedReader reader = new BufferedReader (new InputStreamReader(fis, "UTF-8"));
            String line = null;
            while ((line = reader.readLine()) != null) {
                if (line.startsWith("Index:")) {
                    status = 1;
                    found_aa = false;
                    if (line.indexOf("_test") != -1)
                        is_test = true;
                    else
                        is_test = false;
                    continue;
                }
                if (line.startsWith("===============================")) {
                    if (status == 1)
                        status = 2;
                    else
                        logger.info("status error, Index: should by " + difffile + " " + line);
                    continue;
                }
                if (line.startsWith("---")) {
                    if (status == 2)
                        status = 3;
                    else
                        logger.info("status error, Index: should by ---" + difffile + " " + line);
                    continue;
                }
                if (line.startsWith("+++")) {
                    if (status == 3)
                        status = 4;
                    else
                        logger.info("status error, Index: should by ---" + difffile + " " + line);
                    continue;
                }
                if (line.startsWith("@@")) {
                    if (status == 4 && found_aa == false) {
                        status = 5;
                        found_aa = true;
                    } else if (found_aa) {
                        int i = 0;
                    } else
                        logger.info("status error, Index: should by ---" + difffile + " " + line);
                    continue;
                }
                if (line.startsWith("-")) {
                    if (status == 5) {
                        if (is_test)
                            test_delete_count += 1;
                        else
                            delete_count += 1;
                    } else if (status == 4) {
                        int i = 0;
                    } else {
                        logger.info("status error, Index: should by ---" + difffile + " " + line);
                    }
                    continue;
                }
                if (line.startsWith("+")) {
                    if (status == 5) {
                        if (is_test)
                            test_add_count += 1;
                        else
                            add_count += 1;
                    } else if (status == 4) {
                        int i = 0;
                    } else {
                        logger.info("status error, Index: should by ---" + difffile + " " + line);
                    }
                    continue;
                }
            }
            logger.info("stat result, {}, {}, {}, {}, {}", peoplestat.getName(), add_count, test_add_count, delete_count, test_delete_count);
            peoplestat.setTestdeleted(test_delete_count);
            peoplestat.setTestadded(test_add_count);
            peoplestat.setSrcdeleted(delete_count);
            peoplestat.setSrcadded(add_count);
        } catch (Exception e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
    }

    public void generateDiff(String url, String workingdir, String author, String startdate, String enddate, String outputfile) {
        String command = " gendiff " + " " + url +
                " " + workingdir + " " + author + " " + startdate + " " + enddate + " " + outputfile;
        logger.info("pyton: " + pythonprogram + " " + mypythonprg + " " + command);
        ProcessBuilder builder = new ProcessBuilder(pythonprogram, mypythonprg, "gendiff", url, workingdir, author, startdate, enddate, outputfile);
        builder.redirectErrorStream(true);
        try {
            Process process = builder.start();
            OutputStream stdin = process.getOutputStream ();
            InputStream stderr = process.getErrorStream ();
            InputStream stdout = process.getInputStream ();

            BufferedReader reader = new BufferedReader (new InputStreamReader(stdout));
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(stdin));
            String line;
            while ((line = reader.readLine ()) != null) {
                logger.info("Stdout: " + line);
            }
        } catch (IOException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }
    }

}
