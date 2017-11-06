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

    public static String[] outBuffer = new String[40];
    public static FileBuffer[] fileBuffers = new FileBuffer[20];
    public static int outBuffer_i = 0;

    //public static FileReader[] fileReaders = new FileReader[fileNumber];
    public static BufferedReader[] bufferedReaders = new BufferedReader[fileNumber];


    public static void main(String[] args) {
        System.out.println("Begin Database!");

        //createFile();

        //innerSort();

        mergeFile();

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

        Boolean end = false;

        /*
        *   定义文件buffer缓冲区和out缓冲区
        * */
        // 4KB的大小为4096字节 每行100字节 为40行数据
        // TODO 改为全局变量
        //String[] outBuffer = new String[40];
        outBuffer_i = 0;

        // 20个文件各有一个文件缓冲区4KB大小 40行数据
        // 为了方便标识 转成int比较存储 第一个指数为文件下标 第二个指数为一次拉去的数据
        //TODO 改为全局变量
        //FileBuffer[] fileBuffers = new FileBuffer[20];
        try {
            //文件写入流打开
            FileWriter fw = new FileWriter(mergeFilePath + "data.txt", true);

            //文件读入流打开
            for (int i = 0; i < fileNumber; i++) {
                bufferedReaders[i] = new BufferedReader(new FileReader(createFilePath + "data" + i + ".txt"));
            }

            for (int i = 0; i < 20; i++) {
                fileBuffers[i] = new FileBuffer(0, i, 0);
            }

            //将数据放入fileBuffers 20个文件
            for (int i = 0; i < fileNumber; i++) {
                fileAdd40Line(i);
            }

            //System.out.println("第一组数据放入完成");

            while (!end) {
                //将20 * 40的数据比较 写入 outBuffer  当outBuffer满载4kb时 outBuffer写入数据库 并清空自己 继续执行
                //取出每个buffer的第一个数据 将20个数据取最小放入outBuffer
                SortedData[] sortedData = new SortedData[fileNumber];
                int sortData_i = 0;
                Boolean allFinish = true;

                for (int i = 0; i < fileNumber; i++) {
                    if (Integer.parseInt(fileBuffers[i].buffer[0]) != -1) {      // -1表示该文件已经比较完  不用再比较
                        //有数据 拉出比较
                        sortedData[sortData_i] = new SortedData(Integer.parseInt(fileBuffers[i].buffer[0]), i);
                        sortData_i++;
                        allFinish = false;    //不等于-1 即 还有数据要处理 没有全部完成
                    } else {
                        // =-1 即该文件读完了
                    }
                }

                //TODO if allFinish=True 结束  否则继续下面的操作

                if (allFinish == false) {
                    // 有数据需要比较
                    Arrays.sort(sortedData,0,sortData_i);

                    //取出第一个也就是最小的放到outBuffer中
                    int dataMin = sortedData[0].data;
                    int fileToAddLine = sortedData[0].fileNumber;
                    //将最小值写入到OutBuffer
                    //System.out.println("outBuffer_i:"+outBuffer_i);
                    outBuffer[outBuffer_i] = String.format("%098d", dataMin);
                    outBuffer_i++;      //outBuffer下标加1

                    //将该文件缓冲区数据前移
                    for (int i = 0; i < fileBuffers[fileToAddLine].bufferSize - 1; i++) {
                        fileBuffers[fileToAddLine].buffer[i] = fileBuffers[fileToAddLine].buffer[i + 1];//前移
                    }
                    fileBuffers[fileToAddLine].bufferSize -= 1;     //缓冲池大小-1

                    //判断是否fileBuffers是否为空 outBuffer是否满载
                    if (fileBuffers[fileToAddLine].bufferSize == 0 && fileBuffers[fileToAddLine].fileReadLine < 500000) {      //该文件的缓冲区为0  需要重新加载数据
                        fileAdd40Line(fileToAddLine);
                    }
                    if (fileBuffers[fileToAddLine].bufferSize == 0 && fileBuffers[fileToAddLine].fileReadLine == 500000) {   //该文件已经全部读完
                        //buffer第一个元素 设为-1  在sortdata时 跳过该buffer
                        fileBuffers[fileToAddLine].bufferSize = 1;
                        fileBuffers[fileToAddLine].buffer[0] = "-1";
                        System.out.println("file: "+fileToAddLine+" has done  -1");
                    }

                    if (outBuffer_i == 40) {       //如果outBuffer有40行数据 将其写入到merge文件 并清空
                        //outBufferToFile(40);
                        StringBuffer stringBuffer=new StringBuffer();
                        //String content = "";
                        for (int i = 0; i < 40; i++) {
                            //content += outBuffer[i];
                            //content += "\r\n";
                            stringBuffer.append(outBuffer[i]+'\n');
                        }
                        fw.write(stringBuffer.toString());

                        //清空outBuffer缓冲区
                        //outBuffer = new String[40];
                        outBuffer_i = 0;
                    }
                } else {
                    System.out.println("all -1");
                    // 所有文件的buffer都是-1 没有新数据加载 直接将outBuffer全部写入到文件中
                    //outBufferToFile(outBuffer_i);
                    StringBuffer stringBuffer=new StringBuffer();
                    //String content = "";
                    for (int i = 0; i < 40; i++) {
                        //content += outBuffer[i];
                        //content += "\r\n";
                        stringBuffer.append(outBuffer[i]+'\n');
                    }
                    fw.write(stringBuffer.toString());
                    // TODO 全部结束！！！
                    end = true;
                }
            }

            //关闭流
            fw.close();

            for (int i = 0; i < fileNumber; i++) {
                bufferedReaders[i].close();
                //fileReaders[i].close();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        long endTime = System.currentTimeMillis();
        System.out.println("mergeFile : " + (endTime - startTime) / 1000 + " s");
    }

    /**
     * Method Name : outBufferToFile
     * Description : 将outBuffer中的数据传输到file中去
     * Parameter   :
     * Return      :
     * Date        : 2017/11/6 11:19
     */
    public static void outBufferToFile(int size) {
        //写入文件
        try {
            FileWriter fw = new FileWriter(mergeFilePath + "data.txt", true);
            String content = "";

            for (int i = 0; i < size; i++) {
                content += outBuffer[i];
                content += "\r\n";
            }

            fw.write(content);
            fw.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        //清空outBuffer缓冲区
        outBuffer = new String[40];
        outBuffer_i = 0;

    }

    /**
     * Method Name : fileAddLineToBuffer
     * Description : 从file中抽取40行到buffer中
     * Parameter   : fileNumber--文件编号
     * Return      :
     * Date        : 2017/11/6 10:51
     */
    public static void fileAdd40Line(int i) {
        try {
            //String dataName = createFilePath + "data" + i + ".txt";
            //FileReader fileReader = new FileReader(dataName);
            //BufferedReader bufferedReader = new BufferedReader(fileReader);

            String lineString = "";
            int num = 0;

            //跳过已经读取过的行
            for (int line = 1; line <= fileBuffers[i].fileReadLine; line++) {
                lineString = bufferedReaders[i].readLine();
            }

            //读取数据 40行
            while ((lineString = bufferedReaders[i].readLine()) != null && num < 40 && fileBuffers[i].fileReadLine < 500000) {
                fileBuffers[i].buffer[num] = lineString;
                num++;
                fileBuffers[i].fileReadLine += 1;   //刚刚读了一行
            }
            //fileBuffers[i].fileReadLine += 40;     //记录下刚刚读了40行
            fileBuffers[i].bufferSize = num;         //缓冲区有40行数据

            //bufferedReader.close();
            //fileReader.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
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
