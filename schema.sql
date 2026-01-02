-- Database Schema for University Online Learning Resource Sharing and Q&A System
-- Database: MySQL 8.0+
-- Encoding: UTF8MB4

DROP DATABASE IF EXISTS `study_link_db`;
CREATE DATABASE `study_link_db` CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE `study_link_db`;

-- =============================================
-- 1. Users Table (Roles: ADMIN, STUDENT, TEACHER)
-- =============================================
CREATE TABLE `users` (
    `id` INT AUTO_INCREMENT PRIMARY KEY,
    `username` VARCHAR(50) NOT NULL UNIQUE,
    `password` VARCHAR(255) NOT NULL, -- In real app, store bcrypt hash
    `role` ENUM('ADMIN', 'TEACHER', 'STUDENT') NOT NULL,
    `full_name` VARCHAR(100) NOT NULL,
    `email` VARCHAR(100),
    `avatar_url` VARCHAR(255),
    `bio` TEXT, -- Personal introduction for Teacher/Student
    `title` VARCHAR(50), -- Only for Teacher (e.g. Professor)
    `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    `updated_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB;

-- =============================================
-- 2. Courses Table
-- =============================================
CREATE TABLE `courses` (
    `id` INT AUTO_INCREMENT PRIMARY KEY,
    `name` VARCHAR(100) NOT NULL,
    `teacher_id` INT NOT NULL,
    `description` TEXT NOT NULL,
    `department` VARCHAR(100) NOT NULL, -- e.g. Computer Science
    `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    `updated_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    FOREIGN KEY (`teacher_id`) REFERENCES `users`(`id`) ON DELETE CASCADE
) ENGINE=InnoDB;

-- =============================================
-- 3. Resources Table
-- =============================================
CREATE TABLE `resources` (
    `id` INT AUTO_INCREMENT PRIMARY KEY,
    `title` VARCHAR(200) NOT NULL,
    `description` TEXT, -- Summary/Intro
    `course_id` INT NOT NULL,
    `uploader_id` INT NOT NULL, -- Can be Student or Teacher
    `file_path` VARCHAR(255) NOT NULL,
    `file_type` VARCHAR(20), -- e.g. 'pdf', 'jpg', 'zip'
    `download_count` INT DEFAULT 0, -- Mandatory Requirement
    `visibility` ENUM('PUBLIC', 'PRIVATE') DEFAULT 'PUBLIC', -- Teacher can set to PRIVATE (Class only)
    `status` ENUM('APPROVED', 'PENDING', 'REJECTED') DEFAULT 'APPROVED', -- Admin control
    `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    FOREIGN KEY (`course_id`) REFERENCES `courses`(`id`) ON DELETE CASCADE,
    FOREIGN KEY (`uploader_id`) REFERENCES `users`(`id`) ON DELETE CASCADE
) ENGINE=InnoDB;

-- =============================================
-- 4. Questions Table
-- =============================================
CREATE TABLE `questions` (
    `id` INT AUTO_INCREMENT PRIMARY KEY,
    `course_id` INT NOT NULL,
    `student_id` INT NOT NULL,
    `title` VARCHAR(200) NOT NULL,
    `content` TEXT NOT NULL,
    `image_path` VARCHAR(255), -- Attachment support
    `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    FOREIGN KEY (`course_id`) REFERENCES `courses`(`id`) ON DELETE CASCADE,
    FOREIGN KEY (`student_id`) REFERENCES `users`(`id`) ON DELETE CASCADE
) ENGINE=InnoDB;

-- =============================================
-- 5. Answers Table
-- =============================================
CREATE TABLE `answers` (
    `id` INT AUTO_INCREMENT PRIMARY KEY,
    `question_id` INT NOT NULL,
    `teacher_id` INT NOT NULL,
    `content` TEXT NOT NULL,
    `image_path` VARCHAR(255), -- Attachment support
    `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    FOREIGN KEY (`question_id`) REFERENCES `questions`(`id`) ON DELETE CASCADE,
    FOREIGN KEY (`teacher_id`) REFERENCES `users`(`id`) ON DELETE CASCADE
) ENGINE=InnoDB;

-- =============================================
-- 6. Notifications Table
-- =============================================
CREATE TABLE `notifications` (
    `id` INT AUTO_INCREMENT PRIMARY KEY,
    `user_id` INT NOT NULL, -- Receiver
    `type` ENUM('NEW_ANSWER', 'TODO_QUESTION', 'SYSTEM') NOT NULL,
    `message` TEXT NOT NULL,
    `related_link` VARCHAR(255), -- Link to the resource or question
    `is_read` BOOLEAN DEFAULT FALSE,
    `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    FOREIGN KEY (`user_id`) REFERENCES `users`(`id`) ON DELETE CASCADE
) ENGINE=InnoDB;

-- =============================================
-- 7. Initial Data
-- =============================================

-- Users (1 Admin, 3 Teachers, 4 Students)
INSERT INTO `users` (`username`, `password`, `role`, `full_name`, `email`, `bio`, `title`) VALUES 
('admin', 'admin123', 'ADMIN', 'System Administrator', 'admin@studylink.com', NULL, NULL),
('teacher1', '123456', 'TEACHER', 'Dr. Smith', 'smith@studylink.com', 'Expert in AI', 'Professor'),
('teacher2', '123456', 'TEACHER', 'Prof. Alan Turing', 'turing@studylink.com', 'Father of Computer Science', 'Professor'),
('teacher3', '123456', 'TEACHER', 'Dr. Marie Curie', 'curie@studylink.com', 'Physics and Chemistry Expert', 'Doctor'),
('student1', '123456', 'STUDENT', 'Alice Wonderland', 'alice@studylink.com', NULL, NULL),
('student2', '123456', 'STUDENT', 'Bob Smith', 'bob@studylink.com', NULL, NULL),
('student3', '123456', 'STUDENT', 'Charlie Brown', 'charlie@studylink.com', NULL, NULL),
('student4', '123456', 'STUDENT', 'Diana Prince', 'diana@studylink.com', NULL, NULL);

-- Courses
INSERT INTO `courses` (`name`, `teacher_id`, `description`, `department`) VALUES
('Introduction to Java', (SELECT id FROM users WHERE username='teacher1'), 'Learn Java Programming', 'Computer Science'),
('Advanced Algorithms', (SELECT id FROM users WHERE username='teacher2'), 'Deep dive into graph algorithms and complexity', 'Computer Science'),
('Quantum Physics 101', (SELECT id FROM users WHERE username='teacher3'), 'Introduction to Quantum Mechanics', 'Physics'),
('Organic Chemistry', (SELECT id FROM users WHERE username='teacher3'), 'Study of carbon-containing compounds', 'Chemistry'),
('Artificial Intelligence', (SELECT id FROM users WHERE username='teacher1'), 'Neural Networks and ML basics', 'Computer Science');

-- Resources
INSERT INTO `resources` (`title`, `description`, `course_id`, `uploader_id`, `file_path`, `file_type`, `download_count`, `visibility`, `status`) VALUES
('Java Basics PDF', 'Chapter 1', (SELECT id FROM courses WHERE name='Introduction to Java'), (SELECT id FROM users WHERE username='teacher1'), 'uploads/java_basics.pdf', 'PDF', 10, 'PUBLIC', 'APPROVED'),
('Graph Theory Notes', 'Lecture notes on DFS and BFS', (SELECT id FROM courses WHERE name='Advanced Algorithms'), (SELECT id FROM users WHERE username='teacher2'), 'uploads/graphs.pdf', 'PDF', 42, 'PUBLIC', 'APPROVED'),
('Quantum Mechanics Basics', 'Chapter 1 Textbook', (SELECT id FROM courses WHERE name='Quantum Physics 101'), (SELECT id FROM users WHERE username='teacher3'), 'uploads/quantum.pdf', 'PDF', 15, 'PUBLIC', 'APPROVED'),
('Periodic Table Poster', 'High res image of periodic table', (SELECT id FROM courses WHERE name='Organic Chemistry'), (SELECT id FROM users WHERE username='teacher3'), 'uploads/periodic_table.jpg', 'IMAGE', 8, 'PUBLIC', 'APPROVED'),
('AI Project Guidelines', 'Requirements for final project', (SELECT id FROM courses WHERE name='Artificial Intelligence'), (SELECT id FROM users WHERE username='teacher1'), 'uploads/ai_project.pdf', 'PDF', 100, 'PUBLIC', 'APPROVED');

-- Questions
INSERT INTO `questions` (`course_id`, `student_id`, `title`, `content`) VALUES
((SELECT id FROM courses WHERE name='Advanced Algorithms'), (SELECT id FROM users WHERE username='student2'), 'What is P vs NP?', 'I assume P is subset of NP but are they equal?'),
((SELECT id FROM courses WHERE name='Quantum Physics 101'), (SELECT id FROM users WHERE username='student3'), 'Schrodinger Cat doubt', 'Is the cat dead or alive?'),
((SELECT id FROM courses WHERE name='Artificial Intelligence'), (SELECT id FROM users WHERE username='student1'), 'Backpropagation math', 'Can you explain the derivative part?'),
((SELECT id FROM courses WHERE name='Introduction to Java'), (SELECT id FROM users WHERE username='student4'), 'NullPointerException help', 'Why do I keep getting NPE?');

-- Answers
INSERT INTO `answers` (`question_id`, `teacher_id`, `content`) VALUES
((SELECT id FROM questions WHERE title='What is P vs NP?'), (SELECT id FROM users WHERE username='teacher2'), 'It is the biggest unsolved problem in CS! Most believe they are not equal.'),
((SELECT id FROM questions WHERE title='NullPointerException help'), (SELECT id FROM users WHERE username='teacher1'), 'Check if your object is initialized before accessing its methods.');
