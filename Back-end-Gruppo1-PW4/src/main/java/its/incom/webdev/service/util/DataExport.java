package its.incom.webdev.service.util;

import its.incom.webdev.persistence.model.Product;
import jakarta.enterprise.context.ApplicationScoped;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

@ApplicationScoped
public class DataExport {

    public void exportProductToExcel(List<Product> products, String filePath) throws IOException{
        try(Workbook workbook = new XSSFWorkbook()){
            Sheet sheet = workbook.createSheet("Products");

            Row header = sheet.createRow(0);
            String[] columns = {"Id", "Name", "Quantity", "Price"};

            for(int i = 0; i < columns.length; i++){
                Cell cell = header.createCell(i);
                cell.setCellValue(columns[i]);
            }

            int rowNum = 1;
            for (Product p : products){
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(p.getId());
                row.createCell(1).setCellValue(p.getProductName());
                row.createCell(2).setCellValue(p.getQuantity());
                row.createCell(3).setCellValue(p.getPrice());
            }

            for (int i = 0; i < columns.length; i++) {
                sheet.autoSizeColumn(i);
            }

            try(FileOutputStream fileOut = new FileOutputStream(filePath)){
                workbook.write(fileOut);
            }
        }
    }
}
