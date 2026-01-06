# StudyLink - 大学生线上学习资源共享与问答系统

这是一个基于 Java Web (Servlet + JDBC) 开发的学习资源共享与答疑互动平台。该项目旨在提高大学生的在线学习效率，支持文件共享、在线问答、课程管理等功能。

---


## 1. 技术选型

本项目坚持 **"轻量级、原生化"** 的原则，采用经典的 Java EE 技术栈，未使用重型框架 (Spring/MyBatis)，以确保运行高效且易于理解底层原理。

1.  **学生选课 (Student Enrollment)**
    - _功能描述_: 浏览可选课程并进行订阅。
    - _核心实现_: 后端 `StudentServlet` 接收 `POST` 请求。
    - _技术细节_: 调用 `CourseDAO.enrollStudent` 方法，执行 SQL `INSERT IGNORE INTO course_students ...`。利用 `IGNORE` 关键字或数据库约束防止重复选课。
2.  **资源下载 (Resource Download)**
    - _功能描述_: 安全下载教师发布的私有或公开资料。
    - _核心实现_: `StudentServlet` 处理 `/resource/download` 请求。
    - _技术细节_:
      - **权限校验**: 检查用户是否登录；若资源标记为 `PRIVATE`，需进一步验证该学生是否已选修该课程 (`CourseDAO.isStudentEnrolled`)。
      - **流式传输**: 使用 `FileInputStream` 读取服务器磁盘文件，通过 `response.getOutputStream()` 写入流。
      - **文件名处理**: 设置 `Content-Disposition` 为 `attachment`，并使用 `URLEncoder` 对文件名进行 UTF-8 编码，防止中文乱码。
3.  **资源搜索 (Resource Search)**
    - _功能描述_: 关键词模糊检索。
    - _核心实现_: 前端发送带 `keyword` 参数的请求。
    - _技术细节_: `ResourceDAO` 执行 SQL `SELECT ... FROM resources WHERE (title LIKE ? OR description LIKE ?) AND visibility = 'PUBLIC'`。只返回公开资源或学生已选课程的资源。
4.  **资源查看 (Resource View)**
    - _功能描述_: 动态加载资源列表。
    - _核心实现_: 前端 `mock-data.js` 或 原生 `fetch` 请求 `dashboard_data`。
    - _技术细节_: 后端聚合 `CourseDAO`, `ResourceDAO`, `NotificationDAO` 的查询结果，构建一个包含 `myResources`, `courses`, `notifications` 的 JSON 对象返回给前端渲染。
5.  **消息通知 (Message Notifications)**
    - _功能描述_: 教师回复时自动接收通知。
    - _核心实现_: 基于数据库的轮询或触发机制。
    - _技术细节_: **自动触发插入 (Automatic Insertion)**: 在 `QuestionDAO.addAnswer` 中使用 **数据库事务**。当 `INSERT INTO answers` 成功后，立即在同一事务中执行 `INSERT INTO notifications ... SELECT student_id FROM questions ...`。确保回答和通知的原子性一致性。
6.  **互动问答 (Q&A Interaction)**
    - _功能描述_: 提出问题并等待回复。
    - _核心实现_: `StudentServlet` 处理 `/question/add`。
    - _技术细节_: `QuestionDAO.addQuestion` 将数据存入 `questions` 表。问题与 `course_id` 和 `student_id` 关联。
7.  **个人存储 (Personal Cloud)**
    - _功能描述_: 上传个人学习文件。
    - _核心实现_: 利用 Servlet 3.0 `@MultipartConfig` 注解处理文件流。
    - _技术细节_:
      - **文件重命名**: 使用 `UUID.randomUUID()` 生成唯一文件名前缀，防止同名文件覆盖。
      - **路径存储**: 文件物理保存在服务器 `uploads` 目录，数据库 `resources` 表仅存储相对路径 string。
8.  **个人中心 (Profile Management)**
    - _功能描述_: 修改头像和资料。
    - _核心实现_: `ProfileServlet` 处理更新请求。
    - _技术细节_: 更新成功后，需同步更新 `HttpSession` 中的 `user` 对象，确保页面刷新后立即显示新资料而无需重新登录。
*   **后端核心**: Java Servlet, JDBC (原生数据库连接)
*   **前端核心**: HTML5, CCS3 (Variables), JavaScript (ES6+), AJAX (Fetch API)
*   **数据库**: MySQL 8.0
*   **构建工具**: Apache Maven
*   **服务器**: Apache Tomcat 7/8/9

---

1.  **授课管理 (Course Management)**
    - _功能描述_: 查看负责课程。
    - _核心实现_: `TeacherServlet` 初始化时加载数据。
    - _技术细节_: `CourseDAO.getCoursesByTeacher(teacherId)` 执行 `SELECT * FROM courses WHERE teacher_id = ?`。
2.  **资源发布 (Resource Publish)**
    - _功能描述_: 上传课件并设置权限。
    - _核心实现_: `TeacherServlet` 处理 `/resource/add` 的 `multipart/form-data` 请求。
    - _技术细节_: 读取表单中的 `visibility` 字段 ('PUBLIC' 或 'PRIVATE')，存入 `resources` 表。此字段决定了 `StudentServlet` 中的下载权限校验逻辑。
3.  **资源管理 (Resource Management)**
    - _功能描述_: 变更资源可见性。
    - _核心实现_: 通过 AJAX 发送 `POST /teacher/resource/update_visibility`。
    - _技术细节_: 调用 `ResourceDAO.updateVisibility` 执行 `UPDATE resources SET visibility = ? WHERE id = ?`。
4.  **问答管理 (Q&A Management)**
    - _功能描述_: 回复或撤回回答。
    - _核心实现_:
      - **待处理**: `QuestionDAO` 查询所有 `teacher_id` 匹配且 `answer_count = 0` 的问题。
      - **历史记录/撤回**: `TeacherServlet` 处理 `/answer/delete`。
      - **技术细节**: 删除回答时 (`deleteAnswer`)，通常只删除 `answers` 表记录。若需更完善，应同时处理或标记对应的通知为失效（当前简化实现仅删除回答）。
## 2. 基本设计思路

### 系统架构 (MVC)
系统严格遵循 **MVC (Model-View-Controller)** 设计模式，实现界面与逻辑的分离：
*   **Model (模型层)**: `com.studylink.model` 包中的 Java Bean (User, Course, etc.) 及 `com.studylink.dao` 包中的数据访问对象，负责数据的封装与持久化。
*   **View (视图层)**: `webapp` 目录下的 HTML 文件。不使用 JSP，而是通过 **AJAX** 异步请求后端 JSON 接口，在前端进行数据渲染，实现"前后端分离"的开发体验。
*   **Controller (控制层)**: `com.studylink.controller` 包中的 Servlet。负责接收 HTTP 请求，解析参数，调用 DAO 层业务，并返回 JSON 数据或页面跳转。

1.  **课程管理 (Course Management)**
    - _功能描述_: 维护课程数据。
    - _核心实现_: `AdminServlet` 提供 CRUD 接口。
    - _技术细节_: 直接操作 `courses` 表。删除课程时，需注意外键约束（如 `course_students`, `resources`, `questions` 表中的级联数据），通常由数据库外键 `ON DELETE CASCADE` 处理或代码逻辑处理。
2.  **教师管理 (Teacher Management)**
    - _功能描述_: 创建教师账户。
    - _核心实现_: `AdminServlet` 调用 `UserDAO.addUser`。
    - _技术细节_: 强制设置 `role = 'TEACHER'`。密码在实际生产环境中应进行 Hash 加密存储（当前 Demo 为明文或简单处理）。
3.  **资源审核 (Resource Audit)**
    - _功能描述_: 移除违规资源。
    - _核心实现_: 管理员仪表盘列出所有资源。
    - _技术细节_: 调用 `ResourceDAO.deleteResource`。不仅从数据库删除记录，理想情况下还应通过 `File` API 删除服务器磁盘上的物理文件以释放空间。
4.  **问答审核 (Q&A Audit)**
    - _功能描述_: 净化社区环境。
    - _核心实现_: 查看全站问题列表。
    - _技术细节_: 调用 `QuestionDAO.deleteQuestion`。此操作通过级联删除（DB 外键）自动移除该问题下的所有回答 (`answers`) 和相关通知。
### 数据库设计
核心表结构设计如下：
*   `users`: 存储 Admin/Teacher/Student 用户信息，通过 `role` 字段区分权限。
*   `courses`: 课程信息表。
*   `resources`: 资源表，记录文件路径及上传者/课程的关联。
*   `questions` & `answers`: 问答系统表，支持多对多关联。
*   `notifications`: 异步通知消息表。

---

## 3. 开发进度安排

| 阶段 | 时间节点 | 完成内容 | 状态 |
| :--- | :--- | :--- | :--- |
| **第一周** | Day 1-3 | 需求分析，确定功能模块，完成数据库 Schema 设计 (`study_link_db.sql`)。 | ✅ 完成 |
| | Day 4-7 | 搭建 Maven 项目骨架，实现基础的 UserDAO 及登录/注册功能 (`AuthServlet`)。 | ✅ 完成 |
| **第二周** | Day 1-4 | 完成学生端核心功能：选课、资源浏览、提问 (`StudentServlet`)。 | ✅ 完成 |
| | Day 5-7 | 完成教师端功能：回答问题、发布资源、权限管理 (`TeacherServlet`)。 | ✅ 完成 |
| **第三周** | Day 1-3 | 完成管理员模块及前端 UI 美化 (Material Design 风格)。 | ✅ 完成 |
| | Day 4-5 | 系统集成测试，修复通知不同步及文件下载路径问题。 | ✅ 完成 |
| **验收周** | Day 6-7 | 撰写技术文档及实验报告，准备答辩演示。 | ✅ 完成 |

---

## 4. 功能模块详解

### 4.1 学生功能
*   **通知提醒**: 实时查看教师回复通知。
*   **资源浏览**: 按课程/关键字检索资源，支持 PDF/图片/ZIP下载。
*   **资源上传**: 分享个人学习资料。
*   **互动问答**: 提出问题（支持图片附件），查看历史问答。
*   **个人中心**: 管理个人资料及已发布内容。

### 4.2 教师功能
*   **答疑管理**: 系统自动统计"未回答问题"，教师可进行图文回复。
*   **资源发布**: 上传课件，并可设置"仅本班可见"或"公开"。
*   **课程中心**: 查看所授课程及学生选课情况。

### 4.3 管理员功能
*   **全站维护**: 管理所有课程、教师账号、资源及问答内容，确保内容合规。

---

## 5. 如何运行

### 环境要求
*   JDK 1.8+
*   Maven 3.x
*   MySQL 8.0+

### 启动步骤

1.  **数据库初始化**:
    在 MySQL 中执行根目录下的 `study_link_db.sql` 脚本：
    ```bash
    mysql -u root -p < study_link_db.sql
    ```
    *(默认数据库账号: root / 密码: 123456。如需修改，请编辑 `src/main/resources/db.properties`)*

2.  **启动服务器**:
    在项目根目录运行 Maven 命令：
    ```bash
    mvn tomcat7:run
    ```

3.  **访问系统**:
    打开浏览器访问: [http://localhost:8081/studylink](http://localhost:8081/studylink)

### 测试账号
*   **管理员**: `admin` / `admin123`
*   **教师**: `teacher1` / `123456`
*   **学生**: `student1` / `123456`
