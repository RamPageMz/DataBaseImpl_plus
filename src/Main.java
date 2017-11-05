import org.omg.Messaging.SYNC_WITH_TRANSPORT;

import java.io.*;
import java.util.Arrays;
import java.util.Random;

public class Main {
    private static final int fileBufferSize = 4096;
    private static final int outBufferSize = 4096;
    private static final int recordNumber = 10000000;   //1000万行数据
    private static final int recordSize = 100;      //每行数据100字节
    private static final int fileNumber = 20;       //文件个数
    private static final int recordPerFile = 500000;        //每个文件的

    private static final String createFilePath = "./data/createFile/";
    private static final String mergeFilePath = "./data/mergeFile/";


    public static void main(String[] args) {
        System.out.println("Begin Database!");

        //createFile();

        //innerSort();

    }

    /**
     * Method Name : initialParameter
     * Description : 初始化常量
     * Parameter   :
     * Return      :
     * Date        : 2017/11/5 15:04
     */
    public static void initialParameter() {


    }

    /**
     * Method Name : createFile
     * Description : 按照要求生成20个数据文件
     * Parameter   :
     * Return      :
     * Date        : 2017/11/5 15:05
     */
    public static void createFile() {
        long startTime = System.currentTimeMillis();

        /*
        *   main body
        *   创建数据 写入一个string 当达到50MB的时候将其写入到文件中 清空buffer再写入
        *   50*1024*1024/100=500000 一次写入500000 就需要写入文件中
        * */
        for (int i = 0; i < fileNumber; i++) {
            createOnDisk(createFilePath + "data" + i + ".txt");
        }

        System.out.println("### 创建文件结束 ###");

        byte[] bytes = new byte[100];
        String content = new String(bytes);

        try {
            FileWriter out;

            BufferedWriter bufferedWriter;
            // 生成20个文件
            for (int m = 0; m < fileNumber; m++) {
                System.out.println("file" + m);
                // 打开文件写入流
                bufferedWriter = new BufferedWriter(new FileWriter(createFilePath + "data" + m + ".txt"));

                for (int i = 0; i < recordPerFile; i++) {
                    content = String.format("%098d", new Random().nextInt(recordNumber));
                    bufferedWriter.write(content);
                    bufferedWriter.newLine();
                }

                bufferedWriter.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }


        long endTime = System.currentTimeMillis();
        System.out.println("createFile : " + (endTime - startTime) / 1000 + " s");
    }

    /**
     * Method Name : createFileOn disk
     * Description : called by Function createFile     根据filename创建文件
     * Parameter   : filename content
     * Return      :
     * Date        : 2017/11/5 15:18
     */
    public static void createOnDisk(String filename) {
        File file = new File(filename);

        try {
            if (!file.exists()) {
                //文件不存在
                file.getParentFile().mkdirs();
                file.createNewFile();
                System.out.println("CreateFile success: " + filename);
            } else {
                System.out.println("CreateFile exists: " + filename);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Method Name : innerSort
     * Description : 将每个文件内部排序 升序
     * Parameter   :
     * Return      :
     * Date        : 2017/11/5 15:06
     */
    public static void innerSort() {
        long startTime = System.currentTimeMillis();

        /*
        *  main body
        * */
        int[] data = new int[500000];

        try {
            for (int j = 0; j < 20; j++) {
                String dataName = createFilePath + "data" + j + ".txt";

                FileReader fileReader = new FileReader(dataName);
                BufferedReader bufferedReader = new BufferedReader(fileReader);

                //创建100字节的数据
                byte[] bytes = new byte[100];
                String content = new String(bytes);

                String lineString = "";
                int i = 0;

                while ((lineString = bufferedReader.readLine()) != null) {
                    data[i] = Integer.parseInt(lineString);
                    i++;
                }

                bufferedReader.close();
                fileReader.close();

                Arrays.sort(data);

                FileWriter fileWriter = new FileWriter(dataName);
                BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);

                //写入文件
                for (int m = 0; m < 500000; m++) {
                    content = String.format("%098d", data[m]);
                    bufferedWriter.write(content);
                    bufferedWriter.newLine();
                }

                bufferedWriter.close();
                fileWriter.close();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }


        long endTime = System.currentTimeMillis();
        System.out.println("innerSort : " + (endTime - startTime) / 1000 + " s");
    }

    /**
     * Method Name : mergeFile
     * Description : 利用buffer缓冲区将文件合并为有序文件
     * Parameter   :
     * Return      :
     * Date        : 2017/11/5 15:07
     */
    public static void mergeFile() {
        long startTime = System.currentTimeMillis();

        /*
        *   定义文件buffer缓冲区和out缓冲区
        * */
        // 4KB的大小为4096字节 每行100字节 为40行数据
        String[] outBuffer = new String[40];
        int outBuffer_i = 0;

        // 20个文件各有一个文件缓冲区4KB大小 40行数据
        // 为了方便标识 转成int比较存储 第一个指数为文件下标 第二个指数为一次拉去的数据
        FileBuffer[] fileBuffers = new FileBuffer[20];

        for (int i = 0; i < 20; i++) {
            fileBuffers[i] = new FileBuffer(0, i);
        }

        //将数据放入fileBuffers
        for (int i = 0; i < fileNumber; i++) {
            try {
                String dataName = createFilePath + "data" + i + ".txt";
                FileReader fileReader = new FileReader(dataName);
                BufferedReader bufferedReader = new BufferedReader(fileReader);

                String lineString = "";
                int num = 0;

                //跳过已经读取过的行
                for (int line = 1; line <= fileBuffers[i].fileReadLine; line++) {
                    lineString = bufferedReader.readLine();
                }

                //读取数据 40行
                while ((lineString = bufferedReader.readLine()) != null && num < 40) {
                    fileBuffers[i].buffer[num] = lineString;
                    num++;
                }
                fileBuffers[i].fileReadLine += 40;     //记录下刚刚读了40行

                bufferedReader.close();
                fileReader.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        //将20 * 40的数据比较 写入 outBuffer  当outBuffer满载4kb时 outBuffer写入数据库 并清空自己 继续执行
        //取出每个buffer的第一个数据 将20个数据取最小放入outBuffer
        SortedData[] sortedData = new SortedData[fileNumber];
        for (int i = 0; i < fileNumber; i++) {
            //TODO 最后一个字符串怎么办 ""
            sortedData[i] = new SortedData(Integer.parseInt(fileBuffers[i].buffer[0]), i);
        }

        Arrays.sort(sortedData);

        //取出第一个也就是最小的放到outBuffer中 被取出的fileBuffer数组再从文件读取一个写入fileBuffer
        int dataMin = sortedData[0].data;
        int fileToAddLine = sortedData[0].fileNumber;

        //将最小值写入到OutBuffer
        outBuffer[outBuffer_i] = String.format("%098d", dataMin);
        outBuffer_i++;

        //从fileToAddLine再取一行补充到buffer中去
        String recordAdd = fileAddLineToBuffer(fileToAddLine, fileBuffers[fileToAddLine].fileReadLine);
        fileBuffers[fileToAddLine].fileReadLine += 1;       //该文件又多读了一行


        //将该文件缓冲区数据前移 将刚刚获取的数据放在最后
        for (int i = 0; i < fileBuffers[fileToAddLine].buffer.length - 1; i++) {
            fileBuffers[fileToAddLine].buffer[i] = fileBuffers[fileToAddLine].buffer[i + 1];//前移
        }
        if (recordAdd != "") {//还能读数据 否则就是读完了
            fileBuffers[fileToAddLine].buffer[fileBuffers[fileToAddLine].buffer.length - 1] = recordAdd;    //放在最后
        }else {
            //TODO 最后一个数据怎么办 字符串为空

        }



        long endTime = System.currentTimeMillis();
        System.out.println("mergeFile : " + (endTime - startTime) / 1000 + " s");
    }

    /**
     * Method Name : fileAddLineToBuffer
     * Description : 从指定文件中抽取一行返回给文件的buffer
     * Parameter   : fileNumber--文件编号      lineRead--该文件已经读取过的行数 接着往下读
     * Return      : 一行数据
     * Date        : 2017/11/5 21:21
     */
    public static String fileAddLineToBuffer(int fileNumber, int lineRead) {
        String result = "";
        try {
            String dataName = createFilePath + "data" + fileNumber + ".txt";
            FileReader fileReader = new FileReader(dataName);
            BufferedReader bufferedReader = new BufferedReader(fileReader);

            String lineString = "";
            int num = 0;

            for (int line = 1; line <= lineRead; line++) {
                lineString = bufferedReader.readLine();
            }

            while ((lineString = bufferedReader.readLine()) != null && num < 1) {
                result = lineString;
                num++;
            }

            bufferedReader.close();
            fileReader.close();

        } catch (Exception e) {
            e.printStackTrace();
        }

        return result;
    }

    /**
     * Method Name : showOrder
     * Description : 展示合成后文件的前100条数据以及后100条数据 证明有序
     * Parameter   :
     * Return      :
     * Date        : 2017/11/5 15:09
     */
    public static void showOrder() {
        long startTime = System.currentTimeMillis();

        /*
        *  main body
        * */


        long endTime = System.currentTimeMillis();
        System.out.println("showOrder : " + (endTime - startTime) / 1000 + " s");
    }

}
