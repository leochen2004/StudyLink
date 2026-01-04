/**
 * Mock Data Service for StudyLink
 * Simulates a backend using localStorage to allow "real-time" interaction between
 * Student, Teacher, and Admin pages when running locally via file:// protocol.
 */

const MockAPI = {
  // Initialize Data
  init: function () {
    if (!localStorage.getItem("sl_users")) {
      const users = [
        {
          id: 1,
          username: "admin",
          password: "password",
          role: "ADMIN",
          fullName: "管理员",
        },
        {
          id: 2,
          username: "teacher",
          password: "password",
          role: "TEACHER",
          fullName: "讲师",
        },
        {
          id: 3,
          username: "student",
          password: "password",
          role: "STUDENT",
          fullName: "学生",
        },
      ];
      localStorage.setItem("sl_users", JSON.stringify(users));
    }
    if (!localStorage.getItem("sl_courses")) {
      const courses = [
        { id: 1, name: "CS101 算法导论", teacherId: 2, teacherName: "讲师" },
        { id: 2, name: "MATH200 线性代数", teacherId: 2, teacherName: "讲师" },
      ];
      localStorage.setItem("sl_courses", JSON.stringify(courses));
    }
    if (!localStorage.getItem("sl_resources")) {
      localStorage.setItem(
        "sl_resources",
        JSON.stringify([
          {
            id: 1,
            title: "课程大纲",
            description: "CS101 课程大纲",
            courseId: 1,
            uploaderId: 2,
            uploaderName: "讲师",
            status: "APPROVED",
            downloadCount: 10,
            visibility: "PUBLIC",
            type: "PDF",
          },
        ])
      );
    }
    if (!localStorage.getItem("sl_questions")) {
      localStorage.setItem(
        "sl_questions",
        JSON.stringify([
          {
            id: 1,
            title: "什么是递归？",
            content: "有人能解释一下递归吗？",
            courseId: 1,
            studentId: 3,
            studentName: "学生",
            courseName: "CS101 算法导论",
            answers: [],
          },
        ])
      );
    }
    if (!localStorage.getItem("sl_notifications")) {
      // {userId, message, read}
      localStorage.setItem("sl_notifications", JSON.stringify([]));
    }

    // Session
    if (!sessionStorage.getItem("sl_currentUser")) {
      // Default to null
    }
  },

  // Auth
  login: function (username, password, role) {
    this.init();
    const users = JSON.parse(localStorage.getItem("sl_users"));
    // Relaxed matching for demo ease: check if username exists and role matches (ignoring password for ease or simple check)
    const user = users.find((u) => u.username === username && u.role === role);
    if (user) {
      if (user.password === password) {
        sessionStorage.setItem("sl_currentUser", JSON.stringify(user));
        return { success: true, user: user };
      } else {
        return { success: false, message: "密码错误" };
      }
    }
    // Auto-create for demo if not found? No, let's strict.
    return { success: false, message: "用户未找到或角色不匹配" };
  },

  register: function (user) {
    this.init();
    const users = JSON.parse(localStorage.getItem("sl_users"));
    if (users.find((u) => u.username === user.username)) {
      return { success: false, message: "用户名已存在" };
    }
    user.id = Date.now();
    users.push(user);
    localStorage.setItem("sl_users", JSON.stringify(users));
    return { success: true };
  },

  getCurrentUser: function () {
    return JSON.parse(sessionStorage.getItem("sl_currentUser"));
  },

  logout: function () {
    sessionStorage.removeItem("sl_currentUser");
  },

  // Student Actions
  getStudentDashboard: function (studentId) {
    this.init();
    const resources = JSON.parse(localStorage.getItem("sl_resources"));
    const questions = JSON.parse(localStorage.getItem("sl_questions"));
    const notificationList = JSON.parse(
      localStorage.getItem("sl_notifications")
    );
    const allCourses = JSON.parse(localStorage.getItem("sl_courses"));

    const myRes = resources.filter((r) => r.uploaderId === studentId);
    const myQs = questions.filter((q) => q.studentId === studentId);
    const myNotifs = notificationList.filter(
      (n) => n.userId === studentId && !n.read
    ).length; // simple count

    // Add answerCount to qs
    const formattedQs = myQs.map((q) => ({
      ...q,
      answerCount: q.answers ? q.answers.length : 0,
    }));

    return {
      notifications: myNotifs,
      myResources: myRes,
      myQuestions: formattedQs,
      allCourses: allCourses,
    };
  },

  searchResources: function (keyword) {
    this.init();
    const resources = JSON.parse(localStorage.getItem("sl_resources"));
    if (!keyword) return resources.filter((r) => r.status === "APPROVED");
    const k = keyword.toLowerCase();
    return resources.filter(
      (r) =>
        r.status === "APPROVED" &&
        r.visibility === "PUBLIC" &&
        (r.title.toLowerCase().includes(k) ||
          r.courseName?.toLowerCase().includes(k) ||
          r.uploaderName.toLowerCase().includes(k))
    );
  },

  addQuestion: function (q) {
    this.init();
    const questions = JSON.parse(localStorage.getItem("sl_questions"));
    const courses = JSON.parse(localStorage.getItem("sl_courses"));
    const course = courses.find((c) => c.id == q.courseId);

    q.id = Date.now();
    q.courseName = course ? course.name : "未知";
    q.answers = [];
    questions.push(q);
    localStorage.setItem("sl_questions", JSON.stringify(questions));
    return true;
  },

  uploadResource: function (r) {
    this.init();
    const resources = JSON.parse(localStorage.getItem("sl_resources"));
    const courses = JSON.parse(localStorage.getItem("sl_courses"));
    const course = courses.find((c) => c.id == r.courseId);

    r.id = Date.now();
    r.courseName = course ? course.name : "未知";
    r.downloadCount = 0;
    r.status = "APPROVED"; // Auto approve for demo
    r.type = "FILE";
    r.visibility = r.visibility || "PUBLIC";

    resources.push(r);
    localStorage.setItem("sl_resources", JSON.stringify(resources));
    return true;
  },

  downloadResource: function (id) {
    const resources = JSON.parse(localStorage.getItem("sl_resources"));
    const r = resources.find((res) => res.id == id);
    if (r) {
      r.downloadCount++;
      localStorage.setItem("sl_resources", JSON.stringify(resources));
    }
  },

  // Teacher Actions
  getTeacherDashboard: function (teacherId) {
    this.init();
    const courses = JSON.parse(localStorage.getItem("sl_courses"));
    const questions = JSON.parse(localStorage.getItem("sl_questions"));

    // Get courses taught by teacher (mock: all courses for id=2, or filtered)
    // For demo, if teacherId matches stored courses
    const myCourses = courses; // Simplify: Teacher sees all courses or filter by id if we saved it properly.
    // Let's filter questions that have NO answers yet
    const unanswered = questions.filter(
      (q) => !q.answers || q.answers.length === 0
    );

    return {
      courses: myCourses,
      unansweredQuestions: unanswered,
    };
  },

  addAnswer: function (qId, teacherId, content, teacherName) {
    const questions = JSON.parse(localStorage.getItem("sl_questions"));
    const q = questions.find((question) => question.id == qId);
    if (q) {
      if (!q.answers) q.answers = [];
      q.answers.push({
        id: Date.now(),
        teacherId: teacherId,
        teacherName: teacherName,
        content: content,
        createdAt: new Date().toISOString(),
      });
      localStorage.setItem("sl_questions", JSON.stringify(questions));

      // Notify Student
      const notifs = JSON.parse(localStorage.getItem("sl_notifications"));
      notifs.push({
        userId: q.studentId,
        message: `${teacherName} 回答了 "${q.title}"`,
        read: false,
        link: "student.html#qa",
      });
      localStorage.setItem("sl_notifications", JSON.stringify(notifs));
      return true;
    }
    return false;
  },

  // Admin Actions
  getAdminDashboard: function () {
    this.init();
    return {
      courses: JSON.parse(localStorage.getItem("sl_courses")),
      teachers: JSON.parse(localStorage.getItem("sl_users")).filter(
        (u) => u.role === "TEACHER"
      ),
      resources: JSON.parse(localStorage.getItem("sl_resources")),
      questions: JSON.parse(localStorage.getItem("sl_questions")),
    };
  },

  addCourse: function (course) {
    const courses = JSON.parse(localStorage.getItem("sl_courses"));
    const users = JSON.parse(localStorage.getItem("sl_users"));
    const teacher = users.find((u) => u.id == course.teacherId);
    course.id = Date.now();
    course.teacherName = teacher ? teacher.fullName : "-";
    courses.push(course);
    localStorage.setItem("sl_courses", JSON.stringify(courses));
  },

  deleteItem: function (type, id) {
    if (type === "course") {
      let items = JSON.parse(localStorage.getItem("sl_courses"));
      localStorage.setItem(
        "sl_courses",
        JSON.stringify(items.filter((i) => i.id != id))
      );
    } else if (type === "resource") {
      let items = JSON.parse(localStorage.getItem("sl_resources"));
      localStorage.setItem(
        "sl_resources",
        JSON.stringify(items.filter((i) => i.id != id))
      );
    } else if (type === "question") {
      let items = JSON.parse(localStorage.getItem("sl_questions"));
      localStorage.setItem(
        "sl_questions",
        JSON.stringify(items.filter((i) => i.id != id))
      );
    }
  },
  updateUser: function (user) {
    this.init();
    const users = JSON.parse(localStorage.getItem("sl_users"));
    const idx = users.findIndex((u) => u.id === user.id);
    if (idx !== -1) {
      users[idx] = user;
      localStorage.setItem("sl_users", JSON.stringify(users));
      sessionStorage.setItem("sl_currentUser", JSON.stringify(user));
      return true;
    }
    return false;
  },

  deleteQuestion: function (id) {
    let questions = JSON.parse(localStorage.getItem("sl_questions"));
    questions = questions.filter((q) => q.id !== id);
    localStorage.setItem("sl_questions", JSON.stringify(questions));
  },

  deleteAnswer: function (qId, answerId) {
    let questions = JSON.parse(localStorage.getItem("sl_questions"));
    const q = questions.find((x) => x.id === qId);
    if (q && q.answers) {
      q.answers = q.answers.filter((a) => a.id !== answerId);
      localStorage.setItem("sl_questions", JSON.stringify(questions));
    }
  },
};

MockAPI.init();
