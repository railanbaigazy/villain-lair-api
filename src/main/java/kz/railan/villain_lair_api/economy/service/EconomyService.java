package kz.railan.villain_lair_api.economy.service;

import kz.railan.villain_lair_api.common.exception.InsufficientCoinsException;
import kz.railan.villain_lair_api.economy.entity.Transaction;
import kz.railan.villain_lair_api.economy.entity.TransactionType;
import kz.railan.villain_lair_api.economy.repository.TransactionRepository;
import kz.railan.villain_lair_api.user.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class EconomyService {
    @Autowired
    private TransactionRepository transactionRepository;

    public int credit(User user, int amount, TransactionType type, String description) {
        return changeCoins(user, amount, type, description);
    }

    public int debit(User user, int amount, TransactionType type, String description) {
        return changeCoins(user, -amount, type, description);
    }

    private int changeCoins(User user, int amountDelta, TransactionType type, String description) {
        int currentCoins = user.getCoins() == null ? 0 : user.getCoins();
        int updatedCoins = currentCoins + amountDelta;
        if (updatedCoins < 0) {
            throw new InsufficientCoinsException("Not enough coins for this action");
        }

        user.setCoins(updatedCoins);
        if (amountDelta != 0) {
            Transaction transaction = new Transaction();
            transaction.setUser(user);
            transaction.setAmount(amountDelta);
            transaction.setType(type);
            transaction.setDescription(description);
            transactionRepository.save(transaction);
        }
        return updatedCoins;
    }
}
