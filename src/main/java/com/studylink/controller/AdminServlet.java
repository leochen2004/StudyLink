package com.studylink.controller;

import com.studylink.dao.CourseDAO;
import com.studylink.dao.UserDAO;
import com.studylink.dao.ResourceDAO;
import com.studylink.dao.QuestionDAO;
import com.studylink.model.Course;
import com.studylink.model.User;
import com.studylink.model.Resource;
import com.studylink.model.Question;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.List;

@WebServlet("/admin/*")
public class AdminServlet extends HttpServlet {
    private CourseDAO courseDAO;
    private UserDAO userDAO;
    private ResourceDAO resourceDAO;
    private QuestionDAO questionDAO;

    @Override
    public void init() throws ServletException {
        courseDAO = new CourseDAO();
        userDAO = new UserDAO();
        resourceDAO = new ResourceDAO();
        questionDAO = new QuestionDAO();
    }

    private boolean checkAdmin(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("user") == null) {
            resp.sendRedirect(req.getContextPath() + "/index.html");
            return false;
        }
        User user = (User) session.getAttribute("user");
        if (!"ADMIN".equals(user.getRole())) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN);
            return false;
        }
        return true;
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if (!checkAdmin(req, resp))
            return;

        String pathInfo = req.getPathInfo();
        if ("/dashboard_data".equals(pathInfo)) {
            getDashboardData(req, resp);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if (!checkAdmin(req, resp))
            return;

        String pathInfo = req.getPathInfo();
        if ("/course/add".equals(pathInfo)) {
            addCourse(req, resp);
        } else if ("/course/delete".equals(pathInfo)) {
            deleteCourse(req, resp);
        } else if ("/teacher/add".equals(pathInfo)) {
            addTeacher(req, resp);
        } else if ("/resource/delete".equals(pathInfo)) {
            deleteResource(req, resp);
        } else if ("/question/delete".equals(pathInfo)) {
            deleteQuestion(req, resp);
        }
    }

    private void getDashboardData(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        List<Course> courses = courseDAO.getAllCourses();
        List<User> teachers = userDAO.getAllTeachers();
        List<Resource> resources = resourceDAO.getAllResources();
        List<Question> questions = questionDAO.getAllQuestions();

        StringBuilder json = new StringBuilder();
        json.append("{");

        // Courses
        json.append("\"courses\": [");
        for (int i = 0; i < courses.size(); i++) {
            Course c = courses.get(i);
            json.append(String.format("{\"id\":%d, \"name\":\"%s\", \"teacherName\":\"%s\", \"department\":\"%s\"}",
                    c.getId(), c.getName(), c.getTeacherName(), c.getDepartment()));
            if (i < courses.size() - 1)
                json.append(",");
        }
        json.append("],");

        // Teachers
        json.append("\"teachers\": [");
        for (int i = 0; i < teachers.size(); i++) {
            User t = teachers.get(i);
            json.append(String.format("{\"id\":%d, \"username\":\"%s\", \"fullName\":\"%s\"}",
                    t.getId(), t.getUsername(), t.getFullName()));
            if (i < teachers.size() - 1)
                json.append(",");
        }
        json.append("],");

        // Resources
        json.append("\"resources\": [");
        for (int i = 0; i < resources.size(); i++) {
            Resource r = resources.get(i);
            json.append(String.format(
                    "{\"id\":%d, \"title\":\"%s\", \"courseName\":\"%s\", \"uploaderName\":\"%s\", \"status\":\"%s\"}",
                    r.getId(), r.getTitle(), r.getCourseName(), r.getUploaderName(), r.getStatus()));
            if (i < resources.size() - 1)
                json.append(",");
        }
        json.append("],");

        // Questions
        json.append("\"questions\": [");
        for (int i = 0; i < questions.size(); i++) {
            Question q = questions.get(i);
            json.append(String.format(
                    "{\"id\":%d, \"title\":\"%s\", \"courseName\":\"%s\", \"studentName\":\"%s\", \"answerCount\":%d}",
                    q.getId(), q.getTitle(), q.getCourseName(), q.getStudentName(), q.getAnswerCount()));
            if (i < questions.size() - 1)
                json.append(",");
        }
        json.append("]");

        json.append("}");

        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");
        resp.getWriter().write(json.toString());
    }

    private void addCourse(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String name = req.getParameter("name");
        int teacherId = Integer.parseInt(req.getParameter("teacherId"));
        String desc = req.getParameter("description");
        String dept = req.getParameter("department");
        courseDAO.addCourse(new Course(name, teacherId, desc, dept));
        resp.sendRedirect(req.getContextPath() + "/admin.html");
    }

    private void deleteCourse(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        courseDAO.deleteCourse(Integer.parseInt(req.getParameter("id")));
        resp.sendRedirect(req.getContextPath() + "/admin.html");
    }

    private void addTeacher(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        User u = new User();
        u.setUsername(req.getParameter("username"));
        u.setPassword(req.getParameter("password"));
        u.setRole("TEACHER");
        u.setFullName(req.getParameter("fullName"));
        u.setEmail(req.getParameter("email"));
        u.setBio(req.getParameter("bio"));
        userDAO.register(u);
        resp.sendRedirect(req.getContextPath() + "/admin.html");
    }

    private void deleteResource(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resourceDAO.deleteResource(Integer.parseInt(req.getParameter("id")));
        resp.sendRedirect(req.getContextPath() + "/admin.html");
    }

    private void deleteQuestion(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        questionDAO.deleteQuestion(Integer.parseInt(req.getParameter("id")));
        resp.sendRedirect(req.getContextPath() + "/admin.html");
    }
}
