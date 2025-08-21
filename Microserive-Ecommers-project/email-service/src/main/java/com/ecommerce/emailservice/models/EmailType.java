package com.ecommerce.emailservice.models;
public enum EmailType {
    USER_CREATED,           // Sent after new user created
    USER_PASSWORD_CHANGE,   // send after password change
    ORDER_CONFIRMATION,     // Sent after purchase
    ORDER_DISPATCHED,       // Shipment initiated
    DELIVERY_UPDATE,        // Tracking details
    CART_REMINDER,          // Abandoned cart follow-up
    PASSWORD_RESET,         // Security-related
    ACCOUNT_CREATION,       // Welcome email
    PROMOTION,              // Offers & newsletters
    PAYMENT_FAILED,         // Alert user about issues
    REVIEW_REQUEST,         // Ask for product feedback
    REFUND_INITIATED,       // Refund-related updates
    SUPPORT_TICKET_REPLY    // Response from customer support
}