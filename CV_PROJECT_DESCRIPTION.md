# CV Project Description / Bản mô tả dự án viết CV

Tài liệu này cung cấp mô tả dự án **Educatrix** chuẩn chỉnh, bám sát toàn bộ mã nguồn thực tế của project `myf3school_backend` (thay vì bản LMS cũ). Bạn có thể copy và paste trực tiếp nội dung này vào CV xin việc của mình (tiếng Anh hoặc tiếng Việt).

---

## 📑 1. Tiếng Anh (English Version - Recommended)

### **EDUCATRIX – Enterprise School Management & Student Portal System**
**Role:** Backend Developer  
**Technologies:** Java 17, Spring Boot 3.2.5, Spring Security, JWT (JJwt 0.12.6), Spring Data JPA, Hibernate, MySQL 8.0, Spring WebSocket, Server-Sent Events (SSE), Twilio SDK (SMS & Voice Calls), JavaMail (SMTP), Thymeleaf, Spring Boot Actuator, JUnit 5, Maven  

**Description:**  
An enterprise-grade, highly scalable school management and student portal system. The platform automates administrative workflows, including multi-role portals (Admin, Teacher, Student, Parent), component-based grade books, automated grade correction workflows, real-time messaging, club/extracurricular activities, and automated tuition invoicing.

**Key Contributions & Achievements:**  
*   **Architecture & Design:** Designed and developed the backend services from scratch using **Clean Architecture** and **Domain-Driven Design (DDD)** principles, separating core business logic from database layers to ensure high maintainability and testability.
*   **Production-Ready Authentication:** Implemented a secure authentication system using **Spring Security** and **JWT** with dynamic claim extraction (roles, student, and class IDs mapped in the token payload to eliminate redundant database queries).
*   **Advanced Security & MFA:** Mitigated session hijacking by engineering a **Refresh Token Rotation (RTR)** mechanism that tracks client IP and User-Agent, and integrated **Twilio SDK** for Multi-Factor Authentication (MFA) via SMS OTP and interactive voice calls.
*   **Real-Time Communications:** Developed a bi-directional messaging platform using **Spring WebSocket** and designed a lightweight, unidirectional notification engine using **Server-Sent Events (SSE)** to push grading and attendance updates instantly, reducing server bandwidth compared to HTTP polling.
*   **Complex Data Modeling:** Engineered MySQL relational schemas and JPA mappings using **Hibernate** and **Spring Data JPA**, supporting complex relationships such as parent-student associations (many-to-many), student conduct transactions, and recurring invoices.
*   **Performance Optimization:** Improved system responsiveness by decoupling long-running operations—such as PDF invoice generation and payment reminder mailings—using Spring **`@Async`** and JavaMail.
*   **Testing & Monitoring:** Achieved high reliability by writing integration tests with **JUnit 5** and **Spring Security Test** to validate security contexts, and integrated **Spring Boot Actuator** to expose system health and performance metrics.

---

## 📑 2. Tiếng Việt (Vietnamese Version)

### **EDUCATRIX – Hệ Thống Quản Lý Trường Học & Cổng Thông Tin Học Sinh**
**Vai trò:** Lập trình viên Backend (Backend Developer)  
**Công nghệ sử dụng:** Java 17, Spring Boot 3.2.5, Spring Security, JWT (JJwt 0.12.6), Spring Data JPA, Hibernate, MySQL 8.0, Spring WebSocket, Server-Sent Events (SSE), Twilio SDK (SMS & Voice), JavaMail (SMTP), Thymeleaf, Spring Boot Actuator, JUnit 5, Maven  

**Mô tả dự án:**  
Hệ thống quản lý trường học và cổng thông tin học sinh cấp doanh nghiệp (Enterprise), cho phép tối ưu hóa và tự động hóa quy trình quản trị trường học bao gồm: Phân quyền đa vai trò (Admin, Giáo viên, Học sinh, Phụ huynh), quản lý điểm số phân thành phần, quy trình phúc khảo tự động, nhắn tin thời gian thực, quản lý điểm rèn luyện & câu lạc bộ, và tự động hóa hóa đơn học phí.

**Đóng góp chính & Kết quả đạt được:**  
*   **Kiến trúc hệ thống:** Thiết kế và phát triển mã nguồn theo mô hình **Clean Architecture** và **Domain-Driven Design (DDD)**, giúp tách biệt hoàn toàn nghiệp vụ cốt lõi (Business Logic) khỏi database và thư viện ngoài, tối ưu hóa khả năng bảo trì và viết Unit Test.
*   **Bảo mật & Phân quyền:** Xây dựng hệ thống bảo mật bằng **Spring Security & JWT**. Thiết kế cơ chế trích xuất JWT dynamic claims (lưu trực tiếp role, classId, studentId trong token) giúp giảm tải số lượng truy vấn database khi xác thực tài nguyên.
*   **Cơ chế bảo mật nâng cao:** Phát triển tính năng xoay vòng Refresh Token (**Refresh Token Rotation - RTR**) theo dõi IP/User-Agent giúp chống tấn công chiếm quyền phiên làm việc, tích hợp **Twilio SDK** để xác thực 2 lớp (MFA) qua SMS OTP và cuộc gọi thoại tự động cho các tác vụ nhạy cảm.
*   **Tương tác thời gian thực:** Triển khai tính năng chat trực tuyến bằng **Spring WebSocket** và hệ thống đẩy thông báo tức thời (điểm số, điểm danh) bằng **Server-Sent Events (SSE)**, tối ưu băng thông máy chủ so với cơ chế HTTP Polling truyền thống.
*   **Thiết kế cơ sở dữ liệu:** Thiết kế và tối ưu hóa lược đồ MySQL sử dụng **Hibernate** và **Spring Data JPA**, giải quyết bài toán nghiệp vụ phức tạp như quan hệ nhiều-nhiều giữa phụ huynh - học sinh, hệ thống tích lũy điểm rèn luyện, và quản lý hóa đơn học phí theo kỳ học.
*   **Tối ưu hóa hiệu năng bất đồng bộ:** Tăng tốc độ phản hồi của ứng dụng bằng cách xử lý bất đồng bộ (**`@Async`**) các tác vụ nặng như tạo hóa đơn hàng loạt và tự động gửi email nhắc nợ học phí qua SMTP JavaMail.
*   **Kiểm thử & Giám sát:** Viết kiểm thử tự động với **JUnit 5** và **Spring Security Test** để kiểm thử bảo mật và logic nghiệp vụ, đồng thời tích hợp **Spring Boot Actuator** để giám sát tài nguyên và sức khỏe của hệ thống trực tiếp.

