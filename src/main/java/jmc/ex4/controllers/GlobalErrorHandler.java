package jmc.ex4.controllers;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Global error handler for handling different HTTP error statuses.
 * Maps specific error codes to corresponding error pages.
 */
@Controller
public class GlobalErrorHandler implements ErrorController {

    /**
     * Returns the path to the error page based on the HTTP status code.
     *
     * @param request the HttpServletRequest containing error information
     * @return the name of the error view to be rendered
     */
    @RequestMapping("/error")
    public String handleError(HttpServletRequest request) {
        Object status = request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);
        Object error = request.getAttribute(RequestDispatcher.ERROR_MESSAGE);

        if (status != null) {
            int statusCode = Integer.valueOf(status.toString());

            switch (statusCode) {
                case 404:
                    return "error/404";
                case 403:
                    return "error/403";
                case 500:
                    return "error/500";
            }
        }

        return "error/500";
    }

}
