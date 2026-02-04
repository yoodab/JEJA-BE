package com.jeja.jejabe.finance.service;

import com.jeja.jejabe.finance.domain.DuesEvent;
import com.jeja.jejabe.finance.domain.DuesRecord;
import com.jeja.jejabe.finance.domain.PaymentMethod;
import com.jeja.jejabe.finance.dto.DuesEventDto;
import com.jeja.jejabe.finance.dto.DuesRecordDto;
import com.jeja.jejabe.finance.repository.DuesEventRepository;
import com.jeja.jejabe.finance.repository.DuesRecordRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class DuesService {

    private final DuesEventRepository eventRepository;
    private final DuesRecordRepository recordRepository;

    @Transactional(readOnly = true)
    public List<DuesEventDto> getEvents() {
        return eventRepository.findAllByOrderByDateDesc().stream().map(e -> {
            DuesEventDto dto = new DuesEventDto();
            dto.setId(e.getId());
            dto.setName(e.getName());
            dto.setTargetAmount(e.getTargetAmount());
            dto.setDate(e.getDate());
            dto.setPriceOptions(e.getPriceOptions());
            dto.setTargetDate(e.getTargetDate());
            dto.setScheduleId(e.getScheduleId());
            return dto;
        }).collect(Collectors.toList());
    }

    public Long createEvent(DuesEventDto dto) {
        DuesEvent event = DuesEvent.builder()
                .name(dto.getName())
                .targetAmount(dto.getTargetAmount())
                .date(dto.getDate())
                .scheduleId(dto.getScheduleId())
                .targetDate(dto.getTargetDate())
                .priceOptions(dto.getPriceOptions())
                .build();
        return eventRepository.save(event).getId();
    }

    public void updateEvent(Long id, DuesEventDto dto) {
        DuesEvent event = eventRepository.findById(id).orElseThrow();
        event.update(dto.getName(), dto.getTargetAmount(), dto.getDate(), dto.getTargetDate(), dto.getScheduleId(), dto.getPriceOptions());
    }

    public void deleteEvent(Long id) {
        eventRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public List<DuesRecordDto> getRecords(Long eventId) {
        return recordRepository.findAllByEventIdOrderByMemberNameAsc(eventId).stream().map(r -> {
            DuesRecordDto dto = new DuesRecordDto();
            dto.setId(r.getId());
            dto.setEventId(r.getEvent().getId());
            dto.setMemberName(r.getMemberName());
            dto.setPaidAmount(r.getPaidAmount());
            dto.setExpectedAmount(r.getExpectedAmount());
            dto.setPaymentMethod(r.getPaymentMethod());
            dto.setPaymentDate(r.getPaymentDate());
            dto.setNote(r.getNote());
            return dto;
        }).collect(Collectors.toList());
    }

    public void createRecordsBatch(List<DuesRecordDto> dtos) {
        for (DuesRecordDto dto : dtos) {
            DuesEvent event = eventRepository.findById(dto.getEventId()).orElseThrow();
            recordRepository.save(DuesRecord.builder()
                    .event(event)
                    .memberName(dto.getMemberName())
                    .paidAmount(0L)
                    .paymentMethod(PaymentMethod.ACCOUNT)
                    .build());
        }
    }

    public void updateRecord(Long id, DuesRecordDto dto) {
        DuesRecord record = recordRepository.findById(id).orElseThrow();
        record.update(dto.getPaidAmount(), dto.getExpectedAmount(), dto.getPaymentMethod(), dto.getPaymentDate(), dto.getNote());
    }

    public void deleteRecord(Long id) {
        recordRepository.deleteById(id);
    }
}
