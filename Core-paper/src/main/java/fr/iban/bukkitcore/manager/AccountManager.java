package fr.iban.bukkitcore.manager;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import fr.iban.bukkitcore.CoreBukkitPlugin;
import fr.iban.bukkitcore.utils.Scheduler;
import fr.iban.common.data.Account;
import fr.iban.common.data.AccountDAO;
import fr.iban.common.messaging.CoreChannel;

import java.util.UUID;

public class AccountManager {

    private final CoreBukkitPlugin plugin;
    private final LoadingCache<UUID, Account> accounts = Caffeine.newBuilder().build(uuid -> new AccountDAO().getAccount(uuid));

    public AccountManager(CoreBukkitPlugin plugin) {
        this.plugin = plugin;
    }

    public Account getAccount(UUID uuid) {
        return accounts.get(uuid);
    }

    public void reloadAccount(UUID uuid) {
        accounts.invalidate(uuid);
        Scheduler.runAsync(() -> accounts.get(uuid));
    }

    public void saveAccount(Account account) {
        AccountDAO accountDAO = new AccountDAO();
        accountDAO.sendAccountToDB(account);
        plugin.getMessagingManager().sendMessage(CoreChannel.SYNC_ACCOUNT_CHANNEL, account.getUUID().toString());
    }

    public void saveAccountAsync(Account account) {
        Scheduler.runAsync(() -> saveAccount(account));
    }
}
