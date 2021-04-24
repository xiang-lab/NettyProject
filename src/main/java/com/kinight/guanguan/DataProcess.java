package com.kinight.guanguan;

import com.csvreader.CsvReader;
import com.csvreader.CsvWriter;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.Charset;

public class DataProcess {

    public static int fileNumber = 10;      // 文件数
    public static int flowRateNumber = 100; // 流量数

    public static void main(String[] args) {

        while (fileNumber-- > 0) {
            String filePath = "C:\\Users\\xiang\\workspace\\guanguan\\data\\比对数据\\test" + fileNumber + ".csv";
            writeData(filePath);
        }
    }

    public static void read() {
        String filePath = "C:\\Users\\xiang\\Desktop\\test111111.csv";

        try {
            // 创建csv读对象
            CsvReader csvReader = new CsvReader(filePath);

            // 读表头
            csvReader.readHeaders();
            while (csvReader.readRecord()) {
                // 读一整行
                System.out.println(csvReader.getRawRecord());
                // 读这行的某一列
                System.out.println(csvReader.get("second"));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void write() {
        String filePath = "C:\\Users\\xiang\\Desktop\\test222.csv";

        try {
            CsvWriter csvWriter = new CsvWriter(filePath, ',', Charset.forName("GBK"));

            // 写表头
            String[] headers = {"编号", "姓名", "年龄"};
            String[] content = {"123456", "张三", "34"};

            csvWriter.writeRecord(headers);
            csvWriter.writeRecord(content);
            csvWriter.close();

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public static void writeData(String filePath) {
        try {
            CsvWriter csvWriter = new CsvWriter(filePath, ',', Charset.forName("GBK"));

            int count = flowRateNumber;
            while (count-- > 0) {
                String prefix = "0100001304";
                String suffix = "25";
                String data = generateData();
                String[] content = {prefix+data+suffix};
                csvWriter.writeRecord(content);
            }
            csvWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String generateData() {
//        String unknow = random(1);
        String unknow = "03";
        String commandClassId = random(1);
        String cmdSet = random(1);
        String data = random((int) (Math.random() * 2));
        return unknow + commandClassId + cmdSet + data;
    }

    public static String random(int num) {
        StringBuilder stringBuilder = new StringBuilder();
        while (num-- > 0) {
            int i = (int) (Math.random() * 255);
            System.out.println(i);
            String s = Integer.toHexString(i);
            System.out.println(s);
            if (s.length() == 1) {
                s = "0" + s;
            }
            s = s.toUpperCase();
            stringBuilder.append(s);
        }

        return stringBuilder.toString();
    }
}
