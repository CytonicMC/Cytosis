package net.cytonic.cytosis.migrations;

import java.io.IOException;

import io.ebean.annotation.Platform;
import io.ebean.dbmigration.DbMigration;

public class MigrationGenerator {

    static void main() throws IOException {
        DbMigration migration = DbMigration.create();
        migration.setPlatform(Platform.POSTGRES);
        migration.generateMigration();
    }
}
