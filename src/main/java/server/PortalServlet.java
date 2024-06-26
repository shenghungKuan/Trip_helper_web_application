package server;

import database.DatabaseHandler;
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
import java.sql.Timestamp;


/** An example that demonstrates how to process HTML forms with servlets.
 */
@SuppressWarnings("serial")
public class PortalServlet extends HttpServlet {

	/**
	 * Called by the server (via the service method) to allow a servlet to handle a portal GET request.
	 * @param request an HttpServletRequest object that contains the request the client has made of the servlet
	 * @param response an HttpServletResponse object that contains the response the servlet sends to the client
	 * @throws ServletException if the request for the GET could not be handled
	 * @throws IOException if an input or output error is detected when the servlet handles the GET request
	 */
	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		response.setContentType("text/html");
		response.setStatus(HttpServletResponse.SC_OK);
		HttpSession session = request.getSession();
		PrintWriter out = response.getWriter();

		String username = (String) session.getAttribute("username");
		String message = (String) session.getAttribute("message");
		if (username != null) {
			response.sendRedirect("/search");
		}

		VelocityEngine ve = (VelocityEngine) getServletContext().getAttribute("templateEngine");
		VelocityContext context = new VelocityContext();
		Template template = ve.getTemplate("static/Portal.html");
		context.put("action", "/portal");
		if (message != null) {
			context.put("message", message);
			session.setAttribute("message", null);
		} else {
			context.put("message", "");
		}

		template.merge(context, out);
	}

	/**
	 * Called by the server (via the service method) to allow a servlet to handle a portal POST request.
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

		String username = request.getParameter("username");
		username = StringEscapeUtils.escapeHtml4(username);
		String password = request.getParameter("pass");
		password = StringEscapeUtils.escapeHtml4(password);
		String submitValue = request.getParameter("register");
		if (submitValue == null) {
			response.sendRedirect("/portal");
			return;
		}

		DatabaseHandler dbHandler = DatabaseHandler.getInstance();
		switch (submitValue) {
			case "Sign up" -> {
				if (dbHandler.checkUsername(username)) {
					session.setAttribute("message", username + " is already in use");
					session.setAttribute("username", null);
					response.sendRedirect("/portal");
				} else {
					if (!password.matches(".{8,}")) {
						session.setAttribute("message", "Password is too short (at least 8 characters)");
						session.setAttribute("username", null);
						response.sendRedirect("/portal");
					} else if (username.equals("") || username.equals("Anonymous")) {
						session.setAttribute("message", "Invalid username");
						session.setAttribute("username", null);
						response.sendRedirect("/portal");
					} else if (username.length() >= 32) {
						session.setAttribute("message", "Username too long");
						session.setAttribute("username", null);
						response.sendRedirect("/portal");
					} else {
						dbHandler.registerUser(username, password);
						session.setAttribute("lastlogin", "You haven't logged in before!");
						session.setAttribute("username", username);
						response.sendRedirect("/search");
					}
				}
			}
			case "Sign in" -> {
				Timestamp date;
				if ((date = dbHandler.authenticateUser(username, password)) == null) {
					session.setAttribute("message", "Invalid username or password");
					session.setAttribute("username", null);
					response.sendRedirect("/portal");
				} else {
					session.setAttribute("username", username);
					session.setAttribute("message", null);
					session.setAttribute("lastlogin", date.toString());
					response.sendRedirect("/search");
				}
			}
		}
	}
}