package com.studylink.dao;

import com.studylink.model.Resource;
import com.studylink.utils.DBUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import java.util.ArrayList;
import java.util.List;

import java.util.ArrayList;
import java.util.List;

public class ResourceDAO {

    public Resource getResourceById(int id) {
        String sql = "SELECT r.*, u.full_name as uploader_name, c.name as course_name " +
                "FROM resources r " +
                "JOIN users u ON r.uploader_id = u.id " +
                "JOIN courses c ON r.course_id = c.id " +
                "WHERE r.id = ?";
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            conn = DBUtil.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, id);
            rs = pstmt.executeQuery();
            if (rs.next()) {
                return mapRowToResource(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DBUtil.close(rs, pstmt, conn);
        }
        return null;
    }

    public List<Resource> searchResources(String keyword) {
        List<Resource> resources = new ArrayList<>();
        String sql = "SELECT r.*, u.full_name as uploader_name, c.name as course_name " +
                "FROM resources r " +
                "JOIN users u ON r.uploader_id = u.id " +
                "JOIN courses c ON r.course_id = c.id " +
                "WHERE (r.title LIKE ? OR r.description LIKE ?) AND r.status = 'APPROVED' " +
                "ORDER BY r.created_at DESC";
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            conn = DBUtil.getConnection();
            pstmt = conn.prepareStatement(sql);
            String k = "%" + keyword + "%";
            pstmt.setString(1, k);
            pstmt.setString(2, k);
            rs = pstmt.executeQuery();
            while (rs.next()) {
                resources.add(mapRowToResource(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DBUtil.close(rs, pstmt, conn);
        }
        return resources;
    }

    public boolean incrementDownloadCount(int id) {
        String sql = "UPDATE resources SET download_count = download_count + 1 WHERE id = ?";
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

    public List<Resource> getStudentVisibleResources(int studentId) {
        List<Resource> resources = new ArrayList<>();
        String sql = "SELECT r.*, u.full_name as uploader_name, c.name as course_name " +
                "FROM resources r " +
                "JOIN users u ON r.uploader_id = u.id " +
                "JOIN courses c ON r.course_id = c.id " +
                "WHERE r.status = 'APPROVED' AND (" +
                "  r.visibility = 'PUBLIC' " +
                "  OR " +
                "  (r.visibility = 'PRIVATE' AND r.course_id IN (SELECT course_id FROM course_students WHERE student_id = ?))"
                +
                ") " +
                "ORDER BY r.created_at DESC";
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            conn = DBUtil.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, studentId);
            rs = pstmt.executeQuery();
            while (rs.next()) {
                resources.add(mapRowToResource(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DBUtil.close(rs, pstmt, conn);
        }
        return resources;
    }

    public List<Resource> getResourcesByUploader(int uploaderId) {
        List<Resource> resources = new ArrayList<>();
        String sql = "SELECT r.*, u.full_name as uploader_name, c.name as course_name " +
                "FROM resources r " +
                "JOIN users u ON r.uploader_id = u.id " +
                "JOIN courses c ON r.course_id = c.id " +
                "WHERE r.uploader_id = ? " +
                "ORDER BY r.created_at DESC";
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            conn = DBUtil.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, uploaderId);
            rs = pstmt.executeQuery();
            while (rs.next()) {
                resources.add(mapRowToResource(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DBUtil.close(rs, pstmt, conn);
        }
        return resources;
    }

    private Resource mapRowToResource(ResultSet rs) throws SQLException {
        Resource r = new Resource();
        r.setId(rs.getInt("id"));
        r.setTitle(rs.getString("title"));
        r.setDescription(rs.getString("description"));
        r.setCourseId(rs.getInt("course_id"));
        r.setUploaderId(rs.getInt("uploader_id"));
        r.setFilePath(rs.getString("file_path"));
        r.setFileType(rs.getString("file_type"));
        r.setDownloadCount(rs.getInt("download_count"));
        r.setVisibility(rs.getString("visibility"));
        r.setStatus(rs.getString("status"));
        r.setCreatedAt(rs.getTimestamp("created_at"));
        r.setUploaderName(rs.getString("uploader_name"));
        r.setCourseName(rs.getString("course_name"));
        return r;
    }

    public boolean addResource(Resource r) {
        String sql = "INSERT INTO resources (title, description, course_id, uploader_id, file_path, file_type, visibility, status) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        Connection conn = null;
        PreparedStatement pstmt = null;
        try {
            conn = DBUtil.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, r.getTitle());
            pstmt.setString(2, r.getDescription());
            pstmt.setInt(3, r.getCourseId());
            pstmt.setInt(4, r.getUploaderId());
            pstmt.setString(5, r.getFilePath());
            pstmt.setString(6, r.getFileType());
            pstmt.setString(7, r.getVisibility());
            pstmt.setString(8, "APPROVED"); // Teachers auto approved? Or PENDING? Task says "Teacher publishes
                                            // resource", likely approved or auto. Admin moderates. Let's say APPROVED.
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DBUtil.close(pstmt, conn);
        }
        return false;
    }

    public List<Resource> getAllResources() {
        List<Resource> resources = new ArrayList<>();
        String sql = "SELECT r.*, u.full_name as uploader_name, c.name as course_name " +
                "FROM resources r " +
                "JOIN users u ON r.uploader_id = u.id " +
                "JOIN courses c ON r.course_id = c.id " +
                "ORDER BY r.created_at DESC";
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = DBUtil.getConnection();
            pstmt = conn.prepareStatement(sql);
            rs = pstmt.executeQuery();

            while (rs.next()) {
                resources.add(mapRowToResource(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DBUtil.close(rs, pstmt, conn);
        }
        return resources;
    }

    public boolean updateStatus(int id, String status) {
        String sql = "UPDATE resources SET status = ? WHERE id = ?";
        Connection conn = null;
        PreparedStatement pstmt = null;

        try {
            conn = DBUtil.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, status);
            pstmt.setInt(2, id);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DBUtil.close(pstmt, conn);
        }
        return false;
    }

    public boolean updateVisibility(int id, String visibility) {
        String sql = "UPDATE resources SET visibility = ? WHERE id = ?";
        Connection conn = null;
        PreparedStatement pstmt = null;

        try {
            conn = DBUtil.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, visibility);
            pstmt.setInt(2, id);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DBUtil.close(pstmt, conn);
        }
        return false;
    }

    public boolean deleteResource(int id) {
        String sql = "DELETE FROM resources WHERE id = ?";
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
}
