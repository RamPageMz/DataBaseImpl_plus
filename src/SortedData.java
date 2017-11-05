public class SortedData implements Comparable<SortedData>{

    public int data;    //整型数据

    public int fileNumber;      //来自于文件编号

    public SortedData(int data, int fileNumber) {
        this.data = data;
        this.fileNumber = fileNumber;
    }

    @Override
    public int compareTo(SortedData o) {
        if(this.getData()>o.getData()){
            return 1;
        }else if(this.getData()<o.getData()){
            return -1;
        }else{
            return 0;
        }
    }

    public int getData() {
        return data;
    }

    public void setData(int data) {
        this.data = data;
    }

    public int getFileNumber() {
        return fileNumber;
    }

    public void setFileNumber(int fileNumber) {
        this.fileNumber = fileNumber;
    }
}
