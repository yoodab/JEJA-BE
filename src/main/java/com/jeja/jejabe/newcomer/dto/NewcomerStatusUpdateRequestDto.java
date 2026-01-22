package com.jeja.jejabe.newcomer.dto;

import com.jeja.jejabe.newcomer.domain.NewcomerStatus;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class NewcomerStatusUpdateRequestDto {
    private NewcomerStatus status;
}
