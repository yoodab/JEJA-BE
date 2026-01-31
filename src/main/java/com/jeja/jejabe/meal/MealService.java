package com.jeja.jejabe.meal;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MealService {

    private final MealRepository mealRepository;

    public MealResponse getMealHistory() {
        List<MealHistory> allHistory = mealRepository.findAllByOrderByDateDescIdDesc();

        int totalStock = allHistory.stream()
                .mapToInt(MealHistory::getAmount)
                .sum();

        List<MealResponse.HistoryDto> dtos = allHistory.stream()
                .map(h -> new MealResponse.HistoryDto(
                        h.getId(),
                        h.getDate(),
                        h.getCategory(),
                        h.getTargetName(),
                        h.getNote(),
                        h.getAmount()))
                .collect(Collectors.toList());

        return new MealResponse(totalStock, dtos);
    }

    // 2. 재고 추가 (입고)
    @Transactional
    public void addStock(StockRequest request) {
        MealHistory history = MealHistory.builder()
                .category(MealCategory.STOCK)
                .note(request.note())
                .amount(request.amount())
                .build();

        mealRepository.save(history);
    }

    // 3. 식권 사용
    @Transactional
    public void useTicket(UsageRequest request) {
        MealHistory history = MealHistory.builder()
                .category(MealCategory.USE)
                .targetName(request.userName())
                .note(request.place())
                .amount(-request.count())
                .build();

        mealRepository.save(history);
    }

    // 4. 수정 (Update)
    @Transactional
    public void updateMeal(Long id, MealUpdateRequest request) {
        // 1. 엔티티 조회
        MealHistory history = mealRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("해당 내역이 없습니다. id=" + id));

        // 2. 엔티티에게 수정 요청 (Setter 대신 메서드 호출)
        // request의 값들을 넘겨주면, 엔티티가 알아서 음수/양수 변환 처리함
        history.update(
                request.date(),
                request.targetName(),
                request.note(),
                request.amount()
        );
    }

    // 5. 삭제 (Delete)
    @Transactional
    public void deleteMeal(Long id) {
        mealRepository.deleteById(id);
    }
}