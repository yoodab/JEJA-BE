package com.jeja.jejabe.meal;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/meals")
@RequiredArgsConstructor
public class MealController {

    private final MealService mealService;

    // 조회: GET /api/meals
    @GetMapping
    public ResponseEntity<MealResponse> getMeals() {
        return ResponseEntity.ok(mealService.getMealHistory());
    }

    // 재고 추가: POST /api/meals/stock
    @PostMapping("/stock")
    public ResponseEntity<Void> addStock(@RequestBody StockRequest request) {
        mealService.addStock(request);
        return ResponseEntity.ok().build();
    }

    // 식권 사용: POST /api/meals/use
    @PostMapping("/use")
    public ResponseEntity<Void> useTicket(@RequestBody UsageRequest request) {
        mealService.useTicket(request);
        return ResponseEntity.ok().build();
    }

    // 수정: PUT /api/meals/{id}
    @PutMapping("/{id}")
    public ResponseEntity<Void> updateMeal(@PathVariable Long id, @RequestBody MealUpdateRequest request) {
        mealService.updateMeal(id, request);
        return ResponseEntity.ok().build();
    }

    // 삭제: DELETE /api/meals/{id}
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteMeal(@PathVariable Long id) {
        mealService.deleteMeal(id);
        return ResponseEntity.ok().build();
    }
}