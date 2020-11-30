package com.company.qldp.peopleservice.domain.dto;

import com.company.qldp.peopleservice.domain.validation.phone.ValidPhoneNumber;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotNull;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StayDto {
    
    @NotNull
    @Length(min = 1)
    private String peopleCode;
    
    @ValidPhoneNumber
    private String phoneNumber;
    
    @NotNull
    @Length(min = 1)
    private String fromDate;
    
    @NotNull
    @Length(min = 1)
    private String toDate;
    
    private String reason;
}
