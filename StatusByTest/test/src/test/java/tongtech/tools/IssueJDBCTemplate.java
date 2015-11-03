package tongtech.tools;

import com.tongtech.project.Jiraissue;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

import javax.sql.DataSource;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

/**
 * Created by liuhf on 14-4-1.
 */
public class IssueJDBCTemplate implements IssueDAO {
    private static Logger logger = LogManager.getLogger(IssueJDBCTemplate.class);
    private DataSource dataSource;
    private JdbcTemplate jdbcTemplateObject;
    final String logstatFromDate = "2014-8-3";

    public void setDataSource(DataSource dataSource) {
        this.dataSource = dataSource;
        this.jdbcTemplateObject = new JdbcTemplate(dataSource);
    }

    public Jiraissue getIssueByPKey(String pkey) {
        Jiraissue jiraissue = this.jdbcTemplateObject.queryForObject(
                "select id, pkey, assignee, issuetype, summary, timeoriginalestimate/3600 as \"timeoriginalestimate\", timespent/3600 as \"timespent\" from jiraissue where pkey = ?",
                new Object[]{pkey},
                new RowMapper<Jiraissue>() {
                    public Jiraissue mapRow(ResultSet rs, int rowNum) throws SQLException {
                        Jiraissue jiraissue = new Jiraissue();
                        jiraissue.setId(rs.getInt("id"));
                        jiraissue.setPkey(rs.getString("pkey"));
                        jiraissue.setAssignee(rs.getString("assignee"));
                        jiraissue.setType(rs.getInt("issuetype"));
                        jiraissue.setMessage(rs.getString("summary"));
                        jiraissue.setTimeoriginalestimate((double)rs.getInt("timeoriginalestimate"));
                        jiraissue.setTimespent((double)rs.getInt("timespent"));
                        return jiraissue;
                    }
                });
        return jiraissue;
    }

}
