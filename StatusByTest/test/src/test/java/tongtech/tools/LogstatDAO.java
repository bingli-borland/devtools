package tongtech.tools;

import com.tongtech.project.Peoplestat;

import javax.sql.DataSource;
import java.util.List;

/**
 * Created by liuhf on 14-4-1.
 */
public interface LogstatDAO {
    /**
     * This is the method to be used to initialize
     * database resources ie. connection.
     */
    public void setDataSource(DataSource ds);

    public List<Peoplestat> getTotalLogstat();
}
