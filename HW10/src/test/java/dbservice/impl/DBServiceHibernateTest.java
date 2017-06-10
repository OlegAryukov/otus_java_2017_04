package dbservice.impl;

import dbservice.DBService;
import jpausage.SqlDao;
import jpausage.impl.ConnectionHelper;
import jpausage.impl.SqlDaoImpl;
import model.UserDataSet;
import org.junit.Before;
import org.junit.Test;

import java.sql.Connection;
import java.util.List;

import static junit.framework.TestCase.assertEquals;

/**
 * @author sergey
 *         created on 10.06.17.
 */
public class DBServiceHibernateTest {

    private void createUser() throws Exception {
        final String userName = "userName1" + System.currentTimeMillis();
        final Integer age = 10;
        final String insert = "insert into tuser(name, age) values (?, ?)";
        SqlDao sqlDao = new SqlDaoImpl(new ConnectionHelper().getConnection());
        sqlDao.executeUpdate(insert, ps -> {
            ps.setString(1, userName);
            ps.setInt(2, age);
        });
    }

    private UserDataSet getUser() throws Exception {
        SqlDao sqlDao = new SqlDaoImpl(new ConnectionHelper().getConnection());

        final String select = "select id, name, age from tuser";
        UserDataSet user = sqlDao.queryExecutor(select, null, rs -> {
            UserDataSet result = new UserDataSet();
            while (rs.next()) {
                result.setId(rs.getLong("id"));
                result.setName(rs.getString("name"));
                result.setAge(rs.getInt("age"));
            }
            return result;
        });
        sqlDao.close();
        return user;
    }

    @Before
    public void initTestTable() throws Exception {
        Connection connection = new ConnectionHelper().getConnection();
        SqlDaoImpl sqlDao = new SqlDaoImpl(connection);
        sqlDao.executeUpdate("delete from tuser", null);
        sqlDao.close();
    }

    @Test
    public void getLocalStatus() throws Exception {
        DBService dbService = new DBServiceHibernate();
        dbService.startup();
        String status = dbService.getLocalStatus();
        dbService.shutdown();

        System.out.println("status:" + status);
        assertEquals("userDataSet Age", "ACTIVE", status);

    }

    @Test
    public void save() throws Exception {
        final UserDataSet userDataSet = new UserDataSet();
        userDataSet.setName("userName");
        userDataSet.setAge(33);

        DBService dbService = new DBServiceHibernate();
        dbService.startup();
        dbService.save(userDataSet);
        dbService.shutdown();

        UserDataSet userDataSetFact = getUser();

        assertEquals("userDataSet Name", userDataSet.getName(), userDataSetFact.getName());
        assertEquals("userDataSet Age", userDataSet.getAge(), userDataSetFact.getAge());


    }

    @Test
    public void read() throws Exception {
        createUser();
        UserDataSet user = getUser();

        DBService dbService = new DBServiceHibernate();
        dbService.startup();
        UserDataSet userFact = dbService.read(user.getId());
        dbService.shutdown();
        ///
        System.out.println("loaded userFact:" + userFact);
        assertEquals("userDataSet", user, userFact);
    }

    @Test
    public void readByName() throws Exception {
        createUser();
        UserDataSet user = getUser();

        DBService dbService = new DBServiceHibernate();
        dbService.startup();
        UserDataSet userFact = dbService.readByName(user.getName());
        dbService.shutdown();

        ///
        System.out.println("loaded userFact:" + userFact);
        assertEquals("userDataSet", user, userFact);
    }

    @Test
    public void readAll() throws Exception {
        final int size = 5;
        for (int idx = 0; idx < size; idx++) {
            createUser();
        }

        DBService dbService = new DBServiceHibernate();
        dbService.startup();
        List<UserDataSet> userList = dbService.readAll();
        dbService.shutdown();

        System.out.println("loaded userFact:" + userList);
        assertEquals("userList size", size, userList.size());
    }



}