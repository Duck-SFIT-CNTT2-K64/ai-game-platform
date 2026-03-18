# 🎮 Hướng Dẫn Kiểm Thử Hệ Thống Power-Up

## ✅ Checklist Tính Năng

### 1. Lọc Items/Bombs Theo Chế Độ

**Test: Bot Mode (Không có Items/Bombs)**
- [ ] Vào Game → Chọn Difficulty → Algorithm Selection → BOT mode
- [ ] Nova: **KHÔNG** thấy biểu tượng "D" (items) hoặc "B" (bombs)
- [ ] Chỉ thấy: S (start), F (goal), ~ (explored), . (path)
- [ ] Maze sạch sẽ, chỉ hiển thị đường đi

**Test: Player Mode (Có Items/Bombs)**
- [ ] Vào Game → Chọn Difficulty → PLAYER mode
- [ ] NÊN thấy: D (items xanh lá), B (bombs đỏ) rải khắp maze
- [ ] Items có thể nhặt, bombs có thể tránh
- [ ] Tường (đen), ô trống (xám), start (xanh dương), goal (vàng)

---

### 2. Modal Chọn Power-Up (3 Thẻ Bài)

**Test: Nhặt Item → Modal Hiện**
1. **Chế độ PLAYER**
2. **Di chuyển** tới ô ITEM (D xanh lá)
3. **Kỳ vọng**:
   - ✅ Game tạm dừng input
   - ✅ Modal hiện với tiêu đề "SELECT A POWER-UP"
   - ✅ 3 tấm thẻ bài hiển thị (cards)
   - ✅ Mỗi card có:
     - Độ khó (Easy/Medium/Hard) - màu: xanh (easy), cam (medium), đỏ (hard)
     - Tên Power-Up (tiêu đề)
     - Mô tả tiếng Anh (description)
     - Hiệu ứng tiếng Việt ("Hiệu ứng:")
     - Nút "SELECT" màu xanh

**Test: Chọn Card & Nhận Power-Up**
- [ ] Nhấn **SELECT** trên card bất kỳ
- [ ] Modal tắt (tối giản dần)
- [ ] Quay lại game screen
- [ ] Power-up xuất hiện trong **Inventory Panel** dưới canvas

**Test: 3 Cards Khác Nhau Mỗi Lần**
- [ ] Nhặt item lần 1 → 3 power-ups (vd: Speed Boost, Shield, Reveal Path)
- [ ] Nhặt item lần 2 → 3 power-ups **khác** (ngẫu nhiên)
- [ ] Kiểm tra số lượng cards = 3 mỗi lần
- [ ] Kiểm tra không lặp lại power-up trong cùng modal

---

### 3. Inventory Panel (Lưu Trữ Power-Up)

**Test: Inventory Panel Visible**
- [ ] Game PLAYER mode
- [ ] Dưới canvas thấy:
  - Nhãn: "COLLECTED POWER-UPS" (tiêu đề xanh cyan)
  - Panel: Hiển thị items đã nhặt
- [ ] Bot mode: Inventory KHÔNG visible (hoặc trống)

**Test: Thêm Power-Up Vào Inventory**
1. Nhặt 1 item → Chọn power-up A
2. **Kỳ vọng**:
   - ✅ Card/item xuất hiện trong inventory
   - ✅ Hiển thị: Tên + Icon + Trạng thái "READY"
   - ✅ Border cam (READY) hoặc xanh (ACTIVE)

3. Nhặt 2 item → Chọn power-up B
4. **Kỳ vọng**:
   - ✅ 2 cards hiển thị trong inventory
   - ✅ Power-up A và B cơ bản hiển thị

**Test: Visual Styling**
- [ ] Mỗi item có border xung quanh (2px)
- [ ] Hover vào item → highlight (màu sáng hơn)
- [ ] Cursor đổi thành "hand" (pointer)
- [ ] Background trong suốt, border rõ ràng

---

### 4. Kích Hoạt Power-Up

**Test: Click Item Để Activate**
1. **Inventory** có items (vd: Speed Boost - READY)
2. **Nhấp chuột** vào item
3. **Kỳ vọng**:
   - ✅ Trạng thái: READY → ACTIVE
   - ✅ Màu border: Cam (READY) → Xanh (ACTIVE)
   - ✅ Status text ở giữa màn hình cập nhật

**Test: Deactivation (Hết Hiệu Lực)**
- [ ] Power-up hoạt động trong thời gian (vd: 5s cho BOMB_IMMUNITY)
- [ ] Sau timeout → Tự động quay về READY
- [ ] Border: Xanh → Cam
- [ ] Có thể kích hoạt lại

---

### 5. Status Text Cập Nhật

**Test: Messages Hiển Thị Đúng**
- [ ] **Di chuyển bình thường**: "MOVE WITH ARROW KEYS - REACH THE GOAL FLAG"
- [ ] **Gặp tường**: "BLOCKED BY WALL - TRY ANOTHER DIRECTION"
- [ ] **Nhặt item**: "ITEM COLLECTED - SELECT A POWER-UP"
- [ ] **Gặp bomb**: "HIT A BOMB -1 LIFE" (hoặc GAME OVER nếu lives = 0)
- [ ] **Tới goal**: "YOU REACHED GOAL - FINAL SCORE: [điểm]"

---

### 6. Legendpanel (Vẫn Hoạt Động)

**Test: Toggle Legend**
- [ ] Nút "HIDE LEGEND" hiện → Click
- [ ] Legend panel ẩn, canvas mở rộng
- [ ] Nút đổi thành "SHOW LEGEND"
- [ ] Click lại → Legend hiện, canvas bình thường

---

## 🧪 Test Cases Chi Tiết

### Scenario 1: Đầu Tiên Nhặt Item
```
ACTION: Vào PLAYER mode (Easy) → Di chuyển tới Item
EXPECT:
✅ Modal 3 cards hiện
✅ Cards có tên, mô tả, độ khó khác nhau
✅ Nút SELECT khả dụng

ACTION: Nhấn SELECT trên card 1
EXPECT:
✅ Modal tắt (fade out)
✅ Quay lại game screen
✅ Power-up xuất hiện trong inventory
✅ Status: "ITEM COLLECTED - SELECT A POWER-UP" → "MOVE WITH ARROW KEYS..."
```

### Scenario 2: Độc Lập Inventory
```
ACTION: Nhặt 3 items → Chọn 3 power-ups khác nhau
EXPECT:
✅ Inventory hiển thị 3 items
✅ Mỗi item có tên khác nhau
✅ Tất cả trạng thái = READY

ACTION: Click item 1
EXPECT:
✅ Item 1: READY → ACTIVE (xanh)
✅ Items 2,3: Vẫn READY (cam)
```

### Scenario 3: Bot Mode - Validation
```
ACTION: Vào Bot mode (BFS, Easy)
EXPECT:
✅ Canvas hiển thị maze SẠC (không item, không bomb)
✅ Chỉ: walls (đen), empty (xám), start (S xanh), goal (F vàng), path (.)
✅ Inventory panel: KHÔNG visible
✅ Legend hiển thị bình thường (không affected)

ACTION: Run algorithm
EXPECT:
✅ Robot tìm đường không bị cản bởi items/bombs
✅ Explored nodes hiển thị (~)
✅ Final path hiển thị (.)
```

### Scenario 4: Score & Lives Validation (Player Mode)
```
ACTION: EASY mode, nhặt 1 item
EXPECT:
✅ Score +180
✅ Lives không đổi (vẫn 5)

ACTION: Gặp bomb
EXPECT:
✅ Lives -1 (5 → 4)
✅ Score -120
✅ Status: "HIT A BOMB -1 LIFE"

ACTION: Power-up EXTRA_LIFE → Activate
EXPECT:
✅ Lives +1
✅ Inventory item được dùng (hoặc deactivate)
```

---

## 📊 Performance Tests

### Memory Check
- [ ] Start game: Normal RAM usage (<500MB)
- [ ] Nhặt 5-10 items: No memory leak
- [ ] Open/close modal 20 lần: Stable
- [ ] Final: No garbage collection issues

### UI Responsiveness
- [ ] Di chuyển arrow key: No lag (~60 FPS)
- [ ] Modal hiện: <100ms
- [ ] Inventory update: <50ms
- [ ] Click inventory item: <200ms response

---

## 🔍 Visual Validation

### Colors & Icons
- [ ] Item icon: "D" xanh lá cây (#2BD99F)
- [ ] Bomb icon: "B" đỏ (#EE5A7A)
- [ ] Start: "S" xanh dương (#5EA5FF)
- [ ] Goal: "F" vàng (#FFD166)
- [ ] Robot: "R" xanh (#8BE9FD)
- [ ] Modal border: Cyan (#00FFFF)
- [ ] Inventory READY: Cam (#FFB800)
- [ ] Inventory ACTIVE: Xanh (#00FF9C)

### Font & Text
- [ ] Title "SELECT A POWER-UP": Orbitron, 40pt, cyan
- [ ] Power-up name: Orbitron, 18pt, cyan
- [ ] Description: Arial, 13pt, xanh nhạt
- [ ] Effect: Arial, 12pt, xanh lá
- [ ] Difficulty tag: 12pt, màu theo độ khó
- [ ] Button: "SELECT" center-aligned

---

## 🐛 Edge Cases

### Test 1: Nhặt Item Tại Vị Trí Goal
```
ACTION: Di chuyển tới item ADJACENT to goal
EXPECT:
✅ Nếu bước vào goal → Game over (không chọn power-up)
✅ Nếu bước vào item trước → Modal hiện → Sau đó có thể di chuyển tới goal
```

### Test 2: Bomb Gần Item
```
ACTION: Item = (10,10), Bomb = (9,10)
EXPECT:
✅ Player có thể nhặt item tại (10,10)
✅ Bomb tại (9,10) vẫn tồn tại (independent)
```

### Test 3: Inventory Over-Capacity
```
ACTION: Nhặt 15+ items
EXPECT:
✅ Inventory vẫn hiển thị tất cả (scroll nếu cần)
✅ Không crash
✅ Tất cả items vẫn clickable
```

### Test 4: Rapid Item Collection
```
ACTION: Nhặt Q items liên tiếp (5 bước)
EXPECT:
✅ Mỗi item → Modal riêng (không overlap)
✅ Inventory cập nhật đúng
✅ Không miss power-up nào
```

---

## 📝 Test Report Template

```
╔════════════════════════════════════════════════════════════════════╗
║               POWER-UP SYSTEM TEST REPORT                          ║
║════════════════════════════════════════════════════════════════════║
║ Test Date: ____________________                                     ║
║ Tester: _____________________ 	Version: 1.0            ║
╚════════════════════════════════════════════════════════════════════╝

📋 FEATURE TESTS
├─ [  ] Bot Mode ohne items/bombs
├─ [  ] Item Collection Modal
├─ [  ] Power-up Card Selection
├─ [  ] Inventory Management
├─ [  ] Activate/Deactivate
├─ [  ] Status Messages
└─ [  ] Legend Toggle

🎨 VISUAL TESTS
├─ [  ] Colors Correct
├─ [  ] Icons Display
├─ [  ] Font Sizes OK
├─ [  ] Hover Effects
└─ [  ] Layout Alignment

⚡ PERFORMANCE
├─ [  ] No Memory Leaks
├─ [  ] FPS Stable (60+)
├─ [  ] Modal Responsive
├─ [  ] Inventory Quick
└─ [  ] No Crashes

🐛 ISSUES FOUND
1. ___________________________________
2. ___________________________________
3. ___________________________________

✅ PASS/❌ FAIL: __________
```

---

## 🎯 Critical Success Criteria

✅ = MUST HAVE
- [ ] Bot mode: Zero items/bombs displayed
- [ ] Player mode: Items/bombs present
- [ ] Modal shows 3 different power-ups
- [ ] Click SELECT → power-up in inventory
- [ ] Inventory panel visible for player
- [ ] Status messages update
- [ ] No crashes on item collection
- [ ] No memory leaks

🟡 = NICE TO HAVE
- [ ] Smooth animations
- [ ] Hover effects
- [ ] Color gradient transitions
- [ ] Sound effects (optional)

---

## 📞 Báo Cáo Lỗi

Nếu gặp vấn đề, ghi lại:
1. **Device**: OS, Java version, RAM
2. **Steps to Reproduce**: Bước nào trigger lỗi
3. **Expected vs Actual**: Kỳ vọng gì vs xảy ra gì
4. **Screenshot**: Nếu có UI issue
5. **Console Logs**: Error messages từ terminal

---

**Ghi chú**: Kiểm thử toàn diện trước khi merge vào main 🚀
