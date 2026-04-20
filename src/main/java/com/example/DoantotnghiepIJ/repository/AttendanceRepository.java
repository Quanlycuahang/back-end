package com.example.DoantotnghiepIJ.repository;

import com.example.DoantotnghiepIJ.Enum.AttendanceStatus;
import com.example.DoantotnghiepIJ.entity.Attendance;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;


public interface AttendanceRepository extends JpaRepository<Attendance, Long> {

    // =========================
    // 👨‍💻 NHÂN VIÊN
    // =========================

    Optional<Attendance> findByUserIdAndDate(Long userId, LocalDate date);

    List<Attendance> findByUserIdAndDateBetween(Long userId, LocalDate start, LocalDate end);

    boolean existsByUserIdAndDateAndCheckOutIsNull(Long userId, LocalDate date);

    Optional<Attendance> findTopByUserIdAndDateAndCheckOutIsNullOrderByCheckInDesc(
            Long userId, LocalDate date
    );

    // =========================
    // 👨‍💼 ADMIN
    // =========================

    // 🔥 thiếu cái này (service đang cần)
    List<Attendance> findByDate(LocalDate date);

    // phân trang
    Page<Attendance> findByDate(LocalDate date, Pageable pageable);

    // filter theo status (PENDING / APPROVED / REJECTED)
    List<Attendance> findByStatus(AttendanceStatus status);

    // 🔥 cực hữu ích cho dashboard
    long countByDate(LocalDate date);

    long countByDateAndStatus(LocalDate date, AttendanceStatus status);

}