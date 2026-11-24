package com.saga.payment.service.ports.output.repository;

import com.saga.common.domain.valueobject.UserId;
import com.saga.payment.domain.entity.UserBalance;

import java.util.Optional;

public interface UserBalanceRepository {

    UserBalance save(UserBalance userBalance);

    Optional<UserBalance> findByUserId(UserId userId);
}
