package com.mycompany.myapp.domain;

import static org.assertj.core.api.Assertions.assertThat;

public class NotificationAsserts {

    /**
     * Asserts that the entity has all properties (fields/relationships) set.
     *
     * @param expected the expected entity
     * @param actual the actual entity
     */
    public static void assertNotificationAllPropertiesEquals(Notification expected, Notification actual) {
        assertNotificationAutoGeneratedPropertiesEquals(expected, actual);
        assertNotificationAllUpdatablePropertiesEquals(expected, actual);
    }

    /**
     * Asserts that the entity has all updatable properties (fields/relationships) set.
     *
     * @param expected the expected entity
     * @param actual the actual entity
     */
    public static void assertNotificationAllUpdatablePropertiesEquals(Notification expected, Notification actual) {
        assertNotificationUpdatableFieldsEquals(expected, actual);
        assertNotificationUpdatableRelationshipsEquals(expected, actual);
    }

    /**
     * Asserts that the entity has all the auto generated properties (fields/relationships) set.
     *
     * @param expected the expected entity
     * @param actual the actual entity
     */
    public static void assertNotificationAutoGeneratedPropertiesEquals(Notification expected, Notification actual) {
        assertThat(actual)
            .as("Verify Notification auto generated properties")
            .satisfies(a -> assertThat(a.getId()).as("check id").isEqualTo(expected.getId()));
    }

    /**
     * Asserts that the entity has all the updatable fields set.
     *
     * @param expected the expected entity
     * @param actual the actual entity
     */
    public static void assertNotificationUpdatableFieldsEquals(Notification expected, Notification actual) {
        assertThat(actual)
            .as("Verify Notification relevant properties")
            .satisfies(a -> assertThat(a.getMessage()).as("check message").isEqualTo(expected.getMessage()))
            .satisfies(a -> assertThat(a.getNotificationType()).as("check notificationType").isEqualTo(expected.getNotificationType()))
            .satisfies(a -> assertThat(a.getIsRead()).as("check isRead").isEqualTo(expected.getIsRead()))
            .satisfies(a -> assertThat(a.getCreatedAt()).as("check createdAt").isEqualTo(expected.getCreatedAt()));
    }

    /**
     * Asserts that the entity has all the updatable relationships set.
     *
     * @param expected the expected entity
     * @param actual the actual entity
     */
    public static void assertNotificationUpdatableRelationshipsEquals(Notification expected, Notification actual) {
        // empty method
    }
}
