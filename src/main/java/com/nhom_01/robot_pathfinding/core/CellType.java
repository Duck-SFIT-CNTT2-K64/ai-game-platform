package com.nhom_01.robot_pathfinding.core;

public enum CellType {
    EMPTY,  // O trong
    WALL,   // Tuong
    BOMB,   // Bom (-1 mang)
    ITEM,   // Vat pham (+diem)
    START,  // Vi tri xuat phat
    GOAL    // Cua thoat
}