package net.cytonic.cytosis.auditlog;

import java.util.UUID;

public record Entry(UUID uuid, UUID actor, Category category, String reason) {
}
