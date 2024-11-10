package its.incom.webdev.service;

import its.incom.webdev.persistence.model.Product;
import its.incom.webdev.persistence.repository.ProductRepository;
import its.incom.webdev.persistence.repository.SessionRepository;
import its.incom.webdev.persistence.repository.UserRepository;
import its.incom.webdev.service.exception.ExportDataException;
import its.incom.webdev.service.exception.SessionNotFoundException;
import its.incom.webdev.service.util.DataExport;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

@ApplicationScoped
public class DataExportService {

    private final DataExport dataExport;
    @Inject
    UserRepository userRepository;
    @Inject
    SessionRepository sessionRepository;
    @Inject
    ProductRepository productRepository;

    public DataExportService(DataExport dataExport){
        this.dataExport = dataExport;
    }

    public File exportProductsToExcel(String sessionId) throws SessionNotFoundException, ExportDataException{
        try{
            // Validate session and check admin privileges
            Integer userId = sessionRepository.findUserIdBySessionId(sessionId);
            if (userId == null) {
                throw new SessionNotFoundException("Please log in");
            }
            if (!userRepository.checkAdmin(userId)) {
                throw new SecurityException("Access denied");
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error validating session: " + e.getMessage());
        }

        try{
            String filePath = "Products.xlsx";
            List<Product> products = productRepository.getAll();
            dataExport.exportProductToExcel(products, filePath);

            File csvFile = new File(filePath);
            if(csvFile.exists()){
                return csvFile;
            } else{
                throw new RuntimeException("Failed to generate Excel file");
            }

        } catch (IOException e) {
            throw new ExportDataException("Error exporting Excel file: " + e.getMessage());
        }
    }
}
