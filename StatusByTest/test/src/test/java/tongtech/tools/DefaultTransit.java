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
import com.atlassian.jira.rest.client.api.domain.Transition;
import com.atlassian.jira.rest.client.api.domain.input.TransitionInput;

import java.util.Collections;

/**
 * A sample code how to use JRJC library
 *
 * @since v0.1
 */
public class DefaultTransit {
    public static final int START_PROGRESS_TRANSITION_ID = 4;
    public static final int STOP_PROGRESS_TRANSITION_ID = 301;
    public static void toPassed(JiraRestClient restClient, Issue issue) {
        toInProgress(restClient, issue);
        /*必须在这里获得, 因为上面转换了状态*/
        final Iterable<Transition> transitions = restClient.getIssueClient().getTransitions(issue.getTransitionsUri()).claim();
        final Transition passTransition = getTransitionByName(transitions, "Resolve Issue");
        restClient.getIssueClient().transition(issue.getTransitionsUri(), new TransitionInput(passTransition.getId())).claim();
    }

    public static void toFailed(JiraRestClient restClient, Issue issue) {
        toInProgress(restClient, issue);
        /*do nothing*/
    }

    public static void toInProgress(JiraRestClient restClient, Issue issue) {
        Iterable<Transition> transitions = null;
        Transition assignToRunTransition = null, inProgressTransition = null, reRunTransition = null;
        String statusName =  issue.getStatus().getName();
        System.out.println("status is " + statusName + "/" + issue.getTransitionsUri().toString());
        if (statusName.equals("Create") || statusName.equals("Open") || statusName.equals("Reopened")) {
            //do nothing
            transitions = restClient.getIssueClient().getTransitions(issue.getTransitionsUri()).claim();
            inProgressTransition = getTransitionByName(transitions, "Start Progress");
            restClient.getIssueClient().transition(issue.getTransitionsUri(), new TransitionInput(4/*inProgressTransition.getId()*/)).claim();
        } else if (statusName.equals("In Progress")) {
            /*只能在这里获得, 因为状态不同, transition不同*/
        } else if (statusName.equals("Resolved")) {
            transitions = restClient.getIssueClient().getTransitions(issue.getTransitionsUri()).claim();
            inProgressTransition = getTransitionByName(transitions, "Reopen Issue");
            restClient.getIssueClient().transition(issue.getTransitionsUri(), new TransitionInput(inProgressTransition.getId())).claim();
            transitions = restClient.getIssueClient().getTransitions(issue.getTransitionsUri()).claim();
            assignToRunTransition = getTransitionByName(transitions, "Start Progress");
            restClient.getIssueClient().transition(issue.getTransitionsUri(), new TransitionInput(assignToRunTransition.getId())).claim();
        } else if (statusName.equals("Closed")) {
            System.out.println(issue.getId() + ", " + statusName + ", closed, do nothing.");
        } else {
            /*状态修改过, 程序没有改动.*/
            throw new RuntimeException("status error.");
        }
    }

    public static Transition getTransitionByName(Iterable<Transition> transitions, String transitionName) {
        for (Transition transition : transitions) {
            String tname = transition.getName();
            if (tname.equals(transitionName)) {
                return transition;
            }
        }
        return null;
    }

}
