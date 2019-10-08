package io.bdj.webshop.actions;

import static org.slf4j.LoggerFactory.getLogger;

import javax.servlet.http.HttpServletRequest;
import java.io.InputStream;

import com.opensymphony.xwork2.ActionSupport;
import io.bdj.webshop.service.ProductService;
import io.bdj.webshop.service.ServiceException;
import io.bdj.webshop.service.Services;
import org.apache.struts2.interceptor.ServletRequestAware;
import org.slf4j.Logger;

/**
 *
 */
public class ProductImageAction extends ActionSupport implements ServletRequestAware {

    private static final Logger LOG = getLogger(ProductImageAction.class);

    private ProductService productService = Services.getService(ProductService.class);
    byte[] imageInByte = null;
    String id;
    private HttpServletRequest servletRequest;

    @Override
    public String execute() throws ServiceException {

        try{
            this.imageInByte = productService.getImageData(id);
        } catch (Exception e){
            LOG.error("Could not fetch image", e);
            return ERROR;
        }

        return SUCCESS;
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
