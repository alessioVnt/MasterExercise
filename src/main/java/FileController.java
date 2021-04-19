import java.io.*;
import java.net.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class FileController{

    //Method used to download a file from a URL and to verify its checksum
    public boolean downloadAndCheck(URL fileUrl, URL checksumUrl, String filePath){

        //Download the zip file
        File file = new File(filePath);

        boolean isFileDownloaded = false;

        try {
            while (!isFileDownloaded){
                isFileDownloaded = downloadFileFromURL(fileUrl, file);
            }
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }

        //Generate MD5 checksum of downloaded file
        String checksum;
        try {
            checksum = checksum( file);
        } catch (IOException | NoSuchAlgorithmException e) {
            e.printStackTrace();
            return false;
        }

        // print out the checksum
        System.out.println("Downloaded file's checksum: " + checksum);


        //Download MD5 checksum from URL
        String downloadedChecksum = downloadCSFromURL(checksumUrl);
        if (downloadedChecksum != null) {
            System.out.println("File's checksum downloaded from URL: " + downloadedChecksum);
        } else {
            System.out.println("Error downloading checksum from URL");
            return false;
        }

        //Return whether the checksums correspond or not
        return checksum.equals(downloadedChecksum);
    }

    //Method that prints the files list in a zip archive
    public void printFilesList(String filePath){

        FileInputStream fis;
        ZipInputStream zipIs;
        ZipEntry zEntry;
        try {
            fis = new FileInputStream(filePath);
            zipIs = new ZipInputStream(new BufferedInputStream(fis));
            String dir = "";
            while((zEntry = zipIs.getNextEntry()) != null){
                if (zEntry.isDirectory()){
                    dir = zEntry.getName();
                    System.out.println("Root directory: " + dir);
                } else {
                    System.out.println(zEntry.getName().replace(dir, ""));
                }


            }
            zipIs.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //Method that downloads a file from the given URL, if the file exists but the download was not completed, it resumes the download
    private boolean downloadFileFromURL(URL url, File file) throws IOException {

        HttpURLConnection urlConnection;

        urlConnection = (HttpURLConnection) url.openConnection();
        urlConnection.connect();

        //Get the file size
        int fileSize = urlConnection.getContentLength();

        //Check whether the file exists
        if (file.exists()) {
            if (file.isDirectory())
                throw new IOException("File '" + file + "' is a directory");

            if (!file.canWrite())
                throw new IOException("File '" + file + "' cannot be written");
        } else {
            File parent = file.getParentFile();
            if ((parent != null) && (!parent.exists()) && (!parent.mkdirs())) {
                throw new IOException("File '" + file + "' could not be created");
            }
        }

        FileOutputStream output;
        float totalBytesRead;

        //Reinitialize connection
        urlConnection = (HttpURLConnection) url.openConnection();
        urlConnection.setReadTimeout(5000); //Set a read timeout of 5 seconds
        long outputFileSize = file.length();
        if (outputFileSize < fileSize) {
            //Used to resume download in case it was interrupted
            urlConnection.setRequestProperty(
                    "Range",
                    "bytes=" + outputFileSize + "-" + fileSize
            );
            output = new FileOutputStream(file, true);
            totalBytesRead = outputFileSize;
        } else {
            System.out.println("File already downloaded");
            return true;
        }

        InputStream input = urlConnection.getInputStream();


        byte[] buffer = new byte[8192];
        int n, bytesBuffered = 0;
        try {
            while (0 < (n = input.read(buffer))) {
                output.write(buffer, 0, n);
                totalBytesRead = totalBytesRead + n;
                System.out.println("Download completion: " + (totalBytesRead / fileSize) * 100 + "%");
                bytesBuffered += n;
                if (bytesBuffered > 1024 * 1024) { //flush after 1MB
                    bytesBuffered = 0;
                    output.flush();
                }
            }
        } catch (SocketTimeoutException e){
            e.printStackTrace();
            input.close();
            output.close();
            return false;
        }


        input.close();
        output.close();

        System.out.println("File '" + file + "' downloaded successfully!");

        return true;
    }

    //Method to download a checksum from a given URL
    private String downloadCSFromURL(URL url){
        String line = null;
        // Use try and catch to avoid the exceptions
        try {
            URLConnection urlConnection = url.openConnection(); // creating a urlconnection object

            // wrapping the urlconnection in a bufferedreader
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
            line = bufferedReader.readLine();
            bufferedReader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (line != null){
            return  line.substring(0, 32); //MD5 hashes are composed of 32 hexadecimal digits
        }
        return null;
    }

    //Method that computes a file's checksum using MD5 algorithm
    private String checksum( File file) throws IOException, NoSuchAlgorithmException {

        // instantiate a MessageDigest Object by passing
        // string "MD5" this means that this object will use
        // MD5 hashing algorithm to generate the checksum
        MessageDigest  digest = MessageDigest.getInstance("MD5");

        // Get file input stream for reading the file
        // content
        FileInputStream fis = new FileInputStream(file);

        // Create byte array to read data in chunks
        byte[] byteArray = new byte[1024];
        int bytesCount;

        // read the data from file and update that data in
        // the message digest
        while ((bytesCount = fis.read(byteArray)) != -1) {
            digest.update(byteArray, 0, bytesCount);
        }

        // close the input stream
        fis.close();

        // store the bytes returned by the digest() method
        byte[] bytes = digest.digest();

        // this array of bytes has bytes in decimal format
        // so we need to convert it into hexadecimal format

        // for this we create an object of StringBuilder
        // since it allows us to update the string i.e. its
        // mutable
        StringBuilder sb = new StringBuilder();

        // loop through the bytes array
        for (byte aByte : bytes) {

            // the following line converts the decimal into
            // hexadecimal format and appends that to the
            // StringBuilder object
            sb.append(Integer
                    .toString((aByte & 0xff) + 0x100, 16)
                    .substring(1));
        }

        // finally we return the complete hash
        return sb.toString();
    }
}
