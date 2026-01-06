package com.studylink.controller;

import com.studylink.dao.UserDAO;
import com.studylink.model.User;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.io.PrintWriter;

@WebServlet("/auth/*")
public class AuthServlet extends HttpServlet {
    private UserDAO userDAO;

    @Override
    public void init() throws ServletException {
        userDAO = new UserDAO();
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");
        String pathInfo = req.getPathInfo();
        if ("/login".equals(pathInfo)) {
            handleLogin(req, resp);
        } else if ("/register".equals(pathInfo)) {
            handleRegister(req, resp);
        } else if ("/update".equals(pathInfo)) {
            handleUpdateProfile(req, resp);
        } else {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }

    private void handleUpdateProfile(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("user") == null) {
            resp.sendRedirect(req.getContextPath() + "/index.html");
            return;
        }
        User user = (User) session.getAttribute("user");

        String fullName = req.getParameter("fullName");
        String email = req.getParameter("email");
        String bio = req.getParameter("bio");
        String title = req.getParameter("title");
        String password = req.getParameter("password");

        if (fullName != null)
            user.setFullName(fullName);
        if (email != null)
            user.setEmail(email);
        if (bio != null)
            user.setBio(bio);
        if (title != null)
            user.setTitle(title);
        if (password != null && !password.trim().isEmpty()) {
            user.setPassword(password);
        }

        userDAO.updateUser(user);
        session.setAttribute("user", user);

        String referer = req.getHeader("Referer");
        if (referer != null) {
            resp.sendRedirect(referer);
        } else {
            if ("TEACHER".equals(user.getRole())) {
                resp.sendRedirect(req.getContextPath() + "/teacher.html");
            } else if ("ADMIN".equals(user.getRole())) {
                resp.sendRedirect(req.getContextPath() + "/admin.html");
            } else {
                resp.sendRedirect(req.getContextPath() + "/student.html");
            }
        }
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String pathInfo = req.getPathInfo();
        if ("/logout".equals(pathInfo)) {
            handleLogout(req, resp);
        } else {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }

    private void handleLogin(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String username = req.getParameter("username");
        String password = req.getParameter("password"); // 根据任务简化要求使用明文

        User user = userDAO.login(username, password);

        if (user != null) {
            HttpSession session = req.getSession();
            session.setAttribute("user", user);

            // 根据角色重定向
            // 理想情况下我们重定向到一个仪表盘 Servlet，但现在先重定向到特定页面
            if ("ADMIN".equals(user.getRole())) {
                resp.sendRedirect(req.getContextPath() + "/admin.html");
            } else if ("TEACHER".equals(user.getRole())) {
                resp.sendRedirect(req.getContextPath() + "/teacher.html");
            } else {
                resp.sendRedirect(req.getContextPath() + "/student.html");
            }
        } else {
            resp.setContentType("text/html");
            PrintWriter out = resp.getWriter();
            out.println("<script>alert('Invalid username or password'); window.location.href='" + req.getContextPath()
                    + "/index.html';</script>");
        }
    }

    private void handleRegister(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String username = req.getParameter("username");
        String password = req.getParameter("password");
        String fullName = req.getParameter("fullName");
        String email = req.getParameter("email");
        String role = "STUDENT"; // 默认注册为学生

        if (userDAO.checkUsernameExists(username)) {
            resp.setContentType("text/html");
            PrintWriter out = resp.getWriter();
            out.println("<script>alert('Username already exists'); window.location.href='" + req.getContextPath()
                    + "/register.html';</script>");
            return;
        }

        User newUser = new User(username, password, role, fullName, email);
        if (userDAO.register(newUser)) {
            resp.setContentType("text/html");
            PrintWriter out = resp.getWriter();
            out.println("<script>alert('Registration Successful! Please Login.'); window.location.href='"
                    + req.getContextPath() + "/index.html';</script>");
        } else {
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Registration failed");
        }
    }

    private void handleLogout(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        HttpSession session = req.getSession(false);
        if (session != null) {
            session.invalidate();
        }
        resp.sendRedirect(req.getContextPath() + "/index.html");
    }
}
