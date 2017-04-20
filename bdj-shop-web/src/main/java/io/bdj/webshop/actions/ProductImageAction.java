package io.bdj.webshop.actions;

import static org.slf4j.LoggerFactory.getLogger;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.opensymphony.xwork2.ActionSupport;
import org.apache.commons.io.IOUtils;
import org.apache.struts2.interceptor.ServletRequestAware;
import org.slf4j.Logger;

/**
 *
 */
public class ProductImageAction extends ActionSupport implements ServletRequestAware {

    private static final Logger LOG = getLogger(ProductImageAction.class);
    byte[] imageInByte = null;
    String id;
    private String dbURL = "jdbc:derby://localhost:1527/testdb";
    private HttpServletRequest servletRequest;

    @Override
    public String execute() throws IOException {

        this.imageInByte = getImageData(id);

        return SUCCESS;
    }

    private byte[] getImageData(final String id) throws IOException {

        try {
            Class.forName("org.apache.derby.jdbc.ClientDriver").newInstance();
            try (Connection conn = DriverManager.getConnection(dbURL);
                 //PERF fetch all columns although we only need 1 image
                 PreparedStatement statement = conn.prepareStatement("SELECT * FROM BOUTIQUE.PRODUCTS WHERE id = ?")) {

                statement.setString(1, id);
                final ResultSet rs = statement.executeQuery();
                if (rs.next()) {
                    return rs.getBytes("image");
                }
            } catch (SQLException e) {
                LOG.error("Could not fetch image for id {}", id, e);
            }
        } catch (InstantiationException | IllegalAccessException | ClassNotFoundException e) {
            LOG.error("Could not load DB Driver", e);
        }
        //TODO throw proper exception when image data could not be fetched
        return IOUtils.toByteArray(getUndefinedImage());
    }

    private InputStream getUndefinedImage() {

        return getClass().getResourceAsStream("/images/undefined.png");
    }

    public String getId() {

        return id;
    }

    public void setId(final String id) {

        this.id = id;
    }

    public byte[] getImageInBytes() {

        return imageInByte;
    }

    public String getContentType() {

        return "image/jpeg";
    }

    public String getCustomContentDisposition() {

        return "anyname.jpg";
    }

    @Override
    public void setServletRequest(HttpServletRequest request) {

        this.servletRequest = request;

    }

}
