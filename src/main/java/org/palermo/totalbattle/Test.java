package org.palermo.totalbattle;

import org.palermo.totalbattle.dao.ConnectionHelper;
import org.palermo.totalbattle.dao.ImageDao;

import java.sql.Connection;

public class Test {

    public static void main(String[] args) {
        Connection conn = ConnectionHelper.getConnection();
        ImageDao imageDao = new ImageDao(conn);
    }
}
