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

package com.atlassian.jira.rest.client.internal.json;

import com.atlassian.jira.rest.client.api.domain.Attachment;
import com.atlassian.jira.rest.client.api.domain.BasicComponent;
import com.atlassian.jira.rest.client.api.domain.BasicIssue;
import com.atlassian.jira.rest.client.api.domain.BasicIssueType;
import com.atlassian.jira.rest.client.api.domain.BasicPriority;
import com.atlassian.jira.rest.client.api.domain.BasicProject;
import com.atlassian.jira.rest.client.api.domain.BasicResolution;
import com.atlassian.jira.rest.client.api.domain.BasicStatus;
import com.atlassian.jira.rest.client.api.domain.BasicVotes;
import com.atlassian.jira.rest.client.api.domain.BasicWatchers;
import com.atlassian.jira.rest.client.api.domain.ChangelogGroup;
import com.atlassian.jira.rest.client.api.domain.Comment;
import com.atlassian.jira.rest.client.api.domain.Issue;
import com.atlassian.jira.rest.client.api.domain.IssueField;
import com.atlassian.jira.rest.client.api.domain.IssueFieldId;
import com.atlassian.jira.rest.client.api.domain.IssueLink;
import com.atlassian.jira.rest.client.api.domain.Subtask;
import com.atlassian.jira.rest.client.api.domain.TimeTracking;
import com.atlassian.jira.rest.client.api.domain.User;
import com.atlassian.jira.rest.client.api.domain.Version;
import com.atlassian.jira.rest.client.api.domain.Worklog;
import com.google.common.base.Splitter;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.joda.time.DateTime;

import javax.annotation.Nullable;
import javax.ws.rs.core.UriBuilder;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import static com.atlassian.jira.rest.client.api.domain.IssueFieldId.AFFECTS_VERSIONS_FIELD;
import static com.atlassian.jira.rest.client.api.domain.IssueFieldId.ASSIGNEE_FIELD;
import static com.atlassian.jira.rest.client.api.domain.IssueFieldId.ATTACHMENT_FIELD;
import static com.atlassian.jira.rest.client.api.domain.IssueFieldId.COMMENT_FIELD;
import static com.atlassian.jira.rest.client.api.domain.IssueFieldId.COMPONENTS_FIELD;
import static com.atlassian.jira.rest.client.api.domain.IssueFieldId.CREATED_FIELD;
import static com.atlassian.jira.rest.client.api.domain.IssueFieldId.DESCRIPTION_FIELD;
import static com.atlassian.jira.rest.client.api.domain.IssueFieldId.DUE_DATE_FIELD;
import static com.atlassian.jira.rest.client.api.domain.IssueFieldId.FIX_VERSIONS_FIELD;
import static com.atlassian.jira.rest.client.api.domain.IssueFieldId.ISSUE_TYPE_FIELD;
import static com.atlassian.jira.rest.client.api.domain.IssueFieldId.LABELS_FIELD;
import static com.atlassian.jira.rest.client.api.domain.IssueFieldId.LINKS_FIELD;
import static com.atlassian.jira.rest.client.api.domain.IssueFieldId.PRIORITY_FIELD;
import static com.atlassian.jira.rest.client.api.domain.IssueFieldId.PROJECT_FIELD;
import static com.atlassian.jira.rest.client.api.domain.IssueFieldId.REPORTER_FIELD;
import static com.atlassian.jira.rest.client.api.domain.IssueFieldId.RESOLUTION_FIELD;
import static com.atlassian.jira.rest.client.api.domain.IssueFieldId.STATUS_FIELD;
import static com.atlassian.jira.rest.client.api.domain.IssueFieldId.SUBTASKS_FIELD;
import static com.atlassian.jira.rest.client.api.domain.IssueFieldId.SUMMARY_FIELD;
import static com.atlassian.jira.rest.client.api.domain.IssueFieldId.TIMETRACKING_FIELD;
import static com.atlassian.jira.rest.client.api.domain.IssueFieldId.UPDATED_FIELD;
import static com.atlassian.jira.rest.client.api.domain.IssueFieldId.VOTES_FIELD;
import static com.atlassian.jira.rest.client.api.domain.IssueFieldId.WATCHER_FIELD;
import static com.atlassian.jira.rest.client.api.domain.IssueFieldId.WORKLOGS_FIELD;
import static com.atlassian.jira.rest.client.api.domain.IssueFieldId.WORKLOG_FIELD;
import static com.atlassian.jira.rest.client.internal.json.JsonParseUtil.getStringKeys;

public class IssueJsonParser implements JsonObjectParser<Issue> {

	private static Set<String> SPECIAL_FIELDS = Sets.newHashSet(IssueFieldId.ids());

	public static final String SCHEMA_SECTION = "schema";
	public static final String NAMES_SECTION = "names";

	private final BasicIssueJsonParser basicIssueJsonParser = new BasicIssueJsonParser();
	private final IssueLinkJsonParserV5 issueLinkJsonParserV5 = new IssueLinkJsonParserV5();
	private final BasicVotesJsonParser votesJsonParser = new BasicVotesJsonParser();
	private final BasicStatusJsonParser statusJsonParser = new BasicStatusJsonParser();
	private final JsonObjectParser<BasicWatchers> watchersJsonParser = WatchersJsonParserBuilder.createBasicWatchersParser();
	private final VersionJsonParser versionJsonParser = new VersionJsonParser();
	private final BasicComponentJsonParser basicComponentJsonParser = new BasicComponentJsonParser();
	private final AttachmentJsonParser attachmentJsonParser = new AttachmentJsonParser();
	private final CommentJsonParser commentJsonParser = new CommentJsonParser();
	private final BasicIssueTypeJsonParser issueTypeJsonParser = new BasicIssueTypeJsonParser();
	private final BasicProjectJsonParser projectJsonParser = new BasicProjectJsonParser();
	private final BasicPriorityJsonParser priorityJsonParser = new BasicPriorityJsonParser();
	private final BasicResolutionJsonParser resolutionJsonParser = new BasicResolutionJsonParser();
	private final UserJsonParser userJsonParser = new UserJsonParser();
	private final SubtaskJsonParser subtaskJsonParser = new SubtaskJsonParser();
	private final ChangelogJsonParser changelogJsonParser = new ChangelogJsonParser();
	private final JsonWeakParserForString jsonWeakParserForString = new JsonWeakParserForString();

	private static final String FIELDS = "fields";
	private static final String VALUE_ATTR = "value";

	private final JSONObject providedNames;
	private final JSONObject providedSchema;

	public IssueJsonParser() {
		providedNames = null;
		providedSchema = null;
	}

	public IssueJsonParser(final JSONObject providedNames, final JSONObject providedSchema) {
		this.providedNames = providedNames;
		this.providedSchema = providedSchema;
	}

	static Iterable<String> parseExpandos(final JSONObject json) throws JSONException {
		final String expando = json.getString("expand");
		return Splitter.on(',').split(expando);
	}


	private <T> Collection<T> parseArray(final JSONObject jsonObject, final JsonWeakParser<T> jsonParser, final String arrayAttribute)
			throws JSONException {
//        String type = jsonObject.getString("type");
//        final String name = jsonObject.getString("name");
		final JSONArray valueObject = jsonObject.optJSONArray(arrayAttribute);
		if (valueObject == null) {
			return new ArrayList<T>();
		}
		Collection<T> res = new ArrayList<T>(valueObject.length());
		for (int i = 0; i < valueObject.length(); i++) {
			res.add(jsonParser.parse(valueObject.get(i)));
		}
		return res;
	}

	private <T> Collection<T> parseOptionalArrayNotNullable(final JSONObject json, final JsonWeakParser<T> jsonParser, final String... path)
			throws JSONException {
		Collection<T> res = parseOptionalArray(json, jsonParser, path);
		return res == null ? Collections.<T>emptyList() : res;
	}

	@Nullable
	private <T> Collection<T> parseOptionalArray(final JSONObject json, final JsonWeakParser<T> jsonParser, final String... path)
			throws JSONException {
		final JSONArray jsonArray = JsonParseUtil.getNestedOptionalArray(json, path);
		if (jsonArray == null) {
			return null;
		}
		final Collection<T> res = new ArrayList<T>(jsonArray.length());
		for (int i = 0; i < jsonArray.length(); i++) {
			res.add(jsonParser.parse(jsonArray.get(i)));
		}
		return res;
	}

	private String getFieldStringValue(final JSONObject json, final String attributeName) throws JSONException {
		final JSONObject fieldsJson = json.getJSONObject(FIELDS);

		final Object summaryObject = fieldsJson.get(attributeName);
		if (summaryObject instanceof JSONObject) { // pre JIRA 5.0 way
			return ((JSONObject) summaryObject).getString(VALUE_ATTR);
		}
		if (summaryObject instanceof String) { // JIRA 5.0 way
			return (String) summaryObject;
		}
		throw new JSONException("Cannot parse [" + attributeName + "] from available fields");
	}

	private JSONObject getFieldUnisex(final JSONObject json, final String attributeName) throws JSONException {
		final JSONObject fieldsJson = json.getJSONObject(FIELDS);
		final JSONObject fieldJson = fieldsJson.getJSONObject(attributeName);
		if (fieldJson.has(VALUE_ATTR)) {
			return fieldJson.getJSONObject(VALUE_ATTR); // pre 5.0 way
		} else {
			return fieldJson; // JIRA 5.0 way
		}
	}

	@Nullable
	private String getOptionalFieldStringUnisex(final JSONObject json, final String attributeName)
			throws JSONException {
		final JSONObject fieldsJson = json.getJSONObject(FIELDS);
		return JsonParseUtil.getOptionalString(fieldsJson, attributeName);
	}

	private String getFieldStringUnisex(final JSONObject json, final String attributeName) throws JSONException {
		final JSONObject fieldsJson = json.getJSONObject(FIELDS);
		final Object fieldJson = fieldsJson.get(attributeName);
		if (fieldJson instanceof JSONObject) {
			return ((JSONObject) fieldJson).getString(VALUE_ATTR); // pre 5.0 way
		}
		return fieldJson.toString(); // JIRA 5.0 way
	}

	@Override
	public Issue parse(final JSONObject s) throws JSONException {
		final BasicIssue basicIssue = basicIssueJsonParser.parse(s);
		final Iterable<String> expandos = parseExpandos(s);
		final JSONObject jsonFields = s.getJSONObject(FIELDS);
		final JSONObject commentsJson = jsonFields.optJSONObject(COMMENT_FIELD.id);
		final Collection<Comment> comments = (commentsJson == null) ? Collections.<Comment>emptyList()
				: parseArray(commentsJson, new JsonWeakParserForJsonObject<Comment>(commentJsonParser), "comments");

		final String summary = getFieldStringValue(s, SUMMARY_FIELD.id);
		final String description = getOptionalFieldStringUnisex(s, DESCRIPTION_FIELD.id);

		final Collection<Attachment> attachments = parseOptionalArray(s, new JsonWeakParserForJsonObject<Attachment>(attachmentJsonParser), FIELDS, ATTACHMENT_FIELD.id);
		final Collection<IssueField> fields = parseFields(s);

		final BasicIssueType issueType = issueTypeJsonParser.parse(getFieldUnisex(s, ISSUE_TYPE_FIELD.id));
		final DateTime creationDate = JsonParseUtil.parseDateTime(getFieldStringUnisex(s, CREATED_FIELD.id));
		final DateTime updateDate = JsonParseUtil.parseDateTime(getFieldStringUnisex(s, UPDATED_FIELD.id));

		final String dueDateString = getOptionalFieldStringUnisex(s, DUE_DATE_FIELD.id);
		final DateTime dueDate = dueDateString == null ? null : JsonParseUtil.parseDateTimeOrDate(dueDateString);

		final BasicPriority priority = getOptionalNestedField(s, PRIORITY_FIELD.id, priorityJsonParser);
		final BasicResolution resolution = getOptionalNestedField(s, RESOLUTION_FIELD.id, resolutionJsonParser);
		final User assignee = getOptionalNestedField(s, ASSIGNEE_FIELD.id, userJsonParser);
		final User reporter = getOptionalNestedField(s, REPORTER_FIELD.id, userJsonParser);

		final BasicProject project = projectJsonParser.parse(getFieldUnisex(s, PROJECT_FIELD.id));
		final Collection<IssueLink> issueLinks;
		issueLinks = parseOptionalArray(s, new JsonWeakParserForJsonObject<IssueLink>(issueLinkJsonParserV5), FIELDS, LINKS_FIELD.id);

		Collection<Subtask> subtasks = parseOptionalArray(s, new JsonWeakParserForJsonObject<Subtask>(subtaskJsonParser), FIELDS, SUBTASKS_FIELD.id);

		final BasicVotes votes = getOptionalNestedField(s, VOTES_FIELD.id, votesJsonParser);
		final BasicStatus status = statusJsonParser.parse(getFieldUnisex(s, STATUS_FIELD.id));

		final Collection<Version> fixVersions = parseOptionalArray(s, new JsonWeakParserForJsonObject<Version>(versionJsonParser), FIELDS, FIX_VERSIONS_FIELD.id);
		final Collection<Version> affectedVersions = parseOptionalArray(s, new JsonWeakParserForJsonObject<Version>(versionJsonParser), FIELDS, AFFECTS_VERSIONS_FIELD.id);
		final Collection<BasicComponent> components = parseOptionalArray(s, new JsonWeakParserForJsonObject<BasicComponent>(basicComponentJsonParser), FIELDS, COMPONENTS_FIELD.id);

		final Collection<Worklog> worklogs;
		final URI selfUri = basicIssue.getSelf();

		final String transitionsUriString;
		if (s.has(IssueFieldId.TRANSITIONS_FIELD.id)) {
			Object transitionsObj = s.get(IssueFieldId.TRANSITIONS_FIELD.id);
			transitionsUriString = (transitionsObj instanceof String) ? (String) transitionsObj : null;
		} else {
			transitionsUriString = getOptionalFieldStringUnisex(s, IssueFieldId.TRANSITIONS_FIELD.id);
		}
		final URI transitionsUri = parseTransisionsUri(transitionsUriString, selfUri);

		if (JsonParseUtil.getNestedOptionalObject(s, FIELDS, WORKLOG_FIELD.id) != null) {
			worklogs = parseOptionalArray(s,
					new JsonWeakParserForJsonObject<Worklog>(new WorklogJsonParserV5(selfUri)),
					FIELDS, WORKLOG_FIELD.id, WORKLOGS_FIELD.id);
		} else {
			worklogs = Collections.emptyList();
		}


		final BasicWatchers watchers = getOptionalNestedField(s, WATCHER_FIELD.id, watchersJsonParser);
		final TimeTracking timeTracking = getOptionalNestedField(s, TIMETRACKING_FIELD.id, new TimeTrackingJsonParserV5());

		final Set<String> labels = Sets
				.newHashSet(parseOptionalArrayNotNullable(s, jsonWeakParserForString, FIELDS, LABELS_FIELD.id));

		final Collection<ChangelogGroup> changelog = parseOptionalArray(
				s, new JsonWeakParserForJsonObject<ChangelogGroup>(changelogJsonParser), "changelog", "histories");
		return new Issue(summary, selfUri, basicIssue.getKey(), basicIssue.getId(), project, issueType, status,
				description, priority, resolution, attachments, reporter, assignee, creationDate, updateDate,
				dueDate, affectedVersions, fixVersions, components, timeTracking, fields, comments,
				transitionsUri, issueLinks,
				votes, worklogs, watchers, expandos, subtasks, changelog, labels);
	}

	private URI parseTransisionsUri(final String transitionsUriString, final URI selfUri) {
		return transitionsUriString != null
				? JsonParseUtil.parseURI(transitionsUriString)
				: UriBuilder.fromUri(selfUri).path("transitions").queryParam("expand", "transitions.fields").build();
	}

	@Nullable
	private <T> T getOptionalNestedField(final JSONObject s, final String fieldId, final JsonObjectParser<T> jsonParser)
			throws JSONException {
		final JSONObject fieldJson = JsonParseUtil.getNestedOptionalObject(s, FIELDS, fieldId);
		// for fields like assignee (when unassigned) value attribute may be missing completely
		if (fieldJson != null) {
			return jsonParser.parse(fieldJson);
		}
		return null;
	}

	private Collection<IssueField> parseFields(final JSONObject issueJson) throws JSONException {
		final JSONObject names = (providedNames != null) ? providedNames : issueJson.optJSONObject(NAMES_SECTION);
		final Map<String, String> namesMap = parseNames(names);
		final JSONObject schema = (providedSchema != null) ? providedSchema : issueJson.optJSONObject(SCHEMA_SECTION);
		final Map<String, String> typesMap = parseSchema(schema);

		final JSONObject json = issueJson.getJSONObject(FIELDS);
		final ArrayList<IssueField> res = new ArrayList<IssueField>(json.length());
		@SuppressWarnings("unchecked")
		final Iterator<String> iterator = json.keys();
		while (iterator.hasNext()) {
			final String key = iterator.next();
			try {
				if (SPECIAL_FIELDS.contains(key)) {
					continue;
				}
				// TODO: JRJC-122
				// we should use fieldParser here (some new version as the old one probably won't work)
				// enable IssueJsonParserTest#testParseIssueWithUserPickerCustomFieldFilledOut after fixing this
				final Object value = json.opt(key);
				res.add(new IssueField(key, namesMap.get(key), typesMap.get("key"), value != JSONObject.NULL ? value : null));
			} catch (final Exception e) {
				throw new JSONException("Error while parsing [" + key + "] field: " + e.getMessage()) {
					@Override
					public Throwable getCause() {
						return e;
					}
				};
			}
		}
		return res;
	}

	private Map<String, String> parseSchema(final JSONObject json) throws JSONException {
		final HashMap<String, String> res = Maps.newHashMap();
		final Iterator<String> it = JsonParseUtil.getStringKeys(json);
		while (it.hasNext()) {
			final String fieldId = it.next();
			JSONObject fieldDefinition = json.getJSONObject(fieldId);
			res.put(fieldId, fieldDefinition.getString("type"));

		}
		return res;
	}

	private Map<String, String> parseNames(final JSONObject json) throws JSONException {
		final HashMap<String, String> res = Maps.newHashMap();
		final Iterator<String> iterator = getStringKeys(json);
		while (iterator.hasNext()) {
			final String key = iterator.next();
			res.put(key, json.getString(key));
		}
		return res;
	}

}