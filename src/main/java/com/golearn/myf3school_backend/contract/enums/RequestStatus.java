package com.golearn.myf3school_backend.contract.enums;

public enum RequestStatus {
    DRAFT,      // Nháp — sinh viên chưa gửi chính thức
    PENDING,    // Đang chờ duyệt
    APPROVED,   // Đã được duyệt
    REJECTED    // Bị từ chối
}