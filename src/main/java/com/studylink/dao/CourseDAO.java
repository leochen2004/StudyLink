package com.studylink.dao;

import com.studylink.model.Course;
import com.studylink.utils.DBUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import java.util.ArrayList;
import java.util.List;

public class CourseDAO {

    public CourseDAO() {
        createTableIfNotExists();
    }

    private void createTableIfNotExists() {
        String sql = "CREATE TABLE IF NOT EXISTS course_students (" +
                "id INT AUTO_INCREMENT PRIMARY KEY, " +
                "course_id INT NOT NULL, " +
                "student_id INT NOT NULL, " +
                "UNIQUE KEY unique_enrollment (course_id, student_id), " +
                "FOREIGN KEY (course_id) REFERENCES courses(id) ON DELETE CASCADE, " +
                "FOREIGN KEY (student_id) REFERENCES users(id) ON DELETE CASCADE" +
                ")";
        Connection conn = null;
        PreparedStatement pstmt = null;
        try {
            conn = DBUtil.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DBUtil.close(pstmt, conn);
        }
    }

    public boolean enrollStudent(int courseId, int studentId) {
        String sql = "INSERT IGNORE INTO course_students (course_id, student_id) VALUES (?, ?)";
        Connection conn = null;
        PreparedStatement pstmt = null;
        try {
            conn = DBUtil.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, courseId);
            pstmt.setInt(2, studentId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DBUtil.close(pstmt, conn);
        }
        return false;
    }

    public List<Course> getEnrolledCourses(int studentId) {
        List<Course> courses = new ArrayList<>();
        String sql = "SELECT c.*, u.full_name as teacher_name " +
                "FROM courses c " +
                "JOIN course_students cs ON c.id = cs.course_id " +
                "JOIN users u ON c.teacher_id = u.id " +
                "WHERE cs.student_id = ? " +
                "ORDER BY c.created_at DESC";
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            conn = DBUtil.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, studentId);
            rs = pstmt.executeQuery();
            while (rs.next()) {
                Course c = new Course();
                c.setId(rs.getInt("id"));
                c.setName(rs.getString("name"));
                c.setTeacherId(rs.getInt("teacher_id"));
                c.setDescription(rs.getString("description"));
                c.setDepartment(rs.getString("department"));
                c.setCreatedAt(rs.getTimestamp("created_at"));
                c.setTeacherName(rs.getString("teacher_name"));
                courses.add(c);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DBUtil.close(rs, pstmt, conn);
        }
        return courses;
    }

    public List<Course> getCoursesByTeacher(int teacherId) {
        List<Course> courses = new ArrayList<>();
        String sql = "SELECT c.*, u.full_name as teacher_name FROM courses c JOIN users u ON c.teacher_id = u.id WHERE c.teacher_id = ? ORDER BY c.created_at DESC";
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            conn = DBUtil.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, teacherId);
            rs = pstmt.executeQuery();
            while (rs.next()) {
                Course c = new Course();
                c.setId(rs.getInt("id"));
                c.setName(rs.getString("name"));
                c.setTeacherId(rs.getInt("teacher_id"));
                c.setDescription(rs.getString("description"));
                c.setDepartment(rs.getString("department"));
                c.setCreatedAt(rs.getTimestamp("created_at"));
                c.setTeacherName(rs.getString("teacher_name"));
                courses.add(c);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DBUtil.close(rs, pstmt, conn);
        }
        return courses;
    }

    public List<Course> getAllCourses() {
        List<Course> courses = new ArrayList<>();
        String sql = "SELECT c.*, u.full_name as teacher_name FROM courses c JOIN users u ON c.teacher_id = u.id ORDER BY c.created_at DESC";
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = DBUtil.getConnection();
            pstmt = conn.prepareStatement(sql);
            rs = pstmt.executeQuery();

            while (rs.next()) {
                Course c = new Course();
                c.setId(rs.getInt("id"));
                c.setName(rs.getString("name"));
                c.setTeacherId(rs.getInt("teacher_id"));
                c.setDescription(rs.getString("description"));
                c.setDepartment(rs.getString("department"));
                c.setCreatedAt(rs.getTimestamp("created_at"));
                c.setTeacherName(rs.getString("teacher_name"));
                courses.add(c);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DBUtil.close(rs, pstmt, conn);
        }
        return courses;
    }

    public boolean addCourse(Course course) {
        String sql = "INSERT INTO courses (name, teacher_id, description, department) VALUES (?, ?, ?, ?)";
        Connection conn = null;
        PreparedStatement pstmt = null;

        try {
            conn = DBUtil.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, course.getName());
            pstmt.setInt(2, course.getTeacherId());
            pstmt.setString(3, course.getDescription());
            pstmt.setString(4, course.getDepartment());

            int rows = pstmt.executeUpdate();
            return rows > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DBUtil.close(pstmt, conn);
        }
        return false;
    }

    public boolean deleteCourse(int id) {
        String sql = "DELETE FROM courses WHERE id = ?";
        Connection conn = null;
        PreparedStatement pstmt = null;

        try {
            conn = DBUtil.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, id);
            int rows = pstmt.executeUpdate();
            return rows > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DBUtil.close(pstmt, conn);
        }
        return false;
    }
}
