# Hệ Thống Power-Up Game - Hướng Dẫn Triển Khai

## 📋 Tổng Quan Tính Năng

Dự án đã được nâng cấp với hệ thống power-up hoàn chỉnh dành cho chế độ Player. Các cải tiến chính bao gồm:

### 1. **Lọc Items/Bombs Theo Chế Độ Game**
- **Bot Mode**: Loại bỏ hoàn toàn items và bombs → hiển thị chỉ tường, ô trống, start, goal
- **Player Mode**: Giữ nguyên items và bombs để tương tác người chơi

**Cách thức hoạt động:**
- File `MazeGenerator.java` đã được cập nhật với enum `GameMode` (PLAYER/BOT)
- Phương thức `generate(String difficulty, GameMode mode)` kiểm soát quá trình sinh maze

```java
// Cách sử dụng
Maze playerMaze = MazeGenerator.generate("EASY", MazeGenerator.GameMode.PLAYER);
Maze botMaze = MazeGenerator.generate("EASY", MazeGenerator.GameMode.BOT);
```

---

## 🎁 Hệ Thống Power-Up

### Danh Sách 20 Power-Up

| # | Tên | Mô Tả | Hiệu Ứng | Độ Khó |
|---|---|---|---|---|
| 1 | Speed Boost | Di chuyển 2 bước 1 lần | Nhấn 1 phím nhảy 2 ô + Bóng mờ | Easy |
| 2 | Shield | Bảo vệ trong 10 giây | Bất tử 10s + Đếm ngược chung | Easy |
| 3 | Reveal Path | Hiển thị đường đi | Thấy đường tới goal | Medium |
| 4 | Extra Life | Thêm 1 mạng | +1 life | Easy |
| 5 | Bomb Detector | Phát hiện bomb gần | Hiện vị trí bomb | Medium |
| 6 | Teleport | Dịch chuyển tức thời | Teleport ngẫu nhiên | Medium |
| 7 | Remove Wall | Xóa 1 bức tường | Phá tường 1 lần | Medium |
| 8 | Double Score | Nhân đôi điểm số hiện tại | x2 score ngay lập tức | Easy |
| 9 | Freeze Time | Đóng băng bom | Không đi qua được bom | Medium |
| 10 | AI Assist | Trợ giúp AI | Auto solve đoạn | Medium |
| 11 | Shortest Path Mode | Ưu tiên đường ngắn | BFS/A* priority | Medium |
| 12 | Speed Slow | Chậm nhưng an toàn | Robot chậm | Easy |
| 13 | Lucky Find | Hộp quà may mắn | Kích hoạt hiệu ứng ngẫu nhiên | Medium |
| 14 | Another Options | Thay đổi lựa chọn | Chọn lại bộ 3 items | Hard |
| 15 | Time Bonus | Thêm thời gian (+15s) | Thêm 15 giây vào đồng hồ | Easy |
| 16 | Sonar Radar | Siêu Radar (Magnetic) | Hiện đường đi + Hút item + x2 Score | Hard |

---

## 🎮 Luồng Người Chơi

### Bước 1: Chế Độ Player
```
Menu → Chọn Difficulty → Chế độ PLAYER
```

### Bước 2: Nhặt Vật Phẩm
Khi di chuyển tới ô ITEM:
```
Player → Item Cell = Trigger Modal
```

### Bước 3: Chọn Power-Up
Modal hiện 3 tấm thẻ ngẫu nhiên:
- Card 1: Tên, mô tả, hiệu ứng, độ khó
- Card 2: Tên, mô tả, hiệu ứng, độ khó
- Card 3: Tên, mô tả, hiệu ứng, độ khó

Nhấn **SELECT** trên card muốn chọn

### Bước 4: Lưu Trữ Power-Up
Vật phẩm được thêm vào Inventory Panel dưới canvas:
- Hiển thị: Icon + Tên + Trạng thái (READY/ACTIVE)
- Có thể click để kích hoạt

### Bước 5: Sử Dụng Power-Up
- Nhấp vào item trong inventory
- Power-up kích hoạt (READY → ACTIVE)
- Hiệu ứng được áp dụng
- Sau thời gian hết hiệu lực → quay lại READY

---

## 📁 Cấu Trúc File Mới/Cập Nhật

### File Mới Tạo

```
core/
├── PowerUp.java                    // Enum 20 power-ups
├── CollectedPowerUp.java           // Lớp theo dõi power-up đã nhặt
└── PowerUpExecutor.java            // Xử lý hiệu ứng power-up

ui/components/
├── ItemCardSelectionModal.java     // Modal chọn power-up (3 cards)
└── InventoryPanel.java             // Panel hiển thị inventory

ui/
└── PlayGamePage.java               // CẬP NHẬT: Tích hợp hệ thống power-up
```

### File Cập Nhật

**MazeGenerator.java**
- Thêm enum `GameMode`
- Overload `generate()` với tham số mode
- Lọc items/bombs nếu mode = BOT

**PlayGamePage.java**
- Import thêm `PowerUp`, `CollectedPowerUp`, `InventoryPanel`, `ItemCardSelectionModal`
- Thêm `GameMode` parameter khi gọi `MazeGenerator.generate()`
- Thêm UI component Inventory Panel
- Cập nhật `handlePlayerMove()` để trigger modal khi nhặt item
- Kết nối inventory callback

---

## 🔧 API Sử Dụng

### PowerUp Enum
```java
// Lấy danh sách tất cả power-ups
PowerUp[] allPowerUps = PowerUp.values();

// Lấy power-up ngẫu nhiên theo độ khó
PowerUp easyPowerUp = PowerUp.getRandomByDifficulty(PowerUp.Difficulty.EASY);

// Truy cập thông tin power-up
String name = powerUp.getDisplayName();
String description = powerUp.getEnglishDescription();
String effect = powerUp.getVietnameseDescription();
PowerUp.Difficulty difficulty = powerUp.getDifficulty();
```

### CollectedPowerUp
```java
// Tạo power-up đã nhặt
CollectedPowerUp collected = new CollectedPowerUp(powerUp);

// Kích hoạt
collected.activate();

// Kiểm tra trạng thái
if (collected.isActive()) { /* Đang hoạt động */ }
if (collected.isExpired()) { /* Hết hiệu lực */ }

// Lấy thời gian còn lại
long remaining = collected.getRemainingTime(); // milliseconds
```

### InventoryPanel
```java
// Tạo panel
InventoryPanel inventory = new InventoryPanel();

// Thêm power-up
inventory.addCollectedPowerUp(powerUp);

// Kích hoạt theo index
inventory.activatePowerUp(0);

// Xóa power-up
inventory.removePowerUp(0);

// Lấy inventory
List<CollectedPowerUp> items = inventory.getInventory();
int size = inventory.getInventorySize();

// Callback khi kích hoạt
inventory.setOnActivateCallback(collected -> {
    PowerUpExecutor.executePowerUp(collected, playerPos, maze, gameState);
});

// Lấy UI component
FlowPane container = inventory.getContainer();
```

### ItemCardSelectionModal
```java
// Tạo modal
ItemCardSelectionModal modal = new ItemCardSelectionModal(stage, previousScene);

// Hiển thị modal
modal.show(selectedPowerUp -> {
    if (selectedPowerUp != null) {
        inventory.addCollectedPowerUp(selectedPowerUp);
    }
});
```

---

## 🎯 Hướng Dẫn Sử Dụng (Người Chơi)

### Chế Độ Player
1. **Vào trò chơi**: Menu → Difficulty → Chọn PLAYER
2. **Di chuyển**: Dùng phím mũi tên ↑↓←→
3. **Nhặt Items**: Đi tới ô có biểu tượng "D" (xanh lá)
4. **Chọn Power-Up**: Modal hiện 3 lựa chọn
   - Đọc mô tả tiếng Anh
   - Xem "Độ khó" (Easy/Medium/Hard)
   - Xem hiệu ứng tiếng Việt
   - Nhấn **SELECT** trên card yêu thích
5. **Sử dụng Items**: Cuộn xuống dưới canvas → Click vào item
6. **Tránh Bombs**: Ô có "B" (đỏ) → -1 life
7. **Tới Goal**: Ô có "F" (vàng) → Win

### Chế Độ Bot
1. **Vào trò chơi**: Menu → Difficulty → Algorithm Selection → Chọn BOT
2. **Xem Trực Tiếp**: 
   - Không có items/bombs (sạch sẽ)
   - Thấy đường đã duyệt (tím): Explored nodes
   - Thấy đường tối ưu (chấm): Path result
   - Thấy robot đỏ: Current position
3. **So sánh Algorithms**: Replay với DFS/BFS/A* khác nhau

---

## 🚀 Cải Tiến Tiếp Theo (Optional)

### Phase 2: Nâng Cao
1. **Persistent Inventory** - Lưu power-ups qua nhiều level
2. **Power-Up Combinations** - Combo effects (2+ skill kích hoạt)
3. **Upgrade System** - Nâng cấp power-up (Level 1→2→3)
4. **Achievement System** - Unlock power-ups qua milestones
5. **Leaderboard** - High scores với power-up tracking

### Phase 3: Gameplay
1. **Timed Mode** - Chế độ giới hạn thời gian
2. **Wave System** - Bombs từng đợt gây ác tính
3. **Boss Battles** - Special challenges với boss patterns
4. **Multiplayer** - Cooperative/competitive modes

---

## 📊 Dữ Liệu & Performance

### Memory Impact
- PowerUp enum: ~2KB (constant)
- CollectedPowerUp instance: ~100 bytes
- Inventory (max 20 items): ~2KB
- Modal UI: ~50KB (only during collection)

### Rendering Performance
- Canvas rendering: No impact (same maze display)
- Inventory panel: Lightweight FlowPane rendering
- Modal: Temporary (not persistent during gameplay)

---

## ⚠️ Ghi Chú Quan Trọng

1. **Bot Mode Clean**: Items/Bombs HOÀN TOÀN bị loại bỏ để clear visualization.
2. **Modal Blocking**: Khi player nhặt item, modal hiện ra (game tạm pause input).
3. **Hệ thống Timer thông minh (Unified UI)**: 
    - **Thanh HUD (Trên cùng)**: Hiển thị danh sách tất cả các item đang kích hoạt kèm theo tên và thời gian đếm ngược chi tiết cho từng cái (ví dụ: `🛡 Shield: 10s`, `⚡ Speed: 5s`).
    - **Đầu Robot**: Chỉ hiển thị bộ đếm giây của item **sắp hết hạn nhất**. Khi item đó hết hiệu lực, hệ thống tự động chuyển sang hiển thị thời gian của item tiếp theo sắp hết.
4. **Speed Boost (2-Step)**: Nhấn một phím di chuyển sẽ giúp robot nhảy 2 ô cùng lúc (nếu không bị chặn bởi tường). Có hiệu ứng bóng mờ khi di chuyển.
5. **Freeze Time (Non-destructive)**: Cho phép đi xuyên qua bom mà bom không biến mất và không nổ.
6. **Time Bonus**: Cộng trực tiếp **15 giây** vào đồng hồ đếm ngược của màn chơi.
7. **Lucky Find (Mystery Box)**: Nhặt được một vật phẩm ngẫu nhiên và thêm vào túi đồ thay vì kích hoạt ngay lập tức.
8. **Smart Sonar Radar (Hard Tier)**: 
    - **Reveal Path**: Hiển thị đường đi ngắn nhất tới Goal dưới dạng overlay xanh lá.
    - **Magnetic Vacuum**: Tự động thu thập (hút) tất cả các item trong bán kính 2 ô Manhattan xung quanh Robot (không cần đi đè lên item).
    - **Combo x2 Score**: Tự động kích hoạt trạng thái nhân đôi điểm số trong suốt 15 giây hiệu lực của Radar.
    - **Pulse Effect**: Hiệu ứng sóng radar tỏa ra từ robot.
9. **AI Assist Victory**: Cho phép robot tự động về đích và kết thúc trò chơi với chiến thắng nếu AI đi tới ô Goal.
10. **Double Score (Instant)**: Nhân đôi số điểm hiện tại của người chơi ngay khi kích hoạt (item rời).

---

## 🐛 Troubleshooting

### Modal Không Hiện Khi Nhặt Item
- Kiểm tra: `inventory != null` trong handlePlayerMove
- Kiểm tra: `stage` và `gameScene[0]` được truyền đúng

### Inventory Panel Trống
- Đảm bảo PLAYER mode được chọn
- Check InventoryPanel.getContainer() được add vào page

### Power-Up Không Deactivate
- Kiểm tra `PowerUpExecutor.updatePowerUpStates()` được gọi
- Kiểm tra `CollectedPowerUp.isExpired()` logic

---

## 📞 Hỗ Trợ Tiếp Theo

Để mở rộng thêm:

1. **Thêm More Power-Ups**: Chỉnh sửa enum PowerUp, thêm vào PowerUpExecutor
2. **Custom Effects**: Override `PowerUpExecutor.executePowerUp()`
3. **Upgrade UI**: Tuỳ chỉnh ItemCardSelectionModal colors/fonts
4. **Statistics**: Thêm tracking power-up usage rate

---

**Phiên bản**: 1.1
**Ngày cập nhật**: 2026-04-15
**Status**: ✅ Ready for Testing
