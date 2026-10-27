
package com.remoteconsultation.service;

import com.remoteconsultation.config.JdbcTransactionManager;
import com.remoteconsultation.dao.ProfessionalProfileDao;
import com.remoteconsultation.model.ProfessionalDirectoryItem;
import org.springframework.stereotype.Service;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

@Service
public class ProfessionalDirectoryService {

    private final ProfessionalProfileDao professionalProfileDao;
    private final JdbcTransactionManager transactionManager;

    public ProfessionalDirectoryService(
            ProfessionalProfileDao professionalProfileDao,
            JdbcTransactionManager transactionManager) {

        this.professionalProfileDao = professionalProfileDao;
        this.transactionManager = transactionManager;
    }

    public List<ProfessionalDirectoryItem> getApprovedProfessionals()
            throws SQLException {

        Connection connection = null;

        try {
            connection = transactionManager.getConnection();

            return professionalProfileDao.findApprovedProfessionals(
                    connection
            );

        } finally {
            if (connection != null) {
                transactionManager.close(connection);
            }
        }
    }
}
