package com.jeja.jejabe.global.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum CommonErrorCode implements ErrorCode {
    // =================================================================
    // 공통 에러 (Common Errors) - C0xx
    // =================================================================
    INTERNAL_SERVER_ERROR("C001", "서버 내부 오류가 발생했습니다.", HttpStatus.INTERNAL_SERVER_ERROR),
    BAD_REQUEST("C002", "잘못된 요청입니다.", HttpStatus.BAD_REQUEST),
    FORBIDDEN("C003", "권한이 없는 사용자입니다.", HttpStatus.FORBIDDEN),
    DATA_NOT_FOUND("C004", "요청한 데이터를 찾을 수 없습니다.", HttpStatus.NOT_FOUND),


    // =================================================================
    // 인증/인가 관련 에러 (Authentication & Authorization) - A0xx (Auth)
    // =================================================================
    // (참고: 아래 출석 에러코드 A0xx와 접두사가 겹치지만, 제공해주신 코드 기반으로 유지합니다)
    INVALID_PASSWORD("A001", "아이디 또는 비밀번호가 일치하지 않습니다.", HttpStatus.UNAUTHORIZED),
    ACCOUNT_INACTIVE("A002", "관리자의 승인 대기 중인 사용자입니다.", HttpStatus.FORBIDDEN),
    DUPLICATE_LOGIN_ID("A003", "이미 사용 중인 아이디입니다.", HttpStatus.CONFLICT),
    MEMBER_ALREADY_HAS_ACCOUNT("A004", "이미 계정이 등록된 멤버입니다.", HttpStatus.CONFLICT),
    USER_NOT_FOUND("A005", "사용자를 찾을 수 없습니다.", HttpStatus.NOT_FOUND),


    // =================================================================
    // 멤버(Member) 관리 관련 에러 - M0xx
    // =================================================================
    MEMBER_NOT_FOUND("M001", "멤버 정보를 찾을 수 없습니다.", HttpStatus.NOT_FOUND),

    // =================================================================
    // 앨범(Album) 관리 관련 에러 - ALxx
    // =================================================================
    ALBUM_NOT_FOUND("AL01", "앨범을 찾을 수 없습니다.", HttpStatus.NOT_FOUND),
    PHOTO_NOT_FOUND("AL02", "사진을 찾을 수 없습니다.", HttpStatus.NOT_FOUND),
    ALBUM_READ_FORBIDDEN("AL03", "이 앨범을 조회할 권한이 없습니다.", HttpStatus.FORBIDDEN),
    ALBUM_WRITE_FORBIDDEN("AL04", "앨범에 대한 쓰기(업로드/수정/삭제) 권한이 없습니다.", HttpStatus.FORBIDDEN),
    EVENT_ALREADY_HAS_ALBUM("AL05", "해당 일정에 이미 연결된 앨범이 존재합니다.", HttpStatus.CONFLICT),

    // =================================================================
    // 게시판(Board/Post) 관리 관련 에러 - B0xx
    // =================================================================
    POST_NOT_FOUND("B001", "게시물을 찾을 수 없습니다.", HttpStatus.NOT_FOUND),
    BOARD_NOT_FOUND("B002", "존재하지 않는 게시판입니다.", HttpStatus.NOT_FOUND),
    COMMENT_NOT_FOUND("B003", "존재하지 않는 댓글입니다.", HttpStatus.NOT_FOUND),
    DUPLICATE_BOARD_KEY("B004", "이미 사용 중인 게시판 키입니다.", HttpStatus.CONFLICT),

    // =================================================================
    // 셀(Cell/순) 관리 관련 에러 - L0xx
    // =================================================================
    CELL_NOT_FOUND("L001", "해당 셀(순)을 찾을 수 없습니다.", HttpStatus.NOT_FOUND),
    CELL_ALREADY_EXISTS("L002", "동일한 이름과 연도의 셀(순)이 이미 존재합니다.", HttpStatus.CONFLICT),
    LEADER_ALREADY_ASSIGNED("L003", "해당 멤버는 이미 다른 셀(순)에 배정되어 있습니다.", HttpStatus.CONFLICT),
    NOT_ASSIGNED_TO_CELL("L004", "배정된 셀(순)이 없습니다.", HttpStatus.NOT_FOUND),


    // =================================================================
    // 새신자(Newcomer) 관리 관련 에러 - N0xx
    // =================================================================
    NEWCOMER_NOT_FOUND("N001", "해당 새신자 정보를 찾을 수 없습니다.", HttpStatus.NOT_FOUND),
    ALREADY_SETTLED_NEWCOMER("N002", "이미 정착 처리된 새신자입니다.", HttpStatus.CONFLICT),
    ALREADY_MEMBER_REGISTERED("N003", "이미 멤버로 등록된 새신자입니다.", HttpStatus.CONFLICT),


    // =================================================================
    // 케어(Care) & 일정(Schedule) 관련 에러 - R0xx, S0xx
    // =================================================================
    CARE_DATA_NOT_FOUND("R001", "케어 정보를 찾을 수 없습니다.", HttpStatus.NOT_FOUND),
    SCHEDULE_NOT_FOUND("S001", "해당 일정을 찾을 수 없습니다.", HttpStatus.NOT_FOUND), // R001 중복 방지를 위해 S001로 변경 권장

    // =================================================================
    // 출석(Attendance) 관련 에러 - A0xx (기존 인증 A코드와 겹치므로 순번 주의)
    // =================================================================
    // 기존 코드들
    ALREADY_ATTENDED("ATT01", "이미 출석 처리된 멤버입니다.", HttpStatus.CONFLICT), // 코드명 충돌 방지를 위해 접두어 ATT 추천하나, 기존 유지시 번호 확장
    GUEST_ALREADY_ATTENDED("ATT02", "입력하신 정보로 이미 출석 처리되었습니다.", HttpStatus.CONFLICT),
    IP_ALREADY_USED("ATT03", "이미 해당 기기(IP)로 출석 체크가 완료되었습니다.", HttpStatus.CONFLICT),
    LOCATION_REQUIRED("ATT04", "위치 정보가 필요합니다.", HttpStatus.BAD_REQUEST),
    TOO_FAR_FROM_CHURCH("ATT05", "교회와의 거리가 너무 멉니다. 교회 내에서 시도해주세요.", HttpStatus.BAD_REQUEST),
    MEMBER_NOT_FOUND_FOR_CHECK_IN("ATT06", "등록된 멤버를 찾을 수 없습니다. 이름과 생년월일을 확인하시거나, 관리자에게 문의해주세요.", HttpStatus.BAD_REQUEST),

    // [New] 추가된 출석 관련 에러 코드 (명단 관리, 신청/취소)
    CANNOT_REMOVE_PRESENT_MEMBER("ATT07", "이미 출석 완료된 인원은 명단에서 삭제할 수 없습니다.", HttpStatus.BAD_REQUEST),
    ALREADY_REGISTERED("ATT08", "이미 명단에 등록되어 있거나 참석 신청된 상태입니다.", HttpStatus.CONFLICT),
    NOT_REGISTERED("ATT09", "참석 신청 내역이 존재하지 않습니다.", HttpStatus.NOT_FOUND),
    ALREADY_ATTENDED_CANNOT_CANCEL("ATT10", "이미 출석 처리가 완료되어 신청을 취소할 수 없습니다.", HttpStatus.BAD_REQUEST),
    NOT_REGISTERED_IN_ATTENDANCE_LIST("ATT11", "명단에 등록된 인원만 출석할 수 있습니다.", HttpStatus.FORBIDDEN);

    private final String code;
    private final String message;
    private final HttpStatus httpStatus;
}
