package com.studylink.controller;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.studylink.dao.UserDAO;
import com.studylink.model.User;

@WebServlet("/profile/update")
public class ProfileServlet extends HttpServlet {
    private UserDAO userDAO = new UserDAO();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        HttpSession session = req.getSession();
        User currentUser = (User) session.getAttribute("user");

        if (currentUser == null) {
            resp.sendRedirect(req.getContextPath() + "/auth/login");
            return;
        }

        String fullName = req.getParameter("fullName");
        String password = req.getParameter("password");
        // String email = req.getParameter("email"); // If email was in the model

        if (fullName != null && !fullName.trim().isEmpty()) {
            currentUser.setFullName(fullName);
        }

        if (password != null && !password.trim().isEmpty()) {
            currentUser.setPassword(password); // In real app, hash this!
        }

        boolean success = userDAO.updateUser(currentUser);

        if (success) {
            session.setAttribute("user", currentUser); // Update session
            resp.sendRedirect(req.getContextPath() + "/student.html");
            // Note: Should redirect back to profile page or referring page
        } else {
            req.setAttribute("error", "Update failed");
            req.getRequestDispatcher("/profile.html").forward(req, resp);
        }
    }
}
