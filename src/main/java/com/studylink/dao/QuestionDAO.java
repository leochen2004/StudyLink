package com.studylink.dao;

import com.studylink.model.Question;
import com.studylink.utils.DBUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.studylink.model.Answer;
import java.util.ArrayList;
import java.util.List;

import java.util.ArrayList;
import java.util.List;

public class QuestionDAO {

    public Question getQuestionById(int id) {
        String sql = "SELECT q.*, u.full_name as student_name, c.name as course_name, " +
                "(SELECT COUNT(*) FROM answers a WHERE a.question_id = q.id) as answer_count " +
                "FROM questions q " +
                "JOIN courses c ON q.course_id = c.id " +
                "JOIN users u ON q.student_id = u.id " +
                "WHERE q.id = ?";
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            conn = DBUtil.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, id);
            rs = pstmt.executeQuery();
            if (rs.next()) {
                return mapRowToQuestion(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DBUtil.close(rs, pstmt, conn);
        }
        return null;
    }

    public boolean addQuestion(Question q) {
        String sql = "INSERT INTO questions (course_id, student_id, title, content, image_path) VALUES (?, ?, ?, ?, ?)";
        Connection conn = null;
        PreparedStatement pstmt = null;
        try {
            conn = DBUtil.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, q.getCourseId());
            pstmt.setInt(2, q.getStudentId());
            pstmt.setString(3, q.getTitle());
            pstmt.setString(4, q.getContent());
            pstmt.setString(5, q.getImagePath());
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DBUtil.close(pstmt, conn);
        }
        return false;
    }

    public List<Question> getQuestionsByStudent(int studentId) {
        List<Question> questions = new ArrayList<>();
        String sql = "SELECT q.*, u.full_name as student_name, c.name as course_name, " +
                "(SELECT COUNT(*) FROM answers a WHERE a.question_id = q.id) as answer_count " +
                "FROM questions q " +
                "JOIN courses c ON q.course_id = c.id " +
                "JOIN users u ON q.student_id = u.id " +
                "WHERE q.student_id = ? " +
                "ORDER BY q.created_at DESC";
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            conn = DBUtil.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, studentId);
            rs = pstmt.executeQuery();
            while (rs.next()) {
                questions.add(mapRowToQuestion(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DBUtil.close(rs, pstmt, conn);
        }
        return questions;
    }

    private Question mapRowToQuestion(ResultSet rs) throws SQLException {
        Question q = new Question();
        q.setId(rs.getInt("id"));
        q.setCourseId(rs.getInt("course_id"));
        q.setStudentId(rs.getInt("student_id"));
        q.setTitle(rs.getString("title"));
        q.setContent(rs.getString("content"));
        q.setImagePath(rs.getString("image_path"));
        q.setCreatedAt(rs.getTimestamp("created_at"));
        q.setStudentName(rs.getString("student_name"));
        q.setCourseName(rs.getString("course_name"));
        q.setAnswerCount(rs.getInt("answer_count"));
        return q;
    }

    public boolean addAnswer(Answer a) {
        String sql = "INSERT INTO answers (question_id, teacher_id, content, image_path) VALUES (?, ?, ?, ?)";
        String notifSql = "INSERT INTO notifications (user_id, type, message, related_link) " +
                "SELECT student_id, 'NEW_ANSWER', 'Your question has a new answer', ? " +
                "FROM questions WHERE id = ?";

        Connection conn = null;
        PreparedStatement pstmt = null;
        PreparedStatement pstmtNotif = null;
        try {
            conn = DBUtil.getConnection();
            conn.setAutoCommit(false); // 事务

            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, a.getQuestionId());
            pstmt.setInt(2, a.getTeacherId());
            pstmt.setString(3, a.getContent());
            pstmt.setString(4, a.getImagePath());
            int rows = pstmt.executeUpdate();

            if (rows > 0) {
                // 添加通知
                pstmtNotif = conn.prepareStatement(notifSql);
                pstmtNotif.setString(1, "student/question?id=" + a.getQuestionId()); // 模拟链接
                pstmtNotif.setInt(2, a.getQuestionId());
                pstmtNotif.executeUpdate();

                conn.commit();
                return true;
            } else {
                conn.rollback();
            }
        } catch (SQLException e) {
            e.printStackTrace();
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
        } finally {
            DBUtil.close(pstmt, null); // 手动关闭 pstmt
            DBUtil.close(null, pstmtNotif, conn); // 关闭其余部分
        }
        return false;
    }

    public List<Question> getQuestionsByTeacher(int teacherId) {
        List<Question> questions = new ArrayList<>();
        String sql = "SELECT q.*, u.full_name as student_name, c.name as course_name, " +
                "(SELECT COUNT(*) FROM answers a WHERE a.question_id = q.id) as answer_count " +
                "FROM questions q " +
                "JOIN courses c ON q.course_id = c.id " +
                "JOIN users u ON q.student_id = u.id " +
                "WHERE c.teacher_id = ? " +
                "ORDER BY q.created_at DESC";
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            conn = DBUtil.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, teacherId);
            rs = pstmt.executeQuery();
            while (rs.next()) {
                questions.add(mapRowToQuestion(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DBUtil.close(rs, pstmt, conn);
        }
        return questions;
    }

    public List<Question> getAllQuestions() {
        List<Question> questions = new ArrayList<>();
        String sql = "SELECT q.*, u.full_name as student_name, c.name as course_name, " +
                "(SELECT COUNT(*) FROM answers a WHERE a.question_id = q.id) as answer_count " +
                "FROM questions q " +
                "JOIN users u ON q.student_id = u.id " +
                "JOIN courses c ON q.course_id = c.id " +
                "ORDER BY q.created_at DESC";
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = DBUtil.getConnection();
            pstmt = conn.prepareStatement(sql);
            rs = pstmt.executeQuery();

            while (rs.next()) {
                questions.add(mapRowToQuestion(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DBUtil.close(rs, pstmt, conn);
        }
        return questions;
    }

    public boolean deleteQuestion(int id) {
        String sql = "DELETE FROM questions WHERE id = ?";
        Connection conn = null;
        PreparedStatement pstmt = null;
        try {
            conn = DBUtil.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, id);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DBUtil.close(pstmt, conn);
        }
        return false;
    }

    // 如果我们列出回答，删除回答是分开的。
    // 对于管理员，如果他们列出问题，他们可能会看到详细信息。
    // 如果需要，我会添加 deleteAnswer 以保持完整性。
    public boolean deleteAnswer(int id) {
        String sql = "DELETE FROM answers WHERE id = ?";
        Connection conn = null;
        PreparedStatement pstmt = null;
        try {
            conn = DBUtil.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, id);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DBUtil.close(pstmt, conn);
        }
        return false;
    }

    public List<Answer> getAnswersByQuestionId(int questionId) {
        List<Answer> answers = new ArrayList<>();
        String sql = "SELECT a.*, u.full_name as teacher_name FROM answers a " +
                "JOIN users u ON a.teacher_id = u.id " +
                "WHERE a.question_id = ? ORDER BY a.created_at ASC";
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            conn = DBUtil.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, questionId);
            rs = pstmt.executeQuery();
            while (rs.next()) {
                Answer a = new Answer();
                a.setId(rs.getInt("id"));
                a.setQuestionId(rs.getInt("question_id"));
                a.setTeacherId(rs.getInt("teacher_id"));
                a.setContent(rs.getString("content"));
                a.setImagePath(rs.getString("image_path"));
                a.setCreatedAt(rs.getTimestamp("created_at"));
                a.setTeacherName(rs.getString("teacher_name"));
                answers.add(a);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DBUtil.close(rs, pstmt, conn);
        }
        return answers;
    }

    public List<Question> searchQuestions(String keyword) {
        List<Question> questions = new ArrayList<>();
        String sql = "SELECT q.*, u.full_name as student_name, c.name as course_name, " +
                "(SELECT COUNT(*) FROM answers a WHERE a.question_id = q.id) as answer_count, " +
                "tu.full_name as teacher_name " +
                "FROM questions q " +
                "JOIN courses c ON q.course_id = c.id " +
                "JOIN users u ON q.student_id = u.id " +
                "JOIN users tu ON c.teacher_id = tu.id " +
                "WHERE q.title LIKE ? OR q.content LIKE ? OR c.name LIKE ? OR tu.full_name LIKE ? " +
                "ORDER BY q.created_at DESC";
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            conn = DBUtil.getConnection();
            pstmt = conn.prepareStatement(sql);
            String k = "%" + keyword + "%";
            pstmt.setString(1, k);
            pstmt.setString(2, k);
            pstmt.setString(3, k);
            pstmt.setString(4, k);
            rs = pstmt.executeQuery();
            while (rs.next()) {
                questions.add(mapRowToQuestion(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DBUtil.close(rs, pstmt, conn);
        }
        return questions;
    }
}
