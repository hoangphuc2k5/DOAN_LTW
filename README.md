# 📚 EDMOET - Nền Tảng Hỏi Đáp Học Tập

<div align="center">

![Java](https://img.shields.io/badge/Java-17-orange?logo=openjdk)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.1.5-brightgreen?logo=spring)
![SQL Server](https://img.shields.io/badge/SQL%20Server-Microsoft-blue?logo=microsoft-sql-server)
![License](https://img.shields.io/badge/License-MIT-yellow)

**EDMOET** là một nền tảng hỏi đáp trực tuyến (Q&A Platform) được xây dựng với Spring Boot, giúp người dùng đặt câu hỏi, trả lời và chia sẻ kiến thức trong môi trường học tập.

[Demo](#) • [Tài Liệu](#tài-liệu) • [Báo Lỗi](#báo-lỗi)

</div>

---

## 📑 Mục Lục

- [Giới Thiệu](#-giới-thiệu)
- [Tính Năng](#-tính-năng)
- [Công Nghệ Sử Dụng](#-công-nghệ-sử-dụng)
- [Yêu Cầu Hệ Thống](#-yêu-cầu-hệ-thống)
- [Cài Đặt](#-cài-đặt)
- [Cấu Hình](#-cấu-hình)
- [Chạy Ứng Dụng](#-chạy-ứng-dụng)
- [Cấu Trúc Dự Án](#-cấu-trúc-dự-án)
- [API Documentation](#-api-documentation)
- [Phân Quyền](#-phân-quyền)
- [Triển Khai](#-triển-khai)
- [Đóng Góp](#-đóng-góp)
- [License](#-license)

---

## 🎯 Giới Thiệu

EDMOET là một hệ thống hỏi đáp trực tuyến được thiết kế đặc biệt cho cộng đồng học tập. Nền tảng này cho phép người dùng:

- ✅ Đặt câu hỏi và nhận câu trả lời từ cộng đồng
- ✅ Tích lũy điểm danh tiếng thông qua các hoạt động tích cực
- ✅ Tương tác với chatbot thông minh được tích hợp OpenAI
- ✅ Quản lý nội dung với hệ thống kiểm duyệt và báo cáo
- ✅ Giao tiếp với người dùng khác qua tin nhắn

---

## ✨ Tính Năng

### 👥 Cho Người Dùng (User)
- 🔐 **Xác thực & Bảo mật**: Đăng ký, đăng nhập, quên mật khẩu với OTP
- ❓ **Quản lý Câu Hỏi**: Đặt câu hỏi, chỉnh sửa, xóa, ghim câu hỏi
- 💬 **Hệ Thống Trả Lời**: Trả lời câu hỏi, chấp nhận câu trả lời tốt nhất
- 🏷️ **Tags**: Phân loại câu hỏi bằng tags, tìm kiếm theo tags
- ⭐ **Hệ Thống Điểm**: Tích lũy điểm danh tiếng (reputation), điểm, level
- 🔍 **Tìm Kiếm**: Tìm kiếm câu hỏi theo từ khóa, sắp xếp theo tiêu chí
- 💬 **Tin Nhắn**: Gửi và nhận tin nhắn giữa các người dùng
- 🔔 **Thông Báo**: Nhận thông báo về các hoạt động liên quan
- 🤖 **Chatbot**: Tương tác với chatbot AI được tích hợp OpenAI
- 📊 **Hồ Sơ**: Xem và chỉnh sửa hồ sơ cá nhân
- 📤 **Báo Cáo**: Báo cáo nội dung vi phạm
- 📸 **Upload Hình Ảnh**: Upload và quản lý hình ảnh qua AWS S3

### 👨‍💼 Cho Quản Lý (Manager)
- ✅ **Kiểm Duyệt**: Duyệt/không duyệt câu hỏi và câu trả lời
- 📋 **Quản Lý Câu Hỏi**: Xem danh sách, chi tiết, chỉnh sửa câu hỏi
- 📊 **Quản Lý Báo Cáo**: Xử lý các báo cáo vi phạm
- 🔔 **Gửi Thông Báo**: Gửi thông báo cho người dùng

### 👨‍💻 Cho Quản Trị Viên (Admin)
- 👥 **Quản Lý Người Dùng**: Xem, chỉnh sửa, khóa/mở khóa tài khoản
- ❓ **Quản Lý Câu Hỏi**: Quản lý toàn bộ câu hỏi trên hệ thống
- 💬 **Quản Lý Câu Trả Lời**: Quản lý tất cả câu trả lời
- 🏷️ **Quản Lý Tags**: Tạo, chỉnh sửa, xóa tags
- 📊 **Thống Kê**: Xem thống kê tổng quan về hệ thống
- 📋 **Nhật Ký Hoạt Động**: Theo dõi các hoạt động của người dùng
- 📤 **Quản Lý Báo Cáo**: Xử lý báo cáo vi phạm
- 🔔 **Gửi Thông Báo**: Gửi thông báo hệ thống

---

## 🛠️ Công Nghệ Sử Dụng

### Backend
- **Java 17**: Ngôn ngữ lập trình
- **Spring Boot 3.1.5**: Framework chính
- **Spring Security**: Xác thực và phân quyền
- **Spring Data JPA**: ORM và truy vấn dữ liệu
- **JWT (JSON Web Token)**: Xác thực token
- **Thymeleaf**: Template engine
- **Spring WebSocket**: Real-time communication
- **Spring AOP**: Aspect-oriented programming cho logging

### Database
- **Microsoft SQL Server**: Database chính (deploy trên AWS RDS)

### Frontend
- **Thymeleaf**: Server-side rendering
- **Bootstrap 5**: Framework CSS
- **JavaScript**: Client-side scripting
- **WebSocket (STOMP)**: Real-time messaging

### Dịch Vụ Bên Ngoài
- **AWS S3**: Lưu trữ hình ảnh và files
- **OpenAI API**: Tích hợp chatbot AI (GPT-4o-mini)
- **Gmail SMTP**: Gửi email (OTP, thông báo)

### Công Cụ & Thư Viện
- **Lombok**: Giảm boilerplate code
- **Maven**: Quản lý dependencies
- **Thymeleaf Layout Dialect**: Layout management

---

## 💻 Yêu Cầu Hệ Thống

### Bắt Buộc
- **Java Development Kit (JDK)**: Phiên bản 17 trở lên
- **Apache Maven**: Phiên bản 3.6+ 
- **Microsoft SQL Server**: Phiên bản 2019 trở lên (hoặc AWS RDS)
- **Apache Tomcat**: Phiên bản 10+ (nếu deploy WAR file)

### Tùy Chọn (Để Phát Triển)
- **IDE**: IntelliJ IDEA, Eclipse, hoặc VS Code
- **Git**: Để quản lý phiên bản
- **Postman**: Để test API (nếu có)

---

## 📦 Cài Đặt

### 1. Clone Repository

```bash
git clone https://github.com/your-username/EDMOET.git
cd EDMOET
```

### 2. Cài Đặt Dependencies

```bash
mvn clean install
```

### 3. Cấu Hình Database

Tạo database trong SQL Server:

```sql
CREATE DATABASE LTW2;
```

### 4. Cấu Hình Application Properties

Cập nhật file `src/main/resources/application.properties` với thông tin của bạn (xem phần [Cấu Hình](#-cấu-hình)).

---

## ⚙️ Cấu Hình

### Database Configuration

```properties
spring.datasource.url=jdbc:sqlserver://localhost:1433;databaseName=LTW2;encrypt=true;trustServerCertificate=true
spring.datasource.username=your_username
spring.datasource.password=your_password
```

### JWT Configuration

```properties
jwt.secret=your_secret_key_here
jwt.expiration=86400000  # 24 hours in milliseconds
```

### AWS S3 Configuration

```properties
cloud.aws.credentials.access-key=your_access_key
cloud.aws.credentials.secret-key=your_secret_key
cloud.aws.region.static=ap-southeast-1
cloud.aws.s3.bucket=your_bucket_name
cloud.aws.s3.base-folder=uploads
```

### Email Configuration (Gmail)

```properties
spring.mail.host=smtp.gmail.com
spring.mail.port=587
spring.mail.username=your_email@gmail.com
spring.mail.password=your_app_password  # Sử dụng App Password, không phải mật khẩu Gmail
```

### OpenAI Chatbot Configuration

```properties
chatbot.openai.enabled=true
chatbot.openai.api-key=your_openai_api_key
chatbot.openai.model=gpt-4o-mini
chatbot.openai.max-tokens=500
chatbot.openai.temperature=0.7
```

⚠️ **Lưu Ý Bảo Mật**: 
- Không commit file `application.properties` chứa thông tin nhạy cảm vào Git
- Sử dụng biến môi trường hoặc Spring Cloud Config cho production
- Tạo file `application-local.properties` cho môi trường local

---

## 🚀 Chạy Ứng Dụng

### Chạy với Maven (Development)

```bash
mvn spring-boot:run
```

Ứng dụng sẽ chạy tại: `http://localhost:8080`

### Build WAR File (Production)

```bash
mvn clean package
```

File WAR sẽ được tạo tại: `target/EDMOET-1.0.0.war`

### Deploy WAR File lên Tomcat

1. Copy file `EDMOET-1.0.0.war` vào thư mục `webapps` của Tomcat
2. Khởi động Tomcat
3. Truy cập: `http://localhost:8080/EDMOET-1.0.0`

---

## 📁 Cấu Trúc Dự Án

```
EDMOET/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/edumoet/
│   │   │       ├── aspect/          # AOP cho logging
│   │   │       ├── config/          # Cấu hình Spring
│   │   │       ├── controller/      # Controllers
│   │   │       │   ├── admin/       # Admin controllers
│   │   │       │   ├── manager/     # Manager controllers
│   │   │       │   ├── user/        # User controllers
│   │   │       │   └── common/      # Common controllers
│   │   │       ├── entity/          # JPA Entities
│   │   │       ├── repository/      # JPA Repositories
│   │   │       ├── security/       # Security configuration
│   │   │       └── service/         # Business logic
│   │   │           └── common/      # Common services
│   │   └── resources/
│   │       ├── static/              # Static resources
│   │       │   ├── css/            # CSS files
│   │       │   └── js/             # JavaScript files
│   │       ├── templates/          # Thymeleaf templates
│   │       │   ├── admin/         # Admin pages
│   │       │   ├── manager/       # Manager pages
│   │       │   ├── user/          # User pages
│   │       │   └── layout/        # Layout templates
│   │       └── application.properties
│   └── test/                       # Test files
├── target/                          # Build output
├── pom.xml                          # Maven configuration
└── README.md                        # This file
```

---

## 📖 API Documentation

### Authentication Endpoints

| Method | Endpoint | Mô Tả |
|--------|----------|-------|
| POST | `/auth/register` | Đăng ký tài khoản mới |
| POST | `/auth/login` | Đăng nhập |
| POST | `/auth/logout` | Đăng xuất |
| POST | `/auth/forgot-password` | Quên mật khẩu |
| POST | `/auth/reset-password` | Đặt lại mật khẩu |

### Question Endpoints

| Method | Endpoint | Mô Tả |
|--------|----------|-------|
| GET | `/questions` | Danh sách tất cả câu hỏi |
| GET | `/questions/{id}` | Chi tiết câu hỏi |
| POST | `/questions/ask` | Đặt câu hỏi mới |
| GET | `/questions/edit/{id}` | Form chỉnh sửa câu hỏi |
| POST | `/questions/update/{id}` | Cập nhật câu hỏi |
| DELETE | `/questions/delete/{id}` | Xóa câu hỏi |
| POST | `/questions/{id}/vote` | Vote câu hỏi |

### Answer Endpoints

| Method | Endpoint | Mô Tả |
|--------|----------|-------|
| POST | `/questions/{id}/answers` | Trả lời câu hỏi |
| POST | `/answers/{id}/accept` | Chấp nhận câu trả lời |
| POST | `/answers/{id}/vote` | Vote câu trả lời |
| DELETE | `/answers/{id}` | Xóa câu trả lời |

### User Endpoints

| Method | Endpoint | Mô Tả |
|--------|----------|-------|
| GET | `/users/{id}` | Xem hồ sơ người dùng |
| GET | `/profile` | Hồ sơ của bạn |
| POST | `/profile/edit` | Chỉnh sửa hồ sơ |
| GET | `/profile/my-questions` | Câu hỏi của bạn |

---

## 🔐 Phân Quyền

Hệ thống có 3 vai trò chính:

### 1. USER (Người Dùng)
- Đặt và trả lời câu hỏi
- Vote câu hỏi/câu trả lời
- Quản lý hồ sơ cá nhân
- Gửi tin nhắn
- Báo cáo vi phạm

### 2. MANAGER (Quản Lý)
- Tất cả quyền của USER
- Kiểm duyệt câu hỏi và câu trả lời
- Xử lý báo cáo vi phạm
- Gửi thông báo

### 3. ADMIN (Quản Trị Viên)
- Tất cả quyền của MANAGER
- Quản lý người dùng
- Quản lý tags
- Xem thống kê hệ thống
- Quản lý hoạt động

---

## 🌐 Triển Khai

### Triển Khai lên AWS EC2 với Tomcat

1. **Cài đặt Java 17 và Tomcat trên EC2**
2. **Cấu hình AWS RDS cho SQL Server**
3. **Cấu hình AWS S3 bucket**
4. **Build WAR file**: `mvn clean package`
5. **Deploy WAR file lên Tomcat**
6. **Cấu hình reverse proxy với Nginx** (tùy chọn)

### Biến Môi Trường Production

Khuyến nghị sử dụng biến môi trường hoặc Spring Profiles:

```bash
export SPRING_PROFILES_ACTIVE=production
export DB_URL=jdbc:sqlserver://...
export DB_USERNAME=...
export DB_PASSWORD=...
```

---

## 🤝 Đóng Góp

Chúng tôi hoan nghênh mọi đóng góp! Vui lòng làm theo các bước sau:

1. **Fork** repository
2. **Tạo branch** mới (`git checkout -b feature/AmazingFeature`)
3. **Commit** thay đổi (`git commit -m 'Add some AmazingFeature'`)
4. **Push** lên branch (`git push origin feature/AmazingFeature`)
5. **Mở Pull Request**

### Quy Tắc Đóng Góp

- Tuân thủ code style của dự án
- Viết commit message rõ ràng
- Thêm tests cho tính năng mới
- Cập nhật tài liệu nếu cần

---

## 📝 License

Dự án này được phân phối dưới giấy phép MIT. Xem file `LICENSE` để biết thêm chi tiết.

---

## 👨‍💻 Tác Giả

**Nhóm Phát Triển EDMOET**

- GitHub: [@your-username](https://github.com/your-username)
- Email: contact@edumoet.com

---

## 🙏 Lời Cảm Ơn

- Cảm ơn Spring Boot community
- Cảm ơn tất cả contributors
- Cảm ơn những người dùng đã đóng góp phản hồi

---

## 📞 Liên Hệ & Hỗ Trợ

- **Issues**: [GitHub Issues](https://github.com/your-username/EDMOET/issues)
- **Email**: support@edumoet.com
- **Documentation**: [Wiki](https://github.com/your-username/EDMOET/wiki)

---

<div align="center">

**⭐ Nếu dự án này hữu ích, hãy cho chúng tôi một star! ⭐**

Made with ❤️ by EDMOET Team

</div>
