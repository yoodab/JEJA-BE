package com.jeja.jejabe.newcomer;

import com.jeja.jejabe.global.exception.CommonErrorCode;
import com.jeja.jejabe.global.exception.GeneralException;
import com.jeja.jejabe.member.MemberRepository;
import com.jeja.jejabe.member.domain.Member;
import com.jeja.jejabe.newcomer.domain.MdAssignment;
import com.jeja.jejabe.newcomer.dto.MdAssignmentRequestDto;
import com.jeja.jejabe.newcomer.dto.MdAssignmentResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MdAssignmentService {

    private final MdAssignmentRepository mdRepository;
    private final MemberRepository memberRepository;

    public List<MdAssignmentResponseDto> getAllMds() {
        return mdRepository.findAll().stream()
                .map(MdAssignmentResponseDto::new)
                .collect(Collectors.toList());
    }

    @Transactional
    public Long createMd(MdAssignmentRequestDto dto) {
        Member member = memberRepository.findById(dto.getMemberId())
                .orElseThrow(() -> new GeneralException(CommonErrorCode.MEMBER_NOT_FOUND));

        MdAssignment md = MdAssignment.builder()
                .member(member)
                .charge(dto.getCharge())
                .ageGroup(dto.getAgeGroup())
                .build();

        return mdRepository.save(md).getId();
    }

    @Transactional
    public void updateMd(Long mdId, MdAssignmentRequestDto dto) {
        MdAssignment md = mdRepository.findById(mdId)
                .orElseThrow(() -> new IllegalArgumentException("해당 MD 배치를 찾을 수 없습니다."));

        md.update(dto.getCharge(), dto.getAgeGroup());
    }

    @Transactional
    public void deleteMd(Long mdId) {
        mdRepository.deleteById(mdId);
    }
}
