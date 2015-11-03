package tongtech.tools;

import com.tongtech.project.Peoplestat;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

import javax.sql.DataSource;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.List;

/**
 * Created by liuhf on 14-4-1.
 */
public class EvStatJDBCTemplate {
    private static Logger logger = LogManager.getLogger(EvStatJDBCTemplate.class);
    private DataSource dataSource;
    private JdbcTemplate jdbcTemplateObject;
    final Timestamp logstatFromDate = GetProjectInfo.logstatFromDate;
    private int customfieldvalueFrom = 16000;

    public void setDataSource(DataSource dataSource) {
        this.dataSource = dataSource;
        this.jdbcTemplateObject = new JdbcTemplate(dataSource);
    }

    public List<Peoplestat> getTotalEvstat() {
        List<Peoplestat> peoplestat = this.jdbcTemplateObject.query(
                "SELECT jiraissue.id," +
                        "jiraissue.pkey," +
                        "project.pname AS project," +
                        "jiraissue.summary," +
                        "issuetype.pname AS issuetype," +
                        "issuestatus.pname AS status," +
                        "resolution.pname as resolution," +
                        "jiraissue.assignee," +
                        "jiraissue.reporter," +
                        "jiraissue.created," +
                        "jiraissue.resolutiondate," +
                        "jiraissue.duedate," +
                        "jiraissue.description," +
                        "jiraissue.TIMEORIGINALESTIMATE," +
                        "jiraissue.TIMEESTIMATE," +
                        "jiraissue.TIMESPENT," +
                        "(select cfv1.numbervalue from customfieldvalue cfv1 where cfv1.issue = jiraissue.id and cfv1.customfield = 10502)as ratio," +
                        "(select cfv2.numbervalue from customfieldvalue cfv2 where cfv2.issue = jiraissue.id and cfv2.customfield = 10601)as pv " +
                        "FROM jiraissue " +
                        "join project on jiraissue.project=project.ID " +
                        "join issuetype on jiraissue.issuetype=issuetype.ID " +
                        "join issuestatus on jiraissue.issuestatus=issuestatus.ID " +
                        "left outer join resolution on jiraissue.resolution=resolution.ID " +
                        "WHERE jiraissue.created > ? and jiraissue.issuestatus=\'6\'",  /*6=closed, UPDATED*/
                new Object[]{logstatFromDate},
                new RowMapper<Peoplestat>() {
                    public Peoplestat mapRow(ResultSet rs, int rowNum) throws SQLException {
                        String issueid = rs.getString("id");
                        String pkey = rs.getString("pkey");
                        String project = rs.getString("project");
                        String summary = rs.getString("summary");
                        String issuetype = rs.getString("issuetype");
                        String status = rs.getString("status");
                        String resolution = rs.getString("resolution");
                        String assignee = rs.getString("assignee");
                        String reporter = rs.getString("reporter");
                        String created = rs.getString("created");
                        String resolutiondate = rs.getString("resolutiondate");
                        String duedate = rs.getString("duedate");
                        String description = rs.getString("description");
                        Integer TIMEORIGINALESTIMATE = rs.getInt("TIMEORIGINALESTIMATE");
                        Integer TIMEESTIMATE = rs.getInt("TIMEESTIMATE");
                        Integer TIMESPENT = rs.getInt("TIMESPENT");
                        Double ratio = rs.getDouble("ratio");
                        Double pv = rs.getDouble("pv");
                        Peoplestat peoplestat = new Peoplestat();
                        peoplestat.setName(assignee);
                        peoplestat.setEarnedvalue(ratio * pv);
                        try {
                            //如果要设置一些值，可能where条件中不能设置closed，否则查询出来的不够。
                            logger.info(project + ", pkey=" + pkey + ", ratio=" + ratio + ", pv=" + pv + ", timeoriginal=" + TIMEORIGINALESTIMATE);
                            if (project.equalsIgnoreCase("PDS1") && ratio != null && ratio > 0 && (pv == null || pv == 0)) {
                                if (TIMEORIGINALESTIMATE != null && TIMEORIGINALESTIMATE > 0) {
                                    logger.info("timeoriginalestimate->pv:2:" + "insert into customfieldvalue(id, issue, " +
                                            "customfield, numbervalue) values(" + customfieldvalueFrom + ", " + issueid + ", 10601, " + TIMEORIGINALESTIMATE/(60.0 * 8.0 * 60.0) + ");");
                                    customfieldvalueFrom++;
                                }
                            }
                            if (TIMEORIGINALESTIMATE == null || TIMEORIGINALESTIMATE == 0) {
                                if (pv != null && pv > 0) {//将pv的值放到timeoriginalestimate中
                                    logger.info("pv->timeoriginalestimate, update jiraissue set timeoriginalestimate = " + pv * 60.0 * 8.0 * 60.0 + "where id = " + issueid);
                                }
                            }
                        } catch (Exception e) {
                            logger.error(e);
                        }
                        return peoplestat;
                    }
                });
        return peoplestat;
    }

    public void updateJiraissue(int timeoriginalestimate, long id) {
        String updateSql = "UPDATE jiraissue SET timeoriginalestimate = ? WHERE id = ?";
        // define query arguments
        Object[] params = { timeoriginalestimate, id};
        // define SQL types of the arguments
        int[] types = {Types.DECIMAL, Types.DECIMAL};
        int rows = this.jdbcTemplateObject.update(updateSql, params, types);
        System.out.println(rows + " row(s) updated.");
    }

}
