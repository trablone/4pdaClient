package org.softeg.slartus.forpda.classes.common;

import java.io.*;
import java.net.URLDecoder;

/**
 * User: slinkin
 * Date: 09.11.11
 * Time: 7:31
 */
public class FileUtils {
    public static byte[] toByteArray(File file) throws IOException {

        InputStream input_stream = new BufferedInputStream(new FileInputStream(file));
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        byte[] data = new byte[16384]; // 16K
        int bytes_read;
        while ((bytes_read = input_stream.read(data, 0, data.length)) != -1) {
            buffer.write(data, 0, bytes_read);
        }
        input_stream.close();
        return buffer.toByteArray();
    }

    public static void CopyStream(InputStream is, OutputStream os) {
        final int buffer_size = 1024;
        try {
            byte[] bytes = new byte[buffer_size];
            for (; ; ) {
                int count = is.read(bytes, 0, buffer_size);
                if (count == -1)
                    break;
                os.write(bytes, 0, count);
            }
        } catch (Exception ex) {
        }
    }

    private static final char[] ILLEGAL_CHARACTERS = {'/', '\n', '\r', '\t', '\0', '\f', '`', '?', '*', '\\', '<', '>', '|', '\"', ':'};

    /*
    * Нормализует(уберает иллегальные символы)
     */
    public static String normalize(String fileName) {
        for (char illegalChar : ILLEGAL_CHARACTERS) {
            fileName = fileName.replace(illegalChar, '_');
        }
        return fileName;
    }

    public static String getFileNameFromUrl(String url) {
        String decodedUrl = URLDecoder.decode(url);
        int index = decodedUrl.lastIndexOf("/");

        return normalize(decodedUrl.substring(index + 1, decodedUrl.length()));
    }

    public static String getDirPath(String filePath) {

        return filePath.substring(0, filePath.lastIndexOf(File.separator));
    }

    public static String fileExt(String url) {
        String ext = url.substring(url.lastIndexOf("."));
        if (ext.indexOf("?") > -1) {
            ext = ext.substring(0, ext.indexOf("?"));
        }
        if (ext.indexOf("%") > -1) {
            ext = ext.substring(0, ext.indexOf("%"));
        }
        return ext;
    }

    public static String combine(String path1, String path2) {

        if (!path1.endsWith(File.separator))
            path1 += File.separator;
        return path1 + path2;
    }

    public static String getUniqueFilePath(String dirPath, String fileName) {
        String name = fileName;
        String ext = "";
        int ind = fileName.lastIndexOf(".");
        if (ind != -1) {
            name = fileName.substring(0, ind);
            ext = fileName.substring(ind, fileName.length());
        }
        if (!dirPath.endsWith(File.separator))
            dirPath += File.separator;
        String suffix = "";
        int c = 0;
        while (new File(dirPath + name + suffix + ext).exists() || new File(dirPath + name + suffix + ext + "_download").exists()) {
            suffix = "(" + c + ")";
            c++;
        }
        return dirPath + name + suffix + ext;
    }

    public static Boolean mkDirs(String filePath) {
        //int startind=1;
        String dirPath = new File(filePath).getParentFile().getAbsolutePath() + File.separator;

        File dir = new File(dirPath.replace("/", File.separator));
        return dir.exists() || dir.mkdirs();
//        while(true){
//             if(startind>=dirPath.length()||startind==-1)
//                return true;
//            int slashInd=dirPath.indexOf(File.separator,startind);
//            if(slashInd==-1)return true;
//            String subPath=dirPath.substring(0,slashInd);
//            File f=new File(subPath);
//            if(!f.exists()&&!f.mkdir()){
//                return false;
//            }
//            startind=subPath.length()+1;
//
//        }

    }
}
