import java.net.MalformedURLException;
import java.net.URL;

/**
 * Write a program (you can choose to use the programming language you would prefer or in pseudo code)
 * which performs the following tasks:
 * 1- Download the Movielens datasets from the url ‘http://files.grouplens.org/datasets/movielens/ml-
 * 25m.zip’
 * 2- Download the Movielens checksum from the url ‘http://files.grouplens.org/datasets/movielens/ml-
 * 25m.zip.md5’
 * 3- Check whether the checksum of the archive corresponds to the downloaded one
 * 4- In case of positive check, print the names of the files contained by the downloaded archive
 */
public class Main {

    public static void main(String[] args) {

        //URL to download the file
        String sFileUrl = "http://files.grouplens.org/datasets/movielens/ml-25m.zip";

        URL fileUrl;
        try {
            fileUrl = new URL(sFileUrl);
        } catch (MalformedURLException e) {
            e.printStackTrace();
            return;
        }

        //URL to download checksum
        String sCsUrl = "http://files.grouplens.org/datasets/movielens/ml-25m.zip.md5";

        URL csUrl;
        try {
            csUrl = new URL(sCsUrl);
        } catch (MalformedURLException e) {
            e.printStackTrace();
            return;
        }

        String filePath = "./out/exercise.zip";

        FileController fileController = new FileController();

        if (fileController.downloadAndCheck(fileUrl, csUrl, filePath)) {
            System.out.println("Checksums correspond, printing archive's files list..");
            fileController.printFilesList(filePath);
        } else {
            System.out.println("Error downloading file");
        }
    }
}
