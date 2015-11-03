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
import com.atlassian.jira.rest.client.api.domain.Issue;
import com.atlassian.jira.rest.client.api.domain.SearchResult;
import com.atlassian.jira.rest.client.api.domain.Transition;
import com.atlassian.jira.rest.client.api.domain.input.TransitionInput;
import com.atlassian.jira.rest.client.internal.async.AsynchronousJiraRestClientFactory;
import org.codehaus.jettison.json.JSONException;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

/**
 * A sample code how to use JRJC library
 *
 * @since v0.1
 */
public class TestCaseTransit {
    public static void toPassed(JiraRestClient restClient, Issue issue) {
        toInProgress(restClient, issue);
        /*必须在这里获得, 因为上面转换了状态*/
        final Iterable<Transition> transitions = restClient.getIssueClient().getTransitions(issue.getTransitionsUri()).claim();
        final Transition passTransition = getTransitionByName(transitions, "Pass");
        if (passTransition == null) {
            System.out.println("!!!status error:" + issue.getKey().toString() + ", " + issue.getStatus().getName() + ", " + issue.getId().toString());
        } else {
            restClient.getIssueClient().transition(issue.getTransitionsUri(), new TransitionInput(passTransition.getId())).claim();
        }
    }

    public static void toFailed(JiraRestClient restClient, Issue issue) {
        toInProgress(restClient, issue);
        final Iterable<Transition> transitions = restClient.getIssueClient().getTransitions(issue.getTransitionsUri()).claim();
        final Transition failTransition = getTransitionByName(transitions, "Fail");
        if (failTransition == null) {
            System.out.println("!!!status error:" + issue.getKey().toString() + ", " + issue.getStatus().getName() + ", " + issue.getId().toString());
        } else {
            restClient.getIssueClient().transition(issue.getTransitionsUri(), new TransitionInput(failTransition.getId())).claim();
        }
    }

    public static void toInProgress(JiraRestClient restClient, Issue issue) {
        Iterable<Transition> transitions = null;
        Transition assignToRunTransition = null, inProgressTransition = null, reRunTransition = null;
        String statusName =  issue.getStatus().getName();
        System.out.println("status is " + statusName + "/");
        if (statusName.equals("Create") || statusName.equals("Open") || statusName.equals("Awaiting Review")) {
            //do nothing
            System.out.println(issue.getId() + ", " + statusName + ", not ready, do nothing.");
        } else if (statusName.equals("Template")) {
            /*只能在这里获得, 因为状态不同, transition不同*/
            transitions = restClient.getIssueClient().getTransitions(issue.getTransitionsUri()).claim();
            assignToRunTransition = getTransitionByName(transitions, "Assign To Run");
            restClient.getIssueClient().transition(issue.getTransitionsUri(), new TransitionInput(assignToRunTransition.getId())).claim();
            transitions = restClient.getIssueClient().getTransitions(issue.getTransitionsUri()).claim();
            inProgressTransition = getTransitionByName(transitions, "In Progress");
            restClient.getIssueClient().transition(issue.getTransitionsUri(), new TransitionInput(inProgressTransition.getId())).claim();
        } else if (statusName.equals("Ready To Run")) {
            transitions = restClient.getIssueClient().getTransitions(issue.getTransitionsUri()).claim();
            inProgressTransition = getTransitionByName(transitions, "In Progress");
            restClient.getIssueClient().transition(issue.getTransitionsUri(), new TransitionInput(inProgressTransition.getId())).claim();
        } else if (statusName.equals("In Progress")) {
            /*do nothing*/
        } else if (statusName.equals("Passed") || statusName.equals("Failed")) {
            transitions = restClient.getIssueClient().getTransitions(issue.getTransitionsUri()).claim();
            reRunTransition = getTransitionByName(transitions, "Rerun");
            restClient.getIssueClient().transition(issue.getTransitionsUri(), new TransitionInput(reRunTransition.getId())).claim();
            transitions = restClient.getIssueClient().getTransitions(issue.getTransitionsUri()).claim();
            inProgressTransition = getTransitionByName(transitions, "In Progress");
            restClient.getIssueClient().transition(issue.getTransitionsUri(), new TransitionInput(inProgressTransition.getId())).claim();
        } else if (statusName.equals("Closed") || statusName.equals("Invalid")) {
            /*do nothing, 不做任何处理*/
        } else {
            /*状态修改过, 程序没有改动.*/
            throw new RuntimeException("status error.");
        }
    }

    public static Transition getTransitionByName(Iterable<Transition> transitions, String transitionName) {
        for (Transition transition : transitions) {
            if (transition.getName().equals(transitionName)) {
                return transition;
            }
        }
        return null;
    }

}
