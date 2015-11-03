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
import com.atlassian.jira.rest.client.api.domain.input.TransitionInput;
import com.atlassian.jira.rest.client.internal.async.AsynchronousJiraRestClientFactory;
import com.atlassian.jira.rest.client.internal.json.BasicIssueJsonParser;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.DirectoryFileFilter;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.apache.commons.io.filefilter.RegexFileFilter;
import org.apache.commons.io.filefilter.WildcardFileFilter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.codehaus.jettison.json.JSONException;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;

import org.codehaus.jettison.json.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;
import org.w3c.dom.Element;
import java.io.File;
import java.util.Collection;

/**
 * A sample code how to use JRJC library
 *
 * @since v0.1
 */
public class StatusByTestResult {
    private static Logger logger = LogManager.getLogger(StatusByTestResult.class);

    private static URI jiraServerUri = URI.create("http://10.10.22.206:8081");
    private static boolean quiet = false;
    private static String projectName = "IncuTestManager";
    AsynchronousJiraRestClientFactory factory = null;
    JiraRestClient restClient = null;

    public static void main(String[] args) throws URISyntaxException, JSONException, IOException {
        logger.info("start...");
        if (args.length < 2) {
            System.out.println("usage: tongtech.tools.StatusByTestResult projectName xmlName");
            return;
        }
        String project = args[0];
        String xmlfile = args[1];
        String platform = null;
        if (args.length >= 3)
            platform = args[2].trim();
        StatusByTestResult ex = new StatusByTestResult();

        /**
        ex.factory = new AsynchronousJiraRestClientFactory();
        ex.restClient = ex.factory.createWithBasicHttpAuthentication(jiraServerUri, "testcase", "testcase9211");
        platform = "接口";
        ex.transition("TI-EDGE-TEST", platform, "com.tongtech.edge.client.test.CreateSessionITCase.testCreateSession", false);
        if (true) return;
        **/

        File userFile = new File(xmlfile);
        if (userFile.isDirectory()) {
            Collection<File> files = FileUtils.listFiles(
                    new File(xmlfile),
                    new WildcardFileFilter("*.xml")/*new RegexFileFilter("*.xml")*/,
                    FileFilterUtils.trueFileFilter()
            );
            for (File file : files) {
                logger.info("file " + file.getAbsolutePath());
                ex.readXmlAndTransition(file.getAbsolutePath()/*"E:/Study/atlassian/test.xml"*/, project/*projectName*/, platform);
            }
        } else {
            ex.readXmlAndTransition(xmlfile/*"E:/Study/atlassian/test.xml"*/, project/*projectName*/, platform);
        }

    }

    public void readXmlAndTransition(String filename, String projectName, String platform) throws IOException {
        try {
            factory = new AsynchronousJiraRestClientFactory();
            restClient = factory.createWithBasicHttpAuthentication(jiraServerUri, "testcase", "testcase9211");

            File fXmlFile = new File(filename);
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(fXmlFile);

            //optional, but recommended
            //read this - http://stackoverflow.com/questions/13786607/normalization-in-dom-parsing-with-java-how-does-it-work
            doc.getDocumentElement().normalize();

            System.out.println("Root element :" + doc.getDocumentElement().getNodeName());

            NodeList nList = doc.getElementsByTagName("testcase");
            for (int temp = 0; temp < nList.getLength(); temp++) {/*遍历testcase节点*/
                Node nNode = nList.item(temp);
                System.out.println("\nCurrent Element :" + nNode.getNodeName());
                if (nNode.getNodeType() == Node.ELEMENT_NODE) {
                    Element eElement = (Element) nNode;
                    String className = eElement.getAttribute("classname");
                    if (className == null) {
                        className = "";
                    }
                    String functionName = eElement.getAttribute("name");
                    NodeList errorList = eElement.getElementsByTagName("error"); /*有error或者failure代表失败*/
                    NodeList failureList = eElement.getElementsByTagName("failure");
                    String key = className + "." + functionName;
                    int totalError =  errorList.getLength() + failureList.getLength();
                    logger.info(projectName + ", " + key + ", " + totalError);
                    try {
                        transition(projectName, platform, key, totalError == 0);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            restClient.close();
        }
    }

    public void transition(String projectName, String platform, String key, boolean isPassed) throws URISyntaxException, JSONException, IOException {
        Issue issue = null;
        int count = 0;

        logger.info("project: " + projectName + ", platform: " + platform + ", key: " + key);
        try {
            // let's now print all issues matching a JQL string (here: all assigned issues)
            key = key.replace(".", "\\u002e");/*"."是jira保留字*/
            String jql = "project=\'" + projectName + "\' and cf[10203] ~ " + key;
            System.out.println("jql " + jql);
            final SearchResult searchResult = restClient.getSearchClient().searchJql(jql).claim();
            for (Issue iss : searchResult.getIssues()) {
                if (platform == null) {
                    issue = iss;
                    count++;
                } else {
                    IssueField issueParent = iss.getField("parent"); /*获得parent issue*/
                    if (issueParent != null) {
                        JSONObject jsonParent = (JSONObject)issueParent.getValue();
                        BasicIssue bi = null;
                        try {
                            bi = new BasicIssueJsonParser().parse(jsonParent);
                        } catch (JSONException e1) {
                            e1.printStackTrace();
                        }
                        logger.info("parent: " + bi.getKey());
                        final Issue parentIssue = restClient.getIssueClient().getIssue(bi.getKey()).claim();
                        String parentSummary = parentIssue.getSummary();
                        try {
                            parentSummary = new String(parentSummary.getBytes(), "UTF-8");
                        } catch (Exception e) {
                            logger.error(e);
                        }
                        logger.info("parent: " + parentIssue.getKey() + ", <" + parentSummary + ">, <" + platform + ">" + ", " + parentSummary.indexOf(platform));
                        if (parentSummary.indexOf(platform) != -1) {    /*找到对应平台*/
                            issue = iss;
                            count++;
                            logger.info("found platform and key. " + issue.getKey());
                        }
                    }
                }
                if (count > 1) {   /*多于1个，重复了名称，提示错误*/
                    logger.info("find same implemention. " + iss.getKey() + ", " + key);
                    throw new RuntimeException("duplication error.");
                }
            }
            if (issue == null) {/*没有找到，提示*/
                logger.info("cannot find the implemetation. " + key);
                return;
            }
            if (isPassed) {
                TestCaseTransit.toPassed(restClient, issue);
            } else {
                TestCaseTransit.toFailed(restClient, issue);
            }
        } finally {
        }
    }
}
