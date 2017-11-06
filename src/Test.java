import java.io.File;
import java.io.FileWriter;
import java.util.Arrays;
import java.util.Random;

public class Test {
    public static void main(String[] args) {
        SortedData[] sortedData = new SortedData[10];
        for (int i = 0; i < 10; i++) {
            sortedData[i] = new SortedData(new Random().nextInt(100), i);

            System.out.print(i + ":" + sortedData[i].data + " | ");
        }

        System.out.println();

        Arrays.sort(sortedData);

        System.out.println(sortedData[0].fileNumber);
//        for (int i = 0; i < 10; i++) {
//            System.out.print(sortedData[i].data+" | ");
//        }


        int[] a = new int[4];
        a[0] = 1;
        a[1] = 2;
        a[2] = 3;
        a[3] = 4;

        for (int i = 0; i < 4; i++) {
            System.out.print(a[i] + " | ");
        }

        System.out.println();
        for (int i = 0; i < 4 - 1; i++) {
            a[i] = a[i + 1];
        }
        //a[4-1]=6;

        for (int i = 0; i < 4; i++) {
            System.out.print(a[i] + " | ");
        }

        File file = new File("./data/mergeFile/data.txt");

        if (Integer.parseInt("-1") == -1){
            System.out.println("==-1");
        }

        int[] aa=new int[5];
        for (int i=0;i<5;i++){
            aa[i]=new Random().nextInt(10);
        }

        for (int i=0;i<5;i++){
            System.out.print(aa[i] + " | ");
        }

        Arrays.sort(aa,0,3);

        for (int i=0;i<5;i++){
            System.out.print(aa[i] + " | ");
        }
    }

}
