package fr.iban.common.data;

import org.jdbi.v3.core.Jdbi;

public abstract class Migration {
    protected final Jdbi jdbi;
    private final int version;

    public Migration(Jdbi jdbi, int version) {
        this.jdbi = jdbi;
        this.version = version;
    }

    public int getVersion() {
        return version;
    }

    public abstract void migrate();
}