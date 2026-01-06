/*
 Navicat Premium Dump SQL

 Source Server         : main_connection
 Source Server Type    : MySQL
 Source Server Version : 90400 (9.4.0)
 Source Host           : localhost:3306
 Source Schema         : study_link_db

 Target Server Type    : MySQL
 Target Server Version : 90400 (9.4.0)
 File Encoding         : 65001

 Date: 04/01/2026 14:08:21
*/
-- 在文件最开头添加这几行
CREATE DATABASE IF NOT EXISTS studylink;
USE studylink;

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for answers
-- ----------------------------
DROP TABLE IF EXISTS `answers`;
CREATE TABLE `answers` (
  `id` int NOT NULL AUTO_INCREMENT,
  `question_id` int NOT NULL,
  `teacher_id` int NOT NULL,
  `content` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `image_path` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `question_id` (`question_id`),
  KEY `teacher_id` (`teacher_id`),
  CONSTRAINT `answers_ibfk_1` FOREIGN KEY (`question_id`) REFERENCES `questions` (`id`) ON DELETE CASCADE,
  CONSTRAINT `answers_ibfk_2` FOREIGN KEY (`teacher_id`) REFERENCES `users` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ----------------------------
-- Records of answers
-- ----------------------------
BEGIN;
INSERT INTO `answers` (`id`, `question_id`, `teacher_id`, `content`, `image_path`, `created_at`) VALUES (1, 1, 3, 'It is the biggest unsolved problem in CS! Most believe they are not equal.', NULL, '2026-01-04 10:22:59');
INSERT INTO `answers` (`id`, `question_id`, `teacher_id`, `content`, `image_path`, `created_at`) VALUES (2, 4, 2, 'Check if your object is initialized before accessing its methods.', NULL, '2026-01-04 10:22:59');
INSERT INTO `answers` (`id`, `question_id`, `teacher_id`, `content`, `image_path`, `created_at`) VALUES (3, 5, 2, '如果有两个理论（或模型）都能解释同一个现象，我们应该选择那个更简单的', '', '2026-01-04 14:07:14');
COMMIT;

-- ----------------------------
-- Table structure for course_students
-- ----------------------------
DROP TABLE IF EXISTS `course_students`;
CREATE TABLE `course_students` (
  `id` int NOT NULL AUTO_INCREMENT,
  `course_id` int NOT NULL,
  `student_id` int NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `unique_enrollment` (`course_id`,`student_id`),
  KEY `student_id` (`student_id`),
  CONSTRAINT `course_students_ibfk_1` FOREIGN KEY (`course_id`) REFERENCES `courses` (`id`) ON DELETE CASCADE,
  CONSTRAINT `course_students_ibfk_2` FOREIGN KEY (`student_id`) REFERENCES `users` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=9 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ----------------------------
-- Records of course_students
-- ----------------------------
BEGIN;
INSERT INTO `course_students` (`id`, `course_id`, `student_id`) VALUES (3, 1, 5);
INSERT INTO `course_students` (`id`, `course_id`, `student_id`) VALUES (4, 2, 5);
INSERT INTO `course_students` (`id`, `course_id`, `student_id`) VALUES (8, 2, 6);
INSERT INTO `course_students` (`id`, `course_id`, `student_id`) VALUES (5, 3, 5);
INSERT INTO `course_students` (`id`, `course_id`, `student_id`) VALUES (6, 4, 5);
INSERT INTO `course_students` (`id`, `course_id`, `student_id`) VALUES (7, 5, 5);
COMMIT;

-- ----------------------------
-- Table structure for courses
-- ----------------------------
DROP TABLE IF EXISTS `courses`;
CREATE TABLE `courses` (
  `id` int NOT NULL AUTO_INCREMENT,
  `name` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL,
  `teacher_id` int NOT NULL,
  `description` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `department` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL,
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `teacher_id` (`teacher_id`),
  CONSTRAINT `courses_ibfk_1` FOREIGN KEY (`teacher_id`) REFERENCES `users` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ----------------------------
-- Records of courses
-- ----------------------------
BEGIN;
INSERT INTO `courses` (`id`, `name`, `teacher_id`, `description`, `department`, `created_at`, `updated_at`) VALUES (1, 'Introduction to Java', 2, 'Learn Java Programming', 'Computer Science', '2026-01-04 10:22:59', '2026-01-04 10:22:59');
INSERT INTO `courses` (`id`, `name`, `teacher_id`, `description`, `department`, `created_at`, `updated_at`) VALUES (2, 'Advanced Algorithms', 3, 'Deep dive into graph algorithms and complexity', 'Computer Science', '2026-01-04 10:22:59', '2026-01-04 10:22:59');
INSERT INTO `courses` (`id`, `name`, `teacher_id`, `description`, `department`, `created_at`, `updated_at`) VALUES (3, 'Quantum Physics 101', 4, 'Introduction to Quantum Mechanics', 'Physics', '2026-01-04 10:22:59', '2026-01-04 10:22:59');
INSERT INTO `courses` (`id`, `name`, `teacher_id`, `description`, `department`, `created_at`, `updated_at`) VALUES (4, 'Organic Chemistry', 4, 'Study of carbon-containing compounds', 'Chemistry', '2026-01-04 10:22:59', '2026-01-04 10:22:59');
INSERT INTO `courses` (`id`, `name`, `teacher_id`, `description`, `department`, `created_at`, `updated_at`) VALUES (5, 'Artificial Intelligence', 2, 'Neural Networks and ML basics', 'Computer Science', '2026-01-04 10:22:59', '2026-01-04 10:22:59');
COMMIT;

-- ----------------------------
-- Table structure for notifications
-- ----------------------------
DROP TABLE IF EXISTS `notifications`;
CREATE TABLE `notifications` (
  `id` int NOT NULL AUTO_INCREMENT,
  `user_id` int NOT NULL,
  `type` enum('NEW_ANSWER','TODO_QUESTION','SYSTEM') COLLATE utf8mb4_unicode_ci NOT NULL,
  `message` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `related_link` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `is_read` tinyint(1) DEFAULT '0',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `user_id` (`user_id`),
  CONSTRAINT `notifications_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ----------------------------
-- Records of notifications
-- ----------------------------
BEGIN;
INSERT INTO `notifications` (`id`, `user_id`, `type`, `message`, `related_link`, `is_read`, `created_at`) VALUES (1, 5, 'NEW_ANSWER', 'Your question has a new answer', 'student/question?id=5', 0, '2026-01-04 14:07:14');
INSERT INTO `notifications` (`id`, `user_id`, `type`, `message`, `related_link`, `is_read`, `created_at`) VALUES (2, 5, 'NEW_ANSWER', 'Your question has been answered by Dr. Smith', 'student.html#qa', 0, '2026-01-04 14:07:14');
COMMIT;

-- ----------------------------
-- Table structure for questions
-- ----------------------------
DROP TABLE IF EXISTS `questions`;
CREATE TABLE `questions` (
  `id` int NOT NULL AUTO_INCREMENT,
  `course_id` int NOT NULL,
  `student_id` int NOT NULL,
  `title` varchar(200) COLLATE utf8mb4_unicode_ci NOT NULL,
  `content` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `image_path` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `course_id` (`course_id`),
  KEY `student_id` (`student_id`),
  CONSTRAINT `questions_ibfk_1` FOREIGN KEY (`course_id`) REFERENCES `courses` (`id`) ON DELETE CASCADE,
  CONSTRAINT `questions_ibfk_2` FOREIGN KEY (`student_id`) REFERENCES `users` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ----------------------------
-- Records of questions
-- ----------------------------
BEGIN;
INSERT INTO `questions` (`id`, `course_id`, `student_id`, `title`, `content`, `image_path`, `created_at`) VALUES (1, 2, 6, 'What is P vs NP?', 'I assume P is subset of NP but are they equal?', NULL, '2026-01-04 10:22:59');
INSERT INTO `questions` (`id`, `course_id`, `student_id`, `title`, `content`, `image_path`, `created_at`) VALUES (2, 3, 7, 'Schrodinger Cat doubt', 'Is the cat dead or alive?', NULL, '2026-01-04 10:22:59');
INSERT INTO `questions` (`id`, `course_id`, `student_id`, `title`, `content`, `image_path`, `created_at`) VALUES (3, 5, 5, 'Backpropagation math', 'Can you explain the derivative part?', NULL, '2026-01-04 10:22:59');
INSERT INTO `questions` (`id`, `course_id`, `student_id`, `title`, `content`, `image_path`, `created_at`) VALUES (4, 1, 8, 'NullPointerException help', 'Why do I keep getting NPE?', NULL, '2026-01-04 10:22:59');
INSERT INTO `questions` (`id`, `course_id`, `student_id`, `title`, `content`, `image_path`, `created_at`) VALUES (5, 5, 5, '问一个有关奥卡姆剃刀原则的问题', '请老师帮我详细阐述一下奥卡姆剃刀原则的概念', '', '2026-01-04 14:06:07');
COMMIT;

-- ----------------------------
-- Table structure for resources
-- ----------------------------
DROP TABLE IF EXISTS `resources`;
CREATE TABLE `resources` (
  `id` int NOT NULL AUTO_INCREMENT,
  `title` varchar(200) COLLATE utf8mb4_unicode_ci NOT NULL,
  `description` text COLLATE utf8mb4_unicode_ci,
  `course_id` int NOT NULL,
  `uploader_id` int NOT NULL,
  `file_path` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `file_type` varchar(20) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `download_count` int DEFAULT '0',
  `visibility` enum('PUBLIC','PRIVATE') COLLATE utf8mb4_unicode_ci DEFAULT 'PUBLIC',
  `status` enum('APPROVED','PENDING','REJECTED') COLLATE utf8mb4_unicode_ci DEFAULT 'APPROVED',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `course_id` (`course_id`),
  KEY `uploader_id` (`uploader_id`),
  CONSTRAINT `resources_ibfk_1` FOREIGN KEY (`course_id`) REFERENCES `courses` (`id`) ON DELETE CASCADE,
  CONSTRAINT `resources_ibfk_2` FOREIGN KEY (`uploader_id`) REFERENCES `users` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=13 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ----------------------------
-- Records of resources
-- ----------------------------
BEGIN;
INSERT INTO `resources` (`id`, `title`, `description`, `course_id`, `uploader_id`, `file_path`, `file_type`, `download_count`, `visibility`, `status`, `created_at`) VALUES (1, 'Java Basics PDF', 'Chapter 1', 1, 2, 'uploads/java_basics.pdf', 'PDF', 10, 'PUBLIC', 'APPROVED', '2026-01-04 10:22:59');
INSERT INTO `resources` (`id`, `title`, `description`, `course_id`, `uploader_id`, `file_path`, `file_type`, `download_count`, `visibility`, `status`, `created_at`) VALUES (2, 'Graph Theory Notes', 'Lecture notes on DFS and BFS', 2, 3, 'uploads/graphs.pdf', 'PDF', 42, 'PUBLIC', 'APPROVED', '2026-01-04 10:22:59');
INSERT INTO `resources` (`id`, `title`, `description`, `course_id`, `uploader_id`, `file_path`, `file_type`, `download_count`, `visibility`, `status`, `created_at`) VALUES (3, 'Quantum Mechanics Basics', 'Chapter 1 Textbook', 3, 4, 'uploads/quantum.pdf', 'PDF', 15, 'PUBLIC', 'APPROVED', '2026-01-04 10:22:59');
INSERT INTO `resources` (`id`, `title`, `description`, `course_id`, `uploader_id`, `file_path`, `file_type`, `download_count`, `visibility`, `status`, `created_at`) VALUES (4, 'Periodic Table Poster', 'High res image of periodic table', 4, 4, 'uploads/periodic_table.jpg', 'IMAGE', 8, 'PUBLIC', 'APPROVED', '2026-01-04 10:22:59');
INSERT INTO `resources` (`id`, `title`, `description`, `course_id`, `uploader_id`, `file_path`, `file_type`, `download_count`, `visibility`, `status`, `created_at`) VALUES (5, 'AI Project Guidelines', 'Requirements for final project', 5, 2, 'uploads/ai_project.pdf', 'PDF', 100, 'PUBLIC', 'APPROVED', '2026-01-04 10:22:59');
INSERT INTO `resources` (`id`, `title`, `description`, `course_id`, `uploader_id`, `file_path`, `file_type`, `download_count`, `visibility`, `status`, `created_at`) VALUES (8, '资源0', '', 2, 5, 'uploads/649b7bbf-e476-4d22-a0ef-1a435930908b_AlgoLec0-handout.pdf', 'PDF', 3, 'PUBLIC', 'APPROVED', '2026-01-04 13:15:28');
INSERT INTO `resources` (`id`, `title`, `description`, `course_id`, `uploader_id`, `file_path`, `file_type`, `download_count`, `visibility`, `status`, `created_at`) VALUES (11, '西瓜书', '西瓜书', 5, 2, 'uploads/97f8cc62-104f-4967-8abf-f0d35cf80d72_Machine-Learning 《机器学习（西瓜书）》_周志华.pdf', 'PDF', 0, 'PUBLIC', 'APPROVED', '2026-01-04 13:34:11');
INSERT INTO `resources` (`id`, `title`, `description`, `course_id`, `uploader_id`, `file_path`, `file_type`, `download_count`, `visibility`, `status`, `created_at`) VALUES (12, '高级数据结构教程', 'lecture1', 2, 3, 'uploads/bfc7036a-608c-4fc5-8da5-514789cf34ac_lecture1.ppt', 'FILE', 3, 'PRIVATE', 'APPROVED', '2026-01-04 14:04:05');
COMMIT;

-- ----------------------------
-- Table structure for users
-- ----------------------------
DROP TABLE IF EXISTS `users`;
CREATE TABLE `users` (
  `id` int NOT NULL AUTO_INCREMENT,
  `username` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL,
  `password` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `role` enum('ADMIN','TEACHER','STUDENT') COLLATE utf8mb4_unicode_ci NOT NULL,
  `full_name` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL,
  `email` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `avatar_url` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `bio` text COLLATE utf8mb4_unicode_ci,
  `title` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `username` (`username`)
) ENGINE=InnoDB AUTO_INCREMENT=9 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ----------------------------
-- Records of users
-- ----------------------------
BEGIN;
INSERT INTO `users` (`id`, `username`, `password`, `role`, `full_name`, `email`, `avatar_url`, `bio`, `title`, `created_at`, `updated_at`) VALUES (1, 'admin', 'admin123', 'ADMIN', 'System Administrator', 'admin@studylink.com', NULL, NULL, NULL, '2026-01-04 10:22:59', '2026-01-04 10:22:59');
INSERT INTO `users` (`id`, `username`, `password`, `role`, `full_name`, `email`, `avatar_url`, `bio`, `title`, `created_at`, `updated_at`) VALUES (2, 'teacher1', '123456', 'TEACHER', 'Dr. Smith', 'smith@studylink.com', NULL, 'Expert in AI', 'Professor', '2026-01-04 10:22:59', '2026-01-04 10:22:59');
INSERT INTO `users` (`id`, `username`, `password`, `role`, `full_name`, `email`, `avatar_url`, `bio`, `title`, `created_at`, `updated_at`) VALUES (3, 'teacher2', '123456', 'TEACHER', 'Prof. Alan Turing', 'turing@studylink.com', NULL, 'Father of Computer Science', 'Professor', '2026-01-04 10:22:59', '2026-01-04 10:22:59');
INSERT INTO `users` (`id`, `username`, `password`, `role`, `full_name`, `email`, `avatar_url`, `bio`, `title`, `created_at`, `updated_at`) VALUES (4, 'teacher3', '123456', 'TEACHER', 'Dr. Marie Curie', 'curie@studylink.com', NULL, 'Physics and Chemistry Expert', 'Doctor', '2026-01-04 10:22:59', '2026-01-04 10:22:59');
INSERT INTO `users` (`id`, `username`, `password`, `role`, `full_name`, `email`, `avatar_url`, `bio`, `title`, `created_at`, `updated_at`) VALUES (5, 'student1', '123456', 'STUDENT', 'Alice Wonderland', 'alice@studylink.com', NULL, NULL, NULL, '2026-01-04 10:22:59', '2026-01-04 10:22:59');
INSERT INTO `users` (`id`, `username`, `password`, `role`, `full_name`, `email`, `avatar_url`, `bio`, `title`, `created_at`, `updated_at`) VALUES (6, 'student2', '123456', 'STUDENT', 'Bob Smith', 'bob@studylink.com', NULL, NULL, NULL, '2026-01-04 10:22:59', '2026-01-04 10:22:59');
INSERT INTO `users` (`id`, `username`, `password`, `role`, `full_name`, `email`, `avatar_url`, `bio`, `title`, `created_at`, `updated_at`) VALUES (7, 'student3', '123456', 'STUDENT', 'Charlie Brown', 'charlie@studylink.com', NULL, NULL, NULL, '2026-01-04 10:22:59', '2026-01-04 10:22:59');
INSERT INTO `users` (`id`, `username`, `password`, `role`, `full_name`, `email`, `avatar_url`, `bio`, `title`, `created_at`, `updated_at`) VALUES (8, 'student4', '123456', 'STUDENT', 'Diana Prince', 'diana@studylink.com', NULL, NULL, NULL, '2026-01-04 10:22:59', '2026-01-04 10:22:59');
COMMIT;

SET FOREIGN_KEY_CHECKS = 1;
