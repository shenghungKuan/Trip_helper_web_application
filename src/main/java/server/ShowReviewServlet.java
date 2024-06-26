package server;

import hotelapp.Hotel;
import hotelapp.HotelSearcher;
import hotelapp.Review;
import hotelapp.ReviewSearcher;
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
import java.util.List;

public class ShowReviewServlet extends HttpServlet {
    /**
     * Called by the server (via the service method) to allow a servlet to handle a show review GET request.
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
        String hotelId = request.getParameter("hotelId");
        hotelId = StringEscapeUtils.escapeHtml4(hotelId);
        String page = request.getParameter("page");
        PrintWriter out = response.getWriter();

        HttpSession session = request.getSession();
        String username = (String) session.getAttribute("username");

        if (username == null) {
            response.sendRedirect("/portal");
            return;
        }

        if (hotelId == null) {
            hotelId = (String) session.getAttribute("hotelId");
        } else {
            session.setAttribute("hotelId", hotelId);
        }

        HotelSearcher hotelSearcher = (HotelSearcher) getServletContext().getAttribute("hotelSearcher");
        ReviewSearcher reviewSearcher = (ReviewSearcher) getServletContext().getAttribute("reviewSearcher");


        Hotel hotel = hotelSearcher.find(hotelId);
        if (hotel == null) {
            response.sendRedirect("/search");
            return;
        }

        List<Review> reviews = reviewSearcher.findReview(hotelId);
        if (reviews.size() == 0) {
            out.println("No review");
            return;
        } else if (page == null || page.equals("0")) {
            reviews = reviews.subList(0, Math.min(reviews.size(), 5));
            session.setAttribute("page", "0");
        } else if(page.equals("next")){
            page = (String) session.getAttribute("page");
            int no = Integer.parseInt(page) + 1;
            if (no * 5 >= reviews.size()) {
                reviews = reviews.subList(0, Math.min(reviews.size(), 5));
                session.setAttribute("page", "0");
            } else {
                reviews = reviews.subList(no * 5, Math.min(reviews.size(), (no + 1) * 5));
                session.setAttribute("page", String.valueOf(no));
            }
        } else {
            page = (String) session.getAttribute("page");
            int no = Integer.parseInt(page) - 1;
            if (no < 0) {
                reviews = reviews.subList(0, Math.min(reviews.size(), 5));
                session.setAttribute("page", "0");
            } else {
                reviews = reviews.subList(no * 5, Math.min(reviews.size(), (no + 1) * 5));
                session.setAttribute("page", String.valueOf(no));
            }
        }

        VelocityEngine ve = (VelocityEngine) getServletContext().getAttribute("templateEngine");
        VelocityContext context = new VelocityContext();
        Template template = ve.getTemplate("static/ShowReview.html");
        context.put("reviews", reviews);


        template.merge(context, out);
    }
}
