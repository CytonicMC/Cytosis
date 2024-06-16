package net.cytonic.cytosis.auditlog;

import java.util.UUID;

/**
 * A class representing an entry in the audit log
 *
 * @param uuid     The UUID of the player
 * @param actor    The UUID of the actor who caused the entry
 * @param category The cateory of the entry
 * @param reason   The reason for the entry
 */
public record Entry(UUID uuid, UUID actor, Category category, String reason) {
}