# Enterprise BBS

企业 BBS 系统课程设计项目。

## 技术栈

后端：

- Java 17
- Spring Boot
- Spring MVC
- MyBatis
- PostgreSQL
- Maven

前端：

- Vue 3
- Vite
- Vue Router
- Axios
- Element Plus

## 项目结构

```text
enterprise-bbs/
├── backend/
├── frontend/
├── docs/
└── sql/
```

## 后端启动

macOS：

```bash
cd backend
./mvnw spring-boot:run
```

Windows：

```bash
cd backend
mvnw.cmd spring-boot:run
```

## 前端启动

```bash
cd frontend
npm install
npm run dev
```

## 数据库

数据库默认名称为 bbs_db。建表脚本位于：

```text
sql/schema.sql
```

测试数据位于：

```text
sql/data.sql
```