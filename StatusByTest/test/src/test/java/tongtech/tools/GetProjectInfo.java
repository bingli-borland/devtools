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

import com.atlassian.jira.rest.client.api.JiraRestClient;
import com.atlassian.jira.rest.client.api.domain.*;
import com.atlassian.jira.rest.client.internal.async.AsynchronousJiraRestClientFactory;
import com.atlassian.jira.rest.client.internal.json.WorklogJsonParserV5;
import com.google.common.collect.Iterables;
import com.tongtech.project.*;
import com.tongtech.project.Project;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.joda.time.DateTime;
import org.joda.time.Period;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import java.io.*;
import java.math.BigDecimal;
import java.net.URI;
import java.net.URISyntaxException;
import java.sql.Timestamp;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import javax.xml.transform.*;

/**
 * A sample code how to use JRJC library
 *
 * @since v0.1
 */
public class GetProjectInfo {
    private static Logger logger = LogManager.getLogger(GetProjectInfo.class);
    private static URI jiraServerUri = URI.create("http://10.10.22.206:8081");
    //private static URI jiraServerUri = URI.create("http://127.0.0.1:8080");
    AsynchronousJiraRestClientFactory factory = null;
    JiraRestClient restClient = null;
    LogstatJDBCTemplate logstatJDBCTemplate;
    EvStatJDBCTemplate evstatJDBCTemplate;
    IssueJDBCTemplate issueJDBCTemplate;

    final static Timestamp logstatFromDate = Timestamp.valueOf("2015-3-1 0:0:0");

    final static String []usernames = new String[] {
            /*yanjie, */"chengcca", "liurla", "tangcheng", "duanqj",
            /*jianghuana, */"chengwba", "sunleia", "tanjiana", "xufm", "yaolia", "zhaojl",
            /*linjing, */"lanbing", "lanyonga", "zhukanga",
            /*yanyma, */"huangqiana", "yanyma"};

    final static String []mailUsernames = new String[] {"liubt", "jianghuan", "jianghuan", "zhangwq",
            "chengcc", "chengwb",
            "huangqian", "lanyong", "liurl",
            "sunlei", "tangcheng", "tanjian", "yanjie", "yanym", "yaoli", "zhaojl",
            "zhukang", "lanbing", "xufm", "lijun", "duanqj"};
    //BAM1|CEP2|EDGE2|FSC1|JMS20|LLMC2|LLMJ2|Proxy2
    final static String []projects = new String[] {"TONGIMB", "SZIOT", "PDSB"};
    HashMap<String, Peoplestat> users = new HashMap();
    HashMap<String, Logdetails> logdetailHash = new HashMap<String, Logdetails>();
    String resourceDirPrefix = "/WEB-INF/lib/";
    String xslFile = resourceDirPrefix + "cddev-project-info.xsl";

    HashMap<String, Peoplestat> totalpeoplestatshash = new HashMap();
    HashMap<String, Valuationstat> totalvaluationstatshash = new HashMap();

    public static void main(String[] args) throws URISyntaxException, JSONException, IOException {
        String outputxml = "";
        logger.info("start...");
        if (args.length < 2) {
            System.out.println("usage: tongtech.tools.StatusByTestResult command<byday, byweek, bymonth, byinput> projectName reportdir");
            return;
        }
        ApplicationContext context = new ClassPathXmlApplicationContext("spring-beans.xml");
        String command = args[0];
        String projects = args[1];
        String reportdir = args[2];
        String startdate = "", enddate = "";
        GetProjectInfo ex = new GetProjectInfo();
        ex.logstatJDBCTemplate = (LogstatJDBCTemplate)context.getBean("logstatJDBCTemplate");
        ex.evstatJDBCTemplate = (EvStatJDBCTemplate)context.getBean("evstatJDBCTemplate");
        ex.issueJDBCTemplate = (IssueJDBCTemplate)context.getBean("issueJDBCTemplate");
        if (command.equals("byinput")) {
            if (args.length < 5) {
                System.out.println("usage: tongtech.tools.StatusByTestResult command<byinput> projectName startdate(2013-7-31) enddate");
                return;
            }
            startdate = args[3];
            enddate = args[4];
            String byinput_outputxml = reportdir + "/cddev-project-info" + "." + startdate + "." + enddate;
            ex.handleByDate(command, projects, startdate, enddate, reportdir, byinput_outputxml + ".xml");
            ex.outputHTML(reportdir, byinput_outputxml);
        } else if(command.equals("auto")) {//根据日期生成报告
            DateTime now = new DateTime();
            outputxml = ex.handleByDayWeekMonth(projects, "byday", reportdir);
            ex.outputHTML(reportdir, outputxml);
            if (now.getDayOfWeek() == 1) {//周一
                outputxml = ex.handleByDayWeekMonth(projects, "byweek", reportdir);
                ex.outputHTML(reportdir, outputxml);
            }
            if (now.getDayOfMonth() == 1) {//每个月的第一天
                outputxml = ex.handleByDayWeekMonth(projects, "bymonth", reportdir);
                ex.outputHTML(reportdir, outputxml);
            }
        } else if (command.startsWith("by")) {
            outputxml = ex.handleByDayWeekMonth(projects, command, reportdir);
            ex.outputHTML(reportdir, outputxml);
        }
    }

    public void outputHTML(String reportdir, String outputxml) {
        try {
            String myxslfile = reportdir + xslFile;
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
            logger.info("email, read file.");
            String fileContent = readFileAsString(myxmlfile + ".html");
            logger.info("email, send.");
            SendMail sendMail = new SendMail();
            logger.info("email, send first.");
            for (int i=0; i<mailUsernames.length; i++) {
                String mailAddress = mailUsernames[i] + "@tongtech.com";
                logger.info("email, send to " + mailAddress);
                sendMail.send(mailAddress, "CDDEV数据统计", fileContent);
            }
        }
        catch (Exception e) {
            logger.error("xsl transform error.", e);
            e.printStackTrace( );
        }
    }

    private String readFileAsString(String filePath) throws IOException {
        StringBuffer fileData = new StringBuffer();
        BufferedReader reader = new BufferedReader(
                new FileReader(filePath));
        char[] buf = new char[1024];
        int numRead=0;
        while((numRead=reader.read(buf)) != -1){
            String readData = String.valueOf(buf, 0, numRead);
            fileData.append(readData);
        }
        reader.close();
        return fileData.toString();
    }

    public String handleByDayWeekMonth(String projects, String command, String reportdir) throws JSONException, IOException, URISyntaxException {
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
        String outputxml = reportdir + "/cddev-project-info" + "." + startdate + "." + enddate;
        handleByDate(command, projects, startdate, enddate, reportdir, outputxml + ".xml");
        return outputxml;
    }

    //
    public void handleByDate(String command, String projects, String startdate, String enddate, String reportDir, String outputxml) throws IOException, JSONException, URISyntaxException {
        factory = new AsynchronousJiraRestClientFactory();
        restClient = factory.createWithBasicHttpAuthentication(jiraServerUri, "testcase", "testcase9211");
        String[] sArray=projects.split("\\|");
        ObjectFactory factory= new ObjectFactory();
        Projects projectinfos = factory.createProjects();

        Peoplestats peoplestats = factory.createPeoplestats();
        for (int j=0; j<usernames.length; j++) {
            Peoplestat peoplestat = factory.createPeoplestat();
            peoplestat.setLogworkcount(0);
            peoplestat.setTimespent(0.0);
            peoplestat.setLogtimespent(0.0);
            peoplestat.setName(usernames[j]);
            users.put(usernames[j], peoplestat);

            Logdetails logdetails = factory.createLogdetails();
            logdetails.setName(usernames[j]);
            logdetailHash.put(usernames[j], logdetails);
        }

        Valuationstats valuationstats = factory.createValuationstats();
        for(int i =0; i < sArray.length ; i++) {
            String project = sArray[i];
            //项目信息
            generateInfo(project, startdate, enddate, projectinfos, factory);
            //延期任务统计
            generateDueTask(project, projectinfos, factory);
        }

        //将计算的值放入到相应的统计中
        for (int j=0; j<usernames.length; j++) {
            Peoplestat peoplestat = users.get(usernames[j]);
            peoplestats.getPeoplestat().add(peoplestat);

            Logdetails logdetails = logdetailHash.get(usernames[j]);
            projectinfos.getLogdetails().add(logdetails);
        }
        projectinfos.getPeoplestats().add(peoplestats);

        //进行汇总统计
        for (int j=0; j<usernames.length; j++) {//初始化
            Peoplestat peoplestat = factory.createPeoplestat();
            peoplestat.setLogworkcount(0);
            peoplestat.setTimespent(0.0);
            peoplestat.setLogtimespent(0.0);
            peoplestat.setEarnedvalue(0.0);
            peoplestat.setName(usernames[j]);
            peoplestat.setMemo("");
            logger.debug(usernames[j] + " OK.");
            totalpeoplestatshash.put(usernames[j], peoplestat);
        }
        List<Peoplestat> totalpeoplestat = logstatJDBCTemplate.getTotalLogstat();
        for(Peoplestat peoplestat:totalpeoplestat) {
            totalpeoplestatshash.put(peoplestat.getName(), peoplestat);
        }
        List<Peoplestat> totalpeoplestat2 = logstatJDBCTemplate.getLogstatByDayAuthor();
        for(Peoplestat peoplestat:totalpeoplestat2) {
            Peoplestat old = totalpeoplestatshash.get(peoplestat.getName());
            if (old != null) {
                old.setMemo(old.getMemo() + ", " + peoplestat.getMemo());
                //重新设置每天工作超过8小时的为每天最多8小时。暂存在timespend中。
                old.setTimespent(old.getTimespent() + peoplestat.getTimespent());
            }
        }
        for (int j=0; j<usernames.length; j++) {//初始化
            Peoplestat peoplestat = totalpeoplestatshash.get(usernames[j]);
            if (peoplestat.getMemo() != null && peoplestat.getMemo().length() > 50) {
                peoplestat.setMemo(peoplestat.getMemo().substring(peoplestat.getMemo().length()-50));
                logger.debug("50.2." + peoplestat.getMemo());
            }
            if (peoplestat.getTimespent() > 0) {
                logger.info("工作时间超，截短，name: " + peoplestat.getName() + ", 填写值:" + peoplestat.getLogtimespent() + ", 多算: " + peoplestat.getTimespent());
                peoplestat.setLogtimespent(peoplestat.getLogtimespent() - peoplestat.getTimespent());
            }
        }
        //仅仅显示Memo最长50个字符
        try {
            for(Peoplestat peoplestat:totalpeoplestat2) {
                Peoplestat old = totalpeoplestatshash.get(peoplestat.getName());
                if (old != null) {
                    logger.debug("50." + old.getMemo());

                }
            }
        } catch (Exception e) {
            logger.error(e);
        }

        //统计挣值
        List<Peoplestat> evstats = evstatJDBCTemplate.getTotalEvstat();
        for(Peoplestat peoplestat:evstats) {
            Peoplestat old = totalpeoplestatshash.get(peoplestat.getName());
            if (old != null) {
                logger.debug("" + old.getName() + ", " + peoplestat.getName());
                logger.debug("" + old.getEarnedvalue());
                logger.debug("" + peoplestat.getEarnedvalue());
                double v = old.getEarnedvalue() + peoplestat.getEarnedvalue();
                DecimalFormat    df   = new DecimalFormat("######0.000");
                String result = df.format(v);
                old.setEarnedvalue(Double.parseDouble(result));
            }
        }
        //
        Totalpeoplestats totalpeoplestats = factory.createTotalpeoplestats();
        for (int j=0; j<usernames.length; j++) {
            Peoplestat peoplestat = totalpeoplestatshash.get(usernames[j]);
            totalpeoplestats.getPeoplestat().add(peoplestat);
        }
        projectinfos.getTotalpeoplestats().add(totalpeoplestats);

        //输出
        try {
            JAXBContext jc = JAXBContext.newInstance(ObjectFactory.class);
            Marshaller marshaller = jc.createMarshaller();
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);

            marshaller.marshal(projectinfos, new PrintWriter(outputxml));
        } catch (Exception e) {
            e.printStackTrace();
        }
        restClient.close();
    }

    private void addLogDetail(ObjectFactory factory, Issue newissue, Worklog worklog, boolean isIssueAdded, Logdetail logdetail) {
        try {
                        Worklogdetail worklogdetail = factory.createWorklogdetail();
                        String username = worklog.getAuthor().getName();
                        logger.info("in time." + username);
                        worklogdetail.setName(worklog.getAuthor().getName());
                        worklogdetail.setCreatedate(worklog.getStartDate().toString("yyyy-MM-dd HH"));//yyyy-MM-dd HH:mm:ss
                        double f = worklog.getMinutesSpent() / 60.0;
                        BigDecimal   b   =   new   BigDecimal(f);
                        double   f1   =   b.setScale(2,   BigDecimal.ROUND_HALF_UP).doubleValue();
                        worklogdetail.setTimespent(f1);
                        worklogdetail.setComment(worklog.getComment());
                        if (!isIssueAdded) {
                            isIssueAdded = true;
                            logger.info("add issue: " + newissue.getKey());
                            logdetail.getWorklogdetail().add(worklogdetail);
                            logdetail.setName(newissue.getAssignee().getName());
                            logdetail.setIssuekey(newissue.getKey());
                            logdetail.setIssuetype(newissue.getIssueType().getName());
                            logdetail.setIssuesummary(newissue.getSummary());
                            Logdetails logdetails = logdetailHash.get(username);
                            if (logdetails != null) {
                                logdetails.getLogdetail().add(logdetail);
                            } else {
                                logger.info("cannot find username: " + username);
                            }
                        } else {
                            logdetail.getWorklogdetail().add(worklogdetail);
                        }
        } finally {
        }
    }

    private void generateLogDetail(String startdate, String enddate, ObjectFactory factory) {
        boolean isIssueAdded = false;
        int count = 0;
        try {
            String jql = "updated>=\'" + startdate + "\' and updated<\'" + enddate + "\'";
            logger.info("jql " + jql);
            final SearchResult searchResult = restClient.getSearchClient().searchJql(jql).claim();
            for (Issue issue : searchResult.getIssues()) {
                isIssueAdded = false;
                final Issue newissue = restClient.getIssueClient().getIssue(issue.getKey()).claim();
                Iterable<Worklog> worklogs = newissue.getWorklogs();
                Iterable<Worklog> worklogs1 = issue.getWorklogs();
                logger.info("worklog size: " + Iterables.size(worklogs) + ", " + Iterables.size(worklogs1));
                for (Worklog worklog : worklogs) {
                    logger.info("story:" + worklog.getComment() + ", " + worklog.getAuthor().getName());
                    DateTime datetime = worklog.getStartDate();
                    if (datetime.isAfter(new DateTime(startdate).getMillis()) && (datetime.isBefore(new DateTime(enddate).getMillis()))) {
                        Worklogdetail worklogdetail = factory.createWorklogdetail();
                        String username = worklog.getAuthor().getName();
                        logger.info("in time." + username);
                        worklogdetail.setName(worklog.getAuthor().getName());
                        worklogdetail.setCreatedate(datetime.toString("yyyy-MM-dd HH"));//yyyy-MM-dd HH:mm:ss
                        double f = worklog.getMinutesSpent() / 60.0;
                        BigDecimal   b   =   new   BigDecimal(f);
                        double   f1   =   b.setScale(2,   BigDecimal.ROUND_HALF_UP).doubleValue();
                        worklogdetail.setTimespent(f1);
                        worklogdetail.setComment(worklog.getComment());
                        if (!isIssueAdded) {
                            isIssueAdded = true;
                            logger.info("add issue: " + issue.getKey());
                            Logdetail logdetail = factory.createLogdetail();
                            logdetail.getWorklogdetail().add(worklogdetail);
                            logdetail.setName(issue.getAssignee().getName());
                            logdetail.setIssuekey(issue.getKey());
                            logdetail.setIssuetype(issue.getIssueType().getName());
                            logdetail.setIssuesummary(issue.getSummary());
                            Logdetails logdetails = logdetailHash.get(username);
                            if (logdetails != null) {
                            logdetails.getLogdetail().add(logdetail);
                            } else {
                                logger.info("cannot find username: " + username);
                            }
                        }
                    }
                }//for worklogs
            }//for issues
        } finally {
        }
    }


    /*新建立一个工作流,工作流schema, 将test case与该工作流关联,并且将项目/workflows中, 右上角的actions关联type与工作流*/
    public void generateInfo(String projectName, String startdate, String enddate, Projects projectinfos, ObjectFactory factory) throws URISyntaxException, JSONException, IOException {
        int count = 0;
        int logOnStoryCount = 0;
        int logOnSubtaskCount = 0;
        boolean isParentIssueAdded = false, isSubissueAdded = false;
        Logdetail parentLogdetail = null, subLogdetail = null;
        Project projectinfo = factory.createProject();
        projectinfo.setName(projectName);
        logger.info("------start project: " + projectName);
        try {
            // let's now print all issues matching a JQL string (here: all assigned issues)
            //String jql = "project=\'" + projectName + "\' and issuetype not in subtaskIssueTypes() and issuekey=JMSB-8";
            String jql = "project=\'" + projectName + "\' and issuetype not in subtaskIssueTypes()";
            logger.info("jql " + jql);
            final SearchResult searchResult = restClient.getSearchClient().searchJql(jql).claim();
            String workingpeople = "";
            for (Issue issue : searchResult.getIssues()) {
                isParentIssueAdded = false;
                workingpeople = "";
                logOnStoryCount = logOnSubtaskCount = 0;
                logger.info("parent issue: " + issue.getKey().toString() + ", " + issue.getStatus().getName());
                Projectstory projectstory = factory.createProjectstory();
                logger.info("2");
                {
                    //Iterable<Worklog> worklogs = issue.getWorklogs();
                    final Issue newissue = restClient.getIssueClient().getIssue(issue.getKey()).claim();
                    logger.info("3");
                    Iterable<Worklog> worklogs = newissue.getWorklogs();
                    logger.info("4");
                    int logtimespent = 0;
                    int logworkcount = 0;
                    try {
                        for (Worklog worklog : worklogs) {
                            logger.info("story:" + worklog.getComment() + ", " + worklog.getAuthor().getName());
                            DateTime datetime = worklog.getStartDate();
                            if (datetime.isAfter(new DateTime(startdate).getMillis()) && (datetime.isBefore(new DateTime(enddate).getMillis()))) {
                                logger.info("in time." + worklog.getAuthor().getName());
                                logtimespent += worklog.getMinutesSpent();
                                logworkcount += 1;
                                logOnStoryCount++;
                                /*统计某个人的工作量*/
                                String username = worklog.getAuthor().getName();
                                workingpeople = workingpeople + username + ", ";
                                Peoplestat peoplestat = users.get(username);
                                if (peoplestat == null) {
                                    logger.info("!!!cannot find: " + username);
                                } else {
                                    peoplestat.setLogtimespent(peoplestat.getLogtimespent() + worklog.getMinutesSpent()/60);
                                    peoplestat.setLogworkcount(peoplestat.getLogworkcount() + 1);
                                }

                                if (!isParentIssueAdded) {
                                    Logdetail logdetail = factory.createLogdetail();
                                    addLogDetail(factory, issue, worklog, isParentIssueAdded, logdetail);
                                    parentLogdetail = logdetail;
                                    isParentIssueAdded = true;
                                } else {
                                    addLogDetail(factory, issue, worklog, isParentIssueAdded, parentLogdetail);
                                }
                            }
                        }
                        projectstory.setKey(issue.getKey().toString());
                        projectstory.setDeveloper(issue.getAssignee() == null? "?":issue.getAssignee().getName());
                        projectstory.setStatus(issue.getStatus().getName());
                    } catch (Exception e) {
                        logger.error("", e);
                    }

                    logger.info("5");
                    TimeTracking timeTracking = issue.getTimeTracking();
                    logger.info("6");
                    if (timeTracking != null) {
                        Integer timeSpentMinutes =  timeTracking.getTimeSpentMinutes();
                        projectstory.setTimespent((double)(timeSpentMinutes/60));//hours
                        /*统计某个人的工作量*/
                        Peoplestat peoplestat = users.get(issue.getAssignee().getName());
                        if (peoplestat == null) {
                            logger.info("!!!error:name cannot find" + issue.getAssignee().getName());
                        }
                        if (peoplestat != null && timeSpentMinutes != null)
                        peoplestat.setTimespent((double)(timeSpentMinutes/60));
                    }
                    logger.info("7");
                    projectstory.setLogtimespent((double)(logtimespent/60));
                    projectstory.setLogworkcount(logworkcount);
                    logger.info("key=" + issue.getKey() + ", summary:" + issue.getSummary());
                    projectstory.setSummary(issue.getSummary());
                }

                /*String subjql = "project=\'" + projectName + "\' and parent=\'" + issue.getKey() + "\'";
                System.out.println("subjql " + subjql);
                SearchResult subsearchResult = restClient.getSearchClient().searchJql(subjql).claim();
                System.out.println("subtask: " + subsearchResult.getTotal());*/
                logger.info("8");
                for (Subtask mysubtask : issue.getSubtasks()) {
                    isSubissueAdded = false;
                    final Issue subissue = restClient.getIssueClient().getIssue(mysubtask.getIssueKey()).claim();
                    logger.info("9");
                    Iterable<Worklog> worklogs = subissue.getWorklogs();
                    logger.info("10");
                    logger.info("in subtask " + Iterables.size(worklogs) + subissue.getWorklogUri().toString());
                    int logtimespent = 0;
                    int logworkcount = 0;
                    for (Worklog worklog : worklogs) {
                        logger.info("subtask:" + worklog.getComment() + ", " + worklog.getAuthor().getName());
                        DateTime datetime = worklog.getStartDate();
                        if (datetime.isAfter(new DateTime(startdate).getMillis()) && (datetime.isBefore(new DateTime(enddate).getMillis()))) {
                            logger.info("in time, subtask:" + worklog.getComment() + ", " + worklog.getAuthor().getName());
                            logtimespent += worklog.getMinutesSpent();
                            logworkcount += 1;
                            logOnSubtaskCount++;
                            /*统计某个人的工作量*/
                            String username = worklog.getAuthor().getName();
                            workingpeople = workingpeople + username + ", ";
                            Peoplestat peoplestat = users.get(username);
                            if (peoplestat == null) {
                                logger.info("!!!cannot find: " + username);
                            }
                            logger.info("people: " + peoplestat + ", worklog: " + worklog);
                            if (peoplestat != null) {
                                peoplestat.setLogtimespent(peoplestat.getLogtimespent() + worklog.getMinutesSpent()/60);
                                peoplestat.setLogworkcount(peoplestat.getLogworkcount() + 1);
                            }

                            if (!isSubissueAdded) {
                                Logdetail logdetail = factory.createLogdetail();
                                addLogDetail(factory, issue, worklog, isSubissueAdded, logdetail);
                                subLogdetail = logdetail;
                                isSubissueAdded = true;
                            } else {
                                addLogDetail(factory, issue, worklog, isSubissueAdded, subLogdetail);
                            }
                        }
                    }
                    if (logOnSubtaskCount > 0) {//这段时间工作过
                        logger.info("工作过, 重新计算工作量:" + subissue.getKey().toString());
                        Projectsubtask subtask = factory.createProjectsubtask();
                        subtask.setKey(subissue.getKey().toString());
                        subtask.setDeveloper(subissue.getAssignee().getName());
                        subtask.setStatus(subissue.getStatus().getName());
                        TimeTracking timeTracking = subissue.getTimeTracking();
                        Integer timeSpentMinutes = timeTracking.getTimeSpentMinutes();
                        if (timeSpentMinutes != null)
                            subtask.setTimespent(timeSpentMinutes/60);//hours
                        subtask.setLogtimespent((double)(logtimespent / 60));
                        subtask.setLogworkcount(logworkcount);
                        projectstory.setLogtimespent(projectstory.getLogtimespent() + logtimespent/60);
                        projectstory.setLogworkcount(projectstory.getLogworkcount() + logworkcount);
                        subtask.setSummary(subissue.getSummary());
                        projectstory.getProjectsubtask().add(subtask);
                        /*统计某个人的工作量*/
                        Peoplestat peoplestat = users.get(subissue.getAssignee().getName());
                        if (peoplestat == null) {
                            logger.info("!!!error:name cannot find" + subissue.getAssignee().getName());
                        }
                        if (peoplestat != null && timeSpentMinutes != null) {
                            peoplestat.setTimespent((double)(timeSpentMinutes/60));
                        }
                    }
                }
                if (logOnStoryCount > 0 || logOnSubtaskCount > 0) {//这段时间内如果工作过才需要记录
                    projectstory.setSummary(workingpeople + projectstory.getSummary());
                    projectinfo.getProjectstory().add(projectstory);
                }
            }
        } finally {
        }
        projectinfos.getProject().add(projectinfo);
        logger.info("------end project: " + projectName);
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

    /*获得工作量超期任务*/
    public void generateDueTask(String projectName, Projects projectinfos, ObjectFactory factory) throws URISyntaxException, JSONException, IOException {
        int count = 0;
        int logOnStoryCount = 0;
        int logOnSubtaskCount = 0;
        boolean isParentIssueAdded = false, isSubissueAdded = false;
        Logdetail parentLogdetail = null, subLogdetail = null;
        Duetaskbyprojects dueTaskProjects = factory.createDuetaskbyprojects();
        dueTaskProjects.setName(projectName);
        logger.info("------start project: " + projectName);
        try {
            // let's now print all issues matching a JQL string (here: all assigned issues)
            //String jql = "project=\'" + projectName + "\' and issuetype not in subtaskIssueTypes() and issuekey=JMSB-8";
            String jql = "project=\'" + projectName + "\'" + " and status!=Resolved and status!=Closed and created>\"2014-8-3\"";
            logger.info("jql " + jql);
            final SearchResult searchResult = restClient.getSearchClient().searchJql(jql).claim();
            String workingpeople = "";
            for (Issue issue : searchResult.getIssues()) {
                isParentIssueAdded = false;
                workingpeople = "";
                logOnStoryCount = logOnSubtaskCount = 0;
                logger.info("parent issue: " + issue.getKey().toString() + ", " + issue.getStatus().getName());
                Duetask duetask = factory.createDuetask();
                logger.info("2");
                try {
                    logger.info("key=" + issue.getKey() + ", summary:" + issue.getSummary());
                    logger.info("3");
                    duetask.setKey(issue.getKey().toString());
                    duetask.setDeveloper(issue.getAssignee().getName());
                    duetask.setStatus(issue.getStatus().getName());
                    Jiraissue jiraissue = issueJDBCTemplate.getIssueByPKey(duetask.getKey());
                    if (jiraissue != null) {
                        duetask.setOriginalestimate(jiraissue.getTimeoriginalestimate().intValue());//hours
                        duetask.setLogtimespent(jiraissue.getTimespent());
                    } else {
                        duetask.setOriginalestimate( -1 );
                        duetask.setLogtimespent(-1.0);
                    }
                    duetask.setLogworkcount(0);
                    duetask.setSummary(workingpeople + ", " + issue.getSummary());
                    //写过日志 并且 (超过预计的,或者没有估计时间的).记录
                    if ((duetask.getLogtimespent() > duetask.getOriginalestimate() || duetask.getOriginalestimate() == -1)) {
                        dueTaskProjects.getDuetask().add(duetask);
                    }
                } catch (Exception e) {
                    logger.error("", e);
                }
            }
        } finally {
        }
        projectinfos.getDuetaskbyprojects().add(dueTaskProjects);
        logger.info("------end project: " + projectName);
    }
}
