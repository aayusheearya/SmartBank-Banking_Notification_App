package com.bank_notification.service;

import com.bank_notification.model.FixedDeposit;

import com.bank_notification.model.User;


public interface FixedDepositService {

    /**

     * Create and persist a FixedDeposit for the given user.

     * @throws IllegalArgumentException on invalid input

     */

    FixedDeposit createFixedDepositForUser(User user, FixedDeposit fd) throws IllegalArgumentException;

}
