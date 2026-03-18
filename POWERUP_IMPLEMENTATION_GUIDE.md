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
| 1 | Speed Boost | Robot di chuyển nhanh | Robot nhanh hơn | Easy |
| 2 | Shield | Chặn 1 lần bomb nổ | Gặp bomb không chết | Easy |
| 3 | Reveal Path | Hiển thị đường đi | Thấy đường tới goal | Medium |
| 4 | Extra Life | Thêm 1 mạng | +1 life | Easy |
| 5 | Bomb Detector | Phát hiện bomb gần | Hiện vị trí bomb | Medium |
| 6 | Teleport | Dịch chuyển tức thời | Teleport ngẫu nhiên | Medium |
| 7 | Remove Wall | Xóa 1 bức tường | Phá tường 1 lần | Medium |
| 8 | Slow Bombs | Bomb kích hoạt chậm | Bomb delay | Medium |
| 9 | Double Score | Nhân đôi điểm | x2 score | Easy |
| 10 | Freeze Time | Đông băng bomb | Bomb không nổ | Medium |
| 11 | Reveal Map | Hiển thị toàn map | Thấy toàn bộ mê | Easy |
| 12 | AI Assist | Trợ giúp AI | Auto solve đoạn | Medium |
| 13 | Shortest Path Mode | Ưu tiên đường ngắn | BFS/A* priority | Medium |
| 14 | Bomb Immunity | Miễn nhiễm bomb | Thời gian 5s | Easy |
| 15 | Speed Slow | Chậm nhưng an toàn | Robot chậm | Easy |
| 16 | Lucky Find | Tìm items tốt hơn | Tăng spawn item | Medium |
| 17 | Double Choice | Chọn 2 skill cùng lúc | 2 skills | Hard |
| 18 | Safe Step | Bước tiếp an toàn | Next step safe | Medium |
| 19 | Time Bonus | Thêm thời gian | +30 giây | Easy |
| 20 | Vision Boost | Tầm nhìn xa hơn | 2x vision | Medium |

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

1. **Bot Mode Clean**: Items/Bombs HOÀN TOÀN bị loại bỏ để clear visualization
2. **Modal Blocking**: Khi player nhặt item, modal hiện ra (game tạm pause tile input)
3. **Single Use**: Một số power-ups (EXTRA_LIFE, SHIELD, v.v.) tự deactivate sau dùng
4. **Duration-Based**: Một số power-ups có thời gian (BOMB_IMMUNITY = 5s)
5. **Inventory Unlimited**: Không giới hạn số lượng items nhặt được

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

**Phiên bản**: 1.0  
**Ngày cập nhật**: 2026-03-18  
**Status**: ✅ Ready for Testing
