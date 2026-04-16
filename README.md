# Robot Pathfinding Game (AI)

An educational AI game that simulates the robot pathfinding problem,
allowing users to visualize and compare classical search algorithms
in Artificial Intelligence.

## Project Overview

This project demonstrates how a robot navigates through a maze-like
environment to reach a target position.  
The main goal is to help students understand and compare different
AI search algorithms through direct visualization.

The robot moves in a 2D grid environment containing:
- Walls
- Bombs (reduce robot lives)
- Start position
- Goal position

The robot wins if it reaches the goal with at least one remaining life.

## AI Algorithms

The following classical AI search algorithms are implemented and compared:
- Breadth-First Search (BFS)
- Depth-First Search (DFS)
- A* Search

## Tech Stack

- Java 17
- JavaFX
- Maven

## Features

- Grid-based maze environment
- Step-by-step visualization of search algorithms
- Robot animation during pathfinding
- Comparison of algorithms based on:
  - Path length
  - Number of explored nodes
  - Remaining lives

## Project Status

- Phase 1: Project initialization and architecture setup
- Phase 2: Power-up system and refined UI (Updated 2026)

---

## 🚀 Hướng Dẫn Chạy Dự Án (Terminal Guide)

Nếu bạn vừa tải dự án về từ GitHub hoặc Clone repo, hãy làm theo các bước sau:

### 1. Kiểm tra môi trường (Prerequisites)
Trước khi chạy, máy tính của bạn cần cài đặt:
- **Java JDK 17** hoặc cao hơn.
- **Apache Maven**.

Kiểm tra bằng lệnh sau trong Terminal (CMD/PowerShell):
```bash
java -version
mvn -version
```

*Nếu bạn chưa cài gì:* 
- Tải JDK 17 tại: [Adoptium (Eclipse Temurin)](https://adoptium.net/)
- Tải Maven tại: [Maven Download](https://maven.apache.org/download.cgi) và cấu hình biến môi trường `PATH`.

### 2. Clone dự án (Nếu chưa tải)
```bash
git clone https://github.com/Duck-SFIT-CNTT2-K64/ai-game-platform.git
cd ai-game-platform
```

### 3. Build & Chạy Game
Sử dụng Maven để tải thư viện và khởi động JavaFX:

```bash
mvn clean javafx:run
```

- Lần đầu chạy sẽ mất khoảng 1-2 phút để Maven tải các dependencies (JavaFX, v.v.).
- Nếu gặp lỗi `JAVA_HOME`, hãy đảm bảo biến môi trường này đang trỏ đúng vào thư mục cài đặt JDK 17 của bạn.

---

## 🎁 Hệ Thống Power-Up
Dự án tích hợp hệ thống vật phẩm thông minh, chi tiết xem tại: [POWERUP_IMPLEMENTATION_GUIDE.md](file:///d:/University/TTNT/ai-game-platform/POWERUP_IMPLEMENTATION_GUIDE.md)
