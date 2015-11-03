/*
 * Copyright (C) 2010 Atlassian
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.atlassian.jira.rest.client.api;

import com.atlassian.jira.rest.client.api.domain.User;
import com.atlassian.util.concurrent.Promise;

/**
 * The com.atlassian.jira.rest.client.api handling user resources.
 *
 * @since v0.1
 */
public class UserRestClient {
	Promise<User> getUser(final String username) {
		return null;
	}
}
