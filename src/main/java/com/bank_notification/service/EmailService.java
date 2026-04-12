package com.bank_notification.service;

public interface EmailService {

    /**

     * Send a simple text email.

     *

     * @param to      recipient email address

     * @param subject email subject

     * @param body    plain-text email body

     */

    void sendEmail(String to, String subject, String body);

}
