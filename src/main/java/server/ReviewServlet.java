package server;

import hotelapp.*;
import org.apache.commons.text.StringEscapeUtils;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Objects;

public class ReviewServlet extends HttpServlet {
    /**
     * Called by the server (via the service method) to allow a servlet to handle a review GET request.
     * @param request an HttpServletRequest object that contains the request the client has made of the servlet
     * @param response an HttpServletResponse object that contains the response the servlet sends to the client
     * @throws ServletException if the request for the GET could not be handled
     * @throws IOException if an input or output error is detected when the servlet handles the GET request
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setStatus(HttpServletResponse.SC_OK);
        response.setContentType("text/html");
        HttpSession session = request.getSession();
        String hotelId = (String) session.getAttribute("hotelId");
        hotelId = StringEscapeUtils.escapeHtml4(hotelId);
        session.setAttribute("hotelId", hotelId);
        PrintWriter out = response.getWriter();

        String username = (String) session.getAttribute("username");
        if (username == null) {
            response.sendRedirect("/portal");
            return;
        }


        VelocityEngine ve = (VelocityEngine) getServletContext().getAttribute("templateEngine");
        VelocityContext context = new VelocityContext();
        Template template = ve.getTemplate("static/ReviewHandler.html");
        context.put("hotelId", hotelId);

        String message = (String) session.getAttribute("message");
        context.put("message", Objects.requireNonNullElse(message, ""));
        session.setAttribute("message", null);

        template.merge(context, out);

    }

    /**
     * Called by the server (via the service method) to allow a servlet to handle a review POST request.
     * @param request an HttpServletRequest object that contains the request the client has made of the servlet
     * @param response an HttpServletResponse object that contains the response the servlet sends to the client
     * @throws ServletException if the request for the GET could not be handled
     * @throws IOException if an input or output error is detected when the servlet handles the POST request
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("text/html");
        response.setStatus(HttpServletResponse.SC_OK);
        HttpSession session = request.getSession();

        String hotelId = (String) session.getAttribute("hotelId");
        String username = (String) session.getAttribute("username");
        String submitValue = request.getParameter("choice");
        String title = request.getParameter("title");
        String text = request.getParameter("text");
        if (submitValue == null) {
            response.sendRedirect("/review?hotelId=" + hotelId);
            return;
        }

        ReviewSearcher reviewSearcher = (ReviewSearcher) getServletContext().getAttribute("reviewSearcher");
        boolean hasReview = reviewSearcher.findSpecificReview(hotelId, username);

        switch (submitValue) {
            case "add" -> {
                if (!hasReview) {
                    reviewSearcher.addReview(username, hotelId, title, text, 0);
                    response.sendRedirect("/hotel?hotelId=" + hotelId);
                    session.setAttribute("message", "Successfully added a review");
                } else {
                    session.setAttribute("message", "You already left a review before");
                    response.sendRedirect("/review?hotelId=" + hotelId);
                }
            }
            case "modify" -> {
                if (!hasReview) {
                    session.setAttribute("message", "You don't have any reviews");
                    response.sendRedirect("/review?hotelId=" + hotelId);
                } else {
                    reviewSearcher.deleteReview(username, hotelId);
                    reviewSearcher.addReview(username, hotelId, title, text, 0);
                    response.sendRedirect("/hotel?hotelId=" + hotelId);
                    session.setAttribute("message", "Successfully modified the review");
                }
            }
            case "delete" -> {
                if (!hasReview) {
                    session.setAttribute("message", "You don't have any reviews");
                    response.sendRedirect("/review?hotelId=" + hotelId);
                } else {
                    reviewSearcher.deleteReview(username, hotelId);
                    response.sendRedirect("/hotel?hotelId=" + hotelId);
                    session.setAttribute("message", "Successfully deleted the review");
                }
            }
        }
    }
}
