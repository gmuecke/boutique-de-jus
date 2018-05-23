package io.bdj.webshop.result;

import javax.servlet.http.HttpServletResponse;

import com.opensymphony.xwork2.ActionInvocation;
import com.opensymphony.xwork2.Result;
import org.apache.struts2.ServletActionContext;

/**
 *
 */
public class ErrorResult implements Result {

    private int statusCode;
    private String message;

    public int getStatusCode() {

        return statusCode;
    }

    public void setStatusCode(final int statusCode) {

        this.statusCode = statusCode;
    }

    public String getMessage() {

        return message;
    }

    public void setMessage(final String message) {

        this.message = message;
    }

    @Override
    public void execute(final ActionInvocation invocation) throws Exception {

        HttpServletResponse response = ServletActionContext.getResponse();
        response.sendError(this.statusCode, this.message);
    }
}
