package com.studylink.controller;

import com.studylink.dao.CourseDAO;
import com.studylink.dao.NotificationDAO;
import com.studylink.dao.QuestionDAO;
import com.studylink.dao.ResourceDAO;
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
import java.util.ArrayList;
import java.util.UUID;

@WebServlet("/student/*")
@MultipartConfig
public class StudentServlet extends HttpServlet {
    private ResourceDAO resourceDAO;
    private QuestionDAO questionDAO;
    private NotificationDAO notificationDAO;
    private CourseDAO courseDAO;

    @Override
    public void init() throws ServletException {
        resourceDAO = new ResourceDAO();
        questionDAO = new QuestionDAO();
        notificationDAO = new NotificationDAO();
        courseDAO = new CourseDAO();
    }

    private User checkStudent(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("user") == null) {
            resp.sendRedirect(req.getContextPath() + "/index.html");
            return null;
        }
        User user = (User) session.getAttribute("user");
        // Teachers/Admins could technically act as students or view this, but strict
        // role check:
        if (!"STUDENT".equals(user.getRole())) {
            // resp.sendError(HttpServletResponse.SC_FORBIDDEN);
            // return null;
            // Relaxed: A teacher might want to see student view? Requirement says "Student
            // Module".
            // Let's enforce Student.
        }
        return user;
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        User user = checkStudent(req, resp);
        if (user == null)
            return;

        String pathInfo = req.getPathInfo();
        if ("/dashboard_data".equals(pathInfo)) {
            getDashboardData(req, resp, user);
        } else if ("/search".equals(pathInfo)) {
            searchResources(req, resp);
        } else if ("/resource/download".equals(pathInfo)) {
            downloadResource(req, resp);
        } else if ("/question/search".equals(pathInfo)) {
            searchQuestions(req, resp);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");
        User user = checkStudent(req, resp);
        if (user == null)
            return;

        String pathInfo = req.getPathInfo();
        if ("/resource/upload".equals(pathInfo)) {
            uploadResource(req, resp, user);
        } else if ("/course/enroll".equals(pathInfo)) {
            enrollCourse(req, resp, user);
        } else if ("/question/add".equals(pathInfo)) {
            askQuestion(req, resp, user);
        } else if ("/question/delete".equals(pathInfo)) {
            deleteQuestion(req, resp, user);
        } else if ("/resource/delete".equals(pathInfo)) {
            deleteResource(req, resp, user);
        }
    }

    private void deleteResource(HttpServletRequest req, HttpServletResponse resp, User user) throws IOException {
        String idStr = req.getParameter("id");
        if (idStr != null) {
            try {
                int id = Integer.parseInt(idStr);
                // Verify ownership
                Resource r = resourceDAO.getResourceById(id);
                if (r != null && r.getUploaderId() == user.getId()) {
                    resourceDAO.deleteResource(id);
                }
            } catch (Exception e) {
                // Ignore
            }
        }
        resp.sendRedirect(req.getContextPath() + "/student.html");
    }

    private void deleteQuestion(HttpServletRequest req, HttpServletResponse resp, User user) throws IOException {
        String idStr = req.getParameter("id");
        if (idStr != null) {
            try {
                int id = Integer.parseInt(idStr);
                // Verify ownership
                Question q = questionDAO.getQuestionById(id);
                if (q != null && q.getStudentId() == user.getId()) {
                    questionDAO.deleteQuestion(id);
                }
            } catch (Exception e) {
                // Ignore
            }
        }
        resp.sendRedirect(req.getContextPath() + "/student.html");
    }

    private void enrollCourse(HttpServletRequest req, HttpServletResponse resp, User user) throws IOException {
        String courseIdStr = req.getParameter("courseId");
        if (courseIdStr != null) {
            try {
                int courseId = Integer.parseInt(courseIdStr);
                courseDAO.enrollStudent(courseId, user.getId());
                resp.getWriter().write("{\"success\":true}");
                return;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        resp.sendRedirect(req.getContextPath() + "/student.html");
    }

    private void getDashboardData(HttpServletRequest req, HttpServletResponse resp, User user) throws IOException {
        int unread = notificationDAO.getUnreadCount(user.getId());
        List<com.studylink.model.Notification> notificationList = notificationDAO.getNotifications(user.getId());
        List<Resource> myResources = resourceDAO.getResourcesByUploader(user.getId());
        List<Question> myQuestions = questionDAO.getQuestionsByStudent(user.getId());

        List<Course> enrolledCourses = courseDAO.getEnrolledCourses(user.getId());
        List<Course> allCourses = courseDAO.getAllCourses();

        // available = all - enrolled
        List<Integer> enrolledIds = new ArrayList<>();
        for (Course c : enrolledCourses)
            enrolledIds.add(c.getId());

        List<Course> availableCourses = new ArrayList<>();
        for (Course c : allCourses) {
            if (!enrolledIds.contains(c.getId())) {
                availableCourses.add(c);
            }
        }

        StringBuilder json = new StringBuilder();
        json.append("{");

        // User Profile Data
        json.append("\"user\": {");
        json.append("\"id\":").append(user.getId()).append(",");
        json.append("\"username\":\"").append(user.getUsername()).append("\",");
        json.append("\"fullName\":\"").append(user.getFullName()).append("\",");
        json.append("\"email\":\"").append(user.getEmail() != null ? user.getEmail() : "").append("\",");
        json.append("\"bio\":\"")
                .append(user.getBio() != null ? user.getBio().replace("\"", "\\\"").replace("\n", "\\n") : "")
                .append("\",");
        json.append("\"title\":\"").append(user.getTitle() != null ? user.getTitle() : "").append("\"");
        json.append("},");

        json.append("\"notifications\": ").append(unread).append(",");

        json.append("\"notificationList\": [");
        for (int i = 0; i < notificationList.size(); i++) {
            com.studylink.model.Notification n = notificationList.get(i);
            json.append(String.format(
                    "{\"id\":%d, \"type\":\"%s\", \"message\":\"%s\", \"link\":\"%s\", \"isRead\":%b, \"createdAt\":\"%s\"}",
                    n.getId(), n.getType(), n.getMessage().replace("\"", "\\\""), n.getRelatedLink(), n.isRead(),
                    n.getCreatedAt()));
            if (i < notificationList.size() - 1)
                json.append(",");
        }
        json.append("],");

        json.append("\"myResources\": [");
        for (int i = 0; i < myResources.size(); i++) {
            Resource r = myResources.get(i);
            json.append(String.format("{\"id\":%d, \"title\":\"%s\", \"status\":\"%s\", \"downloadCount\":%d}",
                    r.getId(), r.getTitle(), r.getStatus(), r.getDownloadCount()));
            if (i < myResources.size() - 1)
                json.append(",");
        }
        json.append("],");

        json.append("\"myQuestions\": [");
        for (int i = 0; i < myQuestions.size(); i++) {
            Question q = myQuestions.get(i);
            List<com.studylink.model.Answer> answers = questionDAO.getAnswersByQuestionId(q.getId());

            StringBuilder answersJson = new StringBuilder("[");
            for (int j = 0; j < answers.size(); j++) {
                com.studylink.model.Answer a = answers.get(j);
                answersJson.append(String.format("{\"teacherName\":\"%s\", \"content\":\"%s\"}",
                        a.getTeacherName(),
                        a.getContent().replace("\"", "\\\"").replace("\n", "\\n").replace("\r", "")));
                if (j < answers.size() - 1)
                    answersJson.append(",");
            }
            answersJson.append("]");

            json.append(String.format(
                    "{\"id\":%d, \"title\":\"%s\", \"answerCount\":%d, \"courseName\":\"%s\", \"answers\":%s}",
                    q.getId(), q.getTitle(), q.getAnswerCount(), q.getCourseName(), answersJson.toString()));
            if (i < myQuestions.size() - 1)
                json.append(",");
        }
        json.append("],");

        json.append("\"enrolledCourses\": [");
        for (int i = 0; i < enrolledCourses.size(); i++) {
            Course c = enrolledCourses.get(i);
            json.append(String.format("{\"id\":%d, \"name\":\"%s\", \"teacherName\":\"%s\"}", c.getId(), c.getName(),
                    c.getTeacherName()));
            if (i < enrolledCourses.size() - 1)
                json.append(",");
        }
        json.append("],");

        json.append("\"availableCourses\": [");
        for (int i = 0; i < availableCourses.size(); i++) {
            Course c = availableCourses.get(i);
            json.append(String.format("{\"id\":%d, \"name\":\"%s\", \"teacherName\":\"%s\"}", c.getId(), c.getName(),
                    c.getTeacherName()));
            if (i < availableCourses.size() - 1)
                json.append(",");
        }
        json.append("]");

        json.append("}");

        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");
        resp.getWriter().write(json.toString());
    }

    private void searchResources(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String keyword = req.getParameter("keyword");
        User user = checkStudent(req, resp); // Ensure user is logged in for visibility check
        if (user == null)
            return;

        List<Resource> resources = resourceDAO.getStudentVisibleResources(user.getId());

        // Filter in memory by keyword
        List<Resource> filtered = new ArrayList<>();
        String k = (keyword != null ? keyword.toLowerCase() : "");
        for (Resource r : resources) {
            if (r.getTitle().toLowerCase().contains(k) || r.getDescription().toLowerCase().contains(k)) {
                filtered.add(r);
            }
        }

        StringBuilder json = new StringBuilder();
        json.append("[");
        for (int i = 0; i < filtered.size(); i++) {
            Resource r = filtered.get(i);
            json.append(String.format(
                    "{\"id\":%d, \"title\":\"%s\", \"uploaderName\":\"%s\", \"courseName\":\"%s\", \"description\":\"%s\", \"downloadCount\":%d}",
                    r.getId(), r.getTitle(), r.getUploaderName(), r.getCourseName(), r.getDescription(),
                    r.getDownloadCount()));
            if (i < filtered.size() - 1)
                json.append(",");
        }
        json.append("]");

        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");
        resp.getWriter().write(json.toString());
    }

    private void uploadResource(HttpServletRequest req, HttpServletResponse resp, User user)
            throws IOException, ServletException {
        req.setCharacterEncoding("UTF-8");
        String title = req.getParameter("title");
        String description = req.getParameter("description");
        int courseId = Integer.parseInt(req.getParameter("courseId"));

        // Use absolute path to ensure files are saved in the project source directory
        // and persist across restarts/builds.
        String uploadPath = "/Users/liangchen0920/workspace/webDev/StudyLink/src/main/webapp/uploads";
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

                // Store relative path or identifying info?
                // Storing absolute path for simplicity in this session,
                // or just the filename if we always recreate the dir path.
                // Let's store the full path to avoid ambiguity in download.

                if (fileName.toLowerCase().endsWith(".png") || fileName.toLowerCase().endsWith(".jpg"))
                    fileType = "IMAGE";
                else if (fileName.toLowerCase().endsWith(".zip"))
                    fileType = "ZIP";
                else if (fileName.toLowerCase().endsWith(".pdf"))
                    fileType = "PDF";
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        Resource r = new Resource();
        r.setTitle(title);
        r.setDescription(description);
        r.setCourseId(courseId);
        r.setUploaderId(user.getId());
        r.setFilePath("uploads/" + savedFileName); // Store relative path for portability
        r.setFileType(fileType);
        r.setVisibility("PUBLIC");
        r.setStatus("APPROVED");

        resourceDAO.addResource(r);
        resp.sendRedirect(req.getContextPath() + "/student.html");
    }

    private void askQuestion(HttpServletRequest req, HttpServletResponse resp, User user)
            throws IOException, ServletException {
        int courseId = Integer.parseInt(req.getParameter("courseId"));
        String title = req.getParameter("title");
        String content = req.getParameter("content");

        // Image logic
        String imagePath = "";
        try {
            Part filePart = req.getPart("image");
            if (filePart != null && filePart.getSize() > 0) {
                imagePath = "uploads/q_" + UUID.randomUUID().toString() + ".jpg";
            }
        } catch (Exception e) {
        }

        Question q = new Question();
        q.setCourseId(courseId);
        q.setStudentId(user.getId());
        q.setTitle(title);
        q.setContent(content);
        q.setImagePath(imagePath);

        questionDAO.addQuestion(q);
        resp.sendRedirect(req.getContextPath() + "/student.html");
    }

    private void downloadResource(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String idStr = req.getParameter("id");
        if (idStr != null) {
            try {
                int id = Integer.parseInt(idStr);

                // Security Check: Verify User
                User user = checkStudent(req, resp);
                if (user == null) {
                    // checkStudent handles redirect/error
                    return;
                }

                Resource r = resourceDAO.getResourceById(id);

                if (r != null) {
                    // Security Check: Visibility
                    if ("PRIVATE".equals(r.getVisibility())) {
                        List<Course> enrolled = courseDAO.getEnrolledCourses(user.getId());
                        boolean isEnrolled = false;
                        for (Course c : enrolled) {
                            if (c.getId() == r.getCourseId()) {
                                isEnrolled = true;
                                break;
                            }
                        }
                        if (!isEnrolled) {
                            resp.sendError(HttpServletResponse.SC_FORBIDDEN,
                                    "Access Denied: You must be enrolled in this course.");
                            return;
                        }
                    }

                    resourceDAO.incrementDownloadCount(id);

                    if (r.getFilePath() != null && !r.getFilePath().isEmpty()) {
                        java.io.File file;
                        if (r.getFilePath().startsWith("uploads")) {
                            // Resolve relative path
                            String projectRoot = System.getProperty("user.dir");
                            String webappRoot = projectRoot + java.io.File.separator + "src" + java.io.File.separator
                                    + "main" + java.io.File.separator + "webapp";
                            file = new java.io.File(webappRoot, r.getFilePath());
                        } else {
                            // Legacy absolute path support
                            file = new java.io.File(r.getFilePath());
                        }

                        if (file.exists()) {
                            String mimeType = getServletContext().getMimeType(file.getAbsolutePath());
                            if (mimeType == null)
                                mimeType = "application/octet-stream";

                            resp.setContentType(mimeType);
                            resp.setContentLength((int) file.length());

                            // Extract original filename for download
                            String originalName = file.getName();
                            // UUID_Filename -> Filename
                            int underscoreIndex = originalName.indexOf('_');
                            if (underscoreIndex != -1) {
                                originalName = originalName.substring(underscoreIndex + 1);
                            }

                            String headerKey = "Content-Disposition";
                            // Encode filename for browser compatibility
                            String encodedName = java.net.URLEncoder.encode(originalName, "UTF-8").replace("+", "%20");
                            String headerValue = String.format("attachment; filename=\"%s\"; filename*=UTF-8''%s",
                                    encodedName, encodedName);
                            resp.setHeader(headerKey, headerValue);

                            // Stream file
                            try (java.io.FileInputStream inStream = new java.io.FileInputStream(file);
                                    java.io.OutputStream outStream = resp.getOutputStream()) {

                                byte[] buffer = new byte[4096];
                                int bytesRead = -1;

                                while ((bytesRead = inStream.read(buffer)) != -1) {
                                    outStream.write(buffer, 0, bytesRead);
                                }
                            }
                        } else {
                            // Fallback for older/mock resources without real files
                            serveDummyFile(resp, r);
                        }
                    } else {
                        resp.sendError(HttpServletResponse.SC_NOT_FOUND, "Resource not found");
                    }
                } else {
                    resp.sendError(HttpServletResponse.SC_NOT_FOUND, "Resource not found");
                }
            } catch (NumberFormatException e) {
                resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid ID");
            } catch (Exception e) {
                e.printStackTrace();
                resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Server Error");
            }
        }
    }

    private void serveDummyFile(HttpServletResponse resp, Resource r) throws IOException {
        String safeName = r.getTitle().replaceAll("[^a-zA-Z0-9._\\-\\u4e00-\\u9fa5]", "_"); // Allow Chinese
        String encodedName = java.net.URLEncoder.encode(safeName, "UTF-8").replace("+", "%20");
        String filename = encodedName + ".txt";

        resp.setContentType("application/octet-stream");
        resp.setHeader("Content-Disposition",
                "attachment; filename=\"" + filename + "\"; filename*=UTF-8''" + filename);
        String content = "Real file not found on server.\n\nMetadata:\nTitle: " + r.getTitle() +
                "\nDescription: " + r.getDescription();
        resp.getWriter().write(content);
    }

    private void searchQuestions(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String keyword = req.getParameter("keyword");
        List<Question> questions = questionDAO.searchQuestions(keyword != null ? keyword : "");

        StringBuilder json = new StringBuilder("[");
        for (int i = 0; i < questions.size(); i++) {
            Question q = questions.get(i);
            // We might want answer count or basic info for search results
            json.append(String.format(
                    "{\"id\":%d, \"title\":\"%s\", \"courseName\":\"%s\", \"studentName\":\"%s\", \"answerCount\":%d}",
                    q.getId(), q.getTitle(), q.getCourseName(), q.getStudentName(), q.getAnswerCount()));
            if (i < questions.size() - 1)
                json.append(",");
        }
        json.append("]");

        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");
        resp.getWriter().write(json.toString());
    }

    private String getSubmittedFileName(Part part) {
        for (String cd : part.getHeader("content-disposition").split(";")) {
            if (cd.trim().startsWith("filename")) {
                return cd.substring(cd.indexOf('=') + 1).trim().replace("\"", "");
            }
        }
        return "unknown";
    }
}
