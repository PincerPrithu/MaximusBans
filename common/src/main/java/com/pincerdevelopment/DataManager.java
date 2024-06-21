package com.pincerdevelopment;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.jdbc.JdbcConnectionSource;
import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.stmt.Where;
import com.j256.ormlite.table.TableUtils;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class DataManager {
    private final JdbcConnectionSource connectionSource;
    private static Dao<Punishment, Integer> punishmentDao = null;

    public DataManager(String databaseUrl) throws SQLException {
        this.connectionSource = new JdbcConnectionSource(databaseUrl);
        punishmentDao = DaoManager.createDao(connectionSource, Punishment.class);
        try {
            TableUtils.createTableIfNotExists(connectionSource, Punishment.class);
        } catch (SQLException e) {
            throw new RuntimeException("Failed to create table", e);
        }
    }

    public static CompletableFuture<Void> addPunishment(Punishment punishment) {
        return CompletableFuture.runAsync(() -> {
            try {
                punishmentDao.create(punishment);
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        });
    }

    public static CompletableFuture<List<Punishment>> getPunishmentsByPlayer(UUID playerUUID) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return punishmentDao.queryBuilder().where().eq("playerUUID", playerUUID).query();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        });
    }

    public static CompletableFuture<List<Punishment>> getPunishmentsByIssuer(UUID issuerUUID) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return punishmentDao.queryBuilder().where().eq("issuerUUID", issuerUUID).query();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        });
    }

    public static CompletableFuture<List<Punishment>> getActivePunishments(UUID playerUUID) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                QueryBuilder<Punishment, Integer> queryBuilder = punishmentDao.queryBuilder();
                Where<Punishment, Integer> where = queryBuilder.where();

                where.and(
                        where.eq("playerUUID", playerUUID),
                        where.isNull("pardonDate"),
                        where.or(
                                where.isNull("timeOfExpiry"),
                                where.gt("timeOfExpiry", new Date())
                        )
                );

                return queryBuilder.query();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        });
    }

    public static CompletableFuture<List<Punishment>> getActivePunishmentsByIP(String playerIP) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                QueryBuilder<Punishment, Integer> queryBuilder = punishmentDao.queryBuilder();
                Where<Punishment, Integer> where = queryBuilder.where();

                where.and(
                        where.eq("playerIP", playerIP),
                        where.isNull("pardonDate"),
                        where.or(
                                where.isNull("timeOfExpiry"),
                                where.gt("timeOfExpiry", new Date())
                        )
                );

                return queryBuilder.query();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        });
    }

    public static CompletableFuture<Void> updatePunishmentIP(Punishment punishment) {
        return CompletableFuture.runAsync(() -> {
            try {
                punishmentDao.update(punishment);
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        });
    }

    public static CompletableFuture<Void> updatePardonInfo(Punishment punishment) {
        return CompletableFuture.runAsync(() -> {
            try {
                System.out.println("trying to set pardo nreason");
                punishmentDao.update(punishment);
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        });
    }

    public static CompletableFuture<Void> updatePunishment(Punishment punishment) {
        return CompletableFuture.runAsync(() -> {
            try {
                punishmentDao.update(punishment);
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        });
    }

    public void close() throws IOException {
        connectionSource.close();
    }
}
