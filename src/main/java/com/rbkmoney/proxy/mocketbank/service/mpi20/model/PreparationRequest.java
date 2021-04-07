package com.rbkmoney.proxy.mocketbank.service.mpi20.model;

import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

@Data
@Builder
public class PreparationRequest {
    @ToString.Exclude
    private String pan;
    private String notificationUrl;
}
