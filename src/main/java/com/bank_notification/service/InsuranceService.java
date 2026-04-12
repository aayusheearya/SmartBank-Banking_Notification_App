package com.bank_notification.service;

import com.bank_notification.model.Insurance;

import com.bank_notification.model.User;


public interface InsuranceService {

    /**

     * Create and persist an Insurance entry for the given user.

     * Performs minimal checks (provider & phone required).

     */

    Insurance createInsuranceForUser(User user, Insurance insurance) throws IllegalArgumentException;

}