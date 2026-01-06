package com.studylink.model;

import java.sql.Timestamp;

public class Course {
    private int id;
    private String name;
    private int teacherId;
    private String description;
    private String department;
    private Timestamp createdAt;

    // 用于显示的额外字段（教师姓名）
    private String teacherName;

    public Course() {
    }

    public Course(String name, int teacherId, String description, String department) {
        this.name = name;
        this.teacherId = teacherId;
        this.description = description;
        this.department = department;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getTeacherId() {
        return teacherId;
    }

    public void setTeacherId(int teacherId) {
        this.teacherId = teacherId;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    public String getTeacherName() {
        return teacherName;
    }

    public void setTeacherName(String teacherName) {
        this.teacherName = teacherName;
    }
}
