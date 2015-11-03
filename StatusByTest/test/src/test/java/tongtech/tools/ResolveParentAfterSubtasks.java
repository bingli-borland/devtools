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
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.apache.commons.io.filefilter.WildcardFileFilter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collection;

/**
 * A sample code how to use JRJC library
 *
 * @since v0.1
 */
public class ResolveParentAfterSubtasks {
    private static Logger logger = LogManager.getLogger(ResolveParentAfterSubtasks.class);

    private static URI jiraServerUri = URI.create("http://10.10.22.206:8081");
    //private static URI jiraServerUri = URI.create("http://127.0.0.1:8080");
    AsynchronousJiraRestClientFactory factory = null;
    JiraRestClient restClient = null;

    public static void main(String[] args) throws URISyntaxException, JSONException, IOException {
        if (args.length < 1) {
            System.out.println("usage: tongtech.tools.StatusByTestResult projectName");
            return;
        }
        String projects = args[0];
        String platform = null;
        if (args.length >= 3)
            platform = args[2];
        ResolveParentAfterSubtasks ex = new ResolveParentAfterSubtasks();
        ex.factory = new AsynchronousJiraRestClientFactory();
        ex.restClient = ex.factory.createWithBasicHttpAuthentication(jiraServerUri, "testcase", "testcase9211");
        String[] sArray=projects.split("\\|");
        for(int i =0; i < sArray.length ; i++) {
            String project = sArray[i];
            ex.transition(project, platform);
        }
        ex.restClient.close();
    }
/*新建立一个工作流,工作流schema, 将test case与该工作流关联,并且将项目/workflows中, 右上角的actions关联type与工作流*/
    public void transition(String projectName, String platform) throws URISyntaxException, JSONException, IOException {
        Issue issue = null;
        int count = 0;
        try {
            // let's now print all issues matching a JQL string (here: all assigned issues)
            //String jql = "project=\'" + projectName + "\' and issuetype not in subtaskIssueTypes() and issuekey=TLLM-114";
            String jql = "project=\'" + projectName + "\' and issuetype not in subtaskIssueTypes()";
            logger.debug("jql " + jql);
            final SearchResult searchResult = restClient.getSearchClient().searchJql(jql).claim();
            for (Issue iss : searchResult.getIssues()) {
                issue = iss;
                logger.info("parent issue: " + issue.getKey().toString() + ", " + issue.getStatus().getName());
                {
                    /*判断子任务有没有没完成的*/
                    int notpassedcount = 0;
                    String subjql = "project=\'" + projectName + "\' and parent=\'" + iss.getKey() + "\'";
                    logger.debug("subjql " + subjql);
                    SearchResult subsearchResult = restClient.getSearchClient().searchJql(subjql).claim();
                    for (Issue subissue : subsearchResult.getIssues()) {
                        notpassedcount++;
                    }
                    if (notpassedcount == 0) {/*没有子任务,忽略*/
                        logger.info("not find subissue. " + iss.getKey().toString());
                        continue;
                    }
                    notpassedcount = 0;
                    subjql = "project=\'" + projectName + "\' and parent=\'" + iss.getKey() + "\' and status!=Passed";
                    //subjql = "project=\'" + projectName + "\' and parent=\'" + iss.getKey() + "\' and status!=Resolved";
                    logger.debug("subjql " + subjql);
                    subsearchResult = restClient.getSearchClient().searchJql(subjql).claim();
                    for (Issue subissue : subsearchResult.getIssues()) {
                        logger.info("some status not passed, parent:" + issue.getKey().toString() + ", subtask:" + subissue.getKey().toString() + ", " + subissue.getStatus().getName() + ", " + subissue.getId().toString());
                        notpassedcount++;
                    }
                    if (notpassedcount > 0) { /*转换parent到没有完成*/
                        logger.info("some status not passed, " + issue.getKey().toString() + ", " + notpassedcount);
                        String currentStatus = issue.getStatus().getName();
                        logger.info("currentstatus: " + currentStatus);
                        if (!currentStatus.equals("Failed")) {
                            logger.info("some status not passed, " + issue.getKey().toString() + ", not failed, transfer to failed.");
                            TestCaseTransit.toFailed(restClient, issue);
                        }
                    } else {/*完成*/
                        logger.info("all passed, " + issue.getKey().toString());
                        String currentStatus = issue.getStatus().getName();
                        logger.info("currentstatus: " + currentStatus);
                        if (!currentStatus.equals("Passed")) {
                            logger.info("transfer to passed, " + issue.getKey().toString());
                            TestCaseTransit.toPassed(restClient, issue);
                        }
                    }
                }
            }
        } finally {
        }
    }
}
