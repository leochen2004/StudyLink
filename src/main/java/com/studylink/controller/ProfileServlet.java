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
        req.setCharacterEncoding("UTF-8");
        HttpSession session = req.getSession();
        User currentUser = (User) session.getAttribute("user");

        if (currentUser == null) {
            resp.sendRedirect(req.getContextPath() + "/auth/login");
            return;
        }

        String fullName = req.getParameter("fullName");
        String password = req.getParameter("password");
        // String email = req.getParameter("email"); // 如果模型中有 email

        if (fullName != null && !fullName.trim().isEmpty()) {
            currentUser.setFullName(fullName);
        }

        if (password != null && !password.trim().isEmpty()) {
            currentUser.setPassword(password); // 在实际应用中，对此进行哈希处理！
        }

        boolean success = userDAO.updateUser(currentUser);

        if (success) {
            session.setAttribute("user", currentUser); // 更新会话
            resp.sendRedirect(req.getContextPath() + "/student.html");
            // 注意：应重定向回个人资料页面或引用页面
        } else {
            req.setAttribute("error", "Update failed");
            req.getRequestDispatcher("/profile.html").forward(req, resp);
        }
    }
}
