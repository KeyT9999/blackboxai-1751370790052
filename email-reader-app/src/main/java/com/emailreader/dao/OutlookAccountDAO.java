package com.emailreader.dao;

import com.emailreader.model.OutlookAccount;
import com.emailreader.util.DatabaseUtil;
import com.emailreader.util.EncryptionUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class OutlookAccountDAO {
    private static final Logger logger = LoggerFactory.getLogger(OutlookAccountDAO.class);

    public OutlookAccount save(OutlookAccount account) {
        String sql = """
            INSERT INTO outlook_accounts 
            (email, display_name, access_token, refresh_token, expires_at, added_by_admin_id)
            VALUES (?, ?, ?, ?, ?, ?)
            RETURNING id
        """;

        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, account.getEmail());
            stmt.setString(2, account.getDisplayName());
            // Encrypt sensitive data before storing
            stmt.setString(3, EncryptionUtil.encrypt(account.getAccessToken()));
            stmt.setString(4, EncryptionUtil.encrypt(account.getRefreshToken()));
            stmt.setTimestamp(5, Timestamp.valueOf(account.getExpiresAt()));
            stmt.setLong(6, account.getAddedByAdminId());

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    account.setId(rs.getLong("id"));
                    return account;
                } else {
                    throw new RuntimeException("Failed to save Outlook account");
                }
            }
        } catch (SQLException e) {
            logger.error("Error saving Outlook account: {}", account.getEmail(), e);
            throw new RuntimeException("Database error occurred", e);
        }
    }

    public void updateTokens(Long accountId, String accessToken, String refreshToken, LocalDateTime expiresAt) {
        String sql = """
            UPDATE outlook_accounts 
            SET access_token = ?, refresh_token = ?, expires_at = ?
            WHERE id = ?
        """;

        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, EncryptionUtil.encrypt(accessToken));
            stmt.setString(2, EncryptionUtil.encrypt(refreshToken));
            stmt.setTimestamp(3, Timestamp.valueOf(expiresAt));
            stmt.setLong(4, accountId);

            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected != 1) {
                throw new RuntimeException("Failed to update tokens");
            }
        } catch (SQLException e) {
            logger.error("Error updating tokens for account ID: {}", accountId, e);
            throw new RuntimeException("Database error occurred", e);
        }
    }

    public Optional<OutlookAccount> findById(Long id) {
        String sql = """
            SELECT id, email, display_name, access_token, refresh_token, expires_at, added_by_admin_id
            FROM outlook_accounts WHERE id = ?
        """;

        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, id);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToAccount(rs));
                }
            }
        } catch (SQLException e) {
            logger.error("Error finding Outlook account by ID: {}", id, e);
            throw new RuntimeException("Database error occurred", e);
        }

        return Optional.empty();
    }

    public List<OutlookAccount> findAll() {
        String sql = """
            SELECT id, email, display_name, access_token, refresh_token, expires_at, added_by_admin_id
            FROM outlook_accounts
        """;

        List<OutlookAccount> accounts = new ArrayList<>();

        try (Connection conn = DatabaseUtil.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                accounts.add(mapResultSetToAccount(rs));
            }
        } catch (SQLException e) {
            logger.error("Error finding all Outlook accounts", e);
            throw new RuntimeException("Database error occurred", e);
        }

        return accounts;
    }

    public List<OutlookAccount> findByAdminId(Long adminId) {
        String sql = """
            SELECT id, email, display_name, access_token, refresh_token, expires_at, added_by_admin_id
            FROM outlook_accounts WHERE added_by_admin_id = ?
        """;

        List<OutlookAccount> accounts = new ArrayList<>();

        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, adminId);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    accounts.add(mapResultSetToAccount(rs));
                }
            }
        } catch (SQLException e) {
            logger.error("Error finding Outlook accounts by admin ID: {}", adminId, e);
            throw new RuntimeException("Database error occurred", e);
        }

        return accounts;
    }

    public void delete(Long id) {
        String sql = "DELETE FROM outlook_accounts WHERE id = ?";

        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, id);

            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected != 1) {
                throw new RuntimeException("Failed to delete Outlook account");
            }
        } catch (SQLException e) {
            logger.error("Error deleting Outlook account ID: {}", id, e);
            throw new RuntimeException("Database error occurred", e);
        }
    }

    private OutlookAccount mapResultSetToAccount(ResultSet rs) throws SQLException {
        OutlookAccount account = new OutlookAccount();
        account.setId(rs.getLong("id"));
        account.setEmail(rs.getString("email"));
        account.setDisplayName(rs.getString("display_name"));
        // Decrypt sensitive data when retrieving
        account.setAccessToken(EncryptionUtil.decrypt(rs.getString("access_token")));
        account.setRefreshToken(EncryptionUtil.decrypt(rs.getString("refresh_token")));
        account.setExpiresAt(rs.getTimestamp("expires_at").toLocalDateTime());
        account.setAddedByAdminId(rs.getLong("added_by_admin_id"));
        return account;
    }

    public List<OutlookAccount> findExpiredTokens() {
        String sql = """
            SELECT id, email, display_name, access_token, refresh_token, expires_at, added_by_admin_id
            FROM outlook_accounts 
            WHERE expires_at <= ?
        """;

        List<OutlookAccount> accounts = new ArrayList<>();

        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setTimestamp(1, Timestamp.valueOf(LocalDateTime.now()));

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    accounts.add(mapResultSetToAccount(rs));
                }
            }
        } catch (SQLException e) {
            logger.error("Error finding expired tokens", e);
            throw new RuntimeException("Database error occurred", e);
        }

        return accounts;
    }
}
