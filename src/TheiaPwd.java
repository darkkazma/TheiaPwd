import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.regex.Pattern;

public class TheiaPwd {

    private Scanner scanner = null;
    private ArrayList<USER> UserLIST = new ArrayList<USER>();

    public static void main(String[] args){ new TheiaPwd(); }

    public TheiaPwd(){
        this.scanner = new Scanner(System.in);
        boolean process = true;
        try {
            while (process) {
                PrintMainMessage();
                String command = this.scanner.nextLine();
                int command_ = 0;
                try {
                    command_ = Integer.parseInt(command);
                } catch (NumberFormatException ne) {
                    command_ = -1;
                }
                try {
                    switch (command_) {
                        case 1:
                            PrintUserInfo();
                            continue;
                        case 2:
                            UserAdd();
                            continue;
                        case 3:
                            UserMod();
                            continue;
                        case 4:
                            UserDel();
                            continue;
                        case 5:
                            UserUnLock();
                            continue;
                        case 6:
                            Encryption();
                            continue;
                        case 7:
                            Decryption();
                            continue;
                        case 0:
                            process = false;
                            continue;
                    }
                    System.err.println("Wrong Command input.");
                } catch (Exception ea) {
                    System.out.println("");
                }
            }
        } catch (Exception ea) {
            ea.printStackTrace();
        }
    }

    private void UserUnLock() throws Exception {
        readUserInfo();
        System.out.print("Input UserID for Unlock : ");
        String UserID = this.scanner.nextLine().trim();
        System.out.println("");
        if (UserID != null && UserID.length() > 0) {
            DBConnectionMgr db = DBConnectionMgr.getInstance();
            Connection con = null;
            PreparedStatement pstmt = null;
            ResultSet rs = null;
            try {
                con = db.getConnection();
                String query = "UPDATE USERINFO SET IS_ACCOUNT_LOCK = 'N', LOGIN_FAIL_COUNT = 0 WHERE USERID = ?";
                pstmt = con.prepareStatement(query);
                pstmt.setString(1, UserID);
                pstmt.executeUpdate();
                System.out.println("User unlock success...");
                System.out.println("");
            } catch (Exception ea) {
                System.err.println("User unlock failed..." + ea.toString());
                throw ea;
            } finally {
                try {
                    if (db != null)
                        db.freeConnection(con, pstmt, rs);
                } catch (Exception exception) {

                } finally {
                    db = null;
                }
            }
        }
    }

    private void UserDel() throws Exception {
        readUserInfo();
        System.out.print("Input UserID for DELETE : ");
        String UserID = this.scanner.nextLine().trim();
        System.out.println("");
        if (UserID != null && UserID.length() > 0) {
            DBConnectionMgr db = DBConnectionMgr.getInstance();
            Connection con = null;
            PreparedStatement pstmt = null;
            try {
                con = db.getConnection();
                pstmt = con.prepareStatement("DELETE FROM USERINFO WHERE USERID=?");
                pstmt.setString(1, UserID);
                pstmt.executeUpdate();
                System.out.println("");
                System.out.println("");
                pstmt.close();
                pstmt = null;
                System.out.println("User delete success...");
                System.out.println("");
            } catch (Exception ea) {
                System.err.println("User delete failed..." + ea.toString());
                throw ea;
            } finally {
                try {
                    if (db != null)
                        db.freeConnection(con, pstmt);
                } catch (Exception exception) {

                } finally {
                    db = null;
                }
            }
        }
    }

    public void UserMod() throws Exception {
        readUserInfo();
        System.out.print("Input UserID for Modify : ");
        String UserID = this.scanner.nextLine().trim();
        System.out.println("");
        if (UserID != null && UserID.length() > 0) {
            String UserPWD = null;
            String UserLevel = null;
            String UserEmail = null;
            boolean find = false;
            DBConnectionMgr db = DBConnectionMgr.getInstance();
            Connection con = null;
            PreparedStatement pstmt = null;
            ResultSet rs = null;
            try {
                con = db.getConnection();
                pstmt = con.prepareStatement("SELECT * FROM USERINFO WHERE USERID=?");
                pstmt.setString(1, UserID);
                rs = pstmt.executeQuery();
                while (rs.next()) {
                    UserPWD = rs.getString("PASSWORD");
                    UserLevel = String.valueOf(rs.getInt("AUTHOR"));
                    UserEmail = rs.getString("USERMAIL");
                    find = true;
                }
                System.out.println("");
                System.out.println("");
                rs.close();
                rs = null;
                pstmt.close();
                pstmt = null;
            } catch (Exception ea) {
                throw ea;
            } finally {
                try {
                    if (db != null)
                        db.freeConnection(con, pstmt, rs);
                } catch (Exception exception) {

                } finally {
                    db = null;
                }
            }
            if (find) {
                System.out.print("Enter User New Password (default [" + DESUtil.decrypt(UserPWD) + "]) : ");
                String newPWD = this.scanner.nextLine().trim();
                if (newPWD != null && newPWD.length() == 0) {
                    newPWD = DESUtil.decrypt(UserPWD);
                } else {
                    String pwdPattern = "^(?=.*\\d)(?=.*[~`!@#$%\\^&*()-])(?=.*[a-z])(?=.*[A-Z]).{9,}$";
                    boolean tt = Pattern.matches(pwdPattern, newPWD);
                    while (!tt) {
                        System.err.println("Password must be combination of eng + num + special char with at least 9digits.");
                        System.out.println("");
                        System.out.print("Enter User New Password : ");
                        newPWD = this.scanner.nextLine().trim();
                        if (newPWD != null && newPWD.length() == 0) {
                            newPWD = DESUtil.decrypt(UserPWD);
                            tt = true;
                            continue;
                        }
                        tt = Pattern.matches(pwdPattern, newPWD);
                    }
                }
                System.out.print("Enter User New Level (2: Manager, 1: User , default [" + UserLevel + "]) ");
                String newLevel = this.scanner.nextLine().trim();
                if (newLevel != null && newLevel.length() == 0)
                    newLevel = UserLevel;
                try {
                    Integer.parseInt(newLevel);
                } catch (Exception ea) {
                    newLevel = UserLevel;
                }
                System.out.print("Enter User New Email : ");
                String newEmail = this.scanner.nextLine().trim();
                if (newEmail != null && newEmail.length() == 0)
                    newEmail = UserEmail;
                System.out.print("newPWD : " + newPWD + " | newLevel : " + (newLevel.equals("1") ? "User" : "Manager") + " | UserMail : " + newEmail + "  y or n (default y)");
                String yesorno = this.scanner.nextLine().trim();
                if (yesorno != null && yesorno.length() == 0)
                    yesorno = "y";
                if (yesorno.equalsIgnoreCase("y")) {
                    try {
                        db = DBConnectionMgr.getInstance();
                        con = db.getConnection();
                        String query = "UPDATE USERINFO SET PASSWORD=?, AUTHOR=?, USERMAIL=? WHERE USERID=?";
                        pstmt = con.prepareStatement(query);
                        pstmt.setString(1, DESUtil.encrypt(newPWD));
                        pstmt.setInt(2, Integer.parseInt(newLevel));
                        pstmt.setString(3, UserEmail);
                        pstmt.setString(4, UserID);
                        pstmt.executeUpdate();
                        System.out.println("User modify success..");
                        System.out.println("");
                    } catch (Exception ea) {
                        System.out.println("User modify failed.." + ea.toString());
                        System.out.println("");
                        throw ea;
                    } finally {
                        try {
                            if (db != null)
                                db.freeConnection(con, pstmt, rs);
                        } catch (Exception exception) {

                        } finally {
                            db = null;
                        }
                    }
                } else {
                    throw new Exception();
                }
            } else {
                System.err.println("Not Found User. [" + UserID + "]");
                throw new Exception();
            }
        }
    }

    public void Encryption() throws Exception {
        System.out.print("Input normal text : ");
        String text = this.scanner.nextLine().trim();
        System.out.println("Encryption => [" + DESUtil.encrypt(text) + "]");
        System.out.println("");
    }

    public void Decryption() throws Exception {
        System.out.print("Input encryption text : ");
        String text = this.scanner.nextLine().trim();
        System.out.println("Decryption => [" + DESUtil.decrypt(text) + "]");
        System.out.println("");
    }

    public void UserAdd() throws Exception {
        readUserInfo();
        String UserID = null;
        String UserPWD = null;
        String UserLevel = null;
        String UserEmail = null;
        System.out.print("Input User ID : ");
        UserID = this.scanner.nextLine().trim();
        for (USER user : this.UserLIST) {
            if (user.getUSERID().equals(UserID)) {
                System.err.println("Duplicate UserID");
                throw new Exception();
            }
        }
        System.out.print("Input User PWD : ");
        UserPWD = this.scanner.nextLine().trim();
        String pwdPattern = "^(?=.*\\d)(?=.*[~`!@#$%\\^&*()-])(?=.*[a-z])(?=.*[A-Z]).{9,}$";
        boolean tt = Pattern.matches(pwdPattern, UserPWD);
        while (!tt) {
            System.err.println("Password must be combination of eng + num + special char with at least 9digits.");
            System.out.println("");
            System.out.print("Input User PWD : ");
            UserPWD = this.scanner.nextLine().trim();
            tt = Pattern.matches(pwdPattern, UserPWD);
        }
        System.out.print("Input User level (2: Manager, 1: User) : ");
        UserLevel = this.scanner.nextLine().trim();
        int level = 0;
        try {
            level = Integer.parseInt(UserLevel);
        } catch (NumberFormatException ne) {
            level = 1;
        }
        if (level < 0 || level > 2)
            level = 1;
        System.out.print("Input User Email : ");
        UserEmail = this.scanner.nextLine().trim();
        System.out.print("UserID : " + UserID + " | UserPWD : " + UserPWD + " | UserLevel : " + ((level == 1) ? "User" : "Manager") + " | UserMail : " + UserEmail + "  y or n (default y)");
        String yesorno = this.scanner.nextLine().trim();
        if (yesorno != null && yesorno.length() == 0)
            yesorno = "y";
        if (yesorno.equalsIgnoreCase("y")) {
            DBConnectionMgr db = DBConnectionMgr.getInstance();
            Connection con = null;
            PreparedStatement pstmt = null;
            ResultSet rs = null;
            try {
                con = db.getConnection();
                String query = "INSERT INTO USERINFO (USERID, USERNAME, PASSWORD, AUTHOR, USERMAIL) VALUES (?,?,?,?,?)";
                pstmt = con.prepareStatement(query);
                pstmt.setString(1, UserID);
                pstmt.setString(2, "CMD_USER");
                pstmt.setString(3, DESUtil.encrypt(UserPWD));
                pstmt.setInt(4, level);
                pstmt.setString(5, UserEmail);
                pstmt.executeUpdate();
                System.out.println("User add success..");
                System.out.println("");
            } catch (Exception ea) {
                System.out.println("User add failed.." + ea.toString());
                System.out.println("");
                throw ea;
            } finally {
                try {
                    if (db != null)
                        db.freeConnection(con, pstmt, rs);
                } catch (Exception exception) {

                } finally {
                    db = null;
                }
            }
        } else {
            throw new Exception();
        }
    }

    public void PrintUserInfo() throws Exception {
        DBConnectionMgr db = DBConnectionMgr.getInstance();
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            con = db.getConnection();
            pstmt = con.prepareStatement("SELECT * FROM USERINFO");
            rs = pstmt.executeQuery();
            StringBuffer sb = new StringBuffer();
            sb.append(String.format("%-20s", new Object[] { "UserID" }));
            sb.append(String.format("%-40s", new Object[] { "UserPWD" }));
            sb.append(String.format("%-10s", new Object[] { "UserLevel" }));
            sb.append(String.format("%-20s", new Object[] { "UserMail" }));
            sb.append(String.format("%-10s", new Object[] { "UserLock" }));
            sb.append(String.format("%-20s", new Object[] { "LastLoginIP" }));
            sb.append(String.format("%-20s", new Object[] { "LastLoginDate" }));
            System.out.println(sb.toString());
            System.out.println("-----------------------------------------------------------------------------------------------------------------");
            while (rs.next()) {
                sb.setLength(0);
                sb.append(String.format("%-20s", new Object[] { rs.getString("USERID") }));
                sb.append(String.format("%-40s", new Object[] { rs.getString("PASSWORD") }));
                sb.append(String.format("%-10s", new Object[] { rs.getString("AUTHOR") }));
                sb.append(String.format("%-20s", new Object[] { rs.getString("USERMAIL") }));
                sb.append(String.format("%-10s", new Object[] { rs.getString("IS_ACCOUNT_LOCK") }));
                sb.append(String.format("%-20s", new Object[] { rs.getString("LAST_LOGIN_IP") }));
                sb.append(String.format("%-20s", new Object[] { rs.getString("LAST_LOGIN_DATE") }));
                System.out.println(sb.toString());
            }
            System.out.println("");
            System.out.println("");
            rs.close();
            rs = null;
            pstmt.close();
            pstmt = null;
        } catch (Exception ea) {
            throw ea;
        } finally {
            try {
                if (db != null)
                    db.freeConnection(con, pstmt, rs);
            } catch (Exception exception) {

            } finally {
                db = null;
            }
        }
    }

    public void PrintMainMessage() {
        System.out.println("###########################################################");
        System.out.println(" Theia Console User Info Manager");
        System.out.println("###########################################################");
        System.out.println(" 1) print user info       (사용자 정보를 출력 한다.)");
        System.out.println(" 2) add user info         (사용자 정보를 추가 한다.)");
        System.out.println(" 3) modify user info      (사용자 정보를 수정 한다.)");
        System.out.println(" 4) delete user info      (사용자 정보를 삭제 한다.)");
        System.out.println(" 5) unlock user info      (사용자 계정의 Lock을 해제 한다.)");
        System.out.println(" 6) Encryption password   (평문을 암호화 한다.)");
        System.out.println(" 7) Decryption password   (암호를 복호화 한다.)");
        System.out.println(" 0) exit                  (종료 한다.)");
        System.out.println("");
    }

    public void readUserInfo() throws Exception {
        this.UserLIST.clear();
        DBConnectionMgr db = DBConnectionMgr.getInstance();
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            con = db.getConnection();
            pstmt = con.prepareStatement("SELECT * FROM USERINFO");
            rs = pstmt.executeQuery();
            while (rs.next()) {
                USER user = new USER();
                user.setUSERID(rs.getString("USERID"));
                user.setUSERNAME(rs.getString("USERNAME"));
                user.setPASSWORD(rs.getString("PASSWORD"));
                user.setAUTHOR(rs.getInt("AUTHOR"));
                user.setUSERMAIL(rs.getString("USERMAIL"));
                user.setIS_ACCOUNT_LOCK(rs.getString("IS_ACCOUNT_LOCK"));
                this.UserLIST.add(user);
            }
            System.out.println("");
            System.out.println("");
            rs.close();
            rs = null;
            pstmt.close();
            pstmt = null;
        } catch (Exception ea) {
            throw ea;
        } finally {
            try {
                if (db != null)
                    db.freeConnection(con, pstmt, rs);
            } catch (Exception exception) {

            } finally {
                db = null;
            }
        }
    }

    public class USER {
        String USERID;

        String USERNAME;

        String PASSWORD;

        int AUTHOR;

        String USERMAIL;

        String IS_ACCOUNT_LOCK;

        public String getUSERID() {
            return this.USERID;
        }

        public void setUSERID(String uSERID) {
            this.USERID = uSERID;
        }

        public String getUSERNAME() {
            return this.USERNAME;
        }

        public void setUSERNAME(String uSERNAME) {
            this.USERNAME = uSERNAME;
        }

        public String getPASSWORD() {
            return this.PASSWORD;
        }

        public void setPASSWORD(String pASSWORD) {
            this.PASSWORD = pASSWORD;
        }

        public int getAUTHOR() {
            return this.AUTHOR;
        }

        public void setAUTHOR(int aUTHOR) {
            this.AUTHOR = aUTHOR;
        }

        public String getUSERMAIL() {
            return this.USERMAIL;
        }

        public void setUSERMAIL(String uSERMAIL) {
            this.USERMAIL = uSERMAIL;
        }

        public String getIS_ACCOUNT_LOCK() {
            return this.IS_ACCOUNT_LOCK;
        }

        public void setIS_ACCOUNT_LOCK(String iS_ACCOUNT_LOCK) {
            this.IS_ACCOUNT_LOCK = iS_ACCOUNT_LOCK;
        }
    }
}
