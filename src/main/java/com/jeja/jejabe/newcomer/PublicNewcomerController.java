package com.jeja.jejabe.newcomer;

import com.jeja.jejabe.global.response.ApiResponseForm;
import com.jeja.jejabe.newcomer.dto.NewcomerCreateRequestDto;
import com.jeja.jejabe.newcomer.dto.PublicNewcomerCreateRequestDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/newcomers/public")
@RequiredArgsConstructor
public class PublicNewcomerController {

    private final NewcomerService newcomerService;

    @PostMapping
    public ResponseEntity<ApiResponseForm<Long>> registerPublicNewcomer(@RequestBody PublicNewcomerCreateRequestDto requestDto) {
        // Public DTO -> Internal DTO 변환
        NewcomerCreateRequestDto internalDto = new NewcomerCreateRequestDto();
        internalDto.setName(requestDto.getName());
        internalDto.setBirthDate(requestDto.getBirthDate());
        internalDto.setGender(requestDto.getGender());
        internalDto.setPhone(requestDto.getPhone());
        internalDto.setAddress(requestDto.getAddress());
        
        // 기본값 설정
        internalDto.setIsChurchRegistered(false); // 온라인 등록은 아직 행정 미등록 상태로 간주
        internalDto.setFirstStatus("온라인 본인 등록");

        Long id = newcomerService.registerNewcomer(internalDto);
        return ResponseEntity.ok(ApiResponseForm.success(id, "새가족 등록이 완료되었습니다. 환영합니다!"));
    }
}
