package com.jeja.jejabe.schedule;

import com.jeja.jejabe.global.response.ApiResponseForm;
import com.jeja.jejabe.schedule.domain.WorshipCategory;
import com.jeja.jejabe.schedule.dto.WorshipCategoryResponseDto;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin/worship-categories")
public class WorshipCategoryController {

    @GetMapping
    public ResponseEntity<ApiResponseForm<List<WorshipCategoryResponseDto>>> getCategories() {
        List<WorshipCategoryResponseDto> categories = Arrays.stream(WorshipCategory.values())
                .map(WorshipCategoryResponseDto::from)
                .collect(Collectors.toList());

        return ResponseEntity.ok(ApiResponseForm.success(categories));
    }
}
