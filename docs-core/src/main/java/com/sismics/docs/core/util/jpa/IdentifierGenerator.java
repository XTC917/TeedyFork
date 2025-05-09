package com.sismics.docs.core.util.jpa;

import java.util.UUID;

/**
 * Identifier generator.
 */
public class IdentifierGenerator {
    /**
     * Generate a new identifier.
     *
     * @return New identifier
     */
    public static String generate() {
        return UUID.randomUUID().toString();
    }
} 