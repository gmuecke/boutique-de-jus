package io.bdj.webshop.result;

import javax.servlet.http.HttpServletResponse;

import com.opensymphony.xwork2.ActionInvocation;
import com.opensymphony.xwork2.Result;
import io.bdj.webshop.actions.ProductImageAction;
import org.apache.struts2.ServletActionContext;

/**
 *
 */
public class ImageResult implements Result {

    public void execute(ActionInvocation invocation) throws Exception {

        ProductImageAction action = (ProductImageAction) invocation.getAction();
        HttpServletResponse response = ServletActionContext.getResponse();

        response.setContentType(action.getContentType());
        response.getOutputStream().write(action.getImageInBytes());
        response.getOutputStream().flush();

    }

}
