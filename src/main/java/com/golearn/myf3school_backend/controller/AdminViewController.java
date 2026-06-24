package com.golearn.myf3school_backend.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Serve các trang HTML admin.
 * Thymeleaf resolve "admin/xxx" → templates/view/admin/xxx.html
 */
@Controller
@RequestMapping("/admin")
public class AdminViewController {

    @GetMapping({"/", "/dashboard"})
    public String dashboard() {
        return "admin/dashboard";
    }

    @GetMapping("/applications")
    public String applications() {
        return "admin/application/applications";
    }

    @GetMapping("/notifications")
    public String notifications() {
        return "admin/notification/notification";
    }
    @GetMapping("/attendance")
    public String attendance() {
        return "admin/attendance/attendance";
    }
    @GetMapping("/grades")
    public String grades() {
        return "admin/grade/grade";
    }
    @GetMapping("/grade-corrections")
    public String gradesCorrections() {
        return "admin/grade/grade-corrections";
    }
    @GetMapping("/grades/import")
    public String gradeImport() {
        return "admin/grade/grade_import";
    }

}