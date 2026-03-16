package com.nhom_01.robot_pathfinding.core;

public enum CellType {
    EMPTY,  // Ô trống
    WALL,   // Tường
    BOMB,   // Bom (-1 mạng)
    ITEM,   // Vật phẩm (+điểm)
    START,  // Vị trí xuất phát
    GOAL    // Cửa thoát
}