package com.fitlife.service.impl;

import com.fitlife.repository.BookingRepository;
import com.fitlife.repository.ClassSlotRepository;
import com.fitlife.service.BookingService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class BookingServiceImpl implements BookingService, BookingService {

    private final ClassSlotRepository classSlotRepository;
    private final BookingRepository bookingRepository;

    public BookingServiceImpl(ClassSlotRepository classSlotRepository, BookingRepository bookingRepository) {
        this.classSlotRepository = classSlotRepository;
        this.bookingRepository = bookingRepository;
    }

    @Transactional
    @Override
    public BookingResponse bookClassSlot(Long slotId, Long memberId) {

        // 1. Lấy dữ liệu và LOCK dòng này lại.
        // Bất kỳ user nào khác gọi hàm này với cùng slotId sẽ bị bắt đứng chờ (Blocking).
        ClassSlot slot = classSlotRepository.findByIdWithPessimisticLock(slotId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy lịch tập!"));

        // 2. Kiểm tra nghiệp vụ (Race Condition đã được khắc phục ở đây)
        if (slot.getAvailableSeats() <= 0) {
            throw new RuntimeException("Rất tiếc, lớp học đã kín chỗ!");
        }

        // 3. Xử lý đặt chỗ và trừ số lượng ghế
        slot.setAvailableSeats(slot.getAvailableSeats() - 1);
        classSlotRepository.save(slot);

        // 4. Lưu lịch sử Booking vào DB
        Booking newBooking = Booking.builder()
                .memberId(memberId)
                .classSlot(slot)
                .status("CONFIRMED")
                .build();
        bookingRepository.save(newBooking);

        return new BookingResponse("Đặt lịch thành công!", newBooking.getId());
    }
}
