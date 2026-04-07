package com.example.DoantotnghiepIJ.controller.admin;



import com.example.DoantotnghiepIJ.dto.Booking.BookingRequest;
import com.example.DoantotnghiepIJ.dto.Booking.BookingResponse;
import com.example.DoantotnghiepIJ.Enum.BookingStatus;
import com.example.DoantotnghiepIJ.service.BookingService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/bookings")
@RequiredArgsConstructor
public class BookingController {

    private final BookingService service;

    //  User tạo booking
    @PostMapping
    public BookingResponse create(@RequestBody BookingRequest request) {
        return service.create(request);
    }

    //  Admin xem tất cả
    @GetMapping
    public List<BookingResponse> getAll() {
        return service.getAll();
    }

    //  Filter theo trạng thái
    @GetMapping("/status")
    public List<BookingResponse> getByStatus(@RequestParam BookingStatus status) {
        return service.getByStatus(status);
    }

    //  Confirm
    @PutMapping("/{id}/confirm")
    public String confirm(@PathVariable UUID id) {
        service.confirm(id);
        return "Confirmed";
    }

    //  Cancel
    @PutMapping("/{id}/cancel")
    public String cancel(@PathVariable UUID id) {
        service.cancel(id);
        return "Cancelled";
    }
}