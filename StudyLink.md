# StudyLink 项目启动指南

**测试日期:** 2026-01-04  
**环境:** Localhost (Tomcat 7.0.47)  
**测试人员:** Antigravity Agent

---

## 系统功能模块

StudyLink 平台包含三个主要角色：学生、教师和管理员，各端功能如下：

### 学生端

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

### 教师端

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

### 管理员端

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

## 项目启动指南

### 1. 环境准备

在启动项目之前，请确保你的本地开发环境满足以下要求：

- **JDK**: 1.8 (必需)
- **Maven**: 3.x (用于构建和依赖管理)
- **MySQL**: 8.0+ (建议 8.0.33)

### 2. 数据库配置

本项目使用 MySQL 数据库。

1.  **初始化数据库**:
    使用项目根目录下的 `schema.sql` 脚本创建数据库和表结构。

    ```bash
    mysql -u root -p -e "CREATE DATABASE IF NOT EXISTS study_link_db;"
    mysql -u root -p study_link_db < study_link_db.sql
    ```

2.  **配置连接信息 (.env.local)**:
    为了安全和便利，建议在项目根目录下创建一个名为 `.env.local` 的文件（该文件已被 git 忽略），并填入你的本地数据库配置：
    ```properties
    # .env.local 示例
    DB_URL=jdbc:mysql://localhost:3306/study_link_db?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true
    DB_USERNAME=你的用户名 (例如 root)
    DB_PASSWORD=你的密码
    ```
    _如果不创建此文件，系统将默认尝试使用 `root` / `123456` 连接。_

### 3. 启动项目

本项目集成了 Tomcat 7 Maven 插件，支持一键启动。

在项目根目录下运行：

```bash
mvn tomcat7:run
```

启动成功后，访问地址：[http://localhost:8081/studylink](http://localhost:8081/studylink)

---
