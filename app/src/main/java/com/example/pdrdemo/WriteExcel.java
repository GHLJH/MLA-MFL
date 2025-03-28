package com.example.pdrdemo;

/**
 * Created by zbs on 2023.
 *  * 移动地理空间大数据云服务创新团队 www.dxkjs.com/
 */
import android.os.Environment;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;

public class WriteExcel {
    public static boolean writeList(List<Accelerate_info> list) throws IOException {
        //创建excel工作薄
        HSSFWorkbook workbook = new HSSFWorkbook();
        //创建一个工作表sheet
        HSSFSheet sheet = workbook.createSheet();

        //创建第一行  写列名
        HSSFRow row0 = sheet.createRow(0);

        HSSFCell cell1 = row0.createCell(0);
        cell1.setCellValue("采集时间");
        HSSFCell cell2 = row0.createCell(1);
        cell2.setCellValue("X");
        HSSFCell cell3 = row0.createCell(2);
        cell3.setCellValue("Y");
        HSSFCell cell9 = row0.createCell(3);
        cell9.setCellValue("Z");

        HSSFCell cell4 = row0.createCell(4);
        cell4.setCellValue("步长");
        HSSFCell cell5 = row0.createCell(5);
        cell5.setCellValue("步数");
        HSSFCell cell6 = row0.createCell(6);
        cell6.setCellValue("KF角度");
        HSSFCell cell7 = row0.createCell(7);
        cell7.setCellValue("EKF角度");
        HSSFCell cell8 = row0.createCell(8);
        cell8.setCellValue("常规角度");

        //循环给每一行写值
        for (int i = 0; i < list.size(); i++) {
            HSSFRow row1 = sheet.createRow(i+1);//参数为行数，为行号-1
            //创建单元格
            HSSFCell cell_1_1 = row1.createCell(0);//参数为列数，从0开始
            HSSFCell cell_1_2 = row1.createCell(1);
            cell_1_1.setCellValue(list.get(i)!=null?new SimpleDateFormat("HH:mm:ss:SSS").format(new Date(list.get(i).getTime())):"null");
//            cell_1_2.setCellValue(list.get(i)!=null?(list.get(i).getValue()+""):"null");
            cell_1_2.setCellValue(list.get(i)!=null?(list.get(i).x+""):"null");

            HSSFCell cell_1_3 = row1.createCell(2);
            cell_1_3.setCellValue(list.get(i)!=null?(list.get(i).y+""):"null");

            HSSFCell cell_1_9 = row1.createCell(3);
            cell_1_9.setCellValue(list.get(i)!=null?(list.get(i).z+""):"null");

            HSSFCell cell_1_4 = row1.createCell(4);
            cell_1_4.setCellValue(list.get(i)!=null?(list.get(i).StepLength+""):"null");

            HSSFCell cell_1_5 = row1.createCell(5);
            cell_1_5.setCellValue(list.get(i)!=null?(list.get(i).StepCount+""):"null");

            HSSFCell cell_1_6 = row1.createCell(6);
            cell_1_6.setCellValue(list.get(i)!=null?(list.get(i).KF_angle+""):"null");

            HSSFCell cell_1_7 = row1.createCell(7);
            cell_1_7.setCellValue(list.get(i)!=null?(list.get(i).EKF_angle+""):"null");

            HSSFCell cell_1_8 = row1.createCell(8);
            cell_1_8.setCellValue(list.get(i)!=null?(list.get(i).NormalAngle+""):"null");

        }

        String FILE_Name="PDR_";

        //保存为xls文件
        Date date=new Date();
        String time=date.toLocaleString();

        SimpleDateFormat dateFormat=new SimpleDateFormat("yyyyMMdd_HHmmss");
        String sim=dateFormat.format(date);//纯数字时间
        String filename=FILE_Name+sim+".xls";
        String folder= Environment.getExternalStorageDirectory().toString()+"/1PDR/";
        boolean isok=true;
        //判断是否存在该文件夹
        File fileFolder = new File(folder);
        if(!fileFolder.exists()){
            if (fileFolder.mkdir())
            {
                isok= true;
            }
            else
                isok= false;
        }
        String FilePath=folder+filename;
//        File file = new File(Environment.getExternalStorageDirectory()+"SensorDataCollector/"+filename);
        File file = new File(FilePath);

        if(!file.exists()){
            file.createNewFile();
        }
        FileOutputStream outputStream = new FileOutputStream(file);
        workbook.write(outputStream);//HSSFWorkbook自带写出文件的功能
        outputStream.close();
        System.out.println("写入成功");
        return true;

    }
}
