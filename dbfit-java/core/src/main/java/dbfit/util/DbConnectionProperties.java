package dbfit.util;

import dbfit.util.crypto.CryptoServiceFactory;
import dbfit.util.crypto.CryptoService;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class DbConnectionProperties {

    public String Service;
    public String Username;
    public String Password;
    public String DbName;
    public String FullConnectionString;

    private DbConnectionProperties() {
    }

    /**
     * Unwrap encrypted form password "ENC(password)".
     *
     * @return password text if wrapped in ENC(pass)
     *         null if format is not ENC(...)
     */
    public static String unwrapEncryptedPassword(String encPwd) {
        String encryptedFormPrefix = "ENC(";
        if (encPwd.startsWith(encryptedFormPrefix)) {
            return encPwd.substring(encryptedFormPrefix.length(),
                                        encPwd.length() - 1);
        } else {
            return null;
        }
    }

    public static String parsePassword(String pwd) {
        String encPwd = unwrapEncryptedPassword(pwd);

        if (encPwd == null) {
            return pwd;
        } else {
            CryptoService crypto = CryptoServiceFactory.getCryptoService();
            return crypto.decrypt(encPwd);
        }
    }

    public static String[] splitKeyVal(String line) {
        return line.split("=");
    }

    public static DbConnectionProperties CreateFromString(
            java.util.List<String> lines) {
        int currLine = 0;
        DbConnectionProperties props = new DbConnectionProperties();
        for (String line : lines) {
            currLine++;
            if (line == null)
                continue;
            String trimline = line.trim();
            if (trimline.length() == 0)
                continue;
            if (trimline.startsWith("#"))
                continue;
            String[] keyval = splitKeyVal(trimline);
            String key, val;

            if (keyval.length == 1) {
                key = keyval[0].trim().toLowerCase();
                val = "";
            } else {
                key = keyval[0].trim().toLowerCase();
                val = keyval[1].trim();
            }
            if ("username".equals(key)) {
                props.Username = val;
            } else if ("password".equals(key)) {
                props.Password = parsePassword(val);
            } else if ("service".equals(key)) {
                props.Service = val;
            } else if ("database".equals(key)) {
                props.DbName = val;
            } else if ("connection-string".equals(key)) {
                props.FullConnectionString = val;
            } else {
                throw new UnsupportedOperationException(
                        "Unsupported key in properties file:" + key);
            }

        }
        if (props.FullConnectionString != null)
            return props;
        if (props.Service != null && props.Username != null
                && props.Password != null)
            return props;
        throw new Error(
                "You have to define either the full connection string; or service, username and password in the properties file");
    }

    public static DbConnectionProperties CreateFromFile(String path)
            throws FileNotFoundException, IOException {
        File config = new File(path);
        BufferedReader br = new BufferedReader(new FileReader(config));
        List<String> lines = new ArrayList<String>();
        String line;
        while ((line = br.readLine()) != null) {
            lines.add(line);
        }
        return CreateFromString(lines);
    }
}

