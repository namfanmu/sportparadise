package vn.fs.dto;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.List;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import lombok.Data;
import vn.fs.entities.Order;
import vn.fs.entities.OrderDetail;

@Data
public class OrderExcelExporter {
	
	private XSSFWorkbook workbook;
	private XSSFSheet sheet;

	private List<Order> listOrDetails;
	private List<OrderDetail> listOrderDetails;

	
	public OrderExcelExporter(List<OrderDetail> listOrderDetails) {
		this.listOrderDetails = listOrderDetails;
		workbook = new XSSFWorkbook();
		sheet = workbook.createSheet("OrderDetails");
	}

	private void writeHeaderRow() {

		Row row = sheet.createRow(0);

		Cell cell = row.createCell(0);
		cell.setCellValue("Mã đơn hàng");
		
		cell = row.createCell(1);
		cell.setCellValue("Tên khách hàng");
		
		cell = row.createCell(2);
		cell.setCellValue("SĐT");
		
		cell = row.createCell(3);
		cell.setCellValue("Địa chỉ");
		
		cell = row.createCell(4);
		cell.setCellValue("Ngày đặt");
		
		cell = row.createCell(5);
		cell.setCellValue("Tên sản phẩm");
		
		cell = row.createCell(6);
		cell.setCellValue("Size");
		
		cell = row.createCell(7);
		cell.setCellValue("Số lượng");
		
		cell = row.createCell(8);
		cell.setCellValue("Tổng tiền đơn hàng");

	}
	
	private void writeDataRows() {
		int rowCount = 1;
		for (OrderDetail orderDetail : listOrderDetails) {
			Row row = sheet.createRow(rowCount++);

			Cell cell = row.createCell(0);
			cell.setCellValue(orderDetail.getOrder().getOrderId());
			
			cell = row.createCell(1);
			cell.setCellValue(orderDetail.getOrder().getUser().getName());
			
			cell = row.createCell(2);
			cell.setCellValue(orderDetail.getOrder().getPhone());

			cell = row.createCell(3);
			cell.setCellValue(orderDetail.getOrder().getAddress());
			
			DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");  
			cell = row.createCell(4);
			cell.setCellValue(dateFormat.format(orderDetail.getOrder().getOrderDate()));
			
			cell = row.createCell(5);
			cell.setCellValue(orderDetail.getProduct().getProductName());
			
			cell = row.createCell(6);
			cell.setCellValue(orderDetail.getSize());
			
			cell = row.createCell(7);
			cell.setCellValue(orderDetail.getQuantity());
			
			cell = row.createCell(8);
			cell.setCellValue(orderDetail.getOrder().getAmount());

		}

	}
	
	public void export(HttpServletResponse response) throws IOException {

		writeHeaderRow();
		writeDataRows();

		ServletOutputStream outputStream = response.getOutputStream();
		workbook.write(outputStream);
		workbook.close();
		outputStream.close();

	}

}
