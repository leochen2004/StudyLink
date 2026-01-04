package com.studylink.controller;

import com.studylink.dao.CourseDAO;
import com.studylink.dao.NotificationDAO;
import com.studylink.dao.QuestionDAO;
import com.studylink.dao.ResourceDAO;
import com.studylink.model.Answer;
import com.studylink.model.Course;
import com.studylink.model.Question;
import com.studylink.model.Resource;
import com.studylink.model.User;

import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.http.Part;
import java.io.IOException;
import java.util.List;
import java.util.UUID;

@WebServlet("/teacher/*")
@MultipartConfig
public class TeacherServlet extends HttpServlet {
    private CourseDAO courseDAO;
    private ResourceDAO resourceDAO;
    private QuestionDAO questionDAO;
    private NotificationDAO notificationDAO;

    @Override
    public void init() throws ServletException {
        courseDAO = new CourseDAO();
        resourceDAO = new ResourceDAO();
        questionDAO = new QuestionDAO();
        notificationDAO = new NotificationDAO();
    }

    private User checkTeacher(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("user") == null) {
            resp.sendRedirect(req.getContextPath() + "/index.html");
            return null;
        }
        User user = (User) session.getAttribute("user");
        if (!"TEACHER".equals(user.getRole())) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN);
            return null;
        }
        return user;
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        User user = checkTeacher(req, resp);
        if (user == null)
            return;

        String pathInfo = req.getPathInfo();
        if ("/dashboard_data".equals(pathInfo)) {
            getDashboardData(req, resp, user);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");
        User user = checkTeacher(req, resp);
        if (user == null)
            return;

        String pathInfo = req.getPathInfo();
        if ("/resource/add".equals(pathInfo)) {
            addResource(req, resp, user);
        } else if ("/question/answer".equals(pathInfo) || "/answer/add".equals(pathInfo)) {
            addAnswer(req, resp, user);
        } else if ("/question/delete".equals(pathInfo)) {
            deleteQuestion(req, resp);
        } else if ("/answer/delete".equals(pathInfo)) {
            deleteAnswer(req, resp);
        } else if ("/resource/update_visibility".equals(pathInfo)) {
            updateResourceVisibility(req, resp, user);
        }
    }

    private void getDashboardData(HttpServletRequest req, HttpServletResponse resp, User user) throws IOException {
        List<Course> courses = courseDAO.getCoursesByTeacher(user.getId());
        List<Question> questions = questionDAO.getQuestionsByTeacher(user.getId());

        // Calculate unanswered count
        long unansweredCount = questions.stream().filter(q -> q.getAnswerCount() == 0).count();

        StringBuilder json = new StringBuilder();
        json.append("{");
        json.append("\"notifications\": ").append(unansweredCount).append(",");

        json.append("\"courses\": [");
        for (int i = 0; i < courses.size(); i++) {
            Course c = courses.get(i);
            json.append(String.format("{\"id\":%d, \"name\":\"%s\", \"department\":\"%s\"}",
                    c.getId(), c.getName(), c.getDepartment()));
            if (i < courses.size() - 1)
                json.append(",");
        }
        json.append("],");

        // Split questions into answered and unanswered
        json.append("\"unansweredQuestions\": [");
        boolean first = true;
        for (Question q : questions) {
            if (q.getAnswerCount() == 0) {
                if (!first)
                    json.append(",");
                json.append(String.format(
                        "{\"id\":%d, \"title\":\"%s\", \"content\":\"%s\", \"courseName\":\"%s\", \"studentName\":\"%s\", \"answerCount\":%d}",
                        q.getId(), q.getTitle(),
                        q.getContent().replace("\"", "\\\"").replace("\n", "\\n").replace("\r", ""), q.getCourseName(),
                        q.getStudentName(), q.getAnswerCount()));
                first = false;
            }
        }
        json.append("],");

        // ... existing code ...
        json.append("\"answeredQuestions\": [");
        first = true;
        for (Question q : questions) {
            if (q.getAnswerCount() > 0) {
                if (!first)
                    json.append(",");

                List<Answer> answers = questionDAO.getAnswersByQuestionId(q.getId());
                StringBuilder ansJson = new StringBuilder("[");
                for (int j = 0; j < answers.size(); j++) {
                    Answer a = answers.get(j);
                    if (a.getTeacherId() == user.getId()) {
                        if (ansJson.length() > 1)
                            ansJson.append(",");
                        ansJson.append(String.format("{\"id\":%d, \"content\":\"%s\"}",
                                a.getId(),
                                a.getContent().replace("\"", "\\\"").replace("\n", "\\n").replace("\r", "")));
                    }
                }
                ansJson.append("]");

                json.append(String.format(
                        "{\"id\":%d, \"title\":\"%s\", \"content\":\"%s\", \"courseName\":\"%s\", \"studentName\":\"%s\", \"answerCount\":%d, \"answers\": %s}",
                        q.getId(), q.getTitle(),
                        q.getContent().replace("\"", "\\\"").replace("\n", "\\n").replace("\r", ""), q.getCourseName(),
                        q.getStudentName(), q.getAnswerCount(), ansJson.toString()));
                first = false;
            }
        }
        json.append("],");

        // Add My Resources
        List<Resource> myResources = resourceDAO.getResourcesByUploader(user.getId());
        json.append("\"myResources\": [");
        for (int i = 0; i < myResources.size(); i++) {
            Resource r = myResources.get(i);
            json.append(String.format(
                    "{\"id\":%d, \"title\":\"%s\", \"courseName\":\"%s\", \"visibility\":\"%s\", \"status\":\"%s\", \"downloadCount\":%d}",
                    r.getId(),
                    r.getTitle().replace("\"", "\\\""),
                    r.getCourseName().replace("\"", "\\\""),
                    r.getVisibility(),
                    r.getStatus(),
                    r.getDownloadCount()));
            if (i < myResources.size() - 1)
                json.append(",");
        }
        json.append("]");

        json.append("}");

        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");
        resp.getWriter().write(json.toString());
    }

    private void addResource(HttpServletRequest req, HttpServletResponse resp, User user)
            throws IOException, ServletException {
        req.setCharacterEncoding("UTF-8");
        // Ensure multipart request is parsed
        if (req.getContentType() != null && req.getContentType().toLowerCase().contains("multipart/form-data")) {
            req.getParts();
        }

        String title = req.getParameter("title");
        String desc = req.getParameter("description");
        String courseIdStr = req.getParameter("courseId");
        String visibility = req.getParameter("visibility"); // PUBLIC/PRIVATE

        if (courseIdStr == null || title == null) {
            // Handle missing parameters
            resp.sendRedirect(req.getContextPath() + "/teacher.html?error=missing_params");
            return;
        }

        int courseId = 0;
        try {
            courseId = Integer.parseInt(courseIdStr);
        } catch (NumberFormatException e) {
            resp.sendRedirect(req.getContextPath() + "/teacher.html?error=invalid_course_id");
            return;
        }

        // Use portable path relative to project root
        String projectRoot = System.getProperty("user.dir");
        String uploadPath = projectRoot + java.io.File.separator + "src" + java.io.File.separator + "main"
                + java.io.File.separator + "webapp" + java.io.File.separator + "uploads";
        java.io.File uploadDir = new java.io.File(uploadPath);
        if (!uploadDir.exists())
            uploadDir.mkdirs();

        String filePath = "";
        String fileType = "FILE";

        String savedFileName = "";
        try {
            Part filePart = req.getPart("file");
            if (filePart != null && filePart.getSize() > 0) {
                String fileName = getSubmittedFileName(filePart);
                String uuid = UUID.randomUUID().toString();
                savedFileName = uuid + "_" + fileName;
                filePath = uploadPath + java.io.File.separator + savedFileName;

                // Write file to disk
                filePart.write(filePath);

                if (fileName.toLowerCase().endsWith(".png") || fileName.toLowerCase().endsWith(".jpg"))
                    fileType = "IMAGE";
                else if (fileName.toLowerCase().endsWith(".zip"))
                    fileType = "ZIP";
                else if (fileName.toLowerCase().endsWith(".pdf"))
                    fileType = "PDF";
            }
        } catch (Exception e) {
            // Handle Multipart config issues in simulation or missing part
            System.out.println("File upload upload logic failed: " + e.getMessage());
            e.printStackTrace();
        }

        Resource r = new Resource();
        r.setTitle(title);
        r.setDescription(desc);
        r.setCourseId(courseId);
        r.setUploaderId(user.getId());
        r.setFilePath("uploads/" + savedFileName); // Store relative path
        r.setFileType(fileType);
        r.setVisibility(visibility);

        resourceDAO.addResource(r);
        resp.sendRedirect(req.getContextPath() + "/teacher.html");
    }

    private void addAnswer(HttpServletRequest req, HttpServletResponse resp, User user) throws IOException {
        int questionId = Integer.parseInt(req.getParameter("questionId"));
        String content = req.getParameter("content");

        Answer a = new Answer();
        a.setQuestionId(questionId);
        a.setTeacherId(user.getId());
        a.setContent(content);
        a.setImagePath(""); // Attachment support optional for answer text (requested "pure text + image",
                            // treating image as optional)

        questionDAO.addAnswer(a);

        // Notify Student
        Question q = questionDAO.getQuestionById(questionId);
        if (q != null) {
            notificationDAO.addNotification(q.getStudentId(), "NEW_ANSWER",
                    "Your question has been answered by " + user.getFullName(), "student.html#qa");
        }

        resp.sendRedirect(req.getContextPath() + "/teacher.html");
    }

    private String getSubmittedFileName(Part part) {
        for (String cd : part.getHeader("content-disposition").split(";")) {
            if (cd.trim().startsWith("filename")) {
                return cd.substring(cd.indexOf('=') + 1).trim().replace("\"", "");
            }
        }
        return "unknown";
    }

    private void deleteQuestion(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String idStr = req.getParameter("id");
        if (idStr != null) {
            try {
                questionDAO.deleteQuestion(Integer.parseInt(idStr));
            } catch (Exception e) {
            }
        }
        resp.sendRedirect(req.getContextPath() + "/teacher.html");
    }

    private void deleteAnswer(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String idStr = req.getParameter("id");
        if (idStr != null) {
            try {
                // In a real app, verify ownership
                questionDAO.deleteAnswer(Integer.parseInt(idStr));
            } catch (Exception e) {
            }
        }
        resp.sendRedirect(req.getContextPath() + "/teacher.html");
    }

    private void updateResourceVisibility(HttpServletRequest req, HttpServletResponse resp, User user)
            throws IOException {
        String idStr = req.getParameter("resourceId");
        String visibility = req.getParameter("visibility"); // PUBLIC or PRIVATE

        if (idStr != null && visibility != null) {
            try {
                int resourceId = Integer.parseInt(idStr);
                // In a real app, verify ownership (check if resource belongs to user)
                Resource r = resourceDAO.getResourceById(resourceId);
                if (r != null && r.getUploaderId() == user.getId()) {
                    resourceDAO.updateVisibility(resourceId, visibility);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        resp.sendRedirect(req.getContextPath() + "/teacher.html");
    }
}
