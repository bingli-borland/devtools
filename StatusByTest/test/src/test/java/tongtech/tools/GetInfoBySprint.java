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
import com.google.common.collect.Iterables;
import com.tongtech.sprint.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.codehaus.jettison.json.JSONException;
import org.joda.time.DateTime;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import java.io.*;
import java.math.BigDecimal;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;

/**
 * A sample code how to use JRJC library
 *
 * @since v0.1
 */
public class GetInfoBySprint {
    private static Logger logger = LogManager.getLogger(GetInfoBySprint.class);
    private static URI jiraServerUri = URI.create("http://10.10.22.206:8081");
    //private static URI jiraServerUri = URI.create("http://127.0.0.1:8080");
    AsynchronousJiraRestClientFactory factory = null;
    JiraRestClient restClient = null;
    final static String []usernames = new String[] {"caoyang", "chengcca", "chengwba", "hedba", "helei",
            "huangqiana", /*"jianghuana", */"lanyonga", /*"linjinga", "liuhf", */"liurla", "liyina",
            "luohqa", "qinsx", "sunleia", "tangcheng", "tanjiana", /*"yanjie", */"yanyma", "yaolia", "zhaojl",
            "zhongcya", "zhukanga", "zengkai", "lihui", "shenting", "fuzh", "zhangli"};
    //BAM1|CEP2|EDGE2|FSC1|JMS20|LLMC2|LLMJ2|Proxy2
    final static String []projects = new String[] {"BAM1", "CEP2", "EDGE2", "FSC1", "JMS20", "LLMC2", "LLMJ2", "Proxy2", "DAP1", "ESP1"};
    String resourceDirPrefix = "/WEB-INF/lib/";
    String xslFile = resourceDirPrefix + "cddev-sprint.xsl";
    String sprintStartDate, sprintEndDate;

    public static void main(String[] args) throws URISyntaxException, JSONException, IOException {
        String outputxml = "";
        logger.info("start...");
        if (args.length < 5) {
            System.out.println("usage: tongtech.tools.StatusByTestResult command<byday, byweek, bymonth, byinput> projectName reportdir");
            return;
        }
        String command = args[0];
        String projects = args[1];
        String reportdir = args[2];
        GetInfoBySprint ex = new GetInfoBySprint();
        ex.sprintStartDate = args[3];
        ex.sprintEndDate = args[4];
        if (command.equals("byinput")) {
            if (args.length < 7) {
                System.out.println("usage: tongtech.tools.StatusByTestResult command<byinput> projectName startdate(2013-7-31) enddate");
                return;
            }
            String startdate = "", enddate = "";
            startdate = args[5];
            enddate = args[6];
            String byinput_outputxml = reportdir + "/" + "cddev." + ex.getProject(projects) + "." + ex.getSprint(projects) + "." + startdate + "." + enddate;
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
            outputxml = ex.handleByDayWeekMonth(projects, command.trim(), reportdir);
            ex.outputHTML(reportdir, outputxml);
        }
    }

    public void outputHTML(String reportdir, String outputxml) {
        try {
            String myxslfile = reportdir + xslFile;
            logger.info("xsl=" + myxslfile + ", xml=" + outputxml);
            TransformerFactory tFactory = TransformerFactory.newInstance();
            Transformer transformer = tFactory.newTransformer(new javax.xml.transform.stream.StreamSource(myxslfile));

            String myxmlfile = outputxml;
            logger.info("xml=" + myxmlfile);
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
            sendMail.send("liubt@tongtech.com", "CDDEV SPRINT", fileContent);
            logger.info("email, send two.");
            sendMail.send("yanjie@tongtech.com", "CDDEV SPRINT", fileContent);
            sendMail.send("linjing@tongtech.com", "CDDEV SPRINT", fileContent);
            sendMail.send("jianghuan@tongtech.com", "CDDEV SPRINT", fileContent);
            sendMail.send("zhangwq@tongtech.com", "CDDEV SPRINT", fileContent);
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
        //String outputxml = reportdir + "/cddev-sprint" + "." + startdate + "." + enddate;
        String outputxml = reportdir + "/" + "cddev." + getProject(projects) + "." + getSprint(projects) + "." + startdate + "." + enddate;
        handleByDate(command, projects, startdate, enddate, reportdir, outputxml + ".xml");
        return outputxml;
    }

    public String getProject(String projects) {
        String[] inputsprints = projects.trim().split("\\,");
        return inputsprints[0];
    }

    public String getSprint(String projects) {
        String[] inputsprints = projects.trim().split("\\,");
        return inputsprints[1];
    }

    //
    public void handleByDate(String command, String projects, String startdate, String enddate, String reportDir, String outputxml) throws IOException, JSONException, URISyntaxException {
        factory = new AsynchronousJiraRestClientFactory();
        restClient = factory.createWithBasicHttpAuthentication(jiraServerUri, "testcase", "testcase9211");
        String[] sArray=projects.split("\\|");
        ObjectFactory factory= new ObjectFactory();
        Sprints sprints = factory.createSprints();

        for(int i =0; i < sArray.length ; i++) {
            String projectinfos = sArray[i];
            String[] inputsprints = projectinfos.split("\\,");
            generateInfo(inputsprints[0], inputsprints[1], startdate, enddate, sprints, factory);
        }

        try {
            JAXBContext jc = JAXBContext.newInstance(ObjectFactory.class);
            Marshaller marshaller = jc.createMarshaller();
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);

            marshaller.marshal(sprints, new PrintWriter(outputxml));
        } catch (Exception e) {
            e.printStackTrace();
        }
        restClient.close();
    }

    private void addLogDetail(ObjectFactory factory, Issue newissue, Worklog worklog, boolean isIssueAdded, Taskdetail taskdetail, Sprintdetail sprintdetail) {
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
            if (taskdetail != null) {
               taskdetail.getWorklogdetail().add(worklogdetail);
            } else {
                sprintdetail.getWorklogdetail().add(worklogdetail);
            }
        } finally {
        }
    }


    /*新建立一个工作流,工作流schema, 将test case与该工作流关联,并且将项目/workflows中, 右上角的actions关联type与工作流*/
    public void generateInfo(String projectName, String inputsprints, String startdate, String enddate, Sprints projectinfos, ObjectFactory factory) throws URISyntaxException, JSONException, IOException {
        int count = 0;
        int logOnStoryCount = 0;
        int logOnSubtaskCount = 0;
        boolean isParentIssueAdded = false, isSubissueAdded = false;
        Taskdetail parentTaskdetail = null;
        Sprintdetail parentSprintdetail = null;

        projectinfos.setName(projectName);
        Sprintdetails sprintdetails = factory.createSprintdetails();
        sprintdetails.setName(inputsprints);
        sprintdetails.setStartdate(sprintStartDate);
        sprintdetails.setEnddate(sprintEndDate);
        logger.info("------start project: " + projectName);

        String[] inputsprintsarray=inputsprints.split("\\*");
        for(int i =0; i < inputsprintsarray.length ; i++) {
            String inputsprint = inputsprintsarray[i];

        try {
            // let's now print all issues matching a JQL string (here: all assigned issues)
            //String jql = "project=\'" + projectName + "\' and issuetype not in subtaskIssueTypes() and issuekey=JMSB-8";
            // String jql = "project=\'" + projectName + "\'" + " and gh\\u002esprint\\u002ecustomfield\\u002edefault\\u002ename=\'" + inputsprint + "\'" + " and issuetype not in subtaskIssueTypes()";
            String jql = "project=\'" + projectName + "\'" + " and sprint=\'" + inputsprint + "\'" + " and issuetype not in subtaskIssueTypes()";
            logger.info("jql " + jql);
            final SearchResult searchResult = restClient.getSearchClient().searchJql(jql).claim();
            for (Issue issue : searchResult.getIssues()) {
                isParentIssueAdded = false;
                logOnStoryCount = logOnSubtaskCount = 0;
                logger.info("parent issue: " + issue.getKey().toString() + ", " + issue.getStatus().getName());
                Sprintdetail sprintdetail = factory.createSprintdetail();
                {
                    //Iterable<Worklog> worklogs = issue.getWorklogs();
                    final Issue newissue = restClient.getIssueClient().getIssue(issue.getKey()).claim();
                    Iterable<Worklog> worklogs = newissue.getWorklogs();
                    int logtimespent = 0;
                    int logworkcount = 0;
                    for (Worklog worklog : worklogs) {
                        logger.info("story:" + worklog.getComment() + ", " + worklog.getAuthor().getName());
                        DateTime datetime = worklog.getStartDate();
                        if (datetime.isAfter(new DateTime(startdate).getMillis()) && (datetime.isBefore(new DateTime(enddate).getMillis()))) {
                            logger.info("in time." + worklog.getAuthor().getName());
                            logtimespent += worklog.getMinutesSpent();
                            logworkcount += 1;
                            logOnStoryCount++;
                            /*统计某个人的工作量*/
                            addLogDetail(factory, issue, worklog, isParentIssueAdded, null, sprintdetail);
                        }
                    }
                    sprintdetail.setIssuekey(issue.getKey().toString());
                    sprintdetail.setName(issue.getAssignee().getName());
                    sprintdetail.setIssuesummary(issue.getSummary());
                    sprintdetail.setIssuetype(issue.getIssueType().getName());
                    sprintdetail.setIssuestatus(issue.getStatus().getName());
                    TimeTracking timeTracking = issue.getTimeTracking();
                    if (timeTracking != null) {
                        Integer timeSpentMinutes =  timeTracking.getTimeSpentMinutes();
                        sprintdetail.setIssuetimespent((double)(timeSpentMinutes/60));//hours
                    }
                    sprintdetail.setIssuelogtimespent((double)(logtimespent/60));
                    sprintdetail.setIssuelogworkcount(logworkcount);
                    IssueField issueField = newissue.getFieldByName("Story Points");//获得故事点数
                    if (issueField != null) {
                        Object object = issueField.getValue();
                        if (object != null) {
                            String valuation = object.toString();
                            sprintdetail.setIssuepoint(valuation);
                        } else {
                            sprintdetail.setIssuepoint("##");
                        }
                    } else {
                        sprintdetail.setIssuepoint("#?");
                    }
                    sprintdetails.getSprintdetail().add(sprintdetail);
                    logger.info("key=" + issue.getKey() + ", summary:" + issue.getSummary());
                }

                /*String subjql = "project=\'" + projectName + "\' and parent=\'" + issue.getKey() + "\'";
                System.out.println("subjql " + subjql);
                SearchResult subsearchResult = restClient.getSearchClient().searchJql(subjql).claim();
                System.out.println("subtask: " + subsearchResult.getTotal());*/
                for (Subtask mysubtask : issue.getSubtasks()) {
                    isSubissueAdded = false;
                    final Issue subissue = restClient.getIssueClient().getIssue(mysubtask.getIssueKey()).claim();
                    Iterable<Worklog> worklogs = subissue.getWorklogs();
                    System.out.println("in subtask " + Iterables.size(worklogs) + subissue.getWorklogUri().toString());
                    int logtimespent = 0;
                    int logworkcount = 0;
                    Taskdetail taskdetail = factory.createTaskdetail();
                    for (Worklog worklog : worklogs) {
                        logger.info("subtask:" + worklog.getComment() + ", " + worklog.getAuthor().getName());
                        DateTime datetime = worklog.getStartDate();
                        if (datetime.isAfter(new DateTime(startdate).getMillis()) && (datetime.isBefore(new DateTime(enddate).getMillis()))) {
                            logger.info("in time, subtask:" + worklog.getComment() + ", " + worklog.getAuthor().getName());
                            logtimespent += worklog.getMinutesSpent();
                            logworkcount += 1;
                            logOnSubtaskCount++;
                            addLogDetail(factory, subissue, worklog, isSubissueAdded, taskdetail, null);
                        }
                    }
                    taskdetail.setTaskkey(subissue.getKey().toString());
                    taskdetail.setName(subissue.getAssignee().getName());
                    taskdetail.setTasksummary(subissue.getSummary());
                    taskdetail.setTasktype(subissue.getIssueType().getName());
                    taskdetail.setTaskstatus(subissue.getStatus().getName());
                    TimeTracking timeTracking = subissue.getTimeTracking();
                    if (timeTracking != null) {
                        Integer timeSpentMinutes =  timeTracking.getTimeSpentMinutes();
                        if (timeSpentMinutes != null)
                            sprintdetail.setIssuetimespent((double)(timeSpentMinutes/60));//hours
                    }
                    taskdetail.setTasklogtimespent((double)(logtimespent/60));
                    taskdetail.setTasklogworkcount(logworkcount);
                    sprintdetail.getTaskdetail().add(taskdetail);
                }
            }
        } finally {
        }
        projectinfos.getSprintdetails().add(sprintdetails);
        logger.info("------end project: " + projectName);
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
}
