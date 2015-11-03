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
import java.util.List;

/**
 * Created by liuhf on 14-4-1.
 */
public class LogstatJDBCTemplate implements LogstatDAO {
    private static Logger logger = LogManager.getLogger(LogstatJDBCTemplate.class);
    private DataSource dataSource;
    private JdbcTemplate jdbcTemplateObject;
    final Timestamp logstatFromDate = GetProjectInfo.logstatFromDate;

    public void setDataSource(DataSource dataSource) {
        this.dataSource = dataSource;
        this.jdbcTemplateObject = new JdbcTemplate(dataSource);
    }

    public List<Peoplestat> getTotalLogstat() {
        List<Peoplestat> peoplestat = this.jdbcTemplateObject.query(
                "select author, sum(timeworked)/3600 as \"timespent\", count(*) as \"logcount\" from worklog where updated >= ? group by author",
                new Object[]{logstatFromDate},
                new RowMapper<Peoplestat>() {
                    public Peoplestat mapRow(ResultSet rs, int rowNum) throws SQLException {
                        Peoplestat peoplestat = new Peoplestat();
                        peoplestat.setName(rs.getString("author"));
                        double timespent = rs.getDouble("timespent");
                        peoplestat.setLogtimespent(timespent);
                        peoplestat.setTimespent(0.0); //初始化为0，getProjectInfo中要累积。
                        peoplestat.setEarnedvalue(0.0);
                        peoplestat.setLogworkcount(rs.getInt("logcount"));
                        peoplestat.setMemo("");
                        return peoplestat;
                    }
                });
        return peoplestat;
    }

    //覆盖上面计算的memo和timespent，一天最多计算8小时，加班的通过excel等单独记录。
    public List<Peoplestat> getLogstatByDayAuthor() {
        final int STANDARD_WORK_TIME = 8;

        List<Peoplestat> peoplestat = this.jdbcTemplateObject.query(
                "select author, sum(timeworked)/3600 as \"timespent\", date(updated) as \"date\" from worklog where updated > ? group by date(updated), author order by author, date(updated)",
                new Object[]{logstatFromDate},
                new RowMapper<Peoplestat>() {
                    public Peoplestat mapRow(ResultSet rs, int rowNum) throws SQLException {
                        Peoplestat peoplestat = new Peoplestat();
                        peoplestat.setName(rs.getString("author"));
                        double timespent = rs.getDouble("timespent");
                        if (timespent > STANDARD_WORK_TIME) {//工作时间大约8小时
                            String date = rs.getString("date");
                            peoplestat.setMemo(date + "**" + timespent);
                            peoplestat.setTimespent(timespent - STANDARD_WORK_TIME);//多算的时间
                            logger.info("加班" + peoplestat.getName() + ", " + peoplestat.getMemo());
                        } else {
                            peoplestat.setName("正常,不记录");
                        }
                        return peoplestat;
                    }
                });
        return peoplestat;
    }
}
