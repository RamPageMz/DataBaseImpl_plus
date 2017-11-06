public class FileBuffer {

    public int fileReadLine=0;  //记录该文件已经读取了多少行

    public String[] buffer=new String[40];   //该文件的buffer缓冲区有40行 每行100字节 共4000字节 略小于一个block（4096字节）

    public int fileNumber=0;    //文件编号

    public int bufferSize=0;    //缓冲池的大小

//    public FileBuffer(int fileReadLine, int fileNumber) {
//        this.fileReadLine = fileReadLine;
//        this.fileNumber = fileNumber;
//    }

    public FileBuffer(int fileReadLine, int fileNumber, int bufferSize) {
        this.fileReadLine = fileReadLine;
        this.fileNumber = fileNumber;
        this.bufferSize = bufferSize;
    }

    public int getFileReadLine() {
        return fileReadLine;
    }

    public void setFileReadLine(int fileReadLine) {
        this.fileReadLine = fileReadLine;
    }

    public String[] getBuffer() {
        return buffer;
    }

    public void setBuffer(String[] buffer) {
        this.buffer = buffer;
    }

    public int getFileNumber() {
        return fileNumber;
    }

    public void setFileNumber(int fileNumber) {
        this.fileNumber = fileNumber;
    }

    public int getBufferSize() {
        return bufferSize;
    }

    public void setBufferSize(int bufferSize) {
        this.bufferSize = bufferSize;
    }
}
