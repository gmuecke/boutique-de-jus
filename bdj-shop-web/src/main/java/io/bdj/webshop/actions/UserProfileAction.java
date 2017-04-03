package io.bdj.webshop.actions;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import com.opensymphony.xwork2.ActionSupport;

/**
 *
 */
public class UserProfileAction extends ActionSupport  {

    private List<String> names;

    @Override
    public String execute() throws Exception {

        String dbURL = "jdbc:derby://localhost:1527/testdb?create=true";
        Class.forName("org.apache.derby.jdbc.ClientDriver").newInstance();
        //Get a connection
        try(Connection conn = DriverManager.getConnection(dbURL);
            PreparedStatement statement = conn.prepareStatement("SELECT * from USERS")) {
                ResultSet resultSet = statement.executeQuery();

                List<String> names = new ArrayList<>();
                while (resultSet.next()) {

                    String user = resultSet.getString("name");
                    names.add(user);
                }
                this.names = names;
        }


        return SUCCESS;
    }

    public List<String> getNames() {

        return names;
    }

    public void setNames(final List<String> names) {

        this.names = names;
    }
}
