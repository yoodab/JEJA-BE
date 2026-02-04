package com.jeja.jejabe.finance.dto;

import com.jeja.jejabe.finance.domain.PaymentMethod;
import lombok.Data;

import java.time.LocalDate;

@Data
public class DuesRecordDto {
    private Long id;
    private Long eventId;
    private String memberName;
    private Long paidAmount;
    private Long expectedAmount;
    private PaymentMethod paymentMethod;
    private LocalDate paymentDate;
    private String note;
}
